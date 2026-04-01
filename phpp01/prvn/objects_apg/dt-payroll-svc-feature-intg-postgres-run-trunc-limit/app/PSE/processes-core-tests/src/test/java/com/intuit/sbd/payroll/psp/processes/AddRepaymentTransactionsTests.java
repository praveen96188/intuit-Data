/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/AddRepaymentTransactionsTests.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * User: rkrishna
 * Date: Dec 26, 2007
 * Time: 2:50:17 PM
 */
public class AddRepaymentTransactionsTests {
    public static PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();
    private static Company1Dataloader c1dl;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        loadDataHappyPath();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Test message 5002 - Settlement Type not specified
     */
    @Test
    public void testNullSettlementType() {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setAmount(new SpcfMoney("05.81"));
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5002", errorMessage.getMessageCode());
        assertEquals("Error message", "Required 'SettlementType' input is missing or blank",
                errorMessage.getMessage());
    }

    /**
     * Test message 5001 - Invalid Settlement Type
     */
    @Test
    public void testInvalidSettlementType() {

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setAmount(new SpcfMoney("05.81"));
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.ACH);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "SettlementType has invalid value", errorMessage.getMessage());
    }

    /**
     * Test message 271 - Initiation Date is Too Far in Past
     */
    @Test
    public void testInitiationDateTooFarInPast() {
        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Cash);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 7, 15,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(new SpcfMoney("1705.81"));

        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, c1dl.getCompany().getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "271", errorMessage.getMessageCode());

        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash and date " +
                DateDTO.convertToSpcfCalendar(redebitImpoundDTO.getInitiationDate()).toString() +
                ", which is more than 45 days in the past.", errorMessage.getMessage());
    }

    /**
     * Test message 266 - Initiation Date is too Far In Future
     */
    @Test
    public void testInitiationDateTooFarInFuture() {
        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Cash);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 17,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(new SpcfMoney("1705.81"));

        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, c1dl.getCompany().getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "266", errorMessage.getMessageCode());
        assertEquals("Error message", "Cannot record a transaction of Settlement Type Cash with the future date " +
                DateDTO.convertToSpcfCalendar(redebitImpoundDTO.getInitiationDate()).toString() + ".",
                errorMessage.getMessage());
    }

    @Test
    public void testInvalidAction() {

        //Offload all the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 1, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
        //Persist the Transction Return
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R02", "Non-NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        PayrollServices.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
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
        ProcessResult<DomainEntitySet<FinancialTransaction>> procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                redebitCollection);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        PayrollServices.commitUnitOfWork();

        assertSuccess("Redebit process result", procResult);
        assertEquals("Payroll run status is PendingRedebit", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), "BatchTest05");

        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Add Non-ACH Redebit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Cash);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(new SpcfMoney("1705.81"));

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, c1dl.getCompany().getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());
    }

    /**
     * Test - Return ER debit, create redebit, and then create wire with the exact amount to cover debt
     */
    @Test
    public void testRepaymentRecordPendingRedebitDoesNotCover() {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        FinancialTransaction originalTxn = null;
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
               new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(financialTxs.size() == 1);

        originalTxn = financialTxs.get(0);

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        redebitImpoundDTO.setAmount(originalTxn.getFinancialTransactionAmount());

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Persistence check for Employer Redebit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        DomainEntitySet<FinancialTransaction> createdRedebits = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        PayrollServices.commitUnitOfWork();

        //Ensure the wire was properly created
        assertEquals("Number of Completed Financial Txs", 1, financialTxs.size());
        FinancialTransaction erDDRedb = financialTxs.get(0);
        assertEquals("Payroll Run Id ", payrollRun.getSourcePayRunId(), erDDRedb.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("777.77"), erDDRedb.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Completed, erDDRedb.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.Wire, erDDRedb.getSettlementTypeCd());

        //Ensure the ACH redebit was NOT cancelled
        assertEquals("Number of ACH er dd redebit Financial Txs", 1, createdRedebits.size());
        FinancialTransaction erDDRedbACH = createdRedebits.get(0);
        assertEquals("Payroll Run Id ", payrollRun.getSourcePayRunId(), erDDRedbACH.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("777.77"), erDDRedbACH.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Created, erDDRedbACH.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.ACH, erDDRedbACH.getSettlementTypeCd());

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            //Pending redebits cover
            Assert.assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                    txnReturn.getReturnStatusCd());
        }

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Return ER debit, create reversals, and then create wire with less than the exact amount to cover debt
     */
    @Test
    public void testRepaymentPendingReversalsDoesNotCover(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 26, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
        //Persist the Transction Return
        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturn(c1dl.getCompany().getSourceCompanyId(),
                c1dl.getCompany().getSourceSystemCd(), "BatchTest05");
        assertEquals("Number of txn returns", 1, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        PayrollServices.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), c1dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        TransactionReverseDTO transactionReverseDTO = new TransactionReverseDTO();

        transactionReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        transactionReverseDTO.setDdTransactionIdList(null);
        transactionReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        transactionReverseDTO.setTxDate(null);
        transactionReverseDTO.setChargeFee(false);
        transactionReverseDTO.setIntuitInitiatedReversals(true);

        ProcessResult procResult=PayrollServices.payrollManager.reverseTransaction(c1dl.getCompany().getSourceSystemCd(),
                c1dl.getCompany().getSourceCompanyId(), transactionReverseDTO);

        PayrollServices.commitUnitOfWork();
        assertSuccess("Reversal process result", procResult);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), c1dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        assertEquals("Payroll run status is PendingReversals", PayrollStatus.PendingReversals, payrollRun.getPayrollRunStatus());

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
               new TransactionStateCode[]{TransactionStateCode.Returned});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(new SpcfMoney("150.00"));

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Persistence check for Employer Redebit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), c1dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        assertEquals("Payroll Run Status", PayrollStatus.PendingReversals, payrollRun.getPayrollRunStatus());
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        DomainEntitySet<FinancialTransaction> createdReversals = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        PayrollServices.commitUnitOfWork();

        //Ensure the wire was properly created
        assertEquals("Number of Completed Financial Txs", 1, financialTxs.size());
        FinancialTransaction erDDRedb = financialTxs.get(0);
        assertEquals("Payroll Run Id ", "BatchTest05", erDDRedb.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("150.00"), erDDRedb.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Completed, erDDRedb.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.Wire, erDDRedb.getSettlementTypeCd());

        //Ensure the ACH reversals were NOT cancelled
        assertEquals("Number of ACH ee dd redebit Financial Txs", 2, createdReversals.size());
        for (FinancialTransaction currEEDDReversal : createdReversals) {
            assertEquals("Payroll Run Id ", "BatchTest05", currEEDDReversal.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction State ", TransactionStateCode.Created, currEEDDReversal.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Settlement Type ", SettlementType.ACH, currEEDDReversal.getSettlementTypeCd());
        }

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            Assert.assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Open,
                    txnReturn.getReturnStatusCd());
        }

        PayrollServices.commitUnitOfWork();
    }

    private DomainEntitySet<TransactionReturn> persistTransactionReturn(String pCompanyID,
                                                                  SourceSystemCode pSourceSystem,
                                                                  String pPayrollRunId) {
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(
                pCompanyID, pSourceSystem);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pPayrollRunId);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        DomainEntitySet<FinancialTransaction> c1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : c1FinTxns) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R02");
            transactionReturn.setBankReturnDescription("This is a non-NSF description");
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
        PayrollServices.commitUnitOfWork();
        return returnList;
    }

    private static void loadDataHappyPath() {
        PSPDate.setPSPTime("20070904000000");
        persistCompany1();
    }

    private static void persistCompany1() {
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
    }
}
