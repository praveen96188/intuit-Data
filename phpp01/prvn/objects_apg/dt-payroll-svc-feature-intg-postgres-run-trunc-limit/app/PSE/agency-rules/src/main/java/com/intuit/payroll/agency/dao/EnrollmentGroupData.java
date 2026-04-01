//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.ICredentialData;
import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IAgency;
import com.intuit.payroll.agency.impl.EnrollmentGroup;
import com.intuit.payroll.agency.impl.CredentialData;
//import com.intuit.payroll.agency.impl.
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
public class EnrollmentGroupData extends EnrollmentGroup {
	/// <summary>
	/// get the agency credential requirements.
	/// </summary>
	public ICredentialData getCredentialData()
	{
		return m_credentialData;
	}

	public void setCredentialData(CredentialData cd)
	{
		m_credentialData = cd;
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
		return m_enrollmentCredentialData;
	}
	public void setEnrollmentCredentialData(CredentialData cd)
	{
		m_enrollmentCredentialData = cd;
	}

	/// <summary>
	/// Returns the Agency that this Enrollment Group is associated with.
	/// </summary>
	/// <returns>An instance of IAgency.</returns>
	public IAgency getRelatedAgency()
	{
// jspencer: the original design idea was that agencies might share enrollment groups,
        //  so this interface is questionable to me.  Seems unlikely that a client is using this
        //  but I'll have to check before removing it.
        throw new RuntimeException("not yet implemented");
//		return DAOFactory.getDAOFactory().getAgencyDAO().getAgencyByEnrollmentGroupId(getEnrollmentGroupID());
	}



}
