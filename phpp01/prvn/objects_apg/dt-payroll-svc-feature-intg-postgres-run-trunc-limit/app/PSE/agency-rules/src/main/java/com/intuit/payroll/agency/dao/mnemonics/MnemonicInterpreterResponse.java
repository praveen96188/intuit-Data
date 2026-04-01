//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

	/// <summary>
	/// A messenger object containing the dates created
	/// when the mnemonic interpreter changes mnemonic date
	/// Strings into RulesCalendar objects.
	/// </summary>

package com.intuit.payroll.agency.dao.mnemonics;

import com.intuit.payroll.agency.util.RulesCalendar;
//import com.intuit.spc.foundations.portability.SpcfSystem;
//import com.intuit.spc.foundations.portability.SpcfStringBuilder;
//import com.intuit.spc.foundations.portability.SpcfFactory;

public class MnemonicInterpreterResponse
{
    private RulesCalendar m_startDate;
    /// <summary>
    /// The date the period starts on.
    /// </summary>
    public RulesCalendar getStartDate ()
    {
        return m_startDate;
    }

    public void setStartDate(RulesCalendar that)
    {
        m_startDate = that;
    }

    private RulesCalendar m_endDate;
    /// <summary>
    /// The date the period ends on.
    /// </summary>
    public RulesCalendar getEndDate()
    {
        return m_endDate;
    }

    public void setEndDate (RulesCalendar that)
    {
        m_endDate = that;
    }

    private RulesCalendar m_dueDate;
    /// <summary>
    /// The due date of the period represented
    /// by the start and end dates.
    /// </summary>
    public RulesCalendar getDueDate()
    {
        return m_dueDate;
    }

    public void setDueDate(RulesCalendar that)
    {
        m_dueDate = that;
    }

    private DueDatePolicy m_dueDatePolicy;

    /// <summary>
    /// The Due Date policy for use upstream if further adjustments have to be
    /// made to the period start/end.
    /// </summary>
    public DueDatePolicy getDueDateAdjustmentPolicy()
    {
        return m_dueDatePolicy;
    }

    /// <summary>
    /// The Due Date policy for use upstream if further adjustments have to be
    /// made to the period start/end.
    /// </summary>
    public void setDueDateAdjustmentPolicy(DueDatePolicy that)
    {
        m_dueDatePolicy = that;
    }

    /// <summary>
    /// Default constructor.
    /// </summary>
    public MnemonicInterpreterResponse()
    {}

    /// <summary>
    /// Constructor with the full start, end, and due dates.
    /// </summary>
    /// <param name="startDate">Period start date</param>
    /// <param name="endDate">Period end date</param>
    /// <param name="dueDate">Due date</param>
    public MnemonicInterpreterResponse(RulesCalendar startDate, RulesCalendar endDate, RulesCalendar dueDate)
    {
        m_startDate = startDate;
        m_endDate = endDate;
        m_dueDate = dueDate;
    }

    /// <summary>
    /// Override of Object.toString() to facilitate output
    /// of the MnemonicInterpreterResponse to a String value.
    /// </summary>
    /// <returns>String dump of the object.</returns>
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
//        SpcfStringBuilder builder = SpcfFactory.getInstance().createStringBuilder();
        builder.append("Start Date: ");
        builder.append(m_startDate);
//        builder.append(SpcfSystem.getNewLine());
        builder.append(System.getProperty("line.separator"));
        builder.append("End Date: ");
        builder.append(m_endDate);
//        builder.append(SpcfSystem.getNewLine());
        builder.append(System.getProperty("line.separator"));
        builder.append("Due Date: ");
        builder.append(m_dueDate);

        return builder.toString();
    }
}
