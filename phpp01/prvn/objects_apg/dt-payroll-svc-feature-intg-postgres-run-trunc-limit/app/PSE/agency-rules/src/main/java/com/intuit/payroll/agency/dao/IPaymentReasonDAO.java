//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IPaymentReason;
import com.intuit.payroll.agency.api.IRulesList;

public interface IPaymentReasonDAO
{
	/// <summary>
	/// Method to get a list of payment reason codes of a given payment template / submit method.
	/// </summary>
	/// <param name="paymentTemplateID">Payment template id which the submit method belongs to.</param>
	/// <param name="submitMethodType">Submit method id which the payment reasons belongs to.</param>
	/// <returns>A list of active payment readon codes.</returns>
	IRulesList getActivePaymentReasonCodeList(String paymentTemplateID, String submitMethodType);

	/// <summary>
	/// Method to get a payment reason object.
	/// </summary>
	/// <param name="paymentTemplateID">Payment template id which the submit method belongs to.</param>
	/// <param name="submitMethodID">Submit method id which this payment reason belongs to.</param>
	/// <param name="paymentReasonCode">Code of the payment reason that we want.</param>
	/// <returns>Payment reason object.</returns>
	IPaymentReason getPaymentReason(String paymentTemplateID, String submitMethodID, String paymentReasonCode);
}
