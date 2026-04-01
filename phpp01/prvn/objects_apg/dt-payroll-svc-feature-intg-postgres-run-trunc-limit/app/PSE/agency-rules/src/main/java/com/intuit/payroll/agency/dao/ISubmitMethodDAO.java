//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.ISubmitMethod;
/// <summary>
/// An interface to create ISubmitMethod objects from a
/// datasource and access list data associated with them.
/// </summary>
public interface ISubmitMethodDAO
{
	/// <summary>
	/// get a submit method from the datasource.
	/// </summary>
	/// <param name="paymentTemplateID">The 
	/// payment template the submit method resides 
	/// under.</param>
	/// <param name="submitMethodType">The type of
	/// submit method desired.</param>
	/// <returns>An ISubmitMethod object.</returns>
	ISubmitMethod getSubmitMethod(String paymentTemplateID, String submitMethodType);
	
	/// <summary>
	/// get a list of submit method IDs available
	/// to a given payment template.
	/// </summary>
	/// <param name="paymentTemplateID">The payment template
	/// to query for available submit methods.</param>
	/// <returns>An IRulesList of String submit method types that
	/// are available for retrieval.</returns>
	IRulesList getActiveSubmitMethodIDList(String paymentTemplateID);
	
	/// <summary>
	/// get a list of tax IDs for which it is ok
	/// to send negatives.
	/// </summary>
	/// <param name="paymentTemplateID">
	/// The ID of the payment template the desired submit
	/// method resides under.
	/// </param>
	/// <param name="submitMethodType">
	/// The type of the submit method that the list of 
	/// desired tax IDs resides in.
	/// </param>
	/// <returns>An IRulesList of integer tax IDs.</returns>
	IRulesList getTaxIDToAllowNegativesList(String paymentTemplateID, String submitMethodType);
	
}
