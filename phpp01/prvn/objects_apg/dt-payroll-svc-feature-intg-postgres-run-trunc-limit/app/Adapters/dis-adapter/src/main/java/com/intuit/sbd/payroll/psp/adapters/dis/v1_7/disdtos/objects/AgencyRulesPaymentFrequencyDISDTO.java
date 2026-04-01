package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/AgencyRulesPaymentFrequencyDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
