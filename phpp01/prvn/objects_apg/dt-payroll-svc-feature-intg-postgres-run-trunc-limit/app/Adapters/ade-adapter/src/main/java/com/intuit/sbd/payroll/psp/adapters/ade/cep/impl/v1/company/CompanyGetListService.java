package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.CompanyGetListServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetListService;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.JurisdictionIdMapper;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyAgency;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.schema.payroll.v3.company.ServiceType;
import org.hibernate.ScrollableResults;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/23/14
 * Time: 4:29 PM
 */
public class CompanyGetListService extends TransactionAwareAbstractGetListService<com.intuit.schema.payroll.v3.company.Company, CompanyGetListServiceParams> {
    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        ServiceType servicetype = serviceParams.getServiceType();
        if (servicetype == null) {
            validationResult.getMessages().NullProperty(getClass(), "", CompanyResource.QUERY_PARAM_SERVICE_TYPE);
        } else if (servicetype != ServiceType.ASSISTED) {
            validationResult.getMessages().InvalidProperty(getClass(), CompanyResource.QUERY_PARAM_SERVICE_TYPE, servicetype.toString());
        }

        if (!JurisdictionIdMapper.isValidJurisdictionId(serviceParams.getJurisdiction())) {
            validationResult.getMessages().InvalidProperty(getClass(), CompanyResource.QUERY_PARAM_JURISDICTION, serviceParams.getJurisdiction());
        }

        return validationResult;
    }

    @Override
    protected ServiceResult<List<com.intuit.schema.payroll.v3.company.Company>> executeDelegate() {

        ServiceResult<List<com.intuit.schema.payroll.v3.company.Company>> serviceResult = new ServiceResult<List<com.intuit.schema.payroll.v3.company.Company>>();
        String stateCode = JurisdictionIdMapper.getStateCode(serviceParams.getJurisdiction());
        // Todo: Should we limit this to those that are active or recently cancelled?
        Criterion<Company> where = Company.CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax));
        if (stateCode != null) {
            where = where.And(Company.CompanyAgencySet().Exists(CompanyAgency.Agency().AgencyId().like(stateCode + "%")));
        }
        if (serviceParams.getTaxId() != null) {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,serviceParams.getTaxId());
            where = where.And(Company.FedTaxIdEnc().in(fedTaxIdEncList));
        }

        Expression<Company> query =
                new Query<Company>()
                        .Select(Company.SourceCompanyId().Distinct())
                        .Where(where);
        ScrollableResults pspCompanies = Application.findScrollable(Company.class, query);

        List<com.intuit.schema.payroll.v3.company.Company> companies = new ArrayList<com.intuit.schema.payroll.v3.company.Company>();
        try {
            com.intuit.schema.payroll.v3.company.Company company;
            while (pspCompanies.next()) {
                company = new com.intuit.schema.payroll.v3.company.Company();
                company.setId(String.valueOf(pspCompanies.get(0)));
                companies.add(company);
            }
        } finally {
            pspCompanies.close();
        }

        serviceResult.setResult(companies);
        return serviceResult;
    }
}
