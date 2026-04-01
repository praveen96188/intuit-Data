package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryFinancialTransactionsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryFinancialTransactionsResponseDISDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import org.junit.Before;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryFinancialTransactionsTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryFinancialTransactionsTests {

    private static PayrollRun payrollRun1;

    @Before
    public void loadDataHappyPath() {

        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2, false, true);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()));
        payrollRun1 = runPayroll(company, new DateDTO("2011-02-06"), "1");
        DataLoadServices.runOffload();
        DataLoadServices.runACHTransactionProcessor(5);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 25, SpcfTimeZone.getLocalTimeZone()));
    }

    @Ignore
    @Test
    public void testSingleTransaction() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryFinancialTransactionsRequestDISDTO requestDISDTO = new QueryFinancialTransactionsRequestDISDTO();
            List<String> transactionIds = new ArrayList<String>();
            DomainEntitySet<FinancialTransaction> financialTransactions = payrollRun1.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit);
            transactionIds.add(financialTransactions.get(0).getId().toString());
            requestDISDTO.setTransactionIds(transactionIds);

            QueryFinancialTransactionsResponseDISDTO responseDISDTO = disAdapter.Query_FinancialTransactions(requestDISDTO);

            Assert.assertEquals(1, responseDISDTO.getFinancialTransactionDISDTOs().size());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Ignore
    @Test
    public void testMultipleTransactions() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryFinancialTransactionsRequestDISDTO requestDISDTO = new QueryFinancialTransactionsRequestDISDTO();
            List<String> transactionIds = new ArrayList<String>();
            DomainEntitySet<FinancialTransaction> financialTransactions = payrollRun1.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit);
            if (financialTransactions.size() < 4) {
                Assert.fail("Cannot run this test as there are not enough financial transactions");
            }
            transactionIds.add(financialTransactions.get(0).getId().toString());
            transactionIds.add(financialTransactions.get(1).getId().toString());
            transactionIds.add(financialTransactions.get(2).getId().toString());
            transactionIds.add(financialTransactions.get(3).getId().toString());
            requestDISDTO.setTransactionIds(transactionIds);

            QueryFinancialTransactionsResponseDISDTO responseDISDTO = disAdapter.Query_FinancialTransactions(requestDISDTO);

            Assert.assertEquals(4, responseDISDTO.getFinancialTransactionDISDTOs().size());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Ignore
    @Test
    public void testMultipleWithInvalid() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryFinancialTransactionsRequestDISDTO requestDISDTO = new QueryFinancialTransactionsRequestDISDTO();
            List<String> transactionIds = new ArrayList<String>();
            DomainEntitySet<FinancialTransaction> financialTransactions = payrollRun1.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit);
            if (financialTransactions.size() < 4) {
                Assert.fail("Cannot run this test as there are not enough financial transactions");
            }
            transactionIds.add(financialTransactions.get(0).getId().toString());
            transactionIds.add("This is invalid");
            requestDISDTO.setTransactionIds(transactionIds);

            QueryFinancialTransactionsResponseDISDTO responseDISDTO = disAdapter.Query_FinancialTransactions(requestDISDTO);

            TestHelper.verifyDISResponse(DISMessages.objectNotFound("SAPPayrollTransaction", "This is invalid"),responseDISDTO.getDisResponse());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testNoTransactions() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryFinancialTransactionsRequestDISDTO requestDISDTO = new QueryFinancialTransactionsRequestDISDTO();
            QueryFinancialTransactionsResponseDISDTO responseDISDTO = disAdapter.Query_FinancialTransactions(requestDISDTO);

            Assert.assertEquals(0, responseDISDTO.getFinancialTransactionDISDTOs().size());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testInvalidTransaction() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryFinancialTransactionsRequestDISDTO requestDISDTO = new QueryFinancialTransactionsRequestDISDTO();
            List<String> transactionIds = new ArrayList<String>();
            transactionIds.add("This is an invalid id");
            requestDISDTO.setTransactionIds(transactionIds);
            QueryFinancialTransactionsResponseDISDTO responseDISDTO = disAdapter.Query_FinancialTransactions(requestDISDTO);

            TestHelper.verifyDISResponse(DISMessages.objectNotFound("SAPPayrollTransaction", "This is an invalid id"),responseDISDTO.getDisResponse());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testNullTransaction() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryFinancialTransactionsRequestDISDTO requestDISDTO = new QueryFinancialTransactionsRequestDISDTO();
            List<String> transactionIds = new ArrayList<String>();
            transactionIds.add(null);
            requestDISDTO.setTransactionIds(transactionIds);
            QueryFinancialTransactionsResponseDISDTO responseDISDTO = disAdapter.Query_FinancialTransactions(requestDISDTO);

            Assert.assertEquals(0, responseDISDTO.getFinancialTransactionDISDTOs().size());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    private static PayrollRun runPayroll(Company company, DateDTO date, String amount) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, date, new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"6", "67", "87", "142", "61", "62", "63", "64", "66", "1"}, new String[]{amount, amount, amount, amount, amount, amount, amount, amount, amount, amount});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        return processResult.getResult();
    }
}
