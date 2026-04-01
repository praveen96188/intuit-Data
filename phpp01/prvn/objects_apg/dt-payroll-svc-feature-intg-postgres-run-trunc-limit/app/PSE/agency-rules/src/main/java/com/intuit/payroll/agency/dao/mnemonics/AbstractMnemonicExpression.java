//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

	/// <summary>
	/// Base class for the Mnemonic Expression Interpreter suite of classes.
	/// </summary>

package com.intuit.payroll.agency.dao.mnemonics;

import com.intuit.payroll.agency.util.RulesCalendar;

abstract class AbstractMnemonicExpression
{
    protected MnemonicData m_data;
    /// <summary>
    /// Used as a Date utility helper.
    /// </summary>

    public AbstractMnemonicExpression()
    {
        m_data = new MnemonicData();
    }

    /// <summary>
    /// Main method that subclasses must override.
    /// </summary>
    /// <param name="context">The context object containing the input and output variables.</param>
    public abstract void interpret(MnemonicContext context);

    protected abstract RulesCalendar getDayOfWeek(int dayOfWeek, int roll, RulesCalendar inputDate);

    protected RulesCalendar parseMonthDay(String[] parts, int year)
    {
        String part1 = parts[0].toUpperCase();
        String part2 = parts[1].toUpperCase();
        return getMonthDay(year, m_data.getMonthIndexFromString(part1), part2, 0);
    }

    protected RulesCalendar getMonthDay(int year, int month, String part, int roll)
    {
        int dayNum = 0;
        month = getMonthOffset(month, roll);
        try
        {
            if (part.equals(MnemonicData.EOM))
            {
                // Jump to the last day of the month.
                RulesCalendar cal = RulesCalendar.createCalendar (year, month, 1);
                dayNum = cal.getDaysInMonth();
            }
            else
            {
                // Love it for what it is. Hopefully it's a number.
                dayNum = Integer.parseInt(part);
            }

            return RulesCalendar.createCalendar (year, month, dayNum);
        }
        catch (Exception e)
        {
            throw new InvalidMnemonicException("The Day is out of range: " + dayNum, e);
        }

    }

    /// <summary>
    /// Given a known good month index, calculate the new month
    /// given an integer offset.
    /// </summary>
    /// <param name="month">The 1-based index of the month to calculate
    /// the offset from.</param>
    /// <param name="roll">The number of months to offset.</param>
    /// <returns>A new 1-based index of the offset month.</returns>
    private int getMonthOffset(int month, int roll)
    {
        RulesCalendar cal = RulesCalendar.createCalendar(1900, month, 1); // exact year doesn't matter here
        cal = cal.addMonths(roll);
        return cal.getMonth();
    }

    protected int parseDaysOfWeek(String mnemonicInput)
    {
        return MnemonicData.parseDaysOfWeek(mnemonicInput);
    }
}

