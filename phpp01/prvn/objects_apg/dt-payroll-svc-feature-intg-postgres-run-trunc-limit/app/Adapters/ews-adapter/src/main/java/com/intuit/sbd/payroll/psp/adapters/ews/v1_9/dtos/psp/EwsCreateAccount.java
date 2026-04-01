package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "ewsCompany",
    "ewsEntitlements",
    "ewsServices",
    "forceRandomDollar"
})
public class EwsCreateAccount extends EwsRequest implements Cloneable {

    @XmlElement(name = "CreateCompany", required = true)
    protected EwsCompany ewsCompany;

    @XmlElement(name = "Entitlement", required = true)
    protected ArrayList<EwsEntitlement> ewsEntitlements;

    @XmlElement(name = "Services", required = true)
    protected EwsServices ewsServices;

    @XmlElement(name = "ForceRandomDollar", required = true)
    protected Boolean forceRandomDollar;

    public EwsCreateAccount() {
        super();
    }

    public EwsCreateAccount clone() throws CloneNotSupportedException {
        EwsCreateAccount clone = (EwsCreateAccount) super.clone();

        if (ewsCompany != null) {
            clone.setEwsCompany(ewsCompany.clone());
        }

        if (ewsEntitlements != null) {
            clone.setEwsEntitlements(new ArrayList<EwsEntitlement>());
            for (EwsEntitlement ewsEntitlement : ewsEntitlements) {
                clone.getEwsEntitlements().add(ewsEntitlement.clone());
            }
        }

        if (ewsServices != null) {
            clone.setEwsServices(ewsServices.clone());
        }

        return clone;
    }

    public EwsCompany getEwsCompany() {
        return ewsCompany;
    }

    public void setEwsCompany(EwsCompany ewsCompany) {
        this.ewsCompany = ewsCompany;
    }

    public ArrayList<EwsEntitlement> getEwsEntitlements() {
        return ewsEntitlements;
    }

    public void setEwsEntitlements(ArrayList<EwsEntitlement> ewsEntitlements) {
        this.ewsEntitlements = ewsEntitlements;
    }

    public EwsServices getEwsServices() {
        return ewsServices;
    }

    public void setEwsServices(EwsServices ewsServices) {
        this.ewsServices = ewsServices;
    }

    public Boolean getForceRandomDollar() {
        return forceRandomDollar;
    }

    public void setForceRandomDollar(Boolean forceRandomDollar) {
        this.forceRandomDollar = forceRandomDollar;
    }

    public void validate() throws Exception {
        super.validate();

        ewsCompany.validate();

        for (EwsEntitlement ewsEntitlement : ewsEntitlements) {
            ewsEntitlement.validate();
        }

        ewsServices.validate();
    }
}
