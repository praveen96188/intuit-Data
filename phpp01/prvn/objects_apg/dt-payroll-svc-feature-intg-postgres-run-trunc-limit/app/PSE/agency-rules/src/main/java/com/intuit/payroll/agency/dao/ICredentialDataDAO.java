//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.ICredentialData;
/// <summary>
/// An interface to create ICredentialData objects
/// from a datasource.
/// </summary>
public interface ICredentialDataDAO
{
	/// <summary>
	/// Retrieve the submission credential data from the
	/// agency rules data store based on the supplied enrollment
	/// group ID.
	/// </summary>
	/// <param name="enrollmentGroupId">
	/// The id of the enrollment group to get submission 
	/// credential data for.
	/// </param>
	/// <returns>An ICredentialData object containing the
	/// credential user interface requirements for the specified
	/// submission.</returns>
	/// <remarks>While the EnrollmentGroup implementation is partly
	/// flyweight in that is doesn't immediately load the CredentialData
	/// (only read from datasource when requested through this method,) when
	/// the CredentialData is loaded it loads all child data due to
	/// the structure of the CredentialData data contract (no lightweight
	/// properties, just other large data interfaces).</remarks>
	public ICredentialData getCredentialData(String enrollmentGroupId);
	
	/// <summary>
	/// Retrieve the enrollment credential data from the agency rules data
	/// store based on the supplied enrollment group ID.
	/// </summary>
	/// <param name="enrollmentGroupId">The ID of the enrollment group
	/// to retrieve enrollment credential data for.</param>
	/// <returns>An ICredentialData object containing the
	/// credential user interface requirements for the specified
	/// enrollment.</returns>
	ICredentialData getEnrollmentCredentialData(String enrollmentGroupId);
}
