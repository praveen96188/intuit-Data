package com.intuit.sbd.payroll.psp.batchjobs.amo;

import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.SyncCustomerAssetDataAreaType;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementMessageDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessageStatusCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.gateways.amo.AMODTO;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.amo.AbstractAMOGateway;
import com.intuit.sbd.payroll.psp.gateways.amo.SyncCustomerAssetDataAreaTypeDTO;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 6, 2010
 * Time: 4:18:34 PM
 */
public class ProcessNewAMOMessages {
    private static SpcfLogger logger = Application.getLogger(ProcessNewAMOMessages.class);
    private static JAXBContext jaxbContext;

    private int interval;
    private int minPoolSize;
    private int maxPoolSize;
    private int maxWait;
    private int batchSize;
    private int incrementalBatchSize;
    private long batchToken;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(SyncCustomerAssetDataAreaType.class);
        } catch (JAXBException e) {
            logger.error("Failed to create a jaxb context.", e);
        }
    }

    public ProcessNewAMOMessages() {
        interval = SystemParameter.findIntValue(SystemParameter.Code.AMO_THREAD_POOL_INTERVAL, 60);
        maxWait = SystemParameter.findIntValue(SystemParameter.Code.AMO_THREAD_POOL_MAX_WAIT, 5 * 60);
        minPoolSize = SystemParameter.findIntValue(SystemParameter.Code.AMO_MIN_THREAD_POOL_SIZE, 10);
        maxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.AMO_MAX_THREAD_POOL_SIZE, 40);
        batchSize = SystemParameter.findIntValue(SystemParameter.Code.AMO_BATCH_SIZE, 500);
        incrementalBatchSize = SystemParameter.findIntValue(SystemParameter.Code.AMO_INCREMENTAL_BATCH_SIZE, 50);
        batchToken = SystemParameter.findLongValue(SystemParameter.Code.AMO_BATCH_TOKEN, -1);
        if(batchToken == -1) {
            throw new RuntimeException("Error finding batch token.");
        }
    }

    public void processNewAMOMessages() throws S3UploadException, S3ConnectionException {
        logger.info("Process new amo messages started");

        // we cannot marshall or unmarshall messages if context was not initialized
        if(jaxbContext == null) {
            throw new RuntimeException("Cannot process messages because jaxb context is null");
        }

        StopWatch stopWatch = new StopWatch().start();

        AbstractAMOGateway amoGateway = AMOGatewayFactory.createInstance();
        if(amoGateway == null) {
            return;
        }

        int messageGroupCount = 0;
        int messageCount = 0;
        for (int i = 0; i < batchSize; i += incrementalBatchSize){
            Collection<AMODTO> amodtos = amoGateway.getMessages(incrementalBatchSize);

            // there are no messages on the queue so stop processing
            if(amodtos.size() == 0) {
                break;
            }

            // Process the messages
            logger.info("Number of messages groups: " + amodtos.size());

            try {
                messageCount += multithreadMessageProcessing(stopWatch, amodtos);
            } catch (DatabaseFailureException e) {
                logger.error("Database failure encountered while processing amo messages.", e);
                amoGateway.writeMessagesToFiles(amodtos);
                break;
            }

            messageGroupCount += amodtos.size();
        }

        // All groups were processed
        logger.info("completed processing " + messageGroupCount + " message groups and " + messageCount + " messages in " + stopWatch.getElapsedTimeString());
    }

    private int multithreadMessageProcessing(StopWatch stopWatch, Collection<AMODTO> pAMODTOs) throws DatabaseFailureException {
        ExecutorService threadPool = null;
        try {
            // Create threadPool with given parameters
            threadPool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, interval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            // Process each dto in a separate thread
            int numberOfEntitlementsProcessed = 0;
            for (AMODTO amodto : pAMODTOs) {
                numberOfEntitlementsProcessed++;
                final AMODTO finalAMODTO = amodto;
                completionService.submit(new Callable<Integer>() {
                    public Integer call() {
                        return processDTOForEntitlement(finalAMODTO);
                    }
                });
            }

            // Get the results of each thread execution
            int messageCount = 0;
            try {
                for (int t = 0; t < numberOfEntitlementsProcessed; t++) {
                    Future<Integer> f = completionService.take();
                    messageCount += f.get();

                    if (messageCount > 0 && messageCount % incrementalBatchSize == 0) {
                        logger.info("working -- completed processing " + messageCount + " messages in " + stopWatch.getElapsedTimeString());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw ThreadingUtils.launderThrowable(e.getCause());
            }

            return messageCount;
        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, interval, maxWait);
            }
        }
    }

    /**
     * Hibernate exceptions are handled specially, logging the entire AMO message if one is encountered.
     * This is done because once we receive the message from the message queue we acknowledge the message
     * and cannot receive it from the queue again.
     * @param pAMODTO - messages from AMO
     * @return - number of messages processed
     */
    private int processDTOForEntitlement(AMODTO pAMODTO) {
        int messageCount = 0;
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AMOBatchJob));
        Marshaller marshaller = createMarshaller();
        try {
            if(pAMODTO.getLicenseNumber() != null) {
                saveEntitlementMessages(pAMODTO, marshaller, false);
            } else {
                pAMODTO.setLicenseNumber(pAMODTO.getSourceLicenseNumber());
                saveEntitlementMessages(pAMODTO, marshaller, false);
            }
        } catch (HibernateException e) {
            generateSaveErrorMessage("Hibernate exception encountered.", pAMODTO, e);
            throw new DatabaseFailureException("Error saving amo message.");
        } finally {
            Application.rollbackUnitOfWork();
        }

        return messageCount;
    }

    private void saveProcessedEntitlementMessages(AMODTO pAMODTO, Marshaller marshaller) {
        for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
            syncCustomerAssetDataAreaTypeDTO.setEntitlementMessageStatusCode(EntitlementMessageStatusCode.Processed);
            saveEntitlementMessage(pAMODTO, marshaller, syncCustomerAssetDataAreaTypeDTO);
        }
    }

    private void saveEntitlementMessages(AMODTO pAMODTO, Marshaller marshaller, boolean pErrorProcessing) {
        for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
            if(pErrorProcessing) {
                syncCustomerAssetDataAreaTypeDTO.setEntitlementMessageStatusCode(null);
            }
            saveEntitlementMessage(pAMODTO, marshaller, syncCustomerAssetDataAreaTypeDTO);
        }
    }

    private Marshaller createMarshaller() {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            return marshaller;
        } catch (JAXBException e) {
            // if this exception happens we will lose the messages
            logger.error("Failed to create a marshaller.", e);
        }

        return null;
    }

    /**
     * Database commit exceptions are handled specially, logging the entire AMO message if one is encountered.
     * This is done because once we receive the message from the message queue we acknowledge the message
     * and cannot receive it from the queue again.     
     */
    private void saveEntitlementMessage(AMODTO pAMODTO, Marshaller pMarshaller, SyncCustomerAssetDataAreaTypeDTO pSyncCustomerAssetDataAreaTypeDTO) {
        if(pMarshaller == null) {
            // cannot save the message without a marshaller
            return;
        }

        Application.beginUnitOfWork(FlushMode.MANUAL);
        EntitlementMessageDTO entitlementMessageDTO = new EntitlementMessageDTO();
        entitlementMessageDTO.setOrderNumber(pSyncCustomerAssetDataAreaTypeDTO.getOrderNumber(pAMODTO.getLicenseNumber(), pAMODTO.getEntitlementOfferingCode()));
        entitlementMessageDTO.setLicenseNumber(pAMODTO.getLicenseNumber());
        entitlementMessageDTO.setEntitlementOfferingCode(pAMODTO.getEntitlementOfferingCode());
        entitlementMessageDTO.setToken(batchToken);
        entitlementMessageDTO.setEventReason(pSyncCustomerAssetDataAreaTypeDTO.getEventReason());
        entitlementMessageDTO.setMessageTimestamp(pSyncCustomerAssetDataAreaTypeDTO.getTransactionDatetime());

        if(pSyncCustomerAssetDataAreaTypeDTO.getEntitlementMessageStatusCode() != null) {
            entitlementMessageDTO.setEntitlementMessageStatusCode(pSyncCustomerAssetDataAreaTypeDTO.getEntitlementMessageStatusCode());
        }

        StringWriter stringWriter = new StringWriter();
        try {
            if(pSyncCustomerAssetDataAreaTypeDTO.getMessage() == null) {
                pMarshaller.marshal(pSyncCustomerAssetDataAreaTypeDTO.getSyncCustomerAssetDataAreaType(), stringWriter);
                entitlementMessageDTO.setMessage(stringWriter.toString());
            } else {
                entitlementMessageDTO.setMessage(pSyncCustomerAssetDataAreaTypeDTO.getMessage());
            }

        } catch (JAXBException e) {
            // if this exception happens we will lose the message
            logger.error("Failed to marshal message.", e);
        }

        ProcessResult processResult = PayrollServices.entitlementManager.addEntitlementMessage(entitlementMessageDTO);
        if(processResult.isSuccess()) {
            try {
                Application.commitUnitOfWork();
            } catch (Throwable t) {
                // the db commit failed email the message for a manual work around
                generateSaveErrorMessage("", entitlementMessageDTO, t);
                // throw new database exception to notify threads that there is something wrong with the db
                throw new DatabaseFailureException("Error saving amo message.");
            }
        }
        else {
            generateSaveErrorMessage(processResult.toString(), entitlementMessageDTO, null);
            Application.rollbackUnitOfWork();
        }
    }

    private void generateSaveErrorMessage(String errorMessage, EntitlementMessageDTO pEntitlementMessageDTO, Throwable pThrowable) {
        generateSaveErrorMessage(errorMessage, pEntitlementMessageDTO.getLicenseNumber(), pEntitlementMessageDTO.getEntitlementOfferingCode(), pEntitlementMessageDTO.getMessage(), pThrowable);
    }

    private void generateSaveErrorMessage(String errorMessage, AMODTO pAMODTO, Throwable pThrowable) {
        Marshaller marshaller = createMarshaller();
        for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
            try {
                StringWriter stringWriter = new StringWriter();
                marshaller.marshal(syncCustomerAssetDataAreaTypeDTO.getSyncCustomerAssetDataAreaType(), stringWriter);
                generateSaveErrorMessage(errorMessage, pAMODTO.getLicenseNumber(), pAMODTO.getEntitlementOfferingCode(), stringWriter.toString(), pThrowable);
            } catch (JAXBException e) {
                // if this exception happens we will lose the message
                logger.error("Failed to marshal message.", e);
            }
        }
    }

    private void generateSaveErrorMessage(String errorMessage, String pLicenseNumber, String pEOC, String pMessage, Throwable pThrowable) {
        // send email for manual work around
        errorMessage = "Failed to save entitlement message for license number: " +
                pLicenseNumber + " EOC: " + pEOC +
                ". "  + "\n" + errorMessage + "\n" + pMessage;
        if(pThrowable == null) {
            logger.error(errorMessage);
        } else {
            logger.error(errorMessage, pThrowable);
        }
    }


}
