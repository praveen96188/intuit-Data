package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupUpdateServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractUpdateService;
import com.intuit.sbd.payroll.psp.adapters.ade.json.TaxItemLawMap;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.*;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ADERateUtils;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ServiceHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyFilingAmountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.schema.payroll.v3.company.TaxItem;
import com.intuit.schema.payroll.v3.company.TaxRate;
import com.intuit.schema.payroll.v3.company.TaxSetup;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/24/14
 * Time: 3:37 PM
 */
public class CompanyTaxItemUpdateService extends TransactionAwareAbstractUpdateService<TaxItem, TaxSetupUpdateServiceParams> {
    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if (serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
            return validationResult;
        }

        Company company = Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT);
        if (company == null) {
            validationResult.getMessages().EntityDoesNotExist(com.intuit.schema.payroll.v3.company.Company.class, serviceParams.getCompanyId());
            return validationResult;
        }

        String additionalFilingAmountLaw = AdditionalFilingIdMapper.getPspAtfLawIdByComplianceAdditionalId(cdmEntity.getId());
        Law law = null;
        try {
            law = SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.ADE, cdmEntity.getId());
        } catch (Exception ignore) {}

        if (cdmEntity.getId() == null || (additionalFilingAmountLaw == null && law == null)) {
            validationResult.getMessages().EntityDoesNotExist(TaxItem.class, cdmEntity.getId());
            return validationResult;
        }

        if (law != null && !isAdditionalFilingAmount(law,additionalFilingAmountLaw,cdmEntity)) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, law.getLawId());
            if (companyLaw == null) {
                validationResult.getMessages().EntityDoesNotExist(TaxItem.class, cdmEntity.getId());
                return validationResult;
            }
        }

        // Quarter start must be either Jan 1, Apr 1, July 1, or Oct 1.
        for (TaxRate taxRate : cdmEntity.getTaxRates()) {
            if (taxRate.getStartDate() != null) {
                SpcfCalendar effectiveDate = SpcfCalendar.createInstance(taxRate.getStartDate().getTime(), SpcfTimeZone.getLocalTimeZone());
                // Verify there is a valid Company Law Rate for the provided quarter.
                if (CalendarUtils.getFirstDayOfQuarter(effectiveDate).compareTo(effectiveDate) != 0) {
                    validationResult.getMessages().InvalidProperty(TaxRate.class, "StartDate", taxRate.getStartDate().toString());
                    return validationResult;
                }
            } else {
                validationResult.getMessages().NullProperty(TaxRate.class, null, "StartDate");
                return validationResult;
            }
        }

        return validationResult;
    }

    @Override
    protected ServiceResult<TaxItem> executeDelegate() {
        ServiceResult<TaxItem> serviceResult = new ServiceResult<TaxItem>();

        String additionalFilingAmountLaw = AdditionalFilingIdMapper.getPspAtfLawIdByComplianceAdditionalId(cdmEntity.getId());

        Law law = null;
        try {
            law = SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.ADE, cdmEntity.getId());
        } catch (Exception ignore) {}

        if (law != null && !isAdditionalFilingAmount(law,additionalFilingAmountLaw,cdmEntity)) {
            for (TaxRate taxRate : cdmEntity.getTaxRates()) {
                SpcfCalendar effectiveDate = SpcfCalendar.createInstance(taxRate.getStartDate().getTime(), SpcfTimeZone.getLocalTimeZone());
                BigDecimal taxRateConverted = ADERateUtils.convertPercentageToDecimal(law, taxRate.getRate());
                String state = law.getPaymentTemplate().getAgency().getJurisdiction().getJurisdictionID();

                //This if condition only RI is added to calculate WBI tax. After 2014/Q2 onwards ADE compliance tool will handle it. Remove this condition and keep code present in else part.
                Law baseLaw = null;
                if(TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME) != null) {
                    baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
                }
                if(baseLaw == null && law.getLawCategoryCode().equals(LawCategoryCode.UnemploymentEmployer)) {
                    baseLaw = law;
                }

                if (!rateIsValidValueAndWithinRange(law, taxRateConverted)) {
                    serviceResult.getMessages().InvalidProperty(TaxRate.class, "Rate", taxRate.getRate().toString());
                    return serviceResult;
                }

                CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT), law.getLawId());
                CompanyLawRate currentRate = CompanyLawRate.findEffectiveLawRate(companyLaw, effectiveDate);
                if (currentRate == null || currentRate.getRate() != taxRateConverted.doubleValue()) {
                    // Update the rate.
                    ServiceHelper.mergeServiceResultWithProcessResult(serviceResult, PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, serviceParams.getCompanyId(), law, effectiveDate, taxRateConverted.doubleValue(), true));
                }
            }
        } else if (additionalFilingAmountLaw != null) {
            serviceResult.merge(updateAdditionalFilingRate(additionalFilingAmountLaw));
        }

        return serviceResult;
    }

    @Override
    protected TaxItem refreshEntity() {
        // we're not refreshing entity here, because this service is never called by it's self. The parent service is responsible for returning the correct cdm
        return null;
    }

    private ServiceResult<TaxSetup> updateAdditionalFilingRate(String atfLawId) {
        ServiceResult<TaxSetup> serviceResult = new ServiceResult<TaxSetup>();

        Company company = Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT);
        AdditionalFilingAmount additionalFilingAmount = Application.find(AdditionalFilingAmount.class, AdditionalFilingAmount.ATFLawId().equalTo(atfLawId)).getFirst();
        if (additionalFilingAmount == null) {
            serviceResult.getMessages().GenericValidationMessage(getClass(), "", "Additional filing rate does not exist for law id " + cdmEntity.getId());
            return serviceResult;
        }else if(!additionalFilingAmount.getRate()) {
            serviceResult.getMessages().GenericValidationMessage(getClass(), "", "Additional filing is not a rate and cannot be update through the rate interface. " + cdmEntity.getId());
            return serviceResult;
        }

        for (TaxRate taxRate : cdmEntity.getTaxRates()) {
            SpcfCalendar effectiveDate = SpcfCalendar.createInstance(taxRate.getStartDate().getTime(), SpcfTimeZone.getLocalTimeZone());
            CompanyFilingAmount companyFilingAmount = getCompanyFilingAmount(company, additionalFilingAmount, effectiveDate);
            BigDecimal rateInDecimal = ADERateUtils.convertPercentageToDecimal(taxRate.getRate());
            if (companyFilingAmount != null && companyFilingAmount.getAmount() == rateInDecimal.doubleValue()) {
                // we already have the current rate
                return serviceResult;
            }

            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, additionalFilingAmount.getPaymentTemplate().getAgency().getAgencyId());
            if (companyAgency == null) {
                serviceResult.getMessages().EntityDoesNotExist(TaxItem.class, cdmEntity.getId());
                return serviceResult;
            }

            CompanyFilingAmountDTO companyFilingAmountDTO = new CompanyFilingAmountDTO();
            companyFilingAmountDTO.setName(additionalFilingAmount.getName());
            companyFilingAmountDTO.setEffectiveDate(new DateDTO(effectiveDate));
            companyFilingAmountDTO.setAmount(rateInDecimal.doubleValue());
            ServiceHelper.mergeServiceResultWithProcessResult(serviceResult, PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT,
                                                                                                                                           serviceParams.getCompanyId(),
                                                                                                                                           companyFilingAmountDTO));
        }
        return serviceResult;
    }

    public static CompanyFilingAmount getCompanyFilingAmount(Company company, AdditionalFilingAmount additionalFilingAmount, SpcfCalendar effectiveDate) {
        CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, additionalFilingAmount.getPaymentTemplate());
        return CompanyFilingAmount.findCompanyFilingAmounts(capt, additionalFilingAmount.getName(), effectiveDate).getFirst();
    }

    private static boolean rateIsValidValueAndWithinRange(Law law, BigDecimal rate) {
        return law.rateIsValidValue(SpcfDecimal.createInstance(rate.doubleValue())) && law.rateFallsWithinRange(SpcfDecimal.createInstance(rate.doubleValue()));
    }

    /**
     *
     * @param law
     * @param additionalFilingAmountLaw
     * @return
     */
    public static boolean isAdditionalFilingAmount(Law law, String additionalFilingAmountLaw,TaxItem taxitem) {
        boolean isLaw = false;
        boolean isAdditionalLaw = false;
        if (law != null && additionalFilingAmountLaw != null) {
            if (!law.getPaymentTemplate().getNoCalculation()) {
                isLaw = true;
            }
            AdditionalFilingAmount additionalFilingAmount = Application.find(AdditionalFilingAmount.class, AdditionalFilingAmount.ATFLawId().equalTo(additionalFilingAmountLaw)).getFirst();
            if (!additionalFilingAmount.getPaymentTemplate().getNoCalculation()) {
                isAdditionalLaw = true;
            }
            if (isAdditionalLaw && !isLaw) {
                return true;
            } else {
                isLaw = false;
                isAdditionalLaw = false;
                if (taxitem.getAgencyId() != null) {
                    String lawAgencyID = null;
                    String addnalLawAgencyID = null;
                    try {
                        lawAgencyID = AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(law.getPaymentTemplate().getAgency().getAgencyId());
                        addnalLawAgencyID = AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(additionalFilingAmount.getPaymentTemplate().getAgency().getAgencyId());

                    } catch (Throwable th) {
                        //Nothing to do
                    }
                    if (addnalLawAgencyID != null && addnalLawAgencyID.equals(taxitem.getAgencyId())) {
                        isAdditionalLaw = true;
                    }
                    if (lawAgencyID != null && lawAgencyID.equals(taxitem.getAgencyId())) {
                        isLaw = true;
                    }
                    if (isAdditionalLaw && !isLaw) {
                        return true;
                    }
                } else if (taxitem.getJurisdictionId() != null) {
                    String state = JurisdictionIdMapper.getStateCode(taxitem.getJurisdictionId());
                    String addnalLawAgencyJurisdiction = null;
                    String lawAgencyJurisdiction = null;
                    try {
                        addnalLawAgencyJurisdiction = additionalFilingAmount.getPaymentTemplate().getAgency().getJurisdiction().getJurisdictionID();
                        lawAgencyJurisdiction = law.getPaymentTemplate().getAgency().getJurisdiction().getJurisdictionID();
                    } catch (Throwable th) {
                        //Nothing to do
                    }
                    if (addnalLawAgencyJurisdiction != null && addnalLawAgencyJurisdiction.equals(state)) {
                        isAdditionalLaw = true;
                    }
                    if (lawAgencyJurisdiction != null && lawAgencyJurisdiction.equals(state)) {
                        isLaw = true;
                    }
                    if (isAdditionalLaw && !isLaw) {
                        return true;
                    }
                }
            }

        } else if (additionalFilingAmountLaw != null) {
            return true;
        }
        return false;
    }
}
