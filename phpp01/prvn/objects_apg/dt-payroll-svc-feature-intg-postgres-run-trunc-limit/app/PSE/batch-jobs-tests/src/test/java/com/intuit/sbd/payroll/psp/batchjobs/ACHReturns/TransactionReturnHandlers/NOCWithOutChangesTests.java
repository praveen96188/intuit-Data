package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.File;


public class NOCWithOutChangesTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServices.rollbackUnitOfWork();
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testIntuitNOCWithNoChange_PSRV001448() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.setPSPTime("20090803000000");
        PayrollServices.commitUnitOfWork();

        // create company
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c1dl = new Company3Dataloader();
        c1dl.persistCompany3();
        PayrollServices.commitUnitOfWork();

        // offload verification transactions
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // set the psp time to allow offload of payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090805000000");
        Application.commitUnitOfWork();

        // create payroll for same date
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRun1DTO = c1dl.getCompanyPR_DoesNotExceedLimits(new DateDTO("2009-08-07"));
        c1dl.persistPayrollRun(payrollRun1DTO);
        PayrollServices.commitUnitOfWork();

        // offload payroll
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // set the psp time to next day and process returns
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090806000000");
        Application.commitUnitOfWork();

        // find the edr trace numbers
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, "BatchTest87");

        TransactionType transactionType = PayrollServices.entityFinder.findById(TransactionType.class, TransactionTypeCode.EmployeeDdCredit);

        DomainEntitySet<FinancialTransaction> eeDdCredits =
                PayrollServices.entityFinder.find(FinancialTransaction.class,
                                                  FinancialTransaction.PayrollRun().equalTo(payrollRun1)
                                                          .And(FinancialTransaction.TransactionType().equalTo(transactionType)));

        MoneyMovementTransaction eeDDCreditMMT = null;
        for (FinancialTransaction eeDdCredit : eeDdCredits) {
            if ("343.00".equals(eeDdCredit.getFinancialTransactionAmount().toString())) {
                eeDDCreditMMT = eeDdCredit.getMoneyMovementTransaction();
            }
        }

        assertNotNull(eeDDCreditMMT);

        DomainEntitySet<EntryDetailRecord> edr =
                PayrollServices.entityFinder.find(EntryDetailRecord.class,
                                                  EntryDetailRecord.MoneyMovementTransaction().equalTo(eeDDCreditMMT)
                                                          .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));

        // create the returns file
        String trace1 = edr.get(0).getTraceNumber();
        String pad12 = "000000000000";
        String pad14 = pad12 + "00";

        String pr1record7 = "798C010#      1220006622345                              CHANGE256000000000000001";

        StringBuffer buf = new StringBuffer();
        String newline = System.getProperty("line.separator");
        buf.append("101 021000021972261600006121315551094101JPMORGAN CHASE         INTUIT                         ").append(newline);
        buf.append("5200INTUIT PAYROLL SERVICES    2628703131722616679CORINTUITPAYR090730090730   1000000000000001").append(newline);
        buf.append("62612200066122345            000000000011111111       INTUIT DIRECT DEPOSITS  1000000000000001").append(newline);
        buf.append(pr1record7.replaceFirst("#", pad14.substring(trace1.length()) + trace1)).append(newline);
        buf.append("82000000021234567890000000010000000000010000                                   000000000000001").append(newline);
        buf.append("9000002000001000000041234567890000000030000000000030000                                       ").append(newline);

        File retFile = new File(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_recv_dir"), "retfile.txt");
        Application.commitUnitOfWork();

        try {
            FileWriter fw = new FileWriter(retFile);
            fw.write(buf.toString());
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        new ReturnFileParser().processFile(retFile);

        DomainEntitySet<CompanyEvent> NOCList = CompanyEvent.findCompanyEvents
                (company, EventTypeCode.NOCWithOutChanges, CompanyEventStatus.Active, true);

        assertEquals("Number of NOCWithOutChanges events for payroll 1", NOCList.size(), 1);
    }

}
