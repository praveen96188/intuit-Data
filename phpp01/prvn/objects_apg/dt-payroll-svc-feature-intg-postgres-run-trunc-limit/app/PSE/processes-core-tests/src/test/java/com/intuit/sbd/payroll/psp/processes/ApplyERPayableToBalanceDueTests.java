package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * User: dweinberg
 * Date: 4/27/11
 * Time: 10:08 AM
 */
public class ApplyERPayableToBalanceDueTests {


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

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
    public void testApplyERPayableFromSamePayroll() {
        Company company = setupCompany();

        DataLoadServices.addEEs(company, 2, false, true);  //total of 4 emps so that numbers are more apparent

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 25, SpcfTimeZone.getLocalTimeZone()));
        voidAPaycheck(payrollRun);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxRedebit, TransactionTypeCode.EmployerFeeDebit);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, payrollRun,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -1100.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 1100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, -100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.applyERPayableToBalanceDue(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString(), new SpcfMoney("50.00")));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, payrollRun,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -1050.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 1050.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, -100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -150),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0)
        );

        Application.refresh(payrollRun);
        FinancialTransaction transferFT = assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Executed, TransactionTypeCode.ERPayableAppliedBalanceDue));
        assertEquals("50.00", transferFT.getFinancialTransactionAmount().toString());
        MoneyMovementTransaction transferMMT = transferFT.getMoneyMovementTransaction();
        assertEquals("50.00", transferMMT.getMoneyMovementTransactionAmount().toString());
        DataLoadServices.assertIntuitBankAccounts(transferFT, DataLoadServices.IntuitBankAccountType.Tax, DataLoadServices.IntuitBankAccountType.ER_Return);

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testApplyERPayableFromPreviousPayroll() {

        PayrollRun payrollRun = createERPayable();
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        Company company = Company.findCompany(payrollRun.getCompany().getSourceCompanyId(), payrollRun.getCompany().getSourceSystemCd());
        PayrollServices.rollbackUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, 
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 1100.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -1100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 400),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 150),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        assertSuccess(PayrollServices.payrollManager.applyERPayableToBalanceDue(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString(), new SpcfMoney("50.00")));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 1050.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -1050.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 350),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 150),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0)
        );
        PayrollServices.commitUnitOfWork();
    }

    public PayrollRun createERPayable() {
        Company company = setupCompany();
        DataLoadServices.addEEs(company, 2, false, true);  //total of 4 emps so that numbers are more apparent

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun0 = runPayroll(company, new DateDTO("2011-01-10"), "50");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        voidAPaycheck(payrollRun0);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxRedebit, TransactionTypeCode.EmployerFeeDebit);
        DataLoadServices.runOffload();
        return payrollRun;
    }

    @Test
    public void testApplyERPayableFromSameAndPreviousPayroll() {
        Company company = setupCompany();
        DataLoadServices.addEEs(company, 2, false, true);  //total of 4 emps so that numbers are more apparent

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun0 = runPayroll(company, new DateDTO("2011-01-10"), "50");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24,SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        voidAPaycheck(payrollRun0);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 25,SpcfTimeZone.getLocalTimeZone()));
        voidAPaycheck(payrollRun);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxRedebit, TransactionTypeCode.EmployerFeeDebit);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 400),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 1100.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -1100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 400),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -100),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 100)
        );
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        assertSuccess(PayrollServices.payrollManager.applyERPayableToBalanceDue(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString(), new SpcfMoney("100.00")));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 400),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 1000.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -1000.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 300),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -100),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0)
        );
        PayrollServices.commitUnitOfWork();
    }

    private Company setupCompany() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.claimNoFeesOffer(company);
        return company;
    }

    private PayrollRun runPayroll(Company company, DateDTO date, String amount) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, date, new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"61", "62", "63", "64", "66"}, new String[]{amount, amount, amount, amount, amount});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        return processResult.getResult();
    }

    public void voidAPaycheck(PayrollRun payrollRun) {
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> voidPaychecks = new ArrayList<String>();
        voidPaychecks.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(voidPaychecks);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, payrollRun.getCompany().getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();
    }



}
