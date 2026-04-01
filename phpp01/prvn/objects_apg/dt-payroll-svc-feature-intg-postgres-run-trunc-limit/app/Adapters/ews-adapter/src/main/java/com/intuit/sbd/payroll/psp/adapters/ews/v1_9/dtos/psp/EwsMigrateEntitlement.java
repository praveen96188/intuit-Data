package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "psid",
        "ewsEntitlements"
})

public class EwsMigrateEntitlement extends EwsRequest implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "Entitlement", required = true)
    protected ArrayList<EwsEntitlement> ewsEntitlements;

    public EwsMigrateEntitlement() {
        super();
    }

    public EwsMigrateEntitlement clone() throws CloneNotSupportedException {
        EwsMigrateEntitlement clone = (EwsMigrateEntitlement) super.clone();

        if (ewsEntitlements != null) {
            clone.setEwsEntitlements(new ArrayList<EwsEntitlement>());
            for (EwsEntitlement ewsEntitlement : ewsEntitlements) {
                clone.getEwsEntitlements().add(ewsEntitlement.clone());
            }
        }

        return clone;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public ArrayList<EwsEntitlement> getEwsEntitlements() {
        return ewsEntitlements;
    }

    public void setEwsEntitlements(ArrayList<EwsEntitlement> ewsEntitlements) {
        this.ewsEntitlements = ewsEntitlements;
    }

    public void validate() throws Exception {
        super.validate();

        if (this.psid == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("PSID", "Company"));
        }

        for (EwsEntitlement ewsEntitlement : ewsEntitlements) {
            ewsEntitlement.validate();
        }
    }
}
