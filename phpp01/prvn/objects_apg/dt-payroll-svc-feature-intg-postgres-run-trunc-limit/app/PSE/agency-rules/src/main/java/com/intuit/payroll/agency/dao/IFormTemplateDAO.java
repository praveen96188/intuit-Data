//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IRulesFormTemplate;
/// <summary>
/// Defines methods requried to retrieve form
/// template data from the agency rules data store.
/// </summary>
public interface IFormTemplateDAO
{
	/// <summary>
	/// Retrieves a list of all of the active (read: non-obsolete)
	/// form template ids from the agency rules data store.
	/// </summary>
	/// <returns>An IRulesList of form template ids.</returns>
	IRulesList getActiveFormTemplateIdList();
	
	/// <summary>
	/// Retrieves an IRulesFormTemplate object corresponding to
	/// the data represented by the supplied argument id in the
	/// agency rules data store.
	/// </summary>
	/// <param name="formTemplateId">The id of the IRulesFormTemplate object
	/// to retrieve.</param>
	/// <returns>An IRulesFormTemplate object.</returns>
	IRulesFormTemplate getFormTemplateByFormTemplateId(String formTemplateId);
	
	/// <summary>
	/// Retrieves an IRulesFormTemplate object corresponding
	/// to the form id supplied as an argument.
	/// </summary>
	/// <param name="formId">The form id of the form template to 
	/// retrieve.</param>
	/// <returns>An IRulesFormTemplate object.</returns>
	IRulesFormTemplate getFormTemplateByFormId(String formId);
	
	/// <summary>
	/// get a list of all law ids referred to underneath the specified
	/// form template.
	/// </summary>
	/// <param name="formTemplateId">Id to retrieve the form template for.</param>
	/// <returns>A list of law ids.</returns>
	IRulesList getLawIdList(String formTemplateId);
	
	/// <summary>
	/// get all of the active submit method ids on the form template
	/// identified by the supplied id.
	/// </summary>
	/// <param name="formTemplateId">The id of the form template
	/// to get the active submit methods for.</param>
	/// <returns>A list of active submit method ids.</returns>
	IRulesList getActiveFormSubmitMethodIDList(String formTemplateId);
	
	/// <summary>
	/// get all of the active form template IDs that have references
	/// to any of the law IDs in the supplied list.
	/// </summary>
	/// <param name="lawIDs">List of law IDs to get the corresponding
	/// form template IDs for.</param>
	/// <returns>An IRulesList of FormTemplateID's.</returns>
	IRulesList getActiveFormTemplateIDListFromLawIDs(IRulesList lawIDs);
}
