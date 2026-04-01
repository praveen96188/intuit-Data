package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "randomDebit1",
        "randomDebit2"
})
public class EwsValidateBankAccount implements Cloneable {

    @XmlElement(name = "RandomDebit1", required = true)
    protected String randomDebit1;

    @XmlElement(name = "RandomDebit2", required = true)
    protected String randomDebit2;

    public EwsValidateBankAccount clone() throws CloneNotSupportedException {
        return (EwsValidateBankAccount) super.clone();
    }

    public String getRandomDebit1() {
        return randomDebit1;
    }

    public void setRandomDebit1(String randomDebit1) {
        this.randomDebit1 = randomDebit1;
    }

    public String getRandomDebit2() {
        return randomDebit2;
    }

    public void setRandomDebit2(String randomDebit2) {
        this.randomDebit2 = randomDebit2;
    }

    public void validate() throws Exception {
        if (!Validation.validateValue(this.randomDebit1, false, "0{0,1}\\.{0,1}\\d{1,2}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("RandomDebit1", "ValidateBankAccount"));
        }

        if (!Validation.validateValue(this.randomDebit2, false, "0{0,1}\\.{0,1}\\d{1,2}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("RandomDebit2", "ValidateBankAccount"));
        }
    }

}
