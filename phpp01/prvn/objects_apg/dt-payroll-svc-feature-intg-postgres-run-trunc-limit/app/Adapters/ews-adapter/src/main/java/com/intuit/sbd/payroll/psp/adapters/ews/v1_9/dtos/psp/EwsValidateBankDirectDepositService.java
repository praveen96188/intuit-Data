package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "ewsValidateBankAccount"
})
public class EwsValidateBankDirectDepositService implements Cloneable {

    @XmlElement(name = "BankAccount", required = true)
    protected EwsValidateBankAccount ewsValidateBankAccount;

    public EwsValidateBankDirectDepositService clone() throws CloneNotSupportedException {
        EwsValidateBankDirectDepositService clone = (EwsValidateBankDirectDepositService) super.clone();

        if (ewsValidateBankAccount != null) {
            clone.setEwsValidateBankAccount(ewsValidateBankAccount.clone());
        }

        return clone;
    }

    public EwsValidateBankAccount getEwsValidateBankAccount() {
        return ewsValidateBankAccount;
    }

    public void setEwsValidateBankAccount(EwsValidateBankAccount ewsValidateBankAccount) {
        this.ewsValidateBankAccount = ewsValidateBankAccount;
    }

    public void validate() throws Exception {
        ewsValidateBankAccount.validate();
    }

}
