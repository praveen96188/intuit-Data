package com.intuit.sbd.payroll.psp.api.dtos;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Feb 11, 2010
 * Time: 12:51:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class CheckPrintPaycheckDDINfo {
    private String AccountType;
    private String AccountId;
    private BigDecimal DDAmount;

    public String getAccountType() {
        return AccountType;
    }

    public void setAccountType(String accountType) {
        AccountType = accountType;
    }

    public String getAccountId() {
        return AccountId;
    }

    public void setAccountId(String accountId) {
        AccountId = accountId;
    }

    public BigDecimal getDDAmount() {
        return DDAmount;
    }

    public void setDDAmount(BigDecimal DDAmount) {
        this.DDAmount = DDAmount;
    }
}
