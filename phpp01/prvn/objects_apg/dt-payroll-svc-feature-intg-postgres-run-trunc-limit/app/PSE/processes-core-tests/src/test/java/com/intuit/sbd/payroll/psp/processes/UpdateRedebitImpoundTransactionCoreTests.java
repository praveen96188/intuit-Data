package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.RedebitAddCoreDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jul 21, 2008
 * Time: 5:11:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateRedebitImpoundTransactionCoreTests {
    @Before
    public void runBeforeEachTest() {
        RedebitAddCoreDataLoader.loadBeforeTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void redebitUpdateCompanyDoesNotExist() {

        RedebitImpoundDTO redebitDTO = addRedebit();

        // Now update
        PayrollServices.beginUnitOfWork();
        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE,
                "InvalidCompanyId", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        out.println(processResult);

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
        RedebitImpoundDTO redebitDTO = addRedebit();

        PayrollServices.beginUnitOfWork();
        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(null, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
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
        updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE, null, redebitDTO);
        processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
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

    /**
     * Test for invalid Financial Tranaction Id
     */
    @Test
    public void testInvalidOriginalFinancialTransaction() {

        addRedebit();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        FinancialTransaction originalTxn = null;
        // Get the employer debit transactions returned for the payroll
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        assertTrue(financialTxs.size() == 1);
        originalTxn = financialTxs.get(0);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));

        redebitDTO.setOriginalFinancialTxId("aaaaaaa1-0afb-4d0f-8200-150f92d068fc");

        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE,
                "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "264", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Financial Transaction aaaaaaa1-0afb-4d0f-8200-150f92d068fc does not exist for company QBOE:123272727.";
        assertEquals("Error Message", messageText, message.getMessage());


    }

    /**
     * Test for null payroll
     */
    @Test
    public void testNullRedebitDTO() {

        RedebitImpoundDTO redebitDTO = addRedebit();

        PayrollServices.beginUnitOfWork();
        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", null);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "RedebitImpoundDTO has invalid value";
        assertEquals("Error Message", messageText, message.getMessage());


    }

    /**
     * Test for null payroll
     */
    @Test
    public void testRedebitDTOWithNullValues() {
        RedebitImpoundDTO redebitDTO = addRedebit();

        PayrollServices.beginUnitOfWork();
        redebitDTO = new RedebitImpoundDTO();

        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 3);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "OriginalFinancialTransactionId has invalid value", message.getMessage());

        // validate error code
        message = processResult.getMessages().get(1);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "RedebitAmount has invalid value", message.getMessage());

        // validate error code
        message = processResult.getMessages().get(2);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "InitiationDate has invalid value", message.getMessage());


    }

    /**
     * Test for null payroll
     */
    @Test
    public void testRedebitDTOInvalidAmount() {
        RedebitImpoundDTO redebitDTO = addRedebit();

        PayrollServices.beginUnitOfWork();
        redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("-20"));
        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());

        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 3);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "OriginalFinancialTransactionId has invalid value", message.getMessage());

        // validate error code
        message = processResult.getMessages().get(1);
        assertEquals("Error Code:", "283", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "The amount must be a non-zero, positive number.", message.getMessage());

        // validate error code
        message = processResult.getMessages().get(2);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "InitiationDate has invalid value", message.getMessage());


    }

    /**
     * Test message 186 - Company Bank Account is not Active
     */
    @Test
    public void testCompanyBankAccountIsNotActive() {
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Returned});
        FinancialTransaction originalTxn = null;
        originalTxn = financialTxs.get(0);
        
        PayrollServices.commitUnitOfWork();

        //Set company bank account  status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, "123272727", "123123", true, false);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();


        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1062", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:123272727 does not have an active bank account.", message.getMessage());

    }

    @Test
    public void testInvalidTransactionType() throws Exception {
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadTxData();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        FinancialTransaction originalTxn = null;
        // Get the employer debit transactions returned for the payroll
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertTrue(financialTxs.size() == 2);
        originalTxn = financialTxs.get(0);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action DDRedebitEdit not valid for Financial Transaction with FinTxGseq " +
                originalTxn.getId().toString() + ", which has a tx type of EmployeeDdCredit and a tx status of Created.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test method to test the validation: RedebitAmount should be less than or equal to UncollectedAmount
     */

    @Test
    public void testRedebitAmountExceedsUncollectedAmount() throws Exception {
        RedebitImpoundDTO redebitDTO = addRedebit();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        FinancialTransaction originalTxn = null;
        // Get the employer debit transactions returned for the payroll
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertTrue(financialTxs.size() == 1);
        originalTxn = financialTxs.get(0);
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        redebitDTO.setAmount(new SpcfMoney(redebitDTO.getAmount().add(SpcfDecimal.createInstance("100.00"))));
        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "504", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Redebit transaction amount " + redebitDTO.getAmount().toString()
                + " for transaction type EmployerDdRedebit exceeds the uncollected amount for the transaction.", message.getMessage());
    }

    /**
     * Test method to test the validation: RedebitAmount should be less than or equal to UncollectedAmount
     */

    @Test
    public void testInvalidInitiationDate() throws Exception {

        RedebitImpoundDTO redebitDTO = addRedebit();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        FinancialTransaction originalTxn = null;
        // Get the employer debit transactions returned for the payroll
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertTrue(financialTxs.size() == 1);
        originalTxn = financialTxs.get(0);
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        SpcfCalendar currentDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(currentDate, -1);
        redebitDTO.setInitiationDate(new DateDTO(currentDate));
        UpdateRedebitImpoundTransactionCore updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());

        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "10011", message.getMessageCode());
        CalendarUtils.clearTime(currentDate);
        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Cannot record a transaction of Settlement Type ACH and date " + currentDate.toString()
                + ", which is not a future banking day.", message.getMessage());

        // Verify Holiday
        PayrollServices.beginUnitOfWork();
        redebitDTO.setInitiationDate(new DateDTO("2007-06-16"));
        updateProc = new UpdateRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        processResult = updateProc.execute();
        processResult.setResult(updateProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // vaildate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "10011", message.getMessageCode());
        CalendarUtils.clearTime(currentDate);
        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Cannot record a transaction of Settlement Type ACH and date " +
                DateDTO.convertToSpcfCalendar(redebitDTO.getInitiationDate()).toString()
                + ", which is not a future banking day.", message.getMessage());
    }

    /**
     * Test method to test the process after validations
     */

    @Test
    public void testRedebitUpdate() throws Exception {

        RedebitImpoundDTO redebitDTO = addRedebit();

        PayrollServices.beginUnitOfWork();
        //Ensure that the txn return was resolved by adding the full amount
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        DomainEntitySet<TransactionReturn> txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        PayrollServices.commitUnitOfWork();
        assertEquals("Zero open transaction returns: ", 0, txRets.size());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        FinancialTransaction originalTxn = null;
        // Get the employer debit transactions returned for the payroll
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        assertEquals("Number of redebits", 1, financialTxs.size());
        originalTxn = financialTxs.get(0);
        FinancialTransaction eefinancialTx = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                null).get(0);
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        SpcfCalendar currentDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(currentDate, 1);
        redebitDTO.setInitiationDate(new DateDTO(currentDate));
        redebitDTO.setAmount(new SpcfMoney(originalTxn.getFinancialTransactionAmount().subtract(eefinancialTx.getFinancialTransactionAmount())));

        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList();
        redebitCollection.add(redebitDTO);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBOE, "123272727", redebitCollection);

        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // verify the actual payroll transaction status after redebit
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        assertEquals("Payroll status after redebit add:", payrollRun.getPayrollRunStatus(),
                PayrollStatus.PendingRedebit);

        // verify the existing Redebit was cancelled
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        assertTrue("Number of Cancelled employer DD Redebit transactions for payroll BatchId01: ", financialTxs.size() == 1);

        // verify the new redebit financial transaction added
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertTrue("Number of open employer DD Redebit transactions for payroll BatchId01: ", financialTxs.size() == 1);

        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(redebitDTO.getAmount()));

        // verify financial transaction settlement date
        SpcfCalendar settlementDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(settlementDate, 2);
        CalendarUtils.clearTime(settlementDate);
        SpcfCalendar actualSettlementDate = financialTxs.get(0).getSettlementDate().toLocal();
        CalendarUtils.clearTime(actualSettlementDate);
        assertEquals("Transaction Settlement Date: ", settlementDate.toString(), actualSettlementDate.toString());

        // verify bank account
        CompanyBankAccount expectedCompanyBankAcc = CompanyBankAccount.findCompanyBankAccount(
                company, "123123");


        assertTrue("Company bank a/c: ", financialTxs.get(0).getDebitBankAccount().equals(expectedCompanyBankAcc.getBankAccount()));

        Long token = TransactionResponse.getNextTxnResponseToken();

        int intToken = token.intValue();
        intToken = intToken - 2;
        DomainEntitySet<TransactionResponse> txResponses = TransactionResponse.findTransactionResponses(company, new Long(intToken));

        assertNotNull(txResponses);
        assertNotNull(txResponses.get(0));
        assertTrue("Company: ", txResponses.get(0).getCompany().equals(company));
        TransactionResponse expTxRes = financialTxs.get(0).getCurrentFinancialTransactionState().getTransactionResponse();
        assertTrue("Transaction Response: ", txResponses.get(0).equals(expTxRes));

        // verify transaction returns

        txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        assertEquals("One open transaction returns: ", 1, txRets.size());

        // Verify the company events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.RedebitAmountUpdated, CompanyEventStatus.Active, null, null);
        assertEquals("Number of RedebitAmountUpdated events", 1, companyEvents.size());

        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId),
                payrollRun.getId().toString());
        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.OldAmount),
                originalTxn.getFinancialTransactionAmount().toString());
        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.NewAmount),
                redebitDTO.getAmount().toString());

        companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.RedebitDateUpdated, CompanyEventStatus.Active, null, null);
        assertEquals("Number of RedebitAmountUpdated events", 1, companyEvents.size());

        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId),
                payrollRun.getId().toString());
        assertEquals("Event Detail",
                "2007/10/02 00:00:00.0",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.OldDate));
        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.NewDate),
                settlementDate.toString());

        PayrollServices.commitUnitOfWork();
    }

    private RedebitImpoundDTO addRedebit() {
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        FinancialTransaction originalTxn = null;
        // Get the employer debit transactions returned for the payroll
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        assertTrue(financialTxs.size() == 1);
        originalTxn = financialTxs.get(0);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());

        List<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBOE, "123272727", redebitCollection);

        // Commit
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        DomainEntitySet<FinancialTransaction> allResults = processResult.getResult();

        assertEquals("number of fin txns", 1, allResults.size());

        FinancialTransaction addedTxn = allResults.get(0);
        assertEquals("Fin txn type", TransactionTypeCode.EmployerDdRedebit, addedTxn.getTransactionType().getTransactionTypeCd());

        return redebitDTO;
    }

}
