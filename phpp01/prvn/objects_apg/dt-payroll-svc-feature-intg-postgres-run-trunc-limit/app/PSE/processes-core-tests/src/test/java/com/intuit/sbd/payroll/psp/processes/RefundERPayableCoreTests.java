package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
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

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jun 3, 2011
 * Time: 9:46:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class RefundERPayableCoreTests {


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

    @Test
    public void testHappyPathRefundERPayable(){
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

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();
        voidAPaycheck(payrollRun0);

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, payrollRun0,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 0),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 0),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 400),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 150),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        assertSuccess(PayrollServices.financialTransactionManager.refundERPayable(company.getSourceSystemCd(), company.getSourceCompanyId(), SettlementTypeDTO.ACH, new SpcfMoney("50.00")));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();
        
        PayrollServices.beginUnitOfWork();
        assertEquals("ER PayableRefund events", 1, CompanyEvent.findCompanyEvents(company, EventTypeCode.ERPayableRefundCreated).size());
        DataLoadServices.assertLedgerBalances(company, 
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 0),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 350),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 150),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0),
                new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 0)
        );

        DomainEntitySet<FinancialTransaction> erTaxCredits = FinancialTransaction.findFinancialTransaction(company, TransactionTypeCode.EmployerTaxCredit);
        assertEquals("ER Tax Credits", 1, erTaxCredits.size());
        assertEquals("ER Tax Credit amount", LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERPayable, payrollRun0.getSourcePayRunId(), company), erTaxCredits.get(0).getFinancialTransactionAmount());
        assertEquals("ER Tax Credit MMT amount", erTaxCredits.get(0).getFinancialTransactionAmount(), erTaxCredits.get(0).getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHappyPathRefundERPayableWithERReturnReceivable(){
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

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, payrollRun0,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, -200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -1100.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 1100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 400),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 150),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50)
        );
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> procResult = PayrollServices.financialTransactionManager.refundERPayable(company.getSourceSystemCd(), company.getSourceCompanyId(), SettlementTypeDTO.ACH, new SpcfMoney("50.00"));
        assertEquals("Error messages", 1, procResult.getErrorMessages().size());
        assertEquals("Error message code", "5000", procResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message", "Refund cannot be made with balance in ERReturnReceivable.  Apply ERPayable to balance due instead.", procResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRefundERPayableWithZeroERPayable(){
        Company company = setupCompany();
        DataLoadServices.addEEs(company, 2, false, true);  //total of 4 emps so that numbers are more apparent

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun0 = runPayroll(company, new DateDTO("2011-01-10"), "50");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 200),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0)
        );
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> procResult = PayrollServices.financialTransactionManager.refundERPayable(company.getSourceSystemCd(), company.getSourceCompanyId(), SettlementTypeDTO.ACH, new SpcfMoney("50.00"));
        assertEquals("Error messages", 1, procResult.getErrorMessages().size());
        assertEquals("Error message code", "5000", procResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message", "ERPayable balance must be positive.", procResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();        
    }

    /*
     PSRV002761
     Steps to Reproduce:
        1) Submit a payroll
        2) Offload the payroll debit
        3) Void all or part of the payroll
        4) Refund the outstanding balance in ERPayable
        5) Offload the refund
     */
    @Test
    public void testRefundSettlementDateCopiedFromImpound() {
        Company company = setupCompany();
        DataLoadServices.addEEs(company, 2, false, true);  //total of 4 emps so that numbers are more apparent

        DataLoadServices.setPSPDate(2011, 1, 10);
        PayrollRun payrollRun0 = runPayroll(company, new DateDTO("2011-01-10"), "50");
        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0),
                new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 1000),
                new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 1000)
        );
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 1000),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 1000)
        );
        PayrollServices.rollbackUnitOfWork();

        voidAPaycheck(payrollRun0);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 750),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 250),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 1000)
        );
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.refundERPayable(company.getSourceSystemCd(), company.getSourceCompanyId(), SettlementTypeDTO.ACH, new SpcfMoney("250.00")));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 18);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        assertEquals("ER PayableRefund events", 1, CompanyEvent.findCompanyEvents(company, EventTypeCode.ERPayableRefundCreated).size());
        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 0),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 750),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 750)
        );

        DomainEntitySet<FinancialTransaction> erTaxCredits = FinancialTransaction.findFinancialTransaction(company, TransactionTypeCode.EmployerTaxCredit);
        assertEquals("ER Tax Credits", 1, erTaxCredits.size());
        assertEquals("ER Tax Credit amount", LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERPayable, payrollRun0.getSourcePayRunId(), company), erTaxCredits.get(0).getFinancialTransactionAmount());
        assertEquals("ER Tax Credit MMT amount", erTaxCredits.get(0).getFinancialTransactionAmount(), erTaxCredits.get(0).getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    private Company setupCompany() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.claimNoFeesOffer(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);
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
