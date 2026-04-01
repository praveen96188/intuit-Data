package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * User: ihannur
 * Date: Jul 27, 2011
 * Time: 6:01:54 PM
 */
public class SAPPaymentForVerification {
    private String taxPaymentStatus;
    private Date initiationDate;
    private Date dueDate;
    private Date settlementDate;
    private SAPPaymentTemplate paymentTemplate;
    private Number amount;
    private String paymentMethod;
    private String paymentId;
    private String taxpayerAgencyId;
    private Date periodBeginDate;
    private Date periodEndDate;
    private String debitAccountNumber;
    private String creditAccountRouting;
    private String creditAccountNumber;    
    private ArrayList<SAPKeyValuePair> details;

    public String getTaxPaymentStatus() {
        return taxPaymentStatus;
    }

    public void setTaxPaymentStatus(String taxPaymentStatus) {
        this.taxPaymentStatus = taxPaymentStatus;
    }

    public Date getInitiationDate() {
        return initiationDate;
    }

    public void setInitiationDate(Date initiationDate) {
        this.initiationDate = initiationDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(Date pSettlementDate) {
        settlementDate = pSettlementDate;
    }

    public SAPPaymentTemplate getPaymentTemplate() {
        return paymentTemplate;
    }

    public void setPaymentTemplate(SAPPaymentTemplate paymentTemplate) {
        this.paymentTemplate = paymentTemplate;
    }

    public Number getAmount() {
        return amount;
    }

    public void setAmount(Number amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTaxpayerAgencyId() {
        return taxpayerAgencyId;
    }

    public void setTaxpayerAgencyId(String taxpayerAgencyId) {
        this.taxpayerAgencyId = taxpayerAgencyId;
    }

    public Date getPeriodBeginDate() {
        return periodBeginDate;
    }

    public void setPeriodBeginDate(Date periodBeginDate) {
        this.periodBeginDate = periodBeginDate;
    }

    public Date getPeriodEndDate() {
        return periodEndDate;
    }

    public void setPeriodEndDate(Date periodEndDate) {
        this.periodEndDate = periodEndDate;
    }

    public String getDebitAccountNumber() {
        return debitAccountNumber;
    }

    public void setDebitAccountNumber(String debitAccountNumber) {
        this.debitAccountNumber = debitAccountNumber;
    }

    public String getCreditAccountRouting() {
        return creditAccountRouting;
    }

    public void setCreditAccountRouting(String creditAccountRouting) {
        this.creditAccountRouting = creditAccountRouting;
    }

    public String getCreditAccountNumber() {
        return creditAccountNumber;
    }

    public void setCreditAccountNumber(String creditAccountNumber) {
        this.creditAccountNumber = creditAccountNumber;
    }

    public ArrayList<SAPKeyValuePair> getDetails() {
        return details;
    }

    public void setDetails(ArrayList<SAPKeyValuePair> details) {
        this.details = details;
    }
}
