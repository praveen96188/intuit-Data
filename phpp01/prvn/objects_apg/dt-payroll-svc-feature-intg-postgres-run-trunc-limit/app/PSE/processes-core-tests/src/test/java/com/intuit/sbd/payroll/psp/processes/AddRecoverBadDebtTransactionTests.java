/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/AddRecoverBadDebtTransactionTests.java#2 $
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
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddRecoverBadDebtTransactionDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * User: rkrishna
 * Date: Jan 4, 2008
 * Time: 11:12:06 AM
 * @see BadDebtTaxTests for tests recovering bad debt on tax companies.
 */
public class AddRecoverBadDebtTransactionTests {


    @Before
    public void runBeforeEachTest() {
        AddRecoverBadDebtTransactionDataLoader.loadBeforeTest();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
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
        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();

        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(null, "123272727", badDebtRecoverDTO);

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
        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, null, badDebtRecoverDTO);

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
        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "123272727", badDebtRecoverDTO);

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
        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setSourcePayrollRunId("BatchId01");
        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "1232727", badDebtRecoverDTO);
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
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddRecoverBadDebtTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();
        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setSourcePayrollRunId("BatchId03");
        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "123272727", badDebtRecoverDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "194", errorMessage.getMessageCode());
        assertEquals("Error message", "Payroll Run with DDTxBatchID BatchId03 does not exist for company QBOE:123272727.",
                errorMessage.getMessage());
    }

    /**
     * Test message 165 - Invalid Settlement Type
     */
    @Test
    public void testInvalidSettlementType() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddRecoverBadDebtTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertSuccess("submitPayroll", payrollProcess);

        Application.beginUnitOfWork();

        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setSourcePayrollRunId("BatchId01");
        badDebtRecoverDTO.setSettlementType(SettlementTypeDTO.ACH);
        badDebtRecoverDTO.setFinancialTxAmt(null);
        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "123272727", badDebtRecoverDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "281", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid transaction settlement type for recover bad debt request.",
                errorMessage.getMessage());
    }


    /**
     * Test message 5001 - Null Financial Transaction Amount
     */
    @Test
    public void testNullTxnAmount() {
        ACHReturnsDataLoader.loadData2DayERGenericRetAgentWritesOff();

        Application.beginUnitOfWork();

        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setSourcePayrollRunId("BatchTest05");
        badDebtRecoverDTO.setSettlementType(SettlementTypeDTO.Cash);
        badDebtRecoverDTO.setFinancialTxAmt(null);
        badDebtRecoverDTO.setOriginalTransactionId(getOrigFtId(SourceSystemCode.QBOE, "1234567", "BatchTest05", TransactionTypeCode.EmployerDdDebit));
        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "1234567", badDebtRecoverDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message", "For non-ACH transactions, the amount must be a non-zero, positive number.",
                errorMessage.getMessage());
        assertEquals("Error message code", "267", errorMessage.getMessageCode());
    }

    /**
     * Test message 283 - Negative Financial Transaction Amount
     */
    @Test
    public void testNegativeFinTxnAmount() {
        ACHReturnsDataLoader.loadData2DayERGenericRetAgentWritesOff();

        Application.beginUnitOfWork();

        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setSourcePayrollRunId("BatchTest05");
        badDebtRecoverDTO.setSettlementType(SettlementTypeDTO.Cash);
        badDebtRecoverDTO.setFinancialTxAmt(new SpcfMoney("-180.00"));
        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "1234567", badDebtRecoverDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "267", errorMessage.getMessageCode());

        assertEquals("Error message", "For non-ACH transactions, the amount must be a non-zero, positive number.",
                errorMessage.getMessage());
    }

    /**
     * Test message 271 - Settlement Date is Too Far in Past
     */
    @Test
    public void testSettlementDateTooFarInPast() {

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddRecoverBadDebtTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertSuccess("submitPayroll", payrollProcess);
        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        Application.commitUnitOfWork();

        AddRecoverBadDebtTransactionDataLoader.addWriteOffBadDebtTransaction();

        Application.beginUnitOfWork();

        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setSourcePayrollRunId("BatchId01");
        badDebtRecoverDTO.setSettlementType(SettlementTypeDTO.Cash);
        badDebtRecoverDTO.setFinancialTxAmt(AddRecoverBadDebtTransactionDataLoader.mFinTxnAmt);
        badDebtRecoverDTO.setOriginalTransactionId(getOrigFtId(SourceSystemCode.QBOE, "123272727", payrollRunDTO.getPayrollTXBatchId(), TransactionTypeCode.EmployerDdDebit));
        badDebtRecoverDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 7, 15,
                SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "123272727", badDebtRecoverDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "271", errorMessage.getMessageCode());

        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash and date " +
                DateDTO.convertToSpcfCalendar(badDebtRecoverDTO.getTxDate()).toString() +
                ", which is more than 45 days in the past.", errorMessage.getMessage());
    }

    /**
     * Test message 266 - Settlement Date is too Far In Future
     */
    @Test
    public void testSettlementDateTooFarInFuture() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddRecoverBadDebtTransactionDataLoader.psd1.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertSuccess("submitPayroll", payrollProcess);

        AddRecoverBadDebtTransactionDataLoader.addWriteOffBadDebtTransaction();

        Application.beginUnitOfWork();

        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setSourcePayrollRunId("BatchId01");
        badDebtRecoverDTO.setSettlementType(SettlementTypeDTO.Cash);
        badDebtRecoverDTO.setFinancialTxAmt(AddRecoverBadDebtTransactionDataLoader.mFinTxnAmt);
        badDebtRecoverDTO.setOriginalTransactionId(getOrigFtId(SourceSystemCode.QBOE, "123272727", payrollRunDTO.getPayrollTXBatchId(), TransactionTypeCode.EmployerDdDebit));
        badDebtRecoverDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 27,
                SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "123272727", badDebtRecoverDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "266", errorMessage.getMessageCode());
        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash with the future date " +
                DateDTO.convertToSpcfCalendar(badDebtRecoverDTO.getTxDate()).toString() + ".",
                errorMessage.getMessage());
    }

    /**
     * Test message 279 - Greater than outstanding Financial Transaction Amount
     */
    @Test
    public void testGreaterThanOutstandingFinTxnAmount() {
        ACHReturnsDataLoader.loadData2DayERGenericRetAgentWritesOff();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> fTs = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "1234567", TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Returned);
        assertEquals("Returned Dd Dbt", 1, fTs.size());
        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setSourcePayrollRunId("BatchTest05");
        badDebtRecoverDTO.setSettlementType(SettlementTypeDTO.Wire);
        badDebtRecoverDTO.setFinancialTxAmt(new SpcfMoney("200.00"));
        badDebtRecoverDTO.setOriginalTransactionId(fTs.get(0).getId().toString());
        badDebtRecoverDTO.setTxDate(new DateDTO("2007-09-17"));
        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "1234567", badDebtRecoverDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "279", errorMessage.getMessageCode());

        assertEquals("Error message", "Txn Amount for Bad Debt Recovery must not exceed the balance of the Bad Debt account for this payroll.",
                errorMessage.getMessage());
    }
    /**
     * Test - BadDebt WriteOff Process
     */
    @Test
    public void testBadDebtRecoveryProcess() {
        ProcessResult processResult = addRecoverBadDebtTransaction();

        FinancialTransaction recoveryFT = (FinancialTransaction) processResult.getResult();

        assertEquals("Payroll Run ", "BatchTest05", recoveryFT.getPayrollRun().getSourcePayRunId());

        assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                     recoveryFT.getCurrentTransactionState().getTransactionStateCd());

        assertEquals("Financial Transaction Type ", TransactionTypeCode.BadDebtRecovery,
                     recoveryFT.getTransactionType().getTransactionTypeCd());

        assertEquals("Financial Transaction Amount ", new SpcfMoney("180.00"),
                     recoveryFT.getFinancialTransactionAmount());

    }

    @Test
    public void testBadDebtRecoveryForDD4V() {
        String sourceCompanyId = "123272727";
        // 1. company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        //2. Set up bill payments with 2 Payees
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        BillPaymentDTO billPaymentDTO2 = GenerateData.generateBillPayment("Payee2", new DateDTO("2007-09-10"),1);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        billPaymentDTOs.add(billPaymentDTO2);
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        Collection<PayrollRun> billPaymentResults = submitResult.getResult() ;
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.BillPaymentReceived);
        assertEquals("BillPaymentReceived event count", 1, events.size());
        PayrollServices.commitUnitOfWork();
        assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

        // 3. offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // 4. Return the offloaded transactions as Non Sufficient Funds
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun1 = billPaymentResults.toArray(new PayrollRun[billPaymentResults.size()])[0];
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRun1.getSourcePayRunId());
        DomainEntitySet<FinancialTransaction> offLoadedTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[] {TransactionStateCode.Executed});
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070912000000");
        PayrollServices.commitUnitOfWork();
        DataLoadServices.returnTxns(offLoadedTransactions, "R01","NSF Return");

        //5. Write off the remaining balance on the payroll
        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        ProcessResult abdWOProc = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBDT, sourceCompanyId, payrollRun.getSourcePayRunId());
        PayrollServices.commitUnitOfWork();

        // 6. Offload the writeoffs, so the amounts get registered in the ledger
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        PayrollServices.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // 7. Make sure the correct amounts are returned  in  the call to  getUnrecoveredDirectDepositAmount
        HashMap<FinancialTransaction, SpcfMoney> unrecoveredDirectDepositAmount = payrollRun.getUnrecoveredDirectDepositAmount();
        assertTrue(unrecoveredDirectDepositAmount!=null && unrecoveredDirectDepositAmount.size()==2);
        Set<FinancialTransaction> unrecoveredDirectDepositAmountSet = unrecoveredDirectDepositAmount.keySet() ;
        Iterator<FinancialTransaction> unrecoveredDirectDepositAmountSetItr = unrecoveredDirectDepositAmountSet.iterator();
        while(unrecoveredDirectDepositAmountSetItr.hasNext()) {
            FinancialTransaction ft = unrecoveredDirectDepositAmountSetItr.next() ;
            SpcfMoney amt = unrecoveredDirectDepositAmount.get(ft)    ;
            assertTrue(amt.compareTo(SpcfMoney.ZERO) > 0 );
        }
    }

    private ProcessResult addRecoverBadDebtTransaction(){
        ACHReturnsDataLoader.loadData2DayERGenericRetAgentWritesOff();

        Application.beginUnitOfWork();

        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setSourcePayrollRunId("BatchTest05");
        badDebtRecoverDTO.setSettlementType(SettlementTypeDTO.Cash);
        badDebtRecoverDTO.setFinancialTxAmt(new SpcfMoney("180.00"));
        badDebtRecoverDTO.setOriginalTransactionId(getOrigFtId(SourceSystemCode.QBOE, "1234567", "BatchTest05", TransactionTypeCode.EmployerDdDebit));
        badDebtRecoverDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(SourceSystemCode.QBOE, "1234567", badDebtRecoverDTO);

        Application.commitUnitOfWork();

        assertSuccess("Bad debt recover process result", processResult);

        return processResult;
    }

    @Test
    public void recoverDD() {
        String amount = "180.00";
        ProcessResult<FinancialTransaction> prRecovery;
        prRecovery = recover(amount, TransactionTypeCode.EmployerDdDebit);

        PayrollServicesTest.assertSuccess("Recovery", prRecovery);

        FinancialTransaction ftRecovery = prRecovery.getResult();
        assertEquals("FT type", TransactionTypeCode.BadDebtRecovery, ftRecovery.getTransactionType().getTransactionTypeCd());
        assertEquals("FT state", TransactionStateCode.Completed, ftRecovery.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("FT amount", amount, ftRecovery.getFinancialTransactionAmount().toString());
    }

    @Test
    public void recoverFee() {
        String amount = "10.00";
        ProcessResult<FinancialTransaction> prRecovery;
        prRecovery = recover(amount, TransactionTypeCode.EmployerFeeDebit);

        PayrollServicesTest.assertSuccess("Recovery", prRecovery);

        FinancialTransaction ftRecovery = prRecovery.getResult();
        assertEquals("FT type", TransactionTypeCode.BadDebtRecoveryFee, ftRecovery.getTransactionType().getTransactionTypeCd());
        assertEquals("FT state", TransactionStateCode.Completed, ftRecovery.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("FT amount", amount, ftRecovery.getFinancialTransactionAmount().toString());
    }

    @Test
    public void recoverTax() {
        String amount = "0.16";
        ProcessResult<FinancialTransaction> prRecovery;
        prRecovery = recover(amount, TransactionTypeCode.ServiceSalesAndUseTax);

        PayrollServicesTest.assertSuccess("Recovery", prRecovery);

        FinancialTransaction ftRecovery = prRecovery.getResult();
        assertEquals("FT type", TransactionTypeCode.BadDebtRecoverySalesAndUseTax, ftRecovery.getTransactionType().getTransactionTypeCd());
        assertEquals("FT state", TransactionStateCode.Completed, ftRecovery.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("FT amount", amount, ftRecovery.getFinancialTransactionAmount().toString());
    }

    @Test
    public void recoverExcessFee() {
        String amount = "77.50";
        ProcessResult<FinancialTransaction> prRecovery;
        prRecovery = recover(amount, TransactionTypeCode.EmployerFeeDebit);

        PayrollServicesTest.assertSuccess("Recovery", prRecovery);

        FinancialTransaction ftRecovery = prRecovery.getResult();
        assertEquals("FT type", TransactionTypeCode.BadDebtRecoveryFee, ftRecovery.getTransactionType().getTransactionTypeCd());
        assertEquals("FT state", TransactionStateCode.Completed, ftRecovery.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("FT amount", amount, ftRecovery.getFinancialTransactionAmount().toString());
    }

    @Test
    public void recoverMissingFeeType() {
        String amount = "10.00";
        ProcessResult<FinancialTransaction> prRecovery;
        prRecovery = recover(amount, null); // null fee type

        assertTrue("Recovery process failed", ! prRecovery.isSuccess());
        assertEquals("Number of messages", 1, prRecovery.getMessages().size());
        Message msg = prRecovery.getMessages().get(0);
        assertEquals("Message code", "5002", msg.getMessageCode()); // required input missing or blank
    }

    private ProcessResult<FinancialTransaction> recover(String pAmount, TransactionTypeCode pWriteoffTypeCd) {

        PayrollRun payroll = AddRecoverBadDebtTransactionDataLoader.loadAddRecoverBadDebtDataWithFee();
        SourceSystemCode srcSystemCd = payroll.getCompany().getSourceSystemCd();
        String srcCompanyId = payroll.getCompany().getSourceCompanyId();
        String payrollRunId = payroll.getSourcePayRunId();

        // reload the payroll
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(srcCompanyId, srcSystemCd);
        payroll = PayrollRun.findPayrollRun(company, payrollRunId);

        BadDebtRecoverDTO dtoRecovery = new BadDebtRecoverDTO();
        dtoRecovery.setSourcePayrollRunId(payroll.getSourcePayRunId());
        if (pWriteoffTypeCd == null) {
            dtoRecovery.setOriginalTransactionId(null);
        } else {
            dtoRecovery.setOriginalTransactionId(getOrigFtId(company.getSourceSystemCd(),
                                                    company.getSourceCompanyId(),
                                                    payroll.getSourcePayRunId(),
                                                    pWriteoffTypeCd));
        }
        dtoRecovery.setFinancialTxAmt(new SpcfMoney(pAmount));
        dtoRecovery.setSettlementType(SettlementTypeDTO.Wire);
        dtoRecovery.setTxDate(new DateDTO(PSPDate.getPSPTime()));

        ProcessResult<FinancialTransaction> prRecovery;
        prRecovery = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(company.getSourceSystemCd(),
                                                                        company.getSourceCompanyId(), dtoRecovery);

        PayrollServices.commitUnitOfWork();

        if (prRecovery.isSuccess()) {
            FinancialTransaction ftRecovery = prRecovery.getResult();
            assertEquals("FT state", TransactionStateCode.Completed, ftRecovery.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("FT amount", Double.valueOf(pAmount), Double.valueOf(ftRecovery.getFinancialTransactionAmount().toString()));
        }

        return prRecovery;
    }



    public static String getOrigFtId(SourceSystemCode pSrcSystemCd, String pCompanyId, String pPayrollBatchId, TransactionTypeCode pOrigType) {
        Company company = Company.findCompany(pCompanyId, pSrcSystemCd);
        Assert.assertTrue("Company for original FT exists", company!=null);

        PayrollRun payroll = PayrollRun.findPayrollRun(company, pPayrollBatchId);
        Assert.assertTrue("PayrollRun for original FT exists", payroll!=null);

        DomainEntitySet<FinancialTransaction> found = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{pOrigType},
                null).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));

        Assert.assertTrue("Original FT exists", found.size()>0);

        return found.get(0).getId().toString();
    }
}
