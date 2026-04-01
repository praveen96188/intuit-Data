package com.intuit.sbd.payroll.psp.api.dtos;

import java.util.Collection;
import java.util.Map;

/**
 * User: dweinberg
 * Date: 2/18/11
 * Time: 4:14 PM
 */
public class PriorPaymentSubmissionDTO {
    private String mSourceId;
    private QBDTTransactionInfoDTO mQBDTTransactionInfoDTO;
    //payment template code -> prior payment
    private Map<String, PriorPaymentDTO> mPayments;

    public String getSourceId() {
        return mSourceId;
    }

    public void setSourceId(String mSourceId) {
        this.mSourceId = mSourceId;
    }

    public QBDTTransactionInfoDTO getQBDTTransactionInfoDTO() {
        return mQBDTTransactionInfoDTO;
    }

    public void setQBDTTransactionInfoDTO(QBDTTransactionInfoDTO mQBDTTransactionInfoDTO) {
        this.mQBDTTransactionInfoDTO = mQBDTTransactionInfoDTO;
    }

    public Map<String, PriorPaymentDTO> getPayments() {
        return mPayments;
    }

    public void setPayments(Map<String, PriorPaymentDTO> mPayments) {
        this.mPayments = mPayments;
    }
}
