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
    "mostCurrentTaxYear"
})
public class EwsAssistedService extends EwsBaseService implements Cloneable {

    @XmlElement(name = "BankAccount", required = true)
    protected EwsBankAccount ewsBankAccount;

    @XmlElement(name = "MostCurrentTaxYear", required = false)
    protected String mostCurrentTaxYear;

    public EwsAssistedService() {
        super();
    }

    public EwsAssistedService clone() throws CloneNotSupportedException {
        EwsAssistedService clone = (EwsAssistedService) super.clone();

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

    public String getMostCurrentTaxYear() {
        return mostCurrentTaxYear;
    }

    public void setMostCurrentTaxYear(String mostCurrentTaxYear) {
        this.mostCurrentTaxYear = mostCurrentTaxYear;
    }

    public void validate() throws Exception {
        super.validate();

        ewsBankAccount.validate();

        if (!Validation.validateValue(this.mostCurrentTaxYear, true, "^(\\P{M}\\p{M}*){0,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("MostCurrentTaxYear", "AssistedService"));
        }
    }
}
