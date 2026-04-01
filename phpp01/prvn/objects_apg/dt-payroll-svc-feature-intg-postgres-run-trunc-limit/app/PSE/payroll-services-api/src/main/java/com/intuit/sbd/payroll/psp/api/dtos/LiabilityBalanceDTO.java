package com.intuit.sbd.payroll.psp.api.dtos;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: May 29, 2009
 * Time: 3:56:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class LiabilityBalanceDTO {
    private String lawId;
    private BigDecimal liabilityAmount;
    private BigDecimal paymentAmount;
    private BigDecimal balance;
    
    public String getLawId() {
        return lawId;
    }

    public void setLawId(String lawId) {
        this.lawId = lawId;
    }

    public BigDecimal getLiabilityAmount() {
        return liabilityAmount;
    }

    public void setLiabilityAmount(BigDecimal liabilityAmount) {
        this.liabilityAmount = liabilityAmount;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
