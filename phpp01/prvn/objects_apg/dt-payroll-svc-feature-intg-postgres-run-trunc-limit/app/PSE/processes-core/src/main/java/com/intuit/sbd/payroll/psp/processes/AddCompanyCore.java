/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddCompanyCore.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */


package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ContactDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdditionalInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.common.utils.RealmLogHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.ProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.common.CompanyRealmValidator;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;


/**
 * Core process for adding a new company.
 *
 * @author Wiktor Kozlik, Dawn Martens
 */
public class AddCompanyCore extends Process implements IProcess {
    public static final SpcfLogger logger = Application.getLogger(AddCompanyCore.class);


    private Company domainCompany;
    private CompanyDTO dtoCompany;
    private CompanyRealmValidator companyRealmValidator;


    public Company getCompany() {
        return domainCompany;
    }

    public AddCompanyCore(CompanyDTO pCompany) {
        dtoCompany = pCompany;
        companyRealmValidator = new CompanyRealmValidator();
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        createDomainCompanyFromDTO();

        //Add Default Funding Model According to SourcePayrollParameter
        SourcePayrollParameter defaultFundingModel = SourcePayrollParameter.findSourcePayrollParameter(domainCompany.getSourceSystemCd(), SourcePayrollParameterCode.DefaultFundingModel);

        FundingModel fundingModel = Application.findById(FundingModel.class, defaultFundingModel != null ? defaultFundingModel.getParameterValue() : FundingModel.Codes.FIVE_DAY);
        domainCompany.setFundingModel(fundingModel);

        //Set the STANDARD offload group, as this is the only one we have for now
        OffloadGroup OffloadGroupDO =
                OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD);
        domainCompany.setOffloadGroup(OffloadGroupDO);

        domainCompany = Application.save(domainCompany);
        Application.save(domainCompany.getQuickbooksInfo());

        if(domainCompany.getCompanyAdditionalInfo() != null){
            Application.save(domainCompany.getCompanyAdditionalInfo());
        }

        // todo v1.1: Moved to AddServiceCore (may need to move back here if email unintentionally sent from add service...)
        //CompanyEventBE.addCustomerSignedUpEvent(domainCompany);

        processResult.setResult(domainCompany);

        if (dtoCompany.getTaxExemptExpirationDate() != null) {
            domainCompany.setTaxExemptExpirationDate(DateDTO.convertToSpcfCalendar(dtoCompany.getTaxExemptExpirationDate()));
            CompanyEvent.createCompanyEvent(domainCompany, EventTypeCode.TaxExemptStatusChanged);
        }
        if (dtoCompany.getTaxExemptStatus() != null && !dtoCompany.getTaxExemptStatus().equals(domainCompany.getTaxExemptStatus())) {
            domainCompany.setTaxExemptStatus(dtoCompany.getTaxExemptStatus());
            CompanyEvent.createCompanyEvent(domainCompany, EventTypeCode.TaxExemptStatusChanged);
        }
        domainCompany = Application.save(domainCompany);

        if (StringUtils.isNotBlank(domainCompany.getIAMRealmId())) {
            CompanyEvent.createCompanyInfoChangeEvent(domainCompany, "NOT AVAILABLE",
                    dtoCompany.getIAMRealmId(), EventTypeCode.RealmIdAdded);
        }

        if (processResult.isSuccess()) {
            Application.getSessionCache().addPrimaryKey(domainCompany.getNaturalKey(), domainCompany.getId());
        }


        return processResult;
    }

    private void createDomainCompanyFromDTO() {
        domainCompany = new Company();

        domainCompany.setSourceCompanyId(dtoCompany.getCompanyId());
        domainCompany.setSourceSystemCd(dtoCompany.getSourceSystemCd());
        domainCompany.setFedTaxId(dtoCompany.getFein());
        domainCompany.setLegalName(dtoCompany.getLegalName());
        domainCompany.setNameControl(dtoCompany.getNameControl());
        domainCompany.setDbaName(dtoCompany.getDBA());
        //setting realmId in domain company
        String log;
        domainCompany.setIAMRealmId(dtoCompany.getIAMRealmId());
        if(dtoCompany.getIAMRealmId()!=null) {
            log = RealmLogHelper.getRealmEventMessage(RealmLogHelper.COMPANY_REALM_ADD, dtoCompany.getIAMRealmId(), null, domainCompany, "added from companyRealm");
            Application.printStackTrace(log);
        }
        domainCompany.setPhone(dtoCompany.getPhone());
        domainCompany.setCompanyAdditionalInfo(createCompanyAdditionalInfoFromDTO(dtoCompany.getCompanyAdditionalInfo()));
        domainCompany.setLegalAddress(createDomainAddressFromDTO(dtoCompany.getLegalAddress()));
        domainCompany.setMailingAddress(createDomainAddressFromDTO(dtoCompany.getMailingAddress()));

        domainCompany.setNotificationEmail(dtoCompany.getNotificationEmail());

        PayrollFrequency payrollFrequency = ProcessesToDTO
                .getDomainPayrollFrequency(dtoCompany.getPayrollFrequencyCd());
        domainCompany.setPayrollFrequency(payrollFrequency);

        for (ContactDTO currContact : dtoCompany.getContacts()) {
            domainCompany.addContact(createDomainContactFromDTO(currContact));
        }

        domainCompany.setNextEmployeeId(dtoCompany.getNextEmployeeId());
        domainCompany.setNextPaycheckId(dtoCompany.getNextPaycheckId());
        domainCompany.setNextPayrollItemId(dtoCompany.getNextPayrollItemId());
        domainCompany.setNextPayrollTransactionId(dtoCompany.getNextPayrollTransactionId());

        QuickbooksInfo qbInfo = new QuickbooksInfo();
        if (dtoCompany.getQuickBooksInfo() != null) {
            qbInfo.setApplicationId(dtoCompany.getQuickBooksInfo().getApplicationId());
            qbInfo.setApplicationVersion(dtoCompany.getQuickBooksInfo().getApplicationVersion());
            qbInfo.setQuickbooksSku(dtoCompany.getQuickBooksInfo().getQuickbooksSku());
            qbInfo.setTaxTableId(dtoCompany.getQuickBooksInfo().getTaxTableId());
            qbInfo.setLicenseNumber(dtoCompany.getQuickBooksInfo().getLicenseNumber());
            qbInfo.setCoaFeeAccountName(dtoCompany.getQuickBooksInfo().getCoaFeeAccountName());
            qbInfo.setCoaSalesTaxAccountName(dtoCompany.getQuickBooksInfo().getCoaSalesTaxAccountName());
            qbInfo.setAS400PayrollCount(dtoCompany.getQuickBooksInfo().getPayrollCount());
            //Add qbInfo Realm Id
            qbInfo.setIAMRealmId(dtoCompany.getQuickBooksInfo().getIAMRealmId());
            if(dtoCompany.getQuickBooksInfo().getIAMRealmId()!=null) {
                log= RealmLogHelper.getRealmEventMessage(RealmLogHelper.QB_REALM_ADD, dtoCompany.getQuickBooksInfo().getIAMRealmId(), null, domainCompany, null);
                Application.printStackTrace(log);
            }
             //if companyRealmId was null, then update using QBRealm
            if (domainCompany.getIAMRealmId() == null) {
                if(dtoCompany.getQuickBooksInfo().getIAMRealmId()!=null) {
                    log= RealmLogHelper.getRealmEventMessage(RealmLogHelper.COMPANY_REALM_ADD, dtoCompany.getQuickBooksInfo().getIAMRealmId(), null, domainCompany, "added from QBRealm");
                    Application.printStackTrace(log);
                }
                domainCompany.setIAMRealmId(dtoCompany.getQuickBooksInfo().getIAMRealmId());
            }
        }

        qbInfo.setCompany(domainCompany);
        domainCompany.setQuickbooksInfo(qbInfo);

        if (dtoCompany.getSignUpDate() != null) {
            domainCompany.setSignUpDate(DateDTO.convertToSpcfCalendar(dtoCompany.getSignUpDate()));
        } else {
            domainCompany.setSignUpDate(PSPDate.getPSPTime());
        }

        if (dtoCompany.getCurrentToken() != null) {
            domainCompany.setCurrentToken(dtoCompany.getCurrentToken());
        }
        
        domainCompany.setPriceType(dtoCompany.getPriceType());
    }

    private CompanyAdditionalInfo createCompanyAdditionalInfoFromDTO(CompanyAdditionalInfoDTO pCompanyAdditionalInfoDTO){
        CompanyAdditionalInfo domainCompanyAdditionalInfo = null;
        if(pCompanyAdditionalInfoDTO != null){
            domainCompanyAdditionalInfo = new CompanyAdditionalInfo();
            domainCompanyAdditionalInfo.setCompany(domainCompany);
            if(pCompanyAdditionalInfoDTO.getIndustry() != null){
                domainCompanyAdditionalInfo.setIndustryType(IndustryType.findIndustryType(pCompanyAdditionalInfoDTO.getIndustry()));
            }
            if(pCompanyAdditionalInfoDTO.getOwnership() != null) {
                logger.info("OwnershipType updated to: " + pCompanyAdditionalInfoDTO.getOwnership() + " for company with PSID: " + dtoCompany.getCompanyId());
                domainCompanyAdditionalInfo.setOwnershipType(OwnershipType.findOwnershipType(pCompanyAdditionalInfoDTO.getOwnership()));
            }
        }
        return domainCompanyAdditionalInfo;
    }

    private Contact createDomainContactFromDTO(ContactDTO pContactDTO) {
        if (pContactDTO != null) {
            Contact domainContact = new Contact();
            domainContact.setCompany(domainCompany);

            ContactRole domainContactRole = ProcessesToDTO.getDomainContactRole(pContactDTO.getContactRoleCd());

            domainContact.setContactRoleCd(domainContactRole);
            domainContact.setAuthSignerYnInd(pContactDTO.getAccountSignatory());
            if (pContactDTO.getCommunicationTypeCd() != null) {
                if (CommunicationType.Phone
                        .equals(pContactDTO.getCommunicationTypeCd())) {
                    domainContact
                            .setCommunicationTypePreference(CommunicationType.Phone);
                } else {
                    domainContact
                            .setCommunicationTypePreference(CommunicationType.Email);
                }
            }
            domainContact.setEmail(pContactDTO.getEmail());
            domainContact.setFirstName(pContactDTO.getFirstName());
            //todo:v2 move gender from individual to employee only, as it doesn't belong on a contact
            domainContact.setGenderCd(null);
            domainContact.setLastName(pContactDTO.getLastName());
            if (pContactDTO.getAddress() != null) {
                domainContact.setMailingAddress(createDomainAddressFromDTO(pContactDTO.getAddress()));
            }
            domainContact.setMiddleName(pContactDTO.getMiddleName());
            domainContact.setPhone(pContactDTO.getPhoneNumber());
            domainContact.setTitle(pContactDTO.getTitle());
            domainContact.setSuffix(pContactDTO.getTitleSuffix());
            domainContact.setFax(pContactDTO.getFaxNumber());
            domainContact.setJobTitle(pContactDTO.getJobTitle());
            domainContact.setSecondPhone(pContactDTO.getSecondPhoneNumber());
            domainContact.setIAMAuthenticationId(pContactDTO.getIAMAuthenticationId());
            domainContact.setSourceContactId(pContactDTO.getContactId());
            domainContact.setSocialSecurityNumberPlainText(pContactDTO.getSocialSecurityNumber());
            if(pContactDTO.getDateOfBirth() != null){
                domainContact.setDateOfBirth(DateDTO.convertToSpcfCalendar(pContactDTO.getDateOfBirth()));
            }
            return domainContact;
        } else {
            return null;
        }
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


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Ensure DTO is not null
        if (dtoCompany == null) {
            validationResult.getMessages().CompanyNotSpecified(EntityName.Company, null);
            return validationResult;
        }

        //Validate DTO
        ProcessResult validateCompanyResult = dtoCompany.validateCompanyDTO();
        validationResult.merge(validateCompanyResult);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company does not already exist
        Company alreadyExistingCompany = Company
                .findCompany(dtoCompany.getCompanyId(), dtoCompany.getSourceSystemCd());
        if (alreadyExistingCompany != null) {
            validationResult.getMessages().CompanyAlreadyExists(EntityName.Company, dtoCompany.getCompanyId(),
                    dtoCompany.getSourceSystemCd().toString(), dtoCompany.getCompanyId());
            return validationResult;
        }

        // Validate account signatory
        if (!dtoCompany.hasAccountSignatoryContact()) {
            validationResult.getMessages().NoAccountSignatory(EntityName.Company, dtoCompany.getCompanyId(),
                    dtoCompany.getSourceSystemCd().toString(), dtoCompany.getCompanyId());
        }

        // Validate NameControl
        // if NameControl is not null then
        // 1. Should have whitespaces only at the end
        // 2. Should not be of length greater than 4
        // 3. Should not have any special characters other than - and &

        String nameControl = dtoCompany.getNameControl();
        if(nameControl!= null && !nameControl.isEmpty()){
            if(!nameControl.matches("[A-Z,a-z,0-9,/&,/-]{1,4}")){
                validationResult.getMessages().InvalidNameControlValue(EntityName.Company, dtoCompany.getCompanyId(),
                   dtoCompany.getSourceSystemCd().toString(), dtoCompany.getCompanyId(), nameControl);
            }
        }

        validationResult.merge(companyRealmValidator.validate(CompanyRealmValidator.CompanyCoreEventType.COMPANY_ADD, dtoCompany));

        return validationResult;
    }
}
