package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheckStateCode;
import com.intuit.sbd.payroll.psp.processes.util.WorkersCompTestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: michaelp696
 */
public class PayrollSubmitWorkersCompTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /*
     * Test where a company currently active on workers comp service submits a payroll before service start date,
     * on service start date, and after service start date.
     */
    @Test
    public void testPayrollSubmitActiveWorkersComp() {
        String psid = "123456789";
        Company assistedCompany = DataLoadServices.setupAssistedCompanyForCA(psid, 2, true);
        Assert.assertNotNull(assistedCompany);
        DateDTO serviceStartDate = new DateDTO("2012-12-01");
        CompanyService workersCompService = WorkersCompTestUtil.addWorkersCompServiceToCompany(assistedCompany, serviceStartDate).getResult();
        Assert.assertNotNull(workersCompService);

        DateDTO payrollRunDate = new DateDTO("2012-12-02");
        //Add payroll before service start date
        DateDTO payrollDate = new DateDTO("2012-11-30");
        DataLoadServices.runPayrollForCa(psid, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);
        //Add payroll on service start date
        payrollDate = new DateDTO("2012-12-01");
        PayrollRunDTO onServiceStartDatePayrollRun = DataLoadServices.runPayrollForCa(psid, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);
        //Add payroll after service start date
        payrollDate = new DateDTO("2012-12-02");
        PayrollRunDTO afterServiceStartDatePayrollRun = DataLoadServices.runPayrollForCa(psid, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);

        //Check that paychecks from the payroll run on service start date and after are in the workers comp paycheck queue
        Collection<PaycheckDTO> expectedPaychecks = new ArrayList<PaycheckDTO>();
        expectedPaychecks.addAll(onServiceStartDatePayrollRun.getPaychecks());
        expectedPaychecks.addAll(afterServiceStartDatePayrollRun.getPaychecks());
        WorkersCompTestUtil.assertWorkersCompPaycheck(expectedPaychecks, WorkersCompPaycheckStateCode.PendingNew);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingState(expectedPaychecks, WorkersCompPaycheckStateCode.PendingNew);
    }

    /*
     * Test where a company submits one payroll while active on workers comp service, deactivates service and then submits another payroll
     */
    @Test
    public void testPayrollSubmitDeactivateWorkersComp() {
        String psid = "123456789";
        Company assistedCompany = DataLoadServices.setupAssistedCompanyForCA(psid, 2, true);
        Assert.assertNotNull(assistedCompany);
        DateDTO serviceStartDate = new DateDTO("2012-12-01");
        CompanyService workersCompService = WorkersCompTestUtil.addWorkersCompServiceToCompany(assistedCompany, serviceStartDate).getResult();
        Assert.assertNotNull(workersCompService);

        DateDTO payrollRunDate = new DateDTO("2012-12-02");
        //Run payroll with active service
        DateDTO payrollDate = new DateDTO("2012-12-02");
        PayrollRunDTO activeWorkersCompServicePayrollRun = DataLoadServices.runPayrollForCa(psid, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);

        WorkersCompTestUtil.removeWorkersCompService(assistedCompany);

        //Run payroll with cancelled service
        payrollDate = new DateDTO("2012-12-02");
        DataLoadServices.runPayrollForCa(psid, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);

        //Only the paychecks from the active run should exist
        WorkersCompTestUtil.assertWorkersCompPaycheck(activeWorkersCompServicePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingState(activeWorkersCompServicePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
    }
}
