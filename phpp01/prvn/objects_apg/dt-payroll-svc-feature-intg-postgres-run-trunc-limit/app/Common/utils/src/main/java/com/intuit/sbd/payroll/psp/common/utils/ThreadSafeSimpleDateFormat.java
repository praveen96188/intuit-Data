package com.intuit.sbd.payroll.psp.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 13, 2011
 * Time: 10:02:03 PM
 */
public class ThreadSafeSimpleDateFormat {   

    private DateFormat df;

    public ThreadSafeSimpleDateFormat(String format) {
        this.df = new SimpleDateFormat(format);
    }

    public synchronized String format(Date date) {
        return df.format(date);
    }

    public synchronized Date parse(String string) throws ParseException {
        return df.parse(string);
    }

    public synchronized void setTimeZone(TimeZone pTimeZone) {
        df.setTimeZone(pTimeZone);
    }
}
