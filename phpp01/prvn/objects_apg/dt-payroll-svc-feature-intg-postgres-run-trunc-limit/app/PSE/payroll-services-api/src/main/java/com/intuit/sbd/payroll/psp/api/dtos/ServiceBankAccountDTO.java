/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/ServiceBankAccountDTO.java#1 $
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
import com.intuit.sbd.payroll.psp.domain.ServiceCode;


public class ServiceBankAccountDTO {
    private ServiceCode serviceCode;
    private CompanyBankAccountDTO companyBankAccount;

    public ServiceCode getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(ServiceCode pServiceCode) {
        this.serviceCode = pServiceCode;
    }

    public CompanyBankAccountDTO getCompanyBankAccount() {
        return companyBankAccount;
    }

    public void setCompanyBankAccount(CompanyBankAccountDTO pCompanyBankAccount) {
        this.companyBankAccount = pCompanyBankAccount;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (serviceCode == null) {
            validationResult.getMessages().InvalidValue(EntityName.ServiceBankAccount, null, "ServiceCode");
        }

        if (companyBankAccount == null) {
            validationResult.getMessages().InvalidValue(EntityName.ServiceBankAccount, null, "CompanyBankAccount");
        } else {
            validationResult.merge(companyBankAccount.validateCompanyBankAccount());
        }

        return validationResult;
    }
}
