package com.intuit.sbd.payroll.psp.batchjobs.DepositFrequency;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.PayrollSubmitTaxTests;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.io.File;
import java.util.ArrayList;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 4/27/12
 * Time: 5:28 PM
 */

public class IRSDepositFrequencyFileTests {

    private static String receiveDir = BatchUtils.getConfigString("psp_batch_irs_deposit_frequency_recv_dir");
    private static String archiveDir = BatchUtils.getConfigString("psp_batch_irs_deposit_frequency_archive_dir");

    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();

    static {
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
        supportedPaymentTemplates.add("CA-PITSDI-PAYMENT");
        supportedPaymentTemplates.add("CA-UIETT-PAYMENT");
    }

    @Before
    public void runBeforeEachTest() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        if (receiveDir != null) {
            File receiveDirectory = new File(receiveDir);
            for (File file : receiveDirectory.listFiles()) {
                file.delete();
            }
        }
        if (archiveDir != null) {
            File archiveDirectory = new File(archiveDir);
            for (File file : archiveDirectory.listFiles()) {
                file.delete();
            }
        }
        //setting up input file for batch job
        File inputFile = new File(archiveDir + "/../", "PDBXM.B16N26.F057.E201134.txt");
        assertNotNull("Input File", inputFile);
        BatchUtils.copyFile(inputFile.getAbsolutePath(), receiveDir);
        DataLoadServices.reinitialize(); // To reset EIN Number
        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2005, 1, 1));
        }
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Test
    public void testFileExists() {

        //Run Batch process
        BatchJobManager.runJob(BatchJobType.IRSDepositFrequencyFileProcessor);
        PayrollServices.beginUnitOfWork();
        DepositFrequencyFile depositFrequencyFile = assertOne(Application.find(DepositFrequencyFile.class));
        assertEquals("Deposit Frequency File status", DepositFrequencyFileStatus.Processed, depositFrequencyFile.getStatus());
        assertTrue("Deposit Frequency File isArchived", depositFrequencyFile.getIsArchived());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testFileDoesNotExists() {

        if (receiveDir != null) {
            File receiveDirectory = new File(receiveDir);
            for (File file : receiveDirectory.listFiles()) {
                file.delete();
            }
        }

        //Run Batch process
        BatchJobManager.runJob(BatchJobType.IRSDepositFrequencyFileProcessor);
        PayrollServices.beginUnitOfWork();
        assertTrue(Application.find(DepositFrequencyFile.class).isEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testMultipleFiles() throws Exception{

        File inputFile = new File(receiveDir, "PDBXM.B16N26.F057.E201133.txt");
        assertTrue(inputFile.createNewFile());

        //Run Batch process
        BatchJobManager.runJob(BatchJobType.IRSDepositFrequencyFileProcessor);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<DepositFrequencyFile> depositFrequencyFiles = Application.find(DepositFrequencyFile.class).sort(DepositFrequencyFile.Status());
        assertEquals(2, depositFrequencyFiles.size());

        assertEquals("Deposit Frequency File status", DepositFrequencyFileStatus.Processed, depositFrequencyFiles.get(0).getStatus());
        assertTrue("Deposit Frequency File isArchived", depositFrequencyFiles.get(0).getIsArchived());

        assertEquals("Deposit Frequency File status", DepositFrequencyFileStatus.Skipped, depositFrequencyFiles.get(1).getStatus());
        assertTrue("Deposit Frequency File isArchived", depositFrequencyFiles.get(1).getIsArchived());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test @Ignore
    public void testFileParsing() {

        //Run Batch process
        BatchJobManager.runJob(BatchJobType.IRSDepositFrequencyFileProcessor);
        PayrollServices.beginUnitOfWork();
        DepositFrequencyFile depositFrequencyFile = assertOne(Application.find(DepositFrequencyFile.class));
        assertEquals("Deposit Frequency File status", DepositFrequencyFileStatus.Processed, depositFrequencyFile.getStatus());
        assertEquals("Deposit Frequency File Records", 11, depositFrequencyFile.getDepositFrequencyFileRecCollection().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test @Ignore
    public void testFileParsingWithCompany_ThresholdExceeded_FrequencyNotUpdated() {
        new PayrollSubmitTaxTests().testThreshold_NY_MTA305_PSRV003105();

        DataLoadServices.setPSPDate(2012, 1, 24);
        //Run Batch process
        BatchJobManager.runJob(BatchJobType.IRSDepositFrequencyFileProcessor);

        PayrollServices.beginUnitOfWork();
        Company company = assertOne(Application.find(Company.class).find(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        DepositFrequencyFile depositFrequencyFile = assertOne(Application.find(DepositFrequencyFile.class, DepositFrequencyFile.Status().equalTo(DepositFrequencyFileStatus.Processed)));
        assertEquals("Deposit Frequency File status", DepositFrequencyFileStatus.Processed, depositFrequencyFile.getStatus());
        assertEquals("Deposit Frequency File Records", 11, depositFrequencyFile.getDepositFrequencyFileRecCollection().size());
        assertEquals("Deposit Frequency File Skipped Records ", 9, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.SkippedCompanyDoesNotExist)).size());
        assertEquals("Deposit Frequency File Invalid data Records ", 1, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.InvalidData)).size());
        assertEquals("Deposit Frequency File Processed Records ", 1, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.SkippedUpdating)).size());
        assertEquals("Deposit Frequency File Processed Record EIN ", company.getFedTaxId(),
                depositFrequencyFile.getDepositFrequencyFileRecCollection().find(DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.SkippedUpdating)).get(0).getEIN());
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, PaymentTemplate.getIRS_941(), PSPDate.getPSPTime());
        assertEquals("Effective Deposit Frequency code", DepositFrequencyCode.SEMIWEEKLY, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        assertEquals("Effective Deposit Frequency Effective Date", SpcfCalendar.createInstance(2012, 1, 22, SpcfTimeZone.getLocalTimeZone()), effectiveDepositFrequency.getEffectiveDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test @Ignore
    public void testFileParsingWithCompanySetup_ThresholdReversed_FrequencyUpdated() {

        new PayrollSubmitTaxTests().testThreshold_NY_PSRV003109();

        DataLoadServices.setPSPDate(2012, 5, 2);

        //Run Batch process
        BatchJobManager.runJob(BatchJobType.IRSDepositFrequencyFileProcessor);

        PayrollServices.beginUnitOfWork();
        Company company = assertOne(Application.find(Company.class).find(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        DepositFrequencyFile depositFrequencyFile = assertOne(Application.find(DepositFrequencyFile.class));
        assertEquals("Deposit Frequency File status", DepositFrequencyFileStatus.Processed, depositFrequencyFile.getStatus());
        assertEquals("Deposit Frequency File Records", 11, depositFrequencyFile.getDepositFrequencyFileRecCollection().size());
        assertEquals("Deposit Frequency File Skipped Records ", 9, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.SkippedCompanyDoesNotExist)).size());
        assertEquals("Deposit Frequency File Invalid data Records ", 1, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.InvalidData)).size());
        assertEquals("Deposit Frequency File Processed Records ", 1, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.Processed)).size());
        assertEquals("Deposit Frequency File Processed Record EIN ", company.getFedTaxId(),
                depositFrequencyFile.getDepositFrequencyFileRecCollection().find(DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.Processed)).get(0).getEIN());
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, PaymentTemplate.getIRS_941(), PSPDate.getPSPTime());
        assertEquals("Effective Deposit Frequency code", DepositFrequencyCode.MONTHLY, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        assertEquals("Effective Deposit Frequency Effective Date", SpcfCalendar.createInstance(2012, 5, 2, SpcfTimeZone.getLocalTimeZone()), effectiveDepositFrequency.getEffectiveDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test @Ignore
    public void testFileParsingWithCompanySetup_DefaultSemiWeeklyUpdated() {

        DataLoadServices.setPSPDate(2012, 3, 2);
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        DataLoadServices.setPSPDate(2012, 5, 2);

        //Run Batch process
        BatchJobManager.runJob(BatchJobType.IRSDepositFrequencyFileProcessor);

        PayrollServices.beginUnitOfWork();

        DepositFrequencyFile depositFrequencyFile = assertOne(Application.find(DepositFrequencyFile.class));
        assertEquals("Deposit Frequency File status", DepositFrequencyFileStatus.Processed, depositFrequencyFile.getStatus());
        assertEquals("Deposit Frequency File Records", 11, depositFrequencyFile.getDepositFrequencyFileRecCollection().size());
        assertEquals("Deposit Frequency File Skipped Records ", 9, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.SkippedCompanyDoesNotExist)).size());
        assertEquals("Deposit Frequency File Invalid data Records ", 1, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.InvalidData)).size());
        assertEquals("Deposit Frequency File Processed Records ", 1, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.Processed)).size());
        assertEquals("Deposit Frequency File Processed Record EIN ", company.getFedTaxId(),
                depositFrequencyFile.getDepositFrequencyFileRecCollection().find(DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.Processed)).get(0).getEIN());
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, PaymentTemplate.getIRS_941(), PSPDate.getPSPTime());
        assertEquals("Effective Deposit Frequency code", DepositFrequencyCode.MONTHLY, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
        assertEquals("Effective Deposit Frequency Effective Date", SpcfCalendar.createInstance(2012, 5, 2, SpcfTimeZone.getLocalTimeZone()), effectiveDepositFrequency.getEffectiveDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test @Ignore
    public void testFileParsingWithCompanySetup_ErrorScenario() {

        DataLoadServices.setPSPDate(2012, 3, 2);
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        DataLoadServices.setPSPDate(2012, 5, 2);

        //Delete CompanyAgencyPaymentTemplate to create error scenario
        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "IRS");
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = assertOne(companyAgency.getCompanyAgencyPaymentTemplateCollection().
                find(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(PaymentTemplate.getIRS_941())));
        EffectiveDepositFrequency depositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, PaymentTemplate.getIRS_941(), PSPDate.getPSPTime());
        Application.delete(depositFrequency);
        for (CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod : companyAgencyPaymentTemplate.getCompanyPaymentTemplatePaymentMethodCollection()) {
            Application.delete(companyPaymentTemplatePaymentMethod);
        }
        Application.delete(companyAgencyPaymentTemplate);
        PayrollServices.commitUnitOfWork();

        //Run Batch process
        BatchJobManager.runJob(BatchJobType.IRSDepositFrequencyFileProcessor);

        PayrollServices.beginUnitOfWork();

        DepositFrequencyFile depositFrequencyFile = assertOne(Application.find(DepositFrequencyFile.class));
        assertEquals("Deposit Frequency File status", DepositFrequencyFileStatus.Received, depositFrequencyFile.getStatus());
        assertEquals("Deposit Frequency File Records", 11, depositFrequencyFile.getDepositFrequencyFileRecCollection().size());
        assertEquals("Deposit Frequency File Skipped Records ", 9, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.SkippedCompanyDoesNotExist)).size());
        assertEquals("Deposit Frequency File Invalid data Records ", 1, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.InvalidData)).size());
        assertEquals("Deposit Frequency File Processed Records ", 1, depositFrequencyFile.getDepositFrequencyFileRecCollection().find(
                DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.Error)).size());
        assertEquals("Deposit Frequency File Processed Record EIN ", company.getFedTaxId(),
                depositFrequencyFile.getDepositFrequencyFileRecCollection().find(DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.Error)).get(0).getEIN());
        assertNull("Effective Deposit frequency- IRS 941", EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, PaymentTemplate.getIRS_941(), PSPDate.getPSPTime()));
        PayrollServices.rollbackUnitOfWork();

    }
}
