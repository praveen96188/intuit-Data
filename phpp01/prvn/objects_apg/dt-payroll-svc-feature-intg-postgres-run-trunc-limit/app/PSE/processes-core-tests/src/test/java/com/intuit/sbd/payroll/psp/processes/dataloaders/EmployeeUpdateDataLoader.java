package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import org.hibernate.FlushMode;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: dcrossley
 * Date: Jul 6, 2009
 * Time: 1:15:46 PM
 */
public class EmployeeUpdateDataLoader {
    private static String COMPANY_123456 = "123456";

    public static void initialize() {
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        EmployeeTestSuiteDataLoader dataloader = new EmployeeTestSuiteDataLoader();
        dataloader.loadFullSuite();
        PayrollServices.commitUnitOfWork();
    }

    public static void before(){
        PayrollServicesTest.beforeEachTest();
    }

    public static void updateEmployeeTaxId(){
        EmployeeDTO employeeDTO;

        employeeDTO = getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setSocialSecurityNumber("223456789");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());
        // verify the employee itself
        validateEmployee(COMPANY_123456, employeeDTO, pr.getResult());
    }

    public static EmployeeDTO getTestEmployee() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("TESTSLB");
        employeeDTO.setFirstName("TestFirstName");
        employeeDTO.setLastName("TestLastName");
        employeeDTO.setMiddleName("TMI");
        employeeDTO.setSocialSecurityNumber("111223333");

        return employeeDTO;
    }

    public static void validateEmployee(String pSourceCompanyId, EmployeeDTO originalEmployee, Employee updatedEmployee) {
        // verify the employee itself
        assertEquals("Employee Source Company Id:", pSourceCompanyId,
                updatedEmployee.getCompany().getSourceCompanyId());
        assertEquals("Employee Source Id:", originalEmployee.getEmployeeId(), updatedEmployee
                .getSourceEmployeeId());
        assertEquals("Employee First Name:", originalEmployee.getFirstName(), updatedEmployee
                .getFirstName());
        assertEquals("Employee Last Name:", originalEmployee.getLastName(), updatedEmployee.getLastName());
        assertEquals("Employee Middle Name:", originalEmployee.getMiddleName(), updatedEmployee
                .getMiddleName());
        assertEquals("Employee Tax ID:", originalEmployee.getSocialSecurityNumber(), updatedEmployee.getTaxId());
    }

    public static void changeEmployeeTaxId() {
        initialize();
        before();
        updateEmployeeTaxId();
    }
}
