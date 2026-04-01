//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Enumerates the various release levels of agencies 
/// and submit methods.
/// </summary>
public class ReleaseStatus
{
    private final String name;
    private ReleaseStatus(String that)
    {
        name = that;
    }
    public String toString ()
    {
        return name;
    }
    /// <summary>
	/// Generally reserved for pre-release items.
	/// </summary>
	public static final ReleaseStatus Alpha = new ReleaseStatus ("Alpha");
	/// <summary>
	/// Denotes items that are considered in beta.
	/// </summary>
    public static final ReleaseStatus Beta = new ReleaseStatus ("Beta");
	/// <summary>
	/// Denotes items that are considered in production.
	/// </summary>
    public static final ReleaseStatus Production = new ReleaseStatus ("Production");
	/// <summary>
	/// Items that should no longer be used, but may
	/// be present for backwards-compatibility/upgrade
	/// reasons.
	/// </summary>
    public static final ReleaseStatus Obsolete = new ReleaseStatus ("Obsolete");

    public static ReleaseStatus MapReleaseStatus (String xmlValue)
    {
        if (xmlValue.equals(Alpha.name)) return Alpha;
        if (xmlValue.equals(Beta.name)) return Beta;
        if (xmlValue.equals(Production.name)) return Production;
        if (xmlValue.equals(Obsolete.name)) return Obsolete;
        throw new RuntimeException("Bad release status string");
    }

    

}
