/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/DeactivateEmployeeCoreTests.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * Contains the unit tests for the DeactivateEmployeeCore
 *
 * @author: Sean Barenz
 * @version: August 29, 2007
 */
public class DeactivateEmployeeCoreTests {

    private static String INVALID_COMPANY = "IDONTEXIST";
    private static String INVALID_EE = INVALID_COMPANY;
    private static String ACTIVE_EE = "TESTACTV";

    private DataLoader dataloader = new DataLoader();
    private Company1Dataloader company1DataLoader = new Company1Dataloader();
    
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void deactivateEmployee_InactiveEE() {
        loadEmployee();
        
        //Deactivate Employee
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee1 = Employee.findEmployee(company1, "EE1");
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company1.getSourceCompanyId(), employee1.getSourceEmployeeId(),null);
        PayrollServices.commitUnitOfWork();

        assertSuccess("deactivateEmployee", pr);

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        employee1 = Employee.findEmployee(company1, "EE1");
        pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company1.getSourceCompanyId(), employee1.getSourceEmployeeId(),null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "215", message.getMessageCode());
        assertEquals("Error Message:", "Employee EE1 for company QBOE:1234567 was already inactive.", message.getMessage());
    }

    @Test
    public void deactivateEmployee_EmployeeIdDoesNotExists() {
        loadEmployee();
        
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company1.getSourceCompanyId(), INVALID_EE,null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "168", message.getMessageCode());
        assertEquals("Error Message:", "Employee IDONTEXIST for company QBOE:1234567 does not exist.", message.getMessage());
    }

    @Test
    public void deactivateEmployee_CompanyNotSpecified() {
        loadEmployee();
        
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, null, INVALID_EE,null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());
        assertEquals("Error Message:", "Source Company ID is not specified.", message.getMessage());
    }

    @Test
    public void deactivateEmployee_CompanyDoesNotExists() {
        loadEmployee();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, INVALID_COMPANY, ACTIVE_EE,null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());
        assertEquals("Error Message:", "Company QBOE:IDONTEXIST does not exist.", message.getMessage());
    }

    @Test
    public void deactivateEmployee_EmployeeIdNotSpecified() {
        loadEmployee();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company1.getSourceCompanyId(), null,null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "145", message.getMessageCode());
        assertEquals("Error Message:", "Employee ID is not specified.", message.getMessage());
    }

    @Test
    public void deactivateEmployee_SourceSystemCodeNotSpecified() {
        loadEmployee();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee1 = Employee.findEmployee(company1, "EE1");
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(null, company1.getSourceCompanyId(), employee1.getSourceEmployeeId(),null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());
        assertEquals("Error Message:", "Source System Code is not specified.", message.getMessage());       
    }

    /**
     * Test error message 1101 - is allowed capability false
     */
    @Test
    public void deactivateNotAllowedCapability() {
        loadEmployee();
        
        //Set company status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE, "1234567", ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee1 = Employee.findEmployee(company1, "EE1");
        ProcessResult<Employee> processResult = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company1.getSourceCompanyId(), employee1.getSourceEmployeeId(),null);
        PayrollServices.commitUnitOfWork();

       // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "The operation ChangeCompanyInfo is not allowed for company QBOE:1234567 in its current state.", message.getMessage());

    }

    @Test
    public void deactivateEmployeeOneActiveBankAccount() {
        loadEmployee();

        //Add Employee Bank Account
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee1 = Employee.findEmployee(company1, "EE1");
        EmployeeBankAccount eba1 = company1DataLoader.persistEEBA(company1, employee1, company1DataLoader.getEmployee1BankAccount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company1.getSourceCompanyId(), employee1.getSourceEmployeeId(),null);
        PayrollServices.commitUnitOfWork();
        assertSuccess("deactivateEmployee", pr);

        PayrollServices.beginUnitOfWork();
        // verify the employee itself
        Employee ee = PayrollServices.entityFinder.findById(Employee.class, pr.getResult().getId());
        validateEmployee(ee);

        EmployeeBankAccount eba = EmployeeBankAccount.findEmployeeBankAccount(ee, eba1.getSourceBankAccountId());
        PayrollServices.commitUnitOfWork();
        assertEquals("EE Bank Status:", BankAccountStatus.Inactive, eba.getStatusCd());
    }

    @Test
    public void deactivateEmployeeOneInactiveBank() {
        loadEmployee();

        //Add Employee Bank Account
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee = Employee.findEmployee(company, "EE1");
        EmployeeBankAccount employeeBankAccount = company1DataLoader.persistEEBA(company, employee, company1DataLoader.getEmployee1BankAccount());
        PayrollServices.commitUnitOfWork();
        
        // Create EmployeeBankAccountDTO
        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        String sourceBankAccountId = employeeBankAccount.getSourceBankAccountId();
        employeeBankAccountDTO.setEmployeeBankAccountId(sourceBankAccountId);

        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Deactivate Employee Bank Account", processResult);

        // Verify that the new status is Inactive
        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, sourceBankAccountId);
        PayrollServices.commitUnitOfWork();
        assertEquals("Employee Bank Account Status:", BankAccountStatus.Inactive, employeeBankAccount.getStatusCd());

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company.getSourceCompanyId(), employee.getSourceEmployeeId(),null);
        PayrollServices.commitUnitOfWork();
        assertSuccess("deactivateEmployee", pr);

        //Validate Employee
        PayrollServices.beginUnitOfWork();
        Employee ee = PayrollServices.entityFinder.findById(Employee.class, pr.getResult().getId());
        validateEmployee(ee);
        PayrollServices.commitUnitOfWork();        
    }

    @Test
    public void deactivateEmployee2BanksOneInactive() {
        loadEmployee();

        //Add Employee Bank Accounts
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee = Employee.findEmployee(company, "EE1");
        EmployeeBankAccount employeeBankAccount = company1DataLoader.persistEEBA(company, employee, company1DataLoader.getEmployee1BankAccount());

        EmployeeBankAccountDTO ebaDTO = company1DataLoader.getEmployee1BankAccount();
        ebaDTO.setEmployeeBankAccountId("EEBA2");
        EmployeeBankAccount eba2 = company1DataLoader.persistEEBA(company, employee, ebaDTO);

        PayrollServices.commitUnitOfWork();

        // Create EmployeeBankAccountDTO
        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        String sourceBankAccountId = employeeBankAccount.getSourceBankAccountId();
        employeeBankAccountDTO.setEmployeeBankAccountId(sourceBankAccountId);

        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Deactivate Employee Bank Account", processResult);

        // Verify that the Employee Bank Account1 status is Inactive
        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, sourceBankAccountId);
        PayrollServices.commitUnitOfWork();
        assertEquals("Employee Bank Account Status:", BankAccountStatus.Inactive, employeeBankAccount.getStatusCd());

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company.getSourceCompanyId(), employee.getSourceEmployeeId(),null);
        PayrollServices.commitUnitOfWork();
        assertSuccess("deactivateEmployee", pr);        

        //Validate Employee
        PayrollServices.beginUnitOfWork();
        Employee ee = PayrollServices.entityFinder.findById(Employee.class, pr.getResult().getId());
        validateEmployee(ee);
        PayrollServices.commitUnitOfWork();

        // Verify that the Employee Bank Account1 status is Inactive
        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, eba2.getSourceBankAccountId());
        PayrollServices.commitUnitOfWork();
        assertEquals("Employee Bank Account Status:", BankAccountStatus.Inactive, employeeBankAccount.getStatusCd());
    }

    @Test
    public void deactivateEmployeeEventCheck() {
        loadEmployee();
        
        Company company=null;
        PayrollServices.beginUnitOfWork();
        company=Company.findCompany("1234567", SourceSystemCode.QBOE);
        company.setSourceSystemCd(SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.commitUnitOfWork();
        

        //Deactivate And Delete Employee
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("1234567", SourceSystemCode.QBDT);
        Employee employee1 = Employee.findEmployee(company1, "EE1");
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBDT, company1.getSourceCompanyId(), employee1.getSourceEmployeeId(),null);
        ProcessResult<Employee> pr2 = PayrollServices.employeeManager.deleteEmployee(SourceSystemCode.QBDT, company1.getSourceCompanyId(), employee1.getSourceEmployeeId());
        PayrollServices.commitUnitOfWork();

        assertSuccess("deactivateEmployee", pr);
        assertSuccess("deleteEmployee", pr2);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company1, EventTypeCode.EmployeeDeleted);
        assertEquals("Company Events", 2, companyEventsList.size());
        assertEquals("Event Detail Code", 1,
                companyEventsList.get(0).getCompanyEventDetails(EventDetailTypeCode.EmployeeId).size());
        assertEquals("Event Detail Code", 1,
                companyEventsList.get(1).getCompanyEventDetails(EventDetailTypeCode.EmployeeId).size());
        assertEquals("Event Detail Value", employee1.getId().toString(), companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
        assertEquals("Event Detail Value", employee1.getId().toString(), companyEventsList.get(1).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
        PayrollServices.commitUnitOfWork();
    }

    //@Test
/*
    public void deactivateEmployee2BanksBothActive() {
        PayrollServices.beginUnitOfWork();
        loadEmployee(ACTIVE_COMPANY, ACTIVE_EE, ACTIVE_STATUS);
        loadEmployeeBankAccount(ACTIVE_COMPANY, ACTIVE_EE, TEST_BANK_1, ACTIVE_STATUS, ACCT_NUM);
        loadEmployeeBankAccount(ACTIVE_COMPANY, ACTIVE_EE, TEST_BANK_2, ACTIVE_STATUS, ACCT_NUM);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, ACTIVE_COMPANY, ACTIVE_EE);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess("deactivateEmployee", pr);

        // verify the employee itself
        Employee ee = PayrollServices.entityFinder.findById(Employee.class, pr.getResult().getId());
        validateEmployee(ee);

        EmployeeBankAccount eba = EmployeeBankAccount.findEmployeeBankAccount(ee, TEST_BANK_1);
        assertEquals("EE Bank Status:", BankAccountStatus.Inactive, eba.getStatusCd());

        eba = EmployeeBankAccount.findEmployeeBankAccount(ee, TEST_BANK_2);
        assertEquals("EE Bank Status:", BankAccountStatus.Inactive, eba.getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    //@Test
    public void deactivateEmployee2BanksBothInactive() {
        PayrollServices.beginUnitOfWork();
        loadEmployee(ACTIVE_COMPANY, ACTIVE_EE, ACTIVE_STATUS);
        loadEmployeeBankAccount(ACTIVE_COMPANY, ACTIVE_EE, TEST_BANK_1, INACTIVE_STATUS, ACCT_NUM);
        loadEmployeeBankAccount(ACTIVE_COMPANY, ACTIVE_EE, TEST_BANK_2, INACTIVE_STATUS, ACCT_NUM);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, ACTIVE_COMPANY, ACTIVE_EE);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess("deactivateEmployee", pr);

        // verify the employee itself
        Employee ee = PayrollServices.entityFinder.findById(Employee.class, pr.getResult().getId());
        validateEmployee(ee);

        EmployeeBankAccount eba = EmployeeBankAccount.findEmployeeBankAccount(ee, TEST_BANK_1);
        assertEquals("EE Bank Status:", BankAccountStatus.Inactive, eba.getStatusCd());

        eba = EmployeeBankAccount.findEmployeeBankAccount(ee, TEST_BANK_2);
        assertEquals("EE Bank Status:", BankAccountStatus.Inactive, eba.getStatusCd());
        PayrollServices.commitUnitOfWork();
    }
*/


    /**
     * Helper method for validating employee output
     *
     * @param pEmployee Expected Employee
     */
    private void validateEmployee(Employee pEmployee) {
        // verify the employee itself
        Employee updatedEmployee = Employee.findEmployee(pEmployee
                .getCompany(), pEmployee.getSourceEmployeeId());
        assertEquals("Employee Source Company Id:", pEmployee.getCompany()
                .getSourceCompanyId(), updatedEmployee.getCompany()
                .getSourceCompanyId());
        assertEquals("Employee Source Id:", pEmployee.getSourceEmployeeId(),
                updatedEmployee.getSourceEmployeeId());
        assertEquals("Employee Status Cd:", EmployeeStatus.Inactive,
                updatedEmployee.getStatusCd());
    }

    private void loadEmployee(){
      PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistCompany(company1DataLoader.getCompany1());

        dataloader.persistCompanyService(company1, company1DataLoader.getCompany1Service());

        dataloader.persistCompanyBankAccount(company1, dataloader.getTestCompanyBankAccount());
        company1DataLoader.persistEmployee(company1DataLoader.getEmployee1(company1));

        PayrollServices.commitUnitOfWork();
    }
}
