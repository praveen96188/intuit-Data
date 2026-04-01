/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/EmployerContributionTransactionDTO.java#1 $
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

public class EmployerContributionTransactionDTO {
    private String sourcePayrollItemId;
    private BigDecimal contributionAmount;
    private BigDecimal contributionYTDAmount;
    private BigDecimal taxableWagesAmount;
    private BigDecimal totalWagesAmount;
    private Long payStubOrder;
    private QBDTPaylineInfoDTO mQBDTPaylineInfoDTO = new QBDTPaylineInfoDTO();

    public String getSourcePayrollItemId() {
        return sourcePayrollItemId;
    }

    public void setSourcePayrollItemId(String sourcePayrollItemId) {
        this.sourcePayrollItemId = sourcePayrollItemId;
    }

    public BigDecimal getContributionAmount() {
        return contributionAmount;
    }

    public void setContributionAmount(BigDecimal contributionAmount) {
        this.contributionAmount = contributionAmount;
    }

    public BigDecimal getContributionYTDAmount() {
        return contributionYTDAmount;
    }

    public void setContributionYTDAmount(BigDecimal contributionYTDAmount) {
        this.contributionYTDAmount = contributionYTDAmount;
    }

    public BigDecimal getTaxableWagesAmount() {
        return taxableWagesAmount;
    }

    public void setTaxableWagesAmount(BigDecimal taxableWagesAmount) {
        this.taxableWagesAmount = taxableWagesAmount;
    }

    public BigDecimal getTotalWagesAmount() {
        return totalWagesAmount;
    }

    public void setTotalWagesAmount(BigDecimal totalWagesAmount) {
        this.totalWagesAmount = totalWagesAmount;
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

    public ProcessResult validateEmployerContributionTransactionDTO() {

        ProcessResult validationResult = new ProcessResult();

        if (sourcePayrollItemId == null || !Validator.isValidLength(sourcePayrollItemId, 1, 50)) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.CompensationTransaction, null, "SourceCompensationId");
            return validationResult;
        }

        if (contributionAmount != null && contributionAmount.scale() > 2) {
            validationResult.getMessages().InvalidValue(EntityName.EmployerContributionTransaction, sourcePayrollItemId,
                    "contributionAmount");
        }

        if (contributionYTDAmount != null && contributionYTDAmount.scale() > 2) {
            validationResult.getMessages().InvalidValue(EntityName.EmployerContributionTransaction, sourcePayrollItemId,
                    "contributionYTDAmount");
        }

        if (taxableWagesAmount != null && taxableWagesAmount.scale() > 2) {
            validationResult.getMessages().InvalidValue(EntityName.EmployerContributionTransaction, sourcePayrollItemId,
                    "taxableWagesAmount");
        }

        if (totalWagesAmount != null && totalWagesAmount.scale() > 2) {
            validationResult.getMessages().InvalidValue(EntityName.EmployerContributionTransaction, sourcePayrollItemId,
                    "totalWagesAmount");
        }

        if (payStubOrder != null && payStubOrder<0) {
            validationResult.getMessages().InvalidValue(EntityName.DeductionTransaction, sourcePayrollItemId,
                    "PayStubOrder");
        }

        return validationResult;
    }
}