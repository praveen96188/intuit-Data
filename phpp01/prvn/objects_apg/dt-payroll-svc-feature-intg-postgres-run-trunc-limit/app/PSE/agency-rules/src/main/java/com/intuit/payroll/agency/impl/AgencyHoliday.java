//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.util.IAgencyHoliday;
import com.intuit.payroll.agency.util.RulesCalendar;
/// <summary>
/// Default implementation of IAgencyHoliday..
/// </summary>
public class AgencyHoliday implements IAgencyHoliday {
	private RulesCalendar holidayDateField;
	private String uiStringField;
	
	/// <summary>
	/// Default Constructor.
	/// </summary>
	public AgencyHoliday()
	{
	}


	/// <summary>
	/// The date of the holiday.
	/// </summary>
	public RulesCalendar getHolidayDate()
	{
		 return holidayDateField; 
	}
	public void setHolidayDate(String xml)
	{
        holidayDateField = RulesCalendar.createCalendar(xml);
	}

	public void setHolidayDate(RulesCalendar that)
	{
		holidayDateField = that;

	}

	/// <summary>
	/// The description of the holiday.
	/// </summary>
	public String getUIString()
	{
		 return uiStringField;
	}
	public void setUIString(String that)
	{
		uiStringField = that;
	}

	
}
