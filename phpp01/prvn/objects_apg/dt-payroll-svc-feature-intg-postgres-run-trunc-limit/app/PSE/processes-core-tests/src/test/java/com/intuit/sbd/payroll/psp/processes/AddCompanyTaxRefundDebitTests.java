package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * User: ihannur
 * Date: 8/6/12
 * Time: 1:22 PM
 */
public class AddCompanyTaxRefundDebitTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2012, 4, 23);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testValidations() {
        String psid = "123456789";

        //Test for company does not exists error
        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addRefundDebit(null, "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("5002", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Required 'Financial Transaction ID' input is missing or blank", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

        //Setup Company
        Company company = setupCompanyWithPayrollRun();

        //Create Penalty and Interest credit transactions
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> result = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("5.00"), new SpcfMoney("6.00"), "NoteText", SettlementTypeDTO.ACH);
        assertSuccess(result);
        FinancialTransaction penaltiesTxn = assertOne(result.getResult().find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerPenaltiesRefundCredit))));
        FinancialTransaction interestTxn = assertOne(result.getResult().find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerInterestRefundCredit))));
        PayrollServices.commitUnitOfWork();

        //Test invalid FT Id
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addRefundDebit(SpcfUniqueId.generateRandomUniqueId().toString(), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("5003", processResult.getErrorMessages().get(0).getMessageCode());

        processResult = PayrollServices.financialTransactionManager.addRefundDebit(interestTxn.getId().toString(), "NoteText", null);
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("SettlementType has invalid value", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.rollbackUnitOfWork();

        //Test for ACH settlement type when bank account is in inactive status
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
        companyBankAccount.setStatusCd(BankAccountStatus.Inactive);
        Application.save(companyBankAccount);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addRefundDebit(interestTxn.getId().toString(), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("1062", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Company QBDT:123456789 does not have an active bank account.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

    }


    @Test
    public void test_ACH_HappyPath() {
        Company company = setupCompanyWithPayrollRun();
        SpcfCalendar settlementDate = SpcfCalendar.createInstance(2012, 4, 24, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2012, 4, 23, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(2012, 4, 23);

        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("5.00"), new SpcfMoney("6.00"), "NoteText - Testing", SettlementTypeDTO.ACH);
        DomainEntitySet<FinancialTransaction> financialTransactions = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Number Financial transactions", 2, financialTransactions.size());
        FinancialTransaction penaltyTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerPenaltiesRefundCredit))));
        FinancialTransaction interestTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerInterestRefundCredit))));
        assertEquals("Penalties Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), penaltyTxn.getCurrentTransactionState());
        assertEquals("Penalties Txn Amount", new SpcfMoney("5.00"), penaltyTxn.getFinancialTransactionAmount());
        assertEquals("Penalties Txn settlement type", SettlementType.ACH, penaltyTxn.getSettlementTypeCd());
        assertEquals("Penalties Txn settlement date", settlementDate, penaltyTxn.getSettlementDate().toLocal());
        assertEquals("Interest Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), interestTxn.getCurrentTransactionState());
        assertEquals("Interest Txn Amount", new SpcfMoney("6.00"), interestTxn.getFinancialTransactionAmount());
        assertEquals("Interest Txn settlement type", SettlementType.ACH, interestTxn.getSettlementTypeCd());
        assertEquals("Interest Txn settlement date", settlementDate, interestTxn.getSettlementDate().toLocal());
        assertTrue("Penalties and Interest Txn MMT", penaltyTxn.getMoneyMovementTransaction().equals(interestTxn.getMoneyMovementTransaction()));
        assertEquals("MMT Txn Amount", new SpcfMoney("11.00"), penaltyTxn.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Amount", PaymentMethod.ACHDirectDeposit, penaltyTxn.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, interestTxn.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, interestTxn.getMoneyMovementTransaction().getInitiationDate().toLocal());

        PayrollServices.commitUnitOfWork();

        //Offload transactions
        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 5),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 6),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -11),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );

        DataLoadServices.setPSPDate(2012, 4, 24);
        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> result = PayrollServices.financialTransactionManager.addRefundDebit(penaltyTxn.getId().toString(), "Refund Debit for Penalties Txn", SettlementTypeDTO.ACH);
        assertSuccess(result);
        assertEquals("Refund Dbt Amount", penaltyTxn.getFinancialTransactionAmount(), result.getResult().getFinancialTransactionAmount());
        assertEquals("Refund Dbt Settlement type", SettlementType.ACH, result.getResult().getSettlementTypeCd());
        PayrollServices.commitUnitOfWork();
        //Offload transactions
        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 0),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 6),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -6),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );

        DataLoadServices.setPSPDate(2012, 4, 25);
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.financialTransactionManager.addRefundDebit(interestTxn.getId().toString(), "Refund Debit for Penalties Txn", SettlementTypeDTO.CheckType);
        assertSuccess(result);
        assertEquals("Refund Dbt Amount", interestTxn.getFinancialTransactionAmount(), result.getResult().getFinancialTransactionAmount());
        assertEquals("Refund Dbt Settlement type", SettlementType.CheckType, result.getResult().getSettlementTypeCd());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 0),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 0),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, 0),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );

    }

    private Company setupCompanyWithPayrollRun() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertNotNull(company);
        CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
        assertNotNull(taxService);
        Assert.assertEquals("Company Service Status", ServiceSubStatusCode.ActiveCurrent, taxService.getStatusCd());

        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        DataLoadServices.assertPayrollsEqual(payrollDTO, payroll);
        PayrollServices.commitUnitOfWork();

        return company;
    }

}
