package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;
import com.intuit.sbd.payroll.psp.domain.QbdtPayType;
import com.intuit.sbd.payroll.psp.domain.QbdtSpecialType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 9, 2010
 * Time: 11:08:50 AM
 */
public class QBDTPayrollItemInfoDTO {
    private String mListId;
    private boolean mIsEmployeePaid;
    private String mLiabilityAccount;
    private String mLiabilityAgency;
    private String mAgencyId;
    private boolean mAdjustsGross;
    private boolean mBasedOnQuantity;
    private String mExpenseAccount;
    private double mDefaultRate;
    private QbdtNumericType mDefaultRateType;
    private SpcfMoney mDefaultLimit;
    private boolean mExpenseByJob;
    private QbdtPayType mPayType;
    private QbdtSpecialType mSpecialType;
    private boolean mOnService;
    private boolean mIsDeleted;
    private boolean mIsEarningsTable;
    private double mOvertimeMultiplier;
    private long mDetailType;

    public long getDetailType() {
        return mDetailType;
    }

    public void setDetailType(long pDetailType) {
        mDetailType = pDetailType;
    }

    public double getOvertimeMultiplier() {
        return mOvertimeMultiplier;
    }

    public void setOvertimeMultiplier(double pOvertimeMultiplier) {
        mOvertimeMultiplier = pOvertimeMultiplier;
    }

    public String getListId() {
        return mListId;
    }

    public void setListId(String pListId) {
        mListId = pListId;
    }

    public boolean isEmployeePaid() {
        return mIsEmployeePaid;
    }

    public void setIsEmployeePaid(boolean pIsEmployeePaid) {
        mIsEmployeePaid = pIsEmployeePaid;
    }

    public String getLiabilityAccount() {
        return mLiabilityAccount;
    }

    public void setLiabilityAccount(String pLiabilityAccount) {
        mLiabilityAccount = pLiabilityAccount;
    }

    public String getLiabilityAgency() {
        return mLiabilityAgency;
    }

    public void setLiabilityAgency(String pLiabilityAgency) {
        mLiabilityAgency = pLiabilityAgency;
    }

    public String getAgencyId() {
        return mAgencyId;
    }

    public void setAgencyId(String pAgencyId) {
        mAgencyId = pAgencyId;
    }

    public boolean adjustsGross() {
        return mAdjustsGross;
    }

    public void setAdjustsGross(boolean pAdjustsGross) {
        mAdjustsGross = pAdjustsGross;
    }

    public boolean isBasedOnQuantity() {
        return mBasedOnQuantity;
    }

    public void setBasedOnQuantity(boolean pBasedOnQuantity) {
        mBasedOnQuantity = pBasedOnQuantity;
    }

    public String getExpenseAccount() {
        return mExpenseAccount;
    }

    public void setExpenseAccount(String pExpenseAccount) {
        mExpenseAccount = pExpenseAccount;
    }

    public double getDefaultRate() {
        return mDefaultRate;
    }

    public void setDefaultRate(double pDefaultRate) {
        mDefaultRate = pDefaultRate;
    }

    public SpcfMoney getDefaultLimit() {
        return mDefaultLimit;
    }

    public QbdtNumericType getDefaultRateType() {
        return mDefaultRateType;
    }

    public void setDefaultRateType(QbdtNumericType pDefaultRateType) {
        mDefaultRateType = pDefaultRateType;
    }

    public void setDefaultLimit(SpcfMoney pDefaultLimit) {
        mDefaultLimit = pDefaultLimit;
    }

    public boolean expenseByJob() {
        return mExpenseByJob;
    }

    public void setExpenseByJob(boolean pExpenseByJob) {
        mExpenseByJob = pExpenseByJob;
    }

    public QbdtPayType getPayType() {
        return mPayType;
    }

    public void setPayType(QbdtPayType pPayType) {
        mPayType = pPayType;
    }

    public QbdtSpecialType getSpecialType() {
        return mSpecialType;
    }

    public void setSpecialType(QbdtSpecialType pSpecialType) {
        mSpecialType = pSpecialType;
    }

    public boolean isOnService() {
        return mOnService;
    }

    public void setOnService(boolean pOnService) {
        mOnService = pOnService;
    }

    public boolean isDeleted() {
        return mIsDeleted;
    }

    public void setIsDeleted(boolean pIsDeleted) {
        mIsDeleted = pIsDeleted;
    }

    public boolean isEarningsTable() {
        return mIsEarningsTable;
    }

    public void setIsEarningsTable(boolean pIsEarningsTable) {
        mIsEarningsTable = pIsEarningsTable;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();


        /*if (mAgencyId == null ||
                !(Validator.isValidLength(mAgencyId, 1, 80))) {
            validationResult.getMessages().InvalidValue(EntityName.CompanyAgency, mAgencyId, "AgencyId");
        }*/


        return validationResult;
    }

}
