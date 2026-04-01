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
import java.util.ArrayList;

/**
 * TTEntryDetailRecord - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class TTEntryDetailRecord {
    private double amount;
    private String traceNumber;
    private String creditDebitIndicator;
    private Date settlementDate;
    private String accountNumber;
    private String routingNumber;
    private String bankName;
    private String bankAccountType;
    private String bankAccountOwnerType;
    private String companyId;
    private String companyLegalName;
    private String individualName;
    private String mmTransactionId;
    private ArrayList<TTBankReturn> bankReturns;
    private boolean isBankReturnsExists;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(String traceNumber) {
        this.traceNumber = traceNumber;
    }

    public String getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public void setCreditDebitIndicator(String creditDebitIndicator) {
        this.creditDebitIndicator = creditDebitIndicator;
    }

    public Date getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(Date settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getBankAccountOwnerType() {
        return bankAccountOwnerType;
    }

    public void setBankAccountOwnerType(String bankAccountOwnerType) {
        this.bankAccountOwnerType = bankAccountOwnerType;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public String getMmTransactionId() {
        return mmTransactionId;
    }

    public void setMmTransactionId(String mmTransactionId) {
        this.mmTransactionId = mmTransactionId;
    }

    public ArrayList<TTBankReturn> getBankReturns() {
        return bankReturns;
    }

    public void setBankReturns(ArrayList<TTBankReturn> bankReturns) {
        this.bankReturns = bankReturns;
    }

    public boolean isBankReturnsExists() {
        return isBankReturnsExists;
    }

    public void setBankReturnsExists(boolean bankReturnsExists) {
        isBankReturnsExists = bankReturnsExists;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String companyLegalName) {
        this.companyLegalName = companyLegalName;
    }
}
