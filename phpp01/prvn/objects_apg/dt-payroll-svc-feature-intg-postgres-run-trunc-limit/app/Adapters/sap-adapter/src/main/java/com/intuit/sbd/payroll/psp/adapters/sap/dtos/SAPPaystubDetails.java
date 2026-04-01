package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * User: ihannur
 * Date: 6/26/13
 * Time: 2:00 PM
 */
public class SAPPaystubDetails {
    private String paystubSeq;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String addressLine5;
    private String checkNumber;
    private String fedFilingStatus;
    private Number fedAllowances;
    private Number fedExtra;
    private Number fedClaimDependent;
    private Number fedDeductions;
    private Number fedOtherIncome;
    private String fedMultipleJobs;
    private String fedW4EmpPref;
    private String stateFilingStatus;
    private Number stateAllowances;
    private Number stateExtra;
    private Date payBeginDate;
    private Date payEndDate;
    private Date paycheckDate;
    private ArrayList<SAPPstubPayItem> nonTaxCompanyItems;
    private ArrayList<SAPPstubPayItem> taxCompanyItems;
    private ArrayList<SAPPstubPayItem> taxableEarnings;
    private ArrayList<SAPPstubPayItem> taxes;
    private ArrayList<SAPPstubPayItem> taxAdjustments;
    private ArrayList<SAPPstubPayItem> preTaxDeductions;
    private ArrayList<SAPPstubPaidTimeOffItem> paidTimeOffs;
    private Number netCurrentAmount;
    private Number netYtdAmount;

    public String getPaystubSeq() {
        return paystubSeq;
    }

    public void setPaystubSeq(String pPaystubSeq) {
        paystubSeq = pPaystubSeq;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String pAddressLine1) {
        addressLine1 = pAddressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String pAddressLine2) {
        addressLine2 = pAddressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String pAddressLine3) {
        addressLine3 = pAddressLine3;
    }

    public String getAddressLine4() {
        return addressLine4;
    }

    public void setAddressLine4(String pAddressLine4) {
        addressLine4 = pAddressLine4;
    }

    public String getAddressLine5() {
        return addressLine5;
    }

    public void setAddressLine5(String pAddressLine5) {
        addressLine5 = pAddressLine5;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String pCheckNumber) {
        checkNumber = pCheckNumber;
    }

    public String getFedFilingStatus() {
        return fedFilingStatus;
    }

    public void setFedFilingStatus(String pFedFilingStatus) {
        fedFilingStatus = pFedFilingStatus;
    }

    public Number getFedAllowances() {
        return fedAllowances;
    }

    public void setFedAllowances(Number pFedAllowances) {
        fedAllowances = pFedAllowances;
    }

    public Number getFedExtra() {
        return fedExtra;
    }

    public void setFedExtra(Number pFedExtra) {
        fedExtra = pFedExtra;
    }

    public Number getFedClaimDependent() {
        return fedClaimDependent;
    }

    public void setFedClaimDependent(Number pfedClaimDependent) {
        this.fedClaimDependent = pfedClaimDependent;
    }

    public Number getFedDeductions() {
        return fedDeductions;
    }

    public void setFedDeductions(Number pfedDeductions) {
        this.fedDeductions = pfedDeductions;
    }

    public Number getFedOtherIncome() {
        return fedOtherIncome;
    }

    public void setFedOtherIncome(Number pfedOtherIncome) {
        this.fedOtherIncome = pfedOtherIncome;
    }

    public String getFedW4EmpPref() {
        return fedW4EmpPref;
    }

    public void setFedW4EmpPref(String pfedW4EmpPref) {
        this.fedW4EmpPref = pfedW4EmpPref;
    }

    public String getFedMultipleJobs() {
        return fedMultipleJobs;
    }

    public void setFedMultipleJobs(String fedMultipleJobs) {
        this.fedMultipleJobs = fedMultipleJobs;
    }

    public String getStateFilingStatus() {
        return stateFilingStatus;
    }

    public void setStateFilingStatus(String pStateFilingStatus) {
        stateFilingStatus = pStateFilingStatus;
    }

    public Number getStateAllowances() {
        return stateAllowances;
    }

    public void setStateAllowances(Number pStateAllowances) {
        stateAllowances = pStateAllowances;
    }

    public Number getStateExtra() {
        return stateExtra;
    }

    public void setStateExtra(Number pStateExtra) {
        stateExtra = pStateExtra;
    }

    public Date getPayBeginDate() {
        return payBeginDate;
    }

    public void setPayBeginDate(Date pPayBeginDate) {
        payBeginDate = pPayBeginDate;
    }

    public Date getPayEndDate() {
        return payEndDate;
    }

    public void setPayEndDate(Date pPayEndDate) {
        payEndDate = pPayEndDate;
    }

    public Date getPaycheckDate() {
        return paycheckDate;
    }

    public void setPaycheckDate(Date pPaycheckDate) {
        paycheckDate = pPaycheckDate;
    }

    public ArrayList<SAPPstubPayItem> getNonTaxCompanyItems() {
        return nonTaxCompanyItems;
    }

    public void setNonTaxCompanyItems(ArrayList<SAPPstubPayItem> pNonTaxCompanyItems) {
        nonTaxCompanyItems = pNonTaxCompanyItems;
    }

    public ArrayList<SAPPstubPayItem> getTaxCompanyItems() {
        return taxCompanyItems;
    }

    public void setTaxCompanyItems(ArrayList<SAPPstubPayItem> pTaxCompanyItems) {
        taxCompanyItems = pTaxCompanyItems;
    }

    public ArrayList<SAPPstubPayItem> getTaxableEarnings() {
        return taxableEarnings;
    }

    public void setTaxableEarnings(ArrayList<SAPPstubPayItem> pTaxableEarnings) {
        taxableEarnings = pTaxableEarnings;
    }

    public ArrayList<SAPPstubPayItem> getTaxes() {
        return taxes;
    }

    public void setTaxes(ArrayList<SAPPstubPayItem> pTaxes) {
        taxes = pTaxes;
    }

    public ArrayList<SAPPstubPaidTimeOffItem> getPaidTimeOffs() {
        return paidTimeOffs;
    }

    public void setPaidTimeOffs(ArrayList<SAPPstubPaidTimeOffItem> pPaidTimeOffs) {
        paidTimeOffs = pPaidTimeOffs;
    }

    public Number getNetCurrentAmount() {
        return netCurrentAmount;
    }

    public void setNetCurrentAmount(Number pNetCurrentAmount) {
        netCurrentAmount = pNetCurrentAmount;
    }

    public Number getNetYtdAmount() {
        return netYtdAmount;
    }

    public void setNetYtdAmount(Number pNetYtdAmount) {
        netYtdAmount = pNetYtdAmount;
    }

    public ArrayList<SAPPstubPayItem> getTaxAdjustments() {
        return taxAdjustments;
    }

    public void setTaxAdjustments(ArrayList<SAPPstubPayItem> pTaxAdjustments) {
        taxAdjustments = pTaxAdjustments;
    }

    public ArrayList<SAPPstubPayItem> getPreTaxDeductions() {
        return preTaxDeductions;
    }

    public void setPreTaxDeductions(ArrayList<SAPPstubPayItem> pPreTaxDeductions) {
        preTaxDeductions = pPreTaxDeductions;
    }

}
