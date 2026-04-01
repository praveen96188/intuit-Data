package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "psid",
    "ewsBankServicesResponse"
})
public class EwsBankResponse extends EwsResponse implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "Services", required = true)
    protected EwsBankServicesResponse ewsBankServicesResponse;

    public EwsBankResponse() {
        super();
    }

    public EwsBankResponse clone() throws CloneNotSupportedException {
        EwsBankResponse clone = (EwsBankResponse) super.clone();

        if (ewsBankServicesResponse != null) {
            clone.setEwsBankServicesResponse(ewsBankServicesResponse.clone());
        }

        return clone;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public EwsBankServicesResponse getEwsBankServicesResponse() {
        return ewsBankServicesResponse;
    }

    public void setEwsBankServicesResponse(EwsBankServicesResponse ewsBankServicesResponse) {
        this.ewsBankServicesResponse = ewsBankServicesResponse;
    }

    public void validate() throws Exception {
        super.validate();
    }
}
