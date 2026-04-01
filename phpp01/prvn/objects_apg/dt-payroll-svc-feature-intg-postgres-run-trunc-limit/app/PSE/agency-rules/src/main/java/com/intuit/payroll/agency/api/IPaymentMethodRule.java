//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Interface for the PaymentMethodRule object. This interface is exposed through COM.
/// </summary>
public interface IPaymentMethodRule
{
	/// <summary>
	/// ID of the payment template.
	/// </summary>
	String getPaymentTemplateID();

	/// <summary>
	/// ID of the submit method type.
	/// </summary>
	String getSubmitMethodType();
}
