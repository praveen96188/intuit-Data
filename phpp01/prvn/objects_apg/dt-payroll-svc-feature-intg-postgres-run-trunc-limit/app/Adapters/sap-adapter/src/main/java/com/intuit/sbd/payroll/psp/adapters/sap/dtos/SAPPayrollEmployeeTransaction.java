/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPPayrollEmployeeTransaction.java#1 $
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
 * SAPPayrollEmployeeTransaction - SAP DTO for Payroll Employee Financial Transactions
 *
 * @author Joe Warmelink
 */
public class SAPPayrollEmployeeTransaction extends SAPPayrollTransaction {
    private String employeeName;
    private String employeeBankAccountNumber;
    private String employeeBankRoutingNumber;
    private boolean voidedAfterOffload;
    private Date voidedDate;
    private String emailId;
    private boolean hasInvalidEmail;
    private String employerDDDebitTxnNumber;
    private String jpmcTraceId;

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeBankAccountNumber() {
        return employeeBankAccountNumber;
    }

    public void setEmployeeBankAccountNumber(String employeeBankAccountNumber) {
        this.employeeBankAccountNumber = employeeBankAccountNumber;
    }

    public String getEmployeeBankRoutingNumber() {
        return employeeBankRoutingNumber;
    }

    public void setEmployeeBankRoutingNumber(String employeeBankRoutingNumber) {
        this.employeeBankRoutingNumber = employeeBankRoutingNumber;
    }

    public boolean isVoidedAfterOffload() {
        return voidedAfterOffload;
    }

    public void setVoidedAfterOffload(boolean voidedAfterOffload) {
        this.voidedAfterOffload = voidedAfterOffload;
    }

    public Date getVoidedDate() {
        return voidedDate;
    }

    public void setVoidedDate(Date voidedDate) {
        this.voidedDate = voidedDate;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String pEmailId) {
        emailId = pEmailId;
    }

    public boolean isHasInvalidEmail() {
        return hasInvalidEmail;
    }

    public void setHasInvalidEmail(boolean pHasInvalidEmail) {
        hasInvalidEmail = pHasInvalidEmail;
    }
  
    public String getEmployerDDDebitTxnNumber() { return employerDDDebitTxnNumber; }

    public void setEmployerDDDebitTxnNumber(String employerDDDebitTxnNumber) {
        this.employerDDDebitTxnNumber = employerDDDebitTxnNumber;
    }
  
	  public String getJpmcTraceId() {
		    return jpmcTraceId;
	  }

	  public void setJpmcTraceId(String jpmcTraceId) {
		    this.jpmcTraceId = jpmcTraceId;
	  }
}
