package com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders;

import com.intuit.ems.dataservice.v1.exception.DataServiceException;
import com.intuit.ems.dataservice.v1.exception.ResourceNotFoundException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User: michaelp696
 *
 * Used for finding a PSP company
 */
public class CompanyFinder {
    private static SpcfLogger logger = Application.getLogger(CompanyFinder.class);
    public static Company findCompany(String ein, String subscriptionNumber) {
        final String resourceIdentifierMessage = "ein=" + ein + " subscriptionNumber=" + subscriptionNumber;
        Criterion<EntitlementUnit> entitlementUnitCriterion = EntitlementUnit.Entitlement().SubscriptionNumber().equalTo(subscriptionNumber);
        if(ein == null){
            entitlementUnitCriterion = entitlementUnitCriterion.And(EntitlementUnit.FedTaxIdEnc().isNull());
        } else {
            List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName, ein);
            entitlementUnitCriterion = entitlementUnitCriterion.And(EntitlementUnit.FedTaxIdEnc().in(einEncList));
        }

        if(!AuthUser.hasSAPAdminAccess()){
            entitlementUnitCriterion = entitlementUnitCriterion.And(EntitlementUnit.Company().IsDgDisassociated().equalTo(Boolean.FALSE));
        }

        Expression<EntitlementUnit> query =
                new Query<EntitlementUnit>()
                        .Where(entitlementUnitCriterion);
        DomainEntitySet<EntitlementUnit> entitlementUnits = Application.find(EntitlementUnit.class, query);

        SpcfUniqueId entitlementId = null;
        List<EntitlementUnit> activeEntitlementUnit = new ArrayList<EntitlementUnit>();
        List<EntitlementUnit> inactiveEntitlementUnit = new ArrayList<EntitlementUnit>();
        for (EntitlementUnit entitlementUnit : entitlementUnits) {
            if (entitlementId == null) {
                entitlementId = entitlementUnit.getEntitlement().getId();
            } else {
                if (!entitlementId.equals(entitlementUnit.getEntitlement().getId())) {
                    throw new ResourceNotFoundException(resourceIdentifierMessage, DataServiceException.ERRNUM_PAYROLL_COMPANY_RESOURCE_NOT_FOUND);
                }
            }

            if (entitlementUnit.isActivated()) {
                activeEntitlementUnit.add(entitlementUnit);
            } else {
                inactiveEntitlementUnit.add(entitlementUnit);
            }
        }

        if (!activeEntitlementUnit.isEmpty()) {
            if (activeEntitlementUnit.size() > 1) {
                throw new ResourceNotFoundException(resourceIdentifierMessage, DataServiceException.ERRNUM_PAYROLL_COMPANY_RESOURCE_NOT_FOUND);
            }
            return activeEntitlementUnit.get(0).getCompany();
        }

        if (!inactiveEntitlementUnit.isEmpty()) {
            if (inactiveEntitlementUnit.size() > 1) {
                throw new ResourceNotFoundException(resourceIdentifierMessage, DataServiceException.ERRNUM_PAYROLL_COMPANY_RESOURCE_NOT_FOUND);
            }
            return inactiveEntitlementUnit.get(0).getCompany();
        }

        throw new ResourceNotFoundException(resourceIdentifierMessage, DataServiceException.ERRNUM_PAYROLL_COMPANY_RESOURCE_NOT_FOUND);
    }

    //Returns a list of companies that an employee with a given consumer realm id works for
    public static List<Company> findCompaniesByEmployeeConsumerRealmId(String consumerRealmId) {
        logger.info(String.format("v4log findCompaniesByEmployeeConsumerRealmId started consumerRealmId=%s",consumerRealmId));
        List<Company> companies = null;
        Expression<Employee> query = new Query<Employee>()
                .Where(Employee.ConsumerRealmId().equalTo(consumerRealmId))
                .OrderBy(Employee.TerminationDate(), Employee.HireDate());
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        if(employees != null) {
            companies = new ArrayList<Company>();
            for(Employee employee : employees) {
                companies.add(employee.getCompany());
            }
        }
        Expression<VmpEmployeeInfo> queryVmp = new Query<VmpEmployeeInfo>()
                .Where(VmpEmployeeInfo.ConsumerRealmId().equalTo(consumerRealmId));
        DomainEntitySet<VmpEmployeeInfo> vmpEmployees = Application.find(VmpEmployeeInfo.class, queryVmp);
        if(vmpEmployees != null){
            if(Objects.isNull(companies)) {
                companies = new ArrayList<Company>();
            }
            for (VmpEmployeeInfo vmpEmployeeInfo : vmpEmployees){
                Company company = vmpEmployeeInfo.getCompany();
                if(!companies.contains(company))
                    companies.add(company);
            }
        }
        logger.info(String.format("v4log findCompaniesByEmployeeConsumerRealmId finished consumerRealmId=%s",consumerRealmId));
        return companies;
    }

    public static Company findCompanyByCompanyRealmId(String companyRealmId) {
        logger.info(String.format("v4log findCompanyByCompanyRealmId started companyRealmId=%s",companyRealmId));
        Company company = null;
        Expression<Company> query = new Query<Company>()
                .Where(Company.IAMRealmId().equalTo(companyRealmId));
        DomainEntitySet<Company> companies = Application.find(Company.class, query);
        if(companies == null || companies.size() < 1) {
            throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLL_COMPANY_RESOURCE_NOT_FOUND);
        }
        company = companies.getFirst();
        if(companies.size() > 1) {
            logger.warn(String.format("v4log findCompanyByCompanyRealmId multiple companies found for companyRealmId=%s numCompanies=%s Returning first company in the list",
                    companyRealmId, companies.size()));
        }
        logger.info(String.format("v4log findCompanyByCompanyRealmId finished companyRealmId=%s",companyRealmId));
        return company;
    }

    public static DomainEntitySet<Company> findAllVMPCompanyByCompanyRealmId(String companyRealmId) {
        logger.info(String.format("v4log findAllVMPCompanyByCompanyRealmId started companyRealmId=%s",companyRealmId));
        Expression<Company> query = new Query<Company>()
                .Where(Company.IAMRealmId().equalTo(companyRealmId).And(Company.CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(ServiceCode.ViewMyPaycheck))));
        DomainEntitySet<Company> companies = Application.find(Company.class, query);
        logger.info(String.format("v4log findAllVMPCompanyByCompanyRealmId finished companyRealmId=%s",companyRealmId));
        return companies;
    }

    public static Company findCompanyByPsid(String psid) {
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if(company == null) {
            throw new ResourceNotFoundException("psid=" + psid, DataServiceException.ERRNUM_PAYROLL_COMPANY_RESOURCE_NOT_FOUND);
        }
        return company;
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to findCompanyByCompanyRealmId
     * @param companyUniqueId- can be either company seq or company realm id
     * @return
     */
    public static DomainEntitySet<Company> findCompanyByCompanyUniqueId(String companyUniqueId) {
        Expression<Company> query;

        if(CdmHelper.isSpcfUniqueId(companyUniqueId)) {
            query = new Query<Company>()
                    .Where(Company.Id().equalTo(SpcfUniqueId.createInstance(companyUniqueId)));
        }else{
            query = new Query<Company>()
                    .Where(Company.IAMRealmId().equalTo(companyUniqueId));
        }
        DomainEntitySet<Company> companies = Application.find(Company.class, query);
        if(CollectionUtils.isEmpty(companies)) {
            throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLL_COMPANY_RESOURCE_NOT_FOUND);
        }
        return companies;
    }
}
