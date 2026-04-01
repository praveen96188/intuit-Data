//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

package com.intuit.payroll.agency.api;


/// <summary>
/// ICredentialData is a data interface that contains information
/// about how users enroll with agencies, and what kind of
/// credentials are required when they attempt to submit payments
/// and forms to the agency.  It also returns some interfaces
/// to other data interfaces that reference user-displayable text
/// about the enrollment/submission process.
/// </summary>
public interface ICredentialData
{
	/// <summary>
	/// Caption for the credential window.
	/// </summary>
	String getWindowTitle ();
	
	/// <summary>
	/// Is the ICredentialData object valid according
	/// to the internal validation.
	/// </summary>
	boolean getIsValid ();
	
	/// <summary>
	/// The data interface for the header information
	/// of the credential user interface. Header information
	/// contains things such as label text.
	/// </summary>
	ICredentialFormHeader getHeader ();
	
	/// <summary>
	/// The data interface for the login information of the
	/// credentials user interface. Login information contains
	/// things such as label text and form limitations (length, etc.)
	/// </summary>
	ICredentialFormLogin getLogin ();
	
	/// <summary>
	/// The data interface for the footer information of
	/// the credentials user interface. Footer information contains
	/// things such as the text for the "small print" authorizing transmission 
	/// and possibly label text.
	/// </summary>
	ICredentialFormFooter getFooter ();
}
