//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.util.DueDateRollingPolicy;

public interface IPaymentTemplateDAO
{
	/// <summary>
	/// List of active payment template ids.
	/// </summary>
	/// <returns>IListResponse collection of payment template IDs.</returns>
	IRulesList getActivePaymentTemplateIDList();

	/// <summary>
	/// List of active payment template ids, given a set of tax law ids.
	/// </summary>
	/// <param name="lawIDList">IListRequest collection of law IDs.</param>
	/// <returns>IListResponse collection of payment template IDs.</returns>
	IRulesList getActivePaymentTemplateIDListFromLawIDs(IRulesList lawIDList);
	
	/// <summary>
	/// get a payment template object, given a payment template id.
	/// </summary>
	/// <param name="id">The ID of the Template to retrieve.</param>
	/// <returns>A PaymentTemplate</returns>
	IRulesPaymentTemplate getPaymentTemplate(String id);

	IRulesList getLawIDList(String paymentTemplateID);

	/// <summary>
	/// get a DatePolicy constructed from the data for a given payment template.
	/// </summary>
	/// <param name="paymentTemplateID">The ID of the template to generate a DatePolicy for.</param>
	/// <returns>A DatePOlicy object with all fields filled in with the rules data from the datasource.</returns>
	DueDateRollingPolicy getDueDatePolicy(String paymentTemplateID);
}
