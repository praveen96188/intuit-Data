//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IPaymentFrequency;
/// <summary>
/// Defines methods to retrieve payment frequency info from the 
/// agency rules data store.
/// </summary>
public interface IPaymentFrequencyDAO
{
	/// <summary>
	/// List of active payment frequencies (non-obsolete) from a given payment template ID.
	/// </summary>
	/// <param name="paymentTemplateID"></param>
	/// <returns></returns>
	// 
	IRulesList getActivePaymentFrequencyIDList(String paymentTemplateID);
	
	IPaymentFrequency getPaymentFrequency(String paymentTemplateID, String paymentFrequencyID);
}
