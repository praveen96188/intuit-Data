//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.ICredentialFormLogin;
import com.intuit.payroll.agency.api.RulesObjectBroker;
/// <summary>
/// CredentialFormLogin aggregates the user interface
/// definition for the user login form on a per-enrollment
/// group basis.
/// </summary>
public class CredentialFormLogin implements ICredentialFormLogin {
	private String m_enrollText="";
	private String m_loginText="";
	private String m_titleLabel="";
	private IRulesList m_inputFields = RulesObjectBroker.getInstance().createRulesList(null);

	/// <summary>
	/// The title of the form.
	/// </summary>
	public String getTitleLabel()
	{
		 return m_titleLabel; 
	}
	public void setTitleLabel(String that)
	{
		m_titleLabel = that;
	}

	/// <summary>
	/// The per-agency text instructing the user how to
	/// login (ex: "you'll need your username, pin, and 
	/// the circumference of the sun in centimeters to 
	/// login...").
	/// </summary>
	public String getLoginText()
	{
		 return m_loginText; 
	}
	public void setLoginText(String that)
	{
		m_loginText = that;
	}

	/// <summary>
	/// The per-agency text instructing the user on how
	/// to become enrolled with the agency if they haven't
	/// already done so (ex: "If you haven't enrolled yet,
	/// click the link and go beg Idaho for some credentials.")
	/// </summary>
	public String getEnrollText()
	{
		 return m_enrollText; 
	}
	public void setEnrollText(String that)
	{
		m_enrollText = that;
	}

	/// <summary>
	/// gets the input fields that this login form requires.
	/// </summary>
	/// <returns>An IRulesList of ICredentialFormField objects.</returns>
	public IRulesList getInputFields()
	{
		 return m_inputFields; 
	}
	public void setInputFields(IRulesList that)
	{
		m_inputFields = that;
	}

	public void addInputField(CredentialFormInputField inputField)
	{
		m_inputFields.add(inputField);
	}
}
