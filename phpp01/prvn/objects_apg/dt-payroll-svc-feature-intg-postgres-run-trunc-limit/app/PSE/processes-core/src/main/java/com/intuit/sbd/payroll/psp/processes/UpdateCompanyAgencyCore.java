/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/UpdateCompanyAgencyCore.java#4 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAgencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAgencyPaymentTemplateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyFilingAmountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.FormTemplateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters;

/**
 * Core process update Company-Agency relationship attributes.
 *
 * @author Nadeem Amin
 */
public class UpdateCompanyAgencyCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mAgencyCd;
    private CompanyAgencyDTO mDto;
    private CompanyAgency mCompanyAgency;

    public UpdateCompanyAgencyCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                   String pAgencyCd, CompanyAgencyDTO pDto) {
        mSourceSystemCd = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mAgencyCd = pAgencyCd;
        mDto = pDto;
    }

    /**
     * Update modified attributes. An attribute it considered modified
     * if the value passed in not the same as the current value.
     *
     * @return ProcessResult - containing any messages and/or errors.
     */
    public ProcessResult process() {
        ProcessResult result = new ProcessResult();

        for (CompanyAgencyPaymentTemplateDTO captDTO : mDto.getCompanyAgencyPaymentTemplateDTOList()) {
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(captDTO.getPaymentTemplateCd());
            DomainEntitySet<CompanyAgencyPaymentTemplate> capts = mCompanyAgency.getCompanyAgencyPaymentTemplateCollection().find(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplate));
            //todo how could it happen otherwise without a bug?  Raise error?
            if (capts.size() > 0) {
                CompanyAgencyPaymentTemplate capt = capts.get(0);
                updateAgencyIDs(capt, captDTO);
                updateFilingAmounts(capt, captDTO.getCompanyFilingAmountDTOs());
            }
        }

        if (!ObjectUtils.equals(mCompanyAgency.getIntuitResponsibilityStartDate(), mDto.getIntuitResponsibilityStartDate()))
            mCompanyAgency.setIntuitResponsibilityStartDate(mDto.getIntuitResponsibilityStartDate());
        if (!ObjectUtils.equals(mCompanyAgency.getIntuitResponsibilityEndDate(), mDto.getIntuitResponsibilityEndDate()))
            mCompanyAgency.setIntuitResponsibilityEndDate(mDto.getIntuitResponsibilityEndDate());

        if (!ObjectUtils.equals(mCompanyAgency.getErFicaDeferralEnabled(), mDto.getErFicaDeferralEnabled())) {
            mCompanyAgency.setErFicaDeferralEnabled(mDto.getErFicaDeferralEnabled());
        }

        DomainEntitySet<CompanyAgencyFormTemplate> cAgencyFormTemplatesInPSP = mCompanyAgency.findValidFormTemplatesForCompanyAgency();
        ArrayList<SpcfUniqueId> cAFormTemplatesIn = new ArrayList<SpcfUniqueId>();

        if (mDto.getFormTemplateDtoList() != null && mDto.getFormTemplateDtoList().size() > 0) {
            for (FormTemplateDTO mFormTemplateDto : mDto.getFormTemplateDtoList()) {
                if (mFormTemplateDto.getEffectiveDate() != null) {
                    mFormTemplateDto.setEffectiveDate(CalendarUtils.getFirstDayOfQuarter(mFormTemplateDto.getEffectiveDate()));
                } else if (mCompanyAgency.getIntuitResponsibilityStartDate() != null) {
                    mFormTemplateDto.setEffectiveDate(CalendarUtils.getFirstDayOfQuarter(mCompanyAgency.getIntuitResponsibilityStartDate()));
                } else {
                    FormTemplate ft = Application.findById(FormTemplate.class, mFormTemplateDto.getFilerType());
                    if (ft != null) {
                        mFormTemplateDto.setEffectiveDate(ft.getPaymentTemplate().getSupportStartDate());
                    }
                }

                SpcfUniqueId caftUniqueId = getCompanyAgencyFormTemplateUniqueId(mFormTemplateDto.getEffectiveDate(), mFormTemplateDto.getFilerType(), cAgencyFormTemplatesInPSP);

                if (caftUniqueId == null)  // no existance of effective date & formtemplate combination.
                {
                    CompanyAgencyFormTemplate pCompanyAgencyFT = new CompanyAgencyFormTemplate();
                    pCompanyAgencyFT.setEffectiveDate(mFormTemplateDto.getEffectiveDate());
                    pCompanyAgencyFT.setFormTemplate(Application.<FormTemplate>findById(FormTemplate.class, mFormTemplateDto.getFilerType()));
                    pCompanyAgencyFT.setCompanyAgency(mCompanyAgency);
                    mCompanyAgency.addCompanyAgencyFormTemplate(pCompanyAgencyFT);
                    Application.save(pCompanyAgencyFT);
                    cAFormTemplatesIn.add(pCompanyAgencyFT.getId());
                } else {
                    cAFormTemplatesIn.add(caftUniqueId);
                }
            }
            //retrive all form templates
            invalidateCAFTNotFoundinIncoming(cAgencyFormTemplatesInPSP, cAFormTemplatesIn);
        }
        Application.save(mCompanyAgency);

        //
        return result;
    }

    private void updateAgencyIDs(CompanyAgencyPaymentTemplate capt, CompanyAgencyPaymentTemplateDTO captDTO) {
        if (!StringUtils.equals(capt.getAgencyTaxpayerId(), captDTO.getAgencyTaxpayerId())) {
            String oldId = capt.getAgencyTaxpayerId();
            String newId = captDTO.getAgencyTaxpayerId();
            if (oldId != null && !oldId.equals(newId)) {
                CompanyEvent.createStateIdModifiedEvent(mCompanyAgency.getCompany(), capt.getPaymentTemplate().getPaymentTemplateCd(), oldId, newId);
            }
            capt.updateAgencyTaxpayerId(captDTO.getAgencyTaxpayerId());
            for (CompanyLaw companyLaw : capt.getCompanyAgency().getCompanyLawCollection().find(CompanyLaw.Law().PaymentTemplate().equalTo(capt.getPaymentTemplate()))) {
                if (companyLaw.getLaw().isPrimaryLawForStateID()) {
                    companyLaw.getQbdtPayrollItemInfo().setAgencyId(captDTO.getAgencyTaxpayerId());
                }
            }
        }
    }

    private void updateFilingAmounts(CompanyAgencyPaymentTemplate capt, List<CompanyFilingAmountDTO> filingAmounts) {
        Map<SpcfUniqueId, CompanyFilingAmountDTO> filingAmountMap = new HashMap<SpcfUniqueId, CompanyFilingAmountDTO>();
        List<CompanyFilingAmountDTO> newFilingAmounts = new ArrayList<CompanyFilingAmountDTO>();
        for (CompanyFilingAmountDTO filingAmount : filingAmounts) {
            if (filingAmount.getId() != null) {
                filingAmountMap.put(filingAmount.getId(), filingAmount);
            } else {
                newFilingAmounts.add(filingAmount);
            }
        }
        updateFilingAmounts(capt, filingAmountMap, newFilingAmounts);
    }

    private void updateFilingAmounts(CompanyAgencyPaymentTemplate capt, Map<SpcfUniqueId, CompanyFilingAmountDTO> existingFilingAmounts, List<CompanyFilingAmountDTO> newFilingAmounts) {
        for (CompanyFilingAmount companyFilingAmount : capt.getCompanyFilingAmountCollection()) {
            CompanyFilingAmountDTO filingAmountDTO = existingFilingAmounts.get(companyFilingAmount.getId());
            if (filingAmountDTO != null) {
                //update
                if (companyFilingAmount.getAmount() != filingAmountDTO.getAmount()) {
                    companyFilingAmount.setAmount(filingAmountDTO.getAmount());
                }

                SpcfCalendar domainEffectiveDate = null;
                if (companyFilingAmount.getEffectiveDate() != null) {
                    domainEffectiveDate = companyFilingAmount.getEffectiveDate().toLocal();
                }

                SpcfCalendar dtoEffectiveDate = null;
                if (filingAmountDTO.getEffectiveDate() != null) {
                    dtoEffectiveDate = filingAmountDTO.getEffectiveDate().toSpcfCalendar();
                }

                if (!ObjectUtils.equals(domainEffectiveDate, dtoEffectiveDate)) {
                    companyFilingAmount.setEffectiveDate(filingAmountDTO.getEffectiveDate().toSpcfCalendar());
                }
            } else {
                //invalidate
                companyFilingAmount.setInvalidDate(PSPDate.getPSPTime());
            }

            Application.save(companyFilingAmount);
        }

        //add
        for (CompanyFilingAmountDTO filingAmountDTO : newFilingAmounts) {
            CompanyFilingAmount companyFilingAmount = new CompanyFilingAmount();
            companyFilingAmount.setCompanyAgencyPaymentTemplate(capt);
            companyFilingAmount.setName(filingAmountDTO.getName());
            companyFilingAmount.setAmount(filingAmountDTO.getAmount());
            companyFilingAmount.setEffectiveDate(filingAmountDTO.getEffectiveDate().toSpcfCalendar());
            Application.save(companyFilingAmount);
        }
    }

    private void invalidateCAFTNotFoundinIncoming(DomainEntitySet<CompanyAgencyFormTemplate> pCAgencyFormTemplatesInPSP, ArrayList<SpcfUniqueId> pCAFormTemplatesIncoming) {
        for (CompanyAgencyFormTemplate companyAgencyFormTemplate : pCAgencyFormTemplatesInPSP) {
            if (!pCAFormTemplatesIncoming.contains(companyAgencyFormTemplate.getId()))  // If PSP Template NOT in Incoming templates LIST.
            {
                companyAgencyFormTemplate.setInvalidDate(PSPDate.getPSPTime());  //Invalidate
                Application.save(companyAgencyFormTemplate);
            }
        }
    }

    private SpcfUniqueId getCompanyAgencyFormTemplateUniqueId(SpcfCalendar pEffectiveDate, String pFilerType, DomainEntitySet<CompanyAgencyFormTemplate> agencyFormTemplates) {

        SpcfUniqueId ftUniqueId = null;
        FormTemplate formTempalteStaticData = FormTemplate.findFormTemplateByCd(pFilerType);

        for (CompanyAgencyFormTemplate agencyFormTemplate : agencyFormTemplates) {
            if (pEffectiveDate != null && agencyFormTemplate.getEffectiveDate() != null && agencyFormTemplate.getEffectiveDate().toLocal().equals(pEffectiveDate.toLocal())
                    && agencyFormTemplate.getFormTemplate().equals(formTempalteStaticData)) {
                ftUniqueId = agencyFormTemplate.getId();
                break;
            }
        }
        return ftUniqueId;
    }

    /**
     * Validate process prarameters.
     *
     * @return ProcessResult - containing any validation errors
     */
    public ProcessResult validate() {
        ProcessResult validationResult = validCompanyParameters(mSourceSystemCd, mSourceCompanyId);
        //
        if (mAgencyCd == null) {
            validationResult.getMessages().AgencyCodeNotSpecified(EntityName.CompanyAgency, null);
        }
        //
        if (mAgencyCd != null && Application.findById(Agency.class, mAgencyCd) == null) {
            validationResult.getMessages().InvalidAgencyCode(EntityName.Agency, null, mAgencyCd);
        }
        //
        if (mDto == null) {
            validationResult.getMessages().CompanyAgencyDataNotSpecified(
                    EntityName.CompanyAgency, mSourceCompanyId, mSourceSystemCd.toString(), mSourceCompanyId, mAgencyCd);
        } else {
            validationResult.merge(mDto.validateCompanyAgencyDTO());
        }
        //
        if (!validationResult.isSuccess()) {
            return validationResult;
        }
        //
        mCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, mAgencyCd);
        if (mCompanyAgency == null) {
            validationResult.getMessages().CompanyAgencyNotFound(
                    EntityName.CompanyAgency, mSourceCompanyId, mSourceSystemCd.toString(), mSourceCompanyId, mAgencyCd);
        }
        //


        if (mCompanyAgency != null && mDto != null && mDto.getFormTemplateDtoList() != null && mDto.getFormTemplateDtoList().size() > 0) {
            for (FormTemplateDTO formTemplateDTO : mDto.getFormTemplateDtoList()) {

                FormTemplate formTempalteStaticData = FormTemplate.findFormTemplateByCd(formTemplateDTO.getFilerType());
                if (formTempalteStaticData == null) {
                    validationResult.getMessages().FormTemplateError(EntityName.FormTemplate, mSourceCompanyId, formTemplateDTO.getFilerType());
                }
            }
        }
        return validationResult;
    }

}
