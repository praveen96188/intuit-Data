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

import com.intuit.payroll.agency.api.IAgency;
import com.intuit.payroll.agency.api.IJurisdiction;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.impl.IEfeRules;
import com.intuit.payroll.agency.impl.FormTemplateGroup;
import com.intuit.payroll.agency.impl.FormTemplate;

public interface IDataStore {
	void setVersion (String version);
	void setEfeRules (IEfeRules efeRules);
	void addAgency (IAgency agency);
	void addPaymentTemplate (IRulesPaymentTemplate pt);
    void addJurisdiction (IJurisdiction jur);
    void addHolidayGroup(HolidayGroup holidayGroup);
    void addFormTemplateGroup(FormTemplateGroup formTemplateGroup);
    void addFormTemplate(FormTemplateData formTemplate);
    void addEnrollmentGroup(EnrollmentGroupData enrollmentGroup);

}
