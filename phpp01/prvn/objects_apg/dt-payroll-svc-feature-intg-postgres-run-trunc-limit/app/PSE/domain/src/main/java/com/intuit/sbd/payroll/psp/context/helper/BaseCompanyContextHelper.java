package com.intuit.sbd.payroll.psp.context.helper;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.FlushMode;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract class  BaseCompanyContextHelper {

    protected abstract Collection<Company> filterCompanyList(Collection<Company> companies);

    public Company getCompanyByConsumerRealmId(String consumerRealmId) {

        Company company = null;
        try {
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            if (StringUtils.isNotBlank(consumerRealmId)) {

                Expression<Employee> query = new Query<Employee>()
                        .Where(Employee.ConsumerRealmId().equalTo(consumerRealmId));
                DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
                Collection<Company> filteredCompanies = filterCompanyList(employees.stream().map(employee -> employee.getCompany()).collect(Collectors.toSet()));
                if (CollectionUtils.isEmpty(filteredCompanies) || filteredCompanies.size() > 1) {
                    log.info("No unique company found for consumerRealmId={}", consumerRealmId);
                    return null;
                }
                return filteredCompanies.iterator().next();
            }

        } finally {
            Application.rollbackUnitOfWork();
        }
        return company;
    }

    public Company getCompanyFromCompanyUniqueId(String companyUniqueId) {

        Company company = null;
        try {
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            if (StringUtils.isNotBlank(companyUniqueId)) {
                if (isSpcfUniqueId(companyUniqueId)) {
                    return getCompanyByID(SpcfUniqueId.createInstance(companyUniqueId));
                } else {
                    return getCompanyByRealmId(companyUniqueId);
                }
            }
        } finally {
            Application.rollbackUnitOfWork();
        }
        return company;
    }

    public Company getCompanyByPSID(String sourceCompanyId) {
        boolean isActiveTransaction = false;
        Company company = null;
        try{
            isActiveTransaction = Application.hasActiveTransaction();
            if(!isActiveTransaction){
                Application.beginUnitOfWork(FlushMode.MANUAL, true);
            }
            Expression<Company> query =
                    new Query<Company>()
                            .Where(Company.SourceCompanyId().equalTo(sourceCompanyId));

            DomainEntitySet<Company> companies = Application.find(Company.class, query);
            if(CollectionUtils.isEmpty(companies)){
                return null;
            }
            company = companies.get(0);
        } finally {
            if(!isActiveTransaction){
                Application.rollbackUnitOfWork();
            }
        }
        return company;
    }

    public Company getCompanyByEIN(String ein) {

        DomainEntitySet<Company> companies = Company.searchCompaniesByEIN(ein);
        Collection<Company> filteredCompanies = filterCompanyList(companies);
        if (CollectionUtils.isEmpty(filteredCompanies) || filteredCompanies.size() > 1) {
            log.info("No unique company found for EIN");
            return null;
        }
        return filteredCompanies.iterator().next();
    }

    public Company getCompanyByRealmId(String realmId) {
        DomainEntitySet<Company> companies = Company.findAllCompaniesByRealmId(realmId);
        Collection<Company> filteredCompanies = filterCompanyList(companies);
        if (CollectionUtils.isEmpty(filteredCompanies) || filteredCompanies.size() > 1) {
            log.info("No unique company found for RealmId={}", realmId);
            return null;
        }
        return filteredCompanies.iterator().next();
    }

    /**
     * This method should be invoked only for VMP workflows
     * @param companyUniqueId company_seq or IAM_realmId
     * @param consumerRealmId consumerRealmId
     * @return company
     */
    public Company getCompanyByCFRCompanyUniqueId(String companyUniqueId, String consumerRealmId) {
        try {
            Application.beginUnitOfWork(FlushMode.MANUAL, true);

            if (isSpcfUniqueId(companyUniqueId)) {
                return getCompanyByID(SpcfUniqueId.createInstance(companyUniqueId));
            }
            Expression<Employee> query = new Query<Employee>()
                    .Where(Employee.Company().IAMRealmId().equalTo(companyUniqueId)
                            .And(Employee.ConsumerRealmId().equalTo(consumerRealmId)));

            DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
            Collection<Company> filteredCompanies = filterCompanyList(employees.stream().map(employee -> employee.getCompany()).collect(Collectors.toSet()));

            if (CollectionUtils.isEmpty(filteredCompanies) || filteredCompanies.size() > 1) {
                log.info("No unique company found for consumerRealmId={}", consumerRealmId);
                return null;
            }

            return filteredCompanies.iterator().next();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    public Company getCompanyByID(SpcfUniqueId companyId) {
        boolean isActiveTransaction = false;
        Company company = null;
        try{
            isActiveTransaction = Application.hasActiveTransaction();
            if(!isActiveTransaction){
                Application.beginUnitOfWork(FlushMode.MANUAL,true);
            }
            company = Application.findById(Company.class, companyId);
        } finally {
            if(!isActiveTransaction){
                Application.rollbackUnitOfWork();
            }
        }
        return company;
    }

    private boolean isSpcfUniqueId(String value) {
        try {
            SpcfUniqueId.createInstance(value);
        } catch (SpcfIllegalArgumentException ex) {
            return false;
        }
        return true;
    }
}
