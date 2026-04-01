package com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.manager;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.CompanyDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.CompanyListDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.factory.WorkersCompFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Sriram Nutakki
 * Date created: 8/28/13
 */
public class WorkersCompManager {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompManager.class);

    public CompanyDTO getCompanyByPSID(String psid) {
        CompanyDTO dto = null;
        try {
            if (StringUtils.isBlank(psid)) {
                throw new IllegalArgumentException("Invalid psid");
            }
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            if (company != null) {
                dto = WorkersCompFactory.createCompany(company);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return dto;
    }

    public CompanyDTO getCompanyByEINAndSubsNum(String ein, String subsNo) {
        CompanyDTO dto = null;
        Criterion<Company> companyCriterion = null;
        try {
            if (StringUtils.isBlank(ein) || StringUtils.isBlank(subsNo)) {
                throw new IllegalArgumentException("Invalid ein or subscription number");
            }
            List<String> companyEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, ein);
            List<String> euEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName, ein);
            companyCriterion = Company.FedTaxIdEnc().in(companyEinEncList)
                    .And(Company.EntitlementUnitSet().Exists(
                            EntitlementUnit.FedTaxIdEnc().in(euEinEncList)
                                    .And(EntitlementUnit.Entitlement().SubscriptionNumber().equalTo(subsNo))));

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Expression<Company> query =
                    new Query<Company>().Where(companyCriterion);
            Set<Company> companies = Application.find(Company.class, query);
            Set<Company> activeCompanies = new HashSet<Company>();
            Set<Company> activePrimaryCompanies = new HashSet<Company>();
            Set<Company> inactiveCompanies = new HashSet<Company>();
            if (companies != null && companies.size() > 0) {
                for (Company company : companies) {
                    for (EntitlementUnit unit : company.getEntitlementUnitCollection()) {
                        if (unit.isActivated()) {
                            activeCompanies.add(company);
                            if(unit.getEntitlement().getEntitlementCode().getIsPrimary()) {
                                activePrimaryCompanies.add(company);
                            }
                        } else {
                            inactiveCompanies.add(company);
                        }
                    }
                }
            }

            Company company = null;
            if (activePrimaryCompanies.size() == 1 && activeCompanies.size() == 1) {
                company = activePrimaryCompanies.toArray(new Company[0])[0];
            }
            else if (activePrimaryCompanies.size() == 0 && activeCompanies.size() == 1) {
                company = activeCompanies.toArray(new Company[0])[0];
            }
            else if (activePrimaryCompanies.size() == 0 && activeCompanies.size() == 0 && inactiveCompanies.size() == 1) {
                company = inactiveCompanies.toArray(new Company[0])[0];
            }

            if (company != null) {
                dto = WorkersCompFactory.createCompany(company);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return dto;
    }


    public CompanyListDTO getCompaniesByEINAndSubsNum(String ein, String subsNo) {
        CompanyListDTO dto = null;
        Criterion<Company> companyCriterion = null;
        try {
            if (StringUtils.isBlank(ein) || StringUtils.isBlank(subsNo)) {
                throw new IllegalArgumentException("Invalid ein or subscription number");
            }
            List<String> companyEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, ein);
            List<String> euEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName, ein);
            companyCriterion = Company.FedTaxIdEnc().in(companyEinEncList)
                    .And(Company.EntitlementUnitSet().Exists(
                            EntitlementUnit.FedTaxIdEnc().in(euEinEncList)
                                    .And(EntitlementUnit.Entitlement().SubscriptionNumber().equalTo(subsNo))));
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Expression<Company> query =
                    new Query<Company>().Where(companyCriterion);
            Set<Company> companies = Application.find(Company.class, query);
            if (companies != null && companies.size() > 0) {
                dto = new CompanyListDTO();
                for (Company company : companies) {
                    dto.addCompany(WorkersCompFactory.createCompany(company, ein, subsNo));
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return dto;
    }

    public CompanyListDTO getCompaniesByEIN(String ein){
        CompanyListDTO dto = null;
        Criterion<Company> companyCriterion = null;
        try {
            if (StringUtils.isBlank(ein)) {
                throw new IllegalArgumentException("Invalid ein");
            }
            List<String> companyEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, ein);
            List<String> euEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName, ein);
            companyCriterion = Company.FedTaxIdEnc().in(companyEinEncList)
                    .And(Company.EntitlementUnitSet().Exists(
                            EntitlementUnit.FedTaxIdEnc().in(euEinEncList)));
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Expression<Company> query =
                    new Query<Company>().Where(companyCriterion);
            Set<Company> companies = Application.find(Company.class, query);
            if (companies != null && companies.size() > 0) {
                dto = new CompanyListDTO();
                for (Company company : companies) {
                    dto.addCompany(WorkersCompFactory.createCompany(company, ein));
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return dto;
    }
}
