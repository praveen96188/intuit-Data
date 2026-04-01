package com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import org.junit.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 2, 2008
 * Time: 4:46:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestProcessMissedPayrolls {


    private static Company1Dataloader c1dl;
    private static Company2Dataloader c2dl;
    private static Company3Dataloader c3dl;

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        c2dl = new Company2Dataloader();
        c3dl = new Company3Dataloader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        loadDataHappyPath();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Methods to load data
     */
    private static void loadDataHappyPath() {
        PSPDate.setPSPTime("20070904000000");
        persistCompany1();
    }

    private static void persistCompany1() {
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
    }

    /**
     * Test Case to Cancel the Missied Payrolls for a single company
     */
    @Test
    public void testHappyPath_ForSingleCompany() {
        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "1234567",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are cancelled
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<TransactionResponse> txnResponseList;

        //verify the transaction response was created correctly for this txn
        txnResponseList = TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 3);
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Canceled);

        Assert.assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(c1dl.getCompany(),
                EventTypeCode.PayrollCancelled, CompanyEventStatus.Active, null, null);

        PayrollServices.commitUnitOfWork();

        //Assertion for Payroll Cancelled Event
        assertEquals("Payroll Cancelled Event", 1, companyEventsList.size());
    }

    /**
     * Test Case to Cancel the Missied Payrolls for a single company
     */
    @Test
    public void testHappyPath_OffloadedDebit() {

        //Offload EmployerDebit Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "1234567",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are cancelled
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<TransactionResponse> txnResponseList;

        //verify the transaction response was created correctly for this txn
        txnResponseList = TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 2);
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Canceled);

        Assert.assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(c1dl.getCompany(),
                EventTypeCode.PayrollCancelled, CompanyEventStatus.Active, null, null);

        PayrollServices.commitUnitOfWork();

        //Assertion for Payroll Cancelled Event
        assertEquals("Payroll Cancelled Event", 1, companyEventsList.size());
    }

    /**
     * Test Case to Cancel the Missied Payrolls for a single company
     */
    @Test
    public void testInvalidOffloadDate() {
        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        try{
            process.process("20070928");
        }catch(Exception ex){
            SpcfCalendar currentDate = PSPDate.getPSPTime().toLocal();
            CalendarUtils.clearTime(currentDate);
            assertEquals("Exception Message ",
                         "Invalid processing date specified: 20070928 (must be <= " + currentDate.format("yyyyMMdd") + ")",
                         ex.getMessage());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Test Case to Cancel the Missied Payrolls for a single company
     */
    @Test
    public void testProcessForCancelledTransactions() {
        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "1234567",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        FinancialTransaction finTxn = payroll1FinTxns.get(0);
        finTxn.cancelFinancialTransaction();
        PayrollServices.commitUnitOfWork();

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are cancelled
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", 3 , payroll1FinTxns.size());
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Canceled);
    }

    /**
     * Test Case to Cancel the Missied Payrolls
     */
    @Test
    public void testProcessForCancelledEmployerTransaction() {
        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "1234567",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        FinancialTransaction finTxn = payroll1FinTxns.get(0);
        finTxn.cancelFinancialTransaction();
        PayrollServices.commitUnitOfWork();

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are cancelled
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", 3 , payroll1FinTxns.size());
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Canceled);
    }

    /**
     * Test Case to Cancel the Missied Payrolls for multiple companies
     */
    @Test
    public void testHappyPath_ForMultipleCompanies() {
        PayrollServices.beginUnitOfWork();
        persistCompany2();
        PayrollServices.commitUnitOfWork();

        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "1234567",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070925");
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are cancelled for Company1
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<TransactionResponse> txnResponseList;

        //verify the transaction response was created correctly for this txn
       // txnResponseList = TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", 3, payroll1FinTxns.size());
        assertEquals("Payroll Run Status ",PayrollStatus.Canceled,payrollRun.getPayrollRunStatus() );

        //Assert.assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(c1dl.getCompany(),
                EventTypeCode.PayrollCancelled, CompanyEventStatus.Active, null, null);

        PayrollServices.commitUnitOfWork();

        //Assertion for Payroll Cancelled Event
        assertEquals("Payroll Cancelled Event", 1, companyEventsList.size());

        // verify Payroll Fin txns are cancelled for Company2
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c2dl.getCompany().getSourceCompanyId(),
                c2dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        payroll1FinTxns = payrollRun1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 0);
        assertEquals("Payroll Run Status ", payrollRun1.getPayrollRunStatus(), PayrollStatus.Pending);
    }

    /**
     * Test case to cancel the payroll for QBDT Source System if the Grace Period between Processing Date & Original
     * Initiation Date is >=10 days.
     */
    @Test
    public void testQBDTSourceSystem_CancelPayroll() {
        //Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                "8574536",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071128000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20071128");
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are cancelled for Company3
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<TransactionResponse> txnResponseList;

        //verify the transaction response was created correctly for this txn
        txnResponseList = TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 3);
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Canceled);

        Assert.assertEquals("Transaction response for cancelled transactions", 1, txnResponseList.size());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(c3dl.getCompany(),
                EventTypeCode.PayrollCancelled, CompanyEventStatus.Active, null, null);

        PayrollServices.commitUnitOfWork();

        //Assertion for Payroll Cancelled Event
        assertEquals("Payroll Cancelled Event", 1, companyEventsList.size());
    }

    /**
     * Test case to Send warning email that the payroll will be cancelled in five business days for QBDT Source System
     * if the Grace Period between Processing Date & Original Initiation Date is <10 days for a company on 2-day funding.
     */
    @Test
    public void testQBDT_SendAlertEmail_2Day_Pending_Payroll() {
        // Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        // Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                "8574536",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);
        testQBDTSourceSystem("20071113", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071114", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071115", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071116", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071119", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071120", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071121", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        // skip 20071122 since it is a holiday (Thanksgiving)
        testQBDTSourceSystem("20071123", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071126", TransactionStateCode.Created, PayrollStatus.Pending, 3);

        // Pass the processing date >=10 days
        testQBDTSourceSystem("20071127", TransactionStateCode.Cancelled, PayrollStatus.Canceled, 3);
    }

    /**
     * Test case to Send warning email that the payroll will be cancelled in five business days for QBDT Source System
     * if the Grace Period between Processing Date & Original Initiation Date is <10 days for a company on 5-day funding.
     */
    @Test
    public void testQBDT_SendAlertEmail_5Day_Pending_Payroll() {
        // Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        // set company to 5-day funding
        PayrollServices.beginUnitOfWork();
        FundingModel newFundingModel = PayrollServices.entityFinder.findById(FundingModel.class,
                                                                             FundingModel.Codes.FIVE_DAY);
        ProcessResult<Company> processResult =
                PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT,
                                                                         "8574536",
                                                                         newFundingModel);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompanyFundingModel", processResult);

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        // Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                                                                       "8574536",
                                                                       ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", processResult);

        testQBDTSourceSystem("20071113", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071114", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071115", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071116", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071119", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071120", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071121", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        // skip 20071122 since it is a holiday (Thanksgiving)
        testQBDTSourceSystem("20071123", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071126", TransactionStateCode.Created, PayrollStatus.Pending, 3);

        // Pass the processing date >=10 days
        testQBDTSourceSystem("20071127", TransactionStateCode.Cancelled, PayrollStatus.Canceled, 3);
    }

    /**
     * Test case to Send warning email that the payroll will be cancelled in five business days for QBDT Source System
     * if the Grace Period between Processing Date & Original Initiation Date is <10 days for a company on 5-day funding.
     */
    @Test
    public void testQBDT_SendAlertEmail_OffloadedDebit_Payroll() {
        // Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        // set company to 5-day funding
        PayrollServices.beginUnitOfWork();
        FundingModel newFundingModel = PayrollServices.entityFinder.findById(FundingModel.class,
                                                                             FundingModel.Codes.FIVE_DAY);
        ProcessResult<Company> processResult =
                PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT,
                                                                         "8574536",
                                                                         newFundingModel);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompanyFundingModel", processResult);

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        // offload the employer debit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071113000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                                                                       "8574536",
                                                                       ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", processResult);

        testQBDTSourceSystem("20071114", TransactionStateCode.Created, PayrollStatus.OffloadedDebit, 2);
        testQBDTSourceSystem("20071115", TransactionStateCode.Created, PayrollStatus.OffloadedDebit, 2);
        testQBDTSourceSystem("20071116", TransactionStateCode.Created, PayrollStatus.OffloadedDebit, 2);
        testQBDTSourceSystem("20071119", TransactionStateCode.Created, PayrollStatus.OffloadedDebit, 2);
        testQBDTSourceSystem("20071120", TransactionStateCode.Created, PayrollStatus.OffloadedDebit, 2);
        testQBDTSourceSystem("20071121", TransactionStateCode.Created, PayrollStatus.OffloadedDebit, 2);
        // skip 20071122 since it is a holiday (Thanksgiving)
        testQBDTSourceSystem("20071123", TransactionStateCode.Created, PayrollStatus.OffloadedDebit, 2);
        testQBDTSourceSystem("20071126", TransactionStateCode.Created, PayrollStatus.OffloadedDebit, 2);

        // Pass the processing date >=10 days
        testQBDTSourceSystem("20071127", TransactionStateCode.Cancelled, PayrollStatus.Canceled, 2);
    }

    /**
     * Test Case to Cancel the Missied Payrolls for a OffloadedDebit & Cancelled EmployeeDebit Transactionss
     */
    @Test
    public void testHappyPath_OffloadedDebitAndCancelledEmployeeTransactions() {

        //Offload EmployerDebit Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "1234567",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        PayrollServices.beginUnitOfWork();
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
        dto.setSourcePayrollRunId("BatchTest05");
        dto.setAgentCancel(true);

        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        FinancialTransaction finTxn = payroll1FinTxns.get(0);

        sourcePaycheckIdList.add(finTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        dto.setSourcePaycheckIdList(sourcePaycheckIdList);

        result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), dto);


        PayrollServices.commitUnitOfWork();

        assertSuccess("Transaction Cancel Result  ", result);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are cancelled
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<TransactionResponse> txnResponseList;

        //verify the transaction response was created correctly for this txn
        txnResponseList = TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 2);
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Canceled);

        Assert.assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(c1dl.getCompany(),
                EventTypeCode.PayrollCancelled, CompanyEventStatus.Active, null, null);

        PayrollServices.commitUnitOfWork();

        //Assertion for Payroll Cancelled Event
        assertEquals("Payroll Cancelled Event", 1, companyEventsList.size());
    }

    public void testQBDTSourceSystem(String pProcessingDate,
                                     TransactionStateCode pExpectedTransactionStateCode,
                                     PayrollStatus pExpectedPayrollStatus,
                                     int pExpectedTxnCount) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pProcessingDate + "000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process(pProcessingDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{pExpectedTransactionStateCode});
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of " + pExpectedTransactionStateCode.toString() + " Transactions for Payroll1",
                     pExpectedTxnCount,
                     payroll1FinTxns.size());
        assertEquals("Payroll Run Status ", pExpectedPayrollStatus, payrollRun.getPayrollRunStatus());
    }

    @Test
    public void testQBDTSourceSystem_CancelPayroll1() {
        //Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        //Recall one paycheck
        PayrollServices.beginUnitOfWork();
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        TransactionCancelEEDTO recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("BatchTest87");
        recallDTO.setRequestId("1");
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun
                .findPayrollRun(company, recallDTO.getSourcePayrollRunId());
        PaycheckSplit split = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS2");
        sourcePaycheckIdList.add(split.getPaycheck().getSourcePaycheckId());
        recallDTO.setSourcePaycheckIdList(sourcePaycheckIdList);

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        ProcessResult<TransactionResponse> processResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, "8574536", recallDTO);

        PayrollServices.commitUnitOfWork();
        assertSuccess("Transaction Recall ", processResult);

        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                "8574536",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071128000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20071128");
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are cancelled for Company3
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 4);
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Canceled);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(c3dl.getCompany(),
                EventTypeCode.PayrollCancelled, CompanyEventStatus.Active, null, null);

        PayrollServices.commitUnitOfWork();

        //Assertion for Payroll Cancelled Event
        assertEquals("Payroll Cancelled Event", 1, companyEventsList.size());
    }

    /**
     * Test case to Send warning email that the payroll will be cancelled on the next business day for QBDT Source System
     * if the Grace Period between Processing Date & Original Initiation Date is <10 days.
     */
    private static void persistCompany2() {
        c2dl.persistCompany2();
        PayrollRunDTO payrollRunDTO = c2dl.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO);
    }

    private static void persistCompany3() {
        c3dl.persistCompany3();
    }

    private static void addCompany3Payroll1() {
        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-11-02"));
        c3dl.persistPayrollRun(payrollRunDTO);
    }

    //PSRV001065
    @Test
    public void testQBDTSourceSystem_PSRV001065() {
        //Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071113000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR2_DoesNotExceedLimits(new DateDTO("2007-11-15"));
        c3dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(payroll1FinTxns, "R01",
                "This is an NSF description");
        PayrollServices.commitUnitOfWork();

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            PayrollServices.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071114000000");
        PayrollServices.commitUnitOfWork();

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20071114");
        PayrollServices.commitUnitOfWork();

        //Remove OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBDT,
                "8574536",
                ServiceSubStatusCode.AchRejectR1R9);
        PayrollServices.commitUnitOfWork();

        assertSuccess("RemoveOnHoldReason", result);

        //Add user
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<String> dtoTxnList = new Vector<String>(4);
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
        dto.setSourcePayrollRunId("BatchTest002");

        company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());

        result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), dto);

        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Transaction Cancel Result  ", result);

        // verify Payroll Fin txns are cancelled for Company3
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest002");

        payroll1FinTxns = payrollRun.getFinancialTransactions(
                null,
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 5);
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Canceled);
    }

    @Test
    public void testQBDTSourceSystem_CancelPayroll_PSRV001253() {
        //Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090317000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2009-03-27"));
        c3dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                "8574536",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        //Process Missed Payrolls for 20090317 and make sure that it will not change the MMT initiation Date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090317000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process(null);
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are not cancelled for Company3
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");

        DomainEntitySet<FinancialTransaction> cancelledFinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", cancelledFinTxns.size(), 0);
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Pending);

        //Assertion for the MMT Initiation Date & Original Initiation Date
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for(FinancialTransaction finTxn : payroll1FinTxns){
            MoneyMovementTransaction mmTxn = finTxn.getMoneyMovementTransaction();
            assertEquals("MMT Initiation Date & Original Initiation", mmTxn.getInitiationDate(), mmTxn.getOriginalInitiationDate());
        }

        PayrollServices.commitUnitOfWork();

        //Process Missed Payrolls for 20090327 and make sure that it adds one business day to the MMT initiation Date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090327000000");
        process = new ProcessMissedPayrolls();
        process.process(null);
        PayrollServices.commitUnitOfWork();

        // verify Payroll Fin txns are not cancelled for Company3
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");

        cancelledFinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of cancelled Transactions for Payroll1", cancelledFinTxns.size(), 0);
        assertEquals("Payroll Run Status ", payrollRun.getPayrollRunStatus(), PayrollStatus.Pending);

        //Assertion for the MMT Initiation Date & Original Initiation Date
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                c3dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");

        payroll1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        for(FinancialTransaction finTxn : payroll1FinTxns){
            MoneyMovementTransaction mmTxn = finTxn.getMoneyMovementTransaction();
            assertNotSame("MMT Initiation Date & Original Initiation", mmTxn.getInitiationDate(), mmTxn.getOriginalInitiationDate());
        }
        PayrollServices.commitUnitOfWork();
    }

        /**
     * PSRV001385
     * Test case to successfully ensure MPP works over DST boundaries
     */
        @Test
        public void testDSTBoundaries() {
            // set up company 3
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20090224000000");
            persistCompany3();
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_CrossesDST(new DateDTO("2009-03-10"));
            c3dl.persistPayrollRun(payrollRunDTO);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            TransactionCancelEEDTO txnRecallDTO = new TransactionCancelEEDTO();
            txnRecallDTO.setRequestId("123");
            txnRecallDTO.setSourcePayrollRunId("BatchTest97");
            txnRecallDTO.setTransmissionId(null);
            ArrayList<String> ddTxnIds = new ArrayList<String>();
            ddTxnIds.add("EEBA1PS2");
            txnRecallDTO.setSourcePaycheckIdList(ddTxnIds);
            PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, "8574536", txnRecallDTO);
            PayrollServices.commitUnitOfWork();

            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        }

    /**
     * PSRV001266
     * Test case to successfully process a payroll after the MPP bumps the MMT initiation date(s) and FT settlement date(s).
     */
    @Test
    public void testQBDTSourceSystem_ProcessOffsetPayroll_PSRV001266() {
        // set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        // add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                                                                              c3dl.getCompany().getSourceCompanyId(),
                                                                              ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        // bump mmt init dates past original ft settlement dates
        testQBDTSourceSystem("20071022", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071023", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20071024", TransactionStateCode.Created, PayrollStatus.Pending, 3);

        // set psp date to 10/25 for offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        PayrollServices.commitUnitOfWork();

        // remove OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBDT,
                                                                   c3dl.getCompany().getSourceCompanyId(),
                                                                   ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("removeOnHoldReason", result);

        // offload payroll
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // verify payroll run FT's are executed for Company3
        PayrollServices.beginUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findPayrollRun(c3dl.getCompany(), "BatchTest87");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                null, new TransactionStateCode[]{TransactionStateCode.Executed});

        assertEquals("Number of executed transactions for payroll1", 5, payroll1FinTxns.size());
        assertEquals("Payroll run status", PayrollStatus.OffloadedAll, payrollRun.getPayrollRunStatus());

        for (FinancialTransaction ft : payroll1FinTxns) {
            MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

            // ensure mmt original initiation dates remain unchanged
            assertEquals("Original initiation date", "20071031", mmt.getOriginalInitiationDate().format("yyyyMMdd"));

            // ensure mmt initiation dates got bumped three business days
            assertEquals("Initiation date", "20071031", mmt.getInitiationDate().format("yyyyMMdd"));

            // ensure ft settlement dates got bumped three business days
            switch (ft.getTransactionType().getTransactionTypeCd()) {
                case EmployerDdDebit:
                case EmployerFeeDebit:
                    assertEquals("Settlement date", "20071101", ft.getSettlementDate().format("yyyyMMdd"));
                    break;
                case EmployeeDdCredit:
                    assertEquals("Settlement date", "20071102", ft.getSettlementDate().format("yyyyMMdd"));
                    break;
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    /**
     * PSRV001385
     * Test case to successfully process a payroll after the MPP bumps the MMT initiation date(s) and FT settlement date(s).
     */
    @Test
    public void testQBDTSourceSystem_ProcessOffsetPayroll_PSRV001385() {
        // set up company 3
        PayrollServices.beginUnitOfWork();

        persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090602162000");
        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2009-06-05"));
        c3dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                                                                              c3dl.getCompany().getSourceCompanyId(),
                                                                              ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        // bump mmt init dates past original ft settlement dates
        testQBDTSourceSystem("20090602", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20090603", TransactionStateCode.Created, PayrollStatus.Pending, 3);
        testQBDTSourceSystem("20090604", TransactionStateCode.Created, PayrollStatus.Pending, 3);

        // set psp date to 06/05 for offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090605000000");
        PayrollServices.commitUnitOfWork();

        // remove OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBDT,
                                                                   c3dl.getCompany().getSourceCompanyId(),
                                                                   ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("removeOnHoldReason", result);

        // offload payroll
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // verify payroll run FT's are executed for Company3
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                                                                    c3dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payrollRun.getFinancialTransactions(
                null, new TransactionStateCode[]{TransactionStateCode.Executed});

        assertEquals("Number of executed transactions for payroll1", 5, payroll1FinTxns.size());
        assertEquals("Payroll run status", PayrollStatus.OffloadedAll, payrollRun.getPayrollRunStatus());

        for (FinancialTransaction ft : payroll1FinTxns) {
            MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

            // ensure mmt original initiation dates remain unchanged
            assertEquals("Original initiation date", "20090603", mmt.getOriginalInitiationDate().format("yyyyMMdd"));

            // ensure mmt initiation dates got bumped three business days
            assertEquals("Initiation date", "20090605", mmt.getInitiationDate().format("yyyyMMdd"));

            // ensure ft settlement dates got bumped three business days
            switch (ft.getTransactionType().getTransactionTypeCd()) {
                case EmployerDdDebit:
                case EmployerFeeDebit:
                    assertEquals("Settlement date", "20090608", ft.getSettlementDate().format("yyyyMMdd"));
                    break;
                case EmployeeDdCredit:
                    assertEquals("Settlement date", "20090609", ft.getSettlementDate().format("yyyyMMdd"));
                    break;
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    /**
     * PSRV001390
     * Test case to successfully have the MPP bump the MMT initiation dates for a payroll containing cancelled payroll txns.
     */
    @Test
    public void testQBDTSourceSystem_ProcessOffsetPayroll_PSRV001390() {
        // set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        // cancel a financial transaction to ensure mpp will still bump the dates on the remaining txns.

        PayrollServices.beginUnitOfWork();
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
        dto.setSourcePayrollRunId("BatchTest87");

        PayrollRun payrollRun = PayrollRun.findPayrollRun(c3dl.getCompany(), "BatchTest87");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        FinancialTransaction finTxn = payroll1FinTxns.get(0);

        sourcePaycheckIdList.add(finTxn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        dto.setSourcePaycheckIdList(sourcePaycheckIdList);

        ProcessResult result =
                PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT,
                                                                         c3dl.getCompany().getSourceCompanyId(),
                                                                         dto);

        PayrollServices.commitUnitOfWork();

        assertSuccess("cancelTransaction", result);

        // add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                                                                c3dl.getCompany().getSourceCompanyId(),
                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("addOnHoldReason", result);

        // bump mmt init dates past original ft settlement dates
        testQBDTSourceSystem("20071022", TransactionStateCode.Created, PayrollStatus.Pending, 2);
        testQBDTSourceSystem("20071023", TransactionStateCode.Created, PayrollStatus.Pending, 2);
        testQBDTSourceSystem("20071024", TransactionStateCode.Created, PayrollStatus.Pending, 2);

        // set psp date to 10/25 for offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        PayrollServices.commitUnitOfWork();

        // remove OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBDT,
                                                                   c3dl.getCompany().getSourceCompanyId(),
                                                                   ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("removeOnHoldReason", result);

        // offload payroll
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // verify payroll run FT's are executed for Company3
        PayrollServices.beginUnitOfWork();

        payrollRun = PayrollRun.findPayrollRun(c3dl.getCompany(), "BatchTest87");

        payroll1FinTxns =
                payrollRun.getFinancialTransactions(null, new TransactionStateCode[]{TransactionStateCode.Executed});

        assertEquals("Number of executed transactions for payroll1", 4, payroll1FinTxns.size());
        assertEquals("Payroll run status", PayrollStatus.OffloadedAll, payrollRun.getPayrollRunStatus());

        for (FinancialTransaction ft : payroll1FinTxns) {
            MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

            // ensure mmt original initiation dates remain unchanged
            assertEquals("Original initiation date", "20071031", mmt.getOriginalInitiationDate().format("yyyyMMdd"));

            // ensure mmt initiation dates got bumped three business days
            assertEquals("Initiation date", "20071031", mmt.getInitiationDate().format("yyyyMMdd"));

            // ensure ft settlement dates got bumped three business days
            switch (ft.getTransactionType().getTransactionTypeCd()) {
                case EmployerDdDebit:
                case EmployerFeeDebit:
                    assertEquals("Settlement date", "20071101", ft.getSettlementDate().format("yyyyMMdd"));
                    break;
                case EmployeeDdCredit:
                    assertEquals("Settlement date", "20071102", ft.getSettlementDate().format("yyyyMMdd"));
                    break;
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    /**
     * PSRV003578: Missed Payroll Processor picking up Executed transactions when run from 2nd Offload
     * This is actually a benign error, but because the result set in this scenario tends to be huge (selecting all transactions that were offloaded in the
     * primary offload as well as those that were on-hold) it is causing the job to run very long and could result in an out-of-memory error on large days.
     * This fix simply leaves non-eligible transactions out of the result set by only selecting on-hold transactions to start with.
     */
    @Test
    public void test_mpp_run_before_transaction_states_are_updated_PSRV003578() {
        //
        // Set the PSPDate to 6/1/2007 in preparation for company creation
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1));
        PayrollServices.commitUnitOfWork();

        //
        // Create the company (internally uses a date of 20070601)
        //
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", "987654321", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, "234567891", "198765432", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        //
        // Add an employee
        //
        List<Employee> employeeList1 = DataLoadServices.addEEs(company1, 1, true, false);
        List<Employee> employeeList2 = DataLoadServices.addEEs(company2, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //
        PayrollRunDTO payrollRunDTO1 = DataLoadServices.createDDPayrollRun(company1, new DateDTO(checkDate), employeeList1);
        ProcessResult processResult1 = PayrollServices.payrollManager.submitPayroll(company1.getSourceSystemCd(), company1.getSourceCompanyId(), payrollRunDTO1);
        PSP_PRAssert.assertSuccess("Submit Payroll", processResult1);

        PayrollRunDTO payrollRunDTO2 = DataLoadServices.createDDPayrollRun(company2, new DateDTO(checkDate), employeeList2);
        ProcessResult processResult2 = PayrollServices.payrollManager.submitPayroll(company2.getSourceSystemCd(), company2.getSourceCompanyId(), payrollRunDTO2);
        PSP_PRAssert.assertSuccess("Submit Payroll", processResult2);

        PayrollServices.commitUnitOfWork();

        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 5, 31, SpcfTimeZone.getLocalTimeZone());

        //
        // Add a company hold to one of the companies to keep it from offloading.
        //
        DataLoadServices.addCompanyOnHoldReason(company2, ServiceSubStatusCode.AchRejectR1R9);

        //
        // Confirm the initiation dates of the on-hold MMTs match the intended offload date
        //
        DomainEntitySet<MoneyMovementTransaction> mmtSet = Application.find(MoneyMovementTransaction.class,
                                                                            MoneyMovementTransaction.Company().equalTo(company2).And(
                                                                                    MoneyMovementTransaction.Status().equalTo(PaymentStatus.OnHold)));

        assertEquals("OnHold MMT count", 2, mmtSet.size());

        for (MoneyMovementTransaction mmt : mmtSet) {
            SpcfCalendar initiationDate = mmt.getInitiationDate();
            assertEquals("MMT Initiation Date does not match (before)", offloadDate.format("yyyyMMdd"), initiationDate.format("yyyyMMdd"));
        }

        //
        // Run the query that the MPP runs to ensure it is only selecting on-hold payroll runs
        //
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollsByStatusOnOrBeforeMMTInitiationDate(offloadDate,
                                                                                                             SourceSystemCode.QBDT,
                                                                                                             PayrollStatus.Pending,
                                                                                                             PayrollStatus.OffloadedDebit);
        Application.commitUnitOfWork();

        assertEquals("Number of MPP payroll runs", 1, payrollRuns.size());
        assertEquals("Payroll run for company 2", payrollRuns.get(0).getCompany().getId().toString(), company2.getId().toString());

        //
        // Perform the OffloadAchData step of PrimaryDailyBatchJobs
        //
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(offloadDate);
        Application.commitUnitOfWork();

        //
        // Run the MPP
        //
        PayrollServices.beginUnitOfWork();
        new ProcessMissedPayrolls().process(null);
        Application.commitUnitOfWork();

        //
        // Confirm the initiation dates of the on-hold MMTs were incremented to the next valid offload date
        //
        CalendarUtils.addBusinessDays(offloadDate, 1);

        mmtSet = Application.find(MoneyMovementTransaction.class,
                                  MoneyMovementTransaction.Company().equalTo(company2).And(
                                          MoneyMovementTransaction.Status().equalTo(PaymentStatus.OnHold)));

        assertEquals("OnHold MMT count", 2, mmtSet.size());

        for (MoneyMovementTransaction mmt : mmtSet) {
            SpcfCalendar initiationDate = mmt.getInitiationDate();
            assertEquals("MMT Initiation Date does not match (after)", offloadDate.format("yyyyMMdd"), initiationDate.format("yyyyMMdd"));
        }
    }

    @Test
    public void testAssistedPayrollRun() {
        //
        // Set the PSPDate to 6/1/2007 in preparation for company creation
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1));
        PayrollServices.commitUnitOfWork();

        //
        // Create the company (internally uses a date of 20070601)
        //
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, "234567891", "198765432", false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.activateDDService(company2);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company2);

        //
        // Add an employee
        //

        DataLoadServices.addCompanyLaws(company2, "66", "61", "62", "63", "64", "1");
        List<Employee> employeeList2 = DataLoadServices.addEEs(company2, 1, true, true);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        //
        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        //

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");

        company2 = Company.findCompany(company2.getSourceCompanyId(), company2.getSourceSystemCd());

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company2, payrollRunDTO);

        PayrollRunDTO payrollRunDTO2 = DataLoadServices.createPayrollRun(payrollRunDTO, company2, new DateDTO(checkDate), employeeList2, lawAmounts);
        ProcessResult processResult2 = PayrollServices.payrollManager.submitPayroll(company2.getSourceSystemCd(), company2.getSourceCompanyId(), payrollRunDTO2);
        PSP_PRAssert.assertSuccess("Submit Payroll", processResult2);

        PayrollServices.commitUnitOfWork();

        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 5, 31, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar date941 = SpcfCalendar.createInstance(2007, 6, 7, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar date940 = SpcfCalendar.createInstance(2007, 7, 30, SpcfTimeZone.getLocalTimeZone());

        //
        // Add a company hold to one of the companies to keep it from offloading.
        //
        DataLoadServices.addCompanyOnHoldReason(company2, ServiceSubStatusCode.AchRejectR1R9);

        PayrollServices.beginUnitOfWork();
        //
        // Confirm the initiation dates of the on-hold MMTs match the intended offload date
        //
        DomainEntitySet<MoneyMovementTransaction> mmtSet = Application.find(MoneyMovementTransaction.class,
                                                                            MoneyMovementTransaction.Company().equalTo(company2).And(
                                                                                    MoneyMovementTransaction.Status().equalTo(PaymentStatus.OnHold)));

        assertEquals("OnHold MMT count", 4, mmtSet.size());

        for (MoneyMovementTransaction mmt : mmtSet) {
            SpcfCalendar initiationDate = mmt.getInitiationDate();
            if (mmt.getPaymentTemplate() == null) {
                assertEquals("MMT Initiation Date does not match (before)", offloadDate.format("yyyyMMdd"), initiationDate.format("yyyyMMdd"));
            } else if (mmt.getPaymentTemplate().equals(PaymentTemplate.getIRS_941())) {
                assertEquals("MMT Initiation Date does not match (before)", date941.format("yyyyMMdd"), initiationDate.format("yyyyMMdd"));
            } else if (mmt.getPaymentTemplate().equals(PaymentTemplate.getIRS_940())) {
                assertEquals("MMT Initiation Date does not match (before)", date940.format("yyyyMMdd"), initiationDate.format("yyyyMMdd"));
            }
        }

        //
        // Run the query that the MPP runs to ensure it is only selecting on-hold payroll runs
        //
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollsByStatusOnOrBeforeMMTInitiationDate(offloadDate,
                                                                                                             SourceSystemCode.QBDT,
                                                                                                             PayrollStatus.Pending,
                                                                                                             PayrollStatus.OffloadedDebit);
        Application.commitUnitOfWork();

        assertEquals("Number of MPP payroll runs", 1, payrollRuns.size());
        assertEquals("Payroll run for company 2", payrollRuns.get(0).getCompany().getId().toString(), company2.getId().toString());

        int i = 0;
        while (i < 10) {
            i++;
            //
            // Perform the OffloadAchData step of PrimaryDailyBatchJobs
            //
            Application.beginUnitOfWork();
            PSPDate.setPSPTime(offloadDate);
            Application.commitUnitOfWork();

            //
            // Run the MPP
            //
            PayrollServices.beginUnitOfWork();
            new ProcessMissedPayrolls().process(null);
            Application.commitUnitOfWork();

            //
            // Confirm the initiation dates of the on-hold MMTs were incremented to the next valid offload date
            //
            CalendarUtils.addBusinessDays(offloadDate, 1);

            PayrollServices.beginUnitOfWork();
            mmtSet = Application.find(MoneyMovementTransaction.class,
                                      MoneyMovementTransaction.Company().equalTo(company2).And(
                                              MoneyMovementTransaction.Status().equalTo(PaymentStatus.OnHold)));

            assertEquals("OnHold MMT count", 4, mmtSet.size());

            for (MoneyMovementTransaction mmt : mmtSet) {
                SpcfCalendar initiationDate = mmt.getInitiationDate();
                if (mmt.getPaymentTemplate() == null) {
                    assertEquals("MMT Initiation Date does not match (before)", offloadDate.format("yyyyMMdd"), initiationDate.format("yyyyMMdd"));
                } else if (mmt.getPaymentTemplate().equals(PaymentTemplate.getIRS_941())) {
                    assertEquals("MMT Initiation Date does not match (before)", date941.format("yyyyMMdd"), initiationDate.format("yyyyMMdd"));
                } else if (mmt.getPaymentTemplate().equals(PaymentTemplate.getIRS_940())) {
                    assertEquals("MMT Initiation Date does not match (before)", date940.format("yyyyMMdd"), initiationDate.format("yyyyMMdd"));
                }
            }
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
