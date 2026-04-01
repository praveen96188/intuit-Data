package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 @author Jeff Jones
 */

public class RSEmployeeTransaction implements Comparable<RSEmployeeTransaction>  {

    private String id;
    private BigDecimal netAmount;
    private RSTransactionStatusCode transactionStatus;
    private RSPayee payee;
    private ArrayList<RSTransactionSplit> transactionSplits;
    private Integer alertCount;

    public int compareTo(RSEmployeeTransaction o) {
        return payee.getLastName().compareTo(o.getPayee().getLastName());
    }

    public RSEmployeeTransaction() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public RSTransactionStatusCode getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(RSTransactionStatusCode transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public RSPayee getPayee() {
        return payee;
    }

    public void setPayee(RSPayee payee) {
        this.payee = payee;
    }

    public ArrayList<RSTransactionSplit> getTransactionSplits() {
        if (transactionSplits == null)
            transactionSplits = new ArrayList<RSTransactionSplit>();

        return transactionSplits;
    }
}
