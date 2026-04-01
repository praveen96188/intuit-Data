//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// UserFrequencyType defines the User-defined frequency.
/// </summary>
public class UserFrequencyType
{
    private final String name;

    private UserFrequencyType (String name)
    {
        this.name = name;
    }

    public String toString ()
    {
        return name;
    }

	/// <summary>
	/// No schedule at all.
	/// </summary>
    public static final UserFrequencyType NoSchedule = new UserFrequencyType("NoSchedule");
	
	/// <summary>
	/// Follows the Federal Quarterly Schedule.
	/// </summary>
    public static final UserFrequencyType FedQuarterly = new UserFrequencyType("FedQuarterly");

	/// <summary>
	/// Follows the Federal Monthly Schedule.
	/// </summary>
    public static final UserFrequencyType FedMonthly = new UserFrequencyType("FedMonthly");

	/// <summary>
	/// Follows the Federal Semi-weekly Schedule.
	/// </summary>
    public static final UserFrequencyType FedSemiweekly = new UserFrequencyType("FedSemiweekly");

	/// <summary>
	/// Follows the Federal Next day Schedule.
	/// </summary>
    public static final UserFrequencyType FedNextDay = new UserFrequencyType("FedNextDay");

	/// <summary>
	/// A custom annual frequency.  Will require more inputs.
	/// </summary>
    public static final UserFrequencyType CustomAnnual = new UserFrequencyType("CustomAnnual");

	/// <summary>
	/// A custom quarterly frequency.  Will require more inputs.
	/// </summary>
    public static final UserFrequencyType CustomQuarterly = new UserFrequencyType("CustomQuarterly");

	/// <summary>
	/// A custom monthly frequency.  Will require more inputs.
	/// </summary>
    public static final UserFrequencyType CustomMonthly = new UserFrequencyType("CustomMonthly");

	/// <summary>
	/// A custom weekly frequency.  Will require more inputs.
	/// </summary>
    public static final UserFrequencyType CustomWeekly = new UserFrequencyType("CustomWeekly");

	/// <summary>
	/// Follows the Federal Annual Schedule.
	/// </summary>
    public static final UserFrequencyType FedAnnual = new UserFrequencyType("FedAnnual");
	
	/// <summary>
	/// A Frequency to run after each payroll run.
	/// </summary>
    public static final UserFrequencyType AfterEveryPayrollRun = new UserFrequencyType("AfterEveryPayrollRun");

}
