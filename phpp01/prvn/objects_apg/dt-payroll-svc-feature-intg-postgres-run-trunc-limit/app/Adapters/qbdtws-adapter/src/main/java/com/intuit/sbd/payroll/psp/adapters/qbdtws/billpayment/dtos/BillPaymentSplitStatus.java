package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 24, 2009
 * Time: 8:39:27 AM
 */
@XmlType(name = "BillPaymentSplitStatus")
public class BillPaymentSplitStatus {
    private String mSourcePaymentSplitId;
    private TransactionStateEnum mTransactionState;
    private List<BillPaymentReturn> mBillPaymentReturns;

    @XmlElement(name = "SourcePaymentSplitId", required = true)
    public String getSourcePaymentSplitId() {
        return mSourcePaymentSplitId;
    }

    public void setSourcePaymentSplitId(String pSourcePaymentSplitId) {
        mSourcePaymentSplitId = pSourcePaymentSplitId;
    }

    @XmlElement(name = "TransactionState", required = true)
    public TransactionStateEnum getTransactionState() {
        return mTransactionState;
    }

    public void setTransactionState(TransactionStateEnum pTransactionState) {
        mTransactionState = pTransactionState;
    }

    @XmlElementWrapper(name = "BillPaymentReturns")
    @XmlElement(name = "BillPaymentReturn", required = true)
    public List<BillPaymentReturn> getBillPaymentReturns() {
        if (mBillPaymentReturns == null) {
            mBillPaymentReturns = new ArrayList<BillPaymentReturn>();
        }
        return this.mBillPaymentReturns;
    }
}
