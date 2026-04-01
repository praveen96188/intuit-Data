package com.intuit.sbd.payroll.psp.batchjobs.ReconPlus;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.MockSimpleSftpFile;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.eftps.EftpsDataLoader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.printedcheckfile.ReconPlusFile;
import com.intuit.sbd.payroll.psp.batchjobs.processors.ReconPlusProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.SftpFactory;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.apache.commons.io.FilenameUtils;
import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * User: mwaqarbaig
 * Date: Aug 17, 2011
 * Time: 2:09:28 PM
 */
public class TestReconPlusBatchJob {
    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();

    static {
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
        supportedPaymentTemplates.add("CA-PITSDI-PAYMENT");
        supportedPaymentTemplates.add("CA-UIETT-PAYMENT");
        supportedPaymentTemplates.add("AZ-A1-PAYMENT");
    }

    @BeforeClass
    public static void beforeClass() {
        SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        SftpFactory.setInstanceClass(Transporter.class);
    }

    @Before
    public void beforeEachTest() {
        DataLoadServices.reinitialize();
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        SystemParameter.update(SystemParameter.Code.PRINTED_CHECKS_NEXT_CHECK_NUMBER, Long.toString(1000));
        PayrollServices.commitUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2005, 1, 1));
        }
        EftpsDataLoader.deleteAllFsetTestDirFiles();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * ReconPlus test for the TaxAccount feed. Testing happy path for EFTPS and ACHDirectDeposit
     */
    @Test
    public void test_ReconPlus_TaxAccounts() {
        try {

            DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));

            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 20, SpcfTimeZone.getLocalTimeZone()));

            String psid = "123456789";
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, null);
            DataLoadServices.addCompanyBankAccount(company);
            DataLoadServices.addFederalTaxCompanyLaws(company);

            DataLoadServices.claimNoFeesOffer(company);

            PayrollServices.beginUnitOfWork();
            PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
            PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
            PayrollServices.commitUnitOfWork();
            DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

            List<Employee> emps = DataLoadServices.addEEs(company, 2, true, true);
            DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY);

            /*  Create & execute first payroll and make sure it succeeds    */
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 28, SpcfTimeZone.getLocalTimeZone()));

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-30"), emps, new String[]{"61", "66"}, new String[]{"5000", "3000"});
            payrollDTO.setPayrollTXBatchId("Payroll_1");
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();

            /*  Don't offload it */

            SpcfCalendar today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            SpcfCalendar tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateACHReconPlusFile.class, "");

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<AccountingReportFile> reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            PayrollServices.commitUnitOfWork();
            /*  Should be null since there was no offload  */
            assertEquals("AccountingReportFiles", 1, reportingFiles.size());
            File file = new File(reportingFiles.get(0).getFileName());
            assertTrue("File Exists", file.exists() && file.isFile());
            file = getUnencryptedFile(reportingFiles.get(0).getFileName());
            assertEquals("File is Zero Length", 0, file.length());

            /*  Create & execute another payroll, also offload it    */
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 29, SpcfTimeZone.getLocalTimeZone()));

            payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-07-01"), emps, new String[]{"61", "66"}, new String[]{"800", "300"});
            payrollDTO.setPayrollTXBatchId("Payroll_2");
            processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();

            /*  Offload it */
            new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateACHReconPlusFile.class, "");
            
            PayrollServices.beginUnitOfWork();
            reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            PayrollServices.commitUnitOfWork();
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            assertEncryptedFileContents(reportingFiles.get(0), Arrays.asList("911855633 000000220000Credit  20110629ACHDirectDeposi               TEST_COMPANY_1           123456789                     00000BatchJob       000000001xxxxxxxxExecuted                                                              "),
                    true);

            /*  Create & execute third payroll and make sure it succeeds    */
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 13, SpcfTimeZone.getLocalTimeZone()));

            payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-07-15"), emps, new String[]{"61", "66"}, new String[]{"2000", "4000"});
            payrollDTO.setPayrollTXBatchId("Payroll_3");
            processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();

            /*  Offload it */
            new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            /*  Test the date parsing of the job    */
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 14, SpcfTimeZone.getLocalTimeZone()));
            
            today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateACHReconPlusFile.class, "20110713");

            PayrollServices.beginUnitOfWork();
            reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            PayrollServices.commitUnitOfWork();
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            assertEncryptedFileContents(reportingFiles.get(0), Arrays.asList("911855633 000001200000Credit  20110713ACHDirectDeposi               TEST_COMPANY_1           123456789                     00000BatchJob       000000001xxxxxxxxExecuted                                                              "), true);

            /*  Now back the normal flow    */
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 13, SpcfTimeZone.getLocalTimeZone()));
            
            /*  Send 941 Payment    */
            PaymentTemplate pt941 = PayrollServices.entityFinder.findById(PaymentTemplate.class, "IRS-941-PAYMENT");
            offloadAgencyTaxCredits(pt941);

            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone()));

            today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateACHReconPlusFile.class, "");

            PayrollServices.beginUnitOfWork();
            reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            PayrollServices.rollbackUnitOfWork();
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            assertEncryptedFileContents(reportingFiles.get(0), Arrays.asList(
                    "911855633 000000600000Debit   20110729EFTPS          000000001      TEST_COMPANY_1           123456789 IRS-940-PAYMENT     20112UnitTest       000000001xxxxxxxxExecuted  FUTA: $6,000.00                                             ",
                    "911855633 000001000000Debit   20110729EFTPS          000000001      TEST_COMPANY_1           123456789 IRS-941-PAYMENT     20112UnitTest       000000001xxxxxxxxExecuted  FICA: $10,000.00                                            "),
                    true);
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone()));
            today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateACHReconPlusFile.class, "");

            PayrollServices.beginUnitOfWork();
            reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            PayrollServices.rollbackUnitOfWork();
            assertEncryptedFileContents(reportingFiles.get(0), Arrays.asList(
                    "911855633 000000860000Debit   20111028EFTPS          000000001      TEST_COMPANY_1           123456789 IRS-940-PAYMENT     20113BatchJob       000000001xxxxxxxxExecuted  FUTA: $8,600.00                                             ",
                    "911855633 000000560000Debit   20111028EFTPS          000000001      TEST_COMPANY_1           123456789 IRS-941-PAYMENT     20113BatchJob       000000001xxxxxxxxExecuted  FICA: $5,600.00                                             "
            ), true);
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * ReconPlus test for the TaxAccount feed. Testing happy path for state ACH
     *
     * @throws Exception
     */
    @Test
    public void testReconPlusWithStatePayments() throws Exception {
        String psid = "123456789";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateACHAgentEnabledFlags(company, null, true);
        DataLoadServices.updateRequiredIDs(company, null, true);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2004, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "PA-501-PAYMENT");
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", supportedDate);

        DataLoadServices.updateAgencyTaxpayerId(company, "PA-501-PAYMENT", "12245678");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25));
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(payrollRunDTO, company, new DateDTO("2010-11-02"), emps);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 11, 16, SpcfTimeZone.getLocalTimeZone()));

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, null, ACHFileType.Tax);
        PaymentTemplate pa501 = PayrollServices.entityFinder.findById(PaymentTemplate.class, "PA-501-PAYMENT");
        DataLoadServices.offloadAgencyTaxCredits(pa501);

        try {
            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateACHReconPlusFile.class, "");

            SpcfCalendar today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            SpcfCalendar tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);
            DomainEntitySet<AccountingReportFile> reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            assertEncryptedFileContents(reportingFiles.get(0), Arrays.asList("911855633 000000005000Debit   20101116ACHCredit      12245678       TEST_COMPANY_1           123456789 PA-501-PAYMENT      20104UnitTest       000000001xxxxxxxxExecuted                                                              "),
                    true);
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testReconPlus_NoPayments_Check() throws Exception {
        DataLoadServices.setPSPDate(2011, 10, 1);

        try {
            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateCheckReconPlusFile.class, "");

            SpcfCalendar today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            SpcfCalendar tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<AccountingReportFile> reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            File outputFile = new File(reportingFiles.get(0).getFileName());
            assertTrue("file exists", outputFile.exists() && outputFile.isFile());
            outputFile = getUnencryptedFile(reportingFiles.get(0).getFileName());

            assertEquals("file is zero length", 0, outputFile.length());
            assertEquals("file status", AccountingReportFileStatus.Created, reportingFiles.get(0).getStatus());
            assertEquals("file type", AccountingReportFileType.PrintedCheckReconPlus, reportingFiles.get(0).getType());
            PayrollServices.rollbackUnitOfWork();

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.UploadCheckReconPlusFiles.class, "");
            PayrollServices.beginUnitOfWork();
            reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            outputFile = new File(reportingFiles.get(0).getFileName());
            assertTrue("file exists", outputFile.exists() && outputFile.isFile());
            outputFile = getUnencryptedFile(reportingFiles.get(0).getFileName());

            assertEquals("file is zero length", 0, outputFile.length());
            assertEquals("file status", AccountingReportFileStatus.Transmitted, reportingFiles.get(0).getStatus());
            assertEquals("file type", AccountingReportFileType.PrintedCheckReconPlus, reportingFiles.get(0).getType());
            PayrollServices.rollbackUnitOfWork();

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.ArchiveReconPlusFiles.class, "");
            PayrollServices.beginUnitOfWork();
            reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            outputFile = new File(reportingFiles.get(0).getFileName());
            assertTrue("file exists", outputFile.exists() && outputFile.isFile());
            outputFile = getUnencryptedFile(reportingFiles.get(0).getFileName());

            assertEquals("file is zero length", 0, outputFile.length());
            assertEquals("file status", AccountingReportFileStatus.Archived, reportingFiles.get(0).getStatus());
            assertEquals("file type", AccountingReportFileType.PrintedCheckReconPlus, reportingFiles.get(0).getType());
            PayrollServices.rollbackUnitOfWork();
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @Test
    public void testReconPlus_NoPayments_ACH_FedEFTPS() throws Exception {
        DataLoadServices.setPSPDate(2011, 10, 1);
        try {
            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateACHReconPlusFile.class, "");

            SpcfCalendar today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            SpcfCalendar tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<AccountingReportFile> reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            File outputFile = new File(reportingFiles.get(0).getFileName());
            assertTrue("file exists", outputFile.exists() && outputFile.isFile());
            outputFile = getUnencryptedFile(reportingFiles.get(0).getFileName());

            assertEquals("file is zero length", 0, outputFile.length());
            assertEquals("file status", AccountingReportFileStatus.Created, reportingFiles.get(0).getStatus());
            assertEquals("file type", AccountingReportFileType.TaxAccountsReconPlus, reportingFiles.get(0).getType());
            PayrollServices.rollbackUnitOfWork();

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.UploadTaxAccountsReconPlusFiles.class, "");
            PayrollServices.beginUnitOfWork();
            reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            outputFile = new File(reportingFiles.get(0).getFileName());
            assertTrue("file exists", outputFile.exists() && outputFile.isFile());
            outputFile = getUnencryptedFile(reportingFiles.get(0).getFileName());

            assertEquals("file is zero length", 0, outputFile.length());
            assertEquals("file status", AccountingReportFileStatus.Transmitted, reportingFiles.get(0).getStatus());
            assertEquals("file type", AccountingReportFileType.TaxAccountsReconPlus, reportingFiles.get(0).getType());
            PayrollServices.rollbackUnitOfWork();

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.ArchiveReconPlusFiles.class, "");
            PayrollServices.beginUnitOfWork();
            reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            outputFile = new File(reportingFiles.get(0).getFileName());
            assertTrue("file exists", outputFile.exists() && outputFile.isFile());
            outputFile = getUnencryptedFile(reportingFiles.get(0).getFileName());

            assertEquals("file is zero length", 0, outputFile.length());
            assertEquals("file status", AccountingReportFileStatus.Archived, reportingFiles.get(0).getStatus());
            assertEquals("file type", AccountingReportFileType.TaxAccountsReconPlus, reportingFiles.get(0).getType());
            PayrollServices.rollbackUnitOfWork();
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
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


    @Test
    public void testReturnsReconPlus_WithDebitAndReDebitReturn() {
        try {
            String psid = "123456789";
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();

            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, null);
            DataLoadServices.addCompanyBankAccount(company);

            DataLoadServices.addFederalTaxCompanyLaws(company);
            DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

            List<Employee> emps = DataLoadServices.addEEs(company, 2);
            DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
            DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");

            /*  Create & execute first payroll and make sure it succeeds    */
            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-24"), emps, new String[]{"61", "66"}, new String[]{"5000", "3000"});
            payrollDTO.setPayrollTXBatchId("Payroll_1");
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();

            /*  Offload Transactions    */
            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            /*  Return the debit    */
            Application.beginUnitOfWork();
            PSPDate.setPSPTime("20110128000000");
            DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                            TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Executed);
            assertEquals("Number of fin txns", 1, c1FinTxns.size());
            Application.commitUnitOfWork();

            DataLoadServices.returnTxns(c1FinTxns, "R01", "NSF return");

            /*  Now execute the ReconPlus BatchJob method   */
            SpcfCalendar today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            SpcfCalendar tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateReturnsReconPlusFile.class, "");
            
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<AccountingReportFile> reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            PayrollServices.rollbackUnitOfWork();
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            assertFileContents(reportingFiles.get(0), Arrays.asList(
                    "722616695 0000016079082011012420110125ACHDirectDeposi                    TEST_COMPANY_1           123456789 UnitTest       000000001                              "
            ), false);

            /*  Now try to execute the second payroll & offload it    */
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();

            payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-31"), emps, new String[]{"61", "66"}, new String[]{"800", "300"});
            payrollDTO.setPayrollTXBatchId("Payroll_2");
            processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
            PayrollServices.commitUnitOfWork();

            /* validate errors  */
            Assert.assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);
            assertEquals("Error Code:", "1101", processResult.getMessages().get(0).getMessageCode());

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
            assertEquals("OnHold MMTs", 1, moneyMovementTransactions.size());
            assertEquals("Payment method", PaymentMethod.EFTPS, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
            PayrollServices.rollbackUnitOfWork();

            /*  Offload */
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()));

            /*  Return the re-debit  */
            Application.beginUnitOfWork();
            PSPDate.setPSPTime("20110203000000");
            c1FinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                            TransactionTypeCode.EmployerTaxRedebit, TransactionStateCode.Executed);

            assertEquals("Number of fin txns", 1, c1FinTxns.size());
            Application.commitUnitOfWork();

            DataLoadServices.returnTxns(c1FinTxns, "R01", "NSF return");

            /*  Now execute the ReconPlus BatchJob method, again  */
            today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
            tomorrow = today.copy();
            tomorrow.addDays(1);
            tomorrow.addMilliseconds(-1);

            BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateReturnsReconPlusFile.class, "");

            PayrollServices.beginUnitOfWork();
            reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
            PayrollServices.rollbackUnitOfWork();
            assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
            assertFileContents(reportingFiles.get(0), Arrays.asList("722616695 0000016079082011012820110131ACHDirectDeposi                    TEST_COMPANY_1           123456789 AchOffloadBatch000000001                              "), false);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /** Tests that MS ACHCredit payments get put in ReconPlus output */
    @Test
    public void testMS_ACHPayment() throws Exception {
        String psid = "123456789";
        SpcfCalendar supportStartDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(supportStartDate);

        String[] states = {"MS"};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(psid), 1, states, PaymentTemplateCategory.Withholding));

        DataLoadServices.setPSPDate(2011, 10, 13);

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("27", "2.7");

        DataLoadServices.runPayrollRun(company, states, lawAmounts, new DateDTO("2011-10-14"));

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("MS-M89-PAYMENT");
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 11, 10);

        DataLoadServices.offloadAgencyTaxCredits(paymentTemplate);

        BatchJobManager.runJobStep(BatchJobType.ReconPlus, ReconPlusProcessor.CreateACHReconPlusFile.class, "");

        SpcfCalendar today = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
        SpcfCalendar tomorrow = today.copy();
        tomorrow.addDays(1);
        tomorrow.addMilliseconds(-1);
        DomainEntitySet<AccountingReportFile> reportingFiles = Application.find(AccountingReportFile.class, AccountingReportFile.CreatedDate().between(today, tomorrow));
        assertEquals("Incorrect number of reportingFiles", 1, reportingFiles.size());
        assertEncryptedFileContents(reportingFiles.get(0), Arrays.asList("911855633 000000000540Debit   20111110ACHCredit      12245678       TEST_COMPANY_1           123456789 MS-M89-PAYMENT      20114UnitTest       000000001xxxxxxxxExecuted                                                              "),
                true);
    }

    //PSRV003393: Recon Plus file needs to include ACHDebit and Supercheck payment methods
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testACHDebitAndSuperCheckPayments() throws Throwable {

        String[] states_achDebit = {"AR", "HI"};
        String[] states_super = {"KY", "NH", "UT"};
        DataLoadServices.setPSPDate(2012, 1, 1);

        List<Company> companies = DataLoadServices.setupCompany(1l, 1, states_achDebit, PaymentTemplateCategory.Withholding);

        DataLoadServices.setupCompanyAgency(states_achDebit, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        DataLoadServices.setupCompanyAgency(states_super, PaymentTemplateCategory.SUI, PaymentMethod.SuperCheck);

        for (Company company : companies) {
            DataLoadServices.updateRequiredIDs(company, "HI-VP1-PAYMENT", true);
        }

        SpcfCalendar supportStartDt = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(2012, 5, 1);
        for (Company company : companies) {
            DataLoadServices.enrollEFTPS(company);
            DataLoadServices.runPayrollRun(company, states_achDebit, supportStartDt, new DateDTO("2012-05-05"), false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);
            DataLoadServices.runPayrollRun(company, states_achDebit, supportStartDt, new DateDTO("2012-05-05"), false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
            DataLoadServices.runPayrollRun(company, states_super, supportStartDt, new DateDTO("2012-05-05"), false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.setPSPDate(2012, 5, 3);

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        for (MoneyMovementTransaction mmt : MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("HI-VP1-PAYMENT").find()) {
            PayrollServices.paymentManager.changePaymentMethod(mmt.getCompany().getSourceSystemCd(), mmt.getCompany().getSourceCompanyId(), mmt.getId(), PaymentMethod.CheckPayment);
        }
        PayrollServices.commitUnitOfWork();

        List<String> paymentTemplates = Arrays.asList("AR-209B-PAYMENT", "AR-941M-PAYMENT", "HI-UCB6-PAYMENT", "HI-VP1-PAYMENT", "IRS-940-PAYMENT", "IRS-941-PAYMENT", "KY-UI3-PAYMENT", "NH-DES200-PAYMENT",
                                                      "UT-F3-PAYMENT");
        PaymentMethod[] paymentMethods = {PaymentMethod.ACHDebit, PaymentMethod.SuperCheck};
        for (String paymentTemplateCd : paymentTemplates) {
            PaymentTemplate paymentTemplate1 = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate1).setPaymentMethods(paymentMethods).find();
            PayrollServices.rollbackUnitOfWork();

            for (MoneyMovementTransaction mmt : mmts) {
                DataLoadServices.finalizePayment(mmt);
            }

            DataLoadServices.offloadAgencyTaxCredits(paymentTemplate1);

            if (mmts.size() > 0) {
                BatchJobManager.runJob(BatchJobType.AchDebitOffload);
            }
        }

        BatchJobManager.runJob(BatchJobType.ReconPlus);

        PayrollServices.beginUnitOfWork();
        AccountingReportFile reportFile = assertOne(AccountingReportFile.findByTypeAndStatus(AccountingReportFileType.PrintedCheckReconPlus, AccountingReportFileStatus.Archived, false));
        PayrollServices.rollbackUnitOfWork();
        assertFileContents(reportFile, Arrays.asList("911855708 00000000080020120503CHECK1000000001     TEST_COMPANY_1           1         AR-941M-PAYMENT     20122UnitTest       00000000100001000STATE OF ARKANSAS             " ,
                                                     "911855708 00000000260020120503CHECK12245678       TEST_COMPANY_1           1         HI-VP1-PAYMENT      20122UnitTest       00000000100001001OAHU DISTRICT OFFICE          " ,
                                                     "911855708 00000006040020120503CHECK1000000001     TEST_COMPANY_1           1         KY-UI3-PAYMENT      20122UnitTest       00000000100001002TREASURER, KENTUCKY           " ,
                                                     "911855708 00000005960020120503CHECK1000000001     TEST_COMPANY_1           1         NH-DES200-PAYMENT   20122UnitTest       00000000100001003STATE OF NEW HAMPSHIRE        " ,
                                                     "911855708 00000002560020120503CHECK1000000001     TEST_COMPANY_1           1         UT-F3-PAYMENT       20122UnitTest       00000000100001004UTAH U.C. FUND                "), false);

        PayrollServices.beginUnitOfWork();
        reportFile = assertOne(AccountingReportFile.findByTypeAndStatus(AccountingReportFileType.TaxAccountsReconPlus, AccountingReportFileStatus.Archived, false));
        PayrollServices.rollbackUnitOfWork();
        assertFileContents(reportFile, Arrays.asList("911855633 000000107100Credit  20120503ACHDirectDeposi               TEST_COMPANY_1           1                             00000UnitTest       000000001xxxxxxxxExecuted                                                              " ,
                                                     "911855633 000000156900Credit  20120503ACHDirectDeposi               TEST_COMPANY_1           1                             00000UnitTest       000000001xxxxxxxxExecuted                                                              " ,
                                                     "911855633 000000014700Credit  20120503ACHDirectDeposi               TEST_COMPANY_1           1                             00000UnitTest       000000001xxxxxxxxExecuted                                                              " ,
                                                     "911855633 000000030000Debit   20120503EFTPS          000000001      TEST_COMPANY_1           1         IRS-941-PAYMENT     20122UnitTest       000000001xxxxxxxxExecuted  FICA: $73.80, MEDICARE: $76.20, FIT: $150.00                " ,
                                                     "911855633 000000003900Debit   20120503EFTPS          000000001      TEST_COMPANY_1           1         IRS-940-PAYMENT     20122UnitTest       000000001xxxxxxxxExecuted  FUTA: $39.00                                                " ,
                                                     "911855633 000000046000Debit   20120503ACHDebit       1000000001     TEST_COMPANY_1           1         AR-209B-PAYMENT     20122UnitTest       000000001xxxxxxxxExecuted                                                              ",
                                                     "911855633 000000049800Debit   20120503ACHDebit       1000000001     TEST_COMPANY_1           1         HI-UCB6-PAYMENT     20122UnitTest       000000001xxxxxxxxExecuted                                                              "), true);



    }

    private void offloadAgencyTaxCredits(PaymentTemplate pPaymentTemplate) {
        for (MoneyMovementTransaction mmt : Application.<MoneyMovementTransaction>find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().equalTo(pPaymentTemplate).And(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)))) {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(mmt.getInitiationDate());
            PayrollServices.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
        }
    }

    private void assertFileContents(AccountingReportFile pReportFile, List<String> pFileLines, boolean maskForAccounts) throws Exception {
        assertFileContents(pReportFile.getFileName(),pFileLines,maskForAccounts);
    }
    private void assertFileContents(String fileName, List<String> pFileLines, boolean maskForAccounts) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        String line;
        Collections.sort(pFileLines);
        List<String> actualLines = new ArrayList<String>(2);
        while ((line = in.readLine()) != null) {
            String maskedLine = (maskForAccounts ? maskAccountsFKs(line) : line);
            actualLines.add(maskedLine);
        }
        Collections.sort(actualLines);
        Iterator<String> iterator = pFileLines.iterator();
        for (String actualLine : actualLines) {
            if (iterator.hasNext()) {
                Assert.assertEquals("Line does not match", iterator.next(), actualLine);
            }
            else {
                fail("Unexpected line: " + actualLine);
            }
        }
        if (iterator.hasNext()) {
            fail("Expected line not found: " + iterator.next());
        }
        in.close();
    }

    private void assertEncryptedFileContents(AccountingReportFile pReportFile, List<String> pFileLines, boolean maskForAccounts) throws Exception {
        String fileName ;
        File file = getUnencryptedFile(pReportFile.getFileName());
        fileName = file.getAbsolutePath();

        assertFileContents(fileName,pFileLines,maskForAccounts);
    }

    private String maskAccountsFKs(String pInputString) {
        int FKStartingIndex=152;
        return pInputString.substring(0, FKStartingIndex) + "xxxxxxxx" + pInputString.substring(FKStartingIndex + ReconPlusFile.MAX_FILE_KEY_LENGTH);
    }

    public static DomainEntitySet<TransactionReturn> persistTransactionReturns(DomainEntitySet<FinancialTransaction> pFinTxnList,
                                                                               String pReturnCd, String pReturnDesc) {
        return createTransactionReturns(ACHReturnsDataLoader.getMoneyMovementTransactions(pFinTxnList, false), pReturnCd, pReturnDesc);
    }

    /**
     * Builds TransactionReturns based on a set of MMTs.
     *
     * @param pSetOfMMTs
     * @param pReturnCd
     * @param pReturnDesc
     * @return
     */
    public static DomainEntitySet<TransactionReturn> createTransactionReturns(DomainEntitySet<MoneyMovementTransaction> pSetOfMMTs,
                                                                              String pReturnCd, String pReturnDesc) {
        TransactionReturnBatch batch = ACHReturnsDataLoader.createBatch();
        // for each MMT, create a TransactionReturn and add it to the batch
        DomainEntitySet<TransactionReturn> returns = new DomainEntitySet<TransactionReturn>();
        for (MoneyMovementTransaction mmt : pSetOfMMTs) {
            returns.add(createTransactionReturn(batch, mmt, pReturnCd, pReturnDesc));
        }
        return returns;
    }

    private static TransactionReturn createTransactionReturn(TransactionReturnBatch pBatch,
                                                             MoneyMovementTransaction pMMT,
                                                             String pReturnCd, String pReturnDesc) {
        TransactionReturn txnReturn = new TransactionReturn();

        txnReturn.setBankReturnCd(pReturnCd);
        txnReturn.setBankReturnDescription(pReturnDesc);
        txnReturn.setBankReturnTraceNumber(112L);
        txnReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
        txnReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
        txnReturn.setMoneyMovementTransaction(pMMT);
        txnReturn.setReturnBatch(pBatch);
        txnReturn.setCompany(pMMT.getCompany());

        txnReturn = Application.save(txnReturn);
        return txnReturn;
    }


}
