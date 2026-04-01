//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.util;

import com.intuit.payroll.agency.util.DateRollingPolicy;
/// <summary>
/// An object that encapsulates various values that can
/// be interpreted by calculators and decision trees in
/// order to make decisions about the due date of a payment.
/// </summary>
public class DueDateRollingPolicy
{
	private DateRollingPolicy weekendDateRollPolicy = DateRollingPolicy.Forward;
	private DateRollingPolicy holidayDateRollPolicy = DateRollingPolicy.Forward;

	public DateRollingPolicy getWeekendDateRollPolicy()
	{
			return weekendDateRollPolicy; 
	}
	public void setWeekendDateRollPolicy(DateRollingPolicy that)
	{
		weekendDateRollPolicy = that;
	}

	public DateRollingPolicy getHolidayDateRollPolicy()
	{
			return holidayDateRollPolicy; 
	}
	public void setHolidayDateRollPolicy(DateRollingPolicy that)
	{
		holidayDateRollPolicy = that;
	}

	public DueDateRollingPolicy()
	{
	}
}
