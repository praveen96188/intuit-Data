package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.FeeTransferDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddFeeTransferTransactionDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * User: rkrishna
 * Date: Jan 10, 2008
 * Time: 8:57:56 AM
 */
public class AddFeeTransferTransactionTests {

    @Before
    public void runBeforeEachTest() {
        AddFeeTransferTransactionDataLoader.loadBeforeTest();
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

        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(null, "123272727", feeTransferDTO);

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
        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, null, feeTransferDTO);

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
        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "123272727", feeTransferDTO);

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
        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setSourcePayrollRunId("BatchId01");

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "1232727", feeTransferDTO);
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
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddFeeTransferTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setSourcePayrollRunId("BatchId03");

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "123272727", feeTransferDTO);

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
     * Test message 283 - Null Financial Transaction Amount
     */
    @Test
    public void testNullTxnAmount() {
        ACHReturnsDataLoader.loadData2DayERNSFsOffloadRedebitAndReturnFee();

        Application.beginUnitOfWork();

        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setSourcePayrollRunId("BatchTest05");
        feeTransferDTO.setFinancialTxAmt(null);

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "1234567", feeTransferDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message", "The amount must be a non-zero, positive number.",
                errorMessage.getMessage());
        assertEquals("Error message code", "283", errorMessage.getMessageCode());


    }

    /**
     * Test message 283 - Negative Financial Transaction Amount
     */
    @Test
    public void testNegativeFinTxnAmount() {
        //set PSP Date
        ACHReturnsDataLoader.loadData2DayERNSFsOffloadRedebitAndReturnFee();

        Application.beginUnitOfWork();

        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setSourcePayrollRunId("BatchTest05");
        feeTransferDTO.setFinancialTxAmt(new SpcfMoney("-1705.81"));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "1234567", feeTransferDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message", "The amount must be a non-zero, positive number.",
                errorMessage.getMessage());
        assertEquals("Error message code", "283", errorMessage.getMessageCode());
    }

    /**
     * Test message 5001 - Invalid Fee Type Code.
     */
    @Test
    public void testInvalidFeeType() {

        Application.beginUnitOfWork();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddFeeTransferTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        TransactionType transactionType = TransactionType.findTransactionType(
                TransactionTypeCode.EmployerWriteOff);

        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount
                .findIntuitBankAccount(transactionType,
                        CreditDebitCode.Credit);

        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount
                .findIntuitBankAccount(transactionType,
                        CreditDebitCode.Debit);

        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(company, payrollRun,
                null, creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployerWriteOff, new SpcfMoney("1000.81"), SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployerWriteOff,
                        company.getOffloadGroup()));

        financialTransaction.updateFinancialTransactionState(
                TransactionStateCode.Executed);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setSourcePayrollRunId("BatchId01");
        feeTransferDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        feeTransferDTO.setFeeTypeCode(null);

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "123272727", feeTransferDTO);

        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());

        assertEquals("Error message", "FeeType has invalid value", errorMessage.getMessage());

        Application.beginUnitOfWork();

        feeTransferDTO.setFeeTypeCode(OfferingServiceChargeType.PerPaycheck);

        processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "123272727", feeTransferDTO);

        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, processResult.getMessages().size());
        errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());

        assertEquals("Error message", "FeeType has invalid value", errorMessage.getMessage());
    }

    /**
     * Test message 284 - Txn Amount for Fee Transfer must not exceed the credit balance of the ER Return
     * Receivable ledger account for this payroll.
     */
    @Test
    public void testInvalidFinTxnAmount() {

        Application.beginUnitOfWork();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddFeeTransferTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        TransactionType transactionType = TransactionType.findTransactionType(
                TransactionTypeCode.EmployerWriteOff);

        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount
                .findIntuitBankAccount(transactionType,
                        CreditDebitCode.Credit);

        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount
                .findIntuitBankAccount(transactionType,
                        CreditDebitCode.Debit);

        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(company, payrollRun,
                null, creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployerWriteOff, new SpcfMoney("1000.81"), SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployerWriteOff,
                        company.getOffloadGroup()));

        financialTransaction.updateFinancialTransactionState(
                TransactionStateCode.Executed);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setSourcePayrollRunId("BatchId01");
        feeTransferDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        feeTransferDTO.setFeeTypeCode(OfferingServiceChargeType.DebitReturnFee);

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "123272727", feeTransferDTO);

        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "284", errorMessage.getMessageCode());

        assertEquals("Error message",
                "Txn Amount for Fee Transfer must not exceed the credit balance of the ER Return " +
                        "Receivable ledger account for this payroll.", errorMessage.getMessage());
    }

    /**
     * PSRV000720 is about adding on fee transfer that is under the limit, then adding a second that is also under the
     * limit -- but the sum of the two transfers is over the limit.  The process was not counting pending (Created)
     * transactions in the validation.
     */
    @Test
    public void testBug720() {

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddFeeTransferTransactionDataLoader.psd1.loadDataForPayrollSubmit();
        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        TransactionType transactionType = TransactionType.findTransactionType(TransactionTypeCode.EmployerWriteOff);
        IntuitBankAccount ibaCredit = IntuitBankAccount.findIntuitBankAccount(transactionType,
                                                                                                    CreditDebitCode.Credit);
        IntuitBankAccount ibaDebit = IntuitBankAccount.findIntuitBankAccount(transactionType,
                                                                                                   CreditDebitCode.Debit);
        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(company, payrollRun,
                null, ibaCredit.getBankAccount(), ibaDebit.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployerWriteOff, new SpcfMoney("1000.81"), SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployerWriteOff,company.getOffloadGroup()));
        financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
        Application.commitUnitOfWork();

        // add one fee transfer that's just under the limit
        Application.beginUnitOfWork();
        company = Application.refresh(company);
        SpcfMoney maxTransfer = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, "BatchId01", company, null, true);
        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setSourcePayrollRunId("BatchId01");
        feeTransferDTO.setFinancialTxAmt(new SpcfMoney( maxTransfer.subtract(new SpcfMoney("0.01")) )); // less than the max
        feeTransferDTO.setFeeTypeCode(OfferingServiceChargeType.DebitReturnFee);
        ProcessResult prFirst = PayrollServices.financialTransactionManager.addFeeTransferTransaction(SourceSystemCode.QBOE, "123272727", feeTransferDTO);
        Application.commitUnitOfWork();
        assertSuccess("First fee transfer should succeed", prFirst);

        // add another fee transfer (same DTO) -- still just under the (original) limit, but the sum should exceed the limit
        Application.beginUnitOfWork();
        ProcessResult prSecond = PayrollServices.financialTransactionManager.addFeeTransferTransaction(SourceSystemCode.QBOE, "123272727", feeTransferDTO);
        Application.commitUnitOfWork();
        assertTrue("Second fee transfer should fail", !prSecond.isSuccess());
        assertEquals("Messages size", 1, prSecond.getMessages().size());
        assertEquals("Error message code", "284", prSecond.getMessages().get(0).getMessageCode());
        assertEquals("Error message", "Txn Amount for Fee Transfer must not exceed the credit balance of the ER Return Receivable ledger account for this payroll.", prSecond.getMessages().get(0).getMessage());
    }

    /**
     * Test - Fee Transfer Process for NSFFEEAMT
     */
    @Test
    public void testFeeTransferProcessForNSF_FEE() {
        ACHReturnsDataLoader.loadData2DayERNSFsOffloadRedebitAndReturnFee();

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setSourcePayrollRunId("BatchTest05");
        feeTransferDTO.setFinancialTxAmt(new SpcfMoney("50.00"));
        feeTransferDTO.setFeeTypeCode(OfferingServiceChargeType.DebitReturnFee);

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "1234567", feeTransferDTO);

        Application.commitUnitOfWork();

        assertSuccess("addFeeTransferTransaction Process Result", processResult);

        FinancialTransaction financialTransaction1 = (FinancialTransaction) processResult.getResult();

        assertEquals("Payroll Run ", "BatchTest05",
                financialTransaction1.getPayrollRun().getSourcePayRunId());

        assertEquals("Financial Transaction State ", TransactionStateCode.Created,
                financialTransaction1.getCurrentTransactionState().getTransactionStateCd());

        assertEquals("Financial Transaction Type ", TransactionTypeCode.IntuitFeeTransfer,
                financialTransaction1.getTransactionType().getTransactionTypeCd());

        // verify financial transaction amount
        assertTrue("Transaction Amount: ",
                financialTransaction1.getFinancialTransactionAmount().equals(new SpcfMoney("50.00")));

        // verify the SKU type
        assertEquals("SKU:", "408176", financialTransaction1.getSku()); // debit return (NSF) fee sku for "QBOE DD" offering

        // verify the SKU quantity
        assertTrue("SKU:", 1 == financialTransaction1.getSkuQuantity());
    }

    /**
     * Test - Fee Transfer Process for ReversalFee
     */
    @Test
    public void testFeeTransferProcessForREVERSE_FEE() {

        Application.beginUnitOfWork();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddFeeTransferTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        TransactionType transactionType = TransactionType.findTransactionType(
                TransactionTypeCode.EmployerWriteOff);

        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount
                .findIntuitBankAccount(transactionType,
                        CreditDebitCode.Credit);

        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount
                .findIntuitBankAccount(transactionType,
                        CreditDebitCode.Debit);

        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(company, payrollRun,
                null, creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployerWriteOff, new SpcfMoney("1000.81"), SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployerWriteOff,
                        company.getOffloadGroup()));

        financialTransaction.updateFinancialTransactionState(
                TransactionStateCode.Executed);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setSourcePayrollRunId("BatchId01");
        feeTransferDTO.setFinancialTxAmt(new SpcfMoney("50.00"));
        feeTransferDTO.setFeeTypeCode(OfferingServiceChargeType.ReversalFee);

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addFeeTransferTransaction(SourceSystemCode.QBOE, "123272727", feeTransferDTO);

        Application.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        FinancialTransaction financialTransaction1 = (FinancialTransaction) processResult.getResult();

        assertEquals("Payroll Run ", payrollRunDTO.getPayrollTXBatchId(),
                financialTransaction1.getPayrollRun().getSourcePayRunId());

        assertEquals("Financial Transaction State ", TransactionStateCode.Created,
                financialTransaction1.getCurrentTransactionState().getTransactionStateCd());

        assertEquals("Financial Transaction Type ", TransactionTypeCode.IntuitFeeTransfer,
                financialTransaction1.getTransactionType().getTransactionTypeCd());

        // verify financial transaction amount
        assertTrue("Transaction Amount: ",
                financialTransaction1.getFinancialTransactionAmount().equals(new SpcfMoney("50.00")));

        // verify the SKU type
        assertEquals("SKU:", "408177", financialTransaction1.getSku()); // reversal fee sku for "QBOE DD" offering

        // verify the SKU quantity
        assertTrue("SKU:", 1 == financialTransaction1.getSkuQuantity());
    }
}

