package com.intuit.sbd.payroll.psp.batchjobs.amo;

import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.SyncCustomerAssetDataAreaType;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessage;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessageStatusCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.gateways.amo.AMODTO;
import com.intuit.sbd.payroll.psp.gateways.amo.SyncCustomerAssetDataAreaTypeDTO;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 6, 2010
 * Time: 4:18:18 PM
 */
public class ProcessSavedAMOMessages {
    private static SpcfLogger logger = Application.getLogger(ProcessSavedAMOMessages.class);
    private static JAXBContext jaxbContext;

    private int interval;
    private int minPoolSize;
    private int maxPoolSize;
    private int maxWait;
    private int batchSize;
    private long batchToken;
    private long nextBatchToken;

    private PSPRequestContextManager pspRequestContextManager;

    private static final String ENTITLEMENT_DISABLEMENT = "ENTITLEMENT_DISABLEMENT";
    private static final String ENTITLEMENT_CREATION = "ENTITLEMENT_CREATION";
    private static final String ENTITLEMENT_UNIT_ACTIVATION = "ENTITLEMENT_UNIT_ACTIVATION";


    static {
        try {
            jaxbContext = JAXBContext.newInstance(SyncCustomerAssetDataAreaType.class);
        } catch (JAXBException e) {
            logger.error("Failed to create a jaxb context.", e);
        }
    }

    public ProcessSavedAMOMessages() {
        interval = SystemParameter.findIntValue(SystemParameter.Code.AMO_THREAD_POOL_INTERVAL, 60);
        maxWait = SystemParameter.findIntValue(SystemParameter.Code.AMO_THREAD_POOL_MAX_WAIT, 5 * 60);
        minPoolSize = SystemParameter.findIntValue(SystemParameter.Code.AMO_MIN_THREAD_POOL_SIZE, 10);
        maxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.AMO_MAX_THREAD_POOL_SIZE, 40);
        batchSize = SystemParameter.findIntValue(SystemParameter.Code.AMO_BATCH_SIZE, 500);
        batchToken = SystemParameter.findLongValue(SystemParameter.Code.AMO_BATCH_TOKEN, -1);
        if (batchToken == -1) {
            throw new RuntimeException("Error finding batch token.");
        }
        nextBatchToken = batchToken + 1;
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public void processSavedAMOMessages() {
        logger.info("Process saved amo messages started");
        StopWatch stopWatch = new StopWatch().start();

        Unmarshaller unmarshaller = createUnmarshaller();

        ArrayList<String> errorList = new ArrayList<String>();
        boolean moreRecords = true;
        while (moreRecords) {
            PayrollServices.beginUnitOfWork();
            Application.getHibernateSession().setFlushMode(FlushMode.MANUAL);

            // split messages by license and eoc to allow one thread to process all messages for one entitlement
            DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findNewEntitlementMessages(batchSize, batchToken);

            moreRecords = entitlementMessages.size() == batchSize;

            HashMap<String, AMODTO> amodtoHashMap = new HashMap<String, AMODTO>();
            for (EntitlementMessage entitlementMessage : entitlementMessages) {
                // flag this message to be processed in the next batch; if it is not updated to processed
                entitlementMessage.setToken(nextBatchToken);
                Application.save(entitlementMessage);

                String id = entitlementMessage.getLicenseNumber() + (entitlementMessage.getEntitlementOfferingCode() != null ? entitlementMessage.getEntitlementOfferingCode() : "");
                if (errorList.contains(id)) {
                    // skip messages related to entitlements that had errors
                    continue;
                }
                AMODTO amodto;
                if (amodtoHashMap.containsKey(id)) {
                    amodto = amodtoHashMap.get(id);
                } else {
                    amodto = new AMODTO();
                    amodto.setEntitlementOfferingCode(entitlementMessage.getEntitlementOfferingCode());
                    amodto.setLicenseNumber(entitlementMessage.getLicenseNumber());
                    amodtoHashMap.put(id, amodto);
                }
                try {
                    amodto.addMessage(new SyncCustomerAssetDataAreaTypeDTO((SyncCustomerAssetDataAreaType) unmarshaller.unmarshal(
                            new InputSource(new StringReader(entitlementMessage.getMessage()))),
                                                                           entitlementMessage.getId().toString()));
                } catch (Throwable t) {
                    errorList.add(id);
                    amodtoHashMap.remove(id);
                    logger.error("Error processing saved entitlement message.\n" + amodto.toString(), t);
                }
            }
            Application.getHibernateSession().setFlushMode(FlushMode.AUTO);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            Application.getHibernateSession().setFlushMode(FlushMode.MANUAL);
            // Process the messages
            Collection<AMODTO> amodtos = amodtoHashMap.values();
            logger.info("Number of messages groups: " + amodtos.size());
            multithreadMessageProcessing(stopWatch, amodtos);
            Application.getHibernateSession().setFlushMode(FlushMode.AUTO);
            PayrollServices.commitUnitOfWork();

            // All messages were processed
            logger.info("completed processing " + amodtos.size() + " saved message groups in " + stopWatch.getElapsedTimeString());
        }

        // updated the batch token
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.AMO_BATCH_TOKEN, Long.toString(nextBatchToken));
        PayrollServices.commitUnitOfWork();
    }

    private void multithreadMessageProcessing(StopWatch stopWatch, Collection<AMODTO> pAMODTOs) {
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
                        return processSavedEntitlementMessage(finalAMODTO);
                    }
                });
            }

            // Get the results of each thread execution
            int messageCount = 0;
            try {
                for (int t = 0; t < numberOfEntitlementsProcessed; t++) {
                    Future<Integer> f = completionService.take();
                    messageCount += f.get();

                    if (messageCount > 0 && messageCount % 50 == 0) {
                        logger.info("working -- completed processing " + messageCount + " messages in " + stopWatch.getElapsedTimeString());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw ThreadingUtils.launderThrowable(e.getCause());
            }
            logger.info("working -- completed processing " + messageCount + " messages in " + stopWatch.getElapsedTimeString());
        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, interval, maxWait);
            }

        }
    }

    private int processSavedEntitlementMessage(AMODTO pAMODTO) {
        int messageCount = 0;
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AMOBatchJob));

        try {
            pspRequestContextManager.setRequestContext(null, RequestType.OLAP, "AMOMessageProcessor");

            logger.info("Started processSavedEntitlementMessage: " + pAMODTO.getEntitlementOfferingCode());
            if (pAMODTO.getEntitlementOfferingCode() != null) {
                Entitlement entitlement;
                Application.beginUnitOfWork();
                entitlement = Entitlement.findEntitlement(pAMODTO.getLicenseNumber(), pAMODTO.getEntitlementOfferingCode());
                Application.rollbackUnitOfWork();

                boolean isRetail = false;

                // if the entitlement does not exist save the messages for later processing
                if (entitlement == null) {
                    String creationOrderNumber = null;
                    SyncCustomerAssetDataAreaTypeDTO creationMessage = null;
                    for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
                        logger.info("New entitlement for: " + syncCustomerAssetDataAreaTypeDTO.getEventReason());
                        if (ENTITLEMENT_CREATION.equals(syncCustomerAssetDataAreaTypeDTO.getEventReason())) {
                            creationMessage = syncCustomerAssetDataAreaTypeDTO;
                            creationOrderNumber = syncCustomerAssetDataAreaTypeDTO.getOrderNumber(pAMODTO.getLicenseNumber(), pAMODTO.getEntitlementOfferingCode());
                            logger.info("job=AMOMessageProcessor, Action=processSavedEntitlementMessage, Msg=EntitlementCreationMsgReceived, EOC=" + pAMODTO.getEntitlementOfferingCode() + ", OrderNumber=" + creationOrderNumber);
                            break;
                        }
                    }

                    if (creationOrderNumber != null) {
                        String oldEOC = null;

                        PayrollServices.beginUnitOfWork();
                        DomainEntitySet<EntitlementMessage> entitlementMessages =
                                EntitlementMessage.findEntitlementMessages(pAMODTO.getLicenseNumber(), creationOrderNumber);
                        for (EntitlementMessage entitlementMessage : entitlementMessages) {
                            logger.info("Saved entitlement message: " + entitlementMessage.getEventReason());
                            if (ENTITLEMENT_DISABLEMENT.equals(entitlementMessage.getEventReason()) &&
                                    EntitlementMessageStatusCode.Processed.equals(entitlementMessage.getStatus())) {
                                oldEOC = entitlementMessage.getEntitlementOfferingCode();
                                logger.info("job=AMOMessageProcessor, Action=processSavedEntitlementMessage, Msg=EntitlementDisablementMsgReceivedBefore, EOC=" + pAMODTO.getEntitlementOfferingCode() + ", oldEOC=" + oldEOC + ", OrderNumber=" + creationOrderNumber);
                            }
                        }
                        PayrollServices.rollbackUnitOfWork();

                        if (oldEOC != null) {
                            //Add Entitlement and move existing EntitlementUnits over to newly added Entitlement
                            Application.beginUnitOfWork();

                            EntitlementDTO entitlementDTO = new EntitlementDTO();
                            entitlementDTO.setLicenseNumber(pAMODTO.getLicenseNumber());
                            entitlementDTO.setEntitlementOfferingCode(pAMODTO.getEntitlementOfferingCode());
                            entitlementDTO.setOrderNumber(creationOrderNumber);

                            AMOMessageProcessing.processAssetUpdates(entitlementDTO, creationMessage);

                            Entitlement oldEntitlement = Entitlement.findEntitlement(pAMODTO.getLicenseNumber(), oldEOC);

                            ProcessResult<Entitlement> processResult = PayrollServices.entitlementManager.migrateEntitlement(oldEntitlement, entitlementDTO);
                            if (!processResult.isSuccess()) {
                                Application.rollbackUnitOfWork();
                                String errorMessage = processResult.toString();

                                Application.beginUnitOfWork();
                                PayrollServices.entitlementManager.updateEntitlementMessage(
                                        creationMessage.getEntitlementMessageId(),
                                        pAMODTO.getLicenseNumber(),
                                        null,
                                        errorMessage);
                                Application.commitUnitOfWork();

                                logger.error("Error processing saved entitlement message.\n" + pAMODTO.toString() + "\n" + errorMessage);
                                return 0;
                            }
                            entitlement = processResult.getResult();

                            if (entitlement != null) {
                                // skip all of the messages for now, and process them with any skipped messages that are now new
                                for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
                                    EntitlementMessage entitlementMessage;
                                    if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_HIBERNATE_LICENSE_NUMBER_FILTER)) {
                                        entitlementMessage = EntitlementMessage.findEntitlementMessagesByIdAndLicenseNumber(
                                                syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId(), pAMODTO.getLicenseNumber());
                                    } else {
                                        entitlementMessage = Application.findById(EntitlementMessage.class,
                                                SpcfUniqueId.createInstance(syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId()));
                                    }
                                    entitlementMessage.setToken(EntitlementMessage.PROCESS_WITH_NEXT_BATCH_TOKEN);
                                    Application.save(entitlementMessage);
                                }
                                pAMODTO.getMessages().clear();
                            }
                            Application.commitUnitOfWork();
                        }
                    }
                } else {
                    logger.info("Entitlement is not null. The number of message: " + pAMODTO.getMessages().size());
                    for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
                        if (ENTITLEMENT_UNIT_ACTIVATION.equals(syncCustomerAssetDataAreaTypeDTO.getEventReason())) {
                            isRetail = true;
                        }
                        logger.info("Entitlement message for existing entitlement: " + entitlement + " : " +
                                            syncCustomerAssetDataAreaTypeDTO.getEventReason());
                    }
                }

                // if the entitlement is still null, mark the message as skipped
                if (entitlement == null) {
                    // skip the message
                    Application.beginUnitOfWork();
                    SpcfCalendar currentTime = PSPDate.getPSPTime();
                    for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
                        EntitlementMessage entitlementMessage;
                        if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_HIBERNATE_LICENSE_NUMBER_FILTER)) {
                            entitlementMessage = EntitlementMessage.findEntitlementMessagesByIdAndLicenseNumber(
                                    syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId(), pAMODTO.getLicenseNumber());
                        } else {
                            entitlementMessage = Application.findById(EntitlementMessage.class,
                                    SpcfUniqueId.createInstance(syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId()));
                        }
                        if (entitlementMessage.getExpirationTimestamp() != null && currentTime.after(entitlementMessage.getExpirationTimestamp())) {
                            entitlementMessage.setStatus(EntitlementMessageStatusCode.SkippedEntitlementNotFound);
                        }
                    }
                    Application.commitUnitOfWork();
                    return 0;
                }

                Application.beginUnitOfWork();

                entitlement = Application.findById(Entitlement.class, entitlement.getId());
                EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlement);

                // process the updates with the same dto
                for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
                    AMOMessageProcessing.processAssetUpdates(entitlementDTO, syncCustomerAssetDataAreaTypeDTO);
                    messageCount++;
                }

                if (logger.isInfoEnabled()) {
                    logger.info("Retail flag after asset updates" + entitlementDTO.getLicenseNumber() + ":" +
                                        entitlementDTO.isRetail());
                    logger.info("Subscription start date for " + entitlementDTO.getLicenseNumber() + " : " +
                                        entitlementDTO.getSubscriptionStartDate());
                    logger.info("Creation date for " + entitlement.getLicenseNumber() + " : " +
                                        entitlement.getCreatedDate());
                }


                if (isRetail && entitlementDTO.getSubscriptionStartDate() != null && entitlement.getCreatedDate() != null) {
                    isRetail = entitlementDTO.getSubscriptionStartDate().getYear() > entitlement.getCreatedDate().getYear() &&
                            entitlementDTO.getSubscriptionStartDate().getDayOfYear() >= entitlement.getCreatedDate().getDayOfYear();
                } else {
                    isRetail = false;
                }

                // Change the value only if the retail flag was not set earlier.
                // DO not update the retail flag if the message does not contain the subscription start date,
                // When it was already set to true by a earlier message
                isRetail = entitlement.getRetail() ? true: isRetail;

                if (logger.isInfoEnabled()) {
                    logger.info("Retail flag for " + entitlementDTO.getLicenseNumber() + ":" + isRetail);
                }
                entitlementDTO.setRetail(isRetail);

                ProcessResult<Entitlement> processResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
                if (logger.isInfoEnabled()) {
                    logger.info("Updated entitlement:" + processResult.isSuccess());
                }
                if (processResult.isSuccess()) {
                    Application.commitUnitOfWork();

                    for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
                        Application.beginUnitOfWork();

                        PayrollServices.entitlementManager.updateEntitlementMessage(
                                syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId(),
                                pAMODTO.getLicenseNumber(),
                                syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageStatusCode(),
                                null);
                        Application.commitUnitOfWork();
                    }
                } else {
                    Application.rollbackUnitOfWork();
                    String errorMessage = processResult.toString();
                    for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
                        Application.beginUnitOfWork();
                        PayrollServices.entitlementManager.updateEntitlementMessage(
                                syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId(),
                                pAMODTO.getLicenseNumber(),
                                null,
                                errorMessage);
                        Application.commitUnitOfWork();
                    }
                    logger.error("Error processing saved entitlement message.\n" + pAMODTO.toString() + "\n" + errorMessage);
                }
            } else {
                for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
                    for (AssetType assetType : syncCustomerAssetDataAreaTypeDTO.getSyncCustomerAssetDataAreaType().getSyncCustomerAsset().getAsset()) {

                        AssetType.EntitlementTransfer entitlementTransfer = assetType.getEntitlementTransfer();
                        if (entitlementTransfer == null) {
                            continue;
                        }

                        String sourceLicenseNumber = null;
                        if (entitlementTransfer.getSource() != null && entitlementTransfer.getSource().getLicenseId() != null) {
                            sourceLicenseNumber = entitlementTransfer.getSource().getLicenseId().getValue();
                        }

                        String destinationLicenseNumber = null;
                        if (entitlementTransfer.getTarget() != null && entitlementTransfer.getTarget().getLicenseId() != null) {
                            destinationLicenseNumber = entitlementTransfer.getTarget().getLicenseId().getValue();
                        }

                        Application.beginUnitOfWork();
                        ProcessResult processResult = PayrollServices.entitlementManager.transferEntitlement(sourceLicenseNumber, destinationLicenseNumber);
                        if (processResult.isSuccess()) {
                            Application.commitUnitOfWork();

                            Application.beginUnitOfWork();
                            PayrollServices.entitlementManager.updateEntitlementMessage(
                                    syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId(),
                                    pAMODTO.getLicenseNumber(),
                                    syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageStatusCode(),
                                    null);
                            Application.commitUnitOfWork();

                            messageCount++;
                        } else {
                            Application.rollbackUnitOfWork();

                            String errorMessage = processResult.toString();
                            Application.beginUnitOfWork();
                            PayrollServices.entitlementManager.updateEntitlementMessage(
                                    syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId(),
                                    pAMODTO.getLicenseNumber(),
                                    null,
                                    errorMessage);
                            Application.commitUnitOfWork();
                            logger.error("Error processing entitlement transfer amo message.\n" + pAMODTO.toString() + "\n" + errorMessage);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            PayrollServices.rollbackUnitOfWork();

            logger.error("Error processing saved entitlement message.\n" + pAMODTO.toString(), t);
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            t.printStackTrace(printWriter);
            for (SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO : pAMODTO.getMessages()) {
                Application.beginUnitOfWork();
                PayrollServices.entitlementManager.updateEntitlementMessage(
                        syncCustomerAssetDataAreaTypeDTO.getEntitlementMessageId(),
                        pAMODTO.getLicenseNumber(),
                        null,
                        stringWriter.toString());
                Application.commitUnitOfWork();
            }
        } finally {
            Application.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }

        return messageCount;
    }

    public static Unmarshaller createUnmarshaller() {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            logger.error("Failed to create a unmarshaller.", e);
        }

        return null;
    }
}
