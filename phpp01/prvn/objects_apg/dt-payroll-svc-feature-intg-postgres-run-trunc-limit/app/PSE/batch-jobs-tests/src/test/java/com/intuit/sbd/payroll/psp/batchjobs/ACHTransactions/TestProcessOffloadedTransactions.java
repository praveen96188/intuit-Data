package com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Mar 27, 2008
 * Time: 9:00:39 AM
 */
public class TestProcessOffloadedTransactions {

    private static Company1Dataloader c1dl;
    private static Company3Dataloader c3dl;

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        c3dl = new Company3Dataloader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        loadDataHappyPath();
        PayrollServices.commitUnitOfWork();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Test method to verify transactions in not executed state are not processed
     */
    @Test
    public void testDontProcessNotOffloadedTxns() {

        // call the txn process batch process for the date 11-20-2007 with out offloading any Payrolls
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071120");
        PayrollServices.commitUnitOfWork();

        // verify no processing done
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        // verify the payroll status is Complete
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Pending, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is not Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 0);

        // verify the no QBOE company's funding model is changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test method to verify transactions EmployerTaxDebit is Completed after 5 day waiting period
     */
    @Test
    public void testImpoundTransactions() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO("1");
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "1", payrollDTO);
        PayrollRun payrollRun = (PayrollRun) processResult.getResult();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //
        offloadFinancialTransactions("20110105000000");
        //
        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20110120");
        PayrollServices.commitUnitOfWork();
        //
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> payrollFinTxns = PayrollRun.findPayrollRun(payrollRun.getCompany(), payrollRun.getSourcePayRunId()).getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxDebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number of Completed Transactions for Payroll", 1, payrollFinTxns.size());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * offload financial transactions
     *
     * @param date
     */
    private void offloadFinancialTransactions(String date) {
        //
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(date);
        PayrollServices.commitUnitOfWork();
        //
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }

    /**
     * Test method to test processing if no transactions with settlement date before the processing date.
     * Should not process any transaction.
     */
    @Test
    public void testNoTransactionsToProcess() {

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

        // call the txn process batch process for the date 09-28-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify no processing done
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        // verify the payroll status is Complete
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 0);

        // verify the no QBOE company's funding model is changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();
    }


    /**
     * Test method to test successful processing of both QBOE and QBDT offloaded transactions with
     * no returns.
     */
    @Test
    public void testProcessAllSuccessfulOffloadedTxs() {
        //Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
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

        // offload QBDT All
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071113000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071122000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 11-22-2007
        processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071122");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        Company company3 = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                                               c3dl.getCompany().getSourceSystemCd());

        // verify the payroll status is Complete
        PayrollRun payRunC3 = PayrollRun.findPayrollRun(company3, "BatchTest87");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC3.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRunC3.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the no QBOE company's funding model is changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test method to test successful processing of only QBOE offloaded transaction with
     * no returns. We don't offload QBDT Txns so no QBDT txns should be processed.
     */
    @Test
    public void testProcessSuccessfulQBOEOffloadedTxsOnly() {
        //Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        // offload only QBOE txns

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
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        Company company3 = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                                               c3dl.getCompany().getSourceSystemCd());

        // verify the payroll status
        PayrollRun payRunC3 = PayrollRun.findPayrollRun(company3, "BatchTest87");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Pending, payRunC3.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRunC3.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 0);

        // verify the no QBOE company's funding model is changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test method to verify company changes to 2 Day funding model.
     * Should change the funding model of company1 to 2 Day.
     */
    @Test
    public void testChangeFundingModelSuccessful() {

        // add second payroll to company1
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
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

        // offload Company1 Payroll2 QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload Company1 Payroll2 QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-17-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071005");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071017000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071017");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRun2C1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.TWO_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify company changes to 2 Day funding model.
     * Should not change the funding model of company1 to 2 Day, since one of the two payrolls status is not complete.
     */
    @Test
    public void testChangeFundingModelFailure_PayrollStatusIncomplete() {

        // add second payroll to company1
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
        PayrollServices.commitUnitOfWork();

        // offload all payrolls except company1's second payroll

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
        PSPDate.setPSPTime("20071017000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-17-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071017");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Pending, payRun2C1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 0);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test method to verify company changes to 2 Day funding model.
     * Should not change the funding model of company1 to 2 Day, since one of the payrolls net amount
     * is less than MinimumNonSuspectPayrollAmount.
     */
    @Test
    public void testChangeFundingModelFailure_MinSuspectPayrollAmount() {

        // add second payroll to company1 with net amount less than MinimunNonSuspectPayrollAmount
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2_MinSuspectPayrollAmount();
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

        // offload Company1 Payroll2 QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload Company1 Payroll2 QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071017000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-17-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071017");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "Batch01");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRun2C1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify company changes to 2 Day funding model.
     * Should not change the funding model of company1 to 2 Day, since company's DD status is suspended.
     */
    @Test
    public void testChangeFundingModelFailure_CompanyDDStatusSuspended() {

        // add second payroll to company1
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
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

        // offload Company1 Payroll2 QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload Company1 Payroll2 QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Make company1 DD Status Suspended
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());
        CompanyService ddService = CompanyService.findCompanyService(company1, ServiceCode.DirectDeposit);
        PayrollServices.companyManager.addOnHoldReason(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceSubStatusCode.SuspendedDirectDeposit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071017000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-17-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071017");
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRun2C1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify company changes to 2 Day funding model.
     * Should not change the funding model of company1 to 2 Day, since company has active strikes.
     */
    @Test
    public void testChangeFundingModelFailure_CompanyHasActiveStrikes() {

        // add second payroll to company1
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
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

        // offload Company1 Payroll2 QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload Company1 Payroll2 QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // add strike to company1 DD
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyEvent> processResult = PayrollServices.companyManager.addStrikeEvent(
                c1dl.getCompany().getSourceSystemCd(),
                c1dl.getCompany().getSourceCompanyId(),
                "Strike Reason",
                PSPDate.getPSPTime());
        assertEquals("Add Strike", processResult.isSuccess(), true);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071017000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-17-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071017");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRun2C1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify company changes to 2 Day funding model.
     * Should change the funding model of company1 to 2 Day, since company has no active strikes within last one year.
     */
    @Test
    public void testChangeFundingModelSuccess_ActiveStrikesOneYearOld() {

        // add second payroll to company1
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
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

        // offload Company1 Payroll2 QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload Company1 Payroll2 QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // add strike to company1 DD
        PayrollServices.beginUnitOfWork();
        SpcfCalendar strikeDate = PSPDate.getPSPTime();
        strikeDate.addMonths(-1 * 13);
        ProcessResult<CompanyEvent> processResult = PayrollServices.companyManager.addStrikeEvent(
                c1dl.getCompany().getSourceSystemCd(),
                c1dl.getCompany().getSourceCompanyId(),
                "Strike Reason",
                strikeDate);
        assertEquals("Add Strike", processResult.isSuccess(), true);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-17-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071005");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071017000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071017");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRun2C1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.TWO_DAY,
                     company1.getFundingModel().getFundingModelCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify company changes to 2 Day funding model.
     * Should not change the funding model of company1 to 2 Day, since company has ACH ER returns.
     */
    @Test
    public void testChangeFundingModelFailure_CompanyHasAchERReturns() {
        // add second payroll to company1 with net amount less than MinimunNonSuspectPayrollAmount
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
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

        // offload Company1 Payroll2 QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload Company1 Payroll2 QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Create a return for ER DD DB txn in payroll2 of Company1
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c1FinTxns,
                                                                                                "R01",
                                                                                                "This is an NSF description");
        assertEquals("Number of Company1 ERDDDB EX txns", 1, c1FinTxns.size());
        assertEquals("Number of Company1 ERDDDB Returns", 1, returnList.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071017000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-17-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071017");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRun2C1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify company changes to 2 Day funding model.
     * Company has ACH EE Returns
     * Should change the funding model of company1 to 2 Day, since company has no ACH ER returns.
     */
    @Test
    public void testChangeFundingModelSuccess_CompanyHasAchEEReturns() {
        // add second payroll to company1 with net amount less than MinimunNonSuspectPayrollAmount
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
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

        // offload Company1 Payroll2 QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload Company1 Payroll2 QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Create a return for EE DD CR txn in payroll2 of Company1
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c1FinTxns,
                                                                                                "R01",
                                                                                                "This is an EE Return");
        assertEquals("Number of Company1 EEDDCR EX txns", 2, c1FinTxns.size());
        assertEquals("Number of Company1 EEDDCR Returns", 2, returnList.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-17-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071005");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071017000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071017");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());


        payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRun2C1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        payrollFinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.TWO_DAY,
                     company1.getFundingModel().getFundingModelCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to remove the ACH Return On Holds if ER DD REDEBIT is offloaded successfully and
     * company has no debts to Intuit.
     * Shold succeed, since company is not in debt to Intuit
     */
    @Test
    public void testExpireCompanyACHReturnOnHolds() {
        //Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071031000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();
        // add payroll to company3
        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload QBDT All
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071113000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create ACH Returns
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company3 = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                                               c3dl.getCompany().getSourceSystemCd());
        PayrollRun payRunC3 = PayrollRun.findPayrollRun(company3, "BatchTest87");
        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<FinancialTransaction> c3FinTxns = payRunC3.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c3FinTxns,
                                                                                                "R01",
                                                                                                "This is an NSF description");
        assertEquals("Number of Company3 ERDDDB EX txns", 1, c3FinTxns.size());
        assertEquals("Number of Company3 ERDDDB Returns", 1, returnList.size());
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                                                                                 getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        // offload QBOE ER DD REDEBIT
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Make company1 DD Status Inactive
        PayrollServices.beginUnitOfWork();
        company3 = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                                       c3dl.getCompany().getSourceSystemCd());

        CompanyService ddService = CompanyService.findCompanyService(company3, ServiceCode.DirectDeposit);
        //CompanyBE.addOnHoldReason(company3, ServiceSubStatusCode.AchRejectOther);
        ProcessResult<Company> processResult = PayrollServices.companyManager.addOnHoldReason(
                company3.getSourceSystemCd(),
                company3.getSourceCompanyId(),
                ServiceSubStatusCode.SuspendedDirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Process Result ", processResult);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071122000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-05-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071122");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());
        company3 = Company.findCompany(c3dl.getCompany().getSourceCompanyId(),
                                       c3dl.getCompany().getSourceSystemCd());

        payRunC3 = PayrollRun.findPayrollRun(company3, "BatchTest87");
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC3.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.Pending, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 0);

        payrollFinTxns = payRunC3.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit,
                        TransactionTypeCode.EmployerDdRedebit, TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 4);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());
        // verify on holds of ACH Returns are removed
        assertEquals("Company3 status", company3.isCompanyOnHold(), true);
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test to change the company status to Active from On Hold if ER DD REDEBIT is offloaded successfully and
     * company has debts to Intuit.
     * Shold fail, since company is in debt to Intuit
     */
    @Test
    public void testExpireCompanyACHRturnOnHolds_InDebtToIntuit() {

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
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // create an ER return for company1 first payroll
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");
        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c1FinTxns,
                                                                                                "R02",
                                                                                                "This is an ER Return");

        assertEquals("Number of txn returns", 1, returnList.size());

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                                                                                 getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Company1 ERDDDB EX txns", 1, c1FinTxns.size());
        assertEquals("Number of Company1 ERDDDB Returns", 1, returnList.size());

        // Add write off bad debt for ER Tx of company1 payroll1
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                c1dl.getCompany().getSourceSystemCd(),
                c1dl.getCompany().getSourceCompanyId(), "BatchTest05");

        PayrollServices.commitUnitOfWork();
        assertTrue("Process Result", processResult.isSuccess());


        // Make company1 DD Status Inactive
        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());
        CompanyService ddService = CompanyService.findCompanyService(company1, ServiceCode.DirectDeposit);
        PayrollServices.companyManager.addOnHoldReason(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceSubStatusCode.SuspendedDirectDeposit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071016000000");
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 10-16-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071016");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.WrittenOff, payRun1C1.getPayrollRunStatus());

        // verify the financial txns state is Completed

        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit,
                        TransactionTypeCode.EmployerWriteOff},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll", payrollFinTxns.size(), 2);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        // verify company is still inactive
        assertEquals("Company3 status", company1.isCompanyOnHold(), true);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to make sure that the payroll status is not complete if company is in debt to Intuit for the payroll.
     */
    @Test
    public void testPayrollNotCompleted_InDebtToIntuit() {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), c1dl.getCompany().getSourceSystemCd());
        PayrollServices.rollbackUnitOfWork();

        // offload QBOE ER DB
        DataLoadServices.runOffload(company, 2007, 9, 25);

        // offload QBOE EE CR
        DataLoadServices.runOffload(company, 2007, 9, 28);

        // create an ER return for company1 first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        assertEquals("Number of Company1 ERDDDB EX txns", 1, c1FinTxns.size());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R02", "This is an ER Return");

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.DebitReturned, payRun1C1.getPayrollRunStatus());

        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        assertEquals("Number Of Returned Transactions for Payroll", payrollFinTxns.size(), 1);
        FinancialTransaction originalTxn = payrollFinTxns.get(0);
        FinancialTransaction eefinancialTx = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                null).get(0);
        // add a partial redebit
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney(originalTxn.getFinancialTransactionAmount().subtract(eefinancialTx.getFinancialTransactionAmount())));
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);
        ProcessResult<DomainEntitySet<FinancialTransaction>> procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(c1dl.getCompany().getSourceSystemCd(),
                                                                                                                                                            c1dl.getCompany().getSourceCompanyId(),
                                                                                                                                                            redebitCollection);
        PayrollServices.commitUnitOfWork();
        assertSuccess(procResult);
        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        Assert.assertEquals("Payroll status after redebit add:", payRun1C1.getPayrollRunStatus(), PayrollStatus.PendingRedebit);

        // verify the new redebit financial transaction added

        DomainEntitySet<FinancialTransaction> financialTxs = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("Number of Active Redebit transacions", 1, financialTxs.size());
        PayrollServices.commitUnitOfWork();

        // offload the Redebit
        DataLoadServices.runOffload(company, 2007, 10, 2);

        // call the txn process batch process for the date 10-16-2007
        DataLoadServices.runACHTransactionProcessor(5);

        // verify the payroll status and financial transaction status
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        // verify the payroll status is not complete
        Assert.assertEquals("Payroll status after redebit add:", PayrollStatus.DebitReturned, payRun1C1.getPayrollRunStatus());

        // verify the new redebit financial transaction added

        financialTxs = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number of Completed Redebit transacions", 1, financialTxs.size());
        PayrollServices.commitUnitOfWork();

        // Now add the partial redebit for the remaining amount
        PayrollServices.beginUnitOfWork();
        redebitDTO.setAmount(eefinancialTx.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);
        procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(c1dl.getCompany().getSourceSystemCd(),
                                                                                                       c1dl.getCompany().getSourceCompanyId(),
                                                                                                       redebitCollection);
        PayrollServices.commitUnitOfWork();
        assertSuccess(procResult);

        // offload the Redebit
        DataLoadServices.runOffload(company, 2007, 10, 12);

        // call the txn process batch process for the date 10-16-2007
        DataLoadServices.runACHTransactionProcessor(5);

        // verify the payroll status and financial transaction status
        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        // verify the payroll status is complete
        Assert.assertEquals("Payroll status after redebit add:", payRun1C1.getPayrollRunStatus(), PayrollStatus.Complete);

        // verify the new redebit financial transaction added

        financialTxs = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number of Completed Redebit transacions", 2, financialTxs.size());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify ER Refund txn is created.
     * Should create ER Refund Txn.
     */
    @Test
    public void testCreateERRefund_CompanyActive() {
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
        PSPDate.addBusinessDaysToPSPTime(1);
        c1dl.reverseEntirePayroll("BatchTest05");
        PayrollServices.commitUnitOfWork();

        // offload EE DD RV DB
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
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());
        // Verify Refund txns created
        payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdReversalRefundCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("ER Refund Transactions", 2, payrollFinTxns.size());
        // Verify Transaction responses
        for (FinancialTransaction finTxn : payrollFinTxns) {
            DomainEntitySet<TransactionResponse> txnResponse =
                    TransactionResponse.findTransactionResponses(finTxn);
            assertEquals("Transaction Responses", 1, txnResponse.size());
        }
        // Verify Reversal OK System Event
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company1, EventTypeCode.ReversalOK, null, null, null);
        assertEquals("Reversal OK Events", companyEvents.size(), 2);
        CompanyEvent event = companyEvents.get(0);

        assertEquals("Refund Status Reason", null,
                     event.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));

        event = companyEvents.get(1);
        assertEquals("Refund Status Reason", null,
                     event.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify Payroll status is complete if .
     */
    @Test
    public void testIntuitReversal_CompanyNotInDebt() {
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

        // create an ER return for company1 first payroll
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");
        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c1FinTxns,
                                                                                                "R02",
                                                                                                "This is an EE Return");

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                                                                                 getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Company1 ERDDDB EX txns", 1, c1FinTxns.size());
        assertEquals("Number of Company1 ERDDDB Returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        c1dl.reverseEntirePayroll_IntuitInitiated("BatchTest05");
        PayrollServices.commitUnitOfWork();

        // offload EE DD RV DB
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
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.ReversalsFinished, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 2);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());
        // Verify No Refund txns created
        payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdReversalRefundCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("ER Refund Transactions", 0, payrollFinTxns.size());
        PayrollServices.commitUnitOfWork();

        //Create an EE Return transfer for the EE DD Credit that bounced
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // offload IntuitEmployeeReturnTransfer
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071016000000");
        Application.commitUnitOfWork();

        // call the txn process batch process again for the date 10-09-2007
        processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071016");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.IntuitEmployeeReturnTransfer},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Intuit ER Transfers for Payroll1", payrollFinTxns.size(), 1);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify payroll status is ReversalsFinished.
     */
    @Test
    public void testIntuitReversals_CompanyInDebt() {
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

        // create an ER return for company1 first payroll
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");
        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c1FinTxns,
                                                                                                "R02",
                                                                                                "This is an ER Return");

        assertEquals("Number of txn returns", 1, returnList.size());

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                                                                                 getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Company1 ERDDDB EX txns", 1, c1FinTxns.size());
        assertEquals("Number of Company1 ERDDDB Returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        c1dl.reverseEntirePayroll_IntuitInitiated("BatchTest05");
        PayrollServices.commitUnitOfWork();

        // offload EE DD RV DB
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // return one reversal
        PayrollServices.beginUnitOfWork();
        payRun1C1 = Application.findById(PayrollRun.class, payRun1C1.getId());
        DomainEntitySet<FinancialTransaction> reversalTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        returnList = returnsLoader.persistTransactionReturns(reversalTxns, "R01", "This is an EE Debit Return");

        assertEquals("Number of txn returns", 2, returnList.size());

        transactionReturn = returnList.get(0);

        returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.ReversalsFinished, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 2);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());
        // Verify No Refund txns created
        payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdReversalRefundCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("ER Refund Transactions", 0, payrollFinTxns.size());
        PayrollServices.commitUnitOfWork();

        //Create an EE Return transfer for the EE DD Credit that bounced
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // offload IntuitEmployeeReturnTransfer
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071016000000");
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process again for the date 10-09-2007
        processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071016");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());

        payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.ReversalsFinished, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.IntuitEmployeeReturnTransfer},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Intuit ER Transfers for Payroll1", payrollFinTxns.size(), 1);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testDDReversalOKForDD4V() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        String sourceCompanyId = "123272727";
        // 1. company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        //2. Set up bill payments with 2 Payees
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        BillPaymentDTO billPaymentDTO2 = GenerateData.generateBillPayment("Payee2", new DateDTO("2007-09-10"), 1);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        billPaymentDTOs.add(billPaymentDTO2);
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        Collection<PayrollRun> billPaymentResults = submitResult.getResult();
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.BillPaymentReceived);
        assertEquals("BillPaymentReceived event count", 1, events.size());
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

        // 3. offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // 4. Return the offloaded transactions as Non Sufficient Funds
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun1 = billPaymentResults.toArray(new PayrollRun[billPaymentResults.size()])[0];
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRun1.getSourcePayRunId());
        DomainEntitySet<FinancialTransaction> offLoadedTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070911000000");
        PayrollServices.commitUnitOfWork();
        Application.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txnReverseDTO);
        assertSuccess(result);
        Application.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 10-03-2007 to complete ER Txns
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        processACHTxns.process("20070913");
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();


        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company, payrollRun1.getSourcePayRunId());

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 5);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.TWO_DAY,
                     company.getFundingModel().getFundingModelCd());
        // Verify Refund txns created
        payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdReversalRefundCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("ER Refund Transactions", 3, payrollFinTxns.size());
        // Verify Transaction responses
        for (FinancialTransaction finTxn : payrollFinTxns) {
            DomainEntitySet<TransactionResponse> txnResponse =
                    TransactionResponse.findTransactionResponses(finTxn);
            assertEquals("Transaction Responses", 1, txnResponse.size());
        }
        // Verify Reversal OK System Event
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.ReversalOK, null, null, null);
        assertEquals("Reversal OK Events", companyEvents.size(), 3);
        // Verify emails are generated for these events correctly
        for (CompanyEvent reversalOKEvent : companyEvents) {
            DomainEntitySet<CompanyEventEmail> email = Application.find(CompanyEventEmail.class, CompanyEventEmail.CompanyEvent().equalTo(reversalOKEvent));
            assertEquals(email.size(), 1);
        }
    }

    /**
     * Test method to verify ER Refund txn is created.
     * Should create ER Refund Txn.
     */
    @Test
    public void testCreateERRefund_CompanySuspended() {
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

        // reverse entire payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        c1dl.reverseEntirePayroll("BatchTest05");
        PayrollServices.commitUnitOfWork();

        // offload EE DD RV DB
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 10-03-2007 to complete ER Txns
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071003000000");
        processACHTxns.process("20071003");
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        processACHTxns.process("20071009");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());
        // Verify Refund txns created
        payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdReversalRefundCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("ER Refund Transactions", 2, payrollFinTxns.size());
        // Verify Transaction responses
        for (FinancialTransaction finTxn : payrollFinTxns) {
            DomainEntitySet<TransactionResponse> txnResponse =
                    TransactionResponse.findTransactionResponses(finTxn);
            assertEquals("Transaction Responses", 1, txnResponse.size());
        }
        // Verify Reversal OK System Event
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company1, EventTypeCode.ReversalOK, null, null, null);
        assertEquals("Reversal OK Events", companyEvents.size(), 2);
        CompanyEvent event = companyEvents.get(0);

        assertEquals("Refund Status Reason", null,
                     event.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        event = companyEvents.get(1);
        assertEquals("Refund Status Reason", null,
                     event.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to verify ER Refund txn is created.
     * Should not create ER Refund Txn, since CBA is not active.
     */
    @Test
    public void testCreateERRefund_CBAInActive() {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                              c1dl.getCompany().getSourceSystemCd());
        PayrollServices.rollbackUnitOfWork();

        // create verification credits
        DataLoadServices.runACHTransactionProcessor(0);

        // offload verification credits
        DataLoadServices.runOffload(company, 2007, 9, 14);

        // offload QBOE ER DB
        DataLoadServices.runOffload(company, 2007, 9, 25);

        // offload QBOE EE CR
        DataLoadServices.runOffload(company, 2007, 9, 28);

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        c1dl.reverseEntirePayroll("BatchTest05");
        PayrollServices.commitUnitOfWork();

        // offload EE DD RV DB
        DataLoadServices.runOffload(company, 2007, 10, 2);

        // call the txn process batch process for the date 10-03-2007 to complete ER Txns
        DataLoadServices.runACHTransactionProcessor(1);

        // Inactivate Company1 BA
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());

        assertSuccess(PayrollServices.companyManager.deactivateCompanyBankAccount(
                company1.getSourceSystemCd(),
                company1.getSourceCompanyId(),
                company1.getCompanyBankAccountCollection().iterator().next().getSourceBankAccountId(),
                false, false));
        PayrollServices.commitUnitOfWork();

        // call the txn process batch process for the date 10-09-2007
        DataLoadServices.runACHTransactionProcessor(6);

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                       c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the Company1's funding model is not changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        // Verify no Refund txns created
        payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdReversalRefundCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("ER Refund Transactions", 0, payrollFinTxns.size());

        // Verify Reversal OK System Event
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company1, EventTypeCode.ReversalOK, null, null, null);
        assertEquals("Reversal OK Events", companyEvents.size(), 2);
        CompanyEvent event = companyEvents.get(0);

        assertEquals("Refund Status Reason", EnumUtils.getReadableName(RefundStatusReasonType.BankAccountInactive),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));

        event = companyEvents.get(1);
        assertEquals("Refund Status Reason", EnumUtils.getReadableName(RefundStatusReasonType.BankAccountInactive),
                     event.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testMain() {

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
        PSPDate.setPSPTime("20071017000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 10-17-2007
        ProcessACHTransactions.main(new String[]{"20071017"});

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                               c1dl.getCompany().getSourceSystemCd());


        // verify the payroll status is Complete
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        assertEquals("PayrollRunStatus", PayrollStatus.Complete, payRunC1.getPayrollRunStatus());

        // verify the financial txns state is Completed
        DomainEntitySet<FinancialTransaction> payrollFinTxns = payRunC1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Completed Transactions for Payroll1", payrollFinTxns.size(), 3);

        // verify the no QBOE company's funding model is changed to 2 Day
        assertEquals("Company Funding Model", FundingModel.Codes.FIVE_DAY,
                     company1.getFundingModel().getFundingModelCd());

        PayrollServices.commitUnitOfWork();

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
        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        if (payChecks.iterator().hasNext()) {
            PaycheckDTO firstPayCheck = payChecks.iterator().next();
            firstPayCheck.getDdTransactions().iterator().next().setDDTransactionAmount(new BigDecimal("200.00"));
            firstPayCheck.setPaycheckNetAmount(new SpcfMoney("200.00"));
        }
        c1dl.persistPayrollRun(payrollRunDTO);
    }

    private static void persistCompany3() {
        c3dl.persistCompany3();
    }

    private static void addCompany1Payroll2() {
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-10-10"));
        c1dl.persistPayrollRun(payrollRunDTO);
    }

    private static void addCompany1Payroll2_MinSuspectPayrollAmount() {
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PayrollRunDTO_MinSuspectPayrollAmount(new DateDTO("2007-10-10"));
        c1dl.persistPayrollRun(payrollRunDTO);
    }

    private static void addCompany3Payroll1() {
        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-11-15"));
        c3dl.persistPayrollRun(payrollRunDTO);
    }


    @Test
    public void testDontActivateForBankActivation() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);
        DataLoadServices.addCompanyBankAccount(company, true);

        offloadFinancialTransactions("20110103000000");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110120000000");
        new ProcessACHTransactions().process("20110120");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
        assertEquals(companyBankAccount, null);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEmployerVerificationCredit() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);
        DataLoadServices.addCompanyBankAccount(company, true);

        DomainEntitySet<FinancialTransaction> verificationDebits = FinancialTransaction.findFinancialTransactions(company,
                                                                                                                  TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);
        assertFalse(verificationDebits.isEmpty());
        assertEquals(2, verificationDebits.size());

        List<SpcfMoney> randomAmounts = new ArrayList<SpcfMoney>();
        SpcfUniqueId erBankAccount = verificationDebits.get(0).getDebitBankAccount().getId();
        SpcfUniqueId intuitBankAccount = verificationDebits.get(0).getCreditBankAccount().getId();
        String sku = verificationDebits.get(0).getSku();
        randomAmounts.add(verificationDebits.get(0).getFinancialTransactionAmount());
        randomAmounts.add(verificationDebits.get(1).getFinancialTransactionAmount());

        offloadFinancialTransactions("20110103000000");

        verificationDebits = FinancialTransaction.findFinancialTransactions(company,
                                                                            TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);
        assertFalse(verificationDebits.isEmpty());
        assertEquals(2, verificationDebits.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110111000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20110111");
        PayrollServices.commitUnitOfWork();

        verificationDebits = FinancialTransaction.findFinancialTransactions(company,
                                                                            TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Completed);
        assertFalse(verificationDebits.isEmpty());
        assertEquals(2, verificationDebits.size());

        DomainEntitySet<FinancialTransaction> verificationCredits = FinancialTransaction.findFinancialTransactions(company,
                                                                                                                   TransactionTypeCode.EmployerVerificationCredit, TransactionStateCode.Created);
        assertFalse(verificationCredits.isEmpty());
        assertEquals(2, verificationCredits.size());

        for (FinancialTransaction verificationCredit : verificationCredits) {
            assertTrue(randomAmounts.contains(verificationCredit.getFinancialTransactionAmount()));

            assertEquals(sku, verificationCredit.getSku());

            assertEquals(erBankAccount, verificationCredit.getCreditBankAccount().getId());
            assertEquals(BankAccountOwnerType.Company, verificationCredit.getCreditBankAccountType());

            assertEquals(intuitBankAccount, verificationCredit.getDebitBankAccount().getId());
            assertEquals(BankAccountOwnerType.Intuit, verificationCredit.getDebitBankAccountType());
        }

        offloadFinancialTransactions("20110111000000");

        verificationCredits = FinancialTransaction.findFinancialTransactions(company,
                                                                             TransactionTypeCode.EmployerVerificationCredit, TransactionStateCode.Executed);
        assertFalse(verificationCredits.isEmpty());
        assertEquals(2, verificationCredits.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110119000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20110119");
        PayrollServices.commitUnitOfWork();

        verificationCredits = FinancialTransaction.findFinancialTransactions(company,
                                                                             TransactionTypeCode.EmployerVerificationCredit, TransactionStateCode.Completed);
        assertFalse(verificationCredits.isEmpty());
        assertEquals(2, verificationCredits.size());
    }

    @Test
    public void testEmployerVerificationCreditAmountError() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);
        DataLoadServices.addCompanyBankAccount(company, true);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> verificationDebits = FinancialTransaction.findFinancialTransactions(company,
                                                                                                                  TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);
        assertFalse(verificationDebits.isEmpty());
        assertEquals(2, verificationDebits.size());

        List<SpcfMoney> randomAmounts = new ArrayList<SpcfMoney>();
        SpcfUniqueId erBankAccount = verificationDebits.get(0).getDebitBankAccount().getId();
        SpcfUniqueId intuitBankAccount = verificationDebits.get(0).getCreditBankAccount().getId();
        String sku = verificationDebits.get(0).getSku();
        randomAmounts.add(verificationDebits.get(0).getFinancialTransactionAmount());
        randomAmounts.add(verificationDebits.get(1).getFinancialTransactionAmount());
        PayrollServices.commitUnitOfWork();

        offloadFinancialTransactions("20110103000000");

        PayrollServices.beginUnitOfWork();
        verificationDebits = FinancialTransaction.findFinancialTransactions(company,
                                                                            TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);
        assertFalse(verificationDebits.isEmpty());
        assertEquals(2, verificationDebits.size());

        PSPDate.setPSPTime("20110111000000");

        verificationDebits.get(0).setFinancialTransactionAmount(new SpcfMoney("1.01"));
        Application.save(verificationDebits.get(0));

        verificationDebits.get(1).setFinancialTransactionAmount(new SpcfMoney("1000.00"));
        Application.save(verificationDebits.get(1));

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20110111");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        verificationDebits = FinancialTransaction.findFinancialTransactions(company,
                                                                            TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Completed);
        assertFalse(verificationDebits.isEmpty());
        assertEquals(2, verificationDebits.size());

        DomainEntitySet<FinancialTransaction> verificationCredits = FinancialTransaction.findFinancialTransactions(company,
                                                                                                                   TransactionTypeCode.EmployerVerificationCredit, TransactionStateCode.Created);
        assertTrue(verificationCredits.isEmpty());
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testEmployerVerificationCredit_PSRV004048() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);
        DataLoadServices.addCompanyBankAccount(company, true);

        DomainEntitySet<FinancialTransaction> verificationDebits = FinancialTransaction.findFinancialTransactions(company,
                                                                                                                  TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);
        assertFalse(verificationDebits.isEmpty());
        assertEquals(2, verificationDebits.size());

        List<SpcfMoney> randomAmounts = new ArrayList<SpcfMoney>();
        SpcfUniqueId erBankAccount = verificationDebits.get(0).getDebitBankAccount().getId();
        SpcfUniqueId intuitBankAccount = verificationDebits.get(0).getCreditBankAccount().getId();
        String sku = verificationDebits.get(0).getSku();
        randomAmounts.add(verificationDebits.get(0).getFinancialTransactionAmount());
        randomAmounts.add(verificationDebits.get(1).getFinancialTransactionAmount());

        offloadFinancialTransactions("20110103000000");

        verificationDebits = FinancialTransaction.findFinancialTransactions(company,
                                                                            TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);
        assertFalse(verificationDebits.isEmpty());
        assertEquals(2, verificationDebits.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110111000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20110111");
        PayrollServices.commitUnitOfWork();

        verificationDebits = FinancialTransaction.findFinancialTransactions(company,
                                                                            TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Completed);
        assertFalse(verificationDebits.isEmpty());
        assertEquals(2, verificationDebits.size());

        DomainEntitySet<FinancialTransaction> verificationCredits = FinancialTransaction.findFinancialTransactions(company,
                                                                                                                   TransactionTypeCode.EmployerVerificationCredit, TransactionStateCode.Created);
        assertFalse(verificationCredits.isEmpty());
        assertEquals(2, verificationCredits.size());

        for (FinancialTransaction verificationCredit : verificationCredits) {
            assertTrue(randomAmounts.contains(verificationCredit.getFinancialTransactionAmount()));

            assertEquals(sku, verificationCredit.getSku());

            assertEquals(erBankAccount, verificationCredit.getCreditBankAccount().getId());
            assertEquals(BankAccountOwnerType.Company, verificationCredit.getCreditBankAccountType());

            assertEquals(intuitBankAccount, verificationCredit.getDebitBankAccount().getId());
            assertEquals(BankAccountOwnerType.Intuit, verificationCredit.getDebitBankAccountType());
        }

        offloadFinancialTransactions("20110111000000");

        verificationCredits = FinancialTransaction.findFinancialTransactions(company,
                                                                             TransactionTypeCode.EmployerVerificationCredit, TransactionStateCode.Executed);
        assertFalse(verificationCredits.isEmpty());
        assertEquals(2, verificationCredits.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110119000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20110119");
        PayrollServices.commitUnitOfWork();

        verificationCredits = FinancialTransaction.findFinancialTransactions(company,
                                                                             TransactionTypeCode.EmployerVerificationCredit, TransactionStateCode.Completed);
        assertFalse(verificationCredits.isEmpty());
        assertEquals(2, verificationCredits.size());


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110120000000");
        PayrollServices.commitUnitOfWork();
        DataLoadServices.returnTxns(verificationCredits, "R03", "R03 Return");
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110120000000");
        PayrollServices.commitUnitOfWork();
        offloadFinancialTransactions("20110120000000");
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.QBDT, company.getSourceCompanyId());
        SpcfDecimal feeCashRevenue = LedgerAccount.getLedgerAccountBalance(company,
                                                                           LedgerAccountCode.FeeCashRevenue);
        SpcfDecimal feeIncome = LedgerAccount.getLedgerAccountBalance(company,
                                                                      LedgerAccountCode.FeeIncome);
        assertTrue(feeCashRevenue.isGreaterThan(SpcfDecimal.createInstance(0)));
        assertTrue(feeIncome.isGreaterThan(SpcfDecimal.createInstance(0)));
        PayrollServices.rollbackUnitOfWork();
    }

}
