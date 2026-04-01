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
    "psid",
    "pin",
    "ewsBaseServices",
    "forceRandomDollar"
})
public class EwsAddService extends EwsRequest implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "Pin", required = false)
    protected String pin;

    @XmlElement(name = "Services", required = true)
    protected EwsBaseServices ewsBaseServices;

    @XmlElement(name = "ForceRandomDollar", required = true)
    protected Boolean forceRandomDollar;

    public EwsAddService clone() throws CloneNotSupportedException {
        EwsAddService clone = (EwsAddService) super.clone();

        if (ewsBaseServices != null) {
            clone.setEwsBaseServices(ewsBaseServices.clone());
        }

        return clone;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public EwsBaseServices getEwsBaseServices() {
        return ewsBaseServices;
    }

    public void setEwsBaseServices(EwsBaseServices ewsBaseServices) {
        this.ewsBaseServices = ewsBaseServices;
    }

    public Boolean getForceRandomDollar() {
        return forceRandomDollar;
    }

    public void setForceRandomDollar(Boolean forceRandomDollar) {
        this.forceRandomDollar = forceRandomDollar;
    }

    public void validate() throws Exception {
        if (!Validation.validateValue(this.psid, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "Company"));
        }

        if (this.pin != null) {
            if (!Validation.validateValue(this.getPin(), false, "^(\\P{M}\\p{M}*){8,12}$")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("Pin", "BasePin"));
            }
        }

        ewsBaseServices.validate();
    }
}
