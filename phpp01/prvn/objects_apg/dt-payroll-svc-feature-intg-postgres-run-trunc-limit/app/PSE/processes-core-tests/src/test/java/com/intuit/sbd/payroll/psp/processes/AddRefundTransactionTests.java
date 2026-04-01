/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/AddRefundTransactionTests.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddRefundTransactionDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.util.List;
import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

/**
 * User: rkrishna
 * Date: Dec 12, 2007
 * Time: 9:12:46 AM
 */
public class AddRefundTransactionTests {

    @Before
    public void runBeforeEachTest() {
        AddRefundTransactionDataLoader.loadBeforeTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * ************************************Null tests/incoming data verification***********************************
     */

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(null, "123272727", null);

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

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, null, null);

        Application.commitUnitOfWork();

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

        RefundDTO refundDTO = new RefundDTO();
        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
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
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "1232727", refundDTO);

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

        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId03");

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "194", errorMessage.getMessageCode());
        assertEquals("Error message",
                "Payroll Run with DDTxBatchID BatchId03 does not exist for company QBOE:123272727.",
                errorMessage.getMessage());
    }

    /**
     * Test message 186 - Company Bank Account Id is Not Active
     */
    @Test
    public void testInActiveCompanyBankAccountId() {
        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();


        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        // Deactivate CBA
        ProcessResult cbaDeactivateProcResult = PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE,
                "123272727", "123123", true, false);

        Application.commitUnitOfWork();

        assertTrue("Process Result", cbaDeactivateProcResult.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message", "Company QBOE:123272727 does not have an active bank account.",
                errorMessage.getMessage());
        assertEquals("Error message code", "1062", errorMessage.getMessageCode());
    }

    /**
     * Test message 5001 - Null Financial Transaction Amount
     */
    @Test
    public void testNullTxnAmount() {
        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Other);
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());

        assertEquals("Error message", "FinancialTransactionAmount has invalid value", errorMessage.getMessage());
    }

    /**
     * Test message 283 - Negative Financial Transaction Amount
     */
    @Test
    public void testNegativeFinTxnAmount() {
        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Other);
        refundDTO.setFinancialTxAmt(new SpcfMoney("-1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 25, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "283", errorMessage.getMessageCode());

        assertEquals("Error message", "The amount must be a non-zero, positive number.", errorMessage.getMessage());
    }

    /**
     * Test message 271 - Settlement Date is Too Far in Past
     */
    @Test
    public void testSettlementDateTooFarInPast() {

        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Cash);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 7, 15, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "271", errorMessage.getMessageCode());

        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash and date " +
                DateDTO.convertToSpcfCalendar(refundDTO.getTxDate())
                        .toString() + ", which is more than 45 days in the past.",
                errorMessage.getMessage());
    }

    /**
     * Test message 266 - Settlement Date is too Far In Future
     */
    @Test
    public void testSettlementDateTooFarInFuture() {
        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Cash);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 25, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "266", errorMessage.getMessageCode());
        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash with the future date " +
                DateDTO.convertToSpcfCalendar(refundDTO.getTxDate()).toString() + ".", errorMessage.getMessage());
    }

    /**
     * Test message 282 - Transaction cannot be created due to pending activity against this ledger account
     */
    @Test
    public void testCreateTransactionFailurePendingLedgerActivity() {
        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();


        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 25, SpcfTimeZone.getLocalTimeZone())));


        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "282", errorMessage.getMessageCode());
        assertEquals("Error message",
                "Transaction cannot be created due to pending activity against this ledger account.",
                errorMessage.getMessage());
    }

    /**
     * Test message 280 - Pending Transaction Already Exist
     */
    @Test
    public void testPendingTransactionAlreadyExists() {

        ACHReturnsDataLoader.loadData5AgentCancels1CheckCancelsRefund();

        //Add first refund
        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney("30.00"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult firstProcessResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "1234567", refundDTO);
        Application.commitUnitOfWork();

        assertSuccess("First refund", firstProcessResult);

        //Try to add second refund
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "1234567", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "280", errorMessage.getMessageCode());
        assertEquals("Error message",
                "A pending transaction of this type already exists.  If you wish to create a new one, the existing transaction must be canceled, first.",
                errorMessage.getMessage());
    }


    /**
     * Test - DD Refund Process for ACH Settlement Type
     */
    @Test
    public void testDDRefundProcessForACHSettlementType() {
        ACHReturnsDataLoader.loadData5AgentCancels1CheckCancelsRefund();

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney("30.00"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "1234567", refundDTO);

        TransactionResponse transactionResponse = (TransactionResponse) processResult.getResult();

        Application.commitUnitOfWork();

        assertSuccess(processResult);

        Application.beginUnitOfWork();

        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> refundTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdRefundCredit, null, null,
                TransactionStateCode.Created);
        Application.commitUnitOfWork();

        assertEquals("Number of refund transactions", 1, refundTxns.size());
        FinancialTransaction finTxn = refundTxns.get(0);

        Application.beginUnitOfWork();
        DomainEntitySet<TransactionResponse> actualTxnResponses = TransactionResponse.findTransactionResponses(finTxn);
        Application.commitUnitOfWork();

        assertEquals("Number of txnResponses", 1, actualTxnResponses.size());
        assertEquals("Txn response is linked to new fin txn", transactionResponse, actualTxnResponses.get(0));

        assertEquals("Financial Transaction Amount", new SpcfMoney("30.00"), finTxn.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State", TransactionStateCode.Created, finTxn.getCurrentTransactionState().getTransactionStateCd());
    }

    /**
     * Test - DD Refund process for Settlement Type Otherthan ACH
     */
    @Test
    public void testDDRefundProcessForCashSettlementType() {

        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();


        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        assertSuccess(payrollProcess);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        refundDTO.setSettlementType(SettlementTypeDTO.Other);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        FinancialTransaction financialTransaction = (FinancialTransaction) processResult.getResult();

        assertTrue("Process Result", payrollProcess.isSuccess());

        assertEquals("Financial Txn Amount ", refundDTO.getFinancialTxAmt(),
                financialTransaction.getFinancialTransactionAmount());

        assertEquals("Payroll Run ", refundDTO.getSourcePayrollRunId(),
                financialTransaction.getPayrollRun().getSourcePayRunId());

        assertEquals("Settlement Type", SettlementType.Other,
                financialTransaction.getSettlementTypeCd());

        assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                financialTransaction.getCurrentTransactionState().getTransactionStateCd());

        Application.commitUnitOfWork();
    }

    private TransactionResponse AddRefundTransactionForACHSettlementType() {
        AddRefundTransactionDataLoader.loadAddRefundTransctionForACHData();

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        TransactionResponse transactionResponse = (TransactionResponse) processResult.getResult();

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        return transactionResponse;
    }

    /**
     * Test - DD Refund process for Settlement Type ACH when the Pending transactions already exists : PSRV000760
     */
    @Test
    public void testDDRefundProcessForACHSettlementTypeWhenThePendingTransactionsExists() {
        //Persist comapany and submit the payroll
        Company1Dataloader c1dl = new Company1Dataloader();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-09-07"));
        PayrollRun payrollRun = c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Offload er txn for first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO txnCancelDTO = new TransactionCancelEEDTO();
        List<String> sourcePaycheckIds = new ArrayList<String>();
        PaycheckSplit split = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS1");
        sourcePaycheckIds.add(split.getPaycheck().getSourcePaycheckId());
        txnCancelDTO.setSourcePaycheckIdList(sourcePaycheckIds);
        txnCancelDTO.setSourcePayrollRunId("BatchTest05");
        txnCancelDTO.setAgentCancel(true);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult txnProcResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", txnCancelDTO);

        PayrollServices.commitUnitOfWork();
        assertSuccess("Cancel One DD In the payroll ", txnProcResult);

        /*****Rep cancels ER refund****/
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> refundTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdRefundCredit, null, null,
                TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("One refund txn", 1, refundTxns.size());
        FinancialTransaction refundTxn = refundTxns.get(0);

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult cancelTxnProcResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "1234567", refundTxn.getId().toString());

        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Cancel Automatic ER DD Refund ", cancelTxnProcResult);

        //Create DD Refund for ACH
        PayrollServices.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney("30.00"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "1234567", refundDTO);

        TransactionResponse transactionResponse = (TransactionResponse) processResult.getResult();

        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        refundTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdRefundCredit, null, null,
                TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of refund transactions", 1, refundTxns.size());
        FinancialTransaction finTxn = refundTxns.get(0);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionResponse> actualTxnResponses = TransactionResponse.findTransactionResponses(finTxn);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txnResponses", 1, actualTxnResponses.size());
        assertEquals("Txn response is linked to new fin txn", transactionResponse, actualTxnResponses.get(0));

        assertEquals("Financial Transaction Amount", new SpcfMoney("30.00"), finTxn.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State", TransactionStateCode.Created, finTxn.getCurrentTransactionState().getTransactionStateCd());

    }


    /**
     * Test - DD Refund process for Settlement Type ACH when the Pending transactions already exists : PSRV000760
     */
    @Test
    public void testDDRefundProcessForACHSettlementTypeWhenThePendingTransactionsExists1() {
        //Persist comapany and submit the payroll
        Company1Dataloader c1dl = new Company1Dataloader();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-09-07"));
        PayrollRun payrollRun = c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Offload er txn for first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***Rep cancels one paycheck****/
        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO txnCancelDTO = new TransactionCancelEEDTO();
        List<String> sourcePaycheckIds = new ArrayList<String>();
        PaycheckSplit split = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS1");
        sourcePaycheckIds.add(split.getPaycheck().getSourcePaycheckId());
        txnCancelDTO.setSourcePaycheckIdList(sourcePaycheckIds);
        txnCancelDTO.setSourcePayrollRunId("BatchTest05");
        txnCancelDTO.setAgentCancel(true);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult txnProcResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", txnCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Cancel One DD In the payroll ", txnProcResult);

        /*****Rep cancels ER refund****/
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> refundTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdRefundCredit, null, null,
                TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("One refund txn", 1, refundTxns.size());
        FinancialTransaction refundTxn = refundTxns.get(0);

        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult cancelTxnProcResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "1234567", refundTxn.getId().toString());
        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Cancel Automatic ER DD Refund ", cancelTxnProcResult);

        //Create DD Refund for WIRE
        PayrollServices.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.Wire);
        refundDTO.setFinancialTxAmt(new SpcfMoney("10.00"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "1234567", refundDTO);

        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        refundTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdRefundCredit, null, null,
                TransactionStateCode.Completed);


        assertEquals("Number of refund transactions", 1, refundTxns.size());
        FinancialTransaction finTxn = refundTxns.get(0);

        assertEquals("Financial Txn Amount ", refundDTO.getFinancialTxAmt(),
                finTxn.getFinancialTransactionAmount());

        assertEquals("Payroll Run ", refundDTO.getSourcePayrollRunId(), finTxn.getPayrollRun().getSourcePayRunId());

        assertEquals("Settlement Type", SettlementType.Wire, finTxn.getSettlementTypeCd());

        assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                finTxn.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.commitUnitOfWork();

        //Create DD Refund for ACH
        PayrollServices.beginUnitOfWork();
        refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone())));

        processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "1234567", refundDTO);

        TransactionResponse transactionResponse = (TransactionResponse) processResult.getResult();

        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        refundTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdRefundCredit, null, null,
                TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of refund transactions", 1, refundTxns.size());
        finTxn = refundTxns.get(0);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionResponse> actualTxnResponses = TransactionResponse.findTransactionResponses(finTxn);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txnResponses", 1, actualTxnResponses.size());
        assertEquals("Txn response is linked to new fin txn", transactionResponse, actualTxnResponses.get(0));
        assertEquals("Financial Transaction Amount", new SpcfMoney("20.00"), finTxn.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State", TransactionStateCode.Created, finTxn.getCurrentTransactionState().getTransactionStateCd());
    }

    /**
     * Test - DD Refund process for Settlement Type ACH when the Pending transactions already exists : PSRV000760
     * When the multiple payrolls exisits for the same company and  non cancelled EmployerDdRefundCredit exists for the
     * other payroll.
     */
    @Test
    public void testDDRefundProcessForACHSettlementTypeWhenThePendingTransactionsExists2() {
        //Persist comapany and submit the payroll
        Company1Dataloader c1dl = new Company1Dataloader();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-09-07"));
        PayrollRun payrollRun = c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = c1dl.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-09-07"));
        payrollRun = c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Offload er txn for first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***Rep cancels one paycheck for payroll1 ****/
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        TransactionCancelEEDTO txnCancelDTO = new TransactionCancelEEDTO();
        List<String> sourcePaycheckIds = new ArrayList<String>();
        PaycheckSplit split = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS1");
        sourcePaycheckIds.add(split.getPaycheck().getSourcePaycheckId());
        txnCancelDTO.setSourcePaycheckIdList(sourcePaycheckIds);
        txnCancelDTO.setSourcePayrollRunId("BatchTest05");
        txnCancelDTO.setAgentCancel(true);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult txnProcResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", txnCancelDTO);
        PayrollServices.userManager.deleteUser("UnitTestAgent");

        PayrollServices.commitUnitOfWork();

        assertSuccess("Cancel One DD In the payroll; BatchTest05 ", txnProcResult);

        /*****Rep cancels ER refund****/
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> refundTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdRefundCredit, null, null,
                TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("One refund txn", 1, refundTxns.size());
        FinancialTransaction refundTxn = refundTxns.get(0);

        PayrollServices.beginUnitOfWork();
        ProcessResult cancelTxnProcResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "1234567", refundTxn.getId().toString());
        PayrollServices.commitUnitOfWork();

        assertSuccess("Cancel Automatic ER DD Refund ", cancelTxnProcResult);

        /***Rep cancels one paycheck for payroll2 ****/
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest002");
        txnCancelDTO = new TransactionCancelEEDTO();
        sourcePaycheckIds = new ArrayList<String>();
        split = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS2");
        sourcePaycheckIds.add(split.getPaycheck().getSourcePaycheckId());
        txnCancelDTO.setSourcePaycheckIdList(sourcePaycheckIds);
        txnCancelDTO.setSourcePayrollRunId("BatchTest002");
        txnCancelDTO.setAgentCancel(true);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        txnProcResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", txnCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Cancel One DD In the payroll2 : BatchTest002  ", txnProcResult);

        //Create DD Refund for ACH for payroll : BatchTest05
        PayrollServices.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addRefundTransaction(SourceSystemCode.QBOE, "1234567", refundDTO);

        TransactionResponse transactionResponse = (TransactionResponse) processResult.getResult();

        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        refundTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdRefundCredit, null, null,
                TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of refund transactions", 1, refundTxns.size());
        FinancialTransaction finTxn = refundTxns.get(0);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionResponse> actualTxnResponses = TransactionResponse.findTransactionResponses(finTxn);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txnResponses", 1, actualTxnResponses.size());
        assertEquals("Txn response is linked to new fin txn", transactionResponse, actualTxnResponses.get(0));
        assertEquals("Financial Transaction Amount", new SpcfMoney("30.00"), finTxn.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State", TransactionStateCode.Created, finTxn.getCurrentTransactionState().getTransactionStateCd());
    }
}
