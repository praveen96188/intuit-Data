/*
 * Copyright Statement:
 * CONFIDENTIAL -- Copyright 2000-2003 Intuit Inc. This material contains
 * certain trade secrets and confidential and proprietary information
 * of Intuit Inc. Use, reproduction, disclosure and distribution by
 * any means are prohibited, except pursuant to a written license from
 * Intuit Inc. Use of copyright notice is precautionary and does not
 * imply publication or disclosure.
 */
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IPaymentFrequency;
import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.ISubmitMethod;
import com.intuit.payroll.agency.api.RulesObjectBroker;
import com.intuit.payroll.agency.impl.AgencyFormat;
import com.intuit.payroll.agency.impl.Frequency;
import com.intuit.payroll.agency.impl.PaymentTemplate;
import com.intuit.payroll.agency.impl.SubmitMethod;
import com.intuit.payroll.agency.util.DateRollingPolicy;
import com.intuit.payroll.agency.util.DueDateRollingPolicy;

import java.util.ArrayList;
import java.util.Collection;

public class PaymentTemplateData extends PaymentTemplate {
	// collection of Frequency
    private Collection freqs = new ArrayList();
    private ArrayList<AgencyFormat> aidFormats = new ArrayList<AgencyFormat>();
    private IRulesList activeFreqIDs;

	// collection of Integer (lawIDs)
	private ArrayList<LawData> laws = new ArrayList<LawData>();
	// collection of SubmitMethod
	private Collection submitMethods = new ArrayList();
    private IRulesList activeSubmitMethodIDs;

	public Collection periods = new ArrayList();

	public DateRollingPolicy rollDueDateOnHoliday = DateRollingPolicy.Forward;
	public DateRollingPolicy rollDueDateOnWeekend = DateRollingPolicy.Forward;

	public String rounding;

	public PaymentTemplateData () {}

	/// <summary>
	/// List of tax law ids that apply to this payment.
	/// </summary>
	/// <returns>IListResponse collection of law IDs.</returns>
	public IRulesList getLawIDList() {
        IRulesList list = RulesObjectBroker.getInstance().createRulesList(null);
        for (LawData law : laws)
        {
            list.add(law.getLawID());
        }
        return list;
    }

    public IRulesList getAgencyFormats() {
        IRulesList list = RulesObjectBroker.getInstance().createRulesList(null);
        for (AgencyFormat aidFormat : aidFormats)
        {
            list.add(aidFormat);
        }
        return list;
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


	/// <summary>
	/// List of available payment frequencies of this payment template.
	/// </summary>
	/// <returns>IListResponse collection of payment frequency IDs.</returns>
	synchronized public IRulesList getActivePaymentFrequencyIDList() {
		if (activeFreqIDs == null) {
			activeFreqIDs = RulesObjectBroker.getInstance().createRulesList(null);
            for (Object freq : freqs) {
                IPaymentFrequency f = (IPaymentFrequency) freq;
                if (!f.getIsObsolete()) {
                    activeFreqIDs.add(f.getPaymentFrequencyID());
                }
            }
        }
		return activeFreqIDs;
	}

	/// <summary>
	/// List of available submit methods of this payment template.
	/// </summary>
	/// <returns>IListResponse collection of submit method IDs.</returns>
	synchronized public IRulesList getActiveSubmitMethodIDList() {
        if (activeSubmitMethodIDs == null) {
            activeSubmitMethodIDs= RulesObjectBroker.getInstance().createRulesList(null);
            for (Object o : submitMethods) {
                ISubmitMethod  submitMethod = (ISubmitMethod) o;
                if (!submitMethod.getIsObsolete()) {
                    activeSubmitMethodIDs.add(submitMethod.getSubmitMethodType());
                }
            }
        }
        return activeSubmitMethodIDs;
	}

	/// <summary>
	/// Interface to get payment frequency object, given a frequency ID.
	/// </summary>
	/// <param name="paymentFrequencyID">Payment Frequency ID</param>
	/// <returns>Frequency object. may return null</returns>
	public IPaymentFrequency getPaymentFrequency(String paymentFrequencyID) {
        if (paymentFrequencyID != null)
        {
            for (Object freq : freqs) {
                IPaymentFrequency f = (IPaymentFrequency) freq;
                if (paymentFrequencyID.equals(f.getPaymentFrequencyID())) {
                    return f;
                }
            }
        }
        return null;
	}

	/// <summary>
	/// Interface to get a submit method object, given a submit method type.
	/// </summary>
	/// <param name="submitMethodType">Submit method type.</param>
	/// <returns>SubmitMethod object. may return null</returns>
	public ISubmitMethod getSubmitMethod(String submitMethodType) {
        if (submitMethodType != null)
        {
            for (Object submitMethod : submitMethods) {
                ISubmitMethod s = (ISubmitMethod) submitMethod;
                if (submitMethodType.equals(s.getSubmitMethodType())) {
                    return s;
                }
            }
        }
        return null;
	}

    public DueDateRollingPolicy getDueDateRollingPolicy()
    {
        DueDateRollingPolicy result = new DueDateRollingPolicy();
        result.setHolidayDateRollPolicy(rollDueDateOnHoliday);
        result.setWeekendDateRollPolicy(rollDueDateOnWeekend);
        return result;
    }

	public void setRollDueDateOnHoliday(String that)
	{
		rollDueDateOnHoliday = DateRollingPolicy.createDateRollingPolicy(that);
	}

	public void setRollDueDateOnWeekend(String that)
	{
		rollDueDateOnWeekend = DateRollingPolicy.createDateRollingPolicy(that);
	}



	public void addPaymentFrequency (Frequency freq) {
		freqs.add (freq);
	}
	public void addLaw (LawData law) {
		laws.add (law);
	}
	public void addSubmitMethod (SubmitMethod s) {
		submitMethods.add (s);
	}

    public void addAgencyFormat (AgencyFormat aidFormat) {
        aidFormats.add (aidFormat);
    }

	public String toString () {
		StringBuffer buf = new StringBuffer();
		buf.append("PaymentTemplate: ");
		buf.append(getPaymentTemplateID());
        for (Object freq : freqs) {
            buf.append(freq);
        }
        buf.append ("\r\n");
		return buf.toString();
	}

    public IRulesList getHolidayList()
    {
        throw new RuntimeException ("nyi");
    }

    public String getLawDescriptionFromId(String lawId)
    {
        for (LawData law : laws)
        {
            if (law.getLawID().toString().equals(lawId))
            {
                return law.getDescription();
            }
        }
        return null;
    }

    public LawData getLawFromId(String lawId)
    {
        for (LawData law : laws)
        {
            if (law.getLawID().toString().equals(lawId))
            {
                return law;
            }
        }
        return null;
    }

}

