//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.api.*;
/// <summary>
/// Implementation for the IRulesFormTemplate interface. 
/// </summary>
public abstract class FormTemplate implements IRulesFormTemplate {
	protected String m_formId="";
	protected String m_defaultSubmitMethodId="";
	protected String m_rulesFormTemplateGroupId="";
	protected boolean m_isRequired = false;
	protected String m_description="";
	protected String m_name="";
	protected boolean m_isObsolete = false;
	protected String m_formTemplateId="";
	protected String m_agencyId="";
	protected IFormInfo m_formInfo = null;
	
	/// <summary>
	/// The identification String defined by the agency rules.
	/// </summary>
	public String getFormTemplateID()
	{
		 return m_formTemplateId; 
	}
	public void setFormTemplateID(String that)
	{
		m_formTemplateId = that;
	}
	
	/// <summary>
	/// The id of the agency this form
	/// is for.
	/// </summary>
	public String getAgencyID()
	{
		 return m_agencyId; 
	}
	public void setAgencyID(String that)
	{
		m_agencyId = that;
	}
	
	/// <summary>
	/// The id of the form as reported by tax dev.
	/// </summary>
	public String getFormID()
	{
		 return m_formId; 
	}
	public void setFormID(String that)
	{
		m_formId = that;
	}

	/// <summary>
	/// True if this form template is considered obsolete by
	/// Intuit.
	/// </summary>
	/// <remarks>Once a form template has been marked as obsolete,
	/// there should be a process used to "upgrade" previously used
	/// obsolete template to the new ones that should be used in 
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
	public void setIsObsolete(String that)
	{
		m_isObsolete = PaymentTemplate.valueAsBoolean(that);
	}


	/// <summary>
	/// The customer-facing name for this form template.
	/// </summary>
	/// <remarks>
	/// This String value can be used in product user
	/// interfaces.
	/// </remarks>
	public String getName()
	{
		 return m_name; 
	}
	public void setName(String that)
	{
		m_name = that;
	}

	/// <summary>
	/// The customer-facing description for this form template.
	/// </summary>
	/// <remarks>
	/// This String value can be used in product user interfaces.
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
	/// Indicates if the filer is required to submit this form.
	/// </summary>
	public boolean getIsRequired()
	{
		 return m_isRequired; 
	}
	public void setIsRequired(boolean that)
	{
		m_isRequired = that;
	}
	public void setIsRequired(String that)
	{
		m_isRequired = PaymentTemplate.valueAsBoolean(that);
	}

	/// <summary>
	/// Indicates the form template group this form belongs to,
	/// if any.
	/// </summary>
	/// <remarks>
	///	This is an optional field.
	/// </remarks>
	/// <returns>
	/// If present in the agency rules, will return the group id
	/// this form template belongs to.  If not present, then this
	/// property returns null.
	/// </returns>
	public String getRulesFormTemplateGroupID()
	{
		 return m_rulesFormTemplateGroupId; 
	}
	public void setRulesFormTemplateGroupID(String that)
	{
		m_rulesFormTemplateGroupId = that;
	}

	/// <summary>
	/// The form information and associated metadata.
	/// </summary>
	public abstract IFormInfo getFormInfo();
//	{
//		if(m_formInfo == null)
//		{
//			m_formInfo = DAOFactory.getDAOFactory().getFormInfoDAO().getFormInfoByFormId(m_formId);
//		}
//		return m_formInfo;
//	}

	/// <summary>
	/// The filing frequency for this form template.
	/// </summary>
	public abstract IFormFilingFrequency getFormFilingFrequency();
//	{
//		return DAOFactory.getDAOFactory().getFormFilingFrequencyDAO().getFilingFrequency(getFormTemplateID());
//	}

	/// <summary>
	/// Identifies the default submit method id to use if none
	/// is specified by the client.
	/// </summary>
	public String getDefaultSubmitMethodID()
	{
		 return m_defaultSubmitMethodId; 
	}
	public void setDefaultSubmitMethodID(String that)
	{
		m_defaultSubmitMethodId = that;
	}

	/// <summary>
	/// get the ISubmitMethod object corresponding to the id passed
	/// as an argument.
	/// </summary>
	/// <param name="submitMethodId">The id of the submit method to retrieve.</param>
	/// <returns>An ISubmitMethod of the submit method identified by the
	/// argument. If no submit method with that id is referenced by this 
	/// form template then null is returned.</returns>
	public abstract IFormSubmitMethod getSubmitMethod(String submitMethodId);
//	{
//		return DAOFactory.getDAOFactory().getFormSubmitMethodDAO().getFormSubmitMethod(m_formTemplateId, submitMethodId);
//	}

	/// <summary>
	/// gets a list of law ids that apply to this form template.
	/// </summary>
	/// <returns>An IRulesList of String law ids.</returns>
	public abstract IRulesList getLawIDList();
//	{
//		return DAOFactory.getDAOFactory().getFormTemplateDAO().getLawIdList(m_formTemplateId);
//	}

	/// <summary>
	/// Retrieve the form template group that contains the current form
	/// template.
	/// </summary>
	/// <returns>An IFormTemplateGroup object that the form template
	/// instance belongs to.</returns>
	public abstract IFormTemplateGroup getFormTemplateGroup();
//	{
//		return DAOFactory.getDAOFactory().getFormTemplateGroupDAO().getFormTemplateGroup(getRulesFormTemplateGroupID());
//	}

	/// <summary>
	/// Retrieve a list of all active submit methods on the form
	/// template.
	/// </summary>
	/// <returns>A list of String submit method IDs.</returns>
	public abstract IRulesList getActiveSubmitMethodIDList();
//	{
//		return DAOFactory.getDAOFactory().getFormTemplateDAO().getActiveSubmitMethodIDList(getFormTemplateID());
//	}
}
