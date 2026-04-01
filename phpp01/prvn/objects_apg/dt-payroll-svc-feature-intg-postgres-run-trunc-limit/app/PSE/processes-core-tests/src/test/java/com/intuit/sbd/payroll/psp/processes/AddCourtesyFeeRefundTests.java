package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * User: ihannur
 * Date: 7/3/12
 * Time: 5:04 PM
 */
public class AddCourtesyFeeRefundTests {

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
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("2.00"), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("169", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Company QBDT:123456789 does not exist.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

        //Setup Company
        Company company = setupCompanyWithPayrollRun();

        //Test for zero amount
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("0.00"), "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("283", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("The amount must be a non-zero, positive number.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

        //Test for null values
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(SourceSystemCode.QBDT, psid, null, "NoteText", SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("5002", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Required 'RefundAmount' input is missing or blank", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("1.00"), null, SettlementTypeDTO.ACH);
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("NoteText has invalid value", processResult.getErrorMessages().get(0).getMessage());

        processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("1.00"), "NoteText", null);
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("SettlementType has invalid value", processResult.getErrorMessages().get(0).getMessage());


        processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("1.00"), "NoteText", SettlementTypeDTO.Cash);
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("SettlementType has invalid value", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

        //Test for ACH settlement type when bank account is in inactive status
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
        companyBankAccount.setStatusCd(BankAccountStatus.Inactive);
        Application.save(companyBankAccount);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(SourceSystemCode.QBDT, psid, new SpcfMoney("1.00"), "NoteText", SettlementTypeDTO.ACH);
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
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("5.00"), "NoteText - Testing", SettlementTypeDTO.ACH);
        FinancialTransaction financialTransaction = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Refund Transaction type", TransactionType.findTransactionType(TransactionTypeCode.ERCourtesyRefundCredit), financialTransaction.getTransactionType());
        assertEquals("Refund Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), financialTransaction.getCurrentTransactionState());
        assertEquals("Refund Txn Amount", new SpcfMoney("5.00"), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Refund Txn settlement type", SettlementType.ACH, financialTransaction.getSettlementTypeCd());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction.getSettlementDate().toLocal());

        assertEquals("MMT Txn Amount", new SpcfMoney("5.00"), financialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Payment method", PaymentMethod.ACHDirectDeposit, financialTransaction.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, financialTransaction.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, financialTransaction.getMoneyMovementTransaction().getInitiationDate().toLocal());

        PayrollServices.commitUnitOfWork();

        //Offload transactions
        DataLoadServices.runOffload();

        //Moving one day ahead
        DataLoadServices.setPSPDate(2012, 4, 24);
        CalendarUtils.addBusinessDays(settlementDate, 1);
        CalendarUtils.addBusinessDays(initDate, 1);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> emailEvents = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.RefundedFeeAmount1);
        CompanyEventEmail companyEventEmail = assertOne(emailEvents);
        assertEquals("Refund company event", EventTypeCode.FeeRefunded, companyEventEmail.getCompanyEvent().getEventTypeCd());

        processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("2.25"), "NoteText - Testing", SettlementTypeDTO.ACH);
        financialTransaction = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Refund Txn type", TransactionType.findTransactionType(TransactionTypeCode.ERCourtesyRefundCredit), financialTransaction.getTransactionType());
        assertEquals("Refund Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), financialTransaction.getCurrentTransactionState());
        assertEquals("Refund Txn Amount", new SpcfMoney("2.25"), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Refund Txn settlement type", SettlementType.ACH, financialTransaction.getSettlementTypeCd());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction.getSettlementDate().toLocal());

        assertEquals("MMT Txn Amount", new SpcfMoney("2.25"), financialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Amount", PaymentMethod.ACHDirectDeposit, financialTransaction.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, financialTransaction.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, financialTransaction.getMoneyMovementTransaction().getInitiationDate().toLocal());

        PayrollServices.commitUnitOfWork();

        //Offload transactions
        DataLoadServices.runOffload();

        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransactions.add(financialTransaction);
        DataLoadServices.returnTxns(financialTransactions, "R01", "NSF");

        //Run Transaction processor
        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        emailEvents = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.RefundedFeeAmount1);
        assertEquals("Company email events", 2, emailEvents.size());
        Application.refresh(financialTransaction);
        assertEquals("Refund txn state after returning", TransactionState.findTransactionState(TransactionStateCode.Returned), financialTransaction.getCurrentTransactionState());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_ACH_Multiple_Refunds_CombineTO_MMT() {
        Company company = setupCompanyWithPayrollRun();
        SpcfCalendar settlementDate = SpcfCalendar.createInstance(2012, 4, 24, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2012, 4, 23, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(2012, 4, 23);

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("5.00"), "NoteText - Testing", SettlementTypeDTO.ACH);
        FinancialTransaction financialTransaction = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Refund Transaction type", TransactionType.findTransactionType(TransactionTypeCode.ERCourtesyRefundCredit), financialTransaction.getTransactionType());
        assertEquals("Refund Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), financialTransaction.getCurrentTransactionState());
        assertEquals("Refund Txn Amount", new SpcfMoney("5.00"), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Refund Txn settlement type", SettlementType.ACH, financialTransaction.getSettlementTypeCd());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction.getSettlementDate().toLocal());

        assertEquals("MMT Txn Amount", new SpcfMoney("5.00"), financialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Payment method", PaymentMethod.ACHDirectDeposit, financialTransaction.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, financialTransaction.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, financialTransaction.getMoneyMovementTransaction().getInitiationDate().toLocal());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("2.25"), "NoteText - Testing", SettlementTypeDTO.ACH);
        FinancialTransaction financialTransaction2 = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Refund Txn type", TransactionType.findTransactionType(TransactionTypeCode.ERCourtesyRefundCredit), financialTransaction2.getTransactionType());
        assertEquals("Refund Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), financialTransaction2.getCurrentTransactionState());
        assertEquals("Refund Txn Amount", new SpcfMoney("2.25"), financialTransaction2.getFinancialTransactionAmount());
        assertEquals("Refund Txn settlement type", SettlementType.ACH, financialTransaction2.getSettlementTypeCd());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction2.getSettlementDate().toLocal());

        assertEquals("MMT Txn Amount", new SpcfMoney("2.25"), financialTransaction2.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Amount", PaymentMethod.ACHDirectDeposit, financialTransaction2.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, financialTransaction2.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, financialTransaction2.getMoneyMovementTransaction().getInitiationDate().toLocal());

        Application.refresh(financialTransaction);
        assertEquals("MMT Txn Amount", new SpcfMoney("5.00"), financialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Amount", PaymentMethod.ACHDirectDeposit, financialTransaction.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, financialTransaction.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, financialTransaction.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void test_ACH_HappyPath_Cancel() {
        Company company = setupCompanyWithPayrollRun();
        SpcfCalendar settlementDate = SpcfCalendar.createInstance(2012, 4, 24, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2012, 4, 23, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(2012, 4, 23);

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("5.00"), "NoteText - Testing", SettlementTypeDTO.ACH);
        assertSuccess(processResult);
        FinancialTransaction financialTransaction = processResult.getResult();
        assertEquals("Refund Txn type", TransactionType.findTransactionType(TransactionTypeCode.ERCourtesyRefundCredit), financialTransaction.getTransactionType());
        assertEquals("Refund Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), financialTransaction.getCurrentTransactionState());
        assertEquals("Refund Txn Amount", new SpcfMoney("5.00"), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Refund Txn settlement type", SettlementType.ACH, financialTransaction.getSettlementTypeCd());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction.getSettlementDate().toLocal());

        assertEquals("MMT Txn Amount", new SpcfMoney("5.00"), financialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Payment method", PaymentMethod.ACHDirectDeposit, financialTransaction.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, financialTransaction.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, financialTransaction.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("1.50"), "NoteText - Testing", SettlementTypeDTO.ACH);
        FinancialTransaction financialTransaction2 = processResult.getResult();
        assertSuccess(processResult);

        assertEquals("Refund Txn type", TransactionType.findTransactionType(TransactionTypeCode.ERCourtesyRefundCredit), financialTransaction2.getTransactionType());
        assertEquals("Refund Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), financialTransaction2.getCurrentTransactionState());
        assertEquals("Refund Txn Amount", new SpcfMoney("1.50"), financialTransaction2.getFinancialTransactionAmount());
        assertEquals("Refund Txn settlement type", SettlementType.ACH, financialTransaction2.getSettlementTypeCd());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction2.getSettlementDate().toLocal());

        assertEquals("MMT Txn Amount", new SpcfMoney("1.50"), financialTransaction2.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Amount", PaymentMethod.ACHDirectDeposit, financialTransaction2.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, financialTransaction2.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, financialTransaction2.getMoneyMovementTransaction().getInitiationDate().toLocal());

        Application.refresh(financialTransaction);

        assertEquals("MMT Txn Amount", new SpcfMoney("5.00"), financialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Amount", PaymentMethod.ACHDirectDeposit, financialTransaction.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());

        PayrollServices.commitUnitOfWork();

        //Cancel $ 1.50 interest refund
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.cancelTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), financialTransaction2.getId().toString()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(financialTransaction2);
        assertEquals("Refund Txn status after Cancel", TransactionState.findTransactionState(TransactionStateCode.Cancelled), financialTransaction2.getCurrentTransactionState());
        assertNull("Refund Txn MMT after cancelling", financialTransaction2.getMoneyMovementTransaction());
        Application.refresh(financialTransaction);
        assertEquals("MMT Txn Amount", new SpcfMoney("5.00"), financialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void test_Wire_HappyPath() {
        Company company = setupCompanyWithPayrollRun();
        SpcfCalendar settlementDate = SpcfCalendar.createInstance(2012, 4, 24, SpcfTimeZone.getLocalTimeZone());

        DataLoadServices.setPSPDate(2012, 4, 23);

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("6.00"), "NoteText - Testing", SettlementTypeDTO.Wire);
        assertSuccess(processResult);
        FinancialTransaction financialTransaction = processResult.getResult();

        assertEquals("Refund Txn type", TransactionType.findTransactionType(TransactionTypeCode.ERCourtesyRefundCredit), financialTransaction.getTransactionType());
        assertEquals("Refund Txn status", TransactionState.findTransactionState(TransactionStateCode.Completed), financialTransaction.getCurrentTransactionState());
        assertEquals("Refund Txn Amount", new SpcfMoney("6.00"), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Refund Txn settlement type", SettlementType.Wire, financialTransaction.getSettlementTypeCd());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction.getSettlementDate().toLocal());
        assertNull("Refund Txn MMT", financialTransaction.getMoneyMovementTransaction());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeRefunded));
        assertEquals("Total Refund amount", financialTransaction.getId().toString(), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId));
        assertEquals("Note text", "NoteText - Testing", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NoteText));
        PayrollServices.rollbackUnitOfWork();

        //Void Interest Refund
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.voidTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), financialTransaction.getId().toString()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_Check_HappyPath() {
        Company company = setupCompanyWithPayrollRun();
        SpcfCalendar settlementDate = SpcfCalendar.createInstance(2012, 4, 24, SpcfTimeZone.getLocalTimeZone());

        DataLoadServices.setPSPDate(2012, 4, 23);

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("6.00"), "NoteText - Testing", SettlementTypeDTO.CheckType);
        assertSuccess(processResult);
        FinancialTransaction financialTransaction = processResult.getResult();
        assertEquals("Refund Txn type", TransactionType.findTransactionType(TransactionTypeCode.ERCourtesyRefundCredit), financialTransaction.getTransactionType());
        assertEquals("Refund Txn status", TransactionState.findTransactionState(TransactionStateCode.Completed), financialTransaction.getCurrentTransactionState());
        assertEquals("Refund Txn Amount", new SpcfMoney("6.00"), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Refund Txn settlement type", SettlementType.CheckType, financialTransaction.getSettlementTypeCd());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction.getSettlementDate().toLocal());
        assertNull("Refund Txn MMT", financialTransaction.getMoneyMovementTransaction());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction.getSettlementDate().toLocal());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeRefunded));
        assertEquals("FT event detail", financialTransaction.getId().toString(), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId));
        assertEquals("Note text", "NoteText - Testing", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NoteText));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testApplyCourtesyRefundSku() {
        Company company = setupCompanyWithPayrollRun();
        DataLoadServices.setPSPDate(2012, 4, 23);

        PayrollServices.beginUnitOfWork();
        Offering offering = Offering.findBySKU("AP-59ME-2");
        OfferingInfoDTO offeringInfoDTO = PayrollServices.dtoFactory.create(offering);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateCompanyOffering(SourceSystemCode.QBDT, company.getSourceCompanyId(), offeringInfoDTO);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("5.00"), "NoteText - Testing", SettlementTypeDTO.ACH);
        FinancialTransaction financialTransaction = processResult.getResult();
        assertSuccess(processResult);
        assertEquals("Sku", "297369", financialTransaction.getSku());
        assertEquals("SkuQuantity", 1, financialTransaction.getSkuQuantity());
        PayrollServices.commitUnitOfWork();
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
