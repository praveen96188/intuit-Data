package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.suicredits.ProcessSUICredits;
import com.intuit.sbd.payroll.psp.common.utils.encryption.IDPSDecrypter;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;


/**
 * This class is used to test the Lazy loading of objects.
 * The approach used here is, when the object is loaded
 * the value is null until explicitly called by a get method.
 */
public class LazyLoadTests {

    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testSMSMigration_Lazyload() {

        String realm = "9130349397822666";
        String testString = "Testing";

        PayrollServices.beginUnitOfWork();
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany();
        companyDTO.setSourceSystemCd(com.intuit.sbd.payroll.psp.domain.SourceSystemCode.QBDT);
        companyDTO.setIAMRealmId(realm);

        ProcessResult<Company> result = DataLoader.addCompany(companyDTO);

        PayrollServices.companyManager.addService(com.intuit.sbd.payroll.psp.domain.SourceSystemCode.valueOf(companyDTO.getSourceSystemCd().toString()), companyDTO.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(companyDTO.getCompanyId(), companyDTO.getSourceSystemCd());
        assertEquals("Load company", 0, result.getMessages().size());

        DataLoadServices.addEntitlementUnit(company, "123456", "654321");

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.BaseSMSMigration smsMigration = new SMSMigration();
        smsMigration.setSourceCompanyId(companyDTO.getCompanyId());
        smsMigration.setMigrationStatus(com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus.DataCollectionComplete);
        smsMigration.setValidationErrorResult(testString);
        smsMigration.setCompany(company);
        Application.save(smsMigration);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<SMSMigration> migOutputs = Application.find(SMSMigration.class);

        SMSMigration mig = migOutputs.get(0);

        //Since mValidationErrorResult is loaded lazily, this value should be null
        assertNull(getFieldByReflection(mig, "mValidationErrorResult"));

        //Using get, the value is loaded
        assertEquals(testString, mig.getValidationErrorResult());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testLedgerjob_Lazyload() {

        String originalFile = "Blah";
        LedgerOperationJobDTO jobDTO = new LedgerOperationJobDTO();
        jobDTO.setOriginalFile(originalFile);
        jobDTO.setType(com.intuit.sbd.payroll.psp.domain.LedgerOperationJobType.BulkDebit);
        LedgerOperationDTO operationDTO = new LedgerOperationDTO();
        operationDTO.setAmount(new SpcfMoney("1.00"));
        operationDTO.setCheckDate(new DateDTO("2012-12-31"));
        operationDTO.setLawId("66");
        operationDTO.setMemo("Memo");
        operationDTO.setOriginalLegalName("My Legal Name");
        operationDTO.setSourceCompanyId("123456789");
        operationDTO.setSourceSystemCd(com.intuit.sbd.payroll.psp.domain.SourceSystemCode.QBDT);
        jobDTO.getLedgerOperations().add(operationDTO);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.batchJobManager.addLedgerOperationJob(jobDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class));
        assertEquals(com.intuit.sbd.payroll.psp.domain.LedgerOperationJobStatus.Created, job.getStatus());
        assertSuccess(PayrollServices.batchJobManager.queueLedgerOperationJob(job.getId()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.LedgerOperations);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<LedgerOperationJob> ledgerOutputs = Application.find(LedgerOperationJob.class);

        LedgerOperationJob lop = ledgerOutputs.get(0);

        assertNull(getFieldByReflection(lop, "mOriginalFile"));
        assertNull(getFieldByReflection(lop, "mProcessedFile"));

        assertEquals(originalFile, lop.getOriginalFile());
        assertThat(lop.getProcessedFile(), containsString("My Legal Name"));

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSqlExecutionLogEntry_Lazyload() {

        //Load data
        String sql = "Select * from PSP_COMPANY";
        String reason = "Testing";

        PayrollServices.beginUnitOfWork();
        SqlExecutionLogEntry sqlLogEntry = new SqlExecutionLogEntry();
        sqlLogEntry.setUserName(Application.getCurrentPrincipal().getName());
        sqlLogEntry.setReason(reason);
        sqlLogEntry.setCommitted(false);
        sqlLogEntry.setRowCount(-1);
        sqlLogEntry.setExecutionTime(-1);
        Application.save(sqlLogEntry);
        sqlLogEntry.setSQL(sql);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<SqlExecutionLogEntry> sqlExecutionLogEntries = Application.find(SqlExecutionLogEntry.class);

        SqlExecutionLogEntry sqlExecutionLogEntry = sqlExecutionLogEntries.get(0);

        assertNull(getFieldByReflection(sqlExecutionLogEntry, "mSQL"));

        assertEquals(sql, sqlExecutionLogEntry.getSQL());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testStateReport_Lazyload() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"MI"};
        List<Company> companies = DataLoadServices.setupCompany(1L, 1, statesList, com.intuit.sbd.payroll.psp.domain.PaymentTemplateCategory.Withholding, com.intuit.sbd.payroll.psp.domain.PaymentMethod.CheckPayment);
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList,
                    SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2012-01-03"), false);
        }

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 2, 16, SpcfTimeZone.getLocalTimeZone()));

        BatchJobManager.runJob(BatchJobType.StateReport);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<StateReportOutput> stateReportOutputs = Application.find(StateReportOutput.class);

        StringBuilder combinedReportOutput = new StringBuilder();

        for(StateReportOutput stateReportOutput : stateReportOutputs) {
            assertNull(getFieldByReflection(stateReportOutput, "mReportOutput"));
            combinedReportOutput.append(stateReportOutput.getReportOutput()).append("\n");
        }

        assertThat(combinedReportOutput.toString(), containsString("MI Income Tax Withholding"));

        PayrollServices.rollbackUnitOfWork();

    }
    @Test
    public void testSUICreditsJob_Lazyload() {

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NV-NUCS4072-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.updateAdditionalFilingAmount(company, 555, "NV SUI Credit", 2013, 1);

        Application.beginUnitOfWork();

        PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, "NV-NUCS4072-PAYMENT"));

        PayrollServices.commitUnitOfWork();

        new ProcessSUICredits().process();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<SUICreditsJob> suiCreditsJobs = Application.find(SUICreditsJob.class);

        SUICreditsJob suiCreditsJob = suiCreditsJobs.get(0);

        assertNull(getFieldByReflection(suiCreditsJob, "mProcessedFile"));
        assertThat(suiCreditsJob.getProcessedFile(), containsString("NV-NUCS4072-PAYMENT"));

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testACHEnrollment_Lazyload() throws Exception {

        String fileContent = "Testing file content";

        PayrollServices.beginUnitOfWork();
        ACHEnrollmentFile achEnrollmentFile = new ACHEnrollmentFile();
        achEnrollmentFile.setFileContent(fileContent);
        achEnrollmentFile.setFileContentEnc(IDPSDecrypter.encryptQuickbaseData(fileContent));
        Application.save(achEnrollmentFile);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<ACHEnrollmentFile> achEnrollmentFiles = Application.find(ACHEnrollmentFile.class);

        ACHEnrollmentFile achEnrollmentFile1 = achEnrollmentFiles.get(0);

        assertNull(getFieldByReflection(achEnrollmentFile1, "mFileContent"));
        assertNull(getFieldByReflection(achEnrollmentFile1, "mFileContentEnc"));

        assertEquals(fileContent, achEnrollmentFile1.getFileContent());
        assertEquals(fileContent, IDPSDecrypter.decryptQuickbaseData(achEnrollmentFile1.getFileContentEnc()));

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEntitlementMsg_Lazyload() throws Exception {

        String msg = "Testing";

        PayrollServices.beginUnitOfWork();
        EntitlementMessage entitlementMessage1 = new EntitlementMessage();
        entitlementMessage1.setMessage(msg);
        entitlementMessage1.setMessageEnc(IDPSDecrypter.encryptQuickbaseData(msg));
        entitlementMessage1.setLicenseNumber("123456789");
        entitlementMessage1.setEntitlementOfferingCode("Offer123");
        entitlementMessage1.setOrderNumber("12345");
        Application.save(entitlementMessage1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntitlementMessage> entitlementMessages = Application.find(EntitlementMessage.class);

        EntitlementMessage entitlementMessage = entitlementMessages.get(0);

        assertNull(getFieldByReflection(entitlementMessage, "mMessage"));
        assertNull(getFieldByReflection(entitlementMessage, "mMessageEnc"));

        assertEquals(msg, entitlementMessage.getMessage());
        assertEquals(msg, IDPSDecrypter.decryptQuickbaseData(entitlementMessage.getMessageEnc()));

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEntityUpdate_Lazyload() {

        DataLoadServices.newCompany(SourceSystemCode.QBDT, "Test99", false, ServiceCode.Tax);

        //Create Employee Dto
        Random rand = new Random();
        int empSourceId = rand.nextInt(10000);

        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId("Test123" + empSourceId);
        employeeDTO.setFirstName("TestFName");
        employeeDTO.setLastName("TestLName");
        employeeDTO.setMiddleName("IK");
        employeeDTO.setSocialSecurityNumber("123454321");

        PayrollServices.beginUnitOfWork();
        PayrollServices.employeeManager.addEmployee(com.intuit.sbd.payroll.psp.domain.SourceSystemCode.QBDT, "Test99", employeeDTO);
        PayrollServices.commitUnitOfWork();

        Application.executeSqlCommand("DELETE FROM PSP_ENTITY_UPDATE", true);

        //Update Last Name
        employeeDTO.setLastName("TestLName_updated");

        PayrollServices.beginUnitOfWork();
        PayrollServices.employeeManager.updateEmployee(com.intuit.sbd.payroll.psp.domain.SourceSystemCode.QBDT, "Test99", employeeDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<EntityUpdate> entityUpdates = Application.find(EntityUpdate.class);
        EntityUpdate entityUpdate = entityUpdates.get(0);

        assertNull(getFieldByReflection(entityUpdate, "mChangedAttributes"));
        assertEquals("[LastName]", entityUpdate.getChangedAttributes());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testMessageLog_Lazyload() {

        String reqLog = "TestReqLog";
        String resLog = "TestResLog";
        Application.beginUnitOfWork();
        MessageLog messageLog = new MessageLog();
        messageLog.setTransactionId("1234");
        messageLog.setRequestLog(reqLog);
        messageLog.setResponseLog(resLog);
        messageLog.setCreatedDate(PSPDate.getPSPTime());
        messageLog.setModifiedDate(PSPDate.getPSPTime());
        messageLog = Application.save(messageLog);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<MessageLog> messageLogs = Application.find(MessageLog.class);

        MessageLog messageLog1 = messageLogs.get(0);

        assertNull(getFieldByReflection(messageLog1, "mRequestLog"));
        assertNull(getFieldByReflection(messageLog1, "mResponseLog"));

        assertEquals(reqLog, messageLog1.getRequestLog());
        assertEquals(resLog, messageLog1.getResponseLog());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testTaxCredits9061_Lazyload() {

        String test = "test";
        byte[] testBytes = test.getBytes();
        PayrollServices.beginUnitOfWork();

        TaxCreditsApplication taxCreditsApplication = new TaxCreditsApplication();
        taxCreditsApplication.setSignedDocumentBytes(testBytes);
        taxCreditsApplication.setUnsignedDocumentBytes(testBytes);
        Application.save(taxCreditsApplication);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        TaxCredits9061 tc9061 = new TaxCredits9061();

        tc9061.set9061Bytes(test.getBytes());
        tc9061.setEmployeeName("Test");
        tc9061.setFedTaxId("1234");
        tc9061.setSSN("123456789");
        tc9061.setTaxCreditsApplication(taxCreditsApplication);
        Application.save(tc9061);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<TaxCredits9061> taxCredits9061s = Application.find(TaxCredits9061.class);

        TaxCredits9061 taxCredits9061 = taxCredits9061s.get(0);

        assertNull(getFieldByReflection(taxCredits9061, "mForm9061"));

        assertEquals(test, taxCredits9061.getForm9061());

        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testTaxCreditsApplication_Lazyload() {

        String test = "testing";
        byte[] testBytes = test.getBytes();

        PayrollServices.beginUnitOfWork();

        TaxCreditsApplication taxCreditsApplication = new TaxCreditsApplication();
        taxCreditsApplication.setSignedDocumentBytes(testBytes);
        taxCreditsApplication.setUnsignedDocumentBytes(testBytes);
        Application.save(taxCreditsApplication);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<TaxCreditsApplication> taxCreditsApplications = Application.find(TaxCreditsApplication.class);

        TaxCreditsApplication taxCreditsApplication1 = taxCreditsApplications.get(0);

        assertNull(getFieldByReflection(taxCreditsApplication1, "mSignedDocument"));
        assertNull(getFieldByReflection(taxCreditsApplication1, "mUnsignedDocument"));

        assertEquals(test, taxCreditsApplication1.getSignedDocument());
        assertEquals(test, taxCreditsApplication1.getUnsignedDocument());

        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testRTBAutomationBackup_Lazyload() {

        String rtbBackup = "TestingLazyLoad";

        PayrollServices.beginUnitOfWork();

        RTBAUTOMATIONBACKUP rtbautomationbackup = new RTBAUTOMATIONBACKUP();
        rtbautomationbackup.setRtbBackup(rtbBackup);
        rtbautomationbackup.setCompanyId("Test1234");
        rtbautomationbackup.setEventType((com.intuit.sbd.payroll.psp.domain.RTBBackUpEventType.ERROR2108));
        Application.save(rtbautomationbackup);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<RTBAUTOMATIONBACKUP> rtbautomationbackups = Application.find(RTBAUTOMATIONBACKUP.class);

        RTBAUTOMATIONBACKUP rtbautomationbackup1 = rtbautomationbackups.get(0);

        assertNull(getFieldByReflection(rtbautomationbackup1, "mRtbBackup"));

        assertEquals(rtbBackup, rtbautomationbackup1.getRtbBackup());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testSourceSystemPrintedCheckInfo_Lazyload() throws Exception {

        try {
            createSourcePrintedCheckInfo();

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<SourceSystemPrintedCheckInfo> sourceSystemPrintedCheckInfos = Application.find(SourceSystemPrintedCheckInfo.class);

            SourceSystemPrintedCheckInfo sourceSystemPrintedCheckInfo1 = sourceSystemPrintedCheckInfos.get(0);

            assertNull(getFieldByReflection(sourceSystemPrintedCheckInfo1, "mBankLogo"));
            assertNull(getFieldByReflection(sourceSystemPrintedCheckInfo1, "mSourceSystemLogo"));

            assertNotNull(sourceSystemPrintedCheckInfo1.getBankLogo());
            assertNotNull(sourceSystemPrintedCheckInfo1.getSourceSystemLogo());

            assertTrue(Arrays.equals(readImage("ChaseLogo"), sourceSystemPrintedCheckInfo1.getBankLogoImage()));
            assertTrue(Arrays.equals(readImage("IntuitLogo"), sourceSystemPrintedCheckInfo1.getSourceSystemLogoImage()));

            PayrollServices.rollbackUnitOfWork();

        }finally {
            //Clean up
            Application.executeSqlCommand("DELETE FROM PSP_SOURCESYS_PRINTEDCHK_INFO", true);
        }
    }

    @Test
    public void testCheckPrintSignature_Lazyload() throws Exception {

        try {

            createSourcePrintedCheckInfo();

            byte[] signatureBytes = readImage("signature");

            //Create Check print signature
            PayrollServices.beginUnitOfWork();

            SourceSystemPrintedCheckInfo sourceSystemPrintedCheckInfo = assertOne(Application.find(SourceSystemPrintedCheckInfo.class));
            if (sourceSystemPrintedCheckInfo.getCheckPrintSignature() == null) {
                CheckPrintSignature checkPrintSignature = new CheckPrintSignature();
                checkPrintSignature.setSourceSystemPrintedCheckInfo(sourceSystemPrintedCheckInfo);
                Application.save(checkPrintSignature);
                checkPrintSignature.setSignatureAsImage(signatureBytes);
            }
            PayrollServices.commitUnitOfWork();

            BatchJobManager.runJob(BatchJobType.CheckPrint);

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<CheckPrintSignature> checkPrintSignatures = Application.find(CheckPrintSignature.class);

            CheckPrintSignature checkPrintSignature = checkPrintSignatures.get(0);

            assertNull(getFieldByReflection(checkPrintSignature, "mSignature"));

            assertTrue(Arrays.equals(signatureBytes, checkPrintSignature.getSignatureImage()));

            BufferedImage imgDB = ImageIO.read(new ByteArrayInputStream(checkPrintSignature.getSignatureImage()));
            BufferedImage imgFile = ImageIO.read(new ByteArrayInputStream(signatureBytes));

            ImageIO.write(imgDB, "png", new File("signatureImage.png") );
            ImageIO.write(imgFile, "png", new File("signatureImage1.png") );

            PayrollServices.rollbackUnitOfWork();

        }finally {
            //Clean up
            Application.executeSqlCommand("DELETE FROM PSP_CHECK_PRINT_SIGNATURE", true);
            Application.executeSqlCommand("DELETE FROM PSP_SOURCESYS_PRINTEDCHK_INFO", true);
        }
    }

    @Test
    public void testSavedReports_Lazyload() {

        String query = "Select * from PSP_COMPANY";
        PayrollServices.beginUnitOfWork();
        SavedReports savedReports = new SavedReports();
        savedReports.setReportId("123456");
        savedReports.setDescription("Lazy load testing");
        savedReports.setDisplayName("LazyLoad");
        savedReports.setQuery(query);
        Application.save(savedReports);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<SavedReports> savedReports1 = Application.find(SavedReports.class);

        SavedReports savedReport = savedReports1.get(0);

        assertNull(getFieldByReflection(savedReport, "mQuery"));

        assertEquals(query, savedReport.getQuery());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testSourceSystemTransmission_lazyLoad() {

        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ewsValidateSubscription>\n" +
                "    <IpAddress>103.15.250.10</IpAddress>\n" +
                "    <DateTimeStamp>2019-04-13T00:32:35.520-07:00</DateTimeStamp>\n" +
                "    <SubscriptionNumber>10776885</SubscriptionNumber>\n" +
                "    <EIN>082220161</EIN>\n" +
                "    <PSID>100605106</PSID>\n" +
                "    <QuickBooks>\n" +
                "        <AppVersion>30.00.A.3/21908#belacct</AppVersion>\n" +
                "        <LicenseNumber>0052-3627-0157-634</LicenseNumber>\n" +
                "    </QuickBooks>\n" +
                "</ewsValidateSubscription>\n";


        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ewsValidateSubscriptionResponse>\n" +
                "    <DateTimeStamp>2019-04-10T10:51:08.171-07:00</DateTimeStamp>\n" +
                "    <ResponseStatus>\n" +
                "        <Code>0</Code>\n" +
                "        <Message>Success</Message>\n" +
                "    </ResponseStatus>\n" +
                "    <SubscriptionStatus>Activated</SubscriptionStatus>\n" +
                "    <SubscriptionEndDate>2016-09-21T09:00:34.393-07:00</SubscriptionEndDate>\n" +
                "    <SubscriptionBillingInfo>\n" +
                "        <SubscriptionNextBillDate>2016-09-21T09:00:34.393-07:00</SubscriptionNextBillDate>\n" +
                "    </SubscriptionBillingInfo>\n" +
                "    <PSID>100605106</PSID>\n" +
                "    <CompanyLegalInfo>\n" +
                "        <AddressLine1>6888 Sierra Center Parkway</AddressLine1>\n" +
                "        <City>Reno</City>\n" +
                "        <State>NV</State>\n" +
                "        <Zip>89511</Zip>\n" +
                "        <LegalName>aeic</LegalName>\n" +
                "    </CompanyLegalInfo>\n" +
                "    <QBAccountName>BOFI</QBAccountName>\n" +
                "    <SubType>15</SubType>\n" +
                "    <EntitlementCreationDate>2016-08-22T09:00:34.437-07:00</EntitlementCreationDate>\n" +
                "</ewsValidateSubscriptionResponse>\n";


        ApplicationSecondary.beginUnitOfWork();
        SourceSystemTransmission sourceSystemTransmission = new SourceSystemTransmission();
        sourceSystemTransmission.setTransmissionIdentifier("TestSSTIdentifier");
        sourceSystemTransmission.setHost("Test");
        sourceSystemTransmission.setRequestDocument(request);
        sourceSystemTransmission.setResponseDocument(response);
        sourceSystemTransmission.setDescription("Test Description");
        sourceSystemTransmission.setApplicationId("Test Application");
        sourceSystemTransmission.setTaxTableId("Test tax table");
        ApplicationSecondary.save(sourceSystemTransmission);
        ApplicationSecondary.commitUnitOfWork();

        ApplicationSecondary.beginUnitOfWork();

        DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = ApplicationSecondary.find(SourceSystemTransmission.class);

        SourceSystemTransmission sourceSystemTransmission1 = sourceSystemTransmissions.get(0);

        assertEquals(request, sourceSystemTransmission1.getRequestDocument());
        assertEquals(response, sourceSystemTransmission1.getResponseDocument());

        ApplicationSecondary.rollbackUnitOfWork();

    }

    private Object getFieldByReflection(Object obj, String fieldName) {

        try {
            return FieldUtils.readField(obj, fieldName, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private void createSourcePrintedCheckInfo() throws IOException {

        byte[] chaseLogo = readImage("ChaseLogo");
        byte[] intuitLogo = readImage("IntuitLogo");

        PayrollServices.beginUnitOfWork();

        Address address = new Address();
        address.setAddressLine1("6888 Sierra Cnt Pkwy");
        address.setCity("Reno");
        address.setZipCode("89511");
        address.setState("NV");
        Application.save(address);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SourceSystemPrintedCheckInfo sourceSystemPrintedCheckInfo = new SourceSystemPrintedCheckInfo();
        sourceSystemPrintedCheckInfo.setSourceSystemLogoAsImage(intuitLogo);
        sourceSystemPrintedCheckInfo.setBankLogoAsImage(chaseLogo);
        sourceSystemPrintedCheckInfo.setAddress(address);
        sourceSystemPrintedCheckInfo.setSourceSystemCode(com.intuit.sbd.payroll.psp.domain.SourceSystemCode.QBDT);
        Application.save(sourceSystemPrintedCheckInfo);
        PayrollServices.commitUnitOfWork();
    }

    private byte[] readImage(String imageName) throws IOException {
        RandomAccessFile rf;
        int size;
        rf = new RandomAccessFile(Application.findFileOnClassPath( "lazyLoad/" + imageName + ".png"), "r");
        size = (int) rf.length();
        byte[] chaseLogo = new byte[size];
        rf.readFully(chaseLogo);
        rf.close();
        return chaseLogo;
    }

}