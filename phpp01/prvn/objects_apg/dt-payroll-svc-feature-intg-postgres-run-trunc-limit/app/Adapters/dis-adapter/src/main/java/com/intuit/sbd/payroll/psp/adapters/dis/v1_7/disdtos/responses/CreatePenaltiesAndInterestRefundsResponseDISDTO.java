package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import javax.xml.bind.annotation.*;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/RefundEmployerFinancialTransactionResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
