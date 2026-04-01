package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * User: dweinberg
 * Date: 5/5/11
 * Time: 9:27 AM
 * @see AddWriteOffBadDebtTransactionTests
 * @see AddRecoverBadDebtTransactionTests
 * These were split from there because of Sales Tax Gateway fake/real conflicts
 */
public class BadDebtTaxTests {


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /*
     Write off tests
    */

    @Test
    public void testWriteOffBadDebtTax() {
        Company company = setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxRedebit, TransactionTypeCode.EmployerFeeDebit);

        Application.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId()));
        Application.commitUnitOfWork();

        assertPayrollWrittenOff(payrollRun);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 600.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -600.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100.00),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08)
        );
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -500),
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 500)
        );
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testCannotWriteOffIfVoidRequired() {
        Company company = setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxRedebit, TransactionTypeCode.EmployerFeeDebit);

        Application.beginUnitOfWork();
        PSP_PRAssert.assertContains(5000, MessageInfo.MessageLevel.ERROR, PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId()));
        Application.commitUnitOfWork();
    }

    @Test
    public void testCannotWriteOffIfERPayableAvailable() {
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

        Application.beginUnitOfWork();
        PSP_PRAssert.assertContains(5000, MessageInfo.MessageLevel.ERROR, PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId()));
        Application.commitUnitOfWork();
    }

    @Test
    public void testCannotWriteOffIfERPayableFromPriorPayrollAvailable() {
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

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, payrollRun0,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        DataLoadServices.assertLedgerBalances(company, payrollRun,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -1100.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 1100.08),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, -100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0)
        );        
        PayrollServices.rollbackUnitOfWork();
        
        Application.beginUnitOfWork();
        PSP_PRAssert.assertContains(5000, MessageInfo.MessageLevel.ERROR, PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId()));
        Application.commitUnitOfWork();

    }

    @Test
    public void testWriteOffAfterTaxPaymentVoid() {
        new VoidPayrollTaxPaymentTests().testReturnedTwiceERD_CreatedAndExecutedATC();

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 1, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class));
        Company company = payrollRun.getCompany();

        assertSuccess(PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId()));
        Application.commitUnitOfWork();

        assertPayrollWrittenOff(payrollRun);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 500.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -500.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08)
        );
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -400),
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 400)
        );
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testWriteOffAfterERPayableAppliedAndVoid() {
        new ApplyERPayableToBalanceDueTests().testApplyERPayableFromSamePayroll();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class));
        Company company = payrollRun.getCompany();

        assertSuccess(PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        assertSuccess(PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId()));
        Application.commitUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -800),
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 800),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -200)
        );
        PayrollServices.rollbackUnitOfWork();
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
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, date, new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"61", "62", "63", "64", "65"}, new String[]{amount, amount, amount, amount, amount});
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


    private void assertPayrollWrittenOff(PayrollRun payrollRun) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertEquals(PayrollStatus.WrittenOff, payrollRun.getPayrollRunStatus());

        assertEquals(1, payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.EmployerWriteOffTax).size());
        assertEquals(1, payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.EmployerWriteOffFee).size());
        PayrollServices.rollbackUnitOfWork();
    }

    /*
     Recovery tests
    */

    @Test
    public void testWriteOffBadDebtTax_RecoverCollections() {
        testWriteOffBadDebtTax();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 5, SpcfTimeZone.getLocalTimeZone()));

        Application.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class, PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.WrittenOff)));
        Company company = payrollRun.getCompany();

        recoverTax(payrollRun, SettlementTypeDTO.Cash, "500.00", "100.00", "0.08", false);

        Application.commitUnitOfWork();
        
        assertBadDebtRecovery(payrollRun, "500.00", "100.00", "0.08", false, false);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, 0),
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 0)
        );
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testWriteOffBadDebtTax_RecoverCustomer() {
        testWriteOffBadDebtTax();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 5, SpcfTimeZone.getLocalTimeZone()));

        Application.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class, PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.WrittenOff)));
        Company company = payrollRun.getCompany();

        recoverTax(payrollRun, SettlementTypeDTO.ACH, "500.00", "100.00", "0.08", true);

        Application.commitUnitOfWork();
        
        assertBadDebtRecovery(payrollRun, "500.00", "100.00", "0.08", true, true);

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -500),
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 500)
        );
        PayrollServices.rollbackUnitOfWork();

        List<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        DomainEntitySet<FinancialTransaction> redebitFTs = payrollRun.getFinancialTransactions(TransactionTypeCode.ServiceSalesAndUseTax, TransactionTypeCode.EmployerFeeDebit);
        for (FinancialTransaction redebitFT : redebitFTs) {
            redebitImpoundDTOs.add(new RedebitImpoundDTO(redebitFT.getId().toString(), SpcfMoney.ZERO, new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire));
        }
        redebitImpoundDTOs.add(new RedebitImpoundDTO(payrollRun.getEmployerTaxDebitTransaction().getId().toString(), new SpcfMoney("600.08"), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire));
        assertSuccess(PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), redebitImpoundDTOs));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -500),
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 500),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 600.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -600.08)
        );
        PayrollServices.rollbackUnitOfWork();
        
    }

    @Test
    public void testWriteOffAfterTaxPaymentVoid_RecoverCollections() {
        testWriteOffAfterTaxPaymentVoid();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 5, SpcfTimeZone.getLocalTimeZone()));

        Application.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class, PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.WrittenOff)));
        Company company = payrollRun.getCompany();

        recoverTax(payrollRun, SettlementTypeDTO.Cash, "400.00", "100.00", "0.08", false);

        Application.commitUnitOfWork();

        assertBadDebtRecovery(payrollRun, "400.00", "100.00", "0.08", false, false);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, 0),
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 0)
        );
        PayrollServices.rollbackUnitOfWork();       
    }

    @Test
    public void testWriteOffAfterERPayableAppliedAndVoid_RecoverCustomer() {
        testWriteOffAfterERPayableAppliedAndVoid();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 5, SpcfTimeZone.getLocalTimeZone()));

        Application.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class, PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.WrittenOff)));
        Company company = payrollRun.getCompany();

        recoverTax(payrollRun, SettlementTypeDTO.Cash, "800.00", "100.00", "0.08", false);

        Application.commitUnitOfWork();

        assertBadDebtRecovery(payrollRun, "800.00", "100.00", "0.08", false, false);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, 0),
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 0),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -200)
        );
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testVoidApplyERPayableWriteOffRecoverCustomerReissue() throws Throwable {
        //this test covers the gambit of new functionality
        /*
        1. submit payroll 1
        2. offload debit
        3. offload 941 payments
        4. submit payroll 2
        5. offload debit
        6. offload 941 payments
        7. void paycheck from payroll 1
        8. return debit
        9. return redebit, fee
        10. void 940 payment
        11. apply er payable to balance due from both voids
        12. write off bad debt
        13. recover bad debt, customer
        14. record payment transaction
        15. reissue payment
        */

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

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        assertSuccess(PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString()));
        PayrollServices.commitUnitOfWork();

        VoidPayrollTaxPaymentTests.assertPaymentVoided(payrollRun, "200.00", 1);

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, payrollRun0,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        DataLoadServices.assertLedgerBalances(company, payrollRun,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -900.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 900.08),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, -100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 0),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 0),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0)
        );
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        assertSuccess(PayrollServices.payrollManager.applyERPayableToBalanceDue(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString(), new SpcfMoney("50.00")));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, payrollRun0,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        DataLoadServices.assertLedgerBalances(company, payrollRun,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -850.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 850.08),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, -100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 50),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 0),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, -50)
        );
        PayrollServices.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        assertSuccess(PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId()));
        Application.commitUnitOfWork();

        assertPayrollWrittenOff(payrollRun);

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, payrollRun0,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        DataLoadServices.assertLedgerBalances(company, payrollRun,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, 750),                
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, -750),                
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, -50)
        );
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Application.refresh(payrollRun);
        recoverTax(payrollRun, SettlementTypeDTO.ACH, "750.00", "100.00", "0.08", true);
        PayrollServices.commitUnitOfWork();

        assertBadDebtRecovery(payrollRun, "750.00", "100.00", "0.08", true, true);

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 7, SpcfTimeZone.getLocalTimeZone()));

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        List<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> redebitFTs = payrollRun.getFinancialTransactions(TransactionTypeCode.ServiceSalesAndUseTax, TransactionTypeCode.EmployerFeeDebit);
        for (FinancialTransaction redebitFT : redebitFTs) {
            redebitImpoundDTOs.add(new RedebitImpoundDTO(redebitFT.getId().toString(), SpcfMoney.ZERO, new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire));
        }
        redebitImpoundDTOs.add(new RedebitImpoundDTO(payrollRun.getEmployerTaxDebitTransaction().getId().toString(), new SpcfMoney("850.08"), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire));
        assertSuccess(PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), redebitImpoundDTOs));        
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffload();                
        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, 0),
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.BadDebt, 0),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 150),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50)
        );
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        assertSuccess(PayrollServices.payrollManager.reissuePayrollTaxPayment(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRun.getSourcePayRunId(),
                assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer)).getId().toString()));
        PayrollServices.commitUnitOfWork();

        ReissuePayrollTaxPaymentTests.assertPaymentReissued(payrollRun, "200.00", 1);

        DataLoadServices.runOffload();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, 0),
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -200),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 350),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 150)
        );
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testReturnCancelsPendingRefund() {
        Company company = setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 26, SpcfTimeZone.getLocalTimeZone()));

        voidAPaycheck(payrollRun);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.refundERPayable(company.getSourceSystemCd(), company.getSourceCompanyId(), SettlementTypeDTO.ACH, new SpcfMoney("50.00")));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction refund = assertOne(company.getFinancialTransactions().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxCredit)));
        assertEquals(SpcfCalendar.createInstance(2011, 2, 1, SpcfTimeZone.getLocalTimeZone()), refund.getSettlementDate().toLocal());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffload();

        // running the missed payroll processor one day after to account for daylight savings time.
        // The query uses the time in the database, so it will fail for half of the year.
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        new ProcessMissedACHTransactions().process("20110201");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        refund = Application.refresh(refund);
        //This transaction will not be cancelled because its a EmployerTaxTransaction
        assertEquals(TransactionStateCode.Created, refund.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.commitUnitOfWork();

    }

    private void recoverTax(PayrollRun payrollRun, SettlementTypeDTO settlementType, String badDebtAmount, String feeAmount, String salesTaxAmount, boolean isCustomer) {
        Company company = payrollRun.getCompany();


        if (badDebtAmount != null) {
            BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
            badDebtRecoverDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            badDebtRecoverDTO.setSettlementType(settlementType);
            badDebtRecoverDTO.setFinancialTxAmt(new SpcfMoney(badDebtAmount));
            badDebtRecoverDTO.setOriginalTransactionId(AddRecoverBadDebtTransactionTests.getOrigFtId(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId(), TransactionTypeCode.EmployerTaxDebit));
            badDebtRecoverDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
            badDebtRecoverDTO.setCustomer(isCustomer);

            assertSuccess(PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), badDebtRecoverDTO));
        }

        if (feeAmount != null) {
            BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
            badDebtRecoverDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            badDebtRecoverDTO.setSettlementType(settlementType);
            badDebtRecoverDTO.setFinancialTxAmt(new SpcfMoney(feeAmount));
            badDebtRecoverDTO.setOriginalTransactionId(AddRecoverBadDebtTransactionTests.getOrigFtId(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId(), TransactionTypeCode.EmployerFeeDebit));
            badDebtRecoverDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
            badDebtRecoverDTO.setCustomer(isCustomer);

            assertSuccess(PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), badDebtRecoverDTO));
        }

        if (salesTaxAmount != null) {
            BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
            badDebtRecoverDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            badDebtRecoverDTO.setSettlementType(settlementType);
            badDebtRecoverDTO.setFinancialTxAmt(new SpcfMoney(salesTaxAmount));
            badDebtRecoverDTO.setOriginalTransactionId(AddRecoverBadDebtTransactionTests.getOrigFtId(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId(), TransactionTypeCode.ServiceSalesAndUseTax));
            badDebtRecoverDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
            badDebtRecoverDTO.setCustomer(isCustomer);

            assertSuccess(PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), badDebtRecoverDTO));
        }
    }

    private void assertBadDebtRecovery(PayrollRun payrollRun, String badDebtAmount, String feeAmount, String salesTaxAmount, boolean validateMMT, boolean isCustomer){
        TransactionType badDbtTransactionType;
        TransactionType badDbtFeeTransactionType;
        TransactionType badDbtTaxTransactionType;
        if(isCustomer){
            badDbtTransactionType = TransactionType.findTransactionType(TransactionTypeCode.BadDebtCustomerRecoveryTax);
            badDbtFeeTransactionType = TransactionType.findTransactionType(TransactionTypeCode.BadDebtCustomerRecoveryFee);
            badDbtTaxTransactionType = TransactionType.findTransactionType(TransactionTypeCode.BadDebtCustomerRecoverySalesAndUseTax);
        } else {
            badDbtTransactionType = TransactionType.findTransactionType(TransactionTypeCode.BadDebtRecoveryTax);
            badDbtFeeTransactionType = TransactionType.findTransactionType(TransactionTypeCode.BadDebtRecoveryFee);
            badDbtTaxTransactionType = TransactionType.findTransactionType(TransactionTypeCode.BadDebtRecoverySalesAndUseTax);
        }
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> badDbtFTs = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().equalTo(badDbtTransactionType));
        assertEquals("Bad Debit FTs", 1, badDbtFTs.size());
        assertEquals("Bad Debit FT amount", new SpcfMoney(badDebtAmount), badDbtFTs.get(0).getFinancialTransactionAmount());

        DomainEntitySet<FinancialTransaction> badDbtFeeFTs = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().equalTo(badDbtFeeTransactionType));
        assertEquals("Bad Debit Fee FTs", 1, badDbtFeeFTs.size());
        assertEquals("Bad Debit Fee FT amount", new SpcfMoney(feeAmount), badDbtFeeFTs.get(0).getFinancialTransactionAmount());
        assertEquals("Bad Debit Fee FT amount", badDbtFeeFTs.get(0).getFinancialTransactionAmount(), badDbtFeeFTs.get(0).getOriginalTransaction().getFinancialTransactionAmount());

        DomainEntitySet<FinancialTransaction> badDbtTaxFTs = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().equalTo(badDbtTaxTransactionType));
        assertEquals("Bad Debit Sales FTs", 1, badDbtTaxFTs.size());
        assertEquals("Bad Debit Sales FT amount", new SpcfMoney(salesTaxAmount), badDbtTaxFTs.get(0).getFinancialTransactionAmount());
        assertEquals("Bad Debit Sales FT amount", badDbtTaxFTs.get(0).getFinancialTransactionAmount(), badDbtTaxFTs.get(0).getOriginalTransaction().getFinancialTransactionAmount());

        if(validateMMT){
            assertEquals("Bad Debit MMT amount", badDbtFTs.get(0).getFinancialTransactionAmount(), badDbtFTs.get(0).getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
            assertEquals("Bad Debit Fee MMT amount", badDbtFeeFTs.get(0).getFinancialTransactionAmount(), badDbtFeeFTs.get(0).getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
            assertEquals("Bad Debit Sales MMT amount", badDbtTaxFTs.get(0).getFinancialTransactionAmount(), badDbtTaxFTs.get(0).getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
            DataLoadServices.assertIntuitBankAccounts(badDbtFTs.get(0), DataLoadServices.IntuitBankAccountType.ER_Return, DataLoadServices.IntuitBankAccountType.Fee);
            DataLoadServices.assertIntuitBankAccounts(badDbtFeeFTs.get(0), DataLoadServices.IntuitBankAccountType.ER_Return, DataLoadServices.IntuitBankAccountType.Fee);
            DataLoadServices.assertIntuitBankAccounts(badDbtTaxFTs.get(0), DataLoadServices.IntuitBankAccountType.ER_Return, DataLoadServices.IntuitBankAccountType.Fee);
        } else {
            assertNull("MMT is Null", badDbtFTs.get(0).getMoneyMovementTransaction());
            assertNull("FT Credit bank account is Null", badDbtFTs.get(0).getCreditBankAccount());
            assertNull("FT Debit bank account is Null", badDbtFTs.get(0).getDebitBankAccount());
            assertNull("MMT is Null", badDbtFeeFTs.get(0).getMoneyMovementTransaction());
            assertNull("FT Credit bank account is Null", badDbtFeeFTs.get(0).getCreditBankAccount());
            assertNull("FT Debit bank account is Null", badDbtFeeFTs.get(0).getDebitBankAccount());
            assertNull("MMT is Null", badDbtTaxFTs.get(0).getMoneyMovementTransaction());
            assertNull("FT Credit bank account is Null", badDbtTaxFTs.get(0).getCreditBankAccount());
            assertNull("FT Debit bank account is Null", badDbtTaxFTs.get(0).getDebitBankAccount());
        }
        PayrollServices.rollbackUnitOfWork();

    }
}
