package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 11, 2009
 * Time: 4:39:36 PM
 */
public class TaxPeriod {

    public static SpcfCalendar Q1_BEGIN(String taxYear){
        return Q1_BEGIN(Integer.parseInt(taxYear));
    }

    public static SpcfCalendar Q1_BEGIN(int taxYear){
        return SpcfCalendar.createInstance(taxYear, SpcfCalendar.January, 1);
    }

    public static SpcfCalendar Q1_END(String taxYear){
        return Q1_END(Integer.parseInt(taxYear));
    }

    public static SpcfCalendar Q1_END(int taxYear){
        return SpcfCalendar.createInstance(taxYear, SpcfCalendar.March, 31, 23, 59, 59, 999);
    }

    public static SpcfCalendar Q2_BEGIN(String taxYear){
        return Q2_BEGIN(Integer.parseInt(taxYear));
    }

    public static SpcfCalendar Q2_BEGIN(int taxYear){
        return SpcfCalendar.createInstance(taxYear, SpcfCalendar.April, 1);
    }

    public static SpcfCalendar Q2_END(String taxYear){
        return Q2_END(Integer.parseInt(taxYear));
    }

    public static SpcfCalendar Q2_END(int taxYear){
        return SpcfCalendar.createInstance(taxYear, SpcfCalendar.June, 30, 23, 59, 59, 999);
    }

    public static SpcfCalendar Q3_BEGIN(String taxYear){
        return Q3_BEGIN(Integer.parseInt(taxYear));
    }

    public static SpcfCalendar Q3_BEGIN(int taxYear){
        return SpcfCalendar.createInstance(taxYear, SpcfCalendar.July, 1);
    }

    public static SpcfCalendar Q3_END(String taxYear){
        return Q3_END(Integer.parseInt(taxYear));
    }

    public static SpcfCalendar Q3_END(int taxYear){
        return SpcfCalendar.createInstance(taxYear, SpcfCalendar.September, 30, 23, 59, 59, 999);
    }

    public static SpcfCalendar Q4_BEGIN(String taxYear){
        return Q4_BEGIN(Integer.parseInt(taxYear));
    }

    public static SpcfCalendar Q4_BEGIN(int taxYear){
        return SpcfCalendar.createInstance(taxYear, SpcfCalendar.October, 1);
    }

    public static SpcfCalendar Q4_END(String taxYear){
        return Q4_END(Integer.parseInt(taxYear));
    }

    public static SpcfCalendar Q4_END(int taxYear){
        return SpcfCalendar.createInstance(taxYear, SpcfCalendar.December, 31, 23, 59, 59, 999);
    }

    public static boolean isDateInQuarter(int quarter, SpcfCalendar date){
        if(date != null){
            int year = date.getYear();
            switch(quarter){
                case 1:
                    return date.compareTo(Q1_BEGIN(year)) > -1 && date.compareTo(Q1_END(year)) < 1;
                case 2:
                    return date.compareTo(Q2_BEGIN(year)) > -1 && date.compareTo(Q2_END(year)) < 1;
                case 3:
                    return date.compareTo(Q3_BEGIN(year)) > -1 && date.compareTo(Q3_END(year)) < 1;
                case 4:
                    return date.compareTo(Q4_BEGIN(year)) > -1 && date.compareTo(Q4_END(year)) < 1;
            }
        }
        return false;
    }

    public static SpcfCalendar getQuarterStart(int quarter, int year) {
        switch(quarter){
            case 1:
                return Q1_BEGIN(year);
            case 2:
                return Q2_BEGIN(year);
            case 3:
                return Q3_BEGIN(year);
            case 4:
                return Q4_BEGIN(year);
        }
        return null;
    }

    public static SpcfCalendar getQuarterStart(SpcfCalendar date) {
        switch(TaxPeriod.getQuarterNumber(date)){
            case 1:
                return Q1_BEGIN(date.getYear());
            case 2:
                return Q2_BEGIN(date.getYear());
            case 3:
                return Q3_BEGIN(date.getYear());
            case 4:
                return Q4_BEGIN(date.getYear());
        }
        return null;
    }

    public static int getQuarterNumber(SpcfCalendar date) {
        
        if(date != null){
            int year = date.getYear();

            if(date.compareTo(Q1_BEGIN(year)) > -1 && date.compareTo(Q1_END(year)) < 1) return 1;
            if(date.compareTo(Q2_BEGIN(year)) > -1 && date.compareTo(Q2_END(year)) < 1) return 2;
            if(date.compareTo(Q3_BEGIN(year)) > -1 && date.compareTo(Q3_END(year)) < 1) return 3;
            if(date.compareTo(Q4_BEGIN(year)) > -1 && date.compareTo(Q4_END(year)) < 1) return 4;
        }

        return 0;
    }

    public static int getYearNumber(SpcfCalendar date) {
        if(date != null){
            return date.getYear();
        }
        return 0;
    }

    public static SpcfCalendar getPeriodEndDate(SpcfCalendar date) {
        if(date != null){
            int year = date.getYear();
            if(date.compareTo(Q1_BEGIN(year)) > -1 && date.compareTo(Q1_END(year)) < 1){
                return Q1_END(year);
            }
            if(date.compareTo(Q2_BEGIN(year)) > -1 && date.compareTo(Q2_END(year)) < 1){
                return Q2_END(year);
            }
            if(date.compareTo(Q3_BEGIN(year)) > -1 && date.compareTo(Q3_END(year)) < 1){
                return Q3_END(year);
            }
            if(date.compareTo(Q4_BEGIN(year)) > -1 && date.compareTo(Q4_END(year)) < 1){
                return Q4_END(year);
            }
        }
        return null;
    }

}
