//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// ICredentialFormInputField is a data contract that 
/// describes an input field in the user interface
/// for gathering users' credentials.
/// </summary>
public interface ICredentialFormInputField
{
	/// <summary>
	/// The internal ID String assigned to
	/// the field. Should be human-identifiable,
	/// to a degree.
	/// </summary>
	/// <example>"USER_FIELD","PIN_FIELD_1"</example>
	String getFieldID ();
	
	/// <summary>
	/// The text label to display in the 
	/// user interface.
	/// </summary>
	String getLabel ();
	
	/// <summary>
	/// The maximum number of characters this 
	/// field should hold.
	/// </summary>
	int getLength ();
	
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
	boolean getIsObscure ();
	
	/// <summary>
	/// Reflects whether field is required by the agency. 
	/// </summary>
	/// <remarks>The
	/// user interface should use this to enforce data entry
	/// validation and not allow credentials to be submitted 
	/// unless filled out by the user.</remarks>
	boolean getIsRequired ();
	
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
	boolean getIsValid ();
	
	/// <summary>
	/// The minimum number of characters to be entered in this field.
	/// </summary>
	int getMinimumLength ();
	
	/// <summary>
	/// Interprets whether the field can only have numeric characters or whether
	/// it can be alphanumeric.
	/// </summary>
	boolean getIsNumericOnly ();
	
	/// <summary>
	/// Describes the type of form input field.  
	/// </summary>
	/// <example>
	/// The value 'text' denotes freeform text input.
	/// The value 'enumeration' denotes a selection from a constrained list of values.
	/// </example>
	String getFieldType ();
	
	/// <summary>
	/// Describes the values the form input field could take if
	/// it is of M_fieldType = 'enumeration'.
	/// </summary>
	/// <returns>An IRulesList of Strings.</returns>
	/// <remarks>Note that this returns an IRulesList containing
	/// Strings suitable for use in a user interface.</remarks>
	IRulesList getPossibleFieldValues ();
}

