/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPPayline.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * SAPPayline - SAP DTO for Paycheck payline information related to a single paycheck split.
 *
 * @author Joe Warmelink
 */
public class SAPPayline {
    private double paylineAmount;
    private double tipsAmount;
    private double totalWagesAmount;
    private String sourceTaxTypeId;
    private String paylineTransactionId;

    public double getPaylineAmount() {
        return paylineAmount;
    }

    public void setPaylineAmount(double paylineAmount) {
        this.paylineAmount = paylineAmount;
    }

    public double getTipsAmount() {
        return tipsAmount;
    }

    public void setTipsAmount(double tipsAmount) {
        this.tipsAmount = tipsAmount;
    }

    public double getTotalWagesAmount() {
        return totalWagesAmount;
    }

    public void setTotalWagesAmount(double totalWagesAmount) {
        this.totalWagesAmount = totalWagesAmount;
    }

    public String getSourceTaxTypeId() {
        return sourceTaxTypeId;
    }

    public void setSourceTaxTypeId(String sourceTaxTypeId) {
        this.sourceTaxTypeId = sourceTaxTypeId;
    }

    public String getPaylineTransactionId() {
        return paylineTransactionId;
    }

    public void setPaylineTransactionId(String paylineTransactionId) {
        this.paylineTransactionId = paylineTransactionId;
    }
}
