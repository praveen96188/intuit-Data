package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * User: dweinberg
 * Date: 11/18/11
 * Time: 2:20 PM
 */
public class SAPQuarter implements Comparable<SAPQuarter> {

    private int year;
    private int quarter;

    public SAPQuarter() {}

    public SAPQuarter(int year, int quarter) {
        this.year = year;
        this.quarter = quarter;
    }

    public SAPQuarter(SpcfCalendar pCalendar) {
        this.year = pCalendar.getYear();
        this.quarter = CalendarUtils.getQuarterAsInt(pCalendar);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public SpcfCalendar getFirstDayOfQuarter() {
        return CalendarUtils.getFirstDayOfQuarter(getYear(), getQuarter());
    }

    public SpcfCalendar getLastDayOfQuarter() {
        return CalendarUtils.endOfDay(CalendarUtils.getLastDayOfQuarter(getYear(), getQuarter()));
    }

    public SpcfCalendar getFirstDayOfQuarterMinus45Days() {
        SpcfCalendar calendar = getFirstDayOfQuarter();
        calendar.addDays(-45);
        return calendar;
    }

    public SAPQuarter previousQuarter() {
        return new SAPQuarter(quarter == 1 ? year - 1 : year, quarter == 1 ? 4 : quarter - 1);
    }

    public SAPQuarter nextQuarter() {
        return new SAPQuarter(quarter == 4 ? year + 1 : year, quarter == 4 ? 1 : quarter + 1);
    }

    public int compareTo(SAPQuarter o) {
        if (year != o.year) {
            return year - o.year;
        }
        return quarter - o.quarter;
    }

    public static SAPQuarter currentQuarter() {
        SpcfCalendar now = PSPDate.getPSPTime();
        return new SAPQuarter(TaxPeriod.getYearNumber(now), TaxPeriod.getQuarterNumber(now));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SAPQuarter that = (SAPQuarter) o;

        if (quarter != that.quarter) return false;
        //noinspection RedundantIfStatement
        if (year != that.year) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + quarter;
        return result;
    }

}
