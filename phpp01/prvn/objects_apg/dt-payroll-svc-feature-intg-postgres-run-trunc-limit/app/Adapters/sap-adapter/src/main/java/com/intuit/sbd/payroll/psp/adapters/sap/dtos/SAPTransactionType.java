/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPTransactionType.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.TransactionCategory;
import com.intuit.sbd.payroll.psp.domain.TransactionAssociationType;
import com.intuit.sbd.payroll.psp.domain.NACHABatchType;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;

/**
 * SAPTransationType - SAP DTO for financial transaction type information
 *
 * @author Joe Warmelink
 */
public class SAPTransactionType {
    private TransactionTypeCode transactionTypeCd;
    private String name;
    private String description;
    private TransactionCategory transactionCategory;
    private TransactionAssociationType associationType;
    private boolean feeInd;
    private NACHABatchType NACHABatchType;

    public TransactionTypeCode getTransactionTypeCd() {
        return transactionTypeCd;
    }

    public void setTransactionTypeCd(TransactionTypeCode transactionTypeCd) {
        this.transactionTypeCd = transactionTypeCd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionCategory getTransactionCategory() {
        return transactionCategory;
    }

    public void setTransactionCategory(TransactionCategory transactionCategory) {
        this.transactionCategory = transactionCategory;
    }

    public TransactionAssociationType getAssociationType() {
        return associationType;
    }

    public void setAssociationType(TransactionAssociationType transactionAssociationType) {
        this.associationType = transactionAssociationType;
    }

    public boolean isFeeInd() {
        return feeInd;
    }

    public void setFeeInd(boolean feeInd) {
        this.feeInd = feeInd;
    }

    public NACHABatchType getNACHABatchType() {
        return NACHABatchType;
    }

    public void setNACHABatchType(NACHABatchType nachaBatchType) {
        this.NACHABatchType = nachaBatchType;
    }
}
