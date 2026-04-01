/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/AddEmployeeWriteOffBadDebtTransactionTests.java#3 $
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
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddWriteOffBadDebtTransactionDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: Dawn Martens
 * Date: August 20, 2009
 * Time: 1:45:51 PM
 */
public class AddEmployeeWriteOffBadDebtTransactionTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
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

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(null, "123272727", null);

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
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, null, null);

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
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "123272727", null);

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
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1232727", "BatchId01");
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

        assertSuccess(payrollProcess);

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "123272727", "BatchId03");
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
        ProcessResult processResult2 = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(
                SourceSystemCode.QBOE, "1234567", "BatchTest05");

        Application.commitUnitOfWork();

        assertFalse(processResult2.isSuccess());
        assertTrue("Messages size", processResult2.getMessages().size() > 0);
        Message errorMessage = processResult2.getMessages().get(0);
        assertEquals("Error message code", "1055", errorMessage.getMessageCode());
        assertEquals("Error message", "Action BadDebtWriteOffEEReturn not valid for payroll run with DDTxBatchID BatchTest05 due to current ledger account balances.",
                errorMessage.getMessage());
    }

    /**
     * Ensure we can write off the payroll after reversals are returned
     */
    @Test
    public void testDDReversalReturnedAfterEEReturnTransferPSRV1281() {
        //Load data and agent inits reversals
        ACHReturnsDataLoader.loadData2DayERNSFsAgentReversesPayroll();

        //Offload reversals
        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process returns on 1 reversals
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.addDaysToPSPTime(1); //PSPDate.setPSPTime("20070910000000");
        DomainEntitySet<TransactionReturn> reversalReturns = ACHReturnsDataLoader.loadFirstDDReversalReturn();
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(15); //);.setPSPTime("20070918000000");
        //Complete other reversal
        ProcessACHTransactions processACHtxns = new ProcessACHTransactions();
        processACHtxns.process(PSPDate.getPSPTime());
        Application.commitUnitOfWork();

        //Can't do a reversal write off since we haven't transfered money for an untimely reversal return yet
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(3);
        ProcessResult abdWOProc4 = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        assertFalse(abdWOProc4.isSuccess());
        System.out.println(abdWOProc4.getMessages().toString());
        assertTrue("Messages size", abdWOProc4.getMessages().size() > 0);
        Message errorMessage = abdWOProc4.getMessages().get(0);
        assertEquals("Error message code", "1055", errorMessage.getMessageCode());
        assertEquals("Error message", "Action BadDebtWriteOffEEReturn not valid for payroll run with DDTxBatchID BatchTest05 due to current ledger account balances.",
                errorMessage.getMessage());
        PayrollServices.commitUnitOfWork();        

        Application.beginUnitOfWork();
        ProcessResult eeRetTxfrResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        assertSuccess(eeRetTxfrResult);
        Application.commitUnitOfWork();

        //Offload EmployeeReturnTransfer
        OffloadACHTransactions offloader3 = new OffloadACHTransactions();
        offloader3.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Can't do ee reversal write off after EE Ret txfr happens because we haven't gotten another reversal return yet
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        ProcessResult abdWOProc5 = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        assertFalse(abdWOProc5.isSuccess());
        System.out.println(abdWOProc5.getMessages().toString());
        assertTrue("Messages size", abdWOProc5.getMessages().size() > 0);
        errorMessage = abdWOProc5.getMessages().get(0);
        assertEquals("Error message code", "1055", errorMessage.getMessageCode());
        assertEquals("Error message", "Action BadDebtWriteOffEEReturn not valid for payroll run with DDTxBatchID BatchTest05 due to current ledger account balances.",
                errorMessage.getMessage());
        PayrollServices.commitUnitOfWork();


        //Write off the remaining balance on the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        ProcessResult abdWOProc = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        assertSuccess(abdWOProc);
        //find one EmployerWriteOff with amount 30.00
        DomainEntitySet<FinancialTransaction> writeOffs = PayrollServices.entityFinder.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(company)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerWriteOff)
                        .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))));
        DomainEntitySet<FinancialTransaction> reversalWriteOffs = PayrollServices.entityFinder.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(company)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeReversalFailedWriteOff)
                        .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))));
        assertEquals("Number of write offs", 1, writeOffs.size());
        assertEquals("Number of reversal write offs", 0, reversalWriteOffs.size());
        FinancialTransaction employerWriteOff = writeOffs.get(0);
        assertEquals("Amount for ERWriteOff", new SpcfMoney("30.00"), employerWriteOff.getFinancialTransactionAmount());
        PayrollServices.commitUnitOfWork();

        //Offload writeoff txn
        OffloadACHTransactions offloader4 = new OffloadACHTransactions();
        offloader4.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process returns on other reversals
        PayrollServices.beginUnitOfWork();
        allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.setPSPTime("20070919000000");
        reversalReturns = ACHReturnsDataLoader.load2ndDDReversalReturn();
        assertEquals("One reversal return", 1, reversalReturns.size());
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        //Write off the remaining balance on the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        ProcessResult abdWOProc2 = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        assertSuccess(abdWOProc2);
        //find one EmployerWriteOff with amount 30.00 and one ReversalWriteOff with amount $150.00
        writeOffs = PayrollServices.entityFinder.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(company)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerWriteOff)
                        .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))));
        reversalWriteOffs = PayrollServices.entityFinder.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(company)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeReversalFailedWriteOff)
                        .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))));
        assertEquals("Number of write offs", 1, writeOffs.size());
        assertEquals("Number of reversal write offs", 1, reversalWriteOffs.size());
        employerWriteOff = writeOffs.get(0);
        assertEquals("Amount for ERWriteOff", new SpcfMoney("30.00"), employerWriteOff.getFinancialTransactionAmount());
        FinancialTransaction employeeReversalWriteOff = reversalWriteOffs.get(0);
        assertEquals("Amount for EERevWriteOff", new SpcfMoney("150.00"), employeeReversalWriteOff.getFinancialTransactionAmount());

        PayrollServices.commitUnitOfWork();

        //Try to write it off again- should not be able to even though we have a debit ledger balance for EEReturnLiability
        PayrollServices.beginUnitOfWork();
        ProcessResult abdWOProc3 = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        assertFalse(abdWOProc3.isSuccess());
        assertTrue("Messages size", abdWOProc3.getMessages().size() > 0);
        errorMessage = abdWOProc3.getMessages().get(0);
        assertEquals("Error message code", "282", errorMessage.getMessageCode());
        assertEquals("Error message", "Transaction cannot be created due to pending activity against this ledger account.",
                errorMessage.getMessage());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testMultipleReversalsReturned() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070807000000");
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.persistEmployee2BankAccount2();
        c1dl.persistEmployee3BankAccount1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_ThreeEEs(new DateDTO("2007-09-10"));
        PayrollServices.commitUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);  //a        
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBOE, "1234567", (FundingModel)Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY));
        assertSuccess(procResult);
        procResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070906000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        //Load reversals (rep-inited)
        Application.beginUnitOfWork();
        c1dl.reverseEntirePayroll_IntuitInitiated("BatchTestExceedsBALimits");
        Application.commitUnitOfWork();

        //Offload reversals
        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process returns on 1 reversals
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.setPSPTime("20070910000000");
        DomainEntitySet<TransactionReturn> reversalReturns = ACHReturnsDataLoader.loadFirstDDReversalReturn();
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070918000000");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        //Complete other reversals
        ProcessACHTransactions processACHtxns = new ProcessACHTransactions();
        processACHtxns.process("20070918");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult eeRetTxfrResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, "1234567", "BatchTestExceedsBALimits");
        assertSuccess(eeRetTxfrResult);
        Application.commitUnitOfWork();

        //Offload EmployeeReturnTransfer
        OffloadACHTransactions offloader3 = new OffloadACHTransactions();
        offloader3.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process returns on other reversal
        PayrollServices.beginUnitOfWork();
        allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.setPSPTime("20070919000000");
        reversalReturns = ACHReturnsDataLoader.load2ndDDReversalReturn();
        assertEquals("One reversal return", 1, reversalReturns.size());
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        //Write off the reversal balance on the payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult abdWOProc2 = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTestExceedsBALimits");
        assertSuccess(abdWOProc2);

        //Write off the remaining balance on the payroll
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        ProcessResult abdWOProc = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTestExceedsBALimits");
        assertSuccess(abdWOProc);
        Application.commitUnitOfWork();

        //Offload write offs
        OffloadACHTransactions offloade4 = new OffloadACHTransactions();
        offloade4.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Return last reversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.setPSPTime("20070920000000");
        reversalReturns = ACHReturnsDataLoader.loadDDReversalReturn("EE3");
        assertEquals("One reversal return", 1, reversalReturns.size());
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070920000000");
        PayrollServices.commitUnitOfWork();

        //Write off new reversal return debt
        PayrollServices.beginUnitOfWork();
        ProcessResult abdWOProc3 = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTestExceedsBALimits");
        assertSuccess(abdWOProc3);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTestExceedsBALimits");
//        //find one EmployerWriteOff with amount 110.00 and a reversal write off for each of the reversals
        DomainEntitySet<FinancialTransaction> writeOffs = PayrollServices.entityFinder.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(company)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerWriteOff))
                        .And(FinancialTransaction.PayrollRun().equalTo(payrollRun)));
        DomainEntitySet<FinancialTransaction> reversalWriteOffs = PayrollServices.entityFinder.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(company)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeReversalFailedWriteOff)
                        .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))));
        reversalWriteOffs=reversalWriteOffs.sort(FinancialTransaction.FinancialTransactionAmount());
        assertEquals("Number of write offs", 1, writeOffs.size());
        assertEquals("Number of reversal write offs", 2, reversalWriteOffs.size());
        FinancialTransaction employerWriteOff = writeOffs.get(0);
        assertEquals("Amount for ERWriteOff", new SpcfMoney("80.00"), employerWriteOff.getFinancialTransactionAmount());
        assertEquals("Amount for EERevWriteOff", new SpcfMoney("20.00"), reversalWriteOffs.get(0).getFinancialTransactionAmount());
        assertEquals("Amount for EERevWriteOff", new SpcfMoney("90.00"), reversalWriteOffs.get(1).getFinancialTransactionAmount());

        PayrollServices.commitUnitOfWork();

    }

    /**
     * Ensure we can write off the reversal amounts and then the payroll amounts after reversals are returned
     */
    @Test
    public void testDDReversalReturnedAfterEEReturnTransferWriteOffReversalFirst() {
        //Load data and agent inits reversals
        ACHReturnsDataLoader.loadData2DayERNSFsAgentReversesPayroll();

        //Offload reversals
        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process returns on 1 reversals
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.addDaysToPSPTime(1);
        DomainEntitySet<TransactionReturn> reversalReturns = ACHReturnsDataLoader.loadFirstDDReversalReturn();
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);  //.setPSPTime("20070918000000");
        //Complete other reversal
        ProcessACHTransactions processACHtxns = new ProcessACHTransactions();
        processACHtxns.process(PSPDate.getPSPTime().format("yyyyMMdd")); //."20070918");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        ProcessResult eeRetTxfrResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        assertSuccess(eeRetTxfrResult);
        Application.commitUnitOfWork();

        //Offload EmployeeReturnTransfer
        OffloadACHTransactions offloader3 = new OffloadACHTransactions();
        offloader3.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process returns on other reversal
        PayrollServices.beginUnitOfWork();
        allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.setPSPTime("20070919000000");
        reversalReturns = ACHReturnsDataLoader.load2ndDDReversalReturn();
        assertEquals("One reversal return", 1, reversalReturns.size());
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        //Write off the reversal balance on the payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult abdWOProc2 = PayrollServices.financialTransactionManager.addEmployeeWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        assertSuccess(abdWOProc2);

        //Write off the remaining balance on the payroll
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        ProcessResult abdWOProc = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        assertSuccess(abdWOProc);
        //find one EmployerWriteOff with amount 30.00
        DomainEntitySet<FinancialTransaction> writeOffs = PayrollServices.entityFinder.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(company)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerWriteOff)
                        .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))));
        DomainEntitySet<FinancialTransaction> reversalWriteOffs = PayrollServices.entityFinder.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(company)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeReversalFailedWriteOff)
                        .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))));
        assertEquals("Number of write offs", 1, writeOffs.size());
        assertEquals("Number of reversal write offs", 1, reversalWriteOffs.size());
        FinancialTransaction employerWriteOff = writeOffs.get(0);
        assertEquals("Amount for ERWriteOff", new SpcfMoney("30.00"), employerWriteOff.getFinancialTransactionAmount());
        FinancialTransaction employeeReversalWriteOff = reversalWriteOffs.get(0);
        assertEquals("Amount for EERevWriteOff", new SpcfMoney("150.00"), employeeReversalWriteOff.getFinancialTransactionAmount());

        PayrollServices.commitUnitOfWork();
    }
}
