//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.ICredentialFormFooter;
/// <summary>
/// CredentialForm aggregates the user interface
/// definition for an informational footer on a per-enrollment
/// group basis.
/// </summary>
public class CredentialFormFooter implements ICredentialFormFooter {
	private String m_authorizeText="";

	/// <summary>
	/// The "fine print" explaining that the user is authorizing
	/// us to send their credentials/payment/form to the agency.
	/// </summary>
	public String getAuthorizeText()
	{
		 return m_authorizeText; 
	}
	public void setAuthorizeText(String that)
	{
		m_authorizeText = that;
	}
}
