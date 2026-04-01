package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 11/10/11
 * Time: 8:12 AM
 */
public class QBDTPayrollTransactionLineDTO {
    private SpcfMoney mAmount;
    private SpcfMoney mWageBaseAmount;
    private SpcfMoney mTaxableWageAmount;
    private String payrollItemId;
    private QBDTTransactionInfoDTO mQBDTTransactionInfoDTO;

    public SpcfMoney getAmount() {
        return mAmount;
    }

    public void setAmount(SpcfMoney pAmount) {
        mAmount = pAmount;
    }

    public SpcfMoney getWageBaseAmount() {
        return mWageBaseAmount;
    }

    public void setWageBaseAmount(SpcfMoney pWageBaseAmount) {
        mWageBaseAmount = pWageBaseAmount;
    }

    public SpcfMoney getTaxableWageAmount() {
        return mTaxableWageAmount;
    }

    public void setTaxableWageAmount(SpcfMoney pTaxableWageAmount) {
        mTaxableWageAmount = pTaxableWageAmount;
    }

    public String getPayrollItemId() {
        return payrollItemId;
    }

    public void setPayrollItemId(String pPayrollItemId) {
        payrollItemId = pPayrollItemId;
    }

    public QBDTTransactionInfoDTO getQBDTTransactionInfoDTO() {
        return mQBDTTransactionInfoDTO;
    }

    public void setQBDTTransactionInfoDTO(QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        mQBDTTransactionInfoDTO = pQBDTTransactionInfoDTO;
    }
}
