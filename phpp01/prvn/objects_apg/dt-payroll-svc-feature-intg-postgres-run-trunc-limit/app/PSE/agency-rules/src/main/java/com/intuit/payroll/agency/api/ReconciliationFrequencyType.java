//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Enumerates the possible reconciliation frequencies, used by QBOE payroll engine 
/// in the payment calculation algorithm.
/// </summary>
public class ReconciliationFrequencyType
{
    private final String name;
    private ReconciliationFrequencyType(String that)
    {
        name = that;
    }
    public String toString ()
    {
        return name;
    }
    /// <summary>
	/// Quarterly reconciled.
	/// </summary>
	public static final ReconciliationFrequencyType Quarterly = new ReconciliationFrequencyType ("Quarterly");
	/// <summary>
	/// Reconciled annually.
	/// </summary>
    public static final ReconciliationFrequencyType Annual = new ReconciliationFrequencyType ("Annual");

    public static ReconciliationFrequencyType createReconciliationFrequencyType (String xmlValue)
    {
        if (xmlValue.equals(Quarterly.name)) return Quarterly;
        if (xmlValue.equals(Annual.name)) return Annual;
        throw new RuntimeException("Bad reconciliation frequency string");
    }

    

}
