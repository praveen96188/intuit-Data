package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxItemGetListServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetListService;
import com.intuit.sbd.payroll.psp.adapters.ade.dg.DGCompanyValidator;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AdditionalFilingIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.schema.payroll.v3.company.TaxItem;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User: ihannur
 * Date: 4/23/14
 * Time: 4:29 PM
 */
public class CompanyTaxItemGetListService extends TransactionAwareAbstractGetListService<TaxItem, TaxItemGetListServiceParams> {
    protected static final SpcfLogger logger = Application.getLogger(CompanyTaxItemGetListService.class);

    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if (serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(serviceParams.getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
            return validationResult;
        }

        if (DGCompanyValidator.validateDG(validationResult, serviceParams.getCompanyId())) return validationResult;

        return validationResult;
    }

    @Override
    protected ServiceResult<List<TaxItem>> executeDelegate() {
        ServiceResult<List<TaxItem>> serviceResult = new ServiceResult<List<TaxItem>>();
        List<TaxItem> taxItemsCDM = new ArrayList<TaxItem>();

        Criterion<CompanyAgency> companyAgencyCriterion = CompanyAgency.Company().SourceCompanyId().equalTo(serviceParams.getCompanyId())
                .And(CompanyAgency.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                .And(CompanyAgency.Agency().AgencyId().notIn(CompanyTranslator.IGNORE_AGENCIES));

        if (!AuthUser.hasSAPAdminAccess()){
            companyAgencyCriterion = companyAgencyCriterion.And(CompanyAgency.Company().IsDgDisassociated().equalTo(Boolean.FALSE));
        }

        DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, new Query<CompanyAgency>()
                .Where(companyAgencyCriterion)
                .EagerLoad(CompanyAgency.CompanyAgencyPaymentTemplateSet()));

        for (CompanyAgency companyAgency : companyAgencies) {

            //Adding Company Law rates
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                TaxItem taxItem = CompanyTranslator.buildLawTaxItem(companyLaw, serviceParams.isShowAllRates());
                if (taxItem != null) {
                    taxItemsCDM.add(taxItem);
                }
            }

            //Adding additional law rates
            List <String>lawdIdAddedInResponse  = new ArrayList<String>();
            for (CompanyAgencyPaymentTemplate agencyPaymentTemplate : companyAgency.getCompanyAgencyPaymentTemplateCollection()) {
                for (CompanyFilingAmount companyFilingAmount : agencyPaymentTemplate.getCompanyFilingAmountCollection().find(CompanyFilingAmount.InvalidDate().isNull()).sort(CompanyFilingAmount.Name(),CompanyFilingAmount.EffectiveDate().Descending())) {
                    AdditionalFilingAmount additionalFilingAmount = companyFilingAmount.getAdditionalFilingAmount();
                    String adeTaxId = AdditionalFilingIdMapper.getComplianceAdditionalIdByPspAtfLawId(additionalFilingAmount.getATFLawId());

                    if (StringUtils.isEmpty(adeTaxId) || !additionalFilingAmount.getRate()) {      //This means additional law is not part of ade exchange
                        continue;
                    }
                    //To avoid duplicates  in response
                    if(lawdIdAddedInResponse.contains(adeTaxId)){
                        continue;
                    }
                    //In case of showRateAll is false  , add current effective company filing amount.
                    if(!serviceParams.isShowAllRates()  && (PSPDate.getPSPTime().before(companyFilingAmount.getEffectiveDate()))){
                        continue;
                    }
                    taxItemsCDM.add(CompanyTranslator.buildAdditionalFilingAmountTaxItem(agencyPaymentTemplate, companyFilingAmount, serviceParams.isShowAllRates()));
                    lawdIdAddedInResponse.add(adeTaxId);
                }
            }
        }

        serviceResult.setResult(taxItemsCDM);
        return serviceResult;
    }


}
