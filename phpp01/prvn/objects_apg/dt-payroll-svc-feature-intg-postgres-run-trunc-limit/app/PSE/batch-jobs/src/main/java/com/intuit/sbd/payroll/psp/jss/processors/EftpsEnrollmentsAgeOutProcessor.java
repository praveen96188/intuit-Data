package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: RVL
 * Date: 4/25/17
 * Time: 9:30 AM
 */

@ScheduledJob(name = "EftpsEnrollmentsAgeOut", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EftpsEnrollmentsAgeOutProcessor extends JSSBatchJob {

    public EftpsEnrollmentsAgeOutProcessor(String[] pArguments) {
        super(pArguments);
    }
    public EftpsEnrollmentsAgeOutProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {

        if (BatchUtils.isWeekendOrHoliday()) {
            getLogger().warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }

        getLogger().info("Starting Eftps enrollments age out job");


        StopWatch timer = StopWatch.startTimer();

        executeStep(ProcessEftpsEnrollmentsAgeOut.class);

        getLogger().info("Completed Eftps enrollments age out batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public static class ProcessEftpsEnrollmentsAgeOut extends JSSBatchJobStep<EftpsEnrollmentsAgeOutProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsAgeOutBatchJob);

                EdiManager.ageOutEnrollments();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessEftpsEnrollmentsAgeOut ", t);
            }
        }
    }
}
