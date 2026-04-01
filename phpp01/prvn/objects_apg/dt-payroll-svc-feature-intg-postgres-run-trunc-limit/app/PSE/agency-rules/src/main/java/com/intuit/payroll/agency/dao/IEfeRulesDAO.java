//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.impl.IEfeRules;
/// <summary>
/// Describes an interface for retrieving EfeRules
/// data from a data source.
/// </summary>
public interface IEfeRulesDAO
{
	/// <summary>
	/// Retrieve an EfeRules data object from the
	/// datasouce.
	/// </summary>
	/// <returns>An IEfeRules object.</returns>
	IEfeRules getEfeRules();
}
