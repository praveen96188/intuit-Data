package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.util.RulesCalendar;
/// <summary>
/// Summary description for SettlementDateContext.
/// </summary>
public class SettlementDateRequest
{
	private RulesCalendar todayField;
	private boolean isStartDateField;
	private String paymentTemplateIdField;
	private SettlementDatePolicy settlementDatePolicyField;

	/// <summary>
	/// The date to start the calculations from.
	/// </summary>
	public RulesCalendar getToday()
	{
		return todayField;
	}

	/// <summary>
	/// Indicates if this is a start date calculation.
	/// </summary>
	public boolean getIsStartDate()
	{
		return isStartDateField;
	}
	public void setIsStartDate(boolean that)
	{
		isStartDateField = that;
	}

	/// <summary>
	/// The PaymentTemplateId.
	/// </summary>
	public String getPaymentTemplateId()
	{
		return paymentTemplateIdField;
	}
	public void setPaymentTemplateId(String that)
	{
		paymentTemplateIdField = that;
	}

	public SettlementDatePolicy getSettlementDatePolicy()
	{
		return settlementDatePolicyField;
	}
	public void setSettlementDatePolicy(SettlementDatePolicy that)
	{
		settlementDatePolicyField = that;
	}

	/// <summary>
	/// Default constructor with all fields initialized.
	/// </summary>
	/// <param name="today">Date to start calculations from.</param>
	/// <param name="isStartDate">Indicates if this is a start date calculation.</param>
	/// <param name="paymentTemplateId">The PaymentTemplateId</param>
	/// <param name="policy">The SettlementDatePolicy in effect</param>
	public SettlementDateRequest(RulesCalendar today, boolean isStartDate, String paymentTemplateId, SettlementDatePolicy policy)
	{
		todayField = today;
		isStartDateField = isStartDate;
		paymentTemplateIdField = paymentTemplateId;
		settlementDatePolicyField = policy;
	}


}
