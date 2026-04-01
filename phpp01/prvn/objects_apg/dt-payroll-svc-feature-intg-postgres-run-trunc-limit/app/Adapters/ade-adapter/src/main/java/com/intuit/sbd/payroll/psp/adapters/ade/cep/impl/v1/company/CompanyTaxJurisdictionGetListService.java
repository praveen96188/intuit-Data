package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetListService;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.JurisdictionIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator;
import com.intuit.sbd.payroll.psp.domain.CompanyAgency;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.schema.payroll.v3.company.TaxJurisdiction;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ihannur
 * Date: 4/24/14
 * Time: 9:49 AM
 */
public class CompanyTaxJurisdictionGetListService extends TransactionAwareAbstractGetListService<TaxJurisdiction, CompanyServiceParams> {

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
    protected ServiceResult<List<TaxJurisdiction>> executeDelegate() {
        ServiceResult<List<TaxJurisdiction>> serviceResult = new ServiceResult<List<TaxJurisdiction>>();
        List<TaxJurisdiction> taxJurisdictionList = new ArrayList<TaxJurisdiction>();

        DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, CompanyAgency.Company().SourceCompanyId().equalTo(serviceParams.getCompanyId())
                                                                                                            .And(CompanyAgency.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                                                                                                            .And(CompanyAgency.Agency().AgencyId().notIn(CompanyTranslator.IGNORE_AGENCIES)));
        TaxJurisdiction taxJurisdictionCDM;
        for (CompanyAgency companyAgency : companyAgencies) {
            taxJurisdictionCDM = new TaxJurisdiction();
            taxJurisdictionCDM.setId(JurisdictionIdMapper.getComplianceJurisdictionId("US", companyAgency.getAgency().getJurisdiction().getJurisdictionID()));
            taxJurisdictionList.add(taxJurisdictionCDM);
        }
        serviceResult.setResult(taxJurisdictionList);
        return serviceResult;
    }
}
