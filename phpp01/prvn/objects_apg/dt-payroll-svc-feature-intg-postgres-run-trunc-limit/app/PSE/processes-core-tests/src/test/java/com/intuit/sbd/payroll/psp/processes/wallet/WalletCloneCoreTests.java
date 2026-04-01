package com.intuit.sbd.payroll.psp.processes.wallet;

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
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

public class WalletCloneCoreTests {

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

    /**
     * Test - Successfully Clones the wallet id for existing employee bank account with Wallet id already present
     */
    @Test
    public void cloneEmployeeWalletForExistingWalletIdSuccess() {
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        company.setIAMRealmId("9130349397822666");
        Application.save(company);
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String oldWalletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();

        //Calling Clone wallet
        ProcessResult walletClone = new WalletCloneCore(company,"9130356311152106").execute();
        assertSuccess("cloneWalletForExistingWallet", walletClone);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String newWalletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotSame("Wallet Id is not same", oldWalletId, newWalletId);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.CloneEmployeeWalletOnRealmChangeSuccess);
        assertTrue("Wallet Clone Event", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - No Clone for the wallet id for existing employee bank account with Wallet id absent
     */
    @Test
    public void cloneEmployeeWalletForNoWalletId() {
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        company.setIAMRealmId("9130349397822666");
        Application.save(company);
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        employeeBankAccountDTO.getBankAccount().setAccountNumber("4324abc");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        //Calling Clone wallet
        ProcessResult walletClone = new WalletCloneCore(company,"9130356311152106").execute();
        assertSuccess("cloneWalletForExistingWallet", walletClone);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String newWalletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNull("Wallet Id is null", newWalletId);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.CloneEmployeeWalletOnRealmChangeSuccess);
        assertTrue("Wallet Clone Event not created", companyEvents.isEmpty());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void cloneVendorWalletForExistingWalletIdSuccess() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime((SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone())));
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company persistCompany = Company.findCompany("8574536", SourceSystemCode.QBDT);
        persistCompany.setIAMRealmId("9130349397822666");
        Application.save(persistCompany);
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
        payeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
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
        assertTrue("Payee Bank Account Wallet Not Null:", payeeBankAccount.getBankAccount().getWalletId() != null);
        String oldWalletId = payeeBankAccount.getBankAccount().getWalletId();

        ProcessResult walletClone = new WalletCloneCore(company,"9130356311152106").execute();
        assertSuccess("cloneWalletForExistingWallet", walletClone);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());

        // Verify that payeeBankAccount has been saved
        PayeeBankAccount payeeBankAccount1 = PayeeBankAccount.findPayeeBankAccount(payee, "NewPBATest");
        assertTrue("Payee Bank Account:", payeeBankAccount1 != null);
        assertTrue("Payee Bank Account Wallet Not Null:", payeeBankAccount1.getBankAccount().getWalletId() != null);
        String newWalletId = payeeBankAccount1.getBankAccount().getWalletId();

        assertNotSame("Wallet Id is not same", oldWalletId, newWalletId);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.CloneVendorWalletOnRealmChangeSuccess);
        assertTrue("Wallet Clone Event", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();
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
