package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "subscriptionNumber",
    "ein",
    "psid",
    "quickBooks"
})
public class EwsValidateSubscription extends EwsRequest implements Cloneable {

    //-------------------
    // lookup input
    //-------------------
    @XmlElement(name = "SubscriptionNumber", required = true)
    private String subscriptionNumber;

    @XmlElement(name = "EIN", required = true)
    private String ein;

    //todo_rhn: do we need to use PSID in search if available?
    //todo_rhn: PSID is not used for lookup or validation and is passed in response; remove?
    @XmlElement(name = "PSID", required = false)
    private String psid;

    //-------------------
    // update input (i.e. PSP will update the existing company info with these values
    //  this is mainly useful to populate info from DIY customers
    //-------------------
    @XmlElement(name = "QuickBooks", required = false)
    private EwsQuickBooks quickBooks;

    public EwsValidateSubscription clone() throws CloneNotSupportedException {
        EwsValidateSubscription clone = (EwsValidateSubscription) super.clone();

        if (quickBooks != null) {
            clone.setQuickBooks(quickBooks.clone());
        }

        return clone;
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public EwsQuickBooks getQuickBooks() {
        return quickBooks;
    }

    public void setQuickBooks(EwsQuickBooks quickBooks) {
        this.quickBooks = quickBooks;
    }

    @Override
    public void validate() throws Exception {
        super.validate();
        //todo_rhn: make common validation for subscriptionNumber, EIN, PSID w/specific methods
        if (!Validation.validateValue(subscriptionNumber, false, "^(\\P{M}\\p{M}*){1,19}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("SubscriptionNumber", "ValidateAccount"));
        }

        if (!Validation.validateValue(ein, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("EIN", "ValidateAccount"));
        }

        if (!Validation.validateValue(psid, true, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "ValidateAccount"));
        }

        if (quickBooks != null)
            quickBooks.validate();
    }
}
