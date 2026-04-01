//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.util;

/// <summary>
/// When an expected due date falls on a weekend or holiday,
/// use this enum to specifiy whether the agency policy is to
/// roll back days or forward days to recalculate the date.
/// </summary>
public class DateRollingPolicy
{
    private final String name;

    private DateRollingPolicy (String name)
    {
        this.name = name;
    }

	static public DateRollingPolicy createDateRollingPolicy(String str)
	{
		if (str.equals("Forward") || str.equals("forward"))
		{
			return Forward;
		} 
		else if (str.equals("Backward") || str.equals("backward"))
		{
			return Backward;
		}
		else 
        {
            return None;
		}
	}

    public String toString ()
    {
        return name;
    }

    /// <summary>
    /// Roll the date forward until a valid date is found.
    /// </summary>
    public static final DateRollingPolicy Forward = new DateRollingPolicy("Forward");

    /// <summary>
    /// Roll the date backwards until a valid date is found.
    /// </summary>
    public static final DateRollingPolicy Backward = new DateRollingPolicy("Backward");

    /// <summary>
    /// Do not roll the date. Use the date whether it is valid or not.
    /// </summary>
    public static final DateRollingPolicy None = new DateRollingPolicy("None");
}
