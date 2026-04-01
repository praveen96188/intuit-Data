/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/PenaltyInterestDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.PenaltyInterestType;
import com.intuit.sbd.payroll.psp.domain.Agency;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.domain.PeriodType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.primary.SpcfMoney;

public class PenaltyInterestDTO {

    private String mPenaltyInterestId;
    private String mTransactionId;
    private DateDTO mPenaltyInterestDate;
    private SpcfMoney mPenaltyAmount;
    private SpcfMoney mInterestAmount;
    private String mNote;
    private Agency mAgency;
    private PaymentMethod mPaymentMethod;
    private PeriodType mPeriodType;
    private int mPeriodNumber;
    private int mYear;
    private String mCheckNumber;


    public String getPenaltyInterestId() {
        return mPenaltyInterestId;
    }

    public void setPenaltyInterestId(String mPenaltyInterestId) {
        this.mPenaltyInterestId = mPenaltyInterestId;
    }

    public String getTransactionId() {
        return mTransactionId;
    }

    public void setTransactionId(String mTransactionId) {
        this.mTransactionId = mTransactionId;
    }

    public DateDTO getPenaltyInterestDate() {
        return mPenaltyInterestDate;
    }

    public void setPenaltyInterestDate(DateDTO mPenaltyInterestDate) {
        this.mPenaltyInterestDate = mPenaltyInterestDate;
    }

    public SpcfMoney getPenaltyAmount() {
        return mPenaltyAmount;
    }

    public void setPenaltyAmount(SpcfMoney mPenaltyAmount) {
        this.mPenaltyAmount = mPenaltyAmount;
    }

    public SpcfMoney getInterestAmount() {
        return mInterestAmount;
    }

    public void setInterestAmount(SpcfMoney mInterestAmount) {
        this.mInterestAmount = mInterestAmount;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String mNote) {
        this.mNote = mNote;
    }

    public Agency getAgency() {
        return mAgency;
    }

    public void setAgency(Agency mAgency) {
        this.mAgency = mAgency;
    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int mYear) {
        this.mYear = mYear;
    }

    public PeriodType getPeriodType() {
        return mPeriodType;
    }

    public void setPeriodType(PeriodType mPeriodType) {
        this.mPeriodType = mPeriodType;
    }

    public int getPeriodNumber() {
        return mPeriodNumber;
    }

    public void setPeriodNumber(int mPeriodNumber) {
        this.mPeriodNumber = mPeriodNumber;

    }


    public PaymentMethod getPaymentMethod() {
        return mPaymentMethod;
    }

    public void setPaymentMethod(PaymentMethod mPaymentMethod) {
        this.mPaymentMethod = mPaymentMethod;
    }
    
    public String getCheckNumber() {
         return mCheckNumber;
     }

     public void setCheckNumber(String mCheckNumber) {
         this.mCheckNumber = mCheckNumber;
     }



    /**
     * Validates the DTO
     *
     * @return
     */


    public ProcessResult validatePenaltyInterestDTO() {
        ProcessResult validationResult = new ProcessResult();

        switch (mPeriodType) {
            case Week: {
                if (mPeriodNumber < 1 || mPeriodNumber > 52) {
                    validationResult.getMessages().InvalidValue(EntityName.TaxPenaltyInterest, Integer.toString(mPeriodNumber) , "Period Number");
                }
                break;
            }
            case Month: {
                if (mPeriodNumber < 1 || mPeriodNumber > 12) {
                    validationResult.getMessages().InvalidValue(EntityName.TaxPenaltyInterest, Integer.toString(mPeriodNumber), "Period Number");
                }
                break;
            }
            case Quarter: {
                if (mPeriodNumber < 1 || mPeriodNumber > 4) {
                    validationResult.getMessages().InvalidValue(EntityName.TaxPenaltyInterest, Integer.toString(mPeriodNumber), "Period Number");
                }
                break;
            }
            case Annual: {
                if (mPeriodNumber > 0) {
                    validationResult.getMessages().InvalidValue(EntityName.TaxPenaltyInterest, Integer.toString(mPeriodNumber), "Period Number");
                }
                break;
            }
        }
        return validationResult;
    }
}