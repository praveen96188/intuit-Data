//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.dao.mnemonics.DueDatePolicy;
import com.intuit.payroll.agency.api.IPaymentPeriod;

/// <summary>
/// Implementation of the PaymentPeriod interface.
/// </summary>
public class PaymentPeriod implements IPaymentPeriod
{
	private RulesCalendar m_dueDate;
	private RulesCalendar m_fromAccrualDate;
	private RulesCalendar m_toAccrualDate;
	private String m_uiString;
	private DueDatePolicy m_dueDatePolicy;

	/// <summary>
	/// Due date of the payment period.
	/// </summary>
	public RulesCalendar getDueDate()
	{
		 return m_dueDate; 
	}
	public void setDueDate(RulesCalendar that)
	{
		m_dueDate = that;
	}

	public void setDueDate(String xml)
	{
        m_dueDate = RulesCalendar.createCalendar(xml);
	}


	/// <summary>
	/// From accrual date of the payment period.
	/// </summary>
	public RulesCalendar getFromAccrualDate()
	{
		 return m_fromAccrualDate; 
	}
	public void setFromAccrualDate(RulesCalendar that)
	{
		m_fromAccrualDate = that;
	}
	public void setFromAccrualDate(String xml)
	{
        m_fromAccrualDate = RulesCalendar.createCalendar(xml);
	}

	/// <summary>
	/// To accrual date of the payment period.
	/// </summary>
	public RulesCalendar getToAccrualDate()
	{
		 return m_toAccrualDate; 
	}
	public void setToAccrualDate(RulesCalendar that)
	{
		m_toAccrualDate = that;
	}
	public void setToAccrualDate(String xml)
	{
        m_toAccrualDate = RulesCalendar.createCalendar(xml);
	}



	/// <summary>
	/// The short UI-consumable String that describes the
	/// period. Examples: 'Dec 05', 'Q4', etc.
	/// </summary>
	public String getUIString()
	{
		 return m_uiString; 
	}
	public void setUIString(String that)
	{
		m_uiString = that;
	}

	public void parseUIString()
	{
		m_uiString = m_toAccrualDate.parseUIString(m_uiString);
	}

	public DueDatePolicy getDueDatePolicy()
	{
		 return m_dueDatePolicy; 
	}
	public void setDueDatePolicy(DueDatePolicy that)
	{
		m_dueDatePolicy = that;
	}

	/// <summary>
	/// If blank, put the begin and end accrual period in the UIString.
	/// </summary>
	public void fixupUIString ()
	{
		if (m_uiString == null || m_uiString.length() == 0)
		{
			String fromStr = m_fromAccrualDate.toString("M/d-");
			String toStr = m_toAccrualDate.toString("M/d/yy");
			m_uiString = fromStr+toStr;
		}
	}
	


}
