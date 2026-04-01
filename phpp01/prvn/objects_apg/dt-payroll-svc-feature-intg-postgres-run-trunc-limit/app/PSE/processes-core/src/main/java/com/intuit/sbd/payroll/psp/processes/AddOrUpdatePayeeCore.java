/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;


/**
 * Core process for adding a new employee.
 *
 * @author Marcela Villani
 */
public class AddOrUpdatePayeeCore extends Process implements IProcess {
    private Company company;

    private PayeeDTO payeeDTO;
    private Payee payee;

    private String sourceCompanyId;
    private SourceSystemCode sourceSystemCd;

    /**
     * Constructor for AddPayeeCore
     *
     * @param pSourceSystemCd  Source System Code
     * @param pSourceCompanyId Source Company ID
     * @param pPayeeDTO        Payee data transfer object to add
     */
    public AddOrUpdatePayeeCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayeeDTO pPayeeDTO) {
        payeeDTO = pPayeeDTO;
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
    }

    /**
     * Obtains the Payee attached to the core process
     *
     * @return Payee domain object
     */
    public Payee getPayee() {
        return payee;
    }

    /**
     * Saves the Payee to the database. The system will first check if the
     * Payee already exists.
     */
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        // Payee doesn't exist, instantiate a new object and set the company.
        // Otherwise update the Payee info with the dto info
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());
        Boolean payeeAddedEvent = false;
        Boolean payeeUpdatedEvent = false;

        if (payee == null) {
            payee = new Payee();
            payee.setCompany(company);
            payeeAddedEvent = true;

        }
        if (!payeeAddedEvent) {
            payeeUpdatedEvent = isPayeeUpdateEvent(payee, payeeDTO);
        }


        //We'll  update the Payee info with the dto info if the Payee already exists
        payee.setName(payeeDTO.getName().trim());
        payee.setSourcePayeeId(payeeDTO.getSourcePayeeId().trim());
        payee.setPhone(payeeDTO.getPhone());
        payee.setEmail(payeeDTO.getEmail());
        payee.setTaxId(payeeDTO.getTaxId());
        payee.setIs1099(payeeDTO.getIs1099());

        if(payeeDTO.getAccountNumber() != null)  {
            payee.setAccountNumber(payeeDTO.getAccountNumber());
        }


        if (payeeDTO.getMailingAddress() != null) {
            payee.setMailingAddress(createDomainAddressFromDTO(payeeDTO.getMailingAddress()));
        }


        // Save the payee
        payee = Application.save(payee);


        processResult.setResult(payee);
        if (processResult.isSuccess()) {
            Application.getSessionCache().addPrimaryKey(payee.getNaturalKey(), payee.getId());
            if(company.isCompanyRequiredForOFACScreening()) {
                if (payeeAddedEvent) {
                    CompanyEvent.createChangePayeeCompanyEvent(company, EventTypeCode.PayeeAdded, payee.getId().toString());
                } else if (payeeUpdatedEvent) {
                    CompanyEvent.createChangePayeeCompanyEvent(company, EventTypeCode.PayeeUpdated, payee.getId().toString());
                }
            }

        }

        return processResult;
    }
    private boolean isPayeeUpdateEvent(Payee pPayee, PayeeDTO payeeDTO) {
       if(!pPayee.getName().equals(payeeDTO.getName()) || !isSameAddress(payeeDTO.getMailingAddress(), pPayee.getMailingAddress())) {
            return true;
        }
        return false;
    }



    public ProcessResult validate
            () {
        // Validate inputs from DTO
        ProcessResult validationResult = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd,
                sourceCompanyId);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }
        // Validate payee DTO
        if (payeeDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.Payee, "PayeeDTO", "PayeeDTO");
            return validationResult;
        }
        validationResult = payeeDTO.validate();
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Validate Company Exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (!company.isAllowedCapability(SystemCapabilityCode.ChangeCompanyInfo)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
            return validationResult;
        }

        if (!company.passesAdditionalCancelTermValidation(false, true, true)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
        }



        return validationResult;
    }


    private Address createDomainAddressFromDTO(AddressDTO pAddressDTO) {
        Address domainAddress = new Address();
        domainAddress.setAddressLine1(pAddressDTO.getAddressLine1());
        domainAddress.setAddressLine2(pAddressDTO.getAddressLine2());
        domainAddress.setAddressLine3(pAddressDTO.getAddressLine3());
        domainAddress.setCity(pAddressDTO.getCity());
        domainAddress.setCountry(pAddressDTO.getCountry());
        domainAddress.setState(pAddressDTO.getState());
        domainAddress.setZipCode(pAddressDTO.getZipCode());
        domainAddress.setZipCodeExtension(pAddressDTO.getZipCodeExtension());
        return Application.save(domainAddress);
    }


    private boolean isSameAddress(AddressDTO payeeDTOMailingAddress, Address payeeMailingAddress) {
        if (payeeDTOMailingAddress == null && payeeMailingAddress == null) {
            return true;
        }
        else if (payeeDTOMailingAddress == null || payeeMailingAddress == null) {
            return false;
        }
        return (isSameString(payeeDTOMailingAddress.getAddressLine1(), payeeMailingAddress.getAddressLine1())
                && isSameString(payeeDTOMailingAddress.getAddressLine2(), payeeMailingAddress.getAddressLine2())
                && isSameString(payeeDTOMailingAddress.getAddressLine3(), payeeMailingAddress.getAddressLine3())
                && isSameString(payeeDTOMailingAddress.getCity(), payeeMailingAddress.getCity())
                && isSameString(payeeDTOMailingAddress.getState(), payeeMailingAddress.getState())
                && isSameString(payeeDTOMailingAddress.getCountry(), payeeMailingAddress.getCountry())
                && isSameString(payeeDTOMailingAddress.getZipCode(), payeeMailingAddress.getZipCode())
                && isSameString(payeeDTOMailingAddress.getZipCodeExtension(), payeeMailingAddress.getZipCodeExtension()));
    }

    private boolean isSameString(String str1, String str2) {
        if(str1==null && str2==null) {
            return true;
        }
        else if(str1==null || str2==null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

}