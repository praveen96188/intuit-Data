package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class CheckPrintBatch extends BaseCheckPrintBatch {

	/**
	 * Default constructor.
	 */
	public CheckPrintBatch()
	{
		super();
	}

    public static DomainEntitySet<CheckPrintBatch> getBatchForDate(SpcfCalendar pBatchDate) {
        SpcfCalendar startTime = pBatchDate.copy();
        CalendarUtils.clearTime(startTime);
        SpcfCalendar endTime = startTime.copy();
        endTime.addDays(1);
        endTime.addMilliseconds(-1);

        return Application.find(CheckPrintBatch.class, new Query<CheckPrintBatch>().Where(CheckPrintBatch.<DomainEntity>CreatedDate().between(startTime, endTime)));
    }

}