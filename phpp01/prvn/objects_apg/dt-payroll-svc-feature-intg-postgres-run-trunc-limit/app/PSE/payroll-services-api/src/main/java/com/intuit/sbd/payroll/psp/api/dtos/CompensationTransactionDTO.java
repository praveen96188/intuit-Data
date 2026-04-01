/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/CompensationTransactionDTO.java#1 $
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
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

public class CompensationTransactionDTO {
    private String sourcePayrollItemId;
    private SpcfDecimal hoursWorked;
    private SpcfMoney compensationAmount;
    private SpcfMoney compensationYTDAmount;
    private Long payStubOrder;
    private QBDTPaylineInfoDTO mQBDTPaylineInfoDTO;

    public String getSourcePayrollItemId() {
        return sourcePayrollItemId;
    }

    public void setSourcePayrollItemId(String sourcePayrollItemId) {
        this.sourcePayrollItemId = sourcePayrollItemId;
    }

    public SpcfDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(SpcfDecimal hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public SpcfMoney getCompensationAmount() {
        return compensationAmount;
    }

    public void setCompensationAmount(SpcfMoney compensationAmount) {
        this.compensationAmount = compensationAmount;
    }

    public SpcfMoney getCompensationYTDAmount() {
        return compensationYTDAmount;
    }

    public void setCompensationYTDAmount(SpcfMoney compensationYTDAmount) {
        this.compensationYTDAmount = compensationYTDAmount;
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

    public ProcessResult validateCompensationTransactionDTO() {

        ProcessResult validationResult = new ProcessResult();

        if (sourcePayrollItemId == null || !Validator.isValidLength(sourcePayrollItemId, 1, 50)) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.CompensationTransaction, null, "SourceCompensationId");
            return validationResult;
        }

        if (compensationAmount == null) {
            validationResult.getMessages().InvalidValue(EntityName.CompensationTransaction, sourcePayrollItemId,
                    "CompensationAmount");
        }

        if (payStubOrder != null && payStubOrder<0) {
            validationResult.getMessages().InvalidValue(EntityName.DeductionTransaction, sourcePayrollItemId,
                    "PayStubOrder");
        }

        return validationResult;
    }
}