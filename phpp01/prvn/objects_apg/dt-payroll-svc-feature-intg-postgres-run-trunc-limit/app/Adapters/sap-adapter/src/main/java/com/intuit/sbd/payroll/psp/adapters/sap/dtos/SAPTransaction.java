package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 4/16/12
 * Time: 1:36 PM
 */
public class SAPTransaction {
    private Number amount;
    private Date settlementDate;
    private String createdBy;
    private String transactionId;
    private String status;
    private Date createdDate;
    private String transactionType;
    private String settlementType;
    private String returnCd;
    private ArrayList<SAPActionEvent> actionCollection;

    public Number getAmount() {
        return amount;
    }

    public void setAmount(Number pAmount) {
        amount = pAmount;
    }

    public Date getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(Date pSettlementDate) {
        settlementDate = pSettlementDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String pCreatedBy) {
        createdBy = pCreatedBy;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String pTransactionId) {
        transactionId = pTransactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date pCreatedDate) {
        createdDate = pCreatedDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String pTransactionType) {
        transactionType = pTransactionType;
    }

    public String getReturnCd() {
        return returnCd;
    }

    public void setReturnCd(String pReturnCd) {
        returnCd = pReturnCd;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String pSettlementType) {
        settlementType = pSettlementType;
    }

    public ArrayList<SAPActionEvent> getActionCollection() {
        return actionCollection;
    }

    public void setActionCollection(ArrayList<SAPActionEvent> pActionCollection) {
        actionCollection = pActionCollection;
    }
}
