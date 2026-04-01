package com.intuit.sbd.payroll.psp.adapters.taxcredits.adapter;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: dweinberg
 * Date: Jan 20, 2010
 * Time: 4:06:52 PM
 */
public class TaxCreditsTranslator {

    //@see SAPTranslator    
    public static Date getDateFromSpcfCalendar(SpcfCalendar s) {
        if (s == null) return null;
        return new Date(s.toLocal().getTimeInMilliseconds());
    }

    //@see SAPTranslator
    public static SpcfCalendar getSpcfCalendarFromDate(Date pDate) {
        if (pDate == null)
            return null;

        return SpcfCalendar.createInstance(pDate.getTime(), SpcfTimeZone.getLocalTimeZone());
    }

    /**
     *
     * @param phoneNumber 1234567890
     * @param ext 1234
     * @return (123) 456-7890 x1234
     */
    public static String formatPhoneNumber(String phoneNumber, String ext) {
        String fullNumber = "(" + phoneNumber.substring(0,3) + ")" + phoneNumber.substring(3,6) + "-" + phoneNumber.substring(6);
        if (ext != null && !ext.equals("")) {
            fullNumber += " x" + ext;
        }
        return fullNumber;
    }

    /**
     *
     * @param ein 123456789
     * @return 12-3456789
     */
    public static String formatEIN(String ein) {
        return ein.substring(0,2) + "-" + ein.substring(2);
    }

    /**
     *
     * @param ssn 123456789
     * @return 123-45-6789
     */
    public static String formatSSN(String ssn) {
        return ssn.substring(0,3) + "-" + ssn.substring(3,5) + "-" + ssn.substring(5);
    }

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
    public static String formatShortDate(Date date) {
        return dateFormatter.format(date);
    }


    public static String formatShortDateString(String dateString){
        return dateString.substring(0,2) + "/" + dateString.substring(2,4) + "/" + dateString.substring(4,8);
    }

    public static Date parseDate(String dateString) {
        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(dateString.substring(4,8)), Integer.parseInt(dateString.substring(0,2))-1, Integer.parseInt(dateString.substring(2,4)));
        return c.getTime();
    }
}
