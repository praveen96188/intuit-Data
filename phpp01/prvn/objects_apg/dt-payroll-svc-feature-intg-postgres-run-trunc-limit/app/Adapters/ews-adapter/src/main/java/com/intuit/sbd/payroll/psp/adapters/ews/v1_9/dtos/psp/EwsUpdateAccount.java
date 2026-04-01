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
    "ewsCompany"
})
public class EwsUpdateAccount extends EwsRequest implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "Company", required = true)
    protected EwsCompany ewsCompany;

    public EwsUpdateAccount() {
        super();
    }

    public EwsUpdateAccount clone() throws CloneNotSupportedException {
        EwsUpdateAccount clone = (EwsUpdateAccount) super.clone();

        if (ewsCompany != null) {
            clone.setEwsCompany(ewsCompany.clone());
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

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.psid, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "Company"));
        }

        ewsCompany.validate();
    }
}
