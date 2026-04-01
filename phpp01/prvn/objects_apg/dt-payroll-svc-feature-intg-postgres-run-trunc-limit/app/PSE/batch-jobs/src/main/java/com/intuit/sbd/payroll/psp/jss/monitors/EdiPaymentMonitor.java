package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.EdiPaymentProcessor;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 17, 2011
 * Time: 10:59:52 AM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "EdiPaymentMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class EdiPaymentMonitor extends  JSSBatchJobMonitor {
    
    public EdiPaymentMonitor(String[] pArguments) {
		super(pArguments);
	}

	public EdiPaymentMonitor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}
	
	@Override
	public BatchJobType getBatchJobToMonitor() {
		return  BatchJobType.EdiPayment;
	}

	@Override
	public Class<?> getBatchJobActionToMonitor() {
		return  EdiPaymentProcessor.RecordPaymentEvents.class;
	}

	@Override
    public void execute() throws Exception{
        SpcfCalendar today = PSPDate.getPSPTime();

        if (CalendarUtils.isHoliday(today)) {
        	getLogger().warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        //
        // PSRV002847 - Check to see of there are any pending EDI tax payments for today's date
        //

        long paymentCount = MoneyMovementTransaction.getPendingTaxPaymentCountForDate(PaymentMethod.EDI, today);

        if (paymentCount == 0) {
        	getLogger().info(getClass().getSimpleName() + " skipped (no pending EDI tax payments) ");
            return;
        }

        //
        // At this point, since there are remaining pending EDI tax payments in the system with today's date,
        // this is an error.  Either the EdiPaymentProcessor batch job didn't run or it missed some payments.
        // Both of these conditions are checked below...
        //

        //
        // The execute method will throw an exception if the EdiPaymentProcessor job has not completed execution...
        //

        super.execute();

        //
        // If we get here then the following is true:
        //
        // 1) The current pending EDI tax payment count for today's date is > 0
        // 2) The EdiPaymentProcessor batch job has successfully completed its run
        //
        // Since both of the above conditions are true, this is an error since, if the EdiPaymentProcessor job has
        // successfully completed, there should be no remaining pending EDI tax payments with today's date...
        //

        throw new RuntimeException(String.format("The %s monitor has detected %d remaining pending EDI tax payment(s) " +
                                                 "for date %s after the %s batch job has completed processing.",
                                                 getBatchJobToMonitorId(),
                                                 paymentCount,
                                                 today.format("MM/DD/YYYY"),
                                                 getBatchJobToMonitor() ));
    }
}
