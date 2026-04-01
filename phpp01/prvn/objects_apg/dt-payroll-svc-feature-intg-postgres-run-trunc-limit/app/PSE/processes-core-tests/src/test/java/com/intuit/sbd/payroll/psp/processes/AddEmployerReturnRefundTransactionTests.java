/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/AddEmployerReturnRefundTransactionTests.java#6 $
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
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddEmployerReturnRefundTransactionDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: rkrishna
 * Date: Dec 17, 2007
 * Time: 5:06:58 PM
 */
public class AddEmployerReturnRefundTransactionTests {

    @BeforeClass
    public static void beforeClass() {
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        AddEmployerReturnRefundTransactionDataLoader.loadBeforeTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * ************************************Null tests/incoming data verification***********************************
     */

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(null, "123272727", null);

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
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, null, null);

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

        RefundDTO refundDTO = new RefundDTO();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);
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
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, "1232727", refundDTO);

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
        PayrollRunDTO payrollRunDTO = AddEmployerReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId03");

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "194", errorMessage.getMessageCode());
        assertEquals("Error message", "Payroll Run with DDTxBatchID BatchId03 does not exist for company QBOE:123272727.",
                errorMessage.getMessage());
    }

    /**
     * Test message 186 - Company Bank Account Id is Not Active
     */
    @Test
    public void testInActiveCompanyBankAccountId() {
        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddEmployerReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        //Deactivate CBA
        Application.beginUnitOfWork();
        ProcessResult procResult = PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, "123272727", "123123", true, false);
        Application.commitUnitOfWork();

        assertSuccess(procResult);

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "1062", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:123272727 does not have an active bank account.",
                errorMessage.getMessage());
    }

    /**
     * Test message 5001 - Null Financial Transaction Amount
     */
    @Test
    public void testNullTxnAmount() {
        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddEmployerReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Other);
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());

        assertEquals("Error message", "FinancialTransactionAmount has invalid value", errorMessage.getMessage());
    }

    /**
     * Test message 283 - Negative Financial Transaction Amount
     */
    @Test
    public void testNegativeFinTxnAmount() {
        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddEmployerReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Other);
        refundDTO.setFinancialTxAmt(new SpcfMoney("-1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "283", errorMessage.getMessageCode());

        assertEquals("Error message", "The amount must be a non-zero, positive number.", errorMessage.getMessage());
    }

    /**
     * Test message 271 - Settlement Date is Too Far in Past
     */
    @Test
    public void testSettlementDateTooFarInPast() {

        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddEmployerReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Cash);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 7, 15, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "271", errorMessage.getMessageCode());

        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash and date " +
                DateDTO.convertToSpcfCalendar(refundDTO.getTxDate()).toString() + ", which is more than 45 days in the past.",
                errorMessage.getMessage());
    }

    /**
     * Test message 266 - Settlement Date is too Far In Future
     */
    @Test
    public void testSettlementDateTooFarInFuture() {
        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = AddEmployerReturnRefundTransactionDataLoader.psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(SettlementTypeDTO.Cash);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 25, SpcfTimeZone.getLocalTimeZone())));


        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "266", errorMessage.getMessageCode());
        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash with the future date " +
                DateDTO.convertToSpcfCalendar(refundDTO.getTxDate()).toString() + ".", errorMessage.getMessage());
    }

    /**
     * Test message 280 - Pending Transaction Already Exist
     */
    @Test
    public void testPendingTransactionAlreadyExists()  {
        //Offloads payroll and handles NSF; ER now has an auto-redebit
        ACHReturnsDataLoader.loadData2DayERNSFs();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Create a wire
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070908000000");

        DomainEntitySet<FinancialTransaction> returnedDdFTs = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Returned);
        Assert.assertEquals("Number of Returned EmployerDdDebit FTs", 1, returnedDdFTs.size());

        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("180.00"));
        redebitDTO.setOriginalFinancialTxId(returnedDdFTs.get(0).getId().toString());
        redebitDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 8, SpcfTimeZone.getLocalTimeZone())));
        redebitDTO.setSettlementType(SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, "1234567", collectionOfRedebitImpounds);

        Application.commitUnitOfWork();
        assertSuccess("Wire process was successful", processResult);

        //Create the return transfer
        Application.beginUnitOfWork();
        SpcfMoney maxRefund = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, "BatchId05", returnedDdFTs.get(0).getCompany(), null, true);

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(maxRefund);
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE,
                "1234567", refundDTO);
        Application.commitUnitOfWork();
        assertSuccess("addEmployerReturnRefundTransaction Process Result", processResult);

        //Try to create another
        Application.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE,
                "1234567", refundDTO);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "282", errorMessage.getMessageCode());
    }

    /**
     * Test - ERReturn Refund Process for ACH Settlement Type
     */
    @Test
    public void testERReturnRefundProcessForACHSettlementType()  {
        //Offloads payroll and handles NSF; ER now has an auto-redebit
        ACHReturnsDataLoader.loadData2DayERNSFs();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
                
        //Create a wire
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070908000000");

        DomainEntitySet<FinancialTransaction> returnedDdFTs = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Returned);
        Assert.assertEquals("Number of Returned EmployerDdDebit FTs", 1, returnedDdFTs.size());

        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("180.00"));
        redebitDTO.setOriginalFinancialTxId(returnedDdFTs.get(0).getId().toString());
        redebitDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 8, SpcfTimeZone.getLocalTimeZone())));
        redebitDTO.setSettlementType(SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, "1234567", collectionOfRedebitImpounds);
        
        Application.commitUnitOfWork();
        assertSuccess("Wire process was successful", processResult);

        //Create the return transfer
        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney("180.00"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE,
                "1234567", refundDTO);
        TransactionResponse transactionResponse = (TransactionResponse) processResult.getResult();
        Application.commitUnitOfWork();
        assertSuccess("addEmployerReturnRefundTransaction Process Result", processResult);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransactionState> finTxnStList = transactionResponse.getFinancialTransactionStates(
        );

        for (FinancialTransactionState financialTransactionState : finTxnStList) {
            FinancialTransaction finTxn = financialTransactionState.getFinancialTransaction();
            Assert.assertEquals("Financial Transaction Amount ", new SpcfMoney("180.00"), finTxn.getFinancialTransactionAmount());
            Assert.assertEquals("Payroll Run Id ", "BatchTest05", finTxn.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Created, finTxn.getCurrentTransactionState().getTransactionStateCd());
        }

        Application.commitUnitOfWork();
    }

    private PayrollRun testERReturnRefundProcessTaxSetup() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 5, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-05-20"));

        DataLoadServices.setPSPDate(2012, 5, 17);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 5, 21);
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("3000.00"));
        redebitDTO.setOriginalFinancialTxId(assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit)).getId().toString());
        redebitDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2012, 5, 18, SpcfTimeZone.getLocalTimeZone())));
        redebitDTO.setSettlementType(SettlementTypeDTO.Wire);
        assertSuccess(PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), Arrays.asList(redebitDTO)));
        Application.commitUnitOfWork();

        return payrollRun;
    }

    @Test
    public void testERReturnRefundProcessTaxACH() {
        PayrollRun payrollRun = testERReturnRefundProcessTaxSetup();
        Company company = payrollRun.getCompany();

        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setRefundTaxOnly(true);

        assertSuccess(PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), refundDTO));
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        FinancialTransaction ddRefundCredit = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxReturnedRefundCredit));
        assertEquals(SettlementType.ACH, ddRefundCredit.getSettlementTypeCd());
        assertEquals(new SpcfMoney("524.00"), ddRefundCredit.getFinancialTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testERReturnRefundProcessTaxNonACH() {
        PayrollRun payrollRun = testERReturnRefundProcessTaxSetup();
        Company company = payrollRun.getCompany();

        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        refundDTO.setSettlementType(SettlementTypeDTO.Wire);
        refundDTO.setTxDate(new DateDTO("2012-05-20"));
        refundDTO.setFinancialTxAmt(new SpcfMoney("5000.00"));
        refundDTO.setRefundTaxOnly(true);

        assertSuccess(PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), refundDTO));
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        FinancialTransaction ddRefundCredit = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxReturnedRefundCredit));
        assertEquals(new SpcfMoney("5000.00"), ddRefundCredit.getFinancialTransactionAmount());
        assertEquals(SettlementType.Wire, ddRefundCredit.getSettlementTypeCd());
        PayrollServices.rollbackUnitOfWork();


    }

    private PayrollRun testERReturnRefundProcessTaxDDSetup() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 5, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addEEBankAccounts(company);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-05-20"));

        DataLoadServices.setPSPDate(2012, 5, 17);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 5, 21);
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        List<RedebitImpoundDTO> dtoList = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("3000.00"));
        redebitDTO.setOriginalFinancialTxId(assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit)).getId().toString());
        redebitDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2012, 5, 18, SpcfTimeZone.getLocalTimeZone())));
        redebitDTO.setSettlementType(SettlementTypeDTO.Wire);
        dtoList.add(redebitDTO);
        redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("2000.00"));
        redebitDTO.setOriginalFinancialTxId(assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerDdDebit)).getId().toString());
        redebitDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2012, 5, 18, SpcfTimeZone.getLocalTimeZone())));
        redebitDTO.setSettlementType(SettlementTypeDTO.Wire);
        dtoList.add(redebitDTO);

        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactions(TransactionStateCode.Returned, TransactionTypeCode.EmployerFeeDebit)) {
            redebitDTO = new RedebitImpoundDTO();
            redebitDTO.setAmount(financialTransaction.getFinancialTransactionAmount());
            redebitDTO.setOriginalFinancialTxId(financialTransaction.getId().toString());
            redebitDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2012, 5, 18, SpcfTimeZone.getLocalTimeZone())));
            redebitDTO.setSettlementType(SettlementTypeDTO.Wire);
            dtoList.add(redebitDTO);
        }

        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactions(TransactionStateCode.Returned, TransactionTypeCode.ServiceSalesAndUseTax)) {
            redebitDTO = new RedebitImpoundDTO();
            redebitDTO.setAmount(financialTransaction.getFinancialTransactionAmount());
            redebitDTO.setOriginalFinancialTxId(financialTransaction.getId().toString());
            redebitDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2012, 5, 18, SpcfTimeZone.getLocalTimeZone())));
            redebitDTO.setSettlementType(SettlementTypeDTO.Wire);
            dtoList.add(redebitDTO);
        }

        assertSuccess(PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoList));
        Application.commitUnitOfWork();

        return payrollRun;
    }

    @Test
    public void testERReturnRefundProcessTaxDDACH() {
        PayrollRun payrollRun = testERReturnRefundProcessTaxDDSetup();
        Company company = payrollRun.getCompany();

        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);

        assertSuccess(PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), refundDTO));
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        FinancialTransaction ddRefundCredit = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerDdReturnedRefundCredit));
        assertEquals(new SpcfMoney("1998.00"), ddRefundCredit.getFinancialTransactionAmount());
        FinancialTransaction taxRefundCredit = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxReturnedRefundCredit));
        assertEquals(new SpcfMoney("524.00"), taxRefundCredit.getFinancialTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testERReturnRefundProcessTaxDDNonACH() {
        PayrollRun payrollRun = testERReturnRefundProcessTaxDDSetup();
        Company company = payrollRun.getCompany();

        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        refundDTO.setSettlementType(SettlementTypeDTO.CheckType);
        refundDTO.setTxDate(new DateDTO("2012-04-29"));
        refundDTO.setFinancialTxAmt(new SpcfMoney("4000.00"));

        RefundDTO taxRefundDTO = new RefundDTO();
        taxRefundDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        taxRefundDTO.setSettlementType(SettlementTypeDTO.CheckType);
        taxRefundDTO.setTxDate(new DateDTO("2012-04-29"));
        taxRefundDTO.setFinancialTxAmt(new SpcfMoney("6000.00"));
        taxRefundDTO.setRefundTaxOnly(true);

        assertSuccess(PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), refundDTO));
        assertSuccess(PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), taxRefundDTO));
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        FinancialTransaction ddRefundCredit = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerDdReturnedRefundCredit));
        assertEquals(new SpcfMoney("4000.00"), ddRefundCredit.getFinancialTransactionAmount());
        FinancialTransaction taxRefundCredit = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxReturnedRefundCredit));
        assertEquals(new SpcfMoney("6000.00"), taxRefundCredit.getFinancialTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Test - ERReturn Refund Process for Settlement Type otherthan ACH
     */
    @Test
    public void testERReturnRefundProcessForCashSettlementType() {
        AddEmployerReturnRefundTransactionDataLoader.submitPayroll();

        Application.beginUnitOfWork();
        RefundDTO refundDTO = build_refundDto(SettlementTypeDTO.Other);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(SourceSystemCode.QBOE, "123272727", refundDTO);

        FinancialTransaction financialTransaction = (FinancialTransaction) processResult.getResult();

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        assertEquals("Financial Txn Amount ", refundDTO.getFinancialTxAmt(),
                financialTransaction.getFinancialTransactionAmount());

        assertEquals("Payroll Run ", refundDTO.getSourcePayrollRunId(),
                financialTransaction.getPayrollRun().getSourcePayRunId());

        assertEquals("Settlement Type", SettlementType.Other,
                financialTransaction.getSettlementTypeCd());

        assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                financialTransaction.getCurrentTransactionState().getTransactionStateCd());
    }

    private RefundDTO build_refundDto(SettlementTypeDTO pSettlementType) {
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchId01");
        refundDTO.setSettlementType(pSettlementType);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));

        return refundDTO;
    }

    private DomainEntitySet<TransactionReturn> persistTransactionReturn() {
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> eeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRefundCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : eeFinancialTxs) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("This is an Employer DD Refund return transaction");
            transactionReturn.setBankReturnTraceNumber(112L);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                    SpcfTimeZone.getLocalTimeZone()));
            transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setCompany(financialTransaction.getCompany());
            returnList.add(Application.save(transactionReturn));
        }
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        return returnList;
    }

    /**
     * PSRV000569: Can not do Fee Transfer and Refund at the same time in the ER Receivable account
     * The refund process was failing when there was any pending transaction against the INTU bank account associated
     * with ER Return Receivables.  So, if you did a fee transfer right before the refund, the refund failed.
     * Now it looks at the ledger balance MINUS any pending transactions that would hit that ledger account.  It allows
     * a refund up to the remaining amount.
     */
    @Test
    public void bug569() {
        //ACHReturnsDataLoader.loadData2DayERNSFsOffloadRedebitAndReturnFee();
        ACHReturnsDataLoader.loadData2DayERGenericReturn();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        SpcfMoney balance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, "BatchTest05", company);
        System.out.println("ledger balance after payroll returned: " + balance);
        PayrollServices.commitUnitOfWork();

        // customer pays too much = payroll + $75 fee + $25 extra
        SpcfMoney excessPayment = new SpcfMoney("100.00");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> payrollFTs = FinancialTransaction.findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(), TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Returned);
        String debitID = payrollFTs.get(0).getId().toString();
        ArrayList<RedebitImpoundDTO> redebitDTOs = new ArrayList();
        SpcfDecimal redebitAmount = balance.negate().add(excessPayment);
        redebitDTOs.add(new RedebitImpoundDTO(debitID, new SpcfMoney(redebitAmount), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire));
        ProcessResult<DomainEntitySet<FinancialTransaction>> prRedebit = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), redebitDTOs);
        PayrollServices.commitUnitOfWork();
        assertSuccess("redebit more than amount due", prRedebit);

        // transfer the fee amount
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        FeeTransferDTO dtoTransfer = new FeeTransferDTO();
        dtoTransfer.setSourcePayrollRunId("BatchTest05");
        dtoTransfer.setFinancialTxAmt(new SpcfMoney("75.00"));
        dtoTransfer.setFeeTypeCode(OfferingServiceChargeType.DebitReturnFee);
        ProcessResult prTransfer = PayrollServices.financialTransactionManager.addFeeTransferTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoTransfer);
        PayrollServices.commitUnitOfWork();
        assertSuccess("fee transfer", prTransfer);

        FinancialTransaction ftTransfer = (FinancialTransaction) prTransfer.getResult();
        Assert.assertEquals("Financial Transaction Type", TransactionTypeCode.IntuitFeeTransfer, ftTransfer.getTransactionType().getTransactionTypeCd());
        Assert.assertEquals("Financial Transaction State", TransactionStateCode.Created, ftTransfer.getCurrentTransactionState().getTransactionStateCd());

        PayrollServices.beginUnitOfWork();
        System.out.println("ledger balance AFTER transfer: " +
                LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, "BatchTest05", company));
        PayrollServices.commitUnitOfWork();

        // refund whatever is left
        PayrollServices.beginUnitOfWork();
        RefundDTO dtoRefund = new RefundDTO();
        dtoRefund.setSourcePayrollRunId("BatchTest05");
        dtoRefund.setSettlementType(SettlementTypeDTO.ACH);
        // amount is ignored when settling by ACH
        dtoRefund.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));
        ProcessResult<FinancialTransaction> prRefund = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoRefund);
        PayrollServices.commitUnitOfWork();
        assertSuccess(prRefund);

        // make sure the transaction amount is right... it should be the difference between the over-payment and the fee-transfer
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> refunds = FinancialTransaction.findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(), TransactionTypeCode.EmployerDdReturnedRefundCredit, TransactionStateCode.Created);
        FinancialTransaction ftRefund = refunds.get(0);
        PayrollServices.commitUnitOfWork();
        assertNotNull("refund transaction exists", ftRefund);
        assertEquals("refund amount", excessPayment.subtract(ftTransfer.getFinancialTransactionAmount()), ftRefund.getFinancialTransactionAmount());
    }
}
