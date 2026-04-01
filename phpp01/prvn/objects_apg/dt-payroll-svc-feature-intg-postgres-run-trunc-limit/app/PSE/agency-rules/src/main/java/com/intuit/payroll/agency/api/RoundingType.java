//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Enumerates the ways to round.
/// </summary>
public class RoundingType
{
    private final String name;
    private RoundingType(String that)
    {
        name = that;
    }
    public String toString ()
    {
        return name;
    }
    /// <summary>
	/// Don't round.
	/// </summary>
	public static final RoundingType NoRounding = new RoundingType ("NoRounding");
	/// <summary>
	/// Nearest dollar.
	/// </summary>
    public static final RoundingType NearestDollar = new RoundingType ("NearestDollar");
	/// <summary>
	/// Drop the pennies (truncate).
	/// </summary>
    public static final RoundingType DropPennies = new RoundingType ("DropPennies");

    public static RoundingType createRoundingType (String xmlValue)
    {
        if (xmlValue.equals(NoRounding.name)) return NoRounding;
        if (xmlValue.equals(NearestDollar.name)) return NearestDollar;
        if (xmlValue.equals(DropPennies.name)) return DropPennies;
        throw new RuntimeException("Bad rounding string");
    }
}
