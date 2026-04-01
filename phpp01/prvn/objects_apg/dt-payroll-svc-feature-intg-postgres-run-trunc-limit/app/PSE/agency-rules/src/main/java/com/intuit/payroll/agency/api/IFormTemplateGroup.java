//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Interface for the FormTemplateGroup type defined by the agency rules.
/// This interface will be exposed through COM.
/// </summary>
public interface IFormTemplateGroup
{
	/// <summary>
	/// The String id of the form template group
	/// </summary>
	String getFormTemplateGroupID();
	
	/// <summary>
	/// Indicates whether the form template group is
	/// considered obsolete and unusable by Intuit
	/// Tax Dev.
	/// </summary>
	boolean getIsObsolete();
	
	/// <summary>
	/// A short text description of the form template group.
	/// </summary>
	/// <remarks>
	/// This field is approved to be used in customer-facing
	/// user interfaces.
	/// </remarks>
	String getShortDescription();
	
	/// <summary>
	/// A longer text description of the form template group.
	/// </summary>
	/// <remarks>
	/// This field is approved to be used in customer-facing
	/// user interfaces.
	/// </remarks>
	String getLongDescription();
}	
