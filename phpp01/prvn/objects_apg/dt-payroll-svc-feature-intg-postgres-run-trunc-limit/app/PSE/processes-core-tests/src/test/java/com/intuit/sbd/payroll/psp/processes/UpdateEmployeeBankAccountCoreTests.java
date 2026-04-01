package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Marcela Villani
 */
public class UpdateEmployeeBankAccountCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void updateEmployeeBankAccountSuccessful_OnlyBankName() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", "Active");
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);

        // Create EmployeeBankAccountDTO channging only the Bank Name
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        String sourceBankAccountId = employeeBankAccount.getSourceBankAccountId();
        employeeBankAccountDTO.setEmployeeBankAccountId(sourceBankAccountId);
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setAccountNumber(employeeBankAccount.getBankAccount().getAccountNumber());
        bankAccountDTO.setRoutingNumber(employeeBankAccount.getBankAccount().getRoutingNumber());
        bankAccountDTO.setBankName("NewBankName");
        employeeBankAccountDTO.setBankAccount(bankAccountDTO);

        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        employeeBankAccount = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        PayrollServices.beginUnitOfWork();

        // Verify that employeeBankAccount has been saved
        employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, sourceBankAccountId);
        assertTrue("Employee Bank Account:", employeeBankAccount != null);

        // Verify that the bank name has changed
        assertEquals("Bank Name:", "NewBankName", employeeBankAccount.getBankAccount().getBankName());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateEmployeeBankAccountBankAccountSuccessful_CreateNewOne() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", "Active");
        Employee employee = company.getDirectDepositEmployees().get(0);
        String sourceEmployeeId = employee.getSourceEmployeeId();
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);

        String sourceBankAccountId = employeeBankAccount.getSourceBankAccountId();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO(sourceBankAccountId);
        PayrollServicesTest.save(employeeBankAccount);
        PayrollServices.commitUnitOfWork();

        // Update the same employee bank account with different data
        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        out.println(processResult);

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
        assertTrue("Employee Bank Account:", employeeBankAccount != null);
        PayrollServices.commitUnitOfWork();

    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void companyDoesNotExist() {
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(SourceSystemCode.QBOE, "InvalidCompanyId", "Emp01", GenerateData.getEmployeeBankAccountDTO("NewEBATest"));
        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());

        // Commit
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     *  Test error message 177 - Company is not active
     */
    public void companyNotActive() {
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", "Active");
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);

        // Inactivate company
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), company.getDirectDepositEmployees().get(0).getSourceEmployeeId(),
                GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId()));

        // Commit
        PayrollServices.commitUnitOfWork();
        System.out.println(processResult);

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

        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), "InvalidEmployee", GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId()));

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "168", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Employee InvalidEmployee for company QBOE:123272727 does not exist.", message.getMessage());

        // Commit
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test error message 178 - Employee is not active
     */

    @Test
    public void employeeNotActive() {
        PayrollServices.beginUnitOfWork();
        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", "Active");
        PayrollServices.commitUnitOfWork();

        //Deactivate Employee
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBOE);
        Employee employee = company.getDirectDepositEmployees().get(0);
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company.getSourceCompanyId(), employee.getSourceEmployeeId(), null);
        PayrollServices.commitUnitOfWork();

        assertSuccess("deactivateEmployee", pr);

        //Update Employee Bank Account
        PayrollServices.beginUnitOfWork();
        employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId()));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 2, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "178", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Employee " + employee.getSourceEmployeeId() + " for company QBOE:123272727 is not active.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    /**
     * Test error message 166 - Employee Bank Account Does Not Exist
     */

    @Test
    public void employeeBankAccountDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        Employee employee = company.getDirectDepositEmployees().get(0);
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), GenerateData.getEmployeeBankAccountDTO("AcctDoesNotExist"));

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "166", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Bank Account AcctDoesNotExist for employee Emp1 does not exist.";
        assertEquals("Error Message", messageText, message.getMessage());

        // Commit
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test error message 187 - Employee Bank Account Exists and is Inactive
     */

    @Test
    public void employeeBankAccountExistsAndNotActive() {
        PayrollServices.beginUnitOfWork();
        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", "Active");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId());
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        employeeBankAccount = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        assertSuccess("Deactivate Employee Bank Account", processResult);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "187", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Bank Account " + employeeBankAccount.getSourceBankAccountId() + " for employee Emp1 is not active in the PSE.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    @Test
    public void testInvalidEmployeeBankAccountDTO() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBOE, "123272727", true, ServiceCode.DirectDeposit);
        PayrollServices.beginUnitOfWork();
        Employee employee = Employee.findEmployees(company).getFirst();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("ThereAreMoreThan50CharactersInSourceEmployeeBankAccountId");
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(SourceSystemCode.QBOE, "123272727", employee.getSourceEmployeeId(), employeeBankAccountDTO);

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "EmployeeBankAccountId has invalid value";
        assertEquals("Error Message", messageText, message.getMessage());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testInvalidBankAccountDTO() {
        PayrollServices.beginUnitOfWork();

        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("EmpBankAcct1");
        //  Test Null Bank Account DTO
        employeeBankAccountDTO.setBankAccount(null);
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(SourceSystemCode.QBOE, "123272727", "Emp1", employeeBankAccountDTO);

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
        processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(SourceSystemCode.QBOE, "123272727", "Emp1", employeeBankAccountDTO);

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "255", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Invalid Routing Number 1234 specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testInvalidCompanyParameters() {
        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("EmpAcct01");
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(null, "123272727", "EmpBankAcct01", employeeBankAccountDTO);
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
        processResult = PayrollServices.employeeManager.updateEmployeeBankAccount(SourceSystemCode.QBOE, null, "EmpBankAcct01", employeeBankAccountDTO);
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
