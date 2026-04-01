//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IAgency;
/// <summary>
/// Implementation class for the IAgency interface.
/// </summary>
public class Agency implements IAgency {
	private String m_agencyID;
	private boolean m_isObsolete = false;
	private boolean m_isValid = true;
	private String m_description;
	private String m_jurisdictionID;
	private String m_name;
    private String m_agencyAbbrev;

	/// <summary>
	/// Agency ID String, e.g. "IRS".
	/// </summary>
	public String getAgencyID()
	{
		 return m_agencyID; 
	}
	public void setAgencyID(String that)
	{
		m_agencyID = that;
	}

	/// <summary>
	/// True if this agency is obsolete.
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
	/// True if this object contains valid data.
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
	/// Agency description String for UI (e.g. "Internal Revenue Service").
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
	/// Jurisdiction ID which this agency belongs to. "US" for federal, state postal id for states.
	/// </summary>
	public String getJurisdictionID()
	{
		 return m_jurisdictionID; 
	}
	public void setJurisdictionID(String that)
	{
		m_jurisdictionID = that;
	}

	/// <summary>
	/// The actual agency name
	/// </summary>
	public String getName()
	{
		 return m_name;
	}
	public void setName(String that)
	{
		 m_name = that;
	}

    public String getAgencyAbbrev() {
        return m_agencyAbbrev;
    }

    public void setAgencyAbbrev(String m_agencyAbbrev) {
        this.m_agencyAbbrev = m_agencyAbbrev;
    }
}
