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

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

public class WalletCreateCoreTests {

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
     * Test - Successfully creates the wallet id for new employee bank account
     */
    @Test
    public void createEmployeeWalletSuccess() {
        // Load Data
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
        assertSuccess("addEmployeeBankAccount", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String walletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is non null", walletId);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountWalletSuccess);
        assertTrue("Wallet Create Success Event is present", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Successfully creates the wallet id for new employee bank account with employee name and phone containing invalid character
     * We will sanitise name and phone and then make a wallet create call to avoid failures
     */
    @Test
    public void createEmployeeWalletSuccessForInvalidPhoneAndName() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        company.setIAMRealmId("9130349397822666");
        Application.save(company);
        Employee employee = company.getDirectDepositEmployees().get(0);
        employee.setFirstName("Sample {Employee}");
        employee.setPhone("123454325653{ca}");
        Application.save(employee);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("addEmployeeBankAccount", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String walletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is non null", walletId);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountWalletSuccess);
        assertTrue("Wallet Create Success Event is present", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Successfully creates the wallet id for new employee bank account and phone containing invalid character
     * We will sanitise phone, validate it using regex and then make a wallet create call without phone number(if null/not passing criteria) to avoid failures
     */
    @Test
    public void createEmployeeWalletSuccessForNullPhone() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        company.setIAMRealmId("9130349397822666");
        Application.save(company);
        Employee employee = company.getDirectDepositEmployees().get(0);
        employee.setFirstName("Sample {Employee}");
        employee.setPhone("1235653{ca}");
        Application.save(employee);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("addEmployeeBankAccount", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String walletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is non null", walletId);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountWalletSuccess);
        assertTrue("Wallet Create Success Event is present", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Failed to create the wallet id for new employee bank account with account number containing invalid characters
     * Wallet call will fail with Failure event and no wallet id
     */
    @Test
    public void createEmployeeWalletInvalidAccountNumberFailure() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        company.setIAMRealmId("9130349397822666");
        Application.save(company);
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        employeeBankAccountDTO.getBankAccount().setAccountNumber("abcd$32345");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("addEmployeeBankAccount", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String walletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNull("WalletId is null", walletId);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountWalletFailure);
        assertTrue("Wallet Create Failure Event is  present", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - No wallet id will be created for new employee bank account with no realm id in company
     * Wallet Services require realm id to be present in company.
     * We validate for realm id to exist, the call to Wallet service will not be made if realm id doesnt exist
     */
    @Test
    public void createEmployeeWalletNoRealmId() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("addEmployeeBankAccount", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String walletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNull("WalletId is null", walletId);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountWalletSuccess);
        assertTrue("Wallet Create Event is not present", companyEvents.isEmpty());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Successfully creates the wallet id for 2 new split employee bank account for single employee
     * 2 wallet calls will be made for two different EBA
     */
    @Test
    public void createEmployeeWalletSplitAccountForEmployee() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        company.setIAMRealmId("9130349397822666");
        Application.save(company);
        Employee employee = company.getDirectDepositEmployees().get(0);

        List<EmployeeBankAccountDTO> employeeBankAccountDTOList = new ArrayList<>();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        employeeBankAccountDTO.setOrder(0);
        employeeBankAccountDTOList.add(employeeBankAccountDTO);

        EmployeeBankAccountDTO employeeBankAccountDTO1 = GenerateData.getEmployeeBankAccountDTO("NewEBATest1");
        employeeBankAccountDTO1.getBankAccount().setRoutingNumber("124000012");
        employeeBankAccountDTO1.setOrder(1);
        employeeBankAccountDTOList.add(employeeBankAccountDTO1);
        PayrollServices.commitUnitOfWork();

        for(EmployeeBankAccountDTO employeeBankAccountDTO2 : employeeBankAccountDTOList) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO2);
            assertSuccess("addEmployeeBankAccount", processResult);
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String walletId1 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", walletId1);

        String walletId2 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest1")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", walletId2);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountWalletSuccess);
        assertTrue("Wallet Create Event is present", companyEvents.size() == 2);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Successfully update the wallet id for new employee bank account for existing employee
     * Change in account number will change wallet id
     */
    @Test
    public void updateEmployeeBankAccountSuccess() {
        // Load Data
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
        assertSuccess("addEmployeeBankAccount", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String oldWalletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is non null", oldWalletId);
        PayrollServices.commitUnitOfWork();

        employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult1 = PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("updateEmployeeBankAccount", processResult1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String newWalletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is non null", newWalletId);

        assertNotSame("Wallet Id for new EBA has changed", newWalletId, oldWalletId);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Wallet id will not be updated for existing employee bank account with existing wallet id and only bank name change
     * Change in bank name will not change wallet id
     */
    @Test
    public void updateEmployeeBankAccountNameOnlySuccess() {
        // Load Data
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
        assertSuccess("addEmployeeBankAccount", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String oldWalletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is non null", oldWalletId);
        PayrollServices.commitUnitOfWork();

        employeeBankAccountDTO.getBankAccount().setBankName("ICICI");
        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult1 = PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("updateEmployeeBankAccount", processResult1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String newWalletId = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is non null", newWalletId);

        assertTrue("Wallet Id for EBA remains unchanged", newWalletId.equals(oldWalletId));
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Successfully changing default EBA i.e changing account order
     * Wallet id will not be updated for existing employee bank account with existing wallet id
     * Wallet service will override the default wallet in their system
     */
    @Test
    public void updateEmployeeWalletForAccountOrderChangeSuccess() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        company.setIAMRealmId("9130349397822666");
        Application.save(company);
        Employee employee = company.getDirectDepositEmployees().get(0);

        List<EmployeeBankAccountDTO> employeeBankAccountDTOList = new ArrayList<>();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        employeeBankAccountDTO.setOrder(0);
        employeeBankAccountDTOList.add(employeeBankAccountDTO);

        EmployeeBankAccountDTO employeeBankAccountDTO1 = GenerateData.getEmployeeBankAccountDTO("NewEBATest1");
        employeeBankAccountDTO1.getBankAccount().setRoutingNumber("124000012");
        employeeBankAccountDTO1.setOrder(1);
        employeeBankAccountDTOList.add(employeeBankAccountDTO1);
        PayrollServices.commitUnitOfWork();

        for(EmployeeBankAccountDTO employeeBankAccountDTO2 : employeeBankAccountDTOList) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO2);
            assertSuccess("addEmployeeBankAccount", processResult);
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String walletId1 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", walletId1);

        String walletId2 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest1")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", walletId2);
        PayrollServices.commitUnitOfWork();

        List<EmployeeBankAccountDTO> employeeBankAccountDTOList2 = new ArrayList<>();
        employeeBankAccountDTO.setEmployeeBankAccountId("NewEBATest2");
        employeeBankAccountDTO.setOrder(1);
        employeeBankAccountDTOList2.add(employeeBankAccountDTO);

        employeeBankAccountDTO1.setEmployeeBankAccountId("NewEBATest3");
        employeeBankAccountDTO1.setOrder(0);
        employeeBankAccountDTOList2.add(employeeBankAccountDTO1);

        for(EmployeeBankAccountDTO employeeBankAccountDTO2 : employeeBankAccountDTOList2) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO2);
            assertSuccess("addEmployeeBankAccount", processResult);
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String newWalletId1 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest2")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", newWalletId1);

        String newWalletId2 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest3")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", newWalletId2);

        assertTrue("New and Old WalletId1 is same", newWalletId1.equals(walletId1));
        assertTrue("New and Old WalletId2 is same", newWalletId2.equals(walletId2));

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountWalletSuccess);
        assertTrue("Wallet Create Event is present", companyEvents.size() == 4);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Successfully changing default EBA i.e changing account order
     * Wallet id will not be updated for existing employee bank account with existing wallet id
     * Wallet service will override the default wallet in their system
     */
    @Test
    public void createEmployeeWalletSplitSameAccountDiffTypeForEmployee() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        company.setIAMRealmId("9130349397822666");
        Application.save(company);
        Employee employee = company.getDirectDepositEmployees().get(0);
        PayrollServices.commitUnitOfWork();

        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        employeeBankAccountDTO.setOrder(0);

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("addEmployeeBankAccount1", processResult);
        PayrollServices.commitUnitOfWork();

        employeeBankAccountDTO.setEmployeeBankAccountId("NewEBATest1");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        employeeBankAccountDTO.getBankAccount().setAccountType(BankAccountType.Savings);
        employeeBankAccountDTO.setOrder(1);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("addEmployeeBankAccount2", processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String walletId1 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", walletId1);

        String walletId2 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest1")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", walletId2);

        assertFalse("WalletId not same", walletId1.equals(walletId2));

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountWalletSuccess);
        assertTrue("Wallet Create Event is present", companyEvents.size() == 2);
        PayrollServices.commitUnitOfWork();
    }

    //leading zeros for bank account number - same walletid
    @Test
    public void createEmployeeWalletSameAccountLeadingZeroForEmployee() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        company.setIAMRealmId("9130349397822666");
        Application.save(company);
        Employee employee = company.getDirectDepositEmployees().get(0);
        PayrollServices.commitUnitOfWork();

        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO("NewEBATest");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("addEmployeeBankAccount1", processResult);
        PayrollServices.commitUnitOfWork();

        employeeBankAccountDTO.setEmployeeBankAccountId("NewEBATest1");
        employeeBankAccountDTO.getBankAccount().setAccountNumber("0000" + employeeBankAccountDTO.getBankAccount().getAccountNumber());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("addEmployeeBankAccount2", processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());
        String walletId1 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", walletId1);

        String walletId2 = EmployeeBankAccount.findEmployeeBankAccount(employee, "NewEBATest1")
                .getBankAccount().getWalletId();
        assertNotNull("WalletId is not null", walletId2);

        assertTrue("WalletId not same", walletId1.equals(walletId2));

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountWalletSuccess);
        assertTrue("Wallet Create Event is present", companyEvents.size() == 2);
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test - Successfully creates the wallet id for new payee bank account
     */
    @Test
    public void createVendorWalletSuccess() {
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

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.VendorBankAccountWalletSuccess);
        assertTrue("Wallet Create Success Event is present", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Successfully creates the wallet id for new payee bank account with payee name and phone containing invalid character
     * We will sanitise name and phone and then make a wallet create call to avoid failures
     */
    @Test
    public void createVendorWalletSuccessForInvalidPhoneAndName() {
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
        payeeDTO.setName("Sample {Vendor}");
        payeeDTO.setPhone("123454325653{ca}");
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

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.VendorBankAccountWalletSuccess);
        assertTrue("Wallet Create Success Event is present", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Successfully creates the wallet id for new payee bank account and phone containing invalid character
     * We will sanitise phone, validate it using regex and then make a wallet create call without phone number(if null/not passing criteria) to avoid failures
     */
    @Test
    public void createVendorWalletSuccessForNullPhone() {
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
        payeeDTO.setName("Sample {Vendor}");
        payeeDTO.setPhone("12653{ca}");
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

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.VendorBankAccountWalletSuccess);
        assertTrue("Wallet Create Success Event is present", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();    }

    /**
     * Test - Failed to create the wallet id for new payee bank account with account number containing invalid characters
     * Wallet call will fail with Failure event and no wallet id
     */
    @Test
    public void createVendorWalletInvalidAccountNumberFailure() {
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
        payeeBankAccountDTO.getBankAccount().setAccountNumber("S{abcd1234");
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
        assertTrue("Payee Bank Account Wallet Null:", payeeBankAccount.getBankAccount().getWalletId() == null);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.VendorBankAccountWalletFailure);
        assertTrue("Wallet Create Failure Event is  present", companyEvents.isNotEmpty());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - No wallet id will be created for new employee bank account with no realm id in company
     * Wallet Services require realm id to be present in company.
     * We validate for realm id to exist, the call to Wallet service will not be made if realm id doesnt exist
     */
    @Test
    public void createVendorWalletNoRealmId() {
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
        assertTrue("Payee Bank Account Wallet Null:", payeeBankAccount.getBankAccount().getWalletId() == null);
    }

    /**
     * Test - Successfully update the wallet id for new employee bank account for existing employee
     * Change in account number will change wallet id
     */
    @Test
    public void updateVendorBankAccountSuccess() {
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
        assertSuccess("addPayeeBankAccount", processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());
        PayeeBankAccountDTO payeeBankAccountDTO1 = GenerateData.getPayeeBankAccountDTO("NewPBATest1");
        payeeBankAccountDTO1.getBankAccount().setRoutingNumber("124000012");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayeeBankAccount> processResult1 = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO1);

        // Verify that no  validation errors have been returned
        assertSuccess("updatePayeeBankAccount", processResult1);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());

        // Verify that payeeBankAccount has been saved
        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, "NewPBATest");
        assertTrue("Payee Bank Account:", payeeBankAccount != null);
        assertTrue("Payee Bank Account Wallet Not Null:", payeeBankAccount.getBankAccount().getWalletId() != null);
        String oldWalletId = payeeBankAccount.getBankAccount().getWalletId();

        PayeeBankAccount payeeBankAccount1 = PayeeBankAccount.findPayeeBankAccount(payee, "NewPBATest1");
        assertTrue("New Payee Bank Account:", payeeBankAccount1 != null);
        assertTrue("New Payee Bank Account Wallet Not Null:", payeeBankAccount1.getBankAccount().getWalletId() != null);
        String newWalletId = payeeBankAccount1.getBankAccount().getWalletId();


        assertNotSame("Wallet Id for new PBA has changed", newWalletId, oldWalletId);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Wallet id will not be updated for existing payee bank account with existing wallet id and only bank name change
     * Change in bank name will not change wallet id
     */
    @Test
    public void updateVendorBankAccountNameOnlySuccess() {
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
        assertSuccess("addPayeeBankAccount", processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());

        // Verify that payeeBankAccount has been saved
        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, "NewPBATest");
        assertTrue("Payee Bank Account:", payeeBankAccount != null);
        assertTrue("Payee Bank Account Wallet Not Null:", payeeBankAccount.getBankAccount().getWalletId() != null);
        String oldWalletId = payeeBankAccount.getBankAccount().getWalletId();

        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();

        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());
        payeeBankAccountDTO.getBankAccount().setBankName("ICICI");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayeeBankAccount> processResult1 = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO);

        // Verify that no  validation errors have been returned
        assertSuccess("updatePayeeBankAccount", processResult1);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payee = Payee.findPayee(company, payeeDTO.getSourcePayeeId());

        // Verify that payeeBankAccount has been saved
        PayeeBankAccount payeeBankAccount1 = PayeeBankAccount.findPayeeBankAccount(payee, "NewPBATest");
        assertTrue("New Payee Bank Account:", payeeBankAccount1 != null);
        assertTrue("New Payee Bank Account Wallet Not Null:", payeeBankAccount1.getBankAccount().getWalletId() != null);
        String newWalletId = payeeBankAccount1.getBankAccount().getWalletId();


        assertTrue("Wallet Id for EBA remains unchanged", newWalletId.equals(oldWalletId));
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
