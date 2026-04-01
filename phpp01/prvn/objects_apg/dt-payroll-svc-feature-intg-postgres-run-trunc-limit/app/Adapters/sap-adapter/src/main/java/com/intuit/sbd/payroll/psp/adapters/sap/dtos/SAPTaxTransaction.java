package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 27, 2009
 * Time: 8:44:28 AM 
 */
public class SAPTaxTransaction {
    private String txnDescription;
    private String paymentStatus;
    private Date submissionDate;
    private Date checkPaymentDate;
    private String paymentMethod;
    private double currentTaxes;
    private double currentWages;
    private double QTDTaxes;
    private double QTDWages;
    private double YTDTaxes;
    private double YTDWages;

    private boolean isSummary;
    private String moneyMovementTransactionId;
    private String payrollRunId;
    private String voidId;
    private String lawId;
    private String templateCd;
    private int quarter; //this is the quarter it is applied to which won't be the same as the payment date
    private int year; //this is the year it is applied to which won't be the same as the payment date
    private boolean isLastLineInQuarter;
    private String manualLedgerMemo;
    private String manualLedgerCreator;
    private boolean isReconcilingAdjustment;
    private SAPPayment payment;

    public boolean getIsReconcilingAdjustment() {
        return isReconcilingAdjustment;
    }

    public void setIsReconcilingAdjustment(boolean reconcilingAdjustment) {
        isReconcilingAdjustment = reconcilingAdjustment;
    }

    public String getTxnDescription() {
        return txnDescription;
    }

    public void setTxnDescription(String txnDescription) {
        this.txnDescription = txnDescription;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Date getCheckPaymentDate() {
        return checkPaymentDate;
    }

    public void setCheckPaymentDate(Date checkPaymentDate) {
        this.checkPaymentDate = checkPaymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getCurrentTaxes() {
        return currentTaxes;
    }

    public void setCurrentTaxes(double currentTaxes) {
        this.currentTaxes = currentTaxes;
    }

    public double getCurrentWages() {
        return currentWages;
    }

    public void setCurrentWages(double currentWages) {
        this.currentWages = currentWages;
    }

    public double getQTDTaxes() {
        return QTDTaxes;
    }

    public void setQTDTaxes(double QTDTaxes) {
        this.QTDTaxes = QTDTaxes;
    }

    public double getQTDWages() {
        return QTDWages;
    }

    public void setQTDWages(double QTDWages) {
        this.QTDWages = QTDWages;
    }

    public double getYTDTaxes() {
        return YTDTaxes;
    }

    public void setYTDTaxes(double YTDTaxes) {
        this.YTDTaxes = YTDTaxes;
    }

    public double getYTDWages() {
        return YTDWages;
    }

    public void setYTDWages(double YTDWages) {
        this.YTDWages = YTDWages;
    }

    public boolean getIsSummary() {
        return isSummary;
    }

    public void setIsSummary(boolean isSummary) {
        this.isSummary = isSummary;
    }

    public String getMoneyMovementTransactionId() {
        return moneyMovementTransactionId;
    }

    public void setMoneyMovementTransactionId(String moneyMovementTransactionId) {
        this.moneyMovementTransactionId = moneyMovementTransactionId;
    }

    public String getPayrollRunId() {
        return payrollRunId;
    }

    public void setPayrollRunId(String payrollRunId) {
        this.payrollRunId = payrollRunId;
    }

    public String getVoidId() {
        return voidId;
    }

    public void setVoidId(String voidId) {
        this.voidId = voidId;
    }

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String lawId) {
        this.lawId = lawId;
    }

    public String getTemplateCd() {
        return templateCd;
    }

    public void setTemplateCd(String templateCd) {
        this.templateCd = templateCd;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public boolean getIsLastLineInQuarter() {
        return isLastLineInQuarter;
    }

    public void setIsLastLineInQuarter(boolean isLastLineInQuarter) {
        this.isLastLineInQuarter = isLastLineInQuarter;
    }

    public String getManualLedgerMemo() {
        return manualLedgerMemo;
    }

    public void setManualLedgerMemo(String manualLedgerMemo) {
        this.manualLedgerMemo = manualLedgerMemo;
    }

    public String getManualLedgerCreator() {
        return manualLedgerCreator;
    }

    public void setManualLedgerCreator(String manualLedgerCreator) {
        this.manualLedgerCreator = manualLedgerCreator;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public SAPPayment getPayment() {
        return payment;
    }

    public void setPayment(SAPPayment pPayment) {
        payment = pPayment;
    }
}
