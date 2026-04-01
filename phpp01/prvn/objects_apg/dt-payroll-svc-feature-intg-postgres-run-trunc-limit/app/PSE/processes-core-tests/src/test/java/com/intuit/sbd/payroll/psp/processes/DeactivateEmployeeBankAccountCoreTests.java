package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import static java.lang.System.out;

/**
 *
 * User: mvillani
 * Date: Oct 10, 2007
 * Time: 11:20:34 AM

 */
public class DeactivateEmployeeBankAccountCoreTests {

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
    public void deactivateEmployeeBankAccountSuccessful() {

        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", "Active");
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);

        // Create EmployeeBankAccountDTO channging only the Bank Name
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        String sourceBankAccountId = employeeBankAccount.getSourceBankAccountId();
        employeeBankAccountDTO.setEmployeeBankAccountId(sourceBankAccountId);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        employeeBankAccount = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        // Verify that employeeBankAccount has been saved
        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, sourceBankAccountId);
        assertTrue("Employee Bank Account:", employeeBankAccount != null);

        // Verify that the new status is Inactive
        assertEquals("Employee Bank Account Status:", BankAccountStatus.Inactive, employeeBankAccount.getStatusCd());
        PayrollServices.commitUnitOfWork();
    }


    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void companyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(SourceSystemCode.QBOE, "InvalidCompanyId", "Emp01", GenerateData.getEmployeeBankAccountDTO("NewEBATest"));
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());
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
        PayrollServices.commitUnitOfWork();

        // Inactivate company
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Deactivate Service", cancelServiceProcessResult);
        
        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), company.getDirectDepositEmployees().get(0).getSourceEmployeeId(),
                GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId()));
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
        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), "InvalidEmployee", GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId()));
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
     * Test error message 166 - Employee Bank Account Does Not Exist
     */

    @Test
    public void employeeBankAccountDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        // Load Data
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Employee employee = company.getDirectDepositEmployees().get(0);
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), GenerateData.getEmployeeBankAccountDTO("AcctDoesNotExist"));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "166", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Bank Account AcctDoesNotExist for employee Emp1 does not exist.";
        assertEquals("Error Message", messageText, message.getMessage());
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

        //Deactivate Employee Bank Account
        PayrollServices.beginUnitOfWork();
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId());
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        //Deactivate Employee Bank Account Again
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
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
        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("ThereAreMoreThan50CharactersInSourceEmployeeBankAccountId");
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(SourceSystemCode.QBOE, "123272727", "Emp1", employeeBankAccountDTO);
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
    public void testInvalidCompanyParameters() {
        PayrollServices.beginUnitOfWork();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("EmpAcct01");
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(null, "123272727", "EmpBankAcct01", employeeBankAccountDTO);
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
        processResult = PayrollServices.employeeManager.deactivateEmployeeBankAccount(SourceSystemCode.QBOE, null, "EmpBankAcct01", employeeBankAccountDTO);
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
