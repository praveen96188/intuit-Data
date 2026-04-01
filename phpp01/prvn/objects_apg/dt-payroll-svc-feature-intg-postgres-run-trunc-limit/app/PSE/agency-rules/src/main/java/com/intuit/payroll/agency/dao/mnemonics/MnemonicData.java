//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao.mnemonics;

//import com.intuit.spc.foundations.portability.collections.*;
//import com.intuit.spc.foundations.portability.SpcfFactory;
//import com.intuit.spc.foundations.portability.SpcfStringUtil;
import com.intuit.payroll.agency.util.RulesCalendar;

import java.util.ArrayList;

/// <summary>
/// Helper class for the interpreter. This is the know it all as to what is mnemonically possible.
/// </summary>
class MnemonicData
{
    static public final String HYPHEN_SEPERATOR = "-";
    static public final String PLUS_SEPERATOR = "[+]";

    /// <summary>
    /// Mnemonic for End of Month.
    /// </summary>
    static public final String EOM = "EOM";

    /// <summary>
    /// Mnemonic for the day of the Accrual Date
    /// </summary>
    static public final String PAYDATE = "PAYDATE";

    /// <summary>
    /// Mnemonics for Next business or normal day.  eg: E-3BD.
    /// </summary>
    static public final String END_PERIOD_START_TAG = "E";
    static public final String NEXT_BUSINESS_DAY_TAG = "BD";
    static public final String NEXT_NORMAL_DAY_TAG = "ND";

    static public final String SUN_STRING = "SUN";
    static public final String MON_STRING = "MON";
    static public final String TUE_STRING = "TUE";
    static public final String WED_STRING = "WED";
    static public final String THU_STRING = "THU";
    static public final String FRI_STRING = "FRI";
    static public final String SAT_STRING = "SAT";

    static public final String JAN_STRING = "JAN";
    static public final String FEB_STRING = "FEB";
    static public final String MAR_STRING = "MAR";
    static public final String APR_STRING = "APR";
    static public final String MAY_STRING = "MAY";
    static public final String JUN_STRING = "JUN";
    static public final String JUL_STRING = "JUL";
    static public final String AUG_STRING = "AUG";
    static public final String SEP_STRING = "SEP";
    static public final String OCT_STRING = "OCT";
    static public final String NOV_STRING = "NOV";
    static public final String DEC_STRING = "DEC";

    /// <summary>
    /// Used as a list of day mnemonics such that we can ask .Contains(some String) to determine program flow.
    /// </summary>
    private ArrayList<String> m_daysOfTheWeekList;
//    private SpcfArrayList<String> m_daysOfTheWeekList;

    /// <summary>
    /// Used as a list of month mnemonics such that we can ask .Contains(some String) to determine program flow.
    /// </summary>
    private ArrayList<String> m_monthsOfTheYearList;
//    private SpcfArrayList<String> m_monthsOfTheYearList;
//    SpcfFactory m_factory = SpcfFactory.getInstance();

    public MnemonicData()
    {

        m_daysOfTheWeekList = new ArrayList<String>(7);
        m_daysOfTheWeekList.add(SUN_STRING);
        m_daysOfTheWeekList.add(MON_STRING);
        m_daysOfTheWeekList.add(TUE_STRING);
        m_daysOfTheWeekList.add(WED_STRING);
        m_daysOfTheWeekList.add(THU_STRING);
        m_daysOfTheWeekList.add(FRI_STRING);
        m_daysOfTheWeekList.add(SAT_STRING);

//        m_monthsOfTheYearList = m_factory.<String>createArrayList(12);
        m_monthsOfTheYearList = new ArrayList<String>(12);
        m_monthsOfTheYearList.add(JAN_STRING);
        m_monthsOfTheYearList.add(FEB_STRING);
        m_monthsOfTheYearList.add(MAR_STRING);
        m_monthsOfTheYearList.add(APR_STRING);
        m_monthsOfTheYearList.add(MAY_STRING);
        m_monthsOfTheYearList.add(JUN_STRING);
        m_monthsOfTheYearList.add(JUL_STRING);
        m_monthsOfTheYearList.add(AUG_STRING);
        m_monthsOfTheYearList.add(SEP_STRING);
        m_monthsOfTheYearList.add(OCT_STRING);
        m_monthsOfTheYearList.add(NOV_STRING);
        m_monthsOfTheYearList.add(DEC_STRING);
    }

    /// <summary>
    /// If the Mnemonic can be split by the '-' char then it will be broken into parts, otherwise
    /// it comes back whole.
    /// </summary>
    /// <param name="mnemonicInput">The Mnemonic to split</param>
    /// <returns>An array of String of the parts.</returns>
    public String[] splitMnemonic(String mnemonicInput)
    {
        if (mnemonicInput.indexOf(HYPHEN_SEPERATOR) > -1)
        {
            return mnemonicInput.split(HYPHEN_SEPERATOR, -1);
//            return SpcfStringUtil.split(mnemonicInput, HYPHEN_SEPERATOR);
        }
        else if (mnemonicInput.indexOf("+") > -1)
        {
            return mnemonicInput.split(PLUS_SEPERATOR, -1);
//            return SpcfStringUtil.split(mnemonicInput, );
        }
        else
        {
            return new String[]{mnemonicInput};
        }
    }

    public boolean isMonthDay(String input)
    {
        return m_monthsOfTheYearList.contains(input);
    }

    public boolean isDayOfWeek(String input)
    {
        return m_daysOfTheWeekList.contains(input);
    }

    public boolean isDayOfMonth(String input)
    {
        try
        {
            Integer.parseInt(input);
            return true;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public int getMonthIndexFromString(String month)
    {
        if (month.equals(JAN_STRING))
            return 1;
        else if (month.equals(FEB_STRING))
            return 2;
        else if (month.equals(MAR_STRING))
            return 3;
        else if (month.equals(APR_STRING))
            return 4;
        else if (month.equals(MAY_STRING))
            return 5;
        else if (month.equals(JUN_STRING))
            return 6;
        else if (month.equals(JUL_STRING))
            return 7;
        else if (month.equals(AUG_STRING))
            return 8;
        else if (month.equals(SEP_STRING))
            return 9;
        else if (month.equals(OCT_STRING))
            return 10;
        else if (month.equals(NOV_STRING))
            return 11;
        else if (month.equals(DEC_STRING))
            return 12;
		else
            throw new InvalidMnemonicException("String argument does not match any known month mnemonic: " + month);
    }

    static int parseDaysOfWeek(String mnemonicInput) {
        if (mnemonicInput.equals(SUN_STRING)) return RulesCalendar.Sunday;
        if (mnemonicInput.equals(MON_STRING)) return RulesCalendar.Monday;
        if (mnemonicInput.equals(TUE_STRING)) return RulesCalendar.Tuesday;
        if (mnemonicInput.equals(WED_STRING)) return RulesCalendar.Wednesday;
        if (mnemonicInput.equals(THU_STRING)) return RulesCalendar.Thursday;
        if (mnemonicInput.equals(FRI_STRING)) return RulesCalendar.Friday;
        if (mnemonicInput.equals(SAT_STRING)) return RulesCalendar.Saturday;
        throw new InvalidMnemonicException("Not a valid Day of the Week Mnemonic: " + mnemonicInput);
    }
}
