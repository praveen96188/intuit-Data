package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 @author Jeff Jones
 */

public class RSCompany {

    private String psid;
    private String ein;
    private String legalName;
    private String dba;
    private String subscriptionNumber;
    private String serviceKey;
    private String extensionKey;
    private String nextChargeDate;
    private String billingEmail;
    private String billingContact;
    private String billingZip;
    private String subscriptionEndDate;
    private String creditCard;
    private String creditCardType;
    private String companyEmail;
    private RSBankAccount bankAccount;
    private List<RSAddress> addresses;
    private List<RSContact> contacts;
    private List<RSService> services;

    public RSCompany() {
        services = new ArrayList<RSService>();
        addresses = new ArrayList<RSAddress>();
        contacts = new ArrayList<RSContact>();


    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public List<RSService> getServices() {
        if (services == null){
            services = new ArrayList<RSService>();
        }
        return services;
    }

    public void setServices(List<RSService> services) {
        this.services = services;
    }

    public List<RSContact> getContacts() {
        if (contacts == null) {
            contacts = new ArrayList<RSContact>();
        }
        return contacts;
    }

    public void setContacts(List<RSContact> contacts) {
        this.contacts = contacts;
    }

    public List<RSAddress> getAddresses() {
        if (addresses == null) {
            addresses = new ArrayList<RSAddress>();
        }
        return addresses;
    }

    public void setAddresses(List<RSAddress> addresses) {
        this.addresses = addresses;
    }

    public String getDba() {
        return dba;
    }

    public void setDba(String dba) {
        this.dba = dba;
    }

    public RSBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(RSBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String email) {
        this.companyEmail = email;
    }

    public String getExtensionKey() {
        return extensionKey;
    }

    public void setExtensionKey(String extensionKey) {
        this.extensionKey = extensionKey;
    }

    public String getNextChargeDate() {
        return nextChargeDate;
    }

    public void setNextChargeDate(String nextChargeDate) {
        this.nextChargeDate = nextChargeDate;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public String getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(String creditCardType) {
        this.creditCardType = creditCardType;
    }

    public String getBillingContact() {
        return billingContact;
    }

    public void setBillingContact(String billingContact) {
        this.billingContact = billingContact;
    }

    public String getBillingZip() {
        return billingZip;
    }

    public void setBillingZip(String billingZip) {
        this.billingZip = billingZip;
    }

    public String getSubscriptionEndDate() {
        return subscriptionEndDate;
    }

    public void setSubscriptionEndDate(String subscriptionEndDate) {
        this.subscriptionEndDate = subscriptionEndDate;
    }
}
