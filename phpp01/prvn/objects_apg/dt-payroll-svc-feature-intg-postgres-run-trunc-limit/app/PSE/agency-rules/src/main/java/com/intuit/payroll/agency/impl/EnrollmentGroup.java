//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.ICredentialData;
import com.intuit.payroll.agency.api.IEnrollmentGroup;
import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IAgency;
/// <summary>
/// Rules component (and standard) implementation of 
/// the IEnrollmentGroup interface.
/// </summary>
/// <remarks>
/// The setters on the data properties are
/// not included in the interface, as once they are 
/// set internal to the component they are handed to 
/// clients, who shoudl have no reason to change them.
/// </remarks>
public class EnrollmentGroup implements IEnrollmentGroup {
	protected ICredentialData m_credentialData = null;
	protected ICredentialData m_enrollmentCredentialData = null;
	private boolean m_isValid = true;
	private String m_webSiteEnrollmentURL="";
	private String m_webSiteInstructions="";
	private boolean m_enrollViaWebsite = false;
	private boolean m_enrollmentRequired = false;
	private String m_uiDescription="";
	private String m_uiString="";
	private boolean m_isObsolete = false;
	private String m_enrollmentGroupID="";

	/// <summary>
	/// ID of the enrollment group.
	/// </summary>
	public String getEnrollmentGroupID()
	{
		 return m_enrollmentGroupID; 
	}
	public void setEnrollmentGroupID(String that)
	{
		m_enrollmentGroupID = that;
	}

	/// <summary>
	/// True is this enrollment group is obsolete.
	/// </summary>
	public boolean getIsObsolete()
	{
		 return m_isObsolete; 
	}
	public void setIsObsolete(boolean that)
	{
		m_isObsolete = that;
	}
    public void setIsObsolete(String that)
    {
        m_isObsolete = PaymentTemplate.valueAsBoolean(that);
    }


	/// <summary>
	/// Short "name" for the enrollment.
	/// </summary>
	public String getUIString()
	{
		 return m_uiString; 
	}
	public void setUIString(String that)
	{
		m_uiString = that;
	}

	/// <summary>
	/// Long text description for this enrollment.
	/// </summary>
	public String getUIDescription()
	{
		 return m_uiDescription; 
	}
	public void setUIDescription(String that)
	{
		m_uiDescription = that;
	}

	/// <summary>
	/// Is an enrollment process required for this group?
	/// </summary>
	public boolean getEnrollmentRequired()
	{
		 return m_enrollmentRequired; 
	}
	public void setEnrollmentRequired(boolean that)
	{
		m_enrollmentRequired = that;
	}
    public void setEnrollmentRequired(String that)
    {
        m_enrollmentRequired = PaymentTemplate.valueAsBoolean(that);
    }


	/// <summary>
	/// Enroll via external web site, or from within client?
	/// </summary>
	public boolean getEnrollViaWebsite()
	{
		 return m_enrollViaWebsite; 
	}
	public void setEnrollViaWebsite(boolean that)
	{
		m_enrollViaWebsite = that;
	}
    public void setEnrollViaWebsite(String that)
    {
        m_enrollViaWebsite = PaymentTemplate.valueAsBoolean(that);
    }

	/// <summary>
	/// The html String which has instructions for the
	/// web enrollment process.
	/// </summary>
	public String getWebSiteInstructions()
	{
		 return m_webSiteInstructions; 
	}
	public void setWebSiteInstructions(String that)
	{
		m_webSiteInstructions = that;
	}

	/// <summary>
	/// URL to the enrollment page for the appropriate agency.
	/// </summary>
	public String getWebSiteEnrollmentURL()
	{
		 return m_webSiteEnrollmentURL; 
	}
	public void setWebSiteEnrollmentURL(String that)
	{
		m_webSiteEnrollmentURL = that;
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
	/// get the agency credential requirements.
	/// </summary>
	public ICredentialData getCredentialData()
	{
		throw new RuntimeException("not yet implemented");
/*		if(m_credentialData == null)
		{
			m_credentialData = DAOFactory.getDAOFactory().getCredentialDataDAO().getCredentialData(m_enrollmentGroupID);
		}
		return m_credentialData;
*/
	}

	/// <summary>
	/// get list of {payment template id, submit method type}.
	/// </summary>
	/// <returns>IListResponse collection of payment method rules.</returns>
	public IRulesList getPaymentMethodRuleList()
	{
		throw new RuntimeException("not yet implemented");
	}

	/// <summary>
	/// get the agency enrollment credential requirements
	/// </summary>
	/// <returns></returns>
	public ICredentialData getEnrollmentCredentialData()
	{
		throw new RuntimeException("not yet implemented");
/*		if(m_enrollmentCredentialData == null)
		{
			m_enrollmentCredentialData = DAOFactory.getDAOFactory().getCredentialDataDAO().getEnrollmentCredentialData(m_enrollmentGroupID);
		}
		return m_enrollmentCredentialData;
*/	}

	/// <summary>
	/// Returns the Agency that this Enrollment Group is associated with.
	/// </summary>
	/// <returns>An instance of IAgency.</returns>
	public IAgency getRelatedAgency()
	{
		throw new RuntimeException("not yet implemented");
//		return DAOFactory.getDAOFactory().getAgencyDAO().getAgencyByEnrollmentGroupId(getEnrollmentGroupID());
	}
}
