package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "oldPin"
})
public class EwsUpdatePin extends EwsBasePin implements Cloneable {

    @XmlElement(name = "OldPin", required = true)
    protected String oldPin;

    public EwsUpdatePin() {
        super();
    }

    public EwsUpdatePin clone() throws CloneNotSupportedException {
        return (EwsUpdatePin) super.clone();
    }

    public String getOldPin() {
        return oldPin;
    }

    public void setOldPin(String oldPin) {
        this.oldPin = oldPin;
    }

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.pin, false, "^(\\P{M}\\p{M}*){8,12}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("Pin", "BasePin"));
        }

        if (!Validation.validateValue(this.pin, false, "^(\\P{M}\\p{M}*){4,50}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("OldPin", "UpdatePin"));
        }
    }
}
