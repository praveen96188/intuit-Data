package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: srikanthm180
 * Date: 3/6/13
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "psid",
        "ewsServicesResponse"
})
public class EwsDeactivateServiceResponse extends EwsResponse implements Cloneable {

    @XmlElement(name = "PSID", required = false)
    protected String psid;

    @XmlElement(name = "ServicesResponse", required = false)
    protected EwsServicesResponse ewsServicesResponse;

    public EwsDeactivateServiceResponse clone() throws CloneNotSupportedException {
        EwsDeactivateServiceResponse clone = (EwsDeactivateServiceResponse) super.clone();

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
