package com.intuit.payroll.agency.api;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/PSE/AgencyRules/src/com/intuit/payroll/agency/api/IAgencyFormat.java $
 * $Revision: #1 $
 * $DateTime: 2012/06/21 09:09:55 $
 * $Author: JChickanosky $
 */
public interface IAgencyFormat {
	/// <summary>
	/// Allowable AID format for this payment template.  This is in QuickBooks AID format (e.g. 99-9999999).
	/// </summary>
    public String getFormat();

    public String getRegularExpression();
}
