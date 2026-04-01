//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

import java.util.Collection;
import java.util.Set;

/// <summary>
/// Interface for the Jurisdiction object. This interface is exposed through COM.
/// </summary>
public interface IJurisdiction
{
	/// <summary>
	/// Jurisdiction id (e.g. "CA" or "US"). "US" for federal, state postal id for states. Pick a unique String for local taxes.
	/// </summary>
	String getJurisdictionID();

	/// <summary>
	/// True if this jurisdiction is obsolete.
	/// </summary>
	boolean getIsObsolete();

	/// <summary>
	/// Read-only property that indicates if this object is valid or not.
	/// If IsValid is true, it means that this object contains valid data.
	/// If IsValid is false, it means that this object was not initialized.
	/// </summary>
	boolean getIsValid();

	/// <summary>
	/// Jurisdiction description String for UI (e.g. "California").
	/// </summary>
	String getDescription();

	/// <summary>
	/// Same as JurisdictionID for state and federal taxes.  For local taxes put the relevant state postal code.
	/// </summary>
	String getStateID();

    /**
     * List agencies belonging to this jurisdiction
     */
    Set<String> getAgencyCodes();
}
