/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools;

import java.util.Date;

/**
 * TTBankReturn - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class TTBankReturn {
    private String bankReturnCd;
    private Date creationDate;
    private String id; // GUID
    private String transactionId;  // GUID
    private String sourceEmployeeId;
    private String employeeDisplayName; // First name + last name
    private String traceNumber;
    private String returnStatus;
    private Date statusChangeDate;
    private String routingNumber;
    private String accountNumber;
    private String accountType;
    private String description;

    public String getBankReturnCd() {
        return bankReturnCd;
    }

    public void setBankReturnCd(String bankReturnCd) {
        this.bankReturnCd = bankReturnCd;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSourceEmployeeId() {
        return sourceEmployeeId;
    }

    public void setSourceEmployeeId(String sourceEmployeeId) {
        this.sourceEmployeeId = sourceEmployeeId;
    }

    public String getEmployeeDisplayName() {
        return employeeDisplayName;
    }

    public void setEmployeeDisplayName(String employeeDisplayName) {
        this.employeeDisplayName = employeeDisplayName;
    }

    public String getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(String traceNumber) {
        this.traceNumber = traceNumber;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public Date getStatusChangeDate() {
        return statusChangeDate;
    }

    public void setStatusChangeDate(Date statusChangeDate) {
        this.statusChangeDate = statusChangeDate;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
