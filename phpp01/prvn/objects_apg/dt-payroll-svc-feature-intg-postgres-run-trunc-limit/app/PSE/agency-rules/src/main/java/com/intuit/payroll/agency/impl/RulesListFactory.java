//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
	/// <summary>
	/// Summary description for RulesListFactory.
	/// </summary>

package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IRulesList;

public class RulesListFactory
{
    /// <summary>
    /// factory method for IRulesList
    /// </summary>
    static public IRulesList createRulesList () {return new RulesList ();}
}
