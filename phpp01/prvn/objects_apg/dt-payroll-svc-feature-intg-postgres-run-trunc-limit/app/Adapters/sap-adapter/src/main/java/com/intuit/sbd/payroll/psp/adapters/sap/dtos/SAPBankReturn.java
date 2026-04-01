/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPBankReturn.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * SAPBankReturn - SAP DTO for Bank Returns
 *
 * @author Joe Warmelink
 */
public class SAPBankReturn {
    private String statusCd;
    private String txnType;
    private String companyName;
    private String employeeName;
    private String companyId;
    private String companySourceSystemCd;
    private String sourcePayRunId;
    private String fein;
    private String bankAccountNumber;
    private String bankRoutingNumber;
    private double amount;
    private String returnCd;
    private Date checkDate;
    private Date returnDate;
    private String txnId;
    private String payrollStatus;
    private SAPBankReturnExtendedInfo bankReturnExtendedInfo;

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

     public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanySourceSystemCd() {
        return companySourceSystemCd;
    }

    public void setCompanySourceSystemCd(String companySourceSystemCd) {
        this.companySourceSystemCd = companySourceSystemCd;
    }

    public String getSourcePayRunId() {
        return sourcePayRunId;
    }

    public void setSourcePayRunId(String sourcePayRunId) {
        this.sourcePayRunId = sourcePayRunId;
    }

    public String getFein() {
        return fein;
    }

    public void setFein(String fein) {
        this.fein = fein;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public void setBankRoutingNumber(String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReturnCd() {
        return returnCd;
    }

    public void setReturnCd(String returnCd) {
        this.returnCd = returnCd;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    } 

    public String getPayrollStatus() {
        return payrollStatus;
    }

    public void setPayrollStatus(String payrollStatus) {
        this.payrollStatus = payrollStatus;
    }

    public SAPBankReturnExtendedInfo getBankReturnExtendedInfo() {
        return bankReturnExtendedInfo;
    }

    public void setBankReturnExtendedInfo(SAPBankReturnExtendedInfo bankReturnExtendedInfo) {
        this.bankReturnExtendedInfo = bankReturnExtendedInfo;
    }
}
