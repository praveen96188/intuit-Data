package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.processes.util.WorkersCompTestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: michaelp696
 *
 * Additional tests beyond the basic successful service add in AddServiceCoreTests
 */
public class AddServiceWorkersCompTests {
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
     * Test that when adding workers comp service paychecks on or after the service start date are successfully created
     * as WorkersCompPaycheckPending
     */
    @Test
    public void testAddWorkersCompServiceAssisted() {
        String psid = "123456789";
        Company assistedCompany = DataLoadServices.setupAssistedCompanyForCA(psid, 2, true);
        Assert.assertNotNull(assistedCompany);
        DateDTO serviceStartDate = new DateDTO("2012-12-01");
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

        WorkersCompTestUtil.addWorkersCompServiceToCompany(assistedCompany, serviceStartDate);
        assistedCompany = DataLoadServices.refreshCompany(assistedCompany);
        CompanyService workersCompService = assistedCompany.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());

        //Check that paychecks from the payroll run on service start date and after are in the workers comp paycheck queue
        Collection<PaycheckDTO> expectedPaychecks = new ArrayList<PaycheckDTO>();
        expectedPaychecks.addAll(onServiceStartDatePayrollRun.getPaychecks());
        expectedPaychecks.addAll(afterServiceStartDatePayrollRun.getPaychecks());
        WorkersCompTestUtil.assertWorkersCompPaycheck(expectedPaychecks,WorkersCompPaycheckStateCode.PendingNew);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingState(expectedPaychecks, WorkersCompPaycheckStateCode.PendingNew);
    }

    /*
     * Test to ensure only DIY customers can add workers comp service
     */
    @Test
    public void testAddWorkersCompServiceNotAssisted() {
        DateDTO serviceStartDate = new DateDTO("2012-12-01");
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT);
        Assert.assertNotNull(company);
        ProcessResult<CompanyService> processResult = WorkersCompTestUtil.addWorkersCompServiceToCompany(company, serviceStartDate);
        Assert.assertTrue(processResult.isSuccess());
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        // Workers comp service should have been added successfully
        Assert.assertNotNull(workersCompService);
    }

    /*
     * Test that voided paychecks are not pulled into the initial WorkersComp paychecks
     */
    @Test
    public void testSendPayrollAndVoidBeoreAddingWorkersCompService() {
        String psid = "123456789";
        Company assistedCompany = DataLoadServices.setupAssistedCompanyForCA(psid, 2, true);
        Assert.assertNotNull(assistedCompany);
        DateDTO serviceStartDate = new DateDTO("2012-12-01");
        DateDTO payrollRunDate = new DateDTO("2012-12-02");
        //Add payroll after service start date
        DateDTO payrollDate = new DateDTO("2012-12-02");
        PayrollRunDTO afterServiceStartDatePayrollRun = DataLoadServices.runPayrollForCa(psid, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);
        //Offload before voiding
        DataLoadServices.runOffload(assistedCompany, 2012, 12, 3);
        WorkersCompTestUtil.voidFirstPayroll(assistedCompany);
        WorkersCompTestUtil.addWorkersCompServiceToCompany(assistedCompany, serviceStartDate);
        //The only payroll run has been voided before adding workers comp service, so no paychecks should be added
        WorkersCompTestUtil.assertWorkersCompPaycheckEmpty();
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingStateEmpty();
    }

}
