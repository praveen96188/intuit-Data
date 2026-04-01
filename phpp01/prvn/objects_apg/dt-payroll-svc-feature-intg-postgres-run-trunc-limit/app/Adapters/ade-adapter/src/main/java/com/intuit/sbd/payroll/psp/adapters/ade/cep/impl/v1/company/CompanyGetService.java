package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetService;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/23/14
 * Time: 2:22 PM
 */

public class CompanyGetService extends TransactionAwareAbstractGetService<com.intuit.schema.payroll.v3.company.Company, CompanyServiceParams> {

    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if(serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(this.getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
        }

        Company company = Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT);
        if(company == null) {
            validationResult.getMessages().EntityDoesNotExist(Company.class, serviceParams.getCompanyId());
            return validationResult;
        }

        return validationResult;
    }

    @Override
    protected ServiceResult<com.intuit.schema.payroll.v3.company.Company> executeDelegate() {
        return CompanyTranslator.populateCompany(Company.findCompanyNoEagerLoad(serviceParams.getCompanyId(), SourceSystemCode.QBDT), serviceParams);
    }
}
