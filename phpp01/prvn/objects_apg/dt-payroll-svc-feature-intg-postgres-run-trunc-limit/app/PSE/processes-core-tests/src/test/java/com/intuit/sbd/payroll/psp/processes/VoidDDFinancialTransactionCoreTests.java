package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.VoidDDFinancialTransactionCoreDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: rsakhamuri
 * Date: Jan 3, 2008
 * Time: 2:15:31 PM
 */
public class VoidDDFinancialTransactionCoreTests {

    @Before
    public void runBeforeEachTest() {
        VoidDDFinancialTransactionCoreDataLoader.loadBeforeTest();
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
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "InvalidCompanyId", "123123");
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
     *  Test error message 177 - Company is not active

     @Test public void testCompanyNotActive() {
     PayrollServices.beginUnitOfWork();
     PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
     PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
     PayrollServices.commitUnitOfWork();

     //Set company status to "Inactive"
     PayrollServices.beginUnitOfWork();
     ProcessResult<CompanyService> cancelProcessResult = PayrollServices.companyManager.deactivateService(
     SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
     PayrollServices.commitUnitOfWork();
     assertSuccess("deactivateService", cancelProcessResult);

     PayrollServices.beginUnitOfWork();
     ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(
     SourceSystemCode.QBOE, "123272727", "123123");
     PayrollServices.commitUnitOfWork();
     out.println(processResult);

     // validate error count
     assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

     // validate error code
     Message message = processResult.getMessages().get(0);
     assertEquals("Error Code:", "1101", message.getMessageCode());

     // Verify that the correct massage string has returned
     assertEquals("Error Message", "The operation SubmitPayroll is not allowed for company QBOE:123272727 in its current state.",
     message.getMessage());

     }*/

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
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(null, "123272727", "123123");
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
        processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, null, "123098");
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
     * Test for invalid financial transaction id
     */
    @Test
    public void testInvalidFinancialTxId() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", "2c915611-1732-d4c4-0117-32d53e810028");
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

    /**
     * Test for null financial transaction id
     */
    @Test
    public void testNullFinancialTxId() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", null);
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

    /**
     * Test for already voided transaction
     */
    @Test
    public void testInvalidTxVoidAlreadyVoided() {
        PayrollRunDTO payrollRunDTO = VoidDDFinancialTransactionCoreDataLoader.loadTestTxVoidData();

        // offload the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071003000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        Date txDate = new Date(PSPDate.getPSPTime().toLocal().getTimeInMilliseconds());
        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", payrollRunDTO.getPayrollTXBatchId(),
                                                 SettlementTypeDTO.Wire, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> feeResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess("fee result", feeResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[]{TransactionStateCode.Completed});        
        ProcessResult voidResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        assertSuccess("void result", voidResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action FinancialTransactionVoidTx not valid for Financial Transaction with FinTxGseq " + financialTxs.get(0).getId().toString() + ", which has a tx type of " + TransactionTypeCode.EmployerFeeDebit + " and a tx status of " + TransactionStateCode.Voided + ".";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test for voiding a transaction with transaction state not in executed or completed
     */
    @Test
    public void testInvalidTxVoid() {
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
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action FinancialTransactionVoidTx not valid for Financial Transaction with FinTxGseq " + financialTxs.get(0).getId().toString() + ", which has a tx type of " + TransactionTypeCode.EmployerDdDebit + " and a tx status of " + TransactionStateCode.Created + ".";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test for EE Credit transaction void with transaction state not in Completed or Executed
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
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action FinancialTransactionVoidTx not valid for Financial Transaction with FinTxGseq " + financialTxs.get(0).getId().toString() + ", which has a tx type of " + TransactionTypeCode.EmployeeDdCredit + " and a tx status of " + TransactionStateCode.Created + ".";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test for Voiding a Completed Financial transaction
     */
    @Test
    public void testTxVoid() {
        DomainEntitySet<FinancialTransaction> financialTxs = VoidDDFinancialTransactionCoreDataLoader.loadTestTxVoidDataACH();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        PayrollServices.beginUnitOfWork();
        VoidDDFinancialTransactionCoreDataLoader.payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, VoidDDFinancialTransactionCoreDataLoader.payrollRun.getId());
        DomainEntitySet<FinancialTransaction> financialTxsAfter =
                VoidDDFinancialTransactionCoreDataLoader.payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[]{TransactionStateCode.Voided});


        assertEquals("Financial Transaction Ids:", financialTxs.get(0).getId(), financialTxsAfter.get(0).getId());
        
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test for Voiding a Non-ACH Redebit
     */
    @Test
    public void testVoidNonACHRedebit() {
        DomainEntitySet<FinancialTransaction> financialTxs = VoidDDFinancialTransactionCoreDataLoader.loadTestTxVoidNonACHRedebit();

        // Void the Redebit Transaction
        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        // Verify that PayrollRunStatus has been updated to DebitReturned
        PayrollServices.beginUnitOfWork();
        VoidDDFinancialTransactionCoreDataLoader.payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, VoidDDFinancialTransactionCoreDataLoader.payrollRun.getId());

        assertEquals("Payroll Status", PayrollStatus.DebitReturned, VoidDDFinancialTransactionCoreDataLoader.payrollRun.getPayrollRunStatus());
        SpcfMoney ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                VoidDDFinancialTransactionCoreDataLoader.payrollRun.getSourcePayRunId(), VoidDDFinancialTransactionCoreDataLoader.payrollRun.getCompany());
        assertTrue("Ledger Balance:", ledgerBalance.compareTo(SpcfDecimal.createInstance(0.00)) < 0);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test for Voiding a Non-ACH Redebit with a positive ledger balance on ERReturnReceivable
     */
    @Test
    public void testVoidNonACHRedebitNoLedgerBalance() {
        DomainEntitySet<FinancialTransaction> financialTxs = VoidDDFinancialTransactionCoreDataLoader.loadTestTxVoidNonACHRedebitBalance(true);

        // Void the  Redebit Transaction
        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        SpcfMoney ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                VoidDDFinancialTransactionCoreDataLoader.payrollRun.getSourcePayRunId(), VoidDDFinancialTransactionCoreDataLoader.payrollRun.getCompany());
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        // Verify that PayrollRunStatus has been updated to DebitReturned
        PayrollServices.beginUnitOfWork();
        VoidDDFinancialTransactionCoreDataLoader.payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, VoidDDFinancialTransactionCoreDataLoader.payrollRun.getId());

        assertEquals("Payroll Status", PayrollStatus.Complete, VoidDDFinancialTransactionCoreDataLoader.payrollRun.getPayrollRunStatus());
        ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                VoidDDFinancialTransactionCoreDataLoader.payrollRun.getSourcePayRunId(), VoidDDFinancialTransactionCoreDataLoader.payrollRun.getCompany());
        assertTrue("Ledger Balance:", ledgerBalance.compareTo(SpcfDecimal.createInstance(0.00)) >= 0);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test for Voiding a Non-ACH Redebit with a pending redebit
     */
    @Test
    public void testVoidNonACHRedebitPendingRedebit() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R01", "NSF Ret");

        //Wire the payroll amount

        Application.beginUnitOfWork();
        // Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Returned);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        PSPDate.setPSPTime("20071011000000");
        Collection<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO payrollRedebitImpoundDTO = new RedebitImpoundDTO(c1FinTxns.get(0).getId().toString(), new SpcfMoney("777.77"), new DateDTO("2007-10-09"), SettlementTypeDTO.Wire);
        redebitImpoundDTOs.add(payrollRedebitImpoundDTO);

        ProcessResult<DomainEntitySet<FinancialTransaction>> procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(SourceSystemCode.QBDT, "8574536", redebitImpoundDTOs);
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        //Void the wire
        FinancialTransaction finTxn = procResult.getResult().get(0);
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBDT, "8574536", finTxn.getId().toString());
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        //Test to ensure payroll status is PendingRedebit
        Application.refresh(finTxn);
        assertEquals("Payroll Status", PayrollStatus.PendingRedebit, finTxn.getPayrollRun().getPayrollRunStatus());
        Application.commitUnitOfWork();


    }

    /**
     * Test for Voiding a Non-ACH Redebit with a pending redebit
     */
    @Test
    public void testVoidNonACHRedebitRedebitOffloaded() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R01", "NSF Ret");

        //Wire the payroll amount

        Application.beginUnitOfWork();
        // Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Returned);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        //Offload the redebit
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        Collection<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO payrollRedebitImpoundDTO = new RedebitImpoundDTO(c1FinTxns.get(0).getId().toString(), new SpcfMoney("777.77"), new DateDTO("2007-10-09"), SettlementTypeDTO.Wire);
        redebitImpoundDTOs.add(payrollRedebitImpoundDTO);

        ProcessResult<DomainEntitySet<FinancialTransaction>> procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(SourceSystemCode.QBDT, "8574536", redebitImpoundDTOs);
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();

        //Void the wire
        FinancialTransaction finTxn = procResult.getResult().get(0);
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBDT, "8574536", finTxn.getId().toString());
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        //Test to ensure payroll status is RedebitOffloaded
        Application.refresh(finTxn);
        assertEquals("Payroll Status", PayrollStatus.RedebitOffloaded, finTxn.getPayrollRun().getPayrollRunStatus());
        Application.commitUnitOfWork();


    }

    /**
     * Test for Voiding a Non ACH settlement type Financial transaction
     */
    @Test
    public void testTxVoidNonACH() {
        DomainEntitySet<FinancialTransaction> financialTxs = VoidDDFinancialTransactionCoreDataLoader.loadTestTxDataNonACH();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", financialTxs.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1051", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action FinancialTransactionVoidTx not valid for Financial Transaction with FinTxGseq " + financialTxs.get(0).getId().toString() + ", which has a tx type of " + TransactionTypeCode.EmployerDdDebit + " and a tx status of " + TransactionStateCode.Completed + ".";
        assertEquals("Error Message", messageText, message.getMessage());

    }


    /**
     * Test for Voiding a Non ACH settlement type Financial transaction
     */
    @Test
    public void testVoidNonACHEmployeeDdReversal() {
        // create, submit, offload the ER parts of a payroll
        PayrollRunDTO dtoPayroll = VoidDDFinancialTransactionCoreDataLoader.loadTestTxVoidData();

        // advance the PSPTime and offload the EE credits
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(5);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create a non-ACH EmployeeDdReversalDebit
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        SpcfDecimal ledgerBeforeReversal = LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.EEReturnLiablility);

        DomainEntitySet<FinancialTransaction> eeCredits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "123272727", TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        List<String> idList = new ArrayList<String>();
        idList.add(eeCredits.get(0).getPaycheckSplit().getSourceDdTxnId());

        TransactionReverseDTO dtoReverse = new TransactionReverseDTO();
        dtoReverse.setChargeFee(false);
        dtoReverse.setDdTransactionIdList(idList);
        dtoReverse.setIntuitInitiatedReversals(false);
        dtoReverse.setSourcePayrollRunId(dtoPayroll.getPayrollTXBatchId());
        dtoReverse.setTransmissionId(null);
        dtoReverse.setTxDate(CalendarUtils.convertToCalendar(PSPDate.getPSPTime()));
        dtoReverse.setTxSettlementTypeCd(SettlementTypeDTO.Wire); // not ACH

        ProcessResult prReverse = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, "123272727", dtoReverse);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("reverseTransaction()", prReverse);

        // void it
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        SpcfDecimal ledgerAfterReversal = LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.EEReturnLiablility);

        DomainEntitySet<FinancialTransaction> eeReversals = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "123272727", TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Completed);
        ProcessResult<FinancialTransaction> prVoid = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", eeReversals.get(0).getId().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfDecimal ledgerAfterVoid = LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.EEReturnLiablility);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("voidTransaction()", prVoid);

        assertTrue(ledgerAfterReversal.compareTo(ledgerBeforeReversal) > 0);
        assertEquals("EEReturnLiablility balance after voiding EmployeeDdReversalDebit", ledgerBeforeReversal, ledgerAfterVoid); // PSRV000584 said the void didn't reduce the EEReturnLiablility balance
    }

    /**
     * Test case to same returns(R01) for two different payrolls and create the Non-ACH redebits for both the payrolls
     * and void Non-ACH redebit transaction for a single payroll.
     */
    @Test
    public void testVoidNonACHRedebitForMultiplePayrolls() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        Collection<PayrollRunDTO> payrollRunDTOs = psdl.loadMultiplePayrollsForCompany123272727();
        PayrollServices.commitUnitOfWork();

        for (PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<PayrollRun> processResult =
                    PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
            PayrollServices.commitUnitOfWork();

            // Ensure processing was succsessful
            assertSuccess("submitPayroll", processResult);
        }
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071004000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Return the employer transaction for Payroll Run : BatchId01
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R01",
                "This is an NSF description");
        Application.commitUnitOfWork();

        Assert.assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        Assert.assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        // Return the employer transaction for Payroll Run : BatchId02
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "BatchId02");
        financialTxs =
                payrollRun2.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R01",
                "This is an NSF description");
        Application.commitUnitOfWork();

        Assert.assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        Assert.assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        //Add Non-ACH Redebit Transaction for PayrollRun BatchId01
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(financialTxs.get(0).getFinancialTransactionAmount());

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Add Non-ACH Redebit Transaction for PayrollRun BatchId02
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchId02");

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(financialTxs.get(0).getFinancialTransactionAmount());

        collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        // verify onhold with code AchRejectR1R9 is expired
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        Expression<OnHoldReason> query =
                new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNotNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        DomainEntitySet<OnHoldReason> onHoldReasonList = Application.find(OnHoldReason.class, query);

        PayrollServices.commitUnitOfWork();
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        // void EmployerDdRedebit transaction for PayrollRun BatchId01
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> erDDRedebit = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        ProcessResult<FinancialTransaction> prVoid = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", erDDRedebit.get(0).getId().toString());

        PayrollServices.commitUnitOfWork();
        assertSuccess("voidTransaction()", prVoid);

        // verify onhold with code AchRejectR1R9 is created
        PayrollServices.beginUnitOfWork();
        erDDRedebit = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "123272727", TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Voided);

        company = Application.refresh(company);
        query = new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                               .And(OnHoldReason.ExpirationDate().isNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        onHoldReasonList = Application.find(OnHoldReason.class, query);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Voided Transactions", 1, erDDRedebit.size());
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());
    }

    /**
     * Test case to create different returns(R01 & R02) for two different payrolls and create the Non-ACH redebits for
     * both the payrolls and void Non-ACH redebit transaction for a single payroll.
     */
    @Test
    public void testVoidNonACHRedebitForMultiplePayrollsWithDifferentTransactionReturns() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        Collection<PayrollRunDTO> payrollRunDTOs = psdl.loadMultiplePayrollsForCompany123272727();
        PayrollServices.commitUnitOfWork();

        for (PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<PayrollRun> processResult =
                    PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
            PayrollServices.commitUnitOfWork();

            // Ensure processing was succsessful
            assertSuccess("submitPayroll", processResult);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071004000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Return the employer transaction for Payroll Run : BatchId01
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R02",
                "This is an NSF description");
        Application.commitUnitOfWork();

        Assert.assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        Assert.assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        //Add Non-ACH Redebit Transaction for PayrollRun BatchId01
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(financialTxs.get(0).getFinancialTransactionAmount());

        Collection<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        // verify onhold with code AchRejectOther is expired
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        Expression<OnHoldReason> query =
                new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNotNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        DomainEntitySet<OnHoldReason> onHoldReasonList = Application.find(OnHoldReason.class, query);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectOther, onHoldReasonList.get(0).getOnHoldReasonCd());

        // Return the employer transaction for Payroll Run : BatchId02
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "BatchId02");
        financialTxs =
                payrollRun2.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R01",
                "This is an NSF description");
        Application.commitUnitOfWork();

        Assert.assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        Assert.assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        //Add Non-ACH Redebit Transaction for PayrollRun BatchId02
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchId02");

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(financialTxs.get(0).getFinancialTransactionAmount());

        collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        // verify onhold with code AchRejectOther & AchRejectR1R9 is expired
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        query = new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                               .And(OnHoldReason.ExpirationDate().isNotNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        onHoldReasonList = Application.find(OnHoldReason.class, query);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of On hold reasons", 2, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectOther, onHoldReasonList.get(0).getOnHoldReasonCd());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(1).getOnHoldReasonCd());

        // void EmployerDdRedebit transaction for PayrollRun BatchId02
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        payrollRun = PayrollRun.findPayrollRun(company, "BatchId02");

        DomainEntitySet<FinancialTransaction> erDDRedebit = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        ProcessResult<FinancialTransaction> prVoid = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "123272727", erDDRedebit.get(0).getId().toString());

        PayrollServices.commitUnitOfWork();
        assertSuccess("voidTransaction()", prVoid);

        // verify onhold with code AchRejectR1R9 is created 
        PayrollServices.beginUnitOfWork();
        erDDRedebit = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "123272727", TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Voided);

        company = Application.refresh(company);
        query = new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                               .And(OnHoldReason.ExpirationDate().isNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        onHoldReasonList = Application.find(OnHoldReason.class, query);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Voided Transactions", 1, erDDRedebit.size());
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());
    }


    @Test
    public void testAddOnHoldReasonAndReplaceHoldsToOtherPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company1 = c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1DL.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(payRun);
        PayrollServices.commitUnitOfWork();

        //Offload all the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        // Return the employer debit using R02
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R02",
                "This is an NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        // verify onhold with code AchRejectOther is created
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        Expression<OnHoldReason> query =
                new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                               .And(OnHoldReason.ExpirationDate().isNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        DomainEntitySet<OnHoldReason> onHoldReasonList = Application.find(OnHoldReason.class, query);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectOther, onHoldReasonList.get(0).getOnHoldReasonCd());

        //Manually Remove onhold reasons
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectOther));
        PayrollServices.commitUnitOfWork();

        //Submit another payroll for Comapany : 1234567
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payRun2 = c1DL.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-10-10"));
        c1DL.persistPayrollRun(payRun2);
        PayrollServices.commitUnitOfWork();

        //Ensure payroll2 Transactions are not on Hold
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "BatchTest002");
        DomainEntitySet<FinancialTransaction> payroll2FinTxns =
                payrollRun2.getFinancialTransactions(
                        null,
                        new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Transactions", 4, payroll2FinTxns.size());
        for (FinancialTransaction finTxn : payroll2FinTxns) {
            assertEquals("OnHold ", false, finTxn.getOnHold());
        }

        //Add Non-ACH Redebit Transaction for PayrollRun BatchTest05
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(financialTxs.get(0).getFinancialTransactionAmount());

        Collection<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        // void EmployerDdRedebit transaction for PayrollRun BatchTest05
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> erDDRedebit = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        ProcessResult<FinancialTransaction> prVoid = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.QBOE, "1234567", erDDRedebit.get(0).getId().toString());

        PayrollServices.commitUnitOfWork();
        assertSuccess("voidTransaction()", prVoid);

        // verify onhold with code AchRejectOther is created
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        query = new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                               .And(OnHoldReason.ExpirationDate().isNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        onHoldReasonList = Application.find(OnHoldReason.class, query);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectOther, onHoldReasonList.get(0).getOnHoldReasonCd());

        //Ensure payroll2 Transactions are on Hold
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "1234567", SourceSystemCode.QBOE);
        payrollRun2 = PayrollRun.findPayrollRun(company, "BatchTest002");
        payroll2FinTxns =
                payrollRun2.getFinancialTransactions(
                        null,
                        new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Transactions", 4, payroll2FinTxns.size());
        for (FinancialTransaction finTxn : payroll2FinTxns) {
            assertEquals("OnHold ", true, finTxn.getOnHold());
        }

        //Remove onhold reasons
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.AchRejectOther));
        PayrollServices.commitUnitOfWork();

        // verify onhold with code AchRejectOther is expred
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        Collection<OnHoldReason> onHoldReasons = company.getCurrentOnHoldReasons();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of On hold reasons", 0, onHoldReasons.size());

        //Offload 2nd Payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(5);
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest002");

        financialTxs = payrollRun.getFinancialTransactions(
                null,
                new TransactionStateCode[]{TransactionStateCode.Executed});

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Executed Transactions for Payroll:BatchTest002", 4, financialTxs.size());

        //Recreate Non-ACH Redebit Transaction for PayrollRun BatchTest05
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(financialTxs.get(0).getFinancialTransactionAmount());

        collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        // Return the employer debit using R09
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "1234567", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest002");
        financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R09",
                "This is an NSF description");
        Application.commitUnitOfWork();

        Assert.assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        Assert.assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        // verify onhold with code AchRejectR1R9 is created
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        query = new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                               .And(OnHoldReason.ExpirationDate().isNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        onHoldReasonList = Application.find(OnHoldReason.class, query);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        //Offload the redebit and fee
        //PayrollServices.beginUnitOfWork();
        //PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone()));
        //PayrollServices.commitUnitOfWork();

        offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071020000000");
        Application.commitUnitOfWork();

        //?	Complete the redebit and fee transactions
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071020");
        PayrollServices.commitUnitOfWork();

        // verify onhold with code AchRejectR1R9 is expred
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        query = new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                               .And(OnHoldReason.ExpirationDate().isNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        onHoldReasonList = Application.find(OnHoldReason.class, query);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());
    }

    @Test
    public void testVoid_CloudDDBP401k() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment, ServiceCode.ThirdParty401k);        

        // submit DD payroll
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
        payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        SpcfCalendar feeDate = PSPDate.getPSPTime();
        feeDate.addDays(-2);
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(
                            company.getSourceSystemCd(),
                            company.getSourceCompanyId(),
                            payrollRun.getSourcePayRunId(),
                            SettlementTypeDTO.Wire,
                            new Date(feeDate.getTimeInMilliseconds()),
                            new SpcfMoney("5.00"),
                            OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> addFee = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PSP_PRAssert.assertSuccess("addFee", addFee);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit}, new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("fin txns", 1, financialTransactions.size());
        ProcessResult voidResult = PayrollServices.financialTransactionManager.voidTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), financialTransactions.get(0).getId().toString());
        PSP_PRAssert.assertSuccess("void", voidResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit}, new TransactionStateCode[]{TransactionStateCode.Voided});
        assertEquals("voided fin txns", 1, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();
    }

}
