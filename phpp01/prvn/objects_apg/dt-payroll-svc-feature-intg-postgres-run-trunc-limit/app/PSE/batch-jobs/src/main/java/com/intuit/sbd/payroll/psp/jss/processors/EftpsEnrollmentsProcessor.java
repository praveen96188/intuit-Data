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

@ScheduledJob(name = "EftpsEnrollments", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EftpsEnrollmentsProcessor extends JSSBatchJob {

    public EftpsEnrollmentsProcessor(String[] pArguments) {
        super(pArguments);
    }
    public EftpsEnrollmentsProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {

        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            getLogger().warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        getLogger().info("Starting Eftps process enrollments job");


        StopWatch timer = StopWatch.startTimer();

        executeStep(ProcessEftpsEnrollments.class);

        getLogger().info("Completed Eftps process enrollments batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public static class ProcessEftpsEnrollments extends JSSBatchJobStep<EftpsEnrollmentsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsEnrollmentBatchJob);

                EdiManager.processEnrollments();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessEftpsEnrollments ", t);
            }
        }
    }
}
