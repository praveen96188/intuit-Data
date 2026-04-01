package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.PIIMask;
import com.intuit.sbd.payroll.psp.gateways.iam.IamUser;
import com.intuit.sbd.payroll.psp.util.VMPEmployeePaginationDetails;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

/**
 * User: ihannur
 * Date: 6/28/13
 * Time: 4:27 PM
 */
public class ViewMyPaycheckTranslator {

    public static SAPEmployeeSearchResult getSAPEmployeeSearchResult(Employee employee, boolean canViewSSN) {
        SAPEmployeeSearchResult searchResult = new SAPEmployeeSearchResult();
        searchResult.setCompanyKey(new SAPCompanyKey(employee.getCompany().getSourceSystemCd().toString(), employee.getCompany().getSourceCompanyId()));
        searchResult.setEmployeeName(employee.getFullName());
        searchResult.setEmployeeId(employee.getId().toString());
        searchResult.setCompanyName(employee.getCompany().getLegalName());
        searchResult.setEmployeeSSN(PIIMask.maskText(employee.getTaxId(), !canViewSSN));
        searchResult.setEmployeeEmail(employee.getEmail());
        return searchResult;
    }

    public static SAPVMPEmployeeInfo getSAPEmployeeInfoFromDomainEntity(Employee pEmployee, IamUser pIamUser, boolean canViewSSN) {
        SAPVMPEmployeeInfo sapEmployee = new SAPVMPEmployeeInfo();
        sapEmployee.setEmployeeSeq(pEmployee.getId().toString());
        sapEmployee.setFirstName(pEmployee.getFirstName());
        sapEmployee.setLastName(pEmployee.getLastName());
        sapEmployee.setMiddleName(pEmployee.getMiddleName());
        sapEmployee.setSocialSecurityNumber(PIIMask.maskText(pEmployee.getTaxId(), !canViewSSN));
        sapEmployee.setConsumerId(pEmployee.getConsumerRealmId());

        if (pIamUser != null) {
            sapEmployee.setEmailAddress(pIamUser.getEmailAddress());
            sapEmployee.setUserId(pIamUser.getLoginName());
        }

        return sapEmployee;
    }

    public static SAPPaystub getSAPPaystubFromEntity(String pEmployeeId, Paystub pPaystub) {
        SAPPaystub sapPaystub = new SAPPaystub();
        sapPaystub.setEmployeeSeq(pEmployeeId);
        sapPaystub.setPaystubSeq(pPaystub.getId().toString());
        sapPaystub.setPaycheckDate(SAPTranslator.getDateFromSpcfCalendar(pPaystub.getPaycheckDate()));
        return sapPaystub;
    }

    public static SAPPaystubDetails getSAPPaystubDetailsFromEntity(Paystub pPaystub) {
        SAPPaystubDetails sapPaystubDetails = new SAPPaystubDetails();
        sapPaystubDetails.setPaystubSeq(pPaystub.getId().toString());
        sapPaystubDetails.setPaycheckDate(SAPTranslator.getDateFromSpcfCalendar(pPaystub.getPaycheckDate()));
        sapPaystubDetails.setPayBeginDate(SAPTranslator.getDateFromSpcfCalendar(pPaystub.getPayBeginDate()));
        sapPaystubDetails.setPayEndDate(SAPTranslator.getDateFromSpcfCalendar(pPaystub.getPayEndDate()));
        sapPaystubDetails.setCheckNumber(pPaystub.getCheckNumber());
        sapPaystubDetails.setNetCurrentAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pPaystub.getNetPay()));
        sapPaystubDetails.setNetYtdAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pPaystub.getYTDNetPay()));
        if (pPaystub.getPstubEmployeeInfo() != null) {
            sapPaystubDetails.setFedFilingStatus(pPaystub.getPstubEmployeeInfo().getFedTaxFilingStatus());
            sapPaystubDetails.setFedAllowances(pPaystub.getPstubEmployeeInfo().getFedAllowances());
            sapPaystubDetails.setFedExtra(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pPaystub.getPstubEmployeeInfo().getFedExtra()));
            sapPaystubDetails.setStateFilingStatus(pPaystub.getPstubEmployeeInfo().getStateTaxFilingStatus());
            sapPaystubDetails.setStateAllowances(pPaystub.getPstubEmployeeInfo().getStateAllowances());
            sapPaystubDetails.setStateExtra(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pPaystub.getPstubEmployeeInfo().getStateExtra()));
            sapPaystubDetails.setFedClaimDependent(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pPaystub.getPstubEmployeeInfo().getFedClaimDependents()));
            sapPaystubDetails.setFedDeductions(SAPTranslator.getDoubleFromSpcfMoneyNullZero((pPaystub.getPstubEmployeeInfo().getFedDeductions())));
            sapPaystubDetails.setFedOtherIncome(SAPTranslator.getDoubleFromSpcfMoneyNullZero((pPaystub.getPstubEmployeeInfo().getFedOtherIncome())));
            sapPaystubDetails.setFedMultipleJobs(pPaystub.getPstubEmployeeInfo().getFedMultipleJobs());
            sapPaystubDetails.setFedW4EmpPref(pPaystub.getPstubEmployeeInfo().getFedW4EmployeePref());
            if (pPaystub.getPstubEmployeeInfo().getPstubAddress() != null) {
                sapPaystubDetails.setAddressLine1(StringUtils.trim(pPaystub.getPstubEmployeeInfo().getPstubAddress().getLine1()));
                sapPaystubDetails.setAddressLine2(StringUtils.trim(pPaystub.getPstubEmployeeInfo().getPstubAddress().getLine2()));
                sapPaystubDetails.setAddressLine3(StringUtils.trim(pPaystub.getPstubEmployeeInfo().getPstubAddress().getLine3()));
                sapPaystubDetails.setAddressLine4(StringUtils.trim(pPaystub.getPstubEmployeeInfo().getPstubAddress().getLine4()));
                sapPaystubDetails.setAddressLine5(StringUtils.trim(pPaystub.getPstubEmployeeInfo().getPstubAddress().getLine5()));
            }
        }
        ArrayList<SAPPstubPaidTimeOffItem> paidTimeOffItems = new ArrayList<SAPPstubPaidTimeOffItem>();
        ArrayList<SAPPstubPayItem> nonTaxableCompanyItems = new ArrayList<SAPPstubPayItem>();
        ArrayList<SAPPstubPayItem> taxableCompanyItems = new ArrayList<SAPPstubPayItem>();
        ArrayList<SAPPstubPayItem> earningItems = new ArrayList<SAPPstubPayItem>();
        ArrayList<SAPPstubPayItem> taxItems = new ArrayList<SAPPstubPayItem>();
        ArrayList<SAPPstubPayItem> preTaxDeductions = new ArrayList<SAPPstubPayItem>();
        ArrayList<SAPPstubPayItem> taxAdjustments = new ArrayList<SAPPstubPayItem>();

        sapPaystubDetails.setPaidTimeOffs(paidTimeOffItems);
        sapPaystubDetails.setNonTaxCompanyItems(nonTaxableCompanyItems);
        sapPaystubDetails.setTaxCompanyItems(taxableCompanyItems);
        sapPaystubDetails.setTaxableEarnings(earningItems);
        sapPaystubDetails.setTaxes(taxItems);
        sapPaystubDetails.setPreTaxDeductions(preTaxDeductions);
        sapPaystubDetails.setTaxAdjustments(taxAdjustments);
        if (pPaystub.getPstubPaidTimeoffItemCollection().isNotEmpty()) {
            for (PstubPaidTimeoffItem pstubPaidTimeoffItem : pPaystub.getPstubPaidTimeoffItemCollection()) {
                SAPPstubPaidTimeOffItem paidTimeOffItem = new SAPPstubPaidTimeOffItem();
                paidTimeOffItem.setName(pstubPaidTimeoffItem.getName());
                paidTimeOffItem.setAvailable(pstubPaidTimeoffItem.getAvailable());
                paidTimeOffItem.setYtdUsed(pstubPaidTimeoffItem.getYTDUsed());
                paidTimeOffItems.add(paidTimeOffItem);
            }
        }
        updatePstubPayItems(pPaystub, PstubItemType.NonTaxCompContri, nonTaxableCompanyItems);
        updatePstubPayItems(pPaystub, PstubItemType.TaxCompContri, taxableCompanyItems);
        updatePstubPayItems(pPaystub, PstubItemType.Earnings, earningItems);
        updatePstubPayItems(pPaystub, PstubItemType.Tax, taxItems);
        updatePstubPayItems(pPaystub, PstubItemType.AdjNetPay, taxAdjustments);
        updatePstubPayItems(pPaystub, PstubItemType.PreTaxDeduct, preTaxDeductions);
        return sapPaystubDetails;
    }

    public static void updatePstubPayItems(Paystub pPaystub, PstubItemType pItemType, ArrayList<SAPPstubPayItem> pSAPPayItems) {
        DomainEntitySet<PstubPayItem> pstubPayItems = pPaystub.getPstubPayItemCollection().find(PstubPayItem.Type().equalTo(pItemType));

        for (PstubPayItem pstubPayItem : pstubPayItems) {
            SAPPstubPayItem payItem = new SAPPstubPayItem();
            payItem.setName(pstubPayItem.getName());
            payItem.setCurrentAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pstubPayItem.getCurAmt()));
            payItem.setYtdAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pstubPayItem.getYTD()));
            payItem.setRate(pstubPayItem.getRate());
            if (pstubPayItem.getQtyTime() != null) {
                payItem.setQuantity(pstubPayItem.getQtyTime());
            } else {
                payItem.setQuantity(pstubPayItem.getQtyAmt());
            }
            pSAPPayItems.add(payItem);
        }
    }

    public static VMPEmployeePaginationDetails translateSAPObjectForDomain(SAPVMPEmployeePaginationDetails sapvmpEmployeePaginationDetails)
    {
        return new VMPEmployeePaginationDetails(sapvmpEmployeePaginationDetails.getCurrentPage(), sapvmpEmployeePaginationDetails.getPageSize(), sapvmpEmployeePaginationDetails.getSortBy(), sapvmpEmployeePaginationDetails.isSortDesc());
    }
}
