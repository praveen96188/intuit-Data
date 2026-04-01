package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "ewsBankAccount"
})
public class EwsUpdateBankAssistedService implements Cloneable {

    @XmlElement(name = "BankAccount", required = true)
    protected EwsBankAccount ewsBankAccount;

    public EwsUpdateBankAssistedService clone() throws CloneNotSupportedException {
        EwsUpdateBankAssistedService clone = (EwsUpdateBankAssistedService) super.clone();

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

    public void validate() throws Exception {
        ewsBankAccount.validate();
    }
}
