package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
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
 * Time: 11:23:02 AM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "EdiSend", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EdiSendProcessor extends  JSSBatchJob {
    
    public EdiSendProcessor(String[] pArguments) {
        super(pArguments);
    }
    public EdiSendProcessor(String[] pArguments, String pJobId) {
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

        executeStep( ProcessPendingTransmissions.class);
        executeStep( ArchiveProcessedFiles.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessPendingTransmissions  extends JSSBatchJobStep<EdiSendProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.processPendingStateEdiTransmissions();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessPendingTransmissions ", t);
            }
        }
    }

    public static class ArchiveProcessedFiles extends JSSBatchJobStep<EdiSendProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EdiFileCommBatchJob);

                EdiManager.archiveEDIFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveProcessedFiles ", t);
            }
        }
    }

}
