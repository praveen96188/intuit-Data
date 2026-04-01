package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "billingAccountId"
})
public class EwsBaseEntitlement implements Cloneable {

    @XmlElement(name = "BillingAccountId", required = false)
    protected String billingAccountId;    

    public EwsBaseEntitlement clone() throws CloneNotSupportedException {
        return (EwsBaseEntitlement) super.clone();
    }

    public String getBillingAccountId() {
        return billingAccountId;
    }

    public void setBillingAccountId(String billingAccountId) {
        this.billingAccountId = billingAccountId;
    }

    public void validate() throws Exception {
        if (!Validation.validateValue(this.billingAccountId, true, "^(\\P{M}\\p{M}*){0,50}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("BillingAccountId", "Entitlement"));
        }        
    }
}
