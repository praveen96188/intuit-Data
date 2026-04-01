package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 29, 2010
 * Time: 4:03:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentFileRefTransaction {
    String refNumber;
    SpcfMoney transactionAmount;

    public String getRefNumber() {
        return refNumber;
    }

    public SpcfMoney getTransactionAmount() {
        return transactionAmount;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public void setTransactionAmount(SpcfMoney transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
}
