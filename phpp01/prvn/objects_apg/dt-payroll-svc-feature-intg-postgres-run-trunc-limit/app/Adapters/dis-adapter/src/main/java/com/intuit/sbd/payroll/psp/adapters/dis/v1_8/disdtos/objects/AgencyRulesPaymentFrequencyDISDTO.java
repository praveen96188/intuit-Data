package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgencyRulesPaymentFrequencyDISDTO")
public class AgencyRulesPaymentFrequencyDISDTO {
    @XmlElement(name = "PaymentFrequencyId")
    private String paymentFrequencyId;

    @XmlElement(name = "UIDescription")
    private String uiDescription;

    public String getPaymentFrequencyId() {
        return paymentFrequencyId;
    }

    public void setPaymentFrequencyId(String paymentFrequencyId) {
        this.paymentFrequencyId = paymentFrequencyId;
    }

    public String getUiDescription() {
        return uiDescription;
    }

    public void setUiDescription(String uiDescription) {
        this.uiDescription = uiDescription;
    }
}
