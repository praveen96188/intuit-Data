//---------------------------------------------------------------------------
// Copyright 2007 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IFormFilingFrequency;
/// <summary>
/// Defines methods to retrieve form template filing
/// frequencies from the agency rules data store.
/// </summary>
public interface IFormFilingFrequencyDAO
{
	/// <summary>
	/// Retrieves the filing frequency for form template identified
	/// by the supplied id.
	/// </summary>
	/// <param name="formTemplateId">The id of the form template to
	/// get the filing frequency for.</param>
	/// <returns>An IFormFilingFrequency for the identified form template.</returns>
	IFormFilingFrequency getFilingFrequency(String formTemplateId);
}
