package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERRefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.ERFinancialTxRefundCoreDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * User: rsakhamuri
 * Date: Jan 4, 2008
 * Time: 11:52:45 AM

 */
public class ERFinancialTxRefundCoreTests {

    @Before
    public void runBeforeEachTest() {
          PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    /**
     *  Test error message 169 - Company Does Not Exist
    */
    @Test
    public void companyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader payrollSubmitDataLoader = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = payrollSubmitDataLoader.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERRefundDTO refundDTO  = new ERRefundDTO("2c915611-1732-d4c4-0117-32d53e810028", new SpcfMoney("50.00"),
                                                 new DateDTO(CalendarUtils.convertToSpcfCalendar(txDate)),
                                                 SettlementTypeDTO.ACH);

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272720", refundDTO);
        // Commit
        PayrollServices.commitUnitOfWork();

        out.println(processResult);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:123272720 does not exist.", message.getMessage());

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

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERRefundDTO refundDTO  = new ERRefundDTO("2c915611-1732-d4c4-0117-32d53e810028", new SpcfMoney("50.00"),
                                                 new DateDTO(CalendarUtils.convertToSpcfCalendar(txDate)),
                                                 SettlementTypeDTO.ACH);

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(null, "123272727", refundDTO);
        // Commit
        PayrollServices.commitUnitOfWork();
        out.println(processResult);

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        PayrollServices.beginUnitOfWork();        
        processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, null, refundDTO);
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
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERRefundDTO refundDTO  = new ERRefundDTO("2c915611-1732-d4c4-0117-32d53e810028", new SpcfMoney("50.00"),
                                                 new DateDTO(CalendarUtils.convertToSpcfCalendar(txDate)),
                                                 SettlementTypeDTO.ACH);

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        // Commit
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
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERRefundDTO refundDTO  = new ERRefundDTO(null, new SpcfMoney("50.00"),
                                                 new DateDTO(CalendarUtils.convertToSpcfCalendar(txDate)),
                                                 SettlementTypeDTO.ACH);

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        // Commit
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

    /** Test for already refunded transaction
     *
     */
    @Test
    public void testAlreadyRefundedNonCancelled() throws Exception {

        //Function call to add Employer DD Reject Refund Credit transaction
        ERFinancialTxRefundCoreDataLoader.addEmployerDDRejectRefundTransaction();

        ERRefundDTO erRefundDTO = addEmployerReturnedRefundTransaction();

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(
                SourceSystemCode.QBOE,
                "123272727",
                erRefundDTO);
        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "265", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Financial Transaction "+erRefundDTO.getFinancialTxId()+" cannot be refunded, because a refund has already been attempted.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    /**
     * Test for refund a transaction which is not in the required type and state
     */
    @Test
    public void testRefundInvalidCriteria() {
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
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[] {TransactionStateCode.Returned});

        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERRefundDTO refundDTO  = new ERRefundDTO(financialTxs.get(0).getId().toString(), new SpcfMoney("50.00"),
                                                 new DateDTO(CalendarUtils.convertToSpcfCalendar(txDate)),
                                                 SettlementTypeDTO.ACH);

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action IssueReissueRefundEr not valid for Financial Transaction with FinTxGseq "+ financialTxs.get(0).getId().toString() +", which has a tx type of "+ TransactionTypeCode.EmployerDdDebit +" and a tx status of "+TransactionStateCode.Returned+".";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test for refund a transaction with type not meeting the criteria

    @Test
    public void testRefundTypeNotMeetingCriteria() {
        PayrollServices.beginUnitOfWork();
        Application.truncateTables();
        PayrollServices.commitUnitOfWork();

        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        PayrollRunDTO payrollRunDTO = txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = CompanyBE.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRunBE.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                PayrollRunBE.getFinancialTransactions(payrollRun,
                        new String[] { TransactionTypeBE.Codes.EMPLOYER_DD_DEBIT },
                        new String[] { TransactionStateBE.Codes.RETURNED });

         // change the original transaction type
        TransactionType transactionType = TransactionTypeBE.findTransactionType(TransactionTypeBE.Codes.ER_REVERSE_FEE_REDEBIT);
        financialTxs.get(0).setTransactionType(transactionType);
        FinancialTransactionBE.updateFinancialTransactionState(financialTxs.get(0), TransactionStateBE.Codes.EXECUTED);
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERRefundDTO refundDTO  = new ERRefundDTO(financialTxs.get(0).getId().toString(), new SpcfMoney("50.00"),
                                                 new DateDTO(CalendarUtils.convertToSpcfCalendar(txDate)),
                                                 "123123", SettlementTypeDTO.ACH);

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransaction.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "199", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Refund cannot be completed because refunds aren't allowed for transaction type ERDDRTRFCR.";
        assertEquals("Error Message", messageText, message.getMessage());

    } */

    /**
     * Test message 186 - Company Bank Account is not Active for ACH settlement
     */

    @Test
    public void testCBAIsNotActive() throws Exception {
        //Function call to add Employer DD Reject Refund Credit transaction
         ERFinancialTxRefundCoreDataLoader.addEmployerDDRejectRefundTransaction();

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> financialTxs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBOE, "123272727",
                TransactionTypeCode.EmployerDdDebit,
                TransactionStateCode.Created);

        for (FinancialTransaction finTxn : financialTxs) {
            finTxn.updateFinancialTransactionState(TransactionStateCode.Completed);
        }
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, "123272727", "123123", true, false);
        Application.commitUnitOfWork();

        assertTrue("Process Result", result.isSuccess());

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        Application.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                "BatchId01");
        financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Returned});

        ERRefundDTO refundDTO  = new ERRefundDTO(financialTxs.get(0).getId().toString(), new SpcfMoney("50.00"),
                                                 new DateDTO(CalendarUtils.convertToSpcfCalendar(txDate)),
                                                 SettlementTypeDTO.ACH);

        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        // Commit
        Application.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        /// vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1062", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:123272727 does not have an active bank account.", message.getMessage());

    }

    /**
     * Test invalid amount for NON-ACH
     */

    @Test
    public void invalidNonACHAmount() throws Exception{
        //Function call to add Employer DD Reject Refund Credit transaction
        ERFinancialTxRefundCoreDataLoader.addEmployerDDRejectRefundTransaction();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Returned});

        ERRefundDTO refundDTO  = new ERRefundDTO(financialTxs.get(0).getId().toString(), new SpcfMoney("00.00"),
                                                 new DateDTO(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone())),
                                                 SettlementTypeDTO.Wire);

        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);
        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "267", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "For non-ACH transactions, the amount must be a non-zero, positive number.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    /**
     * Test null amount for NON-ACH
     */

    @Test
    public void testNullNonACHAmount() throws Exception{

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -43);
        Date txDate = pastCalendar.getTime();

        //Function call to add Employer DD Reject Refund Credit transaction
        ERFinancialTxRefundCoreDataLoader.addEmployerDDRejectRefundTransaction();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Returned});

        ERRefundDTO refundDTO  = new ERRefundDTO(financialTxs.get(0).getId().toString(), null,
                                                 new DateDTO(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone())),
                                                 SettlementTypeDTO.Cash);

        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        // Commit
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);
        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "267", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "For non-ACH transactions, the amount must be a non-zero, positive number.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    /**
     * Test invalid settlement date for NON-ACH
     */

    @Test
    public void testInvalidSettlementDateNonACH() throws Exception {
        //Function call to add Employer DD Reject Refund Credit transaction
        ERFinancialTxRefundCoreDataLoader.addEmployerDDRejectRefundTransaction();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Returned});

        ERRefundDTO refundDTO  = new ERRefundDTO(financialTxs.get(0).getId().toString(), new SpcfMoney("50.00"),
                                                 new DateDTO(SpcfCalendar.createInstance(2007, 7, 1, SpcfTimeZone.getLocalTimeZone())),
                                                 SettlementTypeDTO.Wire);

        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        // Commit
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "271", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Cannot record a transaction of Settlement Type Wire and date 2007/07/01 00:00:00.0, which is more than 45 days in the past.";
        assertEquals("Error Message", messageText, message.getMessage());


        refundDTO  = new ERRefundDTO(financialTxs.get(0).getId().toString(), new SpcfMoney("50.00"),
                                                 new DateDTO(SpcfCalendar.createInstance(2007, 10, 04, SpcfTimeZone.getLocalTimeZone())),
                                                 SettlementTypeDTO.Cash);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        // Commit
        PayrollServices.commitUnitOfWork();
        System.out.println(processResult);
        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "266", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Cannot record a transaction of Settlement Type Cash with the future date 2007/10/04 00:00:00.0.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    /**
     * Test refund process with valid data for NON-ACH
     */

    @Test
    public void testRefundNonACH() throws Exception {

        //Function call to add Employer DD Reject Refund Credit transaction
        ERFinancialTxRefundCoreDataLoader.addEmployerDDRejectRefundTransaction();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Returned});

        ERRefundDTO refundDTO  = new ERRefundDTO(financialTxs.get(0).getId().toString(), new SpcfMoney("50.00"),
                                                 new DateDTO(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone())),
                                                 SettlementTypeDTO.Wire);

        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
        // Commit
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);
        assertTrue("Associate Financial Transactions: ", financialTxs.get(0).getAssociatedTransactionsCollection().size() == 1);
        Iterator<FinancialTransaction> iterator = financialTxs.get(0).getAssociatedTransactionsCollection().iterator();
        FinancialTransaction refundTransaction = iterator.next();
        assertTrue("Refund Transaction State:",
                refundTransaction.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Completed));
        assertTrue("Refund Transaction Type:",
                refundTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerDdReturnedRefundCredit));
        assertTrue("Refund Transaction Amount:",refundTransaction.getFinancialTransactionAmount().equals(new SpcfMoney("50.00")) );
        assertTrue("Refund Settlement Date:", refundTransaction.getSettlementDate().equals(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone())));
        // verify the SKU type
        assertEquals("SKU:", financialTxs.get(0).getSku(), refundTransaction.getSku());

        // verify the SKU quantity
        assertTrue("SKU:", financialTxs.get(0).getSkuQuantity() == refundTransaction.getSkuQuantity());
    }

    /**
     * Test to verify PSRV000854
     */
    @Test
    public void testFeeRefund_ACH() {

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));

        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT,
                "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess("submitPayroll", processResult);

        // create a return for Fee Debit
        ACHReturnsDataLoader dataLoader = new ACHReturnsDataLoader();
        // offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(4);
        Company company = Company.findCompany("123272727",
                    SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerFeeDebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        DomainEntitySet<TransactionReturn> returnList = dataLoader.persistTransactionReturns(c1FinTxns, "R01",
                "R01 Description");
        Application.commitUnitOfWork();

        Assert.assertEquals("Number of C1 EmployerFeeDebit EX txns", 1, c1FinTxns.size());
        Assert.assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            currRet = Application.findById(TransactionReturn.class, currRet.getId());
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        // offload and complete the fee redebit
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20071010");
        PayrollServices.commitUnitOfWork();

        // create refund fee transaction
        PayrollServices.beginUnitOfWork();

        types = new Vector<TransactionTypeCode>();
        states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerFeeRedebit);
        states.add(TransactionStateCode.Completed);
        Application.refresh(payrollRun);
        c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        ERRefundDTO refundDTO = new ERRefundDTO();
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(c1FinTxns.get(0).getFinancialTransactionAmount());
        refundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
        refundDTO.setFinancialTxId(c1FinTxns.get(0).getId().toString());
        ProcessResult<FinancialTransaction> result = PayrollServices.financialTransactionManager.refundEmployerTransaction(
                            SourceSystemCode.QBDT, "123272727", refundDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        PayrollServices.beginUnitOfWork();
        FinancialTransaction feeRedebitTransaction = Application.findById(FinancialTransaction.class, c1FinTxns.get(0).getId());
        company = Application.findById(Company.class, company.getId());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("Number Of Refunded Transactions", 1, financialTxs.size());
        assertEquals("Refund Amount", feeRedebitTransaction.getFinancialTransactionAmount(), financialTxs.get(0).getFinancialTransactionAmount());
        assertTrue("Refund Settlement Date:", financialTxs.get(0).getSettlementDate().toLocal().equals(SpcfCalendar.createInstance(2007, 10, 11, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testFeeRefund_NonACH() {

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));

        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT,
                "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess("submitPayroll", processResult);

        // create a return for Fee Debit
        ACHReturnsDataLoader dataLoader = new ACHReturnsDataLoader();
        // offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(4);
        Company company = Company.findCompany("123272727",
                    SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerFeeDebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        DomainEntitySet<TransactionReturn> returnList = dataLoader.persistTransactionReturns(c1FinTxns, "R01",
                "R01 Description");
        Application.commitUnitOfWork();

        Assert.assertEquals("Number of C1 EmployerFeeDebit EX txns", 1, c1FinTxns.size());
        Assert.assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            currRet = Application.findById(TransactionReturn.class, currRet.getId());
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        // offload and complete the fee redebit
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20071010");
        PayrollServices.commitUnitOfWork();

        // create refund fee transaction
        PayrollServices.beginUnitOfWork();

        types = new Vector<TransactionTypeCode>();
        states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerFeeRedebit);
        states.add(TransactionStateCode.Completed);
        Application.refresh(payrollRun);
        c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        ERRefundDTO refundDTO = new ERRefundDTO();
        refundDTO.setSettlementType(SettlementTypeDTO.Wire);
        refundDTO.setFinancialTxAmt(c1FinTxns.get(0).getFinancialTransactionAmount());
        refundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
        refundDTO.setFinancialTxId(c1FinTxns.get(0).getId().toString());
        ProcessResult<FinancialTransaction> result = PayrollServices.financialTransactionManager.refundEmployerTransaction(
                            SourceSystemCode.QBDT, "123272727", refundDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        PayrollServices.beginUnitOfWork();
        FinancialTransaction feeRedebitTransaction = Application.findById(FinancialTransaction.class, c1FinTxns.get(0).getId());
        company = Application.findById(Company.class, company.getId());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("Number Of Refunded Transactions", 1, financialTxs.size());
        assertEquals("Refund Amount", feeRedebitTransaction.getFinancialTransactionAmount(), financialTxs.get(0).getFinancialTransactionAmount());
        assertTrue("Refund Settlement Date:", financialTxs.get(0).getSettlementDate().toLocal().equals(SpcfCalendar.createInstance(2007, 10, 10, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test refund process with valid data for ACH
     */

    @Test
    public void testRefundACH() throws Exception {
        //Function call to add Employer DD Reject Refund Credit transaction
        ERFinancialTxRefundCoreDataLoader.addEmployerDDRejectRefundTransaction();

        ERRefundDTO erRefundDTO = addEmployerReturnedRefundTransaction();
    }

    private ERRefundDTO addEmployerReturnedRefundTransaction(){
        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        //Refund Employer Transaction
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Returned});

        ERRefundDTO erRefundDTO = new ERRefundDTO(financialTxs.get(0).getId().toString(), new SpcfMoney("50.00"),
                new DateDTO(CalendarUtils.convertToSpcfCalendar(txDate)),
                SettlementTypeDTO.ACH);

        ProcessResult processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(
                SourceSystemCode.QBOE, "123272727", erRefundDTO);

        PayrollServices.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);
        assertTrue("Associate Financial Transactions: ", financialTxs.get(0).getAssociatedTransactionsCollection().size() == 1);
        Iterator<FinancialTransaction> iterator = financialTxs.get(0).getAssociatedTransactionsCollection().iterator();
        FinancialTransaction refundTransaction = iterator.next();
        assertTrue("Refund Transaction State:",
                refundTransaction.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created));
        assertTrue("Refund Transaction Type:",
                refundTransaction.getTransactionType().getTransactionTypeCd().equals(
                        TransactionTypeCode.EmployerDdReturnedRefundCredit));
        assertTrue("Refund Transaction Amount:",refundTransaction.getFinancialTransactionAmount().equals(financialTxs.get(0).getFinancialTransactionAmount()) );
        // verify the SKU type
        assertEquals("SKU:", financialTxs.get(0).getSku(), refundTransaction.getSku());

        // verify the SKU quantity
        assertTrue("SKU:", financialTxs.get(0).getSkuQuantity() == refundTransaction.getSkuQuantity());

        // verify transaction response
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionResponse> txnResponses = TransactionResponse.findTransactionResponses(refundTransaction);
        assertEquals("Number of transaction responses for the refunded transaction", 1, txnResponses.size());
        PayrollServices.commitUnitOfWork();

        return erRefundDTO;
    }


}
