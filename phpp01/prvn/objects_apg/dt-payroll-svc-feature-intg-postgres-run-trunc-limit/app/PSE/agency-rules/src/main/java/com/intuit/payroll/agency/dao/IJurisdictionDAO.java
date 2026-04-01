//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IJurisdiction;

public interface IJurisdictionDAO
{
	IJurisdiction getJurisdiction(String id);
	IRulesList getActiveJurisdictionIDList();
}
