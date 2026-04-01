package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

/**
 * @author Jeff Jones
 */

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "psid",
        "ewsEntitlementUnitResponses"
})
public class EwsMigrateEntitlementResponse extends EwsResponse implements Cloneable {

    @XmlElement(name = "PSID", required = false)
    protected String psid;

    @XmlElement(name = "EntitlementUnitResponses", required = false)
    protected ArrayList<EwsEntitlementUnitResponse> ewsEntitlementUnitResponses;

    public EwsMigrateEntitlementResponse() {
        super();
    }

    public EwsMigrateEntitlementResponse clone() throws CloneNotSupportedException {
        return (EwsMigrateEntitlementResponse) super.clone();
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public ArrayList<EwsEntitlementUnitResponse> getEwsEntitlementUnitResponses() {
        if (ewsEntitlementUnitResponses == null)
            ewsEntitlementUnitResponses = new ArrayList<EwsEntitlementUnitResponse>();

        return ewsEntitlementUnitResponses;
    }

    public void setEwsEntitlementUnitResponses(ArrayList<EwsEntitlementUnitResponse> ewsEntitlementUnitResponses) {
        this.ewsEntitlementUnitResponses = ewsEntitlementUnitResponses;
    }

    public void validate() throws Exception {
        super.validate();
    }
}
