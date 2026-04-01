//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Used to identify the type of PaymentPeriodRequest during 
/// processing. The Unknown value is used as a default to
/// check for unconfigured payment period request objects.
/// </summary>
public class PaymentPeriodRequestType
{
	private final String name;
	private PaymentPeriodRequestType(String name) {this.name = name;}

	public String toString() {return name;}

	
	/// <summary>
	/// The default if no type has been specified. 
	/// </summary>
	public static final PaymentPeriodRequestType Unknown = new PaymentPeriodRequestType("Unknown");
	/// <summary>
	/// Use the supplied rules data to attempt to 
	/// generate a payment period matching the 
	/// criteria in the request object.
	/// </summary>
	public static final PaymentPeriodRequestType RulesBased = new PaymentPeriodRequestType("RulesBased");
	/// <summary>
	/// Use the supplied user rules, along with general
	/// frequency information, to generate a payment
	/// period matching the criteria.
	/// </summary>
	public static final PaymentPeriodRequestType UserDefined = new PaymentPeriodRequestType("UserDefined");
	
}
