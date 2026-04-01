/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/EmployeeBankAccountDTO.java#7 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

public class EmployeeBankAccountDTO {
    private String employeeBankAccountId;
    private double amount;
    private QbdtNumericType amountType;
    private BankAccountDTO bankAccount;
    private int order = -1;
    private boolean isPaycheckUpdate = false;
    private boolean generateNewSourceId = false;
    private String sessionId;

    public String getEmployeeBankAccountId() {
        if(employeeBankAccountId == null) {
            employeeBankAccountId = generateBankAccountId();
        }
        return employeeBankAccountId;
    }

    public void setEmployeeBankAccountId(String pEmployeeBankAccountId) {
        this.employeeBankAccountId = pEmployeeBankAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double pAmount) {
        amount = pAmount;
    }

    public QbdtNumericType getAmountType() {
        return amountType;
    }

    public void setAmountType(QbdtNumericType pAmountType) {
        amountType = pAmountType;
    }

    public BankAccountDTO getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountDTO pBankAccount) {
        this.bankAccount = pBankAccount;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int pOrder) {
        order = pOrder;
    }

    public boolean isPaycheckUpdate() {
        return isPaycheckUpdate;
    }

    public void setPaycheckUpdate(boolean pPaycheckUpdate) {
        isPaycheckUpdate = pPaycheckUpdate;
    }

    public boolean isGenerateNewSourceId() {
        return generateNewSourceId;
    }

    public void setGenerateNewSourceId(boolean pGenerateNewSourceId) {
        generateNewSourceId = pGenerateNewSourceId;
    }

    public String getSessionId() { return sessionId; }

    public void setSessionId(String pSessionId) {
        sessionId = pSessionId;
    }

    public ProcessResult validateEmployeeBankAccount() {
        ProcessResult validationResult = new ProcessResult();

        if (!Validator.isValidLength(getEmployeeBankAccountId(), 1, 50)) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.EmployeeBankAccount, getEmployeeBankAccountId(), "EmployeeBankAccountId");
        }

        return validationResult;
    }

    private String generateBankAccountId() {
        if(bankAccount != null) {
            return bankAccount.getAccountNumber() + bankAccount.getRoutingNumber() + bankAccount.getAccountType() + order;
        }

        return SpcfUniqueId.generateRandomUniqueIdString();
    }

    public void resetBankAccountId() {
        employeeBankAccountId = null;
    }
}
