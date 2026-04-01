//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.util.DateRollingPolicy;

/// <summary>
/// Summary description for IPaymentPeriodRequest.
/// </summary>
public interface IPaymentPeriodRequest
{
	/// <summary>
	/// The type of the request so it can be handled by the
	/// correct subsystem.
	/// </summary>
	PaymentPeriodRequestType getRequestType ();
	void setRequestType (PaymentPeriodRequestType that);

	/// <summary>
	/// The id of the payment template to retrieve the
	/// payment period info from.
	/// </summary>
	String getPaymentTemplateId ();
	void setPaymentTemplateId (String that);

	/// <summary>
	/// The frequency id to retrieve the payment period
	/// info for.
	/// </summary>
	String getFrequencyId ();
	void setFrequencyId (String that);

	/// <summary>
	/// The submit method id to retrieve the payment 
	/// period for.
	/// </summary>
	String getSubmitMethodId ();
	void setSubmitMethodId (String that);

	/// <summary>
	/// The date that the accrual occurred on; used to
	/// calculate the concrete dates of the payment period and due
	/// date.
	/// </summary>
	RulesCalendar getAccrualDate ();
	void setAccrualDate (RulesCalendar that);

	/// <summary>
	/// The date rolling policy to apply to the due date in
	/// case it falls on a weekend or holiday.
	/// </summary>
	DateRollingPolicy getDateRollingPreference ();
	void setDateRollingPreference (DateRollingPolicy that);

	/// <summary>
	/// The String encoding of the client's due 
	/// date preference.
	/// </summary>
	String getDueDatePreference ();
	void setDueDatePreference (String that);

	/// <summary>
	/// This number can represent the day of the week, the day of the month 
	/// depending on the context of the UserFrequency choice.
	/// </summary>
	int getUserScheduleNum ();
	void setUserScheduleNum (int that);

	/// <summary>
	/// This date provides context for the calculations that use ScheduleNum and Frequency
	/// to calculate next due date.
	/// </summary>
	RulesCalendar getUserScheduleDate ();
	void setUserScheduleDate (RulesCalendar that);

	/// <summary>
	/// This is the Frequency the the user has chosen to apply to a particular 
	/// payment.
	/// </summary>
	UserFrequencyType getUserFrequency ();
	void setUserFrequency (UserFrequencyType that);

}
