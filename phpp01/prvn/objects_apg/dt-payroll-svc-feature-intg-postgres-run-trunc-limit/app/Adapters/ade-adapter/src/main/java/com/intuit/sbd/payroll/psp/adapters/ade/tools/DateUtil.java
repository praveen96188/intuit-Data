package com.intuit.sbd.payroll.psp.adapters.ade.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 10/2/13
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateUtil {
    /**
     *   This method is used to determine the date range for the current quarter to date.
     *
     *   @param date the Date that the date range is to be based on
     *   @return an array of two Date objects, the first of which is the start date, the second the end date of the date range
     */
    public static Date getQuarterStartDate(final Date date){
        final GregorianCalendar c = makeCalendar(date);


        // Get the dates for the start and the end of the quarter
        int startMonth=Quarter.valueOf(c.get(Calendar.MONTH)).startMonth();
        c.set(Calendar.MONTH,startMonth);
        c.set(Calendar.DAY_OF_MONTH,1);
        c.set(Calendar.YEAR,c.get(Calendar.YEAR));
        beginningOfDay(c);


        return c.getTime();
    }

    /**
     *  This method is used to create the necessary calendar object from the given date. The date is cloned.
     *
     *  @param date the date to be set as current
     *  @return a calendar representing the given date
     */
    private static GregorianCalendar makeCalendar(final Date date){
        final GregorianCalendar c = (GregorianCalendar)GregorianCalendar.getInstance();
        if(date != null){
            c.setTime((Date)date.clone());
        }
        return(c);
    }

    /**
     * Used to set the time of the calendar to the beginning of the day (00:00:00.000).
     *
     * @param cal the calendar
     */
    private static void beginningOfDay(final Calendar cal){
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
    }

    /**
     * Used to set the time of the calendar to the end of the day (23:59:59.999)
     *
     * @param cal
     */
    private static void endOfDay(final Calendar cal){
        cal.set(Calendar.HOUR_OF_DAY,23);
        cal.set(Calendar.MINUTE,59);
        cal.set(Calendar.SECOND,59);
        cal.set(Calendar.MILLISECOND,999);
    }
    enum Quarter {

        FIRST(Calendar.JANUARY,Calendar.MARCH),
        SECOND(Calendar.APRIL,Calendar.JUNE),
        THIRD(Calendar.JULY,Calendar.SEPTEMBER),
        FOURTH(Calendar.OCTOBER,Calendar.DECEMBER);

        private final int startMonth, endMonth;

        /**
         * Creates a quarter with the given start and end month.
         *
         * @param startMonth the first month of the quarter
         * @param endMonth the last month of the quarter
         */
        private Quarter(final int startMonth, final int endMonth){
            this.startMonth = startMonth;
            this.endMonth = endMonth;
        }

        /**
         * Used to retrieve the first month of the quarter.
         *
         * @return the first month of the quarter
         */
        public int startMonth(){return(startMonth);}

        /**
         * Used to retrieve the last month of the quarter.
         *
         * @return the last month of the quarter
         */
        public int endMonth(){return(endMonth);}

        /**
         * Used to convert the month value to its corresponding Quarter object. The values
         * are 0-based (as they would come from a GregorianCalendar object).
         *
         * @param month the number of the month (0-based)
         * @return the Quarter containing the given month
         */
        public static Quarter valueOf(final int month){
            Quarter qtr = null;
            if(month < 3){qtr = Quarter.FIRST;}
            else if(month > 2 && month < 6){qtr = Quarter.SECOND;}
            else if(month > 5 && month < 9){qtr = Quarter.THIRD;}
            else if(month > 8){qtr = Quarter.FOURTH;}
            return(qtr);
        }

        /**
         * Used to retrieve the quarter that comes before this quarter.
         *
         * @return the previous quarter
         */
        public Quarter previous(){
            final Quarter[] qtrs = values();

            int idx = ordinal()-1;
            if(idx < 0){idx = qtrs.length-1;}

            return(qtrs[idx]);
        }

        /**
         * Used to retrieve the quarter that follows this quarter.
         *
         * @return the next quarter
         */
        public Quarter next(){
            final Quarter[] qtrs = values();

            int idx = ordinal()+1;
            if(idx >= qtrs.length){idx = 0;}

            return(qtrs[idx]);
        }
    }
}
