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

@ScheduledJob(name = "EftpsSend", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EftpsSendProcessor extends JSSBatchJob {

    public EftpsSendProcessor(String[] pArguments) {
        super(pArguments);
    }
    public EftpsSendProcessor(String[] pArguments, String pJobId) {
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

        executeStep(ProcessAS400Files.class);
        executeStep(ProcessPendingTransmissions.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    /**
     * Will parse and record EftpsPaymentDetail records for AS400 files waiting to be transmitted (i.e. in eftps/AS400
     * directory.  File will be placed in PendingTransmission status.
     */
    public static class ProcessAS400Files extends JSSBatchJobStep<EftpsSendProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.processAS400Files();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessAS400Files ", t);
            }
        }
    }

    public static class ProcessPendingTransmissions extends JSSBatchJobStep<EftpsSendProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.processPendingTransmissions();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessPendingTransmissions ", t);
            }
        }
    }

}
