//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

/// <summary>
/// Defines methods to retrieve law type data 
/// from the agency rules data store.
/// </summary>
public interface ILawTypeDAO
{
	/// <summary>
	/// Using the supplied law id, retrieve the text description
	/// of the law.
	/// </summary>
	/// <param name="lawId">The law id to get the text description
	/// for.</param>
	/// <returns>A text String description of the law.</returns>
	/// <remarks>
	/// This value is for INTUIT INTERNAL use only, do not add this
	/// to any NON-DEBUG UI.
	/// </remarks>
	String getLawDescriptionFromId(String lawId);

    LawData getLawFromId(String lawId);
}
