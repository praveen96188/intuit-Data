package com.intuit.sbd.payroll.psp.batchjobs.AssistedUsageReport;

import com.intuit.ems.payroll.psp.gateway.brm.BRMAssistedUsageFileUploader;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.AssistedBillStatus;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class AssistedUsageToBRMDataSyncFileGenerator {
    private static SpcfLogger mLogger = Application.getLogger(AssistedUsageToBRMDataSyncFileGenerator.class);

    public void generate() {
        try {
            StopWatch sw = new StopWatch().start();

            SpcfCalendar todaysDate =  PSPDate.getPSPTime();
            SpcfCalendar startDate = CalendarUtils.getFirstDayOfMonth(todaysDate);
            SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(startDate);
            Application.beginUnitOfWork();
            Set<SpcfUniqueId> assistedUsageBills = AssistedBundleBill.findOpenBillsDuring(startDate, endDate);
            Application.rollbackUnitOfWork();
            genFile(assistedUsageBills);
            sw.stop();
            mLogger.info("generated brm sync file for assisted usage bills"
                    + "duration: " + sw.getElapsedTimeString());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }
    protected void genFile(Set<SpcfUniqueId> assistedUsageBills) {

        if (assistedUsageBills == null || assistedUsageBills.isEmpty()) {
            return;
        }
        boolean fileGenSuccess = true;
        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * 2;
        ExecutorService threadPool = null;
        try {
            threadPool = Executors.newFixedThreadPool(threadCount);
            CompletionService<String> completionService = new ExecutorCompletionService<String>(threadPool);
            for (SpcfUniqueId entry : assistedUsageBills) {
                completionService.submit(new AssistedUsageToBRMDataSyncFileGenerator.TransactionGens(entry));
            }
            String filename = BRMAssistedUsageFileUploader.LOCAL_WORK_DIR + BRMAssistedUsageFileUploader.DAP_FILENAME_PATTERN;
            filename = filename.replace("[timestamp]", PSPDate.getPSPTime().toLocal().format("yyyyMMddHHmmss"));
            OutputStreamWriter fileWriter = null;
            try {

                fileWriter = new FileWriter(filename);
                fileWriter.write(BRMAssistedUsageFileUploader.ASST_BRM_FILE_HEADER);
                for (int i = 0; i < assistedUsageBills.size(); i++) {
                    String result = "";
                    try {
                        Future<String> f = completionService.take();
                        result = f.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable t) {
                        // should not get runtime error here, but in case of the unexpected errors, swallow it and let other threads go
                        mLogger.error("failed to sync assisted usages bills to BRM", t);
                    }
                    if (!"".equals(result)) {
                        fileWriter.write(result);
                        fileWriter.flush();
                    }
                }
            } catch (IOException e) {
                fileGenSuccess = false;
                mLogger.error("Can not proceed. IO exception of usage report file", e);
            } finally {
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        fileGenSuccess = false;
                        mLogger.error("failed to close BRM assisted file", e);
                    }
                }
            }
            if (!fileGenSuccess) {
                File outputFile = new File(filename);

                try {
                    outputFile.delete();
                } catch (Throwable e) {
                }
                throw new RuntimeException("BRM assisted file is not generated due to IOException. Please check file system.");
            }

        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool);
            }
        }

    }

    protected class TransactionGens implements Callable<String> {

        private SpcfUniqueId mBillId;
        private AssistedBundleBill mAsstBill;

        public TransactionGens(SpcfUniqueId billId) {
            mBillId = billId;
        }

        public String call() throws Exception {

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AssistedUsageReportProcess));
            StringBuffer transaction = new StringBuffer();
            AssistedBillStatus billStatus = AssistedBillStatus.Processed;
            try {
                Application.beginUnitOfWork();
                mAsstBill = Application.findById(AssistedBundleBill.class, mBillId);
                //File Format: Groupid,SiteGeneratorPortalproduct,EventCode,Quantity,Amount,EventId,Timestamp
                if (mAsstBill.getTotalCount() > 0) {
                    String licenseId = mAsstBill.getAsstBundleCompUsage().getLicenseId();
                    String eoc = mAsstBill.getAsstBundleCompUsage().getEntitlementId();

                    transaction.append(licenseId).append(',');
                    transaction.append(eoc).append(',');
                    transaction.append("DiamondAssitedPayroll,");
                    transaction.append(mAsstBill.getTotalCount()).append(',');
                    transaction.append(mAsstBill.getTotalAmount()).append(',');
                    transaction.append(mAsstBill.getId()).append(',');

                    SpcfCalendar transactionDate = mAsstBill.getBillDate();
                    transactionDate.setValues(transactionDate.getYear(), transactionDate.getMonth(), transactionDate.getDay(), 23, 59, 59, 999);

                    transaction.append(CalendarUtils.convertCalendarToXmlStringNoMilliSeconds(transactionDate)).append("\n");
                    billStatus = AssistedBillStatus.Processed;
                }
                Application.commitUnitOfWork();
            } catch (Throwable e) {
                transaction = new StringBuffer();
                mLogger.error("failed to the generate file for Bill id: " + mBillId, e);
                billStatus = AssistedBillStatus.ProcessingFailed;
            } finally {
                Application.rollbackUnitOfWork();
            }
            Application.beginUnitOfWork();
            mAsstBill = Application.findById(AssistedBundleBill.class, mBillId);
            mAsstBill.setAsstStatus(billStatus);
            Application.commitUnitOfWork();
            return transaction.toString();
        }
    }
}

