//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.util.RulesCalendar;
/// <summary>
/// An data interface that holds information about the
/// EFE filing service itself..
/// </summary>
public interface IEfeRules
{
	/// <summary>
	/// get the number of days to adjust the
	/// due date backwards (closer to accrual date)
	/// in order to comply with the EFE filing
	/// deadlines.
	/// </summary>
	/// <example>-1 would mean 1 day prior to 
	/// the due date</example>
	int getDaysOffsetFromAgencyDueDate();
	
	/// <summary>
	/// Encodes the filing time deadline (in the
	/// timezone referred to by the TimeZone property)
	/// to file with the EFE service by.
	/// </summary>
	/// <example>A 9:00pm time encoded in this this 
	/// property would mean that the payment would have
	/// to be submitted by 9pm in the TimeZone to 
	/// make it "on time".</example>
	RulesCalendar getTimeDue();
	
	/// <summary>
	/// Encodes the reference time zone to use for 
	/// EFE server filing deadlines.
	/// </summary>
	String getTimeZone();
}
