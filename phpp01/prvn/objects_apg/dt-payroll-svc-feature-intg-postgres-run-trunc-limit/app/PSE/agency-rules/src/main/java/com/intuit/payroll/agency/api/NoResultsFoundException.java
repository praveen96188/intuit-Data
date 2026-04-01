//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Exception thrown when a query is executed and no results
/// are found when at least one is expected. Not thrown if
/// zero results is a possible, legal return value of the operation.
/// </summary>
public class NoResultsFoundException extends RuntimeException 
{
	/// <summary>
	/// Simple Constructor.
	/// </summary>
	public NoResultsFoundException()
	{
	}

	/// <summary>
	/// Constructor that accepts a human-readable message.
	/// </summary>
	/// <param name="message">The message.</param>
	public NoResultsFoundException(String message)
	{
	    super(message);
    }
   
}
