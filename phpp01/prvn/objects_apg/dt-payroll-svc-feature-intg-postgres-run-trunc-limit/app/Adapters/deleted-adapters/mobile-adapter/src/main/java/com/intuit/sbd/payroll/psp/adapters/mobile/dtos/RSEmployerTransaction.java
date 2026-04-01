package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Jeff Jones
 */
public class RSEmployerTransaction {

    private String id;
    private BigDecimal netAmount;
    private RSEmployerTransactionTypeCode transactionTypeCode;
    private RSTransactionSplitStatusCode transactionStatus;
    private ArrayList<String> alertIdList;

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

    public RSTransactionSplitStatusCode getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(RSTransactionSplitStatusCode transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public ArrayList<String> getAlertIdList() {
        if (alertIdList == null)
            alertIdList = new ArrayList<String>();

        return alertIdList;
    }

    public RSEmployerTransactionTypeCode getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public void setTransactionTypeCode(RSEmployerTransactionTypeCode transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
    }
}
