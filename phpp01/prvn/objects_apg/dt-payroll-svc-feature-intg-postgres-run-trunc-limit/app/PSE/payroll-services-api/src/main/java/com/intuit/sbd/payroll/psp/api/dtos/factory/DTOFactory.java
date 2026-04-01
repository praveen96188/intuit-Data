/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/factory/DTOFactory.java#12 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos.factory;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wiktor Kozlik
 */
public class DTOFactory implements IDTOFactory {

    private static SpcfLogger logger = PayrollServices.getLogger(DTOFactory.class);

    public AddressDTO create(final Address pAddress) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1(pAddress.getAddressLine1());
        addressDTO.setAddressLine2(pAddress.getAddressLine2());
        addressDTO.setAddressLine3(pAddress.getAddressLine3());
        addressDTO.setCity(pAddress.getCity());
        addressDTO.setCountry(pAddress.getCountry());
        addressDTO.setState(pAddress.getState());
        addressDTO.setZipCode(pAddress.getZipCode());
        addressDTO.setZipCodeExtension(pAddress.getZipCodeExtension());
        return addressDTO;
    }

    public BankAccountDTO create(final BankAccount pBankAccount) {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber(pBankAccount.getAccountNumber());
        bankAccountDTO.setAccountType(BankAccountType.valueOf(pBankAccount.getAccountTypeCd().toString()));
        bankAccountDTO.setAchAccountType(ACHBankAccountType.valueOf(pBankAccount.getACHAccountTypeCd().toString()));
        bankAccountDTO.setBankName(pBankAccount.getBankName());
        bankAccountDTO.setRoutingNumber(pBankAccount.getRoutingNumber());
        return bankAccountDTO;
    }

    public CompanyBankAccountDTO create(final CompanyBankAccount pCompanyBankAccount) {
        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();

        companyBankAccountDTO.setBankAccountDTO(create(pCompanyBankAccount.getBankAccount()));
        companyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccount.getSourceBankAccountId());
        companyBankAccountDTO.setSourceBankAccountName(pCompanyBankAccount.getSourceBankAccountName());

        return companyBankAccountDTO;
    }

    public CompanyDTO create(final Company pCompany) {
        CompanyDTO companyDTO = new CompanyDTO();

        for (Contact contact : pCompany.getContactCollection()) {
            ContactDTO contactDTO = create(contact);
            companyDTO.getContacts().add(contactDTO);
        }

        companyDTO.setDBA(pCompany.getDbaName());
        companyDTO.setFein(pCompany.getFedTaxId());

        AddressDTO legalAddressDTO = create(pCompany.getLegalAddress());
        companyDTO.setLegalAddress(legalAddressDTO);

        AddressDTO mailAddressDTO = create(pCompany.getMailingAddress());
        companyDTO.setMailingAddress(mailAddressDTO);

        Address complianceAddress = pCompany.getComplianceAddress();
        // Compliance Address may be null
        if(complianceAddress != null) {
            AddressDTO complianceAddressDTO = create(complianceAddress);
            companyDTO.setComplianceAddress(complianceAddressDTO);
        }

        companyDTO.setLegalName(pCompany.getLegalName());
        companyDTO.setNotificationEmail(pCompany.getNotificationEmail());
        companyDTO.setNameControl(pCompany.getNameControl());

        PayrollFrequency payrollFreq = pCompany.getPayrollFrequency();
        if (payrollFreq != null) {
            PayrollFrequencyDTO payrollFreqDTO = create(payrollFreq);
            companyDTO.setPayrollFrequencyCd(payrollFreqDTO);
        }
        companyDTO.setCompanyId(pCompany.getSourceCompanyId());
        companyDTO.setSourceSystemCd(pCompany.getSourceSystemCd());
        companyDTO.setNextEmployeeId(pCompany.getNextEmployeeId());
        companyDTO.setNextPaycheckId(pCompany.getNextPaycheckId());
        companyDTO.setNextPayrollItemId(pCompany.getNextPayrollItemId());
        companyDTO.setNextPayrollTransactionId(pCompany.getNextPayrollTransactionId());
        companyDTO.setDebugLogging(pCompany.getDebugLogging());
        companyDTO.setTaxExemptStatus(pCompany.getTaxExemptStatus());

        if (pCompany.getTaxExemptExpirationDate() != null) {
            companyDTO.setTaxExemptExpirationDate(new DateDTO(pCompany.getTaxExemptExpirationDate().toLocal()));
        }

        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();

        if (pCompany.getQuickbooksInfo() != null) {
            qbInfoDTO.setApplicationId(pCompany.getQuickbooksInfo().getApplicationId());
            qbInfoDTO.setApplicationVersion(pCompany.getQuickbooksInfo().getApplicationVersion());
            qbInfoDTO.setQuickbooksSku(pCompany.getQuickbooksInfo().getQuickbooksSku());
            qbInfoDTO.setTaxTableId(pCompany.getQuickbooksInfo().getTaxTableId());
            qbInfoDTO.setLicenseNumber(pCompany.getQuickbooksInfo().getLicenseNumber());
            qbInfoDTO.setCoaFeeAccountName(pCompany.getQuickbooksInfo().getCoaFeeAccountName());
            qbInfoDTO.setCoaSalesTaxAccountName(pCompany.getQuickbooksInfo().getCoaSalesTaxAccountName());
            qbInfoDTO.setFileId(pCompany.getQuickbooksInfo().getFileId());
            qbInfoDTO.setProcessTransmissions(pCompany.getQuickbooksInfo().getProcessTransmissions());
            qbInfoDTO.setAllowTransmissions(pCompany.getQuickbooksInfo().getAllowTransmissions());
        }
        companyDTO.setQuickBooksInfo(qbInfoDTO);

        companyDTO.setDebugLogging(pCompany.getDebugLogging());
        companyDTO.setIAMRealmId(pCompany.getIAMRealmId());

        companyDTO.setPriceType(pCompany.getPriceType());

        return companyDTO;
    }

    public ContactDTO create(final Contact pContact) {
        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setAccountSignatory(pContact.getAuthSignerYnInd());
        if (pContact.getMailingAddress() != null) {
            AddressDTO addressDTO = create(pContact.getMailingAddress());
            contactDTO.setAddress(addressDTO);
        }

        CommunicationType communicationType = CommunicationType.valueOf(pContact.getCommunicationTypePreference().name());
        contactDTO.setCommunicationTypeCd(communicationType);
        ContactRole contactRole = ContactRole.valueOf(pContact.getContactRoleCd().name());
        contactDTO.setContactRoleCd(contactRole);
        contactDTO.setEmail(pContact.getEmail());
        contactDTO.setFirstName(pContact.getFirstName());
        contactDTO.setLastName(pContact.getLastName());
        contactDTO.setMiddleName(pContact.getMiddleName());
        contactDTO.setPhoneNumber(pContact.getPhone());
        contactDTO.setContactId(pContact.getSourceContactId());

        contactDTO.setTitle(pContact.getTitle());
        contactDTO.setTitleSuffix(pContact.getSuffix());
        contactDTO.setFaxNumber(pContact.getFax());
        contactDTO.setJobTitle(pContact.getJobTitle());
        contactDTO.setIAMAuthenticationId(pContact.getIAMAuthenticationId());

        if(pContact.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)){
            contactDTO.setSocialSecurityNumber(pContact.getSocialSecurityNumberPlainText());

            if (pContact.getDateOfBirth() != null) {
                contactDTO.setDateOfBirth(new DateDTO(pContact.getDateOfBirth()));
            }

        }
        return contactDTO;
    }

    public DDServiceInfoDTO create(final DDCompanyServiceInfo pDDCompanyServiceInfo) {
        throw new UnsupportedOperationException(tempErrorMsg);
    }

    public EmployeeBankAccountDTO create(final EmployeeBankAccount pEmployeeBankAccount) {
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        employeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccount.getSourceBankAccountId());
        employeeBankAccountDTO.setAmount(pEmployeeBankAccount.getAmount());
        employeeBankAccountDTO.setAmountType(pEmployeeBankAccount.getAmountType());
        employeeBankAccountDTO.setOrder(pEmployeeBankAccount.getAccountOrder());
        employeeBankAccountDTO.setBankAccount(create(pEmployeeBankAccount.getBankAccount()));
        return employeeBankAccountDTO;
    }

    public PayeeBankAccountDTO create(final PayeeBankAccount pPayeeBankAccount) {
        PayeeBankAccountDTO payeeBankAccountDTO = new PayeeBankAccountDTO();
        payeeBankAccountDTO.setPayeeBankAccountId(pPayeeBankAccount.getSourceBankAccountId());
        payeeBankAccountDTO.setBankAccount(create(pPayeeBankAccount.getBankAccount()));
        return payeeBankAccountDTO;
    }

    public EmployeeDTO create(final Employee pEmployee) {
        return create(pEmployee, true);
    }

    public EmployeeDTO create(final Employee pEmployee, boolean pLoadQBDTCollections) {
        EmployeeDTO employeeDTO = new EmployeeDTO();

        employeeDTO.setEmployeeId(pEmployee.getSourceEmployeeId());
        employeeDTO.setSocialSecurityNumber(pEmployee.getTaxId());
        // todo why wasn't this here. employeeDTO.setExistingEmployeeGuid(pEmployee.getId().toString());

        employeeDTO.setFirstName(pEmployee.getFirstName());
        employeeDTO.setMiddleName(pEmployee.getMiddleName());
        employeeDTO.setLastName(pEmployee.getLastName());
        employeeDTO.setSuffix(pEmployee.getSuffix());

        if (pEmployee.getMailingAddress() != null) {
            employeeDTO.setLiveAddress(create(pEmployee.getMailingAddress()));
        }

        employeeDTO.setFedAllowances(pEmployee.getFedAllowances());
        employeeDTO.setFedFilingStatus(pEmployee.getFedFilingStatus());
        employeeDTO.setFedExtraWithholding(pEmployee.getFedExtraWithholding());
        employeeDTO.setFedClaimDependents(pEmployee.getFedClaimDependents());
        employeeDTO.setFedOtherIncome(pEmployee.getFedOtherIncome());
        employeeDTO.setFedDeduction(pEmployee.getFedDeductions());
        employeeDTO.setFedMultipleJobs(pEmployee.getFedMultipleJobs());
        employeeDTO.setFedW4EmployeePref(pEmployee.getFedW4EmployeePref());

        employeeDTO.setHasRetirementPlan(pEmployee.getHasRetirementPlan());
        employeeDTO.setHasThirdPartySickPay(pEmployee.getHasThirdPartySickPay());
        employeeDTO.setStatutory(pEmployee.getIsStatutory());
        employeeDTO.setDeceased(pEmployee.getIsDeceased());
        employeeDTO.setPayPeriod(pEmployee.getPayPeriod());
        employeeDTO.setQualifiesForAEIC(pEmployee.getQualifiesForAeic());
        employeeDTO.setPhoneNumber(pEmployee.getPhone());
        employeeDTO.setEmail(pEmployee.getEmail());
        employeeDTO.setStatusCd(pEmployee.getStatusCd());
        employeeDTO.setGender(pEmployee.getGenderCd());

        employeeDTO.setWorkState(pEmployee.getWorkState());
        employeeDTO.setLiveState(pEmployee.getLiveState());

        if (pEmployee.getBirthDate() != null) {
            employeeDTO.setBirthDate(new DateDTO(pEmployee.getBirthDate()));
        }
        if (pEmployee.getHireDate() != null) {
            employeeDTO.setHireDate(new DateDTO(pEmployee.getHireDate()));
        }
        if (pEmployee.getTerminationDate() != null) {
            employeeDTO.setTerminationDate(new DateDTO(pEmployee.getTerminationDate()));
        }

        if (pEmployee.getThirdParty401kInfo() != null) {
            ThirdParty401kEmployeeInfoDTO employee401kDTO = new ThirdParty401kEmployeeInfoDTO();
            employee401kDTO.setEmail(pEmployee.getEmail());
            employee401kDTO.setFamilyMember(pEmployee.getThirdParty401kInfo().getIsFamilyMember());
            employee401kDTO.setHighlyCompensatedEmployee(pEmployee.getThirdParty401kInfo().getIsHighlyCompensated());
            employee401kDTO.setOwnershipPercent(new BigDecimal(pEmployee.getThirdParty401kInfo().getOwnershipPercentage()));
            employee401kDTO.setPhoneNumber(pEmployee.getPhone());
            employeeDTO.setEmployee401kInfo(employee401kDTO);
        }

        DomainEntitySet<EmployeeAccrual> employeeAccruals = pEmployee.getEmployeeAccrualCollection();
        for (EmployeeAccrual employeeAccrual : employeeAccruals) {
            EmployeeAccrualDTO employeeAccrualDTO = new EmployeeAccrualDTO();
            employeeAccrualDTO.setAccrualPeriod(employeeAccrual.getAccrualPeriod());
            employeeAccrualDTO.setAccrualType(employeeAccrual.getAccrualType());
            employeeAccrualDTO.setHours(employeeAccrual.getHours());
            employeeAccrualDTO.setHoursPerPeriod(employeeAccrual.getHoursPerPeriod());
            employeeAccrualDTO.setMaxHours(employeeAccrual.getMaxHours());
            employeeAccrualDTO.setNewYearReset(employeeAccrual.getNewYearReset());
            employeeDTO.getEmployeeAccrualDTOs().add(employeeAccrualDTO);
        }

        if(pLoadQBDTCollections) {
            DomainEntitySet<EmployeeCustomField> employeeCustomFields = pEmployee.getEmployeeCustomFieldCollection();
            for (EmployeeCustomField employeeCustomField : employeeCustomFields) {
                EmployeeCustomFieldDTO employeeCustomFieldDTO = new EmployeeCustomFieldDTO();
                employeeCustomFieldDTO.setName(employeeCustomField.getName());
                employeeCustomFieldDTO.setValue(employeeCustomField.getValue());
                employeeCustomFieldDTO.setOrder(employeeCustomField.getFieldOrder());
                employeeDTO.getEmployeeCustomFields().add(employeeCustomFieldDTO);
            }

            DomainEntitySet<EmployeePayrollItem> employeePayrollItems = pEmployee.getEmployeePayrollItemCollection();
            for (EmployeePayrollItem employeePayrollItem : employeePayrollItems) {
                EmployeePayrollItemDTO employeePayrollItemDTO = new EmployeePayrollItemDTO();
                employeePayrollItemDTO.setAmount(employeePayrollItem.getAmount());
                employeePayrollItemDTO.setAmountType(employeePayrollItem.getAmountType());
                employeePayrollItemDTO.setOrder(employeePayrollItem.getItemOrder());
                employeePayrollItemDTO.setItemLimit(employeePayrollItem.getItemLimit());
                employeePayrollItemDTO.setLimitType(employeePayrollItem.getLimitType());
                employeePayrollItemDTO.setPaylineType(employeePayrollItem.getType());
                employeePayrollItemDTO.setPayrollItemId(employeePayrollItem.getCompanyPayrollItem().getSourcePayrollItemId());
                employeeDTO.getEmployeePayrollItemDTOs().add(employeePayrollItemDTO);
            }

            DomainEntitySet<EmployeeTax> employeeTaxes = pEmployee.getEmployeeTaxCollection();
            for (EmployeeTax employeeTax : employeeTaxes) {
                EmployeeTaxDTO employeeTaxDTO = new EmployeeTaxDTO();
                if(employeeTax.getCompanyLaw() != null) {
                    employeeTaxDTO.setCompanyLawId(employeeTax.getCompanyLaw().getSourceId());
                }
                employeeTaxDTO.setState(employeeTax.getState());
                employeeTaxDTO.setSubjectTo(employeeTax.getSubjectTo());
                employeeTaxDTO.setTaxLawVersion(employeeTax.getTaxLawVersion());
                employeeTaxDTO.setTaxType(employeeTax.getTaxType());
                employeeTaxDTO.setW2Name(employeeTax.getW2Name());
                employeeTaxDTO.setFilingStatus(employeeTax.getFilingStatus());
                employeeTaxDTO.setAllowances(employeeTax.getAllowances());
                employeeTaxDTO.setExtraWithholding(employeeTax.getExtraWithholding());
                employeeTaxDTO.setExtraWithholdingType(employeeTax.getExtraWithholdingType());
                employeeTaxDTO.setOrder(employeeTax.getTaxOrder());
                //Add Tax Table Misc Data
                if (employeeTax.getTaxTableMiscDataCollection() != null && employeeTax.getTaxTableMiscDataCollection().size() > 0) {
                    Map<Integer,String> miscDataMap = new HashMap<Integer,String>();
                    for (TaxTableMiscData taxTableMiscData : employeeTax.getTaxTableMiscDataCollection()) {
                        miscDataMap.put(taxTableMiscData.getMiscDataOrder(),taxTableMiscData.getValue());
                    }
                    employeeTaxDTO.setTaxTableMiscData(miscDataMap);
                }
                employeeDTO.getEmployeeTaxDTOs().add(employeeTaxDTO);
            }

            DomainEntitySet<EmployeeWagePlan> employeeWagePlans = pEmployee.getEmployeeWagePlanCollection();
            for (EmployeeWagePlan employeeWagePlan : employeeWagePlans) {
                employeeDTO.getWagePlanDTOs().add(create(employeeWagePlan));
            }
        }

        QbdtEmployeeInfo qbdtEmployeeInfo = pEmployee.getQbdtEmployeeInfo();
        if(qbdtEmployeeInfo != null) {
            QBDTEmployeeInfoDTO qbdtEmployeeInfoDTO = new QBDTEmployeeInfoDTO();
            qbdtEmployeeInfoDTO.setAltPhone(qbdtEmployeeInfo.getAltPhone());
            qbdtEmployeeInfoDTO.setBillPayAccount(qbdtEmployeeInfo.getBillPayAccount());
            qbdtEmployeeInfoDTO.setIsDeleted(qbdtEmployeeInfo.getIsDeleted());
            qbdtEmployeeInfoDTO.setEnforceSubjectTo(qbdtEmployeeInfo.getEnforceSubjectTo());
            qbdtEmployeeInfoDTO.setInitials(qbdtEmployeeInfo.getInitials());
            qbdtEmployeeInfoDTO.setPrintAsName(qbdtEmployeeInfo.getPrintAsName());
            qbdtEmployeeInfoDTO.setQBDTEmployeeType(qbdtEmployeeInfo.getEmployeeType());
            qbdtEmployeeInfoDTO.setTitle(qbdtEmployeeInfo.getTitle());
            qbdtEmployeeInfoDTO.setTrackingClass(qbdtEmployeeInfo.getTrackingClass());
            qbdtEmployeeInfoDTO.setUseDD(qbdtEmployeeInfo.getUseDD());
            qbdtEmployeeInfoDTO.setUseTime(qbdtEmployeeInfo.getUseTime());
            qbdtEmployeeInfoDTO.setListId(qbdtEmployeeInfo.getListId());
            qbdtEmployeeInfoDTO.setIsAssisted(qbdtEmployeeInfo.getIsAssisted());
            qbdtEmployeeInfoDTO.setIsSeasonal(qbdtEmployeeInfo.getEmployeeSeasonal());
            employeeDTO.setQBDTEmployeeInfoDTO(qbdtEmployeeInfoDTO);
        }

        return employeeDTO;
    }

    public WagePlanDTO create(final EmployeeWagePlan pEmployeeWagePlan){
        WagePlanDTO wagePlanDTO = new WagePlanDTO();
        wagePlanDTO.setName(pEmployeeWagePlan.getName());
        wagePlanDTO.setDescription(pEmployeeWagePlan.getDescription());
        wagePlanDTO.setRulesVersion(pEmployeeWagePlan.getRulesVersion());
        wagePlanDTO.setState(pEmployeeWagePlan.getState());
        wagePlanDTO.setWagePlanValue(pEmployeeWagePlan.getWagePlanValue());
        wagePlanDTO.setDomainCode(pEmployeeWagePlan.getWagePlanDomain());

        return wagePlanDTO;
    }

    public OfferDTO create(final Offer pOffer) {
        OfferDTO dto = new OfferDTO();
        dto.setId(pOffer.getId().toString());
        dto.setOfferCd(pOffer.getOfferCd());
        dto.setName(pOffer.getName());
        dto.setDescription(pOffer.getDescription());
        dto.setIsApproved(pOffer.getIsApproved());
        dto.setDiscountType(pOffer.getDiscountType());
        dto.setDiscountAmount(SpcfUtils.convertToBigDecimal(pOffer.getDiscountAmount()));
        dto.setDiscountPercent(BigDecimal.valueOf(pOffer.getDiscountPercent()));
        dto.setBeginEvent(pOffer.getBeginEvent());
        dto.setEndEvent(pOffer.getEndEvent());
        dto.setEndDate(CalendarUtils.convertToCalendar(pOffer.getEndDate().toLocal()));
        dto.setDurationDays(pOffer.getDurationDays());
        dto.setUsagesAllowed(pOffer.getUsagesAllowed());
        for (OfferingServiceCharge osc : pOffer.getOfferingServiceChargeCollection()) {
            dto.getServiceChargeIds().add(osc.getId().toString());
        }

        return dto;
    }

    public OfferingServiceChargeDTO create(final OfferingServiceCharge pCharge) {
        OfferingServiceChargeDTO dto = new OfferingServiceChargeDTO();
        dto.setId(pCharge.getId().toString());
        dto.setGroupId(pCharge.getOfferingServiceChargeGroup().getId().toString());
        dto.setSKU(pCharge.getSKU());
        dto.setIsPriceTier(pCharge.getIsTier());
        if (dto.getIsPriceTier()) {
            dto.setTierNumber(pCharge.getTierNumber());
            dto.setTierUnits(pCharge.getTierUnits());
        }

        return dto;
    }

    public OfferingServiceChargeGroupDTO create(final OfferingServiceChargeGroup pGroup) {
        OfferingServiceChargeGroupDTO dto = new OfferingServiceChargeGroupDTO();
        dto.setId(pGroup.getId().toString());
        dto.setOfferingId(pGroup.getOffering().getId().toString());
        dto.setName(pGroup.getName());
        dto.setDescription(pGroup.getDescription());
        dto.setAppliesTo(pGroup.getAppliesTo());

        return dto;
    }

    public OfferingServiceChargePriceDTO create(final OfferingServiceChargePrice pPrice) {
        OfferingServiceChargePriceDTO dto = new OfferingServiceChargePriceDTO();

        dto.setId(pPrice.getId().toString());
        dto.setChargeId(pPrice.getOfferingServiceCharge().getId().toString());
        dto.setBasePrice(SpcfUtils.convertToBigDecimal(pPrice.getBasePrice()));
        dto.setUnitPrice(SpcfUtils.convertToBigDecimal(pPrice.getUnitPrice()));
        dto.setEffectiveDate(CalendarUtils.convertToCalendar(pPrice.getEffectiveDate().toLocal()));

        return dto;
    }

    public PayrollFrequencyDTO create(final PayrollFrequency pPayrollFreq) {
        String code = pPayrollFreq.getPayrollFreqCd();
        if (code == null) {
            return null;
        }

        if (PayrollFrequency.Codes.DAILY_MISC.equals(code)) {
            return PayrollFrequencyDTO.Daily;
        } else if (PayrollFrequency.Codes.WEEKLY.equals(code)) {
            return PayrollFrequencyDTO.Weekly;
        } else if (PayrollFrequency.Codes.BI_WEEKLY.equals(code)) {
            return PayrollFrequencyDTO.BiWeekly;
        } else if (PayrollFrequency.Codes.SEMI_MONTHLY.equals(code)) {
            return PayrollFrequencyDTO.SemiMonthly;
        } else if (PayrollFrequency.Codes.MONTHLY.equals(code)) {
            return PayrollFrequencyDTO.Monthly;
        } else if (PayrollFrequency.Codes.QUARTERLY.equals(code)) {
            return PayrollFrequencyDTO.Quarterly;
        } else if (PayrollFrequency.Codes.SEMI_ANNUALLY.equals(code)) {
            return PayrollFrequencyDTO.SemiAnnual;
        } else if (PayrollFrequency.Codes.ANNUAL.equals(code)) {
            return PayrollFrequencyDTO.Annual;
        } else {
            throw new RuntimeException("Unexpected payroll frequency code \""+code+"\"");
        }
    }

    public CompensationTransactionDTO create(final Compensation pCompensation) {
        CompensationTransactionDTO compensationDTO = new CompensationTransactionDTO();
        compensationDTO.setSourcePayrollItemId(pCompensation.getCompanyPayrollItem().getSourcePayrollItemId());
        compensationDTO.setCompensationAmount(pCompensation.getCompensationAmount());
        compensationDTO.setHoursWorked(SpcfDecimal.createInstance(pCompensation.getHoursWorked()));
        return compensationDTO;
    }

    public PaycheckDTO create(final Paycheck pPaycheck) {
        PaycheckDTO paycheckDTO = new PaycheckDTO();
        paycheckDTO.setPaycheckId(pPaycheck.getSourcePaycheckId());
        if (pPaycheck.getPayPeriodBeginDate() != null)
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(pPaycheck.getPayPeriodBeginDate()));
        if (pPaycheck.getPayPeriodEndDate() != null)
            paycheckDTO.setPayPeriodEndDate(new DateDTO(pPaycheck.getPayPeriodEndDate()));
        paycheckDTO.setEmployeeId(pPaycheck.getSourceEmployee().getSourceEmployeeId());
        paycheckDTO.setPaycheckGrossAmount(pPaycheck.getGrossAmount());
        paycheckDTO.setPaycheckYTDGrossAmount(pPaycheck.getYTDGrossAmount());
        paycheckDTO.setPaycheckNetAmount(pPaycheck.getNetAmount());
        paycheckDTO.setPaycheckYTDNetAmount(pPaycheck.getYTDNetAmount());

        paycheckDTO.setCompensationTransactions(new ArrayList<CompensationTransactionDTO>());
        paycheckDTO.setDeductionTransactions(new ArrayList<DeductionTransactionDTO>());
        paycheckDTO.setEmployerContributionTransactions(new ArrayList<EmployerContributionTransactionDTO>());
        paycheckDTO.setLiabilityTransactions(new ArrayList<LiabilityTransactionDTO>());

        return paycheckDTO;
    }

    public QBDTPaycheckInfoDTO create(final QbdtPaycheckInfo pQbdtPaycheckInfo) {
        QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = new QBDTPaycheckInfoDTO();
        qbdtPaycheckInfoDTO.setListId(pQbdtPaycheckInfo.getListId());
        qbdtPaycheckInfoDTO.setAccountName(pQbdtPaycheckInfo.getAccountName());
        qbdtPaycheckInfoDTO.setCheckNumber(pQbdtPaycheckInfo.getCheckNumber());
        qbdtPaycheckInfoDTO.setCleared(pQbdtPaycheckInfo.getCleared());
        qbdtPaycheckInfoDTO.setMemo(pQbdtPaycheckInfo.getMemo());
        qbdtPaycheckInfoDTO.setOnService(pQbdtPaycheckInfo.getOnService());
        qbdtPaycheckInfoDTO.setProrate(pQbdtPaycheckInfo.getProrate());
        qbdtPaycheckInfoDTO.setTrackingClass(pQbdtPaycheckInfo.getTrackingClass());
        qbdtPaycheckInfoDTO.setSickHoursAccrued(pQbdtPaycheckInfo.getSickHoursAccrued());
        qbdtPaycheckInfoDTO.setVacationHoursAccrued(pQbdtPaycheckInfo.getVacationHoursAccrued());
        return qbdtPaycheckInfoDTO;
    }
    
   public PayrollRunDTO create(final PayrollRun pPayrollRun) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        payrollRunDTO.setPayrollTXBatchId(pPayrollRun.getSourcePayRunId());
        payrollRunDTO.setSettlementDate(new DateDTO(pPayrollRun.getPaycheckSettlementDate()));
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO(pPayrollRun.getPaycheckDate()));
        return payrollRunDTO;
    }

    public CompanyPayrollItemDTO create(CompanyPayrollItem companyPayrollItem) {
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(companyPayrollItem.getPayrollItem().getPayrollItemCode());
        companyPayrollItemDTO.setPayrollItemStatus(companyPayrollItem.getStatus());
        companyPayrollItemDTO.setSourcePayrollItemId(companyPayrollItem.getSourcePayrollItemId());
        companyPayrollItemDTO.setSourcePayrollItemDescription(companyPayrollItem.getSourceDescription());
        companyPayrollItemDTO.setTaxFormLine(companyPayrollItem.getTaxFormLine());
        companyPayrollItemDTO.setArchived(companyPayrollItem.getIsArchived());

        if (companyPayrollItem.getQbdtPayrollItemInfo() != null) {
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(create(companyPayrollItem.getQbdtPayrollItemInfo()));
        }

        for (PayrollItemTaxableTo payrollItemTaxableTo : companyPayrollItem.getPayrollItemTaxableToCollection()) {
            companyPayrollItemDTO.getTaxableToCompanyLawIds().add(payrollItemTaxableTo.getCompanyLaw().getSourceId());
        }
        return companyPayrollItemDTO;
    }

    public CompanyLawDTO create(CompanyLaw pCompanyLaw) {
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId(pCompanyLaw.getLaw().getLawId());
        companyLawDTO.setExemptionStatus(pCompanyLaw.getExemptionStatus());
        companyLawDTO.setSourceDescription(pCompanyLaw.getSourceDescription());
        companyLawDTO.setSourceId(pCompanyLaw.getSourceId());
        companyLawDTO.setStatus(pCompanyLaw.getStatus());
        companyLawDTO.setFilingStatus(pCompanyLaw.getFilingStatus());
        companyLawDTO.setReimbursableStatus(pCompanyLaw.getReimbursableStatus());
        companyLawDTO.setTaxFormLine(pCompanyLaw.getTaxFormLine());
        companyLawDTO.setArchived(pCompanyLaw.getIsArchived());
        // todo rates

        if (pCompanyLaw.getQbdtPayrollItemInfo() != null) {
            companyLawDTO.setQBDTPayrollItemInfoDTO(create(pCompanyLaw.getQbdtPayrollItemInfo()));
        }

        return companyLawDTO;
    }

    public QBDTPayrollItemInfoDTO create(QbdtPayrollItemInfo pQbdtPayrollItemInfo) {
        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setListId(pQbdtPayrollItemInfo.getListId());
        qbdtPayrollItemInfoDTO.setAdjustsGross(pQbdtPayrollItemInfo.getAdjustsGross());
        qbdtPayrollItemInfoDTO.setAgencyId(pQbdtPayrollItemInfo.getAgencyId());
        qbdtPayrollItemInfoDTO.setBasedOnQuantity(pQbdtPayrollItemInfo.getBasedOnQuantity());
        qbdtPayrollItemInfoDTO.setDefaultLimit(pQbdtPayrollItemInfo.getDefaultLimit());
        qbdtPayrollItemInfoDTO.setDefaultRate(pQbdtPayrollItemInfo.getDefaultRate());
        qbdtPayrollItemInfoDTO.setDefaultRateType(pQbdtPayrollItemInfo.getDefaultRateType());
        qbdtPayrollItemInfoDTO.setExpenseAccount(pQbdtPayrollItemInfo.getExpenseAccount());
        qbdtPayrollItemInfoDTO.setExpenseByJob(pQbdtPayrollItemInfo.getExpenseByJob());
        qbdtPayrollItemInfoDTO.setIsDeleted(pQbdtPayrollItemInfo.getIsDeleted());
        qbdtPayrollItemInfoDTO.setIsEarningsTable(pQbdtPayrollItemInfo.getEarningsTable());
        qbdtPayrollItemInfoDTO.setIsEmployeePaid(pQbdtPayrollItemInfo.getIsEmployeePaid());
        qbdtPayrollItemInfoDTO.setLiabilityAccount(pQbdtPayrollItemInfo.getLiabilityAccount());
        qbdtPayrollItemInfoDTO.setLiabilityAgency(pQbdtPayrollItemInfo.getLiabilityAgency());
        qbdtPayrollItemInfoDTO.setOnService(pQbdtPayrollItemInfo.getOnService());
        qbdtPayrollItemInfoDTO.setPayType(pQbdtPayrollItemInfo.getPayType());
        qbdtPayrollItemInfoDTO.setSpecialType(pQbdtPayrollItemInfo.getSpecialType());
        return qbdtPayrollItemInfoDTO;
    }

    public ServiceInfoDTO create(CompanyService pCompanyService) {
        ServiceInfoDTO serviceInfoDTO;
        if (pCompanyService instanceof CDCompanyServiceInfo) {
            CheckDistributionServiceInfoDTO checkDistributionServiceInfoDTO = new CheckDistributionServiceInfoDTO();
            checkDistributionServiceInfoDTO.setLastPaycheckId(((CDCompanyServiceInfo) pCompanyService).getLastPaycheckId());
            serviceInfoDTO = checkDistributionServiceInfoDTO;
        } else if (pCompanyService instanceof DDCompanyServiceInfo) {
            DDServiceInfoDTO ddServiceInfoDTO = new DDServiceInfoDTO();
            SpcfMoney averagePayRunAmount = ((DDCompanyServiceInfo) pCompanyService).getAveragePayRunAmount();
            SpcfMoney highAnnualPayAmount = ((DDCompanyServiceInfo) pCompanyService).getHighAnnualPayAmount();
            if (averagePayRunAmount != null) {
                ddServiceInfoDTO.setAveragePayrollAmount(SpcfUtils.convertToBigDecimal(averagePayRunAmount));
            }
            if (highAnnualPayAmount != null) {
                ddServiceInfoDTO.setHighAnnualPayrollAmount(SpcfUtils.convertToBigDecimal(highAnnualPayAmount));
            }
            serviceInfoDTO = ddServiceInfoDTO;
        } else if (pCompanyService instanceof ThirdParty401kCompanyServiceInfo) {
            ThirdParty401kServiceInfoDTO thirdParty401kServiceInfoDTO = new ThirdParty401kServiceInfoDTO();
            thirdParty401kServiceInfoDTO.setCustodialId(((ThirdParty401kCompanyServiceInfo) pCompanyService).getCustodialId());
            thirdParty401kServiceInfoDTO.setHasSafeHarbor(((ThirdParty401kCompanyServiceInfo) pCompanyService).getHasSafeHarbor());
            serviceInfoDTO = thirdParty401kServiceInfoDTO;
        } else if(pCompanyService instanceof TaxCompanyServiceInfo) {
            TaxServiceInfoDTO taxServiceInfoDTO = new TaxServiceInfoDTO();
            taxServiceInfoDTO.setLastTaxYear(((TaxCompanyServiceInfo) pCompanyService).getLastTaxYear());
            taxServiceInfoDTO.setLastQuarterToFile(((TaxCompanyServiceInfo) pCompanyService).getLastQuarterToFile());
            taxServiceInfoDTO.setW2DeliveryPreferenceCd(((TaxCompanyServiceInfo) pCompanyService).getW2DeliveryPreferenceCd());
            taxServiceInfoDTO.setClientPacketDeliveryPreferenceCd(((TaxCompanyServiceInfo) pCompanyService).getClientPacketDeliveryPreferenceCd());
            taxServiceInfoDTO.setFileAnnualReturns(((TaxCompanyServiceInfo) pCompanyService).getFileAnnualReturns());
            taxServiceInfoDTO.setFinalAnnualReturns(((TaxCompanyServiceInfo) pCompanyService).getFinalAnnualReturns());
            taxServiceInfoDTO.setLastPayrollDate(((TaxCompanyServiceInfo) pCompanyService).getLastPayrollDate());
            taxServiceInfoDTO.setInHouseW2(((TaxCompanyServiceInfo) pCompanyService).getInHouseW2());
            taxServiceInfoDTO.setIncludeOnSsaFile(((TaxCompanyServiceInfo) pCompanyService).getIncludeOnSSAFile());
            serviceInfoDTO = taxServiceInfoDTO;
        } else {
            serviceInfoDTO = new ServiceInfoDTO();
        }

        serviceInfoDTO.setFundingModel(pCompanyService.getFundingModel());
        serviceInfoDTO.setServiceCode(pCompanyService.getService().getServiceCd());
        serviceInfoDTO.setServiceStartDate(pCompanyService.getServiceStartDate());

        return serviceInfoDTO;
    }

    public CompanyAdjustmentSubmissionDTO create(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        companyAdjustmentSubmissionDTO.setTotalAmount(pCompanyAdjustmentSubmission.getAmount());

        if (pCompanyAdjustmentSubmission.getQbdtTransactionInfo() != null) {
            QbdtTransactionInfo qbdtTransactionInfo = pCompanyAdjustmentSubmission.getQbdtTransactionInfo();
            QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            qbdtTransactionInfoDTO.setAccountName(qbdtTransactionInfo.getAccountName());
            qbdtTransactionInfoDTO.setAgencyName(qbdtTransactionInfo.getAgencyName());
            qbdtTransactionInfoDTO.setCleared(qbdtTransactionInfo.getCleared());
            qbdtTransactionInfoDTO.setIsDeleted(qbdtTransactionInfo.getIsDeleted());
            qbdtTransactionInfoDTO.setMemo(qbdtTransactionInfo.getMemo());
            qbdtTransactionInfoDTO.setOnService(qbdtTransactionInfo.getOnService());
            qbdtTransactionInfoDTO.setReferenceNumber(qbdtTransactionInfo.getReferenceNumber());
            qbdtTransactionInfoDTO.setTrackingClass(qbdtTransactionInfo.getTrackingClass());
            companyAdjustmentSubmissionDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
        }

        for (LiabilityAdjustment liabilityAdjustment : pCompanyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
            LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
            liabilityAdjustmentDTO.setAmount(liabilityAdjustment.getAmount());
            liabilityAdjustmentDTO.setPayrollItemId(liabilityAdjustment.getCompanyLaw().getSourceId());
            liabilityAdjustmentDTO.setLawId(liabilityAdjustment.getLaw().getLawId());
            if (liabilityAdjustment.getEmployee() != null) {
                liabilityAdjustmentDTO.setSourceEmployeeId(liabilityAdjustment.getEmployee().getSourceEmployeeId());
            }
            liabilityAdjustmentDTO.setTaxableWages(liabilityAdjustment.getTaxableWages());
            liabilityAdjustmentDTO.setTotalWages(liabilityAdjustment.getTotalWages());

            if (liabilityAdjustment.getQbdtTransactionInfo() != null) {
                QbdtTransactionInfo qbdtTransactionInfo = liabilityAdjustment.getQbdtTransactionInfo();
                QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
                qbdtTransactionInfoDTO.setAccountName(qbdtTransactionInfo.getAccountName());
                qbdtTransactionInfoDTO.setAgencyName(qbdtTransactionInfo.getAgencyName());
                qbdtTransactionInfoDTO.setCleared(qbdtTransactionInfo.getCleared());
                qbdtTransactionInfoDTO.setIsDeleted(qbdtTransactionInfo.getIsDeleted());
                qbdtTransactionInfoDTO.setMemo(qbdtTransactionInfo.getMemo());
                qbdtTransactionInfoDTO.setOnService(qbdtTransactionInfo.getOnService());
                qbdtTransactionInfoDTO.setReferenceNumber(qbdtTransactionInfo.getReferenceNumber());
                qbdtTransactionInfoDTO.setTrackingClass(qbdtTransactionInfo.getTrackingClass());
                liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
            }

            companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().add(liabilityAdjustmentDTO);
        }

        return companyAdjustmentSubmissionDTO;
    }

    public EntitlementDTO create(Entitlement pEntitlement) {
        return copyEntitlementInfo(pEntitlement, new EntitlementDTO());
    }

    public EntitlementUnitDTO create(EntitlementUnit pEntitlementUnit) {
        EntitlementUnitDTO entitlementUnitDTO = (EntitlementUnitDTO) copyEntitlementInfo(pEntitlementUnit.getEntitlement(), new EntitlementUnitDTO());

        entitlementUnitDTO.setAssetItemNumber(pEntitlementUnit.getEntitlement().getEntitlementCode().getAssetItemNumber());
        entitlementUnitDTO.setEntitlementUnitStatus(pEntitlementUnit.getEntitlementUnitStatus());
        entitlementUnitDTO.setServiceKey(pEntitlementUnit.getServiceKey());
        entitlementUnitDTO.setExtensionKey(pEntitlementUnit.getExtensionKey());
        entitlementUnitDTO.setFedTaxId(pEntitlementUnit.getFedTaxId());
        entitlementUnitDTO.setErrorCount(pEntitlementUnit.getErrorCount());

        return entitlementUnitDTO;
    }

    public EntitlementUnitDTO create(Entitlement pEntitlement, EntitlementUnitDTO pEntitlementUnitDTO) {
        return (EntitlementUnitDTO) copyEntitlementInfo(pEntitlement, pEntitlementUnitDTO);
    }

    private EntitlementDTO copyEntitlementInfo(Entitlement pEntitlement, EntitlementDTO pEntitlementDTO) {
        pEntitlementDTO.setCreditCardExpiration(pEntitlement.getCreditCardExpiration());
        pEntitlementDTO.setCreditCardNumber(pEntitlement.getCreditCardNumber());
        pEntitlementDTO.setCreditCardType(pEntitlement.getCreditCardType());
        pEntitlementDTO.setCustomerId(pEntitlement.getCustomerId());
        pEntitlementDTO.setEditionType(pEntitlement.getEntitlementCode().getEditionType());
        pEntitlementDTO.setEntitlementOfferingCode(pEntitlement.getEntitlementOfferingCode());
        pEntitlementDTO.setLicenseNumber(pEntitlement.getLicenseNumber());
        pEntitlementDTO.setNextChargeDate(pEntitlement.getNextChargeDate());
        pEntitlementDTO.setSubscriptionEndDate(pEntitlement.getSubscriptionEndDate());
        pEntitlementDTO.setNumberOfEmployeesType(pEntitlement.getEntitlementCode().getNumberOfEmployeesType());
        pEntitlementDTO.setOrderNumber(pEntitlement.getOrderNumber());
        pEntitlementDTO.setPaymentMethodType(pEntitlement.getPaymentMethodType());
        pEntitlementDTO.setContactEmail(pEntitlement.getContactEmail());
        pEntitlementDTO.setContactName(pEntitlement.getContactName());
        pEntitlementDTO.setEntitlementState(pEntitlement.getEntitlementState());
        pEntitlementDTO.setBillingZipCode(pEntitlement.getBillingZipCode());
        pEntitlementDTO.setCancellationReason(pEntitlement.getCancellationReason());
        pEntitlementDTO.setLastMessageTimestamp(pEntitlement.getLastMessageTimestamp());
        pEntitlementDTO.setOrderSourceCd(pEntitlement.getOrderSourceCd());
        pEntitlementDTO.setSubscriptionNumber(pEntitlement.getSubscriptionNumber());
        pEntitlementDTO.setTrialAssociated(pEntitlement.getTrialAssociated());
        pEntitlementDTO.setSubscriptionStartDate(pEntitlement.getSubscriptionStartDate());
        pEntitlementDTO.setRetail(pEntitlement.getRetail());
        //This should not be copied...
        //pEntitlementDTO.setAssetItemNumber(pEntitlement.getEntitlementCode().getAssetItemNumber());

        return pEntitlementDTO;
    }

    public QBDTTransactionInfoDTO create(QbdtTransactionInfo pQBDTTransactionInfo) {
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        qbdtTransactionInfoDTO.setId(pQBDTTransactionInfo.getId());
        qbdtTransactionInfoDTO.setAccountName(pQBDTTransactionInfo.getAccountName());
        qbdtTransactionInfoDTO.setAgencyName(pQBDTTransactionInfo.getAgencyName());
        qbdtTransactionInfoDTO.setCleared(pQBDTTransactionInfo.getCleared());
        qbdtTransactionInfoDTO.setIsDeleted(pQBDTTransactionInfo.getIsDeleted());
        qbdtTransactionInfoDTO.setIsDirectDeposit(pQBDTTransactionInfo.getIsDirectDeposit());
        qbdtTransactionInfoDTO.setMemo(pQBDTTransactionInfo.getMemo());
        qbdtTransactionInfoDTO.setOnService(pQBDTTransactionInfo.getOnService());
        qbdtTransactionInfoDTO.setReferenceNumber(pQBDTTransactionInfo.getReferenceNumber());
        qbdtTransactionInfoDTO.setSystemGenerated(pQBDTTransactionInfo.getSystemGenerated());
        qbdtTransactionInfoDTO.setToken(pQBDTTransactionInfo.getToken());
        qbdtTransactionInfoDTO.setTrackingClass(pQBDTTransactionInfo.getTrackingClass());
        return qbdtTransactionInfoDTO;
    }
    
    public LiabilityCheckDTO create(LiabilityCheck pLiabilityCheck) {
        LiabilityCheckDTO liabilityCheckDTO = new LiabilityCheckDTO();
        liabilityCheckDTO.setAmount(pLiabilityCheck.getAmount());
        liabilityCheckDTO.setLiabilityCheckType(pLiabilityCheck.getType());
        liabilityCheckDTO.setIsVoid(pLiabilityCheck.getIsVoid());
        liabilityCheckDTO.setPeriodEndDate(pLiabilityCheck.getPeriodEndDate());
        liabilityCheckDTO.setSourceId(pLiabilityCheck.getSourceId());
        liabilityCheckDTO.setTransactionDate(pLiabilityCheck.getTransactionDate());

        if(pLiabilityCheck.getPayrollRun() != null) {
            liabilityCheckDTO.setSourcePayrollRunId(pLiabilityCheck.getPayrollRun().getSourcePayRunId());
        }

        if(pLiabilityCheck.getQbdtTransactionInfo() != null) {
            liabilityCheckDTO.setQBDTTransactionInfoDTO(create(pLiabilityCheck.getQbdtTransactionInfo()));
        }

        for (LiabilityCheckBillingDetailAssoc liabilityCheckBillingDetailAssoc : pLiabilityCheck.getLiabilityCheckBillingDetailAssocCollection()) {
            liabilityCheckDTO.getAssociatedBillingDetails().add(liabilityCheckBillingDetailAssoc.getBillingDetail());
        }

        return liabilityCheckDTO;
    }

    public LiabilityCheckLineDTO create(LiabilityCheckLine pLiabilityCheckLine) {
        LiabilityCheckLineDTO liabilityCheckLineDTO = new LiabilityCheckLineDTO();
        liabilityCheckLineDTO.setAmount(pLiabilityCheckLine.getAmount());

        if(pLiabilityCheckLine.getCompanyLaw() != null) {
            liabilityCheckLineDTO.setCompanyPayrollItemId(pLiabilityCheckLine.getCompanyLaw().getSourceId());
        } else if(pLiabilityCheckLine.getCompanyPayrollItem() != null) {
            liabilityCheckLineDTO.setCompanyPayrollItemId(pLiabilityCheckLine.getCompanyPayrollItem().getSourcePayrollItemId());
        }

        if(pLiabilityCheckLine.getQbdtTransactionInfo() != null) {
            liabilityCheckLineDTO.setQBDTTransactionInfo(create(pLiabilityCheckLine.getQbdtTransactionInfo()));
        }

        return liabilityCheckLineDTO;
    }
    
    public CompanyAgencyDTO create(final CompanyAgency pCompanyAgency) {

        CompanyAgencyDTO companyAgencyDTO = new CompanyAgencyDTO();
        companyAgencyDTO.setIntuitResponsibilityStartDate(pCompanyAgency.getIntuitResponsibilityStartDate());
        companyAgencyDTO.setIntuitResponsibilityEndDate(pCompanyAgency.getIntuitResponsibilityEndDate());
        companyAgencyDTO.setErFicaDeferralEnabled(pCompanyAgency.getErFicaDeferralEnabled());

        List<FormTemplateDTO> formTemplates = new ArrayList<FormTemplateDTO>();
        DomainEntitySet<CompanyAgencyFormTemplate> cAgencyFormTemplatesInPSP = pCompanyAgency.findValidFormTemplatesForCompanyAgency();
        for (CompanyAgencyFormTemplate currentFormTemplate : cAgencyFormTemplatesInPSP) {
            FormTemplateDTO ftDTo = new FormTemplateDTO();
            ftDTo.setEffectiveDate(currentFormTemplate.getEffectiveDate());
            ftDTo.setFilerType(currentFormTemplate.getFormTemplate().getFormTemplateCd());
            formTemplates.add(ftDTo);
        }

        companyAgencyDTO.setFormTemplateDtoList(formTemplates);

        List<CompanyAgencyPaymentTemplateDTO> captDTOs = new ArrayList<CompanyAgencyPaymentTemplateDTO>();
        DomainEntitySet<CompanyAgencyPaymentTemplate> cAgencyPaymentTemplatesInPSP = pCompanyAgency.getCompanyAgencyPaymentTemplateCollection();
        for (CompanyAgencyPaymentTemplate currentCAPT : cAgencyPaymentTemplatesInPSP) {
            CompanyAgencyPaymentTemplateDTO captDTO = new CompanyAgencyPaymentTemplateDTO();
            captDTO.setAgencyTaxpayerId(currentCAPT.getAgencyTaxpayerId());
            captDTO.setPaymentTemplateCd(currentCAPT.getPaymentTemplate().getPaymentTemplateCd());

            captDTO.setCompanyFilingAmountDTOs(new ArrayList<CompanyFilingAmountDTO>());
            for (CompanyFilingAmount companyFilingAmount : currentCAPT.getCompanyFilingAmountCollection()) {
                CompanyFilingAmountDTO companyFilingAmountDTO = new CompanyFilingAmountDTO();
                companyFilingAmountDTO.setId(companyFilingAmount.getId());
                companyFilingAmountDTO.setName(companyFilingAmount.getName());
                companyFilingAmountDTO.setAmount(companyFilingAmount.getAmount());
                companyFilingAmountDTO.setEffectiveDate(new DateDTO(companyFilingAmount.getEffectiveDate()));
                captDTO.getCompanyFilingAmountDTOs().add(companyFilingAmountDTO);
            }

            captDTOs.add(captDTO);
        }

        companyAgencyDTO.setCompanyAgencyPaymentTemplateDTOList(captDTOs);
        return companyAgencyDTO;
    }

    public EffectiveDepositFrequencyDTO create(final EffectiveDepositFrequency pEffectiveDepositFrequency) {
        EffectiveDepositFrequencyDTO effectiveDepositFrequencyDTO = new EffectiveDepositFrequencyDTO();
        effectiveDepositFrequencyDTO.setAgencyId(pEffectiveDepositFrequency.getCompanyAgencyPaymentTemplate().getPaymentTemplate().getAgency().getAgencyId());
        effectiveDepositFrequencyDTO.setPaymentFrequencyId(pEffectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        effectiveDepositFrequencyDTO.setPaymentTemplateCd(pEffectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentTemplate().getPaymentTemplateCd());
        effectiveDepositFrequencyDTO.setEffectiveDate(pEffectiveDepositFrequency.getEffectiveDate().toLocal());
        return effectiveDepositFrequencyDTO;
    }
    
    public QBDTPayrollTransactionDTO create(QbdtPayrollTransaction pQbdtPayrollTransaction) {
        QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
        qbdtPayrollTransactionDTO.setAmount(pQbdtPayrollTransaction.getAmount());
        if(pQbdtPayrollTransaction.getEmployee() != null) {
            qbdtPayrollTransactionDTO.setEmployeeSourceId(pQbdtPayrollTransaction.getEmployee().getSourceEmployeeId());
        } else {
            qbdtPayrollTransactionDTO.setEmployeeName(pQbdtPayrollTransaction.getEmployeeName());
        }
        qbdtPayrollTransactionDTO.setIsVoided(pQbdtPayrollTransaction.getIsVoided());
        qbdtPayrollTransactionDTO.setPeriodEndDate(pQbdtPayrollTransaction.getPeriodEndDate());
        qbdtPayrollTransactionDTO.setSourceId(pQbdtPayrollTransaction.getSourceId());
        qbdtPayrollTransactionDTO.setTransactionDate(pQbdtPayrollTransaction.getTransactionDate());
        qbdtPayrollTransactionDTO.setTransactionType(pQbdtPayrollTransaction.getTransactionType());
        qbdtPayrollTransactionDTO.setQBDTTransactionInfoDTO(create(pQbdtPayrollTransaction.getQbdtTransactionInfo()));
        return qbdtPayrollTransactionDTO;
    }

    public OfferingInfoDTO create(final Offering pOffering) {
        OfferingInfoDTO offeringInfoDTO = new OfferingInfoDTO();
        offeringInfoDTO.setOfferingCode(pOffering.getOfferingCode());
        offeringInfoDTO.setSKU(pOffering.getSKU());
        return offeringInfoDTO;
    }

    // Get rid of this error message after all methods are implemented
    private static final String tempErrorMsg = "This method is not implemented yet. If you need it, you should code it yourself.";

}
