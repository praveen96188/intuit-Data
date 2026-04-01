/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/DDTransactionDTO.java#1 $
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

public class DDTransactionDTO {
    private String DDTransactionId;
    private BigDecimal DDTransactionAmount;
    private EmployeeBankAccountDTO employeeBankAccount;
    private Long mPayStubOrder;

    public String getDDTransactionId() {
        return DDTransactionId;
    }

    public void setDDTransactionId(String pDDTransactionId) {
        this.DDTransactionId = pDDTransactionId;
    }

    public BigDecimal getDDTransactionAmount() {
        return DDTransactionAmount;
    }

    public void setDDTransactionAmount(BigDecimal pDDTransactionAmount) {
        this.DDTransactionAmount = pDDTransactionAmount;
    }

    public EmployeeBankAccountDTO getEmployeeBankAccount() {
        return employeeBankAccount;
    }

    public void setEmployeeBankAccount(EmployeeBankAccountDTO pEmployeeBankAccount) {
        this.employeeBankAccount = pEmployeeBankAccount;
    }

    public Long getPayStubOrder() {
        return mPayStubOrder;
    }

    public void setPayStubOrder(Long pPayStubOrder) {
        mPayStubOrder = pPayStubOrder;
    }

    public ProcessResult validateDDTransactionDTO() {
        ProcessResult validationResult = new ProcessResult();

        if (DDTransactionId == null || !Validator.isValidLength(DDTransactionId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.DDTransaction, DDTransactionId, "DDTransactionId");
        }

        if (employeeBankAccount == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.DDTransaction, DDTransactionId, "EmployeeBankAccount");
            return validationResult;
        }

        if (DDTransactionAmount == null || DDTransactionAmount.scale() >2) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.DDTransaction, DDTransactionId, "DDTransactionAmount");
        }

         return validationResult;
    }
}
