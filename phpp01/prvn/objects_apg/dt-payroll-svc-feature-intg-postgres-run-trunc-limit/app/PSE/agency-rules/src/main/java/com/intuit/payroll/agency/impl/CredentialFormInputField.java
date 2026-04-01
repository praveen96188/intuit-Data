//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.ICredentialFormInputField;
import com.intuit.payroll.agency.api.RulesObjectBroker;
/// <summary>
/// CredentialFormInputField contains data that describes
/// an input field in the credentials user interface.
/// </summary>
public class CredentialFormInputField implements ICredentialFormInputField {
	private boolean m_isValid = true;
	private boolean m_isRequired = false;
	private boolean m_isObscure = false;
	private int m_length = 0;
	private String m_label;
	private String m_fieldID;
	private boolean m_isNumericOnly = false;
	private int m_minimumLength = 0;
//	private String m_fieldType = RulesConfig.getInstance().getConfigSetting("DefaultCredentialInputFieldType");
	private String m_fieldType = "text";
	private IRulesList m_possibleValues = RulesObjectBroker.getInstance().createRulesList(null);

	/// <summary>
	/// The internal ID String assigned to
	/// the field. Should be human-identifiable,
	/// to a degree.
	/// </summary>
	/// <example>"USER_FIELD","PIN_FIELD_1"</example>
	public String getFieldID()
	{
		 return m_fieldID; 
	}
	public void setFieldID(String that)
	{
		m_fieldID = that;
	}

	/// <summary>
	/// The text label to display in the 
	/// user interface.
	/// </summary>
	public String getLabel()
	{
		 return m_label; 
	}
	public void setLabel(String that)
	{
		m_label = that;
	}

	/// <summary>
	/// The maximum number of characters this 
	/// field should hold.
	/// </summary>
	public int getLength()
	{
		 return m_length; 
	}
	public void setLength(int that)
	{
		m_length = that;
	}

	/// <summary>
	/// Determines whether the input should be
	/// real-time obscured in the user interface.
	/// </summary>
	/// <example>A PIN field should not show the
	/// characters being input. Instead it should show
	/// some mask character: ****.</example>
	/// <remarks>
	/// Should be true for any sensitive fields.
	/// </remarks>
	/// <returns>True if field should use a mask character
	/// to conceal user input. False if it is ok for input
	/// to be reflected as typed in by the user.</returns>
	public boolean getIsObscure()
	{
		 return m_isObscure; 
	}
	public void setIsObscure(boolean that)
	{
		m_isObscure = that;
	}
    public void setIsObscure(String that)
    {
        m_isObscure = PaymentTemplate.valueAsBoolean(that);
    }

	/// <summary>
	/// Reflects whether field is required by the agency. 
	/// </summary>
	/// <remarks>The
	/// user interface should use this to enforce data entry
	/// validation and not allow credentials to be submitted 
	/// unless filled out by the user.</remarks>
	public boolean getIsRequired()
	{
		 return m_isRequired; 
	}
	public void setIsRequired(boolean that)
	{
		m_isRequired = that;
	}
    public void setIsRequired(String that)
    {
        m_isRequired = PaymentTemplate.valueAsBoolean(that);
    }


	/// <summary>
	/// Is the object valid according to component?
	/// </summary>
	/// <remarks>
	/// If a client receives an object of type IComponentFormInputField
	/// and IsValid is false, then there was an error somewhere in
	/// creating the object and it should be treated as corrupt.
	/// </remarks>
	// TODO [zjm]: Instead of a boolean, this should return an object that
	// has the boolean IsValid plus a description of why it isn't valid so
	// the client can be aware of the reason.
	public boolean getIsValid()
	{
		 return m_isValid; 
	}
	public void setIsValid(boolean that)
	{
		m_isValid = that;
	}

	/// <summary>
	/// Describes the minimum length the user input must be.
	/// </summary>
	public int getMinimumLength()
	{
		 return m_minimumLength; 
	}
	public void setMinimumLength(int that)
	{
		m_minimumLength = that;
	}

	/// <summary>
	/// Describes whether the field is numeric only (true) or
	/// alphanumeric (false).
	/// </summary>
	public boolean getIsNumericOnly()
	{
		 return m_isNumericOnly; 
	}
	public void setIsNumericOnly(boolean that)
	{
		m_isNumericOnly = that;
	}
    public void setIsNumericOnly(String that)
    {
        m_isNumericOnly = PaymentTemplate.valueAsBoolean(that);
    }

	/// <summary>
	/// Describes the type of form input field.  
	/// </summary>
	/// <example>
	/// The value 'text' denotes freeform text input.
	/// The value 'enumeration' denotes a selection from a constrained list of values.
	/// </example>
	public String getFieldType()
	{
		 return m_fieldType; 
	}
	public void setFieldType(String that)
	{
		m_fieldType = that;
	}

	/// <summary>
	/// Describes the values the form input field could take if
	/// it is of M_fieldType = 'enumeration'.
	/// </summary>
	/// <returns>An IRulesList of Strings.</returns>
	/// <remarks>Note that this returns an IRulesList containing
	/// Strings suitable for use in a user interface.</remarks>
	public IRulesList getPossibleFieldValues()
	{
		 return m_possibleValues; 
	}
	public void setPossibleFieldValues(IRulesList that)
	{
		m_possibleValues = that;
	}

	public void addPossibleFieldValue(String that)
	{
		m_possibleValues.add(that);
	} 
}
