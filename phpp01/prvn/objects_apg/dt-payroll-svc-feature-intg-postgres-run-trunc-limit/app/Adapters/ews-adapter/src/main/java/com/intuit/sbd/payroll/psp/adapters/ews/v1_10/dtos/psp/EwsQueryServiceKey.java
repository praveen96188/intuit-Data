package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

/**
 * @author Jeff Jones
 */

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "ein",
    "authId"})
public class EwsQueryServiceKey extends EwsRequest implements Cloneable {

    @XmlElement(name = "EIN", required = true)
    protected String ein;

    @XmlElement(name = "AuthId", required = true)
    protected String authId;

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.ein, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("EIN", "Request"));
        }

        if (!Validation.validateValue(this.authId, false, "\\p{Digit}{1,50}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AuthId", "Request"));
        }
    }

}
