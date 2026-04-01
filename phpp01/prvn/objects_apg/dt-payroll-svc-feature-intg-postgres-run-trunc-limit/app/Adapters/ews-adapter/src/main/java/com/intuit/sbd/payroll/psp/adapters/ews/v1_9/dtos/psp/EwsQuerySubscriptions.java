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
    "subscriptionNumber"
})
public class EwsQuerySubscriptions extends EwsRequest implements Cloneable {
    @XmlElement(name = "SubscriptionNumber", required = true)
    protected String subscriptionNumber;

    public EwsQuerySubscriptions clone() throws CloneNotSupportedException {
        return (EwsQuerySubscriptions) super.clone();
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.subscriptionNumber, false, "^(\\P{M}\\p{M}*){1,19}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("SubscriptionNumber", "QuerySubscriptionNumber"));
        }
    }
}
