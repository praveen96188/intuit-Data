package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 23, 2009
 * Time: 2:40:36 PM
 */
@XmlType(name = "BillPaymentStatus")
public class BillPaymentStatus {
    private String mSourcePaymentId;
    private List<BillPaymentSplitStatus> mBillPaymentSplitStatuses;

    @XmlElement(name = "SourcePaymentId", required = true)
    public String getSourcePaymentId() {
        return mSourcePaymentId;
    }

    public void setSourcePaymentId(String pSourcePaymentId) {
        mSourcePaymentId = pSourcePaymentId;
    }

    @XmlElementWrapper(name = "BillPaymentSplitStatuses")
    @XmlElement(name = "BillPaymentSplitStatus", required = true)
    public List<BillPaymentSplitStatus> getBillPaymentSplitStatuses() {
        if (mBillPaymentSplitStatuses == null) {
            mBillPaymentSplitStatuses = new ArrayList<BillPaymentSplitStatus>();
        }
        return this.mBillPaymentSplitStatuses;
    }
}
