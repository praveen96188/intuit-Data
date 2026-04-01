package com.intuit.sbd.payroll.psp.batchjobs.JPMCDirectDepositScreeningReporting;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.JPMCEventMessage;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.JPMCEventMessageBuilder;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports.*;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.utils.CompareResults;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;

import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;

/**
 * Created by charithah418 on 6/6/15.
 */

public class JPMCReportFileFormatTest {

    private final String mSendDir = BatchUtils.getConfigString("psp_batch_ftp_send_dir"); // Directory in which the generated files are located
    public static final String OFAC_RECORD2 = "Primary|p|principal|High Trade|||Address Line 1|2nd Cross|Los Angeles|CA|90001|USA||04/07/1990||120000000|||\r\n";
    public static final String AML_RECORD = "120000000|04071990|Primary p principal|123-45-6789|2341765891\r\n";
    public static final String INDUSTRY_RECORD = "\tHigh Trade\tDBA\tDBA\t8563\t100161189\t120000000\tAddress Line 1\t2nd Cross\tLos Angeles\tCA\tUSA\t90001\t9980999999\tPrimaryPrincipal@gmail.com\t\t\r\n";
    private static SpcfLogger logger = SpcfLogManager.getLogger(JPMCReportFileFormatTest.class);
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

    public static void validateFile(String pExpectedFileName, String pCreatedFileName) {
        try {
            BufferedReader expectedReader = new BufferedReader(new FileReader(pExpectedFileName));
            BufferedReader compareReader;
            Key key = IDPSFileStreamManager.newKeyHandleLatest();

            if(StreamUtil.isFileIDPSEncrypted(pCreatedFileName)){
                compareReader = new BufferedReader(new IDPSFileReader(pCreatedFileName,key));
            }else {
                compareReader = new BufferedReader(new FileReader(pCreatedFileName));
            }

            CompareResults compareResults = compareFiles(expectedReader, compareReader);

            if (!compareResults.getStatus()) {
                System.out.println(compareResults.toString());
            }
            assertEquals("File " + pCreatedFileName + " matches expected file " + pExpectedFileName, true, compareResults.getStatus());

        } catch (Exception ex) {
            ex.printStackTrace();
            TestCase.fail(ex.getMessage());
        }
    }

    private static CompareResults compareFiles(BufferedReader inFile, BufferedReader compareFile) throws Exception{

        boolean valid = true;
        ArrayList failureReasons = new ArrayList();
        String inFileString= null;
        String compareFileString= null;
        try {
            inFileString = String.valueOf(inFile.read());
            compareFileString=String.valueOf(compareFile.read());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        String[] inFileLines= inFileString.split("\\n");

        String[] compareFileLines=compareFileString.split("\\n");
        if(inFileLines.length != compareFileLines.length){
            failureReasons.add("The files being compared are not the same length.");
            valid = false;
        }

        CompareResults result = new CompareResults();

        for (String compareFileLine : compareFileLines) {

            boolean found = false;

            Pattern pattern = Pattern.compile(compareFileLine);

            for (String inFileLine : inFileLines) {
                Matcher matcher = pattern.matcher(inFileLine);

                if (matcher.matches()) {
                    found = true;
                    break;
                }
            }

            if (found == false) {
                valid= false;
                System.out.println(String.format("line 1: %s\n not present in generated file ",  compareFileLine.toString()));
                failureReasons.add(String.format("line 1: %s\n not present in generated file ",  compareFileLine.toString()));
            }
        }
        result.setReasons(failureReasons);
        result.setStatus(valid);
        /*try {
            while (!eof) {
                // read one line at a time
                String compareLine = compareFile.readLine();
                String line = inFile.readLine();

                // check for EOF
                if (compareLine == null || line == null) {
                    if (compareLine == null && line == null)
                        eof = true;
                    else {
                        failureReasons.add("The files being compared are not the same length.");
                        valid = false;
                        eof = true;
                    }
                } else {
                    // if valid is changed to false it cannot be changed back to true
                    if (valid) {
                        valid = line.compareToIgnoreCase(compareLine) == 0;
                        if (!valid) {
                            System.out.println(String.format("line 1: %s\nline 2: %s;", line, compareLine));
                        }
                    }
                }
                // increment current line
                currentLine++;
            }
            // return results
            result.setReasons(failureReasons);
            result.setStatus(valid);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return result;
    }

    public static JPMCEventMessage createOneRecordData() {

        Calendar date = new GregorianCalendar(1990, 3, 7);
        logger.info(date.getTime());

        JPMCEventMessageBuilder jpmcEventMessageBuilder = JPMCEventMessageBuilder.JPMCEventMessage();


        jpmcEventMessageBuilder.withFirstName("Primary")
                               .withLastName("principal")
                               .withMiddleName("p")
                               .withSsn("123456789")
                               .withEmail("PrimaryPrincipal@gmail.com")
                               .withPhoneNumber("9980999999")
                               .withDateOfBirth(date)
                               .withAddressLine1("Address Line 1")
                               .withAddressLine2("2nd Cross")
                               .withCity("Los Angeles")
                               .withState("CA")
                               .withCountry("USA")
                               .withSourceCompanyId("120000000")
                               .withZipCode("90001")
                               .withDba("DBA")
                               .withFedTaxId("100161189")
                               .withLegalName("High Trade")
                               .withIndustrySicCode("8563")
                               .withRealmId("2341765891");

        return jpmcEventMessageBuilder.build();

    }

    @Before
    public void runBeforeEachTest() {
        JPMCReportFileFormatTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
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

    @Test
    public void testApplyFieldConstraint() {
        //Apply field constraint for OFAC Report
        OFACReport ofacReport = new OFACReport();

        String Constraint = ofacReport.applyFieldConstraint("firstAlast10", 10);
        assertEquals("Trim Constraint Failed:", Constraint, "firstAlast");

        Constraint = ofacReport.applyFieldConstraint("first,Alast", 10);
        assertEquals("Coma Constraint Failed:", Constraint, "\"first,Alas\"");

        Constraint = ofacReport.applyFieldConstraint("first'Alast", 10);
        assertEquals("quotes Constraint Failed:", Constraint, "firstAlast");

        Constraint = ofacReport.applyFieldConstraint("first\"Alast", 10);
        assertEquals("Double quotes Constraint Failed:", Constraint, "firstAlast");

        Constraint = ofacReport.applyFieldConstraint("\u0000$&", 10);
        logger.info(Constraint);

        assertEquals("Null check failed", ofacReport.applyFieldConstraint(null, 10), "");


        AMLReport amlReport = new AMLReport();
        assertEquals("SSN check failed", amlReport.applyFieldConstraint("999999999", "ssn"), "999-99-9999");
        assertEquals("Null check failed", amlReport.applyFieldConstraint(null, "ssn"), "");
        assertEquals("Length of ssn", amlReport.applyFieldConstraint("", "ssn"), "");
    }

    @Test
    public void testOFACRecord() {
        //highest match accuracy  FIRSTNAME, LASTNAME, LEGAL STREET ADDRESS, LEGAL STREET ADDRESS-Line2, LEGAL CITY, LEGAL STATE, LEGAL POSTAL, LEGAL COUNTRY_ISO3, DOB, UNIQUE_ID (PSID)

        JPMCEventMessage jpmcEventMessage = createOneRecordData();

        OFACReport ofacReport = new OFACReport();

        StringBuilder recordData = new StringBuilder();
        ofacReport.createOFACRecord(jpmcEventMessage, recordData);

        logger.info(recordData);

        assertEquals("Ofac record is not correct", recordData.toString(), JPMCReportFileFormatTest.OFAC_RECORD2);

    }

    @Test
    public void testAMLRecord() {
        JPMCEventMessage jpmcEventMessage = createOneRecordData();

        AMLReport amlReport = new AMLReport();

        StringBuilder recordData = new StringBuilder();
        amlReport.createAMLRecord(jpmcEventMessage, recordData);

        logger.info(recordData);

        assertEquals(recordData.toString(), AML_RECORD);
    }

    @Test
    public void testIndustryTypeRecord() {
        JPMCEventMessage jpmcEventMessage = createOneRecordData();

        IndustryReport industryReport = new IndustryReport();

        StringBuilder recordData = new StringBuilder();
        industryReport.createIndustryRecord(jpmcEventMessage, recordData);

        logger.info(recordData);

        assertEquals(recordData.toString(), INDUSTRY_RECORD);
    }

    @Test
    public void testCreateOFACTriggerFile() {
        OFACReport ofacReport = new OFACReport();


        SpcfCalendar dateOfRun = SpcfCalendar.createInstance(2015, 6, 29, SpcfTimeZone.getLocalTimeZone());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(dateOfRun);
        PayrollServices.commitUnitOfWork();
        //Initialize the trigger file path
        ofacReport.triggerFile = BatchUtils.getConfigString("psp_batch_ftp_send_dir") + File.separator + "OFAC_File_" + StringFormatter.formatDate(dateOfRun, "yyyyMMdd") + ".trg";

        logger.info(ofacReport.triggerFile);

        try {
            ofacReport.createTriggerFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String generatedFile = mSendDir + "/OFAC_File_" + StringFormatter.formatDate(dateOfRun, "yyyyMMdd") + ".trg";
        validateFile(Application.findFileOnClassPath("jpmcscreening/expected/OFAC_Trigger_File_Happy_Path.trg"), generatedFile);
    }
    
    
    @Test
    public void testCheckOFACReport() {

        JPMCDataLoader.createReportDataForOFAC();
        SpcfCalendar pspDate = PSPDate.getPSPTime();

        JPMCReportBase jpmcReportBase = new OFACReport();

        SpcfCalendar toDate = PSPDate.getPSPTime();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(fromDate, -1);

        try {
            Application.beginUnitOfWork();
            jpmcReportBase.createJPMCReport(fromDate, toDate);
            Application.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String generatedFile = mSendDir + "/OFAC_File_" + StringFormatter.formatDate(pspDate, "yyyyMMdd") + ".csv";
        //Read File
        validateFile(Application.findFileOnClassPath("jpmcscreening/expected/OFAC_HappyPath.csv"), generatedFile);
    }
    
    @Test
    public void testCheckOFACReportDeleteFormat() {

		JPMCDataLoader.createReportDataWithCancelledOrTerminatedDDService();

        SpcfCalendar pspDate = PSPDate.getPSPTime();

        JPMCReportBase jpmcReportBase = new OFACReport();

        SpcfCalendar toDate = PSPDate.getPSPTime();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(fromDate, -1);

        try {
            Application.beginUnitOfWork();
            jpmcReportBase.createJPMCReport(fromDate, toDate);
            Application.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String generatedFile = mSendDir + "/OFAC_File_" + StringFormatter.formatDate(pspDate, "yyyyMMdd") + ".csv";
        //Read File
        validateFile(Application.findFileOnClassPath("jpmcscreening/expected/OFAC_Delete_HappyPath.csv"), generatedFile);
    }

    @Test
    public void testCheckAMLReport() {
        JPMCDataLoader.createReportData();
        SpcfCalendar pspDate = PSPDate.getPSPTime();

        JPMCReportBase jpmcReportBase = new AMLReport();

        SpcfCalendar toDate = PSPDate.getPSPTime();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(fromDate, -1);

        try {
            Application.beginUnitOfWork();
            jpmcReportBase.createJPMCReport(fromDate, toDate);
            Application.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //validate whether the file exists
        String dataFilePath = mSendDir + File.separator + AMLReport.AML_DATA_FILE_PREFIX + pspDate.format("yyyyMMdd")+ AMLReport.ENCRYPTED_FILE_EXT;

        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
    }

    // check load for 50,000 companies
    @Test
    public void testCheckTPSUReport() {

        JPMCDataLoader.createReportData(50);
        SpcfCalendar pspDate = PSPDate.getPSPTime();

        JPMCReportBase jpmcReportBase = new TPSUReport();

        SpcfCalendar toDate = PSPDate.getPSPTime();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(fromDate, -1);

        try {
            Application.beginUnitOfWork();
            jpmcReportBase.createJPMCReport(fromDate, toDate);
            Application.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //validate whether the file exists
        String dataFilePath = mSendDir + File.separator + TPSUReport.TPS_PREFIX+ pspDate.format("yyyyMMdd")+ TPSUReport.DATA_FILE_EXT;

        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
    }

    @Test
     @Ignore//Passes only when run as single test, data creation is done in this way
    public void testCheckIndustryReport() {
        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        today.addMonths(-1);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(today);
        PayrollServices.commitUnitOfWork();

        String dateInFile = CalendarUtils.getLastDayOfMonth(PSPDate.getPSPTime()).format("yyyyMMdd");
        JPMCDataLoader.createReportDataWithCancelledOrTerminatedDDService();

        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // Frequency is First business day of the month
        SpcfCalendar mProcessingDate = PSPDate.getPSPTime();
        SpcfCalendar mPreviousMonth = mProcessingDate.copy();
        mPreviousMonth.addMonths(-1);
        CalendarUtils.clearTime(mPreviousMonth);

        SpcfCalendar mFromDate = CalendarUtils.getFirstDayOfMonth(mPreviousMonth);
        CalendarUtils.clearTime(mFromDate);

        SpcfCalendar mToDate = CalendarUtils.getFirstDayOfMonth(mProcessingDate);
        CalendarUtils.clearTime(mToDate);
        mToDate.addMilliseconds(-1);

        JPMCReportBase industryReport = new IndustryReport();

        try {
            Application.beginUnitOfWork();
            industryReport.createJPMCReport(mFromDate,mToDate);
            Application.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String dataFilePath =BatchUtils.getConfigString("psp_batch_ftp_send_dir") + File.separator + IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.ACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT;
        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());

        //Read File
       validateFile(Application.findFileOnClassPath("offload/generated/" +  IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.ACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT), Application.findFileOnClassPath("jpmcscreening/expected/Industry_ActiveDDService_HappyPath.txt"));
        validateFile(Application.findFileOnClassPath("offload/generated/" +  IndustryReport.INDUSTRY_TYPE_PREFIX + dateInFile + IndustryReport.INACTIVE_DD_SERVICE  + IndustryReport.FILE_EXT), Application.findFileOnClassPath("jpmcscreening/expected/Industry_InActiveDDService_HappyPath.txt"));
    }


    @Test
    public void testCheckOFACReportExcludeSMSCompanies() throws Exception {


        JPMCDataLoader.createReportDataForOFAC();
        JPMCDataLoader.createSMSReportData();
        SpcfCalendar pspDate = PSPDate.getPSPTime();

        JPMCReportBase jpmcReportBase = new OFACReport();

        SpcfCalendar toDate = PSPDate.getPSPTime();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(fromDate, -1);

        try {
            Application.beginUnitOfWork();
            jpmcReportBase.createJPMCReport(fromDate, toDate);
            Application.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String generatedFile = mSendDir + "/OFAC_File_" + StringFormatter.formatDate(pspDate, "yyyyMMdd") + ".csv";
        //Read File
        validateFileContent(Application.findFileOnClassPath("jpmcscreening/expected/OFAC_HappyPath.csv"), generatedFile);
    }

    @Test
    public void testCheckOFACReportIncludeSMSCompaniesUpdates() throws Exception {


        JPMCDataLoader.createReportDataForOFAC();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.QBDTWSAdapter);

        JPMCDataLoader.createSMSReportData();
        SpcfCalendar pspDate = PSPDate.getPSPTime();

        JPMCReportBase jpmcReportBase = new OFACReport();

        SpcfCalendar toDate = PSPDate.getPSPTime();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(fromDate, -1);

        try {
            Application.beginUnitOfWork();
            jpmcReportBase.createJPMCReport(fromDate, toDate);
            Application.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String generatedFile = mSendDir + "/OFAC_File_" + StringFormatter.formatDate(pspDate, "yyyyMMdd") + ".csv";
        //Read File
        validateFileContent(Application.findFileOnClassPath("jpmcscreening/expected/OFAC_File_IncludeSMSUpdates.csv"), generatedFile);
    }

    @Test
    public void testCheckTPSUReportIncludesSMSCompanies() {

        JPMCDataLoader.createReportData(50);
        JPMCDataLoader.createSMSReportData(10);
        SpcfCalendar pspDate = PSPDate.getPSPTime();

        JPMCReportBase jpmcReportBase = new TPSUReport();

        SpcfCalendar toDate = PSPDate.getPSPTime();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(fromDate, -1);

        try {
            Application.beginUnitOfWork();
            jpmcReportBase.createJPMCReport(fromDate, toDate);
            Application.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //validate whether the file exists
        String dataFilePath = mSendDir + File.separator + TPSUReport.TPS_PREFIX+ pspDate.format("yyyyMMdd")+ TPSUReport.DATA_FILE_EXT;

        validateFileContent(Application.findFileOnClassPath("jpmcscreening/expected/Intuit_TPS_File_Include_SMS.csv"), dataFilePath);

        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
    }


    //Test for DGDeletedCompanies
    @Test
    public void testCheckTPSUReportWithDGDeletedCompany() {

        JPMCDataLoader.createReportData(1);

        SpcfCalendar pspDate = PSPDate.getPSPTime();

        JPMCReportBase jpmcReportBase = new TPSUReport();

        SpcfCalendar toDate = PSPDate.getPSPTime();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(fromDate, -1);

        try {
            Application.beginUnitOfWork();
            jpmcReportBase.createJPMCReport(fromDate, toDate);
            Application.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //validate whether the file exists
        String dataFilePath = mSendDir + File.separator + TPSUReport.TPS_PREFIX + pspDate.format("yyyyMMdd") + TPSUReport.DATA_FILE_EXT;

        File dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
        validateFile(Application.findFileOnClassPath("jpmcscreening/expected/Intuit_TPS_File_SingleCompany.csv"), dataFilePath);


        //Mark the company as DGDeleted
        Application.beginUnitOfWork();
        String PSID = "12120";
        Company company = Company.findCompany(PSID, SourceSystemCode.QBDT);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        Application.commitUnitOfWork();

        try {
            Application.beginUnitOfWork();
            jpmcReportBase.createJPMCReport(fromDate, toDate);
            Application.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //validate whether the file exists
        dataFilePath = mSendDir + File.separator + TPSUReport.TPS_PREFIX + pspDate.format("yyyyMMdd") + TPSUReport.DATA_FILE_EXT;

        dataFile = new File(dataFilePath);
        Assert.assertEquals(true, dataFile.exists());
        validateFile(Application.findFileOnClassPath("jpmcscreening/expected/Intuit_TPS_File_NoCompany.csv"), dataFilePath);

    }


    public static void validateFileContent(String pExpectedFileName, String pCreatedFileName) {
        try {
            BufferedReader expectedReader = new BufferedReader(new FileReader(pExpectedFileName));
            BufferedReader compareReader;
            Key key = IDPSFileStreamManager.newKeyHandleLatest();

            if(StreamUtil.isFileIDPSEncrypted(pCreatedFileName)){
                compareReader = new BufferedReader(new IDPSFileReader(pCreatedFileName,key));
            }else {
                compareReader = new BufferedReader(new FileReader(pCreatedFileName));
            }

            CompareResults compareResults = compareFilesContent(expectedReader, compareReader);

            if (!compareResults.getStatus()) {
                System.out.println(compareResults.toString());
            }
            assertEquals("File " + pCreatedFileName + " matches expected file " + pExpectedFileName, true, compareResults.getStatus());

        } catch (Exception ex) {
            ex.printStackTrace();
            TestCase.fail(ex.getMessage());
        }
    }

    private static CompareResults compareFilesContent(BufferedReader inFile, BufferedReader compareFile) throws Exception{

        boolean valid = true;
        ArrayList failureReasons = new ArrayList();

        int Inlines = 0;
        while (inFile.readLine() != null) Inlines++;

        int Generatedlines = 0;
        while (compareFile.readLine() != null) Generatedlines++;

        if(Inlines != Generatedlines){
            valid = false;
        }


        String inFileString= null;
        String compareFileString= null;
        try {
            inFileString = String.valueOf(inFile.read());
            compareFileString=String.valueOf(compareFile.read());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        String[] inFileLines= inFileString.split("\\n");

        String[] compareFileLines=compareFileString.split("\\n");
        if(inFileLines.length != compareFileLines.length){
            failureReasons.add("The files being compared are not the same length.");
            valid = false;
        }

        CompareResults result = new CompareResults();

        for (String compareFileLine : compareFileLines) {

            boolean found = false;

            Pattern pattern = Pattern.compile(compareFileLine);

            for (String inFileLine : inFileLines) {
                Matcher matcher = pattern.matcher(inFileLine);

                if (matcher.matches()) {
                    found = true;
                    break;
                }
            }

            if (found == false) {
                valid= false;
                System.out.println(String.format("line 1: %s\n not present in generated file ",  compareFileLine.toString()));
                failureReasons.add(String.format("line 1: %s\n not present in generated file ",  compareFileLine.toString()));
            }
        }
        result.setReasons(failureReasons);
        result.setStatus(valid);

        return result;
    }

}
