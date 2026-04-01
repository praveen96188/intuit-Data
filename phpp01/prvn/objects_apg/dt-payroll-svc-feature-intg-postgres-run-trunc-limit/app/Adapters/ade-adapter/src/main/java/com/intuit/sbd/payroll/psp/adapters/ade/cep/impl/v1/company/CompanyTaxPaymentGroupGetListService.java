package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxPaymentGroupGetListServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetListService;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.schema.payroll.v3.company.TaxPaymentGroup;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/24/14
 * Time: 12:42 PM
 */
public class CompanyTaxPaymentGroupGetListService extends TransactionAwareAbstractGetListService<TaxPaymentGroup, TaxPaymentGroupGetListServiceParams> {
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

        return validationResult;
    }

    @Override
    protected ServiceResult<List<TaxPaymentGroup>> executeDelegate() {
        Company company = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)
                                                                                            .And(Company.SourceCompanyId().equalTo(serviceParams.getCompanyId())))
                                                                              .EagerLoad(Company.CompanyAgencySet())).getFirst();
        ServiceResult<List<TaxPaymentGroup>> serviceResult = new ServiceResult<List<TaxPaymentGroup>>();
        serviceResult.setResult(CompanyTranslator.buildTaxPaymentTemplates(serviceParams.isShowAllTaxDepositFrequencies(), company));
        return serviceResult;
    }
}
