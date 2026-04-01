package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxItemGetServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetService;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AdditionalFilingIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.RateConverterFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.schema.payroll.v3.company.TaxItem;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/28/14
 * Time: 8:24 AM
 */
public class CompanyTaxItemGetService extends TransactionAwareAbstractGetService<TaxItem, TaxItemGetServiceParams> {
    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if(serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
            return validationResult;
        }

        Company pspCompany = Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT);
        if (pspCompany == null) {
            validationResult.getMessages().EntityDoesNotExist(com.intuit.schema.payroll.v3.company.Company.class, serviceParams.getCompanyId());
            return validationResult;
        }

        if(serviceParams.getTaxItemId() == null) {
            validationResult.getMessages().NullProperty(getClass(), "", CompanyResource.PATH_PARAM_TAX_ITEM_ID);
            return validationResult;
        }

        return validationResult;
    }

    @Override
    protected ServiceResult<TaxItem> executeDelegate() {
        ServiceResult<TaxItem> serviceResult = new ServiceResult<TaxItem>();

        Law law = null;
        try {
            law = SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.ADE, serviceParams.getTaxItemId());
        } catch (Exception ignore) {}

        String additionalFilingAmountId = null;
        if (serviceParams.getTaxItemId() != null && law == null) {
            additionalFilingAmountId = AdditionalFilingIdMapper.getPspAtfLawIdByComplianceAdditionalId(serviceParams.getTaxItemId());
        }

        if (law == null && additionalFilingAmountId == null) {
            serviceResult.getMessages().EntityDoesNotExist(TaxItem.class, serviceParams.getTaxItemId());
            return serviceResult;
        }

        Company company = Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT);
        if (additionalFilingAmountId != null) {
            TaxItem taxItem = new TaxItem();
            taxItem.setId(serviceParams.getTaxItemId());

            // Get active Company filing amounts only, active filing amount's history is fetched based on isShowAllRates flag when building CDM for filing amount in CompanyTranslator
            DomainEntitySet<CompanyFilingAmount> companyFilingAmounts = Application.find(CompanyFilingAmount.class, CompanyFilingAmount.CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(company)
                                                                                                                                       .And(CompanyFilingAmount.InvalidDate().isNull()));
            for (CompanyFilingAmount companyFilingAmount : companyFilingAmounts) {
                AdditionalFilingAmount additionalFilingAmount = companyFilingAmount.getAdditionalFilingAmount();
                if(additionalFilingAmount.getATFLawId().equals(additionalFilingAmountId) && additionalFilingAmount.getRate()) {
                    serviceResult.setResult(CompanyTranslator.buildAdditionalFilingAmountTaxItem(companyFilingAmount.getCompanyAgencyPaymentTemplate(), companyFilingAmount, serviceParams.isShowAllRates()));
                }
            }

            if(serviceResult.getResult() == null) {
                serviceResult.getMessages().EntityDoesNotExist(TaxItem.class, serviceParams.getTaxItemId());
            }

            return serviceResult;
        } else {
            CompanyLaw companyLaw = Application.find(CompanyLaw.class, CompanyLaw.CompanyAgency().Company().equalTo(company)
                                                                                    .And(CompanyLaw.Law().equalTo(law))).getFirst();
            if (companyLaw == null) {
                serviceResult.getMessages().EntityDoesNotExist(TaxItem.class, serviceParams.getTaxItemId());
            } else {
                serviceResult.setResult(CompanyTranslator.buildLawTaxItem(companyLaw, serviceParams.isShowAllRates()));
            }
            return serviceResult;
        }
    }
}
