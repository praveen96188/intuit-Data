/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPPayrollRun.java#2 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.domain.CollectionStageCode;

import java.util.Date;
import java.util.ArrayList;

/**
 * SAPPayrollRun - SAP DTO for a payroll run
 *
 * @author Joe Warmelink
 */
public class SAPPayrollRun {
    private Date paycheckDate;
    private double payrollNetAmount;
    private Date payrollRunDate;
    private String sourcePayRunId;
    private Date statusEffectiveDate;
    private Date paycheckSettlementDate;
    private PayrollStatus payrollRunStatus;
    private String companyId;
    private String sourceSystemId;
    private SAPCompanyBankAccount bankAccount;
    private String id;
    private CollectionStageCode collectionStage;
    private Date wireExpectedDate;
    private ArrayList<SAPActionEvent> actionCollection;
    private Date expectedResolutionDate;
    private boolean hasVoidedPaycheck;
    private boolean isHPDE;
    private String payrollType;
    private String manualCreator;
    private String manualNote;
    private boolean isSuperseded;
    private boolean isBackdated;
    private boolean hasDDTransactions;
    private boolean hasTaxTransactions;
    private double feeOnlyAmount;
    private String employerDDDebitTxnNumber;

    public CollectionStageCode getCollectionStage() {
        return collectionStage;
    }

    public void setCollectionStage(CollectionStageCode collectionStage) {
        this.collectionStage = collectionStage;
    }

    public Date getPaycheckDate() {
        return paycheckDate;
    }

    public void setPaycheckDate(Date paycheckDate) {
        this.paycheckDate = paycheckDate;
    }

    public Date getWireExpectedDate() {
        return wireExpectedDate;
    }

    public void setWireExpectedDate(Date wireExpectedDate) {
        this.wireExpectedDate = wireExpectedDate;
    }

    public double getPayrollNetAmount() {
        return payrollNetAmount;
    }

    public void setPayrollNetAmount(double payrollNetAmount) {
        this.payrollNetAmount = payrollNetAmount;
    }

    public Date getPayrollRunDate() {
        return payrollRunDate;
    }

    public void setPayrollRunDate(Date payrollRunDate) {
        this.payrollRunDate = payrollRunDate;
    }

    public String getSourcePayRunId() {
        return sourcePayRunId;
    }

    public void setSourcePayRunId(String sourcePayRunId) {
        this.sourcePayRunId = sourcePayRunId;
    }

    public Date getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public void setStatusEffectiveDate(Date statusEffectiveDate) {
        this.statusEffectiveDate = statusEffectiveDate;
    }

    public Date getPaycheckSettlementDate() {
        return paycheckSettlementDate;
    }

    public void setPaycheckSettlementDate(Date paycheckSettlementDate) {
        this.paycheckSettlementDate = paycheckSettlementDate;
    }

    public PayrollStatus getPayrollRunStatus() {
        return payrollRunStatus;
    }

    public void setPayrollRunStatus(PayrollStatus payrollRunStatus) {
        this.payrollRunStatus = payrollRunStatus;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getSourceSystemId() {
        return sourceSystemId;
    }

    public void setSourceSystemId(String sourceSystemId) {
        this.sourceSystemId = sourceSystemId;
    }

    public SAPCompanyBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(SAPCompanyBankAccount bankAccount) {
        this.bankAccount = bankAccount;
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

    public Date getExpectedResolutionDate() {
        return expectedResolutionDate;
    }

    public void setExpectedResolutionDate(Date expectedResolutionDate) {
        this.expectedResolutionDate = expectedResolutionDate;
    }

    public boolean getHasVoidedPaycheck() {
        return hasVoidedPaycheck;
    }

    public void setHasVoidedPaycheck(boolean hasVoidedPaycheck) {
        this.hasVoidedPaycheck = hasVoidedPaycheck;
    }

    public boolean isHPDE() {
        return isHPDE;
    }

    public void setHPDE(boolean HPDE) {
        isHPDE = HPDE;
    }

    public String getPayrollType() {
        return payrollType;
    }

    public void setPayrollType(String pPayrollType) {
        payrollType = pPayrollType;
    }

    public String getManualCreator() {
        return manualCreator;
    }

    public void setManualCreator(String manualCreator) {
        this.manualCreator = manualCreator;
    }

    public String getManualNote() {
        return manualNote;
    }

    public void setManualNote(String manualNote) {
        this.manualNote = manualNote;
    }

    public boolean getIsSuperseded() {
        return isSuperseded;
    }

    public void setIsSuperseded(boolean superseded) {
        isSuperseded = superseded;
    }

    public boolean getIsBackdated() {
        return isBackdated;
    }

    public void setIsBackdated(boolean backdated) {
        isBackdated = backdated;
    }

    public boolean getHasDDTransactions() {
        return hasDDTransactions;
    }

    public void setHasDDTransactions(boolean hasDDTransactions) {
        this.hasDDTransactions = hasDDTransactions;
    }

    public boolean getHasTaxTransactions() {
        return hasTaxTransactions;
    }

    public void setHasTaxTransactions(boolean hasTaxTransactions) {
        this.hasTaxTransactions = hasTaxTransactions;
    }

    public double getFeeOnlyAmount() {
        return feeOnlyAmount;
    }

    public void setFeeOnlyAmount(double pFeeOnlyAmount) {
        feeOnlyAmount = pFeeOnlyAmount;
    }

    public String getEmployerDDDebitTxnNumber() { return employerDDDebitTxnNumber; }

    public void setEmployerDDDebitTxnNumber(String employerDDDebitTxnNumber) {
        this.employerDDDebitTxnNumber = employerDDDebitTxnNumber;
    }
}
