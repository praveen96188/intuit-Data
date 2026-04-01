package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * @author Marcela Villani
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "psid",
        "ewsCompany",
        "ewsEntitlements",
        "ewsServices",
        "forceRandomDollar"
})
public class EwsMigrateAccount extends EwsRequest implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "MigrateCompany", required = true)
    protected EwsCompany ewsCompany;

    @XmlElement(name = "Entitlement", required = true)
    protected ArrayList<EwsEntitlement> ewsEntitlements;

    @XmlElement(name = "Services", required = true)
    protected EwsServices ewsServices;

    @XmlElement(name = "ForceRandomDollar", required = true)
    protected Boolean forceRandomDollar;

    public EwsMigrateAccount() {
        super();
    }

    public EwsMigrateAccount clone() throws CloneNotSupportedException {
        EwsMigrateAccount clone = (EwsMigrateAccount) super.clone();

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

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
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

        if (this.psid == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("PSID", "Company"));
        }

        ewsCompany.validate();

        for (EwsEntitlement ewsEntitlement : ewsEntitlements) {
            ewsEntitlement.validate();
        }

        ewsServices.validate();
    }
}