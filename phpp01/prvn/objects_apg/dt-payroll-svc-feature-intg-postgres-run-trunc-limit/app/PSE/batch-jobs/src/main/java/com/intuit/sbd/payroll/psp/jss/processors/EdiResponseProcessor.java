package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 24, 2011
 * Time: 11:14:02 AM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "EdiResponse", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EdiResponseProcessor extends JSSBatchJob {
   
    public EdiResponseProcessor(String[] pArguments) {
        super(pArguments);
    }
    public EdiResponseProcessor(String[] pArguments, String pJobId) {
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

        executeStep( PreProcessWaitingResponseFiles.class);
        executeStep( ProcessWaitingResponseFiles.class);
        executeStep( TransmitAS400Files.class);
        executeStep( ArchiveProcessedFiles.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessWaitingResponseFiles extends JSSBatchJobStep<EdiResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.processWaitingEDIResponseFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step processWaitingResponseFiles ", t);
            }
        }
    }

    public static class PreProcessWaitingResponseFiles extends JSSBatchJobStep<EdiResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.preProcessWaitingEDIResponseFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step preProcessWaitingResponseFiles ", t);
            }
        }
    }

    public static class TransmitAS400Files extends JSSBatchJobStep<EdiResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.transmitAS400StateEDIFiles(); 
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step TransferAS400Files ", t);
            }
        }
    }

    public static class ArchiveProcessedFiles extends JSSBatchJobStep<EdiResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.archiveEDIFiles();

                //Clean AS400 files from AS400 directory, if any files are left after processing.
                EftpsUtil.cleanDirectory(EftpsUtil.getEdiAS400Dir());

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveProcessedFiles ", t);
            }
        }
    }

}
