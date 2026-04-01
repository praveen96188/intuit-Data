/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/BillPaymentSplitDTO.java#1 $
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

public class BillPaymentSplitDTO {
    private String billPaymentSplitId;
    private BigDecimal amount;
    private PayeeBankAccountDTO payeeBankAccount;
    private String referenceNumber;

    public String getBillPaymentSplitId() {
        return billPaymentSplitId;
    }

    public void setBillPaymentSplitId(String billPaymentSplitId) {
        this.billPaymentSplitId = billPaymentSplitId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PayeeBankAccountDTO getPayeeBankAccount() {
        return payeeBankAccount;
    }

    public void setPayeeBankAccount(PayeeBankAccountDTO pEmployeeBankAccount) {
        this.payeeBankAccount = pEmployeeBankAccount;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public ProcessResult validateBillPaymentSplitDTO() {
        ProcessResult validationResult = new ProcessResult();

        if (billPaymentSplitId != null && !Validator.isValidLength(billPaymentSplitId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.DTO, billPaymentSplitId, "BillPaymentSplitId");
        }

        if (payeeBankAccount == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.DTO, billPaymentSplitId, "PayeeBankAccount");
            return validationResult;
        }

        if (amount == null || amount.scale() >2) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.DTO, billPaymentSplitId, "BillPaymentSplitAmount");
        }

        if (!(amount.compareTo(new BigDecimal("0.00"))>0)) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.DTO, billPaymentSplitId,"BillPaymentSplitAmount");
        }

        if(!Validator.isValidLength(referenceNumber,0,50)){
            validationResult.getMessages().InvalidValue(EntityName.BillPayment, billPaymentSplitId, "ReferenceNumber");
        }

        validationResult.merge(payeeBankAccount.validatePayeeBankAccount());

         return validationResult;
    }
}