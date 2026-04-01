/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/AddWriteOffBadDebtTransactionTests.java#5 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddWriteOffBadDebtTransactionDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: rkrishna
 * Date: Dec 28, 2007
 * Time: 1:45:51 PM
 * @see BadDebtTaxTests for tests writing off bad debt on tax companies
 */
public class AddWriteOffBadDebtTransactionTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        AddWriteOffBadDebtTransactionDataLoader.loadBeforeTest();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(null, "123272727", null);

        Application.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                     errorMessage.getMessage());
    }

    /**
     * Test message 138 - Source CompanyId not specified
     */
    @Test
    public void testNullCompany() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBOE, null, null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                     errorMessage.getMessage());
    }

    /**
     * Test message 5001 - PayrollRunId has Invalid value
     */
    @Test
    public void testNullPayrollRunId() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "123272727", null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "PayrollRunId has invalid value",
                     errorMessage.getMessage());
    }

    /**
     * Test message 169 - Company Does Not Exist
     */
    @Test
    public void testInvalidCompany() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1232727", "BatchId01");
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1232727 does not exist.",
                     errorMessage.getMessage());
    }

    /**
     * Test message 194 - PayrollRunId Does Not Exist
     */
    @Test
    public void testInvalidPayrollRunId() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddWriteOffBadDebtTransactionDataLoader.psd1.loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                                                                                                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "123272727", "BatchId03");
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "194", errorMessage.getMessageCode());
        assertEquals("Error message", "Payroll Run with DDTxBatchID BatchId03 does not exist for company QBOE:123272727.",
                     errorMessage.getMessage());
    }

    // Test 1055
    @Test
    public void testCreateTransactionFailureLedgerBalance() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        Application.beginUnitOfWork();
        loader.loadDataForFirstNSFReturn();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult processResult2 = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                SourceSystemCode.QBOE, "1234567", "BatchTest05");

        Application.commitUnitOfWork();

        assertFalse(processResult2.isSuccess());
        assertTrue("Messages size", processResult2.getMessages().size() > 0);
        Message errorMessage = processResult2.getMessages().get(0);
        assertEquals("Error message code", "1055", errorMessage.getMessageCode());
        assertEquals("Error message", "Action BadDebtWriteOff not valid for payroll run with DDTxBatchID BatchTest05 due to current ledger account balances.",
                     errorMessage.getMessage());
    }

    /**
     * Test message 282 - Transaction cannot be created due to pending activity against this ledger account
     *
     */

    @Test
    public void testCreateTransactionFailurePendingLedgerActivity() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        ACHReturnsDataLoader.loadDataForEEReturnTransferReturn();

        //Create an EE Return transfer for the EE DD Credit that bounced
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(12);
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");

        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Can't do a bad debt W/O until the EE DD Return Transfer has gone through!
        Application.beginUnitOfWork();
        ProcessResult processResult2 = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                SourceSystemCode.QBOE, "1234567", "BatchTest05");

        Application.commitUnitOfWork();

        assertFalse(processResult2.isSuccess());
        assertTrue("Messages size", processResult2.getMessages().size() > 0);
        Message errorMessage = processResult2.getMessages().get(0);
        assertEquals("Error message code", "282", errorMessage.getMessageCode());
        assertEquals("Error message", "Transaction cannot be created due to pending activity against this ledger account.",
                     errorMessage.getMessage());

        //Offload the EE DD Return Transfer
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        //Now, do the bad debt write off
        Application.beginUnitOfWork();
        ProcessResult secondBDWO = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                SourceSystemCode.QBOE, "1234567", "BatchTest05");
        Application.commitUnitOfWork();

        assertSuccess(secondBDWO);
    }

    /**
     * Test - Return ER debit, create redebit, and then write off the payroll
     */
    @Test
    public void testWriteOffPendingRedebitPayStatus() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company1 = c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1DL.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(payRun);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Persist the Transction Return
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(c1DL.getCompany(), "BatchTest05");
        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R02", "Non-NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = Application.findById(TransactionReturn.class, returnList.get(0).getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                                                                                 getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();

        FinancialTransaction originalTxn = null;
        // Get the employer debit transactions returned for the payroll
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        assertTrue(financialTxs.size() == 1);
        originalTxn = financialTxs.get(0);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList();
        redebitCollection.add(redebitDTO);
        ProcessResult<DomainEntitySet<FinancialTransaction>> procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(company1.getSourceSystemCd(),
                                                                                                                                                            company1.getSourceCompanyId(),
                                                                                                                                                            redebitCollection);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, payRun.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();

        assertSuccess("Redebit process result", procResult);
        assertEquals("Payroll run status is PendingRedebit", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), payRun.getPayrollTXBatchId());

        Application.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Persistence check for Employer Redebit Transaction
        Application.beginUnitOfWork();
        company = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, payRun.getPayrollTXBatchId());
        assertEquals("Payroll Run Status", PayrollStatus.WrittenOff, payrollRun.getPayrollRunStatus());
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerWriteOff},
                new TransactionStateCode[]{TransactionStateCode.Created});
        DomainEntitySet<FinancialTransaction> canceledRedebits = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        Application.commitUnitOfWork();

        //Ensure the write off was properly created
        assertEquals("Number of Created Financial Txs", 1, financialTxs.size());
        FinancialTransaction erDDRedb = financialTxs.get(0);
        assertEquals("Payroll Run Id ", payRun.getPayrollTXBatchId(), erDDRedb.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("180.00"), erDDRedb.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Created, erDDRedb.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.ACH, erDDRedb.getSettlementTypeCd());

        //Ensure the ACH redebit was cancelled
        assertEquals("Number of Cancelled Financial Txs", 1, canceledRedebits.size());
        FinancialTransaction erDDRedbACH = canceledRedebits.get(0);
        assertEquals("Payroll Run Id ", payRun.getPayrollTXBatchId(), erDDRedbACH.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("180.00"), erDDRedbACH.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, erDDRedbACH.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.ACH, erDDRedbACH.getSettlementTypeCd());

        //Persistence check for Transaction Return Status
        Application.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            Assert.assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                                txnReturn.getReturnStatusCd());
        }

        Application.commitUnitOfWork();


    }

    /**
     * Test - Return ER debit with an NSF and then write off the payroll
     */
    @Test
    public void testWriteOffNSF() throws Exception {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company1 = c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1DL.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(payRun);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 01, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
        //Persist the Transction Return
        DomainEntitySet<TransactionReturn> returnList = persistNSF(company1.getSourceCompanyId(), company1.getSourceSystemCd(), payRun.getPayrollTXBatchId());
        assertEquals("Number of txn returns", 1, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                                                                                 getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        //Ensure the NSF put the payroll in an auto redebit pending status
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payRun.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();
        assertEquals("Payroll run status is PendingAutoRedebit", PayrollStatus.PendingAutoRedebit, payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), payRun.getPayrollTXBatchId());
        Application.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Persistence check for Employer Redebit Transaction
        Application.beginUnitOfWork();
        company = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, payRun.getPayrollTXBatchId());
        assertEquals("Payroll Run Status", PayrollStatus.WrittenOff, payrollRun.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerWriteOff},
                new TransactionStateCode[]{TransactionStateCode.Created});
        DomainEntitySet<FinancialTransaction> canceledRedebits = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        Application.commitUnitOfWork();

        //Ensure the EmployerWriteOff transaction was properly created
        assertEquals("Number of Created Financial Txs", 1, financialTxs.size());
        FinancialTransaction erDDRedb = financialTxs.get(0);
        assertEquals("Payroll Run Id ", payRun.getPayrollTXBatchId(), erDDRedb.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("180.00"), erDDRedb.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Created, erDDRedb.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.ACH, erDDRedb.getSettlementTypeCd());

        //Ensure the ACH redebit was cancelled
        assertEquals("Number of Cancelled Financial Txs", 1, canceledRedebits.size());
        FinancialTransaction erDDRedbACH = canceledRedebits.get(0);
        assertEquals("Payroll Run Id ", payRun.getPayrollTXBatchId(), erDDRedbACH.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("180.00"), erDDRedbACH.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, erDDRedbACH.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.ACH, erDDRedbACH.getSettlementTypeCd());

        //Persistence check for Transaction Return Status
        Application.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            Assert.assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                                txnReturn.getReturnStatusCd());
        }

        Application.commitUnitOfWork();
    }

    /**
     * Test - Return ER debit, create reversals, and then write off the payroll
     */
    @Test
    public void testWriteOffPendingReversals() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company1 = c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1DL.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(payRun);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 1, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();


        //Persist the Transction Return
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(c1DL.getCompany(), "BatchTest05");
        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R02", "Non-NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = Application.findById(TransactionReturn.class, returnList.get(0).getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                                                                                 getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, payRun.getPayrollTXBatchId());
        TransactionReverseDTO transactionReverseDTO = new TransactionReverseDTO();

        transactionReverseDTO.setSourcePayrollRunId(payRun.getPayrollTXBatchId());
        transactionReverseDTO.setDdTransactionIdList(null);
        transactionReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        transactionReverseDTO.setTxDate(null);
        transactionReverseDTO.setChargeFee(false);
        transactionReverseDTO.setIntuitInitiatedReversals(true);

        ProcessResult procResult = PayrollServices.payrollManager.reverseTransaction(company1.getSourceSystemCd(),
                                                                                     company1.getSourceCompanyId(), transactionReverseDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, payRun.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();

        assertSuccess("Reversal process result", procResult);
        assertEquals("Payroll run status is PendingReversals", PayrollStatus.PendingReversals, payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), payRun.getPayrollTXBatchId());
        Application.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Persistence check for Employer Redebit Transaction
        Application.beginUnitOfWork();
        company = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, payRun.getPayrollTXBatchId());
        assertEquals("Payroll Run Status", PayrollStatus.WrittenOff, payrollRun.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerWriteOff},
                new TransactionStateCode[]{TransactionStateCode.Created});
        DomainEntitySet<FinancialTransaction> cancelledReversals = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        Application.commitUnitOfWork();

        //Ensure the wire was properly created
        assertEquals("Number of Created Financial Txs", 1, financialTxs.size());
        FinancialTransaction erDDRedb = financialTxs.get(0);
        assertEquals("Payroll Run Id ", payRun.getPayrollTXBatchId(), erDDRedb.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("180.00"), erDDRedb.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Created, erDDRedb.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.ACH, erDDRedb.getSettlementTypeCd());

        //Ensure the ACH reversals were cancelled
        assertEquals("Number of ACH ee dd reversal Financial Txs", 2, cancelledReversals.size());
        for (FinancialTransaction currEEDDReversal : cancelledReversals) {
            assertEquals("Payroll Run Id ", payRun.getPayrollTXBatchId(), currEEDDReversal.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, currEEDDReversal.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Settlement Type ", SettlementType.ACH, currEEDDReversal.getSettlementTypeCd());
        }

        //Persistence check for Transaction Return Status
        Application.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            Assert.assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                                txnReturn.getReturnStatusCd());
        }

        Application.commitUnitOfWork();
    }

    private DomainEntitySet<TransactionReturn> persistNSF(String pCompanyID, SourceSystemCode pSourceSystem, String pPayrollRunId) {
        Application.beginUnitOfWork();

        Company company = Company.findCompany(
                pCompanyID, pSourceSystem);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pPayrollRunId);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        DomainEntitySet<FinancialTransaction> c1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : c1FinTxns) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("This is an NSF description");
            transactionReturn.setBankReturnTraceNumber(112L);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                                                                                       SpcfTimeZone.getLocalTimeZone()));
            transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setCompany(financialTransaction.getCompany());
            returnList.add(Application.save(transactionReturn));
        }
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        Application.commitUnitOfWork();
        return returnList;
    }

    @Test
    public void collectedNothing() {
        writeoff(0, 0, 0);
    }

    @Test
    public void collectedPartialDD() {
        writeoff(1.23, 0, 0); // collect a little DD money before writing off
    }

    @Test
    public void collectedPartialFee() {
        writeoff(0, 0.12, 0); // collect a little fee money before writing off
    }


    private void writeoff(double pPartialDD, double pPartialFee, double pPartialTax) {
        SpcfCalendar targetPayrollDate = SpcfCalendar.createInstance(/*2007, 10, 2,*/ 2008, 4, 1, SpcfTimeZone.getLocalTimeZone());

        SpcfCalendar offloadDate = targetPayrollDate.copy();
        offloadDate.addDays(-4);

        SpcfCalendar today = offloadDate.copy();
        today.addDays(-23);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(today);//(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        ProcessResult<PayrollRun> prPayroll = createCompanyAndSubmitPayroll(targetPayrollDate);
        PayrollServicesTest.assertSuccess("Payroll submission", prPayroll);

        PayrollRun payroll = prPayroll.getResult();
        Company company = payroll.getCompany();

        // advance the PSP time and offload the payroll transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(offloadDate);//(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // get the debit transactions
        DomainEntitySet<FinancialTransaction> ftDebits = findTransactions(payroll, TransactionTypeGroupCode.Debit, TransactionStateCode.Executed);
        Assert.assertEquals("Number of debit FTs", 3, ftDebits.size()); // 1 DD and 1 (per-paycheck) fee and 1 Sales Tax

        PayrollServices.commitUnitOfWork();

        // simulate an NSF bank return of those transactions
        DomainEntitySet<TransactionReturn> returnList = persistNSF(company.getSourceCompanyId(), company.getSourceSystemCd(), payroll.getSourcePayRunId());
        assertEquals("Number of txn returns", 1, returnList.size());

        // call the return handler
        Application.beginUnitOfWork();
        offloadDate.addDays(1);
        PSPDate.setPSPTime(offloadDate);//(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(returnList.get(0));
        handler.execute(returnList.get(0));
        Application.commitUnitOfWork();

        // make sure the NSF put the payroll in the right status
        PayrollServices.beginUnitOfWork();
        payroll = Application.refresh(payroll);
        DomainEntitySet<FinancialTransaction> ftRedebitsCreated = findTransactions(payroll, TransactionTypeGroupCode.Redebit, TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("PayrollRun status after NSF return", PayrollStatus.PendingAutoRedebit, payroll.getPayrollRunStatus());
        assertEquals("Redebits created", 3, ftRedebitsCreated.size()); // 1 for DD, 1 for (per-paycheck) fee and 1 sales tax

        //offload redebits
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2008, 3, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        //wait 5 days
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(5);
        PayrollServices.commitUnitOfWork();

        //return redebits
        DataLoadServices.returnTxns(payroll,TransactionTypeCode.EmployerDdRedebit, TransactionTypeCode.EmployerFeeRedebit, TransactionTypeCode.EmployerFeeDebit );

        //add a non-ach redebit txn for pPartialDD (AddRepaymentTransactions)
        RedebitImpoundDTO redebitImpoundDTO;
        AddRepaymentTransactions addRepaymentTransactions; //= new AddRepaymentTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(), );

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(20);
        Application.commitUnitOfWork();

        // fully/partially collect any/all of DD/fee/tax
        PayrollServices.beginUnitOfWork();
        payroll = Application.refresh(payroll);
        ftDebits = findTransactions(payroll, TransactionTypeGroupCode.Debit, TransactionStateCode.Returned);
        if (pPartialDD > 0.0) {
            FinancialTransaction origFT = ftDebits.get(0);
            Assert.assertEquals(TransactionTypeCode.EmployerDdDebit, origFT.getTransactionType().getTransactionTypeCd());
            Assert.assertTrue("Hibernate Session exists", Application.getHibernateSession() != null);
            redebitImpoundDTO = new RedebitImpoundDTO(origFT.getId().toString(), new SpcfMoney(String.valueOf(pPartialDD)), new DateDTO(origFT.getMoneyMovementTransaction().getInitiationDate()), SettlementTypeDTO.Wire);
            addRepaymentTransactions = new AddRepaymentTransactions(company.getSourceSystemCd(),company.getSourceCompanyId(),redebitImpoundDTO);
            assertSuccess(addRepaymentTransactions.execute());
        }
        if (pPartialFee > 0.0) {
            FinancialTransaction origFT = ftDebits.get(1);
            Assert.assertEquals(TransactionTypeCode.EmployerFeeDebit, origFT.getTransactionType().getTransactionTypeCd());

            redebitImpoundDTO = new RedebitImpoundDTO(origFT.getId().toString(), new SpcfMoney(String.valueOf(pPartialFee)), new DateDTO(origFT.getMoneyMovementTransaction().getInitiationDate()), SettlementTypeDTO.Wire);
            addRepaymentTransactions = new AddRepaymentTransactions(company.getSourceSystemCd(),company.getSourceCompanyId(),redebitImpoundDTO);
            assertSuccess(addRepaymentTransactions.execute());
        }
        if (pPartialTax > 0.0) {
            FinancialTransaction origFT = ftDebits.get(2);
            Assert.assertEquals(TransactionTypeCode.ServiceSalesAndUseTax, origFT.getTransactionType().getTransactionTypeCd());
            redebitImpoundDTO = new RedebitImpoundDTO(origFT.getId().toString(), new SpcfMoney(String.valueOf(pPartialTax)), new DateDTO(origFT.getMoneyMovementTransaction().getInitiationDate()), SettlementTypeDTO.Wire);
            addRepaymentTransactions = new AddRepaymentTransactions(company.getSourceSystemCd(),company.getSourceCompanyId(),redebitImpoundDTO);
            assertSuccess(addRepaymentTransactions.execute());
        }

        Application.commitUnitOfWork();

        // write off whatever is left
        Application.beginUnitOfWork();
        ProcessResult prWriteoff = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payroll.getSourcePayRunId());
        Application.commitUnitOfWork();

        assertSuccess("Writeoff", prWriteoff);

        Application.beginUnitOfWork();

        // make sure the writeoff put the payroll in the right status
        payroll = Application.refresh(payroll);
        assertEquals("PayrollRun status after writeoff", PayrollStatus.WrittenOff, payroll.getPayrollRunStatus());

        // make sure the writeoff FTs were created correctly
        DomainEntitySet<FinancialTransaction> ftWriteoffsCreated = findTransactions(payroll, TransactionTypeGroupCode.Writeoff, TransactionStateCode.Created);
        assertEquals("Number of Writeoff FTs created", 5, ftWriteoffsCreated.size()); // 1 DD and 2 (per-paycheck) fee and 2 sales tax

        FinancialTransaction ddWriteoffFT = ftWriteoffsCreated.get(0);
        assertEquals("DD Writeoff FT Amount", subtract(mPayrollNetAmount, pPartialDD), ddWriteoffFT.getFinancialTransactionAmount());
        assertEquals("DD Writeoff FT Settlement Type", SettlementType.ACH, ddWriteoffFT.getSettlementTypeCd());

        FinancialTransaction feeWriteoffFT = ftWriteoffsCreated.get(1);
        assertEquals("Fee Writeoff FT Amount", subtract(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().multiply(SpcfDecimal.createInstance(2)), pPartialFee), feeWriteoffFT.getFinancialTransactionAmount());
        assertEquals("Fee Writeoff FT Settlement Type", SettlementType.ACH, feeWriteoffFT.getSettlementTypeCd());

        FinancialTransaction feeWriteoffFT1 = ftWriteoffsCreated.get(2);
        assertEquals("Fee Writeoff FT Amount", new SpcfMoney("100.00"), feeWriteoffFT1.getFinancialTransactionAmount());
        assertEquals("Fee Writeoff FT Settlement Type", SettlementType.ACH, feeWriteoffFT1.getSettlementTypeCd());

        // make sure the transaction return for the original DD debit is Resolved
        DomainEntitySet<FinancialTransaction> ftDebitsReturned = findTransactions(payroll, TransactionTypeGroupCode.Debit, TransactionStateCode.Returned);
        for (FinancialTransaction returnedFT : ftDebitsReturned) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(returnedFT).get(0);
            Assert.assertEquals("TransactionReturn status", TransactionReturnStatusCode.Resolved, txnReturn.getReturnStatusCd());
        }

        Application.commitUnitOfWork();
    }

    private SpcfMoney mPayrollNetAmount;

    private ProcessResult<PayrollRun> createCompanyAndSubmitPayroll(SpcfCalendar pTargetPayrollDate) {
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);

        // this creates the company and other stuff and offloads the bank verfication debits
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();

        payrollRunDTO.setTargetPayrollTXDate(new DateDTO(pTargetPayrollDate));

        PayrollServices.commitUnitOfWork();

        mPayrollNetAmount = payrollRunDTO.getPayrollDirectDepositAmount();

        // this submits the payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);

        if (prPayroll.isSuccess()) {
            // the DataLoader, used by PayrollSubmitDataLoader, creates Companies with invalid addresses, causing the
            // IAS sales-tax service to fail.  so we fix the address here... state and zip must agree
            Company company = prPayroll.getResult().getCompany();
            company.getLegalAddress().setState("NV");
            company.getLegalAddress().setZipCode("89512");
            Application.save(company);

            prPayroll.setResult( (PayrollRun)Application.findById(PayrollRun.class, prPayroll.getResult().getId()) );
        }
        PayrollServices.commitUnitOfWork();

        return prPayroll;
    }

    private DomainEntitySet<FinancialTransaction> findTransactions(PayrollRun pPayrollRun,
                                                                   TransactionTypeGroupCode pGroupCd,
                                                                   TransactionStateCode pStateCd) {
        TransactionState state = Application.findById(TransactionState.class, pStateCd);
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                                                   .And(FinancialTransaction.CurrentTransactionState().equalTo(state)))
                        .OrderBy(FinancialTransaction.TransactionType(), FinancialTransaction.FinancialTransactionAmount());         // sort them by type... this conveniently puts DD before Fee before SalesAndUseTax

        DomainEntitySet<FinancialTransaction> found = Application.find(FinancialTransaction.class, query);

        DomainEntitySet<FinancialTransaction> matches = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction ft : found) {
            if (ft.getTransactionType().getTransactionTypeGroupCd() == pGroupCd) {
                matches.add(ft);
            }
        }

        return matches;
    }

    private FinancialTransaction createRelatedFT(TransactionTypeCode pTypeCd, TransactionStateCode pStateCd,
                                                 double pAmount, FinancialTransaction pOrigFT, PayrollRun pPayroll) {
        IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit),
                CreditDebitCode.Credit);

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(pPayroll.getCompany());

        SpcfCalendar settlementDate = PSPDate.getPSPTime();
        CalendarUtils.clearTime(settlementDate);

        // create the FT
        FinancialTransaction ft = FinancialTransaction.createFinancialTransaction(
                pPayroll.getCompany(), pPayroll, null, iba.getBankAccount(), cba.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Company, pTypeCd,
                new SpcfMoney(String.valueOf(pAmount)), SettlementType.ACH, settlementDate,
                pOrigFT.getSku(), pOrigFT, 0);

        // advance it to the requested state
        if (pStateCd != TransactionStateCode.Created) {
            TransactionState executed = Application.findById(TransactionState.class, TransactionStateCode.Executed);
            ft.addTransactionState(executed);

            if (pStateCd == TransactionStateCode.Completed) {
                TransactionState completed = Application.findById(TransactionState.class, TransactionStateCode.Completed);
                ft.addTransactionState(completed);
            } else if (pStateCd == TransactionStateCode.Returned) {
                TransactionState returned = Application.findById(TransactionState.class, TransactionStateCode.Returned);
                ft.addTransactionState(returned);
            }
        }

        return ft;
    }

    SpcfMoney subtract(SpcfDecimal a, double b) {
        SpcfDecimal c = a.subtract(SpcfDecimal.createInstance(b));
        return new SpcfMoney(c);
    }

    @Test
    public void bug589() {
        /**
         * Write Off transaction created for payroll when there is no payroll balance due
         * Steps to Reproduce:
         * 1) Create a company and payroll
         * 2) Return the employer debit with R01
         * 3) Change the collection method to Intuit reversals
         * 4) Complete the reversals
         * 5) Create an Intuit EE Return Transfer to move the $ from EE to ER
         * 6) Write off the payroll in the payroll ledger screen
         * Results: Payroll write off txn created AND Fee write off txn created
         * Expected: Only Fee write off txn created
         * Implications: Agent can write off more than is due
         */
        SpcfCalendar targetPayrollDate = SpcfCalendar.createInstance(2008, 4, 1, SpcfTimeZone.getLocalTimeZone());

        // 1) Create a company and payroll
        DataLoadServices.setPSPDate(2008, 3, 7);

        PayrollRun payroll = assertSuccessResult(createCompanyAndSubmitPayroll(targetPayrollDate));
        Company company = payroll.getCompany();

        // advance the PSP time and offload the payroll transactions
        DataLoadServices.runOffload(company, 2008, 3, 28);

        Application.beginUnitOfWork();
        // get the debit transactions
        DomainEntitySet<FinancialTransaction> ftDebits = findTransactions(payroll, TransactionTypeGroupCode.Debit, TransactionStateCode.Executed);
        Assert.assertEquals("Number of debit FTs", 3, ftDebits.size()); // 1 DD and 1 (per-paycheck) fee and 1 sales tax

        PayrollServices.commitUnitOfWork();

        // 2) Return the employer debit with R01
        DomainEntitySet<TransactionReturn> returnList = persistNSF(company.getSourceCompanyId(), company.getSourceSystemCd(), payroll.getSourcePayRunId());
        assertEquals("Number of txn returns", 1, returnList.size());

        // call the return handler
        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(returnList.get(0));
        handler.execute(returnList.get(0));
        Application.commitUnitOfWork();

        // make sure the NSF put the payroll in the right status
        PayrollServices.beginUnitOfWork();
        payroll = Application.refresh(payroll);
        DomainEntitySet<FinancialTransaction> ftRedebitsCreated = findTransactions(payroll, TransactionTypeGroupCode.Redebit, TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("PayrollRun status after NSF return", PayrollStatus.PendingAutoRedebit, payroll.getPayrollRunStatus());
        assertEquals("Redebits created", 3, ftRedebitsCreated.size()); // 1 for DD, 1 for (per-paycheck) fee, 1 sales tax

        // 3) Change the collection method to Intuit reversals
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        payroll = Application.refresh(payroll);
        company = Application.refresh(company);
        CompanyService cs = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);

        TransactionReverseDTO dtoReverse = new TransactionReverseDTO();
        dtoReverse.setSourcePayrollRunId(payroll.getSourcePayRunId());
        dtoReverse.setDdTransactionIdList(null);
        dtoReverse.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        dtoReverse.setTxDate(null);
        dtoReverse.setChargeFee(false);
        dtoReverse.setIntuitInitiatedReversals(true);
        ProcessResult prReverse = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dtoReverse);
        Application.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("reverse the payroll", prReverse);

        // 4) Complete the reversals
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> ftRevDebits = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "123272727",
                TransactionTypeCode.EmployeeDdReversalDebit,
                TransactionStateCode.Created);
        FinancialTransaction ft = ftRevDebits.get(0);
        PSPDate.setPSPTime(ft.getMoneyMovementTransaction().getInitiationDate());
        Application.commitUnitOfWork();

        DataLoadServices.runOffload(company, 2008, 3, 31);

        DataLoadServices.runACHTransactionProcessor(5);

        // 5) Create an Intuit EE Return Transfer to move the $ from EE to ER
        Application.beginUnitOfWork();
        company = Application.refresh(company);
        SpcfMoney eeBefore = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.EEReturnLiablility, payroll.getSourcePayRunId(), company);
        SpcfMoney erBefore = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, payroll.getSourcePayRunId(), company);
        assertSuccess(PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBDT, "123272727", payroll.getSourcePayRunId()));
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> ftTransfer = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "123272727",
                TransactionTypeCode.IntuitEmployeeReturnTransfer,
                TransactionStateCode.Created);
        PSPDate.setPSPTime(ftTransfer.get(0).getMoneyMovementTransaction().getInitiationDate());
        Application.commitUnitOfWork();

        DataLoadServices.runOffload(company, 2008, 4, 8);

        Application.beginUnitOfWork();
        SpcfMoney eeAfter = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.EEReturnLiablility, payroll.getSourcePayRunId(), company);
        SpcfMoney erAfter = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, payroll.getSourcePayRunId(), company);
        Application.commitUnitOfWork();

        SpcfDecimal zero = SpcfDecimal.createInstance(0);
        assertTrue("EEReturnLiability > 0 before transfer", eeBefore.compareTo(zero) > 0);
        assertTrue("EEReturnLiability == 0 after transfer", eeAfter.compareTo(zero) == 0);
        assertTrue("ERReturnReceivable < 0 before transfer", erBefore.compareTo(zero) < 0);
        assertTrue("ERReturnReceivable offset by transfer amount", erAfter.subtract(erBefore).compareTo(eeBefore) == 0); // fee+tax amounts remain as negative balance

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // write off whatever is left
        Application.beginUnitOfWork();
        ProcessResult prWriteoff = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payroll.getSourcePayRunId());

        DomainEntitySet<FinancialTransaction> ftWriteoffs = findTransactions(payroll, TransactionTypeGroupCode.Writeoff, TransactionStateCode.Created);
        Application.commitUnitOfWork();

        for (FinancialTransaction wo : ftWriteoffs) {
            System.out.println(wo.getTransactionType().getTransactionTypeCd().toString()+" for "+wo.getFinancialTransactionAmount());
        }
        assertSuccess("Writeoff", prWriteoff);
        assertEquals("number of writeoffs created", 2, ftWriteoffs.size()); // the wrong behavior writes of the payroll amount AND the fee amount (2 writeoff transactions)
        FinancialTransaction feeWriteOff = assertOne(ftWriteoffs.find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerWriteOffFee)));
        FinancialTransaction salesTaxWriteOff = assertOne(ftWriteoffs.find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerWriteOffSalesAndUseTax)));
        assertEquals("writeoff amount", erAfter.abs(), feeWriteOff.getFinancialTransactionAmount().add(salesTaxWriteOff.getFinancialTransactionAmount()));
    }

    @Test
    public void testWriteOffBadDbt_PSRV002421_1() {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"));

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 4, 13, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun payrollrun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(payrollrun, TransactionTypeCode.EmployerTaxDebit);

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(payrollrun, TransactionTypeCode.EmployerFeeDebit);

        PayrollServices.beginUnitOfWork();
        ProcessResult prWriteoff = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollrun.getSourcePayRunId());
        assertEquals("Write off error message list", 1, prWriteoff.getErrorMessages().size());
        assertEquals("Write off error message code", "5000", prWriteoff.getErrorMessages().get(0).getMessageCode());
        assertEquals("Write off error message code", "Pending tax payments must be voided and ER Payable must be applied before writing off bad debt.", prWriteoff.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

        //-- complete ERTaxRedebit
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        PayrollServices.commitUnitOfWork();

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        // write off whatever is left
        Application.beginUnitOfWork();
        prWriteoff = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollrun.getSourcePayRunId());
        assertSuccess(prWriteoff);
        DomainEntitySet<FinancialTransaction> ftWriteoffs = findTransactions(payrollrun, TransactionTypeGroupCode.Writeoff, TransactionStateCode.Created);

        assertEquals("number of writeoffs created", 2, ftWriteoffs.size());
        FinancialTransaction feeWriteOff = assertOne(ftWriteoffs.find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerWriteOffFee)));
        FinancialTransaction salesTaxWriteOff = assertOne(ftWriteoffs.find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerWriteOffSalesAndUseTax)));
        assertEquals("writeoff amount", new SpcfMoney("100.08"), feeWriteOff.getFinancialTransactionAmount().add(salesTaxWriteOff.getFinancialTransactionAmount()));
        Application.commitUnitOfWork();
    }


    @Test
    public void testWriteOffBadDbt_PSRV002421_2() {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"));


        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 4, 13, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun payrollrun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.enrollEFTPS(company);
        //Sent
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT"));
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 14, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(payrollrun, TransactionTypeCode.EmployerTaxDebit);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> fts = FinancialTransaction.findFinancialTransactions(payrollrun, new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxRedebit, TransactionTypeCode.EmployerFeeRedebit}, new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction ft : fts) {
            ft.cancelFinancialTransaction();
        }
        PayrollServices.commitUnitOfWork();

        // write off whatever is left
        PayrollServices.beginUnitOfWork();
        ProcessResult prWriteoff = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollrun.getSourcePayRunId());
        assertEquals("Write off error message list", 1, prWriteoff.getErrorMessages().size());
        assertEquals("Write off error message code", "5000", prWriteoff.getErrorMessages().get(0).getMessageCode());
        assertEquals("Write off error message code", "Pending tax payments must be voided and ER Payable must be applied before writing off bad debt.", prWriteoff.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();



        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(7);
        assertSuccess(PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollrun.getId().toString()));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // write off whatever is left
        PayrollServices.beginUnitOfWork();
        prWriteoff = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollrun.getSourcePayRunId());
        assertSuccess(prWriteoff);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollrun = Application.refresh(payrollrun);
        DomainEntitySet<FinancialTransaction> ftWriteoffs = findTransactions(payrollrun, TransactionTypeGroupCode.Writeoff, TransactionStateCode.Created);
        assertEquals("Number of WriteOff FTs", 1, ftWriteoffs.size());
        assertEquals("Write off FT Amount", new SpcfMoney("13.2"), ftWriteoffs.get(0).getFinancialTransactionAmount());
        assertEquals("Write off FT Transaction Type code", TransactionTypeCode.EmployerWriteOffTax, ftWriteoffs.get(0).getTransactionType().getTransactionTypeCd());
        PayrollServices.commitUnitOfWork();

    }

}
