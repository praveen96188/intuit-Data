package com.intuit.sbd.payroll.psp.adapters.cdmadapter;


import com.intuit.ems.dataservice.v1.exception.AccessDeniedException;
import com.intuit.ems.dataservice.v1.exception.DataServiceException;
import com.intuit.ems.dataservice.v1.exception.ResourceNotFoundException;
import com.intuit.ems.dataservice.v1.resource.EmployerPreferenceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers.PayrollCompanyManager;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.AssertHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedDisasterRecoveryTests;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.schema.ems.v3.EmployeePreference;
import com.intuit.schema.ems.v3.PayrollCompany;
import com.intuit.schema.ems.v3.PayrollContact;
import com.intuit.schema.ems.v3.PayrollEmployee;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;


public class PayrollCompanyManagerTests {
    private final String psid = "123456789";
    private final String companyRealmId = "123456";

    @Before
    public void startUp() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void shutdown() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testGetPayrollCompany() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company, "1234567890", "123456", EditionType.Basic, NumberOfEmployeesType.UNLIMITED, DataLoadServices.AssetItemNumber.DIY_YEARLY, PSPDate.getPSPTime(), "4263", "Visa", "03/16", "89511", "John Doe", "test@intuit.com", PSPDate.getPSPTime());
        Entitlement entitlement = entitlementUnit.getEntitlement();
        String subscriptionNumber = entitlement.getSubscriptionNumber();
        String ein = entitlementUnit.getFedTaxId();
        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();
        PayrollCompany payrollCompany = payrollCompanyManager.getPayrollCompany(ein, subscriptionNumber);
        AssertHelper.assertCompaniesMatch(company, payrollCompany);

        List<PayrollContact> payrollContactList = payrollCompany.getContact();
        boolean matchingId;
        for(Contact contact : company.getContactCollection()) {
            matchingId = false;
            //Find matching contact
            for(PayrollContact payrollContact : payrollContactList) {
                if(payrollContact.getId().equalsIgnoreCase(contact.getId().toString())) {
                    AssertHelper.assertContactMatches(contact, payrollContact);
                    matchingId = true;
                    break;
                }
            }
            if(!matchingId) {
                Assert.fail("Missing contact with id:" + contact.getSourceContactId());
            }
        }

        Expression<Employee> query = new Query<Employee>()
                .Where(Employee.Company().equalTo(company))
                .EagerLoad(Employee.MailingAddress(), Employee.Company());
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        List<PayrollEmployee> payrollEmployeeList = payrollCompany.getEmployee();
        for(Employee employee : employees) {
            matchingId = false;
            //Find matching employee
            for(PayrollEmployee payrollEmployee : payrollEmployeeList ) {
                if(payrollEmployee.getId().equalsIgnoreCase(employee.getId().toString())) {
                    AssertHelper.assertEmployeeMatches(employee, payrollEmployee);
                    matchingId = true;
                    break;
                }
            }
            if(!matchingId) {
                Assert.fail("Missing employee with id:" + employee.getSourceEmployeeId());
            }
        }
    }

    @Test
    public void testCreateEmployerPreference() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
        DataLoadServices.addServices(company, ServiceCode.ViewMyPaycheck);
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();
        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();
        EmployerPreferenceParams params = new EmployerPreferenceParams();
        params.setPreferenceName(EmployerPreference.HIDE_PAYCHECKS_DATED_IN_THE_FUTURE);
        params.setPreferenceValue(EmployerPreference.ON);
        payrollCompanyManager.createOrUpdateEmployerPreferences(companyRealmId, EmployerPreference.VMP, params);

        List<com.intuit.schema.ems.v3.EmployerPreference> employerPreferences = payrollCompanyManager.getEmployerPreferencesByApp(companyRealmId, EmployerPreference.VMP);
        Assert.assertEquals(1, employerPreferences.size());
        com.intuit.schema.ems.v3.EmployerPreference employerPreference = employerPreferences.get(0);
        Assert.assertEquals(EmployerPreference.HIDE_PAYCHECKS_DATED_IN_THE_FUTURE, employerPreference.getPreferenceName());
        Assert.assertEquals(EmployerPreference.ON, employerPreference.getPreferenceValue());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testCreateEmployerPreferenceInvalidCompanyRealmId() {
        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();
        EmployerPreferenceParams params = new EmployerPreferenceParams();
        params.setPreferenceName(EmployerPreference.HIDE_PAYCHECKS_DATED_IN_THE_FUTURE);
        params.setPreferenceValue(EmployerPreference.ON);
        payrollCompanyManager.createOrUpdateEmployerPreferences(companyRealmId, EmployerPreference.VMP, params);
    }

    @Test
    public void testUpdateEmployerPreference() {
        //Create
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
        DataLoadServices.addServices(company, ServiceCode.ViewMyPaycheck);
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();
        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();
        EmployerPreferenceParams params = new EmployerPreferenceParams();
        params.setPreferenceName(EmployerPreference.HIDE_PAYCHECKS_DATED_IN_THE_FUTURE);
        params.setPreferenceValue(EmployerPreference.ON);
        payrollCompanyManager.createOrUpdateEmployerPreferences(companyRealmId, EmployerPreference.VMP, params);
        //Update
        params.setPreferenceName(EmployerPreference.HIDE_PAYCHECKS_DATED_IN_THE_FUTURE);
        params.setPreferenceValue(EmployerPreference.OFF);
        payrollCompanyManager.createOrUpdateEmployerPreferences(companyRealmId, EmployerPreference.VMP, params);

        List<com.intuit.schema.ems.v3.EmployerPreference> employerPreferences = payrollCompanyManager.getEmployerPreferencesByApp(companyRealmId, EmployerPreference.VMP);
        Assert.assertEquals(1, employerPreferences.size());
        com.intuit.schema.ems.v3.EmployerPreference employerPreference = employerPreferences.get(0);
        Assert.assertEquals(EmployerPreference.HIDE_PAYCHECKS_DATED_IN_THE_FUTURE, employerPreference.getPreferenceName());
        Assert.assertEquals(EmployerPreference.OFF, employerPreference.getPreferenceValue());
    }

    @Test
    public void testGetEmployeePreferencesByApp() {
        String ssn = "123456789";
        String consumerRealmId = "123456";
        //Create company with an employee and associate in VMP
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Employee employee = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
        //Create employee preference
        String appName = "VMP";
        String preferenceName = "Email";
        String preferenceValue = "Yes";
        PstubEmployeePreference employeePreference = new PstubEmployeePreference();
        employeePreference.setEmployee(employee);
        employeePreference.setAppName(appName);
        employeePreference.setPreferenceName(preferenceName);
        employeePreference.setPreferenceValue(preferenceValue);
        PstubEmployeePreference.createEmployeePreference(employeePreference);
        //Update company with a company realm
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        //Read the employees preferences
        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();
        List<EmployeePreference> employeePreferences = payrollCompanyManager.getEmployeePreferencesByApp(companyRealmId, employee.getId().toString(), appName);
        Assert.assertEquals(1, employeePreferences.size());
        EmployeePreference cdmEmployeePreference = employeePreferences.get(0);
        Assert.assertEquals(employee.getId().toString(), cdmEmployeePreference.getEmployeeId());
        Assert.assertEquals(appName, cdmEmployeePreference.getAppName());
        Assert.assertEquals(preferenceName, cdmEmployeePreference.getPreferenceName());
        Assert.assertEquals(preferenceValue, cdmEmployeePreference.getPreferenceValue());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetEmployeePreferencesByAppWrongCompany() {
        String ssn = "123456789";
        String consumerRealmId = "123456";
        //Create company with an employee and associate in VMP
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Employee employee = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
        //Create employee preference
        String appName = "VMP";
        String preferenceName = "Email";
        String preferenceValue = "Yes";
        PstubEmployeePreference employeePreference = new PstubEmployeePreference();
        employeePreference.setEmployee(employee);
        employeePreference.setAppName(appName);
        employeePreference.setPreferenceName(preferenceName);
        employeePreference.setPreferenceValue(preferenceValue);
        PstubEmployeePreference.createEmployeePreference(employeePreference);
        //Create another company with a companyRealmId that the employee we are trying to find is not an employee of
        String wrongCompanyPsid = "234567890";
        VmpTestUtil.setupCompanyCreateEmployee(wrongCompanyPsid);
        Application.beginUnitOfWork();
        Company wrongCompany = Company.findCompany(wrongCompanyPsid, SourceSystemCode.QBDT);
        wrongCompany.setIAMRealmId(companyRealmId);
        Application.save(wrongCompany);
        Application.commitUnitOfWork();

        //Read the employees preferences
        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();
        //Shouldn't find any preferences since the employee is not for this company
        payrollCompanyManager.getEmployeePreferencesByApp(companyRealmId, employee.getId().toString(), appName);
    }

    @Test
    public void testCompanyVmpServiceCancelled() {
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        String consumerRealmId = "123456";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());

        //Cancel company service
        Application.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.ViewMyPaycheck, ServiceSubStatusCode.Cancelled);
        Application.commitUnitOfWork();

        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();

        //These employer APIs should fail when ViewMyPaycheck service is cancelled
        try {
            payrollCompanyManager.getPayrollCompany(companyRealmId);
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            Assert.assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }

        try {
            payrollCompanyManager.getEmployerPreferencesByApp(companyRealmId, "VMP");
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            Assert.assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }

        try {
            payrollCompanyManager.createOrUpdateEmployerPreferences(companyRealmId, "VMP", new EmployerPreferenceParams());
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            Assert.assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }

        try {
            payrollCompanyManager.getEmployeePreferencesByApp(companyRealmId, employee.getId().toString(), "VMP");
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            Assert.assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }
    }

    @Test
    public void testGetPayrollCompanyForSingleVMP(){

        //create VMP company
        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        companyDTO.setIAMRealmId("234567890");
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addViewMyPaycheckService(company);

        PayrollCompanyManager payrollCompanyManager  = new PayrollCompanyManager();
        PayrollCompany payrollCompany  = payrollCompanyManager.getPayrollCompany("234567890");
        //should get the VMP companies
        Assert.assertEquals("123456789", payrollCompany.getId());

    }

    @Test
    public void testGetPayrollCompanyForDiffVMP(){
        String companyRealm1 = "234567890";
        String companyRealm2 = "234567891";

        //create VMP company
        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        companyDTO.setIAMRealmId(companyRealm1);
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addViewMyPaycheckService(company);

        //create VMP company
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "223456789");
        companyDTO.setIAMRealmId(companyRealm2);
        company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addViewMyPaycheckService(company);

        PayrollCompanyManager payrollCompanyManager  = new PayrollCompanyManager();
        PayrollCompany payrollCompany  = payrollCompanyManager.getPayrollCompany(companyRealm1);
        //should get the VMP companies
        Assert.assertEquals("123456789", payrollCompany.getId());



        payrollCompany  = payrollCompanyManager.getPayrollCompany(companyRealm2);
        //should get the VMP companies
        Assert.assertEquals("223456789", payrollCompany.getId());

    }

    @Test
    public void testGetPayrollCompanyForActiveVMP(){
        String companyRealm = "234567890";

        //create VMP company
        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "123456789");
        companyDTO.setIAMRealmId(companyRealm);
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);
        DataLoadServices.addViewMyPaycheckService(company);

        //create Non VMP company
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "223456789");
        companyDTO.setIAMRealmId(companyRealm);
        company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);

        //create VMP company & de-activated Entitlement
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "333456789");
        companyDTO.setIAMRealmId(companyRealm);
        company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addDDService(company);
        DataLoadServices.addViewMyPaycheckService(company);
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.makeHistoricEntitlementUnit(entitlementUnit);

        PayrollCompanyManager payrollCompanyManager  = new PayrollCompanyManager();
        PayrollCompany payrollCompany  = payrollCompanyManager.getPayrollCompany(companyRealm);
        //should get the active VMP
        Assert.assertEquals("123456789", payrollCompany.getId());

    }

    @Test
    public void testGetPayrollCompanyForMultipleVMP(){
        String companyRealm = "234567890";
        PayrollCompany payrollCompany = null;
        String psid1 = "123456789";
        String psid2 = "223456789";
        //create VMP company
        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, psid1);
        companyDTO.setIAMRealmId(companyRealm);
        Company company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addViewMyPaycheckService(company);

        //create VMP company again
        companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, psid2);
        companyDTO.setIAMRealmId(companyRealm);
        company = DataLoadServices.newCompany(companyDTO, "123456");
        DataLoadServices.addViewMyPaycheckService(company);
        PayrollCompanyManager payrollCompanyManager  = new PayrollCompanyManager();
        //due to multiple active VMP companies the one of the VMP company (first from list) will be returned
        //in case of multiple active VMP companies scenario we are assuming that the customer (companyRealm) is allowed to view the of multiple company's data as both them are having same companyRealm
        payrollCompany = payrollCompanyManager.getPayrollCompany(companyRealm);
        Assert.assertNotNull("Found payroll company is null",payrollCompany);
        Assert.assertTrue("Unexpected PSID", (psid1.equalsIgnoreCase(payrollCompany.getId()) || psid2.equalsIgnoreCase(payrollCompany.getId())));

    }

    @Test
    public void testGetPayrollCompanyForMulVMPEMP(){

        String psid = "123456";
        String psid2 = "12345678";
        String consumerRealmId = "123456";
        String consumerRealmId2 = "1234567";

        Employee employee = VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, consumerRealmId);
        Employee employee2 = VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid2, consumerRealmId2);

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "0987123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Company company2 = Company.findCompany(psid2, SourceSystemCode.QBDT);
        company2.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.save(company2);
        Application.commitUnitOfWork();


        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();

        PayrollCompany payrollCompany  = payrollCompanyManager.getPayrollCompany(companyRealmId);
        List<PayrollEmployee> payrollEmployees = payrollCompany.getEmployee();

        //the employee from both companies should be part the payrollEmployees as both employee & their companies are the part of same company realm
        Assert.assertTrue("Excepted employee is not found.", isExists(payrollEmployees, employee));
        Assert.assertTrue("Excepted employee is not found.", isExists(payrollEmployees, employee2));



    }

    public boolean isExists(List<PayrollEmployee> pPayrollEmployees, Employee pEmployee){

        for (PayrollEmployee payrollEmployee : pPayrollEmployees){
            if (payrollEmployee.getId().equalsIgnoreCase(pEmployee.getId().toString())){
                return true;
            }
        }

        return false;

    }


}
