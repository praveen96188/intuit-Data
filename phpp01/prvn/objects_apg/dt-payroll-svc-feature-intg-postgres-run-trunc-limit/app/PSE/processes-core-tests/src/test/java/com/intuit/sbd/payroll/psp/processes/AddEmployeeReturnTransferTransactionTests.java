/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/AddEmployeeReturnTransferTransactionTests.java#3 $
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
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: rkrishna
 * Date: Dec 5, 2007
 * Time: 3:26:02 PM
 */
public class AddEmployeeReturnTransferTransactionTests {

    private PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * ************************************Null tests/incoming data verification***********************************
     */

    /**
     * Test message 138 - Source CompanyId not specified
     */
    @Test
    public void testNullCompany() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, null, null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(null, "123272727", null);

        Application.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 5001 - PayrollRunId has Invalid value
     */
    @Test
    public void testNullPayrollRunId() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, "123272727", null);

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
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, "1232727", "BatchId01");
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
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, "123272727", "BatchId03");

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "194", errorMessage.getMessageCode());
        assertEquals("Error message", "Payroll Run with DDTxBatchID BatchId03 does not exist for company QBOE:123272727.",
                errorMessage.getMessage());
    }

    /**
     * Test message 282 - Transaction cannot be created due to pending activity against this ledger account
     */

    @Test
    public void testCreateTransactionFailurePendingLedgerActivity() throws Exception {

        //Submit the payroll
        submitPayroll();

        //Post the transaction return for Employer DD Credit
        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturn();

        //Execute Default Reject Return Event
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE,
                "123272727", "BatchId01");

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "282", errorMessage.getMessageCode());
        assertEquals("Error message", "Transaction cannot be created due to pending activity against this ledger account.",
                errorMessage.getMessage());
    }

    /**
     * Test message 280 - Pending Transaction Already Exist
     */

    @Test
    public void testPendingTransactionAlreadyExists() throws Exception {

        //Add Employee Return Transfer Transaction
        addEmployeeReturnTransferTransaction();

        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(
                SourceSystemCode.QBOE, "1234567", "BatchTest05");

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "280", errorMessage.getMessageCode());
        assertEquals("Error message", "A pending transaction of this type already exists.  If you wish to create a new one, the existing transaction must be canceled, first.",
                errorMessage.getMessage());
    }

    /**
     * Test message 290 - Reversal Transaction Incomplete
     */
    @Test
    public void testCreateTxnFailureReversalTxnIncomplete() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Offload the transaction
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        //Application.beginUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        //PayrollServices.commitUnitOfWork();

        //Reverse single ee txn from payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(4);
        PayrollServices.commitUnitOfWork();
        c1dl.reverseSingleTransactionInPayroll("BatchTest05", "EEBA2PS1");

        //Offload the reversal
        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(
                SourceSystemCode.QBOE, "1234567", "BatchTest05");

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "290", errorMessage.getMessageCode());
        assertEquals("Error message", "This transaction cannot be created until all existing reversal transactions for this payroll have a status of Complete.",
                errorMessage.getMessage());
    }

    /**
     * Test - EEReturn Transfer Process
     */
    @Test
    public void testEEReturnTransferProcess() {

        DomainEntitySet<TransactionReturn> transactionReturns = addEmployeeReturnTransferTransaction();

        //Persistence check for Tansaction Return Status
        for (TransactionReturn transactionReturn : transactionReturns) {
            assertEquals("Transaction Status ", TransactionReturnStatusCode.Resolved,
                    transactionReturn.getReturnStatusCd());
        }

        //Persistence check for Employee Return Transfer Transaction
        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.IntuitEmployeeReturnTransfer},
                new TransactionStateCode[]{TransactionStateCode.Created});

        Application.commitUnitOfWork();

        assertEquals("Number of Financial Txs", 1, financialTxs.size());

        for (FinancialTransaction eeFinTxn : financialTxs) {
            assertEquals("Payroll Run Id ", "BatchTest05",
                    eeFinTxn.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction State ", TransactionStateCode.Created,
                    eeFinTxn.getCurrentTransactionState().getTransactionStateCd());
        }

    }

    private DomainEntitySet<TransactionReturn> addEmployeeReturnTransferTransaction() {
        //Submit payroll and create returns for one of the ees and for the er
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        ACHReturnsDataLoader.loadDataForEEReturnTransferReturn();

        Application.beginUnitOfWork();

        PSPDate.addDaysToPSPTime(1);
        AddEmployeeReturnTransferTransaction eeReturnTransfer = new AddEmployeeReturnTransferTransaction(
                SourceSystemCode.QBOE, "1234567", "BatchTest05");
        ProcessResult processResult = eeReturnTransfer.execute();

        Application.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        return eeReturnTransfer.getTransactionReturns();
    }


    private void submitPayroll() {
        //Submit the Payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());
    }

    private DomainEntitySet<TransactionReturn> persistTransactionReturn() {

        Application.beginUnitOfWork();

        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> eeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : eeFinancialTxs) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R02");
            transactionReturn.setBankReturnDescription("This is an Employee DD Refund return transaction");
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

    private RefundDTO build_refundDto(SettlementTypeDTO pSettlementType) {
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(pSettlementType);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        return refundDTO;
    }

    private void reverseEntirePayroll() {
        //Reverse Entire Payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId("BatchId01");
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, "123272727", txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(0, result.getMessages().size());
    }
}
