//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.util.DateRollingPolicy;
import com.intuit.payroll.agency.api.PaymentPeriodRequestType;
import com.intuit.payroll.agency.api.UserFrequencyType;
import com.intuit.payroll.agency.util.RulesCalendar;
/// <summary>
/// Summary description for PaymentPeriodRequest.
/// </summary>
public class PaymentPeriodRequest implements IPaymentPeriodRequest {
	private PaymentPeriodRequestType m_paymentPeriodRequestType = PaymentPeriodRequestType.Unknown;
	private String m_paymentTemplateId;
	private String m_frequencyId;

	/// <summary>
	/// Default Value is "PRINT"
	/// TODO: [th] put this in the resources.
	/// </summary>
	private String m_submitMethodId = "PRINT";

	private RulesCalendar m_accrualDate;
	private DateRollingPolicy m_dateRollingPreference = DateRollingPolicy.Forward;
	private String m_dueDatePreference;
	private int m_userScheduleNum;
	private RulesCalendar m_userScheduleDate;
	private UserFrequencyType m_userFrequencyType = UserFrequencyType.NoSchedule;

	/// <summary>
	/// Default constructor. Does nothing.
	/// </summary>
	public PaymentPeriodRequest()
	{
	}


	/// <summary>
	/// The type of the request so it can be handled by the
	/// correct subsystem.
	/// </summary>
	public PaymentPeriodRequestType getRequestType()
	{
		 return m_paymentPeriodRequestType; 
	}
	public void setRequestType(PaymentPeriodRequestType that)
	{
		m_paymentPeriodRequestType = that;
	}

	/// <summary>
	/// The id of the payment template to retrieve the
	/// payment period info from.
	/// </summary>
	public String getPaymentTemplateId()
	{
		 return m_paymentTemplateId; 
	}
	public void setPaymentTemplateId(String that)
	{
		m_paymentTemplateId = that;
	}

	/// <summary>
	/// The frequency id to retrieve the payment period
	/// info for.
	/// </summary>
	public String getFrequencyId ()
	{
			return m_frequencyId; 
	}
	public void setFrequencyId (String that)
	{
		m_frequencyId = that;
	}

	/// <summary>
	/// The submit method id to retrieve the payment 
	/// period for.
	/// </summary>
	public String getSubmitMethodId ()
	{
		 return m_submitMethodId; 
	}
	public void setSubmitMethodId (String that)
	{
		m_submitMethodId = that;
	}

	/// <summary>
	/// The date that the accrual occurred on; used to
	/// calculate the concrete dates of the payment period and due
	/// date.
	/// </summary>
	public RulesCalendar getAccrualDate ()
	{
		 return m_accrualDate; 
	}
	public void setAccrualDate (RulesCalendar that)
	{
		m_accrualDate = that;
	}

	/// <summary>
	/// The date rolling policy to apply to the due date in
	/// case it falls on a weekend or holiday.
	/// </summary>
	public DateRollingPolicy getDateRollingPreference ()
	{
		 return m_dateRollingPreference; 
	}
	public void setDateRollingPreference (DateRollingPolicy that)
	{
		m_dateRollingPreference = that;
	}

	/// <summary>
	/// The String encoding of the client's due 
	/// date preference.
	/// </summary>
	public String getDueDatePreference ()
	{
		 return m_dueDatePreference; 
	}
	public void setDueDatePreference (String that)
	{
		m_dueDatePreference = that;
	}

	/// <summary>
	/// This number can represent the day of the week, the day of the month 
	/// depending on the context of the UserFrequency choice.
	/// </summary>
	public int getUserScheduleNum()
	{ 
		 return m_userScheduleNum; 
	}
	public void setUserScheduleNum(int that)
	{
		m_userScheduleNum = that;
	}

	/// <summary>
	/// This date provides context for the calculations that use ScheduleNum and Frequency
	/// to calculate next due date.
	/// </summary>
	public RulesCalendar getUserScheduleDate ()
	{ 
		 return m_userScheduleDate; 
	}
	public void setUserScheduleDate (RulesCalendar that)
	{
		m_userScheduleDate = that;
	}

	/// <summary>
	/// This is the Frequency the the user has chosen to apply to a particular 
	/// payment.
	/// </summary>
	public UserFrequencyType getUserFrequency()
	{ 
		 return m_userFrequencyType; 
	}
	public void setUserFrequency(UserFrequencyType that)
	{
		m_userFrequencyType = that;
	}

	
}
