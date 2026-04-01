//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IAgency;
import com.intuit.payroll.agency.api.IRulesList;

public interface IAgencyDAO
{
	IAgency getAgency(String id);
	IRulesList getActiveAgencyIDList();
	IAgency getAgencyByEnrollmentGroupId(String enrollmentGroupId);
}

