package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.exceptions.MoneyMovementControlException;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoneyMovementControlTests {
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
        PayrollServicesTest.afterEachTest();
    }

    @Test(expected = MoneyMovementControlException.class)
    public void testFinancialTransactionsCreateExceedLimits() {
        try {
            PayrollServices.beginUnitOfWork();
            PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
            PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
            PayrollServices.commitUnitOfWork();

            Application.beginUnitOfWork();

            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                    "123272727", payrollRunDTO);

            Application.commitUnitOfWork();

            // Ensure processing was succsessful
            assertTrue("Process Result", processResult.isSuccess());

            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(
                    "123272727", SourceSystemCode.QBOE);

            PayrollRun payrollRun = PayrollRun.
                    findPayrollRun(company, "BatchId01");

            DomainEntitySet<FinancialTransaction> financialTransactions =
                    payrollRun.getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                            new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("Number of Financial Transactions", 1, financialTransactions.size());

            FinancialTransaction finTxn = financialTransactions.get(0);

            // Create FT with amount that breaches the limit. RuntimeException should be thrown
            FinancialTransaction financialTx =
                    FinancialTransaction.createFinancialTransaction(finTxn.getCompany(),
                            finTxn.getPayrollRun(),
                            null,
                            finTxn.getCreditBankAccount(),
                            finTxn.getDebitBankAccount(),
                            finTxn.getCreditBankAccountType(),
                            finTxn.getDebitBankAccountType(),
                            finTxn.getTransactionType().getTransactionTypeCd(),
                            new SpcfMoney("11000000.00"),
                            finTxn.getSettlementTypeCd(),
                            finTxn.getSettlementDate().toLocal());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            Assert.assertEquals("Financial Transaction Amount 11000000 Breached The Allowed Threshold Amount. psid=123272727" , e.getMessage());
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        Assert.fail("RuntimeException should be thrown for FT amount breach");
    }

    @Test
    public void testFinancialTransactionsCreateExceedLimitsWire() {

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Financial Transactions", 1, financialTransactions.size());

        FinancialTransaction finTxn = financialTransactions.get(0);

        // Create FT with amount that breaches the limit for Wire Settlement Type. Should not throw exception
        FinancialTransaction financialTx =
                FinancialTransaction.createFinancialTransaction(finTxn.getCompany(),
                        finTxn.getPayrollRun(),
                        null,
                        finTxn.getCreditBankAccount(),
                        finTxn.getDebitBankAccount(),
                        finTxn.getCreditBankAccountType(),
                        finTxn.getDebitBankAccountType(),
                        finTxn.getTransactionType().getTransactionTypeCd(),
                        new SpcfMoney("11000000.00"),
                        SettlementType.Wire,
                        finTxn.getSettlementDate().toLocal());
        PayrollServices.commitUnitOfWork();

    }

    @Test(expected = MoneyMovementControlException.class)
    public void testFinancialTransactionsUpdate() {

        try {
            PayrollServices.beginUnitOfWork();
            PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
            PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
            PayrollServices.commitUnitOfWork();

            Application.beginUnitOfWork();

            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                    "123272727", payrollRunDTO);

            Application.commitUnitOfWork();

            // Ensure processing was succsessful
            assertTrue("Process Result", processResult.isSuccess());

            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(
                    "123272727", SourceSystemCode.QBOE);

            PayrollRun payrollRun = PayrollRun.
                    findPayrollRun(company, "BatchId01");

            DomainEntitySet<FinancialTransaction> financialTransactions =
                    payrollRun.getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                            new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("Number of Financial Transactions", 1, financialTransactions.size());

            FinancialTransaction finTxn = financialTransactions.get(0);
            PayrollServices.commitUnitOfWork();

            // update the existing FT to breach the limit
            PayrollServices.beginUnitOfWork();
            finTxn = Application.findById(FinancialTransaction.class, finTxn.getId());
            finTxn.setFinancialTransactionAmount(new SpcfMoney("12000000.00"));
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            Assert.assertEquals("Financial Transaction Amount 12000000 Breached The Allowed Threshold Amount. psid=123272727" , e.getMessage());
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        Assert.fail("RuntimeException should be thrown for FT amount breach");
    }

    @Test
    public void testFinancialTransactionsReadHavingLimitsBreach() {

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Financial Transactions", 1, financialTransactions.size());

        FinancialTransaction finTxn = financialTransactions.get(0);
        PayrollServices.commitUnitOfWork();

        // update the amount that breaches the limit via hibernate query i.e there will be no limit check
        PayrollServices.beginUnitOfWork();
        String update ="update com.intuit.sbd.payroll.psp.domain.FinancialTransaction ft set ft.FinancialTransactionAmount = '12000000.00' where ft.Id='" + finTxn.getId().toString() + "'";
        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(update);
        hibernateQuery.executeUpdate();
        PayrollServices.commitUnitOfWork();

        // read the existing FT that has amount breached and should not cause any limit check exception
        PayrollServices.beginUnitOfWork();
        finTxn = Application.findById(FinancialTransaction.class, finTxn.getId());
        assertEquals(12000000L, finTxn.getFinancialTransactionAmount().getIntegerPart());
        PayrollServices.commitUnitOfWork();
    }

}
