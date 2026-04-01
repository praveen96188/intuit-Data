package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import javax.xml.bind.annotation.*;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Response WS DTO for the query company events request
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class RebillEmployerFinancialTransactionResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private String feeBillingDetailId;

    public String getFeeBillingDetailId() {
        return feeBillingDetailId;
    }

    public void setFeeBillingDetailId(String pFeeBillingDetailId) {
        feeBillingDetailId = pFeeBillingDetailId;
    }

    public void clearElements() {
    }

}
