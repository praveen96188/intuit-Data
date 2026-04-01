//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IEnrollmentGroup;
/// <summary>
/// An interface to create IEnrollmentGroup objects
/// from a datasource.
/// </summary>
public interface IEnrollmentGroupDAO
{
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
	/// <param name="submitMethodType">Unique id of a submit method.</param>
	/// <returns>An IEnrollmentGroup object containing data for that enrollment group.</returns>
	IEnrollmentGroup getEnrollmentGroupFromPaymentTemplate (String paymentTemplateId, String submitMethodType);
}
