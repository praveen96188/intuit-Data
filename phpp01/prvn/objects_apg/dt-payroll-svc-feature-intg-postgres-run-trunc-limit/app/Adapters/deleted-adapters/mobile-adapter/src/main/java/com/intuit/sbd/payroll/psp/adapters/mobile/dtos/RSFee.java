package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.math.BigDecimal;

/**
 @author Jeff Jones
 */

public class RSFee {
    private String feeType;
    private BigDecimal amount;
    private RSTransactionSplitStatusCode status;

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RSTransactionSplitStatusCode getStatus() {
        return status;
    }

    public void setStatus(RSTransactionSplitStatusCode status) {
        this.status = status;
    }
}
