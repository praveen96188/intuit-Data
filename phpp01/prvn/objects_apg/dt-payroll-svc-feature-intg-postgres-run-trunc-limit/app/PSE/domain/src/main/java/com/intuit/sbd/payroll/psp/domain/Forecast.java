package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Hand-written business logic
 */
public class Forecast extends BaseForecast {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static DomainEntitySet<Forecast> findForecasts(ForecastStatus pStatus, SpcfCalendar pDate) {
        Criterion<Forecast> query = Status().equalTo(pStatus);

        if (pDate != null) {
            query = query.And(RunDate().equalTo(pDate));
        }

        return Application.find(Forecast.class, query);
    }

    public static Forecast findForecast(SpcfCalendar pDate) {
        Forecast forecast = null;

        SpcfCalendar forecastDate = SpcfCalendar.createInstance(pDate.getYear(), pDate.getMonth(), pDate.getDay(),
                                                                0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        Criterion<Forecast> query = RunDate().equalTo(forecastDate);
        DomainEntitySet<Forecast> forecasts = Application.find(Forecast.class, query);

        if (forecasts != null && !forecasts.isEmpty()) {
            forecast = forecasts.iterator().next();
        }

        return forecast;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Forecast()
	{
		super();
	}


}