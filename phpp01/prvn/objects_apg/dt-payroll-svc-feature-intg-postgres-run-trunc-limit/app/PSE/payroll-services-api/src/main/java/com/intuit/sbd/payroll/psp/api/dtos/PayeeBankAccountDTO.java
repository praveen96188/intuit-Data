/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/PayeeBankAccountDTO.java#1 $
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

public class PayeeBankAccountDTO {
    private String payeeBankAccountId;
    private BankAccountDTO bankAccount;
    private String sessionId;

    public String getPayeeBankAccountId() {
        return payeeBankAccountId;
    }

    public void setPayeeBankAccountId(String pEmployeeBankAccountId) {
        this.payeeBankAccountId = pEmployeeBankAccountId;
    }

    public BankAccountDTO getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountDTO pBankAccount) {
        this.bankAccount = pBankAccount;
    }

    public String getSessionId() { return sessionId; }

    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public ProcessResult validatePayeeBankAccount() {
        ProcessResult validationResult = new ProcessResult();

        if (payeeBankAccountId == null || !Validator.isValidLength(payeeBankAccountId, 1, 50)) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.PayeeBankAccount, payeeBankAccountId, "PayeeBankAccountId");
        }

        // bank account name is required
        if (bankAccount.getBankName() == null) {
            validationResult.getMessages().InvalidValue(EntityName.BankAccount, bankAccount.getBankName(), "BankName");
        }

        validationResult.merge(bankAccount.validateBankAccountDTO());

        return validationResult;
    }
}