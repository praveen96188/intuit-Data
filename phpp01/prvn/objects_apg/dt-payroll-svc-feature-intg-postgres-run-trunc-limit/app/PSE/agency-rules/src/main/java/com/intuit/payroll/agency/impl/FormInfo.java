//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IFormInfo;
/// <summary>
/// The internal implementation of the IFormInfo interface.
/// </summary>
public class FormInfo implements IFormInfo {
	private boolean m_requiresEmployeeMonthsWorked;
	private boolean m_requiresHoursWorked;
	private boolean m_requiresTipsIncome;
	private boolean m_requiresEmployeeInfo;
	private boolean m_subscriberOnly;
	private boolean m_is1099;
	private boolean m_isW2;
	private boolean m_enhancedQBOnly;
	private boolean m_preprinted;
	private boolean m_requiresSSN;
	private String m_dataBreakoutPeriod;
	private String m_formCategory;
	private String m_formName;
	private String m_formId;
	private String m_formSetId;

	/// <summary>
	/// Identifies the form set the form belongs to.
	/// </summary>
	/// <example>"STATE" or "FED"</example>
	public String getFormSetID()
	{
		 return m_formSetId; 
	}
	public void setFormSetID(String that)
	{
		m_formSetId = that;
	}

	/// <summary>
	/// The TPS form id.
	/// </summary>
	public String getFormID()
	{
		 return m_formId; 
	}
	public void setFormID(String that)
	{
		m_formId = that;
	}

	/// <summary>
	/// The customer-facing name of the form.
	/// </summary>
	/// <remarks>
	/// This value can be used in customer-facing user
	/// inetrfaces and messages.
	/// </remarks>
	public String getFormName()
	{
		 return m_formName; 
	}
	public void setFormName(String that)
	{
		m_formName = that;
	}

	/// <summary>
	/// The category of the form.
	/// </summary>
	/// <remarks>This field is present to retain
	/// backwards compatibility with Denali forms.</remarks>
	public String getFormCategory()
	{
		 return m_formCategory; 
	}
	public void setFormCategory(String that)
	{
		m_formCategory = that;
	}

	/// <summary>
	/// Defines how data must be totaled.
	/// </summary>
	/// <example>
	/// "Daily" - 941B requires daily breakdowns even 
	/// though it is filed quarterly.
	/// </example>
	public String getDataBreakoutPeriod()
	{
		 return m_dataBreakoutPeriod; 
	}
	public void setDataBreakoutPeriod(String that)
	{
		m_dataBreakoutPeriod = that;
	}

	/// <summary>
	/// Does the form require SSN numbers in order
	/// to be processed?
	/// </summary>
	public boolean getRequiresSSN()
	{
		 return m_requiresSSN; 
	}
	public void setRequiresSSN(boolean that)
	{
		m_requiresSSN = that;
	}

	/// <summary>
	/// Does the form support preprinted form printing?
	/// </summary>
	public boolean getPreprinted()
	{
		 return m_preprinted; 
	}
	public void setPreprinted(boolean that)
	{
		m_preprinted = that;
	}

	/// <summary>
	/// Is this form available only in enhanced QuickBooks?
	/// </summary>
	/// <remarks>
	/// TODO [zjm] I don't really understand this field. Jim,
	/// could you comment further or correct my explanation.
	/// </remarks>
	public boolean getEnhancedQBOnly()
	{
		 return m_enhancedQBOnly; 
	}
	public void setEnhancedQBOnly(boolean that)
	{
		m_enhancedQBOnly = that;
	}

	/// <summary>
	/// Is the form a W2?
	/// </summary>
	public boolean getIsW2()
	{
		 return m_isW2; 
	}
	public void setIsW2(boolean that)
	{
		m_isW2 = that;
	}

	/// <summary>
	/// Is the form a 1099?
	/// </summary>
	public boolean getIs1099()
	{
		 return m_is1099; 
	}
	public void setIs1099(boolean that)
	{
		m_is1099 = that;
	}

	/// <summary>
	/// Is the form available only to a subscriber of
	/// the payroll service?
	/// </summary>
	/// <remarks>
	/// TODO [zjm] I don't really understand this field. Jim,
	/// could you comment further or correct my explanation.
	/// </remarks>
	public boolean getSubscriberOnly()
	{
		 return m_subscriberOnly; 
	}
	public void setSubscriberOnly(boolean that)
	{
		m_subscriberOnly = that;
	}

	/// <summary>
	/// Does this form require employee information?
	/// </summary>
	public boolean getRequiresEmployeeInfo()
	{
		 return m_requiresEmployeeInfo; 
	}
	public void setRequiresEmployeeInfo(boolean that)
	{
		m_requiresEmployeeInfo = that;
	}

	/// <summary>
	/// Does this form require tips data?
	/// </summary>
	/// <remarks>
	/// TODO [zjm] I don't really understand this field. Jim,
	/// could you comment further or correct my explanation.
	/// </remarks>
	public boolean getRequiresTipsIncome()
	{
		 return m_requiresTipsIncome; 
	}
	public void setRequiresTipsIncome(boolean that)
	{
		m_requiresTipsIncome = that;
	}

	/// <summary>
	/// Does this form require hours worked data?
	/// </summary>
	public boolean getRequiresHoursWorked()
	{
		 return m_requiresHoursWorked; 
	}
	public void setRequiresHoursWorked(boolean that)
	{
		m_requiresHoursWorked = that;
	}

	/// <summary>
	/// Does this form require employment duration data?
	/// </summary>
	public boolean getRequiresEmployeeMonthsWorked()
	{
		 return m_requiresEmployeeMonthsWorked; 
	}
	public void setRequiresEmployeeMonthsWorked(boolean that)
	{
		m_requiresEmployeeMonthsWorked = that;
	}
}
