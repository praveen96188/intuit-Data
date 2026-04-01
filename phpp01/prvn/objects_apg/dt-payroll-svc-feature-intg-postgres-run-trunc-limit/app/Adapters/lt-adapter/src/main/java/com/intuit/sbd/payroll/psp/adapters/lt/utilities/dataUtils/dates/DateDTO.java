package com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates;

/**
 * DTO for use with the QBDTWS (Cloud) adapter.  This adapter expects it's dates to be broken down and specified as separate
 * elements, such as day, month, year.
 *
 * Internally it treats each element as an int, but wrapper methods are available to output the elements as formatted strings.
 * For instance, the month can be returned as a two character string.  If the month is a single digit it will be formatted
 * with a leading '0'
 */

public class DateDTO {

    private int month;
    private int year;
    private int day;


    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getMonthAsString(){

        return String.format("%02d", this.month);
    }

    public String getYearAsString(){
        return String.valueOf(this.year);
    }

    public String getDayAsString(){
        return String.format("%02d", this.day);
    }

}
