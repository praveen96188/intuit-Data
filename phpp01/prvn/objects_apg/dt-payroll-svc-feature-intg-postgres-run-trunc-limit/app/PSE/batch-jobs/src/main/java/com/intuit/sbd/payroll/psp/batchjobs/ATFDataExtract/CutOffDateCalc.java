package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

//import java.util.GregorianCalendar;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

import java.util.Calendar;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: Jun 5, 2009
 * Time: 4:29:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class CutOffDateCalc {
    private static CutOffDateCalc mInstance;

    private int mMonthOfQuarter; // To signify which month in the quarter. 1 or 2 or 3
    private int mDayOfMonth; // To signify day of the month to start for the day of the week.
    private int mDayOfWeek; // This actually signifies cut-off day of the week. 1 through 7 for Sunday through Saturday.
    /*
        e.g., if mMonthOfQuarter=2, mDayOfMonth=13 and mDayOfWeek=7, it signifies:
        1st Saturday on or after 13th in the 2nd month of the quarter
    */

    private CutOffDateCalc(int pMonthOfQuarter, int pDayOfMonth, int pDayOfWeek) {
        mMonthOfQuarter = pMonthOfQuarter;
        mDayOfMonth = pDayOfMonth;
        mDayOfWeek = pDayOfWeek;
    }

    /**
     * To return the beginning date of the quarter.
     */
    public SpcfCalendar getBeginningOfQuarter(SpcfCalendar pDate) {
        return CalendarUtils.getFirstDayOfQuarter(pDate);
    }


    /**
     * To get the actual cut-off date for the given date.
     */
    public SpcfCalendar getCutOffDate(SpcfCalendar pDate) {

        SpcfCalendar temp = getBeginningOfQuarter(pDate);
        temp.addMonths(mMonthOfQuarter - 1);// As mMonthOfQuarter starts from 1.
        temp.setValues(temp.getYear(), temp.getMonth(), mDayOfMonth);

        while (temp.getDayOfWeek() != mDayOfWeek) {
            temp.addDays(1);
        }

        return (temp);

    }

    /**
     * To compare whether the give date is on or after the cut-off date.
     * If the given date is on or after the cut-off date, it returns true else
     * it retunrs false.
     */
    public boolean isOnOrAfterCutOffDate(SpcfCalendar pDate) {

        boolean onOrAfter = true;

        SpcfCalendar cutOffDate = getCutOffDate(pDate);

        if (pDate.before(cutOffDate)) {
            onOrAfter = false;
        }

        return (onOrAfter);
    }

    /**
     * To return the CutOffDateCalc instance.
     */
    public static void createInstance(int pMonthOfQuarter, int pDayOfMonth, int pDayOfWeek) {
            mInstance = new CutOffDateCalc(pMonthOfQuarter, pDayOfMonth, pDayOfWeek);
    }

    public static CutOffDateCalc getInstance(){
       if (mInstance != null )
            return mInstance;
       else
           throw new RuntimeException("CutOffDateCalc is not yet created.");
    }
}
