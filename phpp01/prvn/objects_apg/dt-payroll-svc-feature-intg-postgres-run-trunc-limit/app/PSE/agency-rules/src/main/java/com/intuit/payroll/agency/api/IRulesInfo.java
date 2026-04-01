//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

import com.intuit.payroll.agency.dao.LawData;

/// <summary>
/// Global interface of the Rules Component. This interface will be exposed through COM.
/// </summary>
public interface IRulesInfo
{
	/// <summary>
	/// List of all active jurisdiction ids.
	/// </summary>
	/// <returns>IListResponse collection of jurisdiction IDs.</returns>
	IRulesList getActiveJurisdictionIDList();

	/// <summary>
	/// List of all active agency ids.
	/// </summary>
	/// <returns>IListResponse collection of agency IDs.</returns>
	IRulesList getActiveAgencyIDList();

	/// <summary>
	/// List of active payment template ids.
	/// </summary>
	/// <returns>IListResponse collection of payment template IDs.</returns>
	IRulesList getActivePaymentTemplateIDList();

	/// <summary>
	/// List of active payment template ids, given a set of tax law ids.
	/// </summary>
	/// <param name="lawIDList">IListRequest collection of law IDs.</param>
	/// <returns>IListResponse collection of payment template IDs.</returns>
	IRulesList getActivePaymentTemplateIDListFromLawIDs(IRulesList lawIDList);

	/// <summary>
	/// payment template id, given a tax law id.
	/// </summary>
	/// <param name="lawID">long law ID.</param>
	/// <returns>String: payment template ID</returns>
	String getPaymentTemplateID (Integer lawID);

	/// <summary>
	/// Retrieve a list of all active enrollment group ids.
	/// </summary>
	/// <returns>A list of IEnrollmentGroup objects.</returns>
	IRulesList getActiveEnrollmentGroupIDList();

	/// <summary>
	/// Retrieve a list of all active enrollment group ids, 
	/// given a set of tax law ids.
	/// </summary>
	/// <param name="lawIDList">A list of law ids.</param>
	/// <param name="submitMethodId">The submit method to get enrollment group ids for.</param>
	/// <returns>A list of IEnrollmentGroup objects that describe
	/// how credential information for the taxes defined by the tax ids
	/// submitted.</returns>
	IRulesList getActiveEnrollmentGroupIDListFromLawIDs(IRulesList lawIDList, String submitMethodId);
	
	/// <summary>
	/// Retrieve a specific enrollment group using an id.
	/// </summary>
	/// <param name="enrollmentGroupId">Unique id of an enrollment group.</param>
	/// <returns>An IEnrollmentGroup object containing data for that enrollment group.</returns>
	IEnrollmentGroup getEnrollmentGroup(String enrollmentGroupId);

	/// <summary>
	/// Retrieve a specific enrollment group using a payment template/submit method.
	/// </summary>
	/// <param name="paymentTemplateId">Unique id of a payment template.</param>
	/// <param name="submitMethodType">Submit method type.</param>
	/// <returns>An IEnrollmentGroup object containing data for that enrollment group.</returns>
	IEnrollmentGroup getEnrollmentGroupFromPaymentTemplate (String paymentTemplateId, String submitMethodType);

	/// <summary>
	/// get a jurisdiction object, given a jurisdiction id.
	/// </summary>
	/// <param name="id">Jurisdiction ID.</param>
	/// <returns>Jurisdiction object. It always returns a non null object, client code should check for IsValid property of the returned object before using it.</returns>
	IJurisdiction getJurisdiction(String id);

	/// <summary>
	/// get an agency object, given an agency id.
	/// </summary>
	/// <param name="id">Agency ID.</param>
	/// <returns>Agency object. It always returns a non null object, client code should check for IsValid property of the returned object before using it.</returns>
	IAgency getAgency(String id);

	/// <summary>
	/// get a payment template object, given a payment template id.
	/// </summary>
	/// <param name="id">The ID of the Template to retrieve.</param>
	/// <returns>A PaymentTemplate</returns>
	IRulesPaymentTemplate getPaymentTemplate(String id);

	/// <summary>
	/// Returns a Payment Period for a given set of request params.
	/// </summary>
	/// <param name="paymentPeriodRq"></param>
	/// <returns>An instance of a PaymentPeriod</returns>
	IPaymentPeriod getPaymentPeriod(IPaymentPeriodRequest paymentPeriodRq);

	/// <summary>
	/// The version number of the data source.
	/// </summary>
	/// <remarks>
	/// The version number is always incremented on release, clients 
	/// can store and compare to enable smart synchronization.
	/// </remarks>
	String getVersionNumber();
	
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
	String getDescriptionByLawId(String lawId);

    LawData getLawByLawId(String lawId);
}
