/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/PropertyAuditAdapter.java#3 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPropertyAudit;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPServicePropertyAudit;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PropertyAudit;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PropertyAuditAdapter - SAP Adapter for access to property audit history
 *
 * @author Joe Warmelink
 */
public class PropertyAuditAdapter {
    private static final SpcfLogger logger = PayrollServices.getLogger(PropertyAuditAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    private ArrayList<SAPPropertyAudit> getPropertyAudit(
            String pCompanyId,
            String pSourceSystemId,
            String pFieldName,
            Date pFromDate) throws Throwable {
        ArrayList<SAPPropertyAudit> returnVal = new ArrayList<SAPPropertyAudit>();

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemId));

            DomainEntitySet<PropertyAudit> propertyAuditList = PropertyAudit.findPropertyAudits(
                    company,
                    pFieldName,
                    SAPTranslator.getSpcfCalendarFromDate(pFromDate));

            for (PropertyAudit propertyAudit : propertyAuditList) {
                SAPPropertyAudit sapPropertyAudit =
                        PropertyAuditTranslator.getSAPPropertyAuditFromDomainEntity(propertyAudit);
                returnVal.add(sapPropertyAudit);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error loading property audit data.", pSourceSystemId, pCompanyId, "Field", pFieldName, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnVal;
    }

    private ArrayList<SAPPropertyAudit> getPropertyAudits(
            String pCompanyId,
            String pSourceSystemId,
            String pClassName,
            Date pFromDate) throws Throwable {
        ArrayList<SAPPropertyAudit> returnVal = new ArrayList<SAPPropertyAudit>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                                                  SourceSystemCode.valueOf(pSourceSystemId));

            Criterion<PropertyAudit> query = PropertyAudit.Company().equalTo(company).And(PropertyAudit.ClassName().equalTo(pClassName));
            if (pFromDate != null) {
                query = query.And(PropertyAudit.AuditDate().greaterOrEqualThan(SAPTranslator.getSpcfCalendarFromDate(pFromDate)));
            }

            DomainEntitySet<PropertyAudit> propertyAuditList =
                    Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(query)
                                                                                    .OrderBy(PropertyAudit.AuditDate().Descending()));

            for (PropertyAudit propertyAudit : propertyAuditList) {
                SAPPropertyAudit sapPropertyAudit =
                        PropertyAuditTranslator.getSAPPropertyAuditFromDomainEntity(propertyAudit);
                returnVal.add(sapPropertyAudit);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error loading property audit data.", pSourceSystemId, pCompanyId, "ClassName", pClassName, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnVal;
    }

    private ArrayList<SAPServicePropertyAudit> getServicePropertyAudit(
            String pServiceCd,
            String pCompanyId,
            String pSourceSystemId,
            String pFieldName,
            Date pFromDate) throws Throwable {
        ArrayList<SAPServicePropertyAudit> returnVal = new ArrayList<SAPServicePropertyAudit>();

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemId));

            DomainEntitySet<PropertyAudit> propertyAuditList = PropertyAudit.findPropertyAudits(
                    company,
                    pFieldName,
                    SAPTranslator.getSpcfCalendarFromDate(pFromDate));

            for (PropertyAudit propertyAudit : propertyAuditList) {
                returnVal.add(PropertyAuditTranslator.getSAPServicePropertyAuditFromDomainEntity(propertyAudit, pServiceCd));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error loading property audit data.", pSourceSystemId, pCompanyId, "Field", pFieldName, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnVal;
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getNotificationEmailHistory(
            String pCompanyId,
            String pSourceSystemId,
            Date pFromDate) throws Throwable {
        return this.getPropertyAudit(pCompanyId, pSourceSystemId, "NotificationEmailAddress", pFromDate);
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getFundingModelHistory(
            String pCompanyId,
            String pSourceSystemId,
            Date pFromDate) throws Throwable {
        return this.getPropertyAudit(pCompanyId, pSourceSystemId, "FundingModelCd", pFromDate);
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getEmployeeDDLimitHistory(
            String pCompanyId,
            String pSourceSystemId,
            Date pFromDate) throws Throwable {
        return this.getPropertyAudit(pCompanyId, pSourceSystemId, PropertyAudit.FieldNames.EMPLOYEE_LIMIT_AMT, pFromDate);
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getPayeeDDLimitHistory(
            String pCompanyId,
            String pSourceSystemId,
            Date pFromDate) throws Throwable {
        return this.getPropertyAudit(pCompanyId, pSourceSystemId, PropertyAudit.FieldNames.PAYEE_LIMIT_AMT, pFromDate);
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getCompanyDDLimitHistory(
            String pCompanyId,
            String pSourceSystemId,
            Date pFromDate) throws Throwable {
        ArrayList<SAPPropertyAudit> companyDDLimits = new ArrayList<SAPPropertyAudit>();
        companyDDLimits.addAll(this.getServicePropertyAudit(ServiceCode.DirectDeposit.toString(), pCompanyId, pSourceSystemId, PropertyAudit.FieldNames.COMPANY_LIMIT_AMT, pFromDate));
        companyDDLimits.addAll(this.getServicePropertyAudit(ServiceCode.BillPayment.toString(), pCompanyId, pSourceSystemId, PropertyAudit.FieldNames.BP_COMPANY_LIMIT_AMT, pFromDate));
        return companyDDLimits;
    }

    @FlexMethod
    public static ArrayList<SAPPropertyAudit> getCompanyBankAccountPropertyAudit(
            String pSourceSystemId,
            String pCompanyId,
            String pSourceCompanyBankAccountId) throws Throwable {
        ArrayList<SAPPropertyAudit> returnVal = new ArrayList<SAPPropertyAudit>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemId));

            DomainEntitySet<PropertyAudit> propertyAuditList = PropertyAudit.findCompanyBankAccountPropertyAudits(
                    company,
                    pSourceCompanyBankAccountId);

            for (PropertyAudit propertyAudit : propertyAuditList) {
                SAPPropertyAudit sapPropertyAudit =
                        PropertyAuditTranslator.getSAPPropertyAuditFromDomainEntity(propertyAudit);
                returnVal.add(sapPropertyAudit);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error loading property audit data.", pSourceSystemId, pCompanyId, "Field", pSourceCompanyBankAccountId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnVal;
    }

    @FlexMethod
    public List<SAPPropertyAudit> getQuickBooksPropertyAudits(String pSourceSystemId, String pCompanyId) throws Throwable {
        return getPropertyAudits(pCompanyId, pSourceSystemId, "QuickbooksInfo", null);
    }

    @FlexMethod
    public List<SAPPropertyAudit> getW2PrintingPreferenceHistory(String pSourceSystemId, String pCompanyId) throws Throwable {
        return getPropertyAudits(pCompanyId, pSourceSystemId, "TaxCompanyServiceInfo", null);
    }

}