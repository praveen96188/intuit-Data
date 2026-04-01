package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * User: ihannur
 * Date: 6/12/13
 * Time: 2:05 PM
 */
public class RemoveConsumerRealmIdTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(2013, 6, 12);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testValidation() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.employeeManager.removeConsumerRealmId(null);
        assertOne(result.getMessages());
        assertEquals("Error Code", "5002", result.getErrorMessages().get(0).getMessageCode());

        result = PayrollServices.employeeManager.removeConsumerRealmId(SpcfUniqueId.generateRandomUniqueIdString());
        assertOne(result.getMessages());
        assertEquals("Error Code", "5003", result.getErrorMessages().get(0).getMessageCode());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHappyPath() {
        List<Employee> employees = DataLoadServices.setupCompany ("12345");

        PayrollServices.beginUnitOfWork();
        for (Employee employee : employees) {
            Application.refresh(employee);
            employee.setConsumerRealmId("consumerId_"+ employee.getSourceEmployeeId());
            Application.save(employee);

        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Employee employee : employees) {
            Application.refresh(employee);
            assertEquals("Consumer Employee Id", "consumerId_" + employee.getSourceEmployeeId(), employee.getConsumerRealmId());
            assertSuccess(PayrollServices.employeeManager.removeConsumerRealmId(employee.getId().toString()));
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (Employee employee : employees) {
            Application.refresh(employee);
            assertNull("Consumer Id", employee.getConsumerRealmId());
        }
        PayrollServices.rollbackUnitOfWork();

    }

}
