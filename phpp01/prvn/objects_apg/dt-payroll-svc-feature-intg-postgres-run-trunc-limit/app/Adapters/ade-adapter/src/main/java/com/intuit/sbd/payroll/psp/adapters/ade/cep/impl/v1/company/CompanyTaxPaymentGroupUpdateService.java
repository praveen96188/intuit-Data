package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ResourceNameEnum;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxPaymentGroupGetServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxPaymentGroupUpdateServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.ServiceFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractUpdateService;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.*;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ServiceHelper;
import com.intuit.sbd.payroll.psp.adapters.ade.validator.CompanyValidator;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyFilingAmountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.schema.payroll.v3.common.FrequencyEnum;
import com.intuit.schema.payroll.v3.company.AdditionalFilingAmount;
import com.intuit.schema.payroll.v3.company.TaxDepositFrequency;
import com.intuit.schema.payroll.v3.company.TaxPaymentGroup;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * User: TimothyD698
 * Date: 4/23/14
 */
public class CompanyTaxPaymentGroupUpdateService extends TransactionAwareAbstractUpdateService<TaxPaymentGroup, TaxPaymentGroupUpdateServiceParams> {
    private static SpcfLogger logger = Application.getLogger(CompanyTaxPaymentGroupUpdateService.class);

    private EffectiveDepositFrequencyDTO mEffectiveDepositFrequencyDTO;

    @Override
    protected ServiceResult validateDelegate() {
        try {
            ServiceResult validationResult = new ServiceResult();
            if (serviceParams.getCompanyId() == null) {
                validationResult.getMessages().NullProperty(getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
                return validationResult;
            }

            Company company = Application.find(Company.class, new Query<Company>().Where(Company.SourceCompanyId().equalTo(serviceParams.getCompanyId())
                            .And(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)))
                    .EagerLoad(Company.CompanyAgencySet())).getFirst();
            if (company == null) {
                validationResult.getMessages().EntityDoesNotExist(com.intuit.schema.payroll.v3.company.Company.class, serviceParams.getCompanyId());
                return validationResult;
            }
            pspRequestContextManager.setRequestContextCompany(company);

            validationResult.merge(CompanyValidator.validateFullServiceCompany(company));
            if (validationResult.notSuccess()) {
                return validationResult;
            }

            if (cdmEntity == null) {
                validationResult.getMessages().NullProperty(TaxPaymentGroup.class, null, "TaxPaymentGroup");
                return validationResult;
            }

            if (cdmEntity.getId() == null) {
                validationResult.getMessages().NullProperty(TaxPaymentGroup.class, "", "Id");
                return validationResult;
            }

            if (TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(cdmEntity.getId()) == null) {
                validationResult.getMessages().InvalidValue(TaxPaymentGroup.class, "", "Id", cdmEntity.getId());
                return validationResult;
            }

            // Check if Payment Template Exists
            PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(cdmEntity.getId()));
            if (paymentTemplate == null) {
                validationResult.getMessages().EntityDoesNotExist(TaxPaymentGroup.class, cdmEntity.getId());
                return validationResult;
            }

            if (StringUtils.isNotBlank(cdmEntity.getAgencyId())) {
                if (AgencyIdMapper.getPSPAgencyIdByComplianceAgencyId(cdmEntity.getAgencyId()) == null) {
                    validationResult.getMessages().InvalidValue(TaxPaymentGroup.class, "", "AgencyId", cdmEntity.getAgencyId());
                    return validationResult;
                }
                Agency agency = Application.findById(Agency.class, AgencyIdMapper.getPSPAgencyIdByComplianceAgencyId(cdmEntity.getAgencyId()));
                if (agency == null) {
                    validationResult.getMessages().EntityDoesNotExist(com.intuit.schema.payroll.v3.company.Agency.class, cdmEntity.getAgencyId());
                    return validationResult;
                }

                if (!paymentTemplate.getAgency().equals(agency)) {
                    validationResult.getMessages().InvalidValue(TaxPaymentGroup.class, null, "AgencyId", cdmEntity.getAgencyId());
                    return validationResult;
                }
            }

            CompanyAgency companyAgency = company.getCompanyAgencyCollection().findEntity(CompanyAgency.Agency().equalTo(paymentTemplate.getAgency()));
            if (companyAgency == null) {
                validationResult.getMessages().EntityDoesNotExist(com.intuit.schema.payroll.v3.company.Agency.class, AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(paymentTemplate.getAgency().getAgencyId()));
                return validationResult;
            }

            // Check if Payment Template is assigned to the company
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(companyAgency, paymentTemplate);
            if (companyAgencyPaymentTemplate == null) {
                validationResult.getMessages().EntityDoesNotExist(TaxPaymentGroup.class, cdmEntity.getId());
                return validationResult;
            }

            if (!cdmEntity.getDepositFrequencies().isEmpty()) {
                if (cdmEntity.getDepositFrequencies().size() > 1) {
                    validationResult.getMessages().InvalidSize(TaxDepositFrequency.class, null, "TaxDepositFrequency", "1", "1");
                    return validationResult;
                }

                //If DF update - check if payment template uses deposit frequency of another payment template
                if (!PaymentTemplate.getUsesFrequencyOf(paymentTemplate.getPaymentTemplateCd()).equals(paymentTemplate.getPaymentTemplateCd())) {
                    validationResult.getMessages().GenericValidationMessage(TaxPaymentGroup.class, "", "Deposit frequency update is not allowed on TaxPaymentGroup: " + cdmEntity.getId() +
                            " because this uses the frequency of TaxPaymentGroup: " + TaxPaymentGroupIdMapper.getComplianceTaxPayGroupIdByPSPPaymentTemplateCd(PaymentTemplate.getUsesFrequencyOf(paymentTemplate.getPaymentTemplateCd())));
                    return validationResult;
                }

                TaxDepositFrequency taxDepositFrequency = cdmEntity.getDepositFrequencies().get(0);
                if (taxDepositFrequency == null) {
                    validationResult.getMessages().NullProperty(TaxPaymentGroup.class, "", "DepositFrequencies");
                    return validationResult;
                }

                FrequencyEnum newDepositFrequency = taxDepositFrequency.getFrequency();
                if (newDepositFrequency == null) {
                    validationResult.getMessages().NullProperty(TaxDepositFrequency.class, "", "Frequency");
                    return validationResult;
                }

                if (FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(newDepositFrequency) == null) {
                    validationResult.getMessages().InvalidValue(TaxDepositFrequency.class, "", "Frequency", newDepositFrequency.name());
                    return validationResult;
                }

                // get effective date for this deposit frequency
                if (taxDepositFrequency.getStartDate() == null) {
                    validationResult.getMessages().NullProperty(TaxDepositFrequency.class, null, "StartDate");
                    return validationResult;
                }

                SpcfCalendar depositFrequencyEffectiveDate = CalendarUtils.convertToSpcfCalendar(taxDepositFrequency.getStartDate());

                mEffectiveDepositFrequencyDTO = createDTO(paymentTemplate, depositFrequencyEffectiveDate, newDepositFrequency);
                //Check if PaymentFrequencyId is supported for the Payment Template and find PaymentTemplateFrequency
                PaymentTemplateFrequency paymentTemplateFrequency = paymentTemplate.findSupportedPaymentTemplateFrequency(mEffectiveDepositFrequencyDTO.getPaymentFrequencyId());
                if (paymentTemplateFrequency == null) {
                    validationResult.getMessages().EntityDoesNotExist(TaxDepositFrequency.class, mEffectiveDepositFrequencyDTO.getPaymentFrequencyId().name());
                    return validationResult;
                }
                if (paymentTemplateFrequency.getObsolete()) {
                    logger.info("Source company id:" + serviceParams.getCompanyId() + ", Payment template frequency is obsolete for payment template: " + paymentTemplate.getPaymentTemplateCd() +
                            " and frequency: " + mEffectiveDepositFrequencyDTO.getPaymentFrequencyId().toString());
                    validationResult.getMessages().EntityDoesNotExist(TaxDepositFrequency.class, mEffectiveDepositFrequencyDTO.getPaymentFrequencyId().name());
                    return validationResult;
                }

                // Check for active company Laws, if no active company laws skip
                if (!companyAgencyPaymentTemplate.hasActiveLaw()) {
                    validationResult.getMessages().GenericValidationMessage(TaxPaymentGroup.class, "", "Skip - No active Laws");
                    return validationResult;
                }

                // Check if threshold is met, if so get the DepositFrequency after threshold hit date. If new deposit frequency is more frequent than current DF (DF after threshold) then skip DF update
                DepositFrequencyCode dfCodeAfterExceedingThreshold = getDepositFrequencyAfterExceedingThreshold(company, paymentTemplate, depositFrequencyEffectiveDate);
                if (dfCodeAfterExceedingThreshold != null && DepositFrequencyUtil.compareDepositFrequencyCodes(FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(newDepositFrequency), dfCodeAfterExceedingThreshold) <= 0) {
                    validationResult.getMessages().GenericValidationMessage(TaxPaymentGroup.class, "", "Skip - Threshold Met");
                    return validationResult;
                }
            }

            for (AdditionalFilingAmount additionalFilingAmount : cdmEntity.getAdditionalFilingAmounts()) {
                if (additionalFilingAmount.getId() == null) {
                    validationResult.getMessages().NullProperty(AdditionalFilingAmount.class, "", "Id");
                    return validationResult;
                } else if (AdditionalFilingIdMapper.getPspAtfLawIdByComplianceAdditionalId(additionalFilingAmount.getId()) == null) {
                    validationResult.getMessages().InvalidValue(AdditionalFilingAmount.class, additionalFilingAmount.getId(), "Id", additionalFilingAmount.getId());
                    return validationResult;
                }

                if (additionalFilingAmount.getAmount() == null) {
                    validationResult.getMessages().NullProperty(AdditionalFilingAmount.class, "", "Amount");
                    return validationResult;
                } else if (additionalFilingAmount.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    validationResult.getMessages().InvalidValue(AdditionalFilingAmount.class, additionalFilingAmount.getId(), "Amount", additionalFilingAmount.getAmount().toString());
                    return validationResult;
                }

                if (additionalFilingAmount.getEffectiveDate() == null) {
                    validationResult.getMessages().NullProperty(AdditionalFilingAmount.class, "", "EffectiveDate");
                    return validationResult;
                } else {
                    SpcfCalendar effectiveDate = SpcfCalendar.createInstance(additionalFilingAmount.getEffectiveDate().getTime(), SpcfTimeZone.getLocalTimeZone());
                    if (CalendarUtils.getFirstDayOfQuarter(effectiveDate).compareTo(effectiveDate) != 0) {
                        validationResult.getMessages().GenericValidationMessage(AdditionalFilingAmount.class, additionalFilingAmount.getId(), "Effective date must be the first day of a quarter");
                        return validationResult;
                    }
                }
            }

            return validationResult;
        } finally {
            pspRequestContextManager.clearRequestContextCompany();
        }
    }

    @Override
    protected ServiceResult executeDelegate() {
        try {
            pspRequestContextManager.setRequestContextCompanyFromPSID(serviceParams.getCompanyId());
            ServiceResult serviceResult = new ServiceResult();

            if (mEffectiveDepositFrequencyDTO != null) {
                ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, serviceParams.getCompanyId(), mEffectiveDepositFrequencyDTO);
                ServiceHelper.mergeServiceResultWithProcessResult(serviceResult, processResult);
            }

            if (!cdmEntity.getAdditionalFilingAmounts().isEmpty()) {
                Company company = Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT);

                for (AdditionalFilingAmount cdmAdditionalFilingAmount : cdmEntity.getAdditionalFilingAmounts()) {
                    com.intuit.sbd.payroll.psp.domain.AdditionalFilingAmount domainAdditionalFilingAmount =
                            Application.find(com.intuit.sbd.payroll.psp.domain.AdditionalFilingAmount.class, com.intuit.sbd.payroll.psp.domain.AdditionalFilingAmount.ATFLawId().equalTo(AdditionalFilingIdMapper.getPspAtfLawIdByComplianceAdditionalId(cdmAdditionalFilingAmount.getId()))).getFirst();
                    if (domainAdditionalFilingAmount == null) {
                        serviceResult.getMessages().GenericValidationMessage(getClass(), "", "Additional filing amount does not exist for id " + cdmEntity.getId());
                        return serviceResult;
                    } else if (domainAdditionalFilingAmount.getRate()) {
                        serviceResult.getMessages().GenericValidationMessage(getClass(), "", "Additional filing is a rate and cannot be update through the additional amount interface. " + cdmEntity.getId());
                        return serviceResult;
                    }

                    SpcfCalendar effectiveDate = SpcfCalendar.createInstance(cdmAdditionalFilingAmount.getEffectiveDate().getTime(), SpcfTimeZone.getLocalTimeZone());
                    CompanyFilingAmount companyFilingAmount = getCompanyFilingAmount(company, domainAdditionalFilingAmount, effectiveDate);

                    if (companyFilingAmount != null && companyFilingAmount.getAmount() == cdmAdditionalFilingAmount.getAmount().doubleValue()) {
                        continue;
                    }

                    CompanyFilingAmountDTO companyFilingAmountDTO = new CompanyFilingAmountDTO();
                    companyFilingAmountDTO.setName(domainAdditionalFilingAmount.getName());
                    companyFilingAmountDTO.setEffectiveDate(new DateDTO(effectiveDate));
                    companyFilingAmountDTO.setAmount(cdmAdditionalFilingAmount.getAmount().doubleValue());
                    ServiceHelper.mergeServiceResultWithProcessResult(serviceResult, PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT,
                            serviceParams.getCompanyId(),
                            companyFilingAmountDTO));
                }
            }

            return serviceResult;
        } finally {
            pspRequestContextManager.clearRequestContextCompany();
        }
    }

    @Override
    protected TaxPaymentGroup refreshEntity() {
        try {
            pspRequestContextManager.setRequestContextCompanyFromPSID(serviceParams.getCompanyId());
            TaxPaymentGroupGetServiceParams taxPaymentGroupGetServiceParams = new TaxPaymentGroupGetServiceParams();
            taxPaymentGroupGetServiceParams.setTaxPaymentGroupId(cdmEntity.getId());
            taxPaymentGroupGetServiceParams.setCompanyId(serviceParams.getCompanyId());
            return ServiceFactory.getInstance().<TaxPaymentGroup, TaxPaymentGroupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroupGetServiceParams).getResult();
        }finally {
            pspRequestContextManager.clearRequestContextCompany();
        }
    }

    private EffectiveDepositFrequencyDTO createDTO(PaymentTemplate pPaymentTemplate, SpcfCalendar pDepositFrequencyEffectiveDate, FrequencyEnum pNewDepositFrequency) {
        EffectiveDepositFrequencyDTO depositFrequencyDTO = new EffectiveDepositFrequencyDTO();

        depositFrequencyDTO.setAgencyId(pPaymentTemplate.getAgency().getAgencyId());
        depositFrequencyDTO.setEffectiveDate(pDepositFrequencyEffectiveDate);
        depositFrequencyDTO.setPaymentFrequencyId(FrequencyMapper.getPSPDepositFrequencyCodeByCDMFrequency(pNewDepositFrequency));
        depositFrequencyDTO.setPaymentTemplateCd(pPaymentTemplate.getPaymentTemplateCd());

        return depositFrequencyDTO;
    }

    private DepositFrequencyCode getDepositFrequencyAfterExceedingThreshold(Company pCompany, PaymentTemplate pPaymentTemplate, SpcfCalendar pDepositFrequencyEffectiveDate) {
        // Get quarter boundary for new deposit frequency effective date
        SpcfCalendar quarterStartDate = CalendarUtils.getFirstDayOfQuarter(pDepositFrequencyEffectiveDate);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(pDepositFrequencyEffectiveDate);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEventWithDetailsEagerLoaded(pCompany, EventTypeCode.ThresholdExceeded, EventDetailTypeCode.PaymentTemplate, pPaymentTemplate.getPaymentTemplateCd());

        for (CompanyEvent companyEvent : companyEvents) {
            String thresholdStart = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdPeriodStartDate);
            String thresholdEnd = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdPeriodEndDate);
            if (companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdReversed) == null && thresholdStart != null && thresholdEnd != null) {
                SpcfCalendar thresholdStartDateUTC = SpcfCalendar.parse("d", thresholdStart);
                SpcfCalendar thresholdEndDateUTC = SpcfCalendar.parse("d", thresholdEnd);
                SpcfCalendar thresholdStartDate = SpcfCalendar.createInstance(thresholdStartDateUTC.getYear(), thresholdStartDateUTC.getMonth(), thresholdStartDateUTC.getDay(), SpcfTimeZone.getLocalTimeZone());
                SpcfCalendar thresholdEndDate = SpcfCalendar.createInstance(thresholdEndDateUTC.getYear(), thresholdEndDateUTC.getMonth(), thresholdEndDateUTC.getDay(), SpcfTimeZone.getLocalTimeZone());
                if (thresholdStartDate.between(quarterStartDate, quarterEndDate) || thresholdEndDate.between(quarterStartDate, quarterEndDate)) {
                    //Getting DF effective after threshold is met
                    thresholdEndDate.addDays(1);
                    EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(pCompany, pPaymentTemplate, thresholdEndDate);
                    if(effectiveDepositFrequency != null) {
                        return effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId();
                    }
                }
            }
        }
        return null;
    }

    public static CompanyFilingAmount getCompanyFilingAmount(Company company, com.intuit.sbd.payroll.psp.domain.AdditionalFilingAmount additionalFilingAmount, SpcfCalendar effectiveDate) {
        CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, additionalFilingAmount.getPaymentTemplate());
        return CompanyFilingAmount.findCompanyFilingAmounts(capt, additionalFilingAmount.getName(), effectiveDate).getFirst();
    }
}
