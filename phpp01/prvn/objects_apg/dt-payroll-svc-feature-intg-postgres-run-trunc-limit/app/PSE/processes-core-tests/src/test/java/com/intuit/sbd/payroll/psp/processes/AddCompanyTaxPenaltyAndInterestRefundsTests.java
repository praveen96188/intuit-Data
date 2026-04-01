package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
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
import static org.junit.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 4/16/12
 * Time: 11:35 AM
 */
public class AddCompanyTaxPenaltyAndInterestRefundsTests {

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
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("1.00"), new SpcfMoney("2.00"), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("169", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Company QBDT:123456789 does not exist.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

        //Setup Company
        Company company = setupCompanyWithPayrollRun();

        //Test for -ve amounts and total zero amount
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("0.00"), new SpcfMoney("-1.00"), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("283", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("The amount must be a non-zero, positive number.", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("-1.00"), new SpcfMoney("0.00"), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("283", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("The amount must be a non-zero, positive number.", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("0.00"), new SpcfMoney("0.00"), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("617", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Total refund amount must be greater than zero.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

        //Test for null values
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, null, new SpcfMoney("1.00"), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("5002", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Required 'PenaltiesRefundAmount' input is missing or blank", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("1.00"), null, "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("5002", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Required 'InterestRefundAmount' input is missing or blank", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("0.00"), new SpcfMoney("1.00"), null, SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("NoteText has invalid value", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("0.00"), new SpcfMoney("1.00"), "NoteText", null);
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("SettlementType has invalid value", processResult.getErrorMessages().get(0).getMessage());


        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("0.00"), new SpcfMoney("1.00"), "NoteText", SettlementTypeDTO.Cash);
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("SettlementType has invalid value", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

        //Test for Total refund amount = zero
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("0.00"), new SpcfMoney("0.00"), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("617", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Total refund amount must be greater than zero.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();


        //Test for ACH settlement type when bank account is in inactive status
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
        companyBankAccount.setStatusCd(BankAccountStatus.Inactive);
        Application.save(companyBankAccount);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("1.00"), new SpcfMoney("0.00"), "NoteText", SettlementTypeDTO.ACH);
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

        //Moving one day ahead
        DataLoadServices.setPSPDate(2012, 4, 24);
        CalendarUtils.addBusinessDays(settlementDate, 1);
        CalendarUtils.addBusinessDays(initDate, 1);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("1.50"), new SpcfMoney("2.25"), "NoteText - Testing", SettlementTypeDTO.ACH);
        financialTransactions = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Number Financial transactions", 2, financialTransactions.size());
        penaltyTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerPenaltiesRefundCredit))));
        interestTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerInterestRefundCredit))));
        assertEquals("Penalties Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), penaltyTxn.getCurrentTransactionState());
        assertEquals("Penalties Txn Amount", new SpcfMoney("1.50"), penaltyTxn.getFinancialTransactionAmount());
        assertEquals("Penalties Txn settlement type", SettlementType.ACH, penaltyTxn.getSettlementTypeCd());
        assertEquals("Penalties Txn settlement date", settlementDate, penaltyTxn.getSettlementDate().toLocal());
        assertEquals("Interest Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), interestTxn.getCurrentTransactionState());
        assertEquals("Interest Txn Amount", new SpcfMoney("2.25"), interestTxn.getFinancialTransactionAmount());
        assertEquals("Interest Txn settlement type", SettlementType.ACH, interestTxn.getSettlementTypeCd());
        assertEquals("Interest Txn settlement date", settlementDate, interestTxn.getSettlementDate().toLocal());
        assertTrue("Penalties and Interest Txn MMT", penaltyTxn.getMoneyMovementTransaction().equals(interestTxn.getMoneyMovementTransaction()));
        assertEquals("MMT Txn Amount", new SpcfMoney("3.75"), penaltyTxn.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Amount", PaymentMethod.ACHDirectDeposit, penaltyTxn.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, interestTxn.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, interestTxn.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.commitUnitOfWork();

        //Offload transactions
        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 6.5),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 8.25),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -14.75),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );

        DataLoadServices.returnTxns(financialTransactions, "R01", "NSF");

        //Run Transaction processor
        DataLoadServices.runACHTransactionProcessor();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 6.5),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 8.25),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -14.75),
                                              new DataLoadServices.LB(LedgerAccountCode.ERReturnCash, 3.75),
                                              new DataLoadServices.LB(LedgerAccountCode.ERReturnReceivable, -3.75),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );
    }

    @Test
    public void test_ACH_HappyPath_Cancel() {
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


        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("1.50"), new SpcfMoney("2.25"), "NoteText - Testing", SettlementTypeDTO.ACH);
        financialTransactions = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Number Financial transactions", 2, financialTransactions.size());
        penaltyTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerPenaltiesRefundCredit))));
        interestTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerInterestRefundCredit))));
        assertEquals("Penalties Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), penaltyTxn.getCurrentTransactionState());
        assertEquals("Penalties Txn Amount", new SpcfMoney("1.50"), penaltyTxn.getFinancialTransactionAmount());
        assertEquals("Penalties Txn settlement type", SettlementType.ACH, penaltyTxn.getSettlementTypeCd());
        assertEquals("Penalties Txn settlement date", settlementDate, penaltyTxn.getSettlementDate().toLocal());
        assertEquals("Interest Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), interestTxn.getCurrentTransactionState());
        assertEquals("Interest Txn Amount", new SpcfMoney("2.25"), interestTxn.getFinancialTransactionAmount());
        assertEquals("Interest Txn settlement type", SettlementType.ACH, interestTxn.getSettlementTypeCd());
        assertEquals("Interest Txn settlement date", settlementDate, interestTxn.getSettlementDate().toLocal());
        assertTrue("Penalties and Interest Txn MMT", penaltyTxn.getMoneyMovementTransaction().equals(interestTxn.getMoneyMovementTransaction()));
        assertEquals("MMT Txn Amount", new SpcfMoney("14.75"), penaltyTxn.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Amount", PaymentMethod.ACHDirectDeposit, penaltyTxn.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, interestTxn.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, interestTxn.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.commitUnitOfWork();

        //Cancel $ 2.25 interest refund
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.cancelTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), interestTxn.getId().toString()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(interestTxn);
        assertEquals("Interest Txn status after Cancel", TransactionState.findTransactionState(TransactionStateCode.Cancelled), interestTxn.getCurrentTransactionState());
        Application.refresh(penaltyTxn);
        assertEquals("MMT Txn Amount", new SpcfMoney("12.50"), penaltyTxn.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        PayrollServices.commitUnitOfWork();

        //Offload transactions
        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 6.5),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 6.00),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -12.50),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );
    }

    @Test
    public void test_Wire_HappyPath() {
        Company company = setupCompanyWithPayrollRun();
        SpcfCalendar settlementDate = SpcfCalendar.createInstance(2012, 4, 24, SpcfTimeZone.getLocalTimeZone());

        DataLoadServices.setPSPDate(2012, 4, 23);

        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("5.00"), new SpcfMoney("6.00"), "NoteText - Testing", SettlementTypeDTO.Wire);
        DomainEntitySet<FinancialTransaction> financialTransactions = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Number Financial transactions", 2, financialTransactions.size());
        FinancialTransaction penaltyTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerPenaltiesRefundCredit))));
        FinancialTransaction interestTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerInterestRefundCredit))));
        assertEquals("Penalties Txn status", TransactionState.findTransactionState(TransactionStateCode.Completed), penaltyTxn.getCurrentTransactionState());
        assertEquals("Penalties Txn Amount", new SpcfMoney("5.00"), penaltyTxn.getFinancialTransactionAmount());
        assertEquals("Penalties Txn settlement type", SettlementType.Wire, penaltyTxn.getSettlementTypeCd());
        assertEquals("Penalties Txn settlement date", settlementDate, penaltyTxn.getSettlementDate().toLocal());
        assertNull("Penalties Txn MMT", penaltyTxn.getMoneyMovementTransaction());
        assertEquals("Interest Txn status", TransactionState.findTransactionState(TransactionStateCode.Completed), interestTxn.getCurrentTransactionState());
        assertEquals("Interest Txn Amount", new SpcfMoney("6.00"), interestTxn.getFinancialTransactionAmount());
        assertEquals("Interest Txn settlement type", SettlementType.Wire, interestTxn.getSettlementTypeCd());
        assertEquals("Interest Txn settlement date", settlementDate, interestTxn.getSettlementDate().toLocal());
        assertNull("Interest Txn MMT", interestTxn.getMoneyMovementTransaction());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.ERPenaltiesAndInterestRefundCreated));
        assertEquals("Total Refund amount", "11.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.TotalRefundAmount));
        assertEquals("Penalties Refund amount", "5.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PenaltiesRefundAmount));
        assertEquals("Interest Refund amount", "6.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.InterestRefundAmount));
        assertEquals("Note text", "NoteText - Testing", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NoteText));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 5),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 6),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -11),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );

        //Void Interest Refund
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.voidTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), interestTxn.getId().toString()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 5),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 0),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -5),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );

        //Test with only penalties and interest refund separately
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney(".50"), new SpcfMoney("0.00"), "NoteText - Testing", SettlementTypeDTO.Wire);
        financialTransactions = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Number Financial transactions", 1, financialTransactions.size());
        penaltyTxn = financialTransactions.getFirst();
        assertEquals("Penalties Txn status", TransactionState.findTransactionState(TransactionStateCode.Completed), penaltyTxn.getCurrentTransactionState());
        assertEquals("Penalties Txn Amount", new SpcfMoney("0.50"), penaltyTxn.getFinancialTransactionAmount());
        assertEquals("Penalties Txn settlement type", SettlementType.Wire, penaltyTxn.getSettlementTypeCd());
        assertEquals("Penalties Txn settlement date", settlementDate, penaltyTxn.getSettlementDate().toLocal());
        assertNull("Penalties Txn MMT", penaltyTxn.getMoneyMovementTransaction());

        processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("0.00"), new SpcfMoney("1.25"), "NoteText - Testing", SettlementTypeDTO.Wire);
        financialTransactions = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Number Financial transactions", 1, financialTransactions.size());
        interestTxn = financialTransactions.getFirst();
        assertEquals("Interest Txn status", TransactionState.findTransactionState(TransactionStateCode.Completed), interestTxn.getCurrentTransactionState());
        assertEquals("Interest Txn Amount", new SpcfMoney("1.25"), interestTxn.getFinancialTransactionAmount());
        assertEquals("Interest Txn settlement type", SettlementType.Wire, interestTxn.getSettlementTypeCd());
        assertEquals("Interest Txn settlement date", settlementDate, interestTxn.getSettlementDate().toLocal());
        assertNull("Interest Txn MMT", interestTxn.getMoneyMovementTransaction());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 5.5),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 1.25),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -6.75),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );

    }

    @Test
    public void test_Check_HappyPath() {
        Company company = setupCompanyWithPayrollRun();
        SpcfCalendar settlementDate = SpcfCalendar.createInstance(2012, 4, 24, SpcfTimeZone.getLocalTimeZone());

        DataLoadServices.setPSPDate(2012, 4, 23);

        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("5.00"), new SpcfMoney("6.00"), "NoteText - Testing", SettlementTypeDTO.CheckType);
        DomainEntitySet<FinancialTransaction> financialTransactions = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Number Financial transactions", 2, financialTransactions.size());
        FinancialTransaction penaltyTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerPenaltiesRefundCredit))));
        FinancialTransaction interestTxn = assertOne(financialTransactions.find(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerInterestRefundCredit))));
        assertEquals("Penalties Txn status", TransactionState.findTransactionState(TransactionStateCode.Completed), penaltyTxn.getCurrentTransactionState());
        assertEquals("Penalties Txn Amount", new SpcfMoney("5.00"), penaltyTxn.getFinancialTransactionAmount());
        assertEquals("Penalties Txn settlement type", SettlementType.CheckType, penaltyTxn.getSettlementTypeCd());
        assertEquals("Penalties Txn settlement date", settlementDate, penaltyTxn.getSettlementDate().toLocal());
        assertNull("Penalties Txn MMT", penaltyTxn.getMoneyMovementTransaction());
        assertEquals("Interest Txn status", TransactionState.findTransactionState(TransactionStateCode.Completed), interestTxn.getCurrentTransactionState());
        assertEquals("Interest Txn Amount", new SpcfMoney("6.00"), interestTxn.getFinancialTransactionAmount());
        assertEquals("Interest Txn settlement type", SettlementType.CheckType, interestTxn.getSettlementTypeCd());
        assertEquals("Interest Txn settlement date", settlementDate, interestTxn.getSettlementDate().toLocal());
        assertNull("Interest Txn MMT", interestTxn.getMoneyMovementTransaction());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.ERPenaltiesAndInterestRefundCreated));
        assertEquals("Total Refund amount", "11.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.TotalRefundAmount));
        assertEquals("Penalties Refund amount", "5.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PenaltiesRefundAmount));
        assertEquals("Interest Refund amount", "6.00", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.InterestRefundAmount));
        assertEquals("Note text", "NoteText - Testing", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NoteText));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 5),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 6),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -11),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 189),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 189)
        );

        //Void penalties Refund
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.voidTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), penaltyTxn.getId().toString()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.TaxPenaltiesExpense, 0),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxInterestExpense, 6),
                                              new DataLoadServices.LB(LedgerAccountCode.FeeCashBalanceSheet, -6),
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
