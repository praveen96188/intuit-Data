//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction
// is a violation of applicable law. This material contains certain
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

/// <summary>
/// Interface to support retrieval of form template group data
/// from the agency rules data store.
/// </summary>

import com.intuit.payroll.agency.api.IFormTemplateGroup;
import com.intuit.payroll.agency.api.IRulesList;
public interface IFormTemplateGroupDAO
{
    /// <summary>
    /// Retrieve the form template group identified by the
    /// supplied ID.
    /// </summary>
    /// <param name="formTemplateGroupId">The ID of the form
    /// template group to retrieve.</param>
    /// <returns>An IFormTemplateGroup object.</returns>
    IFormTemplateGroup getFormTemplateGroup(String formTemplateGroupId);

    /// <summary>
    /// Retrieve a list of all non-obsolete form template IDs.
    /// </summary>
    /// <returns>An IRulesList of string form template IDs.</returns>
    IRulesList getActiveFormTemplateGroupIDList();
}
