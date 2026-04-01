package com.intuit.sbd.payroll.psp.api.dtos;

import java.math.BigInteger;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/18/13
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class PstubEmployeeInfoDTO {
    private String mFirstName;
    private String mMiddleName;
    private String mLastName;
    private PstubAddressDTO mAddressDTO;
    private String mSSN;
    private String mFedTaxFilingStatus;
    private Integer mFedTaxFilingStatusCode;
    private String mStateTaxFilingStatus;
    private Integer mStateTaxFilingStatusCode;
    private Integer mFedAllowances;
    private Integer mStateAllowances;
    private String mFedExtra;
    private String mFedClaimDependents;
    private String mFedOtherIncome;
    private String mFedDeduction;
    private String mFedMultipleJobs;
    private String mFedW4EmpPref;
    private String mStateExtra;
    private String mTaxFilingState;
    private BigInteger mCreateTS;
    private BigInteger mModTS;

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String pFirstName) {
        mFirstName = pFirstName;
    }

    public String getMiddleName() {
        return mMiddleName;
    }

    public void setMiddleName(String pMiddleName) {
        mMiddleName = pMiddleName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String pLastName) {
        mLastName = pLastName;
    }

    public PstubAddressDTO getAddressDTO() {
        return mAddressDTO;
    }

    public void setAddressDTO(PstubAddressDTO pAddressDTO) {
        mAddressDTO = pAddressDTO;
    }

    public String getSSN() {
        return mSSN;
    }

    public void setSSN(String pSSN) {
        mSSN = pSSN;
    }

    public String getFedTaxFilingStatus() {
        return mFedTaxFilingStatus;
    }

    public void setFedTaxFilingStatus(String pFedTaxFilingStatus) {
        mFedTaxFilingStatus = pFedTaxFilingStatus;
    }

    public Integer getFedTaxFilingStatusCode() {
        return mFedTaxFilingStatusCode;
    }

    public void setFedTaxFilingStatusCode(Integer pFedTaxFilingStatusCode) {
        mFedTaxFilingStatusCode = pFedTaxFilingStatusCode;
    }

    public String getStateTaxFilingStatus() {
        return mStateTaxFilingStatus;
    }

    public void setStateTaxFilingStatus(String pStateTaxFilingStatus) {
        mStateTaxFilingStatus = pStateTaxFilingStatus;
    }

    public Integer getStateTaxFilingStatusCode() {
        return mStateTaxFilingStatusCode;
    }

    public void setStateTaxFilingStatusCode(Integer pStateTaxFilingStatusCode) {
        mStateTaxFilingStatusCode = pStateTaxFilingStatusCode;
    }

    public Integer getFedAllowances() {
        return mFedAllowances;
    }

    public void setFedAllowances(Integer pFedAllowances) {
        mFedAllowances = pFedAllowances;
    }

    public String getFedClaimDependents() { return mFedClaimDependents; }

    public void setFedClaimDependents(String pFedClaimDependents) { this.mFedClaimDependents = pFedClaimDependents; }

    public String getFedOtherIncome() { return mFedOtherIncome; }

    public void setFedOtherIncome(String pFedOtherIncome) { this.mFedOtherIncome = pFedOtherIncome; }

    public String getFedDeduction() { return mFedDeduction; }

    public void setFedDeduction(String pFedDeduction) { this.mFedDeduction = pFedDeduction; }

    public String getFedMultipleJobs() {
        return mFedMultipleJobs;
    }

    public void setFedMultipleJobs(String pFedMultipleJobs) { this.mFedMultipleJobs = pFedMultipleJobs; }

    public String getFedW4EmpPref() {
        return mFedW4EmpPref;
    }

    public void setFedW4EmpPref(String pFedW4EmpPref) { this.mFedW4EmpPref = pFedW4EmpPref; }

    public Integer getStateAllowances() {
        return mStateAllowances;
    }

    public void setStateAllowances(Integer pStateAllowances) {
        mStateAllowances = pStateAllowances;
    }

    public String getFedExtra() {
        return mFedExtra;
    }

    public void setFedExtra(String pFedExtra) {
        mFedExtra = pFedExtra;
    }

    public String getStateExtra() {
        return mStateExtra;
    }

    public void setStateExtra(String pStateExtra) {
        mStateExtra = pStateExtra;
    }

    public String getTaxFilingState() {
        return mTaxFilingState;
    }

    public void setTaxFilingState(String pTaxFilingState) {
        mTaxFilingState = pTaxFilingState;
    }

    public BigInteger getCreateTS() {
        return mCreateTS;
    }

    public void setCreateTS(BigInteger pCreateTS) {
        mCreateTS = pCreateTS;
    }

    public BigInteger getModTS() {
        return mModTS;
    }

    public void setModTS(BigInteger pModTS) {
        mModTS = pModTS;
    }
}
