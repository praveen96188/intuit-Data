package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: wnichols
 * Date: Jun 13, 2008
 * Time: 1:09:41 PM
 */
public class RebillFeeTransactionDTO {
    private String feeDebitTransactionId;
    private SpcfMoney overrideAmount;
    private Integer overrideQuantity;

    public RebillFeeTransactionDTO(String pFeeDebitTransactionId, SpcfMoney pOverrideAmount) {
        this.feeDebitTransactionId = pFeeDebitTransactionId;
        this.overrideAmount = pOverrideAmount;
    }

    public RebillFeeTransactionDTO(String pFeeDebitTransactionId, SpcfMoney pOverrideAmount, Integer pOverrideQuantity) {
        feeDebitTransactionId = pFeeDebitTransactionId;
        overrideAmount = pOverrideAmount;
        overrideQuantity = pOverrideQuantity;
    }

    public String getFeeDebitTransactionId() {
        return feeDebitTransactionId;
    }

    public void setFeeDebitTransactionId(String feeDebitTransactionId) {
        this.feeDebitTransactionId = feeDebitTransactionId;
    }

    public SpcfMoney getOverrideAmount() {
        return overrideAmount;
    }

    public void setOverrideAmount(SpcfMoney overrideAmount) {
        this.overrideAmount = overrideAmount;
    }

    public Integer getOverrideQuantity() {
        return overrideQuantity;
    }

    public void setOverrideQuantity(Integer pOverrideQuantity) {
        overrideQuantity = pOverrideQuantity;
    }
}
