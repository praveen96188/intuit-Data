package com.intuit.sbd.payroll.psp.jss.ATFDataExtract;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEEQuarterlyTotals;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: ihannur
 * Date: 5/24/12
 * Time: 11:36 AM
 */
@ScheduledJob(name = "EmployeeTotalsCalculationProcess", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class EmployeeTotalsCalculationProcess extends JSSBatchJob {

    private static final String REG_EXP_COMMAND = "-compSeqPattern";
    private static final String CALC_MODE = "-mode";
    private static final String YEAR_QUARTER = "-yearQuarter";

    private String compSeqPattern = null;
    private CalculateEEQuarterlyTotals.Mode mode;
    private String yearQuarter = null;

    static final String YEAR_QUARTER_FORMAT = "20[0-9]{2}Q[1-4]";

    public EmployeeTotalsCalculationProcess(String[] pArguments) {
        super(pArguments);
    }
    public EmployeeTotalsCalculationProcess(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void validateRuntimeParameters() {
        String commandLine = getJobInstanceParameters().trim();

        PayrollServices.beginUnitOfWork();
        mode = CalculateEEQuarterlyTotals.Mode.valueOf(SystemParameter.findStringValue(SystemParameter.Code.EE_TOTALS_CALC_BATCH_MODE, CalculateEEQuarterlyTotals.Mode.UPDATE.toString()));
        PayrollServices.rollbackUnitOfWork();

        if (commandLine.length() > 0) {
            StringBuilder usageMessage = new StringBuilder();
            usageMessage.append(" Usage [-mode:FLUSH|UPDATE|BACKDATE (default:UPDATE)] [-compSeqPattern:regularExpPattern] [-yearQuarter:yyyyQ[1-4]]");
            usageMessage.append("Precedence:\n").append("\t-mode\n").append("\t-yearQuarter\n").append("\t-compSeqPattern\n");
            usageMessage.append("\n\n");

            String[] args = commandLine.split(" ");
            for (String arg : args) {
                String[] argParts = arg.split(":");
                if (argParts.length == 2) {
                    if (argParts[0].equals(REG_EXP_COMMAND)) {
                        compSeqPattern = argParts[1];
                    } else if (argParts[0].equals(YEAR_QUARTER)) {
                        // yearQuarter must be formatted as yyyyQ[1-4] (more precisely, the format must be 20yyMMdd)
                        if (argParts[1].matches(YEAR_QUARTER_FORMAT)) {
                            yearQuarter = argParts[1];
                        } else {
                            throw new RuntimeException("Invalid value for yearQuarter - Correct format is: " + YEAR_QUARTER_FORMAT + "\n" + usageMessage.toString());
                        }
                    } else if (argParts[0].equals(CALC_MODE)) {
                        mode = CalculateEEQuarterlyTotals.Mode.valueOf(argParts[1]);
                    } else {
                        throw new RuntimeException("Invalid arguments: " + arg + "\n" + usageMessage.toString());
                    }
                } else {
                    throw new RuntimeException("Invalid arguments: " + arg + "\n" + usageMessage.toString());
                }
            }
        }
    }

    @Override
    protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " processor");
        StopWatch timer = StopWatch.startTimer();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);

        if (mode == CalculateEEQuarterlyTotals.Mode.UPDATE) {
            executeStep(ExecuteEETotalsUpdateMode.class);
        } else if(mode == CalculateEEQuarterlyTotals.Mode.FLUSH){
            executeStep(ExecuteEETotalsFlushMode.class);
        } else if(mode == CalculateEEQuarterlyTotals.Mode.BACKDATE) {
            executeStep(ExecuteEETotalsBackdateMode.class);
        } else {
            throw new RuntimeException("Invalid Calc MODE for EE Totals job, Please check this - EE_TOTALS_CALC_BATCH_MODE/commandLine argument (-mode)");
        }

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());

        executeStep(EETotalsJobIsComplete.class);
    }

    //This class is here so that the monitor will know that whichever mode was run has completed
    public static class EETotalsJobIsComplete extends JSSBatchJobStep<EmployeeTotalsCalculationProcess> {

        @Override
        public void execute() {
        }
    }

    public static class ExecuteEETotalsUpdateMode extends JSSBatchJobStep<EmployeeTotalsCalculationProcess> {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);
            StopWatch sw = StopWatch.startTimer();
            getLogger().info("Starting EE Totals in UPDATE Mode");

            CalculateEEQuarterlyTotals calculateEmployeeQuarterlyTotals = new CalculateEEQuarterlyTotals();
            calculateEmployeeQuarterlyTotals.updateForNewPayrolls();

            getLogger().info("Completed EE Totals (UPDATE) calculation - in " + sw.getElapsedTimeString());
        }
    }

    public static class ExecuteEETotalsFlushMode extends JSSBatchJobStep<EmployeeTotalsCalculationProcess> {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);
            StopWatch sw = StopWatch.startTimer();
            getLogger().info("Starting EE Totals in FLUSH Mode: YearQuarter: " + getBatchJobProcessor().yearQuarter + " CompSeq: "+ getBatchJobProcessor().compSeqPattern);

            CalculateEEQuarterlyTotals calculateEmployeeQuarterlyTotals = new CalculateEEQuarterlyTotals();
            calculateEmployeeQuarterlyTotals.flushQuarter(getBatchJobProcessor().yearQuarter, getBatchJobProcessor().compSeqPattern);

            getLogger().info("Completed EE Totals (FLUSH) calculation - in " + sw.getElapsedTimeString());
        }
    }

    public static class ExecuteEETotalsBackdateMode extends JSSBatchJobStep<EmployeeTotalsCalculationProcess> {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeeTotalsCalculationBatchJob);
            StopWatch sw = StopWatch.startTimer();
            getLogger().info("Starting EE Totals in BACKDATE Mode");

            CalculateEEQuarterlyTotals calculateEmployeeQuarterlyTotals = new CalculateEEQuarterlyTotals();
            calculateEmployeeQuarterlyTotals.processBackdatedPayrolls();

            getLogger().info("Completed EE Totals (BACKDATE) calculation - in " + sw.getElapsedTimeString());
        }
    }

}
