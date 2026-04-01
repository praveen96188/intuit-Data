package com.intuit.sbd.payroll.psp.adapters.ade;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.RateConverterFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ADETool;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * User: Shivanandad069
 * Date: 10/29/13
 */
public class ADEDepositFrequencyMasterFileTests {

    private static final String TEST_FILE_PATH = "Adapters/ade-adapter-tests/src/test/resources/expected/";
    private static String DIR_ROOT = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_ade_directory_root");
    final static String COMPANY_ID="19670404";
    final static String COMPANY_NAME="SHIVA TEST ADE";
    final static String COMPANY_FEIN="223456789";
    private static final String ALL_STATES = "AL,AK,AZ,AR,CA,CO,CT,DE,DC,FL,GA,HI,ID,IL,IA,KS,KY,LA,ME,MD,MA,MI,MN,MS,MO,MT,NE,NV,NH,NJ,NM,NY,NC,ND,OH,OK,OR,PA,RI,SC,SD,TN,TX,UT,VT,VA,WA,WV,WI,WY";
    SimpleDateFormat simpleDateFormat =new SimpleDateFormat("MMddyyyy-hhmmss.SSS");
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 1, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        File file = new File(DIR_ROOT + ADETool.REQUEST_FOLDER);
        File []files =file.listFiles() ;
        if(files != null && files.length >0){
            for (File f : files) {
               f.delete();
            }
        }

    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void CO_Happypath_MasterFileVerification() throws Throwable {

        // CA Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19670404", "123456789", true, ServiceCode.Tax);
        String psid = company.getSourceCompanyId();

        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        company.setLegalName("ADE|Company|Name");
        company.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyLawsWithAgencyId("311076-05-1", company, "CO");
        DataLoadServices.addCompanyLawRates(company);

        // CA Company with laws, rates, etc. that is on hold.
        Company onHoldCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.Tax);
        DataLoadServices.addCompanyLawsWithAgencyId("311076-05-2", onHoldCompany, "CO");
        DataLoadServices.addCompanyLawRates(onHoldCompany);
        DataLoadServices.addCompanyOnHoldReason(onHoldCompany, ServiceSubStatusCode.AS400Hold);

        // CO Company with laws, rates, etc. that is Exempt.
        Company exemptCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456790", true, ServiceCode.Tax);
        List<CompanyLaw> claws = DataLoadServices.addCompanyLawsWithAgencyId("311076-05-3", exemptCompany, "CO");
        DataLoadServices.addCompanyLawRates(exemptCompany);
        PayrollServices.beginUnitOfWork();
        for (CompanyLaw claw : claws) {
            // Set to Exempt.
            if (claw.getLaw().getLawId().equals("7")) {
                Application.refresh(claw);
                claw.setExemptionStatus(LawStatus.Exempt);
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        // Read the assisted customer from the database and generate the pipe-delimited file for ADE.
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        generateAdeDepositFrequencyRequest("CO", 2013, 1,dateAndTime);

        // Assume the request has been sent and received.

        compareTextFiles(TEST_FILE_PATH + "HAPPY_PATH_CO_DEPOSIT_FREQUENCY_2013_1.csv", DIR_ROOT + ADETool.REQUEST_FOLDER + "CO_DEPOSIT_FREQUENCY_2013_1"+"_"+dateAndTime + ADETool.FILE_EXT);


    }

    @Test
    public void CT_Happypath_MasterFileVerification() throws Throwable {

        // CT Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19670404", "123456789", true, ServiceCode.Tax);
        String psid = company.getSourceCompanyId();

        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        company.setLegalName("ADE|Company|Name");
        company.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyLawsWithAgencyId("311076-05-1", company, "CT");
        DataLoadServices.addCompanyLawRates(company);

        // CT Company with laws, rates, etc. that is on hold.
        Company onHoldCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19670405","123456799", true, ServiceCode.Tax);
        DataLoadServices.addCompanyLawsWithAgencyId("311076-05-2", onHoldCompany, "CT");
        DataLoadServices.addCompanyLawRates(onHoldCompany);
        DataLoadServices.addCompanyOnHoldReason(onHoldCompany, ServiceSubStatusCode.AS400Hold);
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(onHoldCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        onHoldCompany.setLegalName("ADE|Company|Name2");
        onHoldCompany.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();

        // CT Company with laws, rates, etc. that is Exempt.
        Company exemptCompany =DataLoadServices.newCompany(SourceSystemCode.QBDT, "19670406","123456798", true, ServiceCode.Tax);
        List<CompanyLaw> claws = DataLoadServices.addCompanyLawsWithAgencyId("311076-05-3", exemptCompany, "CT");
        DataLoadServices.addCompanyLawRates(exemptCompany);
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(exemptCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        exemptCompany.setLegalName("ADE|Company|Name3");
        exemptCompany.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("MMddyyyy-hhmmss.SSS");
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        // Read the assisted customer from the database and generate the pipe-delimited file for ADE.
        generateAdeDepositFrequencyRequest("CT", 2013, 1,dateAndTime);

        // Assume the request has been sent and received.

        compareTextFiles(TEST_FILE_PATH + "HAPPY_PATH_CT_DEPOSIT_FREQUENCY_2013_1.csv", DIR_ROOT + ADETool.REQUEST_FOLDER + "CT_DEPOSIT_FREQUENCY_2013_1_"+dateAndTime + ADETool.FILE_EXT);


    }


    @Test
    public void test_CT_ExemptedLaw_DFTest() throws Throwable {

        createCTExemptCompany();
        // Read the assisted customer from the database and generate the pipe-delimited file for ADE.
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        generateAdeDepositFrequencyRequest("CT", 2013, 1,dateAndTime);
        isEmptyTextFiles(DIR_ROOT + ADETool.REQUEST_FOLDER + "CT_DEPOSIT_FREQUENCY_2013_1_"+dateAndTime + ADETool.FILE_EXT);
    }
    @Test
    public void test_CT_ReImbursableLaw_DFTest() throws Throwable {

        createCTReImbursableCompany();
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        // Read the assisted customer from the database and generate the pipe-delimited file for ADE.
        generateAdeDepositFrequencyRequest("CT", 2013, 1,dateAndTime);
        isEmptyTextFiles(DIR_ROOT + ADETool.REQUEST_FOLDER + "CT_DEPOSIT_FREQUENCY_2013_1_"+dateAndTime + ADETool.FILE_EXT);
    }
    @Test
    public void test_CT_InactiveLaw_DFTest() throws Throwable {

        createCTInActiveLawCompany();
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        // Read the assisted customer from the database and generate the pipe-delimited file for ADE.
        generateAdeDepositFrequencyRequest("CT", 2013, 1,dateAndTime);
        isEmptyTextFiles(DIR_ROOT + ADETool.REQUEST_FOLDER + "CT_DEPOSIT_FREQUENCY_2013_1_"+dateAndTime + ADETool.FILE_EXT);
    }
    @Test
    public void test_CO_CurrentAndFuture_DFTest() throws Throwable {

        createCOCompanyWithDF();
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        // Read the assisted customer from the database and generate the pipe-delimited file for ADE.
        generateAdeDepositFrequencyRequest("CO", 2013, 1,dateAndTime);
        compareTextFiles(TEST_FILE_PATH + "CURRENT_AND_FUTURE_CO_DEPOSIT_FREQUENCY_2013_1.csv", DIR_ROOT + ADETool.REQUEST_FOLDER + "CO_DEPOSIT_FREQUENCY_2013_1_"+dateAndTime + ADETool.FILE_EXT);
    }
    @Test
    public void test_CO_InvalidFrequency_DFTest() throws Throwable {

        createCOCompanyWithInactiveDF();
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        // Read the assisted customer from the come database and generate the pipe-delimited file for ADE.
        generateAdeDepositFrequencyRequest("CO", 2013, 1,dateAndTime);
        isEmptyTextFiles(DIR_ROOT + ADETool.REQUEST_FOLDER + "CO_DEPOSIT_FREQUENCY_2013_1_"+dateAndTime + ADETool.FILE_EXT);
    }
    @Test
    public void testOneCompanyMultiStateMasterFile() throws Throwable {
        ArrayList<String> states = new ArrayList<String>();

        // Re-initialize so we can use consistent company names.
        DataLoadServices.reinitialize();

        // Delete the request file if created in a previous test run.
        File file = new File(DIR_ROOT + ADETool.REQUEST_FOLDER + "ADE_DEPOSIT_FREQUENCY_MASTER_FILE_2013_1.csv");
        if (file.exists()) {
            assertTrue(file.delete());
        }

        // CA Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19690209", "987654321", true, ServiceCode.Tax);

        // Add CO Laws.
        List<CompanyLaw> claws =DataLoadServices.addCompanyLawsWithAgencyId("311-0765-1", company, "CO");
        DataLoadServices.addCompanyLawRates(company);
        states.add("CO");

        // Add CT Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("113-5670-3", company, "CT");
        DataLoadServices.addCompanyLawRates(company);
        states.add("CT");

        // Add GA Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "GA");
        DataLoadServices.addCompanyLawRates(company);
        states.add("GA");
        // Add IA Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "IA");
        DataLoadServices.addCompanyLawRates(company);
        states.add("IA");
        // Add IL Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "IL");
        DataLoadServices.addCompanyLawRates(company);
        states.add("IL");
        // Add KS Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "KS");
        DataLoadServices.addCompanyLawRates(company);
        states.add("KS");
        // Add MA Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "MA");
        DataLoadServices.addCompanyLawRates(company);
        states.add("MA");
        // Add MD Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "MO");
        DataLoadServices.addCompanyLawRates(company);
        states.add("MO");
        // Add OH Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "OH");
        DataLoadServices.addCompanyLawRates(company);
        states.add("OH");
        // Add VA Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "VA");
        DataLoadServices.addCompanyLawRates(company);
        states.add("VA");
        // Add WI Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "WI");
        DataLoadServices.addCompanyLawRates(company);
        states.add("WI");
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        // Run the ADE Tool for all 3 states.

        ADETool ade = new ADETool(states, 2013, 1,dateAndTime);
        ade.generateAdeDepositFrequencyRequest();

        // Verify the content.
        compareTextFiles(TEST_FILE_PATH + "ONE_COMPANY_MULTISTATE_ADE_DEPOSIT_FREQUENCY_MASTER_FILE_2013_1.csv", DIR_ROOT + ADETool.REQUEST_FOLDER + "ADE_DEPOSIT_FREQUENCY_MASTER_FILE_2013_1_"+dateAndTime+ADETool.FILE_EXT );
    }



    @Test
    public void testDFGenerationForMultiState() throws Throwable {
        int year = 2013;
        int quarter = 1;
        createMixUpCompanies();
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        String []states={"CA","CO","CT","MD","MT","PA","KS","WI"};
        generateAdeDepositFrequencyRequest(Arrays.asList(states), year, quarter,dateAndTime);
        compareTextFiles(TEST_FILE_PATH + "MULTISTATE_ADE_DEPOSIT_FREQUENCY_MASTER_FILE_2013_1.csv", DIR_ROOT + ADETool.REQUEST_FOLDER + "ADE_DEPOSIT_FREQUENCY_MASTER_FILE_2013_1_"+dateAndTime+ ADETool.FILE_EXT );
    }
    @Test
    public void testAllStateMasterFile() throws Throwable {
        int year = 2013;
        int quarter = 1;
        createMixUpCompanies();
        String dateAndTime= simpleDateFormat.format( new Date() ) ;
        generateAdeDepositFrequencyRequest(Arrays.asList(ALL_STATES.split(",")), year, quarter,dateAndTime);
        compareTextFiles(TEST_FILE_PATH + "ALL_STATES_ADE_DEPOSIT_FREQUENCY_MASTER_FILE_2013_1.csv", DIR_ROOT + ADETool.REQUEST_FOLDER + "ADE_DEPOSIT_FREQUENCY_MASTER_FILE_2013_1_"+dateAndTime+ ADETool.FILE_EXT );

    }

    private static Company createAssistedCompanyWithRates(String state, String psid, String ein, String aid) {
        // Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds(state);
        // / Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        company.setLegalName(COMPANY_NAME);
        company.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();
        lawIds.removeAll(Arrays.asList(RateConverterFactory.INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE));  //remove all inactive laws for SUI rate exchange
        DataLoadServices.addCompanyLawsWithAgencyId(aid, company, state, lawIds);
        DataLoadServices.addCompanyLawRates(company);
        return company;
    }



    private static void generateAdeDepositFrequencyRequest( String state, int year, int quarter ,String currentDateAndTime) {
        ADETool ade = new ADETool(state, year, quarter,currentDateAndTime);
        ade.generateAdeDepositFrequencyRequest();
    }

    private static void generateAdeDepositFrequencyRequest( List<String> pStates, int year, int quarter ,String currentDateAndTime) {
        ADETool ade = new ADETool(pStates, year, quarter,currentDateAndTime);
        ade.generateAdeDepositFrequencyRequest();
    }

    private void isEmptyTextFiles ( String expectedFilename) throws IOException {

        BufferedReader expectedReader = new BufferedReader(new FileReader(expectedFilename));
        int lineCount = 0;
        for ( ; ; lineCount++) {
            String expectedLine = expectedReader.readLine();

            // Files ended at the same line.
            if ( expectedLine == null) {
                return;
            }

            // One file was shorter.
            assertTrue("Files is not empty", lineCount == 0);

        }
    }

    private void compareTextFiles (String testFilename, String expectedFilename) throws IOException {

        BufferedReader testReader = new BufferedReader(new FileReader(testFilename));
        BufferedReader expectedReader = new BufferedReader(new FileReader(expectedFilename));

        for (int lineCount = 1 ; ; lineCount++) {
            String testLine = testReader.readLine();
            String expectedLine = expectedReader.readLine();

            // Files ended at the same line.
            if (testLine == null && expectedLine == null) {
                return;
            }

            // One file was shorter.
            assertFalse("Files are the same size", testLine == null || expectedLine == null);

            // Line differs.
            assertEquals("Line " + lineCount + " Contents", testLine, expectedLine);
        }
    }


    private void createNonActiveCompany(String companyname,String companyid,String jurisdiction,String fein){
        // CA Company with laws, rates, etc. that is on hold.
        com.intuit.sbd.payroll.psp.domain.Company onHoldCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(onHoldCompany);
        // / Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(onHoldCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        onHoldCompany.setLegalName(companyname);
        onHoldCompany.setDbaName(onHoldCompany.getLegalName());
        PayrollServices.commitUnitOfWork();
    }



    private void createNonActiveCompany(){
        // CA Company with laws, rates, etc. that is on hold.
        com.intuit.sbd.payroll.psp.domain.Company onHoldCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, false, ServiceCode.Tax);
        //DataLoadServices.activateTaxServiceExceptBalanceFile(onHoldCompany);
        // / Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(onHoldCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        onHoldCompany.setLegalName(COMPANY_NAME);
        onHoldCompany.setDbaName(onHoldCompany.getLegalName());
        PayrollServices.commitUnitOfWork();



    }

    private void createCTExemptCompany(){
        // CT Company with laws, rates, etc. that is Exempt.
        com.intuit.sbd.payroll.psp.domain.Company exemptCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, true, ServiceCode.Tax);
        List<CompanyLaw> claws = DataLoadServices.addCompanyLawsWithAgencyId("311076-05-1", exemptCompany, "CT");
        DataLoadServices.addCompanyLawRates(exemptCompany);
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(exemptCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        exemptCompany.setLegalName(COMPANY_NAME);
        exemptCompany.setDbaName(exemptCompany.getLegalName());
        for (CompanyLaw claw : claws) {
            // Set CA SUI_ER to Exempt.
            if (claw.getLaw().getLawId().equals("8")) {
                Application.refresh(claw);
                claw.setExemptionStatus(LawStatus.Exempt);
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

    }


    private void createCTReImbursableCompany(){
        // CT Company with laws, rates, etc. that is Exempt.
        com.intuit.sbd.payroll.psp.domain.Company exemptCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, true, ServiceCode.Tax);
        List<CompanyLaw> claws = DataLoadServices.addCompanyLawsWithAgencyId("311076-05-1", exemptCompany, "CT");
        DataLoadServices.addCompanyLawRates(exemptCompany);
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(exemptCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        exemptCompany.setLegalName(COMPANY_NAME);
        exemptCompany.setDbaName(exemptCompany.getLegalName());
        for (CompanyLaw claw : claws) {
            // Set CA SUI_ER to Exempt.
            if (claw.getLaw().getLawId().equals("8")) {
                Application.refresh(claw);
                claw.setReimbursableStatus(ReimbursableStatus.Reimbursable);
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

    }

    private void createCTInActiveLawCompany(){
        // CT Company with laws, rates, etc. that is Exempt.
        com.intuit.sbd.payroll.psp.domain.Company exemptCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, true, ServiceCode.Tax);
        List<CompanyLaw> claws = DataLoadServices.addCompanyLawsWithAgencyId("311076-05-1", exemptCompany, "CT");
        DataLoadServices.addCompanyLawRates(exemptCompany);
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(exemptCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        exemptCompany.setLegalName(COMPANY_NAME);
        exemptCompany.setDbaName(exemptCompany.getLegalName());
        for (CompanyLaw claw : claws) {
            // Set CA SUI_ER to Exempt.
            if (claw.getLaw().getLawId().equals("8")) {
                Application.refresh(claw);
                claw.setFilingStatus(PayrollItemStatus.Inactive);
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

    }
    private void createCompanyWithLaws(String companyname,String companyid,String jurisdiction,String fein){
        // CA Company with laws, rates, etc.
        com.intuit.sbd.payroll.psp.domain.Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyid, fein, true, ServiceCode.Tax);
        String psid = company.getSourceCompanyId();
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        company.setLegalName(companyname);
        company.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addCompanyLawsWithAgencyId("311-0765-1", company, jurisdiction == null ?"CA":jurisdiction);
        DataLoadServices.addCompanyLawRates(company);


    }
    void createMixUpCompanies(){
            createCompanyWithLaws(COMPANY_NAME+"1","19670405","MT","213456789");
            createCompanyWithLaws(COMPANY_NAME+"2","19670406","MD","223456789");
            createCompanyWithLaws(COMPANY_NAME+"3","19670407","PA","233456789");
            createCompanyWithLaws(COMPANY_NAME+"4","19670408","CA","243456789");
            createNonActiveCompany(COMPANY_NAME+"5","19670409","MT","253456789");
            createNonActiveCompany(COMPANY_NAME+"6","19670410","MD","263456789");
            //  createNonActiveCompany(COMPANY_NAME+"7","19670404","PA","273456789");
            createCompanyWithLaws(COMPANY_NAME+"8","19670403","RI","283456789");
            createAssistedCompanyWithRates("CO", "19670411", "987654321", "311076-05-1");
            createAssistedCompanyWithRates("CO", "19670412", "987654322", "");
            createAssistedCompanyWithRates("CO", "19670413", "987654323", "");
            createAssistedCompanyWithRates("CO", "19670414", "987654324", "311076-05-1");
            createAssistedCompanyWithRates("CO", "19670415", "987654325", "311076-05-1");
            createAssistedCompanyWithRates("CO", "19670416", "987654326", "311076-05-1");
            createAssistedCompanyWithRates("CO", "19670417", "987654327", "311076-05-1");
            createAssistedCompanyWithRates("CO", "19670418", "987654328", "311076-05-1");
            createAssistedCompanyWithRates("CO", "19670419", "987654329", "311076-05-1");
            createAssistedCompanyWithRates("CO", "19670420", "987654330", "311076-05-1");
            createAssistedCompanyWithRates("CO", "19670422", "987654331", "311076-05-1");
            createAssistedCompanyWithRates("CO", "19670421", "987654332", "311076-05-1");
            createNonActiveCompany();
    }

    public void createCOCompanyWithDF() {
        String companyPSID = COMPANY_ID;
        createAssistedCompanyWithRates("CO", COMPANY_ID, "987654332", "311076-05-1");


        //Get the latest deposit frequency for the payment template
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto();
        com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(COMPANY_ID, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, dto.getPaymentTemplateCd());
        EffectiveDepositFrequency existingLatestDepositFrequency =
                EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate,
                                                                              dto.getEffectiveDate());
        PayrollServices.commitUnitOfWork();

        // / Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        company.setLegalName(COMPANY_NAME);
        company.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();
        //Update the deposit frequency
        PayrollServices.beginUnitOfWork();
        dto.setPaymentFrequencyId(DepositFrequencyCode.SEMIWEEKLY);

        CalendarUtils.addBusinessDays(existingLatestDepositFrequency.getEffectiveDate(), -1);
        SpcfCalendar newEffectiveDate = existingLatestDepositFrequency.getEffectiveDate();
        CalendarUtils.addBusinessDays(newEffectiveDate, -1);
        dto.setEffectiveDate(newEffectiveDate);
        ProcessResult processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, COMPANY_ID, dto);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Effective Deposit Frequency", processResult);

        //Update the deposit frequency
        PayrollServices.beginUnitOfWork();
        dto.setPaymentFrequencyId(DepositFrequencyCode.MONTHLY);

        CalendarUtils.addBusinessDays(existingLatestDepositFrequency.getEffectiveDate(), 30);
        newEffectiveDate = existingLatestDepositFrequency.getEffectiveDate();
        CalendarUtils.addBusinessDays(newEffectiveDate, 30);
        dto.setEffectiveDate(newEffectiveDate);
        processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, COMPANY_ID, dto);
        PayrollServices.commitUnitOfWork();

        //Update the deposit frequency
        PayrollServices.beginUnitOfWork();
        dto.setPaymentFrequencyId(DepositFrequencyCode.QUARTERMONTHLY);

        CalendarUtils.addBusinessDays(existingLatestDepositFrequency.getEffectiveDate(), 95);
        newEffectiveDate = existingLatestDepositFrequency.getEffectiveDate();
        CalendarUtils.addBusinessDays(newEffectiveDate, 30);
        dto.setEffectiveDate(newEffectiveDate);
        processResult = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, COMPANY_ID, dto);
        PayrollServices.commitUnitOfWork();

    }

    public void createCOCompanyWithInactiveDF() {
        String companyPSID = COMPANY_ID;
        createAssistedCompanyWithRates("CO", COMPANY_ID, "987654332", "311076-05-1");


        //Get the latest deposit frequency for the payment template
        PayrollServices.beginUnitOfWork();
        EffectiveDepositFrequencyDTO dto = getDto();
        com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(COMPANY_ID, SourceSystemCode.QBDT);
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, dto.getPaymentTemplateCd());
        EffectiveDepositFrequency existingLatestDepositFrequency =
                EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(company, paymentTemplate,
                                                                              dto.getEffectiveDate());
        existingLatestDepositFrequency.setInvalidDate(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();


}
    private EffectiveDepositFrequencyDTO getDto() {
        EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
        dto.setAgencyId("CODOR");
        SpcfCalendar newEffectiveDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(newEffectiveDate, 5);

        dto.setEffectiveDate(newEffectiveDate);
        dto.setPaymentTemplateCd("CO-DR1094-PAYMENT");
        dto.setPaymentFrequencyId(DepositFrequencyCode.QUARTERLY);

        return dto;
    }

}
