//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.impl.PaymentTemplate;

/// <summary>
/// </summary>
public class SettlementData  {
	public String uiString;
	public String minOffset;
	public String maxOffset;
	public String thisMonth="false";
	public boolean thisQuarterOrFirstMonthOfNextQuarter;

	// just so the xslt is consistent with other "sets"
	public void setUIString (String uiString) {this.uiString = uiString;}
	public void setMinOffset (String minOffset) {this.minOffset = minOffset;}
	public void setMaxOffset (String maxOffset) {this.maxOffset = maxOffset;}
	public void setThisMonth (String thisMonth) {this.thisMonth = thisMonth;}
	public void setThisQuarterOrFirstMonthOfNextQuarter (boolean thisQuarterOrFirstMonthOfNextQuarter) {this.thisQuarterOrFirstMonthOfNextQuarter = thisQuarterOrFirstMonthOfNextQuarter;}
	public void setThisQuarterOrFirstMonthOfNextQuarter (String xml) 
	{
		thisQuarterOrFirstMonthOfNextQuarter = PaymentTemplate.valueAsBoolean(xml);
	}

}
