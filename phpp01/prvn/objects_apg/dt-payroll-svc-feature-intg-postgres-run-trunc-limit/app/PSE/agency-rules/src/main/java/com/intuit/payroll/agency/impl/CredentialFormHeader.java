//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.ICredentialFormHeader;
/// <summary>
/// CredentialFormHeader aggregates the user interface
/// definition for the informational header on a per-enrollment
/// group basis.
/// </summary>
public class CredentialFormHeader implements ICredentialFormHeader {
	private String m_reasonLabel="";
	private String m_settlementDateLabel="";
	private String m_amountLabel="";
	private String m_periodLabel="";
	private String m_agencyLabel="";
	private String m_titleLabel="";

	/// <summary>
	/// Text label for the title of the header section.
	/// </summary>
	public String getTitleLabel()
	{
		 return m_titleLabel; 
	}
	public void setTitleLabel(String that)
	{
		m_titleLabel = that;
	}

	/// <summary>
	/// Text label for the agency label in the header section.
	/// </summary>
	public String getAgencyLabel()
	{
		 return m_agencyLabel; 
	}
	public void setAgencyLabel(String that)
	{
		m_agencyLabel = that;
	}

	/// <summary>
	/// Text label for the period label in the header section.
	/// </summary>
	public String getPeriodLabel()
	{
		 return m_periodLabel; 
	}
	public void setPeriodLabel(String that)
	{
		m_periodLabel = that;
	}

	/// <summary>
	/// Text label for the amonut label in the header section.
	/// </summary>
	public String getAmountLabel()
	{
		 return m_amountLabel; 
	}
	public void setAmountLabel(String that)
	{
		m_amountLabel = that;
	}

	/// <summary>
	/// Text label for the settlement date label in the header section.
	/// </summary>
	public String getSettlementDateLabel()
	{
		 return m_settlementDateLabel; 
	}
	public void setSettlementDateLabel(String that)
	{
		m_settlementDateLabel = that;
	}

	/// <summary>
	/// Text label for the reason label in the header section.
	/// </summary>
	public String getReasonLabel()
	{
		 return m_reasonLabel; 
	}
	public void setReasonLabel(String that)
	{
		m_reasonLabel = that;
	}
}
