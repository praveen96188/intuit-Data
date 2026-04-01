package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

public class SAPPItem {
    public static final String TAXABLE_ADDITION_TITLE = "Addition (Taxable)";
    public static final String NO_TAX_AFFECT_ADDITION_TITLE = "Addition (No Tax Affect)";
    public static final String COMPENSATION_ITEMS_TITLE = "Compensation";
    public static final String TAXABLE_ER_CONTRIBUTION_TITLE = "ER Contribution (Taxable)";
    public static final String NO_TAX_AFFECT_ER_CONTRIBUTION_TITLE = "ER Contribution (No Tax Affect)";
    public static final String DIRECT_DEPOSIT_TITLE = "Direct Deposit";
    public static final String PRE_TAX_DEDUCTION_TITLE = "Deduction (Pre-tax)";
    public static final String POST_TAX_DEDUCTION_TITLE = "Deduction (No Tax Affect)";

    private String pitemNumber = null;
    private String pitemName = null;
    private String pitemType = null;
    private String pitemDescription = null;
    private String status = null;
    private String taxFormLine = null;
    private String w2Code = null;
    private String taxability = null;
    private String coaExpense = null;
    private String coaLiability = null;
    private String latestId = null;
    private String taxabilityHeader = null;
    private String groupTitle = null;
    private String deleteStatus = null;
    private String token;
    private ArrayList<String> taxableToLawIds = null;

    public String getPitemNumber() {
        return pitemNumber;
    }

    public void setPitemNumber(String pPitemNumber) {
        pitemNumber = pPitemNumber;
    }

    public String getPitemName() {
        return pitemName;
    }

    public void setPitemName(String pPitemName) {
        pitemName = pPitemName;
    }

    public String getPitemType() {
        return pitemType;
    }

    public void setPitemType(String pPitemType) {
        pitemType = pPitemType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }

    public String getTaxFormLine() {
        return taxFormLine;
    }

    public void setTaxFormLine(String pTaxFormLine) {
        taxFormLine = pTaxFormLine;
    }

    public String getW2Code() {
        return w2Code;
    }

    public void setW2Code(String pW2Code) {
        w2Code = pW2Code;
    }

    public ArrayList<String> getTaxableToLawIds() {
        return taxableToLawIds;
    }

    public void setTaxableToLawIds(ArrayList<String> pTaxableToLawIds) {
        taxableToLawIds = pTaxableToLawIds;
    }

    public String getLatestId() {
        return latestId;
    }

    public void setLatestId(String pLatestId) {
        latestId = pLatestId;
    }

    public String getPitemDescription() {
        return pitemDescription;
    }

    public void setPitemDescription(String pPitemDescription) {
        pitemDescription = pPitemDescription;
    }

    public String getTaxability() {
        return taxability;
    }

    public void setTaxability(String pTaxability) {
        taxability = pTaxability;
    }

    public String getCoaExpense() {
        return coaExpense;
    }

    public void setCoaExpense(String pCoaExpense) {
        coaExpense = pCoaExpense;
    }

    public String getCoaLiability() {
        return coaLiability;
    }

    public void setCoaLiability(String pCoaLiability) {
        coaLiability = pCoaLiability;
    }

    public String getTaxabilityHeader() {
        return taxabilityHeader;
    }

    public void setTaxabilityHeader(String pTaxabilityHeader) {
        taxabilityHeader = pTaxabilityHeader;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String pGroupTitle) {
        groupTitle = pGroupTitle;
    }

    public String getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(String pDeleteStatus) {
        deleteStatus = pDeleteStatus;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String pToken) {
        token = pToken;
    }
}
