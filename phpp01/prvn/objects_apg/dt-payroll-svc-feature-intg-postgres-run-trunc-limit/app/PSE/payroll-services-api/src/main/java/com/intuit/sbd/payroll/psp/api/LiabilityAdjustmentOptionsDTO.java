/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/LiabilityAdjustmentOptionsDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api;

import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;

public class LiabilityAdjustmentOptionsDTO {

    private boolean mRecordLiabilities = false;
    private boolean mDebitCustomer = true;
    private boolean mCreditCustomer = false;
    private boolean mRecordFinancialTransactions = false;
    private boolean mRecall = false;
    private boolean mVoid = false;
    private boolean mBALF = false;
    private boolean mForceToRecordFTs = false;
    private boolean useVarianceAccount = false;
    private boolean isSUIAdjustment = false;
    private DateDTO mSettlementDate;


    public LiabilityAdjustmentOptionsDTO() {
    }

    public LiabilityAdjustmentOptionsDTO(boolean pRecordLiabilities, boolean pDebitCustomer, boolean pRecordFinancialTransactions, DateDTO pSettlementDate, boolean pRecall, boolean pBALF) {
        mRecordLiabilities = pRecordLiabilities;
        mDebitCustomer = pDebitCustomer;
        mRecordFinancialTransactions = pRecordFinancialTransactions;
        mRecall = pRecall;
        mBALF = pBALF;
        mSettlementDate = pSettlementDate;
    }

    public boolean recordLiabilities() {
        return mRecordLiabilities;
    }

    public void setRecordLiabilities(boolean pRecordLiabilities) {
        this.mRecordLiabilities = pRecordLiabilities;
    }

    public boolean debitCustomer() {
        return mDebitCustomer;
    }

    public void setDebitCustomer(boolean pDebitCustomer) {
        this.mDebitCustomer = pDebitCustomer;
    }

    public boolean creditCustomer() {
        return mCreditCustomer;
    }

    public void setCreditCustomer(boolean mCreditCustomer) {
        this.mCreditCustomer = mCreditCustomer;
    }

    public boolean recordFinancialTransactions() {
        return mRecordFinancialTransactions;
    }

    public void setRecordFinancialTransactions(boolean pRecordFinancialTransactions) {
        this.mRecordFinancialTransactions = pRecordFinancialTransactions;
    }

    public boolean isRecall() {
        return mRecall;
    }

    public void setRecall(boolean pRecall) {
        this.mRecall = pRecall;
    }

    public boolean isVoid() {
        return mVoid;
    }

    public void setVoid(boolean pVoid) {
        mVoid = pVoid;
    }

    public boolean isBALF() {
        return mBALF;
    }

    public void setBALF(boolean mBALF) {
        this.mBALF = mBALF;
    }

    public boolean isForceToRecordFTs() {
        return mForceToRecordFTs;
    }

    public void setForceToRecordFTs(boolean mForceToRecordFTs) {
        this.mForceToRecordFTs = mForceToRecordFTs;
    }

    public boolean getUseVarianceAccount() {
        return useVarianceAccount;
    }

    public void setUseVarianceAccount(boolean useVarianceAccount) {
        this.useVarianceAccount = useVarianceAccount;
    }

    public boolean isSUIAdjustment() {
        return isSUIAdjustment;
    }

    public void setSUIAdjustment(boolean SUIAdjustment) {
        isSUIAdjustment = SUIAdjustment;
    }

    public DateDTO getSettlementDate() {
        return mSettlementDate;
    }

    public void setSettlementDate(DateDTO pSettlementDate) {
        mSettlementDate = pSettlementDate;
    }
}