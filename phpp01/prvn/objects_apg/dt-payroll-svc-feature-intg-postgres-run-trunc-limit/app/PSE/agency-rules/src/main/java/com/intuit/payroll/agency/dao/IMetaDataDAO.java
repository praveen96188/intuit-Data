//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;
/// <summary>
/// IMetaDataDAO defines methods for retrieving metadata
/// about the datasource being read by the DAO..
/// </summary>
public interface IMetaDataDAO
{
	/// <summary>
	/// gets the version number of the data.
	/// </summary>
	/// <returns>A String denoting the version
	/// of the data source.</returns>
	String getVersionNumber();
}
