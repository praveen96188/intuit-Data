package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetListService;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AgencyIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.JurisdictionIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator;
import com.intuit.sbd.payroll.psp.domain.CompanyAgency;
import com.intuit.sbd.payroll.psp.domain.CompanyAgencyPaymentTemplate;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.schema.payroll.v3.company.Agency;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/24/14
 * Time: 9:00 AM
 */
public class CompanyAgencyListService extends TransactionAwareAbstractGetListService<Agency, CompanyServiceParams> {

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
    protected ServiceResult<List<Agency>> executeDelegate() {
        DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, new Query<CompanyAgency>()
                                                                                                   .Where(CompanyAgency.Company().SourceCompanyId().equalTo(serviceParams.getCompanyId())
                                                                                                                       .And(CompanyAgency.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                                                                                                                       .And(CompanyAgency.Agency().AgencyId().notIn(CompanyTranslator.IGNORE_AGENCIES)))
                                                                                                   .EagerLoad(CompanyAgency.CompanyAgencyPaymentTemplateSet()));

        List<Agency> agencyCDMList = new ArrayList<Agency>();
        for (CompanyAgency companyAgency : companyAgencies) {
            Agency agencyCDM = new Agency();
            agencyCDM.setId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(companyAgency.getAgency().getAgencyId()));
            DomainEntitySet<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplateDomain = companyAgency.getCompanyAgencyPaymentTemplateCollection().find(CompanyAgencyPaymentTemplate.AgencyTaxpayerIdEnc().isNotNull());
            if(companyAgencyPaymentTemplateDomain.size() > 0) {
                CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgencyPaymentTemplateDomain.getFirst();
                agencyCDM.setEmployerAccountNumber(companyAgencyPaymentTemplate.getAgencyTaxpayerId());
            }

            agencyCDM.setJurisdictionId(JurisdictionIdMapper.getComplianceJurisdictionId("US", companyAgency.getAgency().getJurisdiction().getJurisdictionID()));
            agencyCDM.setName(companyAgency.getAgency().getName());
            agencyCDMList.add(agencyCDM);
        }

        ServiceResult<List<Agency>> serviceResult = new ServiceResult<List<Agency>>();
        serviceResult.setResult(agencyCDMList);
        return serviceResult;
    }
}
