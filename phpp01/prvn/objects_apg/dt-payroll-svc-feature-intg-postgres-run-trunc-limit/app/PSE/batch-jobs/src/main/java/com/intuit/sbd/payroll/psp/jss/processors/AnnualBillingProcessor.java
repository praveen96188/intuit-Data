package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.AnnualBilling.AnnualBilling;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 11/28/12
 * Time: 4:33 PM
 */
@ScheduledJob(name = "AnnualBillingProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class AnnualBillingProcessor extends JSSBatchJob {

    public AnnualBillingProcessor(String[] pArguments) {
        super(pArguments);
    }
    public AnnualBillingProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " processor");
        StopWatch timer = StopWatch.startTimer();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.AnnualBillingBatchJob);

        executeStep(AnnualBillingStep.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class AnnualBillingStep extends JSSBatchJobStep<AnnualBillingProcessor> {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.AnnualBillingBatchJob);
            StopWatch sw = StopWatch.startTimer();
            String commandLine = getBatchJobProcessor().getJobInstanceParameters().trim();

            String[] args = new String[1];
            if (commandLine.length() == 0) {
                args[0] = String.valueOf(PSPDate.getPSPTime().getYear() - 1);
            } else {
                args = commandLine.split(" ");
            }

            AnnualBilling w2Billing = new AnnualBilling();
            w2Billing.main(args);

            getLogger().info("completed generating annual billing - in " + sw.getElapsedTimeString());

        }
    }
}
