//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Data interface for the header information on the
/// credentials user interface.
/// </summary>
/// <remarks>
/// The ICredentialFormHeader contains mostly text
/// that is used to build the informational header of the
/// credential submission UI.  It could theoretically contain
/// other items like input definitions, but the header should
/// remain as an informational tool only in the near future.
/// </remarks>
public interface ICredentialFormHeader
{
	/// <summary>
	/// Text label for the title of the header section.
	/// </summary>
	String getTitleLabel ();
	
	/// <summary>
	/// Text label for the agency label in the header section.
	/// </summary>
	String getAgencyLabel ();
	
	/// <summary>
	/// Text label for the period label in the header section.
	/// </summary>
	String getPeriodLabel ();
	
	/// <summary>
	/// Text label for the amonut label in the header section.
	/// </summary>
	String getAmountLabel ();
	
	/// <summary>
	/// Text label for the settlement date label in the header section.
	/// </summary>
	String getSettlementDateLabel ();
	
	/// <summary>
	/// Text label for the reason label in the header section.
	/// </summary>
	String getReasonLabel ();
}
