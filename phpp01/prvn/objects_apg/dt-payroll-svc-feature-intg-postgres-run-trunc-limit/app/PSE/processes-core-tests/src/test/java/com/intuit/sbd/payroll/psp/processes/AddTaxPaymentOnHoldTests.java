package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * User: rnorian
 * Date: Jan 26, 2011
 * Time: 5:06:49 PM
 */
public class AddTaxPaymentOnHoldTests {

    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();

    static {
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
        supportedPaymentTemplates.add("CA-PITSDI-PAYMENT");
        supportedPaymentTemplates.add("CA-UIETT-PAYMENT");
        supportedPaymentTemplates.add("PA-501-PAYMENT");
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2005, 1, 1));
        }
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testMultiplePayroll_NoCompletedTaxImpounds_AddCompanyHold() throws Exception {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "2", "25", "6.5", "6.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-20"), emps, new String[]{"61", "62", "63", "64", "143", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "2", "25", "6.5", "6.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-940-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud));
        assertEquals("OnHold MMTs", 2, MoneyMovementTransaction.findTaxPayments().setCompany(company).setOnHold().setNonDirect().find().size());
        assertEquals("ReadyToSend MMTs", 0, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud));
        assertEquals("OnHold MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setOnHold().setNonDirect().find().size());
        assertEquals("ReadyToSend MMTs", 2, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testIRSPaymentMultiplePayroll_OnePayrollCompletedTaxImpounds_AddCompanyHold() throws Exception {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "2", "25", "6.5", "6.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 2, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        SpcfMoney payroll1LiabilityAmt941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount();
        SpcfMoney payroll1LiabilityAmt940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-940-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-20"), emps, new String[]{"61", "62", "63", "64", "143", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "2", "25", "6.5", "6.6"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        SpcfMoney payroll2LiabilityAmt941 = new SpcfMoney(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount().subtract(payroll1LiabilityAmt941));
        SpcfMoney payroll2LiabilityAmt940 = new SpcfMoney(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount().subtract(payroll1LiabilityAmt940));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-940-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        assertEquals("MMT Amt", payroll1LiabilityAmt941.add(payroll2LiabilityAmt941), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("MMT Amt", payroll1LiabilityAmt940.add(payroll2LiabilityAmt940), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend 941 MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("ReadyToSend 940 MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("MMT 941 ReadyToSend AMT", payroll1LiabilityAmt941, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("MMT 940 ReadyToSend AMT", payroll1LiabilityAmt940, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("OnHold 941 MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertEquals("OnHold 940 MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").size());
        assertEquals("941 MMT OnHold AMT", payroll2LiabilityAmt941, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        assertEquals("940 MMT OnHold AMT", payroll2LiabilityAmt940, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // add a 2nd on hold reason - should have no effect
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.IntuitCollections));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend 941 MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("ReadyToSend 940 MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("941 MMT ReadyToSend AMT", payroll1LiabilityAmt941, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("940 MMT ReadyToSend AMT", payroll1LiabilityAmt940, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("941 OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertEquals("940 OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").size());
        assertEquals("941 MMT OnHold AMT", payroll2LiabilityAmt941, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        assertEquals("940 MMT OnHold AMT", payroll2LiabilityAmt940, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testStatePaymentMultiplePayroll_OnePayrollCompletedTaxImpounds_AddCompanyHold() throws Exception {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (CompanyAgency companyAgency : company.getCompanyAgencyCollection()) {
            DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.Agency().equalTo(companyAgency.getAgency()));
            for (PaymentTemplate paymentTemplate : paymentTemplates) {
                PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), PaymentMethod.ACHCredit, true);
            }
        }
        PayrollServices.commitUnitOfWork();


        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1", "65", "40"}, new String[]{"5", "12", "5.5", "45", "2", "25", "6.5", "75"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 3, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        SpcfMoney payroll1LiabilityAmt941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount();
        SpcfMoney payroll1LiabilityAmt940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount();
        SpcfMoney payroll1LiabilityAmtPA = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-940-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-10"), emps, new String[]{"61", "62", "63", "64", "143", "1", "65", "40"}, new String[]{"5", "12", "5.5", "45", "2", "25", "6.5", "60"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        SpcfMoney payroll2LiabilityAmt941 = new SpcfMoney(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount().subtract(payroll1LiabilityAmt941));
        SpcfMoney payroll2LiabilityAmt940 = new SpcfMoney(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount().subtract(payroll1LiabilityAmt940));
        SpcfMoney payroll2LiabilityAmtPA = new SpcfMoney(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount().subtract(payroll1LiabilityAmtPA));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-940-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        assertEquals("MMT Amt", payroll1LiabilityAmt941.add(payroll2LiabilityAmt941), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("MMT Amt", payroll1LiabilityAmt940.add(payroll2LiabilityAmt940), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().find().size());
        assertEquals("MMT Amt", payroll1LiabilityAmtPA.add(payroll2LiabilityAmtPA), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend 941 MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("ReadyToSend 940 MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("ReadyToSend PA MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("MMT 941 ReadyToSend AMT", payroll1LiabilityAmt941, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("MMT 940 ReadyToSend AMT", payroll1LiabilityAmt940, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("MMT PA ReadyToSend Amt", payroll1LiabilityAmtPA, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("OnHold 941 MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertEquals("OnHold 940 MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").size());
        assertEquals("OnHold PA MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").size());
        assertEquals("941 MMT OnHold AMT", payroll2LiabilityAmt941, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        assertEquals("940 MMT OnHold AMT", payroll2LiabilityAmt940, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        assertEquals("PA MMT OnHold AMT", payroll2LiabilityAmtPA, DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // add a 2nd on hold reason - should have no effect
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.IntuitCollections));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend 941 MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("ReadyToSend 940 MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("ReadyToSend PA MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("MMT 941 ReadyToSend AMT", payroll1LiabilityAmt941, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("MMT 940 ReadyToSend AMT", payroll1LiabilityAmt940, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("MMT PA ReadyToSend Amt", payroll1LiabilityAmtPA, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("OnHold 941 MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertEquals("OnHold 940 MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").size());
        assertEquals("OnHold PA MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").size());
        assertEquals("941 MMT OnHold AMT", payroll2LiabilityAmt941, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        assertEquals("940 MMT OnHold AMT", payroll2LiabilityAmt940, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        assertEquals("PA MMT OnHold AMT", payroll2LiabilityAmtPA, DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // remove holds
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.IntuitCollections));
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        assertEquals("MMT Amt", payroll1LiabilityAmt941.add(payroll2LiabilityAmt941), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("MMT Amt", payroll1LiabilityAmt940.add(payroll2LiabilityAmt940), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().find().size());
        assertEquals("MMT Amt", payroll1LiabilityAmtPA.add(payroll2LiabilityAmtPA), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        PayrollServices.commitUnitOfWork();

    }



    @Test
    public void testIRSPaymentMultiplePayroll_AgentHold() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"5", "12", "5.5", "45", "2", "25"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        SpcfMoney payroll1LiabilityAmt = DataLoadServices.getReadyToSendNonDirectPayments(company).get(0).getMoneyMovementTransactionAmount();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-20"), emps, new String[]{"61", "62", "63", "64", "143", "1"}, new String[]{"5", "12", "5.5", "45", "2", "25"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        SpcfMoney payroll2LiabilityAmt = new SpcfMoney(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount().subtract(payroll1LiabilityAmt));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        assertEquals("MMT Amt", payroll1LiabilityAmt.add(payroll2LiabilityAmt), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        PayrollServices.commitUnitOfWork();


        /******************************************************
         Scenario Testing
         *******************************************************/

        // 1 put a MMT on Agent hold
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0);
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmtRTS, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());

        MoneyMovementTransaction mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertTrue("MMT OnHold = Agent", mmtHold.hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertEquals("MMT OnHold AMT", payroll1LiabilityAmt.add(payroll2LiabilityAmt), mmtHold.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();


        // 2 remove Agent hold - verify back to RTS
        PayrollServices.beginUnitOfWork();
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmtHold, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0);
        assertEquals("OnHold MMTs", 0, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertEquals("MMT AMT", payroll1LiabilityAmt.add(payroll2LiabilityAmt), mmtRTS.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // 3 put Agent hold back on
        PayrollServices.beginUnitOfWork();
        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0);
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmtRTS, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());

        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertTrue("MMT OnHold = Agent", mmtHold.hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertEquals("MMT OnHold AMT", payroll1LiabilityAmt.add(payroll2LiabilityAmt), mmtHold.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // 4 add company hold (should not cause split); only change should be addition of new active on hold reason
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.FraudReview));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        PayrollServices.commitUnitOfWork();

        // 5 remove agent hold (should cause payment split)
        PayrollServices.beginUnitOfWork();
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmtHold, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());

        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0);
        assertEquals("MMT RTS AMT", payroll1LiabilityAmt, mmtRTS.getMoneyMovementTransactionAmount());
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertTrue("MMT OnHold = Company", mmtHold.hasActiveOnHoldReason(PaymentOnHoldReason.Company));
        assertEquals("MMT OnHold AMT", payroll2LiabilityAmt, mmtHold.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();


//        PayrollServices.beginUnitOfWork();
//        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.Fraud));
//        PayrollServices.commitUnitOfWork();
//
//        PayrollServices.beginUnitOfWork();
//        assertEquals("OnHold MMTs", 0, MoneyMovementTransaction.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
//        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.getReadyToSendTaxPayments(company, "IRS-941-PAYMENT").size());
//        assertEquals("MMT Amt", payroll1LiabilityAmt.add(payroll2LiabilityAmt), MoneyMovementTransaction.getReadyToSendTaxPayments(company, "IRS-941-PAYMENT").get(0).getMoneyMovementTransactionAmount());
//        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testStatePaymentMultiplePayroll_AgentHold() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (CompanyAgency companyAgency : company.getCompanyAgencyCollection()) {
            DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.Agency().equalTo(companyAgency.getAgency()));
            for (PaymentTemplate paymentTemplate : paymentTemplates) {
                PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), PaymentMethod.ACHCredit, true);
            }
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "143", "1", "40"}, new String[]{"5", "12", "5.5", "45", "2", "25", "75"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 2, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        SpcfMoney payroll1LiabilityAmt = DataLoadServices.getReadyToSendNonDirectPayments(company).find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("IRS-941-PAYMENT")).get(0).getMoneyMovementTransactionAmount();
        SpcfMoney payroll1LiabilityPAAmt = DataLoadServices.getReadyToSendNonDirectPayments(company).find(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("PA-501-PAYMENT")).get(0).getMoneyMovementTransactionAmount();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-10"), emps, new String[]{"61", "62", "63", "64", "143", "1", "40"}, new String[]{"5", "12", "5.5", "45", "2", "25", "85"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        SpcfMoney payroll2LiabilityAmt = new SpcfMoney(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount().subtract(payroll1LiabilityAmt));
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().size());
        SpcfMoney payroll2LiabilityPAAmt = new SpcfMoney(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount().subtract(payroll1LiabilityPAAmt));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        assertEquals("MMT Amt", payroll1LiabilityAmt.add(payroll2LiabilityAmt), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("MMT Amt", payroll1LiabilityPAAmt.add(payroll2LiabilityPAAmt), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        PayrollServices.commitUnitOfWork();


        /******************************************************
         Scenario Testing
         *******************************************************/

        // 1 put a MMT on Agent hold
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0);
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmtRTS, PaymentOnHoldReason.Agent));
        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().find().get(0);
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmtRTS, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());

        MoneyMovementTransaction mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertTrue("MMT OnHold = Agent", mmtHold.hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertEquals("MMT OnHold AMT", payroll1LiabilityAmt.add(payroll2LiabilityAmt), mmtHold.getMoneyMovementTransactionAmount());

        MoneyMovementTransaction mmtHoldCA = DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").get(0);
        assertTrue("MMT OnHold = Agent", mmtHoldCA.hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertEquals("MMT OnHold AMT", payroll1LiabilityPAAmt.add(payroll2LiabilityPAAmt), mmtHoldCA.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();


        // 2 remove Agent hold - verify back to RTS
        PayrollServices.beginUnitOfWork();
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmtHold, PaymentOnHoldReason.Agent));
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").get(0);
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmtHold, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0);
        assertEquals("OnHold MMTs", 0, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertEquals("MMT AMT", payroll1LiabilityAmt.add(payroll2LiabilityAmt), mmtRTS.getMoneyMovementTransactionAmount());

        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().size());
        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().get(0);
        assertEquals("MMT AMT", payroll1LiabilityPAAmt.add(payroll2LiabilityPAAmt), mmtRTS.getMoneyMovementTransactionAmount());
        assertEquals("OnHold MMTs", 0, DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").size());
        PayrollServices.rollbackUnitOfWork();

        // 3 put Agent hold back on
        PayrollServices.beginUnitOfWork();
        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0);
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmtRTS, PaymentOnHoldReason.Agent));
        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().find().get(0);
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmtRTS, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").size());

        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertTrue("MMT OnHold = Agent", mmtHold.hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertEquals("MMT OnHold AMT", payroll1LiabilityAmt.add(payroll2LiabilityAmt), mmtHold.getMoneyMovementTransactionAmount());
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").get(0);
        assertTrue("MMT OnHold = Agent", mmtHold.hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertEquals("MMT OnHold AMT", payroll1LiabilityPAAmt.add(payroll2LiabilityPAAmt), mmtHold.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // 4 add company hold (should not cause split); only change should be addition of new active on hold reason
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.FraudReview));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").size());
        PayrollServices.commitUnitOfWork();

        // 5 remove agent hold (should cause payment split)
        PayrollServices.beginUnitOfWork();
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmtHold, PaymentOnHoldReason.Agent));
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").get(0);
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmtHold, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());

        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").size());

        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0);
        assertEquals("MMT RTS AMT", payroll1LiabilityAmt, mmtRTS.getMoneyMovementTransactionAmount());
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertTrue("MMT OnHold = Company", mmtHold.hasActiveOnHoldReason(PaymentOnHoldReason.Company));
        assertEquals("MMT OnHold AMT", payroll2LiabilityAmt, mmtHold.getMoneyMovementTransactionAmount());

        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("PA-501-PAYMENT").setNonDirect().setReadyToSend().find().get(0);
        assertEquals("MMT RTS AMT", payroll1LiabilityPAAmt, mmtRTS.getMoneyMovementTransactionAmount());
        mmtHold = DataLoadServices.getOnHoldTaxPayments(company, "PA-501-PAYMENT").get(0);
        assertTrue("MMT OnHold = Company", mmtHold.hasActiveOnHoldReason(PaymentOnHoldReason.Company));
        assertEquals("MMT OnHold AMT", payroll2LiabilityPAAmt, mmtHold.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testBadArgument() {
        PayrollServices.beginUnitOfWork();
        ProcessResult pr = PayrollServices.paymentManager.addTaxPaymentOnHoldReason(null, null);
        assertEquals("bad process args", 2, pr.getMessages().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testIRSPaymentFirstPayrollInPaymentPeriodNSF() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 24);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);       

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        SpcfMoney payroll1LiabilityAmt = runPayroll1(psid, company, emps);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DataLoadServices.setPSPDate(2011, 1, 28);
        //Return the debit
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Executed);
        Application.commitUnitOfWork();
        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        DataLoadServices.returnTxns(c1FinTxns, "R01", "NSF return");

        DataLoadServices.setPSPDate(2011, 1, 31);

        ProcessResult<PayrollRun> processResult2 = runPayroll2(psid, company, emps);
        // validate error count
        Assert.assertTrue("Number of Errors:", processResult2.getMessages().size() >= 1);
        // validate error code
        assertEquals("Error Code:", "1101", processResult2.getMessages().get(0).getMessageCode());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 1, moneyMovementTransactions.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", payroll1LiabilityAmt, moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testIRSPaymentMostRecentPayrollInPaymentPeriodNSF() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 24);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        SpcfMoney payroll1LiabilityAmt = runPayroll1(psid, company, emps);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DataLoadServices.setPSPDate(2011, 1, 29);

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();

        ProcessResult<PayrollRun> processResult2 = runPayroll2(psid, company, emps);
        assertSuccess(processResult2);

        DataLoadServices.setPSPDate(2011, 1, 31);

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DataLoadServices.setPSPDate(2011, 11, 1);

        //Return the debit
        updateFinancialTransactionToNSF(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 1, moneyMovementTransactions.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", new SpcfMoney("500.00"), moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());

        DomainEntitySet<MoneyMovementTransaction> readyToSendMMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find();
        assertEquals("ReadyToSend MMTs", 1, readyToSendMMTs.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, readyToSendMMTs.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", payroll1LiabilityAmt, readyToSendMMTs.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testIRSPaymentNSFPayroll_PendingPayroll() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 24);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        SpcfMoney payroll1LiabilityAmt = runPayroll1(psid, company, emps);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DataLoadServices.setPSPDate(2011, 1, 29);

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();

        ProcessResult<PayrollRun> processResult2 = runPayroll2(psid, company, emps);
        assertSuccess(processResult2);

        PayrollRunDTO payrollRun3DTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRun3DTO);
        payrollRun3DTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRun3DTO, company, new DateDTO("2011-02-15"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"40", "40", "40", "40", "40", "24", "18"});
        ProcessResult<PayrollRun> processResult3 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRun3DTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult3);

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(3);
        Application.commitUnitOfWork();

        //NSF payroll2
        updateFinancialTransactionToNSF(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> readyToSendMMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find();
        assertEquals("ReadyToSend MMTs", 1, readyToSendMMTs.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, readyToSendMMTs.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", payroll1LiabilityAmt, readyToSendMMTs.get(0).getMoneyMovementTransactionAmount());

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 1, moneyMovementTransactions.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", new SpcfMoney("900.00"), moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testIRSPaymentPayrollTriggersDDLimitHold() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 24);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        SpcfMoney payroll1LiabilityAmt = runPayroll1(psid, company, emps);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT, psid, ServiceSubStatusCode.AS400DirectDepositLimitHold);
        PayrollServices.commitUnitOfWork();

        ProcessResult<PayrollRun> processResult2 = runPayroll2(psid, company, emps);
        // validate error count
        Assert.assertEquals("Number of Errors:", 1, processResult2.getMessages().size());
        // validate error code
        assertEquals("Error Code:", "1101", processResult2.getMessages().get(0).getMessageCode());

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 1, moneyMovementTransactions.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", payroll1LiabilityAmt, moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 29);

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = DataLoadServices.getReadyToSendTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("ReadyToSend MMTs", 1, moneyMovementTransactions.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", payroll1LiabilityAmt, moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testIRSPaymentNSFPayrollResolved() {
        String psid = "123456789";
        testIRSPaymentNSFPayroll_PendingPayroll();

        DataLoadServices.setPSPDate(2011, 1, 31);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> returnedTransactions = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Returned);

        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(returnedTransactions.get(0).getId().toString(),
                new SpcfMoney("762.00"),
                new DateDTO("2011-01-31"),
                SettlementTypeDTO.Wire);
        allRedebits.add(currRedebitImpoundDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(),
                company.getSourceCompanyId(), allRedebits);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> readyToSendMMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, readyToSendMMTs.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, readyToSendMMTs.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", new SpcfMoney("1900.00"), readyToSendMMTs.get(0).getMoneyMovementTransactionAmount());

        assertEquals("Hold MMTs", 0, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testIRSPayment100KNSFPayroll() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 24);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 180);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        SpcfMoney payroll1LiabilityAmt = runPayroll1(psid, company, emps);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DataLoadServices.setPSPDate(2011, 1, 25);

        emps.subList(60, 180).clear();
        ProcessResult<PayrollRun> processResult2 = runPayroll2(psid, company, emps);
        assertSuccess(processResult2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, moneyMovementTransactions.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", payroll1LiabilityAmt, moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());

        DomainEntitySet<MoneyMovementTransaction> eftpsDirectDebitMMTs = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.EFTPSDirectDebit);
        assertEquals("OnHold MMTs", 1, eftpsDirectDebitMMTs.size());
        assertEquals("MMTs Amount", new SpcfMoney("15000.00"), eftpsDirectDebitMMTs.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        updateFinancialTransactionToNSF(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> onHoldMMTs = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 1, onHoldMMTs.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, onHoldMMTs.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", payroll1LiabilityAmt, onHoldMMTs.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        //Todo
        //Step 4. On the pay date, the DirectDebit will be offloaded to a completed state.
        DataLoadServices.setPSPDate(2011, 1, 31);

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());
    }

    @Test
    public void testIRSPaymentAgentOnHold_CompanyOnHold_PaymentSplit() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 24);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 120);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        runPayroll1(psid, company, emps);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DataLoadServices.setPSPDate(2011, 1, 29);

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();

        emps.subList(60, 120).clear();
        ProcessResult<PayrollRun> processResult2 = runPayroll2(psid, company, emps);
        assertSuccess(processResult2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, moneyMovementTransactions.size());
        assertEquals("MMTs Amount", new SpcfMoney("75000.00"), moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(moneyMovementTransactions.get(0), PaymentOnHoldReason.Agent));
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT, psid, ServiceSubStatusCode.FraudReview));
        assertTrue("MMT OnHold = Agent", moneyMovementTransactions.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertTrue("MMT OnHold = Company", moneyMovementTransactions.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Company));
        assertEquals("Number of Holds", 2, moneyMovementTransactions.get(0).getTaxPaymentOnHoldReasonCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction onHoldMmt = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").get(0);
        assertEquals("MMTs Amount", new SpcfMoney("75000.00"), onHoldMmt.getMoneyMovementTransactionAmount());
        assertNull("MMTs Original Transaction", onHoldMmt.getOriginalTransaction());
        //remove Agent on Hold
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(onHoldMmt, PaymentOnHoldReason.Agent));
        assertTrue("MMT OnHold = Agent", !onHoldMmt.hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> readyToSendMMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find();
        DomainEntitySet<MoneyMovementTransaction> onHoldMMTs = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("ReadyToSend MMTs", 1, readyToSendMMTs.size());
        assertEquals("OnHold MMTs", 1, onHoldMMTs.size());
        assertEquals("MMTs Amount", new SpcfMoney("15000.00"), onHoldMMTs.get(0).getMoneyMovementTransactionAmount());
        assertEquals("MMTs Amount", new SpcfMoney("60000.00"), readyToSendMMTs.get(0).getMoneyMovementTransactionAmount());
        assertTrue("MMT OnHold = Company", onHoldMMTs.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Company));
        assertEquals("Number of Active Holds", 1, onHoldMMTs.get(0).getActiveOnHoldReasons().size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testIRSPaymentAgentOnHold_CompanyOnHold_PaymentCombination() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 24);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 120);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        runPayroll1(psid, company, emps);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DataLoadServices.setPSPDate(2011, 1, 29);

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();

        emps.subList(60, 120).clear();
        ProcessResult<PayrollRun> processResult2 = runPayroll2(psid, company, emps);
        assertSuccess(processResult2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, moneyMovementTransactions.size());
        assertEquals("MMTs Amount", new SpcfMoney("75000.00"), moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT, psid, ServiceSubStatusCode.FraudReview));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> readyToSendMMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find();
        assertEquals("ReadyToSend MMTs", 1, readyToSendMMTs.size());
        assertEquals("MMTs Amount", new SpcfMoney("60000.00"), readyToSendMMTs.get(0).getMoneyMovementTransactionAmount());
        DomainEntitySet<MoneyMovementTransaction> onHoldMMTs = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 1, onHoldMMTs.size());
        assertEquals("MMTs Amount", new SpcfMoney("15000.00"), onHoldMMTs.get(0).getMoneyMovementTransactionAmount());
        assertTrue("MMT OnHold = Company", onHoldMMTs.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Company));
        //Add Agent hold on both MMTs
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(readyToSendMMTs.get(0), PaymentOnHoldReason.Agent));
        assertTrue("MMT OnHold = Agent", readyToSendMMTs.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(onHoldMMTs.get(0), PaymentOnHoldReason.Agent));
        assertTrue("MMT OnHold = Agent", onHoldMMTs.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ONHold MMTs", 2, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBDT, psid, ServiceSubStatusCode.FraudReview));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> onHoldTransactions = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 2, onHoldTransactions.size());
        assertTrue("MMT OnHold = Agent", onHoldTransactions.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertTrue("MMT OnHold = Company", !onHoldTransactions.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Company));
        assertTrue("MMT OnHold = Agent", onHoldTransactions.get(1).hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertTrue("MMT OnHold = Company", !onHoldTransactions.get(1).hasActiveOnHoldReason(PaymentOnHoldReason.Company));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testIRSPaymentAgentOnHold_CompanyOnHold_EnrollmentOnHold_PaymentCombination() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 24);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 120);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        runPayroll1(psid, company, emps);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DataLoadServices.setPSPDate(2011, 1, 29);

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();

        emps.subList(60, 120).clear();
        ProcessResult<PayrollRun> processResult2 = runPayroll2(psid, company, emps);
        assertSuccess(processResult2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, moneyMovementTransactions.size());
        assertEquals("MMTs Amount", new SpcfMoney("75000.00"), moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT, psid, ServiceSubStatusCode.FraudReview));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> readyToSendMMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find();
        assertEquals("ReadyToSend MMTs", 1, readyToSendMMTs.size());
        assertEquals("MMTs Amount", new SpcfMoney("60000.00"), readyToSendMMTs.get(0).getMoneyMovementTransactionAmount());
        DomainEntitySet<MoneyMovementTransaction> onHoldMMTs = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 1, onHoldMMTs.size());
        assertEquals("MMTs Amount", new SpcfMoney("15000.00"), onHoldMMTs.get(0).getMoneyMovementTransactionAmount());
        assertTrue("MMT OnHold = Company", onHoldMMTs.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Company));

        //Add Enrollment hold on both MMTs
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(readyToSendMMTs.get(0), PaymentOnHoldReason.Enrollment));
        assertTrue("MMT OnHold = Enrollment", readyToSendMMTs.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Enrollment));
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(onHoldMMTs.get(0), PaymentOnHoldReason.Enrollment));
        assertTrue("MMT OnHold = Enrollment", onHoldMMTs.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Enrollment));
        //Add Agent hold on the MMT which has company hold
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(onHoldMMTs.get(0), PaymentOnHoldReason.Agent));
        assertTrue("MMT OnHold = Agent", onHoldMMTs.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        assertEquals("Number of holds", 3, onHoldMMTs.get(0).getTaxPaymentOnHoldReasonCollection().size());
        assertEquals("Number of holds", 1, readyToSendMMTs.get(0).getTaxPaymentOnHoldReasonCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ONHold MMTs", 2, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBDT, psid, ServiceSubStatusCode.FraudReview));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> onHoldTransactions = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 2, onHoldTransactions.size());
        assertTrue("MMT OnHold = Enrollment", onHoldTransactions.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Enrollment));
        assertTrue("MMT OnHold = Enrollment", onHoldTransactions.get(1).hasActiveOnHoldReason(PaymentOnHoldReason.Enrollment));
        if (onHoldTransactions.get(0).getActiveOnHoldReasons().size() == 1) {
            assertTrue("MMT OnHold = Agent", onHoldTransactions.get(1).hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        } else {
            assertTrue("MMT OnHold = Agent", onHoldTransactions.get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Agent));
        }
        PayrollServices.commitUnitOfWork();
    }

    //This is a invalid test case

    @Ignore
    @Test
    public void testIRSPaymentACH_CompanyHold() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.setPSPDate(2011, 1, 24);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        SpcfMoney payroll1LiabilityAmt = runPayroll1(psid, company, emps);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, moneyMovementTransactions.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", payroll1LiabilityAmt, moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());

        DataLoadServices.setPSPDate(2011, 1, 25);

        PayrollRunDTO payrollRun2DTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRun2DTO);
        payrollRun2DTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRun2DTO, company, new DateDTO("2011-01-25"), emps, new String[]{"61", "62", "63", "64", "1"}, new String[]{"50", "50", "50", "50", "50"});
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRun2DTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult2);

        DataLoadServices.setPSPDate(2011, 1, 24);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        DomainEntitySet<MoneyMovementTransaction> eftpsDirectDebitMMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, eftpsDirectDebitMMTs.size());
        assertEquals("MMTs Amount", new SpcfMoney("750.00"), eftpsDirectDebitMMTs.get(0).getMoneyMovementTransactionAmount());

        //NSF payroll2
        updateFinancialTransactionToNSF(company);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 25);
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2011, 1, 29);

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        assertEquals("OnHold MMTs", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testACH_AgentHold_PartialComplete() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2011, 1, 24);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        updateEftpsEnrollmentStatusToEnrolled();

        SpcfMoney payroll1LiabilityAmt = runPayroll1(psid, company, emps);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, moneyMovementTransactions.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", payroll1LiabilityAmt, moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 25);

        ProcessResult<PayrollRun> processResult2 = runPayroll2(psid, company, emps);
        assertSuccess(processResult2);

        // 1 put a MMT on Agent hold
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> eftpsMMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, eftpsMMTs.size());
        assertEquals("MMTs Amount", new SpcfMoney("750.00"), eftpsMMTs.get(0).getMoneyMovementTransactionAmount());
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(eftpsMMTs.get(0), PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> holdMMTs = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 1, holdMMTs.size());
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("MMTs Amount", new SpcfMoney("750.00"), holdMMTs.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 24);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2011, 1, 29);

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> holdMMTsAfterPR1Completes = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("OnHold MMTs", 1, holdMMTsAfterPR1Completes.size());
        assertEquals("ReadyToSend MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("MMTs Amount", new SpcfMoney("750.00"), holdMMTsAfterPR1Completes.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testIRSPaymentERTaxDebitReturnForMultipleTemplates_PSRV002344() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 24);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        updateEftpsEnrollmentStatusToEnrolled();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-24"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"100", "100", "100", "100", "100","65","66"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, processResult.getResult());

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 2, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        PayrollServices.rollbackUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        PayrollRunDTO payrollRun2DTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRun2DTO);
        payrollRun2DTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRun2DTO, company, new DateDTO("2011-01-31"), emps, new String[]{"61", "62", "63", "64", "1","65","66"}, new String[]{"50", "50", "50", "50", "50","11","45"});
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRun2DTO);
        assertEquals("ReadyToSend MMTs", 2, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult2);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0);
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmtRTS, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmtRTS940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0);
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmtRTS940, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 0, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        assertEquals("Hold MMTs", 2, MoneyMovementTransaction.findTaxPayments().setCompany(company).setOnHold().setNonDirect().find().size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.FraudReview));
        assertEquals("Hold MMTs", 2, MoneyMovementTransaction.findTaxPayments().setCompany(company).setOnHold().setNonDirect().find().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().find().get(0);
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmtRTS, PaymentOnHoldReason.Agent));
        //941 payment is split in to two, one has company hold and one does not
        DomainEntitySet<MoneyMovementTransaction> readyToSend941MMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setReadyToSend().setNonDirect().find();
        assertEquals("ReadyToSend 941 MMTs", 1, readyToSend941MMTs.size());
        assertEquals("ReadyToSend 941 MMT amount", new SpcfMoney("1000"), readyToSend941MMTs.get(0).getMoneyMovementTransactionAmount());
        DomainEntitySet<MoneyMovementTransaction> hold941MMTs = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("Hold 941 MMTs", 1, hold941MMTs.size());
        assertEquals("Hold 941 MMT Amount", new SpcfMoney("500"), hold941MMTs.get(0).getMoneyMovementTransactionAmount());
        //940 payment is not split
        DomainEntitySet<MoneyMovementTransaction> hold940MMTs = DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT");
        assertEquals("Hold 940 MMTs", 1, hold940MMTs.size());
        assertEquals("Hold 940 MMT Amount", new SpcfMoney("374"), hold940MMTs.get(0).getMoneyMovementTransactionAmount());
        assertEquals("ReadyToSend 940 MMTs", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        mmtRTS = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find().get(0);
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmtRTS, PaymentOnHoldReason.Agent));
        //941 payment is split in to two, one has company hold and one does not
        readyToSend941MMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find();
        assertEquals("ReadyToSend 941 MMTs", 1, readyToSend941MMTs.size());
        assertEquals("ReadyToSend 941 MMT amount", new SpcfMoney("1000"), readyToSend941MMTs.get(0).getMoneyMovementTransactionAmount());
        hold941MMTs = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("Hold 941 MMTs", 1, hold941MMTs.size());
        assertEquals("Hold 941 MMT Amount", new SpcfMoney("500"), hold941MMTs.get(0).getMoneyMovementTransactionAmount());
        //940 payment is split in to two, one has company hold and one does not
        hold940MMTs = DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT");
        assertEquals("Hold 940 MMTs", 1, hold940MMTs.size());
        assertEquals("Hold 940 MMT Amount", new SpcfMoney("112"), hold940MMTs.get(0).getMoneyMovementTransactionAmount());
        DomainEntitySet<MoneyMovementTransaction> readyToSend940MMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find();
        assertEquals("ReadyToSend 940 MMTs", 1, readyToSend940MMTs.size());
        assertEquals("ReadyToSend 940 MMT amount", new SpcfMoney("262"), readyToSend940MMTs.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testIRSPaymentNSFAfterSendingTaxPayments(){
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        updateEftpsEnrollmentStatusToEnrolled();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 16, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-04-11"), emps, new String[]{"61", "62", "63", "64", "1"}, new String[]{"100", "100", "100", "100", "100"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, processResult.getResult());

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 1, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 18, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Back dated payroll - Initiation date is moved to 4/25/2011
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 25, 2, 15, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);

        Assert.assertEquals("EftpsFile record not found.", 1, eftpsFiles.size());
        Assert.assertEquals("EftpsFile record not in PendingTransmission state.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction.TaxPaymentsFinder finder = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setPaycheckDate(processResult.getResult().getPaycheckDate());
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = finder.find();
        assertEquals("941 payments", 1, moneyMovementTransactions.size());
        assertEquals(PaymentStatus.Executed, moneyMovementTransactions.get(0).getStatus());
        PayrollServices.rollbackUnitOfWork();

        updateFinancialTransactionToNSF(company);
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = finder.find();
        assertEquals("941 payments", 1, moneyMovementTransactions.size());
        assertEquals(PaymentStatus.Executed, moneyMovementTransactions.get(0).getStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testIRSPayment_PSRV002352(){
        String psid = "123456789";
        testIRSPaymentNSFPayroll_PendingPayroll();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DomainEntitySet<FinancialTransaction> returnedTransactions = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Returned);

        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(returnedTransactions.get(0).getId().toString(),
                new SpcfMoney("762.00"),
                new DateDTO(PSPDate.getPSPTime()),
                SettlementTypeDTO.ACH);
        allRedebits.add(currRedebitImpoundDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(company.getSourceSystemCd(),
                company.getSourceCompanyId(), allRedebits);
        assertSuccess(procResult);

        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2011, 2, 15);

        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> readyToSendMMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find();
        assertEquals("ReadyToSend MMTs", 1, readyToSendMMTs.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, readyToSendMMTs.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", new SpcfMoney("1900.00"), readyToSendMMTs.get(0).getMoneyMovementTransactionAmount());
        assertEquals("Hold MMTs", 0, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());

        DomainEntitySet<MoneyMovementTransaction> readyToSend940MMTs = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find();
        assertEquals("ReadyToSend MMTs", 1, readyToSend940MMTs.size());
        assertEquals("Payment method", PaymentMethod.EFTPS, readyToSend940MMTs.get(0).getMoneyMovementPaymentMethod());
        assertEquals("MMTs Amount", new SpcfMoney("870.00"), readyToSend940MMTs.get(0).getMoneyMovementTransactionAmount());
        assertEquals("Hold MMTs", 0, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testIRSPayment_PSRV002355(){
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 21);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

        DataLoadServices.enrollEFTPS(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-22"), emps, new String[]{"61", "62", "63", "64", "1","65","66"}, new String[]{"100", "100", "100", "100", "100","130","132"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 2, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        SpcfMoney payroll1_941LiabilityAmt = new SpcfMoney("1000"); 
        SpcfMoney payroll1_940LiabilityAmt = new SpcfMoney("524"); 
        assertEquals("941 Tax payments", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        assertEquals("941 Tax payment Amount", payroll1_941LiabilityAmt, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("940 Tax payments", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("940 Tax payment Amount", payroll1_940LiabilityAmt, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 22);

        PayrollRunDTO payrollRun2DTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRun2DTO);
        payrollRun2DTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRun2DTO, company, new DateDTO("2011-01-24"), emps, new String[]{"61", "62", "63", "64", "1","65","66"}, new String[]{"50", "50", "50", "50", "50","65","66"});
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRun2DTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult2);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 2, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        assertEquals("941 Tax payments", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().size());
        SpcfMoney mmt941TotalAmount = new SpcfMoney("1500");
        assertEquals("941 Tax payment Amount", mmt941TotalAmount, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("940 Tax payments", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("940 Tax payment Amount", new SpcfMoney("786"), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 21, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 27, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //call next day payments.
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        Assert.assertEquals("No eftps files.", 1, eftpsFiles.size());
        Assert.assertEquals("EFTPS Payment file status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());
        DomainEntitySet<MoneyMovementTransaction> executedEftpsPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setExecutedOrSuccessful().find();
        assertEquals("Executed EFTPS payments", 1, executedEftpsPayments.size());
        assertEquals("Executed EFTPS payment Amount", mmt941TotalAmount, executedEftpsPayments.get(0).getMoneyMovementTransactionAmount());
        assertEquals("Executed EFTPS payment FTs", 10, executedEftpsPayments.get(0).getFinancialTransactionCollection().size());
        PayrollServices.rollbackUnitOfWork();
        

        DataLoadServices.setPSPDate(2011, 1, 28);

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processAchTransactions = new ProcessACHTransactions();
        processAchTransactions.process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();

        //NSF payroll2
        updateFinancialTransactionToNSF(company);

        PayrollServices.beginUnitOfWork();
        executedEftpsPayments = MoneyMovementTransaction.findTaxPayments().setCompany(company).setExecutedOrSuccessful().find();
        assertEquals("Executed EFTPS payments", 1, executedEftpsPayments.size());
        assertEquals("Executed EFTPS payment Amount", mmt941TotalAmount, executedEftpsPayments.get(0).getMoneyMovementTransactionAmount());
        assertEquals("Executed EFTPS payment FTs", 10, executedEftpsPayments.get(0).getFinancialTransactionCollection().size());
        assertEquals("940 Tax payments", 1, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("940 Tax payments Ready To send Amount", new SpcfMoney("524"), MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-940-PAYMENT").setNonDirect().setReadyToSend().find().get(0).getMoneyMovementTransactionAmount());
        assertEquals("940 Tax payments", 1, DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").size());
        assertTrue("940 Tax payments Hold=Company", DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").get(0).hasActiveOnHoldReason(PaymentOnHoldReason.Company));
        assertEquals("940 Tax payments on hold Amount", new SpcfMoney("262"), DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT").get(0).getMoneyMovementTransactionAmount());
        assertEquals("941 Tax payments", 0, MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().setReadyToSend().find().size());
        assertEquals("941 Tax payments", 0, DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT").size());
        PayrollServices.rollbackUnitOfWork();
        
    }

    // Submit first payroll
    // Offload ERTaxDebit and complete
    // Submit second payroll
    // Offload second payroll ERTaxDebit and do not complete
    // assert Payments are combined
    // Add agent Hold, assert payments are combined
    // Return second payroll ERTaxDebit
    // assert Payments are combined
    // Remove agent Hold
    // assert payments are split
    @Test
    public void testPaymentSplit_ERTaxCompleted_AgentHold_NSF_RemoveAgentHold(){
        SpcfCalendar supportDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(supportDate);
        String[] statesList = new String[]{"MS"};
        Company company = assertOne(DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding));
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2011-01-07"), false);

        DataLoadServices.setPSPDate(2011, 1, 5);
        DataLoadServices.runOffload();
        
        DataLoadServices.runACHTransactionProcessor();
        DataLoadServices.setPSPDate(2011, 1, 10);

        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2011-01-12"), false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRuns(company, SpcfCalendar.createInstance(2011, 1, 9, SpcfTimeZone.getLocalTimeZone()), null));

        DataLoadServices.runOffload();

        assertForCombinedPayments();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setTaxPaymentStatuses(TaxPaymentStatus.ReadyToSend).find();

        assertEquals("Number of Tax Payments", 3, moneyMovementTransactions.size());

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(moneyMovementTransaction, PaymentOnHoldReason.Agent));
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 11);
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);

        assertForCombinedPayments(true);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setTaxPaymentStatuses(TaxPaymentStatus.OnHold).find();

        assertEquals("Number of Tax Payments", 3, moneyMovementTransactions.size());

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(moneyMovementTransaction, PaymentOnHoldReason.Agent));
        }
        PayrollServices.commitUnitOfWork();
        
        assertForPaymentSplit();
    }

    // Submit first payroll
    // Offload ERTaxDebit and complete
    // Add agent Hold
    // Create an ATD via a void
    // assert Payments are combined
    @Test
    public void testPaymentDoesNotSplit_ERTaxCompleted_AgentHold_AgencyTaxDebit(){
        SpcfCalendar supportDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(supportDate);
        String[] statesList = new String[]{"MS"};
        Company company = assertOne(DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding));
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2011-01-07"), false);
        PayrollRun payrollRun1 = PayrollRun.findFirstCompanyPayrollRun(company);

        DataLoadServices.setPSPDate(2011, 1, 5);
        DataLoadServices.runOffload();

        DataLoadServices.runACHTransactionProcessor();
        DataLoadServices.setPSPDate(2011, 1, 10);

        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2011-01-12"), false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRuns(company, SpcfCalendar.createInstance(2011, 1, 9, SpcfTimeZone.getLocalTimeZone()), null));

        DataLoadServices.runOffload();

        assertForCombinedPayments();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setTaxPaymentStatuses(TaxPaymentStatus.ReadyToSend).find();

        assertEquals("Number of Tax Payments", 3, moneyMovementTransactions.size());

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(moneyMovementTransaction, PaymentOnHoldReason.Agent));
        }
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        List<String> voidPaychecks = new ArrayList<String>();
        Application.refresh(payrollRun1);
        voidPaychecks.add(payrollRun1.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(voidPaychecks);
        voidPayrollDTO.setSourcePayrollRunId(payrollRun1.getSourcePayRunId());

        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);

        assertSuccess(processResult);

        PayrollServices.commitUnitOfWork();

        //Ensure that the AgencyTaxDebit is on the
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("MS-M89-PAYMENT")).find();

        assertEquals("Number of Tax Payments", 1, moneyMovementTransactions.size());

        MoneyMovementTransaction mmt = moneyMovementTransactions.get(0);

        DomainEntitySet<FinancialTransaction> finTxns = mmt.getFinancialTransactionCollection();
        boolean foundAgencyTaxDebit = false;

        for (FinancialTransaction finTxn : finTxns) {
            TransactionTypeCode txnTypeCode = finTxn.getTransactionType().getTransactionTypeCd();
            if (txnTypeCode == TransactionTypeCode.AgencyTaxDebit) {
                foundAgencyTaxDebit=true;
            }
        }

        assertTrue(foundAgencyTaxDebit);

        PayrollServices.commitUnitOfWork();

    }

    // Submit first payroll
    // Offload ERTaxDebit and do not complete
    // Submit second payroll
    // Offload second payroll ERTaxDebit and do not complete
    // assert Payments are combined
    // Return first payroll ERTaxDebit
    // assert Payments are still combined and onHold
    // Complete the second offloaded payment
    // assert payments are split
    @Test
    public void testPaymentSplit_NSF_ERTaxCompleted(){
        SpcfCalendar supportDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(supportDate);
        String[] statesList = new String[]{"MS"};
        Company company = assertOne(DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding));
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2011-01-07"), false);
        PayrollRun payrollRun1 = PayrollRun.findFirstCompanyPayrollRun(company);

        DataLoadServices.setPSPDate(2011, 1, 5);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 1, 10);
        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2011-01-12"), false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);

        DataLoadServices.runOffload();
        assertForCombinedPayments();

        DataLoadServices.setPSPDate(2011, 1, 11);
        DataLoadServices.returnTxns(payrollRun1, TransactionTypeCode.EmployerTaxDebit);

        assertForCombinedPayments(true);

        DataLoadServices.runACHTransactionProcessor();

        assertForPaymentSplit();
    }

    // Submit first payroll
    // Offload ERTaxDebit and do not complete
    // Submit second payroll
    // Offload second payroll ERTaxDebit and do not complete
    // assert Payments are combined
    // Add agent Hold and complete first payroll ERTaxDebit transaction, assert payments are combined
    // Return second payroll ERTaxDebit
    // assert Payments are combined
    // Remove agent Hold
    // assert payments are split
    @Test
    public void testPaymentSplit_NSF_AgentHold_ERTaxCompleted_RemoveAgentHold(){
        SpcfCalendar supportDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(supportDate);
        String[] statesList = new String[]{"MS"};
        Company company = assertOne(DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.Withholding));
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2011-01-07"), false);

        DataLoadServices.setPSPDate(2011, 1, 5);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 1, 10);

        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2011-01-12"), false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRuns(company, SpcfCalendar.createInstance(2011, 1, 9, SpcfTimeZone.getLocalTimeZone()), null));

        DataLoadServices.runOffload();

        assertForCombinedPayments();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setTaxPaymentStatuses(TaxPaymentStatus.ReadyToSend).find();

        assertEquals("Number of Tax Payments", 3, moneyMovementTransactions.size());

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(moneyMovementTransaction, PaymentOnHoldReason.Agent));
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 11);
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);
        
        DataLoadServices.runACHTransactionProcessor();

        assertForCombinedPayments(true);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setTaxPaymentStatuses(TaxPaymentStatus.OnHold).find();

        assertEquals("Number of Tax Payments", 3, moneyMovementTransactions.size());

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(moneyMovementTransaction, PaymentOnHoldReason.Agent));
        }
        PayrollServices.commitUnitOfWork();

        assertForPaymentSplit();
    }

    @Test
    public void testSplittingPaymentWithATDDoesNotCreateNegativePayment() {
        //won't necessarily fail on old code as it is somewhat arbitrary.
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 1, 16);
        DataLoadServices.runACHTransactionProcessor(0);
        PayrollRun secondPayroll = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 18));
        DataLoadServices.runOffload();

        DataLoadServices.voidAPaycheck(secondPayroll);

        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.Fraud);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction.TaxPaymentsFinder finder = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940();
        assertEquals(1, finder.setReadyToSend().find().size());
        assertEquals(1, finder.setOnHold().find().size());
        PayrollServices.rollbackUnitOfWork();
    }

    private void assertForCombinedPayments(boolean isOnHold) {
        if(isOnHold) {
            assertForCombinedPayments(TaxPaymentStatus.OnHold);
        } else{
            assertForCombinedPayments();
        }

    }

    private void assertForCombinedPayments() {
        assertForCombinedPayments(TaxPaymentStatus.ReadyToSend);
    }

    private void assertForCombinedPayments(TaxPaymentStatus pTaxPaymentStatus) {
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("MS-M89-PAYMENT").find());
        assertEquals("MS MMT Status", pTaxPaymentStatus, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("MS MMT Amount", new SpcfMoney("108"), moneyMovementTransaction.getMoneyMovementTransactionAmount());

        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("IRS-941-PAYMENT").find());
        assertEquals("IRS-941 MMT Status", pTaxPaymentStatus, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("IRS-941 MMT Amount", new SpcfMoney("200"), moneyMovementTransaction.getMoneyMovementTransactionAmount());

        moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("IRS-940-PAYMENT").find());
        assertEquals("IRS-940 MMT Status", pTaxPaymentStatus, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("IRS-940 MMT Amount", new SpcfMoney("26"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    private void assertForPaymentSplit() {
        MoneyMovementTransaction moneyMovementTransaction;
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("MS-M89-PAYMENT").find();
        assertEquals("MS Payment is not split", 2, moneyMovementTransactions.size());
        moneyMovementTransaction = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)));
        assertEquals("MS MMT Amount", new SpcfMoney("54"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        moneyMovementTransaction = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)));
        assertEquals("MS MMT Amount", new SpcfMoney("54"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertNotNull("MS MMT Company Hold", moneyMovementTransaction.getActiveOnHoldReason(PaymentOnHoldReason.Company));

        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("IRS-941-PAYMENT").find();
        assertEquals("IRS-941 Payment is not split", 2, moneyMovementTransactions.size());
        moneyMovementTransaction = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)));
        assertEquals("IRS-941 MMT Amount", new SpcfMoney("100"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        moneyMovementTransaction = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)));
        assertEquals("IRS-941 MMT Amount", new SpcfMoney("100"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertNotNull("IRS-941 MMT Company Hold", moneyMovementTransaction.getActiveOnHoldReason(PaymentOnHoldReason.Company));

        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("IRS-940-PAYMENT").find();
        assertEquals("IRS-940 Payment is not split", 2, moneyMovementTransactions.size());
        moneyMovementTransaction = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)));
        assertEquals("IRS-940 MMT Amount", new SpcfMoney("13"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        moneyMovementTransaction = assertOne(moneyMovementTransactions.find(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.OnHold)));
        assertEquals("IRS-940 MMT Amount", new SpcfMoney("13"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertNotNull("IRS-940 MMT Company Hold", moneyMovementTransaction.getActiveOnHoldReason(PaymentOnHoldReason.Company));
        PayrollServices.rollbackUnitOfWork();
    }

    private void updateEftpsEnrollmentStatusToEnrolled() {
        DataLoadServices.enrollEFTPS(assertOne(Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT))));
    }

    private void updateFinancialTransactionToNSF(Company company) {
        //Return the debit
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Executed);
        Application.commitUnitOfWork();

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        DataLoadServices.returnTxns(c1FinTxns, "R01", "NSF return");
    }

    private ProcessResult<PayrollRun> runPayroll2(String psid, Company company, List<Employee> emps) {
        PayrollRunDTO payrollRun2DTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRun2DTO);
        payrollRun2DTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRun2DTO, company, new DateDTO("2011-01-31"), emps, new String[]{"61", "62", "63", "64", "1","65","66"}, new String[]{"50", "50", "50", "50", "50","65","66"});
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRun2DTO);
        PayrollServices.commitUnitOfWork();
        return processResult2;
    }

    private SpcfMoney runPayroll1(String psid, Company company, List<Employee> emps) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-24"), emps, new String[]{"61", "62", "63", "64", "1","65","66"}, new String[]{"100", "100", "100", "100", "100","130","132"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("ReadyToSend MMTs", 2, DataLoadServices.getReadyToSendNonDirectPayments(company).size());
        SpcfMoney payroll1LiabilityAmt = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IRS-941-PAYMENT").setNonDirect().find().get(0).getMoneyMovementTransactionAmount();
        PayrollServices.commitUnitOfWork();
        return payroll1LiabilityAmt;
    }

}
