package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertContains;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

/**
 * User: dweinberg
 * Date: 4/11/11
 * Time: 11:54 AM
 */
public class VoidPayrollTaxPaymentTests {



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
    public void testExecutedERD_CreatedATC() {
        Company company = setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        assertContains(1055, MessageInfo.MessageLevel.ERROR, PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString()));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testReturnedERD_CreatedATC() {
        Company company = setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentVoided(payrollRun, "500.00", 5);

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testReturnedTwiceERD_CreatedATC() {
        Company company = setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxRedebit, TransactionTypeCode.EmployerFeeDebit);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentVoided(payrollRun, "500.00", 5);

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08)
        );
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void nocForEmployerTaxCreditApplied() {
        Company company = setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-01-24"), "50");
        DataLoadServices.runOffload(PSPDate.getPSPTime());

        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollToVoid = PayrollRun.findPayrollRuns(company).get(0);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollToVoid.getSourcePayRunId());
        List<String> paychecksToVoid = new ArrayList<String>();
        for (Paycheck paycheck : payrollToVoid.getPaycheckCollection()) {
            paychecksToVoid.add(paycheck.getSourcePaycheckId());
        }
        voidPayrollDTO.setPaycheckIdList(paychecksToVoid);

        PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-02-02"), "50");
        DataLoadServices.runOffload(PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.createNocForTxns(payrollRun, TransactionTypeCode.EmployerTaxCreditApplied);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC);
        assertEquals("NOC Event Count", 1, events.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testReturnedTwiceERD_CreatedAndExecutedATC() {
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

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentVoided(payrollRun, "100.00", 1);

        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 500.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -500.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100.00),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08)
        );

    }

    @Test
    public void testReturnedTwiceERD_CreatedAndExecutedATCAndATD() {
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
        assertSuccess(PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentVoided(payrollRun, "150.00", 1);
        assertPaymentATDsVoided(payrollRun, 1);

        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 950.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -950.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 50),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -200),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
    }

    @Test
    public void testAppliedCreditAndOverPaymentDebitReturnedTwiceERD_CreatedAndExecutedATCAndATD() {
        Company company = setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 3, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun voidedPayrollRun = runPayroll(company, new DateDTO("2011-01-5"), "10");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runACHTransactionProcessor();

        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 20, SpcfTimeZone.getLocalTimeZone()));

        voidAPaycheck(voidedPayrollRun);

        PayrollServices.beginUnitOfWork();
                DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 40),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 20),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -30),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 10)
        );
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 21, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-01-24"), "50");

        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxRedebit, TransactionTypeCode.EmployerFeeDebit);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentVoided(payrollRun, "100.00", 1);

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 450.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -450.08),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100.00),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 10),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 10)

        );
        PayrollServices.commitUnitOfWork();

    }



    //Test out liability increase and decrease on backdated paycheck. This is voided later:
    @Test
    public void testBackdatedPaycheckWithIncreaseAndDecreaseInLiabilty(){

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 10, 5));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 10, 26));
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-10-26"), emps, new String[]{"61", "62", "1"}, new String[]{"100", "100","50"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2011, 10, 25));
        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2011, 10, 26));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 10, 25));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2011, 10, 26));

        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 11, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 01, 03, SpcfTimeZone.getLocalTimeZone()));


        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
//Increase the liability amount
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-10-27"), emps, new String[]{ "61"}, new String[]{"90"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
//Decrease the liability amount
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-10-26"), emps, new String[]{ "61"}, new String[]{"-70"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

//Void entire payroll run older paycheck
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(voidProcessResult);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfDecimal atrAmount= LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company,LedgerAccountCode.AgencyTaxRefund);
        Assert.assertEquals("ATR amount", new SpcfMoney("230"),atrAmount);
        SpcfDecimal erpAmount= LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company,LedgerAccountCode.ERPayable);
        Assert.assertEquals("ERP amount",SpcfMoney.ZERO,erpAmount);
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

        DataLoadServices.claimOffer(company, "Waive all major fees");

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

    public static void assertPaymentVoided(PayrollRun payrollRun, String amount, int voidedATCs) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> ATCs = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit));
        assertEquals(0, ATCs.find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)).size());
        assertEquals(voidedATCs, ATCs.find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Voided)).size());

        for (MoneyMovementTransaction pendingPayment : Application.<MoneyMovementTransaction>find(MoneyMovementTransaction.class,
                MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.EFTPS, PaymentMethod.EFTPSDirectDebit)
                        .And(MoneyMovementTransaction.TaxPaymentStatus().notIn(TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency)))) {
            //only ones here should be from other payrolls
            for (FinancialTransaction pendingFT : pendingPayment.getFinancialTransactionCollection()) {
                PayrollRun associatedPayrollRun = pendingFT.getPayrollRun();
                assertNotSame(payrollRun, associatedPayrollRun);
                assertEquals("not a payment-voided payroll", 0, associatedPayrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer).size());
            }
        }


        FinancialTransaction transferFT = assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.IntuitTaxVoidTransfer));
        assertEquals(amount, transferFT.getFinancialTransactionAmount().toString());
        DataLoadServices.assertIntuitBankAccounts(transferFT, DataLoadServices.IntuitBankAccountType.Tax, DataLoadServices.IntuitBankAccountType.ER_Return);
        MoneyMovementTransaction transferMMT = transferFT.getMoneyMovementTransaction();
        assertEquals(amount, transferMMT.getMoneyMovementTransactionAmount().toString());

        CompanyEvent event = assertOne(CompanyEvent.findCompanyEvents(payrollRun.getCompany(), EventTypeCode.PayrollTaxPaymentVoided));
        assertEquals(payrollRun.getId().toString(), event.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId));
        assertEquals(amount, event.getCompanyEventDetailValue(EventDetailTypeCode.Amount));

        PayrollServices.rollbackUnitOfWork();
    }

    private void assertPaymentATDsVoided(PayrollRun payrollRun, int voidedATDs) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> ATDs = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit));
        assertEquals(0, ATDs.find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)).size());
        assertEquals(voidedATDs, ATDs.find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Voided)).size());

        PayrollServices.rollbackUnitOfWork();
    }


}
