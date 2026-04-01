package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

@XmlRootElement()
@XmlType(name = "QueryBillPaymentStatusResponse")
public class QueryBillPaymentStatusResponse extends QBProcessingMessages {

    private List<BillPaymentStatus> mBillPaymentStatuses;

    @XmlElementWrapper(name = "PaymentStatuses")
    @XmlElement(name = "BillPaymentStatus")
    public List<BillPaymentStatus> getPaymentStatuses() {
        if (mBillPaymentStatuses == null) {
            mBillPaymentStatuses = new ArrayList<BillPaymentStatus>();
        }
        return this.mBillPaymentStatuses;
    }
}
