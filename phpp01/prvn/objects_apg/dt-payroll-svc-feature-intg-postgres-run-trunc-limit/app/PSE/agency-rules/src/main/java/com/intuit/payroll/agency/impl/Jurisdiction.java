//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IJurisdiction;
import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IAgency;
import com.intuit.payroll.agency.dao.DataStore;

import java.util.*;
/// <summary>
/// Implementation class for the IJurisdiction interface.
/// </summary>
public class Jurisdiction implements IJurisdiction {
    public static IJurisdiction findJurisdiction(String pJurisdictionId) {
        DataStore store = DataStore.getDataStore();
        return store.getJurisdiction(pJurisdictionId);
    }

	private String m_jurisdictionID = "";
	private boolean m_isObsolete = false;
	private boolean m_isValid = true;
	private String m_description = "";
	private String m_stateID = "";
    private transient Set<String> m_agencyCodes;

	/// <summary>
	/// Jurisdiction id (e.g. "CA" or "US"). "US" for federal, state postal id for states. Pick a unique String for local taxes.
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
	/// True if this jurisdiction is obsolete.
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
	/// Jurisdiction description String for UI (e.g. "California").
	/// </summary>
	public String getDescription()
	{
		 return m_description; 
	}

    public Set<String> getAgencyCodes() {
        if (m_agencyCodes != null) return m_agencyCodes;
        /*
         * We are not building the map for all jurisdictions
         * since it is very likely that we only service a small
         * subset of jurisdictions at least for next few years
         */
        DataStore store = DataStore.getDataStore();
        Set<String> agencies = new HashSet<String>();
        IRulesList list = store.getActiveAgencyIDList();
        for (int i = 0; i < list.getCount(); i++) {
            IAgency agency = store.getAgency((String) list.getItem(i));
            if (agency.getJurisdictionID().equals(m_jurisdictionID)) {
                agencies.add(agency.getAgencyID());
            }
        }
        //
        m_agencyCodes = Collections.unmodifiableSet(agencies);
        return m_agencyCodes;
    }

    public void setDescription(String that)
	{
		m_description = that;
	}

	/// <summary>
	/// Same as JurisdictionID for state and federal taxes.  For local taxes put the relevant state postal code.
	/// </summary>
	public String getStateID()
	{
		 return m_stateID; 
	}
	public void setStateID(String that)
	{
		m_stateID = that;
	}
}
