//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.util.IAgencyHoliday;
//import com.intuit.spc.foundations.portability.collections.SpcfCollectionIterable;

public interface IHolidayGroupDAO
{
	/// <summary>
	/// gets the holidays for a given Group ID.
	/// </summary>
	/// <param name="holidayGroupID">The ID of the group to get holidays for.</param>
	/// <returns>A list of IAgencyHolidays</returns>
	Iterable<IAgencyHoliday> getHolidays(String holidayGroupID);
//	SpcfCollectionIterable<IAgencyHoliday> getHolidays(String holidayGroupID);
}
