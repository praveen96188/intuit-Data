//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IFormTemplateGroup;
/// <summary>
/// Internal implementation of the IFormTemplateGroup interface.
/// </summary>
public class FormTemplateGroup implements IFormTemplateGroup {
	private String m_longDescription;
	private String m_shortDescription;
	private boolean m_isObsolete = false;
	private String m_formTemplateGroupId;

	/// <summary>
	/// The String id of the form template group
	/// </summary>
	public String getFormTemplateGroupID()
	{
		 return m_formTemplateGroupId; 
	}
	public void setFormTemplateGroupID(String that)
	{
		m_formTemplateGroupId = that;
	}

	/// <summary>
	/// Indicates whether the form template group is
	/// considered obsolete and unusable by Intuit
	/// Tax Dev.
	/// </summary>
	public boolean getIsObsolete()
	{
		 return m_isObsolete; 
	}
	public void setIsObsolete(boolean that)
	{
		m_isObsolete = that;
	}
	public void setIsObsolete(String that)
	{
		m_isObsolete = PaymentTemplate.valueAsBoolean(that);
	}


	/// <summary>
	/// A short text description of the form template group.
	/// </summary>
	/// <remarks>
	/// This field is approved to be used in customer-facing
	/// user interfaces.
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
	/// A longer text description of the form template group.
	/// </summary>
	/// <remarks>
	/// This field is approved to be used in customer-facing
	/// user interfaces.
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