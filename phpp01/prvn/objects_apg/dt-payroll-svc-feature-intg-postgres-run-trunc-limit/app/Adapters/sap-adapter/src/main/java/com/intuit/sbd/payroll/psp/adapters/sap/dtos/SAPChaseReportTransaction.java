package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 18, 2008
 * Time: 1:49:29 PM
 */
public class SAPChaseReportTransaction {
    private Date settlementDate;
    private double debitAmount;
    private double creditAmount;
    private String debitAccountName;
    private String creditAccountName;
    private String debitAccountRoutingNumber;
    private String creditAccountRoutingNumber;
    private String debitAccountNumber;
    private String creditAccountNumber;

    public Date getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(Date settlementDate) {
        this.settlementDate = settlementDate;
    }

    public double getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(double debitAmount) {
        this.debitAmount = debitAmount;
    }

    public double getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(double creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getDebitAccountName() {
        return debitAccountName;
    }

    public void setDebitAccountName(String debitAccountName) {
        this.debitAccountName = debitAccountName;
    }

    public String getCreditAccountName() {
        return creditAccountName;
    }

    public void setCreditAccountName(String creditAccountName) {
        this.creditAccountName = creditAccountName;
    }

    public String getDebitAccountRoutingNumber() {
        return debitAccountRoutingNumber;
    }

    public void setDebitAccountRoutingNumber(String debitAccountRoutingNumber) {
        this.debitAccountRoutingNumber = debitAccountRoutingNumber;
    }

    public String getCreditAccountRoutingNumber() {
        return creditAccountRoutingNumber;
    }

    public void setCreditAccountRoutingNumber(String creditAccountRoutingNumber) {
        this.creditAccountRoutingNumber = creditAccountRoutingNumber;
    }

    public String getDebitAccountNumber() {
        return debitAccountNumber;
    }

    public void setDebitAccountNumber(String debitAccountNumber) {
        this.debitAccountNumber = debitAccountNumber;
    }

    public String getCreditAccountNumber() {
        return creditAccountNumber;
    }

    public void setCreditAccountNumber(String creditAccountNumber) {
        this.creditAccountNumber = creditAccountNumber;
    }
}