package com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers;

import com.intuit.ems.dataservice.v1.beans.CompanyDTO;
import com.intuit.ems.dataservice.v1.exception.DataServiceException;
import com.intuit.ems.dataservice.v1.exception.ResourceNotFoundException;
import com.intuit.ems.dataservice.v1.manager.IPayrollCompanyManager;
import com.intuit.ems.dataservice.v1.resource.EmployerPreferenceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.factories.CdmFactory;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.factories.DomainFactory;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.CompanyFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.EmployeeFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSRealmClientWrapper;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.schema.ems.v3.EmployeePreference;
import com.intuit.schema.ems.v3.EmployerPreference;
import com.intuit.schema.ems.v3.PayrollCompany;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.v4.RequestContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.FlushMode;

import java.util.*;



public class PayrollCompanyManager implements IPayrollCompanyManager {
    private static SpcfLogger logger = Application.getLogger(PayrollCompanyManager.class);

    private IUSRealmClientWrapper iusRealmClientWrapper;

    public PayrollCompanyManager() {
        iusRealmClientWrapper = PayrollApplicationBeanFactory.getBean(IUSRealmClientWrapper.class);
    }

    public PayrollCompany getPayrollCompany(String ein, String subscriptionNumber) {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = CompanyFinder.findCompany(ein, subscriptionNumber);
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            return CdmFactory.createPayrollCompany(company, employees);
        } catch (RuntimeException e) {
            String errorMessage = "Error getting company by ein=" + ein + " subscriptionNumber=" + subscriptionNumber;
            CdmHelper.logRunTimeException(logger, errorMessage, e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public PayrollCompany getPayrollCompany(String companyRealmId) {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            DomainEntitySet<Company> companies = CompanyFinder.findAllVMPCompanyByCompanyRealmId(companyRealmId);
            Company company = getActiveCompany(companies);
            CdmHelper.checkIfCompanyIsOnVmpService(company);
            Set<Employee> employeeSet = new HashSet<Employee>(EmployeeFinder.findEmployeesByCompanyRealm(companyRealmId));
            DomainEntitySet<Employee> employeeDomainEntitySet = new DomainEntitySet<Employee>(employeeSet);
            return CdmFactory.createPayrollCompany(company, employeeDomainEntitySet);
        } catch (RuntimeException e) {
            String errorMessage = "Error getting company by companyRealmId=" + companyRealmId;
            CdmHelper.logRunTimeException(logger, errorMessage, e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Company getActiveCompany(DomainEntitySet<Company> companies){
        if (companies != null && companies.size() == 1){
            return companies.getFirst();
        }

        if (companies != null && companies.size() > 1){
            Iterator<Company> comp = companies.iterator();
            while (comp.hasNext()){
                Company c = comp.next();
                Application.refresh(c);
                boolean isActive = false;
                for  (EntitlementUnit eu : c.getEntitlementUnitCollection()){
                    if (eu.isActivated()){
                        isActive = true;
                        break;
                    }
                }
                if (!isActive && c.getEntitlementUnitCollection().size() > 0){//some times entitlement could be empty if they are not on DD or Assisted service
                    comp.remove();// it means all entitlement Unit statues are in Historic or in deactivated status so removing from list
                }
            }
        }

        if(companies == null || companies.size() < 1 ) {
            throw new ResourceNotFoundException(DataServiceException.ERRNUM_PAYROLL_COMPANY_RESOURCE_NOT_FOUND);
        }
        return companies.getFirst();
    }

    @Override
    public List<EmployeePreference> getEmployeePreferencesByApp(String companyRealmId, String employeeId, String app) {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfCompanyIsOnVmpService(companyRealmId);
            List<PstubEmployeePreference> employeePreferences =
                    PstubEmployeePreference.getCompanyEmployeePreferencesByApp(companyRealmId, SpcfUniqueId.createInstance(employeeId), app);
            if(employeePreferences == null || employeePreferences.size() < 1) {
                logger.info("EmployeePreferences not found. ErrorCode=" + DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
            }
            List<EmployeePreference> cdmEmployeePreferences = new ArrayList<EmployeePreference>();
            for(PstubEmployeePreference employeePreference : employeePreferences) {
                cdmEmployeePreferences.add(CdmFactory.createEmployeePreference(employeePreference));
            }
            return cdmEmployeePreferences;
        } catch (RuntimeException e) {
            String errorMessage = "Error getting employee preferences by company and app companyRealmId=" + companyRealmId + " employeeId=" + employeeId + " app=" + app;
            CdmHelper.logRunTimeException(logger, errorMessage, e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Override
    public List<EmployerPreference> getEmployerPreferencesByApp(String companyRealmId, String appName) {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            CdmHelper.checkIfCompanyIsOnVmpService(companyRealmId);
            List<com.intuit.sbd.payroll.psp.domain.EmployerPreference> employerPreferences = com.intuit.sbd.payroll.psp.domain.EmployerPreference.findEmployerPreferences(companyRealmId, appName);
            if(employerPreferences == null || employerPreferences.size() < 1) {
                logger.info("EmployerPreferences not found. ErrorCode="+DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
            }
            List<EmployerPreference> cdmEmployerPreferences = new ArrayList<EmployerPreference>();
            for(com.intuit.sbd.payroll.psp.domain.EmployerPreference employerPreference : employerPreferences) {
                cdmEmployerPreferences.add(CdmFactory.createEmployerPreference(employerPreference));
            }
            return cdmEmployerPreferences;
        } catch (RuntimeException e) {
            String errorMessage = "Error getting employer preferences for companyRealmId:" + companyRealmId + " appName:" + appName;
            CdmHelper.logRunTimeException(logger, errorMessage, e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Override
    public List<EmployerPreference> createOrUpdateEmployerPreferences(String companyRealmId, String appName, EmployerPreferenceParams preferenceParams) {
        List<EmployerPreference> cdmEmployerPreferences = null;
        try {
            PayrollServices.beginUnitOfWork();
            CdmHelper.checkIfCompanyIsOnVmpService(companyRealmId);
            com.intuit.sbd.payroll.psp.domain.EmployerPreference domainEmployerPreference =
                    com.intuit.sbd.payroll.psp.domain.EmployerPreference.findEmployerPreference(companyRealmId, appName, preferenceParams.getPreferenceName());
            //Preference already exists so update it
            if(domainEmployerPreference != null) {
                domainEmployerPreference.setPreferenceValue(preferenceParams.getPreferenceValue());
            } else { //Preference does not exist so create it
                Company company = CompanyFinder.findCompanyByCompanyRealmId(companyRealmId);
                if(company != null) {
                    domainEmployerPreference = DomainFactory.createEmployerPreference(appName, preferenceParams, company);
                } else {
                    logger.info("No company found matching companyRealmId=" + companyRealmId+". ErrorCode="+DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
                    throw new ResourceNotFoundException("No company found matching companyRealmId:" + companyRealmId, DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
                }
            }

            Application.save(domainEmployerPreference);
            //Populate CDM response
            cdmEmployerPreferences = new ArrayList<EmployerPreference>();
            cdmEmployerPreferences.add(CdmFactory.createEmployerPreference(domainEmployerPreference));
            PayrollServices.commitUnitOfWork();
        } catch (RuntimeException e) {
            String errorMessage = "Error creating or updating employer preference for companyRealmId:" + companyRealmId;
            CdmHelper.logRunTimeException(logger, errorMessage, e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return cdmEmployerPreferences;
    }
    
     @Override
    public List<CompanyDTO> getCompaniesByConsumerRealmId(String consumerRealmId) {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            List<Company> companyList = CompanyFinder.findCompaniesByEmployeeConsumerRealmId(consumerRealmId);
            if (companyList == null || companyList.size() < 1) {
                logger.info("Companies not found. ErrorCode="+DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
                throw new ResourceNotFoundException(DataServiceException.ERRNUM_PREFERENCE_NOT_FOUND);
            }
            List<CompanyDTO> companyDTOList = new ArrayList<CompanyDTO>();
            for(Company company : companyList) {
                String realmID = company.getIAMRealmId();
                String name = company.getLegalName();
                if (realmID != null && name != null) {
                     companyDTOList.add(new CompanyDTO(realmID, name));
                }
            }
            return companyDTOList;
        } catch (RuntimeException e) {
            String errorMessage = "Error getting companies by consumer realm ID=" + consumerRealmId;
            CdmHelper.logRunTimeException(logger, errorMessage, e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to getCompaniesByConsumerRealmId
     * @param consumerRealmId
     * @return
     */
    @Override
    public List<CompanyDTO> getCompaniesByCompanyUniqueId(String consumerRealmId) {
        logger.info("VMP fix is enabled. Executing getCompaniesByCompanyUniqueId");
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            List<Company> companyList = CompanyFinder.findCompaniesByEmployeeConsumerRealmId(consumerRealmId);
            List<Company> vmpEnabledCompanyList = filterVmpEnabledCompanyList(companyList);
            logger.info("VMP Enabled Companies count = " + vmpEnabledCompanyList.size() + ".Consumer Realm Id = " +consumerRealmId);
            List<CompanyDTO> companyDTOList = getCompanyDTOList(vmpEnabledCompanyList);
            return companyDTOList;
        } catch (RuntimeException e) {
            String errorMessage = "Error getting companies by consumer realm ID=" + consumerRealmId;
            CdmHelper.logRunTimeException(logger, errorMessage, e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Override
    public List<CompanyDTO> getCompaniesByCompanyUniqueId(RequestContext context, String consumerRealmId) {
        logger.info("VMP fix is enabled. Executing getCompaniesByCompanyUniqueId");
        List<Company> vmpEnabledCompanyList = new ArrayList<>();
        List<CompanyDTO> companyDTOList = new ArrayList<>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            List<Company> companyList = CompanyFinder.findCompaniesByEmployeeConsumerRealmId(consumerRealmId);
            vmpEnabledCompanyList = filterVmpEnabledCompanyList(companyList);
            logger.info("VMP Enabled Companies count = " + vmpEnabledCompanyList.size() + ".Consumer Realm Id = " +consumerRealmId);
            logger.info("AuthId="+context.getAuthorization().getAuthId());
            companyDTOList = getCompanyDTOList(vmpEnabledCompanyList);
        } catch (RuntimeException e) {
            String errorMessage = "Error getting companies by consumer realm ID=" + consumerRealmId;
            CdmHelper.logRunTimeException(logger, errorMessage, e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        if(Objects.nonNull(vmpEnabledCompanyList)) {
            if(FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_ADD_USER_TO_COMPANY_REALM, true)) {
                for(Company company : vmpEnabledCompanyList) {
                    iusRealmClientWrapper.initiatePersonaFixIfPersonaCheckNotDone(company, context.getAuthorization().getAuthId(), consumerRealmId);
                }
            }
        }
        return companyDTOList;
    }

    private List<CompanyDTO> getCompanyDTOList(List<Company> vmpEnabledCompanyList) {
        List<CompanyDTO> companyDTOList = new ArrayList<CompanyDTO>();
        if (CollectionUtils.isNotEmpty(vmpEnabledCompanyList)) {
            vmpEnabledCompanyList.forEach(company -> {
                String uniqueId = company.getIAMRealmId();
                if (StringUtils.isBlank(uniqueId) || !StringUtils.isNumeric(uniqueId)) {
                    uniqueId = company.getId().toString();
                }
                companyDTOList.add(new CompanyDTO(uniqueId, company.getLegalName()));
            });
        }
        return companyDTOList;
    }

    private List<Company> filterVmpEnabledCompanyList(List<Company> companyList) {
        List<Company> filteredCompanyList;
        if (CollectionUtils.isNotEmpty(companyList)) {
            Company[] companyArray = new Company[companyList.size()];
            filteredCompanyList = CompanyService.filterVMPEnabledCompanies(companyList.toArray(companyArray));
        } else {
            filteredCompanyList = new ArrayList<>();
        }
        return filteredCompanyList;
    }
}
