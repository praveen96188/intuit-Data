package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.resource.CompanyResource;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.TransactionAwareAbstractGetListService;
import com.intuit.sbd.payroll.psp.adapters.ade.dg.DGCompanyValidator;
import com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.schema.payroll.v3.common.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/24/14
 * Time: 9:26 AM
 */
public class CompanyContactListService extends TransactionAwareAbstractGetListService<Contact, CompanyServiceParams> {
    @Override
    protected ServiceResult validateDelegate() {
        ServiceResult validationResult = new ServiceResult();

        if (serviceParams.getCompanyId() == null) {
            validationResult.getMessages().NullProperty(getClass(), "", CompanyResource.PATH_PARAM_COMPANY_ID);
        }

        if (DGCompanyValidator.validateDG(validationResult, serviceParams.getCompanyId())) return validationResult;

        return validationResult;
    }

    @Override
    protected ServiceResult<List<Contact>> executeDelegate() {
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>()
                                                               .Where(Company.SourceCompanyId().equalTo(serviceParams.getCompanyId())
                                                                             .And(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)))
                                                               .EagerLoad(Company.ContactSet()));

        Company company = companies.getFirst();
        List<Contact> contactList = new ArrayList<Contact>();
        if(company != null) {
            Contact payrollAdmin = CompanyTranslator.getContactRoleInfo(company, ContactRole.PayrollAdmin);
            if(payrollAdmin != null) {
                contactList.add(payrollAdmin);
            }

            Contact primaryPrincipal = CompanyTranslator.getContactRoleInfo(company, ContactRole.PrimaryPrincipal);
            if (primaryPrincipal != null) {
                contactList.add(primaryPrincipal);
            }

            contactList.addAll(CompanyTranslator.getAllContactsRoleInfo(company, ContactRole.SecondaryPrincipal));
        }

        ServiceResult<List<Contact>> serviceResult = new ServiceResult<List<Contact>>();
        serviceResult.setResult(contactList);
        return serviceResult;
    }
}
