//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.IFormSubmitMethod;
import com.intuit.payroll.agency.api.IFormTemplateGroup;
import com.intuit.payroll.agency.api.IFormInfo;
import com.intuit.payroll.agency.api.IFormFilingFrequency;
import com.intuit.payroll.agency.api.RulesObjectBroker;
import com.intuit.payroll.agency.api.ISubmitMethod;
import com.intuit.payroll.agency.impl.FormTemplate;
import com.intuit.payroll.agency.dao.mnemonics.MnemonicPeriod;

import java.util.ArrayList;

public class FormTemplateData extends FormTemplate {

	private ArrayList<FrequencyData> freqs = new ArrayList<FrequencyData>();
    private IRulesList activeFreqIDs;

	// collection of Integer (lawIDs)
	private ArrayList<LawData> laws = new ArrayList<LawData>();
	// collection of SubmitMethod
	private ArrayList<SubmitMethodData> submitMethods = new ArrayList<SubmitMethodData>();
    private IRulesList activeSubmitMethodIDs;

	public ArrayList<MnemonicPeriod> periods = new ArrayList<MnemonicPeriod>();

	public void addFrequency (FrequencyData freq) {
        if (freqs.size() > 0)
        {
            throw new RuntimeException("nyi");// rest of interface assumes only one
        }
        freqs.add (freq);
	}
	public void addLaw (LawData law) {
		laws.add (law);
	}
	public void addSubmitMethod (SubmitMethodData s) {
		submitMethods.add (s);
	}



	/// <summary>
	/// The form information and associated metadata.
	/// </summary>
	public IFormInfo getFormInfo()
	{
		if(m_formInfo == null)
		{
			m_formInfo = DAOFactory.getDAOFactory().getFormInfoDAO().getFormInfoByFormId(m_formId);
		}
		return m_formInfo;
	}

	/// <summary>
	/// The filing frequency for this form template.
	/// </summary>
	public IFormFilingFrequency getFormFilingFrequency()
	{
		return freqs.get(0);
	}

	/// <summary>
	/// get the ISubmitMethod object corresponding to the id passed
	/// as an argument.
	/// </summary>
	/// <param name="submitMethodId">The id of the submit method to retrieve.</param>
	/// <returns>An ISubmitMethod of the submit method identified by the
	/// argument. If no submit method with that id is referenced by this 
	/// form template then null is returned.</returns>
	public IFormSubmitMethod getSubmitMethod(String submitMethodId)
	{
        for (SubmitMethodData sm : submitMethods)
        {
            if (sm.getSubmitMethodType().equals(submitMethodId))
            {
                return sm;
            }
        }
        return null;
    }

	/// <summary>
	/// gets a list of law ids that apply to this form template.
	/// </summary>
	/// <returns>An IRulesList of String law ids.</returns>
	public IRulesList getLawIDList()
	{
        IRulesList list = RulesObjectBroker.getInstance().createRulesList(null);
        for (LawData law : laws)
        {
            list.add(law.getLawID());
        }
        return list;
	}

	/// <summary>
	/// Retrieve the form template group that contains the current form
	/// template.
	/// </summary>
	/// <returns>An IFormTemplateGroup object that the form template
	/// instance belongs to.</returns>
	public IFormTemplateGroup getFormTemplateGroup()
	{
		return DAOFactory.getDAOFactory().getFormTemplateGroupDAO().getFormTemplateGroup(m_rulesFormTemplateGroupId);
	}

	/// <summary>
	/// Retrieve a list of all active submit methods on the form
	/// template.
	/// </summary>
	/// <returns>A list of String submit method IDs.</returns>
	synchronized public IRulesList getActiveSubmitMethodIDList()
	{
        if (activeSubmitMethodIDs == null) {
            activeSubmitMethodIDs= RulesObjectBroker.getInstance().createRulesList(null);
            for (Object o : submitMethods) {
                ISubmitMethod submitMethod = (ISubmitMethod) o;
                if (!submitMethod.getIsObsolete()) {
                    activeSubmitMethodIDs.add(submitMethod.getSubmitMethodType());
                }
            }
        }
        return activeSubmitMethodIDs;
	}

    public boolean supportsLawID(IRulesList lawIDList)
    {
        for (LawData ptLaw: laws)
        {
            for (int i=0; i<lawIDList.getCount(); i++)
            {
                Integer lawID = (Integer) lawIDList.getItem(i);
                if (ptLaw.getLawID().equals(lawID))
                {
                    return true;
                }
            }
        }
        return false;
    }

}
