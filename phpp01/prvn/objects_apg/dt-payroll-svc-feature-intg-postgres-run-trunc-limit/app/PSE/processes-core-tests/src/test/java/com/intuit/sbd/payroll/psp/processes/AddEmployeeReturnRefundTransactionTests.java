/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/AddEmployeeReturnRefundTransactionTests.java#1 $
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
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddEmployeeReturnRefundTransactionDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * User: rkrishna
 * Date: Dec 14, 2007
 * Time: 3:54:14 PM

 */
public class AddEmployeeReturnRefundTransactionTests {

    @Before
    public void runBeforeEachTest() {
        AddEmployeeReturnRefundTransactionDataLoader.loadBeforeTest();
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
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(null, "123272727", null);

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
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, null, null);

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

        RefundDTO refundDTO = new RefundDTO();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
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

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "1232727", refundDTO);

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
        PayrollRunDTO payrollRunDTO = AddEmployeeReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId03");

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "194", errorMessage.getMessageCode());
        assertEquals("Error message", "Payroll Run with DDTxBatchID BatchId03 does not exist for company QBOE:123272727.",
                errorMessage.getMessage());
    }

    /**
     * Test message 186 - Company Bank Account Id is Not Active
     */
    @Test
    public void testInActiveCompanyBankAccountId() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddEmployeeReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

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

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message", "Company QBOE:123272727 does not have an active bank account.",
                errorMessage.getMessage());
        Assert.assertEquals("Error message code", "1062", errorMessage.getMessageCode());
    }

    /**
     * Test message 5001 - Null Financial Transaction Amount
     */
    @Test
    public void testNullTxnAmount() {
        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddEmployeeReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Other);
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
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
        PayrollRunDTO payrollRunDTO = AddEmployeeReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Other);
        refundDTO.setFinancialTxAmt(new SpcfMoney("-1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

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
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddEmployeeReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Cash);
        refundDTO.setFinancialTxAmt(new SpcfMoney("705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 7, 15, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "271", errorMessage.getMessageCode());

        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash and date " +
                DateDTO.convertToSpcfCalendar(refundDTO.getTxDate()).toString() + ", which is more than 45 days in the past.",
                errorMessage.getMessage());
    }

    /**
     * Test message 266 - Settlement Date is too Far In Future
     */
    @Test
    public void testSettlementDateTooFarInFuture() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddEmployeeReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();

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


        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "266", errorMessage.getMessageCode());
        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash with the future date " +
                DateDTO.convertToSpcfCalendar(refundDTO.getTxDate()).toString() + ".", errorMessage.getMessage());
    }

    /**
     * Test message 280 - Pending Transaction Already Exist
     */
    @Test
    public void testPendingTransactionAlreadyExists() {
        AddEmployeeReturnRefundTransactionDataLoader.submitPayroll();

        PayrollServices.beginUnitOfWork();
        //Offload the transaction
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        reverseEntirePayroll();

        //Add Employee Return Refund Transaction
        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 25, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(
                SourceSystemCode.QBOE, "123272727", refundDTO);

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
        AddEmployeeReturnRefundTransactionDataLoader.submitPayroll();

        PayrollServices.beginUnitOfWork();
        //Offload the transaction
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        reverseEntirePayroll();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        Application.commitUnitOfWork();

        //Application.beginUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        //PayrollServices.commitUnitOfWork();

        //Add Employee Return Refund Transaction
        Application.beginUnitOfWork();

        RefundDTO refundDTO = build_refundDto(SettlementTypeDTO.ACH);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "290", errorMessage.getMessageCode());
        assertEquals("Error message", "This transaction cannot be created until all existing reversal transactions for this payroll have a status of Complete.",
                errorMessage.getMessage());
    }

    /**
     * Test - EEReturn Refund Process for ACH Settlement Type
     */
    @Test
    public void testEEReturnRefundProcessForACHSettlementType() {
        ACHReturnsDataLoader.loadData2DayCompanyPutOnHold1EEReturnCompanyOffHold();
        
        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney("30.00"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(
                SourceSystemCode.QBOE, "1234567", refundDTO);

        TransactionResponse transactionResponse = (TransactionResponse) processResult.getResult();

        Application.commitUnitOfWork();

        assertSuccess("addEmployeeReturnRefundTransaction Process Result", processResult);
        assertEquals("Company Id ", "1234567", transactionResponse.getCompany().getSourceCompanyId());

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransactionState> finTxnStList = transactionResponse.getFinancialTransactionStates(
        );

        for (FinancialTransactionState financialTransactionState : finTxnStList) {
            FinancialTransaction finTxn = financialTransactionState.getFinancialTransaction();
            Assert.assertEquals("Payroll Run Id ", "BatchTest05",
                    finTxn.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Created,
                    finTxn.getCurrentTransactionState().getTransactionStateCd());
            Assert.assertEquals("Transaction Type ", TransactionTypeCode.EmployerDdRejectRefundCredit,
                    finTxn.getTransactionType().getTransactionTypeCd());
        }
        Application.commitUnitOfWork();

        assertEquals("number of txn states for txn response", 1, finTxnStList.size());
    }

    /**
     * Test - EEReturn Refund Process for ACH Settlement Type
     */
    @Test
    public void testEEReturnRefundProcessForACHSettlementTypeChangeCBA() {
        ACHReturnsDataLoader.loadData2DayCompanyPutOnHold1EEReturnCompanyOffHold();

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                                    CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                                SourceSystemCode.QBOE,
                                c1dl.getCompany1().getCompanyId(),
                                companyBankAccountDTO, false, true, true);
        assertSuccess("Change company bank account", result);
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney("30.00"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(
                SourceSystemCode.QBOE, "1234567", refundDTO);

        TransactionResponse transactionResponse = (TransactionResponse) processResult.getResult();

        Application.commitUnitOfWork();

        assertSuccess("addEmployeeReturnRefundTransaction Process Result", processResult);
        assertEquals("Company Id ", "1234567", transactionResponse.getCompany().getSourceCompanyId());

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransactionState> finTxnStList = transactionResponse.getFinancialTransactionStates(
        );

        for (FinancialTransactionState financialTransactionState : finTxnStList) {
            FinancialTransaction finTxn = financialTransactionState.getFinancialTransaction();
            Assert.assertEquals("Payroll Run Id ", "BatchTest05",
                    finTxn.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Created,
                    finTxn.getCurrentTransactionState().getTransactionStateCd());
            Assert.assertEquals("Transaction Type ", TransactionTypeCode.EmployerDdRejectRefundCredit,
                    finTxn.getTransactionType().getTransactionTypeCd());
            assertEquals("CBA", changedCompanyBankAccount.getBankAccount(), finTxn.getCreditBankAccount());
        }
        Application.commitUnitOfWork();

        assertEquals("number of txn states for txn response", 1, finTxnStList.size());
    }

    /**
     * Test - EEReturn Refund Process for Settlement Type otherthan ACH
     */
    @Test
    public void testEEReturnRefundProcessForCashSettlementType() {

        ACHReturnsDataLoader.loadData2DayCompanyPutOnHold1EEReturnCompanyOffHold();

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.Other);
        refundDTO.setFinancialTxAmt(new SpcfMoney("30.00"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE, "1234567", refundDTO);

        FinancialTransaction financialTransaction = (FinancialTransaction) processResult.getResult();

        Application.commitUnitOfWork();

        assertSuccess("Process Result addEmployeeReturnRefundTransaction", processResult);

        assertEquals("Financial Txn Amount ", refundDTO.getFinancialTxAmt(),
                financialTransaction.getFinancialTransactionAmount());

        assertEquals("Payroll Run ", refundDTO.getSourcePayrollRunId(),
                financialTransaction.getPayrollRun().getSourcePayRunId());

        assertEquals("Settlement Type", SettlementType.Other,
                financialTransaction.getSettlementTypeCd());

        assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                financialTransaction.getCurrentTransactionState().getTransactionStateCd());
    }

    private void reverseEntirePayroll(){
        //Reverse Entire Payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId("BatchId01");
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, "123272727", txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess("reverseTransaction", result);
    }

    private RefundDTO build_refundDto(SettlementTypeDTO pSettlementType) {
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(pSettlementType);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        return refundDTO;
    }

}
