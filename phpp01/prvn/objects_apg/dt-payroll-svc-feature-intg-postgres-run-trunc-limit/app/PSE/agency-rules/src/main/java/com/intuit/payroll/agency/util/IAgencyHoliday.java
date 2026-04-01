//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

/// <summary>
/// Summary description for IAgencyHoliday.
/// </summary>

package com.intuit.payroll.agency.util;

import com.intuit.payroll.agency.util.RulesCalendar;

public interface IAgencyHoliday
{
	/// <summary>
	/// The date of the holiday.
	/// </summary>
	RulesCalendar getHolidayDate();

	/// <summary>
	/// The description of the holiday.
	/// </summary>
	String getUIString();
}
