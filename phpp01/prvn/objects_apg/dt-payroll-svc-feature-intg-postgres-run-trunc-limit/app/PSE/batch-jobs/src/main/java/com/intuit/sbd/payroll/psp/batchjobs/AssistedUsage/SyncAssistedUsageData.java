package com.intuit.sbd.payroll.psp.batchjobs.AssistedUsage;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.*;

public class SyncAssistedUsageData {
    private static SpcfLogger logger = Application.getLogger(com.intuit.sbd.payroll.psp.batchjobs.AssistedUsage.SyncAssistedUsageData.class);

    public void sync() {
        try {
            StopWatch sw = new StopWatch().start();

            // get last token and the events to process
            HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> payrollRunsToProcess = new HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>>();
            long newToken = AssistedUsagePayrollRunHelper.findNextSetPayrollRunsToProcess(payrollRunsToProcess);

            // process
            if (payrollRunsToProcess.size() > 0) {
                multithreadProcessing(payrollRunsToProcess, true);
                updateSyncToken(newToken);
            }
            sw.stop();
            logger.info(String.format("completed processing - end token: %d + events: %d + duration: %s",
                    newToken, payrollRunsToProcess.values().size(), sw.getElapsedTimeString()));
        } catch (Throwable t) {
            logger.error("failed to sync PSP to Assisted Usage Data", t);
        }
    }

    protected void updateSyncToken(Long newToken) throws Exception {
        try {
            Application.beginUnitOfWork();
            ProcessResult pr = PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.ASSISTED_USAGE_BILLING_TOKEN, newToken.toString());
            if (!pr.isSuccess()) {
                logger.error("failed to write new token (" + newToken + ") to PSP_SYSTEM_PARAMETER under key: " + SystemParameter.Code.ASSISTED_USAGE_BILLING_TOKEN);
            }
            Application.commitUnitOfWork();
        } catch (Throwable t) {
            logger.error("failed to write new token (" + newToken + ") to PSP_SYSTEM_PARAMETER under key: " + SystemParameter.Code.ASSISTED_USAGE_BILLING_TOKEN);
            throw new RuntimeException(t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    protected HashSet<SpcfUniqueId> multithreadProcessing(HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> pCompanyAndPayRunIds, boolean needSaveFailuresToDB) throws Exception {
        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * 2;
        ExecutorService threadPool = null;
        HashSet<SpcfUniqueId> results = new HashSet<SpcfUniqueId>();
        try {
            threadPool = Executors.newFixedThreadPool(threadCount);
            CompletionService<HashSet<SpcfUniqueId>> completionService = new ExecutorCompletionService<HashSet<SpcfUniqueId>>(threadPool);

            for (SpcfUniqueId companyId : pCompanyAndPayRunIds.keySet()) {
                completionService.submit(new com.intuit.sbd.payroll.psp.batchjobs.AssistedUsage.AssistedUsageSyncCoreProcessor(pCompanyAndPayRunIds.get(companyId)));
            }

            for (int i = 0; i < pCompanyAndPayRunIds.size(); i++) {
                try {
                    Future<HashSet<SpcfUniqueId>> f = completionService.take();
                    results.addAll(f.get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool);
            }
        }

        return results;
    }
}
