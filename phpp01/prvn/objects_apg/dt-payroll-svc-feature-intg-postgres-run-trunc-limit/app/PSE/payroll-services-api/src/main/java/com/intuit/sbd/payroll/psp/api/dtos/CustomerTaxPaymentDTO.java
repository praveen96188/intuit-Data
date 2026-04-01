/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/CustomerTaxPaymentDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import java.math.BigDecimal;
import java.util.Map;

public class CustomerTaxPaymentDTO {
    private int mYear;
    private int mQuarter;
    private DateDTO mPaymentDate;
    private String mPaymentTemplateId;
    private String mMemo;
    private Map<String, BigDecimal> mLawPaymentAmounts;
    private boolean mApplyPayments;
    private boolean immediateCredit;


    public int getYear() {
        return mYear;
    }

    public void setYear(int pYear) {
        this.mYear = pYear;
    }

    public int getQuarter() {
        return mQuarter;
    }

    public void setQuarter(int pQuarter) {
        this.mQuarter = pQuarter;
    }

    public DateDTO getPaymentDate() {
        return mPaymentDate;
    }

    public void setPaymentDate(DateDTO pPaymentDate) {
        this.mPaymentDate = pPaymentDate;
    }

    public String getPaymentTemplateId() {
        return mPaymentTemplateId;
    }

    public void setPaymentTemplateId(String pPaymentTemplate) {
        this.mPaymentTemplateId = pPaymentTemplate;
    }

    public String getMemo() {
        return mMemo;
    }

    public void setMemo(String pMemo) {
        this.mMemo = pMemo;
    }

    public Map<String, BigDecimal> getPaymentAmounts() {
        return mLawPaymentAmounts;
    }

    public void setPaymentAmounts(Map<String, BigDecimal> pPaymentAmounts) {
        this.mLawPaymentAmounts = pPaymentAmounts;
    }

    public boolean applyPayments() {
        return mApplyPayments;
    }

    public void setApplyPayments(boolean mApplyPayments) {
        this.mApplyPayments = mApplyPayments;
    }

    public boolean isImmediateCredit() {
        return immediateCredit;
    }

    public void setImmediateCredit(boolean pImmediateCredit) {
        immediateCredit = pImmediateCredit;
    }
}