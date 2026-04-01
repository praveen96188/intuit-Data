package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jul 25, 2011
 * Time: 2:27:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class RejectTaxPaymentCoreTests {

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
    public void testValidationPath() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList);
        }

        DataLoadServices.enrollEFTPS(companies.get(0));

        //Todo update this later
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> eftpsPayments = DataLoadServices.getReadyToSendTaxPayments(companies.get(0), PaymentMethod.EFTPS);
        assertEquals("EFTPS payments", 2, eftpsPayments.size());
        MoneyMovementTransaction moneyMovementTransaction = eftpsPayments.get(0);
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.SentToAgency);
        moneyMovementTransaction.setStatus(PaymentStatus.Executed);
        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        String reason = "Testing for check payment rejection";
        ProcessResult<MoneyMovementTransaction> processResult = PayrollServices.paymentManager.rejectPayment(moneyMovementTransaction.getId().toString(), reason);
        assertEquals("Error messages count", 1, processResult.getErrorMessages().size());
        assertEquals("Error message code", "10108", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message ", "Money Movement Transaction payment method has to be CheckPayment, SuperCheck, ACHDebit or ACHCredit.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testValidation_happyPath_ACHCredit() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList);
        }
        //Todo update this later
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> statePayments = DataLoadServices.getReadyToSendTaxPayments(companies.get(0), PaymentMethod.ACHCredit);
        assertEquals("AR state ACH payments", 1, statePayments.size());
        MoneyMovementTransaction moneyMovementTransaction = statePayments.get(0);
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.SentToAgency);
        moneyMovementTransaction.setStatus(PaymentStatus.Executed);
        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        String reason = "Testing for ACHCredit rejection";
        assertSuccess(PayrollServices.paymentManager.rejectPayment(moneyMovementTransaction.getId().toString(), reason));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("Payment status", TaxPaymentStatus.RejectedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        DomainEntitySet<VoidedCheck> voidedChecks = Application.find(VoidedCheck.class, VoidedCheck.MoneyMovementTransaction().equalTo(moneyMovementTransaction).And(VoidedCheck.Reason().equalTo(reason)));
        assertEquals("Voided Checks", 0, voidedChecks.size());
        for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
            assertEquals("FT status", TransactionState.findTransactionState(TransactionStateCode.Returned), financialTransaction.getCurrentTransactionState());
        }
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(moneyMovementTransaction.getCompany(), EventTypeCode.TaxPaymentStatusChanged);
        assertEquals("Tax Payment status change events", 1, companyEvents.size());
        assertEquals("Rejection reason", reason, companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.ReasonDescription).get(0).getValue());
        assertEquals("Company event detail new value", TaxPaymentStatus.RejectedByAgency.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).get(0).getValue());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void test_happyPath_ACHDebit() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"NM"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NM-ES903A-PAYMENT").find());
        assertEquals("NM-ES903A-PAYMENT Payment method", PaymentMethod.ACHDebit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("NM-ES903A-PAYMENT Payment Amount", new SpcfMoney("610"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("NM-ES903A-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("NM-ES903A-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 4, 29);
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("NM-ES903A-PAYMENT Tax payment status", TaxPaymentStatus.AcknowledgedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("NM-ES903A-PAYMENT status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.paymentManager.rejectPayment(moneyMovementTransaction.getId().toString(), "Testing");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("NM-ES903A-PAYMENT Tax payment status", TaxPaymentStatus.RejectedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("NM-ES903A-PAYMENT status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testValidation_happyPath_CheckPayment() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.Withholding);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList);
        }
        //Todo update this later
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> statePayments = DataLoadServices.getReadyToSendTaxPayments(companies.get(0), PaymentMethod.ACHCredit);
        assertEquals("AR state ACH payments", 1, statePayments.size());
        MoneyMovementTransaction moneyMovementTransaction = statePayments.get(0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        Company company = companies.get(0);
        assertSuccess(PayrollServices.paymentManager.changePaymentMethod(company.getSourceSystemCd(), company.getSourceCompanyId(), moneyMovementTransaction.getId(), PaymentMethod.CheckPayment));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        moneyMovementTransaction.setStatus(PaymentStatus.Executed);
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.SentToAgency);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        String reason = "Testing for check payment rejection";
        assertSuccess(PayrollServices.paymentManager.rejectPayment(moneyMovementTransaction.getId().toString(), reason));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("Payment status", TaxPaymentStatus.RejectedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        DomainEntitySet<VoidedCheck> voidedChecks = Application.find(VoidedCheck.class, VoidedCheck.MoneyMovementTransaction().equalTo(moneyMovementTransaction).And(VoidedCheck.Reason().equalTo(reason)));
        assertEquals("Voided Checks", 1, voidedChecks.size());
        assertNotNull(voidedChecks.getFirst().getCompany());
        assertEquals(voidedChecks.getFirst().getMoneyMovementTransaction().getCompany()
                ,voidedChecks.getFirst().getCompany());
        for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
            assertEquals("FT status", TransactionState.findTransactionState(TransactionStateCode.Returned), financialTransaction.getCurrentTransactionState());
        }
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(moneyMovementTransaction.getCompany(), EventTypeCode.TaxPaymentStatusChanged);
        assertEquals("Tax Payment status change events", 1, companyEvents.size());
        assertEquals("Rejection reason", reason, companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.ReasonDescription).get(0).getValue());
        assertEquals("Company event detail new value", TaxPaymentStatus.RejectedByAgency.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).get(0).getValue());
        PayrollServices.commitUnitOfWork();

    }

}
