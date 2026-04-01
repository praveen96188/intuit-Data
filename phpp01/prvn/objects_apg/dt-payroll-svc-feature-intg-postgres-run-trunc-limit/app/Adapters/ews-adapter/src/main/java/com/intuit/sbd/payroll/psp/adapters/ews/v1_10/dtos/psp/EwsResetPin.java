package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "psid",
        "pinSignature",
        "userName"
})
public class EwsResetPin extends EwsRequest implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;    

    @XmlElement(name = "PinSignature", required = true)
    protected String pinSignature;

    @XmlElement(name = "UserName", required = true)
    protected String userName;

    public EwsResetPin() {
        super();
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }                                 

    public EwsResetPin clone() throws CloneNotSupportedException {
        return (EwsResetPin) super.clone();
    }

    public String getPinSignature() {
        return pinSignature;
    }

    public void setPinSignature(String pinSignature) {
        this.pinSignature = pinSignature;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void validate() throws Exception {
        super.validate();
    }
}
