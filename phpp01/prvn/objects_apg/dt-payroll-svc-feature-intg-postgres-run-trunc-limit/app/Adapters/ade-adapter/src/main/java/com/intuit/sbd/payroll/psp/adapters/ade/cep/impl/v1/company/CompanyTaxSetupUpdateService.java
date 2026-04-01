package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ResourceNameEnum;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxPaymentGroupGetListServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxPaymentGroupUpdateServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupGetServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupUpdateServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.ServiceFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractUpdateService;
import com.intuit.sbd.payroll.psp.adapters.ade.json.TaxItemLawMap;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.IRateConverter;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.RateConverterFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.validator.CompanyValidator;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.schema.payroll.v3.company.Agency;
import com.intuit.schema.payroll.v3.company.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/24/14
 * Time: 1:39 PM
 */
public class CompanyTaxSetupUpdateService extends TransactionAwareAbstractUpdateService<TaxSetup, TaxSetupUpdateServiceParams> {

    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if (serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
            return validationResult;
        }

        Company company = Company.findCompany(serviceParams.getCompanyId(), SourceSystemCode.QBDT);
        if (company == null) {
            validationResult.getMessages().EntityDoesNotExist(com.intuit.schema.payroll.v3.company.Company.class, serviceParams.getCompanyId());
            return validationResult;
        }

        validationResult.merge(CompanyValidator.validateFullServiceCompany(company));
        if (validationResult.notSuccess()) {
            return validationResult;
        }

        if (cdmEntity == null) {
            validationResult.getMessages().NullProperty(TaxSetup.class, null, "TaxSetup");
            return validationResult;
        }

        return validationResult;
    }

    @Override
    protected ServiceResult executeDelegate() {
        ServiceResult serviceResult = new ServiceResult();

        for (Agency agency : cdmEntity.getAgencies()) {
            serviceResult.merge(ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.AGENCIES).service(agency, serviceParams));
        }

        for (TaxPaymentGroup taxPaymentGroup : cdmEntity.getTaxPaymentGroups()) {
            TaxPaymentGroupUpdateServiceParams taxPaymentGroupUpdateServiceParams = new TaxPaymentGroupUpdateServiceParams();
            taxPaymentGroupUpdateServiceParams.setCompanyId(serviceParams.getCompanyId());
            serviceResult.merge(ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXPAYMENTGROUPS).service(taxPaymentGroup, taxPaymentGroupUpdateServiceParams));
        }

        // If UI base rate item is present, checking supplemental/surcharge taxItem is missing in ADE request if any for the baseLaw Item. If missing adding taxItem
        if (cdmEntity.getTaxItems().size() > 0) {
            Date effectiveDate = null;
            Map<Law, BigDecimal> rates = new HashMap<Law, BigDecimal>();
            for (TaxItem taxItem : cdmEntity.getTaxItems()) {
                Law law;
                try {
                    law = SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.ADE, taxItem.getId());
                } catch (Exception ignore) {
                    continue;
                }
                Law baseLaw = null;
                String state = law.getPaymentTemplate().getAgency().getJurisdiction().getJurisdictionID();
                try {
                    baseLaw = Application.findById(Law.class, TaxItemLawMap.getLawId(state, TaxItemLawMap.BASE_RATE_NAME));
                } catch (Exception ex) {
                    if (law.getLawCategoryCode().equals(LawCategoryCode.UnemploymentEmployer)) {
                        baseLaw = law;
                    }
                }
                if (law.getLawCategoryCode().equals(LawCategoryCode.UnemploymentEmployer) || law.equals(baseLaw)) {
                    IRateConverter converter = RateConverterFactory.createInstance(state, true);
                    if (taxItem.getTaxRates().size() > 0) {
                        rates = converter.getRates(state, taxItem.getTaxRates().get(0).getRate(), null);
                        effectiveDate = taxItem.getTaxRates().get(0).getStartDate();
                    }
                }
                if (effectiveDate != null || !rates.isEmpty()) {
                    break;
                }
            }
            if (!rates.isEmpty()) {
                for (TaxItem taxItem : cdmEntity.getTaxItems()) {
                    Law law;
                    try {
                        law = SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.ADE, taxItem.getId());
                    } catch (Exception ignore) {
                        continue;
                    }
                    rates.keySet().remove(law);
                }
                for (Law law : rates.keySet()) {
                    String sourceIdBySourceSystemAndLaw;
                    try {
                        sourceIdBySourceSystemAndLaw = SourceSystemLawAssoc.findSourceIdBySourceSystemAndLaw(SourceSystemCode.ADE, law);
                    } catch (Exception ignore) {
                        continue;
                    }
                    TaxItem taxItem = new TaxItem();
                    taxItem.setId(sourceIdBySourceSystemAndLaw);
                    taxItem.setTaxRates(new ArrayList<TaxRate>());
                    TaxRate taxRate = new TaxRate();
                    taxRate.setRate(rates.get(law));
                    taxRate.setStartDate(effectiveDate);
                    taxItem.getTaxRates().add(taxRate);
                    cdmEntity.getTaxItems().add(taxItem);
                }
            }

            // Updating TaxItems after adding missing TaxItems if any
            for (TaxItem taxItem : cdmEntity.getTaxItems()) {
                serviceResult.merge(ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXITEMS).service(taxItem, serviceParams));
            }
        }

        List<TaxFilingType> taxFilingTypes = cdmEntity.getTaxFilingTypes();
        if (taxFilingTypes == null) {
            taxFilingTypes = new ArrayList<TaxFilingType>();
        }
        for (TaxFilingType taxFilingType : taxFilingTypes) {
            CompanyServiceParams companyServiceParams = new CompanyServiceParams();
            companyServiceParams.setCompanyId(serviceParams.getCompanyId());
            serviceResult.merge(ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXFILINGTYPES).service(taxFilingType, companyServiceParams));
        }
        return serviceResult;
    }

    @Override
    protected TaxSetup refreshEntity() {
        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(serviceParams.getCompanyId());
        return ((TaxSetup) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams).getResult());
    }
}
