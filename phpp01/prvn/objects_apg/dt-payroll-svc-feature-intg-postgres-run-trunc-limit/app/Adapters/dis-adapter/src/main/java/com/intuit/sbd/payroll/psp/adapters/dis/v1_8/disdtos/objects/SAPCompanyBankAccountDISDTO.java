package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Address DIS DTO that will be returned by the WS
 * Most address fields are option when getting or setting an address,
 *    which allows the update address request to only pass in fields
 *    that they want to change.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPCompanyBankAccount", propOrder = {"sourceBankAccountId","bankAccountStatus",
        "accountNumber","routingNumber","bankAccountType","bankName","sourceBankAccountName"})
public class SAPCompanyBankAccountDISDTO {

    @XmlElement(name = "BankAccountId")
    private String sourceBankAccountId;

    public String getSourceBankAccountId() {
        return sourceBankAccountId;
    }

    public void setSourceBankAccountId(String sourceBankAccountId) {
        this.sourceBankAccountId = sourceBankAccountId;
    }

    @XmlElement(name = "BankAccountStatus")
    private BankAccountStatus bankAccountStatus;

    public BankAccountStatus getBankAccountStatus() {
        return bankAccountStatus;
    }

    public void setBankAccountStatus(BankAccountStatus bankAccountStatus) {
        this.bankAccountStatus = bankAccountStatus;
    }

    @XmlElement(name = "AccountNumber")
    private String accountNumber;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @XmlElement(name = "RoutingNumber")
    private String routingNumber;

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    @XmlElement(name = "BankAccountType")
    private BankAccountType bankAccountType;

    public BankAccountType getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(BankAccountType bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    @XmlElement(name = "BankName")
    private String bankName;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @XmlElement(name = "SourceBankAccountName")
    private String sourceBankAccountName;

    public String getSourceBankAccountName() {
        return sourceBankAccountName;
    }

    public void setSourceBankAccountName(String sourceBankAccountName) {
        this.sourceBankAccountName = sourceBankAccountName;
    }



}
