//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.ReleaseStatus;
import com.intuit.payroll.agency.api.IFormSubmitMethod;
/// <summary>
/// Internal implementation of the IFormSubmitMethod 
/// interface.
/// </summary>
class FormSubmitMethod implements IFormSubmitMethod {
	private int m_sendByOffset = 0;
	private String m_enrollmentGroupId;
	private String m_description;
	private boolean m_isObsolete = false;
	private String m_submitMethodType;
	private ReleaseStatus m_releaseStatus = ReleaseStatus.Obsolete; // If not overridden, default is Obsolete

	/// <summary>
	/// The String id of the submit method.
	/// </summary>
	/// <example>
	/// "EFE-EFILE", "PRINT"
	/// </example>
	public String getSubmitMethodType()
	{
		 return m_submitMethodType; 
	}
	public void setSubmitMethodType(String that)
	{
		m_submitMethodType = that;
	}

	/// <summary>
	/// Describes whether this submit method is
	/// no longer considered usable by Intuit
	/// tax development.
	/// </summary>
	public boolean getIsObsolete()
	{
		 return m_isObsolete; 
	}
	public void setIsObsolete(boolean that)
	{
		m_isObsolete = that;
	}

	/// <summary>
	/// Defines the customer-facing name of this
	/// submit method.
	/// </summary>
	/// <example>"E-file", "Print"</example>
	/// <remarks>
	/// This field is cleared to be used in customer
	/// facing user interfaces.
	/// </remarks>
	public String getDescription()
	{
		 return m_description; 
	}
	public void setDescription(String that)
	{
		m_description = that;
	}

	/// <summary>
	/// Holds the ID used to retrieve enrollment data.
	/// </summary>
	public String getEnrollmentGroupID()
	{
		 return m_enrollmentGroupId; 
	}
	public void setEnrollmentGroupID(String that)
	{
		m_enrollmentGroupId = that;
	}

	/// <summary>
	/// Number of days to bump the agency due date
	/// in order to get the agency send by date.
	/// </summary>
	public int getSendByOffset()
	{
		 return m_sendByOffset; 
	}
	public void setSendByOffset(int that)
	{
		m_sendByOffset = that;
	}

	/// <summary>
	/// The status of the item with regard to release level.
	/// </summary>
	public ReleaseStatus getReleaseStatus()
	{
		 return m_releaseStatus; 
	}
	public void setReleaseStatus(ReleaseStatus that)
	{
		m_releaseStatus = that;
	}
	public void setReleaseStatus(String that)
	{
		m_releaseStatus = ReleaseStatus.MapReleaseStatus (that);
	}
}