//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.dao.LawData;
import com.intuit.payroll.agency.calculator.DueDateCalculator;
import com.intuit.payroll.agency.api.*;
//import com.intuit.spc.foundations.portability.SpcfRuntimeException;

/// <summary>
/// Implementation class for the global interface of the Rules Component.
/// </summary>
public final class RulesInfo implements IRulesInfo {
	private DAOFactory factory;
	private String m_versionNumber;

	/// <summary>
	/// Default Constructor.
	/// </summary>
	public RulesInfo()
	{
		factory = DAOFactory.getDAOFactory();
	}
	/// <summary>
	/// List of all active jurisdiction ids.
	/// </summary>
	/// <returns>IListResponse collection of jurisdiction IDs.</returns>
	public IRulesList getActiveJurisdictionIDList()
	{
		return factory.getJurisdictionDAO().getActiveJurisdictionIDList();
	}

	/// <summary>
	/// List of all active agency ids.
	/// </summary>
	/// <returns>IListResponse collection of agency IDs.</returns>
	public IRulesList getActiveAgencyIDList()
	{
		return factory.getAgencyDAO().getActiveAgencyIDList();
	}

	/// <summary>
	/// List of active payment template ids.
	/// </summary>
	/// <returns>IListResponse collection of payment template IDs.</returns>
	public IRulesList getActivePaymentTemplateIDList()
	{
		return factory.getPaymentTemplateDAO().getActivePaymentTemplateIDList();
	}

	/// <summary>
	/// List of active payment template ids, given a set of tax law ids.
	/// </summary>
	/// <param name="lawIDList">IListRequest collection of law IDs.</param>
	/// <returns>IListResponse collection of payment template IDs.</returns>
	public IRulesList getActivePaymentTemplateIDListFromLawIDs(IRulesList lawIDList)
	{
		//Defensive at the front door.  Garbage in/Garbage out.
		if (lawIDList == null) 
		{
			return RulesListFactory.createRulesList();
		}
		return factory.getPaymentTemplateDAO().getActivePaymentTemplateIDListFromLawIDs(lawIDList);
	}

	/// <summary>
	/// payment template id, given a tax law id.  just a convenience method for Setup.
	/// </summary>
	/// <param name="lawID">long law ID.</param>
	/// <returns>String: payment template ID</returns>
	public String getPaymentTemplateID (Integer lawID)
	{
		IRulesList lawIDList = RulesObjectBroker.getInstance().createRulesList(null);
		lawIDList.add (lawID);
		IRulesList paymentIDList = getActivePaymentTemplateIDListFromLawIDs (lawIDList);
		if (paymentIDList.getCount() == 1) 
		{
			return (String) paymentIDList.getItem(0);
		} 
		else 
		{
			return "";
		}
	}

	/// <summary>
	/// Retrieve a list of all active enrollment group ids.
	/// </summary>
	/// <returns>A list of IEnrollmentGroup objects.</returns>
	public IRulesList getActiveEnrollmentGroupIDList()
	{
		return factory.getEnrollmentGroupDAO().getActiveEnrollmentGroupIDList();
	}

	/// <summary>
	/// Retrieve a list of all active enrollment group ids, 
	/// given a set of tax law ids.
	/// </summary>
	/// <param name="lawIDList">A list of law ids.</param>
	/// <param name="submitMethodId">The submit method to get enrollment group ids for.</param>
	/// <returns>A list of IEnrollmentGroup objects that describe
	/// how credential information for the taxes defined by the tax ids
	/// submitted.</returns>
	public IRulesList getActiveEnrollmentGroupIDListFromLawIDs(IRulesList lawIDList, String submitMethodId)
	{
		//Defensive at the front door.  Garbage in/Garbage out.
		if (lawIDList == null || lawIDList.getCount() < 1) {
			return RulesListFactory.createRulesList();
		}
		return factory.getEnrollmentGroupDAO().getActiveEnrollmentGroupIDListFromLawIDs(lawIDList, submitMethodId);
	}

	/// <summary>
	/// Retrieve a specific enrollment group using an id.
	/// </summary>
	/// <param name="enrollmentGroupId">Unique id of an enrollment group.</param>
	/// <returns>An IEnrollmentGroup object containing data for that enrollment group.</returns>
	public IEnrollmentGroup getEnrollmentGroup(String enrollmentGroupId)
	{
		return factory.getEnrollmentGroupDAO().getEnrollmentGroup(enrollmentGroupId);
	}

	/// <summary>
	/// Retrieve a specific enrollment group using a payment template/submit method.
	/// </summary>
	/// <param name="paymentTemplateId">Unique id of a payment template.</param>
	/// <param name="submitMethodType">Unique id of a submit method.</param>
	/// <returns>An IEnrollmentGroup object containing data for that enrollment group.</returns>
	public IEnrollmentGroup getEnrollmentGroupFromPaymentTemplate (String paymentTemplateId, String submitMethodType) 
	{
		return factory.getEnrollmentGroupDAO().getEnrollmentGroupFromPaymentTemplate(paymentTemplateId, submitMethodType);
	}

	/// <summary>
	/// get a jurisdiction object, given a jurisdiction id.
	/// </summary>
	/// <param name="id">Jurisdiction ID.</param>
	/// <returns>Jurisdiction object. It always returns a non null object, client code should check for IsValid property of the returned object before using it.</returns>
	public IJurisdiction getJurisdiction(String id)
	{
		return factory.getJurisdictionDAO().getJurisdiction(id);
	}

	/// <summary>
	/// get an agency object, given an agency id.
	/// </summary>
	/// <param name="id">Agency ID.</param>
	/// <returns>Agency object. It always returns a non null object, client code should check for IsValid property of the returned object before using it.</returns>
	public IAgency getAgency(String id)
	{
		return factory.getAgencyDAO().getAgency(id);
	}

	/// <summary>
	/// get a payment template object, given a payment template id.
	/// </summary>
	/// <param name="id"></param>
	/// <returns>IListResponse collection of payment template IDs.</returns>
	public IRulesPaymentTemplate getPaymentTemplate(String id)
	{
		return factory.getPaymentTemplateDAO().getPaymentTemplate(id);
	}

	/// <summary>
	/// Returns an instance of a payment period.
	/// </summary>
	/// <param name="paymentPeriodRq"></param>
	/// <returns></returns>
	public IPaymentPeriod getPaymentPeriod( IPaymentPeriodRequest paymentPeriodRq ) 
	{
		if(paymentPeriodRq == null)
		{
//			throw new ArgumentNullException("PaymentPeriodRequest argument cannot be null.", "PaymentPeriodRequest");	
			throw new RuntimeException("PaymentPeriodRequest argument cannot be null.");
//			throw new SpcfRuntimeException("PaymentPeriodRequest argument cannot be null.");
		}
		return DueDateCalculator.getInstance().getPaymentPeriod(paymentPeriodRq);
	}

	/// <summary>
	/// The version number of the data source.
	/// </summary>
	/// <remarks>
	/// The version number is always incremented on release, clients 
	/// can store and compare to enable smart synchronization.
	/// </remarks>
	public String getVersionNumber()
    {
        if(m_versionNumber == null || m_versionNumber.length() == 0)
		{
			m_versionNumber = DAOFactory.getDAOFactory().getMetaDataDAO().getVersionNumber();
		}
		return m_versionNumber;
	}

	/// <summary>
	/// Retrieves the description text associated with the
	/// supplied law id.
	/// </summary>
	/// <param name="lawId">The law id to get the text description for.</param>
	/// <returns>A text String describing the law id.</returns>
	/// <remarks>
	/// The law id is typically numerical (136). This method allows a client
	/// to get an english text description of the law id.
	/// </remarks>
	public String getDescriptionByLawId(String lawId)
	{
		return factory.getLawTypeDAO().getLawDescriptionFromId(lawId);
	}

    public LawData getLawByLawId(String lawId)
	{
		return factory.getLawTypeDAO().getLawFromId(lawId);
	}
}
