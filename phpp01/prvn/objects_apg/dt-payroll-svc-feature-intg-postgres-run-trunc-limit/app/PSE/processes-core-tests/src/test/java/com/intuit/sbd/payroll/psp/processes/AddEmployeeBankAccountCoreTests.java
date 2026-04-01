package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;


/**
 *
 * User: mvillani
 * Date: Sep 17, 2007
 * Time: 10:48:34 AM

 */
public class AddEmployeeBankAccountCoreTests {


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
    public void addEmployeeBankAccountSuccessful() {

        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);

        // Verify that no  validation errors have been returned
        assertSuccess("addEmployeeBankAccount", processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());

        // Verify that employeeBankAccount has been saved
        EmployeeBankAccount employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest");
        PayrollServices.commitUnitOfWork();
        assertTrue("Employee Bank Account:", employeeBankAccount != null);

    }

    @Test
    public void addEmployeeBankAccountBankAccountExistsAndIsInactive() {
        PayrollServices.beginUnitOfWork();
        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", null);
        PayrollServices.commitUnitOfWork();

        //Deactivate Employee Bank Account
        PayrollServices.beginUnitOfWork();
        Employee employee = company.getDirectDepositEmployees().get(0);
        String sourceEmployeeId = employee.getSourceEmployeeId();
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        String sourceBankAccountId = employeeBankAccount.getSourceBankAccountId();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId());
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        // Add same employee bank account
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        // Verify that correct data was persisted
        PayrollServices.beginUnitOfWork();
        employee = Employee.findEmployee(company, sourceEmployeeId);

        // Verify that employeeBankAccount has been saved and there are now two accounts in the database,
        // one Active and one Inactive
        assertEquals("Total Number of Accounts:", 2, employee.getEmployeeBankAccountCollection().size());
        // Find Inactive Account
        Criterion<EmployeeBankAccount> where = EmployeeBankAccount.Employee().equalTo(employee)
                                                  .And(EmployeeBankAccount.SourceBankAccountId().equalTo(sourceBankAccountId));

        where = where.And(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Inactive));

        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = Application.find(EmployeeBankAccount.class, where);
        assertEquals("Number of Inactive Accounts:", 1, employeeBankAccounts.size());
        // Find Active Account
        employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, sourceBankAccountId);
        PayrollServices.commitUnitOfWork();
        assertTrue("Employee Bank Account:", employeeBankAccount != null);
    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void companyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.
                addEmployeeBankAccount(SourceSystemCode.QBOE, "InvalidCompanyId", "Emp01", GenerateData.getEmployeeBankAccountDTO("NewEBATest"));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());
    }

    @Test
    /**
     *  Test error message 177 - Company is not active
     */
    public void companyNotActive() {
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadInactiveCompany();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(
                SourceSystemCode.QBOE, company.getSourceCompanyId(),
                company.getDirectDepositEmployees().get(0).getSourceEmployeeId(),
                GenerateData.getEmployeeBankAccountDTO("NewEBATest"));
        PayrollServices.commitUnitOfWork();
        
        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "The operation ChangeEmployeeBankAccount is not allowed for company QBOE:123272727 in its current state.",
                message.getMessage());
    }

    /**
     * Test error message 168 - Employee does not exist
     */

    @Test
    public void employeeDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), "InvalidEmployee", employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "168", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Employee InvalidEmployee for company QBOE:123272727 does not exist.", message.getMessage());

    }

    /**
     * Test error message 178 - Employee is not active
     */

    @Test
    public void employeeNotActive() {
        PayrollServices.beginUnitOfWork();
        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Inactive", null);
        PayrollServices.commitUnitOfWork();

        //Deactivate Employee
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBOE);
        Employee employee = company.getDirectDepositEmployees().get(0);
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company1.getSourceCompanyId(), employee.getSourceEmployeeId(),null);
        PayrollServices.commitUnitOfWork();

        assertSuccess("deactivateEmployee", pr);
        
        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employee = company.getDirectDepositEmployees().get(0);
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "178", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Employee " + employee.getSourceEmployeeId() + " for company QBOE:123272727 is not active.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    /**
     * Test error message 164 - Employee Bank Account already Exists
     */

    @Test
    public void employeeBankAccountExistsAndNotInactive() {
        PayrollServices.beginUnitOfWork();
        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", "Active");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());

        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "164", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "BankAccount " + employeeBankAccount.getSourceBankAccountId() + " for employee Emp1 already exists.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    @Test
    public void testInvalidEmployeeBankAccountDTO() {
        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("ThereAreMoreThan50CharactersInSourceEmployeeBankAccountId");
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(
                SourceSystemCode.QBOE, "123272727", "Emp1", employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "EmployeeBankAccountId has invalid value";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    @Test
    public void testInvalidBankAccountDTO() {
        PayrollServices.beginUnitOfWork();

        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("EmpBankAcct1");
        //  Test Null Bank Account DTO
        employeeBankAccountDTO.setBankAccount(null);
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(
                SourceSystemCode.QBOE, "123272727", "Emp1", employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "142", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Bank Account is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        // Test Invalid Routing Number
        employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("EmpBankAcct1");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("1234");
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.employeeManager.addEmployeeBankAccount(SourceSystemCode.QBOE, "123272727",
                                                                                "Emp1", employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "255", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Invalid Routing Number 1234 specified.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    @Test
    public void testInvalidCompanyParameters() {
        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("EmpAcct01");
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(null, "123272727", "EmpBankAcct01", employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.employeeManager.addEmployeeBankAccount(
                            SourceSystemCode.QBOE, null, "EmpBankAcct01", employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

}
