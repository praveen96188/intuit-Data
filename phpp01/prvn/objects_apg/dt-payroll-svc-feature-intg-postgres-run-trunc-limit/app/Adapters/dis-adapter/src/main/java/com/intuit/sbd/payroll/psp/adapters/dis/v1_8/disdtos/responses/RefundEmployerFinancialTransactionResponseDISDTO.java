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
public class RefundEmployerFinancialTransactionResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private String refundTransactionId;

    public String getRefundTransactionId() {
        return refundTransactionId;
    }

    public void setRefundTransactionId(String pRefundTransactionId) {
        refundTransactionId = pRefundTransactionId;
    }

    public void clearElements() {
    }

}
