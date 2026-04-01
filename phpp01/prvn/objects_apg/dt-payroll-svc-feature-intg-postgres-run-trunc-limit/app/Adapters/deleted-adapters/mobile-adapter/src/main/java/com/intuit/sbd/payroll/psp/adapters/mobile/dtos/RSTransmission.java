package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 @author Jeff Jones
 */

public class RSTransmission {

    private String id;
    private String periodStart;
    private String periodEnd;
    private String settlementDate;
    private String runDate;
    private RSTransmissionTypeCode transmissionType;
    private RSTransmissionStatusCode transmissionStatus;
    private BigDecimal amount;
    private ArrayList<RSEmployerTransaction> erTransactions;
    private ArrayList<RSEmployeeTransaction> eeTransactions;
    private Integer alertCount;
    private String alertId;
    private RSBankAccount bankAccount;

    public RSTransmission() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(String periodStart) {
        this.periodStart = periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(String settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getRunDate() {
        return runDate;
    }

    public void setRunDate(String runDate) {
        this.runDate = runDate;
    }

    public RSTransmissionTypeCode getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(RSTransmissionTypeCode transmissionType) {
        this.transmissionType = transmissionType;
    }

    public RSTransmissionStatusCode getTransmissionStatus() {
        return transmissionStatus;
    }

    public void setTransmissionStatus(RSTransmissionStatusCode transmissionStatus) {
        this.transmissionStatus = transmissionStatus;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public ArrayList<RSEmployerTransaction> getEmployerTransactions() {
        if (erTransactions == null)
            erTransactions = new ArrayList<RSEmployerTransaction>();

        return erTransactions;
    }

    public ArrayList<RSEmployeeTransaction> getEmployeeTransactions() {
        if (eeTransactions == null)
            eeTransactions = new ArrayList<RSEmployeeTransaction>();

        return eeTransactions;
    }

    public Integer getAlertCount() {
        return alertCount;
    }

    public void setAlertCount(Integer alertCount) {
        this.alertCount = alertCount;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public RSBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(RSBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }
}
