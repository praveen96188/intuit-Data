/*
 * $Id: //psp/dev/PSE/BatchJobs/test/com/intuit/sbd/payroll/psp/batchjobs/offload/TestOffloadACHTransactions.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.offload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.utils.ACHCompare;
import com.intuit.sbd.payroll.psp.batchjobs.utils.CompareResults;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReaderFactory;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.*;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.MoneyMovementControlUtil;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class tests the class that generates the NACHA file and updates the statuses of the transactions that ended up in the file
 *
 * @author Dawn Martens
 */
public class TestOffloadACHTransactions {
    private static Company1Dataloader c1dl;
    private static Company2Dataloader c2dl;
    private static Company3Dataloader c3dl;
    private static boolean createOffloadBatchesOnTheFly = false;
    private int achTaxOffloadOffset;


    @Before
    public void beforeEachTest() {
        DataLoadServices.reinitialize();
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        c2dl = new Company2Dataloader();
        c3dl = new Company3Dataloader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        loadDataHappyPath();
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011,1,1));

        PayrollServices.beginUnitOfWork();
        createOffloadBatchesOnTheFly = SystemParameter.findBooleanValue(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY, false);
        PayrollServices.rollbackUnitOfWork();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();

        // reset createOffloadBatchesOnTheFly
        DataLoadServices.updateSystemParameter(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY, Boolean.toString(createOffloadBatchesOnTheFly));

        DataLoadServices.updateSystemParameter(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, "false");
        DataLoadServices.updateSystemParameter(SystemParameter.Code.JPMC_SKIP_SIGNATURE_VERIFICATION, "false");

        // restore $0 fee price
        //todo:offerings fix this
//        BillingDataLoader.updatePrice("dddddddd-dddd-dddd-dddd-000000000211", BigDecimal.ZERO,
//                                      CalendarUtils.convertToCalendar(PSPDate.getPSPTime()));
    }

    @Test
    public void testValidate_nullOffloadGroup() {
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        SpcfCalendar runDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        try {
            offloader.validate(null, runDate);
            TestCase.fail("Did not catch expected RuntimeException NULL Offload Group");
        } catch (Throwable t) {
            assertEquals("RuntimeException message", "NULL Offload Group", t.getMessage());
        }
    }

    @Test
    public void testValidate_invalidOffloadGroup() {
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        SpcfCalendar runDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        try {
            offloader.validate("MADEUPOFFLOADGROUP", runDate);
            TestCase.fail("Did not catch expected RuntimeException Invalid offload group code: MADEUPOFFLOADGROUP");
        } catch (Throwable t) {
            assertEquals("RuntimeException message", "Invalid offload group code: MADEUPOFFLOADGROUP", t.getMessage());
        }
    }

    @Test
    public void testEmployerDDRedebitPayRunStatus() {
        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        addCompany2Payroll4();
        addCompany1Payroll3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone());
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        offloadDate = SpcfCalendar.createInstance(2007, 10, 11, SpcfTimeZone.getLocalTimeZone());
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071012000000");
        offloadDate = SpcfCalendar.createInstance(2007, 10, 12, SpcfTimeZone.getLocalTimeZone());

        returnCompany1ERDDDB_R02();
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC1_2 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest81");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest80");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC2.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.RedebitOffloaded, payRunC1_2.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("1108.00");
        SpcfMoney expectedTotalDebits = new SpcfMoney("1108.00");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1NormalFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<FinancialTransaction> c2FinTxnsOnly = new DomainEntitySet<FinancialTransaction>();
        //We're only interested in the latest payroll's txns, so get rid of those for the first offloaded payroll
        for (FinancialTransaction currFinTxn : c2FinTxns) {
            if (!currFinTxn.getPayrollRun().getSourcePayRunId().equals("BatchTest05")) {
                c2FinTxnsOnly.add(currFinTxn);
            }
        }

        assertEquals("Number of C1 EmployerDdRedebit EX txns", 1, c1FinTxns.size());
        assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c1NormalFinTxns.size());
        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2FinTxnsOnly.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/
        validateFileAndTraceNumbers(
                createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testERREDCCD_R02expected.ach"), NACHAFileType.CCD);
    }

    @Test
    public void testEmployerDDNSFPayRunStatus() {
        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        updateCompany1To2DayFunding();
        addCompany2Payroll4();
        addCompany1Payroll3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 10, 5, SpcfTimeZone.getLocalTimeZone());
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071012000000");
        offloadDate = SpcfCalendar.createInstance(2007, 10, 12, SpcfTimeZone.getLocalTimeZone());
        c1dl.returnERDDDB("BatchTest81", "R01");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC1_2 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest81");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest80");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC2.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.AutoRedebitOffloaded, payRunC1_2.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("1208.00");
        SpcfMoney expectedTotalDebits = new SpcfMoney("1208.00");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1NormalFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        //We're only interested in the latest payroll's txns, so get rid of those for the first offloaded payroll
        DomainEntitySet<FinancialTransaction> filteredC2FinTxns = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction currFinTxn : c2FinTxns) {
            if (!currFinTxn.getPayrollRun().getSourcePayRunId().equals("BatchTest05")) {
                filteredC2FinTxns.add(currFinTxn);
            }
        }
        assertEquals("Number of C1 EmployerDdRedebit EX txns", 1, c1FinTxns.size());
        assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c1NormalFinTxns.size());
        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, filteredC2FinTxns.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/
        validateFileAndTraceNumbers(
                createdFile.getFileName(),
                Application.findFileOnClassPath("offload\\expected\\testERREDCCD_expected.ach"), NACHAFileType.CCD);
    }

    @Test
    public void testOffloadPPD() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone());

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest05");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC2.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("1982.45");
        SpcfMoney expectedTotalDebits = new SpcfMoney("1982.45");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.PPD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployeeDdCredit EX txns", 2, c1FinTxns.size());
        assertEquals("Number of C2 EmployeeDdCredit EX txns", 2, c2FinTxns.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c2FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/
        validateFileAndTraceNumbers(createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testPPD_expected.ach"), NACHAFileType.PPD);

    }

    @Test
    public void testBooktxfr() {
        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070926000000");
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 26, SpcfTimeZone.getLocalTimeZone());
        c1dl.returnERDDDB("BatchTest05", "R01");
        updateCompany2To2DayFunding();
        PayrollRunDTO payrollRunDTO = c2dl.get2ndCompany2PR_DoesNotExceedLimits(new DateDTO("2007-09-26"));
        c2dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        OffloadBatch createdBatch = offloader2.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        SpcfMoney expectedTotalCredits = new SpcfMoney("957.77");
        SpcfMoney expectedTotalDebits = new SpcfMoney("957.77");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> bookTxfrTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.Intuit5DayReturnTransfer, TransactionStateCode.Executed);

        assertEquals("Number of C1 Intuit5DayReturnTransfer EX txns", 1, bookTxfrTxns.size());

        verifyMMTxnAndTraceNums(bookTxfrTxns, createdBatch);

        PayrollServices.commitUnitOfWork();

        validateFileAndTraceNumbers(
                createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testBookTxfr_expected.ach"), NACHAFileType.CCD);
    }

    @Test
    public void testBadDebtBookTxfr() {
        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload ee db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload er db and ee crs
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 11, 13, SpcfTimeZone.getLocalTimeZone());
        updateCompany2To2DayFunding();
        addCompany2Payroll3();
        updateCompany2BackTo5DayFunding();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071113000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071115000000");
        offloadDate = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());        
        c2dl.returnERDDDB("BatchTest10");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadDataBadDebitWriteOff();
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        OffloadBatch createdBatch = offloader2.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        SpcfMoney expectedTotalCredits = new SpcfMoney("9211.11");
        SpcfMoney expectedTotalDebits = new SpcfMoney("9211.11");

        System.out.println("num nacha files" + nachaFiles.size());

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();
        SourceSystem qboe = Application.findById(SourceSystem.class, SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> bookTxfrTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerWriteOff, TransactionStateCode.Executed);

        assertEquals("Number of C1 ERWO EX txns", 1, bookTxfrTxns.size());

        verifyMMTxnAndTraceNums(bookTxfrTxns, createdBatch);

        PayrollServices.commitUnitOfWork();

        validateFileAndTraceNumbers(
                createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testBadDebtBookTxfr_expected.ach"), NACHAFileType.CCD);
    }

    @Test
    public void testOffloadCCD() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone());

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest05");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC2.getPayrollRunStatus());
        SpcfMoney expectedTotalCredits = new SpcfMoney("1982.45");
        SpcfMoney expectedTotalDebits = new SpcfMoney("1982.45");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c1FinTxns.size());
        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2FinTxns.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c2FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testCCD_expected.ach"), NACHAFileType.CCD);

    }

    @Test
    public void testOffloadCCDWithEncryption() {
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, "true");
        SystemParameter.update(SystemParameter.Code.JPMC_SKIP_SIGNATURE_VERIFICATION, "true");
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone());

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest05");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC2.getPayrollRunStatus());
        SpcfMoney expectedTotalCredits = new SpcfMoney("1982.45");
        SpcfMoney expectedTotalDebits = new SpcfMoney("1982.45");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c1FinTxns.size());
        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2FinTxns.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c2FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testCCD_expected.ach"), NACHAFileType.CCD);

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testSeconfOffload() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone());

        // Create second offload
        PayrollServices.beginUnitOfWork();
        SpcfCalendar secondOffloadDate = SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone());
        secondOffloadDate.addHours(19);
        ProcessResult pr = PayrollServices.batchJobManager.scheduleSecondOffload(OffloadGroup.findOffloadGroup("STD"), secondOffloadDate);
        assert(pr.getMessages().size() == 1 && pr.getMessages().get(0).getMessageCode().equals("10010"));  // error is "Failed to create scheduled second offload in Flux scheduler ..."
        PayrollServices.commitUnitOfWork();

        /***********************Persistence testing*********************/
        OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup("STD");

        DomainEntitySet<OffloadBatch> offloadBatches =
                Application.find(OffloadBatch.class,
                        new Query<OffloadBatch>()
                              .Where(OffloadBatch.OffloadGroup().equalTo(offloadGroup)
                                     .And(OffloadBatch.OffloadDate().equalTo(offloadDate))
                                     .And(OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.InProcess)))
                              .OrderBy(OffloadBatch.CreatedDate()));
        assertEquals("In process offload batches for date " + offloadDate.toString(), 2, offloadBatches.size());

        SpcfUniqueId regularOffloadId = offloadBatches.find(OffloadBatch.<DomainEntity>CreatorId().equalTo("System")).get(0).getId();
        SpcfUniqueId secondOffloadId = offloadBatches.find(OffloadBatch.<DomainEntity>CreatorId().notEqualTo("System")).get(0).getId();

        // Regular offload
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals(regularOffloadId, createdBatch.getId());
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest05");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC2.getPayrollRunStatus());
        SpcfMoney expectedTotalCredits = new SpcfMoney("1982.45");
        SpcfMoney expectedTotalDebits = new SpcfMoney("1982.45");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c1FinTxns.size());
        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2FinTxns.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c2FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(), Application.findFileOnClassPath("offload/expected/testCCD_expected.ach"), NACHAFileType.CCD);

        // Add new payroll and verify it is associated with second offload batch
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        payrollRunDTO.setPayrollTXBatchId("Secondoffload01");

        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        /***********************Persistence testing*********************/
        PayrollServices.beginUnitOfWork();
        PayrollRun payRunSecondOffload = PayrollRun.findPayrollRun(c1dl.getCompany(), "Secondoffload01");
        DomainEntitySet<FinancialTransaction> ftsWithInitiationDateEqualToOffloadDate = payRunSecondOffload.getFinancialTransactionCollection().find(FinancialTransaction.MoneyMovementTransaction().InitiationDate().equalTo(offloadDate)
                                                                                                                                                                         .And(FinancialTransaction.FinancialTransactionAmount().equalTo(SpcfMoney.ZERO)));
        assertEquals(1, ftsWithInitiationDateEqualToOffloadDate.size());

        for (FinancialTransaction ft : ftsWithInitiationDateEqualToOffloadDate) {
            assertEquals(ft.getMoneyMovementTransaction().getOffloadBatch().getId(), secondOffloadId);
        }
        PayrollServices.commitUnitOfWork();

        // Execute second offload
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals(secondOffloadId, createdBatch.getId());
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        payRunSecondOffload = PayrollRun.findPayrollRun(c1dl.getCompany(), "Secondoffload01");
        nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunSecondOffload.getPayrollRunStatus());
        expectedTotalCredits = new SpcfMoney("180.00");
        expectedTotalDebits = new SpcfMoney("180.00");

        createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);    
    }

    @Test
    public void testOffloadCCDTerminatedCompanyStatus() {
        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Cancel payroll before we can terminate company
        TransactionCancelEEDTO c1TxnCancelDTO = new TransactionCancelEEDTO();
        c1TxnCancelDTO.setSourcePayrollRunId("BatchTest05");
        ProcessResult pr = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", c1TxnCancelDTO);

        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess("cancelEmployeeTransaction", pr);

        PSPDate.setPSPTime("20070925000000");
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone());
        pr = PayrollServices.companyManager.terminateService
                (SourceSystemCode.QBOE, "1234567", ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest05");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.Canceled, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC2.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("1802.45");
        SpcfMoney expectedTotalDebits = new SpcfMoney("1802.45");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDdDebit EX txns", 0, c1FinTxns.size());
        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2FinTxns.size());

        verifyMMTxnAndTraceNums(c2FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(), 
                Application.findFileOnClassPath("offload/expected/testCCDOneCompTermed_expected.ach"),
                NACHAFileType.CCD);

    }

    @Test
    public void testOffloadThenReversal() {
        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload ee cr
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        loadDataReversal();
        PSPDate.setPSPTime("20071011000000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 10, 11, SpcfTimeZone.getLocalTimeZone());

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest05");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC2.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("180.00");
        SpcfMoney expectedTotalDebits = new SpcfMoney("180.00");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.PPD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployeeDdReversalDebit EX txns", 2, c1FinTxns.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(),
                                    Application.findFileOnClassPath("offload/expected/testReversal_expected.ach"),
                                    NACHAFileType.PPD);
    }

    /**
     * Offloads db and credits, adds reversals (pending), then offloads again
     */
    @Test
    public void testOffloadThenReversalsPending() {
        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload ee cr
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Return the debit
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071008000000");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        loadDataReversalsPending();
        PSPDate.setPSPTime("20071009000000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone());

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest05");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 2, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.ReversalsOffloaded, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC2.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("180.00");
        SpcfMoney expectedTotalDebits = new SpcfMoney("180.00");

        NACHAFile createdFile = nachaFiles.get(1);
        validateNACHAFileFinalized(createdFile, NACHAFileType.PPD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployeeDdReversalDebit EX txns", 2, c1FinTxns.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(),
                                    Application.findFileOnClassPath("offload/expected/testReversalsPending_expected.ach"),
                                    NACHAFileType.PPD);
    }

    @Test
    public void testTwoDayFunding() {
        //Offload er db and ee crs
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071113000000");
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 11, 13, SpcfTimeZone.getLocalTimeZone());
        updateCompany2To2DayFunding();
        addCompany2Payroll3();
        updateCompany2BackTo5DayFunding();
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRun = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest10");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        //Check payroll run status and number of nacha files
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRun.getPayrollRunStatus());
        assertEquals("Number of files:", 2, nachaFiles.size());

        SpcfMoney expectedTotalCredits = new SpcfMoney("9111.11");
        SpcfMoney expectedTotalDebits = new SpcfMoney("9111.11");

        //Persistence test for CCD file (ordering in the query assures us that ccd is first
        NACHAFile ccdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(ccdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        //Persistence test for the PPD file
        NACHAFile ppdFile = nachaFiles.get(1);
        validateNACHAFileFinalized(ppdFile, NACHAFileType.PPD, expectedTotalCredits, expectedTotalDebits);

        //Make sure the statues were appropriately updated
        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c2ERFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2EEFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2ERFinTxns.size());
        assertEquals("Number of C2 EmployeeDdCredit EX txns", 2, c2EEFinTxns.size());

        verifyMMTxnAndTraceNums(c2ERFinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c2EEFinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        //Make sure the files are correct
        validateFileAndTraceNumbers(
                ccdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testCCD_2DExpected.ach"), NACHAFileType.CCD);
        validateFileAndTraceNumbers(
                ppdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testPPD_2DExpected.ach"), NACHAFileType.PPD);
    }

    @Test
    public void testSpecialChars() {
        CompanySpecialCharDataloader cSpecial = new CompanySpecialCharDataloader();
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070905000000");
        cSpecial.persistCompany1();
        PayrollRunDTO payrollRunDTO = cSpecial.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        cSpecial.persistPayrollRun(payrollRunDTO);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        assertEquals("Number of files for batch", 1, nachaFiles.size());
        String ccdFileName = nachaFiles.get(0).getFileName();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        createdBatch = offloader.getOffloadBatch();
        nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        assertEquals("Number of files for batch", 1, nachaFiles.size());
        String ppdFileName = nachaFiles.get(0).getFileName();
        PayrollServices.commitUnitOfWork();

        //Make sure the files are correct
        validateFileAndTraceNumbers(
                ccdFileName,
                Application.findFileOnClassPath("offload/expected/testSpecialCharsCCD.ach"), NACHAFileType.CCD);
        validateFileAndTraceNumbers(
                ppdFileName,
                Application.findFileOnClassPath("offload/expected/testSpecialCharsPPD.ach"), NACHAFileType.PPD);
    }

    /**
     * Simulates a delayed offload run on 9/26 for the prior day, 9/25
     */
    @Test
    public void testDelayedOffload() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime("20070926000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, offloadDate);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest05");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC2.getPayrollRunStatus());
        SpcfMoney expectedTotalCredits = new SpcfMoney("1982.45");
        SpcfMoney expectedTotalDebits = new SpcfMoney("1982.45");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c1FinTxns.size());
        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2FinTxns.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c2FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(),
                                    Application.findFileOnClassPath("offload/expected/testCCD_expected.ach"), NACHAFileType.CCD);
    }

    @Test
    public void testCCDPPDReversals() {
        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload ee cr
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        loadDataReversal();
        updateCompany1To2DayFunding();
        updateCompany2To2DayFunding();
        addCompany2Payroll4();
        addCompany1Payroll3();
        updateCompany1BackTo5DayFunding();
        updateCompany2BackTo5DayFunding();
        PSPDate.setPSPTime("20071011000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 10, 11, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 2, nachaFiles.size());

        SpcfMoney expectedTotalCreditsPPD = new SpcfMoney("2588.00");
        SpcfMoney expectedTotalDebitsPPD = new SpcfMoney("2588.00");

        SpcfMoney expectedTotalCreditsCCD = new SpcfMoney("2408.00");
        SpcfMoney expectedTotalDebitsCCD = new SpcfMoney("2408.00");

        PayrollServices.beginUnitOfWork();
        //Persistence test for CCD file (ordering in the query assures us that ccd is first
        NACHAFile ccdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(ccdFile, NACHAFileType.CCD, expectedTotalCreditsCCD, expectedTotalDebitsCCD);

        //Persistence test for the PPD file
        NACHAFile ppdFile = nachaFiles.get(1);
        validateNACHAFileFinalized(ppdFile, NACHAFileType.PPD, expectedTotalCreditsPPD, expectedTotalDebitsPPD);

        //Make sure the statues were appropriately updated


        DomainEntitySet<FinancialTransaction> c2ERFinTxns = FinancialTransaction
                .findFinancialTransactions(c2dl.getCompany(), "BatchTest80", null, null, null,
                        TransactionTypeCode.EmployerDdDebit, null, null,null);
        DomainEntitySet<FinancialTransaction> c1ERFinTxns = FinancialTransaction
                .findFinancialTransactions(c1dl.getCompany(), "BatchTest81", null, null, null,
                        TransactionTypeCode.EmployerDdDebit, null, null, null);
        DomainEntitySet<FinancialTransaction> c2EEFinTxns = FinancialTransaction
                .findFinancialTransactions(c2dl.getCompany(), "BatchTest80", null, null, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null, null);
        DomainEntitySet<FinancialTransaction> c1EEREVFinTxns = FinancialTransaction
                .findFinancialTransactions(c1dl.getCompany(), null, null, null, null,
                        TransactionTypeCode.EmployeeDdReversalDebit, null, null, null);
        DomainEntitySet<FinancialTransaction> c1EEFinTxns = FinancialTransaction
                .findFinancialTransactions(c1dl.getCompany(), "BatchTest81", null, null, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null, null);

        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2ERFinTxns.size());
        assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c1ERFinTxns.size());
        assertEquals("Number of C1 EmployeeDdCredit EX txns", 2, c1EEFinTxns.size());
        assertEquals("Number of C2 EmployeeDdCredit EX txns", 2, c2EEFinTxns.size());
        assertEquals("Number of C1 EmployeeDdReversalDebit EX txns", 2, c1EEREVFinTxns.size());


        verifyMMTxnAndTraceNums(c2ERFinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c1ERFinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c1EEFinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c2EEFinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c1EEREVFinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        //Make sure the files are correct
        validateFileAndTraceNumbers(
                ccdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testCCDMult_expected.ach"), NACHAFileType.CCD);
        validateFileAndTraceNumbers(
                ppdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testPPDMult_expected.ach"), NACHAFileType.PPD);
    }


    @Test
    public void testRollover() {
        try {
            // skip the FT validation for this testcase
            MoneyMovementControlUtil.setSkipValidation(true);
            PayrollServices.beginUnitOfWork();
            SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 11, 13, SpcfTimeZone.getLocalTimeZone());
            PSPDate.setPSPTime("20071113000000");
            updateCompany2To2DayFunding();
            addCompany2PayrollWillCauseRollover();
            updateCompany2BackTo5DayFunding();
            PayrollServices.commitUnitOfWork();

            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            /***********************Persistence testing*********************/
            PayrollServices.beginUnitOfWork();
            OffloadBatch createdBatch = offloader.getOffloadBatch();
            assertNotNull(createdBatch);
            assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                    createdBatch.getOffloadGroup().getOffloadGroupCd());
            assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
            assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
            assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

            PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest10");
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
            PayrollServices.commitUnitOfWork();
            assertEquals("Number of files:", 2, nachaFiles.size());
            assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC2.getPayrollRunStatus());
            SpcfMoney expectedTotalCredits = new SpcfMoney("199999998.98");
            SpcfMoney expectedTotalDebits = new SpcfMoney("199999998.98");

            //Persistence test for CCD file (ordering in the query assures us that ccd is first
            NACHAFile ccdFile = nachaFiles.get(0);
            validateNACHAFileFinalized(ccdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

            //Persistence test for the PPD file
            NACHAFile ppdFile = nachaFiles.get(1);
            validateNACHAFileFinalized(ppdFile, NACHAFileType.PPD, expectedTotalCredits, expectedTotalDebits);

            //Make sure the statues were appropriately updated
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<FinancialTransaction> c2ERFinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                            TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
            DomainEntitySet<FinancialTransaction> c2EEFinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                            TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

            assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2ERFinTxns.size());
            assertEquals("Number of C2 EmployeeDdCredit EX txns", 2, c2EEFinTxns.size());

            verifyMMTxnAndTraceNums(c2ERFinTxns, createdBatch);
            verifyMMTxnAndTraceNums(c2EEFinTxns, createdBatch);

            PayrollServices.commitUnitOfWork();
            /***********************End persistence testing*********************/

            //Make sure the files are correct
            validateFileAndTraceNumbers(ccdFile.getFileName(), Application.findFileOnClassPath("offload/expected/testCCDRollover_expected.ach"), NACHAFileType.CCD);

            validateFileAndTraceNumbers(ppdFile.getFileName(), Application.findFileOnClassPath("offload/expected/testPPDRollover_expected.ach"), NACHAFileType.PPD);
        } finally {
            MoneyMovementControlUtil.setSkipValidation(false);
        }
    }

    @Test
    public void testNonACHTxnNotOffloaded() {
        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload ee cr
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071106000000");
        c1dl.addFee1();
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 11, 6, SpcfTimeZone.getLocalTimeZone());

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader2.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of NACHA files", 0, nachaFiles.size());
    }

    @Test
    public void testMultSourceSystems_WithFee() {
        //function call to update the Offering Service Charge price for the given Service Charge Price Id
        //todo:offerings fix this
//        BillingDataLoader.updatePrice("dddddddd-dddd-dddd-dddd-000000000211", new BigDecimal("7.99"),
//                                      CalendarUtils.convertToCalendar(PSPDate.getPSPTime()));

        //Set up company 3
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071107000000");
        persistCompany3();
        PayrollServices.commitUnitOfWork();

        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071113000000");
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 11, 13, SpcfTimeZone.getLocalTimeZone());
        updateCompany2To2DayFunding();
        addCompany2Payroll3();
        updateCompany2BackTo5DayFunding();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        addCompany3Payroll1();

        PayrollRun payrollRun = PayrollRun.findPayrollRun(c3dl.getCompany(), "BatchTest87");

        CompanyBankAccount companyBankAccount = payrollRun
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        //Statement to save the billing details by calling BillingManager.save() method for the bills,
        // which was added by PayrollSubmitDD process by calling BillingManager.add() method.
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC3 = PayrollRun.findPayrollRun(c3dl.getCompany(), "BatchTest87");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest10");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC3.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC2.getPayrollRunStatus());
        assertEquals("Number of files:", 2, nachaFiles.size());

        SpcfMoney expectedTotalCredits = new SpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(2).add(new SpcfMoney("17712.22")));
        SpcfMoney expectedTotalDebits = new SpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(2).add(new SpcfMoney("17712.22")));

        PayrollServices.beginUnitOfWork();
        //Persistence test for CCD file (ordering in the query assures us that ccd is first
        NACHAFile ccdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(ccdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        expectedTotalCredits = new SpcfMoney("17712.22");
        expectedTotalDebits = new SpcfMoney("17712.22");
        //Persistence test for the PPD file
        NACHAFile ppdFile = nachaFiles.get(1);
        validateNACHAFileFinalized(ppdFile, NACHAFileType.PPD, expectedTotalCredits, expectedTotalDebits);

        //Make sure the statues were appropriately updated

        DomainEntitySet<FinancialTransaction> c2ERFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c2EEFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c3ERFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c3dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c3EEFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c3dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2ERFinTxns.size());
        assertEquals("Number of C2 EmployeeDdCredit EX txns", 2, c2EEFinTxns.size());
        assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c3ERFinTxns.size());
        assertEquals("Number of C1 EmployeeDdCredit EX txns", 2, c3EEFinTxns.size());

        verifyMMTxnAndTraceNums(c2ERFinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c2EEFinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c3ERFinTxns, createdBatch);
        verifyMMTxnAndTraceNums(c3EEFinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        //Make sure the files are correct
        validateFileAndTraceNumbers(ccdFile.getFileName(),
                                    Application.findFileOnClassPath("offload/expected/testCCDMultSSWithFee_expected.ach"), NACHAFileType.CCD);
        validateFileAndTraceNumbers(
                ppdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testPPDMultSSWithFee_expected.ach"), NACHAFileType.PPD);
    }

    @Test
    public void testMultSourceSystems_QBDTRollsOver() {
        //function call to update the Offering Service Charge price for the given Service Charge Price Id
        //todo:offerings fix this
//        BillingDataLoader.updatePrice("dddddddd-dddd-dddd-dddd-000000000211", new BigDecimal("7.99"),
//                                      CalendarUtils.convertToCalendar(PSPDate.getPSPTime()));

        //Set up company 3
        try {
            // skip the FT validation for this testcase
            MoneyMovementControlUtil.setSkipValidation(true);
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20071024000000");
            persistCompany3();
            PayrollServices.commitUnitOfWork();

            //Offload er db
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20071121000000");
            SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
            updateCompany2To2DayFunding();
            addCompany2Payroll3();
            updateCompany2BackTo5DayFunding();
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            addCompany3PayrollWillCauseRollover();

            PayrollRun payrollRun = PayrollRun.findPayrollRun(c3dl.getCompany(), "BatchTest10");

            CompanyBankAccount companyBankAccount = payrollRun
                    .getCompanyBankAccountForService(ServiceCode.DirectDeposit);

            //Statement to save the billing details by calling BillingManager.save() method for the bills,
            // which was added by PayrollSubmitDD process by calling BillingManager.add() method.
            PayrollServices.commitUnitOfWork();

            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            /***********************Persistence testing*********************/
            OffloadBatch createdBatch = offloader.getOffloadBatch();
            assertNotNull(createdBatch);
            assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                    createdBatch.getOffloadGroup().getOffloadGroupCd());
            assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
            assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
            assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

            PayrollServices.beginUnitOfWork();
            PayrollRun payRunC3 = PayrollRun.findPayrollRun(c3dl.getCompany(), "BatchTest10");
            PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest10");
            DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
            PayrollServices.commitUnitOfWork();
            assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC3.getPayrollRunStatus());
            assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payRunC2.getPayrollRunStatus());
            assertEquals("Number of files:", 2, nachaFiles.size());

            SpcfMoney expectedTotalCredits =  new SpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(2).add(new SpcfMoney("200009110.09")));
            SpcfMoney expectedTotalDebits = new SpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(2).add(new SpcfMoney("200009110.09")));

            PayrollServices.beginUnitOfWork();
            //Persistence test for CCD file (ordering in the query assures us that ccd is first
            NACHAFile ccdFile = nachaFiles.get(0);
            validateNACHAFileFinalized(ccdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

            expectedTotalCredits = new SpcfMoney("200009110.09");
            expectedTotalDebits = new SpcfMoney("200009110.09");
            //Persistence test for the PPD file
            NACHAFile ppdFile = nachaFiles.get(1);
            validateNACHAFileFinalized(ppdFile, NACHAFileType.PPD, expectedTotalCredits, expectedTotalDebits);

            //Make sure the statues were appropriately updated

            DomainEntitySet<FinancialTransaction> c2ERFinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                            TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
            DomainEntitySet<FinancialTransaction> c2EEFinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                            TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
            DomainEntitySet<FinancialTransaction> c3ERFinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBDT, c3dl.getCompany1().getCompanyId(),
                            TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
            DomainEntitySet<FinancialTransaction> c3EEFinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBDT, c3dl.getCompany1().getCompanyId(),
                            TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

            assertEquals("Number of C2 EmployerDdDebit EX txns", 1, c2ERFinTxns.size());
            assertEquals("Number of C2 EmployeeDdCredit EX txns", 2, c2EEFinTxns.size());
            assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c3ERFinTxns.size());
            assertEquals("Number of C1 EmployeeDdCredit EX txns", 2, c3EEFinTxns.size());

            verifyMMTxnAndTraceNums(c2ERFinTxns, createdBatch);
            verifyMMTxnAndTraceNums(c2EEFinTxns, createdBatch);
            verifyMMTxnAndTraceNums(c3ERFinTxns, createdBatch);
            verifyMMTxnAndTraceNums(c3EEFinTxns, createdBatch);

            PayrollServices.commitUnitOfWork();
            /***********************End persistence testing*********************/

            //Make sure the files are correct
            validateFileAndTraceNumbers(
                    ccdFile.getFileName(),
                    Application.findFileOnClassPath("offload/expected/testQBDTRolloverCCD_expected.ach"), NACHAFileType.CCD);
            validateFileAndTraceNumbers(
                    ppdFile.getFileName(),
                    Application.findFileOnClassPath("offload/expected/testQBDTRolloverPPD_expected.ach"), NACHAFileType.PPD);
        } finally {
            MoneyMovementControlUtil.setSkipValidation(false);
        }
    }

    /**
     * Methods to load data*
     */
    private static void loadDataHappyPath() {
        PSPDate.setPSPTime("20070904000000");
        persistCompany1();
        persistCompany2();
    }

    private static void loadDataReversal() {
        PSPDate.setPSPTime("20071011000000");
        c1dl.reverseEntirePayroll("BatchTest05");
    }

    private static void loadDataReversalsPending() {
        c1dl.reverseEntirePayrollReversalsPending("BatchTest05");

    }

    private static void persistCompany1() {
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
    }

    private static void persistCompany2() {
        c2dl.persistCompany2();
        PayrollRunDTO payrollRunDTO = c2dl.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO);
    }

    private static void persistCompany3() {
        c3dl.persistCompany3();
    }

    private static void addCompany3Payroll1() {
        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-11-15"));
        c3dl.persistPayrollRun(payrollRunDTO);
    }

    private static void addCompany2Payroll2() {
        PayrollRunDTO payrollRunDTO = c2dl.get2ndCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-10"));
        c2dl.persistPayrollRun(payrollRunDTO);
    }

    private static void updateCompany2To2DayFunding() {
        c2dl.updateTo2DayFundingModel();
    }

    private static void updateCompany2BackTo5DayFunding() {
        c2dl.updateTo5DayFundingModel();
    }

    private static void updateCompany1To2DayFunding() {
        c1dl.updateTo2DayFundingModel();
    }

    private static void updateCompany1BackTo5DayFunding() {
        c1dl.updateTo5DayFundingModel();
    }

    private static void updateCompany2Limits() {
        c2dl.updateLimits(new SpcfMoney("9999999999.99"));
    }

    private static void updateCompany3Limits() {
        c3dl.updateLimits(new SpcfMoney("9999999999.99"));
    }

    private static void addCompany2Payroll3() {
        PayrollRunDTO payrollRunDTO = c2dl.get3rdCompany2PR_DoesNotExceedLimits(new DateDTO("2007-11-15"));
        c2dl.persistPayrollRun(payrollRunDTO);
    }

    private void addCompany2Payroll4() {
        PayrollRunDTO payrollRunDTO = c2dl.get4thCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-15"));
        c2dl.persistPayrollRun(payrollRunDTO);
    }

    private void addCompany1Payroll3() {
        PayrollRunDTO payrollRunDTO = c1dl.get3rdCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-15"));
        c1dl.persistPayrollRun(payrollRunDTO);
    }

    private static void addCompany2PayrollWillCauseRollover() {
        updateCompany2Limits();
        PayrollRunDTO payrollRunDTO = c2dl.get3rdCompany2PR_ExceedsOldLimits(new DateDTO("2007-11-15"));
        c2dl.persistPayrollRun(payrollRunDTO);
    }

    private static void addCompany3PayrollWillCauseRollover() {
        updateCompany3Limits();
        PayrollRunDTO payrollRunDTO = c3dl.get3rdCompany2PR_ExceedsOldLimits(new DateDTO("2007-11-15"));
        c3dl.persistPayrollRun(payrollRunDTO);
    }

    private void returnCompany1ERDDDB_R02() {
        c1dl.returnERDDDB("BatchTest81", "R02");
        PayrollRun payrollRun = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest81");
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

        ArrayList<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        redebitImpoundDTOs.add(redebitDTO);
        ProcessResult processResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                c1dl.getCompany().getSourceSystemCd(), c1dl.getCompany().getSourceCompanyId(), redebitImpoundDTOs);

        assertSuccess("addRedebitImpoundTransaction", processResult);
    }

    private static void loadDataBadDebitWriteOff() {
        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addWriteOffBadDebtTransaction(
                        SourceSystemCode.valueOf(c2dl.getCompany1().getSourceSystemCd().toString()),
                        c2dl.getCompany1().getCompanyId(), "BatchTest10");
        Assert.assertEquals(0, processResult.getMessages().size());
    }

    /**
     * Private validators *
     */
    private void assertYearMonthDayEquals(SpcfCalendar pTimeToCompare, SpcfCalendar pPSPTime) {
        assertEquals(pTimeToCompare.getDay(), pPSPTime.getDay());
        assertEquals(pTimeToCompare.getMonth(), pPSPTime.getMonth());
        assertEquals(pTimeToCompare.getYear(), pPSPTime.getYear());
    }

    private void verifyMMTxnAndTraceNums(DomainEntitySet<FinancialTransaction> pFinTxns, OffloadBatch pOffloadBatch) {
        for (FinancialTransaction currFinTxn : pFinTxns) {
            assertEquals("Financial transaction is executed", TransactionStateCode.Executed,
                    currFinTxn.getCurrentTransactionState().getTransactionStateCd());
            MoneyMovementTransaction mmTxn = currFinTxn.getMoneyMovementTransaction();
            OffloadBatch associatedOffloadBatch = mmTxn.getOffloadBatch();
            assertEquals("Offload batch for mmTxn", pOffloadBatch, associatedOffloadBatch);
            assertEquals("mmTxn status", PaymentStatus.Executed, mmTxn.getStatus());
            DomainEntitySet<EntryDetailRecord> entryDetailRecords = mmTxn.getEntryDetailRecordCollection();
            assertTrue("Number of entryDetailRecords", entryDetailRecords.size() >= 1);
            for (EntryDetailRecord currRecord : entryDetailRecords) {
                //Ensure that Intuit transactions don't have a trace number and that non-Intuit transactions do have a trace number
                if (currRecord.getRecordData()!=null) {
                    assertNotNull(currRecord.getTraceNumber());
                } else {
                    assertNull(currRecord.getTraceNumber());
                }
            }
        }
    }

    private void validateNACHAFileFinalized(NACHAFile pCreatedFile, NACHAFileType pFileType,
                                            SpcfMoney pExpectedTotalCredits, SpcfMoney pExpectedTotalDebits) {
        assertNull(pCreatedFile.getConfirmationCode());
        assertNull(pCreatedFile.getConfirmationDate());
        assertNull(pCreatedFile.getTransmissionDate());
        assertEquals("Total debit amount: ", pExpectedTotalDebits, pCreatedFile.getDebitTxnTotalAmount());
        assertEquals("Total credit amount: ", pExpectedTotalCredits, pCreatedFile.getCreditTxnTotalAmount());
        boolean b = Pattern.matches("\\p{Alnum}", pCreatedFile.getFileIDModifier());
        assertTrue("File ID modifier matches regular expression", b);

        assertNotNull("File name", pCreatedFile.getFileName());
        String expectedPath = ConfigurationManager
                .getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");

        int indexOfLastSeparator = pCreatedFile.getFileName().lastIndexOf(File.separator);
        String actualPath = pCreatedFile.getFileName().substring(0, indexOfLastSeparator);
        String actualName = pCreatedFile.getFileName()
                .substring(indexOfLastSeparator + 1, pCreatedFile.getFileName().length());

        boolean enableEncryption = SystemParameter.findBooleanValue(
                        SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
        String fileExt = enableEncryption ? ".pgp" : ".txt";

        assertEquals("Path", expectedPath, actualPath);
        assertEquals("Length of file name", 22, actualName.length());
        assertTrue("File ends with " + pFileType.toString() + fileExt,
                actualName.endsWith(pFileType.toString() + fileExt));
        assertTrue("File begins with d.", actualName.startsWith("d."));
        assertEquals("File Type", pFileType, pCreatedFile.getFileType());
        assertYearMonthDayEquals(PSPDate.getPSPTime(), pCreatedFile.getFinalizationDate().toLocal());
        assertEquals("File Status", NACHAFileStatus.Finalized, pCreatedFile.getStatus());
        assertYearMonthDayEquals(PSPDate.getPSPTime(), pCreatedFile.getStatusEffectiveDate().toLocal());
    }

    private ACHCompare validateFile(String pCreatedFileName, String pExpectedFileName, NACHAFileType pFileType) {

        try {
            String expectedFileName = null;
            CompareResults validatedCompareResults = null;
            ACHCompare validatedComparedACH = null;

            BufferedReader expectedReader = new BufferedReader(new FileReader(pExpectedFileName));
            PgpReader pgpReader = PgpReaderFactory.createInstance();
            pgpReader.open(pCreatedFileName);

            ACHCompare achCompare = new ACHCompare();
            CompareResults compareResults = achCompare.compareACH(expectedReader, pgpReader, pFileType);
            if (compareResults.getStatus()) {
                expectedFileName = pExpectedFileName;
                validatedCompareResults = compareResults;
                validatedComparedACH = achCompare;
            } else {
                System.out.println("Actual:\n" + FileUtils.readFileToString(new File(pCreatedFileName)));
                System.out.println("Expected:\n" + FileUtils.readFileToString(new File(pExpectedFileName)));
                fail(compareResults.toString());
            }
            assertNotNull("File "+pCreatedFileName+" matches expected file "+expectedFileName,validatedCompareResults);
            return validatedComparedACH;
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }
        return null;
    }

    private void validateFileAndTraceNumbers(String pCreatedFileName, String pExpectedFileName, NACHAFileType pFileType) {
        try {

            ACHCompare achCompare = validateFile(pCreatedFileName, pExpectedFileName, pFileType);
            HashMap<String, String> recordsTraceNums = achCompare.getRecordTraceNumMap();

            for (String currRecord : recordsTraceNums.keySet()) {
                String currTraceNum = recordsTraceNums.get(currRecord);
                EntryDetailRecord entryDetailRecordsWithTraceNumber = EntryDetailRecord.findEntryDetailRecordsWithTraceNumber(Long.parseLong(currTraceNum));
                if (entryDetailRecordsWithTraceNumber != null) {
                    boolean foundAMatch=false;
                    Long expectedTraceNumber = Long.parseLong(currTraceNum);
                    Long actualTraceNumber = Long.parseLong(entryDetailRecordsWithTraceNumber.getTraceNumber());
                    if (actualTraceNumber.equals(expectedTraceNumber)) {
                        foundAMatch=true;
                    }
                    assertTrue("Found a match for "+currTraceNum, foundAMatch);
                }
            }

            //ensure trace numbers are not duplicated in the file
            ArrayList<String> foundTraceNumbers = new ArrayList<String>();

            for (String traceNumber : achCompare.getTraceNumbers()) {
                if (foundTraceNumbers.contains(traceNumber)) {
                    fail("Found duplicate trace number in file: "+pCreatedFileName+" of: "+traceNumber);
                }
                foundTraceNumbers.add(traceNumber);
            }

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }

    }


    @Test
    public void testOffloadCCD_MultipleOffloadGroups() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.updateSystemParameter(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY, "true");
        changeOffloadGroup(c1dl.getCompany());

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        PayrollRun payRunC2 = PayrollRun.findPayrollRun(c2dl.getCompany(), "BatchTest05");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.Pending, payRunC1.getPayrollRunStatus());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC2.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("1802.45");
        SpcfMoney expectedTotalDebits = new SpcfMoney("1802.45");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDdDebit Created txns", 1, c1FinTxns.size());
        assertEquals("Number of C2 EmployerDdDebit Executed txns", 1, c2FinTxns.size());

        PayrollServices.commitUnitOfWork();

        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(),
                                    Application.findFileOnClassPath("offload/expected/testCCDMultipleOffloadGroup_expected.ach"), NACHAFileType.CCD);
    }

    @Test
    public void testBooktxfr_MultipleOffloadGroups() {
        // inorder to create a new offload group we need to create offload batches on the fly
        DataLoadServices.updateSystemParameter(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY, "true");
        changeOffloadGroup(c2dl.getCompany());
        //Offload er db
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070926000000");
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 26, SpcfTimeZone.getLocalTimeZone());
        c1dl.returnERDDDB("BatchTest05", "R01");
        addCompany2Payroll2();
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        OffloadBatch createdBatch = offloader2.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        SpcfMoney expectedTotalCredits = new SpcfMoney("180.00");
        SpcfMoney expectedTotalDebits = new SpcfMoney("180.00");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> bookTxfrTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.Intuit5DayReturnTransfer, TransactionStateCode.Executed);

        assertEquals("Number of C1 Intuit5DayReturnTransfer EX txns", 1, bookTxfrTxns.size());

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Returned);

        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);

        assertEquals("Number of C1 EmployerDdDebit Returned txns", 1, c1FinTxns.size());
        assertEquals("Number of C2 EmployerDdDebit Created txns", 2, c2FinTxns.size());
        PayrollServices.commitUnitOfWork();

        validateFileAndTraceNumbers(Application.findFileOnClassPath("offload/expected/testBookTxfrMultipleOffloadGroup_expected.ach"),
                                    createdFile.getFileName(),
                                    NACHAFileType.CCD);
    }

    /**
     * Offload stored procedure is supposed to create unique token for each FeeOffloadedEvent per company.
     */
    @Test
    public void testUniqueTokenPerFeeOffloadedEvent() {
        // Setup
        PayrollServices.beginUnitOfWork();
        Company c1 = Company.findCompany("2222222", SourceSystemCode.QBOE);
        c1.setSourceSystemCd(SourceSystemCode.QBDT);
        Application.save(c1);
        Company c2 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        c2.setSourceSystemCd(SourceSystemCode.QBDT);
        Application.save(c2);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBDT, "2222222", "BatchTest05",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("75.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071106000000");
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBDT, "1234567", "BatchTest05",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("75.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // Test
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Create fee offload events
        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

        // Verification
        PayrollServices.beginUnitOfWork();
        c1 = Application.refresh(c1);
        c2 = Application.refresh(c2);
        assertEquals("Company current token", 5, c1.getCurrentToken());
        assertEquals("Company current token", 5, c2.getCurrentToken());
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(c1, 5L);
        assertEquals("FeeOffloadedEvent type", EventTypeCode.FeeOffloaded, companyEvents.get(0).getEventTypeCd());
        assertEquals("FeeOffloadedEvent token", 5L, companyEvents.get(0).getEventToken());
        assertEquals("FeeOffloadedEvent type", EventTypeCode.FeeOffloaded, companyEvents.get(1).getEventTypeCd());
        assertEquals("FeeOffloadedEvent token", 5L, companyEvents.get(1).getEventToken());

        companyEvents = CompanyEvent.findCompanyEvents(c2, 5L);
        assertEquals("FeeOffloadedEvent type", EventTypeCode.FeeOffloaded, companyEvents.get(0).getEventTypeCd());
        assertEquals("FeeOffloadedEvent token", 5L, companyEvents.get(0).getEventToken());
        assertEquals("FeeOffloadedEvent type", EventTypeCode.FeeOffloaded, companyEvents.get(1).getEventTypeCd());
        assertEquals("FeeOffloadedEvent token", 5L, companyEvents.get(1).getEventToken());
        Application.commitUnitOfWork();
    }

    /**
     * Test method to verify no PPD or CCD NACHA File records are created if there are no corresponding transactions to offload
     */
    @Test
    public void testOnlyCCDOrPPDTransactionsToProcess() {
        // offload all txns

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        OffloadBatch offloadBatch = offloader.getOffloadBatch();
        assertNotNull(offloadBatch);

        // Verify no PPD Records are created
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> ppdFiles = Application.find(NACHAFile.class, NACHAFile.FileType().equalTo(NACHAFileType.PPD).And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of PPD File Records", 0, ppdFiles.size());

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        offloadBatch = offloader.getOffloadBatch();
        assertNotNull(offloadBatch);

        // Verify no new CCD Records are created
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> ccdFiles =
                Application.find(NACHAFile.class,
                                 NACHAFile.FileType().equalTo(NACHAFileType.CCD).And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of CCD File Records", 3, ccdFiles.size());

        // Now there should be one PPD File
        PayrollServices.beginUnitOfWork();
        ppdFiles = Application.find(NACHAFile.class,
                                    NACHAFile.FileType().equalTo(NACHAFileType.PPD).And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of PPD File Records", 1, ppdFiles.size());

    }


    /**
     * Function to change the offload group of a company
     * @param pCompany Companys
     */
    private void changeOffloadGroup(Company pCompany){
        PayrollServices.beginUnitOfWork();
        OffloadGroup offloadGroup = new OffloadGroup();
        offloadGroup.setOffloadGroupCd("ABC");
        offloadGroup.setCutoffTime("15:00:00");
        offloadGroup.setName("Test OffloadGroup ");
        offloadGroup.setDescription("Test 15:00 Offload Group");
        offloadGroup = Application.save(offloadGroup);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        offloadGroup = PayrollServices.entityFinder.findById(OffloadGroup.class, offloadGroup.getId());
        pCompany.setOffloadGroup(offloadGroup);
        Application.save(pCompany);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testOffloadCCD_BankAccountNumberWithWhiteSpaces() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit_BankAccountNumberWithWhiteSpaces();
        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : payChecks) {
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(new BigDecimal("100.22"));
            }
        }
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "1234562", payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 10, 01, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234562", SourceSystemCode.QBOE);
        PayrollRun payRunC1 = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedDebit, payRunC1.getPayrollRunStatus());
        SpcfMoney expectedTotalCredits = new SpcfMoney("200.44");
        SpcfMoney expectedTotalDebits = new SpcfMoney("200.44");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDdDebit EX txns", 1, c1FinTxns.size());

        verifyMMTxnAndTraceNums(c1FinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testCCDBankAccountNumberWithWhiteSpaces_expected.ach"), NACHAFileType.CCD);
    }

    @Test
    public void testOffloadEmployerTaxDebit() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.claimOffer(company, DataLoadServices.WAIVE_ALL_FEES);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-10"), employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        PayrollRun payrollRun = (PayrollRun) processResult.getResult();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110106180000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone());
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payrollRun.getPayrollRunStatus());
        SpcfMoney expectedTotalCredits = new SpcfMoney("189.00");
        SpcfMoney expectedTotalDebits = new SpcfMoney("189.00");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        payrollRun = Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> erTaxDBFinTxns = payrollRun.getEmployerTaxDebitTransactions();

        assertEquals("Number of C1 EmployerTaxDebit EX txns", 1, erTaxDBFinTxns.size());

        verifyMMTxnAndTraceNums(erTaxDBFinTxns, createdBatch);

        PayrollServices.commitUnitOfWork();
        /***********************End persistence testing*********************/

        validateFileAndTraceNumbers(createdFile.getFileName(),
                                    Application.findFileOnClassPath("offload/expected/testEmployerTaxDebitCCD_expected.ach"), NACHAFileType.CCD);
    }

    @Test
    public void testEmployerTaxNSFPayRunStatus() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.claimOffer(company, DataLoadServices.WAIVE_ALL_FEES);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-10"), employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        PayrollRun payrollRun = (PayrollRun) processResult.getResult();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110106180000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone());
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                        payrollRun.getSourcePayRunId(), null, null, null, TransactionTypeCode.EmployerTaxDebit,
                        null, null, TransactionStateCode.Executed);

        assertEquals("Number of ER Tax Debit txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        offloadDate = SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone());

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);

        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.AutoRedebitOffloaded, payrollRun.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("289.08");
        SpcfMoney expectedTotalDebits = new SpcfMoney("289.08");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> erTaxRedebitFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerTaxRedebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerTaxDebit EX txns", 1, erTaxRedebitFinTxns.size());

        verifyMMTxnAndTraceNums(erTaxRedebitFinTxns, createdBatch);
        PayrollServices.commitUnitOfWork();

        validateFileAndTraceNumbers(
                createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testERTaxRedebitCCD_expected.ach"), NACHAFileType.CCD);
    }

    @Test
    public void testRetryPaymentNachaBatchType() {

        //1. Set up a date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        //2. Set up the company
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        //3. Add employees to this company
        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        //4. Submit a payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-10"), employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        PayrollRun payrollRun = (PayrollRun) processResult.getResult();

        //5. Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110106180000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone());
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //6. Get the Executed the FT transactions after the offload so we will return them to test our functionality
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                                                               payrollRun.getSourcePayRunId(), null, null, null, TransactionTypeCode.EmployerTaxDebit,
                                                               null, null, TransactionStateCode.Executed);

        assertEquals("Number of ER Tax Debit txns", 1, c1FinTxns.size());

        DomainEntitySet<FinancialTransaction> c2FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                                                               payrollRun.getSourcePayRunId(), null, null, null, TransactionTypeCode.EmployerFeeDebit,
                                                               null, null, TransactionStateCode.Executed);

        assertEquals("Number of ER Fee Debit txns", 1, c2FinTxns.size());

        DomainEntitySet<FinancialTransaction> c3FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                                                               payrollRun.getSourcePayRunId(), null, null, null, TransactionTypeCode.ServiceSalesAndUseTax,
                                                               null, null, TransactionStateCode.Executed);

        assertEquals("Number of ER ServiceSalesAndUseTax Debit txns", 1, c3FinTxns.size());


        DomainEntitySet<FinancialTransaction> finTxns = new DomainEntitySet<FinancialTransaction>();
        finTxns.addAll(c1FinTxns);
        finTxns.addAll(c2FinTxns);
        finTxns.addAll(c3FinTxns);
        Application.commitUnitOfWork();



        //7. Return the transactions, so we can have re-debits created in system
        DataLoadServices.returnTxns(finTxns, "R01", "This is an NSF description");



        //8. Add 1 day for next day offload
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();


        //9. Offload the batch
        offloadDate = SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone());
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //10. Verify the offload complete
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                     createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());


        //11. Finalized the offload and verify the nach file details
        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.AutoRedebitOffloaded, payrollRun.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("368.16");
        SpcfMoney expectedTotalDebits = new SpcfMoney("368.16");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);


        //12. Verify the re-debits
        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> erRedebitFinTxns = new DomainEntitySet<FinancialTransaction>();


        DomainEntitySet<FinancialTransaction> erTaxRedebitFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerTaxRedebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerTaxRedebit EX txns", 1, erTaxRedebitFinTxns.size());


        DomainEntitySet<FinancialTransaction> erFeeRedebitFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerFeeRedebit EX txns", 1, erFeeRedebitFinTxns.size());
        DomainEntitySet<FinancialTransaction> erFeeSalesRedebitFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.ServiceSalesAndUseTaxRedebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 ServiceSalesAndUseTaxRedebit EX txns", 1, erFeeSalesRedebitFinTxns.size());


        //13. Verify the above FTs in MMTs with their batches
        erRedebitFinTxns.addAll(erTaxRedebitFinTxns);
        erRedebitFinTxns.addAll(erFeeRedebitFinTxns);
        erRedebitFinTxns.addAll(erFeeSalesRedebitFinTxns);
        verifyMMTxnAndTraceNums(erRedebitFinTxns, createdBatch);
        PayrollServices.commitUnitOfWork();

        //14.Verify the final nacha file for RetryPayments batches
        validateFileAndTraceNumbers(
                createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testRetryPaymentCCD_expected.ach"), NACHAFileType.CCD);

    }

    @Test
    public void testRetryPaymentDDNachaBatchType() {

        //1. Set up a date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        //2. Set up the company
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};
        DataLoadServices.addFederalTaxCompanyLaws(company);
        //DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        //3. Add employees to this company
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, true);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        //4. Submit a payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-10"), employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        PayrollRun payrollRun = (PayrollRun) processResult.getResult();

        //5. Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110106180000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2011, 1, 6, SpcfTimeZone.getLocalTimeZone());
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        //6. Get the Executed the FT transactions after the offload so we will return them to test our functionality
        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                                                               payrollRun.getSourcePayRunId(), null, null, null, TransactionTypeCode.EmployeeDdCredit,
                                                               null, null, TransactionStateCode.Executed);

        assertEquals("Number of EmployeeDdCredit txns", 1, c1FinTxns.size());


        DomainEntitySet<FinancialTransaction> c2FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                                                               payrollRun.getSourcePayRunId(), null, null, null, TransactionTypeCode.EmployerTaxDebit,
                                                               null, null, TransactionStateCode.Executed);

        assertEquals("Number of ER Tax Debit txns", 1, c2FinTxns.size());

        DomainEntitySet<FinancialTransaction> c3FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                                                               payrollRun.getSourcePayRunId(), null, null, null, TransactionTypeCode.EmployerFeeDebit,
                                                               null, null, TransactionStateCode.Executed);

        assertEquals("Number of ER Fee Debit txns", 2, c3FinTxns.size());

        DomainEntitySet<FinancialTransaction> c4FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                                                               payrollRun.getSourcePayRunId(), null, null, null, TransactionTypeCode.ServiceSalesAndUseTax,
                                                               null, null, TransactionStateCode.Executed);

        assertEquals("Number of ER ServiceSalesAndUseTax Debit txns",2, c4FinTxns.size());


        DomainEntitySet<FinancialTransaction> finTxns = new DomainEntitySet<FinancialTransaction>();
        finTxns.addAll(c1FinTxns);
        finTxns.addAll(c2FinTxns);
        finTxns.addAll(c3FinTxns);
        finTxns.addAll(c4FinTxns);
        Application.commitUnitOfWork();



        //7. Return the transactions, so we can have re-debits created in system
        DataLoadServices.returnTxns(finTxns, "R01", "This is an NSF description");


        //8. Add 1 day for next day offload
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();


        //9. Offload the batch
        offloadDate = SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone());
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //10. Verify the offload complete
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                     createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());


        //11. Finalized the offload and verify the nach file details
        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 1, nachaFiles.size());
        assertEquals("PayrollRunStatus", PayrollStatus.AutoRedebitOffloaded, payrollRun.getPayrollRunStatus());

        SpcfMoney expectedTotalCredits = new SpcfMoney("276.19");
        SpcfMoney expectedTotalDebits = new SpcfMoney("276.19");

        NACHAFile createdFile = nachaFiles.get(0);
        validateNACHAFileFinalized(createdFile, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);


        //12. Verify the re-debits
        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> erRedebitFinTxns = new DomainEntitySet<FinancialTransaction>();

        DomainEntitySet<FinancialTransaction> erDDRedebitFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);

        assertEquals("Number of EmployerDdRedebit EX txns", 1, erDDRedebitFinTxns.size());


        DomainEntitySet<FinancialTransaction> erTaxRedebitFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerTaxRedebit, TransactionStateCode.Executed);

        assertEquals("Number of EmployerTaxRedebit EX txns", 1, erTaxRedebitFinTxns.size());


        DomainEntitySet<FinancialTransaction> erFeeRedebitFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Executed);

        assertEquals("Number of EmployerFeeRedebit EX txns", 2, erFeeRedebitFinTxns.size());
        DomainEntitySet<FinancialTransaction> erFeeSalesRedebitFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.ServiceSalesAndUseTaxRedebit, TransactionStateCode.Executed);

        assertEquals("Number of ServiceSalesAndUseTaxRedebit EX txns", 2, erFeeSalesRedebitFinTxns.size());


        //13. Verify the above FTs in MMTs with their batches
        erRedebitFinTxns.addAll(erDDRedebitFinTxns);
        erRedebitFinTxns.addAll(erTaxRedebitFinTxns);
        erRedebitFinTxns.addAll(erFeeRedebitFinTxns);
        erRedebitFinTxns.addAll(erFeeSalesRedebitFinTxns);
        verifyMMTxnAndTraceNums(erRedebitFinTxns, createdBatch);
        PayrollServices.commitUnitOfWork();

        //14.Verify the final nacha file for RetryPayments batches
        validateFileAndTraceNumbers(
                createdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testRetryPaymentDDCCD_expected.ach"), NACHAFileType.CCD);

    }

    private void setUpCompanyAndRunPayroll(String psid) {

        //2. Set up the company
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};
        DataLoadServices.addFederalTaxCompanyLaws(company);
        //DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        //3. Add employees to this company
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, true);
        //DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        //4. Submit a payroll
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        //DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        //PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-10"), employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        PayrollRun payrollRun = (PayrollRun) processResult.getResult();



    }

    @Test
    public void testNoTransactionsToOffload() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070926000000");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 26, SpcfTimeZone.getLocalTimeZone());

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***********************Persistence testing*********************/
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        assertEquals("Offload date", offloadDate, createdBatch.getOffloadDate().toLocal());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 0, nachaFiles.size());
    }

    /**
     * Test scenarios when the creditTotals and debitTotals in the CCD and PPD files do not match
     * 1. When the difference between creditTotals and debitTotals is greater than the threshold defined $80000, when
     *    the job stops, we reset the PERFORM_CREDIT_DEBIT_TOTALS_CHECK flag and rerun the job which should succeed.
     *    However, notifications would be sent out to PD and BizOps
     * 2. When the difference between creditTotals and debitTotals is lesser than the threshold defined $80000, then
     *    the job wont stop and should succeed. However, notifications would be sent out to PD and BizOps
     */
    @Test
    public void testCreditDebitTotalsMismatchForCCDandPPD() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925070000");
        PayrollServices.commitUnitOfWork();
        String thresholdAmount = "$" + SystemParameter.findSystemParameter(SystemParameter.Code.CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD).getSystemParameterValue();

        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2007, 9, 25, 00, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());

        //Checking for CCD file for difference above threshold
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class,
                new Query<EntryDetailRecord>()
                        .Where(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)
                                .And(EntryDetailRecord.InitiationDate().equalTo(offloadDate)
                                        .And(EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCD)))));

        if(entryDetailRecords.size() > 1){
            EntryDetailRecord entryDetailRecord = entryDetailRecords.getFirst();
            entryDetailRecord.setAmount(new SpcfMoney("90000"));
            Application.save(entryDetailRecord);
            PayrollServices.commitUnitOfWork();
        } else {
            PayrollServices.rollbackUnitOfWork();
        }

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        try {
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
            TestCase.fail("Did not catch expected RuntimeException The totalDebitAmount and totalCreditAmount do not match and the difference is greater than the threshold " + thresholdAmount);
        } catch (Exception rte) {
            assertEquals("RuntimeException message", "The totalDebitAmount and totalCreditAmount do not match and the difference is greater than or equal to the threshold " + thresholdAmount, rte.getMessage());
            DomainEntitySet<OffloadBatch> offloadBatches = Application.find(OffloadBatch.class,
                    new Query<OffloadBatch>()
                            .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                    .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD)))));
            DomainEntitySet<NACHAFile> nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                    new Query<NACHAFile>()
                            .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                    .And(NACHAFile.FileType().equalTo(NACHAFileType.CCD)
                                            .And(NACHAFile.Status().equalTo(NACHAFileStatus.InProcess)))));
            assertEquals("Offload batch count", 1, offloadBatches.size());
            assertEquals("Offload batch status", OffloadBatchStatus.InProcess, offloadBatches.getFirst().getStatusCd());
            assertEquals("CCD Nachafile count with InProcess status", 1, nachaFilesForOffloadDate.size());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        PayrollServices.beginUnitOfWork();
        SystemParameter systemParameter = SystemParameter.findSystemParameter(SystemParameter.Code.PERFORM_CREDIT_DEBIT_TOTALS_CHECK);
        systemParameter.setSystemParameterValue("false");
        PayrollServices.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DomainEntitySet<OffloadBatch> offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD)))));
        DomainEntitySet<NACHAFile> nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.CCD)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));
        assertEquals("Offload batch count", 1, offloadBatches.size());
        assertEquals("Offload batch status", OffloadBatchStatus.Completed, offloadBatches.getFirst().getStatusCd());
        assertEquals("CCD Nachafile count with InProcess status", 1, nachaFilesForOffloadDate.size());

        //Checking for CCD file for difference below threshold
        PayrollServices.beginUnitOfWork();

        //Move the OffloadBatch and NACHAFile status to InProcess
        offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD)))));
        nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.CCD)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));
        offloadBatches.getFirst().setStatusCd(OffloadBatchStatus.InProcess);
        nachaFilesForOffloadDate.getFirst().setStatus(NACHAFileStatus.InProcess);
        systemParameter = SystemParameter.findSystemParameter(SystemParameter.Code.PERFORM_CREDIT_DEBIT_TOTALS_CHECK);
        systemParameter.setSystemParameterValue("true");

        entryDetailRecords = Application.find(EntryDetailRecord.class,
                new Query<EntryDetailRecord>()
                        .Where(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)
                                .And(EntryDetailRecord.InitiationDate().equalTo(offloadDate)
                                        .And(EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCD)))));

        if(entryDetailRecords.size() > 1){
            EntryDetailRecord entryDetailRecord = null;
            for (EntryDetailRecord edr : entryDetailRecords){
                if (edr.getAmount().equals(new SpcfMoney("90000"))) {
                    entryDetailRecord = edr;
                    break;
                }
            }
            entryDetailRecord.setAmount(SpcfMoney.ZERO);
            Application.save(entryDetailRecord);
            PayrollServices.commitUnitOfWork();
        } else {
            PayrollServices.rollbackUnitOfWork();
        }

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD)))));
        nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.CCD)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));
        assertEquals("Offload batch count", 1, offloadBatches.size());
        assertEquals("Offload batch status", OffloadBatchStatus.Completed, offloadBatches.getFirst().getStatusCd());
        assertEquals("CCD Nachafile count with InProcess status", 1, nachaFilesForOffloadDate.size());

        //Checking for PPD file
        PayrollServices.beginUnitOfWork();
        systemParameter = SystemParameter.findSystemParameter(SystemParameter.Code.PERFORM_CREDIT_DEBIT_TOTALS_CHECK);
        systemParameter.setSystemParameterValue("true");
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928070000");
        PayrollServices.commitUnitOfWork();
        offloadDate = SpcfCalendar.createInstance(2007, 9, 28, 00, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());

        PayrollServices.beginUnitOfWork();
        entryDetailRecords = Application.find(EntryDetailRecord.class,
                new Query<EntryDetailRecord>()
                        .Where(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)
                                .And(EntryDetailRecord.InitiationDate().equalTo(offloadDate)
                                        .And(EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.PPD)))));

        if(entryDetailRecords.size() > 1){
            EntryDetailRecord entryDetailRecord = entryDetailRecords.getFirst();
            entryDetailRecord.setAmount(new SpcfMoney("90000"));
            Application.save(entryDetailRecord);
            PayrollServices.commitUnitOfWork();
        } else {
            PayrollServices.rollbackUnitOfWork();
        }

        offloader = new OffloadACHTransactions();
        try {
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
            TestCase.fail("Did not catch expected RuntimeException The totalDebitAmount and totalCreditAmount do not match and the difference is greater than the threshold " + thresholdAmount);
        } catch (Exception rte) {
            assertEquals("RuntimeException message", "The totalDebitAmount and totalCreditAmount do not match and the difference is greater than or equal to the threshold "+thresholdAmount, rte.getMessage());
            offloadBatches = Application.find(OffloadBatch.class,
                    new Query<OffloadBatch>()
                            .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                    .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD)))));
            nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                    new Query<NACHAFile>()
                            .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                    .And(NACHAFile.FileType().equalTo(NACHAFileType.PPD)
                                            .And(NACHAFile.Status().equalTo(NACHAFileStatus.InProcess)))));
            assertEquals("Offload batch count", 1, offloadBatches.size());
            assertEquals("Offload batch status", OffloadBatchStatus.InProcess, offloadBatches.getFirst().getStatusCd());
            assertEquals("PPD Nachafile count with InProcess status", 1, nachaFilesForOffloadDate.size());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        PayrollServices.beginUnitOfWork();
        systemParameter = SystemParameter.findSystemParameter(SystemParameter.Code.PERFORM_CREDIT_DEBIT_TOTALS_CHECK);
        systemParameter.setSystemParameterValue("false");
        PayrollServices.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD)))));
        nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.PPD)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));
        assertEquals("Offload batch count", 1, offloadBatches.size());
        assertEquals("Offload batch status", OffloadBatchStatus.Completed, offloadBatches.getFirst().getStatusCd());
        assertEquals("PPD Nachafile count with InProcess status", 1, nachaFilesForOffloadDate.size());

        //Checking for PPD file for difference below threshold
        PayrollServices.beginUnitOfWork();

        //Move the OffloadBatch and NACHAFile status to InProcess
        offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD)))));
        nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.PPD)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));
        offloadBatches.getFirst().setStatusCd(OffloadBatchStatus.InProcess);
        nachaFilesForOffloadDate.getFirst().setStatus(NACHAFileStatus.InProcess);
        systemParameter = SystemParameter.findSystemParameter(SystemParameter.Code.PERFORM_CREDIT_DEBIT_TOTALS_CHECK);
        systemParameter.setSystemParameterValue("true");

        entryDetailRecords = Application.find(EntryDetailRecord.class,
                new Query<EntryDetailRecord>()
                        .Where(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)
                                .And(EntryDetailRecord.InitiationDate().equalTo(offloadDate)
                                        .And(EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.PPD)))));

        if(entryDetailRecords.size() > 1){
            EntryDetailRecord entryDetailRecord = null;
            for (EntryDetailRecord edr : entryDetailRecords){
                if (edr.getAmount().equals(new SpcfMoney("90000"))) {
                    entryDetailRecord = edr;
                    break;
                }
            }
            entryDetailRecord.setAmount(SpcfMoney.ZERO);
            Application.save(entryDetailRecord);
            PayrollServices.commitUnitOfWork();
        } else {
            PayrollServices.rollbackUnitOfWork();
        }

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD)))));
        nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.PPD)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));
        assertEquals("Offload batch count", 1, offloadBatches.size());
        assertEquals("Offload batch status", OffloadBatchStatus.Completed, offloadBatches.getFirst().getStatusCd());
        assertEquals("PPD Nachafile count with InProcess status", 1, nachaFilesForOffloadDate.size());

    }

    /**
     * Test scenarios when the creditTotals and debitTotals in the CCDPlus file do not match
     * 1. When the difference between creditTotals and debitTotals is greater than the threshold defined $80000, when
     *    the job stops, we reset the PERFORM_CREDIT_DEBIT_TOTALS_CHECK flag and rerun the job which should succeed.
     *    However, notifications would be sent out to PD and BizOps
     * 2. When the difference between creditTotals and debitTotals is lesser than the threshold defined $80000, then
     *    the job wont stop and should succeed. However, notifications would be sent out to PD and BizOps
     */
    @Test
    public void testCreditDebitTotalsMismatchForCCDPlus() {

        Company company = DataLoadServices.setupAssistedCompanyForCA("492904929", 4, Boolean.TRUE);
        String[] lawIds = {"61", "62", "63", "64", "143", "1","6","67"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25","6","67"};
        String paymentTemplateCd = "CA-PITSDI-PAYMENT";
        String thresholdAmount = SystemParameter.findSystemParameter(SystemParameter.Code.CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD).getSystemParameterValue();

        DataLoadServices.addCompanyLaws(company, lawIds);

        //enabling the ACHCredit payment method
        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(paymentTemplateCd));
        CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod = companyAgencyPaymentTemplate.getCompanyPaymentTemplatePaymentMethod(PaymentMethod.ACHCredit);
        companyPaymentTemplatePaymentMethod.setEnabled(Boolean.TRUE);
        Application.save(companyPaymentTemplatePaymentMethod);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2017, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), company.getEmployees());

        ArrayList<Employee> employees = new ArrayList<Employee>(company.getEmployees());
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2017-01-10"), employees, lawIds, amounts);
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();

        //offload date 4/27/2017
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2017, 5, 1, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(offloadDate, -achTaxOffloadOffset);
        
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(offloadDate);
        PayrollServices.commitUnitOfWork();

        //Checking for CCDPlus file for difference above threshold
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class,
                new Query<EntryDetailRecord>()
                        .Where(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)
                                .And(EntryDetailRecord.InitiationDate().equalTo(offloadDate)
                                        .And(EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus)))));

        if(entryDetailRecords.size() > 0){
            EntryDetailRecord entryDetailRecord = entryDetailRecords.getFirst();
            entryDetailRecord.setAmount(new SpcfMoney("90000"));
            Application.save(entryDetailRecord);
            PayrollServices.commitUnitOfWork();
        } else {
            PayrollServices.rollbackUnitOfWork();
        }

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        try {
            offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);
            TestCase.fail("Did not catch expected RuntimeException The totalDebitAmount and totalCreditAmount do not match and the difference is greater than or equal to the threshold $"+thresholdAmount);
        } catch (Exception rte) {
            assertEquals("RuntimeException message", "The totalDebitAmount and totalCreditAmount do not match and the difference is greater than or equal to the threshold $"+thresholdAmount, rte.getMessage());
            DomainEntitySet<OffloadBatch> offloadBatches = Application.find(OffloadBatch.class,
                    new Query<OffloadBatch>()
                            .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                    .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.TAXPAYMENT)))));
            DomainEntitySet<NACHAFile> nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                    new Query<NACHAFile>()
                            .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                    .And(NACHAFile.FileType().equalTo(NACHAFileType.CCDPlus)
                                            .And(NACHAFile.Status().equalTo(NACHAFileStatus.InProcess)))));
            assertEquals("Offload batch count", 1, offloadBatches.size());
            assertEquals("Offload batch status", OffloadBatchStatus.InProcess, offloadBatches.getFirst().getStatusCd());
            assertEquals("CCDPlus Nachafile count with InProcess status", 1, nachaFilesForOffloadDate.size());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        PayrollServices.beginUnitOfWork();
        SystemParameter systemParameter = SystemParameter.findSystemParameter(SystemParameter.Code.PERFORM_CREDIT_DEBIT_TOTALS_CHECK);
        systemParameter.setSystemParameterValue("false");
        PayrollServices.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        DomainEntitySet<OffloadBatch> offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.TAXPAYMENT)))));
        DomainEntitySet<NACHAFile> nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.CCDPlus)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));
        assertEquals("Offload batch count", 1, offloadBatches.size());
        assertEquals("Offload batch status", OffloadBatchStatus.Completed, offloadBatches.getFirst().getStatusCd());
        assertEquals("CCDPlus Nachafile count with InProcess status", 1, nachaFilesForOffloadDate.size());

        //Checking for CCDPlus file for difference below threshold
        PayrollServices.beginUnitOfWork();

        //Move the OffloadBatch and NACHAFile status to InProcess
        offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.TAXPAYMENT)))));
        nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.CCDPlus)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));
        offloadBatches.getFirst().setStatusCd(OffloadBatchStatus.InProcess);
        nachaFilesForOffloadDate.getFirst().setStatus(NACHAFileStatus.InProcess);
        systemParameter = SystemParameter.findSystemParameter(SystemParameter.Code.PERFORM_CREDIT_DEBIT_TOTALS_CHECK);
        systemParameter.setSystemParameterValue("true");

        entryDetailRecords = Application.find(EntryDetailRecord.class,
                new Query<EntryDetailRecord>()
                        .Where(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)
                                .And(EntryDetailRecord.InitiationDate().equalTo(offloadDate)
                                        .And(EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus)))));

        if(entryDetailRecords.size() > 0){
            EntryDetailRecord entryDetailRecord = entryDetailRecords.getFirst();
            entryDetailRecord.setAmount(SpcfMoney.ZERO);
            Application.save(entryDetailRecord);
            PayrollServices.commitUnitOfWork();
        } else {
            PayrollServices.rollbackUnitOfWork();
        }

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);

        offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.TAXPAYMENT)))));
        nachaFilesForOffloadDate = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.CCDPlus)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));
        assertEquals("Offload batch count", 1, offloadBatches.size());
        assertEquals("Offload batch status", OffloadBatchStatus.Completed, offloadBatches.getFirst().getStatusCd());
        assertEquals("CCDPlus Nachafile count with InProcess status", 1, nachaFilesForOffloadDate.size());
    }


    /**
     * Added this unit test to create data for verifying the SAP UI. It does not add any value apart from that, so marking this as ignore.
     */

    @Test
    public void testDataForSAPChange(){

        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2017, 5, 1, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(offloadDate, -achTaxOffloadOffset);

        testCreditDebitTotalsMismatchForCCDPlus();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> offloadBatches = Application.find(OffloadBatch.class,
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(offloadDate)
                                .And(OffloadBatch.OffloadGroup().equalTo(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.TAXPAYMENT)))));
        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class,
                new Query<NACHAFile>()
                        .Where(NACHAFile.OffloadBatch().equalTo(offloadBatches.getFirst())
                                .And(NACHAFile.FileType().equalTo(NACHAFileType.CCDPlus)
                                        .And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)))));

        nachaFiles.getFirst().setStatus(NACHAFileStatus.Transmitted);
        PayrollServices.commitUnitOfWork();

        System.out.println("Data created verify SAP now!!");
    }

}
