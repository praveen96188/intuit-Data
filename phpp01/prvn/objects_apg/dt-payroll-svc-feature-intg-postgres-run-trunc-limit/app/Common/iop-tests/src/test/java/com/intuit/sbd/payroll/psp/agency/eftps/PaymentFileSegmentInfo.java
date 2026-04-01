package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 28, 2010
 * Time: 9:00:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentFileSegmentInfo {
    String stSegmentId;
    String btiIdCode;
    SpcfMoney segmentTotalAmount;
    String transactionHandlingCode;
    List<PaymentFileRefTransaction> refTransactions = new ArrayList<PaymentFileRefTransaction>();
    List<String> refNumbers = new ArrayList<String>();
    String transactionTypeFlag;
    String authorizationNumber;
    String ricErrorCode;
    String returnErrorCode;
    String returnRefNumber;
    

    public PaymentFileSegmentInfo(String stSegmentId) {
        this.stSegmentId = stSegmentId;
    }

    public String getStSegmentId() {
        return stSegmentId;
    }

    public String getBtiIdCode() {
        return btiIdCode;
    }

    public List<PaymentFileRefTransaction> getRefTransactions() {
        return refTransactions;
    }

    public void setStSegmentId(String stSegmentId) {
        this.stSegmentId = stSegmentId;
    }

    public void setBtiIdCode(String btiIdCode) {
        this.btiIdCode = btiIdCode;
    }

    public void setRefTransactions(List<PaymentFileRefTransaction> pRefTransaction) {
        this.refTransactions = pRefTransaction;
    }

    public void addRefNumber(PaymentFileRefTransaction pRefTransaction){
        refTransactions.add(pRefTransaction);
        refNumbers.add(pRefTransaction.getRefNumber());
    }

    public SpcfMoney getSegmentTotalAmount() {
        return segmentTotalAmount;
    }

    public void setSegmentTotalAmount(SpcfMoney segmentTotalAmount) {
        this.segmentTotalAmount = segmentTotalAmount;
    }

    public List<String> getRefNumbers() {
        return refNumbers;
    }

    public void setReturnRefNumber(String returnRefNumber) {
        this.returnRefNumber = returnRefNumber;
    }

    public void setReturnErrorCode(String returnErrorCode) {
        this.returnErrorCode = returnErrorCode;
    }

    public void setAuthorizationNumber(String authorizationNumber) {
        this.authorizationNumber = authorizationNumber;
    }

    public void setTransactionTypeFlag(String transactionTypeFlag) {
        this.transactionTypeFlag = transactionTypeFlag;
    }

    public String getTransactionTypeFlag() {
        return transactionTypeFlag;
    }

    public String getAuthorizationNumber() {
        return authorizationNumber;
    }

    public String getReturnErrorCode() {
        return returnErrorCode;
    }

    public String getReturnRefNumber() {
        return returnRefNumber;
    }

    public void setRicErrorCode(String ricErrorCode) {
        this.ricErrorCode = ricErrorCode;
    }

    public String getRicErrorCode() {
        return ricErrorCode;
    }

    public String getTransactionHandlingCode() {
        return transactionHandlingCode;
    }

    public void setTransactionHandlingCode(String transactionHandlingCode) {
        this.transactionHandlingCode = transactionHandlingCode;
    }

}
