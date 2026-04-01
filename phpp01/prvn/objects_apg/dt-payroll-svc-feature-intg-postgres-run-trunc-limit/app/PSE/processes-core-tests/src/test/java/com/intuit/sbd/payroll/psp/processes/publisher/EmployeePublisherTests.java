package com.intuit.sbd.payroll.psp.processes.publisher;


import com.google.gson.JsonObject;
import com.intuit.eventbus.exceptions.FormatException;
import com.intuit.eventbus.utils.Result;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.domain.Gender;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.Status;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.BankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeTestSuiteDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.hibernate.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.kafka.core.ConsumerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static org.junit.Assert.*;

public class EmployeePublisherTests {
    private static String COMPANY_123456 = "123456";
    private EmployeeDTO employeeDTO;
    private AddressDTO addressDTO;
    private EmployeeBankAccountDTO employeeBankAccountDTO;
    private static EmployeeTestSuiteDataLoader dataloader;
    private static PublishedEmployeeVerifier employeeVerifier;

    //@BeforeClass
    public static void initialize() {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
            PayrollServicesTest.truncateTables();
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            dataloader = new EmployeeTestSuiteDataLoader();
            //dataloader.loadFullSuite();
            dataloader.saveActiveCompany();
            PayrollServices.commitUnitOfWork();
            employeeVerifier = new PublishedEmployeeVerifier();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    //@Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        //initialize();
        employeeVerifier.init();

    }
    //@After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    //@Test
    public void addEmployee() throws IOException {
        employeeDTO = getTestEmployee();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityCreate, null);

    }

    //@Test
    public void addEmployeeWithBankAccount() throws IOException {
        employeeDTO = getTestEmployee();
        employeeBankAccountDTO = getTestEmployeeBankAccount(employeeDTO);

        if(Objects.isNull(employeeDTO.getEmployeeBankAccountDTOs())){
            List<EmployeeBankAccountDTO> employeeBankAccountDTOList = new ArrayList<>();
            employeeDTO.setEmployeeBankAccountDTOs(employeeBankAccountDTOList);
        }

        employeeDTO.getEmployeeBankAccountDTOs().add(employeeBankAccountDTO);

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityCreate, null);

    }

    //@Test
    public void addEmployeeWithHomeAddress() throws IOException {
        employeeDTO = getTestEmployee();
        addressDTO = getTestAddress();

        employeeDTO.setLiveAddress(addressDTO);


        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityCreate, null);

    }

    //@Test
    public void addEmployeeWithWorkState() throws IOException {
        employeeDTO = getTestEmployee();

        employeeDTO.setWorkState("NY");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityCreate, null);

    }

    //@Test
    public void addEmployeeWithAddressAndBankAccount() throws IOException {
        employeeDTO = getTestEmployee();

        employeeBankAccountDTO = getTestEmployeeBankAccount(employeeDTO);
        if(Objects.isNull(employeeDTO.getEmployeeBankAccountDTOs())){
            List<EmployeeBankAccountDTO> employeeBankAccountDTOList = new ArrayList<>();
            employeeDTO.setEmployeeBankAccountDTOs(employeeBankAccountDTOList);
        }
        employeeDTO.getEmployeeBankAccountDTOs().add(employeeBankAccountDTO);

        addressDTO = getTestAddress();
        employeeDTO.setLiveAddress(addressDTO);

        employeeDTO.setWorkState("NY");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityCreate, null);

    }

    //@Test
    public void updateEmployeeFirstName() throws IOException {
        addEmployee();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        employeeDTO.setFirstName("UPDATEDFirst");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        Set<String> changedAttributes = new HashSet<>();
        changedAttributes.add("FirstName");

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityUpdate, changedAttributes);
    }

    //@Test
    public void updateEmployeeHireDate() throws IOException {
        addEmployee();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        DateDTO hireDate = new DateDTO();
        hireDate.set(2011,01,01);
        employeeDTO.setHireDate(hireDate);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        verifyEmployeeNotPublished(employeeDTO);
    }

    //@Test
    public void addEmployeeBankAccount() throws IOException {
        addEmployee();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        employeeBankAccountDTO = getTestEmployeeBankAccount(employeeDTO);
        if(Objects.isNull(employeeDTO.getEmployeeBankAccountDTOs())){
            List<EmployeeBankAccountDTO> employeeBankAccountDTOList = new ArrayList<>();
            employeeDTO.setEmployeeBankAccountDTOs(employeeBankAccountDTOList);
        }
        employeeDTO.getEmployeeBankAccountDTOs().add(employeeBankAccountDTO);

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        Set<String> changedAttributes = new HashSet<>();
        changedAttributes.add("BankAccount");

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityUpdate, changedAttributes);
    }

    //@Test
    public void UpdateEmployeeBankAccountNumber() throws IOException {
        addEmployeeWithBankAccount();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        employeeBankAccountDTO.getBankAccount().setAccountNumber("567864367");

        if(Objects.isNull(employeeDTO.getEmployeeBankAccountDTOs())){
            List<EmployeeBankAccountDTO> employeeBankAccountDTOList = new ArrayList<>();
            employeeDTO.setEmployeeBankAccountDTOs(employeeBankAccountDTOList);
        }
        employeeDTO.getEmployeeBankAccountDTOs().add(employeeBankAccountDTO);

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> pr = PayrollServices.employeeManager.updateEmployeeBankAccount(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO.getEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        Set<String> changedAttributes = new HashSet<>();
        changedAttributes.add("BankAccount");

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityUpdate, changedAttributes);
    }

    //@Test
    public void UpdateEmployeeBankAccountName() throws IOException {
        addEmployeeWithBankAccount();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        employeeBankAccountDTO.getBankAccount().setBankName("UpdatedName");

        if(Objects.isNull(employeeDTO.getEmployeeBankAccountDTOs())){
            List<EmployeeBankAccountDTO> employeeBankAccountDTOList = new ArrayList<>();
            employeeDTO.setEmployeeBankAccountDTOs(employeeBankAccountDTOList);
        }
        employeeDTO.getEmployeeBankAccountDTOs().add(employeeBankAccountDTO);

        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> pr = PayrollServices.employeeManager.updateEmployeeBankAccount(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO.getEmployeeId(), employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        verifyEmployeeNotPublished(employeeDTO);
    }

    //@Test
    public void addEmployeeHomeAddress() throws IOException {
        addEmployee();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        addressDTO = getTestAddress();
        employeeDTO.setLiveAddress(addressDTO);

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        Set<String> changedAttributes = new HashSet<>();
        changedAttributes.add("HomeAddress");

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityUpdate, changedAttributes);
    }

    //@Test
    public void UpdateEmployeeHomeAddress() throws IOException {
        addEmployeeWithHomeAddress();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        employeeDTO.getLiveAddress().setState("AK");
        employeeDTO.getLiveAddress().setZipCode("84995");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        Set<String> changedAttributes = new HashSet<>();
        changedAttributes.add("HomeAddress");

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityUpdate, changedAttributes);
    }

    //@Test
    public void AddEmployeeWorkState() throws IOException {
        addEmployee();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        employeeDTO.setWorkState("AL");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        Set<String> changedAttributes = new HashSet<>();
        changedAttributes.add("WorkAddress");

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityUpdate, changedAttributes);
    }

    //@Test
    public void UpdateEmployeeWorkState() throws IOException {
        addEmployeeWithWorkState();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        employeeDTO.setWorkState("AK");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        Set<String> changedAttributes = new HashSet<>();
        changedAttributes.add("WorkAddress");

        verifyEmployeePublished(employeeDTO, EventEnumType.EntityUpdate, changedAttributes);
    }

    //@Test
    public void UpdateEmployee() throws IOException {
        addEmployeeWithAddressAndBankAccount();
        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        employeeDTO.setWorkState("MA");

        employeeDTO.getLiveAddress().setState("WA");
        employeeDTO.getLiveAddress().setZipCode("89895");

        employeeDTO.setSocialSecurityNumber("123454321");
        employeeDTO.setGender(Gender.Male);
        DateDTO hireDate = new DateDTO();
        hireDate.set(2011,01,01);
        employeeDTO.setHireDate(hireDate);

        employeeBankAccountDTO.getBankAccount().setAccountNumber("1234321");
        employeeBankAccountDTO.getBankAccount().setBankName("JPMC");

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        Set<String> changedAttributes = new HashSet<>();
        changedAttributes.add("WorkAddress");
        changedAttributes.add("HomeAddress");
        changedAttributes.add("TaxId");
        changedAttributes.add("Gender");
        changedAttributes.add("BankAccount");


        verifyEmployeePublished(employeeDTO, EventEnumType.EntityUpdate, changedAttributes);
    }

    private void verifyEmployeePublished(EmployeeDTO employeeDTO, EventEnumType eventType, Set<String> changedAttributes) throws IOException {
        Company company = Company.findCompany(COMPANY_123456, SourceSystemCode.QBOE);
        Employee employee = Employee.findEmployee(company, employeeDTO.getEmployeeId());
        employeeVerifier.verifyEmployeePublished(employee.getId().toString(), eventType, changedAttributes);
    }

    private void verifyEmployeeNotPublished(EmployeeDTO employeeDTO) {
        Company company = Company.findCompany(COMPANY_123456, SourceSystemCode.QBOE);
        Employee employee = Employee.findEmployee(company, employeeDTO.getEmployeeId());
        employeeVerifier.verifyEmployeeNotPublished(employee.getId().toString());
    }

    private EmployeeDTO getTestEmployee() {
        Random rand = new Random();
        int empSourceId = rand.nextInt(10000);

        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("TESTSLB" + empSourceId);
        employeeDTO.setFirstName("TestFirstName");
        employeeDTO.setLastName("TestLastName");
        employeeDTO.setMiddleName("TMI");
        employeeDTO.setSuffix("Jr.");
        employeeDTO.setSocialSecurityNumber("111223333");
        return employeeDTO;
    }

    private EmployeeBankAccountDTO getTestEmployeeBankAccount(EmployeeDTO employeeDTO) {
        int accountId = 1;
        if(!CollectionUtils.isEmpty(employeeDTO.getEmployeeBankAccountDTOs())){
            accountId += employeeDTO.getEmployeeBankAccountDTOs().size();
        }
        return GenerateData.getEmployeeBankAccountDTO(employeeDTO.getEmployeeId()+"Acc"+accountId);
    }

    private AddressDTO getTestAddress() {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("T303");
        addressDTO.setAddressLine2("5th Street");
        addressDTO.setAddressLine3("1st Cross");
        addressDTO.setCity("NYC");
        addressDTO.setCountry("US");
        addressDTO.setState("NY");
        addressDTO.setZipCode("23984");
        return addressDTO;
    }

}

