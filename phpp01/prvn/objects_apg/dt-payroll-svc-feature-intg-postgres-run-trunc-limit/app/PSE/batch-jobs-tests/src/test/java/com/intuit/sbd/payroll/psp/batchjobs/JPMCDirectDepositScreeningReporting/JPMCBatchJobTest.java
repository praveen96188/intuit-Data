package com.intuit.sbd.payroll.psp.batchjobs.JPMCDirectDepositScreeningReporting;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.AMLReport;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.IndustryReport;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.OFACReport;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.TPSUReport;
import com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.AMLReportProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.IndustryReportProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.OFACReportProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.TPSUReportProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.utils.CommonUtilityException;
import intuit.osp.common.utils.FileUtils;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Created by charithah418 on 6/14/15.
 */
public class JPMCBatchJobTest {

    private static SpcfLogger logger = SpcfLogManager.getLogger(JPMCReportFileFormatTest.class);
    private final String mSendDir = BatchUtils.getConfigString("psp_batch_ftp_send_dir"); // Directory in which the generated files are located
    private final String mArchiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir"); // Directory in which the uploaded file are located
    private static String processingDate;
    DataLoader dataloader;

    public static void beforeEachTest() {
        Application.initialize();
        ApplicationSecondary.initialize();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.UnitTest);
        assertTransactionNotInProgress();
    }

    public static void afterEachTest() {
        assertTransactionNotInProgress();
    }

    private static void assertTransactionNotInProgress() {
        if (Application.hasActiveTransaction()) {
            logger.error("Transaction in progress. The current unit of work originated at:\n" +
                                 Application.getSessionCache().getOriginOfUnitOfWork());
            Application.rollbackUnitOfWork();
        }
    }
    @Before
    public void runBeforeEachTest() {
        JPMCReportFileFormatTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();


        /* The reports are generated based on the processingDate.
           Any record which is modified/created between processingDate and  processingDate -1 will be picked up
           The time is 12 A.M from processingDate -1 till 12 A.M processingDate
           so any record that is created yesterday will be picked up in todays report and so we are creating data with yesterdays date and running the report today */
        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        processingDate =today.format("yyyyMMdd");
        CalendarUtils.addBusinessDays(today, -1);
        PSPDate.setPSPTime(today);

        dataloader = new DataLoader();
        dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    private static void deleteFile(String pSourceFile){
        new File(pSourceFile).delete(); // delete the source file
    }

    private void deleteGeneratedOFACReports(String directory){
        deleteFile(directory + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.DATA_FILE_EXT);
        deleteFile(directory + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.ENCRYPTED_DATA_FILE_EXT);
        deleteFile(directory + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT);
        deleteFile(directory + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.AUDIT_FILE + OFACReport.ENCRYPTED_DATA_FILE_EXT);
    }

    private void deleteGeneratedAMLReports(String directory){
        deleteFile(directory + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate + AMLReport.ENCRYPTED_FILE_EXT);
    }

    private void deleteGeneratedIndustryReports(String directory){
        String dateInFile = CalendarUtils.getLastDayOfMonth(PSPDate.getPSPTime()).format("yyyyMMdd");
        deleteFile( directory + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.INACTIVE_DD_SERVICE);
        deleteFile( directory + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.ACTIVE_DD_SERVICE);
    }

    private void setPSPDateToTodaysDate(){
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        PSPDate.setPSPTime(today);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    @Ignore//Passes only when run as single test, data creation is done in this way
    public void runCreateIndustryFileStep(){
        setPSPDateToTodaysDate();
        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        today.addMonths(-1);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(today);
        PayrollServices.commitUnitOfWork();

        // The industry report runs on first business day of a month and it will have data for the previous month.
        // so the file date will always be last day of the month for which the data is generated.
        String dateInFile = CalendarUtils.getLastDayOfMonth(PSPDate.getPSPTime()).format("yyyyMMdd");
        JPMCDataLoader.createReportDataWithCancelledOrTerminatedDDService();

        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        logger.info("Creating Industry File");
        logger.info(BatchJobManager.runJobStep(BatchJobType.IndustryReportProcessor, IndustryReportProcessor.CreateFileStep.class, processingDate));

        String dataFilePath = mSendDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.ACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        JPMCReportFileFormatTest.validateFile(Application.findFileOnClassPath("offload/generated/" +  IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.ACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT), Application.findFileOnClassPath("jpmcscreening/expected/Industry_ActiveDDService_HappyPath.txt"));

        dataFilePath = mSendDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.INACTIVE_DD_SERVICE  +  IndustryReport.FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        JPMCReportFileFormatTest.validateFile(Application.findFileOnClassPath("offload/generated/" + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.INACTIVE_DD_SERVICE + IndustryReport.FILE_EXT), Application.findFileOnClassPath("jpmcscreening/expected/Industry_InActiveDDService_HappyPath.txt"));
        deleteGeneratedIndustryReports(mSendDir);
    }

    @Test
    public void runCreateOFACFileStepWithProcessingDate(){
        JPMCDataLoader.createReportData();

        logger.info("Creating OFAC File");
        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.CreateFileStep.class, processingDate);

        String dataFileExt = OFACReport.DATA_FILE_EXT;
        String encryptFile = BatchUtils.getConfigString("psp_jpmc_csi_encrypt_file");
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            dataFileExt = OFACReport.ENCRYPTED_DATA_FILE_EXT;
        }
        String dataFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX+ processingDate + dataFileExt;
        String triggerFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT;
        String auditFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.AUDIT_FILE + OFACReport.ENCRYPTED_DATA_FILE_EXT;

        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        File triggerFile = new File(triggerFilePath);
        Assert.assertEquals(true, triggerFile.exists());
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            File auditFile = new File(auditFilePath);
            Assert.assertEquals(true, auditFile.exists());
        }


        deleteGeneratedOFACReports(mSendDir);
    }

    @Test (expected = RuntimeException.class)
    public void runCreateOFACFileStepWithInvalidProcessingDate(){
        JPMCDataLoader.createReportData();
        logger.info("Creating OFAC File");
        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.CreateFileStep.class, "invalidDate");
    }

    @Test
    public void runCreateOFACFileStepWithoutProcessingDate(){
        JPMCDataLoader.createReportData();

        /*
            PSPDate is set to yesterdays date to create the report data.
            If the processing date is not passed as argument, by default the PSPDate will be used to run the report.
            In this case, we have to reset the PSPDate to todays date, so that the report picks up the correct data
         */
        setPSPDateToTodaysDate();
        logger.info("Creating OFAC File");
        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.CreateFileStep.class);

        String dataFileExt = OFACReport.DATA_FILE_EXT;
        String encryptFile = BatchUtils.getConfigString("psp_jpmc_csi_encrypt_file");
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            dataFileExt = OFACReport.ENCRYPTED_DATA_FILE_EXT;
        }

        String dataFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + dataFileExt;
        String triggerFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT;
        String auditFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.AUDIT_FILE + OFACReport.ENCRYPTED_DATA_FILE_EXT;

        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        File triggerFile = new File(triggerFilePath);
        Assert.assertEquals(true, triggerFile.exists());
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            File auditFile = new File(auditFilePath);
            Assert.assertEquals(true, auditFile.exists());
        }


        deleteGeneratedOFACReports(mSendDir);
    }


    @Test
    public void runCreateAMLFileStepWithProcessingDate(){
        JPMCDataLoader.createReportData();
        logger.info("Creating AML File");
        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.CreateFileStep.class, processingDate);

        String dataFilePath = mSendDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        deleteGeneratedAMLReports(mSendDir);
    }

    @Test
    public void runCreateAMLFileStepWithoutProcessingDate(){
        JPMCDataLoader.createReportData();

        /*
            PSPDate is set to yesterdays date to create the report data.
            If the processing date is not passed as argument, by default the PSPDate will be used to run the report.
            In this case, we have to reset the PSPDate to todays date, so that the report picks up the correct data
         */
        setPSPDateToTodaysDate();
        logger.info("Creating AML File");
        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.CreateFileStep.class);

        String dataFilePath = mSendDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;

        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        deleteGeneratedAMLReports(mSendDir);
    }

    @Test (expected = RuntimeException.class)
    public void runCreateAMLFileStepWithInvalidProcessingDate(){
        JPMCDataLoader.createReportData();
        logger.info("Creating AML File");
        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.CreateFileStep.class, "InvalidDate");
    }
    
    @Test
    public void runArchiveAMLFileStepWithNoArguments(){
        JPMCDataLoader.createReportData();
        logger.info("Creating AML File");
        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.CreateFileStep.class, processingDate);

        String dataFilePath = mSendDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        Long fileSize = dataFile.length();

        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.ArchiveFileStep.class);
        dataFilePath = mArchiveDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
        Assert.assertEquals(Long.valueOf(fileSize), Long.valueOf(dataFile.length()));
        deleteGeneratedAMLReports(mArchiveDir);
    }


    @Test
    public void runArchiveAMLFileStepWithArguments(){
        JPMCDataLoader.createReportData();
        logger.info("Creating AML File");
        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.CreateFileStep.class, processingDate);

        String dataFilePath = mSendDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
        long fileSize = dataFile.length();

        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.ArchiveFileStep.class,  AMLReport.AML_DATA_FILE_PREFIX  + processingDate +AMLReport.ENCRYPTED_FILE_EXT);
		dataFilePath = mArchiveDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
		Assert.assertEquals(Long.valueOf(fileSize), Long.valueOf(dataFile.length()));
        deleteGeneratedAMLReports(mArchiveDir);
    }

    @Test(expected = RuntimeException.class)
    public void runArchiveAMLFileStepWithWrongArguments(){
        JPMCDataLoader.createReportData();
        logger.info("Creating AML File");
        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.CreateFileStep.class, processingDate);
        String dataFilePath = mSendDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate+ AMLReport.ENCRYPTED_FILE_EXT;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.ArchiveFileStep.class, "OFAC_Data_File_");

    }

    @Test
    public void runArchiveOFACFileStepWithNoArguments(){
        JPMCDataLoader.createReportData();
        logger.info("Creating AML File");
        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.CreateFileStep.class, processingDate);

        String dataFileExt = OFACReport.DATA_FILE_EXT;
        String encryptFile = BatchUtils.getConfigString("psp_jpmc_csi_encrypt_file");
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            dataFileExt = OFACReport.ENCRYPTED_DATA_FILE_EXT;
        }

        String dataFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + dataFileExt;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

		String triggerFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT;
        File triggerFile = new File(triggerFilePath);
        Assert.assertEquals(true, triggerFile.exists());
        Long auditFileSize = 0L;
        File auditFile;
        String auditFilePath = "";
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            auditFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.AUDIT_FILE + OFACReport.ENCRYPTED_DATA_FILE_EXT;
            auditFile = new File(auditFilePath);
            Assert.assertEquals(true, auditFile.exists());
            auditFileSize = auditFile.length();

        }

		Long dataFileSize = dataFile.length();
		Long triggerFileSize = triggerFile.length();

        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.ArchiveFileStep.class);

        dataFilePath = mArchiveDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + dataFileExt;
        dataFile = new File(dataFilePath);

        triggerFilePath = mArchiveDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT;
        triggerFile = new File(triggerFilePath);

        Assert.assertEquals(true, triggerFile.exists());
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            auditFilePath = mArchiveDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.AUDIT_FILE + OFACReport.ENCRYPTED_DATA_FILE_EXT;
            auditFile = new File(auditFilePath);
            Assert.assertEquals(true, auditFile.exists());
            Assert.assertEquals(Long.valueOf(auditFileSize), Long.valueOf(auditFile.length()));
        }
		Assert.assertEquals(Long.valueOf(dataFileSize), Long.valueOf(dataFile.length()));
		Assert.assertEquals(Long.valueOf(triggerFileSize), Long.valueOf(triggerFile.length()));

        deleteGeneratedOFACReports(mArchiveDir);
    }

    @Test
    public void runArchiveOFACFileStepWithArguments(){
        JPMCDataLoader.createReportData();
        logger.info("Creating AML File");
        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.CreateFileStep.class, processingDate);

        String dataFileExt = OFACReport.DATA_FILE_EXT;
        String encryptFile = BatchUtils.getConfigString("psp_jpmc_csi_encrypt_file");
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            dataFileExt = OFACReport.ENCRYPTED_DATA_FILE_EXT;
        }

        String dataFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + dataFileExt ;
        File dataFile = new File(dataFilePath);
        String triggerFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT;
        File triggerFile = new File(triggerFilePath);
        Assert.assertEquals(true, dataFile.exists());
        Assert.assertEquals(true, triggerFile.exists());
        Long auditFileSize = 0L;
        String auditFilePath = "";
        File auditFile;
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            auditFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.AUDIT_FILE + OFACReport.ENCRYPTED_DATA_FILE_EXT;
            auditFile = new File(auditFilePath);
            Assert.assertEquals(true, auditFile.exists());
            auditFileSize = auditFile.length();
        }


        Long dataFileSize = dataFile.length();
		Long triggerFileSize = triggerFile.length();

	
        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.ArchiveFileStep.class,  OFACReport.OFAC_PREFIX + processingDate + dataFileExt, OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT);

        dataFilePath = mArchiveDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + dataFileExt;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
        triggerFilePath = mArchiveDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT;
        triggerFile = new File(triggerFilePath);
        Assert.assertEquals(true, triggerFile.exists());
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            auditFilePath = mArchiveDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.AUDIT_FILE + OFACReport.ENCRYPTED_DATA_FILE_EXT;
            auditFile = new File(auditFilePath);
            Assert.assertEquals(Long.valueOf(auditFileSize), Long.valueOf(auditFile.length()));
        }


        Assert.assertEquals(Long.valueOf(dataFileSize), Long.valueOf(dataFile.length()));
		Assert.assertEquals(Long.valueOf(triggerFileSize), Long.valueOf(triggerFile.length()));

		deleteGeneratedOFACReports(mArchiveDir);
    }

    @Test(expected = RuntimeException.class)
    public void runArchiveOFACFileStepWithWrongArguments(){
        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.ArchiveFileStep.class,  "AML_FILE" + processingDate + ".csv");
    }


    @Test
    public void runArchiveIndustryFileStepWithNoArguments(){
        setPSPDateToTodaysDate();

        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        today.addMonths(-1);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(today);
        PayrollServices.commitUnitOfWork();

        // The industry report runs on first business day of a month and it will have data for the previous month.
        // so the file date will always be last day of the month for which the data is generated.
        String dateInFile = CalendarUtils.getLastDayOfMonth(PSPDate.getPSPTime()).format("yyyyMMdd");
        JPMCDataLoader.createReportDataWithCancelledOrTerminatedDDService();

        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        logger.info("Creating Industry File");
        BatchJobManager.runJobStep(BatchJobType.IndustryReportProcessor, IndustryReportProcessor.CreateFileStep.class, processingDate);

        String dataFilePath = mSendDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.ACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
        Long activeDDfileSize = dataFile.length();

        dataFilePath = mSendDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.INACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
        Long inActiveDDfileSize = dataFile.length();

        BatchJobManager.runJobStep(BatchJobType.IndustryReportProcessor, IndustryReportProcessor.ArchiveFileStep.class);
        dataFilePath = mArchiveDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.ACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
        Assert.assertEquals(Long.valueOf(activeDDfileSize), Long.valueOf(dataFile.length()));

        dataFilePath = mArchiveDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.INACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
        Assert.assertEquals(Long.valueOf(inActiveDDfileSize), Long.valueOf(dataFile.length()));

        deleteGeneratedIndustryReports(mSendDir);
    }

    /* Files can be uploaded only in the E2E environment and not via unit test
            Enable this unit test locally when we want to test the entire batch job
         */
    @Ignore
    @Test
    public void runOFACBatchJob(){
        JPMCDataLoader.createReportData();
        logger.info("Uploading OFAC File ");
        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.CreateFileStep.class, processingDate);

        String dataFileExt = OFACReport.DATA_FILE_EXT;
        String encryptFile = BatchUtils.getConfigString("psp_jpmc_csi_encrypt_file");
        if (encryptFile != null && encryptFile.trim().equalsIgnoreCase("true")) {
            dataFileExt = OFACReport.ENCRYPTED_DATA_FILE_EXT;
        }

        String dataFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX+ processingDate + dataFileExt;
        String triggerFilePath = mSendDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT;

        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        File triggerFile = new File(triggerFilePath);
        Assert.assertEquals(true, triggerFile.exists());

        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.UploadFileStep.class);
        BatchJobManager.runJobStep(BatchJobType.OFACReportProcessor, OFACReportProcessor.ArchiveFileStep.class);

        dataFilePath = mArchiveDir + File.separator + OFACReport.OFAC_PREFIX+ processingDate + dataFileExt;
        triggerFilePath = mArchiveDir + File.separator + OFACReport.OFAC_PREFIX + processingDate + OFACReport.TRG_FILE_EXT;

        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        triggerFile = new File(triggerFilePath);
        Assert.assertEquals(true, triggerFile.exists());
    }

    /* Files can be uploaded only in the E2E environment and not via unit test
        Enable this unit test locally when we want to test the entire batch job
     */
    @Ignore
    @Test
    public void runAMLBatchJob(){
        JPMCDataLoader.createReportData();
        logger.info("Uploading OFAC File ");
        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.CreateFileStep.class, processingDate);

        String dataFilePath = mSendDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate + AMLReport.ENCRYPTED_FILE_EXT;

        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.UploadFileToHost1Step.class);
        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.UploadFileToHost2Step.class);
        BatchJobManager.runJobStep(BatchJobType.AMLReportProcessor, AMLReportProcessor.ArchiveFileStep.class);

        dataFilePath = mArchiveDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + processingDate + AMLReport.ENCRYPTED_FILE_EXT;

        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
    }

    /* Files can be uploaded only in the E2E environment and not via unit test
        Enable this unit test locally when we want to test the entire batch job
     */

    @Test
    @Ignore
    public void runIndustryBatchJob(){
        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        today.addMonths(-1);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(today);
        PayrollServices.commitUnitOfWork();

        // The industry report runs on first business day of a month and it will have data for the previous month.
        // so the file date will always be last day of the month for which the data is generated.
        String dateInFile = CalendarUtils.getLastDayOfMonth(PSPDate.getPSPTime()).format("yyyyMMdd");
        JPMCDataLoader.createReportDataWithCancelledOrTerminatedDDService();

        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        logger.info("Creating Industry File");
        BatchJobManager.runJobStep(BatchJobType.IndustryReportProcessor, IndustryReportProcessor.CreateFileStep.class, processingDate);

        String dataFilePath = mSendDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.ACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        dataFilePath = mSendDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.INACTIVE_DD_SERVICE + IndustryReport.FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        BatchJobManager.runJobStep(BatchJobType.IndustryReportProcessor, IndustryReportProcessor.UploadFileStep.class);
        BatchJobManager.runJobStep(BatchJobType.IndustryReportProcessor, IndustryReportProcessor.ArchiveFileStep.class);

        dataFilePath = mArchiveDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.ACTIVE_DD_SERVICE + IndustryReport.FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        dataFilePath = mArchiveDir + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.INACTIVE_DD_SERVICE + IndustryReport.FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
    }

    @Ignore //We don't have JPMC Test environment to forward the file.
    @Test
    public void runTPSUBatchJob(){
        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        today.addMonths(-1);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(today);
        PayrollServices.commitUnitOfWork();

        //TPSU Report runs every first business day of the month.

        String dateInFile = CalendarUtils.getLastDayOfMonth(PSPDate.getPSPTime()).format("yyyyMMdd");
        JPMCDataLoader.createReportDataWithCancelledOrTerminatedDDService();

        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        logger.info("Creating TPSU File");
        BatchJobManager.runJobStep(BatchJobType.TPSUReportProcessor, TPSUReportProcessor.CreateTPSUFileStep.class, processingDate);

        String dataFilePath = mSendDir + File.separator + TPSUReport.TPS_PREFIX + PSPDate.getPSPTime().format("yyyyMMdd") + TPSUReport.DATA_FILE_EXT;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        BatchJobManager.runJobStep(BatchJobType.TPSUReportProcessor, TPSUReportProcessor.UploadTPSUFileStep.class);
        BatchJobManager.runJobStep(BatchJobType.TPSUReportProcessor, TPSUReportProcessor.ArchiveTPSUFileStep.class);

        dataFilePath = mArchiveDir + File.separator + TPSUReport.TPS_PREFIX + PSPDate.getPSPTime() + TPSUReport.DATA_FILE_EXT;
        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

    }
}
