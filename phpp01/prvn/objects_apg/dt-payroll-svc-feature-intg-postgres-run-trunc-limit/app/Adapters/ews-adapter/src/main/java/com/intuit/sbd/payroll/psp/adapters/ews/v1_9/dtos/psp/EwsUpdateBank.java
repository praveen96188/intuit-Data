package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "psid",
    "ewsUpdateBankServices",
    "forceRandomDollar"
})
public class EwsUpdateBank extends EwsRequest implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "Services", required = true)
    protected EwsUpdateBankServices ewsUpdateBankServices;

    @XmlElement(name = "ForceRandomDollar", required = true)
    protected Boolean forceRandomDollar;

    public EwsUpdateBank() {
        super();
    }

    public EwsUpdateBank clone() throws CloneNotSupportedException {
        EwsUpdateBank clone = (EwsUpdateBank) super.clone();

        if (ewsUpdateBankServices != null) {
            clone.setEwsUpdateBankServices(ewsUpdateBankServices.clone());
        }

        return clone;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public EwsUpdateBankServices getEwsUpdateBankServices() {
        return ewsUpdateBankServices;
    }

    public void setEwsUpdateBankServices(EwsUpdateBankServices ewsUpdateBankServices) {
        this.ewsUpdateBankServices = ewsUpdateBankServices;
    }

    public Boolean getForceRandomDollar() {
        return forceRandomDollar;
    }

    public void setForceRandomDollar(Boolean forceRandomDollar) {
        this.forceRandomDollar = forceRandomDollar;
    }

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.psid, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "Company"));
        }

        ewsUpdateBankServices.validate();
    }
}
