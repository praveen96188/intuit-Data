package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddIntuit5DayReturnTransferTransactionDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * User: rkrishna
 * Date: Jan 10, 2008
 * Time: 1:45:41 PM

 */
public class AddIntuit5DayReturnTransferTransactionTests {


    @Before
    public void runBeforeEachTest() {
        AddIntuit5DayReturnTransferTransactionDataLoader.loadBeforeTest();
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

        ProcessResult processResult = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(null, "123272727", null);

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

        ProcessResult processResult = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(SourceSystemCode.QBOE, null, null);

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

        ProcessResult processResult = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(SourceSystemCode.QBOE, "123272727", null);

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

        ProcessResult processResult = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(SourceSystemCode.QBOE, "1232727", "BatchId01");
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
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddIntuit5DayReturnTransferTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(SourceSystemCode.QBOE, "123272727", "BatchId03");

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
    public void testCreateTransactionFailurePendingLedgerActivity() {
        Application.beginUnitOfWork();
        ACHReturnsDataLoader.loadDataHappyPath();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
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
    public void testPendingTransactionAlreadyExists() throws Exception{
        ACHReturnsDataLoader.loadData5Day1ERRet();

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.
                addIntuit5DayReturnTransferTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "280", errorMessage.getMessageCode());
        assertEquals("Error message", "A pending transaction of this type already exists.  If you wish to create a " +
                "new one, the existing transaction must be canceled, first.", errorMessage.getMessage());
    }

    /**
     * Test - Intuit 5Day Return Transfer Process
     */
    @Test
    public void testIntuit5DayReturnTransferProcess_QBOE() {

        ACHReturnsDataLoader.loadData5Day1ERRet();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE );
        PayrollRun payrolRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        payrolRun.cancelPayrollFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.Intuit5DayReturnTransfer});
        PayrollServices.commitUnitOfWork();


        Application.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.
                addIntuit5DayReturnTransferTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        Application.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        DomainEntitySet<FinancialTransaction> ftList = processResult.getResult();

        assertEquals("Financial Transaction Count ", 1, ftList.size());

        FinancialTransaction financialTransaction = ftList.get(0);

        assertEquals("Financial Transaction Type ", TransactionTypeCode.Intuit5DayReturnTransfer,
                financialTransaction.getTransactionType().getTransactionTypeCd());

        assertEquals("Payroll Run ", "BatchTest05",
                financialTransaction.getPayrollRun().getSourcePayRunId());

        assertEquals("Financial Transaction State ", TransactionStateCode.Created,
                financialTransaction.getCurrentTransactionState().getTransactionStateCd());

        assertEquals("Financial Transaction Amount ", new SpcfMoney("180.00"),
                financialTransaction.getFinancialTransactionAmount());

        assertEquals("Company ", "1234567",
                financialTransaction.getCompany().getSourceCompanyId());

    }

    /**
     * Test - Intuit 5Day Return Transfer Process
     */
    @Test
    public void testIntuit5DayReturnTransferProcess_QBDT() {
        ACHReturnsDataLoader.loadAndRunQBDTPayrollReturned_5Day("R01", "NSF description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrolRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        DomainEntitySet<FinancialTransaction> bookTransfers =
                FinancialTransaction.findFinancialTransactions(payrolRun,
                                                               new TransactionTypeCode[] {TransactionTypeCode.Intuit5DayReturnTransfer,
                                                                                          TransactionTypeCode.Intuit5DayFeeReturnTransfer,
                                                                                          TransactionTypeCode.Intuit5DaySalesTaxReturnTransfer},
                                                               new TransactionStateCode[] {TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        bookTransfers = bookTransfers.sort(FinancialTransaction.TransactionType());

        assertEquals("Book transfer txn count", 3, bookTransfers.size());

        assertEquals("Financial Transaction Type ", TransactionTypeCode.Intuit5DayReturnTransfer,
                bookTransfers.get(0).getTransactionType().getTransactionTypeCd());

        assertEquals("Financial Transaction Type ", TransactionTypeCode.Intuit5DayFeeReturnTransfer,
                bookTransfers.get(1).getTransactionType().getTransactionTypeCd());

        assertEquals("Financial Transaction Type ", TransactionTypeCode.Intuit5DaySalesTaxReturnTransfer,
                bookTransfers.get(2).getTransactionType().getTransactionTypeCd());

    }
}
