//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.util.RulesCalendar;
/// <summary>
/// Implementation of the  IEfeRules data
/// interface.
/// </summary>
public class EfeRules implements IEfeRules
{
	private String m_timeZone;
	private RulesCalendar m_timeDue;
	private int m_daysOffsetFromAgencyDueDate = 0;

	/// <summary>
	/// get the number of days to adjust the
	/// due date backwards (closer to accrual date)
	/// in order to comply with the EFE filing
	/// deadlines.
	/// </summary>
	/// <example>-1 would mean 1 day prior to 
	/// the due date</example>
	public int getDaysOffsetFromAgencyDueDate()
	{
		 return m_daysOffsetFromAgencyDueDate; 
	}
	public void setDaysOffsetFromAgencyDueDate(int that)
	{
		m_daysOffsetFromAgencyDueDate = that;
	}

	/// <summary>
	/// Encodes the filing time deadline (in the
	/// timezone referred to by the TimeZone property)
	/// to file with the EFE service by.
	/// </summary>
	/// <example>A 9:00pm time encoded in this this 
	/// property would mean that the payment would have
	/// to be submitted by 9pm in the TimeZone to 
	/// make it "on time".</example>
	public RulesCalendar getTimeDue()
	{
		 return m_timeDue; 
	}
	public void setTimeDue(String xml)
	{
		m_timeDue = RulesCalendar.createCalendar(xml);
	}

	/// <summary>
	/// Encodes the reference time zone to use for 
	/// EFE server filing deadlines.
	/// </summary>
	public String getTimeZone()
	{
		 return m_timeZone; 
	}
	public void setTimeZone(String that)
	{
		m_timeZone = that;
	}
}
