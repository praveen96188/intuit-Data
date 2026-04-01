package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.billing.ProcessMonthlyFees;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 7/6/12
 * Time: 6:21 AM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "MonthlyFee", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class MonthlyFeeProcessor extends JSSBatchJob {
    private SpcfCalendar mBillingPeriod = null;

    public MonthlyFeeProcessor(String[] pArguments) {
        super(pArguments);
	}
	public MonthlyFeeProcessor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
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
    	getLogger().info("Starting Monthly Fee batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(ProcessMonthlyOfferingFees.class);

        getLogger().info("Completed Monthly Fee batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessMonthlyOfferingFees extends JSSBatchJobStep<MonthlyFeeProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.MonthlyFeeBatchJob);

                new ProcessMonthlyFees().process(getBatchJobProcessor().getBillingPeriod());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step UpdateLedgerBalance ", t);
            }
        }
    }
}
