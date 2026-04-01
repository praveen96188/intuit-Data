/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/CompanyBankAccountDTO.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * @author Marcela Villani
 */
public class CompanyBankAccountDTO {


    private String companyBankAccountID;
    private BankAccountDTO bankAccountDTO;
    private String sourceBankAccountName;

    public String getCompanyBankAccountID() {
        return companyBankAccountID;
    }

    public void setCompanyBankAccountID(String pCompanyBankAccountID) {
        this.companyBankAccountID = pCompanyBankAccountID;
    }

    public BankAccountDTO getBankAccountDTO() {
        return bankAccountDTO;
    }

    public void setBankAccountDTO(BankAccountDTO pBankAccountDTO) {
        this.bankAccountDTO = pBankAccountDTO;
    }

    public String getSourceBankAccountName() {
        return sourceBankAccountName;
    }

    public void setSourceBankAccountName(String sourceBankAccountName) {
        this.sourceBankAccountName = sourceBankAccountName;
    }    

    public ProcessResult validateCompanyBankAccount() {
        ProcessResult validationResult = new ProcessResult();

        // Check if bank account id is null
        if (companyBankAccountID == null || !Validator.isValidLength(companyBankAccountID, 1, 50)) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.CompanyBankAccount, companyBankAccountID, "BankAccountId");
        }

        // Validate Bank Account DTO
        if (bankAccountDTO != null) {
            validationResult.merge(bankAccountDTO.validateBankAccountDTO());

        }

        if (!Validator.isValidLength(sourceBankAccountName, 0, 128)) {
            validationResult.getMessages().InvalidValue(EntityName.CompanyBankAccount, companyBankAccountID, "SourceBankAccountName");
        }
        
        return validationResult;
    }
}
