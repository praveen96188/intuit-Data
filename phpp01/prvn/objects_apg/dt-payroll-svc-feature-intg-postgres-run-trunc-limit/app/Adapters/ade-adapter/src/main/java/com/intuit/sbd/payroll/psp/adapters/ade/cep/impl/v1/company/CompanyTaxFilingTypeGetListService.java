package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetListService;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.schema.payroll.v3.company.TaxFilingType;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * shivanandad069
 * 2/25/2015
 */
public class CompanyTaxFilingTypeGetListService extends TransactionAwareAbstractGetListService<TaxFilingType, CompanyServiceParams> {
    protected static final SpcfLogger logger = Application.getLogger(CompanyTaxFilingTypeGetListService.class);

    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if (serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(serviceParams.getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
            return validationResult;
        }

        return validationResult;
    }

    @Override
    protected ServiceResult<List<TaxFilingType>> executeDelegate() {
        // Only return current filing type forward, not historical
        boolean showAllFilerTypeWithHistory = false;
        ServiceResult<List<TaxFilingType>> serviceResult = new ServiceResult<List<TaxFilingType>>();
        List<TaxFilingType> taxFilingTypesCDM = new ArrayList<TaxFilingType>();


        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(SourceSystemCode.QBDT, serviceParams.getCompanyId(), Agency.IRS);
        if (companyAgency == null) {
            return serviceResult;
        }
        DomainEntitySet<CompanyAgencyFormTemplate> companyAgencyFormTemplateSet = null;
        SpcfCalendar now = PSPDate.getPSPTime();
        if (showAllFilerTypeWithHistory) {
            companyAgencyFormTemplateSet = companyAgency.getCompanyAgencyFormTemplateCollection().find(CompanyAgencyFormTemplate.FormTemplate().PaymentTemplate().equalTo(PaymentTemplate.getIRS_941()))
                                                        .sort(CompanyAgencyFormTemplate.EffectiveDate().Descending());
        } else {
            companyAgencyFormTemplateSet = companyAgency.getCompanyAgencyFormTemplateCollection().find(CompanyAgencyFormTemplate.FormTemplate().PaymentTemplate().equalTo(PaymentTemplate.getIRS_941())
                                                        .And(CompanyAgencyFormTemplate.InvalidDate().isNull()).And(CompanyAgencyFormTemplate.EffectiveDate().lessOrEqualThan(now).Or(CompanyAgencyFormTemplate.EffectiveDate().isNull())))
                                                        .sort(CompanyAgencyFormTemplate.EffectiveDate().Descending());
        }

        for (CompanyAgencyFormTemplate agencyFormTemplate : companyAgencyFormTemplateSet) {

            if (!showAllFilerTypeWithHistory && agencyFormTemplate.getEffectiveDate().compareTo(now) > 0) {
                continue;
            }
            TaxFilingType taxFilingType = CompanyTranslator.buildTaxFilingType(agencyFormTemplate);
            if (taxFilingType.getFilingType() != null) {
                taxFilingTypesCDM.add(taxFilingType);
            }
            //After getting filer type with effective date  less than or equal, break the loop to make sure we have only active filer type for current quarter
            if (!showAllFilerTypeWithHistory && agencyFormTemplate.getEffectiveDate().compareTo(now) <= 0) {
                break;
            }
        }
        serviceResult.setResult(taxFilingTypesCDM);
        return serviceResult;
    }


}
