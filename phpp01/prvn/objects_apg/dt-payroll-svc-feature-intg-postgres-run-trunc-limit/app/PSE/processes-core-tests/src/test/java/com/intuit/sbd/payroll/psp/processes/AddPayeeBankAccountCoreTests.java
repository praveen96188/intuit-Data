package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * User: mvillani
 * Date: Sep 17, 2007
 * Time: 10:48:34 AM
 */
public class AddPayeeBankAccountCoreTests {

    private static Company3Dataloader c3dl = new Company3Dataloader();

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
    public void addPayeeBankAccountSuccessful() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime((SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone())));
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();
        PayeeDTO payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        // Load Data
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        Payee payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());
        PayeeBankAccountDTO payeeBankAccountDTO = GenerateData.getPayeeBankAccountDTO("NewPBATest");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayeeBankAccount> processResult = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO);

        // Verify that no  validation errors have been returned
        assertSuccess("addOrUpdatePayeeBankAccount", processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());

        // Verify that payeeBankAccount has been saved
        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, "NewPBATest");
        assertTrue("Payee Bank Account:", payeeBankAccount != null);
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testCompanyEventsForPBA() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime((SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone())));
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();
        PayeeDTO payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        // Load Data
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        Payee payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());
        PayeeBankAccountDTO payeeBankAccountDTO = GenerateData.getPayeeBankAccountDTO("PBATest");
        PayrollServices.commitUnitOfWork();

        /** Test no 1*/
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayeeBankAccount> processResult = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount
                (company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO);

        // Verify that no  validation errors have been returned
        assertSuccess("addOrUpdatePayeeBankAccount", processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Verify that payeeBankAccount has been saved
        Application.refresh(payee);
        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, "PBATest");
        assertTrue("Payee Bank Account:", payeeBankAccount != null);

        // Verify that Event is created in Company_Event table
        DomainEntitySet<CompanyEvent> compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayeeBankAccountChange);
        Assert.assertTrue("Number of PBA Change events", 1 == compEvents.size());

        // Verify that Event is created in Company_Event_Detail table
        DomainEntitySet<CompanyEventDetail> compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.NewPayeeBankAccountNumber, payeeBankAccount.getBankAccount().getAccountNumber());
        assertTrue("Number of New PBA detail events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.NewPayeeBankRoutingNumber, payeeBankAccount.getBankAccount().getRoutingNumber());
        assertTrue("Number of New change events", 1 == compEventDetails.size());

        compEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.OldPayeeBankAccountNumber);
        assertTrue("Number of Old PBA detail events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.OldPayeeBankRoutingNumber);
        assertTrue("Number of Old PBA detail events", 1 == compEventDetails.size());
        PayrollServices.rollbackUnitOfWork();

        /** Test no 2*/
        /*Same Source_Bank_ID with only Bank name changed
        Exp o/p : Same PayeeBankAccount record will be updated with Bank name*/
        PayrollServices.beginUnitOfWork();
        payeeBankAccountDTO.getBankAccount().setBankName("BankRenamed");
        processResult = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertSuccess("addOrUpdatePayeeBankAccount", processResult);

        // Verify that payeeBankAccount has been renamed
        PayrollServices.beginUnitOfWork();
        Application.refresh(payee);
        payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, "PBATest");
        assertEquals("BankRenamed", payeeBankAccount.getBankAccount().getBankName());

        // Verify that no New Event is created in Company_Event table
        compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayeeBankAccountChange);
        Assert.assertTrue("Total Number of PBA Change events", 1 == compEvents.size());

        PayrollServices.rollbackUnitOfWork();

        /** Test no 3*/
        /*Same Source_Bank_ID with diff bank acc details
        Exp o/p : Activate both old & new PayeeBankAccount and create PBA Change event*/
        PayrollServices.beginUnitOfWork();
        PayeeBankAccountDTO payeeBankAccountDTO2 = GenerateData.getPayeeBankAccountDTO("PBATest");
        processResult = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO2);
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertSuccess("addOrUpdatePayeeBankAccount", processResult);

        PayrollServices.beginUnitOfWork();
        Application.refresh(payee);
        String oldAccNum = payeeBankAccount.getBankAccount().getAccountNumber();
        String oldRoutingNum = payeeBankAccount.getBankAccount().getRoutingNumber();

        // Verify that payeeBankAccount has been saved
        payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, "PBATest");
        assertEquals("Active", payeeBankAccount.getStatusCd().toString());

        // Verify that Event is created in Company_Event table
        compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayeeBankAccountChange);
        Assert.assertTrue("Number of total PBA change events", 2 == compEvents.size());

        // Verify that Event is created in Company_Event_Detail table
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.NewPayeeBankAccountNumber, payeeBankAccount.getBankAccount().getAccountNumber());
        assertTrue("Number of New change events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.NewPayeeBankRoutingNumber, payeeBankAccount.getBankAccount().getRoutingNumber());
        assertTrue("Number of New change events", 1 == compEventDetails.size());

        compEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.OldPayeeBankAccountNumber, oldAccNum);
        assertTrue("Number of Old PBA detail events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.OldPayeeBankRoutingNumber, oldRoutingNum);
        assertTrue("Number of Old PBA detail events", 1 == compEventDetails.size());
        PayrollServices.rollbackUnitOfWork();

        /** Test no 4*/
        /*Diff Source_Bank_ID with diff bank acc details
        Exp o/p : Activate both old & new PayeeBankAccount and create PBA Change event*/
        PayrollServices.beginUnitOfWork();
        PayeeBankAccountDTO payeeBankAccountDTO3 = GenerateData.getPayeeBankAccountDTO("PBATestNew");
        processResult = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO3);
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertSuccess("addOrUpdatePayeeBankAccount", processResult);

        PayrollServices.beginUnitOfWork();
        Application.refresh(payee);
        oldAccNum = payeeBankAccount.getBankAccount().getAccountNumber();
        // Verify that payeeBankAccount has been saved
        payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, "PBATestNew");
        assertTrue("Payee Bank Account:", payeeBankAccount != null);
        assertEquals("Active", payeeBankAccount.getStatusCd().toString());

        // Verify that Event is created in Company_Event table
        compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayeeBankAccountChange);
        Assert.assertTrue("Number of total PBA change events", 3 == compEvents.size());

        // Verify that Event is created in Company_Event_Detail table
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.NewPayeeBankAccountNumber, payeeBankAccount.getBankAccount().getAccountNumber());
        Assert.assertTrue("Number of New change events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.NewPayeeBankRoutingNumber, payeeBankAccount.getBankAccount().getRoutingNumber());
        assertTrue("Number of New change events", 1 == compEventDetails.size());

        compEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.OldPayeeBankAccountNumber, oldAccNum);
        assertTrue("Number of Old PBA detail events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.PayeeBankAccountChange, EventDetailTypeCode.OldPayeeBankRoutingNumber, oldRoutingNum);
        assertTrue("Number of Old PBA detail events", 1 == compEventDetails.size());
        PayrollServices.rollbackUnitOfWork();

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
        ProcessResult<Employee> pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, company1.getSourceCompanyId(), employee.getSourceEmployeeId(), null);
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

    public PayeeDTO getTestPayee() {
        PayeeDTO payee = new PayeeDTO();
        payee.setSourcePayeeId("TESTADDPAYEE");
        payee.setName("Add Payee Core Test");
        payee.setPhone("775-227-7227");
        payee.setTaxId("123456789");

        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("123 High Country Rd");
        mailingAddress.setCity("Reno");
        mailingAddress.setState("NV");
        mailingAddress.setZipCode("89502");
        mailingAddress.setCountry("USA");
        payee.setMailingAddress(mailingAddress);

        return payee;
    }
}