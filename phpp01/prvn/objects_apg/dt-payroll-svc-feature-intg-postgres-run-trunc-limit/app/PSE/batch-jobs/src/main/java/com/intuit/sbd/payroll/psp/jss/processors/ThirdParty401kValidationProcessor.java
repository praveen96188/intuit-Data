package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kValidationProcess;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: Jeff Jones
 * Flux Workflow to validate 401k data and send emails when it finds problems.
 */
@ScheduledJob(name = "ThirdParty401kValidation", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ThirdParty401kValidationProcessor extends JSSBatchJob{

	 public ThirdParty401kValidationProcessor(String[] pArguments) {
	        super(pArguments);
	    }
	    public ThirdParty401kValidationProcessor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	    }

    @Override
    protected void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
        	getLogger().warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }
        getLogger().info("Starting third party 401k validation batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(Validate401kData.class);

        getLogger().info("Completed third party 401k validation batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class Validate401kData extends JSSBatchJobStep<ThirdParty401kValidationProcessor>  {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new ThirdParty401kValidationProcess().validate401kData();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step Validate401kData ", t);
            }
        }
    }
}
