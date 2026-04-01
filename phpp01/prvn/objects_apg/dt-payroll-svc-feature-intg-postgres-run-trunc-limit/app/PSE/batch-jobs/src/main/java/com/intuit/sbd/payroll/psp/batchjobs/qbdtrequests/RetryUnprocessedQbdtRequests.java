package com.intuit.sbd.payroll.psp.batchjobs.qbdtrequests;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedRequestProcessor;
import com.intuit.sbd.payroll.psp.adapters.qbdt.CredentialType;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.QbdtRequestStatus;
import com.intuit.sbd.payroll.psp.domain.QbdtUnprocessedRequest;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class RetryUnprocessedQbdtRequests {
    private static SpcfLogger logger = Application.getLogger(RetryUnprocessedQbdtRequests.class);
    private PSPRequestContextManager pspRequestContextManager;

    private int interval;
    private int minPoolSize;
    private int maxPoolSize;
    private int maxWait;
    private int batchSize;

    public RetryUnprocessedQbdtRequests() {
        interval = SystemParameter.findIntValue(SystemParameter.Code.RETRY_OFX_THREAD_POOL_INTERVAL, 60);
        maxWait = SystemParameter.findIntValue(SystemParameter.Code.RETRY_OFX_THREAD_POOL_MAX_WAIT, 5 * 60);
        minPoolSize = SystemParameter.findIntValue(SystemParameter.Code.RETRY_OFX_MIN_THREAD_POOL_SIZE, 10);
        maxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.RETRY_OFX_MAX_THREAD_POOL_SIZE, 40);

        batchSize = 500;
        try {
            SystemParameter systemParameter = SystemParameter.findSystemParameter("RETRY_OFX_THREAD_BATCH_SIZE");
            if (systemParameter != null) {
                batchSize = Integer.parseInt(systemParameter.getSystemParameterValue());
            }
        } catch (Throwable t) {
        }

        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public void retryUnprocessedRequests() {
        if (!SystemParameter.findBooleanValue(SystemParameter.Code.PROCESS_ASSISTED_REQUESTS, false)) {
            logger.info("Bypassing retry OFX batch process because PROCESS_ASSISTED_REQUESTS is false");
            return;
        }

        logger.info("Retry OFX Process Started");
        StopWatch stopWatch = new StopWatch().start();

        SpcfCalendar processStartTime = PSPDate.getPSPTime();
        logger.debug("After PSPDate.getPSPTime()");

        DomainEntitySet<QbdtUnprocessedRequest> unprocessedRequests = QbdtUnprocessedRequest.findUnprocessedRequests(batchSize, QbdtRequestStatus.Queued);

        // We need to process all requests for a given company on the same thread, so we first group them
        Map<Company, List<QbdtUnprocessedRequest>> unprocessedRequestsPerCompany = new HashMap<Company, List<QbdtUnprocessedRequest>>();
        for (int i = 0; i < unprocessedRequests.size(); i++) {
            QbdtUnprocessedRequest unprocessedRequest = unprocessedRequests.get(i);

            List<QbdtUnprocessedRequest> unprocessedRequestsForACompany = unprocessedRequestsPerCompany.get(unprocessedRequest.getCompany());
            if (unprocessedRequestsForACompany == null) {
                unprocessedRequestsForACompany = new ArrayList<QbdtUnprocessedRequest>();
                unprocessedRequestsPerCompany.put(unprocessedRequest.getCompany(), unprocessedRequestsForACompany);
            }

            unprocessedRequestsForACompany.add(unprocessedRequest);
        }

        int unprocessedCompanyCount = unprocessedRequestsPerCompany.keySet().size();
        // Process the pending requests
        logger.info("Number of companies with unprocessed requests: " + unprocessedCompanyCount + " - total unprocessed requests in run: " + unprocessedRequests.size());
        int numberOfRequestsProcessedSuccesfully = multithreadUnprocessedRequestProcessing(stopWatch, unprocessedRequestsPerCompany);
        logger.info("Number of companies with unprocessed requests: " + unprocessedCompanyCount + " - total unprocessed requests successfully reprocessed: " + numberOfRequestsProcessedSuccesfully);

        // All unprocessed requests were processed
        logger.info("completed processing " + unprocessedCompanyCount + " companies in " + stopWatch.getElapsedTimeString());
        logger.info("Completed business logic for retry ofx batch job");
    }

    private int multithreadUnprocessedRequestProcessing(StopWatch stopWatch, Map<Company, List<QbdtUnprocessedRequest>> pUnprocessedRequestsPerCompany) {
        ExecutorService threadPool = null;
        try {
            // Create threadPool with given parameters
            threadPool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, interval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            // Process each company in a separate thread
            int numberOfProcessedCompanies = 0;
            for (List<QbdtUnprocessedRequest> unprocessedRequestsPerCompany : pUnprocessedRequestsPerCompany.values()) {
                numberOfProcessedCompanies++;
                final List<QbdtUnprocessedRequest> finalUnprocessedRequestsPerCompany = unprocessedRequestsPerCompany;
                completionService.submit(new Callable<Integer>() {
                    public Integer call() {
                        return processUnprocessedRequestsForCompany(finalUnprocessedRequestsPerCompany);
                    }
                });
            }

            // Get the results of each thread execution
            int unprocessedRequestsProcessedCount = 0;
            try {
                for (int t = 0; t < numberOfProcessedCompanies; t++) {
                    Future<Integer> f = completionService.take();
                    unprocessedRequestsProcessedCount += f.get();

                    if (unprocessedRequestsProcessedCount % 40 == 0) {
                        logger.info("working -- completed processing " + (numberOfProcessedCompanies + 1) + " companies (" + unprocessedRequestsProcessedCount + " total unprocessed requests) for retry ofx batch job in " + stopWatch.getElapsedTimeString());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw ThreadingUtils.launderThrowable(e.getCause());
            }
            return unprocessedRequestsProcessedCount;
        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, interval, maxWait);
            }

        }
    }

    private int processUnprocessedRequestsForCompany(List<QbdtUnprocessedRequest> pUnprocessedRequests) {
        int unprocessedRequestProcessedCount = 0;
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.RetryUnprocessedQbdtRqBatchJob));
        for (QbdtUnprocessedRequest unprocessedRequest : pUnprocessedRequests) {
            try {
                pspRequestContextManager.setRequestContext(unprocessedRequest.getCompany(), RequestType.OLAP, "QbdtUnprocessedRequestsRetry");
                // We pass the created date as the PSPDate. That will make all records created by this process to be approximately the date
                // the request was submitted
                AssistedRequestProcessor assistedRequestProcessor = new AssistedRequestProcessor();
                assistedRequestProcessor.setIsRetry(true);

                PayrollServices.beginUnitOfWorkWithSecondary();
                unprocessedRequest = Application.findById(QbdtUnprocessedRequest.class, unprocessedRequest.getId());

                // Try to process the request again

                String psid = unprocessedRequest.getCompany().getSourceCompanyId();
                assistedRequestProcessor.setPSID(psid);
                String requestStr = null;
                OFX ofx = null;
                SourceSystemTransmissionDTO sourceSystemTransmissionDTO = null;
                SourceSystemTransmission sourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(unprocessedRequest.getSourceSystemTransmissionId());
                DomainEntityChangeManager.setDomainEntityChangeModelContext(sourceSystemTransmission.getClass(), sourceSystemTransmission);
                requestStr = sourceSystemTransmission.getRequestDocument();
                ofx = OFXManager.ofxRequestToJava(requestStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
                assistedRequestProcessor.setAppVerion(new OFXAPPVERObject(ofx.getSIGNONMSGSRQV1().getSONRQ().getAPPVER()));
                sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO(sourceSystemTransmission.getType(), requestStr);
                sourceSystemTransmissionDTO.setRequestToken(sourceSystemTransmission.getRequestToken());
                sourceSystemTransmissionDTO.setResponseToken(sourceSystemTransmission.getResponseToken());
                sourceSystemTransmissionDTO.setFromSourceSystem(sourceSystemTransmission.getFromSourceSystem());
                sourceSystemTransmissionDTO.setToSourceSystem(sourceSystemTransmission.getToSourceSystem());
                sourceSystemTransmissionDTO.setDescription(sourceSystemTransmission.getDescription());
                sourceSystemTransmissionDTO.setIPAddress(sourceSystemTransmission.getIPAddress());
                assistedRequestProcessor.setSourceTransmissionId(sourceSystemTransmission.getTransmissionIdentifier());
                sourceSystemTransmissionDTO.setResponseDocument(null);
                assistedRequestProcessor.setSourceSystemTransmissionDTO(sourceSystemTransmissionDTO);

                PayrollServices.rollbackUnitOfWorkWithSecondary();

                String response = assistedRequestProcessor.processAssistedRequest(requestStr, ofx, sourceSystemTransmissionDTO.getTransmissionType(), CredentialType.Pin, sourceSystemTransmission.getIPAddress());
                if (QBOFX.ofxStringContainsErrorSeverity(response)) {
                    throw new RuntimeException("Error retrying request: \n" + response);
                }

                Application.beginUnitOfWork();
                unprocessedRequest = Application.findById(QbdtUnprocessedRequest.class, unprocessedRequest.getId());
                unprocessedRequest.setStatus(QbdtRequestStatus.Processed);
                unprocessedRequest.setErrorMessage(null);
                unprocessedRequestProcessedCount++;
                Application.commitUnitOfWork();
            } catch (Throwable t) {
                PayrollServices.rollbackUnitOfWork();

                Application.beginUnitOfWork();
                unprocessedRequest = Application.findById(QbdtUnprocessedRequest.class, unprocessedRequest.getId());

                unprocessedRequest.setStatus(QbdtRequestStatus.Error);

                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                t.printStackTrace(printWriter);
                String errorMessage = writer.toString();
                if (errorMessage != null && errorMessage.length() > 4000) {
                    errorMessage = errorMessage.substring(0, 4000);
                }
                unprocessedRequest.setErrorMessage(errorMessage);

                Application.commitUnitOfWork();
                logger.error(t.getMessage(), t);
                break;
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
                DomainEntityChangeManager.removeDomainEntityChangeModel();
                PayrollServices.rollbackUnitOfWork();
            }
        }
        return unprocessedRequestProcessedCount;
    }
}
