package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class ForecastDetail extends BaseForecastDetail {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static DomainEntitySet<ForecastDetail> findForecastDetails(ForecastStatus pStatus,
                                                         int pFromTxnCount,
                                                         int pToTxnCount,
                                                         SpcfCalendar pFromDate,
                                                         SpcfCalendar pToDate,
                                                         String pJobAction) {
        int i = 0;
        String[] paramNames = new String[6];
        paramNames[i++] = "status";
        paramNames[i++] = "fromTxnCount";
        paramNames[i++] = "toTxnCount";
        paramNames[i++] = "fromDate";
        paramNames[i++] = "toDate";
        paramNames[i] = "jobAction";

        i = 0;
        Object[] paramValues = new Object[6];
        paramValues[i++] = pStatus;
        paramValues[i++] = pFromTxnCount;
        paramValues[i++] = pToTxnCount;
        paramValues[i++] = pFromDate;
        paramValues[i++] = pToDate;
        paramValues[i] = pJobAction;

        return Application.findByNamedQuery("findForecastDetailsByTxnCountRunDateAndJobAction", paramNames, paramValues);
    }

    public static ForecastDetail findForecastDetail(String pJobAction, ForecastStatus pStatus, SpcfCalendar pDate) {
        ForecastDetail detail = null;

        Criterion<ForecastDetail> query = JobAction().equalTo(pJobAction)
                .And(Forecast().Status().equalTo(pStatus))
                .And(Forecast().RunDate().equalTo(pDate));

        DomainEntitySet<ForecastDetail> details = Application.find(ForecastDetail.class, query);
        if (details != null && !details.isEmpty()) {
            detail = details.iterator().next();
        }

        return detail;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ForecastDetail()
	{
		super();
	}
}