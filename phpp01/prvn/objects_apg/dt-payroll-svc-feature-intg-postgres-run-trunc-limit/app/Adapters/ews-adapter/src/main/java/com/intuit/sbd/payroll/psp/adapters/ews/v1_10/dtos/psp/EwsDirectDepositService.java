package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "ewsBankAccount",
    "offerCode"
})
public class EwsDirectDepositService extends EwsBaseService implements Cloneable {

    @XmlElement(name = "BankAccount", required = true)
    protected EwsBankAccount ewsBankAccount;

    @XmlElement(name = "OfferCode", required = false)
    protected String offerCode;

    public EwsDirectDepositService() {
        super();
    }

    public EwsDirectDepositService clone() throws CloneNotSupportedException {
        EwsDirectDepositService clone = (EwsDirectDepositService) super.clone();

        if (ewsBankAccount != null) {
            clone.setEwsBankAccount(ewsBankAccount.clone());
        }

        return clone;
    }

    public EwsBankAccount getEwsBankAccount() {
        return ewsBankAccount;
    }

    public void setEwsBankAccount(EwsBankAccount ewsBankAccount) {
        this.ewsBankAccount = ewsBankAccount;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public void validate() throws Exception {
        super.validate();

        ewsBankAccount.validate();

        if (!Validation.validateValue(this.offerCode, true, "^(\\P{M}\\p{M}*){0,50}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("OfferCode", "DirectDepositService"));
        }
    }    
}
