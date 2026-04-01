//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
	/// <summary>
	/// Interface for the Agency object. This interface is exposed through COM.
	/// </summary>
package com.intuit.payroll.agency.api;

public interface IAgency
{
    /// <summary>
    /// Agency ID String, e.g. "IRS".
    /// </summary>
    String getAgencyID();

    /// <summary>
    /// True if this agency is obsolete.
    /// </summary>
    boolean getIsObsolete();

    /// <summary>
    /// Read-only property that indicates if this object is valid or not.
    /// If IsValid is true, it means that this object contains valid data.
    /// If IsValid is false, it means that this object was not initialized.
    /// </summary>
    boolean getIsValid();

    /// <summary>
    /// The "Payable To" value for the agency (e.g. "Internal Revenue Service").
    /// </summary>
    String getDescription();

    /// <summary>
    /// Jurisdiction ID which this agency belongs to. "US" for federal, state postal id for states.
    /// </summary>
    String getJurisdictionID();

    /// <summary>
    /// The actual agency name
    /// </summary>
    String getName();

    String getAgencyAbbrev();
}
