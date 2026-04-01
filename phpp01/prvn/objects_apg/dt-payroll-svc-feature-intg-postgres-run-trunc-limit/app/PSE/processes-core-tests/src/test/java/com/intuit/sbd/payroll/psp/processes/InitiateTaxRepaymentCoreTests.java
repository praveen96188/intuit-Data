package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.eftps.EftpsDataLoader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CustomerTaxPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.zeropayments.ProcessZeroPayments;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.*;

import java.math.BigDecimal;
import java.util.HashMap;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jan 21, 2011
 * Time: 4:10:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class InitiateTaxRepaymentCoreTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005,1,1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void happyPath_EFTPSDirectDebit() throws Exception {
        new PayrollSubmitTaxTests().test100KWithOrderedPayrolls();

        DataLoadServices.setPSPDate(2011, 2, 18);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2011, 2, 21);
        EftpsDataLoader.overridePendingToCompletedStatus();
        EftpsDataLoader.callReturnSimulator();
        EdiManager.processWaitingResponseFiles();

        DataLoadServices.setPSPDate(2011, 2, 22);
        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 2, 23, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2011, 2, 24, SpcfTimeZone.getLocalTimeZone());

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPSDirectDebit)));

        @SuppressWarnings("unchecked") ProcessResult<MoneyMovementTransaction> updateProcessResult = PayrollServices.paymentManager.initiateTaxRepayment(moneyMovementTransaction.getId().toString(), newInitDate, false);
        assertSuccess(updateProcessResult);
        MoneyMovementTransaction newMmt = updateProcessResult.getResult();
        newMmt = Application.findById(MoneyMovementTransaction.class, newMmt.getId());
        PayrollServices.commitUnitOfWork();

        SpcfCalendar newInitiationDate = newMmt.getInitiationDate().copy();
        CalendarUtils.clearTime(newInitiationDate);
        Assert.assertEquals("Initiation Date", newInitDate.toString(), newInitiationDate.toString());
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, FinancialTransaction.MoneyMovementTransaction().equalTo(newMmt));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            SpcfCalendar settlementDate = financialTransaction.getSettlementDate().copy();
            CalendarUtils.clearTime(settlementDate);
            Assert.assertEquals("Settlement Date", newSettlementDate.toString(), settlementDate.toString());
        }
        Assert.assertEquals("Number of Financial transactions:", financialTransactions.size(), moneyMovementTransaction.getFinancialTransactionCollection().size());
        Assert.assertNotSame("New Id is generated:", moneyMovementTransaction.getId(), newMmt.getId());
    }

    //TODO Ignoring test for build migration 
    @Ignore
    @Test
    public void happyPath_EFTPS() throws Exception {
        new PayrollSubmitTaxTests().test100KWithOrderedPayrolls();

        DataLoadServices.setPSPDate(2011, 2, 18);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2011, 2, 21);
        EftpsDataLoader.overridePendingToCompletedStatus();
        EftpsDataLoader.callReturnSimulator();
        EdiManager.processWaitingResponseFiles();

        DataLoadServices.setPSPDate(2011, 2, 22);
        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 2, 23, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2011, 2, 24, SpcfTimeZone.getLocalTimeZone());

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPSDirectDebit)));

        @SuppressWarnings("unchecked") ProcessResult<MoneyMovementTransaction> updateProcessResult = PayrollServices.paymentManager.initiateTaxRepayment(moneyMovementTransaction.getId().toString(), newInitDate, false);
        assertSuccess(updateProcessResult);
        MoneyMovementTransaction newMmt = updateProcessResult.getResult();
        newMmt = Application.findById(MoneyMovementTransaction.class, newMmt.getId());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar newInitiationDate = newMmt.getInitiationDate().copy();
        CalendarUtils.clearTime(newInitiationDate);
        Assert.assertEquals("Initiation Date", newInitDate.toString(), newInitiationDate.toString());
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, FinancialTransaction.MoneyMovementTransaction().equalTo(newMmt));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            SpcfCalendar settlementDate = financialTransaction.getSettlementDate().copy();
            CalendarUtils.clearTime(settlementDate);
            Assert.assertEquals("Settlement Date", newSettlementDate.toString(), settlementDate.toString());
        }
        moneyMovementTransaction = Application.refresh(moneyMovementTransaction);
        Assert.assertEquals("Number of Financial transactions:", financialTransactions.size(), moneyMovementTransaction.getFinancialTransactionCollection().size());
        Assert.assertNotSame("New Id is generated:", moneyMovementTransaction.getId(), newMmt.getId());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void happyPath_ACHCredit() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        String[] statesList = new String[]{"AR"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.Withholding));
        DataLoadServices.runPayrollRun(company, statesList);

        DataLoadServices.setPSPDate(2011, 1, 3);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("AR-941M-PAYMENT"));

        DataLoadServices.setPSPDate(2011, 1, 4);
        DataLoadServices.returnAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("AR-941M-PAYMENT"));

        DataLoadServices.setPSPDate(2011, 1, 21);

        PayrollServices.beginUnitOfWork();
        
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        int achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("AR-941M-PAYMENT").find());

        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2011, 2, 28, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar newInitDate = newSettlementDate.copy();
        CalendarUtils.addBusinessDays(newInitDate, -achTaxOffloadOffset);

        @SuppressWarnings("unchecked") ProcessResult<MoneyMovementTransaction> updateProcessResult = PayrollServices.paymentManager.initiateTaxRepayment(moneyMovementTransaction.getId().toString(), newInitDate, false);
        assertSuccess(updateProcessResult);
        MoneyMovementTransaction newMmt = updateProcessResult.getResult();
        PayrollServices.commitUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        newMmt = Application.findById(MoneyMovementTransaction.class, newMmt.getId());
        assertSuccess(updateProcessResult);
        SpcfCalendar newInitiationDate = newMmt.getInitiationDate().copy();
        CalendarUtils.clearTime(newInitiationDate);
        Assert.assertEquals("Initiation Date", newInitDate.toString(), newInitiationDate.toString());
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, FinancialTransaction.MoneyMovementTransaction().equalTo(newMmt));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            SpcfCalendar settlementDate = financialTransaction.getSettlementDate().copy();
            CalendarUtils.clearTime(settlementDate);
            Assert.assertEquals("Settlement Date", newSettlementDate.toString(), settlementDate.toString());
        }
        moneyMovementTransaction = Application.refresh(moneyMovementTransaction);
        Assert.assertEquals("Number of Financial transactions:", financialTransactions.size(), moneyMovementTransaction.getFinancialTransactionCollection().size());
        Assert.assertNotSame("New Id is generated:", moneyMovementTransaction.getId(), newMmt.getId());
        assertEquals("New MMT payment method", PaymentMethod.ACHCredit,  newMmt.getMoneyMovementPaymentMethod());
        assertEquals("Entry detail Records on new MMT" , 2, newMmt.getEntryDetailRecordCollection().size());
        assertEquals("Entry detail records on cancelled MMT", 0, moneyMovementTransaction.getEntryDetailRecordCollection().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testValidationPath() throws Exception {
        PayrollSubmitTaxTests payrollSubmitTaxTests = new PayrollSubmitTaxTests();
        payrollSubmitTaxTests.test100KWithOrderedPayrolls();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 21, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 01, 24, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPSDirectDebit));
        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.get(0);
        Application.save(moneyMovementTransaction);
        ProcessResult<MoneyMovementTransaction> initiateProcessResult = PayrollServices.paymentManager.initiateTaxRepayment(moneyMovementTransaction.getId().toString(), newInitDate, false);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals("Process success", false, initiateProcessResult.isSuccess());
        Assert.assertEquals("Error message", 2, initiateProcessResult.getErrorMessages().size());
        Assert.assertEquals("Error message code", "10109", initiateProcessResult.getErrorMessages().get(0).getMessageCode());
        Assert.assertEquals("Error message code", "10110", initiateProcessResult.getErrorMessages().get(1).getMessageCode());

    }


    @Test
    public void testRepaymentOnZeroDollarFTWithNoPR() {
        //these FTs do not have PRs--was causing NPE
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WI-WT6-PAYMENT", SpcfCalendar.createInstance(2010, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.invalidateDepositFrequencies(company, "WI-WT6-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "WI-WT6-PAYMENT", DepositFrequencyCode.MONTHLY);

        DataLoadServices.setPSPDate(2012, 2, 1);
        PayrollServices.beginUnitOfWork();
        new ProcessZeroPayments().process(SpcfCalendar.createInstance(2012, 2, 1));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2012, 2, 2);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-30"));

        DataLoadServices.setPSPDate(2012, 2, 13);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("WI-WT6-PAYMENT"));

        DataLoadServices.setPSPDate(2012, 2, 18);
        DataLoadServices.returnAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("WI-WT6-PAYMENT"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("WI-WT6-PAYMENT").setRejectedOrReturned().find();
        MoneyMovementTransaction wiPayment = assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO)));
        assertSuccess(PayrollServices.paymentManager.initiateTaxRepayment(wiPayment.getId().toString(), MoneyMovementTransaction.getNextInitiationDate(PaymentMethod.ACHCredit), false));
        PayrollServices.commitUnitOfWork();

    }


    @Test
    @Ignore
    public void reinitiateEFTPSDirectDebit_CreateManualPayment() throws Exception {
        new PayrollSubmitTaxTests().test100KWithOrderedPayrolls();
        String psid = "";
        DataLoadServices.setPSPDate(2011, 2, 18);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2011, 2, 21);
        EftpsDataLoader.overridePendingToCompletedStatus();
        EftpsDataLoader.callReturnSimulator();
        EdiManager.processWaitingResponseFiles();

        DataLoadServices.setPSPDate(2011, 2, 22);
        SpcfCalendar newInitDate = SpcfCalendar.createInstance(2011, 2, 23, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2011, 2, 24, SpcfTimeZone.getLocalTimeZone());

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().
                equalTo(PaymentMethod.EFTPSDirectDebit)));

        @SuppressWarnings("unchecked") ProcessResult<MoneyMovementTransaction> updateProcessResult = PayrollServices.paymentManager.initiateTaxRepayment(moneyMovementTransaction.getId().toString(), newInitDate, false);
        assertSuccess(updateProcessResult);
        MoneyMovementTransaction newMmt = updateProcessResult.getResult();
        newMmt = Application.findById(MoneyMovementTransaction.class, newMmt.getId());
        PayrollServices.commitUnitOfWork();

        SpcfCalendar newInitiationDate = newMmt.getInitiationDate().copy();
        CalendarUtils.clearTime(newInitiationDate);
        Assert.assertEquals("Initiation Date", newInitDate.toString(), newInitiationDate.toString());
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, FinancialTransaction.MoneyMovementTransaction().equalTo(newMmt));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            SpcfCalendar settlementDate = financialTransaction.getSettlementDate().copy();
            CalendarUtils.clearTime(settlementDate);
            Assert.assertEquals("Settlement Date", newSettlementDate.toString(), settlementDate.toString());
        }
        Assert.assertEquals("Number of Financial transactions:", financialTransactions.size(), moneyMovementTransaction.getFinancialTransactionCollection().size());
        Assert.assertNotSame("New Id is generated:", moneyMovementTransaction.getId(), newMmt.getId());
        psid = newMmt.getCompany().getSourceCompanyId();

        DataLoadServices.setPSPDate(2011, 2, 23);
        // Create Customer Payment
        PayrollServices.beginUnitOfWork();
        CustomerTaxPaymentDTO customerTaxPaymentDTO = new CustomerTaxPaymentDTO();
        customerTaxPaymentDTO.setPaymentDate(new DateDTO(2011, 2, 22));
        customerTaxPaymentDTO.setQuarter(1);
        customerTaxPaymentDTO.setYear(2011);
        customerTaxPaymentDTO.setPaymentTemplateId("IRS-941-PAYMENT");
        HashMap<String, BigDecimal> lawAmounts = new HashMap<String, BigDecimal>();
        lawAmounts.put(Law.EEFICA, new BigDecimal("15000"));
        lawAmounts.put(Law.ERFICA, new BigDecimal("15000"));
        customerTaxPaymentDTO.setPaymentAmounts(lawAmounts);
        customerTaxPaymentDTO.setApplyPayments(true);
        assertSuccess(PayrollServices.payrollManager.addCustomerTaxPayment(SourceSystemCode.QBDT, psid, customerTaxPaymentDTO ));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentMethod[] paymentMethods = {PaymentMethod.HPDE};
        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentMethods(paymentMethods).find());
        Assert.assertNotNull("HPDE MMT", moneyMovementTransaction);
        DomainEntitySet<ATFPaymentsToProcess> paymentsToProcess = Application.find(ATFPaymentsToProcess.class, ATFPaymentsToProcess.MoneyMovementTransaction().equalTo(moneyMovementTransaction));
        Assert.assertEquals("Payment to process", 2, paymentsToProcess.size());
        PayrollServices.rollbackUnitOfWork();

    }

}
