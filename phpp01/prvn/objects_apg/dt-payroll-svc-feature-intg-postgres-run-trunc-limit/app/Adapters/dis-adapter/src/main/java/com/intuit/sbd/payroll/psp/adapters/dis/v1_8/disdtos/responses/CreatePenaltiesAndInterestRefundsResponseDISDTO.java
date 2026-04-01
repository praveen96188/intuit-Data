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
public class CreatePenaltiesAndInterestRefundsResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private String interestRefundTransactionId;
    private String penaltyRefundTransactionId;

    public String getInterestRefundTransactionId() {
        return interestRefundTransactionId;
    }

    public void setInterestRefundTransactionId(String pInterestRefundTransactionId) {
        interestRefundTransactionId = pInterestRefundTransactionId;
    }

    public String getPenaltyRefundTransactionId() {
        return penaltyRefundTransactionId;
    }

    public void setPenaltyRefundTransactionId(String pPenaltyRefundTransactionId) {
        penaltyRefundTransactionId = pPenaltyRefundTransactionId;
    }

    public void clearElements() {
    }

}
