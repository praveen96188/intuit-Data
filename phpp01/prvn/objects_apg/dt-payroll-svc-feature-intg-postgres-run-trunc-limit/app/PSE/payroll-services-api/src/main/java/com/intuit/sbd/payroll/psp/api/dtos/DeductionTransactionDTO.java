/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/DeductionTransactionDTO.java#1 $
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

public class DeductionTransactionDTO {
    private String sourcePayrollItemId;
    private BigDecimal deductionAmount;
    private BigDecimal deductionYTDAmount;
    private Long payStubOrder;
    private QBDTPaylineInfoDTO mQBDTPaylineInfoDTO = new QBDTPaylineInfoDTO();

    public String getSourcePayrollItemId() {
        return sourcePayrollItemId;
    }

    public void setSourcePayrollItemId(String sourcePayrollItemId) {
        this.sourcePayrollItemId = sourcePayrollItemId;
    }


    public BigDecimal getDeductionAmount() {
        return deductionAmount;
    }

    public void setDeductionAmount(BigDecimal deductionAmount) {
        this.deductionAmount = deductionAmount;
    }

    public BigDecimal getDeductionYTDAmount() {
        return deductionYTDAmount;
    }

    public void setDeductionYTDAmount(BigDecimal deductionYTDAmount) {
        this.deductionYTDAmount = deductionYTDAmount;
    }

    public Long getPayStubOrder() {
        return payStubOrder;
    }

    public void setPayStubOrder(Long payStubOrder) {
        this.payStubOrder = payStubOrder;
    }

    public QBDTPaylineInfoDTO getQBDTPaylineInfoDTO() {
        return mQBDTPaylineInfoDTO;
    }

    public void setQBDTPaylineInfoDTO(QBDTPaylineInfoDTO pQBDTPaylineInfoDTO) {
        mQBDTPaylineInfoDTO = pQBDTPaylineInfoDTO;
    }

    public ProcessResult validateDeductionTransactionDTO() {

        ProcessResult validationResult = new ProcessResult();

        if (sourcePayrollItemId == null || !Validator.isValidLength(sourcePayrollItemId, 1, 50)) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.DeductionTransaction, null, "SourceDeductionId");
            return validationResult;
        }

        if (deductionAmount == null || deductionAmount.scale() > 2) {
            validationResult.getMessages().InvalidValue(EntityName.DeductionTransaction, sourcePayrollItemId,
                    "DeductionAmount");
        }

        if (deductionYTDAmount != null && deductionAmount.scale() > 2) {
            validationResult.getMessages().InvalidValue(EntityName.DeductionTransaction, sourcePayrollItemId,
                    "DeductionAmount");
        }

        if (payStubOrder != null && payStubOrder<0) {
            validationResult.getMessages().InvalidValue(EntityName.DeductionTransaction, sourcePayrollItemId,
                    "PayStubOrder");
        }

        return validationResult;
    }
}