package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IFormRulesInfo;
import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IFormTemplateGroup;
import com.intuit.payroll.agency.api.IRulesFormTemplate;
import com.intuit.payroll.agency.dao.DAOFactory;
/// <summary>
/// Implementation class for the global interface of the Rules Component.
/// </summary>
public final class FormRulesInfo implements IFormRulesInfo {
	private DAOFactory m_factory;
	
	/// <summary>
	/// Default constructor
	/// </summary>
	public FormRulesInfo()
	{
		m_factory = DAOFactory.getDAOFactory();
	}
	
	/// <summary>
	/// Returns a list of active form template ids.
	/// </summary>
	/// <returns>An IRulesList of id's corresponding to 
	/// valid IRulesFormTemplates.</returns>
	/// <remarks>
	/// These ids can be used by the client to retrieve 
	/// individual form templates using getFormTemplateByFormTemplateID(String id).
	/// </remarks>
	public IRulesList getActiveFormTemplateIDList()
	{
		return m_factory.getFormTemplateDAO().getActiveFormTemplateIdList();
	}

	/// <summary>
	/// Retrieve the IRulesFormTemplate object represented in the
	/// agency rules store by the supplied String argument 
	/// <code>id</code>.
	/// </summary>
	/// <param name="id">The id of the form template in
	/// the agency rules store.</param>
	/// <returns>An IRulesFormTemplate object represented by the supplied
	/// id in the agency rules.</returns>
	public IRulesFormTemplate getFormTemplateByFormTemplateID(String id)
	{
		return m_factory.getFormTemplateDAO().getFormTemplateByFormTemplateId(id);
	}

	/// <summary>
	/// Retrieve the IRulesFormTemplate object represented in the
	/// agency rules data store by the supplied <code>id</code>.
	/// </summary>
	/// <param name="id">The TPS form id of the tax form.</param>
	/// <returns>An IRulesFormTemplate object represented by the
	/// supplied form id in the agency rules.</returns>
	public IRulesFormTemplate getFormTemplateByFormID(String id)
	{
		return m_factory.getFormTemplateDAO().getFormTemplateByFormId(id);
	}

	/// <summary>
	/// Retrieve a list of form template ids that are referred to
	/// by the supplied list of law ids in the agency rules data store.
	/// </summary>
	/// <param name="lawIDs">A list of law ids to get a list of
	/// corresponding form template ids for.</param>
	/// <returns>An IRulesList filled with String formTemplateIDs.</returns>
	public IRulesList getActiveFormTemplateIDListFromLawIDs(IRulesList lawIDs)
	{
		return DAOFactory.getDAOFactory().getFormTemplateDAO().getActiveFormTemplateIDListFromLawIDs(lawIDs);
	}

	/// <summary>
	/// Retrieve a form template group object that contains the form
	/// template referred to by the supplied form template id.
	/// </summary>
	/// <param name="id">The id of the form template to get
	/// the owner group for.</param>
	/// <returns>The form template group that includes the given
	/// form template.</returns>
	public IFormTemplateGroup getFormTemplateGroup(String id)
	{
		return DAOFactory.getDAOFactory().getFormTemplateGroupDAO().getFormTemplateGroup(id);
	}

	/// <summary>
	/// Retrieve a list of all non-obsolete form template group IDs.
	/// </summary>
	/// <returns>An IRulesList of form template group IDs.</returns>
	public IRulesList getActiveFormTemplateGroupIDList()
	{
		return DAOFactory.getDAOFactory().getFormTemplateGroupDAO().getActiveFormTemplateGroupIDList();
	}
}