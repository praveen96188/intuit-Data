package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.util.WorkersCompTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * User: michaelp696
 *
 * Tests usage of WorkersCompPaycheck cancelOrDeleteWorkersCompPaycheck and markAsSent, with the core processes that use them.
 */
public class CancelOrDeletePayrollWorkersCompTests {
    private static final String PSID = "123456789";

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
     * In this test we test voiding an offloaded payroll that has not been sent to the workers comp service
     */
    @Test
    public void testVoidPayrollNotSentToWorkersComp() {
        Company assistedCompany = WorkersCompTestUtil.createAssistedCompanyWithWorkersCompService(PSID);

        DateDTO payrollRunDate = new DateDTO("2012-12-02");
        //Add payroll on service start date
        DateDTO payrollDate = new DateDTO("2012-12-01");
        PayrollRunDTO onServiceStartDatePayrollRun = DataLoadServices.runPayrollForCa(PSID, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);
        //Check that added payroll is in pending new state
        WorkersCompTestUtil.assertWorkersCompPaycheck(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingState(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
        //Offload before voiding
        DataLoadServices.runOffload(assistedCompany, 2012, 12, 3);

        WorkersCompTestUtil.voidFirstPayroll(assistedCompany);
        //The paychecks should now be cancelled as they have not yet been sent to the workers comp service
        WorkersCompTestUtil.assertWorkersCompPaycheck(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.Cancelled);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingStateEmpty();
    }

    /*
    * In this test we test voiding an offloaded payroll that has been sent to the workers comp service
    */
    @Test
    public void testVoidPayrollSentToWorkersComp() {
        Company assistedCompany = WorkersCompTestUtil.createAssistedCompanyWithWorkersCompService(PSID);

        DateDTO payrollRunDate = new DateDTO("2012-12-02");
        //Add payroll on service start date
        DateDTO payrollDate = new DateDTO("2012-12-01");
        PayrollRunDTO onServiceStartDatePayrollRun = DataLoadServices.runPayrollForCa(PSID, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);
        //Check that added payroll is in pending new state
        WorkersCompTestUtil.assertWorkersCompPaycheck(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingState(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
        //Offload before voiding
        DataLoadServices.runOffload(assistedCompany, 2012, 12, 3);

        //Simulate paychecks being sent
        WorkersCompTestUtil.markWorkersCompPaychecksSent(assistedCompany, onServiceStartDatePayrollRun);
        WorkersCompTestUtil.voidFirstPayroll(assistedCompany);
        //The paychecks should now be pending delete
        WorkersCompTestUtil.assertWorkersCompPaycheck(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingDelete);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingState(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingDelete);
    }

    /*
     * This test ensures that voiding a paycheck before workers comp service started does not create any workers comp checks
     */
    @Test
    public void testVoidPayrollWithoutWorkersCompPaychecks() {
        Company assistedCompany = WorkersCompTestUtil.createAssistedCompanyWithWorkersCompService(PSID);

        DateDTO payrollRunDate = new DateDTO("2012-12-02");
        //Add payroll on service start date
        DateDTO payrollDate = new DateDTO("2012-11-30");
        PayrollRunDTO beforeServiceStartDatePayrollRun = DataLoadServices.runPayrollForCa(PSID, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);
        //Check that added payroll does not exist
        WorkersCompTestUtil.assertWorkersCompPaycheckEmpty();
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingStateEmpty();
        //Offload before voiding
        DataLoadServices.runOffload(assistedCompany, 2012, 12, 3);
        WorkersCompTestUtil.voidFirstPayroll(assistedCompany);
        //No paychecks should have been created
        WorkersCompTestUtil.assertWorkersCompPaycheckEmpty();
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingStateEmpty();
    }

    /*
     * Test cancels a paycheck before offload and before it is sent to the workers comp service
     */
    @Test
    public void testCancelEETransactionNotSentToWorkersComp() {
        Company assistedCompany = WorkersCompTestUtil.createAssistedCompanyWithWorkersCompService(PSID);

        DateDTO payrollRunDate = new DateDTO("2012-12-02");
        //Add payroll on service start date
        DateDTO payrollDate = new DateDTO("2012-12-01");
        PayrollRunDTO onServiceStartDatePayrollRun = DataLoadServices.runPayrollForCa(PSID, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);
        //Check that added payroll is in pending new state
        WorkersCompTestUtil.assertWorkersCompPaycheck(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingState(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
        //Cancel paychecks that have not been sent
        WorkersCompTestUtil.cancelEmployeeTransactionFirstPayroll(assistedCompany);
        //Checks were never sent so they should be in cancelled state
        WorkersCompTestUtil.assertWorkersCompPaycheck(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.Cancelled);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingStateEmpty();
    }

    @Test
    public void testCancelEETransactionSentToWorkersComp() {
        Company assistedCompany = WorkersCompTestUtil.createAssistedCompanyWithWorkersCompService(PSID);

        DateDTO payrollRunDate = new DateDTO("2012-12-02");
        //Add payroll on service start date
        DateDTO payrollDate = new DateDTO("2012-12-01");
        PayrollRunDTO onServiceStartDatePayrollRun = DataLoadServices.runPayrollForCa(PSID, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);
        //Check that added payroll is in pending new state
        WorkersCompTestUtil.assertWorkersCompPaycheck(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingState(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingNew);
        //Simulate paychecks being sent
        WorkersCompTestUtil.markWorkersCompPaychecksSent(assistedCompany, onServiceStartDatePayrollRun);
        //Cancel paychecks that have been sent
        WorkersCompTestUtil.cancelEmployeeTransactionFirstPayroll(assistedCompany);
        //Checks were sent so they should be in pending delete
        WorkersCompTestUtil.assertWorkersCompPaycheck(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingDelete);
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingState(onServiceStartDatePayrollRun.getPaychecks(), WorkersCompPaycheckStateCode.PendingDelete);
    }

    /*
    * This test ensures that cancelling transactions before workers comp service started does not create any workers comp checks
    */
    @Test
    public void testCancelEETransactionsWithoutWorkersCompPaychecks() {
        Company assistedCompany = WorkersCompTestUtil.createAssistedCompanyWithWorkersCompService(PSID);

        DateDTO payrollRunDate = new DateDTO("2012-12-02");
        //Add payroll on service start date
        DateDTO payrollDate = new DateDTO("2012-11-30");
        PayrollRunDTO beforeServiceStartDatePayrollRun = DataLoadServices.runPayrollForCa(PSID, new ArrayList<Employee>(assistedCompany.getEmployees()), payrollRunDate, payrollDate);
        //Check that added payroll does not exist
        WorkersCompTestUtil.assertWorkersCompPaycheckEmpty();
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingStateEmpty();
        //Offload before voiding
        DataLoadServices.runOffload(assistedCompany, 2012, 12, 3);
        WorkersCompTestUtil.cancelEmployeeTransactionFirstPayroll(assistedCompany);
        //No paychecks should have been created
        WorkersCompTestUtil.assertWorkersCompPaycheckEmpty();
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingStateEmpty();
    }

}
