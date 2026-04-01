//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

import com.intuit.payroll.agency.util.RulesCalendar;

/// <summary>
/// Interface for the PaymentPeriod object. This interface will be exposed through COM.
/// </summary>
public interface IPaymentPeriod
{
	/// <summary>
	/// Due date of the payment period.
	/// </summary>
	RulesCalendar getDueDate();
	void setDueDate(RulesCalendar that);

	/// <summary>
	/// From accrual date of the payment period.
	/// </summary>
	RulesCalendar getFromAccrualDate();
	void setFromAccrualDate(RulesCalendar that);

	/// <summary>
	/// To accrual date of the payment period.
	/// </summary>
	RulesCalendar getToAccrualDate();
	void setToAccrualDate(RulesCalendar that);

	/// <summary>
	/// The short UI-consumable String that describes the
	/// period. Examples: 'Dec 05', 'Q4', etc.
	/// </summary>
	String getUIString();
	void setUIString(String that);

}
