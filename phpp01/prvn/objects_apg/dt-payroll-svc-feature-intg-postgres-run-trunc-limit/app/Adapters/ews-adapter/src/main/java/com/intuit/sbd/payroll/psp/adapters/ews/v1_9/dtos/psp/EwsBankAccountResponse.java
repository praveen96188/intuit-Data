package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsBankAccountType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

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
        "holdReason",
        "pendingPayrollExists",
        "randomDebitDateTime"
})
public class EwsBankAccountResponse extends EwsBaseBankAccountResponse implements Cloneable {

    @XmlElement(name = "BankName", required = false)
    protected String bankName;

    @XmlElement(name = "AccountNumber", required = false)
    protected String accountNumber;

    @XmlElement(name = "RoutingNumber", required = false)
    protected String routingNumber;

    @XmlElement(name = "QuickBooksName", required = false)
    protected String quickBooksName;

    @XmlElement(name = "AccountType", required = false)
    protected EwsBankAccountType accountType;

    @XmlElement(name = "HoldReason", required = false)
    protected String holdReason;

    @XmlElement(name = "PendingPayrollExists", required = false)
    protected boolean pendingPayrollExists;

    @XmlElement(name = "RandomDebitDateTime", required = false)
    protected Calendar randomDebitDateTime;

    public EwsBankAccountResponse() {
        super();
    }

    public EwsBankAccountResponse clone() throws CloneNotSupportedException {
        return (EwsBankAccountResponse) super.clone();
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

    public String getHoldReason() {
        return holdReason;
    }

    public void setHoldReason(String holdReason) {
        this.holdReason = holdReason;
    }

    public boolean getPendingPayrollExists() {
        return pendingPayrollExists;
    }

    public void setPendingPayrollExists(boolean pendingPayrollExists) {
        this.pendingPayrollExists = pendingPayrollExists;
    }

    public Calendar getRandomDebitDateTime() {
        return randomDebitDateTime;
    }

    public void setRandomDebitDateTime(Calendar randomDebitDateTime) {
        this.randomDebitDateTime = randomDebitDateTime;
    }

    public void validate() throws Exception {
        super.validate();
    }
}
