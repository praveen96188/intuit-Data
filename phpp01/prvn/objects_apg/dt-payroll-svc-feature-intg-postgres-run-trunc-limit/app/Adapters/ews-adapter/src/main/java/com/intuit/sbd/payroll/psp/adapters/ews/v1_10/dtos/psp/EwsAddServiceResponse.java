package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "psid",
    "ewsServicesResponse"
})
public class EwsAddServiceResponse extends EwsResponse implements Cloneable  {

    @XmlElement(name = "PSID", required = false)
    protected String psid;

    @XmlElement(name = "ServicesResponse", required = false)
    protected EwsServicesResponse ewsServicesResponse;

    public EwsAddServiceResponse clone() throws CloneNotSupportedException {
        EwsAddServiceResponse clone = (EwsAddServiceResponse) super.clone();

        if (ewsServicesResponse != null) {
            clone.setEwsServicesResponse(ewsServicesResponse.clone());
        }

        return clone;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public EwsServicesResponse getEwsServicesResponse() {
        return ewsServicesResponse;
    }

    public void setEwsServicesResponse(EwsServicesResponse ewsServicesResponse) {
        this.ewsServicesResponse = ewsServicesResponse;
    }
}
