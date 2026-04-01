package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 11/16/11
 * Time: 4:17 PM
 */
public abstract class AbstractPayrollTransaction {
    protected TransactionType mTransactionType = null;
    protected List<TransactionLine> mTransactionLines;

    public abstract boolean processInPSP();

    public abstract String getSourceId();

    public abstract Date getTransactionDate();

    public abstract TransactionType getTransactionType();

    public abstract String getEmployeeId();

    public abstract String getEmployeeName();

    public abstract String getAgencyName();

    public abstract String getReferenceNumber();

    public abstract String getAccountName();

    public abstract SpcfMoney getTotalAmount();

    public abstract String getMemo();

    public abstract SpcfCalendar getPeriodEndDate();

    public abstract boolean getIsVoided();

    public abstract boolean getIsOnService();

    public abstract String getCleared();

    public abstract boolean getSystemModified();

    // compare amounts on liability adjustments
    public abstract boolean equals(PayrollTransactionResponse pPayrollTransactionResponse);

    public abstract List<TransactionLine> getTransactionLines();

    public class TransactionLine {
        private SpcfMoney mAmount;
        private String mPayrollItemId;
        private String mAccountName;
        private String mMemo;
        private boolean mIsDirectDeposit;
        private SpcfMoney mTaxableWages;
        private SpcfMoney mTotalWages;
        private String mTrackingClass;
        private String mLawId;

        public TransactionLine(SpcfMoney pAmount, String pPayrollItemId, String pAccountName, String pMemo, boolean pIsDirectDeposit, SpcfMoney pTaxableWages, SpcfMoney pTotalWages, String pTrackingClass) {
            mAmount = pAmount;
            mPayrollItemId = pPayrollItemId;
            mAccountName = pAccountName;
            mMemo = pMemo;
            mIsDirectDeposit = pIsDirectDeposit;
            mTaxableWages = pTaxableWages;
            mTotalWages = pTotalWages;
            mTrackingClass = pTrackingClass;
        }

        public SpcfMoney getAmount() {
            return mAmount;
        }

        public void setAmount(SpcfMoney pAmount) {
            mAmount = pAmount;
        }

        public String getLawId() {
            return mLawId;
        }

        public void setLawId(String pLawId) {
            mLawId = pLawId;
        }

        public String getPayrollItemId() {
            return mPayrollItemId;
        }

        public String getAccountName() {
            return mAccountName;
        }

        public String getMemo() {
            return mMemo;
        }

        public boolean isDirectDeposit() {
            return mIsDirectDeposit;
        }

        public SpcfMoney getTaxableWages() {
            return mTaxableWages;
        }

        public SpcfMoney getTotalWages() {
            return mTotalWages;
        }

        public String getTrackingClass() {
            return mTrackingClass;
        }
    }

    public enum TransactionType {
        PriorPayment,
        EmployeeLiabilityAdjustment,
        CompanyLiabilityAdjustment,
        LiabilityCheck,
        Refund,
        DirectDepositReturn,
        FundsTransfer
    }
}
