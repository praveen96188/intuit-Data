package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "psid",
        "ewsValidateBankServices"
})
public class EwsValidateBank extends EwsRequest implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "Services", required = true)
    protected EwsValidateBankServices ewsValidateBankServices;

    public EwsValidateBank() {
        super();
    }

    public EwsValidateBank clone() throws CloneNotSupportedException {
        EwsValidateBank clone = (EwsValidateBank) super.clone();

        if (ewsValidateBankServices != null) {
            clone.setEwsValidateBankServices(ewsValidateBankServices.clone());
        }

        return clone;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public EwsValidateBankServices getEwsValidateBankServices() {
        return ewsValidateBankServices;
    }

    public void setEwsValidateBankServices(EwsValidateBankServices ewsValidateBankServices) {
        this.ewsValidateBankServices = ewsValidateBankServices;
    }

    public void validate() throws Exception {
        super.validate();

        ewsValidateBankServices.validate();
    }
}
