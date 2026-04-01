package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RebillFeeTransactionDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.CancelERFinancialTxCoreDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * User: rsakhamuri
 * Date: Dec 31, 2007
 * Time: 4:58:01 PM

 */
public class CancelERFinancialTxCoreTests {


    @Before
    public void runBeforeEachTest() {
        CancelERFinancialTxCoreDataLoader.loadBeforeTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void testCompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "InvalidCompanyId", "123123");
        // Commit
        PayrollServices.commitUnitOfWork();        

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());

    }

    /**
     * Test for null source system or null company
      */

    @Test
    public void testInvalidCompanyParameters() {

        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.cancelTransaction(null, "123272727", "123123");
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        PayrollServices.beginUnitOfWork();        
        processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, null, "123098");
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /** Test for invalid financial transaction id
     *
     */
    @Test
    public void testInvalidFinancialTxId() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "123272727", "2c915611-1732-d4c4-0117-32d53e810028");
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "264", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Financial Transaction 2c915611-1732-d4c4-0117-32d53e810028 does not exist for company QBOE:123272727.";
        assertEquals("Error Message", messageText, message.getMessage());


    }

    /** Test for null financial transaction id
     *
     */
    @Test
    public void testNullFinancialTxId() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "123272727", null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "264", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Financial Transaction null does not exist for company QBOE:123272727.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /** Test for Invalid ER Txn type and state
     *
     */
    @Test
    public void testInvalidERCancel() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[] { TransactionStateCode.Created });

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action FinancialTransactionCancel not valid for Financial Transaction with FinTxGseq "+ financialTxs.get(0).getId().toString() +
                ", which has a tx type of "+TransactionTypeCode.EmployerDdDebit +" and a tx status of "+ TransactionStateCode.Created+".";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /** Test for EE Credit transaction cancel
     *
     */
    @Test
    public void testInvalidEECancel() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[] {TransactionStateCode.Created});

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action FinancialTransactionCancel not valid for Financial Transaction with FinTxGseq "+ financialTxs.get(0).getId().toString()
                +", which has a tx type of "+TransactionTypeCode.EmployeeDdCredit+" and a tx status of "+ TransactionStateCode.Created+".";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /** Test for Employer transaction cancel
     *
     */
    @Test
    public void testERCancel() throws Exception {
        CancelERFinancialTxCoreDataLoader.loadTestERCancelData();
     

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[] {TransactionStateCode.Created});
        DomainEntitySet<TransactionResponse> transactionResponseCollection =
					TransactionResponse.findTransactionResponses(financialTxs.get(0));

        PayrollServices.commitUnitOfWork();

        assertNotNull("MM txn", financialTxs.get(0).getMoneyMovementTransaction());
        assertTrue("Payroll run:", financialTxs.get(0).getPayrollRun().getPayrollRunStatus().equals(PayrollStatus.OffloadedAll));

        TransactionTypeCode transactionTypeCode = financialTxs.get(0).getTransactionType().getTransactionTypeCd();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> financialTxsAfter =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[] {TransactionStateCode.Cancelled });
        DomainEntitySet<TransactionResponse> transactionResponsesAfter =
					TransactionResponse.findTransactionResponses(financialTxs.get(0));

        assertEquals("Financial Transaction Ids:", financialTxs.get(0).getId(), financialTxsAfter.get(0).getId());
        assertEquals("Transaction Response size:", transactionResponsesAfter.size() , transactionResponseCollection.size() + 1);
//        assertEquals("Transaction Response Token:", transactionResponsesAfter.get(transactionResponsesAfter.size() - 1 ).getTransactionTokenNumber(),
//                                                    transactionResponseCollection.get(0).getTransactionTokenNumber() + 1 );
//        assertTrue("Transaction Responses:", transactionResponsesAfter.get(0).equals(transactionResponseCollection.get(0)));
        assertEquals("Payroll run source Ids:", financialTxsAfter.get(0).getPayrollRun().getSourcePayRunId(), financialTxs.get(0).getPayrollRun().getSourcePayRunId());
        assertEquals("Payroll run Ids:", financialTxsAfter.get(0).getPayrollRun().getId(), financialTxs.get(0).getPayrollRun().getId());
        assertEquals("Payroll run Amounts:", financialTxsAfter.get(0).getPayrollRun().getPayrollDirectDepositAmount(), financialTxs.get(0).getPayrollRun().getPayrollDirectDepositAmount());
        assertTrue("Payroll run Status:", financialTxsAfter.get(0).getPayrollRun().getPayrollRunStatus().equals(PayrollStatus.OffloadedAll));

        //Ensure there aren't any lingering mm txns
        assertNull("MM txn", financialTxsAfter.get(0).getMoneyMovementTransaction());

        PayrollServices.commitUnitOfWork();
    }

    /** Test for Employer transaction cancel after transactions cutoff time
     *
     */
    @Test
    public void testERCancel_AfterCutOffTime() throws Exception {
        CancelERFinancialTxCoreDataLoader.loadTestERCancelData();


        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[] {TransactionStateCode.Created});
        DomainEntitySet<TransactionResponse> transactionResponseCollection =
					TransactionResponse.findTransactionResponses(financialTxs.get(0));

        PayrollServices.commitUnitOfWork();

        assertNotNull("MM txn", financialTxs.get(0).getMoneyMovementTransaction());
        assertTrue("Payroll run:", financialTxs.get(0).getPayrollRun().getPayrollRunStatus().equals(PayrollStatus.OffloadedAll));

        TransactionTypeCode transactionTypeCode = financialTxs.get(0).getTransactionType().getTransactionTypeCd();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002180000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);
        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:" + message.toString(), "1051", message.getMessageCode());
    }

    /** Test for Employer transaction cancel
     *
     */
    @Test
    public void testERCancelNSF() throws Exception {
        PayrollServices.beginUnitOfWork();

        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForNSFTransactionReturn();

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.Intuit5DayReturnTransfer},
                        new TransactionStateCode[] { TransactionStateCode.Created});
        DomainEntitySet<TransactionResponse> transactionResponseCollection =
					TransactionResponse.findTransactionResponses(financialTxs.get(0));

        PayrollServices.commitUnitOfWork();

        assertNotNull("MM txn", financialTxs.get(0).getMoneyMovementTransaction());

        assertTrue("Payroll run:", financialTxs.get(0).getPayrollRun().getPayrollRunStatus().equals(PayrollStatus.NSFCanceled));

        TransactionTypeCode transactionTypeCode = financialTxs.get(0).getTransactionType().getTransactionTypeCd();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> financialTxsAfter =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.Intuit5DayReturnTransfer},
                        new TransactionStateCode[] {TransactionStateCode.Cancelled});
        DomainEntitySet<TransactionResponse> transactionResponsesAfter =
					TransactionResponse.findTransactionResponses(financialTxs.get(0));

        assertEquals("Financial Transaction Ids:", financialTxs.get(0).getId(), financialTxsAfter.get(0).getId());
        assertEquals("Transaction Response size:", transactionResponsesAfter.size() , transactionResponseCollection.size());
        
        assertEquals("Payroll run source Ids:", financialTxsAfter.get(0).getPayrollRun().getSourcePayRunId(), financialTxs.get(0).getPayrollRun().getSourcePayRunId());
        assertEquals("Payroll run Ids:", financialTxsAfter.get(0).getPayrollRun().getId(), financialTxs.get(0).getPayrollRun().getId());
        assertEquals("Payroll run Amounts:", financialTxsAfter.get(0).getPayrollRun().getPayrollDirectDepositAmount(), financialTxs.get(0).getPayrollRun().getPayrollDirectDepositAmount());
        assertTrue("Payroll run Status:", financialTxsAfter.get(0).getPayrollRun().getPayrollRunStatus().equals(PayrollStatus.NSFCanceled));

        //Ensure there aren't any lingering mm txns
        assertNull("MM txn", financialTxsAfter.get(0).getMoneyMovementTransaction());

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testCancelServiceSalesAndUseTaxRefundCredit() throws Exception {
        ACHReturnsDataLoader.loadQBDTCompanyRequests1TxnReversed();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction executedFeeFT = null;
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    executedFeeFT = currTxn;
                }
            }
        }

        // advance the PSPTime by more than (ACH wait period + 1) days so that refund FTs will get the "asap" settlement date
        // (see FinancialTransactionBE.getRefundSettlementDate())
        PSPDate.addDaysToPSPTime(7);

        // now rebill that fee
        RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(executedFeeFT.getId().toString(),new SpcfMoney("9.99"));
        ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);

        DomainEntitySet<FinancialTransaction> refundedTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("Financial Txns ", 1, refundedTaxFTs.size());

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.
                cancelTransaction(SourceSystemCode.QBDT, "8574536", refundedTaxFTs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,"BatchTest09");
        DomainEntitySet<FinancialTransaction> financialTxsAfter =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit},
                        new TransactionStateCode[] {TransactionStateCode.Cancelled});
        DomainEntitySet<TransactionResponse> transactionResponsesAfter =
					TransactionResponse.findTransactionResponses(financialTxsAfter.get(0));

        assertEquals("Financial Transaction Ids:", financialTxsAfter.get(0).getId(), financialTxsAfter.get(0).getId());
        assertEquals("Transaction Response size:", transactionResponsesAfter.size() , transactionResponsesAfter.size());

        //Ensure there aren't any lingering mm txns
        assertNull("MM txn", financialTxsAfter.get(0).getMoneyMovementTransaction());

        PayrollServices.commitUnitOfWork();
    }

    /** Test for Customer intiated reversal cancel
     *
     */
    @Test
    public void testCustomerIntitatedReversalCancel() throws Exception {
        CancelERFinancialTxCoreDataLoader.loadEEReversalData(false);


        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployeeDdReversalDebit},
                        new TransactionStateCode[] {TransactionStateCode.Created});
        DomainEntitySet<TransactionResponse> transactionResponseCollection =
                    TransactionResponse.findTransactionResponses(financialTxs.get(0));

        PayrollServices.commitUnitOfWork();

        financialTxs = financialTxs.sort(FinancialTransaction.FinancialTransactionAmount());

        assertNotNull("MM txn", financialTxs.get(0).getMoneyMovementTransaction());
        assertTrue("Payroll run:", financialTxs.get(0).getPayrollRun().getPayrollRunStatus().equals(PayrollStatus.OffloadedAll));

        TransactionTypeCode transactionTypeCode = financialTxs.get(0).getTransactionType().getTransactionTypeCd();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        ProcessResult processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> financialTxsAfter =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployeeDdReversalDebit},
                        new TransactionStateCode[] {TransactionStateCode.Cancelled });
        DomainEntitySet<TransactionResponse> transactionResponsesAfter =
                    TransactionResponse.findTransactionResponses(financialTxs.get(0));

        assertEquals("Financial Transaction Ids:", financialTxs.get(0).getId(), financialTxsAfter.get(0).getId());
        assertEquals("Transaction Response size:", transactionResponsesAfter.size() , 0);
//        assertEquals("Transaction Response Token:", transactionResponsesAfter.get(transactionResponsesAfter.size() - 1 ).getTransactionTokenNumber(),
//                                                    transactionResponseCollection.get(0).getTransactionTokenNumber() + 1 );
//        assertTrue("Transaction Responses:", transactionResponsesAfter.get(0).equals(transactionResponseCollection.get(0)));
        assertEquals("Payroll run source Ids:", financialTxsAfter.get(0).getPayrollRun().getSourcePayRunId(), financialTxs.get(0).getPayrollRun().getSourcePayRunId());
        assertEquals("Payroll run Ids:", financialTxsAfter.get(0).getPayrollRun().getId(), financialTxs.get(0).getPayrollRun().getId());
        assertEquals("Payroll run Amounts:", financialTxsAfter.get(0).getPayrollRun().getPayrollDirectDepositAmount(), financialTxs.get(0).getPayrollRun().getPayrollDirectDepositAmount());
        assertTrue("Payroll run Status:", financialTxsAfter.get(0).getPayrollRun().getPayrollRunStatus().equals(PayrollStatus.OffloadedAll));

        //Ensure there aren't any lingering mm txns
        assertNull("MM txn", financialTxsAfter.get(0).getMoneyMovementTransaction());

        PayrollServices.commitUnitOfWork();
    }

    /** Test for intuit intiated reversal cancel
     *
     */
    @Test
    public void testIntuitIntitatedReversalCancel() throws Exception {
        CancelERFinancialTxCoreDataLoader.loadEEReversalData(true);


        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployeeDdReversalDebit},
                        new TransactionStateCode[] {TransactionStateCode.Created});
        DomainEntitySet<TransactionResponse> transactionResponseCollection =
                    TransactionResponse.findTransactionResponses(financialTxs.get(0));

        PayrollServices.commitUnitOfWork();

        financialTxs = financialTxs.sort(FinancialTransaction.FinancialTransactionAmount());
        assertNotNull("MM txn", financialTxs.get(0).getMoneyMovementTransaction());
        assertTrue("Payroll run:", financialTxs.get(0).getPayrollRun().getPayrollRunStatus().equals(PayrollStatus.PendingReversals));

        TransactionTypeCode transactionTypeCode = financialTxs.get(0).getTransactionType().getTransactionTypeCd();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);
         // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action FinancialTransactionCancel not valid for Financial Transaction with FinTxGseq "+ financialTxs.get(0).getId().toString() +
                ", which has a tx type of "+TransactionTypeCode.EmployeeDdReversalDebit +" and a tx status of "+ TransactionStateCode.Created+".";
        assertEquals("Error Message", messageText, message.getMessage());

    }

}
