package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "eligibleForDIY",
        "eligibleForDD",
        "eligibleForAssisted"
})
public class EwsEinServiceEligibilityResponse extends EwsResponse implements Cloneable {

    @XmlElement(name = "EligibleForDIY", required = false)
    protected Boolean eligibleForDIY;

    @XmlElement(name = "EligibleForDD", required = false)
    protected Boolean eligibleForDD;

    @XmlElement(name = "EligibleForAssisted", required = false)
    protected Boolean eligibleForAssisted;

    public Boolean getEligibleForDIY() {
        return eligibleForDIY;
    }

    public void setEligibleForDIY(Boolean eligibleForDIY) {
        this.eligibleForDIY = eligibleForDIY;
    }

    public Boolean getEligibleForDD() {
        return eligibleForDD;
    }

    public void setEligibleForDD(Boolean eligibleForDD) {
        this.eligibleForDD = eligibleForDD;
    }

    public Boolean getEligibleForAssisted() {
        return eligibleForAssisted;
    }

    public void setEligibleForAssisted(Boolean eligibleForAssisted) {
        this.eligibleForAssisted = eligibleForAssisted;
    }
}
