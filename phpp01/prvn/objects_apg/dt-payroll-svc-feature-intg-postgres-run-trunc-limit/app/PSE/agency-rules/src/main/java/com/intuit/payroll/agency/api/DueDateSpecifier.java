//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction
// is a violation of applicable law. This material contains certain
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Specifies whether the due date is a postmark date or a received by date.
/// </summary>
public class DueDateSpecifier
{
    private final String name;
    private DueDateSpecifier(String that)
    {
        name = that;
    }
    public String toString ()
    {
        return name;
    }
    /// <summary>
	/// Agency accepts postmark on due date.
	/// </summary>
	public static final DueDateSpecifier PostMark = new DueDateSpecifier ("POSTMARK");
	/// <summary>
	/// Agency doesn't accept postmark, you gotta get it to them.
	/// </summary>
    public static final DueDateSpecifier ReceivedBy = new DueDateSpecifier ("RECEIVED");

    public static DueDateSpecifier MapDueDateSpecifier (String xmlValue)
    {
        if (xmlValue.equals(PostMark.name)) return PostMark;
        if (xmlValue.equals(ReceivedBy.name)) return ReceivedBy;
        throw new RuntimeException("Bad due date specifier string");
    }



}
