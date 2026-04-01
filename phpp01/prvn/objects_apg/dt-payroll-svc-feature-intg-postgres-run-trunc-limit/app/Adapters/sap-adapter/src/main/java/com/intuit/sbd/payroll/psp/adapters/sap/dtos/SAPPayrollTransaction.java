/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPPayrollTransaction.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;

import java.util.Date;
import java.util.ArrayList;


/**
 * SAPPayrollTransaction - DTO for SAP financial transaction
 *
 * @author Joe Warmelink
 */
public class SAPPayrollTransaction {
    private Date createdDate;
    private Date txnDate;
    private TransactionTypeCode txnType;
    private SettlementType settlementType;
    private TransactionStateCode status;
    private double amount;
    private String sourcePayRunId;
    private String transactionId;
    private String id;
    private ArrayList<SAPActionEvent> actionCollection;
    private boolean isCredit = false;
    private String returnCd;
    private String description;

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(Date txnDate) {
        this.txnDate = txnDate;
    }

    public TransactionTypeCode getTxnType() {
        return txnType;
    }

    public void setTxnType(TransactionTypeCode txnType) {
        this.txnType = txnType;
    }

    public SettlementType getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(SettlementType settlementType) {
        this.settlementType = settlementType;
    }

    public TransactionStateCode getStatus() {
        return status;
    }

    public void setStatus(TransactionStateCode status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getSourcePayRunId() {
        return sourcePayRunId;
    }

    public void setSourcePayRunId(String sourcePayRunId) {
        this.sourcePayRunId = sourcePayRunId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<SAPActionEvent> getActionCollection() {
        return actionCollection;
    }

    public void setActionCollection(ArrayList<SAPActionEvent> actionCollection) {
        this.actionCollection = actionCollection;
    }

    public boolean isCredit() {
        return isCredit;
    }

    public void setCredit(boolean credit) {
        isCredit = credit;
    }

    public String getReturnCd() {
        return returnCd;
    }

    public void setReturnCd(String returnCd) {
        this.returnCd = returnCd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
