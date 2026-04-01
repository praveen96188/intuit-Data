package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.BankAccountType;

import java.util.Date;

/**
 * User: cyoder
 * Date: Jun 17, 2008
 * Time: 9:36:16 AM
 */
public class SAPEmployeeBankAccountHistoryItem {
    private Date changeDate;
    private String changedBy;

    private String accountNumber;
    private String routingNumber;
    private BankAccountType accountTypeCd;

    private String oldAccountNumber;
    private String oldRoutingNumber;
    private BankAccountType oldAccountTypeCd;

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
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

    public BankAccountType getAccountTypeCd() {
        return accountTypeCd;
    }

    public void setAccountTypeCd(BankAccountType accountTypeCd) {
        this.accountTypeCd = accountTypeCd;
    }

    public String getOldAccountNumber() {
        return oldAccountNumber;
    }

    public void setOldAccountNumber(String oldAccountNumber) {
        this.oldAccountNumber = oldAccountNumber;
    }

    public String getOldRoutingNumber() {
        return oldRoutingNumber;
    }

    public void setOldRoutingNumber(String oldRoutingNumber) {
        this.oldRoutingNumber = oldRoutingNumber;
    }

    public BankAccountType getOldAccountTypeCd() {
        return oldAccountTypeCd;
    }

    public void setOldAccountTypeCd(BankAccountType oldAccountTypeCd) {
        this.oldAccountTypeCd = oldAccountTypeCd;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
}
