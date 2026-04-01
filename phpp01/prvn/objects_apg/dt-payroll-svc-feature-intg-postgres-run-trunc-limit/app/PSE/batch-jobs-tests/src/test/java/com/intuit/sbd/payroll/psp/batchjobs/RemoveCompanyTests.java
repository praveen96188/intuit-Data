package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: May 1, 2008
 * Time: 11:17:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveCompanyTests {

    private static Company1Dataloader c1dl;

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
//        loadDataHappyPath();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }
    @Test
    public void deleteCompany() throws Exception {
        Application.initialize();
        ApplicationSecondary.initialize();
        loadDataHappyPath();
        Application.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                                                     c1dl.getCompany().getSourceSystemCd());
        Application.deleteCompany(company.getId().toString());
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                                                     c1dl.getCompany().getSourceSystemCd());
        assertEquals("Company Removed", null, company);
        Application.commitUnitOfWork();
    }

    /**
     * Methods to load data
    */
    private static void loadDataHappyPath() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        persistCompany1();
        addCompany1Payroll2();
        PayrollServices.commitUnitOfWork();

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
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // create an ER return for company1 first payroll
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                                                     c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[] {TransactionStateCode.Executed});
        assertEquals("Number of Company1 ERDDDB EX txns", 1, c1FinTxns.size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R02", "This is an ER Return");

        // Add write off bad debt for ER Tx of company1 payroll1
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                c1dl.getCompany().getSourceSystemCd(),
                c1dl.getCompany().getSourceCompanyId(), "BatchTest05");

        PayrollServices.commitUnitOfWork();
        assertTrue("Process Result", processResult.isSuccess());
    }

    private static void persistCompany1() {
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        if (payChecks.iterator().hasNext()) {
            PaycheckDTO firstPayCheck = payChecks.iterator().next();
            firstPayCheck.getDdTransactions().iterator().next().setDDTransactionAmount(new BigDecimal("200.00"));
        }
        c1dl.persistPayrollRun(payrollRunDTO);
    }

    private static void addCompany1Payroll2() {
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-10-10"));
        c1dl.persistPayrollRun(payrollRunDTO);
    }
}
