package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEmployeeAnnualTotals;
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

@ScheduledJob(name = "EmployeeW2TotalsCalculationProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EmployeeW2TotalsCalculationProcessor extends JSSBatchJob {

    public EmployeeW2TotalsCalculationProcessor(String[] pArguments) {
        super(pArguments);
    }
    public EmployeeW2TotalsCalculationProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void validateRuntimeParameters() {
        //Todo verify if any validation required
        super.validateRuntimeParameters();
    }

    @Override
    protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " processor");
        StopWatch timer = StopWatch.startTimer();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeW2TotalsCalculationBatchJob);

        executeStep(CalculateW2Totals.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class CalculateW2Totals extends JSSBatchJobStep<EmployeeW2TotalsCalculationProcessor> {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeW2TotalsCalculationBatchJob);
            StopWatch sw = StopWatch.startTimer();
            String commandLine = getBatchJobProcessor().getJobInstanceParameters().trim();
            String[] args = commandLine.split(" ");
            CalculateEmployeeAnnualTotals calculateEmployeeAnnualTotals = new CalculateEmployeeAnnualTotals();
            calculateEmployeeAnnualTotals.main(args);

            getLogger().info("completed calculateEmployeeAnnualTotals - in " + sw.getElapsedTimeString());

        }

    }
}
