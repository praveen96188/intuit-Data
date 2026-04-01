//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

package com.intuit.payroll.agency.dao.mnemonics;

import com.intuit.payroll.agency.util.RulesCalendar;
//import com.intuit.spc.foundations.primary.SpcfInteger;

class EndDateMnemonicExpression extends AbstractMnemonicExpression
{
    public void interpret(MnemonicContext context)
    {
        RulesCalendar endDate = getEndDate(context.getInput().getEndMnemonicInput(), context.getOutput().getStartDate());
        context.getOutput().setEndDate (endDate);
    }

    protected RulesCalendar getDayOfWeek(int dayOfWeek, int roll, RulesCalendar inputDate)
    {
        /*
        * 1-7 (Java not .net convention)
        * SUN	MON		TUE		WED		THU		FRI		SAT
        */
        int startDateDOW = inputDate.getDayOfWeek();
        // We are trying to place the end date after or equal to the accrual Date.
        // If the difference is less than zero, the day of the week is AHEAD of us, so we go there, then add a week.
        // If there is no difference, then we are on the right day.
        // If the difference is positive, the day of the weeks is BEHIND us, so we just go there.
        int diff = dayOfWeek - startDateDOW;
        RulesCalendar result = RulesCalendar.createCalendar(inputDate);
        result = result.addDays(diff);
        if (diff < 0)
        {
            // roll to next week
            result = result.addWeeks(1);
        }

        return result;
    }

    protected RulesCalendar getEndDate(String mnemonicInput, RulesCalendar startDate)
    {
        String[] parts = m_data.splitMnemonic(mnemonicInput);
        String part1 = parts[0].toUpperCase();
        if (part1.equals(MnemonicData.EOM))
        {
            return getMonthDay(startDate.getYear(), startDate.getMonth(), part1, 0);
        }
        if (part1.equals(MnemonicData.PAYDATE))
        {
            return startDate;
        }
        if (m_data.isMonthDay(part1))
        {
            RulesCalendar temp = parseMonthDay(parts, startDate.getYear());
            if(temp.before(startDate))
            {
                temp = temp.addYears(1);
            }
            return temp;
        }
        else if (m_data.isDayOfWeek(part1))
        {
            int day =  parseDaysOfWeek(mnemonicInput);
            return getDayOfWeek(day, 0, startDate);
        }
        else if (m_data.isDayOfMonth(part1))
        {
//            int day = SpcfInteger.parseInt(part1);
            int day = Integer.parseInt(part1);
            return RulesCalendar.createCalendar(startDate.getYear(), startDate.getMonth(), day);

        }
        else
        {
            throw new InvalidMnemonicException("Invalid Start or End Period Mnemonic: " + mnemonicInput);
        }
    }
}
