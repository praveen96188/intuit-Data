//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IFormSubmitMethod;
/// <summary>
/// Interface to retrieve the form submit method 
/// data from the agency rules data store.
/// </summary>
public interface IFormSubmitMethodDAO
{
	/// <summary>
	/// get an IFormSubmitMethod based on the form template
	/// id and submit method id.
	/// </summary>
	/// <param name="formTemplateId">The id of the form template to get the submit method for.</param>
	/// <param name="formSubmitMethodId">The id of the form submit method.</param>
	/// <returns>An IFormSubmitMethod</returns>
	IFormSubmitMethod getFormSubmitMethod(String formTemplateId, String formSubmitMethodId);
}
