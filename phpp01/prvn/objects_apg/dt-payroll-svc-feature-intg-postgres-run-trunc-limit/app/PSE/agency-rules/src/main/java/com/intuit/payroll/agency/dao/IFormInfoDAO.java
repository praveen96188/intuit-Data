//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IFormInfo;
/// <summary>
/// Interface to retrieve form info data from the
/// agency rules data store.	
/// </summary>
public interface IFormInfoDAO
{
	/// <summary>
	/// Retrieve the form info for the supplied form
	/// id from the agency rules data store.
	/// </summary>
	/// <param name="formId">The id of the form to get the
	/// form info for.</param>
	/// <returns>If the supplied form id is found to match data
	/// in the agency rules data store, a fully-formed IFormInfo object 
	/// is returned.  If no match is found, null is returned.</returns>
	IFormInfo getFormInfoByFormId(String formId);
}
