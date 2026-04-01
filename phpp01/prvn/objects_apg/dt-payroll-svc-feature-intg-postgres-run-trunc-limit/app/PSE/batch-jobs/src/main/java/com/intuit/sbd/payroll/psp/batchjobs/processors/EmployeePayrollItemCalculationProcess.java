package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.FlushMode;

import java.sql.Timestamp;

/**
 * User: ihannur
 * Date: 8/6/13
 * Time: 4:13 PM
 */
public class EmployeePayrollItemCalculationProcess extends BatchJobProcessor {
    private static final String COMPANY_SEQ = "-compSeq";
    private static final String YEAR = "-year";
    private static final String YEAR_QUARTER = "-yearQuarter";

    static final String YEAR_QUARTER_FORMAT = "20[0-9]{2}Q[1-4]";
    static final String YEAR_FORMAT = "20[0-9]{2}";

    private SpcfUniqueId mCompanySeq = null;
    private String mYear = null;
    private SpcfCalendar mQuarterStartDate = null;


    public EmployeePayrollItemCalculationProcess(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void validateRuntimeParameters() {
        String commandLine = getJobInstanceParameters().trim();

        if (commandLine.length() > 0) {
            StringBuilder usageMessage = new StringBuilder();
            usageMessage.append("Usage [-year=20YY] [-compSeq=companySeqId -yearQuarter=yyyyQ[1-4]] [-yearQuarter=yyyyQ[1-4]]\n");
            usageMessage.append("Precedence:\n").append("\t-year\n").append("\t-compSeq -yearQuarter\n").append("\t-yearQuarter\n");
            usageMessage.append("Default - previous quarter totals are calculated for all companies.\n");
            usageMessage.append("If -compSeq is mentioned, -yearQuarter is mandatory\n\n");

            String[] args = commandLine.split(" ");
            for (String arg : args) {
                String[] argParts = arg.split("=");
                if (argParts.length == 2) {
                    if (argParts[0].equals(COMPANY_SEQ)) {
                        mCompanySeq = SpcfUniqueId.createInstance(argParts[1]);
                    } else if (argParts[0].equals(YEAR_QUARTER)) {
                        // yearQuarter must be formatted as yyyyQ[1-4] (more precisely, the format must be 20yyQ)
                        if (argParts[1].matches(YEAR_QUARTER_FORMAT)) {
                            String yearQuarter = argParts[1];
                            mQuarterStartDate = CalendarUtils.getFirstDayOfQuarter(Integer.parseInt(yearQuarter.substring(0, 4)), Integer.parseInt(yearQuarter.substring(5)));
                        } else {
                            throw new RuntimeException("Invalid value for yearQuarter - Correct format is: " + YEAR_QUARTER_FORMAT + "\n" + usageMessage.toString());
                        }
                    } else if (argParts[0].equals(YEAR)) {
                        // year must be formatted as yyyy (more precisely, the format must be 20yy)
                        if (argParts[1].matches(YEAR_FORMAT)) {
                            mYear = argParts[1];
                        } else {
                            throw new RuntimeException("Invalid value for year - Correct format is: " + YEAR_FORMAT + "\n" + usageMessage.toString());
                        }
                    } else {
                        throw new RuntimeException("Invalid arguments: " + arg + "\n" + usageMessage.toString());
                    }
                } else {
                    throw new RuntimeException("Invalid arguments: " + arg + "\n" + usageMessage.toString());
                }
            }

            if (mCompanySeq != null && mQuarterStartDate == null) {
                throw new RuntimeException("-yearQuarter is required when -compSeq is specified :\n" + usageMessage.toString());
            }
        } else {
            PayrollServices.beginUnitOfWork();
            mQuarterStartDate = CalendarUtils.getFirstDayOfPreviousQuarter(PSPDate.getPSPTime());
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Override
    protected void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " processor");
        StopWatch timer = StopWatch.startTimer();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeePayrollItemsCalcBatchJob);

        if (mYear != null) {
            executeStep(new ExecuteEEPayrollItemTotalsForYear());
        } else if (mCompanySeq != null) {
            executeStep(new ExecuteEEPayrollItemTotalsForCompanyAndQuarter());
        } else {
            executeStep(new ExecuteEEPayrollItemTotalsForQuarter());
        }
        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ExecuteEEPayrollItemTotalsForYear extends BatchJobProcessorStep {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeePayrollItemsCalcBatchJob);
            logger.info("Starting EE Payroll item Totals - ExecuteEEPayrollItemTotalsForYear.");

            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                logger.info("Calling storedProcedure="+StoredProcedures.PAYROLL_ITEM_TOTALS_YEAR_PAYROLL_ITEM_TOT.getStoredProcedureName()+" mYear="+mYear);
                Application.executeSqlProcedure(StoredProcedures.PAYROLL_ITEM_TOTALS_YEAR_PAYROLL_ITEM_TOT, true,
                                                Pair.of(String.class, mYear));
                PayrollServices.commitUnitOfWork();
            } catch (Exception e) {
                throw new RuntimeException("Error in executing Stored procedure - PK_PAYROLL_ITEM_TOTALS.PRC_YEAR_PAYROLL_ITEM_TOT, Parameters- Year: " + mYear);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class ExecuteEEPayrollItemTotalsForQuarter extends BatchJobProcessorStep {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeePayrollItemsCalcBatchJob);
            logger.info("Starting EE Payroll item Totals - ExecuteEEPayrollItemTotalsForQuarter.");

            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                logger.info("Calling storedProcedure="+StoredProcedures.PAYROLL_ITEM_TOTALS_QTR_PAYROLL_ITEM_TOT.getStoredProcedureName() +
                        " mQuarterStartDate="+mQuarterStartDate.getTimeInMilliseconds());
                Application.executeSqlProcedure(StoredProcedures.PAYROLL_ITEM_TOTALS_QTR_PAYROLL_ITEM_TOT, true,
                                                Pair.of(Timestamp.class, new Timestamp(mQuarterStartDate.getTimeInMilliseconds())));
                PayrollServices.commitUnitOfWork();
            } catch (Exception e) {
                throw new RuntimeException("Error in executing Stored procedure - PK_PAYROLL_ITEM_TOTALS.PRC_QTR_PAYROLL_ITEM_TOT, Parameters- QuarterStartDate: " + new Timestamp(mQuarterStartDate.getTimeInMilliseconds()));
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class ExecuteEEPayrollItemTotalsForCompanyAndQuarter extends BatchJobProcessorStep {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmployeePayrollItemsCalcBatchJob);
            logger.info("Starting EE Payroll item Totals - ExecuteEEPayrollItemTotalsForCompanyAndQuarter.");

            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                logger.info("Calling storedProcedure="+StoredProcedures.PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT.getStoredProcedureName() +
                        " mCompanySeq="+mCompanySeq+" mQuarterStartDate="+mQuarterStartDate.getTimeInMilliseconds());
                Application.executeSqlProcedure(StoredProcedures.PAYROLL_ITEM_TOTALS_COMP_QTR_PAYROLL_ITEM_TOT, true,
                                                Pair.of(String.class, mCompanySeq.toString()),
                                                Pair.of(Timestamp.class, new Timestamp(mQuarterStartDate.getTimeInMilliseconds())));
                PayrollServices.commitUnitOfWork();
            } catch (Exception e) {
                throw new RuntimeException("Error in executing Stored procedure - PK_PAYROLL_ITEM_TOTALS.PRC_COMP_QTR_PAYROLL_ITEM_TOT, Parameters- CompanySeq: " + mCompanySeq + " QuarterStartDate: " + new Timestamp(mQuarterStartDate.getTimeInMilliseconds()));
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

}
