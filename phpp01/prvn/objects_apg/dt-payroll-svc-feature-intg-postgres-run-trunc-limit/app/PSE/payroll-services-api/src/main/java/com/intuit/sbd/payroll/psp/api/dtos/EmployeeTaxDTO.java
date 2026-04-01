package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.EmployeeTaxType;
import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 8, 2010
 * Time: 12:41:08 PM
 */
public class EmployeeTaxDTO {
    private String mState;
    private EmployeeTaxType mTaxType;
    private boolean mSubjectTo;
    private String mTaxLawVersion;
    private String mW2Name;
    private String mCompanyLawId;
    private String mFilingStatus;
    private int mAllowances;
    private double mExtraWithholding;
    private QbdtNumericType mExtraWithholdingType;
    private Map<Integer, String> mTaxTableMiscData;
    private int mOrder = -1;

    public String getState() {
        return mState;
    }

    public void setState(String pState) {
        mState = pState;
    }

    public EmployeeTaxType getTaxType() {
        return mTaxType;
    }

    public void setTaxType(EmployeeTaxType pTaxType) {
        mTaxType = pTaxType;
    }

    public boolean isSubjectTo() {
        return mSubjectTo;
    }

    public void setSubjectTo(boolean pSubjectTo) {
        mSubjectTo = pSubjectTo;
    }

    public String getTaxLawVersion() {
        return mTaxLawVersion;
    }

    public void setTaxLawVersion(String pTaxLawVersion) {
        mTaxLawVersion = pTaxLawVersion;
    }

    public String getW2Name() {
        return mW2Name;
    }

    public void setW2Name(String pW2Name) {
        mW2Name = pW2Name;
    }

    public String getCompanyLawId() {
        return mCompanyLawId;
    }

    public void setCompanyLawId(String pCompanyLawId) {
        mCompanyLawId = pCompanyLawId;
    }

    public String getFilingStatus() {
        return mFilingStatus;
    }

    public void setFilingStatus(String pFilingStatus) {
        mFilingStatus = pFilingStatus;
    }

    public int getAllowances() {
        return mAllowances;
    }

    public void setAllowances(int pAllowances) {
        mAllowances = pAllowances;
    }

    public double getExtraWithholding() {
        return mExtraWithholding;
    }

    public void setExtraWithholding(double pExtraWithholding) {
        mExtraWithholding = pExtraWithholding;
    }

    public QbdtNumericType getExtraWithholdingType() {
        return mExtraWithholdingType;
    }

    public void setExtraWithholdingType(QbdtNumericType pExtraWithholdingType) {
        mExtraWithholdingType = pExtraWithholdingType;
    }

    public Map<Integer, String> getTaxTableMiscData() {
        if(mTaxTableMiscData == null) {
            mTaxTableMiscData = new HashMap<Integer, String>();
        }
        return mTaxTableMiscData;
    }

    public void setTaxTableMiscData(Map<Integer, String> pTaxTableMiscData) {
        mTaxTableMiscData = pTaxTableMiscData;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int pOrder) {
        mOrder = pOrder;
    }
}
