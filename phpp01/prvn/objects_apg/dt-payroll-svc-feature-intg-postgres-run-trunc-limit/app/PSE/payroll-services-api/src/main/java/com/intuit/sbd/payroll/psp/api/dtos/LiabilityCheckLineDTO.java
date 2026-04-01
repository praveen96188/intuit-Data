package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 9, 2010
 * Time: 1:43:14 PM
 */
public class LiabilityCheckLineDTO {
    private SpcfMoney mAmount;
    private QBDTTransactionInfoDTO mQBDTTransactionInfo;
    private String mCompanyPayrollItemId;
    private boolean mIsFeeLine;

    public SpcfMoney getAmount() {
        return mAmount;
    }

    public void setAmount(SpcfMoney pAmount) {
        mAmount = pAmount;
    }

    public QBDTTransactionInfoDTO getQBDTTransactionInfo() {
        return mQBDTTransactionInfo;
    }

    public void setQBDTTransactionInfo(QBDTTransactionInfoDTO pQBDTTransactionInfo) {
        mQBDTTransactionInfo = pQBDTTransactionInfo;
    }

    public String getCompanyPayrollItemId() {
        return mCompanyPayrollItemId;
    }

    public void setCompanyPayrollItemId(String pCompanyPayrollItemId) {
        mCompanyPayrollItemId = pCompanyPayrollItemId;
    }

    public boolean isFeeLine() {
        return mIsFeeLine;
    }

    public void setFeeLine(boolean pFeeLine) {
        mIsFeeLine = pFeeLine;
    }
}
