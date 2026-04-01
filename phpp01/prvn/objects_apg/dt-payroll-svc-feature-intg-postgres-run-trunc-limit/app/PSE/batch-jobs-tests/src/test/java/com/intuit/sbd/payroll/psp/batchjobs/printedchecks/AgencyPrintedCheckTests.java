package com.intuit.sbd.payroll.psp.batchjobs.printedchecks;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.MockSimpleSftpFile;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.CheckDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.LineItemDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.PayerDTO;
import com.intuit.sbd.payroll.psp.batchjobs.processors.ReconPlusProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReaderFactory;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.SftpFactory;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.sbd.payroll.psp.util.SystemParameterTestUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.lowagie.text.FontFactory;
import org.apache.commons.io.FilenameUtils;
import org.junit.*;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 11, 2011
 * Time: 2:45:41 PM
 */
public class AgencyPrintedCheckTests {
    @BeforeClass
    public static void beforeClass() {
        SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
        FontFactory.register(Application.findFileOnClassPath("checkdistribution/IDAutomationSMICR_for_testing_only.ttf"), "IDAutomationMICR");
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(null);
        SftpFactory.setInstanceClass(Transporter.class);
    }

    private boolean allowNegativeMmt;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        allowNegativeMmt = SystemParameter.findBooleanValue(SystemParameter.Code.ALLOW_NEGATIVE_MMT);

        DataLoadServices.resetAllPaymentTemplateSupportDates();
        DataLoadServices.updatePaymentTemplateSupportedDate("UT-TC96-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 6, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        deleteFiles();
        DataLoadServices.reinitialize();

        SystemParameterTestUtils.updateAndSavePrevious(SystemParameter.Code.PRINTED_CHECKS_NEXT_CHECK_NUMBER, Long.toString(1000));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();

        MockSftpTransport.setMockChannelSftp(null);
        SftpFactory.setInstanceClass(Transporter.class);
        SystemParameterTestUtils.restoreChangedSystemParameters();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPaymentSelection_MultipleTemplatesOnDifferentDays() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.updateRequiredIDs(company, "UT-TC96-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "UT-TC96-PAYMENT", false);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "UT-TC96-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-28"), emps, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"5", "12", "50.5", "45", "25", "15"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        assertEquals("payments", 2, payments.size());
        for (MoneyMovementTransaction payment : payments) {
            ProcessResult paymentMethodPR =
                    PayrollServices.paymentManager.changePaymentMethod(SourceSystemCode.QBDT,
                                                                       psid,
                                                                       payment.getId(),
                                                                       PaymentMethod.CheckPayment);

            assertSuccess(paymentMethodPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        assertEquals("payments", 2, payments.size());
        BankAccount debitBankAccount = IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_CHECK).getBankAccount();
        for (MoneyMovementTransaction payment : payments) {
            assertEquals("payment method", PaymentMethod.CheckPayment, payment.getMoneyMovementPaymentMethod());
            for (FinancialTransaction financialTransaction : payment.getFinancialTransactionCollection()) {
                assertEquals("settlement type", SettlementType.CheckType, financialTransaction.getSettlementTypeCd());
                assertEquals("debit account", debitBankAccount, financialTransaction.getDebitBankAccount());
            }
        }
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 29, SpcfTimeZone.getLocalTimeZone()));

        new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);

        PayrollServices.beginUnitOfWork();
        assertPayment("CA-PITSDI-PAYMENT", "00001000");

        DomainEntitySet<AgencyCheckBatch> agencyCheckBatches = Application.find(AgencyCheckBatch.class);
        assertEquals("check batches", 1, agencyCheckBatches.size());
        AgencyCheckBatch agencyCheckBatch = agencyCheckBatches.get(0);
        assertEquals("number of checks", 1, agencyCheckBatch.getNumberOfChecks());
        assertEquals("batch template", "CA-PITSDI-PAYMENT", agencyCheckBatch.getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("batch status", CheckPrintBatchStatus.Pending, agencyCheckBatch.getCheckPrintBatchStatusCode());
        assertEquals("association collection", 1, agencyCheckBatch.getPaymentBatchAssocCollection().size());
        assertEquals("payment template", "CA-PITSDI-PAYMENT", agencyCheckBatch.getPaymentBatchAssocCollection().get(0).getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());

        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            switch (printedCheckFile.getType()) {
                case PositivePay:
                    assertEquals("batches", 1, printedCheckFile.getPositivePayFileBatchesCollection().size());
                    break;
                case PrintedCheckReconPlus:
                    assertEquals("batches", 1, printedCheckFile.getReconPlusFileBatchesCollection().size());
                    break;
                default:
                    fail("unknown type: " + printedCheckFile.getType());
            }
            assertEquals("file status", AccountingReportFileStatus.New, printedCheckFile.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 28, SpcfTimeZone.getLocalTimeZone()));

        new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);

        PayrollServices.beginUnitOfWork();
        assertPayment("UT-TC96-PAYMENT", "00001001");

        agencyCheckBatches = Application.find(AgencyCheckBatch.class, AgencyCheckBatch.PaymentTemplate().PaymentTemplateCd().equalTo("UT-TC96-PAYMENT"));
        assertEquals("check batches", 1, agencyCheckBatches.size());
        agencyCheckBatch = agencyCheckBatches.get(0);
        assertEquals("number of checks", 1, agencyCheckBatch.getNumberOfChecks());
        assertEquals("batch template", "UT-TC96-PAYMENT", agencyCheckBatch.getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("batch status", CheckPrintBatchStatus.Pending, agencyCheckBatch.getCheckPrintBatchStatusCode());
        assertEquals("association collection", 1, agencyCheckBatch.getPaymentBatchAssocCollection().size());
        assertEquals("payment template", "UT-TC96-PAYMENT", agencyCheckBatch.getPaymentBatchAssocCollection().get(0).getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());

        printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            switch (printedCheckFile.getType()) {
                case PositivePay:
                    assertEquals("batches", 2, printedCheckFile.getPositivePayFileBatchesCollection().size());
                    break;
                case PrintedCheckReconPlus:
                    assertEquals("batches", 2, printedCheckFile.getReconPlusFileBatchesCollection().size());
                    break;
                default:
                    fail("unknown type: " + printedCheckFile.getType());
            }
            assertEquals("file status", AccountingReportFileStatus.New, printedCheckFile.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPaymentSelection_MultipleTemplatesOnSameDay() {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.updateRequiredIDs(company, "UT-TC96-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "UT-TC96-PAYMENT", false);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "UT-TC96-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-28"), emps, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"5", "12", "50.5", "45", "25", "15"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        assertEquals("payments", 2, payments.size());
        SpcfCalendar initiationDate = SpcfCalendar.createInstance(2011, 7, 28, SpcfTimeZone.getLocalTimeZone());
        for (MoneyMovementTransaction payment : payments) {
            ProcessResult paymentMethodPR =
                    PayrollServices.paymentManager.changePaymentMethod(SourceSystemCode.QBDT,
                                                                       psid,
                                                                       payment.getId(),
                                                                       PaymentMethod.CheckPayment);

            assertSuccess(paymentMethodPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class,
                                    MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        for (MoneyMovementTransaction payment : payments) {
            payment.updateTaxInitiationDate(initiationDate);
            Application.save(payment);
        }
        PayrollServices.commitUnitOfWork();

        // make sure it won't pick anything up on a day that does not hav any payments
        new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);
        DomainEntitySet<AgencyCheckBatch> agencyCheckBatches = Application.find(AgencyCheckBatch.class);
        assertEquals("check batches", 0, agencyCheckBatches.size());

        DataLoadServices.setPSPDate(initiationDate);

        new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);

        PayrollServices.beginUnitOfWork();
        assertPayment("CA-PITSDI-PAYMENT", "00001000");
        assertPayment("UT-TC96-PAYMENT", "00001001");

        agencyCheckBatches = Application.find(AgencyCheckBatch.class);
        assertEquals("check batches", 2, agencyCheckBatches.size());
        for (AgencyCheckBatch agencyCheckBatch : agencyCheckBatches) {
            if (agencyCheckBatch.getPaymentTemplate().getPaymentTemplateCd().equals("CA-PITSDI-PAYMENT")) {
                assertEquals("number of checks", 1, agencyCheckBatch.getNumberOfChecks());
                assertEquals("batch status", CheckPrintBatchStatus.Pending, agencyCheckBatch.getCheckPrintBatchStatusCode());
                assertEquals("association collection", 1, agencyCheckBatch.getPaymentBatchAssocCollection().size());
                assertEquals("payment template", "CA-PITSDI-PAYMENT", agencyCheckBatch.getPaymentBatchAssocCollection().get(0).getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());
            } else {
                assertEquals("number of checks", 1, agencyCheckBatch.getNumberOfChecks());
                assertEquals("batch status", CheckPrintBatchStatus.Pending, agencyCheckBatch.getCheckPrintBatchStatusCode());
                assertEquals("association collection", 1, agencyCheckBatch.getPaymentBatchAssocCollection().size());
                assertEquals("payment template", "UT-TC96-PAYMENT", agencyCheckBatch.getPaymentBatchAssocCollection().get(0).getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());
            }
        }

        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            switch (printedCheckFile.getType()) {
                case PositivePay:
                    assertEquals("batches", 2, printedCheckFile.getPositivePayFileBatchesCollection().size());
                    break;
                case PrintedCheckReconPlus:
                    assertEquals("batches", 2, printedCheckFile.getReconPlusFileBatchesCollection().size());
                    break;
                default:
                    fail("unknown type: " + printedCheckFile.getType());
            }
            assertEquals("file status", AccountingReportFileStatus.New, printedCheckFile.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPaymentSelection_BatchSizeSplitting() {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.updateRequiredIDs(company, "UT-TC96-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "UT-TC96-PAYMENT", false);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company);

        String psid2 = "123456788";

        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid2, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company2, null);
        DataLoadServices.addCompanyBankAccount(company2);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company2);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company2, "UT");
        DataLoadServices.updateCompanyService(company2, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps2 = DataLoadServices.addEEs(company2, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid2, "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company2);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-28"), emps, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"5", "12", "50.5", "45", "25", "15"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company2, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company2, new DateDTO("2011-06-28"), emps2, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"6", "13", "6.5", "46", "26", "16"});

        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid2, payrollDTO2);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("CA-PITSDI-PAYMENT"));
        assertEquals("payments", 2, payments.size());
        for (MoneyMovementTransaction payment : payments) {
            ProcessResult paymentMethodPR =
                    PayrollServices.paymentManager.changePaymentMethod(SourceSystemCode.QBDT,
                                                                       psid,
                                                                       payment.getId(),
                                                                       PaymentMethod.CheckPayment);

            assertSuccess(paymentMethodPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.PRINTED_CHECKS_BATCH_SIZE, Long.toString(1));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 29, SpcfTimeZone.getLocalTimeZone()));

        new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class,
                                    MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("CA-PITSDI-PAYMENT"));
        payments = payments.sort(MoneyMovementTransaction.ReferenceNumber());
        assertEquals("payments", 2, payments.size());
        int checkNumber = 1000;
        for (MoneyMovementTransaction payment : payments) {
            assertEquals("payment status", PaymentStatus.InProcess, payment.getStatus());
            assertEquals("check number", checkNumber++, Integer.parseInt(payment.getReferenceNumber()));
        }


        DomainEntitySet<AgencyCheckBatch> agencyCheckBatches = Application.find(AgencyCheckBatch.class);
        assertEquals("check batches", 2, agencyCheckBatches.size());
        for (AgencyCheckBatch agencyCheckBatch : agencyCheckBatches) {
            assertEquals("number of checks", 1, agencyCheckBatch.getNumberOfChecks());
            assertEquals("batch status", CheckPrintBatchStatus.Pending, agencyCheckBatch.getCheckPrintBatchStatusCode());
            assertEquals("association collection", 1, agencyCheckBatch.getPaymentBatchAssocCollection().size());
            assertEquals("payment template", "CA-PITSDI-PAYMENT", agencyCheckBatch.getPaymentBatchAssocCollection().get(0).getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());
        }

        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            switch (printedCheckFile.getType()) {
                case PositivePay:
                    assertEquals("batches", 2, printedCheckFile.getPositivePayFileBatchesCollection().size());
                    break;
                case PrintedCheckReconPlus:
                    assertEquals("batches", 2, printedCheckFile.getReconPlusFileBatchesCollection().size());
                    break;
                default:
                    fail("unknown type: " + printedCheckFile.getType());
            }
            assertEquals("file status", AccountingReportFileStatus.New, printedCheckFile.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.PRINTED_CHECKS_BATCH_SIZE, Long.toString(500));
        PayrollServices.commitUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPaymentSelection_CompanyPrintOrder() {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.updateRequiredIDs(company, "UT-TC96-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "UT-TC96-PAYMENT", false);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company);

        String psid2 = "123456788";

        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid2, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company2, null);
        DataLoadServices.addCompanyBankAccount(company2);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company2);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company2, "UT");
        DataLoadServices.updateCompanyService(company2, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> emps2 = DataLoadServices.addEEs(company2, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid2, "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company2);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-28"), emps, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"5", "12", "50.5", "45", "25", "15"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company2, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company2, new DateDTO("2011-06-28"), emps2, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"6", "13", "6.5", "46", "26", "16"});

        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid2, payrollDTO2);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("CA-PITSDI-PAYMENT"));
        assertEquals("payments", 2, payments.size());
        for (MoneyMovementTransaction payment : payments) {
            ProcessResult paymentMethodPR =
                    PayrollServices.paymentManager.changePaymentMethod(SourceSystemCode.QBDT,
                                                                       psid,
                                                                       payment.getId(),
                                                                       PaymentMethod.CheckPayment);

            assertSuccess(paymentMethodPR);
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 29, SpcfTimeZone.getLocalTimeZone()));

        new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class,
                                    MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("CA-PITSDI-PAYMENT"));
        payments = payments.sort(MoneyMovementTransaction.ReferenceNumber());
        assertEquals("payments", 2, payments.size());
        int checkNumber = 1000;
        for (MoneyMovementTransaction payment : payments) {
            assertEquals("payment status", PaymentStatus.InProcess, payment.getStatus());
            assertEquals("check number", checkNumber++, Integer.parseInt(payment.getReferenceNumber()));
            if (checkNumber == 1000) {
                assertEquals("company number", psid, payment.getCompany().getSourceCompanyId());
            }
        }


        DomainEntitySet<AgencyCheckBatch> agencyCheckBatches = Application.find(AgencyCheckBatch.class);
        assertEquals("check batches", 1, agencyCheckBatches.size());
        for (AgencyCheckBatch agencyCheckBatch : agencyCheckBatches) {
            assertEquals("number of checks", 2, agencyCheckBatch.getNumberOfChecks());
            assertEquals("batch status", CheckPrintBatchStatus.Pending, agencyCheckBatch.getCheckPrintBatchStatusCode());
            assertEquals("association collection", 2, agencyCheckBatch.getPaymentBatchAssocCollection().size());
            for (PaymentBatchAssoc paymentBatchAssoc : agencyCheckBatch.getPaymentBatchAssocCollection()) {
                assertEquals("payment template", "CA-PITSDI-PAYMENT", paymentBatchAssoc.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPositivePayFileGeneration_MultipleBatches() throws Exception {
        testPaymentSelection_MultipleTemplatesOnDifferentDays();

        PayrollServices.beginUnitOfWork();
        PrintedCheckFlatFileGenerator.createFile(AccountingReportFileType.PositivePay);
        PayrollServices.commitUnitOfWork();

        String outputDirectory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");
        String fileName = outputDirectory + File.separator + PrintedCheckFlatFileGenerator.POSITIVE_PAY_FILE_PREFIX + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") + PrintedCheckFlatFileGenerator.UNENCRYPTED_FILE_EXTENSION;

        assertFileContent(fileName,
                          Arrays.asList("HDR0911855708110728",
                                        "091185570800000010000000003400110701                STATE OF CALIFORNIA                               ",
                                        "091185570800000010010000010100110801                UTAH STATE TAX COMMISSION                         ",
                                        "EOF00000001350000000002"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            if (printedCheckFile.getType() == AccountingReportFileType.PositivePay) {
                assertEquals("file status", AccountingReportFileStatus.Created, printedCheckFile.getStatus());
                assertEquals("file name", fileName, printedCheckFile.getFileName());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }
    
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPositivePayFileGeneration_MultipleBatchesWithEncryption() throws Exception {
        testPaymentSelection_MultipleTemplatesOnDifferentDays();

        SystemParameterTestUtils.updateAndSavePrevious(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, "true");
        SystemParameterTestUtils.updateAndSavePrevious(SystemParameter.Code.JPMC_SKIP_SIGNATURE_VERIFICATION, "true");

        PayrollServices.beginUnitOfWork();
        PrintedCheckFlatFileGenerator.createFile(AccountingReportFileType.PositivePay);
        PayrollServices.commitUnitOfWork();

        String outputDirectory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");
        String fileName = outputDirectory + File.separator + PrintedCheckFlatFileGenerator.POSITIVE_PAY_FILE_PREFIX + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") + PrintedCheckFlatFileGenerator.ENCRYPTED_FILE_EXTENSION;

        assertFileContent(fileName,
                          Arrays.asList("HDR0911855708110728",
                                        "091185570800000010000000003400110701                STATE OF CALIFORNIA                               ",
                                        "091185570800000010010000010100110801                UTAH STATE TAX COMMISSION                         ",
                                        "EOF00000001350000000002"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            if (printedCheckFile.getType() == AccountingReportFileType.PositivePay) {
                assertEquals("file status", AccountingReportFileStatus.Created, printedCheckFile.getStatus());
                assertEquals("file name", fileName, printedCheckFile.getFileName());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPositivePayFileGeneration_IncludeVoidedCheck() throws Exception {
        System.setProperty("psp.test.email", "true");
        testPositivePayFileGeneration_MultipleBatches();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("CA-PITSDI-PAYMENT"));
        assertEquals("payments", 1, payments.size());
        MoneyMovementTransaction payment = payments.get(0);
        // todo this should call initiate repayment, but we don't have one for non eftps payments now
        VoidedCheck voidedCheck = new VoidedCheck();
        voidedCheck.setCompany(payment.getCompany());
        voidedCheck.setMoneyMovementTransaction(payment);
        Application.save(voidedCheck);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PrintedCheckFlatFileGenerator.createFile(AccountingReportFileType.PositivePay);
        PayrollServices.commitUnitOfWork();

        String outputDirectory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");
        String fileName = outputDirectory + File.separator + PrintedCheckFlatFileGenerator.POSITIVE_PAY_FILE_PREFIX + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") + PrintedCheckFlatFileGenerator.UNENCRYPTED_FILE_EXTENSION;

        assertFileContent(fileName,
                          Arrays.asList("HDR0911855708110728",
                                        "091185570800000010000000003400110701V               STATE OF CALIFORNIA                               ",
                                        "EOF00000000340000000001"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 3, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            if (printedCheckFile.getType() == AccountingReportFileType.PositivePay) {
                if (printedCheckFile.getVoidedCheckCollection().size() > 0) {
                    assertEquals("file status", AccountingReportFileStatus.Created, printedCheckFile.getStatus());
                } else {
                    assertEquals("file status", AccountingReportFileStatus.Created, printedCheckFile.getStatus());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPrintedCheckBatchProcessor() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.updateRequiredIDs(company, "UT-TC96-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "UT-TC96-PAYMENT", false);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "UT-TC96-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-28"), emps, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"5", "12", "50.5", "45", "25", "15"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        assertEquals("payments", 2, payments.size());
        SpcfCalendar initiationDate = SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone());
        for (MoneyMovementTransaction payment : payments) {
            ProcessResult paymentMethodPR =
                    PayrollServices.paymentManager.changePaymentMethod(SourceSystemCode.QBDT,
                                                                       psid,
                                                                       payment.getId(),
                                                                       PaymentMethod.CheckPayment);

            assertSuccess(paymentMethodPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class,
                                    MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        for (MoneyMovementTransaction payment : payments) {
            payment.updateTaxInitiationDate(initiationDate);
            Application.save(payment);
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(initiationDate);
        String outputDirectory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir");
        String fileName = outputDirectory + File.separator + PrintedCheckFlatFileGenerator.POSITIVE_PAY_FILE_PREFIX + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") + PrintedCheckFlatFileGenerator.UNENCRYPTED_FILE_EXTENSION;
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        assertFileContent(fileName,
                          Arrays.asList("HDR0911855708110729",
                                        "091185570800000010000000003400110802                STATE OF CALIFORNIA                               ",
                                        "091185570800000010010000010100110802                UTAH STATE TAX COMMISSION                         ",
                                        "EOF00000001350000000002"), false);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            if (printedCheckFile.getType() == AccountingReportFileType.PositivePay) {
                assertEquals("file status", AccountingReportFileStatus.Archived, printedCheckFile.getStatus());
                assertNotNull("transmitted", printedCheckFile.getTransmissionDate());

                for (CheckPrintBatch checkPrintBatch : printedCheckFile.getPositivePayFileBatchesCollection()) {
                    List<PaymentBatchAssoc> paymentBatchAssociations = PaymentBatchAssoc.findPaymentBatchAssocsByBatch((AgencyCheckBatch) checkPrintBatch, true);
                    for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                        MoneyMovementTransaction payment = paymentBatchAssociation.getMoneyMovementTransaction();
                        assertEquals("tax payment status", TaxPaymentStatus.AcknowledgedByAgency, payment.getTaxPaymentStatus());
                        assertEquals("status", PaymentStatus.Executed, payment.getStatus());

                        for (FinancialTransaction financialTransaction : payment.getFinancialTransactionCollection()) {
                            assertEquals("status", TransactionStateCode.Completed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
                        }
                    }
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testSuperCheckBatchProcessor() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("KY-K1-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("KY-UI3-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NH-DES200-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-45MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();
        Company company3 = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company2, new DateDTO("2012-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company3, new DateDTO("2012-01-20"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> suiPayments = MoneyMovementTransaction.findTaxPayments().find().find(MoneyMovementTransaction.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI));
        PayrollServices.rollbackUnitOfWork();
        for (MoneyMovementTransaction suiPayment : suiPayments) {
            DataLoadServices.finalizePayment(suiPayment);
        }

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction company3Nh = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NH-DES200-PAYMENT").setCompany(company3).find());

        PayrollServices.paymentManager.changePaymentMethod(SourceSystemCode.QBDT,
                                                           company3.getSourceCompanyId(),
                                                           company3Nh.getId(),
                                                           PaymentMethod.CheckPayment);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2012, 4, 26);
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);
        BatchJobManager.runJob(BatchJobType.ReconPlus);

        PayrollServices.beginUnitOfWork();
        AgencyCheckBatch kyBatch = findUniqueSuperCheckAgencyCheckBatchForPaymentTemplate("KY-UI3-PAYMENT");
        assertNotNull(kyBatch);
        assertTrue(kyBatch.getSuperCheck());
        assertEquals(3, kyBatch.getPaymentBatchAssocCollection().size());
        assertEquals(3, kyBatch.getNumberOfChecks());

        AgencyCheckBatch nhBatch = findUniqueSuperCheckAgencyCheckBatchForPaymentTemplate("NH-DES200-PAYMENT");
        assertNotNull(nhBatch);
        assertTrue(nhBatch.getSuperCheck());
        assertEquals(2, nhBatch.getPaymentBatchAssocCollection().size());
        assertEquals(2, nhBatch.getNumberOfChecks());

        //two super check batches, 1 check batch
        assertFileContent(kyBatch.getPositivePayFile().getFileName(),
                          Arrays.asList("HDR0911855708120426",
                                        "091185570800000010000000119200120430                STATE OF NEW HAMPSHIRE                            ",
                                        "091185570800000010010000362400120430                TREASURER, KENTUCKY                               ",
                                        "091185570800000010020000238400120430                STATE OF NEW HAMPSHIRE                            ",
                                        "091185570800000010030000322800120430                NYS EMPLOYMENT CONTRIBUTIONS & TAXES              ",
                                        "EOF00000104280000000004"), false);


        //6 entries (one for each MMT)
        assertFileContent(kyBatch.getReconPlusFile().getFileName(),
                          Arrays.asList(
                                  "911855708 00000010760020120426CHECKTEST_0001000000TEST_COMPANY_1           TEST_0001 NY-45MN-PAYMENT     20121UnitTest       00000000100001003NYS EMPLOYMENT CONTRIBUTIONS &",
                                  "911855708 00000010760020120426CHECKTEST_0002000000TEST_COMPANY_2           TEST_0002 NY-45MN-PAYMENT     20121UnitTest       00000000200001003NYS EMPLOYMENT CONTRIBUTIONS &",
                                  "911855708 00000010760020120426CHECKTEST_0003000000TEST_COMPANY_3           TEST_0003 NY-45MN-PAYMENT     20121UnitTest       00000000300001003NYS EMPLOYMENT CONTRIBUTIONS &",
                                  "911855708 00000011920020120426CHECKTEST_0001000000TEST_COMPANY_1           TEST_0001 NH-DES200-PAYMENT   20121UnitTest       00000000100001002STATE OF NEW HAMPSHIRE        ",
                                  "911855708 00000011920020120426CHECKTEST_0002000000TEST_COMPANY_2           TEST_0002 NH-DES200-PAYMENT   20121UnitTest       00000000200001002STATE OF NEW HAMPSHIRE        ",
                                  "911855708 00000011920020120426CHECKTEST_0003000000TEST_COMPANY_3           TEST_0003 NH-DES200-PAYMENT   20121UnitTest       00000000300001000STATE OF NEW HAMPSHIRE        ",
                                  "911855708 00000012080020120426CHECKTEST_0001000000TEST_COMPANY_1           TEST_0001 KY-UI3-PAYMENT      20121UnitTest       00000000100001001TREASURER, KENTUCKY           ",
                                  "911855708 00000012080020120426CHECKTEST_0002000000TEST_COMPANY_2           TEST_0002 KY-UI3-PAYMENT      20121UnitTest       00000000200001001TREASURER, KENTUCKY           ",
                                  "911855708 00000012080020120426CHECKTEST_0003000000TEST_COMPANY_3           TEST_0003 KY-UI3-PAYMENT      20121UnitTest       00000000300001001TREASURER, KENTUCKY           "),
                          true);


        PayrollServices.rollbackUnitOfWork();

        //uncomment to get PDFs
        /*
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.PRINTED_CHECKS_PRINTER_NAME, ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir") + "/test.pdf");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.createCheckPrintSignature();

        BatchJobManager.runJob(BatchJobType.CheckPrint);
        */

        DataLoadServices.setPSPDate(2012, 5, 7);
        PayrollServices.beginUnitOfWork();
        for (MoneyMovementTransaction company1Payment : MoneyMovementTransaction.findTaxPayments().setExecutedOrSuccessful().setCompany(company).find().find(MoneyMovementTransaction.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI))) {
            //just reject for company 1 as will reject for all
            assertSuccess(PayrollServices.paymentManager.rejectPayment(company1Payment.getId().toString(), "Big Problem Exception"));
        }
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        PayrollServices.beginUnitOfWork();
        Application.refresh(kyBatch);
        assertFileContent(kyBatch.getVoidedCheck().getAccountingReportFile().getFileName(),
                          Arrays.asList(
                                  "HDR0911855708120507",
                                  "091185570800000010010000362400120430V               TREASURER, KENTUCKY                               ",
                                  "091185570800000010020000238400120430V               STATE OF NEW HAMPSHIRE                            ",
                                  "091185570800000010030000322800120430V               NYS EMPLOYMENT CONTRIBUTIONS & TAXES              ",
                                  "EOF00000092360000000003"), false);
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void testSuperCheckBatchProcessorNY45MN() throws Throwable {
        SftpFactory.setInstanceClass(MockSimpleSftpFile.class);

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-45MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();
        Company company3 = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company2, new DateDTO("2012-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company3, new DateDTO("2012-01-20"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> suiPayments = MoneyMovementTransaction.findTaxPayments().find().find(MoneyMovementTransaction.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI));
        PayrollServices.rollbackUnitOfWork();
        for (MoneyMovementTransaction suiPayment : suiPayments) {
            DataLoadServices.finalizePayment(suiPayment);
        }

        PayrollServices.beginUnitOfWork();

        //Find MMT for tax payment
        PaymentMethod[] paymentMethods = {PaymentMethod.SuperCheck};
        DomainEntitySet<MoneyMovementTransaction> mmts= MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NY-45MN-PAYMENT").setPaymentMethods(paymentMethods).find();
        assertEquals("Tax payments for NY-45MN", 3, mmts.size());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2012, 4, 26);
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);
        BatchJobManager.runJob(BatchJobType.ReconPlus);

        PayrollServices.beginUnitOfWork();

        AgencyCheckBatch nyBatch = findUniqueSuperCheckAgencyCheckBatchForPaymentTemplate("NY-45MN-PAYMENT");
        assertNotNull(nyBatch);
        assertTrue(nyBatch.getSuperCheck());
        assertEquals(3, nyBatch.getPaymentBatchAssocCollection().size());
        assertEquals(3, nyBatch.getNumberOfChecks());

//        1 super check batch
        assertFileContent(nyBatch.getPositivePayFile().getFileName(),
                          Arrays.asList("HDR0911855708120426",
                                        "091185570800000010000000322800120430                NYS EMPLOYMENT CONTRIBUTIONS & TAXES              ",
                                        "EOF00000032280000000001"), false);


        //3 entries (one for each MMT)
        assertFileContent(getUnencryptedFile(nyBatch.getReconPlusFile().getFileName()).getAbsolutePath(),
                          Arrays.asList(
                                  "911855708 00000010760020120426CHECKTEST_0001000000TEST_COMPANY_1           TEST_0001 NY-45MN-PAYMENT     20121UnitTest       00000000100001000NYS EMPLOYMENT CONTRIBUTIONS &",
                                  "911855708 00000010760020120426CHECKTEST_0002000000TEST_COMPANY_2           TEST_0002 NY-45MN-PAYMENT     20121UnitTest       00000000200001000NYS EMPLOYMENT CONTRIBUTIONS &",
                                  "911855708 00000010760020120426CHECKTEST_0003000000TEST_COMPANY_3           TEST_0003 NY-45MN-PAYMENT     20121UnitTest       00000000300001000NYS EMPLOYMENT CONTRIBUTIONS &"),
                          true);


        PayrollServices.rollbackUnitOfWork();

        //uncomment to get PDFs
//
//        PayrollServices.beginUnitOfWork();
//        SystemParameter.update(SystemParameter.Code.PRINTED_CHECKS_PRINTER_NAME, ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir") + "/test.pdf");
//        PayrollServices.commitUnitOfWork();
//
//        DataLoadServices.createCheckPrintSignature();
//
//        BatchJobManager.runJob(BatchJobType.CheckPrint);


    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testSuperCheckOnlyProcessesFinalizedPayments() throws Exception {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("KY-UI3-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company2, new DateDTO("2012-01-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction company1Payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("KY-UI3-PAYMENT").setCompany(company).find());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.finalizePayment(company1Payment);

        DataLoadServices.setPSPDate(2012, 4, 26);
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company1Payment);
        MoneyMovementTransaction company2Payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("KY-UI3-PAYMENT").setCompany(company2).find());

        assertEquals(TaxPaymentStatus.AcknowledgedByAgency, company1Payment.getTaxPaymentStatus());
        assertEquals(TaxPaymentStatus.ReadyToSend, company2Payment.getTaxPaymentStatus());

    }

    @Test
    public void testSuperCheckProcessZeroDollarPayments() throws Exception {
        final String testTemplateCd = "UT-F3-PAYMENT";

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(testTemplateCd, SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-01-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction companyPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(testTemplateCd).setCompany(company).find());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2013, 1, 20, SpcfTimeZone.getLocalTimeZone()));

        // Void the payroll.
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        // DataLoadServices.finalizePayment(companyPayment);

        DataLoadServices.setPSPDate(2013, 4, 26);

        // Run the batch job.
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        // Verify database contents.
        PayrollServices.beginUnitOfWork();
        Application.refresh(companyPayment);
        assertEquals("Status", PaymentStatus.Executed, companyPayment.getStatus());
        assertEquals("Tax Payment Status", TaxPaymentStatus.AcknowledgedByAgency, companyPayment.getTaxPaymentStatus());
        assertEquals("Number of FTs", 2, companyPayment.getFinancialTransactionCollection().size());
        TransactionState completedState = Application.findById(TransactionState.class, TransactionStateCode.Completed);
        assertEquals("1st FT State", completedState, companyPayment.getFinancialTransactionCollection().get(0).getCurrentTransactionState());
        assertEquals("2nd FT State", completedState, companyPayment.getFinancialTransactionCollection().get(1).getCurrentTransactionState());
        PayrollServices.commitUnitOfWork();
    }

    private AgencyCheckBatch findUniqueSuperCheckAgencyCheckBatchForPaymentTemplate(String paymentTemplateCd) {
        AgencyCheckBatch foundBatch = null;
        for (AgencyCheckBatch agencyCheckBatch : Application.find(AgencyCheckBatch.class)) {
            if (!agencyCheckBatch.getSuperCheck()) {
                continue;
            }
            for (PaymentBatchAssoc paymentBatchAssoc : agencyCheckBatch.getPaymentBatchAssocCollection()) {
                if (paymentBatchAssoc.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd().equals(paymentTemplateCd)) {
                    if (foundBatch != null && foundBatch != agencyCheckBatch) {
                        fail("batch not unique");
                    }
                    foundBatch = agencyCheckBatch;
                    break;
                }
            }

        }
        return foundBatch;
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPrintedCheckBatchProcessor_NoUpload() throws Exception {
        String psid = "123456789";

        SystemParameterTestUtils.updateAndSavePrevious(SystemParameter.Code.PRINTED_CHECKS_UPLOAD_POSITIVE_PAY_FILES, "false");

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.updateRequiredIDs(company, "UT-TC96-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "UT-TC96-PAYMENT", false);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "UT-TC96-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-28"), emps, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"5", "12", "50.5", "45", "25", "15"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        assertEquals("payments", 2, payments.size());
        SpcfCalendar initiationDate = SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone());
        for (MoneyMovementTransaction payment : payments) {
            ProcessResult paymentMethodPR =
                    PayrollServices.paymentManager.changePaymentMethod(SourceSystemCode.QBDT,
                                                                       psid,
                                                                       payment.getId(),
                                                                       PaymentMethod.CheckPayment);

            assertSuccess(paymentMethodPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class,
                                    MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        for (MoneyMovementTransaction payment : payments) {
            payment.updateTaxInitiationDate(initiationDate);
            Application.save(payment);
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(initiationDate);
        String outputDirectory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir");
        String fileName = outputDirectory + File.separator + PrintedCheckFlatFileGenerator.POSITIVE_PAY_FILE_PREFIX + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") + PrintedCheckFlatFileGenerator.UNENCRYPTED_FILE_EXTENSION;
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        assertFileContent(fileName,
                          Arrays.asList("HDR0911855708110729",
                                        "091185570800000010000000003400110802                STATE OF CALIFORNIA                               ",
                                        "091185570800000010010000010100110802                UTAH STATE TAX COMMISSION                         ",
                                        "EOF00000001350000000002"), false);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            if (printedCheckFile.getType() == AccountingReportFileType.PositivePay) {
                assertEquals("file status", AccountingReportFileStatus.Archived, printedCheckFile.getStatus());
                assertNull("transmitted", printedCheckFile.getTransmissionDate());

                for (CheckPrintBatch checkPrintBatch : printedCheckFile.getPositivePayFileBatchesCollection()) {
                    List<PaymentBatchAssoc> paymentBatchAssociations = PaymentBatchAssoc.findPaymentBatchAssocsByBatch((AgencyCheckBatch) checkPrintBatch, true);
                    for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                        MoneyMovementTransaction payment = paymentBatchAssociation.getMoneyMovementTransaction();
                        assertEquals("tax payment status", TaxPaymentStatus.AcknowledgedByAgency, payment.getTaxPaymentStatus());
                        assertEquals("status", PaymentStatus.Executed, payment.getStatus());

                        for (FinancialTransaction financialTransaction : payment.getFinancialTransactionCollection()) {
                            assertEquals("status", TransactionStateCode.Completed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
                        }
                    }
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }
    
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testReconPlusFileGeneration_MultipleBatches() throws Exception {
        testPaymentSelection_MultipleTemplatesOnDifferentDays();

        PayrollServices.beginUnitOfWork();
        PrintedCheckFlatFileGenerator.createFile(AccountingReportFileType.PrintedCheckReconPlus);
        PayrollServices.commitUnitOfWork();

        String outputDirectory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");
        String fileName = outputDirectory + File.separator + PrintedCheckFlatFileGenerator.RECON_PLUS_FILE_PREFIX + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") + PrintedCheckFlatFileGenerator.UNENCRYPTED_FILE_EXTENSION;

        assertFileContent(fileName,
                          Arrays.asList("911855708 00000000340020110629CHECK123456789000000TEST_COMPANY_1           123456789 CA-PITSDI-PAYMENT   20112UnitTest       00000000100001000STATE OF CALIFORNIA           ",
                                        "911855708 00000001010020110728CHECK12345678123WTH TEST_COMPANY_1           123456789 UT-TC96-PAYMENT     20112UnitTest       00000000100001001UTAH STATE TAX COMMISSION     "),
                          true);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            if (printedCheckFile.getType() == AccountingReportFileType.PrintedCheckReconPlus) {
                assertEquals("file status", AccountingReportFileStatus.Created, printedCheckFile.getStatus());
                assertEquals("file name", fileName, printedCheckFile.getFileName());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testReconPlusFileGeneration_EmptyFile() throws Exception {
        PayrollServices.beginUnitOfWork();
        PrintedCheckFlatFileGenerator.createFile(AccountingReportFileType.PrintedCheckReconPlus);
        PayrollServices.commitUnitOfWork();
        String outputDirectory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");
        String fileName = outputDirectory + File.separator + PrintedCheckFlatFileGenerator.RECON_PLUS_FILE_PREFIX + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") ;
        fileName+=PrintedCheckFlatFileGenerator.ENCRYPTED_FILE_EXTENSION;
        File outputFile = getUnencryptedFile(fileName);
        assertTrue("PrintedCheckReconPlus file created", outputFile.exists());
        assertEquals("PrintedCheckReconPlus is zero length", 0, outputFile.length());
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 1, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            if (printedCheckFile.getType() == AccountingReportFileType.PrintedCheckReconPlus) {
                assertEquals("file status", AccountingReportFileStatus.Created, printedCheckFile.getStatus());
                assertEquals("file name", fileName, printedCheckFile.getFileName());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    protected File getUnencryptedFile(String file) throws Exception {
        String workingDir = FilenameUtils.getFullPath(file);
        String encryptedFileName = FilenameUtils.getName(file);
        String unencryptedFileName = FilenameUtils.getBaseName(file) + ".txt";
        PgpFileUtils.pgpDecryptUnsingedFile(workingDir
                , encryptedFileName
                , unencryptedFileName
                , BatchUtils.getConfigString("psp_tfa_intuit_private_key")
                , BatchUtils.getConfigString("psp_tfa_intuit_key_password"));
        File result = new File(workingDir + unencryptedFileName);
        return result;
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testReconPlusFileGeneration_RerunBatchJobForSameDay() throws Exception {
        testPaymentSelection_MultipleTemplatesOnDifferentDays();

        BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateCheckReconPlusFile.class, "20110629");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CheckPrintBatch> batches = CheckPrintBatch.getBatchForDate(SpcfCalendar.createInstance(2011, 6, 29));
        for (CheckPrintBatch batch : batches) {
            AccountingReportFile createdFile = batch.getReconPlusFile();
            if (createdFile != null) {
                createdFile.setStatus(AccountingReportFileStatus.Archived);
                Application.save(createdFile);
            }
        }
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateCheckReconPlusFile.class, "20110629");

        String outputDirectory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");
        String fileName = outputDirectory + File.separator + PrintedCheckFlatFileGenerator.RECON_PLUS_FILE_PREFIX + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") + PrintedCheckFlatFileGenerator.UNENCRYPTED_FILE_EXTENSION;

        assertFileContent(fileName,
                          Arrays.asList("911855708 00000000340020110629CHECK123456789000000TEST_COMPANY_1           123456789 CA-PITSDI-PAYMENT   20112UnitTest       00000000100001000STATE OF CALIFORNIA           ",
                                        "911855708 00000001010020110728CHECK12345678123WTH TEST_COMPANY_1           123456789 UT-TC96-PAYMENT     20112UnitTest       00000000100001001UTAH STATE TAX COMMISSION     "),
                          true);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            if (printedCheckFile.getType() == AccountingReportFileType.PrintedCheckReconPlus) {
                assertEquals("file status", AccountingReportFileStatus.Created, printedCheckFile.getStatus());
                assertEquals("file name", fileName, printedCheckFile.getFileName());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testReconPlusProcessor() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.updateRequiredIDs(company, "UT-TC96-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "UT-TC96-PAYMENT", false);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "UT-TC96-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-28"), emps, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"5", "12", "50.5", "45", "25", "15"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        assertEquals("payments", 2, payments.size());
        SpcfCalendar initiationDate = SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone());
        for (MoneyMovementTransaction payment : payments) {
            ProcessResult paymentMethodPR =
                    PayrollServices.paymentManager.changePaymentMethod(SourceSystemCode.QBDT,
                                                                       psid,
                                                                       payment.getId(),
                                                                       PaymentMethod.CheckPayment);

            assertSuccess(paymentMethodPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class,
                                    MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        for (MoneyMovementTransaction payment : payments) {
            payment.updateTaxInitiationDate(initiationDate);
            Application.save(payment);
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(initiationDate);
        String outputDirectory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir");
        String fileName = outputDirectory + File.separator + PrintedCheckFlatFileGenerator.RECON_PLUS_FILE_PREFIX + StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMddHHmm") + PrintedCheckFlatFileGenerator.UNENCRYPTED_FILE_EXTENSION;
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);
        BatchJobManager.runJob(BatchJobType.ReconPlus);

        assertFileContent(fileName,
                          Arrays.asList("911855708 00000000340020110729CHECK123456789000000TEST_COMPANY_1           123456789 CA-PITSDI-PAYMENT   20112UnitTest       00000000100001000STATE OF CALIFORNIA           ",
                                        "911855708 00000001010020110729CHECK12345678123WTH TEST_COMPANY_1           123456789 UT-TC96-PAYMENT     20112UnitTest       00000000100001001UTAH STATE TAX COMMISSION     "),
                          true);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 3, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            if (printedCheckFile.getType() == AccountingReportFileType.PrintedCheckReconPlus) {
                assertEquals("file status", AccountingReportFileStatus.Archived, printedCheckFile.getStatus());
                assertNotNull("transmitted", printedCheckFile.getTransmissionDate());

                for (CheckPrintBatch checkPrintBatch : printedCheckFile.getReconPlusFileBatchesCollection()) {
                    List<PaymentBatchAssoc> paymentBatchAssociations = PaymentBatchAssoc.findPaymentBatchAssocsByBatch((AgencyCheckBatch) checkPrintBatch, false);
                    for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                        MoneyMovementTransaction payment = paymentBatchAssociation.getMoneyMovementTransaction();
                        assertEquals("tax payment status", TaxPaymentStatus.AcknowledgedByAgency, payment.getTaxPaymentStatus());
                        assertEquals("status", PaymentStatus.Executed, payment.getStatus());

                        for (FinancialTransaction financialTransaction : payment.getFinancialTransactionCollection()) {
                            assertEquals("status", TransactionStateCode.Completed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
                        }
                    }
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }
    
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPaymentSelection_ZeroDollar() {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.updateRequiredIDs(company, "UT-TC96-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "UT-TC96-PAYMENT", false);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "UT-TC96-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-28"), emps, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"5", "-5", "50.5", "45", "25", "15"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        assertEquals("payments", 2, payments.size());
        SpcfCalendar initiationDate = SpcfCalendar.createInstance(2011, 7, 28, SpcfTimeZone.getLocalTimeZone());
        for (MoneyMovementTransaction payment : payments) {
            ProcessResult paymentMethodPR =
                    PayrollServices.paymentManager.changePaymentMethod(SourceSystemCode.QBDT,
                                                                       psid,
                                                                       payment.getId(),
                                                                       PaymentMethod.CheckPayment);

            assertSuccess(paymentMethodPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class,
                                    MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("CA-PITSDI-PAYMENT", "UT-TC96-PAYMENT"));
        for (MoneyMovementTransaction payment : payments) {
            payment.updateTaxInitiationDate(initiationDate);
            Application.save(payment);
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(initiationDate);

        new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class,
                                    MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("CA-PITSDI-PAYMENT"));
        assertEquals("payments", 1, payments.size());
        MoneyMovementTransaction payment = payments.get(0);
        assertEquals("payment status", PaymentStatus.Executed, payment.getStatus());
        assertNull("check number", payment.getReferenceNumber());

        assertPayment("UT-TC96-PAYMENT", "00001000");

        DomainEntitySet<AgencyCheckBatch> agencyCheckBatches = Application.find(AgencyCheckBatch.class);
        assertEquals("check batches", 1, agencyCheckBatches.size());
        for (AgencyCheckBatch agencyCheckBatch : agencyCheckBatches) {
            if (agencyCheckBatch.getPaymentTemplate().getPaymentTemplateCd().equals("CA-PITSDI-PAYMENT")) {
                assertEquals("number of checks", 1, agencyCheckBatch.getNumberOfChecks());
                assertEquals("batch status", CheckPrintBatchStatus.Pending, agencyCheckBatch.getCheckPrintBatchStatusCode());
                assertEquals("association collection", 1, agencyCheckBatch.getPaymentBatchAssocCollection().size());
                assertEquals("payment template", "CA-PITSDI-PAYMENT", agencyCheckBatch.getPaymentBatchAssocCollection().get(0).getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());
            } else {
                assertEquals("number of checks", 1, agencyCheckBatch.getNumberOfChecks());
                assertEquals("batch status", CheckPrintBatchStatus.Pending, agencyCheckBatch.getCheckPrintBatchStatusCode());
                assertEquals("association collection", 1, agencyCheckBatch.getPaymentBatchAssocCollection().size());
                assertEquals("payment template", "UT-TC96-PAYMENT", agencyCheckBatch.getPaymentBatchAssocCollection().get(0).getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());
            }
        }

        DomainEntitySet<AccountingReportFile> printedCheckFiles = Application.find(AccountingReportFile.class);
        assertEquals("check files", 2, printedCheckFiles.size());
        for (AccountingReportFile printedCheckFile : printedCheckFiles) {
            switch (printedCheckFile.getType()) {
                case PositivePay:
                    assertEquals("batches", 1, printedCheckFile.getPositivePayFileBatchesCollection().size());
                    break;
                case PrintedCheckReconPlus:
                    assertEquals("batches", 1, printedCheckFile.getReconPlusFileBatchesCollection().size());
                    break;
                default:
                    fail("unknown type: " + printedCheckFile.getType());
            }
            assertEquals("file status", AccountingReportFileStatus.New, printedCheckFile.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPaymentSelection_NoAgencyId() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");

        DataLoadServices.updateAgencyTaxpayerId(company, "CA-PITSDI-PAYMENT", null);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "CA-PITSDI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "UT-TC96-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 2));
        DataLoadServices.enrollEFTPS(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-28"), emps, new String[]{"6", "67", "47", "142", "1", "66"}, new String[]{"5", "-5", "50.5", "45", "25", "15"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in("UT-TC96-PAYMENT")));
        assertNull(payment.getMoneyMovementPaymentMethod());
        assertNotNull(payment.getActiveOnHoldReason(PaymentOnHoldReason.Enrollment));

    }

    private void assertPayment(String pPaymentTemplate, String pCheckNumber) {
        DomainEntitySet<MoneyMovementTransaction> payments;
        MoneyMovementTransaction payment;
        payments = Application.find(MoneyMovementTransaction.class,
                                    MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo(pPaymentTemplate));
        assertEquals("payments", 1, payments.size());
        payment = payments.get(0);
        assertEquals("payment status", PaymentStatus.InProcess, payment.getStatus());
        if (pCheckNumber != null) {
            assertEquals("check number", pCheckNumber, payment.getReferenceNumber());
        } else {
            assertNull("check number", payment.getReferenceNumber());
        }
    }

    @Test
    public void testSingleNameAndAddressLineCheck() throws Exception {
        byte[] pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("printedchecks/TestSingleNameAndAddress.pdf")));
        byte[] pdf = PrintManualChecks.generateManualChecks(Arrays.asList(generateCheckDTO(false)));
        assertTrue("pdf is empty", pdf.length > 0);
        assertEquals("pdfs do not match", pdfFile.length, pdf.length);
    }

    @Test
    public void testMultipleNameAndAddressLineCheck() throws Exception {
        byte[] pdfFile = getBytesFromFile(new File(Application.findFileOnClassPath("printedchecks/TestSingleNameAndAddress.pdf")));
        byte[] pdf = PrintManualChecks.generateManualChecks(Arrays.asList(generateCheckDTO(false)));
        assertTrue("pdf is empty", pdf.length > 0);
        assertEquals("pdfs do not match", pdfFile.length, pdf.length);
    }

    @Test
    public void testSoftCopyForGASUIChecks() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AZ"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 5, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }
        DataLoadServices.setPSPDate(2011, 4, 28);
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        PayrollServices.beginUnitOfWork();
        PaymentMethod[] paymentMethods = {PaymentMethod.CheckPayment};
        assertEquals("AZ-UC018-PAYMENT check Payments", 5, MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("AZ-UC018-PAYMENT"))
                                                                                   .setTaxPaymentStatuses(TaxPaymentStatus.AcknowledgedByAgency).setPaymentMethods(paymentMethods).find().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSoftCopyForNVSUIChecks() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"NV", "AZ"};
        List<Company> companies = DataLoadServices.setupCompanyWithRandomPsid(158905, 5, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        String agencyIds[]={"162920.00","229030.00","273125.00",null,"275073.00"};
        int count=0;
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }
        for (Company company : companies) {
            if(count < agencyIds.length){
                DataLoadServices.updateAgencyTaxpayerId(company,"NV-NUCS4072-PAYMENT",agencyIds[count++]);
            }
        }

        DataLoadServices.setPSPDate(2011, 4, 28);
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        PayrollServices.beginUnitOfWork();
        PaymentMethod[] paymentMethods = {PaymentMethod.CheckPayment};
        DomainEntitySet<MoneyMovementTransaction> paymentsNV = Application.find(MoneyMovementTransaction.class,
                                                                                               MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("NV-NUCS4072-PAYMENT").And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.CheckPayment)));

        assertEquals("NV-NUCS4072-PAYMENT check Payments", 1, paymentsNV.size());
        long  mCheckNumber = SystemParameter.findLongValue(SystemParameter.Code.PRINTED_CHECKS_NEXT_CHECK_NUMBER);
        long checkNumber = mCheckNumber- paymentsNV.size();
        for (MoneyMovementTransaction payment : paymentsNV) {
            assertEquals("payment status", PaymentStatus.Executed, payment.getStatus());
            assertEquals("check number", checkNumber++, Long.parseLong(payment.getReferenceNumber()));
        }

        assertEquals("AZ-UC018-PAYMENT check Payments", 5, MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("AZ-UC018-PAYMENT"))
                                                                                   .setTaxPaymentStatuses(TaxPaymentStatus.AcknowledgedByAgency).setPaymentMethods(paymentMethods).find().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSoftCopyForNVGASUIChecks() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"NV"};
        List<Company> companies = DataLoadServices.setupCompanyWithRandomPsid(158905, 5, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        String agencyNVIds[]={"162920.00","229030.00","273125.00",null,"275073.00"};
        String agencyGAIds[]={"874271-00","3067373-NF","07992808",null,"025354-02"};
        int count=0;
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }
        for (Company company : companies) {
            if(count < agencyNVIds.length){
                DataLoadServices.updateAgencyTaxpayerId(company,"NV-NUCS4072-PAYMENT",agencyNVIds[count]);
            }
            count++;
        }

        DataLoadServices.setPSPDate(2011, 4, 28);
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        //uncomment to get PDFs
        /*
        SystemParameterTestUtils.updateAndSavePrevious(SystemParameter.Code.PRINTED_CHECKS_PRINTER_NAME, ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir") + "/test.pdf");

        DataLoadServices.createCheckPrintSignature();

        BatchJobManager.runJob(BatchJobType.CheckPrint);
        */

        PayrollServices.beginUnitOfWork();
        PaymentMethod[] paymentMethods = {PaymentMethod.CheckPayment};
        DomainEntitySet<MoneyMovementTransaction> paymentsGA = Application.find(MoneyMovementTransaction.class,
                                                                                MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("GA-DOL4-PAYMENT").And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.CheckPayment)));
        paymentsGA = paymentsGA.sort(MoneyMovementTransaction.TaxPaymentStatusEffectiveDate());

        DomainEntitySet<MoneyMovementTransaction> paymentsNV = Application.find(MoneyMovementTransaction.class,
                                                                                MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("NV-NUCS4072-PAYMENT").And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.CheckPayment)));

        assertEquals("NV-NUCS4072-PAYMENT check Payments", 1, paymentsNV.size());

        //asserting sorted checks   for GA
        long  mCheckNumber = SystemParameter.findLongValue(SystemParameter.Code.PRINTED_CHECKS_NEXT_CHECK_NUMBER);
        long startCheckNumber = mCheckNumber- paymentsGA.size()- paymentsNV.size();
        for (MoneyMovementTransaction payment : paymentsGA) {
            assertEquals("payment status", PaymentStatus.Executed, payment.getStatus());
            assertEquals("check number", startCheckNumber++, Long.parseLong(payment.getReferenceNumber()));
        }

        //asserting sorted checks   for NV

        for (MoneyMovementTransaction payment : paymentsNV) {
            assertEquals("payment status", PaymentStatus.Executed, payment.getStatus());
            assertEquals("check number", startCheckNumber++, Long.parseLong(payment.getReferenceNumber()));
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSoftCopyForAKUIChecks() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AK"};
        List<Company> companies = DataLoadServices.setupCompanyWithRandomPsid(158905, 5, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        String agencyAKIds[]={"162920.00","229030.00","273125.00",null,"275073.00"};
        int count=0;
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }
        for (Company company : companies) {
            if(count < agencyAKIds.length){
                DataLoadServices.updateAgencyTaxpayerId(company,"AK-AKNS-PAYMENT",agencyAKIds[count]);
            }

            count++;
        }

        DataLoadServices.setPSPDate(2011, 4, 28);
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        //uncomment to get PDFs
        /*
        SystemParameterTestUtils.updateAndSavePrevious(SystemParameter.Code.PRINTED_CHECKS_PRINTER_NAME, ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir") + "/test.pdf");

        DataLoadServices.createCheckPrintSignature();

        BatchJobManager.runJob(BatchJobType.CheckPrint);
        */

        PayrollServices.beginUnitOfWork();
        PaymentMethod[] paymentMethods = {PaymentMethod.CheckPayment};

        DomainEntitySet<MoneyMovementTransaction> paymentsAK = Application.find(MoneyMovementTransaction.class,
                                                                                MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("AK-AKNS-PAYMENT").And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.CheckPayment)));
        paymentsAK = paymentsAK.sort(MoneyMovementTransaction.ReferenceNumber());
        assertEquals("AK-AKNS-PAYMENT check Payments", 5, paymentsAK.size());

        PaymentTemplatePrintedCheckInfo paymentTemplatePrintedCheckInfo = PaymentTemplatePrintedCheckInfo.findPaymentTemplatePrintedCheckInfo(paymentsAK.get(0).getPaymentTemplate());
        assertEquals("Agency payable name", "DOLWD EMPLOYMENT SECURITY TAX", paymentTemplatePrintedCheckInfo.getNameLine1());

        //asserting sorted checks   for AK
        long  mCheckNumber = SystemParameter.findLongValue(SystemParameter.Code.PRINTED_CHECKS_NEXT_CHECK_NUMBER);
        long startCheckNumber = mCheckNumber- paymentsAK.size();

        for (MoneyMovementTransaction payment : paymentsAK) {
            assertEquals("payment status", PaymentStatus.Executed, payment.getStatus());
            assertEquals("check number", startCheckNumber++, Long.parseLong(payment.getReferenceNumber()));
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testNegativePaymentsCheckPrinting() throws Exception {
        SystemParameterTestUtils.updateAndSavePrevious(SystemParameter.Code.ALLOW_NEGATIVE_MMT, "true");

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("KY-UI3-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateRequiredIDs(company, "CA-PITSDI-PAYMENT", false);
        DataLoadServices.updateRequiredIDs(company2, "CA-PITSDI-PAYMENT", false);


        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company2, new DateDTO("2012-01-20"));

        PayrollServices.beginUnitOfWork();
        PaymentMethod[] paymentMethods = {PaymentMethod.CheckPayment, PaymentMethod.SuperCheck};
        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findTaxPayments().setPaymentMethods(paymentMethods).find();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(mmts.find(MoneyMovementTransaction.PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT")).And(MoneyMovementTransaction.Company().equalTo(company))));
        moneyMovementTransaction.setMoneyMovementTransactionAmount((SpcfMoney) moneyMovementTransaction.getMoneyMovementTransactionAmount().multiply(new SpcfMoney("-1")));
        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();

        for (MoneyMovementTransaction mmt : mmts.find(MoneyMovementTransaction.PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate("KY-UI3-PAYMENT")))) {
            PayrollServices.beginUnitOfWork();
            if (mmt.getCompany().equals(company2)) {
                Application.refresh(mmt);
                mmt.setMoneyMovementTransactionAmount((SpcfMoney) mmt.getMoneyMovementTransactionAmount().multiply(new SpcfMoney("-1")));
                Application.save(mmt);
            }
            PayrollServices.commitUnitOfWork();
            DataLoadServices.finalizePayment(mmt);
        }

        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"));
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("KY-UI3-PAYMENT"));
        //Validated by verifying ERROR log messages.

    }

    private void setupGenericCheckDataForNVandAZ() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"NV", "AZ"};
        List<Company> companies = DataLoadServices.setupCompanyWithRandomPsid(158905, 5, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");
        String[] agencyIds = {"162920.00", "229030.00", "273125.00", null, "275073.00"};
        int count = 0;
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<>(), PaymentTemplateCategory.SUI);
        }
        for (Company company : companies) {
            if (count < agencyIds.length) {
                DataLoadServices.updateAgencyTaxpayerId(company, "NV-NUCS4072-PAYMENT", agencyIds[count++]);
            }
        }

        DataLoadServices.setPSPDate(2011, 4, 28);
        SystemParameterTestUtils.updateAndSavePrevious(SystemParameter.Code.PRINTED_CHECKS_UPLOAD_POSITIVE_PAY_FILES, "false");
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);
    }

    private void assertFileContent(String pFileName, List<String> pFileLines) throws Exception {
        assertFileContent(pFileName, pFileLines, false);
    }

    private void assertFileContent(String pFileName, List<String> pFileLines, Boolean sortLines) throws Exception {
        PgpReader reader = PgpReaderFactory.createInstance();
        reader.open(pFileName);

        String line;
        List<String> actualLines = new ArrayList<String>(2);
        while ((line = reader.readLine()) != null) {
            actualLines.add(line);
        }
        if (sortLines) {
            Collections.sort(actualLines);
        }
        Iterator<String> iterator = pFileLines.iterator();
        for (String actualLine : actualLines) {
            if (iterator.hasNext()) {
                assertEquals("Line does not match", iterator.next(), actualLine);
            } else {
                fail("Unexpected line: " + actualLine);
            }
        }
        if (iterator.hasNext()) {
            fail("Expected line not found: " + iterator.next());
        }
        reader.close();
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    @SuppressWarnings("deprecation")
    private static CheckDTO generateCheckDTO(boolean useSecondNameAndAddressLines) throws Exception {
        byte[] signature = readSignature();
        byte[] chaseLogo = readBankLogo();
        byte[] intuitLogo = readIntuitLogo();

        SourceSystemPrintedCheckInfo sourceSystemPrintedCheckInfo;
        DomainEntitySet<SourceSystemPrintedCheckInfo> sourceSystemPrintedCheckInfos = Application.find(SourceSystemPrintedCheckInfo.class,
                SourceSystemPrintedCheckInfo.SourceSystemCode().equalTo(SourceSystemCode.QBDT));
        if(sourceSystemPrintedCheckInfos.size() != 1) {
            throw new RuntimeException("Error finding check info for source system: " + SourceSystemCode.QBDT);
        }
        sourceSystemPrintedCheckInfo = sourceSystemPrintedCheckInfos.get(0);

        CheckDTO checkDTO = new CheckDTO();
        checkDTO.setCheckAmount(new BigDecimal(234185.63));
        checkDTO.setCheckDate(new Date("04/04/2011"));
        checkDTO.setCheckNumber("01478438");
        checkDTO.setCompanyLegalName("Lenox Hill Invertentional Card Company");
        checkDTO.setMemo("*Refund*");
        checkDTO.setPrintDate(new Date("03/31/2011"));
        checkDTO.setSourceCompanyNumber("336012450");
        if (useSecondNameAndAddressLines) {
            checkDTO.setTaxId("201435770 9127");
        }
        checkDTO.setFEIN("999999999");

        PayerDTO payerDTO = new PayerDTO();
        payerDTO.setAddressLine1("6888 Sierra Center Parkway");
        if (useSecondNameAndAddressLines) {
            payerDTO.setNameLine2("Is awesome!");
            payerDTO.setAddressLine2("Cube 12G23");
        }
        payerDTO.setNameLine1(sourceSystemPrintedCheckInfo.getNameLine1());
        payerDTO.setCity("Reno");
        payerDTO.setState("NV");
        payerDTO.setZip("89511-2210");
        checkDTO.setPayerDTO(payerDTO);

        PayeeDTO payeeDTO = new PayeeDTO();
        payeeDTO.setNameLine1("NYS Employement Taxes");
        if (useSecondNameAndAddressLines) {
            payeeDTO.setNameLine2("A second Name");
            payeeDTO.setAddressLine2("Box 258/4");
        }
        payeeDTO.setAddressLine1("33 Lewis Road");
        payeeDTO.setCity("Binghamton");
        payeeDTO.setState("NY");
        payeeDTO.setZip("13905-1040");
        checkDTO.setPayeeDTO(payeeDTO);

        checkDTO.setBankAccountNumber("401890838");
        checkDTO.setRoutingNumber("011309279");

        checkDTO.getPayerDTO().setSignature(signature);
        checkDTO.getPayerDTO().setBankLogo(chaseLogo);
        checkDTO.getPayerDTO().setLogo(intuitLogo);

        LineItemDTO lineItem = new LineItemDTO();
        lineItem.setAmount(new BigDecimal(999999999.99));
        lineItem.setLiabilityQuarter(1);
        lineItem.setLiabilityYear(2011);
        lineItem.setType("ST W/H");
        checkDTO.getLineItems().add(lineItem);

        lineItem = new LineItemDTO();
        lineItem.setAmount(new BigDecimal(5959.71));
        lineItem.setLiabilityQuarter(1);
        lineItem.setLiabilityYear(2011);
        lineItem.setType("NYC RE");
        checkDTO.getLineItems().add(lineItem);

        lineItem = new LineItemDTO();
        lineItem.setAmount(new BigDecimal(4.14));
        lineItem.setLiabilityQuarter(1);
        lineItem.setLiabilityYear(2011);
        lineItem.setType("123456789012345");
        checkDTO.getLineItems().add(lineItem);

        return checkDTO;
    }

    private static byte[] readIntuitLogo() throws IOException {
        RandomAccessFile rf;
        int size;
        rf = new RandomAccessFile(Application.findFileOnClassPath("checkdistribution/IntuitLogo.png"), "r");
        size = (int) rf.length();
        byte[] intuitLogo = new byte[size];
        rf.readFully(intuitLogo);
        rf.close();
        return intuitLogo;
    }

    private static byte[] readBankLogo() throws IOException {
        RandomAccessFile rf;
        int size;
        rf = new RandomAccessFile(Application.findFileOnClassPath("checkdistribution/ChaseLogo.png"), "r");
        size = (int) rf.length();
        byte[] chaseLogo = new byte[size];
        rf.readFully(chaseLogo);
        rf.close();
        return chaseLogo;
    }

    private static byte[] readSignature() throws IOException {
        RandomAccessFile rf = new RandomAccessFile(Application.findFileOnClassPath("checkdistribution/signature.png"), "r");
        int size = (int) rf.length();
        byte signature[] = new byte[size];
        rf.readFully(signature);
        rf.close();
        return signature;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private static void deleteFiles() {
        File archiveDir = new File(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir"));
        File sendDir = new File(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir"));

        File[] files = archiveDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                file.delete();
            }
        }

        files = sendDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static void createCheckPrintSignature() throws Exception {
        PayrollServices.beginUnitOfWork();
        SourceSystemPrintedCheckInfo sourceSystemPrintedCheckInfo = assertOne(Application.find(SourceSystemPrintedCheckInfo.class));
        if (sourceSystemPrintedCheckInfo.getCheckPrintSignature() == null) {
            CheckPrintSignature checkPrintSignature = new CheckPrintSignature();
            checkPrintSignature.setSourceSystemPrintedCheckInfo(sourceSystemPrintedCheckInfo);
            Application.save(checkPrintSignature);
            checkPrintSignature.setSignatureAsImage(getBytesFromFile(new File(Application.findFileOnClassPath("checkdistribution/signature.png"))));
        }
        PayrollServices.commitUnitOfWork();

    }

    // only used to generate initial test files for comparison
    /*public static void main(String[] args) {
        try {
            FontFactory.register(Application.findFileOnClassPath("checkdistribution/IDAutomationSMICR_for_testing_only.ttf"), "IDAutomationMICR");
            
            writeFile("TestSingleNameAndAddress.pdf", PrintManualChecks.generateManualChecks(Arrays.asList(generateCheckDTO(false))));

            writeFile("TestTwoNameAndAddress.pdf", PrintManualChecks.generateManualChecks(Arrays.asList(generateCheckDTO(true))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeFile (String fileName, byte[] fileContents) {

        try {
            FileOutputStream file = new FileOutputStream(fileName);
            file.write(fileContents);
            file.close();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }*/
}
