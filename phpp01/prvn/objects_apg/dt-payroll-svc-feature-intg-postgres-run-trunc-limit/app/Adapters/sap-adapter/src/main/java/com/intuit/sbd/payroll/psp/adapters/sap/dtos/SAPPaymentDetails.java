package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: ihannur
 * Date: Jan 19, 2011
 * Time: 12:28:46 PM
 */
public class SAPPaymentDetails {
    private String ftId;
    private Date createdDate;
    private Date checkDate;
    private double amount;
    private String law;
    private String lawType;
    private String txnType;

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public double getAmount() {
        return amount;
    }

    public String getLaw() {
        return law;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setLaw(String law) {
        this.law = law;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getLawType() {
        return lawType;
    }

    public void setLawType(String lawType) {
        this.lawType = lawType;
    }

    public String getFtId() {
        return ftId;
    }

    public void setFtId(String ftId) {
        this.ftId = ftId;
    }
}
