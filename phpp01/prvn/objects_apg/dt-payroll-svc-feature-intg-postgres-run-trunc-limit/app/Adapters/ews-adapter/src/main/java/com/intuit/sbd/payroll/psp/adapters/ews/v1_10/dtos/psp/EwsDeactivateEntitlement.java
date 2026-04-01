package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * User: praveenkumarh635
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "eins",
        "subscriptionNumber"
})

public class EwsDeactivateEntitlement extends EwsRequest implements Cloneable {

    @XmlElement(name = "EIN", required = true)
    protected List<String> eins;

    @XmlElement(name = "SubscriptionNumber", required = true)
    protected String subscriptionNumber;

    public EwsDeactivateEntitlement() {
        super();
    }

    public EwsDeactivateEntitlement clone() throws CloneNotSupportedException {
        return (EwsDeactivateEntitlement) super.clone();
    }

    public List<String> getEins() {
        return this.eins;
    }

    public void setEins(List<String> eins) {
        this.eins = eins;
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String pSubscriptionNumber) {
        subscriptionNumber = pSubscriptionNumber;
    }

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.subscriptionNumber, false, "^(\\P{M}\\p{M}*){1,19}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("SubscriptionNumber", "QuerySubscriptionNumber"));
        }

        if(this.eins == null || this.eins.isEmpty()) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("EIN", "Company"));
        }

        for(String ein : this.eins){
            if(ein == null || ein.isEmpty()){
                throw new EwsException(EwsMessages.objectCanNotBeNull("EIN"));
            }
            if (!Validation.validateValue(ein, false, "\\p{Digit}{9,9}")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("EIN", "Company"));
            }
        }
    }
}
