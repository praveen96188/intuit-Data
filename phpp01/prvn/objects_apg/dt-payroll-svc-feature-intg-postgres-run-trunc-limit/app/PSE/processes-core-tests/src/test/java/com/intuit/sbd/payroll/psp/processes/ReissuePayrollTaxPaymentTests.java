package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertNull;

/**
 * User: dweinberg
 * Date: 4/26/11
 * Time: 12:51 PM
 */
public class ReissuePayrollTaxPaymentTests {


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
    public void testReturnedERD_CreatedATC_reissue() {
        new VoidPayrollTaxPaymentTests().testReturnedERD_CreatedATC();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class));
        Company company = payrollRun.getCompany();

        assertSuccess(PayrollServices.payrollManager.reissuePayrollTaxPayment(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRun.getSourcePayRunId(),
                assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer)).getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentReissued(payrollRun, "500.00", 5);

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 0),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 0)
        );

        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 500),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 500),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 500),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -500)
        );
    }

    @Test
    public void testReturnedTwiceERD_CreatedATC_newPayroll_reissue() {
        new VoidPayrollTaxPaymentTests().testReturnedTwiceERD_CreatedATC();

        //Wire outstanding balance and remove Risk Assessment hold
        Application.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class));
        Company company = payrollRun.getCompany();
        DomainEntitySet<FinancialTransaction> returnedERTaxRedebitTransactions = FinancialTransaction.findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(),
                TransactionTypeCode.EmployerTaxRedebit, TransactionStateCode.Returned);
        DomainEntitySet<FinancialTransaction> returnedERFeeDebitTransactions = FinancialTransaction.findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(),
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Returned);
        DomainEntitySet<FinancialTransaction> returnedSalesTaxDebitTransactions = FinancialTransaction.findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(),
                TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Returned);

        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(returnedERTaxRedebitTransactions.get(0).getId().toString(),
                new SpcfMoney("500.00"),
                new DateDTO("2011-01-31"),
                SettlementTypeDTO.Wire);
        allRedebits.add(currRedebitImpoundDTO);
        RedebitImpoundDTO currFeeDebitImpoundDTO = new RedebitImpoundDTO(returnedERFeeDebitTransactions.get(0).getId().toString(),
                new SpcfMoney("100.00"),
                new DateDTO("2011-01-31"),
                SettlementTypeDTO.Wire);
        allRedebits.add(currFeeDebitImpoundDTO);
        RedebitImpoundDTO currSalesTaxDebitImpoundDTO = new RedebitImpoundDTO(returnedSalesTaxDebitTransactions.get(0).getId().toString(),
                new SpcfMoney("0.08"),
                new DateDTO("2011-01-31"),
                SettlementTypeDTO.Wire);
        allRedebits.add(currSalesTaxDebitImpoundDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(),
                company.getSourceCompanyId(), allRedebits);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();       

        //offload
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone()));

        //Submit new payroll
        PayrollRun payrollRun2 = runPayroll(company, new DateDTO("2011-02-15"), "10");

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.reissuePayrollTaxPayment(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRun.getSourcePayRunId(),
                assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer)).getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentReissued(payrollRun, "500.00", 5);

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -500),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 500),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 100),
                new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 100)
        );

        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 600),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 600)
        );
    }

    @Test
    public void testReturnedERD_CreatedATC_reissueIntoPriorQuarter() {
        new VoidPayrollTaxPaymentTests().testReturnedERD_CreatedATC();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 15, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class));
        Company company = payrollRun.getCompany();

        assertSuccess(PayrollServices.payrollManager.reissuePayrollTaxPayment(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRun.getSourcePayRunId(),
                assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer)).getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentReissued(payrollRun, "500.00", 5);

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 0),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 0)
        );

        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 500),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 500),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 500),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -500)
        );
    }



    @Test
    public void testReturnedTwiceERD_CreatedATC_reissue() {
        new VoidPayrollTaxPaymentTests().testReturnedTwiceERD_CreatedATC();

        DataLoadServices.runOffload();
        
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class));
        Company company = payrollRun.getCompany();

        assertSuccess(PayrollServices.payrollManager.reissuePayrollTaxPayment(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRun.getSourcePayRunId(),
                assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer)).getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentReissued(payrollRun, "500.00", 5);

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08)
        );

        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 500),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 500),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 600.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -600.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08)
        );
    }

    @Test
    public void testReturnedTwiceERD_CreatedAndExecutedATC_reissue() {
        new VoidPayrollTaxPaymentTests().testReturnedTwiceERD_CreatedAndExecutedATC();

        DataLoadServices.runOffload();
        
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class));
        Company company = payrollRun.getCompany();

        assertSuccess(PayrollServices.payrollManager.reissuePayrollTaxPayment(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRun.getSourcePayRunId(),
                assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer)).getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentReissued(payrollRun, "100.00", 1);

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 500.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -500.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08)
        );

        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 100),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 100),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 600.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -600.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08)
        );
    }

    @Test
    public void testReturnedTwiceERD_CreatedAndExecutedATCAndATD_reissue() {
        new VoidPayrollTaxPaymentTests().testReturnedTwiceERD_CreatedAndExecutedATCAndATD();

        DataLoadServices.runOffload();
        
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class));
        Company company = payrollRun.getCompany();

        assertSuccess(PayrollServices.payrollManager.reissuePayrollTaxPayment(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRun.getSourcePayRunId(),
                assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer)).getId().toString()));
        PayrollServices.commitUnitOfWork();

        assertPaymentReissued(payrollRun, "150.00", 1);
        assertPaymentATDsVoided(payrollRun, "50.00", 1);

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 950.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -950.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 50),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50),
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200)
        );

        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentCash, 200),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -50),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, 1100.08),
                new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, -1100.08),
                new DataLoadServices.LB(LedgerAccountCode.ERPayable, 50),
                new DataLoadServices.LB(LedgerAccountCode.FeeCashRevenue, 100.08),
                new DataLoadServices.LB(LedgerAccountCode.FeeIncome, 100),
                new DataLoadServices.LB(LedgerAccountCode.SalesAndUseTax, 0.08),
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 200)
        );
        
    }

    public void testAppliedCreditAndOverPaymentDebitReturnedTwiceERD_CreatedAndExecutedATCAndATD_reissue() {
        new VoidPayrollTaxPaymentTests().testAppliedCreditAndOverPaymentDebitReturnedTwiceERD_CreatedAndExecutedATCAndATD();

        //Todo - review this later
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 15, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.<PayrollRun>find(PayrollRun.class));
        Company company = payrollRun.getCompany();

        assertSuccess(PayrollServices.payrollManager.reissuePayrollTaxPayment(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                payrollRun.getSourcePayRunId(),
                assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer)).getId().toString()));
        PayrollServices.commitUnitOfWork();

    }



    public static void assertPaymentReissued(PayrollRun payrollRun, String amount, int reissuedATCs) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertPayments(payrollRun, reissuedATCs, TransactionTypeCode.AgencyTaxCredit);

        FinancialTransaction transferFT = assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.ReissueTaxLiabilityTransfer));
        assertEquals(amount, transferFT.getFinancialTransactionAmount().toString());
        DataLoadServices.assertIntuitBankAccounts(transferFT, DataLoadServices.IntuitBankAccountType.ER_Return, DataLoadServices.IntuitBankAccountType.Tax);
        MoneyMovementTransaction transferMMT = transferFT.getMoneyMovementTransaction();
        assertEquals(amount, transferMMT.getMoneyMovementTransactionAmount().toString());

        CompanyEvent event = assertOne(CompanyEvent.findCompanyEvents(payrollRun.getCompany(), EventTypeCode.PayrollTaxPaymentReissued));
        assertEquals(payrollRun.getId().toString(), event.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId));
        assertEquals(amount, event.getCompanyEventDetailValue(EventDetailTypeCode.Amount));

        PayrollServices.rollbackUnitOfWork();
    }

    private static void assertPayments(PayrollRun payrollRun, int count, TransactionTypeCode transTypeCd) {
        DomainEntitySet<FinancialTransaction> createdATCs = payrollRun.getFinancialTransactions(TransactionStateCode.Created, transTypeCd);
        DomainEntitySet<FinancialTransaction> voidedATCs = payrollRun.getFinancialTransactions(TransactionStateCode.Voided, transTypeCd);

        assertEquals(count, createdATCs.size());
        assertEquals(count, voidedATCs.size());

        List<MoneyMovementTransaction> associatedMMTs = new ArrayList<MoneyMovementTransaction>();
        for (FinancialTransaction createdATC : createdATCs) {
            assertNotNull(createdATC.getOriginalTransaction());
            FinancialTransaction originalATC = createdATC.getOriginalTransaction();
            PSP_PRAssert.assertCollectionContains(voidedATCs, originalATC);

            assertEquals(originalATC.getFinancialTransactionAmount(), createdATC.getFinancialTransactionAmount());
            assertEquals(originalATC.getSettlementTypeCd(), createdATC.getSettlementTypeCd());
            assertEquals(originalATC.getLaw(), createdATC.getLaw());

            assertNotNull(createdATC.getMoneyMovementTransaction());
            associatedMMTs.add(createdATC.getMoneyMovementTransaction());

            DataLoadServices.assertTaxSettlementDate(createdATC);
        }

        for (MoneyMovementTransaction associatedMMT : associatedMMTs) {
            //make sure new MMTs have a future date
            SpcfCalendar nextOffload = MoneyMovementTransaction.getNextInitiationDate(associatedMMT.getMoneyMovementPaymentMethod());
            CalendarUtils.clearTime(nextOffload);
            assertTrue(!associatedMMT.getInitiationDate().before(nextOffload));
        }
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

    private void assertPaymentATDsVoided(PayrollRun payrollRun, String amount, int voidedATDsCount) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertPayments(payrollRun, voidedATDsCount, TransactionTypeCode.AgencyTaxDebit);

        FinancialTransaction transferFT = assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Completed, TransactionTypeCode.ReissueAgencyTaxDebitOffset));
        assertEquals(amount, transferFT.getFinancialTransactionAmount().toString());
        assertNull("ReissueAgencyTaxDebitOffset MMT", transferFT.getMoneyMovementTransaction());
        assertNull("Credit Bank Account", transferFT.getCreditBankAccount());
        assertNull("Debit Bank Account", transferFT.getDebitBankAccount());

        PayrollServices.rollbackUnitOfWork();
    }
}
