//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Data interface for the login form in the credentials
/// user interface.
/// </summary>
/// <remarks>
/// ICredentialFormLogin defines the fields and text that
/// appear in the actual credential form presented to the 
/// user.  Right now it defines only the fields and the
/// constraints on the data entered in those fields.
/// </remarks>
public interface ICredentialFormLogin
{
	/// <summary>
	/// The title of the form.
	/// </summary>
	String getTitleLabel ();
	
	/// <summary>
	/// The per-agency text instructing the user how to
	/// login (ex: "you'll need your username, pin, and 
	/// the circumference of the sun in centimeters to 
	/// login...").
	/// </summary>
	String getLoginText ();
	
	/// <summary>
	/// The per-agency text instructing the user on how
	/// to become enrolled with the agency if they haven't
	/// already done so (ex: "If you haven't enrolled yet,
	/// click the link and go beg Idaho for some credentials.")
	/// </summary>
	String getEnrollText ();
	
	/// <summary>
	/// gets the input fields that this login form requires.
	/// </summary>
	/// <returns>An IRulesList of ICredentialFormField objects.</returns>
	IRulesList getInputFields ();
}
