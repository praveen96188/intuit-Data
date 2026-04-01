package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;



import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlSeeAlso({
    EwsCompany.class
})

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "ein",
        "realmId"
})
public class EwsBaseCompany implements Cloneable {

    @XmlElement(name = "EIN", required = false)
    protected String ein;

    @XmlElement(name = "RealmId", required = false)
    protected String realmId;

    public EwsBaseCompany clone() throws CloneNotSupportedException {
        return (EwsBaseCompany) super.clone();
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public void validate() throws Exception {
        if (!Validation.validateValue(this.ein, true, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("EIN", "Company"));
        }

        if (!Validation.validateValue(this.realmId, true, "\\p{Digit}{0,50}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("RealmId", "Company"));
        }
    }
}
