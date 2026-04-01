package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

@XmlRootElement()
@XmlType(name = "VoidPaymentResponse")
public class VoidPaymentResponse extends QBProcessingMessages {
    private List<FeeTransaction> feeTransactions;

    @XmlElementWrapper(name = "UpdatedFeeTransactions")
    @XmlElement(name = "FeeTransaction")
    public List<FeeTransaction> getFeeTransactions() {
        if (feeTransactions == null) {
            feeTransactions = new ArrayList<FeeTransaction>();
        }
        return this.feeTransactions;
    }

    public void setFeeTransactions(List<FeeTransaction> pFeeTransactions) {
        feeTransactions = pFeeTransactions;
    }
}
