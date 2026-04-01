package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlSeeAlso({
    EwsResetPinResponse.class
})

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "psid",
        "privateKey",
        "servicesResponse"
})
public class EwsBasePinResponse extends EwsResponse implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "PrivateKey", required = false)
    protected String privateKey;

    @XmlElement(name = "ServicesResponse", required = false)
    protected EwsPinServicesResponse servicesResponse;

    public EwsBasePinResponse() {
        super();
    }

    public EwsBasePinResponse clone() throws CloneNotSupportedException {
        EwsBasePinResponse clone = (EwsBasePinResponse) super.clone();

        if (servicesResponse != null) {
            clone.setServicesResponse(servicesResponse.clone());
        }

        return clone;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public EwsPinServicesResponse getServicesResponse() {
        return servicesResponse;
    }

    public void setServicesResponse(EwsPinServicesResponse servicesResponse) {
        this.servicesResponse = servicesResponse;
    }

    public void validate() throws Exception {
        super.validate();
    }
}
