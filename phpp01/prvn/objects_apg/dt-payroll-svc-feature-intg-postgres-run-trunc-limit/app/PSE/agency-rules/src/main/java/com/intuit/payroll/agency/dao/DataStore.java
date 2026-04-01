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

import com.intuit.payroll.agency.api.*;
import com.intuit.payroll.agency.impl.*;
import com.intuit.payroll.agency.util.*;
import com.intuit.payroll.agency.dao.generated.*;

import java.util.*;

/**
 * Implmentation notes:
 * Why not use the same DOM/XPath approach as the .NET implementation?
 * The javax.xml.xpath, org.w3c.dom.Document, javax.xml.parsers.DocumentBuilderFactory approach
 * is extremely slow and uses lots of memory. The GC frees >1000K during a test program
 * that lists active payment templates, their allowed deposit frequencies and computes a due date
 * for default values. Increasing the JVM memory helps with speed a bit, but the memory usage is too
 * expensive.
 */
public class DataStore extends DAOFactory implements IDataStore, IAgencyDAO, IEfeRulesDAO, IJurisdictionDAO, IMetaDataDAO,
		IPaymentFrequencyDAO, IPaymentPeriodDAO, IPaymentTemplateDAO, IPaymentReasonDAO, ISubmitMethodDAO,
        IHolidayGroupDAO, ILawTypeDAO,
        IFormTemplateDAO, /*IFormFilingFrequencyDAO, */ /*IFormInfoDAO,*/
        /*IFormSubmitMethodDAO,*/ IFormTemplateGroupDAO, IEnrollmentGroupDAO
{
	// singleton instance
	private static DataStore instance=DataStore.createDataStore();
	private static DataStore userDefined=DataStore.createUserDefined();

	private ArrayList <PaymentTemplateData> paymentTemplates = new ArrayList<PaymentTemplateData>();

	private ArrayList<IAgency> agencies = new ArrayList<IAgency>();

    private ArrayList<IJurisdiction> jurs = new ArrayList<IJurisdiction>();

    private ArrayList<HolidayGroup> holidayGroups = new ArrayList<HolidayGroup>();

    private ArrayList<FormTemplateData> formTemplates = new ArrayList<FormTemplateData>();
    private ArrayList<FormTemplateGroup> formTemplateGroups = new ArrayList<FormTemplateGroup>();

    private ArrayList<EnrollmentGroupData> enrollmentGroups = new ArrayList<EnrollmentGroupData>();

    // AgencyRules.xml/AgencyRules/VersionNumber
	private String version;

	// AgencyRules.xml/AgencyRules/EFERules
	private IEfeRules efeRules;

	// collection of IAgency
	private IRulesList activeAgencies;

	// collection of IJurisdiction
	private IRulesList activeJurs;

	private IRulesList activePaymentTemplateIDs;
    private IRulesList activeFormTemplateIDs;
    private IRulesList activeFormTemplateGroupIDs;
    private IRulesList activeEnrollmentGroupIDs;

    // singleton
    private static DataStore createDataStore()
    {
        DataStore result = new DataStore();
        AgencyRules agencyRulesSource = new AgencyRules();
        agencyRulesSource.addToDataStore(result);
        return result;
    }
    private static DataStore createUserDefined() {
        DataStore result = new DataStore();
		UserDefinedRules userDefinedSource = new UserDefinedRules();
		userDefinedSource.addToDataStore(result);
        return result;
    }

    private DataStore () {
	}



    /**
	 * Singleton access
	 * @return   DataStore
	 */
	synchronized public static DataStore getDataStore () {
		return instance;
	}

	synchronized public static DataStore getUserDefinedDataStore () {
		return userDefined;
	}

    /// not thread safe!
    synchronized public static void qaSwitchDataSource(IDataSource dataSource)
    {
        instance.clear();
        dataSource.addToDataStore(instance);
    }

    private void clear()
    {
        paymentTemplates.clear();
        agencies.clear();
        jurs.clear();
        holidayGroups.clear();
        formTemplates.clear();
        formTemplateGroups.clear();
        enrollmentGroups.clear();
        version = null;
        efeRules = null;
        activeAgencies = null;
        activeJurs = null;
        activePaymentTemplateIDs = null;
        activeFormTemplateIDs = null;
        activeFormTemplateGroupIDs = null;
        activeEnrollmentGroupIDs = null;
    }

    // Implements IDataStore
	synchronized public void setVersion(String version) {
		this.version = version;
	}

	// Implements IDataStore
	synchronized public void setEfeRules(IEfeRules efeRules) {
		this.efeRules = efeRules;
	}

	// Implements IDataStore
	synchronized public void addAgency(IAgency agency) {
		agencies.add (agency);
//		Jurisdiction jur = (Jurisdiction) getJurisdiction(agency.getJurisdictionID());
//		jur.addAgency((Agency) agency);
	}

	// Implements IDataStore
	synchronized public void addPaymentTemplate(IRulesPaymentTemplate pt) {
        paymentTemplates.add ((PaymentTemplateData)pt);
	}

    // Implements IDataStore
    synchronized public void addJurisdiction(IJurisdiction jur) {
        jurs.add (jur);
    }

    // Implements IDataStore
    synchronized public void addHolidayGroup(HolidayGroup holidayGroup) {
        holidayGroups.add (holidayGroup);
    }

    // Implements IDataStore
    synchronized public void addFormTemplateGroup(FormTemplateGroup formTemplateGroup) {
        formTemplateGroups.add (formTemplateGroup);
    }

    // Implements IDataStore
    synchronized public void addFormTemplate(FormTemplateData formTemplate) {
        formTemplates.add (formTemplate);
    }

    // Implements IDataStore
    synchronized public void addEnrollmentGroup(EnrollmentGroupData enrollmentGroup) {
        enrollmentGroups.add (enrollmentGroup);
    }

	// Implmenents IAgencyDAO
	public IAgency getAgency(String id) {
        for (IAgency agency : agencies)
        {
            if (agency.getAgencyID().equals(id))
            {
                return agency;
            }
        }
        return null;
	}

	// Implmenents IAgencyDAO
	synchronized public IRulesList getActiveAgencyIDList() {
		if (activeAgencies == null) {
			activeAgencies = RulesObjectBroker.getInstance().createRulesList(null);
            for (IAgency agency : agencies) {
                if (!agency.getIsObsolete()) {
                    activeAgencies.add(agency.getAgencyID());
                }
            }
        }
		return activeAgencies;
	}

	// Implements IEfeRulesDAO
	public IEfeRules getEfeRules() {
		return efeRules;
	}

	// Implements IJurisdictionDAO
	synchronized public IJurisdiction getJurisdiction(String id) {
        for (IJurisdiction jur : jurs)
        {
            if (jur.getJurisdictionID().equals(id))
            {
                return jur;
            }
        }
        return null;
	}

	// Implements IJurisdictionDAO
	synchronized public IRulesList getActiveJurisdictionIDList() {
		if (activeJurs == null) {
			activeJurs = RulesObjectBroker.getInstance().createRulesList(null);
            for (IJurisdiction jur : jurs) {
                if (!jur.getIsObsolete()) {
                    activeJurs.add(jur.getJurisdictionID());
                }
            }
        }
		return activeJurs;
	}

	// Implements IMetaDataDAO
	public String getVersionNumber() {
		return version;
	}

	// Implements IPaymentFrequencyDAO
	public IRulesList getActivePaymentFrequencyIDList(String paymentTemplateID) {
		IRulesPaymentTemplate pt = getPaymentTemplate (paymentTemplateID);
		return pt.getActivePaymentFrequencyIDList();
	}

	// Implements IPaymentFrequencyDAO
	public IPaymentFrequency getPaymentFrequency(String paymentTemplateID, String paymentFrequencyID) {
		IRulesPaymentTemplate template = getPaymentTemplate (paymentTemplateID);
		return template.getPaymentFrequency(paymentFrequencyID);
	}

    // Implements IPaymentPeriodDAO
    public PaymentPeriod getPaymentPeriod(IPaymentPeriodRequest paymentPeriodRq) {
        FrequencyData freq;
        if (paymentPeriodRq.getRequestType() == PaymentPeriodRequestType.RulesBased
            && paymentPeriodRq.getUserFrequency() == UserFrequencyType.NoSchedule)
        {
            freq = (FrequencyData) getPaymentFrequency(paymentPeriodRq.getPaymentTemplateId(), paymentPeriodRq.getFrequencyId());
        }
        else
        {
			IRulesPaymentTemplate pt = DataStore.userDefined.paymentTemplates.get(0);
            freq = (FrequencyData)pt.getPaymentFrequency(paymentPeriodRq.getFrequencyId());
        }

        if(freq == null) {
            throw new RuntimeException("Error finding payment period for" +
                    " Request Type: " + paymentPeriodRq.getRequestType() +
                    " Payment Template:" + paymentPeriodRq.getPaymentTemplateId() +
                    " User Frequency: " + paymentPeriodRq.getUserFrequency() +
                    " Frequency: " + paymentPeriodRq.getFrequencyId() );
        }

		return freq.getPaymentPeriod(paymentPeriodRq.getAccrualDate());
    }


	// Implements IPaymentTemplateDAO
	public IRulesList getActivePaymentTemplateIDList() {
		if (activePaymentTemplateIDs == null) {
			activePaymentTemplateIDs = RulesObjectBroker.getInstance().createRulesList(null);
            for (IRulesPaymentTemplate pt : paymentTemplates) {
                if (!pt.getIsObsolete()) {
                    activePaymentTemplateIDs.add(pt.getPaymentTemplateID());
                }
            }
        }
		return activePaymentTemplateIDs;
	}
             
	// Implements IPaymentTemplateDAO
	public IRulesList getActivePaymentTemplateIDListFromLawIDs(IRulesList lawIDList) {
        ArrayList<String> idArrayList = new ArrayList<String>();
        for(PaymentTemplateData pt:paymentTemplates)
        {
            if (!pt.getIsObsolete() && pt.supportsLawID(lawIDList))
            {
                idArrayList.add(pt.getPaymentTemplateID());
            }
        }
        return RulesObjectBroker.getInstance().createRulesList(idArrayList);
    }

	// Implements IPaymentTemplateDAO
	public IRulesPaymentTemplate getPaymentTemplate(String id) {
        for (IRulesPaymentTemplate pt : paymentTemplates) {
            if (pt.getPaymentTemplateID().equals(id))
            {
                return pt;
            }
        }
        return null;
	}

	// Implements IPaymentTemplateDAO
	public IRulesList getLawIDList(String paymentTemplateID) {
        IRulesPaymentTemplate pt = getPaymentTemplate(paymentTemplateID);
        return pt.getLawIDList();
	}

	// Implements IPaymentTemplateDAO
    public DueDateRollingPolicy getDueDatePolicy(String paymentTemplateID)
    {
		PaymentTemplateData ptd = (PaymentTemplateData) getPaymentTemplate(paymentTemplateID);
		return ptd.getDueDateRollingPolicy();
	}

	// Implements ISubmitMethodDAO
	public ISubmitMethod getSubmitMethod(String paymentTemplateID, String submitMethodType) {
        return getPaymentTemplate(paymentTemplateID).getSubmitMethod(submitMethodType);
	}

	// Implements ISubmitMethodDAO
	public IRulesList getActiveSubmitMethodIDList(String paymentTemplateID) {
		IRulesPaymentTemplate pt = getPaymentTemplate(paymentTemplateID);
		return pt.getActiveSubmitMethodIDList();
	}

	// Implements ISubmitMethodDAO
	public IRulesList getTaxIDToAllowNegativesList(String paymentTemplateID, String submitMethodType) {
		ISubmitMethod submitMethod = getSubmitMethod(paymentTemplateID, submitMethodType);
		return submitMethod.getTaxIDToAllowNegativesList();
	}

    /// <summary>
    /// gets the holidays for a given Group ID.
    /// </summary>
    /// <param name="holidayGroupID">The ID of the group to get holidays for.</param>
    /// <returns>A list of IAgencyHolidays</returns>
    public Iterable<IAgencyHoliday> getHolidays(String holidayGroupID)
//    public SpcfCollectionIterable<IAgencyHoliday> getHolidays(String holidayGroupID)
    {
        ArrayList<IAgencyHoliday> holidays = new ArrayList<IAgencyHoliday>();
//        SpcfArrayList<IAgencyHoliday> holidays = SpcfFactory.getInstance().createArrayList();
        if (holidayGroupID != null  && holidayGroupID.length()>0)
        {
            getHolidays(holidayGroupID, holidays);
        }
        return holidays;
    }

    private void getHolidays(String holidayGroupID, ArrayList<IAgencyHoliday> holidays)
//    private void getHolidays(String holidayGroupID, SpcfArrayList<IAgencyHoliday> holidays)
    {
        HolidayGroup group = getHolidayGroup(holidayGroupID);
        if(group == null) {
            group = getHolidayGroup("FEDERALRESERVE");
        }

        String extendID = group.getExtendsHolidayGroupID();
        if (extendID != null && extendID.length()>0)
        {
            getHolidays(extendID, holidays);
        }
        for (IAgencyHoliday holiday: group.getHolidays())
        {
            holidays.add(holiday);
        }
    }

    private HolidayGroup getHolidayGroup (String id)
    {
        for(HolidayGroup hg : holidayGroups)
        {
            if (hg.getHolidayGroupID().equals(id))
            {
                return hg;
            }
        }
        return null;
    }

    /// <summary>
	/// implements IPaymentReasonDAO
	/// Method to get a list of payment reason codes of a given payment template / submit method.
	/// </summary>
	/// <param name="paymentTemplateID">Payment template id which the submit method belongs to.</param>
	/// <param name="submitMethodType">Submit method id which the payment reasons belongs to.</param>
	/// <returns>A list of active payment readon codes.</returns>
	public IRulesList getActivePaymentReasonCodeList(String paymentTemplateID, String submitMethodType)
	{
		ISubmitMethod submitMethod = getSubmitMethod(paymentTemplateID, submitMethodType);
		return submitMethod.getActivePaymentReasonCodeList();
	}

	public IPaymentReason getPaymentReason(String paymentTemplateID, String submitMethodID, String paymentReasonCode)
	{
		ISubmitMethod submitMethod = getSubmitMethod(paymentTemplateID, submitMethodID);
		return submitMethod.getPaymentReason(paymentReasonCode);
	}

	public String getLawDescriptionFromId(String lawId)
	{
		for (PaymentTemplateData pt : paymentTemplates)
        {
            String desc = pt.getLawDescriptionFromId(lawId);
            if (desc != null)
            {
                return desc;
            }

        }
        return null;
    }

    public LawData getLawFromId(String lawId) {
        for (PaymentTemplateData pt : paymentTemplates)
        {
            LawData law = pt.getLawFromId(lawId);
            if (law != null)
            {
                return law;
            }

        }
        return null;
    }


    // Extends DAOFactory
	public IAgencyDAO getAgencyDAO() {
		return this;
	}

	// Extends DAOFactory
	public IJurisdictionDAO getJurisdictionDAO() {
		return this;
	}

	// Extends DAOFactory
	public IPaymentFrequencyDAO getPaymentFrequencyDAO() {
		return this;
	}

	// Extends DAOFactory
	public IPaymentTemplateDAO getPaymentTemplateDAO() {
		return this;
	}

	// Extends DAOFactory
	public IPaymentPeriodDAO getPaymentPeriodDAO() {
		return this;
	}

	// Extends DAOFactory
	public ISubmitMethodDAO getSubmitMethodDAO() {
		return this;
	}

	// Extends DAOFactory
	public IPaymentReasonDAO getPaymentReasonDAO() {
		return this;
	}

	// Extends DAOFactory
	public IEnrollmentGroupDAO getEnrollmentGroupDAO() {
		return this;
	}

	// Extends DAOFactory
	public ICredentialDataDAO getCredentialDataDAO() {
		throw new IllegalStateException("NYI");
//		return this;
	}

	// Extends DAOFactory
	public IMetaDataDAO getMetaDataDAO() {
		return this;
	}

	// Extends DAOFactory
	public IEfeRulesDAO getEfeRulesDAO() {
		return this;
	}

    public IHolidayGroupDAO getHolidayGroupDAO()
    {
        return this;
    }

    public IFormTemplateDAO getFormTemplateDAO()
    {
		return this;
    }

    public IFormFilingFrequencyDAO getFormFilingFrequencyDAO()
    {
        throw new RuntimeException("nyi");  // not needed on java
    }
    public ILawTypeDAO getLawTypeDAO()
    {
		return this;
    }
    public IFormInfoDAO getFormInfoDAO()
    {
		throw new RuntimeException ("nyi");
	}
    public IFormSubmitMethodDAO getFormSubmitMethodDAO()
    {
        throw new RuntimeException ("nyi");
    }
    public IFormTemplateGroupDAO getFormTemplateGroupDAO()
    {
		return this;
    }
    public IAgency getAgencyByEnrollmentGroupId(String enrollmentGroupId)
    {
        throw new IllegalStateException("NYI");

    }

    public IRulesFormTemplate getFormTemplateByFormTemplateId(String id)
    {
        for(FormTemplate ft : formTemplates)
        {
            if (ft.getFormTemplateID().equals(id))
            {
                return ft;
            }
        }
        return null;
    }

    public IRulesFormTemplate getFormTemplateByFormId(String id)
    {
        for(FormTemplate ft : formTemplates)
        {
            if (ft.getFormID().equals(id))
            {
                return ft;
            }
        }
        return null;
    }

    public IRulesList getLawIdList(String formTemplateID)
    {
        IRulesFormTemplate ft = getFormTemplateByFormTemplateId(formTemplateID);
        return ft.getLawIDList();
    }

    public IRulesList getActiveFormSubmitMethodIDList(String formTemplateID)
    {
	    IRulesFormTemplate ft = getFormTemplateByFormTemplateId(formTemplateID);
		return ft.getActiveSubmitMethodIDList();
    }

	public IRulesList getActiveFormTemplateIDListFromLawIDs(IRulesList lawIDList) {
        ArrayList<String> idArrayList = new ArrayList<String>();
        for(FormTemplateData ft:formTemplates)
        {
            if (!ft.getIsObsolete() && ft.supportsLawID(lawIDList))
            {
                idArrayList.add(ft.getFormTemplateID());
            }
        }
        return RulesObjectBroker.getInstance().createRulesList(idArrayList);
    }


    // form template dao
    synchronized public IRulesList getActiveFormTemplateIdList()
    {
		if (activeFormTemplateIDs == null) {
			activeFormTemplateIDs = RulesObjectBroker.getInstance().createRulesList(null);
	        for (FormTemplate formTemplate : formTemplates)
	        {
                if (!formTemplate.getIsObsolete())
                {
                    activeFormTemplateIDs.add(formTemplate.getFormTemplateID());
                }
            }
        }
		return activeFormTemplateIDs;
    }

    // form template group dao
    synchronized public IRulesList getActiveFormTemplateGroupIDList()
    {
        if (activeFormTemplateGroupIDs == null)
        {
            activeFormTemplateGroupIDs = RulesObjectBroker.getInstance().createRulesList(null);
            for (FormTemplateGroup formTemplateGroup : formTemplateGroups)
            {
                if (!formTemplateGroup.getIsObsolete())
                {
                    activeFormTemplateGroupIDs.add(formTemplateGroup.getFormTemplateGroupID());
                }
            }
        }
        return activeFormTemplateGroupIDs;
    }

    public IFormTemplateGroup getFormTemplateGroup(String id)
    {
        for (FormTemplateGroup ftg:formTemplateGroups)
        {
            if (ftg.getFormTemplateGroupID().equals(id))
            {
                return ftg;
            }
        }
        return null;
    }

    //IEnrollmentGroupDAO
    synchronized public IRulesList getActiveEnrollmentGroupIDList()
    {
        if (activeEnrollmentGroupIDs == null)
        {
            activeEnrollmentGroupIDs = RulesObjectBroker.getInstance().createRulesList(null);
            for (EnrollmentGroup enrollmentGroup : enrollmentGroups)
            {
                if (!enrollmentGroup.getIsObsolete())
                {
                    activeEnrollmentGroupIDs.add(enrollmentGroup.getEnrollmentGroupID());
                }
            }
        }
        return activeEnrollmentGroupIDs;
    }

    /// <summary>
    /// Retrieve a list of all active enrollment group ids,
    /// given a set of tax law ids.
    /// </summary>
    /// <param name="lawIDList">A list of law ids.</param>
    /// <param name="submitMethodId">The submit method to get enrollment group ids for.</param>
    /// <returns>A list of IEnrollmentGroup objects that describe
    /// how credential information for the taxes defined by the tax ids
    /// submitted.</returns>
    public IRulesList getActiveEnrollmentGroupIDListFromLawIDs(IRulesList lawIDList, String submitMethodId)
	{
// maybe get the active payment templates from the law ids
// then get the enrollment groups from the payment templates
        ArrayList<String> idArrayList = new ArrayList<String>();

        // search the payment templates
        IRulesList ptList = getActivePaymentTemplateIDListFromLawIDs(lawIDList);
        for (int i=0; i<ptList.getCount(); i++)
        {
            String ptID = (String)ptList.getItem(i);
            IEnrollmentGroup eg = getEnrollmentGroupFromPaymentTemplate (ptID, submitMethodId);
            if (eg != null && !eg.getIsObsolete())
            {
                if(!idArrayList.contains(eg.getEnrollmentGroupID()))
                {
                    idArrayList.add(eg.getEnrollmentGroupID());
                }
            }
        }

        // search the form templates
        IRulesList ftList = getActiveFormTemplateIDListFromLawIDs(lawIDList);
        for (int i=0; i<ftList.getCount(); i++)
        {
            String ftID = (String)ftList.getItem(i);
            IEnrollmentGroup eg = getEnrollmentGroupFromFormTemplate (ftID, submitMethodId);
            if (eg != null && !eg.getIsObsolete())
            {
                if(!idArrayList.contains(eg.getEnrollmentGroupID()))
                {
                    idArrayList.add(eg.getEnrollmentGroupID());
                }
            }
        }

        return RulesObjectBroker.getInstance().createRulesList(idArrayList);
	}

    /// <summary>
    /// Retrieve a specific enrollment group using an id.
    /// </summary>
    /// <param name="enrollmentGroupId">Unique id of an enrollment group.</param>
    /// <returns>An IEnrollmentGroup object containing data for that enrollment group.</returns>
    public IEnrollmentGroup getEnrollmentGroup(String enrollmentGroupId)
	{
        for (EnrollmentGroup eg:enrollmentGroups)
        {
            if (eg.getEnrollmentGroupID().equals(enrollmentGroupId))
            {
                return eg;
            }
        }
        return null;
	}

    /// <summary>
    /// Retrieve a specific enrollment group using a payment template/submit method.
    /// </summary>
    /// <param name="paymentTemplateId">Unique id of a payment template.</param>
    /// <param name="submitMethodType">Unique id of a submit method.</param>
    /// <returns>An IEnrollmentGroup object containing data for that enrollment group.</returns>
    public IEnrollmentGroup getEnrollmentGroupFromPaymentTemplate (String paymentTemplateId, String submitMethodType)
	{
		for(PaymentTemplateData ptd : paymentTemplates)
		{
            if (ptd.getPaymentTemplateID().equals(paymentTemplateId))
            {
                ISubmitMethod sm = ptd.getSubmitMethod(submitMethodType);
                if (sm != null && !sm.getIsObsolete())
                {
                    String enrollID = sm.getEnrollmentGroupID();
                    IEnrollmentGroup eg = getEnrollmentGroup(enrollID);
                    if (eg != null)
                    {
                        return eg;
                    }
                }
            }
        }
        return null;
    }

    public IEnrollmentGroup getEnrollmentGroupFromFormTemplate(String formTemplateId, String submitMethodType)
	{
		for(FormTemplateData ftd : formTemplates)
		{
            if (ftd.getFormTemplateID().equals(formTemplateId))
            {
                IFormSubmitMethod sm = ftd.getSubmitMethod(submitMethodType);
                if (sm != null && !sm.getIsObsolete())
                {
                    String enrollID = sm.getEnrollmentGroupID();
                    IEnrollmentGroup eg = getEnrollmentGroup(enrollID);
                    if (eg != null)
                    {
                        return eg;
                    }
                }
            }
        }
        return null;
    }

}
