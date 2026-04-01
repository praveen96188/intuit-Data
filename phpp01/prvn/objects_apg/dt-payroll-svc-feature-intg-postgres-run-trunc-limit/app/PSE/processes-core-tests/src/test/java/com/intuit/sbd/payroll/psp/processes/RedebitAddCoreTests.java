package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company123272727DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.RedebitAddCoreDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static org.junit.Assert.*;

/**
 * User: rsakhamuri
 * Date: Dec 12, 2007
 * Time: 3:05:44 PM
 */
public class RedebitAddCoreTests {

    static String sPayrollBatchId;
    static String sFeeTxnId; // ID of original Fee debit transaction
    private static FinancialTransaction sRedebitTxn; // the Fee Redebit txn we created, if we succeeded in creating one


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

    public void redebitAddCompanyDoesNotExist() {

        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

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
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());

        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE,
                "InvalidCompanyId", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());

        // Commit
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
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
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

        List<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        redebitImpoundDTOs.add(redebitDTO);
        ProcessResult processResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                null, "123272727", redebitImpoundDTOs);

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        processResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                SourceSystemCode.QBOE, null, redebitImpoundDTOs);

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
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

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

        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
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
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", null);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
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
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
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
        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("-2"));
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
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
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action DDRedebitAdd not valid for Financial Transaction with FinTxGseq " +
                originalTxn.getId().toString() + ", which has a tx type of EmployeeDdCredit and a tx status of Created.";
        assertEquals("Error Message", messageText, message.getMessage());

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
        for (FinancialTransaction finTx : financialTxs) {
            finTx.updateFinancialTransactionState(TransactionStateCode.Completed);
        }
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
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1062", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:123272727 does not have an active bank account.", message.getMessage());

    }

    /**
     * Test method to test the validation: RedebitAmount should be less than or equal to UncollectedAmount
     */

    @Test
    public void testRedebitAmountExceedsUncollectedAmount() throws Exception {

        PayrollRunDTO payrollRunDTO = RedebitAddCoreDataLoader.loadTestRedebitAddData();

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
        redebitDTO.setAmount(new SpcfMoney(originalTxn.getFinancialTransactionAmount().add(SpcfDecimal.createInstance("100.00"))));
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "504", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Redebit transaction amount " + redebitDTO.getAmount().toString()
                + " for transaction type EmployerDdDebit exceeds the uncollected amount for the transaction.", message.getMessage());
    }

    /**
     * Test method to test the validation: RedebitAmount should be less than or equal to UncollectedAmount
     */

    @Test
    public void testInvalidInitiationDate() throws Exception {

        PayrollRunDTO payrollRunDTO = RedebitAddCoreDataLoader.loadTestRedebitAddData();

        // Verify Past date
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
        SpcfCalendar currentDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(currentDate, -1);
        redebitDTO.setInitiationDate(new DateDTO(currentDate));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
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
        addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
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
    public void testRedebitAdd() throws Exception {

        PayrollRunDTO payrollRunDTO = RedebitAddCoreDataLoader.loadTestRedebitAddData();

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
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // verify no validation errors
        org.junit.Assert.assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        // verify the actual payroll transaction status after redebit
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());

//        assertEquals("Payroll status after redebit add:",
//                PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

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
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(payrollRun.getPayrollDirectDepositAmount()));

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

        DomainEntitySet<TransactionReturn> txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        assertTrue("No open transaction returns: ", txRets.size() == 0);

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test method to add a redebit again followed by Returned ER DD DEBIT, and ER DD REDEBIT transactions
     */

    @Test
    public void testRedebitAddTwice() throws Exception {

        PayrollServices.beginUnitOfWork();
        // load data
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForRedebitReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
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
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();
        System.out.println("processResult " + processResult.getMessages());

        // verify no validation errors
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        // verify the actual payroll transaction status after redebit is redebit pending
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());

//        assertEquals("Payroll status after redebit add:",
//                PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        // verify the new redebit financial transaction added

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertTrue("Number of open employer DD Redebit transactions for payroll BatchId01: ", financialTxs.size() == 1);

        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(payrollRun.getPayrollDirectDepositAmount()));

        // verify intuit and company bank a/cs

        CompanyBankAccount expectedCompanyBankAcc = CompanyBankAccount.findCompanyBankAccount(
                company, "123123");
        IntuitBankAccount expectedIntuitBankAcc = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(TransactionTypeCode.EmployerDdRedebit),
                CreditDebitCode.Credit);

        assertTrue("Company bank a/c: ", financialTxs.get(0).getDebitBankAccount().equals(expectedCompanyBankAcc.getBankAccount()));
        assertTrue("Company bank a/c: ", financialTxs.get(0).getCreditBankAccount().getAccountNumber().equals(expectedIntuitBankAcc.getBankAccount().getAccountNumber()));

        // verify transaction response
        Long token = TransactionResponse.getNextTxnResponseToken();

        int intToken = token.intValue();
        intToken = intToken - 3;
        DomainEntitySet<TransactionResponse> txResponses = TransactionResponse.findTransactionResponses(company, new Long(intToken));
        TransactionResponse expTxRes = financialTxs.get(0).getCurrentFinancialTransactionState().getTransactionResponse();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertNotNull(txResponses);
        assertTrue("Number of transaction responses: ", txResponses.size() == 2);
        assertNotNull(txResponses.get(0));
        assertTrue("Company: ", txResponses.get(0).getCompany().equals(company));
        assertTrue("Transaction Response: ", txResponses.get(0).equals(expTxRes));

        // verify transaction returns
        DomainEntitySet<TransactionReturn> txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        PayrollServices.commitUnitOfWork();
        assertTrue("No open transaction returns: ", txRets.size() == 0);

    }

    /**
     * Test method to verify the open transaction returns associated to the original financial transaction
     * are resolved.
     */

    @Test
    public void testEmployerRedebitAdd_ResolveReturns() throws Exception {

        PayrollServices.beginUnitOfWork();
        // load data
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForEmployerDebitReturn_R01();
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        PayrollServices.commitUnitOfWork();

        offloadAndReturn(payrollRun, TransactionTypeCode.EmployerDdRedebit);

        FinancialTransaction originalTxn = null;
        PayrollServices.beginUnitOfWork();
        // Get the employer debit transactions returned for the payroll
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        FinancialTransaction eefinancialTx = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                null).get(0);
        assertTrue(financialTxs.size() == 1);
        originalTxn = financialTxs.get(0);
        // Add a partial redebit
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney(originalTxn.getFinancialTransactionAmount().subtract(eefinancialTx.getFinancialTransactionAmount())));
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // verify the actual payroll transaction status after redebit is redebit pending
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());

        // verify the new redebit financial transaction added

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();
        assertTrue("Number of open employer DD Redebit transactions for payroll BatchId01: ", financialTxs.size() == 1);

        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(
                new SpcfMoney(originalTxn.getFinancialTransactionAmount().subtract(eefinancialTx.getFinancialTransactionAmount()))));

        offloadAndReturn(payrollRun, TransactionTypeCode.EmployerDdRedebit);

        // Verify there are three open transaction returns
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        assertTrue("Number of unresolved transaction returns: ", txRets.size() == 3);

        // Now add full redebit
        redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // verify the full redebit
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        // verify intuit and company bank a/cs

        CompanyBankAccount expectedCompanyBankAcc = CompanyBankAccount.findCompanyBankAccount(
                company, "123123");
        IntuitBankAccount expectedIntuitBankAcc = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(TransactionTypeCode.EmployerDdRedebit),
                CreditDebitCode.Credit);

        assertTrue("Company bank a/c: ", financialTxs.get(0).getDebitBankAccount().equals(expectedCompanyBankAcc.getBankAccount()));
        assertTrue("Company bank a/c: ", financialTxs.get(0).getCreditBankAccount().getAccountNumber().equals(expectedIntuitBankAcc.getBankAccount().getAccountNumber()));

        // verify transaction response
        Long token = TransactionResponse.getNextTxnResponseToken();

        int intToken = token.intValue();
        intToken = intToken - 3;
        DomainEntitySet<TransactionResponse> txResponses = TransactionResponse.findTransactionResponses(company, new Long(intToken));
        TransactionResponse expTxRes = financialTxs.get(0).getCurrentFinancialTransactionState().getTransactionResponse();

        assertNotNull(txResponses);
        assertTrue("Number of transaction responses: ", txResponses.size() == 2);
        assertNotNull(txResponses.get(0));
        assertTrue("Company: ", txResponses.get(0).getCompany().equals(company));
        assertTrue("Transaction Response: ", txResponses.get(0).equals(expTxRes));

        // verify no open transaction returns
        txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        PayrollServices.commitUnitOfWork();
        assertTrue("No open transaction returns: ", txRets.size() == 0);

    }

    /**
     * Test method to verify the open transaction returns associated to the original financial transaction
     * are resolved.
     */

    @Test
    public void testFeeRedebitAdd_ResolveReturns() throws Exception {

        PayrollServices.beginUnitOfWork();
        // load data
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForEmployerDebitReturn_R01();
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        PayrollServices.commitUnitOfWork();

        offloadAndReturn(payrollRun, TransactionTypeCode.EmployerDdRedebit);
        offloadAndReturn(payrollRun, TransactionTypeCode.EmployerFeeDebit);

        FinancialTransaction originalTxn = null;

        PayrollServices.beginUnitOfWork();
        // Get the employer debit transactions returned for the payroll
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        FinancialTransaction eefinancialTx = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                null).get(0);
        assertTrue(financialTxs.size() == 1);
        originalTxn = financialTxs.get(0);
        // Add a partial redebit
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney(originalTxn.getFinancialTransactionAmount().subtract(eefinancialTx.getFinancialTransactionAmount())));
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // verify the new redebit financial transaction added
        PayrollServices.beginUnitOfWork();
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertTrue("Number of open employer DD Redebit transactions for payroll BatchId01: ", financialTxs.size() == 1);

        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(
                new SpcfMoney(originalTxn.getFinancialTransactionAmount().subtract(eefinancialTx.getFinancialTransactionAmount()))));
        PayrollServices.commitUnitOfWork();

        offloadAndReturn(payrollRun, TransactionTypeCode.EmployerDdRedebit);

        PayrollServices.beginUnitOfWork();       
        // Get the Fee debit transactions returned for the payroll
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned}).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));

        assertEquals(1, financialTxs.size());
        originalTxn = financialTxs.get(0);
        // Add a partial redebit for fee
        redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney(originalTxn.getFinancialTransactionAmount().subtract(SpcfDecimal.createInstance("10.00"))));
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // verify the actual payroll transaction status after redebit is redebit pending
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());

//assertEquals("Payroll status after redebit add:",
//                PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        // verify the new redebit financial transaction added

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of open employer Fee Redebit transactions for payroll BatchId01: ", 1, financialTxs.size());

        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(
                new SpcfMoney(originalTxn.getFinancialTransactionAmount().subtract(SpcfDecimal.createInstance("10.00")))));
        PayrollServices.commitUnitOfWork();

        offloadAndReturn(payrollRun, TransactionTypeCode.EmployerFeeRedebit);

        // Verify there are four open transaction returns
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        assertEquals("No of open transaction returns: ", 5, txRets.size());

        // Now add full redebit for the fee
        redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // verify the full redebit
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        // verify intuit and company bank a/cs

        CompanyBankAccount expectedCompanyBankAcc = CompanyBankAccount.findCompanyBankAccount(
                company, "123123");
        IntuitBankAccount expectedIntuitBankAcc = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(TransactionTypeCode.EmployerDdRedebit),
                CreditDebitCode.Credit);

        assertTrue("Company bank a/c: ", financialTxs.get(0).getDebitBankAccount().equals(expectedCompanyBankAcc.getBankAccount()));
        assertTrue("Company bank a/c: ", financialTxs.get(0).getCreditBankAccount().getAccountNumber().equals(expectedIntuitBankAcc.getBankAccount().getAccountNumber()));

        // verify transaction response
        Long token = TransactionResponse.getNextTxnResponseToken();

        int intToken = token.intValue();
        intToken = intToken - 3;
        DomainEntitySet<TransactionResponse> txResponses = TransactionResponse.findTransactionResponses(company, new Long(intToken));
        TransactionResponse expTxRes = financialTxs.get(0).getCurrentFinancialTransactionState().getTransactionResponse();

        assertNotNull(txResponses);
        assertTrue("Number of transaction responses: ", txResponses.size() == 2);
        assertNotNull(txResponses.get(0));
        assertTrue("Company: ", txResponses.get(0).getCompany().equals(company));
        assertTrue("Transaction Response: ", txResponses.get(0).equals(expTxRes));

        // verify three open ER Debit/Redebit transaction returns
        txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        PayrollServices.commitUnitOfWork();
        assertEquals("No of open transaction returns: ", 3, txRets.size());

    }

    /**
     * Test method to verify the open transaction returns associated to the original financial transaction
     * are resolved.
     */

    @Test
    public void testSalesTaxRedebitAdd_ResolveReturns() throws Exception {

        PayrollServices.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company = c3dl.persistCompany3();

        // make sure that company is at an address that will be subject to sales tax
        DTOFactory fac = new DTOFactory();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO dtoUpdate = fac.create(company);
        dtoUpdate.setLegalAddress(DataLoader.TAXABLE_ADDRESS);
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(),
                                                                                       company.getSourceCompanyId(),
                                                                                       dtoUpdate);
        PayrollServicesTest.assertSuccess("Updating company address for taxability", prUpdate);

        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollRun payroll = c3dl.persistPayrollRun(payrollRunDTO);

        // add a per-transmission charge -- it's subject to sales tax at the TAXABLE_ADDRESS... per-check is not
        DomainEntitySet<FinancialTransaction> found = FinancialTransaction.findFinancialTransactions(
                                                    company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                    TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        SpcfCalendar settlementDate = found.get(0).getSettlementDate().toLocal();
        CompanyBankAccount cba = payroll.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        CompanyOffering companyOffering = company.getOffering(ServiceCode.DirectDeposit);
        BillingDetail.createBillingDetail(payroll, cba, OfferingServiceChargeType.PerTransmission, 1, settlementDate, companyOffering.getOffering().getOfferingCode()); // a taxable fee -- per-check is not

        PayrollServices.commitUnitOfWork();

        //Offload er txn for first payroll
        Application.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloadAndReturn(payrollRun, TransactionTypeCode.ServiceSalesAndUseTax);
        offloadAndReturn(payrollRun, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);

        // Verify there are open transaction returns
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        PayrollServices.commitUnitOfWork();
        assertTrue("No open transaction returns: ", txRets.size() == 1);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest87");
        FinancialTransaction originalTxn = null;
        // Get the service sales and use tax transaction
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned ServiceSalesAndUseTax txns", 1, financialTxs.size());

        originalTxn = financialTxs.get(0);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBDT, "8574536", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        // verify the actual payroll transaction status after redebit is redebit pending
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "8574536", SourceSystemCode.QBDT);

        payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());

//assertEquals("Payroll status after redebit add:",
//                PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        // verify the new redebit financial transaction added

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTaxRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();
        assertTrue("Number of open ServiceSalesAndUseTax Redebit transactions for payroll: ", financialTxs.size() == 1);

        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(
                new SpcfMoney(originalTxn.getFinancialTransactionAmount())));

        // Verify there are open transaction returns for the original debit and redebit
        PayrollServices.beginUnitOfWork();
        txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of open transaction returns: ", 2, txRets.size());

    }

    /**
     * Test method to test whether the process is cancelling the Pending Reversals for the payroll
     */

    @Test
    public void testRedebitAdd_WithPendingReversals() throws Exception {
        RedebitAddTestDataLoader redebitDataLoader = new RedebitAddTestDataLoader();
        PayrollServices.beginUnitOfWork();
        redebitDataLoader.loadTxData();
        PayrollServices.commitUnitOfWork();

        // offload all txns

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        Application.commitUnitOfWork();

        // create an ER return for company1 first payroll
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company, "BatchId01");
        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c1FinTxns,
                "R02",
                "This is an ER Return");

        Assert.assertEquals("Number of txn returns", 1, returnList.size());

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of Company1 ERDDDB EX txns", 1, c1FinTxns.size());
        Assert.assertEquals("Number of Company1 ERDDDB Returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId("BatchId01");
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        txnReverseDTO.setIntuitInitiatedReversals(true);

        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, company.getSourceCompanyId(), txnReverseDTO);
        assertSuccess(result);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        PayrollServices.commitUnitOfWork();

        assertEquals("Payroll Status", payrollRun.getPayrollRunStatus(), PayrollStatus.PendingReversals);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
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
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // verify no validation errors
        org.junit.Assert.assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        // verify the actual payroll transaction status after redebit
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRun.getSourcePayRunId());

//assertEquals("Payroll status after redebit add:",
//                PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

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
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(payrollRun.getPayrollDirectDepositAmount()));

        // verify financial transaction settlement date
        SpcfCalendar settlementDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(settlementDate, 1);
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

        DomainEntitySet<TransactionReturn> txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        assertTrue("No open transaction returns: ", txRets.size() == 0);

        // verify EE DD RV DB txns are cancelled
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        assertEquals("Cancelled Employee Reversal Transactions", 2, financialTxs.size());
        PayrollServices.commitUnitOfWork();
    }

    // Fee Redebit tests

    @Test
    public void testFeeRedebitHappy() {
        reset();

        // make the fee debit look like it was returned by the bank
        returnFeeDebit(sFeeTxnId);
//        resolveReturnedTxn(txnReturn);

        // now do the test
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        FinancialTransaction originalTxn = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(sFeeTxnId));
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        org.junit.Assert.assertTrue(processResult.isSuccess());
        sRedebitTxn = processResult.getResult();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction originalFeeTransaction = Application
                .findById(FinancialTransaction.class, SpcfUniqueId.createInstance(sFeeTxnId));

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        // verify the actual payroll transaction status after redebit
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sPayrollBatchId);

//        assertEquals("Payroll status after redebit add:",
//                PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        // verify the new redebit financial transaction added
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertTrue("Number of open employer DD Redebit transactions for payroll BatchId01: ", financialTxs.size() == 1);

        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(originalTxn.getFinancialTransactionAmount()));

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

        DomainEntitySet<TransactionReturn> txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        assertTrue("No open transaction returns: ", txRets.size() == 0);
        // verify the fee debit transaction
        assertEquals("SKU:", "408177", originalFeeTransaction.getSku());
        assertTrue("SKU Quantity:", 1 == originalFeeTransaction.getSkuQuantity());

        // verify the fee redebit transaction
        assertEquals("SKU:", originalFeeTransaction.getSku(), getSRedebitTxn().getSku());
        assertTrue("Quantity", 1 == getSRedebitTxn().getSkuQuantity());
        assertEquals("Financial Transaction association", originalFeeTransaction, getSRedebitTxn().getOriginalTransaction());
        PayrollServices.commitUnitOfWork();
    }

    // Non-cancelled Fee Redebit already exists
    @Test
    public void testFeeRedebit_FeeRedebitAlreadyExists() {
        reset();

        // make the fee debit look like it was returned by the bank
        returnFeeDebit(sFeeTxnId);

        // now do the test
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        FinancialTransaction originalTxn = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(sFeeTxnId));
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        org.junit.Assert.assertTrue(processResult.isSuccess());
        sRedebitTxn = processResult.getResult();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction originalFeeTransaction = Application
                .findById(FinancialTransaction.class, SpcfUniqueId.createInstance(sFeeTxnId));

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        // verify the actual payroll transaction status after redebit
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sPayrollBatchId);

//assertEquals("Payroll status after redebit add:",
//                PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        // verify the new redebit financial transaction added
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertTrue("Number of open employer DD Redebit transactions for payroll BatchId01: ", financialTxs.size() == 1);

        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(originalTxn.getFinancialTransactionAmount()));

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

        DomainEntitySet<TransactionReturn> txRets = TransactionReturn.findTransactionReturnsExcludedStatus(company,
                TransactionReturnStatusCode.Resolved);
        assertTrue("No open transaction returns: ", txRets.size() == 0);
        // verify the fee debit transaction
        assertEquals("SKU:", "408177", originalFeeTransaction.getSku());
        assertTrue("SKU Quantity:", 1 == originalFeeTransaction.getSkuQuantity());

        // verify the fee redebit transaction
        assertEquals("SKU:", originalFeeTransaction.getSku(), getSRedebitTxn().getSku());
        assertTrue("Quantity", 1 == getSRedebitTxn().getSkuQuantity());
        assertEquals("Financial Transaction association", originalFeeTransaction, getSRedebitTxn().getOriginalTransaction());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        originalTxn = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(sFeeTxnId));
        redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        // verify validation errors
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "285", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "A fee transaction was already reissued.  Company: QBOE:123272727" +
                ", financial transaction id: " + originalTxn.getId().toString() + ".", message.getMessage());

    }

    @Test
    public void feeAndTaxRedebit() {
        PayrollServices.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company = c3dl.persistCompany3();

        // make sure that company is at an address that will be subject to sales tax
        DTOFactory fac = new DTOFactory();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO dtoUpdate = fac.create(company);
        dtoUpdate.setLegalAddress(DataLoader.TAXABLE_ADDRESS);
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                dtoUpdate);
        PayrollServicesTest.assertSuccess("Updating company address for taxability", prUpdate);

        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollRun payroll = c3dl.persistPayrollRun(payrollRunDTO);

        // add a per-transmission charge -- it's subject to sales tax at the TAXABLE_ADDRESS... per-check is not
        DomainEntitySet<FinancialTransaction> found = FinancialTransaction.findFinancialTransactions(
                company.getSourceSystemCd(), company.getSourceCompanyId(),
                TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        SpcfCalendar settlementDate = found.get(0).getSettlementDate().toLocal();
        CompanyBankAccount cba = payroll.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        CompanyOffering companyOffering = company.getOffering(ServiceCode.DirectDeposit);
        BillingDetail.createBillingDetail(payroll, cba, OfferingServiceChargeType.PerTransmission, 1, settlementDate, companyOffering.getOffering().getOfferingCode()); // a taxable fee -- per-check is not

        PayrollServices.commitUnitOfWork();

        //Offload er txn for first payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(5);
        TransactionReverseDTO txnRevDTO = new TransactionReverseDTO();
        txnRevDTO.setChargeFee(true);
        txnRevDTO.setIntuitInitiatedReversals(false);
        txnRevDTO.setSourcePayrollRunId("BatchTest87");
        txnRevDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseResult = PayrollServices.payrollManager
                .reverseTransaction(SourceSystemCode.QBDT, "8574536", txnRevDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(reverseResult);

        //Offload reversals
        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Return reversal fee
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest87", null, null, null, TransactionTypeCode.EmployerFeeDebit, null, null,
                TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> txnsToReturn = new DomainEntitySet<FinancialTransaction>();

        for (FinancialTransaction currTxn : c1FinTxns) {
            String sku = currTxn.getSku();
            if (sku != null) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
                if (osc.equals(OfferingServiceChargeType.ReversalFee)) {
                    txnsToReturn.add(currTxn);
                }
            }
        }
        assertEquals("Number of reversal fees", 1, txnsToReturn.size());
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(txnsToReturn, "R01",
                "This is an NSF description");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        // now do the test: add Fee Redebit
        PayrollServices.beginUnitOfWork();
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        FinancialTransaction originalTxn = txnsToReturn.get(0);
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBDT, "8574536", redebitDTO);
        ProcessResult<FinancialTransaction> processResult = addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());

        PayrollServices.commitUnitOfWork();

        assertSuccess("fee redebit", processResult);
        sRedebitTxn = processResult.getResult();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction originalFeeTransaction = Application
                .findById(FinancialTransaction.class, txnsToReturn.get(0).getId());
        // verify the fee debit transaction
        assertEquals("SKU:", "293939", originalFeeTransaction.getSku());
        assertTrue("SKU Quantity:", 1 == originalFeeTransaction.getSkuQuantity());

        // verify the fee redebit transaction
        assertEquals("SKU:", originalFeeTransaction.getSku(), getSRedebitTxn().getSku());
        assertTrue("Quantity", 1 == getSRedebitTxn().getSkuQuantity());
        assertEquals("Financial Transaction association", originalFeeTransaction, getSRedebitTxn().getOriginalTransaction());
        PayrollServices.commitUnitOfWork();

        // now do the test: add Service Sales and Use Tax Redebit
        PayrollServices.beginUnitOfWork();
        c1FinTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest87", null, null, null, TransactionTypeCode.ServiceSalesAndUseTax, null, null,
                TransactionStateCode.Returned);

        redebitDTO = new RedebitImpoundDTO();
        originalTxn = c1FinTxns.get(0);
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBDT, "8574536", redebitDTO);
        processResult = addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());

        PayrollServices.commitUnitOfWork();

        assertSuccess("fee redebit", processResult);
        sRedebitTxn = processResult.getResult();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction originalTaxTransaction = Application
                .findById(FinancialTransaction.class, c1FinTxns.get(0).getId());
        // verify the service sales and use tax debit transaction
        assertEquals("SKU:", "293939", originalTaxTransaction.getSku());
        assertTrue("SKU Quantity:", 1 == originalTaxTransaction.getSkuQuantity());

        // verify the service sales and use tax redebit transaction
        assertEquals("SKU:", originalTaxTransaction.getSku(), getSRedebitTxn().getSku());
        assertTrue("Quantity", 1 == getSRedebitTxn().getSkuQuantity());
        assertEquals("Financial Transaction association", originalTaxTransaction, getSRedebitTxn().getOriginalTransaction());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void happy_with_unresolved_return() {
        reset();

        // make the fee debit look like it was returned by the bank
        returnFeeDebit(sFeeTxnId);
        // not calling resolveReturnedTxn(txnReturn);

        // now do the test
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        FinancialTransaction originalTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class,
                SpcfUniqueId.createInstance(sFeeTxnId));
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());

        PayrollServices.commitUnitOfWork();

        org.junit.Assert.assertTrue(processResult.isSuccess());
        sRedebitTxn = processResult.getResult();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction originalFeeTransaction = Application
                .findById(FinancialTransaction.class, SpcfUniqueId.createInstance(sFeeTxnId));
        // verify the fee debit transaction
        assertEquals("SKU:", "408177", originalFeeTransaction.getSku());
        assertTrue("SKU Quantity:", 1 == originalFeeTransaction.getSkuQuantity());

        // verify the fee redebit transaction
        assertEquals("SKU:", originalFeeTransaction.getSku(), getSRedebitTxn().getSku());
        assertTrue("Quantity", 1 == getSRedebitTxn().getSkuQuantity());
        assertEquals("Financial Transaction association", originalFeeTransaction, getSRedebitTxn().getOriginalTransaction());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void txn_already_redebited() {
        reset();
        testFeeRedebitHappy();

        // now try to do another one...
        PayrollServices.beginUnitOfWork();
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        FinancialTransaction originalTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class,
                SpcfUniqueId.createInstance(sFeeTxnId));
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        AddRedebitImpoundTransactionCore addProc = new AddRedebitImpoundTransactionCore(SourceSystemCode.QBOE, "123272727", redebitDTO);
        ProcessResult<FinancialTransaction> processResult =addProc.execute();
        processResult.setResult(addProc.getFinancialTransaction());
        PayrollServices.commitUnitOfWork();

        assertError("already has un-cancelled child redebit", processResult, "285");
    }

    private static void reset() {
        try {
            PayrollServices.beginUnitOfWork();
            Application.truncateTables();
            ApplicationSecondary.truncateTables();
            PayrollServices.commitUnitOfWork();

            // make a fee debit transaction
            sPayrollBatchId = null;
            sRedebitTxn = null;
            sFeeTxnId = addFeeDebit();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static String addFeeDebit() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company123272727DataLoader cdl = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        // create and save a payroll run
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        cdl.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        sPayrollBatchId = payrollRunDTO.getPayrollTXBatchId();

        // update its status to Complete
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payroll = PayrollRun.findPayrollRun(company, sPayrollBatchId);
        payroll.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payroll);
        PayrollServices.commitUnitOfWork();

        // create a fee transaction
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = calendar.getTime();

        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", sPayrollBatchId,
                SettlementTypeDTO.ACH /*was WIRE*/, txDate, new SpcfMoney("50.00"),
                OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> result = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        payroll = PayrollServices.entityFinder.findById(PayrollRun.class, payroll.getId());
        DomainEntitySet<FinancialTransaction> txns;
        txns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created}).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));

        assertTrue(txns.size() == 1);
        FinancialTransaction feeTxn = txns.get(0);

        // make sure this is the one we just added
        // (lazy initialization means you have to call things like feeTxn.getCompany() within the UnitOfWork...)
        assertTrue("Company: ", feeTxn.getCompany().equals(company));
        assertTrue("Payroll: ", feeTxn.getPayrollRun().equals(payroll));

        PayrollServices.commitUnitOfWork();

        return feeTxn.getId().toString();
    }

    private static void returnFeeDebit(String feeTxnId) {
        SpcfCalendar returnTime = SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone());

        // offload all txns

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();


        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE Fee ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create an ER return for company1 first payroll Fee Txn
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company, "BatchId01");
        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed}).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c1FinTxns,
                "R01",
                "This is an ER Return");

        Assert.assertEquals("Number of txn returns", 1, returnList.size());

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);

        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of Company1 ERFEEDB EX txns", 1, c1FinTxns.size());
        Assert.assertEquals("Number of Company1 ERFEEDB Returns", 1, returnList.size());

    }

    private static FinancialTransaction getSRedebitTxn() {
        if (sRedebitTxn != null) {
            return PayrollServices.entityFinder.findById(FinancialTransaction.class, sRedebitTxn.getId());
        } else {
            return null;
        }
    }

    private static void assertError(String msg, ProcessResult result, String errorCode) {
        org.junit.Assert.assertTrue(msg, !result.isSuccess());
        org.junit.Assert.assertTrue(msg, result.getMessages().size() > 0);
        org.junit.Assert.assertTrue(msg, result.getMessages().get(0).getMessageCode().equals(errorCode));
        System.out.println(msg + ": got expected error " + errorCode);
    }

    private void offloadAndReturn(PayrollRun pPayrollRun, TransactionTypeCode pTransactionTypeCode) {
        // return the partial redebit
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(7);
        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        pPayrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, pPayrollRun.getId());
        DomainEntitySet<FinancialTransaction> c1FinTxns = pPayrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{pTransactionTypeCode},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c1FinTxns,
                "R01",
                "This is an ER Return");

        Assert.assertEquals("Number of txn returns", 1, returnList.size());

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();
    }

}
