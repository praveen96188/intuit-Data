package com.intuit.sbd.payroll.psp.adapters.cdmadapter;

import com.intuit.ems.dataservice.v1.beans.CompanyDTO;
import com.intuit.ems.dataservice.v1.exception.AccessDeniedException;
import com.intuit.ems.dataservice.v1.exception.DataServiceException;
import com.intuit.ems.dataservice.v1.exception.ResourceNotFoundException;
import com.intuit.ems.dataservice.v1.exception.ValidationException;
import com.intuit.ems.dataservice.v1.resource.EmployeeIdentificationParams;
import com.intuit.ems.dataservice.v1.resource.EmployeePreferenceParams;
import com.intuit.ems.dataservice.v1.resource.EmployerPreferenceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers.PayrollCompanyManager;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers.PayrollEmployeeManager;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers.PaystubManager;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.AssertHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.schema.ems.v3.PayrollEmployee;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PayrollEmployeeManagerTests {
    Employee firstEmployee;
    private static final String psid = "99000123";
    private static final String realmId = "123456";

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
    public void testGetForRealmWithOneEmployee() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, realmId);

        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        List<PayrollEmployee> payrollEmployees = payrollEmployeeManager.getForRealm(realmId);
        boolean matchingId = false;
        //Find matching employee
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(employee.getId().toString())) {
                AssertHelper.assertEmployeeMatches(employee, payrollEmployee);
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("Missing employee for consumerRealmId=" + realmId);
        }
    }

    @Test
    public void testInvalidRealmOneEmployee() {
        VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, realmId);
        try {
            PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
            payrollEmployeeManager.getForRealm("12390");
        } catch (Exception e) {
            Assert.assertEquals(e.getLocalizedMessage().contains("Employee record not found"), true);
        }
    }

    @Test
    public void testBlankRealmIdOneEmployee() {
        VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, realmId);
        try {
            PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
            payrollEmployeeManager.getForRealm("");
        } catch (Exception e) {
            Assert.assertEquals(e.getLocalizedMessage().contains("Employee record not found"), true);
        }
    }

    @Test
    public void testEmployeeParamsOneEmployee() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        EmployeeIdentificationParams employeeIdentificationParams = new EmployeeIdentificationParams();
        try {
            PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
            payrollEmployeeManager.associateEmployeeWithRealm(realmId, employeeIdentificationParams);
            payrollEmployeeManager.getForRealm(realmId);
        } catch (Exception e) {
            Assert.assertEquals(e.getLocalizedMessage().contains("Employee record not found due to wrong SSN"), true);
        }
    }

    //@Test Not implemented, format has not been agreed on
    public void testEmployeeFullName() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getSourceEmployeeId())) {
                Assert.assertEquals("Employee name does not match", firstEmployee.getFullName(), payrollEmployee.getFullName());
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    @Test
    public void testEmployeeCompanyName() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        //Find matching employee
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getId().toString())) {
                Assert.assertEquals("Company name does not match", firstEmployee.getCompany().getLegalName(), payrollEmployee.getCompanyName());
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    @Test
    public void testEmployeeActive() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        boolean activeEmployee = false;
        //Find matching employee
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getId().toString())) {
                if (firstEmployee.getStatusCd().toString().equals("Active")) {
                    activeEmployee = true;
                }
                Assert.assertEquals("Employee is Active does not match", activeEmployee, payrollEmployee.isActive());
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    @Test
    public void testEmployeeType() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        //Find matching employee
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getId().toString())) {
                Assert.assertEquals("Employee Type mismatch", "Employee", payrollEmployee.getEmployeeType());
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    @Test
    public void testEmployeeSSN() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getId().toString())) {
                Assert.assertEquals("Employee SSN mismatch", firstEmployee.getTaxId().substring(5), payrollEmployee.getSSN().substring(4));
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    @Test
    public void testEmployeeBirthDate() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getId().toString())) {
                Assert.assertEquals("Employee Birth date mismatch", firstEmployee.getBirthDate().getDay(), payrollEmployee.getBirthDate().getDay());
                Assert.assertEquals("Employee Birth date mismatch", firstEmployee.getBirthDate().getMonth(), payrollEmployee.getBirthDate().getMonth());
                Assert.assertEquals("Employee Birth date mismatch", firstEmployee.getBirthDate().getYear(), payrollEmployee.getBirthDate().getYear());
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    @Test
    public void testEmployeeGender() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getId().toString())) {
                Assert.assertEquals("Employee Gender mismatch", firstEmployee.getGenderCd().toString().toLowerCase(), payrollEmployee.getGender().value().toLowerCase());
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    @Test
    public void testEmployeeHireDate() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getId().toString())) {
                Assert.assertEquals("Employee Hire date mismatch", firstEmployee.getHireDate().getDay(), payrollEmployee.getHiredDate().getDay());
                Assert.assertEquals("Employee Hire date mismatch", firstEmployee.getHireDate().getMonth(), payrollEmployee.getHiredDate().getMonth());
                Assert.assertEquals("Employee Hire date mismatch", firstEmployee.getHireDate().getYear(), payrollEmployee.getHiredDate().getYear());
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    // @Test not implemented
    public void testEmployeePayPeriodHistory() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getId().toString())) {
                Assert.assertEquals("Employee pay period mismatch", firstEmployee.getPayPeriod(), payrollEmployee.getPayPeriodHistory().getStartDate());
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    @Test
    public void testEmployeeAddress() {
        List<PayrollEmployee> payrollEmployees = employeeList();
        boolean matchingId = false;
        for (PayrollEmployee payrollEmployee : payrollEmployees) {
            if (payrollEmployee.getId().equalsIgnoreCase(firstEmployee.getId().toString())) {
                Assert.assertEquals("Employee Address mismatch", firstEmployee.getMailingAddress().getAddressLine1(), payrollEmployee.getPrimaryAddress().getLine1());
                Assert.assertEquals("Employee Address mismatch", firstEmployee.getMailingAddress().getAddressLine2(), payrollEmployee.getPrimaryAddress().getLine2());
                Assert.assertEquals("Employee Address mismatch", firstEmployee.getMailingAddress().getAddressLine3(), payrollEmployee.getPrimaryAddress().getLine3());
                Assert.assertEquals("Employee Address mismatch", firstEmployee.getMailingAddress().getCity(), payrollEmployee.getPrimaryAddress().getCity());
                Assert.assertEquals("Employee Address mismatch", firstEmployee.getMailingAddress().getCountry(), payrollEmployee.getPrimaryAddress().getCountry());
                Assert.assertEquals("Employee Address mismatch", firstEmployee.getMailingAddress().getCity(), payrollEmployee.getPrimaryAddress().getCity());
                Assert.assertEquals("Employee Address mismatch", firstEmployee.getMailingAddress().getZipCode(), payrollEmployee.getPrimaryAddress().getPostalCode());
                Assert.assertEquals("Employee Address mismatch", firstEmployee.getMailingAddress().getZipCodeExtension(), payrollEmployee.getPrimaryAddress().getPostalCodeSuffix());
                matchingId = true;
                break;
            }
        }
        if (!matchingId) {
            Assert.fail("No match found for realmId " + realmId);
        }
    }

    //CompanyEvent.queueEmail only allows certain emails to assisted, we want to make sure VMP emails are allowed
    @Test
    public void testVmpSignUpEmailAssisted() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax, ServiceCode.DirectDeposit, ServiceCode.ViewMyPaycheck);
        Employee employee = findEmployee(company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        String employeeEmailAddress = employee.getEmail();
        String iamEmailAddress = "iamEmailAddress@intuit.com";
        VmpTestUtil.associateEmployeeWithRealm(realmId, employee.getTaxId(), iamEmailAddress, "1000.00");

        assertEmployerEmailPresent(company);
        DomainEntitySet<CompanyEventEmail> employeeEmails = getEmployeeEmails();
        Assert.assertEquals(2, employeeEmails.size());
        boolean foundEmployeeEmailAddress = false;
        boolean foundIamEmailAddress = false;
        for(CompanyEventEmail employeeEmail : employeeEmails) {
            String recipientEmail = employeeEmail.getEmailParamValue(EventEmailParamTypeCode.RecipientEmail);
            if(employeeEmailAddress.equals(recipientEmail)) {
                foundEmployeeEmailAddress = true;
            } else if(iamEmailAddress.equals(recipientEmail)) {
                foundIamEmailAddress = true;
            }
        }
        Assert.assertTrue("Did not find employee email address", foundEmployeeEmailAddress);
        Assert.assertTrue("Did not find iam email address", foundIamEmailAddress);
    }

    //This test should find an IAM email for the employee and one for the employer
    @Test
    public void testVmpSignUpEmployeeEmailNoEmployeeEmailAddress() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        //find employee again for new UOW
        employee = findEmployee(employee.getCompany());
        employee.setEmail(null);
        Application.save(employee);
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");

        assertEmployerEmailPresent(employee.getCompany());
        DomainEntitySet<CompanyEventEmail> employeeEmails = getEmployeeEmails();
        Assert.assertEquals(1, employeeEmails.size());
        CompanyEventEmail employeeEmail = employeeEmails.getFirst();
        Assert.assertEquals("iamEmailAddress@intuit.com", employeeEmail.getEmailParamValue(EventEmailParamTypeCode.RecipientEmail));
    }

    //This test should send to the employee email address and employer
    @Test
    public void testVmpSignUpEmployeeEmailMatchingEmployeeAndIamEmailAddress() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        //find employee again for new UOW
        employee = findEmployee(employee.getCompany());
        String employeeEmailAddress = "employeeEmailAddress@intuit.com";
        employee.setEmail(employeeEmailAddress);
        Application.save(employee);
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, employee.getTaxId(), employeeEmailAddress, "1000.00");

        assertEmployerEmailPresent(employee.getCompany());
        DomainEntitySet<CompanyEventEmail> employeeEmails = getEmployeeEmails();
        Assert.assertEquals(1, employeeEmails.size());
        CompanyEventEmail employeeEmail = employeeEmails.getFirst();
        Assert.assertEquals(employeeEmailAddress, employeeEmail.getEmailParamValue(EventEmailParamTypeCode.RecipientEmail));
    }

    //This test should send two employee emails, one to IAM and one to the employee.  One employer email is sent as well.
    @Test
    public void testVmpSignUpEmployeeEmailNonMatchingEmployeeAndIamEmailAddress() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        //find employee again for new UOW
        employee = findEmployee(employee.getCompany());
        String employeeEmailAddress = "employeeEmailAddress@intuit.com";
        employee.setEmail(employeeEmailAddress);
        Application.save(employee);
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        String iamEmailAddress = "iamEmailAddress@intuit.com";
        VmpTestUtil.associateEmployeeWithRealm(realmId, employee.getTaxId(), iamEmailAddress, "1000.00");

        assertEmployerEmailPresent(employee.getCompany());
        DomainEntitySet<CompanyEventEmail> employeeEmails = getEmployeeEmails();
        Assert.assertEquals(2, employeeEmails.size());
        boolean foundEmployeeEmailAddress = false;
        boolean foundIamEmailAddress = false;
        for (CompanyEventEmail employeeEmail : employeeEmails) {
            String recipientEmail = employeeEmail.getEmailParamValue(EventEmailParamTypeCode.RecipientEmail);
            if (employeeEmailAddress.equals(recipientEmail)) {
                foundEmployeeEmailAddress = true;
            } else if (iamEmailAddress.equals(recipientEmail)) {
                foundIamEmailAddress = true;
            }
        }
        Assert.assertTrue("Did not find employee email address", foundEmployeeEmailAddress);
        Assert.assertTrue("Did not find iam email address", foundIamEmailAddress);
    }

    //This test should send the employee an email but not the employer because their payroll service has been cancelled
    @Test
         public void testVmpSignUpEmployerEmailPayrollServiceCancelledOrTerminated() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax, ServiceCode.DirectDeposit);
        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Cloud);
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, psid, ServiceCode.DirectDeposit);
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
        assertEmployerEmailNotPresent();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDefaultSSN() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Employee employee = VmpTestUtil.createEmployee(Employee.DEFAULT_SSN, company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, Employee.DEFAULT_SSN, "iamEmailAddress@intuit.com", "1000.0");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDefaultSSNWithDashes() {
        String defaultSsnWithDashes = "000-00-0000";
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Employee employee = VmpTestUtil.createEmployee(Employee.DEFAULT_SSN, company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, defaultSsnWithDashes, "iamEmailAddress@intuit.com", "1000.0");
    }

    @Test
    public void testAssociateEmployeeWithRealmPrecisionOff() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.0");

        Application.beginUnitOfWork();
        Application.refresh(employee);
        Assert.assertEquals(realmId, employee.getConsumerRealmId());
        Application.rollbackUnitOfWork();
    }


    @Test
    public void testAssociateEmployeeWithRealm() {
        String paystubAmount = "1000.00";
        Employee employee = VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, realmId, paystubAmount);
        Application.beginUnitOfWork();
        Application.refresh(employee);
        Assert.assertEquals(realmId, employee.getConsumerRealmId());
        Application.rollbackUnitOfWork();
    }

    //Two employees with matching ssn / last paycheck amounts.  Only one should end up with a consumer realm id
    @Test
    public void testAssociateEmployeeWithRealmMultipleEmployees() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        //Create two employees with same ssn
        String ssn = "123456789";
        Employee employeeOne = VmpTestUtil.createEmployee(ssn, company);
        Employee employeeTwo = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        //Create paychecks / paystubs for employee one
        VmpTestUtil.createPaystub(employeeOne, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        //Create paychecks / paystubs for employee two
        VmpTestUtil.createPaystub(employeeTwo, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
        //Only the first employee found should have consumer realm id added, it can be either since there is no ordering done on the query
        Application.beginUnitOfWork();
        Application.refresh(employeeOne);
        Application.refresh(employeeTwo);
        boolean foundOneConsumerRealmId = false;
        foundOneConsumerRealmId = realmId.equals(employeeOne.getConsumerRealmId());
        if(foundOneConsumerRealmId) {
            foundOneConsumerRealmId = !realmId.equals(employeeTwo.getConsumerRealmId());
            Assert.assertTrue("Error both employees had consumer realm id set", foundOneConsumerRealmId);
        } else {
            foundOneConsumerRealmId = realmId.equals(employeeTwo.getConsumerRealmId());
            Assert.assertTrue("Error neither employee had consumer realm id set", foundOneConsumerRealmId);
        }
        Application.rollbackUnitOfWork();
    }

    //Two employees with matching ssn, but different last paystub amounts. Only the one with correct amount should end up with a consumer realm id
    @Test
    public void testAssociateEmployeeWithRealmSameSsnDifferentPaystubAmounts() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        //Create two employees with same ssn
        String ssn = "123456789";
        Employee employeeOne = VmpTestUtil.createEmployee(ssn, company);
        Employee employeeTwo = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        //Create paychecks / paystubs for employee one
        VmpTestUtil.createPaystub(employeeOne, "999.00", SpcfCalendar.createInstance(2013, 1, 1));
        //Create paychecks / paystubs for employee two
        VmpTestUtil.createPaystub(employeeTwo, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
        //Only employee two should have consumer realm id
        Application.beginUnitOfWork();
        Application.refresh(employeeOne);
        Application.refresh(employeeTwo);
        Assert.assertEquals(null, employeeOne.getConsumerRealmId());
        Assert.assertEquals(realmId, employeeTwo.getConsumerRealmId());
    }

    //Try to use 2nd most recent paystub, should work
    @Test
    public void testAssociateEmployeeWithRealmPaystubInLastTwo() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        VmpTestUtil.createPaystub(employee, "999.00", SpcfCalendar.createInstance(2013, 1, 2));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00");

        Application.beginUnitOfWork();
        Application.refresh(employee);
        Assert.assertEquals(realmId, employee.getConsumerRealmId());
        Application.rollbackUnitOfWork();
    }

    //Try to use the paystub amount from 3 paystubs ago, which should not work
    @Test
    public void testAssociateEmployeeWithRealmPaystubNotInLastTwo() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        VmpTestUtil.createPaystub(employee, "999.00", SpcfCalendar.createInstance(2013, 1, 2));
        VmpTestUtil.createPaystub(employee, "998.00", SpcfCalendar.createInstance(2013, 1, 3));
        Application.commitUnitOfWork();

        try {
            VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
            Assert.fail("Expected ResourceNotFoundException");
        } catch (ResourceNotFoundException resourceNotFoundException) {
            Assert.assertEquals(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND_DUE_TO_WRONG_NET_AMOUNT, resourceNotFoundException.getErrorCode());
        }
    }

    //Try to associate a new consumer realm id on an employee who already has a different consumer realm id, expecting an exception
    @Test
    public void testAssociateEmployeeWithRealmConsumerRealmIdAlreadyExists() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
        try {
            VmpTestUtil.associateEmployeeWithRealm("999999", ssn, "iamEmailAddress@intuit.com", "1000.00");
            Assert.fail("Expected AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            Assert.assertEquals(DataServiceException.ERRNUM_REALMID_ALREADY_EXIST, accessDeniedException.getErrorCode());
        }
    }

    @Test
    public void testAssociateEmployeWithRealmSelfServiceSignUpOff() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        //Disable self service sign up
        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();
        EmployerPreferenceParams params = new EmployerPreferenceParams();
        params.setPreferenceName(EmployerPreference.SELF_SERVICE_SIGN_IN);
        params.setPreferenceValue(EmployerPreference.OFF);
        payrollCompanyManager.createOrUpdateEmployerPreferences(companyRealmId, EmployerPreference.VMP, params);
        //Try to self service, expecting error
        try {
            VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
            Assert.fail("Expecting access denied exception");
        } catch (AccessDeniedException e) {
            Assert.assertEquals(DataServiceException.ERRNUM_EMPLOYEE_NOT_ALLOWED_TO_SELF_SERVICE_SIGN_IN, e.getErrorCode());
        }
    }

    //This test is an edge case.  If the employee exists in multiple companies with their last paystub amounts being equal in all of them.
    //In one of the companies they are already signed up, in another they are signed up with a different login, and
    //one company has not been associated yet.  It should find the company that has not been associated because it takes precedence over the other two found.
    @Test
    public void testAssociateEmployeeWithRealmMismatchAndAlreadyAssociatedAndNewAssociation() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        //Two different consumer realms exist for this scenario
        String existingRealm = "123456";
        String differentLoginRealm = "234567";
        String ssn = "123456789";
        Employee employeeAlreadySignedUp = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employeeAlreadySignedUp, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        //Setup the existing realm for first employee
        VmpTestUtil.associateEmployeeWithRealm(existingRealm, ssn, "iamEmailAddress@intuit.com", "1000.00");

        //Now setup differentLogin realm
        Employee employeeDifferentLogin = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employeeDifferentLogin, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(differentLoginRealm, ssn, "iamEmailAddress2@intuit.com", "1000.00");

        //Finally associate the last employee
        Employee employeeNotAssociated = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employeeNotAssociated, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(existingRealm, ssn, "iamEmailAddress@intuit.com", "1000.00");

        Application.beginUnitOfWork();
        Application.refresh(employeeAlreadySignedUp);
        Application.refresh(employeeDifferentLogin);
        Application.refresh(employeeNotAssociated);
        Assert.assertEquals(existingRealm, employeeAlreadySignedUp.getConsumerRealmId());
        Assert.assertEquals(differentLoginRealm, employeeDifferentLogin.getConsumerRealmId());
        Assert.assertEquals(existingRealm, employeeNotAssociated.getConsumerRealmId());
        Application.rollbackUnitOfWork();
    }

    //We have two employees in this test with same ssn / last paystub amounts and both are associated with different realms.
    //If we try to associate again with one of their existing realms we should not receive an error, meaning finding a match
    //with the same consumer realm id takes precedence over the mismatch
    @Test
    public void testAssociateEmployeeWithRealmMismatchAndAlreadyAssociated() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        //Two different consumer realms exist for this scenario
        String realmOne = "123456";
        String realmTwo = "234567";
        String ssn = "123456789";
        Employee employeeOne = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employeeOne, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        //Setup the existing realm for first employee
        VmpTestUtil.associateEmployeeWithRealm(realmOne, ssn, "iamEmailAddress@intuit.com", "1000.00");

        Employee employeeTwo = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employeeTwo, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        //Setup 2nd realm for 2nd employee
        VmpTestUtil.associateEmployeeWithRealm(realmTwo, ssn, "iamEmailAddress2@intuit.com", "1000.00");
        //Call associate again, should not receive an error
        VmpTestUtil.associateEmployeeWithRealm(realmTwo, ssn, "iamEmailAddress2@intuit.com", "1000.00");
    }

    //If an employee exists with the same ssn / last paystub amount in two different companies then two calls should associate
    //both employees with the realm
    @Test
    public void testAssociateEmployeeWithRealmTwoCompanies() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company companyOne = Company.findCompany(psid, SourceSystemCode.QBDT);
        String psidTwo = psid + "1";
        VmpTestUtil.setupCompanyCreateEmployee(psidTwo);
        Company companyTwo = Company.findCompany(psidTwo, SourceSystemCode.QBDT);
        String ssn = "123456789";
        Employee employeeOne = VmpTestUtil.createEmployee(ssn, companyOne);
        Employee employeeTwo = VmpTestUtil.createEmployee(ssn, companyTwo);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employeeOne, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        VmpTestUtil.createPaystub(employeeTwo, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00");
        //Both employees should have the same consumer realm id now
        Application.beginUnitOfWork();
        Application.refresh(employeeOne);
        Application.refresh(employeeTwo);
        Assert.assertEquals(realmId, employeeOne.getConsumerRealmId());
        Assert.assertEquals(realmId, employeeTwo.getConsumerRealmId());
    }

    //Test where we provide a matching last name
    @Test
    public void testAssociateEmployeeWithRealmLastName() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);
        String lastName = employee.getLastName();
        Application.beginUnitOfWork();
        //Create paychecks / paystubs for employee
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00", lastName);
        Application.beginUnitOfWork();
        Application.refresh(employee);
        Assert.assertEquals(realmId, employee.getConsumerRealmId());
        Application.rollbackUnitOfWork();
    }

    //Test where we provide the wrong last name
    @Test
    public void testAssociateEmployeeWithRealmWrongLastName() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);
        String lastName = employee.getLastName();
        Application.beginUnitOfWork();
        //Create paychecks / paystubs for employee
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        try {
            VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00", "Wronglastname");
            Assert.fail("Expecting resource not found because of wrong last name");
        } catch(ResourceNotFoundException resourceNotFoundException) {
            Assert.assertEquals(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }
    }

    //Test where we provide a last name but the employee does not have one
    @Test
    public void testAssociateEmployeeWithRealmNoLastName() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);
        Application.beginUnitOfWork();
        Application.refresh(employee);
        employee.setLastName(null);

        //Create paychecks / paystubs for employee
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        try {
            VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00", "MissingLastName");
            Assert.fail("Expecting resource not found because employee does not have a last name");
        } catch(ResourceNotFoundException resourceNotFoundException) {
            Assert.assertEquals(DataServiceException.ERRNUM_PAYROLLEMPLOYEE_RESOURCE_NOT_FOUND, resourceNotFoundException.getErrorCode());
        }

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

        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();

        //These employer APIs should fail when ViewMyPaycheck service is cancelled
        try {
            payrollEmployeeManager.getPayrollEmployeesByCompanyRealmId(companyRealmId);
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            Assert.assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }

        try {
            payrollEmployeeManager.updateEmployeeIsViewingPaystubDisabled(companyRealmId, employee.getId().toString(), new PayrollEmployee());
            Assert.fail("Expecting AccessDeniedException");
        } catch (AccessDeniedException accessDeniedException) {
            Assert.assertEquals(DataServiceException.ERRNUM_COMPANY_DEACTIVATED_VIEW_MY_PAYCHECK, accessDeniedException.getErrorCode());
        }

        //The employee preferences, employee list, and associate employee APIs should continue to function when service is cancelled
        try {
            payrollEmployeeManager.getEmployeePreferencesByApp(consumerRealmId, employee.getId().toString(), "VMP");
        } catch (ResourceNotFoundException resourceNotFoundException) {
            //Expecting no preferences to be found
        }

        EmployeePreferenceParams employeePreferenceParams = new EmployeePreferenceParams();
        employeePreferenceParams.setPreferenceName("test");
        employeePreferenceParams.setPreferenceValue("test");
        payrollEmployeeManager.createOrUpdateEmployeePreferences(consumerRealmId, employee.getId().toString(), "VMP", employeePreferenceParams);

        payrollEmployeeManager.getForRealm(consumerRealmId);

        EmployeeIdentificationParams employeeIdentificationParams = new EmployeeIdentificationParams();
        employeeIdentificationParams.setLastName(employee.getLastName());
        employeeIdentificationParams.setIamEmail("iamEmailAddress@intuit.com");
        employeeIdentificationParams.setLastPaycheckNetAmount("1000.00");
        employeeIdentificationParams.setSsn(employee.getTaxId());
        payrollEmployeeManager.associateEmployeeWithRealm(consumerRealmId, employeeIdentificationParams);
    }

    @Test
    public void testVmpEmployeeForCompanyUniqueId() {
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

        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        PayrollEmployee pEmp1 = payrollEmployeeManager.getPayrollEmployeeByCompanyRealmIdAndConsumerRealmId(companyRealmId, consumerRealmId);
        PayrollEmployee pEmp2 = payrollEmployeeManager.getPayrollEmployeeByCompanyUniqueIdAndConsumerRealmId(company.getId().toString(), consumerRealmId);
        Assert.assertEquals(pEmp1.getId(),pEmp2.getId());
        PayrollEmployee pEmp3=payrollEmployeeManager.getPayrollEmployeeByCompanyUniqueIdAndConsumerRealmId(companyRealmId,consumerRealmId);
        Assert.assertEquals(pEmp1.getId(),pEmp3.getId());
    }

    @Test
    public void testVmpCompanyForCompanyUniqueId() {
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        String ssn = "123456789";
        String consumerRealmId = "123456";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());

        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();

        List<CompanyDTO> companyDTOList1 = payrollCompanyManager.getCompaniesByConsumerRealmId(consumerRealmId);
        List<CompanyDTO> companyDTOList2 = payrollCompanyManager.getCompaniesByCompanyUniqueId(consumerRealmId);
        Assert.assertEquals("CompanyDTOList1 is empty", 0, companyDTOList1.size());
        Assert.assertEquals("CompanyDTOList2 has value", 1, companyDTOList2.size());

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        List<CompanyDTO> companyDTOList3=payrollCompanyManager.getCompaniesByConsumerRealmId(consumerRealmId);
        List<CompanyDTO> companyDTOList4=payrollCompanyManager.getCompaniesByCompanyUniqueId(consumerRealmId);
        Assert.assertEquals("CompanyDTOList3 has value",1,companyDTOList3.size());
        Assert.assertEquals("CompanyDTOList4 has value",1,companyDTOList4.size());

    }

    @Test
    public void testVmpPaystubForCompanyUniqueId() {
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        String ssn = "123456789";
        String consumerRealmId = "123456";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());

        PaystubManager paystubManager = new PaystubManager();
        String checkDateStart = "2013-01-01";
        String checkDateEnd = "2013-01-10";

        try {
            List<com.intuit.schema.ems.v3.Paystub> pstubList1 = paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(company.getIAMRealmId(), employee.getId().toString(), checkDateStart, checkDateEnd, 0, 1);
        }catch(ResourceNotFoundException ex){
            Assert.assertEquals(ex.getMessage(),"Company not found");
        }

        List<com.intuit.schema.ems.v3.Paystub> pstubList2 = paystubManager.getPaystubsByCompanyUniqueIdAndEmployeeId(company.getId().toString(), employee.getId().toString(), checkDateStart, checkDateEnd, 0, 1);
        Assert.assertEquals("PaystubList2 has value", 1, pstubList2.size());

        try {
        com.intuit.schema.ems.v3.Paystub pstub=paystubManager.getPaystubForCompany(company.getIAMRealmId(),paystub.getId().toString());
        }catch(ResourceNotFoundException ex){
            Assert.assertEquals(ex.getMessage(),"Company not found");
        }
        try {
            com.intuit.schema.ems.v3.Paystub pstub1 = paystubManager.getPaystubForCompanyByCompanyUniqueId(company.getIAMRealmId(), paystub.getId().toString());
        } catch (ResourceNotFoundException ex) {
            Assert.assertEquals(ex.getMessage(), "Company not found");
        }
        com.intuit.schema.ems.v3.Paystub pstub2=paystubManager.getPaystubForCompanyByCompanyUniqueId(company.getId().toString(),paystub.getId().toString());

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456789012567890123456789012";
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        List<com.intuit.schema.ems.v3.Paystub> pstubList3 = paystubManager.getPaystubsByCompanyRealmIdAndEmployeeId(company.getIAMRealmId(), employee.getId().toString(), checkDateStart, checkDateEnd, 0, 1);
        List<com.intuit.schema.ems.v3.Paystub> pstubList4 = paystubManager.getPaystubsByCompanyUniqueIdAndEmployeeId(company.getId().toString(), employee.getId().toString(), checkDateStart, checkDateEnd, 0, 1);
        Assert.assertEquals("PaystubList3 has value", 1, pstubList3.size());
        Assert.assertEquals("PaystubList4 has value", 1, pstubList4.size());

        com.intuit.schema.ems.v3.Paystub pstub3=paystubManager.getPaystubForCompany(company.getIAMRealmId(),paystub.getId().toString());
        com.intuit.schema.ems.v3.Paystub pstub4=paystubManager.getPaystubForCompanyByCompanyUniqueId(company.getId().toString(),paystub.getId().toString());
        com.intuit.schema.ems.v3.Paystub pstub5=paystubManager.getPaystubForCompanyByCompanyUniqueId(company.getIAMRealmId(),paystub.getId().toString());
        Assert.assertEquals(pstub3.getId(),pstub4.getId());
        Assert.assertEquals(pstub3.getId(),pstub5.getId());
    }

    @Test
    public void testVmpLastPaystubForCompanyUniqueId() {
        String psid = "123456";
        VmpTestUtil.setupCompanyCreateEmployee(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        String ssn = "123456789";
        String consumerRealmId = "123456";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(consumerRealmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());

        PaystubManager paystubManager = new PaystubManager();

        try {
            com.intuit.schema.ems.v3.Paystub pstub=paystubManager.findLastPaystub(company.getIAMRealmId(),consumerRealmId);
        }catch(Exception ex){

        }

        try {
            com.intuit.schema.ems.v3.Paystub pstub1=paystubManager.findLastPaystubByCompanyUniqueId(company.getIAMRealmId(),consumerRealmId);
        }catch(Exception ex){

        }
        com.intuit.schema.ems.v3.Paystub pstub2=paystubManager.findLastPaystubByCompanyUniqueId(company.getId().toString(),consumerRealmId);

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "1234567887654321";
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        com.intuit.schema.ems.v3.Paystub pstub3=paystubManager.findLastPaystub(company.getIAMRealmId(),consumerRealmId);
        com.intuit.schema.ems.v3.Paystub pstub4=paystubManager.findLastPaystubByCompanyUniqueId(company.getId().toString(),consumerRealmId);
        com.intuit.schema.ems.v3.Paystub pstub5=paystubManager.findLastPaystubByCompanyUniqueId(company.getIAMRealmId(),consumerRealmId);
        Assert.assertEquals(pstub3.getId(),pstub4.getId());
        Assert.assertEquals(pstub3.getId(),pstub5.getId());

    }


    @Test
    public void testUpdateEmployeeIsViewingPaystubDisabledInvalidRequest() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());

        Application.beginUnitOfWork();
        Application.refresh(employee);
        //Make sure employee is setup properly
        Assert.assertEquals(realmId, employee.getConsumerRealmId());
        Application.rollbackUnitOfWork();

        //Send in a null payroll employee
        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        try {
            payrollEmployeeManager.updateEmployeeIsViewingPaystubDisabled(companyRealmId, employee.getId().toString(), null);
            Assert.fail("Expecting ValidationException");
        } catch (ValidationException validationException) {
            //Expected
        }
    }

    @Test
    public void testUpdateEmployeeIsViewingPaystubDisabled() {
        VmpTestUtil.setupCompanyCreateEmployee(psid);

        //Setup a company realm
        Application.beginUnitOfWork();
        String companyRealmId = "123456";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        String ssn = "123456789";
        Employee employee = VmpTestUtil.createEmployee(ssn, company);

        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();

        VmpTestUtil.associateEmployeeWithRealm(realmId, ssn, "iamEmailAddress@intuit.com", "1000.00", employee.getLastName());

        Application.beginUnitOfWork();
        Application.refresh(employee);
        //Make sure employee is setup properly
        Assert.assertEquals(realmId, employee.getConsumerRealmId());
        Application.rollbackUnitOfWork();

        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        PayrollEmployee payrollEmployee = new PayrollEmployee();
        payrollEmployee.setViewingPaystubDisabled(true);
        payrollEmployeeManager.updateEmployeeIsViewingPaystubDisabled(companyRealmId, employee.getId().toString(), payrollEmployee);

        Application.beginUnitOfWork();
        Application.refresh(employee);
        Assert.assertTrue(employee.getIsViewingPaystubDisabled());
        Application.rollbackUnitOfWork();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testCreateNewEmployees(){
        String recnum = "6872382";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud, ServiceCode.ViewMyPaycheck);
        Application.beginUnitOfWork();
        for (int i=0; i<1; i++){
            VmpEmployeeInfo vmpEmployeeInfo = new VmpEmployeeInfo();
            vmpEmployeeInfo.setCompany(company);
            vmpEmployeeInfo.setEmployeeRecnum(recnum + i);
            vmpEmployeeInfo.setConsumerRealmId(realmId);
            vmpEmployeeInfo.setEmail("testmail@mail.com");
            vmpEmployeeInfo.setPersonaId("83084093040");
            Application.save(vmpEmployeeInfo);
        }
        Application.commitUnitOfWork();
        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        List<PayrollEmployee> payrollEmployees = payrollEmployeeManager.getForRealm(realmId);
        Assert.assertNotNull(payrollEmployees);
        Assert.assertEquals("No VmpEmployee found", 1, payrollEmployees.size());
        PayrollEmployee payrollEmployee = payrollEmployees.get(0);
        String id = payrollEmployee.getId();
        PaystubManager paystubManager = new PaystubManager();
        List<com.intuit.schema.ems.v3.Paystub> paystubs = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(realmId, id, null, null, 10, 10);
        Assert.assertEquals("Paystubs found while unexpected", 0, paystubs.size());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testNewEmployeesForGetEmployeesByCompanyRealmIdAndConsumerRealmId(){
        String recnum = "6872382";
        String companyRealmId="123147964594684";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud, ServiceCode.ViewMyPaycheck);

        //Setup a company realm
        Application.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        for (int i=0; i<1; i++){
            VmpEmployeeInfo vmpEmployeeInfo = new VmpEmployeeInfo();
            vmpEmployeeInfo.setCompany(company);
            vmpEmployeeInfo.setEmployeeRecnum(recnum + i);
            vmpEmployeeInfo.setConsumerRealmId(realmId);
            vmpEmployeeInfo.setEmail("testmail@mail.com");
            vmpEmployeeInfo.setPersonaId("83084093040");
            Application.save(vmpEmployeeInfo);
        }
        Application.commitUnitOfWork();

        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        PayrollEmployee payrollEmployee = payrollEmployeeManager.getPayrollEmployeeByCompanyRealmIdAndConsumerRealmId(company.getIAMRealmId(), realmId);
        Assert.assertNotNull(payrollEmployee);
        String id = payrollEmployee.getId();
        PaystubManager paystubManager = new PaystubManager();
        List<com.intuit.schema.ems.v3.Paystub> paystubs = paystubManager.getPaystubsByConsumerRealmIdAndEmployeeId(realmId, id, null, null, 10, 10);
        Assert.assertEquals("Paystubs found while unexpected", 0, paystubs.size());
    }

    @Test
    public void testNewEmployeesForGetEmployeesByCompanyRealmId(){
        String recnum = "6872382";
        String companyRealmId="123147964594684";
        VmpTestUtil.setupCompanyCreateEmployee(psid);

        //Setup a company realm
        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setIAMRealmId(companyRealmId);
        Application.save(company);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        for (int i=0; i<1; i++){
            VmpEmployeeInfo vmpEmployeeInfo = new VmpEmployeeInfo();
            vmpEmployeeInfo.setCompany(company);
            vmpEmployeeInfo.setEmployeeRecnum(recnum + i);
            vmpEmployeeInfo.setConsumerRealmId(realmId);
            vmpEmployeeInfo.setEmail("testmail@mail.com");
            vmpEmployeeInfo.setPersonaId("83084093040");
            Application.save(vmpEmployeeInfo);
        }
        Application.commitUnitOfWork();

        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        List<PayrollEmployee> payrollEmployeesByCompanyRealmId = payrollEmployeeManager.getPayrollEmployeesByCompanyRealmId(company.getIAMRealmId());
        Assert.assertNotNull(payrollEmployeesByCompanyRealmId);
        Assert.assertEquals("Employee count does not match", 3, payrollEmployeesByCompanyRealmId.size());
    }



    public void assertEmployerEmailNotPresent() {
        DomainEntitySet<CompanyEventEmail> employerEmails = getEmployerEmails();
        Assert.assertEquals(0, employerEmails.size());
    }

    public void assertEmployerEmailPresent(Company company) {
        DomainEntitySet<CompanyEventEmail> employerEmails = getEmployerEmails();
        Assert.assertEquals(1, employerEmails.size());
        CompanyEventEmail employerEmail = employerEmails.getFirst();
        Contact payrollAdmin = company.getContactByRoleCode(ContactRole.PayrollAdmin);
        Assert.assertEquals(payrollAdmin.getEmail(), employerEmail.getEmailParamValue(EventEmailParamTypeCode.PayrollAdminEmail));
    }

    public DomainEntitySet<CompanyEventEmail> getEmployerEmails() {
        Expression<CompanyEventEmail> vmpEmployerQuery = new Query<CompanyEventEmail>()
            .Where(CompanyEventEmail.EmailTemplateTypeCd().in(EventEmailTemplateTypeCode.VmpEmployerWelcome));
        return Application.find(CompanyEventEmail.class, vmpEmployerQuery);
    }

    public DomainEntitySet<CompanyEventEmail> getEmployeeEmails() {
        Expression<CompanyEventEmail> vmpEmployeeQuery = new Query<CompanyEventEmail>()
            .Where(CompanyEventEmail.EmailTemplateTypeCd().in(EventEmailTemplateTypeCode.VmpEmployeeWelcome));
        return Application.find(CompanyEventEmail.class, vmpEmployeeQuery);
    }

    public Employee findEmployee(Company company) {
        Expression<Employee> query = new Query<Employee>()
            .Where(Employee.Company().equalTo(company))
            .EagerLoad(Employee.MailingAddress(), Employee.Company());
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        return employees.getFirst();
    }

    public List<PayrollEmployee> employeeList() {
        List<PayrollEmployee> payrollEmployees = null;
        firstEmployee = VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, realmId);
        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        payrollEmployees = payrollEmployeeManager.getForRealm(realmId);
        return payrollEmployees;
    }
}



