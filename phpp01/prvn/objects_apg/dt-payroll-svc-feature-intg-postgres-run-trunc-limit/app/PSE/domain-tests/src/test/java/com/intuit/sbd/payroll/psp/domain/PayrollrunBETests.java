package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls.ProcessFraudulentPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jul 16, 2008
 * Time: 3:25:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class PayrollrunBETests {
    private static Company1Dataloader c1dl;
    private static Company3Dataloader c3dl;




    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testNumberOfPayrollsPerDayExceeded() {
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        loadDataHappyPath(2, new BigDecimal("5001.00"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX0");
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

        //Assertion for Payroll Fraud Batch Token. Maker sure that payrollfraud batch token is notupdated when the
        //Payroll Run status changed
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX0");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //Call PayrollRunBE.numberOfPayrollsPerDayExceeded() method
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX1");
        payrollRun.numberOfPayrollsPerDayExceeded();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 3, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);

        String note = "Company ran more than 2 payrolls in a 3 day period for payroll with check date 11/27/2007";

        //Assertion for NumberOfPayrollsPerDayExceeded Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Company Event Detail Value ", note,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", "BatchIDX1", payrollRun.getSourcePayRunId());

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        PayrollServices.commitUnitOfWork();

        //Call PayrollRunBE.numberOfPayrollsPerDayExceeded() method second time, to make sure that it's not creating NumberOfPayrollsPerDayExceeded event again.
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX1");
        payrollRun.numberOfPayrollsPerDayExceeded();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Assertion for NumberOfPayrollsPerDayExceeded Event
        assertEquals("Company Events", 1, companyEventsList.size());
    }

    @Test
    public void testNumberOfPayrollsPerDayExceeded_DoesNotMeetTheCriteria() {
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        loadDataHappyPath(5, new BigDecimal("5001.00"));
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071126000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071120000000");
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

        //Call PayrollRunBE.numberOfPayrollsPerDayExceeded() method
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).numberOfPayrollsPerDayExceeded();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);
        PayrollServices.commitUnitOfWork();

        //Assertion for NumberOfPayrollsPerDayExceeded Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Test
    public void testNumberOfPayrollsPerDayExceededForQBDT_WithMigratedPayrolls() {
        persistQBDTCompany(2, new BigDecimal("1001.00"));
        //Set the migrated payroll count as 2
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(2);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071123000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071120000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071121000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //Call PayrollRunBE.numberOfPayrollsPerDayExceeded() method
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX1");
        payrollRun.numberOfPayrollsPerDayExceeded();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 3, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);

        String note = "Company ran more than 2 payrolls in a 3 day period for payroll with check date 11/26/2007";

        //Assertion for NumberOfPayrollsPerDayExceeded Event
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Company Event Detail Value ", note,
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details));

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", "BatchIDX1", payrollRun.getSourcePayRunId());

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        PayrollServices.commitUnitOfWork();

        //Call PayrollRunBE.numberOfPayrollsPerDayExceeded() method second time, to make sure that it's not creating NumberOfPayrollsPerDayExceeded event again.
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX1");
        payrollRun.numberOfPayrollsPerDayExceeded();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Assertion for NumberOfPayrollsPerDayExceeded Event
        assertEquals("Company Events", 1, companyEventsList.size());
    }

    @Test
    public void testNumberOfPayrollsPerDayExceededForQBDT_DoesNotMeetCriteria() {
        persistQBDTCompany(2, new BigDecimal("1001.00"));
        //Set the migrated payroll count as 4
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(4);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071123000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071120000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071121000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //Call PayrollRunBE.numberOfPayrollsPerDayExceeded() method
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX1");
        payrollRun.numberOfPayrollsPerDayExceeded();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 3, payrollRuns.size());

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);

        PayrollServices.commitUnitOfWork();
        //Assertion for NumberOfPayrollsPerDayExceeded Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Test
    public void testEmployeePaidGreaterThanMax() {

        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));

        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        BigDecimal amount = new BigDecimal("5001.00");
        for (PaycheckDTO currPaycheck : payChecks) {
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
            }
        }
        c1DL.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071004000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-04-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071004");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).employeePaidGreaterThanMax();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidGreaterThanMax, null, null, null);

        String employee1Note = "EE FirstNameOfEE1:TestLastName paid 5001.00 in a single paycheck for payroll with check date 10/02/2007";
        String employee2Note = "EE FirstNameOfEE2:TestLastName2 paid 5001.00 in a single paycheck for payroll with check date 10/02/2007";

        //Assertion for EmployeePaidGreaterThanMax Event
        assertEquals("Company Events", 2, companyEventsList.size());

        CompanyEvent employee1Event = null;
        CompanyEvent employee2Event = null;

        for (CompanyEvent companyEvent : companyEventsList) {
            String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
            Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

            if (employee.getSourceEmployeeId().equals(c1DL.getEmployee1(company).getSourceEmployeeId())) {
                employee1Event = companyEvent;
            }

            if (employee.getSourceEmployeeId().equals(c1DL.getEmployee2(company).getSourceEmployeeId())) {
                employee2Event = companyEvent;
            }
        }

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRuns.get(0).getSourcePayRunId(), payrollRun.getSourcePayRunId());

        String employeeId = employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
        Employee employee1 = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

        junit.framework.Assert.assertEquals("Employee1 Id ", c1DL.getEmployee1(company).getSourceEmployeeId(), employee1.getSourceEmployeeId());

        assertEquals("Company Event Detail Value ", employee1Note,
                employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        employeeId = employee2Event.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
        Employee employee2 = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

        junit.framework.Assert.assertEquals("Employee2 Id ", c1DL.getEmployee2(company).getSourceEmployeeId(), employee2.getSourceEmployeeId());

        assertEquals("Company Event Detail Value ", employee2Note,
                employee2Event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                employee2Event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEmployeePaidGreaterThanMax_DoesNotMeetTheCriteria() {
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        loadDataHappyPath(2, new BigDecimal("300.00"));
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

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).employeePaidGreaterThanMax();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 3, payrollRuns.size());
        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidGreaterThanMax, null, null, null);
        PayrollServices.commitUnitOfWork();

        //Assertion for EmployeePaidGreaterThanMax Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Test
    public void testEmployeePaidGreaterThanMaxForQBDT_WithMigratedPayrolls() {

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));

        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        BigDecimal amount = new BigDecimal("5001.00");
        for (PaycheckDTO currPaycheck : payChecks) {
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
            }
        }
        c3DL.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Set the migrated payroll count as 4
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(4);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).employeePaidGreaterThanMax();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidGreaterThanMax, null, null, null);

        String employee1Note = "EE ThirdCompEEFirst:ThirdCompEELast paid 5001.00 in a single paycheck for payroll with check date 10/02/2007";
        String employee2Note = "EE ThirdCompEEFirstTwo:ThirdCompEELastTwo paid 5001.00 in a single paycheck for payroll with check date 10/02/2007";

        //Assertion for EmployeePaidGreaterThanMax Event
        assertEquals("Company Events", 2, companyEventsList.size());

        CompanyEvent employee1Event = null;
        CompanyEvent employee2Event = null;

        for (CompanyEvent companyEvent : companyEventsList) {
            String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
            Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

            if (employee.getSourceEmployeeId().equals(c3DL.getEmployee1(company).getSourceEmployeeId())) {
                employee1Event = companyEvent;
            }

            if (employee.getSourceEmployeeId().equals(c3DL.getEmployee2(company).getSourceEmployeeId())) {
                employee2Event = companyEvent;
            }
        }

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRuns.get(0).getSourcePayRunId(), payrollRun.getSourcePayRunId());

        String employeeId = employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
        Employee employee1 = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

        junit.framework.Assert.assertEquals("Employee1 Id ", c3DL.getEmployee1(company).getSourceEmployeeId(), employee1.getSourceEmployeeId());

        assertEquals("Company Event Detail Value ", employee1Note,
                employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        employeeId = employee2Event.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
        Employee employee2 = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

        junit.framework.Assert.assertEquals("Employee2 Id ", c3DL.getEmployee2(company).getSourceEmployeeId(), employee2.getSourceEmployeeId());

        assertEquals("Company Event Detail Value ", employee2Note,
                employee2Event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                employee2Event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEmployeePaidGreaterThanMaxForQBDT_DoesNotMeetCriteria() {

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));

        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        BigDecimal amount = new BigDecimal("5001.00");
        for (PaycheckDTO currPaycheck : payChecks) {
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
            }
        }
        c3DL.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Set the migrated payroll count as 6
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(6);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).employeePaidGreaterThanMax();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidGreaterThanMax, null, null, null);

        PayrollServices.commitUnitOfWork();
        //Assertion for EmployeePaidGreaterThanMax Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Test
    public void testTotalPayrollExceedsLimit() {
        PayrollServices.beginUnitOfWork();
        DateDTO payrollDateOutsideofRange = new DateDTO();
        payrollDateOutsideofRange.set(2007, Calendar.SEPTEMBER, 8);
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PR_ExistingPR(new DateDTO("2007-10-02"));
        existingPayrollRun.setTargetPayrollTXDate(payrollDateOutsideofRange);
        c1DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070919000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070914000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 09-25-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20070925");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).totalPayrollExceedsLimit();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.TotalPayrollExceedsLimit, null, null, null);

        String note = "Company ran a total payroll of amount 15150.00 for payroll with check date 09/21/2007";

        //Assertion for TotalPayrollExceedsLimit Event
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent event = companyEventsList.get(0);

        String payrollRunId = event.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRuns.get(0).getSourcePayRunId(), payrollRun.getSourcePayRunId());

        assertEquals("Company Event Detail Value ", note,
                event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        Application.commitUnitOfWork();
    }

    @Test
    public void testTotalPayrollExceedsLimit_DoesNotMeetTheCriteria() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).totalPayrollExceedsLimit();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.TotalPayrollExceedsLimit, null, null, null);
        PayrollServices.commitUnitOfWork();

        //Assertion for TotalPayrollExceedsLimit Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Test
    public void testTotalPayrollExceedsLimitQBDT_WithMigratedPayrolls() {
        PayrollServices.beginUnitOfWork();
        DateDTO payrollDateOutsideofRange = new DateDTO();
        payrollDateOutsideofRange.set(2007, Calendar.SEPTEMBER, 8);
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollRunDTO existingPayrollRun = c3DL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Collection<PaycheckDTO> payChecks = existingPayrollRun.getPaychecks();
        BigDecimal amount = new BigDecimal("7501.00");
        for (PaycheckDTO currPaycheck : payChecks) {
            SpcfDecimal paycheckAmount = SpcfDecimal.createInstance("0.00");
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
                paycheckAmount = paycheckAmount.add(SpcfUtils.convertToSpcfMoney(amount));
            }
            currPaycheck.setPaycheckNetAmount(new SpcfMoney(paycheckAmount));
        }
        c3DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        //Set the migrated payroll count as 4
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(4);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).totalPayrollExceedsLimit();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.TotalPayrollExceedsLimit, null, null, null);

        String note = "Company ran a total payroll of amount 15002.00 for payroll with check date 10/02/2007";

        //Assertion for TotalPayrollExceedsLimit Event
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent event = companyEventsList.get(0);

        String payrollRunId = event.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRuns.get(0).getSourcePayRunId(), payrollRun.getSourcePayRunId());

        assertEquals("Company Event Detail Value ", note,
                event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        Application.commitUnitOfWork();
    }

    @Test
    public void testTotalPayrollExceedsLimitForQBDT_DoesNotMeetCriteria() {
        PayrollServices.beginUnitOfWork();
        DateDTO payrollDateOutsideofRange = new DateDTO();
        payrollDateOutsideofRange.set(2007, Calendar.SEPTEMBER, 8);
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollRunDTO existingPayrollRun = c3DL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Collection<PaycheckDTO> payChecks = existingPayrollRun.getPaychecks();
        BigDecimal amount = new BigDecimal("7501.00");
        for (PaycheckDTO currPaycheck : payChecks) {
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
            }
        }
        c3DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        //Set the migrated payroll count as 6
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(6);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).totalPayrollExceedsLimit();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.TotalPayrollExceedsLimit, null, null, null);

        PayrollServices.commitUnitOfWork();

        //Assertion for TotalPayrollExceedsLimit Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }


    @Test
    public void testEmployeePaidEvenDollarAmount() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PRData(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).employeePaidEvenDollarAmount();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidEvenDollarAmount, null, null, null);
        String employee1Note = "EE FirstNameOfEE1:TestLastName paid 10001.00, an even dollar amount which is greater than 10000.00, for payroll with check date 10/02/2007";
        String employee2Note = "EE FirstNameOfEE2:TestLastName2 paid 150.00, an even dollar amount which is greater than 10000.00, for payroll with check date 10/02/2007";

        //Assertion for EmployeePaidEvenDollarAmount Event
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent employee1Event = null;


        for (CompanyEvent companyEvent : companyEventsList) {
            String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
            Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

            if (employee.getSourceEmployeeId().equals(c1DL.getEmployee1(company).getSourceEmployeeId())) {
                employee1Event = companyEvent;
            }

        }

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRuns.get(0).getSourcePayRunId(), payrollRun.getSourcePayRunId());

        String employeeId = employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
        Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

        junit.framework.Assert.assertEquals("Employee1 Id ", c1DL.getEmployee1(company).getSourceEmployeeId(), employee.getSourceEmployeeId());

        assertEquals("Company Event Detail Value ", employee1Note,
                employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        Application.commitUnitOfWork();
    }


    @Test
    public void testPayeePaidEvenDollarAmount() {
        DataLoadServices.setPSPDate(2007, 8, 31);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment);
        Payee payee = assertOne(DataLoadServices.addPayees(company, 1));

        PayrollServices.beginUnitOfWork();
        SpcfCalendar paymentDate = SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone());

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment(payee.getSourcePayeeId(), new DateDTO(paymentDate), 1);
        billPaymentDTOs.add(billPaymentDTO);
        billPaymentDTO.setAmount(new SpcfMoney("10120.00"));
        assertOne(billPaymentDTO.getPaymentTransactions()).setAmount(new BigDecimal("10120.00"));
        ProcessResult<Collection<PayrollRun>> processResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertSuccess(processResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun billPayment = Application.refresh(assertOne(processResult.getResult()));
        billPayment.payeePaidEvenDollarAmount();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(company);
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());
        Application.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidEvenDollarAmount, null, null, null);

        //Assertion for PayeePaidTooManyTimes Event
        CompanyEvent event = assertOne(companyEventsList);
        assertEquals("Company Event Detail Value ", "Payee " + payee.getName() +" paid 10120.00, an even dollar amount which is greater than 10000.00, for vendor payment with check date 09/05/2007",
                event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals(payee.getId().toString(), event.getCompanyEventDetailValue(EventDetailTypeCode.PayeeId));

        Application.commitUnitOfWork();
    }

    @Test
    public void testEmployeePaidEvenDollarAmount_DoesNotMeetTheCriteria() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));

        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        BigDecimal amount = new BigDecimal("100.15");
        for (PaycheckDTO currPaycheck : payChecks) {
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
            }
            amount = amount.add(new BigDecimal("150.42"));
        }
        c1DL.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).employeePaidEvenDollarAmount();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidEvenDollarAmount, null, null, null);
        PayrollServices.commitUnitOfWork();

        //Assertion for EmployeePaidEvenDollarAmount Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Test
    public void testEmployeePaidEvenDollarAmountForQBDT_WithMigratedPayrolls() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollRunDTO existingPayrollRun = c3DL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Collection<PaycheckDTO> payChecks = existingPayrollRun.getPaychecks();
        BigDecimal amount = new BigDecimal("10001.00");
        for (PaycheckDTO currPaycheck : payChecks) {
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
            }
        }
        c3DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        //Set the migrated payroll count as 1
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(1);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).employeePaidEvenDollarAmount();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidEvenDollarAmount, null, null, null);

        String employee1Note = "EE ThirdCompEEFirst:ThirdCompEELast paid 10001.00, an even dollar amount which is greater than 10000.00, for payroll with check date 10/02/2007";

        //Assertion for EmployeePaidEvenDollarAmount Event
        assertEquals("Company Events", 2, companyEventsList.size());

        CompanyEvent employee1Event = null;
        CompanyEvent employee2Event = null;

        for (CompanyEvent companyEvent : companyEventsList) {
            String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
            Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

            if (employee.getSourceEmployeeId().equals(c3DL.getEmployee1(company).getSourceEmployeeId())) {
                employee1Event = companyEvent;
            }

            if (employee.getSourceEmployeeId().equals(c3DL.getEmployee2(company).getSourceEmployeeId())) {
                employee2Event = companyEvent;
            }
        }

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRuns.get(0).getSourcePayRunId(), payrollRun.getSourcePayRunId());

        String employeeId = employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
        Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

        junit.framework.Assert.assertEquals("Employee1 Id ", c3DL.getEmployee1(company).getSourceEmployeeId(), employee.getSourceEmployeeId());

        assertEquals("Company Event Detail Value ", employee1Note,
                employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                employee1Event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        Application.commitUnitOfWork();
    }

    @Test
    public void testEmployeePaidEvenDollarAmountForQBDT_DoesNotMeetCriteria() {
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollRunDTO existingPayrollRun = c3DL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Collection<PaycheckDTO> payChecks = existingPayrollRun.getPaychecks();
        BigDecimal amount = new BigDecimal("150.00");
        for (PaycheckDTO currPaycheck : payChecks) {
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
            }
        }
        c3DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        //Set the migrated payroll count as 3
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(3);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).employeePaidEvenDollarAmount();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidEvenDollarAmount, null, null, null);

        PayrollServices.commitUnitOfWork();
        //Assertion for EmployeePaidEvenDollarAmount Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }


    /**
     * Test case to test the fraud criteria for PayrollProcessedToSoon by submitting more than the maximum allowed number of payrolls one business
     * day before the configured parameter number of days to check
     */
    @Test
    public void testPayrollProcessedTooSoon_LessThanXDays() {
        DataLoader dataLoader = new DataLoader();
        PayrollSubmitDataLoader payrollSubmitDataLoader = new PayrollSubmitDataLoader();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20080801000000");
        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany();
        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);
        String psID = company.getSourceCompanyId();

        //Create CompanyBankAccount
        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), dataLoader.getTestCompanyBankAccount(), true, true);
        PayrollServices.commitUnitOfWork();
        assertSuccess("addCompanyBankAccount", addCBAProcResult);

        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        //Offload & Verify Bank Accounts
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        company = Application.refresh(company);
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRNumberOfDaysForXPayrolls);

        int numberOfDays = Integer.parseInt(fraudValue.getValue());

        SpcfCalendar submitDate = company.getSignUpDate().copy();
        CalendarUtils.addBusinessDays(submitDate, numberOfDays - 1);
        PSPDate.setPSPTime(submitDate);

        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psID, SourceSystemCode.QBOE);
        DomainEntitySet< CompanyBankAccount> cba = CompanyBankAccount.findCompanyBankAccounts(company);
        companyBankAccount = cba.get(0);
        companyBankAccount.addVerificationTransaction();

        companyBankAccount.addVerificationTransaction();

        DomainEntitySet<FinancialTransaction> verificationTransactions1 = companyBankAccount.getVerificationTransactions();
        TransactionState completedTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Completed);
        for(FinancialTransaction fTransaction :verificationTransactions1 ) {
            fTransaction.setCurrentTransactionState(completedTransactionState);
        }
        // Application.save(FinancialTransaction.class);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBOE);
        ProcessResult<HashMap<String, String>> procResult =
                PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBOE, company.getSourceCompanyId(), "1234567a");
        PayrollServicesTest.assertSuccess("createPINResult", procResult);
        GenerateData.generateEmployees(company, 3);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        for (int i=1; i<4; i++)  {
            PayrollRunDTO payrollRunDTO = payrollSubmitDataLoader.createPayrollRunDTO(company, companyBankAccount, "BatchId0"+i);
            payrollRunDTO.setTargetPayrollTXDate(new DateDTO("2008-08-08"));
            Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
            BigDecimal amount = new BigDecimal("4000.00");
            for (PaycheckDTO currPaycheck : payChecks) {
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionAmount(amount);
                }
            }
            ProcessResult<PayrollRun> processResult1 = PayrollServices.payrollManager
                    .submitPayroll(SourceSystemCode.QBOE, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess("submitPayroll", processResult1);


        }


        PayrollServices.commitUnitOfWork();
        // Ensure processing was succsessful


        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId02");
        payrollRun.payrollProcessedTooSoon();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.PayrollProcessedTooSoon, null, null, null);

        //Todo MV check rewording of message
        String note = "Company ran 3 payrolls of amount greater than 10000.00 with in 3 days of test verification debit date  of 08/06/2008";

        //Assertion for PayrollProcessedTooSoon Event
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent event = companyEventsList.get(0);

        String payrollRunId = event.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun1 = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRun.getSourcePayRunId(), payrollRun1.getSourcePayRunId());

        assertEquals("Company Event Detail Value ", note,
                event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        Application.commitUnitOfWork();
    }

    /**
     * Test case to test the fraud criteria for PayrollProcessedToSoon by submitting the payroll in 2 business day after
     * company signup date.
     */
    @Test
    public void testPayrollProcessedSoon_LessThanAllowedNumberOfPayrolls() {
        DataLoader dataLoader = new DataLoader();
        PayrollSubmitDataLoader payrollSubmitDataLoader = new PayrollSubmitDataLoader();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20080801000000");
        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany();
        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);

        String psID = company.getSourceCompanyId();

        //Create CompanyBankAccount
        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), dataLoader.getTestCompanyBankAccount(), true, true);
        PayrollServices.commitUnitOfWork();
        assertSuccess("addCompanyBankAccount", addCBAProcResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psID, SourceSystemCode.QBOE);
        DomainEntitySet< CompanyBankAccount> cba = CompanyBankAccount.findCompanyBankAccounts(company);
        CompanyBankAccount companyBankAccount = cba.get(0);
        // TODO: Commented to make them pass in postgres
        //  Need to review why it was added in the first place.
//        companyBankAccount.addVerificationTransaction();

//        companyBankAccount.addVerificationTransaction();

        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        TransactionState completedTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Completed);
//        for(FinancialTransaction fTransaction :verificationTransactions ) {
//            fTransaction.setCurrentTransactionState(completedTransactionState);
//        }
        // Application.save(FinancialTransaction.class);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        companyBankAccount = addCBAProcResult.getResult();
        //Offload & Verify Bank Accounts
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions1 = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions1) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.setPSPTime("20080805000000");
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBOE);
        ProcessResult<HashMap<String, String>> procResult =
                PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBOE, company.getSourceCompanyId(), "1234567a");
        PayrollServicesTest.assertSuccess("createPINResult", procResult);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        PayrollRunDTO payrollRunDTO = payrollSubmitDataLoader.createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO("2008-08-08"));
        ProcessResult<PayrollRun> processResult1 = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult1);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        payrollRun.payrollProcessedTooSoon();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.PayrollProcessedTooSoon, null, null, null);

        //PayrollProcessedTooSoon Event not created
        assertEquals("Company Events", 0, companyEventsList.size());


        Application.commitUnitOfWork();
    }

    @Test
    public void testPayrollProcessedTooSoon_DoesNotMeetTheCriteria() {
        DataLoader dataLoader = new DataLoader();
        PayrollSubmitDataLoader payrollSubmitDataLoader = new PayrollSubmitDataLoader();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20080801000000");
        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany();
        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);
        String psID = company.getSourceCompanyId();

        //Create CompanyBankAccount
        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), dataLoader.getTestCompanyBankAccount(), true, true);
        PayrollServices.commitUnitOfWork();
        //add random dollar debits

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psID, SourceSystemCode.QBOE);
        DomainEntitySet< CompanyBankAccount> cba = CompanyBankAccount.findCompanyBankAccounts(company);
        CompanyBankAccount companyBankAccount = cba.get(0);
//        companyBankAccount.addVerificationTransaction();

//        companyBankAccount.addVerificationTransaction();

        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        TransactionState completedTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Completed);
//        for(FinancialTransaction fTransaction :verificationTransactions ) {
//            fTransaction.setCurrentTransactionState(completedTransactionState);
//        }
        // Application.save(FinancialTransaction.class);
        PayrollServices.commitUnitOfWork();assertSuccess("addCompanyBankAccount", addCBAProcResult);





        PayrollServices.beginUnitOfWork();
        companyBankAccount = addCBAProcResult.getResult();


        //Offload & Verify Bank Accounts
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions1 = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions1) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // Set PSP Time to a date in the future so that the validation of settlement date will pass
        PSPDate.setPSPTime("20080806000000");
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
        PayrollServices.commitUnitOfWork();
        assertSuccess("verifyCompanyBankAccount", processResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBOE);
        ProcessResult<HashMap<String, String>> procResult =
                PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBOE, company.getSourceCompanyId(), "1234567a");
        PayrollServicesTest.assertSuccess("createPINResult", procResult);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        PayrollRunDTO payrollRunDTO = payrollSubmitDataLoader.createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO("2008-08-08"));
        ProcessResult<PayrollRun> processResult1 = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult1);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        payrollRun.payrollProcessedTooSoon();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.PayrollProcessedTooSoon, null, null, null);
        PayrollServices.commitUnitOfWork();

        //Assertion for PayrollProcessedTooSoon Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Test
    public void testCurrentPayrollPercentageIncrease() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount = new BigDecimal("180.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(amount);
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            BigDecimal amount1 = amount.multiply(new BigDecimal(i + 1));
            amount = amount.add(amount1);
        }

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
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).currentPayrollPercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CurrentPayrollPercentageIncrease, null, null, null);

        //Assertion for CurrentPayrollPercentageIncrease Event
        assertEquals("Company Events", 2, companyEventsList.size());

        CompanyEvent event1 = null;
        CompanyEvent event2 = null;

        for (CompanyEvent companyEvent : companyEventsList) {
            String payrollRunId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
            PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

            if (payrollRun.getSourcePayRunId().equals("BatchIDX1")) {
                event1 = companyEvent;
            }

            if (payrollRun.getSourcePayRunId().equals("BatchIDX2")) {
                event2 = companyEvent;
            }
        }

        String note1 = "Payroll(s) for the settlement date of 12/06/2007 have a combined amount of 720.00 which is over 50% more than prior payroll amount.";
        String note2 = "Payroll(s) for the settlement date of 12/13/2007 have a combined amount of 2160.00 which is over 50% more than prior payroll amount.";

        String payrollRunId = event1.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", "BatchIDX1", payrollRun.getSourcePayRunId());

        assertEquals("Company Event Detail Value ", note1,
                event1.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event1.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        payrollRunId = event2.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", "BatchIDX2", payrollRun.getSourcePayRunId());

        assertEquals("Company Event Detail Value ", note2,
                event2.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event2.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        Application.commitUnitOfWork();
    }

    @Test
    public void testCurrentPayrollPercentageIncrease_CancelledTxns() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount = new BigDecimal("180.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(amount);
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            BigDecimal amount1 = amount.multiply(new BigDecimal(i + 1));
            amount = amount.add(amount1);
        }

        PayrollServices.commitUnitOfWork();

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
        dto.setSourcePayrollRunId("BatchIDX1");

        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX1");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        FinancialTransaction finTxn = payroll1FinTxns.get(0);

        sourcePaycheckIdList.add(finTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        dto.setSourcePaycheckIdList(sourcePaycheckIdList);
        PSPDate.setPSPTime("20071129000000");
        ProcessResult result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), dto);
        assertSuccess(result);

        dto.setSourcePayrollRunId("BatchIDX2");
        payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX2");

        payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        sourcePaycheckIdList.remove(finTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        for (FinancialTransaction financialTxn : payroll1FinTxns) {
            if (!sourcePaycheckIdList.contains(financialTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId())) {
                sourcePaycheckIdList.add(financialTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
            }
        }
        dto.setSourcePaycheckIdList(sourcePaycheckIdList);
        PSPDate.setPSPTime("20071206000000");
        result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), dto);

        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        // offload All txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071204000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //offloader.offloadAndPostOffload(OffloadGroupBE.Codes.STANDARD, null);
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
        company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).currentPayrollPercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CurrentPayrollPercentageIncrease, null, null, null);

        //Assertion for CurrentPayrollPercentageIncrease Event
        assertEquals("Company Events", 0, companyEventsList.size());

        Application.commitUnitOfWork();
    }

    @Test
    public void testCurrentPayrollPercentageIncreaseForQBDT_WithMigratedPayrolls() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        c3dl = new Company3Dataloader();
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();

        //Set the migrated payroll count as 2
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(2);
        Application.save(company);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount = new BigDecimal("180.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(amount);
                }

                SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
                for (DDTransactionDTO currDDTxn : currPaycheck.getDdTransactions()) {
                    SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                    totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
                }
                currPaycheck.setPaycheckNetAmount(totalPaycheckNetAmount);
            }

            c3dl.persistPayrollRun(currentPayrollRunDTO);
            BigDecimal amount1 = amount.multiply(new BigDecimal(i + 1));
            amount = amount.add(amount1);
        }

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX0");
        payrollRun.currentPayrollPercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CurrentPayrollPercentageIncrease, null, null, null);

        //Assertion for CurrentPayrollPercentageIncrease Event
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent companyEvent = companyEventsList.get(0);

        String payrollRunId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        String note1 = "Payroll(s) for the settlement date of 12/05/2007 have a combined amount of 720.00 which is over 50% more than prior payroll amount.";

        assertEquals("Payroll Run Id ", "BatchIDX1", payrollRun.getSourcePayRunId());

        assertEquals("Company Event Detail Value ", note1,
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        Application.commitUnitOfWork();
    }

    @Test
    public void testCurrentPayrollPercentageIncreaseForQBDT_DoesNotMeetCriteria() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        c3dl = new Company3Dataloader();
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(7);
        Application.save(company);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount = new BigDecimal("180.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(amount);
                }

                SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
                for (DDTransactionDTO currDDTxn : currPaycheck.getDdTransactions()) {
                    SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                    totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
                }
                currPaycheck.setPaycheckNetAmount(totalPaycheckNetAmount);
            }

            c3dl.persistPayrollRun(currentPayrollRunDTO);
            BigDecimal amount1 = amount.multiply(new BigDecimal(i + 1));
            amount = amount.add(amount1);
        }

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX0");
        payrollRun.currentPayrollPercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CurrentPayrollPercentageIncrease, null, null, null);
        PayrollServices.commitUnitOfWork();
        //Assertion for CurrentPayrollPercentageIncrease Event
        assertEquals("Company Events", 0, companyEventsList.size());

    }

    /**
     * Test Case to test the CurrentPayroll Percentage Increase method by persisting the data for different payrols
     * with same paycheck settlement date.
     */
    @Test
    public void testCurrentPayrollPercentageIncrease_MeetCriteriaForNextPayroll() {
        loadDataForDifferentPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX0");
        payrollRun.currentPayrollPercentageIncrease();
        PayrollServices.commitUnitOfWork();

        //assertEquals("Number Of Completed Payrolls", 3, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CurrentPayrollPercentageIncrease, null, null, null);

        //Assertion for CurrentPayrollPercentageIncrease Event
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent companyEvent = companyEventsList.get(0);

        String payrollRunId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        String note1 = "Payroll(s) for the settlement date of 11/28/2007 have a combined amount of 720.00 which is over 50% more than prior payroll amount.";

        assertEquals("Payroll Run Id ", "BatchIDX1", payrollRun.getSourcePayRunId());

        assertEquals("Company Event Detail Value ", note1,
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        Application.commitUnitOfWork();
    }

    /**
     * Test Case to test the CurrentPayroll Percentage Increase method by persisting the data for different payrols
     * with same paycheck settlement date.
     */
    @Test
    public void testCurrentPayrollPercentageIncrease_MeetCriteriaForPriorPayroll() {
        loadDataForDifferentPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX2");
        payrollRun.currentPayrollPercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CurrentPayrollPercentageIncrease, null, null, null);

        //Assertion for CurrentPayrollPercentageIncrease Event
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent companyEvent = companyEventsList.get(0);

        String payrollRunId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        String note1 = "Payroll(s) for the settlement date of 12/05/2007 have a combined amount of 2160.00 which is over 50% more than prior payroll amount.";

        assertEquals("Payroll Run Id ", "BatchIDX2", payrollRun.getSourcePayRunId());

        assertEquals("Company Event Detail Value ", note1,
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        Application.commitUnitOfWork();
    }

    /**
     * Test Case to test the CurrentPayroll Percentage Increase method by persisting the data for different payrols
     * with same paycheck settlement date.
     */
    @Test
    public void testCurrentPayrollPercentageIncrease_DoesNotMeetCriteria() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        payrollRun.currentPayrollPercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CurrentPayrollPercentageIncrease, null, null, null);
        PayrollServices.commitUnitOfWork();

        //Assertion for CurrentPayrollPercentageIncrease Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    private void loadDataForDifferentPayrolls() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount = new BigDecimal("180.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            testPaycheckDate.addDays(7);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(amount);
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            BigDecimal amount1 = amount.multiply(new BigDecimal(i + 1));
            amount = amount.add(amount1);
        }

        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071120000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071115000000");
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
    }

    @Ignore
    @Test
    public void testEmployeeBankAccountPaidFromFraudReviewCompany() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company = c1DL.persistCompany1();
        PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.FraudReview);
        Application.save(company);
        Employee employee = Employee.findEmployees(company).get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        company = c3DL.persistCompany3();
        Employee newEmployee = Employee.findEmployees(company).get(0);
        EmployeeBankAccount newEmployeeBankAccount = newEmployee.getEmployeeBankAccountCollection().get(0);
        newEmployeeBankAccount.getBankAccount().setAccountNumber(employeeBankAccount.getBankAccount().getAccountNumber());
        newEmployeeBankAccount.getBankAccount().setRoutingNumber(employeeBankAccount.getBankAccount().getRoutingNumber());
        newEmployeeBankAccount.getBankAccount().setAccountTypeCd(employeeBankAccount.getBankAccount().getAccountTypeCd());
        Application.save(newEmployeeBankAccount.getBankAccount());
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
        payrollRuns.get(0).isEmployeeBankAccountInTerminatedOrFraudHoldCompany();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Pending Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeeBankAccountInTermedCompany, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 2, companyEventsList.size());
    }

    @Ignore
    @Test
    public void testPayeeBankAccountPaidFromFraudReviewCompany() {
        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment);
        Payee payee = assertOne(DataLoadServices.addPayees(company, 1));

        SpcfCalendar paymentDate = SpcfCalendar.createInstance(2013, 1, 3, SpcfTimeZone.getLocalTimeZone());

        //Submit a payment for the first company
        PayrollServices.beginUnitOfWork();
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment(payee.getSourcePayeeId(), new DateDTO(paymentDate), 1);
        PayeeBankAccountDTO payeeBankAccountDTO = PayrollServices.dtoFactory.create(Application.refresh(payee).getPayeeBankAccountCollection().getFirst());
        billPaymentDTO.getPaymentTransactions().iterator().next().setPayeeBankAccount(payeeBankAccountDTO);
        billPaymentDTOs.add(billPaymentDTO);
        ProcessResult<Collection<PayrollRun>> processResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Put that first company on hold
        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.Fraud);

        //Create a new company
        Company newCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment);
        Payee newPayee = assertOne(DataLoadServices.addPayees(newCompany, 1));
        DataLoadServices.addPayeeBankAccount(newCompany, newPayee, BankAccountType.Checking);

        //Run a payment with the same bank account
        PayrollServices.beginUnitOfWork();
        billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTO = GenerateData.generateBillPayment(newPayee.getSourcePayeeId(), new DateDTO(paymentDate), 1);
        //use same bank account as above but different source ID
        billPaymentDTO.getPaymentTransactions().iterator().next().setPayeeBankAccount(payeeBankAccountDTO);
        billPaymentDTOs.add(billPaymentDTO);
        processResult = PayrollServices.billPaymentManager.submitBillPayment(newCompany.getSourceSystemCd(), newCompany.getSourceCompanyId(), billPaymentDTOs);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Check for fraud
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(processResult.getResult());
        Application.refresh(payrollRun);
        payrollRun.isPayeeBankAccountInTerminatedOrFraudHoldCompany();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(newCompany);
        assertEquals("Company Fraud Flag ", true, newCompany.getIsFlaggedForFraud());
        assertOne(CompanyEvent.findCompanyEvents(newCompany, EventTypeCode.EmployeeBankAccountInTermedCompany, null, null, null));
        PayrollServices.commitUnitOfWork();
    }

    @Ignore
    @Test
    public void testEmployeeBankAccountPaidFromFraudCompany() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company = c1DL.persistCompany1();
        PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud);
        Application.save(company);
        Employee employee = Employee.findEmployees(company).get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        company = c3DL.persistCompany3();
        Employee newEmployee = Employee.findEmployees(company).get(0);
        EmployeeBankAccount newEmployeeBankAccount = newEmployee.getEmployeeBankAccountCollection().get(0);
        newEmployeeBankAccount.getBankAccount().setAccountNumber(employeeBankAccount.getBankAccount().getAccountNumber());
        newEmployeeBankAccount.getBankAccount().setRoutingNumber(employeeBankAccount.getBankAccount().getRoutingNumber());
        newEmployeeBankAccount.getBankAccount().setAccountTypeCd(employeeBankAccount.getBankAccount().getAccountTypeCd());
        Application.save(newEmployeeBankAccount.getBankAccount());
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
        payrollRuns.get(0).isEmployeeBankAccountInTerminatedOrFraudHoldCompany();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Pending Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeeBankAccountInTermedCompany, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 2, companyEventsList.size());
    }

    @Ignore
    @Test
    public void testEmployeeBankAccountPaidFromCompanyWithExpiredFraudReason() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company = c1DL.persistCompany1();
        PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud);
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud));
        Application.save(company);
        Employee employee = Employee.findEmployees(company).get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        company = c3DL.persistCompany3();
        Employee newEmployee = Employee.findEmployees(company).get(0);
        EmployeeBankAccount newEmployeeBankAccount = newEmployee.getEmployeeBankAccountCollection().get(0);
        newEmployeeBankAccount.getBankAccount().setAccountNumber(employeeBankAccount.getBankAccount().getAccountNumber());
        newEmployeeBankAccount.getBankAccount().setRoutingNumber(employeeBankAccount.getBankAccount().getRoutingNumber());
        newEmployeeBankAccount.getBankAccount().setAccountTypeCd(employeeBankAccount.getBankAccount().getAccountTypeCd());
        Application.save(newEmployeeBankAccount.getBankAccount());
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
        payrollRuns.get(0).isEmployeeBankAccountInTerminatedOrFraudHoldCompany();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Pending Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeeBankAccountInTermedCompany, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Ignore
    @Test
    public void testEmployeeBankAccountPaidFromTerminatedCompany() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company = c1DL.persistCompany1();
        CompanyService service = company.getCompanyServiceCollection().get(0);
        service.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.save(service);
        Employee employee = Employee.findEmployees(company).get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        company = c3DL.persistCompany3();
        Employee newEmployee = Employee.findEmployees(company).get(0);
        EmployeeBankAccount newEmployeeBankAccount = newEmployee.getEmployeeBankAccountCollection().get(0);
        newEmployeeBankAccount.getBankAccount().setAccountNumber(employeeBankAccount.getBankAccount().getAccountNumber());
        newEmployeeBankAccount.getBankAccount().setRoutingNumber(employeeBankAccount.getBankAccount().getRoutingNumber());
        newEmployeeBankAccount.getBankAccount().setAccountTypeCd(employeeBankAccount.getBankAccount().getAccountTypeCd());
        Application.save(newEmployeeBankAccount.getBankAccount());
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
        payrollRuns.get(0).isEmployeeBankAccountInTerminatedOrFraudHoldCompany();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Pending Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeeBankAccountInTermedCompany, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 2, companyEventsList.size());
    }

    @Test
    public void testEmployeePaidFromTerminatedCompany1stPayroll() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company = c1DL.persistCompany1();
        CompanyService service = company.getCompanyServiceCollection().get(0);
        service.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        Application.save(service);
        Employee employee = Employee.findEmployees(company).sort(Employee.SourceEmployeeId()).get(0);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3DL = new Company3Dataloader();
        company = c3DL.persistCompany3();
        Employee newEmployee = Employee.findEmployees(company).sort(Employee.SourceEmployeeId()).get(0);
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
        payrollRuns.get(0).isEmployeeInTerminatedCompany();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Pending Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeeInTermedCompany, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent fraudEvent = companyEventsList.get(0);
        String details = fraudEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details);
        assertEquals("Details: ", "Employee TestLastName, FirstNameOfEE1 TMI was detected in company PSID: 1234567 that has been terminated.", details);

        PayrollServices.commitUnitOfWork();

    }

    @Test
    // Expect Fraud to be set
    public void testEmployeePaidFromTerminatedCompany3rdPayroll() {
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
        PayrollServices.commitUnitOfWork();
        persistXPayrollsForCompany3(2, c3DL);

        // Match Employee to termed company
        PayrollServices.beginUnitOfWork();
        Employee newEmployee = Employee.findEmployees(company).get(0);
        newEmployee.setFirstName(employee.getFirstName());
        newEmployee.setLastName(employee.getLastName());
        Application.save(newEmployee);
        PayrollServices.commitUnitOfWork();

        // Submit payroll
        PayrollServices.beginUnitOfWork();
        c3DL.persistPayrollRun(c3DL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-11-22")));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Pending);
        payrollRuns.get(0).isEmployeeInTerminatedCompany();
        PayrollServices.commitUnitOfWork();

        //      assertEquals("Number Of Pending Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeeInTermedCompany, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 1, companyEventsList.size());

    }

    @Test
    // Expect no fraud alert
    public void testEmployeePaidFromTerminatedCompany4thPayroll() {
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
        PayrollServices.commitUnitOfWork();
        persistXPayrollsForCompany3(3, c3DL);

        // Match Employee to termed company
        PayrollServices.beginUnitOfWork();
        Employee newEmployee = Employee.findEmployees(company).get(0);
        newEmployee.setFirstName(employee.getFirstName());
        newEmployee.setLastName(employee.getLastName());
        Application.save(newEmployee);
        PayrollServices.commitUnitOfWork();

        // Submit payroll
        PayrollServices.beginUnitOfWork();
        c3DL.persistPayrollRun(c3DL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-11-29")));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Pending);
        payrollRuns.get(0).isEmployeeInTerminatedCompany();
        PayrollServices.commitUnitOfWork();

        //      assertEquals("Number Of Pending Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeeInTermedCompany, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 0, companyEventsList.size());


    }

    @Test
    public void testSingleEmployeePercentageIncrease() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount = new BigDecimal("180.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(amount);
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            BigDecimal amount1 = amount.multiply(new BigDecimal(i + 1));
            amount = amount.add(amount1);
        }

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
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).singleEmployeePercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.SingleEmployeePercentageIncrease, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 4, companyEventsList.size());

        String note1 = "Employee FirstNameOfEE1 TestLastName has a combined amount of 360.00 for the settlement date of 12/06/2007 which is over 50% more than in the prior payroll.";
        String note2 = "Employee FirstNameOfEE2 TestLastName2 has a combined amount of 360.00 for the settlement date of 12/06/2007 which is over 50% more than in the prior payroll.";
        String note3 = "Employee FirstNameOfEE1 TestLastName has a combined amount of 1080.00 for the settlement date of 12/13/2007 which is over 50% more than in the prior payroll.";
        String note4 = "Employee FirstNameOfEE2 TestLastName2 has a combined amount of 1080.00 for the settlement date of 12/13/2007 which is over 50% more than in the prior payroll.";

        for (CompanyEvent companyEvent : companyEventsList) {
            String payrollRunId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
            PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

            if (payrollRun.getSourcePayRunId().equals("BatchIDX1")) {

                assertEquals("Payroll Run Id ", "BatchIDX1", payrollRun.getSourcePayRunId());

                String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
                Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

                if (employee.getSourceEmployeeId().equals(c1DL.getEmployee1(company).getSourceEmployeeId())) {
                    junit.framework.Assert.assertEquals("Employee1 Id ", c1DL.getEmployee1(company).getSourceEmployeeId(), employee.getSourceEmployeeId());

                    assertEquals("Company Event Detail Value ", note1,
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

                    assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
                }

                if (employee.getSourceEmployeeId().equals(c1DL.getEmployee2(company).getSourceEmployeeId())) {
                    junit.framework.Assert.assertEquals("Employee2 Id ", c1DL.getEmployee2(company).getSourceEmployeeId(), employee.getSourceEmployeeId());

                    assertEquals("Company Event Detail Value ", note2,
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

                    assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
                }
            }

            if (payrollRun.getSourcePayRunId().equals("BatchIDX2")) {
                assertEquals("Payroll Run Id ", "BatchIDX2", payrollRun.getSourcePayRunId());

                String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
                Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

                if (employee.getSourceEmployeeId().equals(c1DL.getEmployee1(company).getSourceEmployeeId())) {
                    junit.framework.Assert.assertEquals("Employee1 Id ", c1DL.getEmployee1(company).getSourceEmployeeId(), employee.getSourceEmployeeId());

                    assertEquals("Company Event Detail Value ", note3,
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

                    assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
                }

                if (employee.getSourceEmployeeId().equals(c1DL.getEmployee2(company).getSourceEmployeeId())) {
                    junit.framework.Assert.assertEquals("Employee2 Id ", c1DL.getEmployee2(company).getSourceEmployeeId(), employee.getSourceEmployeeId());

                    assertEquals("Company Event Detail Value ", note4,
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

                    assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
                }
            }
        }
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> fraudEventCategoryList = CompanyEvent.findActiveCompanyFraudEvents(null, FraudEventCategory.Payroll, null,
                null, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Fraud Event Category Detail List ", 4, fraudEventCategoryList.size());
    }

    @Test
    public void testSingleEmployeePercentageIncrease_CancelledTxns() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount = new BigDecimal("180.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(amount);
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            BigDecimal amount1 = amount.multiply(new BigDecimal(i + 1));
            amount = amount.add(amount1);
        }

        PayrollServices.commitUnitOfWork();

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
        dto.setSourcePayrollRunId("BatchIDX1");

        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX1");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        FinancialTransaction finTxn = payroll1FinTxns.get(0);

        sourcePaycheckIdList.add(finTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        dto.setSourcePaycheckIdList(sourcePaycheckIdList);
        PSPDate.setPSPTime("20071129000000");
        ProcessResult result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), dto);
        assertSuccess(result);

        dto.setSourcePayrollRunId("BatchIDX2");
        payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX2");

        payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        sourcePaycheckIdList.remove(finTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        for (FinancialTransaction financialTxn : payroll1FinTxns) {
            if (!sourcePaycheckIdList.contains(financialTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId())) {
                sourcePaycheckIdList.add(financialTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
            }
        }
        dto.setSourcePaycheckIdList(sourcePaycheckIdList);
        PSPDate.setPSPTime("20071206000000");
        result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), dto);

        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        assertSuccess(result);
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
        company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX1");

        payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        finTxn = payroll1FinTxns.get(0);

        // update the amount for the second financial transaction
        finTxn.setFinancialTransactionAmount(new SpcfMoney(SpcfMoney.createInstance("180.00")));
        Application.save(finTxn);

        payrollRuns.get(0).singleEmployeePercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.SingleEmployeePercentageIncrease, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 1, companyEventsList.size());

        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> fraudEventCategoryList = CompanyEvent.findActiveCompanyFraudEvents(null, FraudEventCategory.Payroll,
                null, null, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Fraud Event Category Detail List ", 1, fraudEventCategoryList.size());
    }

    @Test
    public void testSingleEmployeePercentageIncrease_DoesNotMeetTheCriteria() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        payrollRun.singleEmployeePercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.SingleEmployeePercentageIncrease, null, null, null);
        PayrollServices.commitUnitOfWork();

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Test
    public void testSingleEmployeePercentageIncrease_MeetTheCriteriaForNextPayroll() {
        loadDataForDifferentPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX0");
        payrollRun.singleEmployeePercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.SingleEmployeePercentageIncrease, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 2, companyEventsList.size());

        String note1 = "Employee FirstNameOfEE1 TestLastName has a combined amount of 360.00 for the settlement date of 11/28/2007 which is over 50% more than in the prior payroll.";
        String note2 = "Employee FirstNameOfEE2 TestLastName2 has a combined amount of 360.00 for the settlement date of 11/28/2007 which is over 50% more than in the prior payroll.";

        for (CompanyEvent companyEvent : companyEventsList) {
            String payrollRunId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
            payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

            if (payrollRun.getSourcePayRunId().equals("BatchIDX1")) {

                assertEquals("Payroll Run Id ", "BatchIDX1", payrollRun.getSourcePayRunId());

                String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
                Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

                if (employee.getSourceEmployeeId().equals("EE1")) {
                    assertEquals("Employee1 Id ", "EE1", employee.getSourceEmployeeId());

                    assertEquals("Company Event Detail Value ", note1,
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

                    assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
                }

                if (employee.getSourceEmployeeId().equals("EE2")) {
                    assertEquals("Employee2 Id ", "EE2", employee.getSourceEmployeeId());

                    assertEquals("Company Event Detail Value ", note2,
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

                    assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
                }
            }
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void testSingleEmployeePercentageIncrease_MeetTheCriteriaForPriorPayroll() {
        loadDataForDifferentPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchIDX2");
        payrollRun.singleEmployeePercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.SingleEmployeePercentageIncrease, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 2, companyEventsList.size());

        String note1 = "Employee FirstNameOfEE1 TestLastName has a combined amount of 1080.00 for the settlement date of 12/05/2007 which is over 50% more than in the prior payroll.";
        String note2 = "Employee FirstNameOfEE2 TestLastName2 has a combined amount of 1080.00 for the settlement date of 12/05/2007 which is over 50% more than in the prior payroll.";

        for (CompanyEvent companyEvent : companyEventsList) {
            String payrollRunId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
            payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

            if (payrollRun.getSourcePayRunId().equals("BatchIDX2")) {

                assertEquals("Payroll Run Id ", "BatchIDX2", payrollRun.getSourcePayRunId());

                String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
                Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

                if (employee.getSourceEmployeeId().equals("EE1")) {
                    assertEquals("Employee1 Id ", "EE1", employee.getSourceEmployeeId());

                    assertEquals("Company Event Detail Value ", note1,
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

                    assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
                }

                if (employee.getSourceEmployeeId().equals("EE2")) {
                    assertEquals("Employee2 Id ", "EE2", employee.getSourceEmployeeId());

                    assertEquals("Company Event Detail Value ", note2,
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

                    assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                            companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
                }
            }
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testEESpikeInPayBAUpdated() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 5 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount = new BigDecimal("80.00");
        for (int i = 0; i < 4; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(amount);
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            BigDecimal amount1 = amount.multiply(new BigDecimal(i + 1));
            amount = amount.add(amount1);
        }

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

        // Update EE Bank Account
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071217000000");
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        EmployeeBankAccountDTO eebaDTO = c1DL.getEmployee1BankAccount();
        BankAccountDTO ba = new BankAccountDTO();
        ba.setAccountNumber("12345678");
        ba.setRoutingNumber("111000025");
        ba.setBankName("abc bank");
        ba.setAccountType(BankAccountType.Checking);

        eebaDTO.setEmployeeBankAccountId("EEBA1");
        eebaDTO.setBankAccount(ba);

        PayrollServices.employeeManager.updateEmployeeBankAccount(SourceSystemCode.QBOE, company.getSourceCompanyId(), "EE1", eebaDTO);
        PayrollServices.commitUnitOfWork();

        // Execute Fraud Control Method
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun.findLatestCompanyPayrollRun(company).employeeBankAccountChanged();

        PayrollServices.commitUnitOfWork();


        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeeBankAccountChangedSpikeInPay, null, null, null);

        //Assertion for EmployeeBankAccountChangedSpikeInPay Event
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent companyEvent = companyEventsList.get(0);
        String note = "EE FirstNameOfEE1:TestLastName paid over 50.00% more than the average of the last 3 paychecks in payroll with paycheck date of 12/20/2007";


        String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
        Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));


        junit.framework.Assert.assertEquals("Employee1 Id ", c1DL.getEmployee1(company).getSourceEmployeeId(), employee.getSourceEmployeeId());

        assertEquals("Company Event Detail Value ", note,
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));


    }

    private static void loadDataHappyPath(int numberOfDays, BigDecimal pAmount) {
        PSPDate.setPSPTime("20070904000000");
        persistCompany1(numberOfDays, pAmount);
    }

    private static void persistCompany1(int pNumberOfDays, BigDecimal pAmount) {
        c1dl.persistCompany1();
        //set up a company with 2 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        for (int i = 0; i < 12; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(pNumberOfDays);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(pNumberOfDays);
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
    public void testGetPendingRedebit() throws Exception {
        RedebitAddTestDataLoader redebitDataloader = new RedebitAddTestDataLoader();
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollDTO = redebitDataloader.loadDataForEmployerDebitReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        // find the pending redebit
        DomainEntitySet<FinancialTransaction> erDebits = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        FinancialTransaction originalTransaction = erDebits.get(0);

        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTransaction.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTransaction.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());

        assertSuccess(processResult);

        FinancialTransaction pendingRedebit =
                FinancialTransaction.getPendingRedebitTransaction(originalTransaction.getId().toString());
        PayrollServices.commitUnitOfWork();

        assertNotNull(pendingRedebit);
        assertEquals("Redebit amount", originalTransaction.getFinancialTransactionAmount(), pendingRedebit.getFinancialTransactionAmount());
        assertEquals("Original Transaction", originalTransaction, pendingRedebit.getOriginalTransaction());
        assertEquals("Transaction Type", TransactionTypeCode.EmployerDdRedebit, pendingRedebit.getTransactionType().getTransactionTypeCd());
        assertEquals("Transaction State", TransactionStateCode.Created, pendingRedebit.getCurrentTransactionState().getTransactionStateCd());

        // offload redebit
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        // Again verify and make sure no pending redebits
        PayrollServices.beginUnitOfWork();
        pendingRedebit =
                FinancialTransaction.getPendingRedebitTransaction(originalTransaction.getId().toString());
        assertEquals("No Pending Redebit", null, pendingRedebit);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void expectedResolutionDate() {
        PayrollSubmitDataLoader loader = new PayrollSubmitDataLoader();
        loader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);

        // create company, etc.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070831000000");
        PayrollRunDTO dtoPayroll = loader.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        // submit a payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", dtoPayroll);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("submitPayroll()", prPayroll);

        // offload payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        // charge a fee
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        PayrollRun payroll = Application.refresh(prPayroll.getResult());
        ERFeeAddDTO dtoFee = new ERFeeAddDTO(SourceSystemCode.QBDT, "123272727", payroll.getSourcePayRunId(), SettlementTypeDTO.ACH, null, null, OfferingServiceChargeType.DebitReturnFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> prFee = PayrollServices.financialTransactionManager.addFeeTransaction(dtoFee);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("addFeeTransaction()", prFee);
        FinancialTransaction ftFee = prFee.getResult().getFirst();

        // offload the fee
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(0); // manually-billed fees settle ASAP
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions feeOffloader = new OffloadACHTransactions();
        feeOffloader.offloadAndPostOffload("STD", null);

        // create a return for the fee and handle that return
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        ftFee = Application.refresh(ftFee);
        DomainEntitySet<FinancialTransaction> feeFTs = new DomainEntitySet<FinancialTransaction>();
        feeFTs.add(ftFee);
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(ACHReturnsDataLoader.getMoneyMovementTransactions(feeFTs, false), "R02", "This is a non-NSF description");
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(returnList.get(0));
        handler.execute(returnList.get(0));
        PayrollServices.commitUnitOfWork();

        // create a redebit for the manual fee
        PayrollServices.beginUnitOfWork();
        RedebitImpoundDTO dtoRedebit = new RedebitImpoundDTO(ftFee.getId().toString(),
                ftFee.getFinancialTransactionAmount(),
                new DateDTO(PSPDate.getPSPTime()),
                SettlementTypeDTO.ACH);
        ArrayList<RedebitImpoundDTO> redebitDTOs = new ArrayList<RedebitImpoundDTO>();
        redebitDTOs.add(dtoRedebit);
        ProcessResult prRedebit = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                SourceSystemCode.QBDT, "123272727", redebitDTOs);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("addOrEditPayrollRelatedRedebitImpound()", prRedebit);

        // try to get the expected resolution date...
        PayrollServices.beginUnitOfWork();
        payroll = Application.refresh(payroll);
        SpcfCalendar expected = null;
        try {
            expected = payroll.getExpectedResolutionDate(); // PSRV000570 sez this call throws NPE
            System.out.println("Expected resolution date is " + expected);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("PayrollRunBE.getExpectedResolutionDate() still throwing exception");
        }
        PayrollServices.commitUnitOfWork();
        assertTrue("expected resolution date is non-null", expected != null);
    }


    @Test
    public void testEEsPaidSameBank() {
        PayrollServices.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        Company1Dataloader c1dl = new Company1Dataloader();
        Company company = c1dl.persistCompany1();
        c1dl.persistEmployee2BankAccount2();
        c1dl.persistEmployee3BankAccount1();
        c1dl.persistEmployee4BankAccount1();
        c1dl.persistEmployeeBankAccount();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_SameBASameCheckDate(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollRunDTO thisPayrollRunDTO = c1dl.getCompany1PR_MultiplePaycheckSplitsDifferentBA(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        // set parameter values
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRNumberOfPayrollsToCheckSameBank);
        fraudValue.setValue("10");
        Application.save(fraudValue);

        FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRTotalEmployeesToCheckSameBank);
        fraudValue.setValue("1");
        Application.save(fraudValue);

        FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRPercentEmployeesPaidSameBank);
        fraudValue.setValue("95");
        Application.save(fraudValue);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        payrollRuns = payrollRuns.sort(PayrollRun.PayrollRunDate().Descending());
        payrollRuns.get(0).employeesPaidToTheSameBank();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeesPaidToSameBank, null, null, null);

        //Assertion for EmployeesPaidToSameBank Event
        assertEquals("Company Events", 1, companyEventsList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEEsPaidSameBank_NumberOfPayrollsGreaterThanNumberToCheck() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company = c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1DL.persistEmployeesAndBankAccounts();

        company = Application.refresh(company);

        // set parameter values
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRNumberOfPayrollsToCheckSameBank);
        fraudValue.setValue("-1");
        Application.save(fraudValue);

        fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRPercentEmployeesPaidSameBank);
        fraudValue.setValue("95");
        Application.save(fraudValue);

        fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRTotalEmployeesToCheckSameBank);
        fraudValue.setValue("1");
        Application.save(fraudValue);

        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 10, 23, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        ArrayList<PayrollRunDTO> payrollRunDTOs = c1DL.getCompany1PR_MultiplePaycheckSplitsDifferentBA1(new DateDTO("2007-10-31"), 1);
        for (PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            c1DL.persistPayrollRun(payrollRunDTO);
        }

        testTime = SpcfCalendar.createInstance(2007, 11, 1, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        //set up a company with 3 payrolls with different paycheck dates
        payrollRunDTOs = c1DL.getCompany1PR_MultiplePaycheckSplitsDifferentBA1(new DateDTO("2007-11-06"), 5);

        for (int i = payrollRunDTOs.size() - 1; i >= 0; i--) {
            PayrollRunDTO payrollRunDTO = payrollRunDTOs.get(i);
            Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionAmount(new BigDecimal("180.00"));
                }

                SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
                for (DDTransactionDTO currDDTxn : currPaycheck.getDdTransactions()) {
                    SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                    totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
                }
                currPaycheck.setPaycheckNetAmount(totalPaycheckNetAmount);
            }

            c1DL.persistPayrollRun(payrollRunDTO);

        }
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
        PSPDate.setPSPTime("20071223000000");
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
        company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        payrollRuns = payrollRuns.sort(PayrollRun.PayrollRunDate().Descending());
        payrollRuns.get(0).employeesPaidToTheSameBank();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());


    }

    @Test
    public void testEEsPaidSameBank_PercentLessThanPercentToCheck() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company = c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1DL.persistEmployeesAndBankAccounts();

        company = Application.refresh(company);

        // set parameter values
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRNumberOfPayrollsToCheckSameBank);
        fraudValue.setValue("3");
        Application.save(fraudValue);

        fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRPercentEmployeesPaidSameBank);
        fraudValue.setValue("101");
        Application.save(fraudValue);

        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 10, 23, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        ArrayList<PayrollRunDTO> payrollRunDTOs = c1DL.getCompany1PR_MultiplePaycheckSplitsDifferentBA1(new DateDTO("2007-10-31"), 1);
        for (PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            c1DL.persistPayrollRun(payrollRunDTO);
        }

        testTime = SpcfCalendar.createInstance(2007, 11, 1, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        //set up a company with 3 payrolls with different paycheck dates
        payrollRunDTOs = c1DL.getCompany1PR_MultiplePaycheckSplitsDifferentBA1(new DateDTO("2007-11-06"), 5);

        for (int i = payrollRunDTOs.size() - 1; i >= 0; i--) {
            PayrollRunDTO payrollRunDTO = payrollRunDTOs.get(i);
            Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionAmount(new BigDecimal("180.00"));
                }

                SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
                for (DDTransactionDTO currDDTxn : currPaycheck.getDdTransactions()) {
                    SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                    totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
                }
                currPaycheck.setPaycheckNetAmount(totalPaycheckNetAmount);
            }

            c1DL.persistPayrollRun(payrollRunDTO);

        }
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        payrollRuns = payrollRuns.sort(PayrollRun.PayrollRunDate().Descending());
        payrollRuns.get(0).employeesPaidToTheSameBank();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());


    }


    @Test
    public void testPayeesPaidSameBank() {
        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment);
        List<Payee> payees = DataLoadServices.addPayees(company, 3);

        PayrollServices.beginUnitOfWork();
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        for (Payee payee : payees) {
            SpcfCalendar paymentDate = SpcfCalendar.createInstance(2013, 1, 3, SpcfTimeZone.getLocalTimeZone());
            BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment(payee.getSourcePayeeId(), new DateDTO(paymentDate), 1);
            //Default behavior from GenerateData is to create a new bank account for each bill payment.
            //We want to use the one that's already on the Payee (they all have the same routing number)
            PayeeBankAccountDTO payeeBankAccountDTO = PayrollServices.dtoFactory.create(Application.refresh(payee).getPayeeBankAccountCollection().getFirst());
            billPaymentDTO.getPaymentTransactions().iterator().next().setPayeeBankAccount(payeeBankAccountDTO);
            billPaymentDTOs.add(billPaymentDTO);
        }
        ProcessResult<Collection<PayrollRun>> processResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // set parameter values
        PayrollServices.beginUnitOfWork();
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRNumberOfPayrollsToCheckSameBank);
        fraudValue.setValue("10");
        Application.save(fraudValue);

        FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRTotalEmployeesToCheckSameBank);
        fraudValue.setValue("3");
        Application.save(fraudValue);

        FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRPercentEmployeesPaidSameBank);
        fraudValue.setValue("75");
        Application.save(fraudValue);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(processResult.getResult());
        Application.refresh(payrollRun);
        payrollRun.employeesPaidToTheSameBank();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(company);
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeesPaidToSameBank, null, null, null);

        //Assertion for EmployeesPaidToSameBank Event
        assertEquals("Company Events", 1, companyEventsList.size());
        PayrollServices.commitUnitOfWork();
    }

    //begin same bank account tests
    @Test
    public void testEEsPaidToSameBankAccount_2EEs1BA() {
        setSourceParamsToDefault();
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.persistEmployee2BankAccount2();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "1234567", new SpcfMoney("100000.00"), new SpcfMoney("50000.00"));
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_OneBAExceedsLimits(new DateDTO("2007-10-02"));

        ProcessResult submitDDPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        Application.commitUnitOfWork();
        assertTrue(submitDDPayroll.isSuccess());

        Application.beginUnitOfWork();
        // simulate fraud checks
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        employeesPaidToSameBankAccountOnLastPayroll(company);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeesPaidToSameBankAccount, null, null, null);

        assertEquals("Company Events", 0, companyEventsList.size());

    }


    @Test
    public void testEEsPaidToSameBankAccount_3EEsOneBA() {
        setSourceParamsToDefault();
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.persistEmployee2BankAccount2();
        c1dl.persistEmployee3BankAccount1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_OneBAThreeEEs(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "1234567", new SpcfMoney("100000.00"), new SpcfMoney("50000.00"));
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        ProcessResult submitDDPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        Application.commitUnitOfWork();
        assertTrue(submitDDPayroll.isSuccess());

        Application.beginUnitOfWork();
        // simulate fraud checks
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        employeesPaidToSameBankAccountOnLastPayroll(company);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeesPaidToSameBankAccount, null, null, null);

        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testEEsPaidToSameBankAccount_4EEsOneBA() {
        setSourceParamsToDefault();
        Application.beginUnitOfWork();

        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.persistEmployee2BankAccount2();
        c1dl.persistEmployee3BankAccount1();
        c1dl.persistEmployee4BankAccount1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_OneBAFourEEs(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "1234567", new SpcfMoney("100000.00"), new SpcfMoney("50000.00"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        ProcessResult submitDDPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        Application.commitUnitOfWork();
        assertTrue(submitDDPayroll.isSuccess());

        Application.beginUnitOfWork();
        // simulate fraud checks
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        employeesPaidToSameBankAccountOnLastPayroll(company);
        PayrollServices.commitUnitOfWork();

        // test
        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeesPaidToSameBankAccount, null, null, null);

        assertEquals("Company Events", 1, companyEventsList.size());


        String details = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details);

        Assert.assertEquals("Company Event Details:",
                "More than 3 unique employees paid into a single bank account on the same day \n" +
                        "\n" +
                        "Bank Name: abc bank\n" +
                        "Bank Routing: 111000025\n" +
                        "Account Type: Checking\n" +
                        "Bank Account: 12345\n" +
                        "\n" +
                        "Source Payroll Id: BatchTestExceedsBALimits\n" +
                        "Payroll Date: October 02, 2007 12:00 AM\n" +
                        "Payroll Amount: $21,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE1 TestLastName\n" +
                        "Paycheck Amount: $8,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE2 TestLastName2\n" +
                        "Paycheck Amount: $9,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE3 TestLastName3\n" +
                        "Paycheck Amount: $2,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE4 TestLastName4\n" +
                        "Paycheck Amount: $2,000.00\n" +
                        "\n", details);

        Application.commitUnitOfWork();
    }

    @Test
    public void testEEsPaidToSameBankAccount_4EEsOneExistingPayroll() {
        setSourceParamsToDefault();
        Application.beginUnitOfWork();

        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.persistEmployee2BankAccount2();
        c1dl.persistEmployee3BankAccount1();
        c1dl.persistEmployee4BankAccount1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_ExistingPR(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollRunDTO thisPayrollRunDTO = c1dl.getCompany1PR_SameBASameCheckDate(new DateDTO("2007-10-02"));
        thisPayrollRunDTO.setPayrollTXBatchId("AnotherBatch");
        PayrollServices.commitUnitOfWork();

        //this one should not produce any events
        PayrollServices.beginUnitOfWork();
        employeesPaidToSameBankAccountOnLastPayroll(Company.findCompany("1234567", SourceSystemCode.QBOE));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "1234567", new SpcfMoney("100000.00"), new SpcfMoney("50000.00"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult submitDDPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", thisPayrollRunDTO);
        Application.commitUnitOfWork();
        assertTrue(submitDDPayroll.isSuccess());

        Application.beginUnitOfWork();
        // simulate fraud checks
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        employeesPaidToSameBankAccountOnLastPayroll(company);
        PayrollServices.commitUnitOfWork();

        // test
        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeesPaidToSameBankAccount, null, null, null);

        assertEquals("Company Events", 1, companyEventsList.size());


        String details = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details);

        Assert.assertEquals("Company Event Details:",
                "More than 3 unique employees paid into a single bank account on the same day \n" +
                        "\n" +
                        "Bank Name: abc bank\n" +
                        "Bank Routing: 111000025\n" +
                        "Account Type: Checking\n" +
                        "Bank Account: 12345\n" +
                        "\n" +
                        "Source Payroll Id: AnotherBatch\n" +
                        "Payroll Date: October 02, 2007 12:00 AM\n" +
                        "Payroll Amount: $14,300.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE2 TestLastName2\n" +
                        "Paycheck Amount: $14,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE3 TestLastName3\n" +
                        "Paycheck Amount: $150.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE4 TestLastName4\n" +
                        "Paycheck Amount: $150.00\n" +
                        "\n" +
                        "Source Payroll Id: BatchTest02\n" +
                        "Payroll Date: October 02, 2007 12:00 AM\n" +
                        "Payroll Amount: $15,150.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE1 TestLastName\n" +
                        "Paycheck Amount: $15,000.00\n" +
                        "\n"
                , details);

        Application.commitUnitOfWork();
    }

    @Test
    public void testEEsPaidToSameBankAccount_4EEsThreeExisting() {
        setSourceParamsToDefault();
        //simulate submissions

        Application.beginUnitOfWork();

        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.persistEmployee2BankAccount2();
        c1dl.persistEmployee3BankAccount1();
        c1dl.persistEmployee4BankAccount1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_SameBASameCheckDate(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollRunDTO thisPayrollRunDTO = c1dl.getCompany1PR_ExistingPR(new DateDTO("2007-10-02"));

        thisPayrollRunDTO.setPayrollTXBatchId("AnotherBatch");
        PayrollServices.commitUnitOfWork();

        //this one should not produce any events
        PayrollServices.beginUnitOfWork();
        employeesPaidToSameBankAccountOnLastPayroll(Company.findCompany("1234567", SourceSystemCode.QBOE));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "1234567", new SpcfMoney("100000.00"), new SpcfMoney("50000.00"));
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult submitDDPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", thisPayrollRunDTO);
        Application.commitUnitOfWork();
        assertTrue(submitDDPayroll.isSuccess());

        PayrollServices.beginUnitOfWork();
        // simulate fraud checks
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        employeesPaidToSameBankAccountOnLastPayroll(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // simulate fraud checks
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        //test results
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeesPaidToSameBankAccount, null, null, null);

        assertEquals("Company Events", 1, companyEventsList.size());


        String details = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details);


        Assert.assertEquals("Company Event Details",
                "More than 3 unique employees paid into a single bank account on the same day \n" +
                        "\n" +
                        "Bank Name: abc bank\n" +
                        "Bank Routing: 111000025\n" +
                        "Account Type: Checking\n" +
                        "Bank Account: 12345\n" +
                        "\n" +
                        "Source Payroll Id: AnotherBatch\n" +
                        "Payroll Date: October 02, 2007 12:00 AM\n" +
                        "Payroll Amount: $15,150.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE1 TestLastName\n" +
                        "Paycheck Amount: $15,000.00\n" +
                        "\n" +
                        "Source Payroll Id: BatchTest02\n" +
                        "Payroll Date: October 02, 2007 12:00 AM\n" +
                        "Payroll Amount: $14,300.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE2 TestLastName2\n" +
                        "Paycheck Amount: $14,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE3 TestLastName3\n" +
                        "Paycheck Amount: $150.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE4 TestLastName4\n" +
                        "Paycheck Amount: $150.00\n" +
                        "\n"
                , details);

        Application.commitUnitOfWork();
    }

    @Test
    public void testEEsPaidToSameBankAccount_MultipleEvents() {
        setSourceParamsToDefault();
        Application.beginUnitOfWork();

        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.persistEmployee2BankAccount2();
        c1dl.persistEmployee3BankAccount1();
        c1dl.persistEmployee4BankAccount1();
        c1dl.persistEmployeeBankAccount();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_SameBASameCheckDate(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollRunDTO thisPayrollRunDTO = c1dl.getCompany1PR_MultiplePaycheckSplitsDifferentBA(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        //this one should not produce any events
        PayrollServices.beginUnitOfWork();
        employeesPaidToSameBankAccountOnLastPayroll(Company.findCompany("1234567", SourceSystemCode.QBOE));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "1234567", new SpcfMoney("100000.00"), new SpcfMoney("50000.00"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult submitDDPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", thisPayrollRunDTO);
        Application.commitUnitOfWork();
        assertTrue(submitDDPayroll.isSuccess());

        PayrollServices.beginUnitOfWork();
        // simulate fraud checks
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        employeesPaidToSameBankAccountOnLastPayroll(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // simulate fraud checks
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        //test results
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeesPaidToSameBankAccount, null, null, null);

        assertEquals("Company Events", 2, companyEventsList.size());


        String detail1 = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.Details);
        String detail2 = companyEventsList.get(1).getCompanyEventDetailValue(EventDetailTypeCode.Details);

        //which is which? sort by the text, (might as well)
        if (detail1.compareTo(detail2) > 0) {
            String tmp = detail1;
            detail1 = detail2;
            detail2 = tmp;
        }

        Assert.assertEquals("Company Event Details 1:",
                "More than 3 unique employees paid into a single bank account on the same day \n" +
                        "\n" +
                        "Bank Name: abc bank\n" +
                        "Bank Routing: 111000025\n" +
                        "Account Type: Checking\n" +
                        "Bank Account: 12345\n" +
                        "\n" +
                        "Source Payroll Id: BatchTest00\n" +
                        "Payroll Date: October 02, 2007 12:00 AM\n" +
                        "Payroll Amount: $19,750.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE1 TestLastName\n" +
                        "Paycheck Amount: $15,000.00\n" +
                        "\n" +
                        "Source Payroll Id: BatchTest02\n" +
                        "Payroll Date: October 02, 2007 12:00 AM\n" +
                        "Payroll Amount: $14,300.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE2 TestLastName2\n" +
                        "Paycheck Amount: $14,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE3 TestLastName3\n" +
                        "Paycheck Amount: $150.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE4 TestLastName4\n" +
                        "Paycheck Amount: $150.00\n" +
                        "\n", detail1);

        Assert.assertEquals("Company Event Details 2:",
                "More than 3 unique employees paid into a single bank account on the same day \n" +
                        "\n" +
                        "Bank Name: abc bank2\n" +
                        "Bank Routing: 111000025\n" +
                        "Account Type: Checking\n" +
                        "Bank Account: 11111\n" +
                        "\n" +
                        "Source Payroll Id: BatchTest00\n" +
                        "Payroll Date: October 02, 2007 12:00 AM\n" +
                        "Payroll Amount: $19,750.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE5 TestLastName5\n" +
                        "Paycheck Amount: $1,000.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE6 TestLastName6\n" +
                        "Paycheck Amount: $1,100.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE7 TestLastName7\n" +
                        "Paycheck Amount: $1,200.00\n" +
                        "\n" +
                        "Employee Name: FirstNameOfEE8 TestLastName8\n" +
                        "Paycheck Amount: $1,300.00\n" +
                        "\n", detail2);

        Application.commitUnitOfWork();
    }

    @Test
    public void testEEsPaidToSameBankAccount_2EEBAOneNullDTO() {
        setSourceParamsToDefault();
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.persistEmployee2BankAccount2();
        c1dl.persistEmployee3BankAccount1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_OneBAThreeEEsOneNull(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "1234567", new SpcfMoney("100000.00"), new SpcfMoney("50000.00"));
        PayrollServices.commitUnitOfWork();


        Application.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        ProcessResult submitDDPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        Application.commitUnitOfWork();
        assertTrue(submitDDPayroll.isSuccess());

        Application.beginUnitOfWork();
        // simulate fraud checks
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        employeesPaidToSameBankAccountOnLastPayroll(company);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeesPaidToSameBankAccount, null, null, null);

        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();
    }

    private void employeesPaidToSameBankAccountOnLastPayroll(Company company) {
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        payrollRuns = payrollRuns.sort(PayrollRun.PayrollRunDate().Descending());
        payrollRuns.get(0).employeesPaidToTheSameBankAccount();
    }
    //end same bank account tests

    private void setSourceParamsToDefault() {
        PayrollServices.beginUnitOfWork();

        FraudRule fraudRule = Application.find(FraudRule.class, FraudRule.SourceSystemCd().equalTo(SourceSystemCode.QBOE)).getFirst();
        FraudValue fraudValue = fraudRule.findFraudValueByName(FraudValueType.FraudPREmployeesSameBankAccountMax);

        fraudValue.setValue("3");
        Application.save(fraudValue);

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testCurrentPayrollPercentageIncrease2() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount1 = new BigDecimal("180.00");
        BigDecimal amount2 = new BigDecimal("270.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    if (i == 1) {
                        currDDTxn.setDDTransactionAmount(amount1);
                    } else {
                        currDDTxn.setDDTransactionAmount(amount2);
                    }
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
        }

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testCurrentPayrollPercentageIncrease3() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount1 = new BigDecimal("180.00");
        BigDecimal amount2 = new BigDecimal("270.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    if (i == 0) {
                        currDDTxn.setDDTransactionAmount(amount1);
                    } else {
                        currDDTxn.setDDTransactionAmount(amount2);
                    }
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
        }

        PayrollServices.commitUnitOfWork();
    }

    private static void persistQBDTCompany(int pNumberOfDays, BigDecimal pAmount) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        c3dl = new Company3Dataloader();
        c3dl.persistCompany3();
        //set up a company with 2 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(pNumberOfDays);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(pNumberOfDays);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(pAmount);
                }

                SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
                for (DDTransactionDTO currDDTxn : currPaycheck.getDdTransactions()) {
                    SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                    totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
                }
                currPaycheck.setPaycheckNetAmount(totalPaycheckNetAmount);
            }

            c3dl.persistPayrollRun(currentPayrollRunDTO);
        }
        PayrollServices.commitUnitOfWork();
    }


    /**
     * Test case to test the SingleEmployeePercentage Increase fraud control with multiple paychecks for same employee
     * in the current payroll.
     */
    @Test
    public void testSingleEmployeePercentageIncrease_MeetTheCriteriaForPriorPayrollWithMultiplePaychecks() {

        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-11-08"));
        Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
        BigDecimal amount = new BigDecimal("450.00");
        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                currDDTxn.setDDTransactionAmount(amount);
            }
        }

        c1DL.persistPayrollRun(currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        //c1DL.persistEmployee3BankAccount1();
        currentPayrollRunDTO = c1DL.getCompany1PR_MultiplePaychecksSameEE_DoesNotExceedLimits(new DateDTO("2007-11-14"));
        c1DL.persistPayrollRun(currentPayrollRunDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest06");
        payrollRun.singleEmployeePercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.SingleEmployeePercentageIncrease, null, null, null);

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 1, companyEventsList.size());

        String note1 = "Employee FirstNameOfEE1 TestLastName has a combined amount of 750.00 for the settlement date of 11/29/2007 which is over 50% more than in the prior payroll.";

        for (CompanyEvent companyEvent : companyEventsList) {
            String payrollRunId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
            payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

            assertEquals("Payroll Run Id ", "BatchTest06", payrollRun.getSourcePayRunId());

            String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
            Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

            assertEquals("Employee1 Id ", "EE1", employee.getSourceEmployeeId());

            assertEquals("Company Event Detail Value ", note1,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

            assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test case to test the SingleEmployeePercentage Increase fraud control for not meeting the criteria
     * with multiple paychecks for same employee in the prior payroll.
     */
    @Test
    public void testSingleEmployeePercentageIncrease_WithMultiplePaychecks_DoesNotMeetCriteria() {

        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_MultiplePaychecksSameEE_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(currentPayrollRunDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-11-02"));
        Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
        BigDecimal amount = new BigDecimal("650.00");
        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                currDDTxn.setDDTransactionAmount(amount);
            }
        }

        c1DL.persistPayrollRun(currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        payrollRun.singleEmployeePercentageIncrease();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.SingleEmployeePercentageIncrease, null, null, null);
        PayrollServices.commitUnitOfWork();

        //Assertion for SingleEmployeePercentageIncrease Event
        assertEquals("Company Events", 0, companyEventsList.size());
    }

    @Test
    public void testEmployeePaidMultipleTimes() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 3 payrolls
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        BigDecimal amount = new BigDecimal("180.00");
        for (int i = 0; i < 3; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(1);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(1);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(amount);
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            BigDecimal amount1 = amount.multiply(new BigDecimal(i + 1));
            amount = amount.add(amount1);
        }

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
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        payrollRuns = payrollRuns.sort(PayrollRun.PayrollRunDate().Descending());
        payrollRuns.get(0).employeePaidTooManyTimes();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidTooManyTimes, null, null, null);

        //Assertion for EmployeePaidTooManyTimes Event
        assertEquals("Company Events", 2, companyEventsList.size());

        CompanyEvent event1 = null;
        CompanyEvent event2 = null;

        for (CompanyEvent companyEvent : companyEventsList) {
            String employeeId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
            Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

            if (employee.getSourceEmployeeId().equals("EE1")) {
                event1 = companyEvent;
            }

            if (employee.getSourceEmployeeId().equals("EE2")) {
                event2 = companyEvent;
            }
        }

        String note1 = "EE FirstNameOfEE1:TestLastName paid 3 times in a period of 5 days.";
        String note2 = "EE FirstNameOfEE2:TestLastName2 paid 3 times in a period of 5 days.";

        assertEquals("Company Event Detail Value ", note1,
                event1.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event1.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));


        assertEquals("Company Event Detail Value ", note2,
                event2.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event2.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        Application.commitUnitOfWork();
    }

    @Test
    public void testCurrentPayrollPercentageIncrease1() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1DL.persistEmployeesAndBankAccounts();

        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 10, 23, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        ArrayList<PayrollRunDTO> payrollRunDTOs = c1DL.getCompany1PR_MultiplePaycheckSplitsDifferentBA1(new DateDTO("2007-10-31"), 1);
        for (PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            c1DL.persistPayrollRun(payrollRunDTO);
        }

        testTime = SpcfCalendar.createInstance(2007, 11, 1, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        //set up a company with 3 payrolls with different paycheck dates
        payrollRunDTOs = c1DL.getCompany1PR_MultiplePaycheckSplitsDifferentBA1(new DateDTO("2007-11-06"), 5);

        for (int i = payrollRunDTOs.size() - 1; i >= 0; i--) {
            PayrollRunDTO payrollRunDTO = payrollRunDTOs.get(i);
            Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionAmount(new BigDecimal("180.00"));
                }

                SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
                for (DDTransactionDTO currDDTxn : currPaycheck.getDdTransactions()) {
                    SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                    totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
                }
                currPaycheck.setPaycheckNetAmount(totalPaycheckNetAmount);
            }

            c1DL.persistPayrollRun(payrollRunDTO);

        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEmployeePaidXPercentGreaterThanOtherEEs() {

        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));

        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        BigDecimal amount = new BigDecimal("0.00");
        for (PaycheckDTO currPaycheck : payChecks) {
            if (currPaycheck.getEmployeeId().equals("EE1")) {
                DomainEntitySet<Employee> employees = PayrollServices.entityFinder.find(Employee.class, Employee.SourceEmployeeId().equalTo(currPaycheck.getEmployeeId()));
                Employee employee = employees.get(0);

                employee.setStatusEffectiveDate(SpcfCalendar.createInstance(2002, 9, 14));
                Application.save(employee);
                amount = new BigDecimal("1000.00");
            } else {
                amount = new BigDecimal("2000.00");
            }

            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
            }
        }
        c1DL.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071004000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-04-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071004");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Complete);
        payrollRuns.get(0).employeePaidPercentageGreaterThanOthers();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Completed Payrolls", 1, payrollRuns.size());
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.EmployeePaidPercentageGreaterThanOthers, null, null, null);


        String employee2Note = "EE FirstNameOfEE2:TestLastName2 paid over 50.00% more than any other EE in payroll with check date 10/02/2007";

        //Assertion for EmployeePaidGreaterThanMax Event
        assertEquals("Company Events", 1, companyEventsList.size());


        CompanyEvent employee2Event = companyEventsList.get(0);


        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRuns.get(0).getSourcePayRunId(), payrollRun.getSourcePayRunId());


        String employeeId = employee2Event.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
        Employee employee2 = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));

        junit.framework.Assert.assertEquals("Employee2 Id ", c1DL.getEmployee2(company).getSourceEmployeeId(), employee2.getSourceEmployeeId());

        assertEquals("Company Event Detail Value ", employee2Note,
                employee2Event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                employee2Event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        PayrollServices.commitUnitOfWork();
    }

    private void persistXPayrollsForCompany3(int count, Company3Dataloader c3dL) {
        PayrollServices.beginUnitOfWork();

        //set up a company with 3 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.commitUnitOfWork();
        BigDecimal amount1 = new BigDecimal("180.00");
        BigDecimal amount2 = new BigDecimal("270.00");
        for (int i = 0; i < count; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3dL.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(7);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(7);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    if (i == 0) {
                        currDDTxn.setDDTransactionAmount(amount1);
                    } else {
                        currDDTxn.setDDTransactionAmount(amount2);
                    }
                }
            }

            PayrollRun pr = c3dL.persistPayrollRun(currentPayrollRunDTO);
            pr.setPayrollRunStatus(PayrollStatus.Complete);
            Application.save(pr);
            PayrollServices.commitUnitOfWork();
        }


    }

    @Test
    public void testPayeePaidGreaterThanMax() {

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPayeePaidMax);
        SpcfDecimal billPaymentSplitAmount = new SpcfMoney(fraudValue.getValue()).add(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Pending);
        payrollRuns.get(0).payeePaidGreaterThanMax();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.PayeePaidGreaterThanMax, null, null, null);
        assertEquals("Company Events", 1, companyEventsList.size());
        String note = "Payee Payee1 Name. paid " + billPaymentAmount.toString() + " in a single payment with date 09/12/2007";

        CompanyEvent payeeEvent = companyEventsList.get(0);

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRuns.get(0).getSourcePayRunId(), payrollRun.getSourcePayRunId());

        String payeeId = payeeEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeId);
        Payee payee = PayrollServices.entityFinder.findById(Payee.class, SpcfUniqueId.createInstance(payeeId));

        junit.framework.Assert.assertEquals("Payee Id ", "Payee1", payee.getSourcePayeeId());

        assertEquals("Company Event Detail Value ", note,
                payeeEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                payeeEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        PayrollServices.commitUnitOfWork();

    }


    @Test
    public void testTotalBillPaymentSubmissionExceedsLimit() {

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        LimitValue spp = LimitRule.findLimitRule(company, ServiceCode.BillPayment).findLimitValueByName(LimitValueType.DefaultEmployeeLimit);
        String oldValue = spp.getValue();
        spp.setValue("500000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudBPMax);
        SpcfDecimal billPaymentSplitAmount = new SpcfMoney(fraudValue.getValue()).add(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Pending);
        payrollRuns.get(0).totalBillPaymentSubmissionExceedsLimit();
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.TotalBillPaymentExceedsLimit, null, null, null);

        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent event = companyEventsList.get(0);

        String payrollRunId = event.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRuns.get(0).getSourcePayRunId(), payrollRun.getSourcePayRunId());

        String note = "Company ran a total bill payment submission of amount " + billPaymentAmount.toString() + " for date 09/12/2007";
        assertEquals("Company Event Detail Value ", note,
                event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        spp = LimitRule.findLimitRule(company, ServiceCode.BillPayment).findLimitValueByName(LimitValueType.DefaultEmployeeLimit);
        spp.setValue(oldValue);
        Application.save(spp);
        Application.commitUnitOfWork();

    }

    @Test
    public void testPayeePaidMultipleTimes() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        SpcfCalendar paymentDate = SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone());

        for (int i = 0; i < 8; i++) {
            Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
            BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO(paymentDate), 2);
            billPaymentDTOs.add(billPaymentDTO);
            ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
            paymentDate.addDays(1);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        payrollRuns = payrollRuns.sort(PayrollRun.PayrollRunDate().Descending());
        payrollRuns.get(0).payeePaidTooManyTimes();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.PayeePaidTooManyTimes, null, null, null);

        //Assertion for PayeePaidTooManyTimes Event
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent event = companyEventsList.get(0);
        String note = "Payee Payee1 Name. paid 8 times in a period of 3 days.";


        assertEquals("Company Event Detail Value ", note,
                event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        Application.commitUnitOfWork();
    }

    @Test
    public void testPayrollProcessedTooSoon_BP() {
        DataLoadServices.setPSPDate(2007, 8, 31);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment);

        String psID = company.getSourceCompanyId();
        List<Payee> payee = DataLoadServices.addPayees(company, 4);
        CompanyBankAccount companyBankAccount = DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psID, SourceSystemCode.QBDT);
        DomainEntitySet< CompanyBankAccount> cba = CompanyBankAccount.findCompanyBankAccounts(company);
        companyBankAccount = cba.get(0);
        companyBankAccount.addVerificationTransaction();
        companyBankAccount.addVerificationTransaction();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        TransactionState completedTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Completed);
        for(FinancialTransaction fTransaction :verificationTransactions ) {
            fTransaction.setCurrentTransactionState(completedTransactionState);
        }
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        SpcfCalendar paymentDate = SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone());
        for (int i=0; i<4 ; i++) {
            Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
            BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment(payee.get(i).getSourcePayeeId(), new DateDTO(paymentDate), 1);
            billPaymentDTO.setAmount(new SpcfMoney("10020.00"));
            billPaymentDTOs.add(billPaymentDTO);
            PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);

        }
        paymentDate.addDays(1);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        payrollRuns = payrollRuns.sort(PayrollRun.PayrollRunDate().Descending());
        payrollRuns.get(0).payrollProcessedTooSoon();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.PayrollProcessedTooSoon, null, null, null);

        //Assertion for PayeePaidTooManyTimes Event
        CompanyEvent event = assertOne(companyEventsList);
        assertEquals("Company Event Detail Value ", "Company ran 3 payrolls of amount greater than 10000.00 with in 3 days of test verification debit date  of 09/04/2007",
                event.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category Detail Value ", EnumUtils.getReadableName(FraudEventCategory.Payroll),
                event.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        Application.commitUnitOfWork();
    }

    @Test
    public void testInactiveCompanySubmitFraudPayrollBP() {

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        Payee payee = assertOne(DataLoadServices.addPayees(company, 1));
        PayrollServices.beginUnitOfWork();

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment(payee.getSourcePayeeId(), new DateDTO("2007-09-10"), 2);
        SpcfDecimal billPaymentSplitAmount = SpcfDecimal.createInstance(5001.00);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        PayeeBankAccountDTO payeeBankAccountDTO = PayrollServices.dtoFactory.create(Application.refresh(payee).getPayeeBankAccountCollection().getFirst());
        split.setPayeeBankAccount(payeeBankAccountDTO);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertTrue("first payroll submit", submitResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // wait for inactivity period
        PayrollServices.beginUnitOfWork();
        SpcfCalendar newDate = PSPDate.getPSPTime();
        company = Application.refresh(company);
        payee = Application.refresh(payee);
        newDate.addDays(Integer.parseInt(FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudBPInactivityDays).getValue()));
        PSPDate.setPSPTime(newDate);
        PayrollServices.commitUnitOfWork();

        // submit a payroll greater than the inactivity fraud amount
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        SpcfCalendar paymentDate = PSPDate.getPSPTime();
        paymentDate.addDays(2);
        billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO(paymentDate), 2);
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudBPInactivityPayrollAmount);
        billPaymentSplitAmount = new SpcfMoney(fraudValue.getValue());
        billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        split = billPaymentDTO.getPaymentTransactions().iterator().next();
        billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));
        PayeeBankAccountDTO payeeBankAccountDTO1 = PayrollServices.dtoFactory.create(Application.refresh(payee).getPayeeBankAccountCollection().getFirst());
        split.setPayeeBankAccount(payeeBankAccountDTO1);
        DomainEntitySet<PayeeBankAccount> pBA = Application.find(PayeeBankAccount.class);
        SpcfCalendar accountUpdatedDate = PSPDate.getPSPTime();
        accountUpdatedDate.addDays(1);
        pBA.get(0).setEffectiveDate(accountUpdatedDate);
        submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertTrue("second payroll submit", submitResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // run fraudulent payroll batch job
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertTrue("Company Fraud Flag ", company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.InactivityBPPayrollAmountExceeded, null, null, null);
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent fraudEvent = companyEventsList.get(0);

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));
        assertNotNull("Payroll Run ", payrollRun);

        assertNotNull("Company Event Detail Value ", fraudEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category", EnumUtils.getReadableName(FraudEventCategory.Payroll), fraudEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testPayeePaidGreaterThanXForAccountUpdate() {

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        Payee payee = assertOne(DataLoadServices.addPayees(company, 1));
        PayrollServices.beginUnitOfWork();

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment(payee.getSourcePayeeId(), new DateDTO("2007-09-10"), 2);
        SpcfDecimal billPaymentSplitAmount = SpcfDecimal.createInstance(5001.00);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        PayeeBankAccountDTO payeeBankAccountDTO = PayrollServices.dtoFactory.create(Application.refresh(payee).getPayeeBankAccountCollection().getFirst());
        split.setPayeeBankAccount(payeeBankAccountDTO);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertTrue("first payroll submit", submitResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // wait for inactivity period
        PayrollServices.beginUnitOfWork();
        SpcfCalendar newDate = PSPDate.getPSPTime();
        company = Application.refresh(company);
        payee = Application.refresh(payee);
        newDate.addDays(Integer.parseInt(FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudBPInactivityDays).getValue()));
        PSPDate.setPSPTime(newDate);
        PayrollServices.commitUnitOfWork();

        // submit a payroll greater than the inactivity fraud amount
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        SpcfCalendar paymentDate = PSPDate.getPSPTime();
        paymentDate.addDays(2);
        billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO(paymentDate), 2);
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudBPInactivityPayrollAmount);
        billPaymentSplitAmount = new SpcfMoney(fraudValue.getValue());
        billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        split = billPaymentDTO.getPaymentTransactions().iterator().next();
        billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));
        PayeeBankAccountDTO payeeBankAccountDTO1 = PayrollServices.dtoFactory.create(Application.refresh(payee).getPayeeBankAccountCollection().getFirst());
        split.setPayeeBankAccount(payeeBankAccountDTO1);
        DomainEntitySet<PayeeBankAccount> pBA = Application.find(PayeeBankAccount.class);
        SpcfCalendar accountUpdatedDate = PSPDate.getPSPTime();
        accountUpdatedDate.addDays(1);
        pBA.get(0).setEffectiveDate(accountUpdatedDate);
        submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertTrue("second payroll submit", submitResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // run fraudulent payroll batch job
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertTrue("Company Fraud Flag ", company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.InactivityBPPayrollAmountExceeded, null, null, null);
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent fraudEvent = companyEventsList.get(0);

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));
        assertNotNull("Payroll Run ", payrollRun);

        assertNotNull("Company Event Detail Value ", fraudEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        assertEquals("FraudEvent Category", EnumUtils.getReadableName(FraudEventCategory.Payroll), fraudEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testInactiveCompanySubmitFraudPayrollBP_CompanyNotOnServiceLongEnough() {

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        SpcfDecimal billPaymentSplitAmount = SpcfDecimal.createInstance(10000.00);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertTrue("first payroll submit", submitResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // run fraudulent payroll batch job
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertTrue("Company Fraud Flag ", company.getIsFlaggedForFraud());
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.InactivityBPPayrollAmountExceeded, null, null, null);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testFraudForPayrollWithBothDDAndNonDDPaychecks(){
        DataLoadServices.setPSPDate(2007, 9, 4);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Tax, ServiceCode.DirectDeposit);

        List<EmployeeDTO> employeeDTOs = DataLoadServices.createEEs(1, true);
        DataLoadServices.addEE(company, employeeDTOs.get(0));
        DataLoadServices.addEEBankAccounts(company);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollRunDTO ddPayrollRun = DataLoadServices.createDDPayrollRun(company, new DateDTO("2007-10-02"));

        Collection<PaycheckDTO> paychecks = ddPayrollRun.getPaychecks();
        DomainEntitySet<Employee> employeesForDD = company.getEmployees();

        //Creating a paycheck without a paychecksplit (or a non-DD paycheck)
        PaycheckDTO paycheckDTO = DataLoadServices.createPaycheckDTO(new ArrayList<DDTransactionDTO>(), employeesForDD.getFirst().getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString());
        paycheckDTO.setPaycheckNetAmount(new SpcfMoney("10001.00"));
        paychecks.add(paycheckDTO);

        for(PaycheckDTO paycheckDTO1 : paychecks ) {
            for (DDTransactionDTO ddTransactionDTO : paycheckDTO1.getDdTransactions()) {
                ddTransactionDTO.setDDTransactionAmount(new BigDecimal(10001));
            }

        }

        ddPayrollRun.setPaychecks(paychecks);

        DataLoadServices.addAssistedBankAccounts(company, ddPayrollRun);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), ddPayrollRun);
        PayrollServicesTest.assertSuccess("submit payroll", processResult);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollRun payrollRun = PayrollRun.findLatestCompanyPayrollRun(company);
        Assert.assertNotNull(payrollRun);
        payrollRun.employeePaidEvenDollarAmount();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeePaidEvenDollarAmount, null, null, null);
        //Assertion for EmployeePaidEvenDollarAmount Event
        assertEquals("Company Events", 2, companyEventsList.size());

        String payrollRunId = companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId);
        PayrollRun payrollRunFromEventList = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

        assertEquals("Payroll Run Id ", payrollRun.getSourcePayRunId(), payrollRunFromEventList.getSourcePayRunId());
        Application.rollbackUnitOfWork();


        /**
         * Scenario where only non-DD paychecks are present in the PayrollRun. The Fraud Flag should not be set
         */

        DataLoadServices.setPSPDate(2007, 9, 5);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollRunDTO nonDDPayrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO("2007-10-03"));
        company.setIsFlaggedForFraud(false);
        Collection<PaycheckDTO> nonDDPaychecks = new ArrayList<PaycheckDTO>();
        DomainEntitySet<Employee> employees = company.getEmployees();
        //Creating a paycheck without a paychecksplit (or a non-DD paycheck)
        PaycheckDTO nonDDPaycheckDTO = DataLoadServices.createPaycheckDTO(new ArrayList<DDTransactionDTO>(), employees.getFirst().getSourceEmployeeId(),
                SpcfUniqueId.createInstance(true).toString());
        nonDDPaychecks.add(nonDDPaycheckDTO);
        nonDDPayrollRunDTO.setPaychecks(nonDDPaychecks);
        DataLoadServices.addAssistedBankAccounts(company, nonDDPayrollRunDTO);
        ProcessResult<PayrollRun> processResultForNonDD = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), nonDDPayrollRunDTO);
        PayrollServicesTest.assertSuccess("submit payroll", processResultForNonDD);
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResultForNonDD);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollRun nonDDPayrollRun =  PayrollRun.findLatestCompanyPayrollRun(company);
        Assert.assertNotNull(nonDDPayrollRun);
        nonDDPayrollRun.employeePaidEvenDollarAmount();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", false, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsListNonDD = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeePaidEvenDollarAmount, CompanyEventStatus.Active, nonDDPayrollRun.getPayrollRunDate(), nonDDPayrollRun.getPayrollRunDate());

        //Assertion for EmployeePaidEvenDollarAmount Event
        assertEquals("Company Events", 0, companyEventsListNonDD.size());

        Application.commitUnitOfWork();
    }

}
