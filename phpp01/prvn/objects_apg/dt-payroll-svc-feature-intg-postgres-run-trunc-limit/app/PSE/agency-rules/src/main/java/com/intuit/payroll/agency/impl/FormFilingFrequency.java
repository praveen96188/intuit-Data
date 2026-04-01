//---------------------------------------------------------------------------
// Copyright 2007 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IFormFilingFrequency;
/// <summary>
/// Implementation of the form template filing frequency
/// data.	
/// </summary>
class FormFilingFrequency implements IFormFilingFrequency {
	private String m_longDescription;
	private String m_shortDescription;
	private boolean m_isObsolete = false;
	private String m_formFrequencyId;

	/// <summary>
	/// The id of the form frequency.
	/// </summary>
	public String getFormFrequencyID()
	{
		 return m_formFrequencyId; 
	}
	public void setFormFrequencyID(String that)
	{
		m_formFrequencyId = that;
	}

	/// <summary>
	/// True if this IFormFrequency is considered obsolete
	/// by Intuit.
	/// </summary>
	/// <remarks>Once a form frequency has been marked as obsolete,
	/// there should be a process used to "upgrade" previously used
	/// obsolete frequency to the new ones that should be used in 
	/// their place. TODO [zjm]: Update this remark with the actual 
	/// process to be used (from an agency rules perspective).</remarks>
	public boolean getIsObsolete()
	{
		 return m_isObsolete; 
	}
	public void setIsObsolete(boolean that)
	{
		m_isObsolete = that;
	}

	/// <summary>
	/// A customer-facing short text description of the frequency.
	/// </summary>
	/// <example>"Monthly"</example>
	/// <remarks>
	/// This String value can be used in product user
	/// interfaces.
	/// </remarks>
	public String getShortDescription()
	{
		 return m_shortDescription; 
	}
	public void setShortDescription(String that)
	{
		m_shortDescription = that;
	}

	/// <summary>
	/// A customer facing long text description of the filing 
	/// frequency.
	/// </summary>
	/// <example>"Forms must be filed by the 10th of the first 
	/// month after the end of the quarter."</example>
	/// <remarks>
	/// This String value can be used in product interfaces.
	/// </remarks>
	public String getLongDescription()
	{
		 return m_longDescription; 
	}
	public void setLongDescription(String that)
	{
		m_longDescription = that;
	}
}