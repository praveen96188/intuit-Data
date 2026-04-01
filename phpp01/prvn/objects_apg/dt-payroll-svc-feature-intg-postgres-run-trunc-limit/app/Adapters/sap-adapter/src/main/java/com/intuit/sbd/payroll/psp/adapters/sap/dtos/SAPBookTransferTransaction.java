package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * User: ihannur
 * Date: 2/16/12
 * Time: 5:45 PM
 */
public class SAPBookTransferTransaction {

    private String fromAccount;
    private String toAccount;
    private Number amount;
    private Date settlementDate;
    private String createdBy;
    private String transactionId;
    private String status;
    private Date createdDate;
    private String transactionType;
    private ArrayList<SAPActionEvent> actionCollection;

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String pFromAccount) {
        this.fromAccount = pFromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String pToAccount) {
        this.toAccount = pToAccount;
    }

    public Number getAmount() {
        return amount;
    }

    public void setAmount(Number amount) {
        this.amount = amount;
    }

    public Date getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(Date settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public ArrayList<SAPActionEvent> getActionCollection() {
        return actionCollection;
    }

    public void setActionCollection(ArrayList<SAPActionEvent> actionCollection) {
        this.actionCollection = actionCollection;
    }
}
