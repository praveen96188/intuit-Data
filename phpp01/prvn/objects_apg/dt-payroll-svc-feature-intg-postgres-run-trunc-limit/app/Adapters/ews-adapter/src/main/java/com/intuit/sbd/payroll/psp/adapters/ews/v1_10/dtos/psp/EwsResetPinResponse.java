package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "pin"
})
public class EwsResetPinResponse extends EwsBasePinResponse implements Cloneable {

    @XmlElement(name = "Pin", required = true)
    protected String pin;

    public EwsResetPinResponse() {
        super();
    }

    public EwsResetPinResponse clone() throws CloneNotSupportedException {
        return (EwsResetPinResponse) super.clone();
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void validate() throws Exception {
        super.validate();
    }
}
