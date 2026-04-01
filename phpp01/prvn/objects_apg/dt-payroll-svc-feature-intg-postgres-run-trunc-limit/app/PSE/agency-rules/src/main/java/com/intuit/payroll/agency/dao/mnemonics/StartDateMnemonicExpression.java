//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

package com.intuit.payroll.agency.dao.mnemonics;

import com.intuit.payroll.agency.util.RulesCalendar;
//import com.intuit.spc.foundations.primary.SpcfInteger;

/// <summary>
/// Interprets the Start Date Mnemonic expression.
/// </summary>
class StartDateMnemonicExpression extends AbstractMnemonicExpression
{
    public void interpret(MnemonicContext context)
    {
        RulesCalendar startDate = getStartDate(
                context.getInput().getStartMnemonicInput(), context.getInput().getAccrualDate());
        context.getOutput().setStartDate(startDate);
    }

    protected RulesCalendar getDayOfWeek(int dayOfWeek, int roll, RulesCalendar inputDate)
    {
        /*
        * 1-7 (Java not .net convention)
        * SUN	MON		TUE		WED		THU		FRI		SAT
        */
        int accrualDateDOW = inputDate.getDayOfWeek();
        // We are trying to place the start date before or equal to the accrual Date.
        // If the difference is less than zero, the day of the week is AHEAD of us, so we go there, then remove a week.
        // If there is no difference, then we are on the right day.
        // If the difference is positive, the day of the weeks is BEHIND us, so we just go there.
        int diff = dayOfWeek - accrualDateDOW;
        RulesCalendar result = RulesCalendar.createCalendar (inputDate);
        result = result.addDays(diff);
        if (diff > 0)
        {
            // roll to previous week.
            result = result.addWeeks(-1);
        }
        return result;
    }

    protected RulesCalendar getStartDate(String mnemonicInput, RulesCalendar accrualDate)
    {
        String[] parts = m_data.splitMnemonic(mnemonicInput);
        String part1 = parts[0].toUpperCase();
        if (part1.equals(MnemonicData.EOM))
        {
            return getMonthDay(accrualDate.getYear(), accrualDate.getMonth(), part1, 0);
        }
        if (part1.equals(MnemonicData.PAYDATE))
        {
            return accrualDate;
        }
        if (m_data.isMonthDay(part1))
        {
            RulesCalendar temp = parseMonthDay(parts, accrualDate.getYear());
            if(temp.after(accrualDate))
            {
                temp = temp.addYears(-1);
            }
            return temp;
        }
        else if (m_data.isDayOfWeek(part1))
        {
            int day =  parseDaysOfWeek(mnemonicInput);
            return getDayOfWeek(day, 0, accrualDate);
        }
        else if (m_data.isDayOfMonth(part1))
        {
//            int day = SpcfInteger.parseInt(part1);
            int day = Integer.parseInt(part1);
            return RulesCalendar.createCalendar(accrualDate.getYear(), accrualDate.getMonth(), day);

        }
        else
        {
            throw new InvalidMnemonicException("Invalid Start or End Period Mnemonic: " + mnemonicInput);
        }
    }
}
