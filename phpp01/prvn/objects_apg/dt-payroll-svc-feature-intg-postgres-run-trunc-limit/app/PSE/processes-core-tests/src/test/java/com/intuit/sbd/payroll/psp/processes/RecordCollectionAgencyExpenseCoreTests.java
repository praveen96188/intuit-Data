package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * User: dweinberg
 * Date: 4/15/11
 * Time: 10:00 AM
 */
public class RecordCollectionAgencyExpenseCoreTests {


    @Before
    public void before() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
    }

    @After
    public void after() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testRecordCollectionAgencyExpense() {


        new BadDebtTaxTests().testWriteOffBadDebtTax();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class, PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.WrittenOff)));
        Company company = payrollRun.getCompany();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 500),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -500),
                new DataLoadServices.LB(LedgerAccountCode.CollectionExpense, 0)
                );
        assertSuccess(PayrollServices.financialTransactionManager.recordCollectionAgencyExpense(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString(), new SpcfMoney("42.17"), new DateDTO("2011-05-02")));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> expenseFTs = payrollRun.getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Completed), TransactionType.findTransactionType(TransactionTypeCode.ThirdPartyCollectionExpense));
        assertEquals("Number of ThirdPartyCollectionExpense FTs", 1, expenseFTs.size());
        assertEquals("ThirdPartyCollectionExpense FT Amount", new SpcfMoney("42.17"), expenseFTs.get(0).getFinancialTransactionAmount());
        assertEquals("ThirdPartyCollectionExpense FT Settlement Type", SettlementType.CheckType, expenseFTs.get(0).getSettlementTypeCd());
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 500),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -542.17),
                new DataLoadServices.LB(LedgerAccountCode.CollectionExpense, 42.17)
                );
        PayrollServices.rollbackUnitOfWork();

    }
}
