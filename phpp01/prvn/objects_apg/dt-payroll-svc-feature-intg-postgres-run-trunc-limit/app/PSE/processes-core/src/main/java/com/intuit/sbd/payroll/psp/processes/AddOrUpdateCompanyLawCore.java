/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddOrUpdateCompanyLawCore.java#4 $
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
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyLawDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTPayrollItemInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.StringUtils;

/**
 * Core process for adding a company law.
 *
 * @author Marcela Villani
 */
public class AddOrUpdateCompanyLawCore extends Process implements IProcess {
    private Company company;

    private CompanyLawDTO companyLawDTO;
    private Law law;
    private CompanyLaw companyLaw;
    private QbdtPayrollItemInfo qbdtPayrollItemInfo;

    private String sourceCompanyId;
    private SourceSystemCode sourceSystemCd;
    private AddOrUpdateCompanyLawRate mAddOrUpdateCompanyLawRate;


    /**
     * Constructor for AddOrUpdateCompanyLawCore
     *
     * @param pSourceSystemCd  Source System Code
     * @param pSourceCompanyId Source Company ID
     * @param pCompanyLawDTO   CompanyLaw data transfer object to add
     */
    public AddOrUpdateCompanyLawCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, CompanyLawDTO pCompanyLawDTO) {

        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        companyLawDTO = pCompanyLawDTO;
    }

    /**
     * Obtains the CompanyLaw attached to the core process
     *
     * @return CompanyLaw domain object
     */
    public CompanyLaw getCompanyLaw() {
        return companyLaw;
    }


    public ProcessResult<CompanyLaw> process() {
        ProcessResult<CompanyLaw> processResult = new ProcessResult<CompanyLaw>();

        // Get Agency
        Agency agency = law.getPaymentTemplate().getAgency();

        // Verify if CompanyAgency object already exists. If not, create it.
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, agency.getAgencyId());
        if (companyAgency == null) {
            companyAgency = CompanyAgency.addCompanyAgency(company, agency.getAgencyId(), PSPDate.getPSPTime(), false); //do not add CAPT so can add DFs
        }

        Application.getSessionCache().addPrimaryKey(companyAgency.getNaturalKey(company), companyAgency.getId());

        // Verify if CompanyAgencyPaymentTemplate already exists. If not, create it
        PaymentTemplate paymentTemplate = law.getPaymentTemplate();

        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(companyAgency, paymentTemplate);
        if (companyAgencyPaymentTemplate == null) {
            companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.createNewCompanyAgencyPaymentTemplate(paymentTemplate, companyAgency, companyLawDTO.getQBDTPayrollItemInfoDTO().getAgencyId());

            // Add deposit frequencies if creating a new companyAgencyPaymentTemplate
            if (companyLawDTO.getDepositFrequencies().size() > 0) {
                Boolean firstFrequency = true;
                for (EffectiveDepositFrequencyDTO depositFrequencyDTO : companyLawDTO.getDepositFrequencies()) {
                    EffectiveDepositFrequency depositFrequency = new EffectiveDepositFrequency();
                    SpcfCalendar effectiveDate = depositFrequencyDTO.getEffectiveDate();
                    if (firstFrequency) {
                        effectiveDate = companyAgency.getIntuitResponsibilityStartDate();
                        firstFrequency = false;
                    }
                    if (effectiveDate != null) {
                        CalendarUtils.clearTime(effectiveDate);
                    }

                    depositFrequency.setEffectiveDate(effectiveDate);
                    PaymentTemplateFrequency paymentTemplateFrequency = paymentTemplate.getPaymentTemplateFrequency(depositFrequencyDTO.getPaymentFrequencyId().toString());
                    depositFrequency.setPaymentTemplateFrequency(paymentTemplateFrequency);
                    companyAgencyPaymentTemplate.addEffectiveDepositFrequency(depositFrequency);
                    companyAgencyPaymentTemplate = Application.save(companyAgencyPaymentTemplate);
                    depositFrequency.setCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate);
                    Application.save(depositFrequency);
                }
            } else {
                EffectiveDepositFrequency depositFrequency = new EffectiveDepositFrequency();
                SpcfCalendar effectiveDate = companyAgency.getIntuitResponsibilityStartDate().toLocal();
                CalendarUtils.clearTime(effectiveDate);
                depositFrequency.setEffectiveDate(effectiveDate);
                PaymentTemplateFrequency paymentTemplateFrequency = paymentTemplate.getPaymentTemplateFrequency(paymentTemplate.getDefaultDepositFrequency());
                depositFrequency.setPaymentTemplateFrequency(paymentTemplateFrequency);
                companyAgencyPaymentTemplate.addEffectiveDepositFrequency(depositFrequency);
                companyAgencyPaymentTemplate = Application.save(companyAgencyPaymentTemplate);
                depositFrequency.setCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate);
                Application.save(depositFrequency);
            }

            // add the payment template to the cache
            Application.getSessionCache().addPrimaryKey(companyAgencyPaymentTemplate.getNaturalKey(company), companyAgencyPaymentTemplate.getId());
        }

        // Verify if TaxPayerId has changed and update it
        if (agency.isIRS()) {
            companyAgencyPaymentTemplate.updateAgencyTaxpayerId(company.getFedTaxId());
        } else if (!StringUtils.equals(companyAgencyPaymentTemplate.getAgencyTaxpayerId(), companyLawDTO.getQBDTPayrollItemInfoDTO().getAgencyId())) {
            if (law.isPrimaryLawForStateID()) {
                String oldId = companyAgencyPaymentTemplate.getAgencyTaxpayerId();
                companyAgencyPaymentTemplate.updateAgencyTaxpayerId(companyLawDTO.getQBDTPayrollItemInfoDTO().getAgencyId());
                String newId = companyAgencyPaymentTemplate.getAgencyTaxpayerId();
                if (oldId != null && !oldId.equals(newId)) {
                    CompanyEvent.createStateIdModifiedEvent(company, paymentTemplate.getPaymentTemplateCd(), oldId, newId);
                }
            }
        }


        // CompanyLaw doesn't exist, instantiate a new object
        // Otherwise update the CompanyLaw info with the dto info

        companyLaw = CompanyLaw.findCompanyLawBySourceId(company, companyLawDTO.getSourceId());

        if (companyLaw == null) {
            companyLaw = new CompanyLaw();
            companyLaw.setCompanyAgency(companyAgency);
            companyAgency.addCompanyLaw(companyLaw);
            companyLaw.setLaw(law);
            companyLaw.setTaxFormLine(companyLawDTO.getTaxFormLine());
            companyLaw.setW2Code(companyLawDTO.getW2Code() == null ? -1 : companyLawDTO.getW2Code());
            qbdtPayrollItemInfo = new QbdtPayrollItemInfo();
            companyLaw.setQbdtPayrollItemInfo(qbdtPayrollItemInfo);
            qbdtPayrollItemInfo.setCompany(companyLaw.getCompanyAgency().getCompany());
            qbdtPayrollItemInfo.setCompanyLaw(companyLaw);
            qbdtPayrollItemInfo.setRatePushToken(-1);
            qbdtPayrollItemInfo = Application.save(qbdtPayrollItemInfo);

            // Record event if CompanyLaw already exists for companyAgency + Law and record event
            CompanyLaw duplicateCompanyLaw = CompanyLaw.findCompanyLaw(companyAgency, law);
            if (duplicateCompanyLaw != null) {
                CompanyEvent event = CompanyEvent.createCompanyEvent(company, EventTypeCode.MultipleCompanyLawsCreated);
                event.addCompanyEventDetail(EventDetailTypeCode.CompanyAgency, companyAgency.getId().toString());
                event.addCompanyEventDetail(EventDetailTypeCode.Law, law.getLawId());
                event.addCompanyEventDetail(EventDetailTypeCode.AgencyId, companyAgency.getAgency().getAgencyId());
                Application.save(event);
            }

            if (!law.getLawId().equals(Law.LAW_177) && !law.getLawId().equals(Law.LAW_9)) {
                CompanyLaw originalCompanyLaw = CompanyLaw.findCompanyLaw(company, companyLawDTO.getLawId());
                if (originalCompanyLaw != null) {
                    CompanyEvent event = CompanyEvent.createCompanyEvent(company, EventTypeCode.DuplicatePayrollItemReceived);
                    event.addCompanyEventDetail(EventDetailTypeCode.Description, originalCompanyLaw.getSourceDescription());
                    event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, companyLawDTO.getSourceId());
                    event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, originalCompanyLaw.getSourceId());
                    Application.save(event);

                    if (originalCompanyLaw.getQbdtPayrollItemInfo() != null) {
                        // hide the law from the ui and data recovery
                        originalCompanyLaw.getQbdtPayrollItemInfo().setToken(Company.EXCLUDE_TOKEN);
                    }
                    originalCompanyLaw.setAdditionalCompanyLaw(companyLaw);
                    Application.save(originalCompanyLaw);
                }
            }
        }

        updateCompanyLawFromDTO();

        // Save the company Law
        companyLaw = Application.save(companyLaw);

        processResult.setResult(companyLaw);

        // add the company law to the session cache
        if (processResult.isSuccess()) {
            companyLaw.cache(company);
        }

        if (companyLaw.getCompanyAgency().getAgency().getAgencyId().equals(Agency.FL_AGENT_ID)) {
            if (companyLaw.getFilingStatus() == null || companyLaw.getFilingStatus() == PayrollItemStatus.Active) {
                //If Company Law is Active, check for FL ACH Enrollment, enroll if not enrolled already
                processResult.merge(PayrollServices.companyManager.addACHEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, false));
            }
        }

        // CompanyLawRate
        if (companyLawDTO.getRateDTOs().size() > 0) {
            this.mAddOrUpdateCompanyLawRate.process();
        }

        return processResult;
    }

    public ProcessResult validate() {
        // Validate inputs from DTO
        ProcessResult validationResult = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }
        // Validate companyLaw DTO
        if (companyLawDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.CompanyLaw, "CompanyLawDTO", "CompanyLawDTO");
            return validationResult;
        }
        validationResult = companyLawDTO.validate();
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Validate Company Exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (!company.isAllowedCapability(SystemCapabilityCode.ChangeCompanyInfo)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
            return validationResult;
        }

        if (!company.passesAdditionalCancelTermValidation(false, true, true, true)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
        }


        // Validate Law Exists
        law = PayrollServices.entityFinder.findById(Law.class, companyLawDTO.getLawId());
        if (law == null) {
            validationResult.getMessages().LawDoesNotExist(EntityName.Law, companyLawDTO.getLawId());
            return validationResult;
        }

        if (companyLawDTO.getRateDTOs().size() > 0) {
            mAddOrUpdateCompanyLawRate = new AddOrUpdateCompanyLawRate(this.sourceSystemCd, this.sourceCompanyId, companyLawDTO.getSourceId(), companyLawDTO.getRateDTOs(), companyLawDTO.isDTOCreatedBySystem());
            validationResult.merge(mAddOrUpdateCompanyLawRate.validate());
        }

        // If deposit frequencies are passed, validate if the payment template supports them
        if (companyLawDTO.getDepositFrequencies().size() > 0) {
            for (EffectiveDepositFrequencyDTO depositFrequencyDTO : companyLawDTO.getDepositFrequencies()) {
                String frequencyId = depositFrequencyDTO.getPaymentFrequencyId().toString();
                PaymentTemplateFrequency paymentTemplateFrequency = law.getPaymentTemplate().getPaymentTemplateFrequency(frequencyId);
                if (paymentTemplateFrequency == null) {
                    validationResult.getMessages().PaymentFrequencyNotSupportedForThePaymentTemplate(EntityName.PaymentTemplate, frequencyId, frequencyId, law.getPaymentTemplate().getPaymentTemplateCd());
                }
            }
        }
        return validationResult;
    }

    private void updateCompanyLawFromDTO() {
        companyLaw.setExemptionStatus(companyLawDTO.getExemptionStatus() != null ? companyLawDTO.getExemptionStatus() : LawStatus.NonExempt);
        companyLaw.setSourceId(companyLawDTO.getSourceId());
        companyLaw.setSourceDescription(companyLawDTO.getSourceDescription());
        companyLaw.setStatus(companyLawDTO.getStatus());
        companyLaw.setFilingStatus(companyLawDTO.getFilingStatus() != null ? companyLawDTO.getFilingStatus() : PayrollItemStatus.Active);
        companyLaw.setReimbursableStatus(companyLawDTO.getReimbursableStatus() != null ? companyLawDTO.getReimbursableStatus() : ReimbursableStatus.NotReimbursable);
        companyLaw.setTaxFormLine(companyLawDTO.getTaxFormLine());
        companyLaw.setW2Code(companyLawDTO.getW2Code() == null ? -1 : companyLawDTO.getW2Code());
        companyLaw.setIsArchived(companyLawDTO.isArchived());

        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = companyLawDTO.getQBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfo = companyLaw.getQbdtPayrollItemInfo();
        qbdtPayrollItemInfo.setListId(qbdtPayrollItemInfoDTO.getListId());
        qbdtPayrollItemInfo.setAdjustsGross(qbdtPayrollItemInfoDTO.adjustsGross());
        qbdtPayrollItemInfo.setAgencyId(qbdtPayrollItemInfoDTO.getAgencyId());
        qbdtPayrollItemInfo.setBasedOnQuantity(qbdtPayrollItemInfoDTO.isBasedOnQuantity());
        qbdtPayrollItemInfo.setDefaultLimit(qbdtPayrollItemInfoDTO.getDefaultLimit());
        qbdtPayrollItemInfo.setDefaultRate(qbdtPayrollItemInfoDTO.getDefaultRate());
        qbdtPayrollItemInfo.setDefaultRateType(qbdtPayrollItemInfoDTO.getDefaultRateType());
        qbdtPayrollItemInfo.setExpenseAccount(qbdtPayrollItemInfoDTO.getExpenseAccount());
        qbdtPayrollItemInfo.setExpenseByJob(qbdtPayrollItemInfoDTO.expenseByJob());
        qbdtPayrollItemInfo.setIsDeleted(qbdtPayrollItemInfoDTO.isDeleted());
        qbdtPayrollItemInfo.setIsEmployeePaid(qbdtPayrollItemInfoDTO.isEmployeePaid());
        qbdtPayrollItemInfo.setLiabilityAccount(qbdtPayrollItemInfoDTO.getLiabilityAccount());
        qbdtPayrollItemInfo.setLiabilityAgency(qbdtPayrollItemInfoDTO.getLiabilityAgency());
        qbdtPayrollItemInfo.setOnService(qbdtPayrollItemInfoDTO.isOnService());
        qbdtPayrollItemInfo.setPayType(qbdtPayrollItemInfoDTO.getPayType());
        qbdtPayrollItemInfo.setSpecialType(qbdtPayrollItemInfoDTO.getSpecialType());
        qbdtPayrollItemInfo.setOvertimeMultiplier(qbdtPayrollItemInfoDTO.getOvertimeMultiplier());
    }

}
