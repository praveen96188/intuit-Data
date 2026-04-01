package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "QBBankAccount")
public class QBBankAccount {

    private String accountNumber;
    private QBBankAccountTypeEnum accountType;
    private String bankName;
    private String routingNumber;
    private String sourceBankAccountId;

    @XmlElement(name = "AccountNumber", required = true, nillable = false)
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String pAccountNumber) {
        accountNumber = pAccountNumber;
    }

    @XmlElement(name = "AccountType", required = true, nillable = false)
    public QBBankAccountTypeEnum getAccountType() {
        return accountType;
    }

    public void setAccountType(QBBankAccountTypeEnum pAccountType) {
        accountType = pAccountType;
    }

    @XmlElement(name = "BankName", required = true, nillable = false)
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String pBankName) {
        bankName = pBankName;
    }

    @XmlElement(name = "RoutingNumber", required = true, nillable = false)
    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String pRoutingNumber) {
        routingNumber = pRoutingNumber;
    }

    @XmlElement(name = "SourceBankAccountId", required = true, nillable = false)
    public String getSourceBankAccountId() {
        return sourceBankAccountId;
    }

    public void setSourceBankAccountId(String pSourceBankAccountId) {
        sourceBankAccountId = pSourceBankAccountId;
    }


}
