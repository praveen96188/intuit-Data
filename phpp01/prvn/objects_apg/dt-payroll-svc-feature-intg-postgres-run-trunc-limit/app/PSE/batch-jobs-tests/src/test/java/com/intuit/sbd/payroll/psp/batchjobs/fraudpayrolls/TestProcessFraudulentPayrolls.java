package com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jul 19, 2008
 * Time: 11:43:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestProcessFraudulentPayrolls {
    private static Company1Dataloader c1dl;
    private static Company2Dataloader c2dl;

    private Company company;
    private DataLoader dataloader;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        company = new Company();
        dataloader = new DataLoader();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Test case to test the Fraudulent Payrolls by calling the FraudulentPayrolls batch process for multiple payrolls.
     */
    @Test
    public void testFraudulentPayrollsProcess() {
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        loadDataHappyPath();
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071123000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071119000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollFraudBatch fraudBatch = process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        assertNotNull("Fraud Batch", fraudBatch);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        String note = "Company ran more than 2 payrolls in a 3 day period for payroll with check date 11/27/2007";

        //Assertion for NumberOfPayrollsPerDayExceeded Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Company Event Detail Value ", note,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", "BatchIDX1", payrollRun.getSourcePayRunId());

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process again to make sure that there are no payrolls to process
        PayrollServices.beginUnitOfWork();
        process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        fraudBatch = process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        assertEquals("Fraud Batch", null, fraudBatch);
    }

    /**
     * Test case to test the Fraudulent Payrolls by calling the FraudulentPayrolls batch process for multiple payrolls
     * and multiple companies.
     */
    @Test
    public void testFraudulentPayrollsProcess_ForMultipleCompanies() {
        PayrollServices.beginUnitOfWork();
        //Load Data for Company1
        c1dl = new Company1Dataloader();
        loadDataHappyPath();

        PSPDate.setPSPTime("20070905000000");
        //Load Data for Company2
        c2dl = new Company2Dataloader();
        Company company1 = c2dl.persistCompany2();
        PayrollRunDTO payrollRunDTO = c2dl.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO);

        FraudValue fraudValue = FraudRule.findFraudRule(company1).findFraudValueByName(FraudValueType.FraudEEPercentGreaterThanOtherEEs);

        String originalFraudEEPercentGreatherThanOtherEEs = fraudValue.getValue();
        fraudValue.setValue("300");

        Application.save(fraudValue);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071123000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071119000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE ER DB for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollFraudBatch fraudBatch = process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        assertNotNull("Fraud Batch", fraudBatch);

        //CompanyEvent Assertions for Company1
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);


        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        String note = "Company ran more than 2 payrolls in a 3 day period for payroll with check date 11/27/2007";

        //Assertion for NumberOfPayrollsPerDayExceeded Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Company Event Detail Value ", note,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", "BatchIDX1", payrollRun.getSourcePayRunId());

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        PayrollServices.commitUnitOfWork();

        //CompanyEvent Assertions for Company1
        PayrollServices.beginUnitOfWork();
        Company company2 = Company.findCompany(c2dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        companyEventsList = CompanyEvent.findActiveCompanyFraudEvents(company2.getSourceCompanyId(), FraudEventCategory.Payroll, null, null, null, null);
        PayrollServices.commitUnitOfWork();

        // Restore source payroll parameter
        PayrollServices.beginUnitOfWork();
        Application.refresh(fraudValue);
        fraudValue.setValue(originalFraudEEPercentGreatherThanOtherEEs);
        Application.save(fraudValue);
        PayrollServices.commitUnitOfWork();


        assertEquals("Company Fraud Flag ", false, company2.getIsFlaggedForFraud());
        assertEquals("Company Events For FraudEvent", 0, companyEventsList.size());
    }

    /**
     * Test case to test the Fraudulent Payrolls by calling the FraudulentPayrolls batch process for multiple payrolls
     * and multiple companies. This test case call the Batch process seperately for each company.
     */
    @Test
    public void testFraudulentPayrollsProcess_MultipleBatches() {
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        loadDataHappyPath();
        FraudValue fraudValue = FraudRule.findFraudRule(c1dl.getCompany()).findFraudValueByName(FraudValueType.FraudPRNumberOfPayrollsInXDays);

        String previousFraudPRNumberOfPayrollsInXDaysValue = fraudValue.getValue();
        fraudValue.setValue("1");
        Application.save(fraudValue);


        PayrollServices.commitUnitOfWork();

        // offload all txns for Company1
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071123000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071119000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollFraudBatch fraudBatch1 = process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        assertNotNull("Fraud Batch", fraudBatch1);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Company1 Fraud Flag ", true, company.getIsFlaggedForFraud());
        assertEquals("Company Events", 1, companyEventsList.size());

        //Load Data for Company2
        PayrollServices.beginUnitOfWork();
        c2dl = new Company2Dataloader();
        c2dl.persistCompany2();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = Company.findCompany(c2dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PSPDate.setPSPTime(company2.getSignUpDate().toLocal());
        PayrollRunDTO payrollRunDTO = c2dl.getCompany2PR_ExceedsPaycheckLimit(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload QBOE EE CR for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE ER DB for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // call the txn process batch process for the date 11-30-2007
        processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071002");
        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process
        PayrollServices.beginUnitOfWork();
        process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollFraudBatch fraudBatch2 = process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        assertNotNull("Fraud Batch", fraudBatch2);

        PayrollServices.beginUnitOfWork();
        company2 = Company.findCompany(c2dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        companyEventsList = CompanyEvent.findCompanyEvents(company2,
                EventTypeCode.EmployeePaidGreaterThanMax, null, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Company2 Fraud Flag ", true, company2.getIsFlaggedForFraud());
        assertEquals("Company2 Events", 1, companyEventsList.size());

        // Restore source payroll parameter
        PayrollServices.beginUnitOfWork();

        Application.refresh(fraudValue);
        fraudValue.setValue(previousFraudPRNumberOfPayrollsInXDaysValue);

        Application.save(fraudValue);
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testEmpPaidGreaterThanXForAccountUpdate() {
        DataLoadServices.setPSPDate(2012, 8, 8);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        List<EmployeeDTO> employeeDTOs = DataLoadServices.createEEs(1, true);
        for (EmployeeDTO employeeDTO : employeeDTOs) {
            ProcessResult processResult = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employeeDTO);
            assertTrue(processResult.isSuccess());
        }

        int i = 0;
        for (Employee employee : company.getEmployees()) {
            BankAccountDTO bankAccountDTO = new BankAccountDTO();
            bankAccountDTO.setAccountNumber("123456" + Integer.toString(i++));
            bankAccountDTO.setAccountType(BankAccountType.Checking);
            bankAccountDTO.setBankName("Test");
            bankAccountDTO.setRoutingNumber("123123123");

            EmployeeBankAccountDTO employeeBankAccountDTO = DataLoadServices.createEmployeeBankAccount(employee, bankAccountDTO);
            ProcessResult processResult = PayrollServices.employeeManager.addEmployeeBankAccount(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employee.getSourceEmployeeId(),
                    employeeBankAccountDTO);
            assertTrue(processResult.isSuccess());
        }

        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));

        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 17), employees);
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
        }
        PayrollServices.commitUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));                                                                                                                                    // month is zero based
        payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 20), employees);
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            paycheckDTO.setPaycheckNetAmount(new SpcfMoney("4999.99"));
            paycheckDTO.getDdTransactions().get(0).setDDTransactionAmount(new BigDecimal("4999.99"));
        }
        PayrollServices.commitUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertTrue(company.getIsFlaggedForFraud());
        // assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.SingleEmployeePercentageIncrease).isNotEmpty());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.CurrentPayrollPercentageIncrease).isNotEmpty());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.EmployeeBankAccountChangedSpikeInPay).isNotEmpty());
        assertFalse(CompanyEvent.findCompanyEvents(company,EventTypeCode.PayrollProcessedTooSoon).isNotEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Ignore
    @Test
    public void findCompanyMatchesFraudMixedCaseCriteria() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company = c1DL.persistCompany1();
        CompanyService service = company.getCompanyServiceCollection().get(0);
        service.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.save(service);
        Employee employee = Employee.findEmployees(company).get(0);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        company = c3DL.persistCompany3();
        Employee newEmployee = Employee.findEmployees(company).get(0);
        newEmployee.setFirstName(employee.getFirstName());
        newEmployee.setLastName(employee.getLastName());
        Application.save(newEmployee);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        c3DL.persistPayrollRun(c3DL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        PayrollServices.commitUnitOfWork();
        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071204000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071129000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071230000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071230");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Pending);
        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollFraudBatch fraudBatch1 = process.getFraudBatch();
        assertNotNull("Fraud Batch", fraudBatch1);

        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> employeeBankAccountInTermedCompanyEvents = Application.find(CompanyEvent.class, CompanyEvent.Company().equalTo(company).And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.EmployeeBankAccountInTermedCompany)));
        assertEquals(employeeBankAccountInTermedCompanyEvents.size(), 2);

        PayrollServices.commitUnitOfWork();
    }


    private static void loadDataHappyPath() {
        PSPDate.setPSPTime("20070904000000");
        persistCompany1();
    }

    private static void persistCompany1() {
        c1dl.persistCompany1();
        //set up a company with 2 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        for (int i = 0; i < 12; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(2);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(2);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    //    currDDTxn.setDDTransactionAmount(pAmount);
                }
            }

            c1dl.persistPayrollRun(currentPayrollRunDTO);
        }
    }

    @Test
    public void test3EmpsSameBankAccountWithTax() {
        DataLoadServices.setPSPDate(2012, 8, 8);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        List<EmployeeDTO> employeeDTOs = DataLoadServices.createEEs(4, true);
        for (EmployeeDTO employeeDTO : employeeDTOs) {
            ProcessResult processResult = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employeeDTO);
            assertTrue(processResult.isSuccess());
        }

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber("123456");
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setBankName("Test");
        bankAccountDTO.setRoutingNumber("123123123");

        for (Employee employee : company.getEmployees()) {
            EmployeeBankAccountDTO employeeBankAccountDTO = DataLoadServices.createEmployeeBankAccount(employee, bankAccountDTO);
            ProcessResult processResult = PayrollServices.employeeManager.addEmployeeBankAccount(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employee.getSourceEmployeeId(),
                    employeeBankAccountDTO);
            assertTrue(processResult.isSuccess());
        }

        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 20), employees);
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        int count = 0;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            count++;
            if(count == 4) {
                for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                    ddTransactionDTO.setDDTransactionAmount(new BigDecimal(10001));
                }
            }
        }
        PayrollServices.commitUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertTrue(company.getIsFlaggedForFraud());
        // assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.EmployeesPaidToSameBank).isNotEmpty());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.EmployeePaidEvenDollarAmount).isNotEmpty());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.EmployeesPaidToSameBankAccount).isNotEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTaxEmpPaidMax() {
        DataLoadServices.setPSPDate(2012, 8, 8);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber("123456");
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setBankName("Test");
        bankAccountDTO.setRoutingNumber("123123123");

        for (Employee employee : company.getEmployees()) {
            EmployeeBankAccountDTO employeeBankAccountDTO = DataLoadServices.createEmployeeBankAccount(employee, bankAccountDTO);
            ProcessResult processResult = PayrollServices.employeeManager.addEmployeeBankAccount(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employee.getSourceEmployeeId(),
                    employeeBankAccountDTO);
            assertTrue(processResult.isSuccess());
        }

        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 20), employees);
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            paycheckDTO.setPaycheckNetAmount(new SpcfMoney("5000.01"));
            paycheckDTO.getDdTransactions().get(0).setDDTransactionAmount(new BigDecimal("5000.01"));
        }
        PayrollServices.commitUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertTrue(company.getIsFlaggedForFraud());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.EmployeePaidGreaterThanMax).isNotEmpty()); ;
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTaxPayrollPaidMax() {
        DataLoadServices.setPSPDate(2012, 8, 8);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        List<EmployeeDTO> employeeDTOs = DataLoadServices.createEEs(3, true);
        for (EmployeeDTO employeeDTO : employeeDTOs) {
            ProcessResult processResult = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employeeDTO);
            assertTrue(processResult.isSuccess());
        }

        int i = 0;
        for (Employee employee : company.getEmployees()) {
            BankAccountDTO bankAccountDTO = new BankAccountDTO();
            bankAccountDTO.setAccountNumber("123456" + Integer.toString(i++));
            bankAccountDTO.setAccountType(BankAccountType.Checking);
            bankAccountDTO.setBankName("Test");
            bankAccountDTO.setRoutingNumber("123123123");

            EmployeeBankAccountDTO employeeBankAccountDTO = DataLoadServices.createEmployeeBankAccount(employee, bankAccountDTO);
            ProcessResult processResult = PayrollServices.employeeManager.addEmployeeBankAccount(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employee.getSourceEmployeeId(),
                    employeeBankAccountDTO);
            assertTrue(processResult.isSuccess());
        }

        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 20), employees);
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            paycheckDTO.setPaycheckNetAmount(new SpcfMoney("4999.99"));
            paycheckDTO.getDdTransactions().get(0).setDDTransactionAmount(new BigDecimal("4999.99"));
        }
        PayrollServices.commitUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertTrue(company.getIsFlaggedForFraud());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.TotalPayrollExceedsLimit).isNotEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTaxPayrollPercentageIncrease() {
        DataLoadServices.setPSPDate(2012, 8, 8);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        List<EmployeeDTO> employeeDTOs = DataLoadServices.createEEs(1, true);
        for (EmployeeDTO employeeDTO : employeeDTOs) {
            ProcessResult processResult = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employeeDTO);
            assertTrue(processResult.isSuccess());
        }

        int i = 0;
        for (Employee employee : company.getEmployees()) {
            BankAccountDTO bankAccountDTO = new BankAccountDTO();
            bankAccountDTO.setAccountNumber("123456" + Integer.toString(i++));
            bankAccountDTO.setAccountType(BankAccountType.Checking);
            bankAccountDTO.setBankName("Test");
            bankAccountDTO.setRoutingNumber("123123123");

            EmployeeBankAccountDTO employeeBankAccountDTO = DataLoadServices.createEmployeeBankAccount(employee, bankAccountDTO);
            ProcessResult processResult = PayrollServices.employeeManager.addEmployeeBankAccount(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employee.getSourceEmployeeId(),
                    employeeBankAccountDTO);
            assertTrue(processResult.isSuccess());
        }

        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));

        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 17), employees);
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
        }
        PayrollServices.commitUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));                                                                                                                                    // month is zero based
        payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 20), employees);
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            paycheckDTO.setPaycheckNetAmount(new SpcfMoney("4999.99"));
            paycheckDTO.getDdTransactions().get(0).setDDTransactionAmount(new BigDecimal("4999.99"));
        }
        PayrollServices.commitUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertTrue(company.getIsFlaggedForFraud());
        // assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.SingleEmployeePercentageIncrease).isNotEmpty());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.CurrentPayrollPercentageIncrease).isNotEmpty());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.EmployeeBankAccountChangedSpikeInPay).isNotEmpty());
        assertFalse(CompanyEvent.findCompanyEvents(company,EventTypeCode.PayrollProcessedTooSoon).isNotEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTaxOneEmployeePercentageIncrease() throws Exception {
        DataLoadServices.setPSPDate(2012, 8, 8);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        DataLoadServices.setPSPDate(2012, 8, 15);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        List<EmployeeDTO> employeeDTOs = DataLoadServices.createEEs(1, true);
        for (EmployeeDTO employeeDTO : employeeDTOs) {
            ProcessResult processResult = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employeeDTO);
            assertTrue(processResult.isSuccess());
        }

        int i = 0;
        for (Employee employee : company.getEmployees()) {
            BankAccountDTO bankAccountDTO = new BankAccountDTO();
            bankAccountDTO.setAccountNumber("123456" + Integer.toString(i++));
            bankAccountDTO.setAccountType(BankAccountType.Checking);
            bankAccountDTO.setBankName("Test");
            bankAccountDTO.setRoutingNumber("123123123");

            EmployeeBankAccountDTO employeeBankAccountDTO = DataLoadServices.createEmployeeBankAccount(employee, bankAccountDTO);
            ProcessResult processResult = PayrollServices.employeeManager.addEmployeeBankAccount(SourceSystemCode.QBDT,
                    company.getSourceCompanyId(),
                    employee.getSourceEmployeeId(),
                    employeeBankAccountDTO);
            assertTrue(processResult.isSuccess());
        }

        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));

        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 17), employees);
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
        }
        PayrollServices.commitUnitOfWork();

        //DataLoadServices.setPSPDate(PSPDate.getPSPTime().);

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));                                                                                                                                    // month is zero based
        payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 20), employees);

        payrollRunDTO.getPaychecks().iterator().next().setPaycheckNetAmount(new SpcfMoney("10001"));
        payrollRunDTO.getPaychecks().iterator().next().getDdTransactions().get(0).setDDTransactionAmount(new BigDecimal("10001"));

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
        }
        PayrollServices.commitUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertTrue(company.getIsFlaggedForFraud());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.EmployeePaidEvenDollarAmount).isNotEmpty());
        //assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.SingleEmployeePercentageIncrease).isNotEmpty());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.EmployeeBankAccountChangedSpikeInPay).isNotEmpty());
        assertTrue(CompanyEvent.findCompanyEvents(company,EventTypeCode.CurrentPayrollPercentageIncrease).isNotEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

}
