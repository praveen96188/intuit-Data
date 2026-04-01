package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.math.BigDecimal;

/**
 * @author Jeff Jones
 */
public class RSPaycheckLineItem {

    private String description;
    private BigDecimal currentAmount;
    private BigDecimal ytdAmount;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public BigDecimal getYtdAmount() {
        return ytdAmount;
    }

    public void setYtdAmount(BigDecimal ytdAmount) {
        this.ytdAmount = ytdAmount;
    }
}
