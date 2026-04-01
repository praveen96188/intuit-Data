package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 @author Jeff Jones
 */

public class RSTransactionSplit {

    private BigDecimal amount;
    private RSBankAccount bankAccount;
    private RSTransactionSplitStatusCode status;
    private ArrayList<String> alertIdList;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RSBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(RSBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public RSTransactionSplitStatusCode getStatus() {
        return status;
    }

    public void setStatus(RSTransactionSplitStatusCode status) {
        this.status = status;
    }

    public ArrayList<String> getAlertIdList() {
        if (alertIdList == null) {
            alertIdList = new ArrayList<String>();
        }

        return alertIdList;
    }
}
