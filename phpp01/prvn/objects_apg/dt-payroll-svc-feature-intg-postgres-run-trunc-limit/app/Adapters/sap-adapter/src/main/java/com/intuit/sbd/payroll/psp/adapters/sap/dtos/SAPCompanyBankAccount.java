package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 27, 2008
 * Time: 2:40:16 PM
 */
public class SAPCompanyBankAccount {
    private String accountId;
    private String accountNumber;
    private String routingNumber;
    private String bankName;
    private BankAccountType accountType;
    private BankAccountStatus bankAccountStatusCd;
    private long verifyRetryCount;
    private String sourceBankAccountName;
    private String sourceBankAccountId;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public BankAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(BankAccountType accountType) {
        this.accountType = accountType;
    }

    public BankAccountStatus getBankAccountStatusCd() {
        return bankAccountStatusCd;
    }

    public void setBankAccountStatusCd(BankAccountStatus bankAccountStatusCd) {
        this.bankAccountStatusCd = bankAccountStatusCd;
    }

    public long getVerifyRetryCount() {
        return verifyRetryCount;
    }

    public void setVerifyRetryCount(long verifyRetryCount) {
        this.verifyRetryCount = verifyRetryCount;
    }

    public String getSourceBankAccountName() {
        return sourceBankAccountName;
    }

    public void setSourceBankAccountName(String sourceBankAccountName) {
        this.sourceBankAccountName = sourceBankAccountName;
    }

    public String getSourceBankAccountId() {
        return sourceBankAccountId;
    }

    public void setSourceBankAccountId(String sourceBankAccountId) {
        this.sourceBankAccountId = sourceBankAccountId;
    }
}
