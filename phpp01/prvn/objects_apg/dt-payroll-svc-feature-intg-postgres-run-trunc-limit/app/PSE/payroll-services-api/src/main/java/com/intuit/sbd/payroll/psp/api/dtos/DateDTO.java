/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/DateDTO.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DateDTO implements Comparable<DateDTO> {

    private int month;
    private int year;
    private int day;

    public DateDTO() {
    }

    /**
     * Creates DateDTO from a String in format 'yyyy-MM-dd'
     *
     * @param pDate
     */
    public DateDTO(String pDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(pDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public DateDTO(String pDate, String pDateFormat) {
        this(pDate, new SimpleDateFormat(pDateFormat));
    }

    public DateDTO(String pDate, SimpleDateFormat pFormat) {
        try {
            Date date = pFormat.parse(pDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public DateDTO(SpcfCalendar pSpcfCalendar) {
        month = pSpcfCalendar.getMonth() - 1;
        year = pSpcfCalendar.getYear();
        day = pSpcfCalendar.getDay();
    }

    public DateDTO(int year, int month, int day) {
        // month is zero based
        set(year, month -1, day);
    }

    // Converts an integer of format YYYYMMDD to a DateDTO (MM between 01 andd 12)
    public DateDTO(int anInt) {
        year = anInt / 10000;
        month = (anInt - year * 10000) / 100;
        day = anInt - year * 10000 - month * 100;

        month--;
    }

    public DateDTO(Date pDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(pDate);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int pMonth) {
        this.month = pMonth;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int pYear) {
        this.year = pYear;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int pDay) {
        this.day = pDay;
    }

    public int getPrintMonth() {
        return month + 1;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (month < 0 || month > 11) {
            validationResult.getMessages().InvalidValue(EntityName.Date, "", "Month");
        }

        if (year <= 0) {
            validationResult.getMessages().InvalidValue(EntityName.Date, "", "Year");
        }

        if (day <= 0 || day > 31) {
            validationResult.getMessages().InvalidValue(EntityName.Date, "", "Day");
        }

        return validationResult;
    }

    public ProcessResult validate(DateDTOValidator dateValidator) {
        return dateValidator.validate(this);
    }

    /**
     * Sets the values on the DateDTO
     *
     * @param pYear
     * @param pMonth Like Java calendar, Month value is 0-based. e.g., 0 for January.
     * @param pDay
     */
    public void set(int pYear, int pMonth, int pDay) {
        setYear(pYear);
        setMonth(pMonth);
        setDay(pDay);
    }

    public static SpcfCalendar convertToSpcfCalendar(DateDTO pDateDTO) {
        if (pDateDTO != null) {
            return SpcfCalendar.createInstance(pDateDTO.getYear(), pDateDTO.getMonth() + 1, pDateDTO.getDay(),
                    SpcfTimeZone.getLocalTimeZone());
        } else {
            return null;
        }

    }

    public SpcfCalendar toSpcfCalendar() {
        return SpcfCalendar.createInstance(getYear(), getMonth() + 1, getDay(), SpcfTimeZone.getLocalTimeZone());
    }

    public String toString() {
        return String.format("mm/dd/yyyy: %d/%d/%d", getPrintMonth(), day, year);
    }

    public String getMMDDYYYY() {
        return String.format("%02d/%02d/%04d", getPrintMonth(), day, year);
    }
    public String getYYYYMMDD() {
        return String.format("%04d-%02d-%02d", year, getPrintMonth(), day);
    }
    public String getMMDDYY() {
        return String.format("%02d/%02d/", getPrintMonth(), day) + String.format("%04d", year).substring(2);
    }

    public long getYYYYMMDDAsLong() {
        return day + (month + 1) * 100 + year * 10000;
    }

    @Override
    public boolean equals(Object pDateDTO) {
        return year == ((DateDTO)pDateDTO).year && month == ((DateDTO)pDateDTO).month && day == ((DateDTO)pDateDTO).day;
    }

    public int compareTo(DateDTO o) {
        return (year * 10000 + month * 100 + day) - (o.year * 10000 + o.month * 100 + o.day);
    }
}
