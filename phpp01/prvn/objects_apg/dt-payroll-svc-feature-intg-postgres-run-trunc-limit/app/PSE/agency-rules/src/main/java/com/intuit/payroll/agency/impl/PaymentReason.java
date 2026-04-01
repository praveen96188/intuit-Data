//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IPaymentReason;
/// <summary>
/// Implementation of IPaymentReason interface.
/// </summary>
public class PaymentReason implements IPaymentReason {
	private String m_paymentReasonCode = "";
	private boolean m_isObsolete = false;
	private boolean m_isValid = true;
	private String m_description = "";
	private boolean m_isTaxItemAllowed = false;
	private boolean m_isPenaltyAllowed = false;
	private boolean m_isInterestAllowed = false;
	private boolean m_isOtherExpenseAllowed = false;

	/// <summary>
	/// Payment reason code to send to agency.
	/// </summary>
	public String getPaymentReasonCode()
	{
		 return m_paymentReasonCode; 
	}
	public void setPaymentReasonCode(String that)
	{
		m_paymentReasonCode = that;
	}
	/// <summary>
	/// True if this PaymentReason is obsolete.
	/// </summary>
	public boolean getIsObsolete()
	{
		 return m_isObsolete; 
	}
	public void setIsObsolete(boolean that)
	{
		m_isObsolete = that;
	}
	public void setIsObsolete(String xml)
	{
		m_isObsolete = PaymentTemplate.valueAsBoolean(xml);
	}


	/// <summary>
	/// Read-only property that indicates if this object is valid or not.
	/// If IsValid is true, it means that this object contains valid data.
	/// If IsValid is false, it means that this object was not initialized.
	/// </summary>
	public boolean getIsValid()
	{
		 return m_isValid; 
	}
	public void setIsValid(boolean that)
	{
		m_isValid = that;
	}

	/// <summary>
	/// Payment reason text for the UI.
	/// </summary>
	public String getDescription()
	{
		 return m_description; 
	}
	public void setDescription(String that)
	{
		m_description = that;
	}

	/// <summary>
	/// Is tax item allowed?
	/// </summary>
	public boolean getIsTaxItemAllowed()
	{
		 return m_isTaxItemAllowed; 
	}
	public void setIsTaxItemAllowed(boolean that)
	{
		m_isTaxItemAllowed = that;
	}
	public void setIsTaxItemAllowed(String xml)
	{
		m_isTaxItemAllowed = PaymentTemplate.valueAsBoolean(xml);
	}

	/// <summary>
	/// Is penalty allowed?
	/// </summary>
	public boolean getIsPenaltyAllowed()
	{
		 return m_isPenaltyAllowed; 
	}
	public void setIsPenaltyAllowed(boolean that)
	{
		m_isPenaltyAllowed = that;
	}
	public void setIsPenaltyAllowed(String xml)
	{
		m_isPenaltyAllowed = PaymentTemplate.valueAsBoolean(xml);
	}

	/// <summary>
	/// Is interest allowed?
	/// </summary>
	public boolean getIsInterestAllowed()
	{
		 return m_isInterestAllowed; 
	}
	public void setIsInterestAllowed(boolean that)
	{
		m_isInterestAllowed = that;
	}
	public void setIsInterestAllowed(String xml)
	{
		m_isInterestAllowed = PaymentTemplate.valueAsBoolean(xml);
	}

	/// <summary>
	/// Is other expense allowed?
	/// </summary>
	public boolean getIsOtherExpenseAllowed()
	{
		 return m_isOtherExpenseAllowed; 
	}
	public void setIsOtherExpenseAllowed(boolean that)
	{
		m_isOtherExpenseAllowed = that;
	}
	public void setIsOtherExpenseAllowed(String xml)
	{
		m_isOtherExpenseAllowed = PaymentTemplate.valueAsBoolean(xml);
	}
}
