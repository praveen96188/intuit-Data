package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "QBPayee")
public class QBPayee {

    private QBAddress address;
    private String emailAddress;
    private String name;
    private String payeeSourceId;
    private String phoneNumber;
    private String taxId;
    private Boolean is1099;
    private String accountNumber;

    @XmlElement(name = "Address")
    public QBAddress getAddress() {
        return address;
    }

    public void setAddress(QBAddress pAddress) {
        address = pAddress;
    }

    @XmlElement(name = "EmailAddress")
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String pEmailAddress) {
        emailAddress = pEmailAddress;
    }

    @XmlElement(name = "Name", required = true, nillable = false)
    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    @XmlElement(name = "PayeeSourceId", required = true, nillable = false)
    public String getPayeeSourceId() {
        return payeeSourceId;
    }

    public void setPayeeSourceId(String pPayeeSourceId) {
        payeeSourceId = pPayeeSourceId;
    }

    @XmlElement(name = "PhoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String pPhoneNumber) {
        phoneNumber = pPhoneNumber;
    }

    @XmlElement(name = "TaxId")
    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String pTaxId) {
        taxId = pTaxId;
    }

    @XmlElement(name = "Is1099")
    public Boolean getIs1099() {
        return is1099;
    }

    public void setIs1099(Boolean is1099) {
        this.is1099 = is1099;
    }

    @XmlElement(name = "AccountNumber")
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
