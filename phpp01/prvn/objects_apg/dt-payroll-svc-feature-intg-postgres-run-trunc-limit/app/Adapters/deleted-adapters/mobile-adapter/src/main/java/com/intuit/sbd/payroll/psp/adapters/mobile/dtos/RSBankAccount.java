package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.math.BigDecimal;

/**
 @author Jeff Jones
 */
public class RSBankAccount {

    private String id;
    private String bankName;
    private String accountNumber;
    private String routingNumber;
    private RSBankAccountTypeCode type;
    private RSBankAccountStatusCode status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public RSBankAccountTypeCode getType() {
        return type;
    }

    public void setType(RSBankAccountTypeCode type) {
        this.type = type;
    }

    public RSBankAccountStatusCode getStatus() {
        return status;
    }

    public void setStatus(RSBankAccountStatusCode status) {
        this.status = status;
    }
}
