package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.billing.ProcessMonthlyFees;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 7/6/12
 * Time: 6:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class MonthlyFeeProcessor extends BatchJobProcessor {
    private SpcfCalendar mBillingPeriod = null;

    public MonthlyFeeProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    public SpcfCalendar getBillingPeriod() {
        return mBillingPeriod;
    }

    public void setBillingPeriod(SpcfCalendar pBillingPeriod) {
        mBillingPeriod = pBillingPeriod;
    }

    private SpcfCalendar getFirstDayOfPreviousBillingPeriod(SpcfCalendar pDate) {
        int year = pDate.getYear();
        int month = pDate.getMonth();

        if (month > 1) { // Feb (2) thru Dec (12)
            --month;
        } else { // Jan (1)
            month = 12;
            --year;
        }

        return SpcfCalendar.createInstance(year, month, 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
    }

    @Override
    protected void validateRuntimeParameters() {
        SpcfCalendar billingPeriod = getFirstDayOfPreviousBillingPeriod(PSPDate.getPSPTime());
        String commandLine = getJobInstanceParameters().trim();

        if (commandLine.length() > 0) {
            String[] args = commandLine.split(" ");

            if (args.length > 0) {
                for (String arg : args) {
                    if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                        SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                        billingPeriod = SpcfCalendar.createInstance(clDate.getYear(), clDate.getMonth(), 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());

                        break;
                    }
                }
            }
        }

        setBillingPeriod(billingPeriod);
    }

    @Override
    protected void validateStepRuntimeParameters(String stepName) {
        validateRuntimeParameters();
    }

    @Override
    public void execute() {
        logger.info("Starting Monthly Fee batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessMonthlyOfferingFees());

        logger.info("Completed Monthly Fee batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessMonthlyOfferingFees extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.MonthlyFeeBatchJob);

                new ProcessMonthlyFees().process(getBillingPeriod());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UpdateLedgerBalance ", t);
            }
        }
    }
}
