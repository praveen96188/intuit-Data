/*
 * $Id:  $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;


import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.aia.BillInfo;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * BillingHistoryTranslator -- this class is used by the SAP adapter to convert AIA entities to SAP DTOs
 *
 * @author Vidhya Krishnamoorthy
 */
public class BillingHistoryTranslator {
    /**
     * Translates the AIA Invoice details to SAPUsageBillingInvoice
     *
     * @param pBillInfo AIA BillInfo object
     * @return SAPUsageBillingInvoice
     */
    public static SAPUsageBillingInvoice getSAPUsageBillingInvoiceFromAIABillInfo(BillInfo pBillInfo) {
        SAPUsageBillingInvoice billingInvoice = new SAPUsageBillingInvoice();
        billingInvoice.setStatementDate(CalendarUtils.convertToDate(pBillInfo.getBillDate()));
        billingInvoice.setBillPOID(pBillInfo.getBillPOID());
        return billingInvoice;
    }

    /**
     * Translates the Employee Usages Domain objects to   SAPUsageBillingEmployeeDetail
     *
     * @param pBillDetail  Domain entity
     * @param pEntitlement THe DB Entitlement object
     * @return SAPUsageBillingEmployeeDetail
     */
    public static SAPUsageBillingEmployeeDetail getSAPUsageBillingEmployeeDetailFromDomainObject(Object[] pBillDetail, Entitlement pEntitlement) {
        SAPUsageBillingEmployeeDetail empDetail = new SAPUsageBillingEmployeeDetail();
        String companyName = (String) pBillDetail[0];
        String employeeName = (String) pBillDetail[1];
        SpcfCalendar paycheckDate = (SpcfCalendar) pBillDetail[2];

        empDetail.setCompanyName(companyName);
        empDetail.setEmployeeName(employeeName);
        if (paycheckDate != null) {
            Date checkDate = CalendarUtils.convertToDate(paycheckDate);
            empDetail.setPaycheckDate(checkDate);
        }
        empDetail.setCheckNumber((String) pBillDetail[3]);
        String sourceCompanyId = (String)pBillDetail[5] ;
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT) ;
        if(company!=null && company.getActivePrimaryEntitlementUnit()!=null) {
            empDetail.setEin(company.getActivePrimaryEntitlementUnit().getFedTaxId());
        }
        return empDetail;
    }

    /**
     * Get SAPUsageBillingDetail DTO from the DB Domain Objects
     *
     * @param pBill        DB Bill entity
     * @param pBillDetails Array of employee usages
     * @param pEntitlement DB entitlements
     * @return SAPUsageBillingDetail
     */
    public static SAPUsageBillingDetail getSAPUsageBillingDetailFromDomainObject(SpcfCalendar pBillDate, List<Object[]> pBillDetails, Entitlement pEntitlement) {
        SAPUsageBillingDetail billingDetail = new SAPUsageBillingDetail();
        ArrayList<SAPUsageBillingEmployeeDetail> empDetails = new ArrayList<SAPUsageBillingEmployeeDetail>();
        DomainEntitySet<EntitlementUnit> euList = pEntitlement.getActiveEntitlementUnitCollection();
        boolean isMultiEin = (euList != null && euList.size() > 1);
        billingDetail.setIsMultiEin(isMultiEin);

        String prevEmployeeName = null;
        String prevCompanyName = null;
        int numEmployees = 0;
        int numCompanies = 0;
        for (Object[] billDetail : pBillDetails) {
            if (billDetail != null) {
                SAPUsageBillingEmployeeDetail empDetail = getSAPUsageBillingEmployeeDetailFromDomainObject(billDetail, pEntitlement);
                empDetails.add(empDetail);
                if (empDetail.getEmployeeName() != null && !empDetail.getEmployeeName().equals(prevEmployeeName)) {
                    numEmployees++;
                }
                if (empDetail.getCompanyName() != null && !empDetail.getCompanyName().equals(prevCompanyName)) {
                    numCompanies++;
                }
                prevEmployeeName = empDetail.getEmployeeName();
                prevCompanyName = empDetail.getCompanyName();
            }
        }
        SpcfCalendar usageStartDateCal = CalendarUtils.getFirstDayOfPrevMonth(pBillDate);
        SpcfCalendar usageEndDateCal = CalendarUtils.getLastDayOfMonth(usageStartDateCal);
        billingDetail.setUsagePeriodStartDate(CalendarUtils.convertToDate(usageStartDateCal));
        billingDetail.setUsagePeriodEndDate(CalendarUtils.convertToDate(usageEndDateCal));
        billingDetail.setEmployeeDetails(empDetails);
        billingDetail.setNumEmployeesBilled(numEmployees);
        billingDetail.setNumCompaniesBilled(numCompanies);
        return billingDetail;
    }
}


