package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: RVL
 * Date: 4/25/17
 * Time: 9:30 AM
 */

@ScheduledJob(name = "EftpsResponse", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EftpsResponseProcessor extends JSSBatchJob {

    public EftpsResponseProcessor(String[] pArguments) {
        super(pArguments);
    }
    public EftpsResponseProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            getLogger().warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(DownloadFileFromS3.class);
        executeStep(ProcessWaitingResponseFiles.class);
        executeStep(TransmitAS400Files.class);
        executeStep(ArchiveProcessedFiles.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class DownloadFileFromS3 extends JSSBatchJobStep<EftpsResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.downloadFileFromS3();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step DownloadFileFromS3 ", t);
            }
        }
    }

    public static class ProcessWaitingResponseFiles extends JSSBatchJobStep<EftpsResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.processWaitingResponseFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step processWaitingResponseFiles ", t);
            }
        }
    }

    public static class TransmitAS400Files extends JSSBatchJobStep<EftpsResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.transmitAS400Files();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step TransferAS400Files ", t);
            }
        }
    }

    public static class ArchiveProcessedFiles extends JSSBatchJobStep<EftpsResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.archiveFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveProcessedFiles ", t);
            }
        }
    }
}
