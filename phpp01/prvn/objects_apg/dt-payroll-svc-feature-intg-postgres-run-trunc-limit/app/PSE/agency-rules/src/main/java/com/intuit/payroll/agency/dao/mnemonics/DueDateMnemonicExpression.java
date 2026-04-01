//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao.mnemonics;

import com.intuit.payroll.agency.util.RulesCalendar;
//import com.intuit.spc.foundations.primary.SpcfInteger;
//import com.intuit.spc.foundations.portability.SpcfNumberFormatException;
//import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;


class DueDateMnemonicExpression extends AbstractMnemonicExpression
{
    public void interpret(MnemonicContext context)
    {
        RulesCalendar dueDate = getDueDate(context);
        context.getOutput().setDueDate(dueDate);

        // Check to see if we need to roll the year
        // TODO: [th] this may impact the month if it's Feb and a leap year?
        if ((context.getOutput().getEndDate().getMonth() > context.getOutput().getDueDate().getMonth())
                && (context.getOutput().getDueDate().getYear() == context.getInput().getAccrualDate().getYear()))
        {
            context.getOutput().setDueDate(context.getOutput().getDueDate().addYears(1));
        }
    }

    protected RulesCalendar getDueDate(MnemonicContext context)
    {
        String mnemonicInput = context.getInput().getDueMnemonicInput();
        RulesCalendar periodEnd = context.getOutput().getEndDate();

        // Set a default DueDatePolicy that does not adjust the due date
        context.getOutput().setDueDateAdjustmentPolicy(new DueDatePolicy(false, 0, false));

        String[] parts = m_data.splitMnemonic(mnemonicInput);
        String part1 = parts[0].toUpperCase();
        if (part1.equals(MnemonicData.EOM))
        {
            // If the due date is EOM+2 (for example), then we roll the month that the
            // due date is at the end of 2 months from the month of the period end date.
            if (parts.length > 1)
            {
                String part2 = parts[1];
                try
                {
                    // Convert to a number
//                    int roll = SpcfInteger.parseInt(part2);
                    int roll = Integer.parseInt(part2);
                    return getMonthDay(periodEnd.getYear(), periodEnd.getMonth(), part1, roll);
                }
                catch(NumberFormatException fe)
//                catch(SpcfNumberFormatException fe)
                {
                    // If we've reached this point, the modifier to the EOM mnemonic
                    // is not a number. Throw an invalid mnemonic exception.
                    throw new InvalidMnemonicException("Invalid Due Date Mnemonic [EOM+x, the x modifier is NaN]: " + mnemonicInput, fe);
                }
            }
            else
            {
                return getMonthDay(periodEnd.getYear(), periodEnd.getMonth(), part1, 0);
            }
        }
        if (m_data.isMonthDay(part1))
        {
            if(periodEnd.getMonth() > m_data.getMonthIndexFromString(part1))
            { // If the due date rolls over a year boundary from the
                // period end, bump the year to ensure correct EOM calcs.
                return parseMonthDay(parts, periodEnd.getYear()+1);
            }
            else
            {
                return parseMonthDay(parts, periodEnd.getYear());
            }
        }
        else if (m_data.isDayOfWeek(part1))
        {
            int day = parseDaysOfWeek(part1);
            if (parts.length > 1)
            {
                String part2 = parts[1];
                //convert to a number
//                int roll = SpcfInteger.parseInt(part2);
                int roll = Integer.parseInt(part2);
                return getDayOfWeek(day, roll, periodEnd);
            }
            else
            {
                return getDayOfWeek(day, 0, periodEnd);
            }

        }
        else
        {
            try
            {
                return getDayOfMonth(part1, periodEnd);
            }
                // swallow exceptions that are thrown if the part is not a number

            catch(NumberFormatException e) {}
//            catch(SpcfNumberFormatException e) {}
            catch(IllegalArgumentException e) {}
//            catch(SpcfIllegalArgumentException e) {}
        }

        if (parts.length > 1)
        {
            String part2 = parts[1].toUpperCase();
            if (part2.endsWith(MnemonicData.NEXT_BUSINESS_DAY_TAG))
            {
                String numDays = part2.substring(0, part2.length() -2);	// java.lang.String doesn't have remove
//                int nextDays = SpcfInteger.parseInt(numDays);
                int nextDays = Integer.parseInt(numDays);

                // set the DueDatePolicy for use upstream.
                DueDatePolicy dueDatePolicy = new DueDatePolicy(true, nextDays, true);
                context.getOutput().setDueDateAdjustmentPolicy(dueDatePolicy);

                return periodEnd.addBusinessDays(nextDays, null /*holidays*/);
            }
            else if (part2.endsWith(MnemonicData.NEXT_NORMAL_DAY_TAG))
            {
                // return periodEnd.addDays(1);
                String numDays = part2.substring(0, part2.length() -2);	// java.lang.String doesn't have remove
//                int nextDays = SpcfInteger.parseInt(numDays);
                int nextDays = Integer.parseInt(numDays);

                // set the DueDatePolicy for use upstream.
                DueDatePolicy dueDatePolicy = new DueDatePolicy(true, nextDays, false);
                context.getOutput().setDueDateAdjustmentPolicy(dueDatePolicy);

                return periodEnd.addDays(nextDays);
            }
            else
            {
                throw new InvalidMnemonicException("Invalid Due Date Mnemonic: " + mnemonicInput);
            }
        }
        else
        {
            throw new InvalidMnemonicException("Invalid Due Date Mnemonic: " + mnemonicInput);
        }
    }

    private RulesCalendar getDayOfMonth( String part1, RulesCalendar periodEnd )
    {
//        int day = SpcfInteger.parseInt( part1 );
        int day = Integer.parseInt( part1 );
        int daysInCurrentMonth = periodEnd.getDaysInMonth();

        if(day > periodEnd.getDay() && day <= daysInCurrentMonth)
        {
            return RulesCalendar.createCalendar(periodEnd.getYear(),periodEnd.getMonth(),day);
        }
        else
        {
            RulesCalendar nextMonth = RulesCalendar.createCalendar (periodEnd);
            nextMonth = nextMonth.addMonths(1);
            int daysInNextMonth = nextMonth.getDaysInMonth();
            if(day > daysInNextMonth) { day = daysInNextMonth; }
            return RulesCalendar.createCalendar(nextMonth.getYear(),nextMonth.getMonth(),day);
        }
    }

    protected RulesCalendar getDayOfWeek(int dayOfWeek, int roll, RulesCalendar inputDate)
    {
        int periodEndDOW = inputDate.getDayOfWeek();

        int diff = dayOfWeek - periodEndDOW;

        RulesCalendar result = RulesCalendar.createCalendar (inputDate);
        result = result.addDays(diff);
        if (diff <= 0)
        {
            // roll to next week
            result = result.addWeeks(1);
        }

        // Apply any roll policy.
        if (roll > 0)
        {
            result = result.addWeeks(roll);
        }

        return result;
    }
}
