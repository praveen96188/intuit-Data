package com.intuit.sbd.payroll.psp.batchjobs.workforce;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.*;

import java.util.List;


public class BulkWorkforceInviteProcessorTests {

    private String psid = "987654321";
    private Company company;

    private List<Employee> employeeList;

    //we will modify paychecks of this employee to have dd_employee_fk
    private Employee ddEmp;
    //paychecks for this employee won't have dd_employee_fk
    private Employee nonDDEmp;
    //we will create ER 'EmployeeInvited' event for this employee
    private Employee erInvitedEmp;

    private final String invitationSourceForERInvite = "PayrollAPI";
    private final String invitationSourceForBulkInvite = "BulkWorkforceInviteProcessor";


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    /**
     * Testing the fresh invites mode for BulkWorkforceInviteProcessor
     * We fill DB with company, employee and paycheck data
     *
     * BatchJob #1 - We run batch job with isDDQuery set to true
     * In this run, we should send invite only to ddEmp (verified by non-null persona id) and no invite for nonDDEmp
     *
     * BatchJob#2 - We run batch job with isDDQuery set to false
     * In this run, we should send invite only to nonDDEmp, ddEmp shouldn't receive a new invite as it has already received a fresh invite
     * (ddEmp should have same persona id as after #1 while nonDDEmp should now have a non-null persona id)
     * @throws Exception
     */
    @Test
    public void testFreshInvites() throws Exception {
        //create paychecks such that paycheck created date and settlement date are within range
        createDataBeforeInvites("20220111000000");

        //Assert that both employees don't have personaId initially
        refreshEmployeesFromDB();
        Assert.assertNull(ddEmp.getPersonaId());
        Assert.assertNull(nonDDEmp.getPersonaId());

        /*
        Test 1- run batch job with isDDQuery = true
        Expectations - we should send invite to only ddEmp
         */
        setPSPTime("20220114000000");
        String[] args = {"run", "BulkWorkforceInviteProcessor", "-companyIds", company.getId().toString(), "-isDDQuery","true",
                "-settlementDateDuration","10","-lastPaidDurationEmployee","10",
                "-invitationModeEmailTemplates","FreshInvites:templateName"};
        //Run batch job with isDDQuery set to true
        BatchJobManager.executeCommand(args);

        //invite should be sent for employee with DD paychecks
        refreshEmployeesFromDB();
        String ddEmpPersonaAfterInvite = ddEmp.getPersonaId();
        Assert.assertNotNull(ddEmp.getPersonaId());
        Assert.assertNull(nonDDEmp.getPersonaId());

        /*
        Test 2- run batch job with isDDQuery = false
        Expectation - again we should send only 1 invite - to nonDDEmp
        note that since ddEmp is now already invited, it should not be invited again
         */
        String[] args2 = {"run", "BulkWorkforceInviteProcessor", "-companyIds", company.getId().toString(), "-isDDQuery","false",
                "-settlementDateDuration","10","-lastPaidDurationEmployee","10",
                "-invitationModeEmailTemplates","FreshInvites:templateName"};
        BatchJobManager.executeCommand(args2);

        //invite should be sent only to nonDDEmp, ddEmp should have same persona as after first batch job run
        refreshEmployeesFromDB();
        //ddEmp should still have the same persona
        Assert.assertEquals(ddEmpPersonaAfterInvite, ddEmp.getPersonaId());
        Assert.assertNotNull(nonDDEmp.getPersonaId());

        //check if company flag is being set correctly
        refreshCompanyFromDB();
        String companyPublishStatus = company.getPublishStatus();
        String bulkInviteBit = companyPublishStatus.substring(6,7);
        Assert.assertEquals("2",bulkInviteBit);
    }


    /**
     * Testing the reEngageER mode for BulkWorkforceInviteProcessor
     * We fill DB with company, employee and paycheck data\n
     * We create ER invite event for erInvitedEmp and Bulk invite for ddEmp,
     * these events are created with time stamp at least 30 days earlier than time of batch job
     *
     * Note that since we are only creating events in the table without sending invites, all employees initially have null personaID
     *
     * BatchJob #1 - We run batch job with isDDQuery set to true and in ReEngageER invitation mode only
     * We should send invite only to erInvitedEmp, no invite for ddEmp
     * company flag should be set to REPUBLISH_DONE (4)
     *
     * @throws Exception
     */
    @Test
    public void testReEngageERInvites() throws Exception {

        //create paychecks such that paycheck created date and settlement date are within range
        createDataBeforeInvites("20220111000000");
        //create ER invited event for employee more than 30 days in past
        createERInviteForEmployee(erInvitedEmp, "20211101000000");
        //create bulk invited event for DD employee, this employee SHOULD NOT be invited if we run in "ReEngageER mode"
        createBulkInviteEventForEmp(ddEmp,"20211101000000");

        setPSPTime("20220114000000");
        //check the events
        String[] args = {"run", "BulkWorkforceInviteProcessor", "-companyIds", company.getId().toString(), "-isDDQuery","false",
                "-settlementDateDuration","10","-lastPaidDurationEmployee","10",
                "-invitationModeEmailTemplates","ReEngageERInvited:templateName"};
        //Run batch job
        BatchJobManager.executeCommand(args);

        refreshEmployeesFromDB();
        Assert.assertNotNull(erInvitedEmp.getPersonaId());
        Assert.assertNull(ddEmp.getPersonaId());

        //check that company flag is being set to fresh_invites done
        refreshCompanyFromDB();
        String companyPublishStatus = company.getPublishStatus();
        String bulkInviteBit = companyPublishStatus.substring(6,7);
        Assert.assertEquals("4",bulkInviteBit);

    }

    /**
     * Testing the reEngageAuto mode for BulkWorkforceInviteProcessor
     * We fill DB with company, employee and paycheck data\n
     * We create Bulk invite events for ddEmp,nonDDEmp
     * these events are created with time stamp at least 30 days earlier than time of batch job
     *
     * Note that since we are only creating events in the table without sending invites, all employees initially have null personaID
     *
     * BatchJob #1 - We run batch job with isDDQuery set to true and in ReEnageAuto invitation mode only
     * We should send invite only to ddEmp, no invite for nonDDEmp (since isDDQuery is true)
     *
     * BatchJob#2 - run batch job again but with isDD=false, but within 30 days (our cooling period between two bulk invites)
     * This time invite for nonDDEmp, no invite for DDEmp(as within cooling period)
     *
     *
     * company flag should be set to REPUBLISH_DONE (4)
     *
     * @throws Exception
     */
    @Test
    public void testReEngageBulkInvites() throws Exception {
        //create paychecks such that paycheck created date and settlement date are within range
        createDataBeforeInvites("20220111000000");

        //create Bulk invited event for DD employee more than 30 days in past
        createBulkInviteEventForEmp(ddEmp, "20211101000000");
        //create Bulk invited event for nonDD employee more than 30 days in past
        createBulkInviteEventForEmp(nonDDEmp,"20211101000000");

        /*
        Run the batch job with isDD set to true
        expectation - invite only to ddEmp
        Note that since we have manually created the events without actual invites,
        thus personaId SHOULD BE NULL in DB initially for both
         */
        setPSPTime("20220114000000");
        String[] args = {"run", "BulkWorkforceInviteProcessor", "-companyIds", company.getId().toString(), "-isDDQuery","true",
                "-settlementDateDuration","10","lastPaidDurationEmployee","10",
                "-invitationModeEmailTemplates","ReEngageAutoInvited:templateName"};
        BatchJobManager.executeCommand(args);

        refreshEmployeesFromDB();
        Assert.assertNotNull(ddEmp.getPersonaId());
        String ddEmpPersonaAfterFirstRun = ddEmp.getPersonaId();
        Assert.assertNull(nonDDEmp.getPersonaId());

        /*
        Run the batch job with isDD set to false
        expectation - invite only to nonDDEmp
         */
        setPSPTime("20220114000000");
        String[] args2 = {"run", "BulkWorkforceInviteProcessor", "-companyIds", company.getId().toString(), "-isDDQuery","false",
                "-settlementDateDuration","10","lastPaidDurationEmployee","10"};
        BatchJobManager.executeCommand(args2);

        refreshEmployeesFromDB();
        Assert.assertEquals(ddEmpPersonaAfterFirstRun,ddEmp.getPersonaId());
        Assert.assertNotNull(nonDDEmp.getPersonaId());

        //check that company flag is being set to fresh_invites done
        refreshCompanyFromDB();
        String companyPublishStatus = company.getPublishStatus();
        String bulkInviteBit = companyPublishStatus.substring(6,7);
        Assert.assertEquals("4",bulkInviteBit);

    }

    /**
     * Test to check if ERROR flag is correctly set in DB
     * We set invalid email to ddEmp after creating data
     *
     * invite should be attempted for ddEmp but fail,
     * thus ddEmp should have null persona
     *
     * company publish status should be ERROR (3)
     * @throws Exception
     */
    @Test
    public void testCompanyMarkedAsError() throws Exception {

        //creating paychecks so that we get valid employees
        createDataBeforeInvites("20220111000000");

        //we will set an invalid email for DD employee
        Application.beginUnitOfWork();
        ddEmp = Application.findById(Employee.class,ddEmp.getId());
        ddEmp.setEmail("rxysfae");
        Application.save(ddEmp);
        Application.commitUnitOfWork();

        //Assert that employee doesn't have personaId initially
        refreshEmployeesFromDB();
        Assert.assertNull(ddEmp.getPersonaId());

        /*
        Test 1- run batch job with isDDQuery = true
        Expectations - no invite sent to ddEmp due to invalid email, company status should be set to Error (3)
         */
        setPSPTime("20220114000000");
        String[] args = {"run", "BulkWorkforceInviteProcessor", "-companyIds", company.getId().toString(), "-isDDQuery","true",
                "-settlementDateDuration","10","-lastPaidDurationEmployee","10",
                "-invitationModeEmailTemplates","FreshInvites:templateName"};
        BatchJobManager.executeCommand(args);

        //invite should be not be sent
        refreshEmployeesFromDB();
        Assert.assertNull(ddEmp.getPersonaId());

        //check that company flag is being set to ERROR
        refreshCompanyFromDB();
        String companyPublishStatus = company.getPublishStatus();
        String bulkInviteBit = companyPublishStatus.substring(6,7);
        Assert.assertEquals("3",bulkInviteBit);

    }

    private void addRealmIdToCompany() {
        Application.beginUnitOfWork();
        //refresh the company object
        company = Application.findById(Company.class, company.getId());
        company.setIAMRealmId("9130354965488516");
        Application.save(company);
        Application.commitUnitOfWork();

    }


    private void createERInviteForEmployee(Employee employee, String time) {
        setPSPTime(time);
        PayrollServices.beginUnitOfWork();
        CompanyEvent.createEmployeeInvitedEvent(company,employee.getId().toString(),invitationSourceForERInvite,"emailTemplate","invitationID","personaID");
        PayrollServices.commitUnitOfWork();
    }



    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @AfterClass
    public static void afterClass() {
        PayrollServicesTest.truncateTables();
    }

    private void createDataBeforeInvites(String time) {
        setPSPTime(time);

        //create company with VMP and Assisted service
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax,ServiceCode.ViewMyPaycheck);
        addRealmIdToCompany();

        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        //required to create these payrolls
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};
        DataLoadServices.addFederalTaxCompanyLaws(company);

        //adding three employees
        employeeList = DataLoadServices.addEEs(company, 3, false, false);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        ddEmp=employeeList.get(0);
        nonDDEmp=employeeList.get(1);
        erInvitedEmp=employeeList.get(2);


        //run payroll for the company(with these 3 employees)
        {
            PayrollServices.beginUnitOfWork();
            SpcfCalendar checkDate = PSPDate.getPSPTime();
            PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-11"), employeeList, lawIds, amounts);
            PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
            PayrollServices.commitUnitOfWork();
        }

        //Add dd_employee_fk explicitly to paychecks for ddEmp
        Application.beginUnitOfWork();
        //add DD_EMPLOYEE_FK for one of the employees
        DomainEntitySet<Paycheck> paychecks = Paycheck.findPaychecksBySourceEmployee(company,ddEmp);
        for(Paycheck paycheck: paychecks) {
            paycheck.setDDEmployee(ddEmp);
            Application.save(paycheck);
        }
        Application.commitUnitOfWork();
        //commit work here if required
    }

    private void createBulkInviteEventForEmp(Employee employee, String time) {
        setPSPTime(time);
        PayrollServices.beginUnitOfWork();
        CompanyEvent.createEmployeeInvitedEvent(company,employee.getId().toString(),invitationSourceForBulkInvite,"emailTemplate","invitationID","personaID");
        PayrollServices.commitUnitOfWork();
    }

    private void refreshEmployeesFromDB() {
        ddEmp = Application.findById(Employee.class,ddEmp.getId());
        nonDDEmp = Application.findById(Employee.class,nonDDEmp.getId());
        erInvitedEmp = Application.findById(Employee.class,erInvitedEmp.getId());
    }

    private void refreshCompanyFromDB() {
        company = Application.findById(Company.class,company.getId());
    }

    private void setPSPTime(String pspTIme) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pspTIme);
        PayrollServices.commitUnitOfWork();
    }
}
