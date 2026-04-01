/*
 * $Id: $
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
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTOAssistedValidator;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTEmployeeInfoDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeStatus;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.VmpEmployeeInfo;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeTestSuiteDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo.MessageLevel;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import org.hibernate.FlushMode;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Contains the unit tests for the <CODE>Message</CODE> class.
 *
 * @author: chetzler
 * @version: Jun 20, 2007
 */
public class AddEmployeeCoreTests {
    private static String COMPANY_123456 = "123456";
    private static String COMPANY_654321 = "654321";
    private static String COMPANY_INACTIVE = "123456Inactive";
    private static String COMPANY_HOLD = "123456OnHold";
    private static String COMPANY_PENDING_TERM = "123456PendingTermination";
    private static String COMPANY_TERMINATED = "123456Terminated";

    private EmployeeDTO employeeDTO;
    private static EmployeeTestSuiteDataLoader dataloader;

    @BeforeClass
    public static void initialize() {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
            PayrollServicesTest.truncateTables();
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            dataloader = new EmployeeTestSuiteDataLoader();
            dataloader.loadFullSuite();
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        initialize();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void addEmployeeCoreNewEE() {
        employeeDTO = getTestEmployee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        // Persistence check
        verifyEmployeeDTO(employeeDTO);
    }

    @Test
    public void addEmployeeCoreNewEEHavingValidFirstName() {
        employeeDTO = getTestEmployeeWithOnlyFirstName();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        // Persistence check
        verifyEmployeeDTO(employeeDTO);
    }
    @Test
    public void addEmployeeCoreNewEEHavingValidLastName() {
        employeeDTO = getTestEmployeeWithOnlyLastName();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        // Persistence check
        verifyEmployeeDTO(employeeDTO);
    }
    @Test
    public void addEmployeeCoreNewEEHavingInvalidLastName() {
        employeeDTO = getTestEmployeeWithInvalidLastName();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertFalse("Test Result:", pr.isSuccess());

        // Persistence check
    }

    @Test
    public void addEmployeeCoreNewEEWithNullNames() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("1");
        employeeDTO.setSocialSecurityNumber(null);
        //setting assisted employee validator and passing first and last names as null
        employeeDTO.setValidator(new EmployeeDTOAssistedValidator(null));
        employeeDTO.setFirstName(null);
        employeeDTO.setLastName(null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Employee Name has invalid value", message.getMessage());
    }

    @Test
    public void addEmployeeCoreNewEEWithNullLastName() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("1");
        employeeDTO.setSocialSecurityNumber(null);
        //setting assisted employee validator and passing first and last names as null
        employeeDTO.setValidator(new EmployeeDTOAssistedValidator(null));
        employeeDTO.setFirstName("FirstName");
        employeeDTO.setLastName(null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 0, pr.getMessages().size());
    }

    @Test
    public void addEmployeeCoreExistingInactiveEE() {
        employeeDTO = getTestEmployee();
        employeeDTO.setEmployeeId("TESTINACTV");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(pr);

        // Persistence check
        PayrollServices.beginUnitOfWork();
        verifyEmployeeDTO(employeeDTO);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addEmployeeCoreExistingActiveEE() {
        employeeDTO = getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());
        verifyEmployeeDTO(dataloader.getActiveEmployeeDTO());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "163", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.WARNING, message.getLevel());
        assertEquals("Error Message:", "Employee TESTACTV for company QBOE:" + COMPANY_123456 + " already exists.", message.getMessage());
    }

    @Test
    public void addCompanyNotSpecified() {
        employeeDTO = getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, null, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Source Company ID is not specified.", message.getMessage());
    }

    @Test
    public void addCompanyInactive() {
        
        
        employeeDTO = getTestEmployee();
        DataLoadServices.setPrincipalToQBDT();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_INACTIVE, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 0, pr.getMessages().size());
    }

    @Test
    public void addCompanyOnHold() {
        employeeDTO = getTestEmployee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_HOLD, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "The operation ChangeCompanyInfo is not allowed for company QBOE:123456OnHold in its current state.",
                     message.getMessage());
    }

    @Test
    public void addCompanyPendingTermination() {
        employeeDTO = getTestEmployee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_PENDING_TERM, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "The operation ChangeCompanyInfo is not allowed for company QBOE:123456PendingTermination in its current state.",
                     message.getMessage());
    }

    @Test
    public void addCompanyTerminated() {
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        dataloader.saveTerminatedCompany();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPrincipalToQBDT();

        employeeDTO = getTestEmployee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_TERMINATED, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 0, pr.getMessages().size());
    }

    @Test
    public void addEmployeeTerminated_Agent() {
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        dataloader.saveTerminatedCompany();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPrincipalToAgent();

        employeeDTO = getTestEmployee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_TERMINATED, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 0, pr.getMessages().size());
    }

    @Test
    public void addEmployeeTerminated_Agent_HasPermission() {
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        dataloader.saveTerminatedCompany();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPrincipalToAgent(OperationId.UpdateCancelTermCompany);

        employeeDTO = getTestEmployee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_TERMINATED, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(pr);
    }

    @Test
    public void addCompanyNotExists() {
        employeeDTO = getTestEmployee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, "IDONTEXIST", employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Company QBOE:IDONTEXIST does not exist.", message.getMessage());
    }

    @Test
    public void addEmployeeNotSpecified() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "101", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Employee is not specified.", message.getMessage());
    }

    @Test
    public void addEmployeeEventCheck() {
        employeeDTO = getTestEmployee();
        Company company=null;
        PayrollServices.beginUnitOfWork();
        company=Company.findCompany(COMPANY_123456, SourceSystemCode.QBOE);
        company.setSourceSystemCd(SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());
        verifyEmployeeDTO(employeeDTO, SourceSystemCode.QBDT);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeAdded);
        //This accounts for the two other employees created in the BeforeClass
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event detail mismatch", Employee.findEmployee(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), employeeDTO.getEmployeeId()).getId().toString(), companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addEmployeeInviteSent() {
        String listId = "82898349-8928392";
        String cfr = "0504959";
        String recnum = "82898349";
        employeeDTO = getEmployeeDTOWithListId(listId);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, Boolean.TRUE, ServiceCode.ViewMyPaycheck);
        PayrollServices.beginUnitOfWork();
        VmpEmployeeInfo vmpEmployeeInfo = new VmpEmployeeInfo();
        vmpEmployeeInfo.setConsumerRealmId(cfr);
        vmpEmployeeInfo.setEmployeeRecnum(recnum);
        vmpEmployeeInfo.setCompany(company);
        Application.save(vmpEmployeeInfo);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT, company.getSourceCompanyId(), employeeDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<VmpEmployeeInfo> vmpEmployeesInfo = VmpEmployeeInfo.findVmpEmployeesInfo(company);
        //Verify the VmpEmployeeInfo entry is deleted after employee creation
        assertEquals("Count of VmpEmployeeInfos is not zero", 0, vmpEmployeesInfo.size());

        //Verify the employee added has the same employee recnum
        List<Employee> employeeWithRecnum = company.getEmployees().stream().filter(employee -> employee.getQbdtEmployeeInfo().getListId().startsWith(recnum)).collect(Collectors.toList());
        assertEquals("Employee with given recnum not found", 1, employeeWithRecnum.size());
        assertNotNull(employeeWithRecnum.get(0));

        //Verify the ConsumerRealmId is updated on the employee
        Employee employeeByQBListId = Employee.findEmployeeByQBListId(company, listId);
        assertNotNull(employeeByQBListId.getConsumerRealmId());
        assertEquals("ConsumerRealmId updated incorrectly", cfr, employeeByQBListId.getConsumerRealmId());
        PayrollServices.rollbackUnitOfWork();
    }

    private EmployeeDTO getEmployeeDTOWithListId(String listId){
        EmployeeDTO employeeDTO = getTestEmployee();
        QBDTEmployeeInfoDTO qbdtEmployeeInfoDTO = new QBDTEmployeeInfoDTO();
        qbdtEmployeeInfoDTO.setListId(listId);
        employeeDTO.setQBDTEmployeeInfoDTO(qbdtEmployeeInfoDTO);
        return employeeDTO;
    }

    
    private void verifyEmployeeDTO(EmployeeDTO dto){
        verifyEmployeeDTO(dto, SourceSystemCode.QBOE);
    }
    
    private void verifyEmployeeDTO(EmployeeDTO dto, SourceSystemCode pSourceSystemCode) {
        Company company = Company.findCompany(COMPANY_123456, pSourceSystemCode);
        Employee employee = Employee.findEmployee(company, dto.getEmployeeId());

        assertEquals("Employee First Name:", dto.getFirstName(), employee.getFirstName());
        assertEquals("Employee Last Name:", dto.getLastName(), employee.getLastName());
        assertEquals("Employee Middle Name:", dto.getMiddleName(), employee.getMiddleName());
        assertEquals("Employee Source Id:", dto.getEmployeeId(), employee.getSourceEmployeeId());
        assertEquals("Employee SSN:", dto.getSocialSecurityNumber(), employee.getTaxId());
        assertEquals("Employee Status:", EmployeeStatus.Active, employee.getStatusCd());
    }

    private EmployeeDTO getTestEmployee() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("TESTSLB101");
        employeeDTO.setFirstName("TestFirstName");
        employeeDTO.setLastName("TestLastName");
        employeeDTO.setMiddleName("TMI");
        employeeDTO.setSuffix("Jr.");
        employeeDTO.setSocialSecurityNumber("111223333");

        return employeeDTO;
    }

    private EmployeeDTO getTestEmployeeWithOnlyFirstName() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("TESTSLB1");
        employeeDTO.setFirstName("EmployeeWithOnlyFirstName");
        //employeeDTO.setLastName("TestLastName");
        employeeDTO.setMiddleName("TMI");
        employeeDTO.setSuffix("Jr.");
        employeeDTO.setSocialSecurityNumber("111223333");

        return employeeDTO;
    }


    private EmployeeDTO getTestEmployeeWithOnlyLastName() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("TESTSLB2");
        //employeeDTO.setFirstName("TestFirstName");
        employeeDTO.setLastName("EmployeeWithOnlyLastName");
        employeeDTO.setMiddleName("TMI");
        employeeDTO.setSuffix("Jr.");
        employeeDTO.setSocialSecurityNumber("111223333");

        return employeeDTO;
    }
    private EmployeeDTO getTestEmployeeWithInvalidLastName() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("TESTSLB");
        //employeeDTO.setFirstName("TestFirstName");
        employeeDTO.setLastName(" ");
        employeeDTO.setMiddleName("TMI");
        employeeDTO.setSuffix("Jr.");
        employeeDTO.setSocialSecurityNumber("111223333");

        return employeeDTO;
    }
}
