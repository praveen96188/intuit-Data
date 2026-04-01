package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEmployeeAnnualTotals;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * User: mvillani
 * Date: 8/24/12
 * Time: 11:36 AM
 */
public class EmployeeW2TotalsCalculationProcessor extends BatchJobProcessor {

    public EmployeeW2TotalsCalculationProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void validateRuntimeParameters() {
        //Todo verify if any validation required
        super.validateRuntimeParameters();
    }

    @Override
    protected void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " processor");
        StopWatch timer = StopWatch.startTimer();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeW2TotalsCalculationBatchJob);

        executeStep(new CalculateW2Totals());

        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class CalculateW2Totals extends BatchJobProcessorStep {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeW2TotalsCalculationBatchJob);
            StopWatch sw = StopWatch.startTimer();
            String commandLine = getJobInstanceParameters().trim();
            String[] args = commandLine.split(" ");
            CalculateEmployeeAnnualTotals calculateEmployeeAnnualTotals = new CalculateEmployeeAnnualTotals();
            calculateEmployeeAnnualTotals.main(args);

            logger.info("completed calculateEmployeeAnnualTotals - in "+ sw.getElapsedTimeString());

        }

    }
}
