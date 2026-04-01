package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsBankAccountType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "bankName",
        "accountNumber",
        "routingNumber",
        "quickBooksName",
        "accountType",
        "createRandomDebits"
})
public class EwsBankAccount implements Cloneable {

    @XmlElement(name = "BankName", required = true)
    protected String bankName;

    @XmlElement(name = "AccountNumber", required = true)
    protected String accountNumber;

    @XmlElement(name = "RoutingNumber", required = true)
    protected String routingNumber;

    @XmlElement(name = "QuickBooksName", required = true)
    protected String quickBooksName;

    @XmlElement(name = "AccountType", required = true)
    protected EwsBankAccountType accountType;

    @XmlElement(name = "CreateRandomDebits", required = true)
    protected boolean createRandomDebits;

    public EwsBankAccount clone() throws CloneNotSupportedException {
        return (EwsBankAccount) super.clone();
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

    public String getQuickBooksName() {
        return quickBooksName;
    }

    public void setQuickBooksName(String quickBooksName) {
        this.quickBooksName = quickBooksName;
    }

    public EwsBankAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(EwsBankAccountType accountType) {
        this.accountType = accountType;
    }

    public boolean isCreateRandomDebits() {
        return createRandomDebits;
    }

    public void setCreateRandomDebits(boolean createRandomDebits) {
        this.createRandomDebits = createRandomDebits;
    }

    public void validate() throws Exception {
        if (!Validation.validateValue(this.bankName, false, "^(\\P{M}\\p{M}*){1,255}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("BankName", "BankAccount"));
        }

        if (!Validation.validateValue(this.accountNumber, false, "^(\\P{M}\\p{M}*){1,17}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AccountNumber", "BankAccount"));
        }

        if (!Validation.validateValue(this.routingNumber, false, "^(\\P{M}\\p{M}*){9,9}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("RoutingNumber", "BankAccount"));
        }

        if (!Validation.validateValue(this.quickBooksName, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("QuickBooksName", "BankAccount"));
        }

        if (this.accountType == null) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AccountType", "BankAccount"));
        }
    }
}
