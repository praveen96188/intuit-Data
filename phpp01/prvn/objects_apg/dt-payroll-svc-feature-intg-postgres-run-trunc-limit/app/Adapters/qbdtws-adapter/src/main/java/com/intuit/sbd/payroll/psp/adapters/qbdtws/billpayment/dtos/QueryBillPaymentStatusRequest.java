package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

@XmlRootElement()
@XmlType(name = "QueryBillPaymentStatusRequest")
public class QueryBillPaymentStatusRequest extends Request {

    private List<String> mBillPaymentIds;

    @XmlElementWrapper(name = "BillPaymentIds")
    @XmlElement(name = "BillPaymentId")
    public List<String> getBillPaymentIds() {
        if (mBillPaymentIds == null) {
            mBillPaymentIds = new ArrayList<String>();
        }
        return this.mBillPaymentIds;
    }
}
