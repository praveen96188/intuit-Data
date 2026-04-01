/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/PayeeDTO.java#1 $
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
public class PayeeDTO {
    private String sourcePayeeId;
    private String name;
    private String taxId;
    private String email;
    private String phone;
    private boolean is1099;
    private AddressDTO mailingAddress;
    private String accountNumber;


    public String getSourcePayeeId() {
        return sourcePayeeId;
    }

    public void setSourcePayeeId(String sourcePayeeId) {
        this.sourcePayeeId = sourcePayeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean getIs1099() {
        return is1099;
    }

    public void setIs1099(boolean is1099) {
        this.is1099 = is1099;
    }

    public AddressDTO getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(AddressDTO mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //  Validate the employee id
        if ( (sourcePayeeId == null) || (sourcePayeeId.trim().equals(""))) {
            validationResult.getMessages().EmployeeIdNotSpecified(EntityName.Employee, null);
        } else if (!Validator.isValidLength(sourcePayeeId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.Payee, sourcePayeeId, "PayeeId");
        }

        if (name== null ||
                !(Validator.isValidLength(name, 1, 300))) {
            validationResult.getMessages().InvalidValue(EntityName.Payee, sourcePayeeId, "Name");
        }

        if (taxId != null && !Validator.isMatchingPattern(taxId.replaceAll("-", ""), "^[0-9]{9}$")) {
            validationResult.getMessages().InvalidValue(EntityName.Payee, sourcePayeeId, "taxId");
        }

        if (!Validator.isValidLength(email, 0, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.Payee, sourcePayeeId, "Email");
        }


        if (!Validator.isValidLength(phone, 0, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.Payee, sourcePayeeId, "Phone");
        }

        if(!Validator.isValidLength(accountNumber,0,150)){
            validationResult.getMessages().InvalidValue(EntityName.Payee, sourcePayeeId, "AccountNumber");
        }

        if(mailingAddress != null) {
            validationResult.merge(mailingAddress.validateAddressDTO());
        }

        return validationResult;

    }
}