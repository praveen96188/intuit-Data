/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/LiabilityTransactionDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

import java.math.BigDecimal;

public class LiabilityTransactionDTO {

    private String lawId;
    private String payrollItemId;
    private BigDecimal liabilityAmount;
    private BigDecimal liabilityTotalWages;
    private BigDecimal liabilityTaxableWages;
    private BigDecimal liabilityAmountYTD;
    private BigDecimal liabilityTipsTaxableWages;
    private Long payStubOrder;


    public BigDecimal getLiabilityAmountYTD() {
        return liabilityAmountYTD;
    }

    public void setLiabilityAmountYTD(BigDecimal liabilityAmountYTD) {
        this.liabilityAmountYTD = liabilityAmountYTD;
    }

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String pLawId) {
        this.lawId = pLawId;
    }

    public String getPayrollItemId() {
        return payrollItemId;
    }

    public void setPayrollItemId(String pPayrollItemId) {
        payrollItemId = pPayrollItemId;
    }

    public BigDecimal getLiabilityAmount() {
        return liabilityAmount;
    }

    public void setLiabilityAmount(BigDecimal pLiabilityAmount) {
        this.liabilityAmount = pLiabilityAmount;
    }

    public BigDecimal getLiabilityTotalWages() {
        return liabilityTotalWages;
    }

    public void setLiabilityTotalWages(BigDecimal pLiabilityTotalWages) {
        this.liabilityTotalWages = pLiabilityTotalWages;
    }

    public BigDecimal getLiabilityTaxableWages() {
        return liabilityTaxableWages;
    }

    public void setLiabilityTaxableWages(BigDecimal pLiabilityTaxableWages) {
        this.liabilityTaxableWages = pLiabilityTaxableWages;
    }

    public Long getPayStubOrder() {
        return payStubOrder;
    }

    public void setPayStubOrder(long payStubOrder) {
        this.payStubOrder = payStubOrder;
    }

    public BigDecimal getLiabilityTipsTaxableWages() {
        return liabilityTipsTaxableWages;
    }

    public void setLiabilityTipsTaxableWages(BigDecimal pLiabilityTipsTaxableWages) {
        liabilityTipsTaxableWages = pLiabilityTipsTaxableWages;
    }

    public ProcessResult validateLiabilityTransactionDTO() {

        ProcessResult validationResult = new ProcessResult();
        

        if (lawId == null || !Validator.isValidLength(lawId, 1, 50)) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.LiabilityTransaction, "LawId" ,"LawId");
        }
        if (liabilityAmount == null || liabilityAmount.scale() >2) {
            validationResult.getMessages().InvalidValue(EntityName.LiabilityTransaction, lawId,
                    "LiabilityTransactionAmount");
        }
        if (liabilityTotalWages == null || liabilityTotalWages.scale() >2) {
            validationResult.getMessages().InvalidValue(EntityName.LiabilityTransaction, lawId,
                    "LiabilityTotalWages");
        }
        if (liabilityTaxableWages == null || liabilityTaxableWages.scale() >2) {
            validationResult.getMessages().InvalidValue(EntityName.LiabilityTransaction, lawId,
                    "LiabilityTaxableWages");
        }

        if (liabilityTipsTaxableWages != null && liabilityTipsTaxableWages.scale() > 2) {
            validationResult.getMessages().InvalidValue(EntityName.LiabilityTransaction, lawId,
                    "LiabilityTipsTaxableWages");
        }

        return validationResult;
    }
}
