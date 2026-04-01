//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.dao.PaymentTemplateData;
import com.intuit.payroll.agency.calculator.DueDateCalculator;
import com.intuit.payroll.agency.util.IAgencyHoliday;
import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.api.*;

/// <summary>
/// Implementation for the PaymentTemplate interface. 
/// </summary>
public abstract class PaymentTemplate implements IRulesPaymentTemplate {
	private String m_paymentTemplateID;
	private boolean m_isObsolete = false;
	private boolean m_isValid = true;
	private String m_description;
	private String m_longDescription;
	private String m_agencyID;
	private String m_defaultPaymentFrequencyID;
	private String m_defaultSubmitMethodID;
	private String m_holidayGroupID;
	private RoundingType m_wageRounding = RoundingType.NoRounding;
	private RoundingType m_paymentAmountRounding = RoundingType.NoRounding;
	private ReconciliationFrequencyType m_reconciliationFrequency = ReconciliationFrequencyType.Quarterly;
	private boolean m_paymentMaySpanReconciliationPeriods = false;
    private String m_paymentTemplateAbbrev;
	private String m_usesFrequencyOf;
    

    public static PaymentTemplate createPaymentTemplate()
	{
		return new PaymentTemplateData();
	}

	protected PaymentTemplate() {}

    /// <summary>
	/// Payment template id (e.g. "IRS941").
	/// </summary>
	public String getPaymentTemplateID()
	{
		 return m_paymentTemplateID; 
	}
	public void setPaymentTemplateID(String that)
	{
		m_paymentTemplateID = that;
	}

	/// <summary>
	/// True if this PaymentTemplate is obsolete.
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

    static public boolean valueAsBoolean(String that) {
		if ("1".equals(that)) {
			return true;
		}
		else 
		{
        	return that != null && !"0".equals(that) && Boolean.parseBoolean(that);
		}
    }

	/// <summary>
	/// Read-only property that indicates if this object is valid or not.
	/// If IsValid is true, it means that this object contains valid data.
	/// If IsValid is false, it means that this object was not initialized.
	/// </summary>
	public boolean getIsValid()
	{
		 return m_isValid; 
	}
	public void setIsValid(boolean that)
	{
		m_isValid = that;
	}

	/// <summary>
	/// Payment description String for UI (e.g. "941 Payment").
	/// </summary>
	public String getDescription()
	{
		 return m_description; 
	}
	public void setDescription(String that)
	{
		m_description = that;
	}

	/// <summary>
	/// Long payment description String for UI (e.g. "941 Payment is blah blah blah").
	/// </summary>
	public String getLongDescription()
	{
		 return m_longDescription; 
	}
	public void setLongDescription(String that)
	{
		m_longDescription = that;
	}

	/// <summary>
	/// ID of the agency that owns this payment template.
	/// </summary>
	public String getAgencyID()
	{
		 return m_agencyID; 
	}
	public void setAgencyID(String that)
	{
		m_agencyID = that;
	}

	/// <summary>
	/// ID of the default payment frequency.
	/// </summary>
	public String getDefaultPaymentFrequencyID()
	{
		 return m_defaultPaymentFrequencyID; 
	}
	public void setDefaultPaymentFrequencyID(String that)
	{
		m_defaultPaymentFrequencyID = that;
	}

	/// <summary>
	/// ID of the default submit method.
	/// </summary>
	public String getDefaultSubmitMethodID()
	{
		 return m_defaultSubmitMethodID; 
	}
	public void setDefaultSubmitMethodID(String that)
	{
		m_defaultSubmitMethodID = that;
	}

	/// <summary>
	/// Id of the Holiday Group this template uses.
	/// </summary>
	public String getHolidayGroupID()
	{
		 return m_holidayGroupID; 
	}
	public void setHolidayGroupID(String that)
	{
		m_holidayGroupID = that;
	}

	public RoundingType getWageRounding()
	{
		return m_wageRounding;
	}
	public void setWageRounding(String that)
	{
		m_wageRounding = RoundingType.createRoundingType(that);
	}

	public RoundingType getPaymentAmountRounding()
	{
		return m_paymentAmountRounding;
	}
	public void setPaymentAmountRounding(String that)
	{
		m_paymentAmountRounding = RoundingType.createRoundingType(that);
	}

	public ReconciliationFrequencyType getReconciliationFrequency()
	{			    
		return m_reconciliationFrequency;
	}
	public void setReconciliationFrequency(String that)
	{			    
		m_reconciliationFrequency = ReconciliationFrequencyType.createReconciliationFrequencyType(that);
	}

	public boolean getPaymentMaySpanReconciliationPeriods()
	{
		return m_paymentMaySpanReconciliationPeriods;
	}
	public void setPaymentMaySpanReconciliationPeriods(String that)
	{
		m_paymentMaySpanReconciliationPeriods = PaymentTemplate.valueAsBoolean(that);
	}



	/// Given a frequency id, submit method id, and
	/// the date of the accrual, return an IPaymentPeriod 
	/// that applies to the accrual with a due date 
	/// adjusted for holidays and weekends.
	/// 
	/// Pattern: Front Controller
	/// </summary>
	/// <param name="paymentPeriodRq">
	/// The payment period request object, which wraps the criteria for
	/// the construction of rules-based and user-defined payments.
	/// </param>
	/// <returns>An IPaymentPeriod with the start/end dates of the accrual 
	/// period and the due date (adjusted for weekends and holidays).</returns>
	public IPaymentPeriod getPaymentPeriod( IPaymentPeriodRequest paymentPeriodRq )
	{
		if(paymentPeriodRq == null)
		{
//			throw new ArgumentNullException("PaymentPeriodRequest argument cannot be null.", "PaymentPeriodRequest");
			throw new RuntimeException("PaymentPeriodRequest argument cannot be null.");
//			throw new SpcfRuntimeException("PaymentPeriodRequest argument cannot be null.");
		}
		// TODO: This is hacky, Peter, let's change the tests to have a template ID.
		paymentPeriodRq.setPaymentTemplateId(m_paymentTemplateID);
		return DueDateCalculator.getInstance().getPaymentPeriod(paymentPeriodRq);
	}

    /// <summary>
    /// Calculate the due date for the supplied payment period request.
    /// </summary>
    /// <param name="paymentPeriodRq">Payment period request to calculate the due date for</param>
    /// <returns>RulesCalendar The due date the agency requires the payment to be received by.
    /// It is not modified by any EFE lead times or processing times..</returns>
    public RulesCalendar getPaymentDueDate ( IPaymentPeriodRequest paymentPeriodRq )
	{
		if(paymentPeriodRq == null)
		{
			throw new RuntimeException("PaymentPeriodRequest argument cannot be null.");
		}
		paymentPeriodRq.setPaymentTemplateId(m_paymentTemplateID);
		return DueDateCalculator.getInstance().getPaymentDueDate(paymentPeriodRq);
	}

	/// <summary>
	/// List of tax law ids that apply to this payment.
	/// </summary>
	/// <returns>IListResponse collection of law IDs.</returns>
	public abstract IRulesList getLawIDList() ;
//		return DAOFactory.getDAOFactory().getPaymentTemplateDAO().getLawIDList(m_paymentTemplateID);

	public abstract boolean supportsLawID(IRulesList lawIDList);

	
	/// <summary>
	/// List of available submit methods of this payment template.
	/// </summary>
	/// <returns>IListResponse collection of submit method IDs.</returns>
	public abstract IRulesList getActiveSubmitMethodIDList(); 
//	{
//		return DAOFactory.getDAOFactory().getSubmitMethodDAO().getActiveSubmitMethodIDList(m_paymentTemplateID);
//	}
	
	/// <summary>
	/// List of available payment frequencies of this payment template.
	/// </summary>
	/// <returns>IListResponse collection of payment frequency IDs.</returns>
	public abstract IRulesList getActivePaymentFrequencyIDList(); 
//	{
//		return DAOFactory.getDAOFactory().getPaymentFrequencyDAO().getActivePaymentFrequencyIDList(m_paymentTemplateID);
//	}
	
	/// <summary>
	/// Interface to get payment frequency object, given a frequency ID.
	/// </summary>
	/// <param name="paymentFrequencyID">Payment Frequency ID</param>
	/// <returns>Frequency object. It always returns a non null object, client code should check for IsValid property of the returned object before using it.</returns>
	public abstract IPaymentFrequency getPaymentFrequency(String paymentFrequencyID); 
//	{
//		return DAOFactory.getDAOFactory().getPaymentFrequencyDAO().getPaymentFrequency(m_paymentTemplateID, paymentFrequencyID);
//	}
	
	/// <summary>
	/// Interface to get a submit method object, given a submit method type.
	/// </summary>
	/// <param name="submitMethodType">Submit method type.</param>
	/// <returns>SubmitMethod object. It always returns a non null object, client code should check for IsValid property of the returned object before using it.</returns>
	public abstract ISubmitMethod getSubmitMethod(String submitMethodType);
//	{
//		return DAOFactory.getDAOFactory().getSubmitMethodDAO().getSubmitMethod(m_paymentTemplateID, submitMethodType);
//	}

	/// <summary>
	/// Returns a list of holidays for this template.
	/// </summary>
	/// <returns>A List of IAgencyHolidays</returns>
	public IRulesList getHolidayList()
	{
		Iterable<IAgencyHoliday> holidays = DAOFactory.getDAOFactory().getHolidayGroupDAO().getHolidays(getHolidayGroupID());
//		SpcfCollectionIterable<IAgencyHoliday> holidays = DAOFactory.getDAOFactory().getHolidayGroupDAO().getHolidays(getHolidayGroupID());
		RulesList results = new RulesList();
        for (IAgencyHoliday holiday: holidays)
		{
			results.add(holiday);
		}
		return results;
	}

    public void setPaymentTemplateAbbrev(String m_paymentTemplateAbbrev) {
        this.m_paymentTemplateAbbrev = m_paymentTemplateAbbrev;
    }

    public String getPaymentTemplateAbbrev() {
        return m_paymentTemplateAbbrev;
    }
	
	public void setUsesFrequencyOf(String m_usesFrequencyOf) {
        this.m_usesFrequencyOf = m_usesFrequencyOf;
    }

    public String getUsesFrequencyOf() {
        return m_usesFrequencyOf;
    }
	
}
