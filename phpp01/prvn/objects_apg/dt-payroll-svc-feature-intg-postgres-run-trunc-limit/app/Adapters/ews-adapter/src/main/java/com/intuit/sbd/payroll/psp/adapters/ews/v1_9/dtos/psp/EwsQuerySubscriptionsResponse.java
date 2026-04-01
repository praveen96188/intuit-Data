package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "ewsSubscriptionResponses"
})
public class EwsQuerySubscriptionsResponse extends EwsResponse implements Cloneable {
    @XmlElement(name = "SubscriptionResponses", required = false)
    protected ArrayList<EwsSubscriptionResponse> ewsSubscriptionResponses;

    public EwsQuerySubscriptionsResponse clone() throws CloneNotSupportedException {
        EwsQuerySubscriptionsResponse clone = (EwsQuerySubscriptionsResponse) super.clone();

        if (ewsSubscriptionResponses != null) {
            clone.setEwsSubscriptionResponses(new ArrayList<EwsSubscriptionResponse>());
            for (EwsSubscriptionResponse ewsSubscriptionResponse : ewsSubscriptionResponses) {
                clone.getEwsSubscriptionResponses().add(ewsSubscriptionResponse.clone());
            }
        }

        return clone;
    }

    public ArrayList<EwsSubscriptionResponse> getEwsSubscriptionResponses() {
        return ewsSubscriptionResponses;
    }

    public void setEwsSubscriptionResponses(ArrayList<EwsSubscriptionResponse> ewsSubscriptionResponses) {
        this.ewsSubscriptionResponses = ewsSubscriptionResponses;
    }
}
