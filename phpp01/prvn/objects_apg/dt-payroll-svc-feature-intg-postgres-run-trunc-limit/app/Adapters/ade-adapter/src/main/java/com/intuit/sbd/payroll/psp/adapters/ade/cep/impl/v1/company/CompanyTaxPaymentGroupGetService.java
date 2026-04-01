package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxPaymentGroupGetServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetService;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AgencyIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.JurisdictionIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.TaxPaymentGroupIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.schema.payroll.v3.company.*;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/24/14
 * Time: 10:43 AM
 */
public class CompanyTaxPaymentGroupGetService extends TransactionAwareAbstractGetService<TaxPaymentGroup, TaxPaymentGroupGetServiceParams> {
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

        if (serviceParams.getTaxPaymentGroupId() == null) {
            validationResult.getMessages().NullProperty(getClass(), "", "taxPaymentGroupId");
            return validationResult;
        }

        if(TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(serviceParams.getTaxPaymentGroupId()) == null) {
            validationResult.getMessages().EntityDoesNotExist(TaxPaymentGroup.class, serviceParams.getTaxPaymentGroupId());
            return validationResult;
        }

        return validationResult;
    }

    @Override
    protected ServiceResult<TaxPaymentGroup> executeDelegate() {
        ServiceResult<TaxPaymentGroup> serviceResult = new ServiceResult<TaxPaymentGroup>();

        String paymentTemplate = TaxPaymentGroupIdMapper.getPSPPaymentTemplateCdByComplianceTaxPayGroupIdId(serviceParams.getTaxPaymentGroupId());
        DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, new Query<CompanyAgency>().Where(CompanyAgency.CompanyAgencyPaymentTemplateSet()
                                                                                                                                             .Exists(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().equalTo(paymentTemplate))
                                                                                                                                             .And(CompanyAgency.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                                                                                                                                             .And(CompanyAgency.Company().SourceCompanyId().equalTo(serviceParams.getCompanyId())))
                                                                                                                         .EagerLoad(CompanyAgency.Agency(),
                                                                                                                                    CompanyAgency.CompanyAgencyPaymentTemplateSet()));

        if (companyAgencies.size() == 0) {
            serviceResult.getMessages().EntityDoesNotExist(TaxPaymentGroup.class, serviceParams.getTaxPaymentGroupId());
            return serviceResult;
        }

        CompanyAgency companyAgency = companyAgencies.getFirst();
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgency.getCompanyAgencyPaymentTemplateCollection().find(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().equalTo(paymentTemplate)).getFirst();
        serviceResult.setResult(CompanyTranslator.buildTaxPaymentGroup(companyAgency, companyAgencyPaymentTemplate, serviceParams.isShowAllTaxDepositFrequencies()));

        return serviceResult;
    }
}
