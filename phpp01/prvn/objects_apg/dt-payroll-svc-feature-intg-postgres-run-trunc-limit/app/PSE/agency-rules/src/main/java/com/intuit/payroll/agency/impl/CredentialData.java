//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.ICredentialFormLogin;
import com.intuit.payroll.agency.api.ICredentialData;
import com.intuit.payroll.agency.api.ICredentialFormFooter;
import com.intuit.payroll.agency.api.ICredentialFormHeader;
/// <summary>
/// CredentialData aggregates the user interface
/// definition for user credentials on a per-enrollment
/// group basis.
/// </summary>
public class CredentialData implements ICredentialData {
	private ICredentialFormFooter m_footer = new CredentialFormFooter();
	private ICredentialFormLogin m_login = new CredentialFormLogin();
	private ICredentialFormHeader m_header = new CredentialFormHeader();
	private String m_windowTitle="";
	private boolean m_isValid = true;

	/// <summary>
	/// Caption for the credential window.
	/// </summary>
	public String getWindowTitle()
	{
		 return m_windowTitle; 
	}
	public void setWindowTitle(String that)
	{
		m_windowTitle = that;
	}

	/// <summary>
	/// Is the ICredentialData object valid according
	/// to the internal validation.
	/// </summary>
	public boolean getIsValid()
	{
		 return m_isValid; 
	}
	public void setIsValid(boolean that)
	{
		m_isValid = that;
	}

	/// <summary>
	/// The data interface for the header information
	/// of the credential user interface. Header information
	/// contains things such as label text.
	/// </summary>
	public ICredentialFormHeader getHeader()
	{
		 return m_header; 
	}
	public void setHeader(ICredentialFormHeader that)
	{
		m_header = that;
	}

	/// <summary>
	/// The data interface for the login information of the
	/// credentials user interface. Login information contains
	/// things such as label text and form limitations (length, etc.)
	/// </summary>
	public ICredentialFormLogin getLogin()
	{
		 return m_login; 
	}
	public void setLogin(ICredentialFormLogin that)
	{
		m_login = that;
	}

	/// <summary>
	/// The data interface for the footer information of
	/// the credentials user interface. Footer information contains
	/// things such as the text for the "small print" authorizing transmission 
	/// and possibly label text.
	/// </summary>
	public ICredentialFormFooter getFooter()
	{
		 return m_footer; 
	}
	public void setFooter(ICredentialFormFooter that)
	{
		m_footer = that;
	}
}
