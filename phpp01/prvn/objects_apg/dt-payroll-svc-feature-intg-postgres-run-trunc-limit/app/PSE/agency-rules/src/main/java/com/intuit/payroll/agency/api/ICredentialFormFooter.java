//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Data interface for the footer information for the credentials
/// user interface.
/// </summary>
/// <remarks>
/// ICredentialFormFooter contains any text information that 
/// needs to be displayed in the footer of the form.
/// </remarks>
public interface ICredentialFormFooter
{
	/// <summary>
	/// The "fine print" explaining that the user is authorizing
	/// us to send their credentials/payment/form to the agency.
	/// </summary>
	String getAuthorizeText ();
}
