package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 28, 2009
 * Time: 1:26:06 PM
 */
@XmlType(name = "BillPaymentReturn")
public class BillPaymentReturn {
    private ACHReturnReasonEnum mACHReturnReason;
    private String mReturnDescription;
    private ReturnStatusEnum mReturnStatus;

    @XmlElement(name = "ACHReturnReason")
    public ACHReturnReasonEnum getACHReturnReason() {
        return mACHReturnReason;
    }

    public void setACHReturnReason(ACHReturnReasonEnum pACHReturnReason) {
        mACHReturnReason = pACHReturnReason;
    }

    @XmlElement(name = "ReturnDescription")
    public String getReturnDescription() {
        return mReturnDescription;
    }

    public void setReturnDescription(String pReturnDescription) {
        mReturnDescription = pReturnDescription;
    }

    @XmlElement(name = "ReturnStatus")
    public ReturnStatusEnum getReturnStatus() {
        return mReturnStatus;
    }

    public void setReturnStatus(ReturnStatusEnum pReturnStatus) {
        mReturnStatus = pReturnStatus;
    }
}
