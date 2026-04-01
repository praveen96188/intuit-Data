package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeTestSuiteDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 1, 2009
 * Time: 2:55:01 PM
 */
public class ReactivateEmployeeCoreTests {
    EmployeeTestSuiteDataLoader loader = new EmployeeTestSuiteDataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2009, 5, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    private EmployeeDTO getEmployeeDTO() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("Employee1");
        employeeDTO.setFirstName("EmpFirstName");
        employeeDTO.setLastName("EmpLastName");
        employeeDTO.setMiddleName("EmpMI");
        employeeDTO.setSuffix("Jr.");
        employeeDTO.setSocialSecurityNumber("556677889");

        employeeDTO.setWorkState("Nevada");
        DateDTO date = new DateDTO();
        date.set(2008, Calendar.AUGUST, 2);
        employeeDTO.setHireDate(date);
        employeeDTO.setFedFilingStatus("Married");
        employeeDTO.setFedAllowances(3);

        // todo fix these
        /*employeeDTO.setStateFilingStatus("Married");
        employeeDTO.setStateAllowances(3);*/

        employeeDTO.setHasRetirementPlan(true);
        employeeDTO.setHasThirdPartySickPay(true);
        employeeDTO.setStatutory(true);

        AddressDTO liveAddr = new AddressDTO();
        liveAddr.setAddressLine1("123 High Country Rd");
        liveAddr.setCity("Reno");
        liveAddr.setState("NV");
        liveAddr.setZipCode("89502");
        liveAddr.setCountry("USA");
        employeeDTO.setLiveAddress(liveAddr);

        return employeeDTO;
    }

    /**
     * Test message 5001 - ReHireDate not Specified
     */
    @Test
    public void testReHireDateNotSpecified() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.ThirdParty401k);

        EmployeeDTO employeeDTO = getEmployeeDTO();

        //Add Employee
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(company.getSourceSystemCd(), company.getSourceCompanyId(), employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        Employee employee = Employee.findEmployee(company, employeeDTO.getEmployeeId());
        ProcessResult processResult = PayrollServices.employeeManager.reactivateEmployee(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(),null);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "ReHire Date has invalid value",
                errorMessage.getMessage());
    }


    @Test
    public void testHappyPath(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeAdded);
        assertEquals("Company Events", 1, companyEventsList.size()); //To account for Employees created in DataLoadServices.newCompany
        PayrollServices.commitUnitOfWork();

        EmployeeDTO employeeDTO = getEmployeeDTO();

        //Add Employee
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.addEmployee(company.getSourceSystemCd(), company.getSourceCompanyId(), employeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        DateDTO terminationDate = new DateDTO();
        terminationDate.set(2009, Calendar.JANUARY, 4);

        //Deactivate Employee
        PayrollServices.beginUnitOfWork();
        Employee employee = Employee.findEmployee(company, employeeDTO.getEmployeeId());
        pr = PayrollServices.employeeManager.deactivateEmployee(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(),terminationDate);
        PayrollServices.commitUnitOfWork();
        assertSuccess("deactivateEmployee", pr);

        //Validate Employee
        PayrollServices.beginUnitOfWork();
        Employee ee = PayrollServices.entityFinder.findById(Employee.class, pr.getResult().getId());
        validateEmployee(ee);
        PayrollServices.commitUnitOfWork();

        //Reactivate Employee
        PayrollServices.beginUnitOfWork();
        DateDTO reHireDate = new DateDTO();
        reHireDate.set(2009, Calendar.MAY, 28);

        employee = Employee.findEmployee(company, employeeDTO.getEmployeeId());
        pr = PayrollServices.employeeManager.reactivateEmployee(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(),reHireDate);
        PayrollServices.commitUnitOfWork();
        assertSuccess("reactivateEmployee", pr);

        //Persistency Check
        PayrollServices.beginUnitOfWork();
        employee = Employee.findEmployee(company, employeeDTO.getEmployeeId());
        PayrollServices.commitUnitOfWork();
        assertEquals("Employee Status Cd:", EmployeeStatus.Active, employee.getStatusCd());
        assertEquals("Employee ReHire Date:", DateDTO.convertToSpcfCalendar(reHireDate), employee.getReHireDate().toLocal());
        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeAdded);
        assertEquals("Company Events", 3, companyEventsList.size());
        assertEquals("Event Detail Code", 1,
                companyEventsList.get(2).getCompanyEventDetails(EventDetailTypeCode.EmployeeId).size());
        assertEquals("Event Detail Value", employee.getId().toString(), companyEventsList.get(2).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeDeleted);
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Detail Code", 1,
                companyEventsList.get(0).getCompanyEventDetails(EventDetailTypeCode.EmployeeId).size());
        assertEquals("Event Detail Value", employee.getId().toString(), companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
        PayrollServices.commitUnitOfWork();
    }

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
}
