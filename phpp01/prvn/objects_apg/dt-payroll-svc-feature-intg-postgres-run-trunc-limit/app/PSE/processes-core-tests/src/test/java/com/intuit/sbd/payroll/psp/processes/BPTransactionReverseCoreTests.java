package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionReverseDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.TransactionReverseCoreDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * User: kpaul
 * Date: Dec 20, 2007
 * Time: 2:48:24 PM
 */
public class BPTransactionReverseCoreTests {


    @Before
    public void runBeforeEachTest() {
        TransactionReverseCoreDataLoader.loadPayrollRunForBPTransactionReverseTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Create a TransactionReverseDTO for use in the various tests.
     *
     * @param pBatchId     The payroll run id
     * @param pDdTxnIdList A list of transactions to reverse (if null, reverse all transactions for the given payroll run).
     * @return The new TransactionReverseDTO
     */
    private TransactionReverseDTO buildTransactionReverseDTO(String pBatchId, List<String> pDdTxnIdList) {
        TransactionReverseDTO dto = new TransactionReverseDTO();

        dto.setSourcePayrollRunId(pBatchId);
        dto.setDdTransactionIdList(pDdTxnIdList);
        dto.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        dto.setTxDate(null);
        dto.setChargeFee(false);
        dto.setIntuitInitiatedReversals(false);

        return dto;
    }

    /**
     * Test to ensure the core process will fail on invalid company id.
     * Rules:
     * - Cannot be null
     * - Valid length is 1..50
     * - Company id must exist in system.
     */
    @Test
    public void testInvalidCompanyId() {
        ProcessResult processResult = new ProcessResult();
        TransactionReverseDTO dto = buildTransactionReverseDTO("BatchId01", null);

        PayrollServices.beginUnitOfWork();
        {
            // invalid company id (null)
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, null, dto));

            // invalid company id (too short - valid range = 1..50)
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "", dto));

            // invalid company id (too long - valid range = 1..50)
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(
                    SourceSystemCode.QBDT, "123456789012345678901234567890123456789012345678901", dto));

            // invalid company id (does not exist)
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "INVALID", dto));
        }
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 4, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "138", message1.getMessageCode());
        Message message2 = processResult.getMessages().get(1);
        assertEquals("Error Code", "138", message2.getMessageCode());
        Message message3 = processResult.getMessages().get(2);
        assertEquals("Error Code", "138", message3.getMessageCode());
        Message message4 = processResult.getMessages().get(3);
        assertEquals("Error Code", "169", message4.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText1, message1.getMessage());
        String messageText2 = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText2, message2.getMessage());
        String messageText3 = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText3, message3.getMessage());
        String messageText4 = "Company QBDT:INVALID does not exist.";
        assertEquals("Error Message", messageText4, message4.getMessage());
    }

    /**
     * Test to ensure the core process will fail on invalid dto (null).
     */
    @Test
    public void testNullDto() {
        ProcessResult processResult = new ProcessResult();

        PayrollServices.beginUnitOfWork();
        // invalid dto (null)
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", null));
        PayrollServices.commitUnitOfWork();

        // validate error count
        Assert.assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code", "5002", message1.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Required 'TransactionReverseDTO' input is missing or blank";
        Assert.assertEquals("Error Message", messageText1, message1.getMessage());
    }

    /**
     * Test to ensure the core process will fail on invalid payroll run (DD Transaction Batch) id.
     * Rules:
     * - Cannot be null
     * - Length must be 1..50
     * - Payroll run id must exits in system.
     */
    @Test
    public void testInvalidDdTxBatchId() {
        ProcessResult processResult = new ProcessResult();
        TransactionReverseDTO dto = buildTransactionReverseDTO("BatchId01", null);

        PayrollServices.beginUnitOfWork();
        {
            // invalid dd transaction batch id (null)
            dto.setSourcePayrollRunId(null);
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));

            // invalid dd transaction batch id (too short - valid range = 1..50)
            dto.setSourcePayrollRunId("");
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));

            // invalid dd transaction batch id (too long - valid range = 1..50)
            dto.setSourcePayrollRunId("123456789012345678901234567890123456789012345678901");
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));

            // invalid dd transaction batch id (does not exist)
            dto.setSourcePayrollRunId("INVALID");
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        }
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 4, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "130", message1.getMessageCode());
        Message message2 = processResult.getMessages().get(1);
        assertEquals("Error Code", "130", message2.getMessageCode());
        Message message3 = processResult.getMessages().get(2);
        assertEquals("Error Code", "130", message3.getMessageCode());
        Message message4 = processResult.getMessages().get(3);
        assertEquals("Error Code", "194", message4.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Source Payroll Run ID is not specified.";
        assertEquals("Error Message", messageText1, message1.getMessage());
        String messageText2 = "Source Payroll Run ID is not specified.";
        assertEquals("Error Message", messageText2, message2.getMessage());
        String messageText3 = "Source Payroll Run ID is not specified.";
        assertEquals("Error Message", messageText3, message3.getMessage());
        String messageText4 = "Payroll Run with DDTxBatchID INVALID does not exist for company QBDT:123272727.";
        assertEquals("Error Message", messageText4, message4.getMessage());
    }

    /**
     * Test to ensure the core process will fail on invalid settlement type.
     * Invalid states are:
     * Settlement Type = null
     * Settlement Type = non-ACH, and the TxDate is null
     */
    @Test
    public void testInvalidSettlementType() {
        ProcessResult processResult = new ProcessResult();
        TransactionReverseDTO dto = buildTransactionReverseDTO("BatchId01", null);

        PayrollServices.beginUnitOfWork();
        {
            // invalid settlement type code (null)
            dto.setTxSettlementTypeCd(null);
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));

            // invalid: settlement type code is non-ach and txDate is null
            dto.setTxSettlementTypeCd(SettlementTypeDTO.Cash);
            dto.setTxDate(null);
            processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        }
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 2, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "11", message1.getMessageCode());
        Message message2 = processResult.getMessages().get(1);
        assertEquals("Error Code", "269", message2.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Invalid argument: null";
        assertEquals("Error Message", messageText1, message1.getMessage());
        String messageText2 = "Settlement Date not specified.";
        assertEquals("Error Message", messageText2, message2.getMessage());
    }

    /**
     * Test to ensure the core process will fail if the payroll status is not in a valid state to be cancelled.
     *
     * @param pPayrollStatus The invalid payroll status code to test.
     */
    private void testInvalidPayrollStatus(PayrollStatus pPayrollStatus) {
        ProcessResult processResult = new ProcessResult();


        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);
        // set payroll run status to given (invalid) state
        payrollRun.setPayrollRunStatus(pPayrollStatus);

        Application.save(payrollRun);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error code
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "1048", message1.getMessageCode());

        // Verify that the correct message string was returned
        String messageText1 = "Action DDTransactionReverse is not valid for payroll run with DDTxBatchID " + payrollRunId + ", which has status of " + pPayrollStatus.toString() + ".";
        assertEquals("Error Message", messageText1, message1.getMessage());
    }

    /**
     * Test to ensure the core process will fail on invalid payroll status.
     * Invalid statuses are:
     * - Canceled
     * - DebitReturnedCanceled
     * - NSFCanceled
     * - OffloadedDebit
     * - Pending
     * - WrittenOff
     */
    @Test
    public void testInvalidPayrollStatus() {
        testInvalidPayrollStatus(PayrollStatus.Canceled);
        testInvalidPayrollStatus(PayrollStatus.DebitReturnedCanceled);
        testInvalidPayrollStatus(PayrollStatus.NSFCanceled);
        testInvalidPayrollStatus(PayrollStatus.OffloadedDebit);
        testInvalidPayrollStatus(PayrollStatus.Pending);
        testInvalidPayrollStatus(PayrollStatus.WrittenOff);

        // Don't test these status codes since they're valid
//        testInvalidPayrollStatus(PayrollStatus.Complete);
//        testInvalidPayrollStatus(PayrollStatus.DebitReturned);
//        testInvalidPayrollStatus(PayrollStatus.DebitReturnedRedebitOffloaded);
//        testInvalidPayrollStatus(PayrollStatus.DebitReturnedRedebitPending);
//        testInvalidPayrollStatus(PayrollStatus.NSF);
//        testInvalidPayrollStatus(PayrollStatus.NSFRedebitOffloaded);
//        testInvalidPayrollStatus(PayrollStatus.NSFRedebitPending);
//        testInvalidPayrollStatus(PayrollStatus.NSFTwice);
//        testInvalidPayrollStatus(PayrollStatus.OffloadedAll);
    }

    /**
     * Test to ensure the core process will fail on invalid transaction (settlement) date.
     * Valid range is: -45 days <= TxDate <= today
     *
     * @param pSettlementType The non-ACH settlement type to test
     */
    private void testInvalidTxDateForNonAchTransaction(SettlementTypeDTO pSettlementType) {
        ProcessResult processResult = new ProcessResult();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);
        SpcfCalendar txDatePlus1 = PSPDate.getPSPTime();
        SpcfCalendar txDateMinus46 = PSPDate.getPSPTime();

        txDatePlus1.addDays(1);
        txDateMinus46.addDays(-46);

        dto.setTxSettlementTypeCd(pSettlementType);

        PayrollServices.beginUnitOfWork();
        // set invalid transaction date to 1 day in the future
        dto.setTxDate(CalendarUtils.convertToCalendar(txDatePlus1));
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRuns(company).get(0);


        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // set invalid transaction date to 46 days in the past
        dto.setTxDate(CalendarUtils.convertToCalendar(txDateMinus46));
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 2, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "266", message1.getMessageCode());
        Message message2 = processResult.getMessages().get(1);
        assertEquals("Error Code", "271", message2.getMessageCode());

        // Verify that the correct message strings were returned
        SimpleDateFormat fDate = new SimpleDateFormat("M/d/yyyy");
        String messageText1 = "Cannot record a transaction of Settlement Type " +
                pSettlementType.toString() +
                " with the future date " +
                fDate.format(CalendarUtils.convertToCalendar(txDatePlus1).getTime()) + ".";
        assertEquals("Error Message", messageText1, message1.getMessage());
        String messageText2 = "Cannot record a transaction of Settlement Type " +
                pSettlementType.toString() +
                " and date " +
                fDate.format(CalendarUtils.convertToCalendar(txDateMinus46).getTime()) +
                ", which is more than 45 days in the past.";
        assertEquals("Error Message", messageText2, message2.getMessage());
    }

    /**
     * Test to ensure the core process will fail on invalid transaction (settlement) date.
     * (the given txDate is only used for non-ACH transactions)
     */
    @Test
    public void testInvalidTxDate() {
        testInvalidTxDateForNonAchTransaction(SettlementTypeDTO.Cash);
        testInvalidTxDateForNonAchTransaction(SettlementTypeDTO.CheckType);
        testInvalidTxDateForNonAchTransaction(SettlementTypeDTO.Other);
        testInvalidTxDateForNonAchTransaction(SettlementTypeDTO.Wire);

        // Don't test settlement type of ACH since it bypasses txn date validation
        //testInvalidTxDateForNonAchTransaction(SettlementTypeDTO.ACH);
    }

    /**
     * Test to ensure the core process will fail if the no active company bank account exists.
     */
    @Test
    public void testInvalidCompanyBankAccount() {
        ProcessResult processResult = new ProcessResult();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);

        dto.setChargeFee(true);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        CompanyBankAccount companyBankAccount =
                CompanyBankAccount.findCompanyBankAccount(company, "123123");

        // set the bank account status to inactive
        PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBDT, "123272727",
                companyBankAccount.getSourceBankAccountId(), true, false);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "1062", message1.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Company QBDT:123272727 does not have an active bank account.";
        assertEquals("Error Message", messageText1, message1.getMessage());
    }

    /**
     * Test to ensure the core process will fail on invalid source transaction id.
     * Rules:
     * - Cannot be null
     * - length must be 1..50
     * - Transaction must exist in system.
     */
    @Test
    public void testInvalidSourceTransactionId() {
        String txn1, txn2, txn3;
        List<String> dtoTxnList = new Vector<String>(3);
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, dtoTxnList);

        ///////////////////////////////////////////////////////////////////////
        // Test source transaction id(s) that are structurally invalid
        // (this will fail validation at the dto)
        ///////////////////////////////////////////////////////////////////////
        ProcessResult processResult1 = new ProcessResult();

        // invalid source transaction id (null)
        txn1 = null;
        dtoTxnList.add(txn1);

        // invalid source transaction id (empty)
        txn2 = "";
        dtoTxnList.add(txn2);

        // invalid source transaction id (too long)
        txn3 = "123456789012345678901234567890123456789012345678901";
        dtoTxnList.add(txn3);

        PayrollServices.beginUnitOfWork();
        processResult1.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 3, processResult1.getMessages().size());

        // validate error codes
        Message message1 = processResult1.getMessages().get(0);
        assertEquals("Error Code", "11", message1.getMessageCode());
        Message message2 = processResult1.getMessages().get(1);
        assertEquals("Error Code", "11", message2.getMessageCode());
        Message message3 = processResult1.getMessages().get(2);
        assertEquals("Error Code", "11", message3.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Invalid argument: " + txn1;
        assertEquals("Error Message", messageText1, message1.getMessage());
        String messageText2 = "Invalid argument: " + txn2;
        assertEquals("Error Message", messageText2, message2.getMessage());
        String messageText3 = "Invalid argument: " + txn3;
        assertEquals("Error Message", messageText3, message3.getMessage());

        ///////////////////////////////////////////////////////////
        // Test source transaction id(s) that do not exist
        // (this will fail validation within the core process)
        ///////////////////////////////////////////////////////////
        ProcessResult processResult2 = new ProcessResult();

        dtoTxnList.clear();

        // invalid source transaction id (does not exist)
        txn1 = "INVALID1";
        dtoTxnList.add(txn1);

        // invalid source transaction id (does not exist)
        txn2 = "INVALID2";
        dtoTxnList.add(txn2);

        // invalid source transaction id (does not exist)
        txn3 = "INVALID3";
        dtoTxnList.add(txn3);

        PayrollServices.beginUnitOfWork();
        processResult2.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 3, processResult2.getMessages().size());

        // validate error codes
        message1 = processResult2.getMessages().get(0);
        assertEquals("Error Code", "195", message1.getMessageCode());
        message2 = processResult2.getMessages().get(1);
        assertEquals("Error Code", "195", message2.getMessageCode());
        message3 = processResult2.getMessages().get(2);
        assertEquals("Error Code", "195", message3.getMessageCode());

        // Verify that the correct message strings were returned
        messageText1 = "Transaction " + txn1 + " does not exist for company QBDT:123272727.";
        assertEquals("Error Message", messageText1, message1.getMessage());
        messageText2 = "Transaction " + txn2 + " does not exist for company QBDT:123272727.";
        assertEquals("Error Message", messageText2, message2.getMessage());
        messageText3 = "Transaction " + txn3 + " does not exist for company QBDT:123272727.";
        assertEquals("Error Message", messageText3, message3.getMessage());
    }

    /**
     * Test to ensure the core process will fail if any requested transactions to be reversed are in an invalid state.
     * Rules:
     * Transaction in a CREATED state cannot be reversed.
     * Transaction in a CANCELLED state cannot be reversed.
     * Transaction in a RETURNED state cannot be reversed.
     * Transactions in any other state can be reversed unless:
     * 1. There is already a reversal transction associated with the given transaction AND
     * 2. The reversal transaction is not CANCELLED or VOIDED.
     */
    @Ignore
    @Test
    public void testAttemptedReversalForInvalidTransactionStates() {
        ProcessResult processResult = new ProcessResult();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);
        List<String> dtoTxnList = new Vector<String>(4);

        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
            DomainEntitySet<FinancialTransaction> txnList =
                    payrollRun.getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                            null);

            int ctr = 0;
            for (FinancialTransaction txn : txnList) {
                switch (++ctr) {
                    case 1:
                        dtoTxnList.add(txn.getBillPaymentSplit().getSourceId());
                        txn.updateFinancialTransactionState(TransactionStateCode.Created);
                        break;
                    case 2:
                        dtoTxnList.add(txn.getBillPaymentSplit().getSourceId());
                        txn.updateFinancialTransactionState(TransactionStateCode.Cancelled);
                        break;
                    case 3:
                        dtoTxnList.add(txn.getBillPaymentSplit().getSourceId());
                        txn.updateFinancialTransactionState(TransactionStateCode.Returned);
                        break;
                    case 4:
                        dtoTxnList.add(txn.getBillPaymentSplit().getSourceId());
                        FinancialTransaction reversalTxn = FinancialTransaction.createFinancialTransaction(
                                company,
                                payrollRun,
                                null,   // paycheck split
                                null,   // credit bank account
                                null,   // debit bank account
                                null,   // credit bank account type
                                null,   // debit bank account type
                                TransactionTypeCode.EmployeeDdReversalDebit,
                                txn.getFinancialTransactionAmount(),
                                SettlementType.Cash,
                                txn.getSettlementDate().copy());

                        // Update the new reversal txn to EXECUTED and then COMPLETED (so the ledger is kept in sync)
                        reversalTxn.updateFinancialTransactionState(TransactionStateCode.Executed);
                        reversalTxn.updateFinancialTransactionState(TransactionStateCode.Completed);

                        // Associate new reversal txn with the original txn
                        // (identifies the original financial transaction as having a reversal against it)
                        reversalTxn.setOriginalTransaction(txn);
                        txn.addAssociatedTransactions(reversalTxn);

                        Application.save(reversalTxn);
                        break;
                    default:
                        break;
                }
            }

            dto.setDdTransactionIdList(dtoTxnList);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "260", message1.getMessageCode());
//        Message message2 = processResult.getMessages().get(1);
//        assertEquals("Error Code", "261", message2.getMessageCode());
//        Message message3 = processResult.getMessages().get(2);
//        assertEquals("Error Code", "262", message3.getMessageCode());
//        Message message4 = processResult.getMessages().get(3);
//        assertEquals("Error Code", "263", message4.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Transaction " + dtoTxnList.get(0) + " cannot be reversed, because it is still pending.";
        assertEquals("Error Message", messageText1, message1.getMessage());
//        String messageText2 = "Transaction " + dtoTxnList.get(1) + " cannot be reversed, because it was previously canceled.";
//        assertEquals("Error Message", messageText2, message2.getMessage());
//        String messageText3 = "Transaction " + dtoTxnList.get(2) + " cannot be reversed, because an ACH Return was received for this transaction.";
//        assertEquals("Error Message", messageText3, message3.getMessage());
//        String messageText4 = "Transaction " + dtoTxnList.get(3) + " cannot be reversed, because a reversal has already been attempted.";
//        assertEquals("Error Message", messageText4, message4.getMessage());
    }

    /**
     * Verify that a request to perform an ACH reversal of a transaction will succeed and assess a fee.
     */
    @Test
    public void testAchReversalOfSingleTransactionWithFee() {
        ProcessResult processResult = new ProcessResult();
        List<String> dtoTxnList = new Vector<String>(4);
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);

        // Retrieve a list of paycheck split transacitons and use the first one found
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);

            // any transaction will for for this test, so use the first one in the list.
            dtoTxnList.add(txnList.get(0).getBillPaymentSplit().getSourceId());

            dto.setDdTransactionIdList(dtoTxnList);
            dto.setChargeFee(true);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // If any errors do occur, write them out to stdout before the assertion...
        if (!processResult.isSuccess()) {
            for (Message msg : processResult.getMessages()) {
                System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
            }
        }

        // validate error count
        assertEquals("Number of Errors", 0, processResult.getMessages().size());

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
            IntuitBankAccount intuitReversalBankAccount = IntuitBankAccount.
                    findIntuitBankAccount(TransactionTypeCode.EmployeeDdReversalDebit, CreditDebitCode.Credit);
            IntuitBankAccount intuitReversalFeeBankAccount = IntuitBankAccount.
                    findIntuitBankAccount(TransactionTypeCode.EmployerFeeDebit, CreditDebitCode.Credit);
            CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);
            SpcfCalendar expectedSettlementDate = TransactionReverseCoreDataLoader.prTxnReverseSubmitDate.copy();
            SpcfCalendar txnSettlementDate;
            CalendarUtils.addBusinessDays(expectedSettlementDate, 1);
            CalendarUtils.clearTime(expectedSettlementDate);

            // verify first bill payment split transaction was reversed (and no others).
            boolean foundReversedTransactionAlready = false;
            FinancialTransaction reversalTxn = null;
            for (FinancialTransaction txn : txnList) {
                if (foundReversedTransactionAlready) {
                    // verify no other txns were affected
                    assertEquals("Associated txn count", 0, txn.getAssociatedTransactionsCollection().size());
                } else {
                    if (txn.getAssociatedTransactionsCollection().size() > 0) {
                        foundReversedTransactionAlready = true;

                        assertEquals("Associated txn count", 1, txn.getAssociatedTransactionsCollection().size());

                        reversalTxn = txn.getAssociatedTransactionsCollection().iterator().next();
                        FinancialTransaction originalTxn = reversalTxn.getOriginalTransaction();

                        // verify the reversal txn is associated with the original txn
                        assertNotNull("Original txn", originalTxn);
                        assertEquals("Reversal txn association", txn.getId(), originalTxn.getId());

                        // verify the credit account is intuit
                        assertEquals("Reversed txn credit account",
                                intuitReversalBankAccount.getBankAccount().getId(),
                                reversalTxn.getCreditBankAccount().getId());

                        // verify the debit account is from the original txn credit account
                        assertEquals("Reversed txn debit account",
                                originalTxn.getCreditBankAccount().getId(),
                                reversalTxn.getDebitBankAccount().getId());

                        // verify the associated txn is indeed a reversal transaction
                        assertEquals("Reversal txn type",
                                TransactionTypeCode.EmployeeDdReversalDebit,
                                reversalTxn.getTransactionType().getTransactionTypeCd());

                        // verify the reversal amount
                        assertEquals("Reversed txn amount",
                                originalTxn.getFinancialTransactionAmount(),
                                reversalTxn.getFinancialTransactionAmount());

                        // verify the reversal txn settlement type is ACH
                        assertEquals("Reversed txn settlement type",
                                SettlementType.ACH,
                                reversalTxn.getSettlementTypeCd());

                        // verify the settlement date for the reversal txn
                        txnSettlementDate = reversalTxn.getSettlementDate().toLocal().copy();
                        CalendarUtils.clearTime(txnSettlementDate);
                        assertEquals("Reversed txn settlement date", expectedSettlementDate, txnSettlementDate);
                    }
                }
            }

            // verify the fee txn was created
            txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                    new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("Reversal fee txn", 1, txnList.size());

            FinancialTransaction feeTxn = txnList.get(0);

            // verify the credit account is intuit
            assertEquals("Fee txn credit account",
                    intuitReversalFeeBankAccount.getBankAccount().getId(),
                    feeTxn.getCreditBankAccount().getId());

            // verify the debit account is the company
            assertEquals("Fee txn debit account",
                    companyBankAccount.getBankAccount().getId(),
                    feeTxn.getDebitBankAccount().getId());

            // verify the settlement type is ACH
            assertEquals("Fee txn settlement type",
                    SettlementType.ACH,
                    feeTxn.getSettlementTypeCd());

            // verify the SKU type
            //assertEquals("SKU:", "408177", feeTxn.getSku()); // reversal fee sku for "QBDT DD" offering

            // verify the SKU quantity
            assertTrue("SKU:", 1 == feeTxn.getSkuQuantity());

            // verify the settlement date for the fee txn
            txnSettlementDate = feeTxn.getSettlementDate().toLocal().copy();
            //CalendarUtils.clearTime(txnSettlementDate);

            // for ACH-settled fees, this should be based on the company's DD offload group
            SpcfCalendar settlementDate = null;
            PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
            settlementDate = FinancialTransaction.getSettlementDate(company.getOffloadGroup());
            PSPDate.resetPSPTime();

            assertEquals("Fee txn settlement date", settlementDate.toLocal(), txnSettlementDate);

            // verify the transaction response was created correctly for the fee txn
            DomainEntitySet<TransactionResponse> txnResponseList =
                    TransactionResponse.findTransactionResponses(feeTxn);
            assertEquals("Transaction response count for fee txn", 1, txnResponseList.size());

            // verify Reversal Requested event
            DomainEntitySet<CompanyEvent> companyEvents =
                    CompanyEvent.findCompanyEvents(company, EventTypeCode.ReversalRequested, null, null, null);
            assertEquals("Number of Reversal Requested Events", 1, companyEvents.size());

            assertEquals("Event status", CompanyEventStatus.Active, companyEvents.get(0).getStatusCd());
            // verify the details
            DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(0).getCompanyEventDetailCollection();
            eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

            CompanyService companyService = company.getCompanyService(ServiceCode.BillPayment);

            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CompanyServiceId, eventDetails.get(0).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", companyService.getId().toString(), eventDetails.get(0).getValue());
            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.FinancialTransactionId, eventDetails.get(1).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", reversalTxn.getId().toString(), eventDetails.get(1).getValue());
            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.IntuitInitiated, eventDetails.get(2).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", Boolean.FALSE.toString(), eventDetails.get(2).getValue());

        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to perform a non-ACH reversal of a transaction will succeed and assess a fee.
     *
     * @param pSettlementType The non-ACH settlement type to test
     */
    private void testNonAchReversalOfSingleTransactionWithFee(SettlementTypeDTO pSettlementType) {
        ProcessResult processResult = new ProcessResult();
        List<String> dtoTxnList = new Vector<String>(4);
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);

        SpcfCalendar settlementDate = TransactionReverseCoreDataLoader.prTxnReverseSubmitDate.copy();

        // set settlement date 10 calendar days in the past
        settlementDate.addDays(-10); // Rule: -45 days <= txnDate <= today (PSPDate.getPSPTime())
        CalendarUtils.clearTime(settlementDate);

        // Retrieve a list of paycheck split transacitons and use the first one found
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);

            payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);

            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);

            // any transaction will for for this test, so use the first one in the list.
            dtoTxnList.add(txnList.get(0).getBillPaymentSplit().getSourceId());
            System.out.println("dtoTxnList " + txnList.get(0).getBillPaymentSplit().getSourceId());
            dto.setDdTransactionIdList(dtoTxnList);
            dto.setChargeFee(true);
            dto.setTxSettlementTypeCd(pSettlementType);
            dto.setTxDate(CalendarUtils.convertToCalendar(settlementDate));
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // If any errors do occur, write them out to stdout before the assertion...
        if (!processResult.isSuccess()) {
            for (Message msg : processResult.getMessages()) {
                System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
            }
        }

        // validate error count
        assertEquals("Number of Errors", 0, processResult.getMessages().size());

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
            IntuitBankAccount intuitReversalFeeBankAccount = IntuitBankAccount.
                    findIntuitBankAccount(
                            TransactionTypeCode.EmployerFeeDebit, CreditDebitCode.Credit);
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);
            SpcfCalendar txnSettlementDate;

            // verify first paycheck split transaction was reversed (and no others).
            //boolean isFirstTxn = true;
            FinancialTransaction reversalTxn = null;
            for (FinancialTransaction txn : txnList) {
                System.out.println("Fin Txn   : " + txn.getId() + " - " + txn.getCurrentTransactionState().getTransactionStateCd());
                if (txn.getBillPaymentSplit().getSourceId().equals(dtoTxnList.get(0))) {
                    //if (isFirstTxn) {
                    //        isFirstTxn = false;

                    assertEquals("Associated txn count", 1, txn.getAssociatedTransactionsCollection().size());

                    reversalTxn = txn.getAssociatedTransactionsCollection().iterator().next();
                    FinancialTransaction originalTxn = reversalTxn.getOriginalTransaction();

                    // verify the reversal txn is associated with the original txn
                    assertNotNull("Original txn", originalTxn);
                    assertEquals("Reversal txn association", txn.getId(), originalTxn.getId());

                    // verify the credit account is null
                    assertNull("Reversed txn credit account", reversalTxn.getCreditBankAccount());

                    // verify the debit account is null
                    assertNull("Reversed txn debit account", reversalTxn.getDebitBankAccount());

                    // verify the associated txn is indeed a reversal transaction
                    assertEquals("Reversal txn type",
                            TransactionTypeCode.EmployeeDdReversalDebit,
                            reversalTxn.getTransactionType().getTransactionTypeCd());

                    // verify the reversal amount
                    assertEquals("Reversed txn amount",
                            originalTxn.getFinancialTransactionAmount(),
                            reversalTxn.getFinancialTransactionAmount());

                    // verify the reversal txn settlement type is as requested
                    assertEquals("Reversed txn settlement type",
                            SettlementType.valueOf(pSettlementType.toString()),
                            reversalTxn.getSettlementTypeCd());

                    // verify the settlement date for the reversal txn
                    txnSettlementDate = reversalTxn.getSettlementDate().toLocal().copy();
                    CalendarUtils.clearTime(txnSettlementDate);
                    assertEquals("Reversed txn settlement date", settlementDate, txnSettlementDate);
                } else { // verify no other txns were affected
                    assertEquals("Associated txn count", 0, txn.getAssociatedTransactionsCollection().size());
                }
            }

            // verify the fee txn was created
            txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                    new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("Reversal fee txn", 1, txnList.size());

            FinancialTransaction feeTxn = txnList.get(0);

            // verify the credit account is intuit
            assertEquals("Fee txn credit account",
                    intuitReversalFeeBankAccount.getBankAccount().getId(),
                    feeTxn.getCreditBankAccount().getId());

            // verify the debit account is the company
            assertEquals("Fee txn debit account",
                    companyBankAccount.getBankAccount().getId(),
                    feeTxn.getDebitBankAccount().getId());

            // verify the settlement type is ACH
            assertEquals("Fee txn settlement type",
                    SettlementType.ACH,
                    feeTxn.getSettlementTypeCd());

            // verify the SKU type
        //    assertEquals("SKU:", "408177", feeTxn.getSku()); // reversal fee sku for "QBDT DD" offering

            // verify the SKU quantity
            assertTrue("SKU:", 1 == feeTxn.getSkuQuantity());

            // verify the settlement date for the fee txn
            txnSettlementDate = feeTxn.getSettlementDate().toLocal().copy();
            CalendarUtils.clearTime(txnSettlementDate);

            // for ACH-settled fees, this should be based on the company's DD offload group
            settlementDate = null;
            PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
            settlementDate = FinancialTransaction.getSettlementDate(company.getOffloadGroup());
            PSPDate.resetPSPTime();

            assertEquals("Fee txn settlement date", settlementDate.toLocal(), txnSettlementDate);

            // verify the transaction response was created correctly for the fee txn
            DomainEntitySet<TransactionResponse> txnResponseList =
                    TransactionResponse.findTransactionResponses(feeTxn);
            assertEquals("Transaction response count for fee txn", 1, txnResponseList.size());

            // verify Reversal Requested event
            DomainEntitySet<CompanyEvent> companyEvents =
                    CompanyEvent.findCompanyEvents(company, EventTypeCode.ReversalRequested, null, null, null);
            assertEquals("Number of Reversal Requested Events", 1, companyEvents.size());

            assertEquals("Event status", CompanyEventStatus.Active, companyEvents.get(0).getStatusCd());
            // verify the details
            DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(0).getCompanyEventDetailCollection();
            eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

            CompanyService companyService = company.getCompanyService(ServiceCode.BillPayment);

            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CompanyServiceId, eventDetails.get(0).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", companyService.getId().toString(), eventDetails.get(0).getValue());
            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.FinancialTransactionId, eventDetails.get(1).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", reversalTxn.getId().toString(), eventDetails.get(1).getValue());
            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.IntuitInitiated, eventDetails.get(2).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", Boolean.FALSE.toString(), eventDetails.get(2).getValue());
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to perform a non-ACH reversal of a transaction will succeed and assess a fee.
     */
    @Test
    public void testCashReversalOfSingleTransactionWithFee() {
        testNonAchReversalOfSingleTransactionWithFee(SettlementTypeDTO.Cash);

        // Do not test settlement type of ACH since it is (by definition) not in the scope of this test.
        //testNonAchReversalOfSingleTransactionWithFee(SettlementTypeDTO.ACH);
    }

    @Test
    public void testCheckTypeReversalOfSingleTransactionWithFee() {
        testNonAchReversalOfSingleTransactionWithFee(SettlementTypeDTO.CheckType);
    }

    @Test
    public void testOtherReversalOfSingleTransactionWithFee() {
        testNonAchReversalOfSingleTransactionWithFee(SettlementTypeDTO.Other);
    }

    @Test
    public void testWireReversalOfSingleTransactionWithFee() {
        testNonAchReversalOfSingleTransactionWithFee(SettlementTypeDTO.Wire);
    }

    /**
     * Verify that a request to perform an ACH reversal of all transactions (implicit) will succeed.
     */
    @Test
    public void testImplicitAchReversalOfAllTransactions() {
        ProcessResult processResult = new ProcessResult();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // If any errors do occur, write them out to stdout before the assertion...
        if (!processResult.isSuccess()) {
            for (Message msg : processResult.getMessages()) {
                System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
            }
        }

        // validate error count
        assertEquals("Number of Errors", 0, processResult.getMessages().size());

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
        // make sure payroll status is not updated for non-intuit initiated reversals
        assertTrue("Payroll Status", PayrollStatus.PendingReversals != payrollRun.getPayrollRunStatus());
        IntuitBankAccount intuitReversalBankAccount =
                IntuitBankAccount.findIntuitBankAccount(
                        TransactionTypeCode.EmployeeDdReversalDebit,
                        CreditDebitCode.Credit);
        DomainEntitySet<FinancialTransaction> txnList =
                FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);
        SpcfCalendar expectedSettlementDate = TransactionReverseCoreDataLoader.prTxnReverseSubmitDate.copy();
        SpcfCalendar txnSettlementDate;

        CalendarUtils.addBusinessDays(expectedSettlementDate, 1);
        CalendarUtils.clearTime(expectedSettlementDate);

        // verify Reversal Requested event
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.ReversalRequested, null, null, null);
        assertEquals("Number of Reversal Requested Events", 28, companyEvents.size());
        int i = 0;
        List<String> eventDetailTxns = new ArrayList<String>();
        List<String> reverseTxns = new ArrayList<String>();

        // verify all paycheck split transactions were reversed.
        for (FinancialTransaction txn : txnList) {
            assertEquals("Associated txn count", 1, txn.getAssociatedTransactionsCollection().size());

            FinancialTransaction reversalTxn = txn.getAssociatedTransactionsCollection().iterator().next();
            FinancialTransaction originalTxn = reversalTxn.getOriginalTransaction();

            // verify the reversal txn is associated with the original txn
            assertNotNull("Original txn", originalTxn);
            assertEquals("Reversal txn association", txn.getId(), originalTxn.getId());

            // verify the credit account is intuit
            assertEquals("Reversed txn credit account",
                    intuitReversalBankAccount.getBankAccount().getId(),
                    reversalTxn.getCreditBankAccount().getId());

            // verify the debit account is from the original txn credit account
            assertEquals("Reversed txn debit account",
                    originalTxn.getCreditBankAccount().getId(),
                    reversalTxn.getDebitBankAccount().getId());

            // verify the associated txn is indeed a reversal transaction
            assertEquals("Reversal txn type",
                    TransactionTypeCode.EmployeeDdReversalDebit,
                    reversalTxn.getTransactionType().getTransactionTypeCd());

            // verify the reversal amount
            assertEquals("Reversed txn amount",
                    originalTxn.getFinancialTransactionAmount(),
                    reversalTxn.getFinancialTransactionAmount());

            // verify the reversal txn settlement type is ACH
            assertEquals("Reversed txn settlement type",
                    SettlementType.ACH,
                    reversalTxn.getSettlementTypeCd());

            // verify the settlement date for the reversal txn
            txnSettlementDate = reversalTxn.getSettlementDate().toLocal().copy();
            CalendarUtils.clearTime(txnSettlementDate);
            assertEquals("Reversed txn settlement date", expectedSettlementDate, txnSettlementDate);

            assertEquals("Event status", CompanyEventStatus.Active, companyEvents.get(i).getStatusCd());
            // verify the details
            DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(i++).getCompanyEventDetailCollection();
            eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

            CompanyService companyService = company.getCompanyService(ServiceCode.BillPayment);

            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CompanyServiceId, eventDetails.get(0).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", companyService.getId().toString(), eventDetails.get(0).getValue());
            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.FinancialTransactionId, eventDetails.get(1).getEventDetailTypeCd());
            // Don't compare here, since company events can be in different order
            // Assert.assertEquals("Event Detail Value", reversalTxn.getId().toString(), eventDetails.get(0).getValue());
            reverseTxns.add(reversalTxn.getId().toString());
            eventDetailTxns.add(eventDetails.get(1).getValue());
            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.IntuitInitiated, eventDetails.get(2).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", Boolean.FALSE.toString(), eventDetails.get(2).getValue());
        }
        // Make sure event details financial transaction ids are same as the reversed transaction ids
        assertTrue("Financial Transaction Ids", reverseTxns.containsAll(eventDetailTxns));
        assertTrue("Financial Transaction Ids", eventDetailTxns.containsAll(reverseTxns));
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to perform an ACH reversal of all transactions (implicit) will succeed.
     */
    @Test
    public void testImplicitAchReversal_IntuitInitiated() {
        //create a Transction Return for
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();

        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R01", "NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 14, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
        // verify the fee txn was created
        DomainEntitySet<FinancialTransaction> feeTxnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[] {TransactionStateCode.Created});

        assertEquals("Reversal fee txn", 1, feeTxnList.size());
        PayrollServices.commitUnitOfWork();

        assertEquals("Payroll run status is PendingAutoRedebit", PayrollStatus.PendingAutoRedebit, payrollRun.getPayrollRunStatus());

        ProcessResult processResult = new ProcessResult();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);

        dto.setIntuitInitiatedReversals(true);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
        DomainEntitySet<FinancialTransaction> cancelledRedebits = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        // verify the Redebit transaction is cancelled
        assertEquals("Number of ACH ER DD Redebit Financial Txs", 1, cancelledRedebits.size());
        for (FinancialTransaction currDDRedebit : cancelledRedebits) {
            assertEquals("Payroll Run Id ", payrollRunId, currDDRedebit.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, currDDRedebit.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Settlement Type ", SettlementType.ACH, currDDRedebit.getSettlementTypeCd());
        }
        // verify payroll run
        assertEquals("PayrollRun status", PayrollStatus.PendingReversals, payrollRun.getPayrollRunStatus());
        IntuitBankAccount intuitReversalBankAccount =
                IntuitBankAccount.findIntuitBankAccount(
                        TransactionTypeCode.EmployeeDdReversalDebit,
                        CreditDebitCode.Credit);
        DomainEntitySet<FinancialTransaction> txnList =
                FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);
        SpcfCalendar expectedSettlementDate = TransactionReverseCoreDataLoader.prTxnReverseSubmitDate.copy();
        SpcfCalendar txnSettlementDate;

        CalendarUtils.addBusinessDays(expectedSettlementDate, 1);
        CalendarUtils.clearTime(expectedSettlementDate);

        // verify no new fee txns were created
        feeTxnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[] {TransactionStateCode.Created});

        assertEquals("Reversal fee txn", 1, feeTxnList.size());

        // verify Reversal Requested event
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.ReversalRequested, null, null, null);
        assertEquals("Number of Reversal Requested Events", 28, companyEvents.size());
        int i = 0;
        List<String> eventDetailTxns = new ArrayList<String>();
        List<String> reverseTxns = new ArrayList<String>();

        // verify all paycheck split transactions were reversed.
        for (FinancialTransaction txn : txnList) {
            assertEquals("Associated txn count", 1, txn.getAssociatedTransactionsCollection().size());

            FinancialTransaction reversalTxn = txn.getAssociatedTransactionsCollection().iterator().next();
            FinancialTransaction originalTxn = reversalTxn.getOriginalTransaction();

            // verify the reversal txn is associated with the original txn
            assertNotNull("Original txn", originalTxn);
            assertEquals("Reversal txn association", txn.getId(), originalTxn.getId());

            // verify the credit account is intuit
            assertEquals("Reversed txn credit account",
                    intuitReversalBankAccount.getBankAccount().getId(),
                    reversalTxn.getCreditBankAccount().getId());

            // verify the debit account is from the original txn credit account
            assertEquals("Reversed txn debit account",
                    originalTxn.getCreditBankAccount().getId(),
                    reversalTxn.getDebitBankAccount().getId());

            // verify the associated txn is indeed a reversal transaction
            assertEquals("Reversal txn type",
                    TransactionTypeCode.EmployeeDdReversalDebit,
                    reversalTxn.getTransactionType().getTransactionTypeCd());

            // verify the reversal amount
            assertEquals("Reversed txn amount",
                    originalTxn.getFinancialTransactionAmount(),
                    reversalTxn.getFinancialTransactionAmount());

            // verify the reversal txn settlement type is ACH
            assertEquals("Reversed txn settlement type",
                    SettlementType.ACH,
                    reversalTxn.getSettlementTypeCd());

            // verify the settlement date for the reversal txn
            txnSettlementDate = reversalTxn.getSettlementDate().toLocal().copy();
            CalendarUtils.clearTime(txnSettlementDate);
            assertEquals("Reversed txn settlement date", expectedSettlementDate, txnSettlementDate);

            // verify the details
            DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(i++).getCompanyEventDetailCollection();
            eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

            CompanyService companyService = company.getCompanyService(ServiceCode.BillPayment);

            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CompanyServiceId, eventDetails.get(0).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", companyService.getId().toString(), eventDetails.get(0).getValue());
            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.FinancialTransactionId, eventDetails.get(1).getEventDetailTypeCd());
            // Don't compare here, since company events can be in different order
            // Assert.assertEquals("Event Detail Value", reversalTxn.getId().toString(), eventDetails.get(0).getValue());
            reverseTxns.add(reversalTxn.getId().toString());
            eventDetailTxns.add(eventDetails.get(1).getValue());
            Assert.assertEquals("Event Detail Code", EventDetailTypeCode.IntuitInitiated, eventDetails.get(2).getEventDetailTypeCd());
            Assert.assertEquals("Event Detail Value", Boolean.TRUE.toString(), eventDetails.get(2).getValue());
        }
        // Make sure event details financial transaction ids are same as the reversed transaction ids
        assertTrue("Financial Transaction Ids", reverseTxns.containsAll(eventDetailTxns));
        assertTrue("Financial Transaction Ids", eventDetailTxns.containsAll(reverseTxns));
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to perform an ACH reversal of all transactions (explicit) will succeed.
     */
    @Test
    public void testExplicitAchReversalOfAllTransactions() {
        ProcessResult processResult = new ProcessResult();
        List<String> dtoTxnList = new Vector<String>(4);
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);

        // Retrieve a list of all paycheck split transacitons and add them to the dto be reversed
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);

            for (FinancialTransaction txn : txnList) {
                dtoTxnList.add(txn.getPaycheckSplit().getSourceDdTxnId());
            }

            dto.setDdTransactionIdList(dtoTxnList);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
            // make sure payroll status is not updated for non-intuit initiated reversals
            assertTrue("Payroll Status", PayrollStatus.PendingReversals != payrollRun.getPayrollRunStatus());
            IntuitBankAccount intuitReversalBankAccount =
                    IntuitBankAccount.findIntuitBankAccount(
                            TransactionTypeCode.EmployeeDdReversalDebit,
                            CreditDebitCode.Credit);
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);
            SpcfCalendar expectedSettlementDate = TransactionReverseCoreDataLoader.prTxnReverseSubmitDate.copy();
            SpcfCalendar txnSettlementDate;

            CalendarUtils.addBusinessDays(expectedSettlementDate, 1);
            CalendarUtils.clearTime(expectedSettlementDate);

            // verify Reversal Requested event
            DomainEntitySet<CompanyEvent> companyEvents =
                    CompanyEvent.findCompanyEvents(company, EventTypeCode.ReversalRequested, null, null, null);
            assertEquals("Number of Reversal Requested Events", 28, companyEvents.size());
            int i = 0;
            List<String> eventDetailTxns = new ArrayList<String>();
            List<String> reverseTxns = new ArrayList<String>();

            // verify first paycheck split transaction was reversed (and no others).
            for (FinancialTransaction txn : txnList) {
                assertEquals("Associated txn count", 1, txn.getAssociatedTransactionsCollection().size());

                FinancialTransaction reversalTxn = txn.getAssociatedTransactionsCollection().iterator().next();
                FinancialTransaction originalTxn = reversalTxn.getOriginalTransaction();

                // verify the reversal txn is associated with the original txn
                assertNotNull("Original txn", originalTxn);
                assertEquals("Reversal txn association", txn.getId(), originalTxn.getId());

                // verify the credit account is intuit
                assertEquals("Reversed txn credit account",
                        intuitReversalBankAccount.getBankAccount().getId(),
                        reversalTxn.getCreditBankAccount().getId());

                // verify the debit account is from the original txn credit account
                assertEquals("Reversed txn debit account",
                        originalTxn.getCreditBankAccount().getId(),
                        reversalTxn.getDebitBankAccount().getId());

                // verify the associated txn is indeed a reversal transaction
                assertEquals("Reversal txn type",
                        TransactionTypeCode.EmployeeDdReversalDebit,
                        reversalTxn.getTransactionType().getTransactionTypeCd());

                // verify the reversal amount
                assertEquals("Reversed txn amount",
                        originalTxn.getFinancialTransactionAmount(),
                        reversalTxn.getFinancialTransactionAmount());

                // verify the reversal txn settlement type is ACH
                assertEquals("Reversed txn settlement type",
                        SettlementType.ACH,
                        reversalTxn.getSettlementTypeCd());

                // verify the settlement date for the reversal txn
                txnSettlementDate = reversalTxn.getSettlementDate().toLocal().copy();
                CalendarUtils.clearTime(txnSettlementDate);
                assertEquals("Reversed txn settlement date", expectedSettlementDate, txnSettlementDate);

                // verify the event details
                DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(i++).getCompanyEventDetailCollection();
                eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

                CompanyService companyService = company.getCompanyService(ServiceCode.BillPayment);

                Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CompanyServiceId, eventDetails.get(0).getEventDetailTypeCd());
                Assert.assertEquals("Event Detail Value", companyService.getId().toString(), eventDetails.get(0).getValue());
                Assert.assertEquals("Event Detail Code", EventDetailTypeCode.FinancialTransactionId, eventDetails.get(1).getEventDetailTypeCd());
                // Don't compare here, since company events can be in different order
//                Assert.assertEquals("Event Detail Value", reversalTxn.getId().toString(), eventDetails.get(0).getValue());
                reverseTxns.add(reversalTxn.getId().toString());
                eventDetailTxns.add(eventDetails.get(1).getValue());
                Assert.assertEquals("Event Detail Code", EventDetailTypeCode.IntuitInitiated, eventDetails.get(2).getEventDetailTypeCd());
                Assert.assertEquals("Event Detail Value", Boolean.FALSE.toString(), eventDetails.get(2).getValue());
            }
            // Make sure event details financial transaction ids are same as the reversed transaction ids
            assertTrue("Financial Transaction Ids", reverseTxns.containsAll(eventDetailTxns));
            assertTrue("Financial Transaction Ids", eventDetailTxns.containsAll(reverseTxns));
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to perform an ACH reversal of partial transactions will not succeed.
     */
    @Test
    public void testAchReversalOfPartialPaycheck_IntuitInitiated() {
        //create a Transction Return for ER Debit
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R01", "NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 14, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
        PayrollServices.commitUnitOfWork();

        assertEquals("Payroll run status is PendingAutoRedebit", PayrollStatus.PendingAutoRedebit, payrollRun.getPayrollRunStatus());
        ProcessResult processResult = new ProcessResult();
        List<String> dtoTxnList = new Vector<String>(4);
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);

        dto.setIntuitInitiatedReversals(true);
        // Retrieve a list of all paycheck split transacitons and add them to the dto be reversed
        PayrollServices.beginUnitOfWork();
        {
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);
            // make sure there are more than one paycheck
            assertTrue("Number Of paycheck splits ", txnList.size() > 1);
            txnList.remove(0);
            for (FinancialTransaction txn : txnList) {
                dtoTxnList.add(txn.getBillPaymentSplit().getSourceId());
            }

            dto.setDdTransactionIdList(dtoTxnList);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto);
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "503", message1.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Partial reversal of payroll is not allowed for Intuit-initiated reversals.";
        assertEquals("Error Message", messageText1, message1.getMessage());

        // verify no Reversal Requested events created
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.ReversalRequested, null, null, null);
        assertEquals("Number of Reversal Requested Events", 0, companyEvents.size());


    }

    /**
     * Verify that a request to perform an ACH reversal of all transactions (explicit) will succeed.
     */
    @Test
    public void testExplicitAchReversal_IntuitInitiated() {
        //create a Transction Return for ER Debit
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();

        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R01", "NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 14, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
        // verify the fee txn was created
        DomainEntitySet<FinancialTransaction> feeTxnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[] {TransactionStateCode.Created});

        assertEquals("Reversal fee txn", 1, feeTxnList.size());
        PayrollServices.commitUnitOfWork();

        assertEquals("Payroll run status is PendingAutoRedebit", PayrollStatus.PendingAutoRedebit, payrollRun.getPayrollRunStatus());

        ProcessResult processResult = new ProcessResult();
        List<String> dtoTxnList = new Vector<String>(4);
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);
        dto.setIntuitInitiatedReversals(true);

        // Retrieve a list of all paycheck split transacitons and add them to the dto be reversed
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);

            for (FinancialTransaction txn : txnList) {
                dtoTxnList.add(txn.getBillPaymentSplit().getSourceId());
            }

            dto.setDdTransactionIdList(dtoTxnList);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // If any errors do occur, write them out to stdout before the assertion...
        if (!processResult.isSuccess()) {
            for (Message msg : processResult.getMessages()) {
                System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
            }
        }

        // validate error count
        assertEquals("Number of Errors", 0, processResult.getMessages().size());


        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
            DomainEntitySet<FinancialTransaction> cancelledRedebits = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                    new TransactionStateCode[]{TransactionStateCode.Cancelled});
            // verify the Redebit transaction is cancelled
            assertEquals("Number of ACH ER DD Redebit Financial Txs", 1, cancelledRedebits.size());
            for (FinancialTransaction currDDRedebit : cancelledRedebits) {
                assertEquals("Payroll Run Id ", payrollRunId, currDDRedebit.getPayrollRun().getSourcePayRunId());
                assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, currDDRedebit.getCurrentTransactionState().getTransactionStateCd());
                assertEquals("Settlement Type ", SettlementType.ACH, currDDRedebit.getSettlementTypeCd());
            }
            // verify payroll run
            assertEquals("PayrollRun status", PayrollStatus.PendingReversals, payrollRun.getPayrollRunStatus());
            IntuitBankAccount intuitReversalBankAccount =
                    IntuitBankAccount.findIntuitBankAccount(
                            TransactionTypeCode.EmployeeDdReversalDebit,
                            CreditDebitCode.Credit);
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);
            SpcfCalendar expectedSettlementDate = TransactionReverseCoreDataLoader.prTxnReverseSubmitDate.copy();
            SpcfCalendar txnSettlementDate;

            CalendarUtils.addBusinessDays(expectedSettlementDate, 1);
            CalendarUtils.clearTime(expectedSettlementDate);
            // verify no new fee txns were created
            feeTxnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                    new TransactionStateCode[] {TransactionStateCode.Created});

            assertEquals("Reversal fee txn", 1, feeTxnList.size());

            // verify Reversal Requested events
            DomainEntitySet<CompanyEvent> companyEvents =
                    CompanyEvent.findCompanyEvents(company, EventTypeCode.ReversalRequested, null, null, null);
            assertEquals("Number of Reversal Requested Events", 28, companyEvents.size());
            int i = 0;
            List<String> eventDetailTxns = new ArrayList<String>();
            List<String> reverseTxns = new ArrayList<String>();
            // verify first paycheck split transaction was reversed (and no others).
            for (FinancialTransaction txn : txnList) {
                assertEquals("Associated txn count", 1, txn.getAssociatedTransactionsCollection().size());

                FinancialTransaction reversalTxn = txn.getAssociatedTransactionsCollection().iterator().next();
                FinancialTransaction originalTxn = reversalTxn.getOriginalTransaction();

                // verify the reversal txn is associated with the original txn
                assertNotNull("Original txn", originalTxn);
                assertEquals("Reversal txn association", txn.getId(), originalTxn.getId());

                // verify the credit account is intuit
                assertEquals("Reversed txn credit account",
                        intuitReversalBankAccount.getBankAccount().getId(),
                        reversalTxn.getCreditBankAccount().getId());

                // verify the debit account is from the original txn credit account
                assertEquals("Reversed txn debit account",
                        originalTxn.getCreditBankAccount().getId(),
                        reversalTxn.getDebitBankAccount().getId());

                // verify the associated txn is indeed a reversal transaction
                assertEquals("Reversal txn type",
                        TransactionTypeCode.EmployeeDdReversalDebit,
                        reversalTxn.getTransactionType().getTransactionTypeCd());

                // verify the reversal amount
                assertEquals("Reversed txn amount",
                        originalTxn.getFinancialTransactionAmount(),
                        reversalTxn.getFinancialTransactionAmount());

                // verify the reversal txn settlement type is ACH
                assertEquals("Reversed txn settlement type",
                        SettlementType.ACH,
                        reversalTxn.getSettlementTypeCd());

                // verify the settlement date for the reversal txn
                txnSettlementDate = reversalTxn.getSettlementDate().toLocal().copy();
                CalendarUtils.clearTime(txnSettlementDate);
                assertEquals("Reversed txn settlement date", expectedSettlementDate, txnSettlementDate);

                // verify the event details
                DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(i++).getCompanyEventDetailCollection();
                eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

                CompanyService companyService = company.getCompanyService(ServiceCode.BillPayment);

                Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CompanyServiceId, eventDetails.get(0).getEventDetailTypeCd());
                Assert.assertEquals("Event Detail Value", companyService.getId().toString(), eventDetails.get(0).getValue());
                Assert.assertEquals("Event Detail Code", EventDetailTypeCode.FinancialTransactionId, eventDetails.get(1).getEventDetailTypeCd());
                // Don't compare here, since company events can be in different order
//                Assert.assertEquals("Event Detail Value", reversalTxn.getId().toString(), eventDetails.get(0).getValue());
                reverseTxns.add(reversalTxn.getId().toString());
                eventDetailTxns.add(eventDetails.get(1).getValue());
                Assert.assertEquals("Event Detail Code", EventDetailTypeCode.IntuitInitiated, eventDetails.get(2).getEventDetailTypeCd());
                Assert.assertEquals("Event Detail Value", Boolean.TRUE.toString(), eventDetails.get(2).getValue());
            }
            // Make sure event details financial transaction ids are same as the reversed transaction ids
            assertTrue("Financial Transaction Ids", reverseTxns.containsAll(eventDetailTxns));
            assertTrue("Financial Transaction Ids", eventDetailTxns.containsAll(reverseTxns));
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to perform a non-ACH reversal of all transactions (implicit) will succeed.
     *
     * @param pSettlementType The non-ACH settlement type to test
     */
    private void testImplicitNonAchReversalOfAllTransactions(SettlementTypeDTO pSettlementType) {
        ProcessResult processResult = new ProcessResult();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        assertTrue(payrollRuns.size() > 0);
        PayrollRun payrollRun = payrollRuns.get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);
        SpcfCalendar settlementDate = TransactionReverseCoreDataLoader.prTxnReverseSubmitDate.copy();

        // set settlement date 10 calendar days in the past
        settlementDate.addDays(-10); // Rule: -45 days <= txnDate <= today (PSPDate.getPSPTime())
        CalendarUtils.clearTime(settlementDate);

        dto.setTxSettlementTypeCd(pSettlementType);
        dto.setTxDate(CalendarUtils.convertToCalendar(settlementDate));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // If any errors do occur, write them out to stdout before the assertion...
        if (!processResult.isSuccess()) {
            for (Message msg : processResult.getMessages()) {
                System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
            }
        }

        // validate error count
        assertEquals("Number of Errors", 0, processResult.getMessages().size());

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);
            SpcfCalendar txnSettlementDate;

            // verify Reversal Requested events
            DomainEntitySet<CompanyEvent> companyEvents =
                    CompanyEvent.findCompanyEvents(company, EventTypeCode.ReversalRequested, null, null, null);
            assertEquals("Number of Reversal Requested Events", 28, companyEvents.size());
            int i = 0;
            List<String> eventDetailTxns = new ArrayList<String>();
            List<String> reverseTxns = new ArrayList<String>();
            // verify first paycheck split transaction was reversed (and no others).
            for (FinancialTransaction txn : txnList) {
                assertEquals("Associated txn count", 1, txn.getAssociatedTransactionsCollection().size());

                FinancialTransaction reversalTxn = txn.getAssociatedTransactionsCollection().iterator().next();
                FinancialTransaction originalTxn = reversalTxn.getOriginalTransaction();

                // verify the reversal txn is associated with the original txn
                assertNotNull("Original txn", originalTxn);
                assertEquals("Reversal txn association", txn.getId(), originalTxn.getId());

                // verify the credit account is null
                assertNull("Reversed txn credit account", reversalTxn.getCreditBankAccount());

                // verify the debit account is null
                assertNull("Reversed txn debit account", reversalTxn.getDebitBankAccount());

                // verify the associated txn is indeed a reversal transaction
                assertEquals("Reversal txn type",
                        TransactionTypeCode.EmployeeDdReversalDebit,
                        reversalTxn.getTransactionType().getTransactionTypeCd());

                // verify the reversal amount
                assertEquals("Reversed txn amount",
                        txn.getFinancialTransactionAmount(),
                        reversalTxn.getFinancialTransactionAmount());

                // verify the reversal txn settlement type is as requested
                assertEquals("Reversed txn settlement type",
                        SettlementType.valueOf(pSettlementType.toString()),
                        reversalTxn.getSettlementTypeCd());

                // verify the settlement date for the reversal txn
                txnSettlementDate = reversalTxn.getSettlementDate().toLocal().copy();
                CalendarUtils.clearTime(txnSettlementDate);
                assertEquals("Reversed txn settlement date", settlementDate, txnSettlementDate);
                // verify the event details
                DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(i++).getCompanyEventDetailCollection();
                eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

                CompanyService companyService = company.getCompanyService(ServiceCode.BillPayment);

                Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CompanyServiceId, eventDetails.get(0).getEventDetailTypeCd());
                Assert.assertEquals("Event Detail Value", companyService.getId().toString(), eventDetails.get(0).getValue());
                Assert.assertEquals("Event Detail Code", EventDetailTypeCode.FinancialTransactionId, eventDetails.get(1).getEventDetailTypeCd());
                // Don't compare here, since company events can be in different order
                //Assert.assertEquals("Event Detail Value", reversalTxn.getId().toString(), eventDetails.get(0).getValue());
                reverseTxns.add(reversalTxn.getId().toString());
                eventDetailTxns.add(eventDetails.get(1).getValue());
                Assert.assertEquals("Event Detail Code", EventDetailTypeCode.IntuitInitiated, eventDetails.get(2).getEventDetailTypeCd());
                Assert.assertEquals("Event Detail Value", Boolean.FALSE.toString(), eventDetails.get(2).getValue());
            }
            // Make sure event details financial transaction ids are same as the reversed transaction ids
            assertTrue("Financial Transaction Ids", reverseTxns.containsAll(eventDetailTxns));
            assertTrue("Financial Transaction Ids", eventDetailTxns.containsAll(reverseTxns));
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to perform a non-ACH reversal of all transactions (implicit) will succeed.
     */
    @Test
    public void testImplicitCashReversalOfAllTransactions() {
        testImplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.Cash);

        // Do not test settlement type of ACH since it is (by definition) not in the scope of this test.
        //testImplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.ACH);
    }

    @Test
    public void testImplicitCheckTypeReversalOfAllTransactions() {
        testImplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.CheckType);
    }

    @Test
    public void testImplicitOtherReversalOfAllTransactions() {
        testImplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.Other);
    }

    @Test
    public void testImplicitWireReversalOfAllTransactions() {
        testImplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.Wire);
    }


    /**
     * Verify that a request to perform a non-ACH reversal of all transactions (explicit) will succeed.
     *
     * @param pSettlementType The non-ACH settlement type to test
     */
    private void testExplicitNonAchReversalOfAllTransactions(SettlementTypeDTO pSettlementType) {
        ProcessResult processResult = new ProcessResult();
        List<String> dtoTxnList = new Vector<String>(4);
         Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);
        SpcfCalendar settlementDate = TransactionReverseCoreDataLoader.prTxnReverseSubmitDate.copy();

        // set settlement date 10 calendar days in the past
        settlementDate.addDays(-10); // Rule: -45 days <= txnDate <= today (PSPDate.getPSPTime())
        CalendarUtils.clearTime(settlementDate);

        // Retrieve a list of all paycheck split transacitons and add them to the dto be reversed
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);

            for (FinancialTransaction txn : txnList) {
                dtoTxnList.add(txn.getPaycheckSplit().getSourceDdTxnId());
            }

            dto.setDdTransactionIdList(dtoTxnList);
            dto.setTxSettlementTypeCd(pSettlementType);
            dto.setTxDate(CalendarUtils.convertToCalendar(settlementDate));
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // If any errors do occur, write them out to stdout before the assertion...
        if (!processResult.isSuccess()) {
            for (Message msg : processResult.getMessages()) {
                System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
            }
        }

        // validate error count
        assertEquals("Number of Errors", 0, processResult.getMessages().size());

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findBillPaymentSplitFinancialTransactions(payrollRun, null);
            SpcfCalendar txnSettlementDate;

            // verify first paycheck split transaction was reversed (and no others).
            for (FinancialTransaction txn : txnList) {
                assertEquals("Associated txn count", 1, txn.getAssociatedTransactionsCollection().size());

                FinancialTransaction reversalTxn = txn.getAssociatedTransactionsCollection().iterator().next();
                FinancialTransaction originalTxn = reversalTxn.getOriginalTransaction();

                // verify the reversal txn is associated with the original txn
                assertNotNull("Original txn", originalTxn);
                assertEquals("Reversal txn association", txn.getId(), originalTxn.getId());

                // verify the credit account is null
                assertNull("Reversed txn credit account", reversalTxn.getCreditBankAccount());

                // verify the debit account is null
                assertNull("Reversed txn debit account", reversalTxn.getDebitBankAccount());

                // verify the associated txn is indeed a reversal transaction
                assertEquals("Reversal txn type",
                        TransactionTypeCode.EmployeeDdReversalDebit,
                        reversalTxn.getTransactionType().getTransactionTypeCd());

                // verify the reversal amount
                assertEquals("Reversed txn amount",
                        txn.getFinancialTransactionAmount(),
                        reversalTxn.getFinancialTransactionAmount());

                // verify the reversal txn settlement type is as requested
                assertEquals("Reversed txn settlement type",
                        SettlementType.valueOf(pSettlementType.toString()),
                        reversalTxn.getSettlementTypeCd());

                // verify the settlement date for the reversal txn
                txnSettlementDate = reversalTxn.getSettlementDate().toLocal().copy();
                CalendarUtils.clearTime(txnSettlementDate);
                assertEquals("Reversed txn settlement date", settlementDate, txnSettlementDate);
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to perform a non-ACH reversal of all transactions (explicit) will succeed.
     */
    @Test
    public void testExplicitCashReversalOfAllTransactions() {
        testExplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.Cash);

        // Do not test settlement type of ACH since it is (by definition) not in the scope of this test.
        //testExplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.ACH);
    }

    @Test
    public void testExplicitCheckTypeReversalOfAllTransactions() {
        testExplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.CheckType);
    }

    @Test
    public void testExplicitOtherReversalOfAllTransactions() {
        testExplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.Other);
    }

    @Test
    public void testExplicitWireReversalOfAllTransactions() {
        testExplicitNonAchReversalOfAllTransactions(SettlementTypeDTO.Wire);
    }

    @Test
    public void testIntuitInitiatedReversal_InvalidData() {
        ProcessResult processResult = new ProcessResult();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);

        dto.setIntuitInitiatedReversals(true);
        dto.setTxSettlementTypeCd(SettlementTypeDTO.Other);
        dto.setChargeFee(true);

        SpcfCalendar settlementDate = TransactionReverseCoreDataLoader.prTxnReverseSubmitDate.copy();

        // set settlement date 10 calendar days in the past
        settlementDate.addDays(-10);
        CalendarUtils.clearTime(settlementDate);
        dto.setTxDate(CalendarUtils.convertToCalendar(settlementDate));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        processResult.merge(PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "501", message1.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Intuit-initiated reversals must have ACH settlement type.";
        assertEquals("Error Message", messageText1, message1.getMessage());

        dto.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "502", message1.getMessageCode());

        // Verify that the correct message strings were returned
        messageText1 = "A reversal fee cannot be charged for Intuit-initiated reversals.";
        assertEquals("Error Message", messageText1, message1.getMessage());
    }

    /**
     * PSRV000800: Failed Customer Reversal changes Payroll Status to Reversals Finished
     */
    @Test
    public void testBug800() {
        // advance the date beyond the ACH wait period and run the ACH processor to make sure the payroll status is Complete
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions achPostProcessor = new ProcessACHTransactions();
        achPostProcessor.process(PSPDate.getPSPTime());
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();

        PayrollServices.commitUnitOfWork();
        assertEquals("payroll status before reversal", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        // reverse the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(TransactionReverseCoreDataLoader.prTxnReverseSubmitDate);
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);
        ProcessResult prReverse = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("payroll reversal", prReverse);

        // offload the EE DD Reversal Debits
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> reversalDebits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "123272727",
                        TransactionTypeCode.EmployeeDdReversalDebit,
                        TransactionStateCode.Created);
        PSPDate.setPSPTime(reversalDebits.get(0).getMoneyMovementTransaction().getInitiationDate());
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        PayrollServices.beginUnitOfWork();
        FinancialTransaction ftOffloaded = Application.refresh(reversalDebits.get(0));
        TransactionStateCode ftStateCd = ftOffloaded.getCurrentTransactionState().getTransactionStateCd();
        PayrollServices.commitUnitOfWork();
        assertEquals("ee reversal debits offloaded", TransactionStateCode.Executed, ftStateCd);

        // create TransactionReturns for them, and handle the returns
        PayrollServices.beginUnitOfWork();
        reversalDebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,
                "123272727",
                TransactionTypeCode.EmployeeDdReversalDebit,
                TransactionStateCode.Executed);
        DomainEntitySet<MoneyMovementTransaction> mmts = ACHReturnsDataLoader.getMoneyMovementTransactions(reversalDebits, true);
        DomainEntitySet<TransactionReturn> returns = ACHReturnsDataLoader.createTransactionReturns(mmts, "R01", "whatever");
        for (TransactionReturn ret : returns) {
            TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(ret);
            handler.execute(ret);
        }
        PayrollServices.commitUnitOfWork();

        // the bug sez the payroll is now in ReversalsFinished
        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        PayrollServices.commitUnitOfWork();
        // before the fix, this was the case: assertEquals("payroll status", PayrollStatus.ReversalsFinished, payroll.getPayrollRunStatus());
        assertEquals("payroll status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
    }

    @Ignore
    @Test
    public void testIntuitInitiatedReversal_ReturnEECreditTransaction() {
        //create a Transction Return for EmployerDdDebit
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> payrollFTs = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R01", "NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 14, returnList.size());

        PayrollServices.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        //create a Transction Return for one of the EmployeeDdCredit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> employeeFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        payrollMMTs = new DomainEntitySet<MoneyMovementTransaction>();
        payrollMMTs.add(employeeFinTxns.get(0).getMoneyMovementTransaction());

        DomainEntitySet<TransactionReturn> returns = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R02", "This is an Non NSF description");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        transactionReturn = returns.get(0);

        returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        List<String> dtoTxnList = new Vector<String>(4);
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).get(0);
        String payrollRunId = payrollRun.getSourcePayRunId();
        TransactionReverseDTO dto = buildTransactionReverseDTO(payrollRunId, null);

        dto.setIntuitInitiatedReversals(true);

        PayrollServices.beginUnitOfWork();
        employeeFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        for (FinancialTransaction finTxn : employeeFinTxns) {
            dtoTxnList.add(finTxn.getBillPaymentSplit().getSourceId());
        }

        dto.setDdTransactionIdList(dtoTxnList);
        PayrollServices.commitUnitOfWork();

        //Intuit Initiated Reversal
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "123272727", dto);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Result ", processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);
        DomainEntitySet<FinancialTransaction> cancelledRedebits = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        // verify the Redebit transaction is cancelled
        assertEquals("Number of ACH ER DD Redebit Financial Txs", 1, cancelledRedebits.size());
        for (FinancialTransaction currDDRedebit : cancelledRedebits) {
            assertEquals("Payroll Run Id ", payrollRunId, currDDRedebit.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, currDDRedebit.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Settlement Type ", SettlementType.ACH, currDDRedebit.getSettlementTypeCd());
        }

        // verify payroll run
        assertEquals("PayrollRun status", PayrollStatus.PendingReversals, payrollRun.getPayrollRunStatus());

        employeeFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);


        DomainEntitySet<FinancialTransaction> reversalTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Created);

        assertEquals("Number of Reversal Transactions ", employeeFinTxns.size(), reversalTxns.size());
        PayrollServices.commitUnitOfWork();

    }

 
}
