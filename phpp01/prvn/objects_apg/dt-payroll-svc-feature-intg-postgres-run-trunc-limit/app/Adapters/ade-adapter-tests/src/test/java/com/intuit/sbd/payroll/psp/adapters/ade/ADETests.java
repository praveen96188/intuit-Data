package com.intuit.sbd.payroll.psp.adapters.ade;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.json.TaxItemLawMap;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.RateConverterFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.processes.RateApplyProcess;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ADERateUtils;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ADETool;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.utils.FileUtils;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: TimothyD698
 * Date: 1/7/13
 */
public class ADETests {





    private static final String TEST_FILE_PATH = "Adapters/ade-adapter-tests/src/test/resources/expected/";
    private static String DIR_ROOT = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_ade_directory_root");

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
    public void CA_WithExceptions_AndMasterFileVerification() throws Throwable {

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

        DataLoadServices.addCompanyLawsWithAgencyId("311-0765-1", company, "CA");
        DataLoadServices.addCompanyLawRates(company);

        // CA Company with laws, rates, etc. that is on hold.
        Company onHoldCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.Tax);
        DataLoadServices.addCompanyLawsWithAgencyId("123-4567-8", onHoldCompany, "CA");
        DataLoadServices.addCompanyLawRates(onHoldCompany);
        DataLoadServices.addCompanyOnHoldReason(onHoldCompany, ServiceSubStatusCode.AS400Hold);

        // CA Company with laws, rates, etc. that is Exempt.
        Company exemptCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456790", true, ServiceCode.Tax);
        List<CompanyLaw> claws = DataLoadServices.addCompanyLawsWithAgencyId("123-4567-9", exemptCompany, "CA");
        DataLoadServices.addCompanyLawRates(exemptCompany);
        PayrollServices.beginUnitOfWork();
        for (CompanyLaw claw : claws) {
            // Set CA SUI_ER to Exempt.
            if (claw.getLaw().getLawId().equals("87")) {
                Application.refresh(claw);
                claw.setExemptionStatus(LawStatus.Exempt);
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        // Read the assisted customer from the database and generate the pipe-delimited file for ADE.
        generateAdeRequest("CA", 2013, 1);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("CAEDD", 2013, 1, AgencyRateRequestStatus.ResponseReceived);

        compareTextFiles(TEST_FILE_PATH + "CA_Happy_ExpectedOutput.csv", DIR_ROOT + ADETool.REQUEST_FOLDER + "CA_2013_1" + ADETool.FILE_EXT);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CA_Happy_TestData.json", "CA_2013_1_" +
                agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates.
        applyAdeRates( "CA", 2013, 1 );

        assertEquals("Base SUI Rate", new Double(0.036d), getCurrentRate("CA SUI-ER", psid, 2013, 1));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("ETT", psid, 2013, 1));
    }

    @Test
    public void NJ_WithMasterFileVerification() throws Throwable {

        // CA Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19690209", "987654321", true, ServiceCode.Tax);
        String psid = company.getSourceCompanyId();

        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        company.setLegalName("Piscataway Pete's");
        company.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addCompanyLawsWithAgencyId("113-5670-3", company, "NJ");
        DataLoadServices.addCompanyLawRates(company);

        // Read the assisted customer from the database and generate the pipe-delimited file for ADE.
        generateAdeRequest("NJ", 2013, 1);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NJDLWD", 2013, 1, AgencyRateRequestStatus.ResponseReceived);

        // Verify the content.
        compareTextFiles(TEST_FILE_PATH + "NJ_Happy_ExpectedOutput.csv", DIR_ROOT + ADETool.REQUEST_FOLDER + "NJ_2013_1" + ADETool.FILE_EXT);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NJ_Happy_TestData.json", "NJ_2013_1_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates.
        applyAdeRates( "NJ", 2013, 1 );

        assertEquals("Base SUI Rate", new Double(0.036d), getCurrentRate("NJ SUI-ER", psid, 2013, 1));
        assertEquals("Supplemental SUI Rate", new Double(0.00118d), getCurrentRate("SUP-NJ WDF", psid, 2013, 1));
        assertEquals("SDI Rate", new Double(0.002d), getCurrentRate("NJ SDI-ER", psid, 2013, 1));
    }

    @Test
    public void testMultiStateMasterFile() throws Throwable {
        ArrayList<String> states = new ArrayList<String>();

        // Re-initialize so we can use consistent company names.
        DataLoadServices.reinitialize();

        // Delete the request file if created in a previous test run.
        File file = new File(DIR_ROOT + ADETool.REQUEST_FOLDER + "ADE_MASTER_FILE_2013_2.csv");
        if (file.exists()) {
            assertTrue(file.delete());
        }

        // CA Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19690209", "987654321", true, ServiceCode.Tax);

        // Add California Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("311-0765-1", company, "CA");
        DataLoadServices.addCompanyLawRates(company);
        states.add("CA");

        // Add New Jersey Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("113-5670-3", company, "NJ");
        DataLoadServices.addCompanyLawRates(company);
        states.add("NJ");

        // Add Massachusetts Laws.
        DataLoadServices.addCompanyLawsWithAgencyId("456-9876-2", company, "MA");
        DataLoadServices.addCompanyLawRates(company);
        states.add("MA");

        // Run the ADE Tool for all 3 states.
        ADETool ade = new ADETool(states, 2013, 2);
        ade.generateAdeRequest();

        // Verify the content.
        compareTextFiles(TEST_FILE_PATH + "ADE_MASTER_FILE_2013_2.csv", DIR_ROOT + ADETool.REQUEST_FOLDER + "ADE_MASTER_FILE_2013_2.csv" );
    }


    @Test
    public void testItemLawMap() {

        PayrollServices.beginUnitOfWork();

        assertEquals("142", TaxItemLawMap.getLawId("CA", "Employment Training Tax"));

        assertNull(TaxItemLawMap.getLawId("Not a State", "Employment Training Tax"));
        assertNull(TaxItemLawMap.getLawId("CA", "Not a Tax Item"));

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void AR_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("AR", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("AR", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("ARESD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("AR_Round_Trip.json", "AR_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("AR", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.1105d), getCurrentRate("AR SUI-ER", psid, year, quarter));
       //This is inactive now
       assertEquals("Supplemental SUI Rate", null, getCurrentActiveRateWithoutAssert("SUP-AR ST", psid, year, quarter));

        // Company request should not have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertNotSame("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void AZ_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("AZ", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("AZ", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("AZDES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("AZ_Round_Trip.json", "AZ_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("AZ", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.008d), getCurrentRate("AZ SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("SUP-AZ TT", psid, year, quarter));
    }

    @Test
    public void CA_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("CA", psid, "987654321", "311-0765-1");

        // Create the request.
        generateAdeRequest("CA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("CAEDD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CA_Round_Trip.json", "CA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("CA", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.036d), getCurrentRate("CA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("ETT", psid, year, quarter));
    }
    @Test
    public void CA_Round_Trip_With_NoETTRateChanged() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        Company company=createAssistedCompanyWithRates("CA", psid, "987654321", "311-0765-1");
        DataLoadServices.changeCompanyLawRates(company,"142",0.001);
        // Create the request.
        generateAdeRequest("CA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("CAEDD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CA_Round_Trip_With_NoETTRateChanged.json", "CA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("CA", year, quarter);
        CompanyRateRequest   rateRequest= findCompanyRateRequest("CAEDD",company,year,quarter);
        assertNotNull("Company Rate Requests",rateRequest );
        assertEquals("Company Rate Request Status",RateRequestStatus.Applied,rateRequest.getStatus());

        assertEquals("Base SUI Rate", new Double(0.036d), getCurrentRate("CA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("ETT", psid, year, quarter));
    }
    @Test
    public void CA_Round_Trip_With_NoUIRateChanged() {
        int year = 2013;
        int quarter = 1;
        String psid = "100010002";

        Company company=createAssistedCompanyWithRates("CA", psid, "987654322", "311-0766-1");
       // DataLoadServices.changeCompanyLawRates(company,"142",0.004);
        // Create the request.
        generateAdeRequest("CA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("CAEDD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CA_Round_Trip_With_NoETTRateChanged.json", "CA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("CA", year, quarter);
        CompanyRateRequest   rateRequest= findCompanyRateRequest("CAEDD",company,year,quarter);
        assertNotNull("Company Rate Requests",rateRequest );
        assertEquals("Company Rate Request Status",RateRequestStatus.NoChange,rateRequest.getStatus());

        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("CA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("ETT", psid, year, quarter));
    }
    @Test
    public void CA_Round_Trip_With_NoUIAndETTRateChanged() {
        int year = 2013;
        int quarter = 1;
        String psid = "100010002";

        Company company=createAssistedCompanyWithRates("CA", psid, "987654322", "311-0766-1");
        DataLoadServices.changeCompanyLawRates(company,"142",0.001);
        // Create the request.
        generateAdeRequest("CA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("CAEDD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CA_Round_Trip_With_NoETTRateChanged.json", "CA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("CA", year, quarter);
        CompanyRateRequest   rateRequest= findCompanyRateRequest("CAEDD",company,year,quarter);
        assertNotNull("Company Rate Requests",rateRequest );
        assertEquals("Company Rate Request Status",RateRequestStatus.NoChange,rateRequest.getStatus());

        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("CA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("ETT", psid, year, quarter));
    }
    @Test
    public void CA_Not_Enrolled() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        // Create a California company.
        Company company = createAssistedCompanyWithRates("CA", psid, "987654321", "311-0765-1");

        // Create the request for California.
        generateAdeRequest("CA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("CAEDD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CA_Round_Trip.json", "CA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Delete the rates and company law info for this company as if they are not enrolled in CA.
        Application.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "87");
        for (CompanyLawRate rate : companyLaw.getCompanyLawRateCollection()) {
            Application.delete(rate);
        }
        Application.delete(companyLaw.getQbdtPayrollItemInfo());
        Application.delete(companyLaw);
        Application.commitUnitOfWork();

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("CA", year, quarter);

        // Verify that it was an error.
        DomainEntitySet<CompanyRateRequest> request = Application.find(CompanyRateRequest.class);
        assertEquals("Request Size", 1, request.size());
        assertEquals("Request Status", RateRequestStatus.Error, request.getFirst().getStatus());
    }

    @Test
    public void CO_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("CO", psid, "987654321", "311076-05-1");

        // Create the request.
        generateAdeRequest("CO", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("CODLE", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CO_Round_Trip.json", "CO_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("CO", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.017d), getCurrentRate("CO SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0033d), getCurrentRate("SUP-CO ST", psid, year, quarter));
    }

    @Test
    public void CO_Qtr3_Round_Trip() {
        int year = 2013;
        int quarter = 3;
        String psid = "199210091";

        createAssistedCompanyWithRates("CO", psid, "987654321", "311076-05-1");

        // Create the request.
        generateAdeRequest("CO", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("CODLE", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CO_Qtr3_Round_Trip.json", "CO_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("CO", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0031d), getCurrentRate("CO SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0033d), getCurrentRate("SUP-CO ST", psid, year, quarter));
    }

    @Test
    public void CT_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("CT", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("CT", year, quarter);

        String agencyName = "CTDOL";

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest(agencyName, year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CT_Round_Trip.json", "CT_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("CT", year, quarter, agencyName);

        assertEquals("Base SUI Rate", new Double(0.068d), getCurrentRate("CT SUI-ER", psid, year, quarter));
    }

    @Test
    public void FL_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("FL", psid, "987654321", "1215304");

        // Create the request.
        generateAdeRequest("FL", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("FLDOR", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("FL_Round_Trip.json", "FL_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("FL", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0102d), getCurrentRate("FL SUI-ER", psid, year, quarter));
    }

    @Test
    public void GA_Round_Trip_With_NoSUPRateChanged() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        Company company=createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");
        DataLoadServices.changeCompanyLawRates(company,"154",0.0006);
        // Create the request.
        generateAdeRequest("GA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("GADOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("GA_Round_Trip_With_NoSUPRateChanged.json", "GA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("GA", year, quarter);
        CompanyRateRequest   rateRequest= findCompanyRateRequest("GADOL",company,year,quarter);
        assertNotNull("Company Rate Requests",rateRequest );
        assertEquals("Company Rate Request Status",RateRequestStatus.Applied,rateRequest.getStatus());

        assertEquals("Base SUI Rate", new Double(0.0092d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0006d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }

    @Test
    public void GA_Round_Trip_With_NoUIRateChanged() {
        int year = 2013;
        int quarter = 1;
        String psid = "20296020";

        Company company=createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");
        DataLoadServices.changeCompanyLawRates(company,"93",0.0092);
        // Create the request.
        generateAdeRequest("GA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("GADOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("GA_Round_Trip_With_NoSUPRateChanged.json", "GA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("GA", year, quarter);
        CompanyRateRequest   rateRequest= findCompanyRateRequest("GADOL",company,year,quarter);
        assertNotNull("Company Rate Requests",rateRequest );
        assertEquals("Company Rate Request Status",RateRequestStatus.NoChange,rateRequest.getStatus());

        assertEquals("Base SUI Rate", new Double(0.0092d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0006d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }
    @Test
    public void GA_Round_Trip_With_NoUIAndSUPRateChanged() {
        int year = 2013;
        int quarter = 1;
        String psid = "20296020";

        Company company=createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");
        DataLoadServices.changeCompanyLawRates(company,"93",0.0092);
        DataLoadServices.changeCompanyLawRates(company,"154",0.0006);
        // Create the request.
        generateAdeRequest("GA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("GADOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("GA_Round_Trip_With_NoSUPRateChanged.json", "GA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("GA", year, quarter);

        CompanyRateRequest   rateRequest= findCompanyRateRequest("GADOL",company,year,quarter);
        assertNotNull("Company Rate Requests",rateRequest );
        assertEquals("Company Rate Request Status",RateRequestStatus.NoChange,rateRequest.getStatus());

        assertEquals("Base SUI Rate", new Double(0.0092d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0006d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }

    private CompanyRateRequest findCompanyRateRequest(String pAgencyName, Company pCompany, int pYear, int pQuarter) {
        //To change body of created methods use File | Settings | File Templates.
        AgencyRateRequest request = getAgencyRequestAllStatuses(pAgencyName, pYear, pQuarter);
        if(request == null){
            return null;
        }

        DomainEntitySet<CompanyRateRequest> rateRequests = Application.find(CompanyRateRequest.class,
                                                                            CompanyRateRequest.AgencyRateRequest().equalTo(request)
                                                                                              .And(CompanyRateRequest.CompanyAgency().Company().equalTo(pCompany)));

        return (rateRequests==null || rateRequests.size() ==0) ?null:rateRequests.get(0);
    }

    @Test
    public void GA_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");

        // Create the request.
        generateAdeRequest("GA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("GADOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("GA_Round_Trip.json", "GA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("GA", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0004d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }

    @Test
    public void GA_Qtr2_Round_Trip() {
        int year = 2013;
        int quarter = 2;
        String psid = "199210091";

        createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");

        // Create the request.
        generateAdeRequest("GA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("GADOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("GA_Qtr2_Round_Trip.json", "GA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("GA", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0342d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0008d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }

    @Test
    public void GA_Qtr2_Supp_Max_Round_Trip() {
        int year = 2013;
        int quarter = 2;
        String psid = "16358080";

        createAssistedCompanyWithRates("GA", psid, "200924255", "915483-03");

        // Create the request.
        generateAdeRequest("GA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("GADOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("GA_Qtr2_Round_Trip.json", "GA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("GA", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.081d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }

    @Test
    public void HI_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("HI", psid, "987654321", "0007381875");

        // Create the request.
        generateAdeRequest("HI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("HIDLIR", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("HI_Round_Trip.json", "HI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("HI", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.018d), getCurrentRate("HI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0001d), getCurrentRate("SUP HI ETA", psid, year, quarter));
    }

    @Test
    public void ID_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("ID", psid, "987654321", "4595533-0");

        // Create the request.
        generateAdeRequest("ID", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("IDCL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("ID_Round_Trip.json", "ID_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("ID", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.034435d), getCurrentRate("ID SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001065d), getCurrentRate("SUP-ID WDF", psid, year, quarter));
    }

    @Test
    public void IL_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("IL", psid, "987654321", "4595533-0");

        // Create the request.
        generateAdeRequest("IL", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("ILDES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("IL_Round_Trip.json", "IL_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("IL", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0055d), getCurrentRate("IL SUI-ER", psid, year, quarter));
    }

    @Test
    public void KS_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("KS", psid, "987654321", "06502929");

        // Create the request.
        generateAdeRequest("KS", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("KSDOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("KS_Round_Trip.json", "KS_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("KS", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.076d), getCurrentRate("KS SUI-ER", psid, year, quarter));
    }

    @Test
    public void KY_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("KY", psid, "987654321", "06502929");

        // Create the request.
        generateAdeRequest("KY", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("KYDES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("KY_Round_Trip.json", "KY_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("KY", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.06283d), getCurrentRate("KY SUI-ER", psid, year, quarter));
        assertEquals("KY Surcharge", new Double(0.0021d), getCurrentRate("SUP-KY SC", psid, year, quarter));
    }
    @Test
    public void KY_Round_Trip_Zero_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("KY", psid, "987654321", "06502929");

        // Create the request.
        generateAdeRequest("KY", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("KYDES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("KY_Round_Trip_Zero_Rate.json", "KY_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("KY", year, quarter);

        //This means as  base law has 0%, it will be updated.So old value remains.
        assertEquals("Base SUI Rate", new Double(0.03), getCurrentRate("KY SUI-ER", psid, year, quarter));
        assertEquals("KY Surcharge", new Double(0.03d), getCurrentRate("SUP-KY SC", psid, year, quarter));
    }
    @Test
    public void LA_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("LA", psid, "987654321", "06502929");

        // Create the request.
        generateAdeRequest("LA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("LADOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("LA_Round_Trip.json", "LA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("LA", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.059d), getCurrentRate("LA SUI-ER", psid, year, quarter));
        // SUP-LA SCT is not defined in ADE.
        // assertEquals("Supplemental SUI Rate", new Double(0.00075d), getCurrentRate("SUP-LA RSF", psid, year, quarter));
    }

    @Test
    public void MA_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MA", psid, "987654321", "311-0765-1");

        // Create the request.
        generateAdeRequest("MA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MAWUA", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MA_Round_Trip.json", "MA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MA", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0283d), getCurrentRate("MA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0006d), getCurrentRate("SUP-MA WTF", psid, year, quarter));
        // TODO: TLD - This is a NO-CALC law that has no rates in PSP, but was generated on the AS/400.  Is QB generating automatically?
        // assertEquals("UHI", new Double(0.015d), getCurrentRate("UHI", psid, year, quarter));
    }
    @Test
    public void MA_Round_Trip_EMAC() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 1, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        int year = 2014;
        int quarter = 1;
        String psid = "199210091";

        Company company=createAssistedCompanyWithRates("MA", psid, "987654321", "311-0765-1");

        // Create the request.
        generateAdeRequest("MA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MAWUA", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MA_Round_Trip_EMAC.json", "MA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MA", year, quarter);
        CompanyRateRequest   rateRequest= findCompanyRateRequest("MAWUA",company,year,quarter);
        assertNotNull("Company Rate Requests",rateRequest );
        assertEquals("Company Rate Request Status",RateRequestStatus.Applied,rateRequest.getStatus());

        assertEquals("Base SUI Rate", new Double(0.0160d), getCurrentRate("MA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0006d), getCurrentRate("SUP-MA WTF", psid, year, quarter));
        assertEquals("MA Er Medical Assistance Contribution", new Double(0.0009d), getCurrentAdditinalFilingRate("MA Er Medical Assistance Contribution", psid, year, quarter));
    }
    @Test
    public void MA_NoChangeInUIButChangeInEMAC() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 1, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        int year = 2014;
        int quarter = 1;
        String psid = "16241910";

        Company company= createAssistedCompanyWithRates("MA", psid, "987654321", "311-0765-1");

        // Create the request.
        generateAdeRequest("MA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MAWUA", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MA_Round_Trip_EMAC.json", "MA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MA", year, quarter);
        CompanyRateRequest   rateRequest= findCompanyRateRequest("MAWUA",company,year,quarter);
        assertNotNull("Company Rate Requests",rateRequest );
        assertEquals("Company Rate Request Status",RateRequestStatus.NoChange,rateRequest.getStatus());

        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0006d), getCurrentRate("SUP-MA WTF", psid, year, quarter));
        assertEquals("MA Er Medical Assistance Contribution", new Double(0.0008d), getCurrentAdditinalFilingRate("MA Er Medical Assistance Contribution", psid, year, quarter));
    }
    @Test
    public void MA_ErrorInBaseUIApply_EMAC() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 1, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        int year = 2014;
        int quarter = 1;
        String psid = "16240196";

        Company company= createAssistedCompanyWithRates("MA", psid, "987654321", "311-0765-1");

        // Create the request.
        generateAdeRequest("MA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MAWUA", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MA_Round_Trip_EMAC.json", "MA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MA", year, quarter);
        CompanyRateRequest   rateRequest= findCompanyRateRequest("MAWUA",company,year,quarter);
        assertNotNull("Company Rate Requests",rateRequest );
        assertEquals("Company Rate Request Status",RateRequestStatus.Applied,rateRequest.getStatus());

        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MA WTF", psid, year, quarter));
        assertEquals("MA Er Medical Assistance Contribution", new Double(0.0008), getCurrentAdditinalFilingRate("MA Er Medical Assistance Contribution", psid, year, quarter));
    }


    @Test
    public void MD_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MD", psid, "987654321", "4595533-0");

        // Create the request.
        generateAdeRequest("MD", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MDOUI", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MD_Round_Trip.json", "MD_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MD", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.075d), getCurrentRate("MD SUI-ER", psid, year, quarter));
    }

    @Test
    public void ME_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("ME", psid, "987654321", "10-07818-1");

        // Create the request.
        generateAdeRequest("ME", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MEUITD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("ME_Round_Trip.json", "ME_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("ME", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.064d), getCurrentRate("ME SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0006d), getCurrentRate("SUP-ME CSSF", psid, year, quarter));
        // ME Surtax is not defined in ADE
        // assertEquals("Supplemental SUI Rate", new Double(0.0006d), getCurrentRate("SUP-ME ST", psid, year, quarter));
    }

    @Test
    public void MI_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MI", psid, "987654321", "10-07818-1");

        // Create the request.
        generateAdeRequest("MI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MIDLEG", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MI_Round_Trip.json", "MI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MI", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.06625d), getCurrentRate("MI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0075d), getCurrentRate("SUP-MI OA", psid, year, quarter));
    }

    @Test
    public void MN_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MN", psid, "987654321", "10-07818-1");

        // Create the request.
        generateAdeRequest("MN", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MNEEUI", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MN_Round_Trip.json", "MN_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MN", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.04d), getCurrentRate("MN SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("SUP-MN DWA", psid, year, quarter));
    }

    @Test
    public void MO_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MO", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("MO", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MODES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MO_Round_Trip.json", "MO_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MO", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0283d), getCurrentRate("MO SUI-ER", psid, year, quarter));
    }

    @Test
    public void MS_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MS", psid, "987654321", "17 08366 0 00");

        // Create the request.
        generateAdeRequest("MS", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MSDES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MS_Round_Trip.json", "MS_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MS", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.054d), getCurrentRate("MS SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-MS WTEF", psid, year, quarter));
    }
    @Test
    public void MS_Round_TripHaveWetRate() {
        int year = 2013;
        int quarter = 1;
        String psid = "2222326";

        createAssistedCompanyWithRates("MS", psid, "987654321", "17 08366 0 00");

        // Create the request.
        generateAdeRequest("MS", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MSDES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MS_Round_Trip.json", "MS_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MS", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.054d), getCurrentRate("MS SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0016d), getCurrentRate("SUP-MS WTEF", psid, year, quarter));
    }

    @Test
    public void MT_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MT", psid, "987654321", "2036142");

        // Create the request.
        generateAdeRequest("MT", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MTUID", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MT_Round_Trip.json", "MT_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MT", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.020d), getCurrentRate("MT SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0018d), getCurrentRate("SUP-MT AFT", psid, year, quarter));
    }

    @Test
    public void NC_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NC", psid, "987654321", "10-07818-1");

        // Create the request.
        generateAdeRequest("NC", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NCESC", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NC_Round_Trip.json", "NC_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NC", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0473d), getCurrentRate("NC SUI-ER", psid, year, quarter));
    }

    @Test
    public void ND_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("ND", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("ND", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NDJS", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("ND_Round_Trip.json", "ND_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("ND", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0225d), getCurrentRate("ND SUI-ER", psid, year, quarter));
    }

    @Test
    public void NE_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NE", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("NE", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NEWD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NE_Round_Trip.json", "NE_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NE", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0d), getCurrentRate("NE SUI-ER", psid, year, quarter));
    }

    @Test
    public void NJ_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NJ", psid, "987654321", "06502929");

        // Create the request.
        generateAdeRequest("NJ", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NJDLWD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NJ_Round_Trip.json", "NJ_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NJ", year, quarter);

        // Base rate and WDF rates from example are both getting rounded.
        assertEquals("Base SUI Rate", new Double(0.032825d), getCurrentRate("NJ SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00118d), getCurrentRate("SUP-NJ WDF", psid, year, quarter));
        assertEquals("SDI Rate", new Double(0.005d), getCurrentRate("NJ SDI-ER", psid, year, quarter));
        assertEquals("Healthcare Rate", new Double(0.0d), getCurrentRate("SUP-NJ HSF", psid, year, quarter));
        assertEquals("FLI Rate", new Double(0.004d), getCurrentRate("SUP-NJ FLI", psid, year, quarter));
        //test law 171
        assertEquals("Special Healthcare Contributions", null, getCurrentActiveRateWithoutAssert("SUP-NJ SHC", psid, year, quarter));

    }
    @Test
    public void NJ_Round_Trip_InActiveFilingStatus() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        //createAssistedCompanyWithRates("NJ", psid, "987654321", "06502929");
        Company company=createAssistedCompanyWithRatesOld("NJ", psid, "987654321", "06502929");
        // Create the request.
        generateAdeRequest("NJ", year, quarter);
        Application.beginUnitOfWork();
        CompanyLaw companyLaw=CompanyLaw.findCompanyLaw(company,"171");
        companyLaw.setFilingStatus(PayrollItemStatus.Inactive);
        Application.commitUnitOfWork();

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NJDLWD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NJ_Round_Trip.json", "NJ_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NJ", year, quarter);

        // Base rate and WDF rates from example are both getting rounded.
        assertEquals("Base SUI Rate", new Double(0.032825d), getCurrentRate("NJ SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00118d), getCurrentRate("SUP-NJ WDF", psid, year, quarter));
        assertEquals("SDI Rate", new Double(0.005d), getCurrentRate("NJ SDI-ER", psid, year, quarter));
        assertEquals("Healthcare Rate", new Double(0.0d), getCurrentRate("SUP-NJ HSF", psid, year, quarter));
        assertEquals("FLI Rate", new Double(0.004d), getCurrentRate("SUP-NJ FLI", psid, year, quarter));
        assertEquals("Special Healthcare Contributions", null, getCurrentActiveRateWithoutAssert("SUP-NJ SHC", psid, year, quarter));

    }

    @Ignore
    @Test
    public void NM_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NM", psid, "987654321", "10-07818-1");

        // Create the request.
        generateAdeRequest("NM", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NMDOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NM_Round_Trip.json", "NM_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NM", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0225d), getCurrentRate("NM SUI-ER", psid, year, quarter));
        // NM Trust Fund is not defined in ADE.
        //assertEquals("Supplemental SUI Rate", new Double(0.00075d), getCurrentRate("SUP-NM TF", psid, year, quarter));
    }

    @Test
    public void NH_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NH", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("NH", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NHES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NH_Round_Trip.json", "NH_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NH", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.105d), getCurrentRate("NH SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.002d), getCurrentRate("SUP-NH AC", psid, year, quarter));
    }

    @Test
    public void NV_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NV", psid, "987654321", "4595533-0");

        // Create the request.
        generateAdeRequest("NV", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NVETR", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NV_Round_Trip.json", "NV_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NV", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.03725d), getCurrentRate("NV SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0005d), getCurrentRate("SUP-NV CEP", psid, year, quarter));
    }

    @Test
    public void NY_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NY", psid, "987654321", "06502929");

        // Create the request.
        generateAdeRequest("NY", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NYDOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NY_Round_Trip.json", "NY_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NY", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.03625d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00075d), getCurrentRate("SUP-NY RSF", psid, year, quarter));
    }

    @Test
    public void NY_Qtr4_Round_Trip() {
        int year = 2013;
        int quarter = 4;
        String psid = "199210091";

        createAssistedCompanyWithRates("NY", psid, "987654321", "06502929");

        // Create the request.
        generateAdeRequest("NY", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NYDOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NY_Qtr4_Round_Trip.json", "NY_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NY", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.03625d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00075d), getCurrentRate("SUP-NY RSF", psid, year, quarter));
    }

    @Test
    public void OH_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("OH", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("OH", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("OHJFS", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("OH_Round_Trip.json", "OH_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("OH", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.003d), getCurrentRate("OH SUI-ER", psid, year, quarter));
    }

    @Test
    public void OK_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("OK", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("OK", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("OKESC", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("OK_Round_Trip.json", "OK_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("OK", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.003d), getCurrentRate("OK SUI-ER", psid, year, quarter));
    }

    @Test
    public void OR_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("OR", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("OR", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("ORDOR", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("OR_Round_Trip.json", "OR_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("OR", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.022d), getCurrentRate("OR SUI-ER", psid, year, quarter));
    }

    @Test
    public void PA_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("PA", psid, "987654321", "3110765");

        // Create the request.
        generateAdeRequest("PA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("PADLI", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("PA_Round_Trip.json", "PA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("PA", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.15098d), getCurrentRate("PA SUI-ER", psid, year, quarter));
    }

    @Test
    public void RI_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        // Create the request.
        generateAdeRequest("RI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("RIDOT", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("RI_Round_Trip.json", "RI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("RI", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0979d), getCurrentRate("RI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0021d), getCurrentRate("SUP-RI JDF", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.1000d), getCurrentRate("SUP-RI WBI", psid, year, quarter));
    }

    @Test
    public void RI_Round_Trip_UIRateLessThanThreshold() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        // Create the request.
        generateAdeRequest("RI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("RIDOT", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("RI_Round_Trip_UIRateLessThanThreshold.json", "RI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("RI", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0978d), getCurrentRate("RI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0021d), getCurrentRate("SUP-RI JDF", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-RI WBI", psid, year, quarter));
    }

    @Test
    public void RI_Round_Trip_UIRateEqualsToThreshold() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        // Create the request.
        generateAdeRequest("RI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("RIDOT", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("RI_Round_Trip_UIRateEqualToThreshold.json", "RI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("RI", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0980d), getCurrentRate("RI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0021d), getCurrentRate("SUP-RI JDF", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.1001d), getCurrentRate("SUP-RI WBI", psid, year, quarter));
    }

    @Test
    public void SC_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("SC", psid, "987654321", "0757-930 6");

        // Create the request.
        generateAdeRequest("SC", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("SCESC", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("SC_Round_Trip.json", "SC_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("SC", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.054d), getCurrentRate("SC SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.000505d), getCurrentRate("SUP-SC CAT", psid, year, quarter));
    }

    @Test
    public void TN_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("TN", psid, "987654321", "0757-930 6");

        // Create the request.
        generateAdeRequest("TN", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("TNDOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("TN_Round_Trip.json", "TN_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("TN", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.027d), getCurrentRate("TN SUI-ER", psid, year, quarter));
        //This is inactive now
        //assertEquals("Supplemental SUI Rate", new Double(0.00d), getCurrentRate("SUP-TN JSF", psid, year, quarter));

        // Company request should not have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertNotSame("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void TX_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("TX", psid, "987654321", "12-152331-0");

        // Create the request.
        generateAdeRequest("TX", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("TXWC", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("TX_Round_Trip.json", "TX_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("TX", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0054d), getCurrentRate("TX SUI-ER", psid, year, quarter));
        //This is inactive now
        //assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-TX SJA", psid, year, quarter));

        // Company request should not have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertNotSame("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void TX_Round_Trip_InActiveFilingStatusForSupplementLaw() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        Company company=createAssistedCompanyWithRatesOld("TX", psid, "987654321", "12-152331-0");

        // Create the request.
        generateAdeRequest("TX", year, quarter);
        Application.beginUnitOfWork();
        CompanyLaw companyLaw=CompanyLaw.findCompanyLaw(company,"163");
        companyLaw.setFilingStatus(PayrollItemStatus.Inactive);
        Application.commitUnitOfWork();
        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("TXWC", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("TX_Round_Trip.json", "TX_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("TX", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0054d), getCurrentRate("TX SUI-ER", psid, year, quarter));

        // Company request should not have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertNotSame("Rate Request", RateRequestStatus.Error, request.getStatus());
        //This is inactive now
        assertEquals("Supplemental SUI Rate", null, getCurrentActiveRateWithoutAssert("SUP-TX SJA", psid, year, quarter));
    }

    @Test
    public void UT_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("UT", psid, "987654321", "C5-749288-0");

        // Create the request.
        generateAdeRequest("UT", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("UTDWS", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("UT_Round_Trip.json", "UT_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("UT", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.016d), getCurrentRate("UT SUI-ER", psid, year, quarter));
    }


    @Test
    public void VA_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("VA", psid, "987654321", "0001215233");

        // Create the request.
        generateAdeRequest("VA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("VAEC", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("VA_Round_Trip.json", "VA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("VA", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0068d), getCurrentRate("VA SUI-ER", psid, year, quarter));
    }

    @Test
    public void VT_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("VT", psid, "987654321", "311076-05-1");

        // Create the request.
        generateAdeRequest("VT", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("VTDOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("VT_Round_Trip.json", "VT_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("VT", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.013d), getCurrentRate("VT SUI-ER", psid, year, quarter));
    }

    @Test
    public void WA_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("WA", psid, "987654321", "311076-05-1");

        // Create the request.
        generateAdeRequest("WA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("WAESD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("WA_Round_Trip.json", "WA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("WA", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.0405d), getCurrentRate("WA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0003d), getCurrentRate("SUP-WA WTF", psid, year, quarter));
    }

    @Test
    public void WI_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("WI", psid, "987654321", "311076-05-1");

        // Create the request.
        generateAdeRequest("WI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("WIDWD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("WI_Round_Trip.json", "WI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("WI", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.098d), getCurrentRate("WI SUI-ER", psid, year, quarter));
    }

    @Test
    public void WV_Round_Trip() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("WV", psid, "987654321", "311076-05-1");

        // Create the request.
        generateAdeRequest("WV", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("WVBEP", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("WV_Round_Trip.json", "WV_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("WV", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.05257d), getCurrentRate("WV SUI-ER", psid, year, quarter));
    }

    @Test
    public void AR_Invalid_Zero_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("AR", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("AR", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("ARESD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("AR_Invalid_Rate.json", "AR_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("AR", year, quarter);

        // Base rate and Stabilization rate should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("AR SUI-ER", psid, year, quarter));
        //This is inactive now
        //assertEquals("Stabilization Rate", new Double(0.03d), getCurrentRate("SUP-AR ST", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void CA_Invalid_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("CA", psid, "987654321", "311-0765-1");

        // Create the request.
        generateAdeRequest("CA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("CAEDD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("CA_Invalid_Rate.json", "CA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("CA", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("CA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("ETT", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void FL_Invalid_Min_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("FL", psid, "987654321", "1215304");

        // Create the request.
        generateAdeRequest("FL", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("FLDOR", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("FL_Invalid_Rate.json", "FL_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("FL", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("FL SUI-ER", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void FL_Invalid_Max_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "15984062";

        createAssistedCompanyWithRates("FL", psid, "591551742", "02403680");

        // Create the request.
        generateAdeRequest("FL", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("FLDOR", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("FL_Invalid_Rate.json", "FL_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("FL", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("FL SUI-ER", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void GA_Invalid_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");

        // Create the request.
        generateAdeRequest("GA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("GADOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("GA_Invalid_Rate.json", "GA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("GA", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-GA AA", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void HI_Invalid_Zero_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("HI", psid, "987654321", "0007381875");

        // Create the request.
        generateAdeRequest("HI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("HIDLIR", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("HI_Invalid_Rate.json", "HI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("HI", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("HI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP HI ETA", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void ID_Invalid_Zero_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("ID", psid, "987654321", "4595533-0");

        // Create the request.
        generateAdeRequest("ID", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("IDCL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("ID_Invalid_Rate.json", "ID_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("ID", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("ID SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-ID WDF", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void IL_Invalid_Min_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("IL", psid, "987654321", "4595533-0");

        // Create the request.
        generateAdeRequest("IL", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("ILDES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("IL_Invalid_Rate.json", "IL_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("IL", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("IL SUI-ER", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void IL_Invalid_Max_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "1957324";

        createAssistedCompanyWithRates("IL", psid, "272671887", "4600691-1");

        // Create the request.
        generateAdeRequest("IL", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("ILDES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("IL_Invalid_Rate.json", "IL_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("IL", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("IL SUI-ER", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void MA_Invalid_Zero_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MA", psid, "987654321", "311-0765-1");

        // Create the request.
        generateAdeRequest("MA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MAWUA", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MA_Invalid_Rate.json", "MA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MA", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MA WTF", psid, year, quarter));
        // TODO: TLD - This is a NO-CALC law that has no rates in PSP, but was generated on the AS/400.  Is QB generating automatically?
        // assertEquals("UHI", new Double(0.015d), getCurrentRate("UHI", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }


    @Test
    public void ME_Invalid_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("ME", psid, "987654321", "10-07818-1");

        // Create the request.
        generateAdeRequest("ME", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MEUITD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("ME_Invalid_Rate.json", "ME_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("ME", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("ME SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-ME CSSF", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void MI_Invalid_Min_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MI", psid, "987654321", "10-07818-1");

        // Create the request.
        generateAdeRequest("MI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MIDLEG", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MI_Invalid_Rate.json", "MI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MI", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MI OA", psid, year, quarter));
    }

    @Test
    public void MI_Invalid_Max_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "23238703";

        createAssistedCompanyWithRates("MI", psid, "010654545", "010-664-445 000");

        // Create the request.
        generateAdeRequest("MI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MIDLEG", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MI_Invalid_Rate.json", "MI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MI", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MI OA", psid, year, quarter));
    }

    @Test
    public void MN_Invalid_Zero_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MN", psid, "987654321", "10-07818-1");

        // Create the request.
        generateAdeRequest("MN", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MNEEUI", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MN_Invalid_Rate.json", "MN_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MN", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MN SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MN DWA", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void MS_Invalid_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MS", psid, "987654321", "17 08366 0 00");

        // Create the request.
        generateAdeRequest("MS", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MSDES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MS_Invalid_Rate.json", "MS_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MS", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MS SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MS WTEF", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void MT_Invalid_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("MT", psid, "987654321", "2036142");

        // Create the request.
        generateAdeRequest("MT", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("MTUID", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("MT_Invalid_Rate.json", "MT_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("MT", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MT SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MT AFT", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void NH_Invalid_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NH", psid, "987654321", "3110765 1");

        // Create the request.
        generateAdeRequest("NH", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NHES", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NH_Invalid_Rate.json", "NH_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NH", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NH SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-NH AC", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void NJ_Invalid_Min_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NJ", psid, "987654321", "065029029");

        // Create the request.
        generateAdeRequest("NJ", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NJDLWD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NJ_Invalid_Rate.json", "NJ_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NJ", year, quarter);

        // Base rate should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NJ SUI-ER", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void NJ_Invalid_Max_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "23238703";

        createAssistedCompanyWithRates("NJ", psid, "010654545", "010664445");

        // Create the request.
        generateAdeRequest("NJ", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NJDLWD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NJ_Invalid_Rate.json", "NJ_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NJ", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NJ SUI-ER", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void NY_Invalid_Zero_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("NY", psid, "987654321", "06502929");

        // Create the request.
        generateAdeRequest("NY", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NYDOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NY_Invalid_Rate.json", "NY_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NY", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-NY RSF", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void NY_Invalid_Min_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "2647374";

        createAssistedCompanyWithRates("NY", psid, "134014984", "04327166");

        // Create the request.
        generateAdeRequest("NY", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NYDOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NY_Invalid_Rate.json", "NY_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NY", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-NY RSF", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Applied, request.getStatus());
    }

    @Test
    public void NY_Invalid_Max_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "1691504";

        createAssistedCompanyWithRates("NY", psid, "161843355", "90171230");

        // Create the request.
        generateAdeRequest("NY", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("NYDOL", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("NY_Invalid_Rate.json", "NY_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("NY", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-NY RSF", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void RI_Invalid_Zero_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        // Create the request.
        generateAdeRequest("RI", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("RIDOT", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("RI_Invalid_Rate.json", "RI_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("RI", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("RI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-RI JDF", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-RI WBI", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void WA_Invalid_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "17001609";

        createAssistedCompanyWithRates("WA", psid, "542017446", "655983007");

        // Create the request.
        generateAdeRequest("WA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("WAESD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("WA_Invalid_Rate.json", "WA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("WA", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("WA SUI-ER", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void WA_Invalid_Min_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("WA", psid, "987654321", "311076-05 1");

        // Create the request.
        generateAdeRequest("WA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("WAESD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("WA_Invalid_Rate.json", "WA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("WA", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("WA SUI-ER", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void WA_Invalid_Max_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "23022105";

        createAssistedCompanyWithRates("WA", psid, "912211461", "286341-00 0");

        // Create the request.
        generateAdeRequest("WA", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("WAESD", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("WA_Invalid_Rate.json", "WA_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("WA", year, quarter);

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("WA SUI-ER", psid, year, quarter));

        // Company request should have an error status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.Error, request.getStatus());
    }

    @Test
    public void GA_Apply_Invalid_Qtr() {
        int year = 2013;
        int reqQuarter = 1;
        int applyQuarter = 3;
        String psid = "199210091";

        createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");

        // Create the request.
        generateAdeRequest("GA", year, reqQuarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("GADOL", year, reqQuarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("GA_Round_Trip.json", "GA_" + year + "_" + reqQuarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database for a different quarter.
        try {
            applyAdeRates("GA", year, applyQuarter);
        } catch  (Exception e) {
            // ADE tool should throw an exception indicating the request could not be found
            assertEquals(e.getLocalizedMessage().contains("Unable to find a request"), true);

            // Rates should be unchanged at 3.0%.
            assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("GA SUI-ER", psid, year, reqQuarter));
            assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-GA AA", psid, year, reqQuarter));
        }
    }

    @Test
    public void FL_No_Change() {
        int year = 2013;
        int quarter = 1;
        String psid = "199210091";

        createAssistedCompanyWithRates("FL", psid, "987654321", "1215304");

        // Create the request.
        generateAdeRequest("FL", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("FLDOR", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("FL_No_Change.json", "FL_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("FL", year, quarter);

        // Rate should still be 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("FL SUI-ER", psid, year, quarter));

        // Company request should have a NoChange status.
        CompanyRateRequest request = Application.find(CompanyRateRequest.class).getFirst();
        assertEquals("Rate Request", RateRequestStatus.NoChange, request.getStatus());

    }

    @Test
    public void FL_Rounding_Rate() {
        int year = 2013;
        int quarter = 1;
        String psid = "10654125";

        createAssistedCompanyWithRates("FL", psid, "592732677", "9760382");

        // Create the request.
        generateAdeRequest("FL", year, quarter);

        // Assume the request has been sent and received.
        String agencyRequestId = findAndUpdateAgencyRequest("FLDOR", year, quarter, AgencyRateRequestStatus.ResponseReceived);

        // Copy the test input file to the location where the ADE Tool expects to find it.
        copyFile("FL_Round_Trip.json", "FL_" + year + "_" + quarter + "_" + agencyRequestId + RateApplyProcess.RESPONSE_FILE_EXT);

        // Read the test JSON file and apply the rates to the database.
        applyAdeRates("FL", year, quarter);

        assertEquals("Base SUI Rate", new Double(0.073995d), getCurrentRate("FL SUI-ER", psid, year, quarter));
    }

    @Test
    public void TestLawRateRanges() {
        Application.beginUnitOfWork();

        // If we add range checks for more states, we should add checks here.
        DomainEntitySet<LawRateRange> ranges = Application.find(LawRateRange.class,
                                                                LawRateRange.MinRate().isNotNull()
                                                                    .Or(LawRateRange.MaxRate().isNotNull()));

        assertEquals(57, ranges.size());

        assertTrue(getLawById("85").rateFallsWithinRange(SpcfDecimal.createInstance(.0615)));
        assertFalse(getLawById("85").rateFallsWithinRange(SpcfDecimal.createInstance(.145)));
        assertFalse(getLawById("85").rateFallsWithinRange(SpcfDecimal.createInstance(.0005)));

        assertFalse(getLawById("86").rateFallsWithinRange(SpcfDecimal.createInstance(0.3000)));
        assertTrue(getLawById("86").rateFallsWithinRange(SpcfDecimal.createInstance(0.1110)));
        assertFalse(getLawById("86").rateFallsWithinRange(SpcfDecimal.createInstance(0.0005)));

        assertTrue(getLawById("87").rateFallsWithinRange(SpcfDecimal.createInstance(.0485)));
        assertFalse(getLawById("87").rateFallsWithinRange(SpcfDecimal.createInstance(.083)));
        assertFalse(getLawById("87").rateFallsWithinRange(SpcfDecimal.createInstance(.014)));

        assertFalse(getLawById("88").rateFallsWithinRange(SpcfDecimal.createInstance(-.0002)));
        assertTrue(getLawById("88").rateFallsWithinRange(SpcfDecimal.createInstance(.05300)));
        assertFalse(getLawById("88").rateFallsWithinRange(SpcfDecimal.createInstance(0.10400)));

        assertTrue(getLawById("89").rateFallsWithinRange(SpcfDecimal.createInstance(.0435)));
        assertFalse(getLawById("89").rateFallsWithinRange(SpcfDecimal.createInstance(.069)));
        assertFalse(getLawById("89").rateFallsWithinRange(SpcfDecimal.createInstance(.016)));

        assertTrue(getLawById("90").rateFallsWithinRange(SpcfDecimal.createInstance(.043)));
        assertFalse(getLawById("90").rateFallsWithinRange(SpcfDecimal.createInstance(.076)));
        assertFalse(getLawById("90").rateFallsWithinRange(SpcfDecimal.createInstance(.015)));

        assertTrue(getLawById("92").rateFallsWithinRange(SpcfDecimal.createInstance(.0375)));
        assertFalse(getLawById("92").rateFallsWithinRange(SpcfDecimal.createInstance(.075)));
        assertFalse(getLawById("92").rateFallsWithinRange(SpcfDecimal.createInstance(0)));

        assertTrue(getLawById("93").rateFallsWithinRange(SpcfDecimal.createInstance(.0407)));
        assertFalse(getLawById("93").rateFallsWithinRange(SpcfDecimal.createInstance(.082)));
        assertFalse(getLawById("93").rateFallsWithinRange(SpcfDecimal.createInstance(0.0003)));

        assertTrue(getLawById("94").rateFallsWithinRange(SpcfDecimal.createInstance(.04575)));
        assertFalse(getLawById("94").rateFallsWithinRange(SpcfDecimal.createInstance(.0837)));
        assertFalse(getLawById("94").rateFallsWithinRange(SpcfDecimal.createInstance(-.0100)));

        assertFalse(getLawById("96").rateFallsWithinRange(SpcfDecimal.createInstance(.069)));
        assertTrue(getLawById("96").rateFallsWithinRange(SpcfDecimal.createInstance(.0027381)));
        assertFalse(getLawById("96").rateFallsWithinRange(SpcfDecimal.createInstance(.00132)));

        assertFalse(getLawById("97").rateFallsWithinRange(SpcfDecimal.createInstance(.1300)));
        assertTrue(getLawById("97").rateFallsWithinRange(SpcfDecimal.createInstance(.0300)));
        assertFalse(getLawById("97").rateFallsWithinRange(SpcfDecimal.createInstance(.0040)));

        assertTrue(getLawById("98").rateFallsWithinRange(SpcfDecimal.createInstance(.06431)));
        assertFalse(getLawById("98").rateFallsWithinRange(SpcfDecimal.createInstance(.12462)));
        assertFalse(getLawById("98").rateFallsWithinRange(SpcfDecimal.createInstance(.004)));

        assertTrue(getLawById("99").rateFallsWithinRange(SpcfDecimal.createInstance(.04755)));
        assertFalse(getLawById("99").rateFallsWithinRange(SpcfDecimal.createInstance(.095)));
        assertFalse(getLawById("99").rateFallsWithinRange(SpcfDecimal.createInstance(0.0800)));
        assertTrue(getLawById("99").rateFallsWithinRange(SpcfDecimal.createInstance(0.0030)));

        assertFalse(getLawById("100").rateFallsWithinRange(SpcfDecimal.createInstance(.200)));
        assertTrue(getLawById("100").rateFallsWithinRange(SpcfDecimal.createInstance(.070)));
        assertFalse(getLawById("100").rateFallsWithinRange(SpcfDecimal.createInstance(.002)));

        assertTrue(getLawById("101").rateFallsWithinRange(SpcfDecimal.createInstance(.0315)));
        assertFalse(getLawById("101").rateFallsWithinRange(SpcfDecimal.createInstance(.063)));
        assertFalse(getLawById("101").rateFallsWithinRange(SpcfDecimal.createInstance(0)));

        assertTrue(getLawById("102").rateFallsWithinRange(SpcfDecimal.createInstance(.0833)));
        assertFalse(getLawById("102").rateFallsWithinRange(SpcfDecimal.createInstance(.155)));
        assertFalse(getLawById("102").rateFallsWithinRange(SpcfDecimal.createInstance(.004)));

        assertFalse(getLawById("103").rateFallsWithinRange(SpcfDecimal.createInstance(.150)));
        assertTrue(getLawById("103").rateFallsWithinRange(SpcfDecimal.createInstance(.100)));
        assertFalse(getLawById("103").rateFallsWithinRange(SpcfDecimal.createInstance(.001)));

        assertTrue(getLawById("104").rateFallsWithinRange(SpcfDecimal.createInstance(.04575)));
        assertFalse(getLawById("104").rateFallsWithinRange(SpcfDecimal.createInstance(.0837)));
        assertFalse(getLawById("104").rateFallsWithinRange(SpcfDecimal.createInstance(.003)));

        assertTrue(getLawById("105").rateFallsWithinRange(SpcfDecimal.createInstance(.0768)));
        assertFalse(getLawById("105").rateFallsWithinRange(SpcfDecimal.createInstance(.154)));
        assertFalse(getLawById("105").rateFallsWithinRange(SpcfDecimal.createInstance(0.0003)));
        assertFalse(getLawById("105").rateFallsWithinRange(SpcfDecimal.createInstance(0.145)));

        assertTrue(getLawById("106").rateFallsWithinRange(SpcfDecimal.createInstance(.05608)));
        assertFalse(getLawById("106").rateFallsWithinRange(SpcfDecimal.createInstance(.10816)));
        assertFalse(getLawById("106").rateFallsWithinRange(SpcfDecimal.createInstance(.0009)));

        assertTrue(getLawById("107").rateFallsWithinRange(SpcfDecimal.createInstance(.06825)));
        assertFalse(getLawById("107").rateFallsWithinRange(SpcfDecimal.createInstance(.1375)));

        assertTrue(getLawById("108").rateFallsWithinRange(SpcfDecimal.createInstance(.031)));
        assertFalse(getLawById("108").rateFallsWithinRange(SpcfDecimal.createInstance(.0565)));
        assertFalse(getLawById("108").rateFallsWithinRange(SpcfDecimal.createInstance(.0650)));

        assertTrue(getLawById("109").rateFallsWithinRange(SpcfDecimal.createInstance(.049)));
        assertFalse(getLawById("109").rateFallsWithinRange(SpcfDecimal.createInstance(.0928)));
        assertTrue(getLawById("109").rateFallsWithinRange(SpcfDecimal.createInstance(.0000)));

        assertTrue(getLawById("110").rateFallsWithinRange(SpcfDecimal.createInstance(.0342)));
        assertFalse(getLawById("110").rateFallsWithinRange(SpcfDecimal.createInstance(.0694)));

        assertTrue(getLawById("111").rateFallsWithinRange(SpcfDecimal.createInstance(.030)));
        assertFalse(getLawById("111").rateFallsWithinRange(SpcfDecimal.createInstance(.1288)));
        assertFalse(getLawById("111").rateFallsWithinRange(SpcfDecimal.createInstance(.0005)));

        assertTrue(getLawById("112").rateFallsWithinRange(SpcfDecimal.createInstance(.03245)));
        assertFalse(getLawById("112").rateFallsWithinRange(SpcfDecimal.createInstance(.0659)));

        assertTrue(getLawById("113").rateFallsWithinRange(SpcfDecimal.createInstance(.056)));
        assertFalse(getLawById("113").rateFallsWithinRange(SpcfDecimal.createInstance(.111)));

        assertTrue(getLawById("114").rateFallsWithinRange(SpcfDecimal.createInstance(.0375)));
        assertFalse(getLawById("114").rateFallsWithinRange(SpcfDecimal.createInstance(.074825)));
        assertFalse(getLawById("114").rateFallsWithinRange(SpcfDecimal.createInstance(.000175)));

        assertTrue(getLawById("115").rateFallsWithinRange(SpcfDecimal.createInstance(.0275)));
        assertFalse(getLawById("115").rateFallsWithinRange(SpcfDecimal.createInstance(.0025)));
        assertFalse(getLawById("115").rateFallsWithinRange(SpcfDecimal.createInstance(0)));

        assertTrue(getLawById("116").rateFallsWithinRange(SpcfDecimal.createInstance(.02825)));
        assertFalse(getLawById("116").rateFallsWithinRange(SpcfDecimal.createInstance(.055)));
        assertFalse(getLawById("116").rateFallsWithinRange(SpcfDecimal.createInstance(.0015)));

        assertFalse(getLawById("117").rateFallsWithinRange(SpcfDecimal.createInstance(.1000)));
        assertTrue(getLawById("117").rateFallsWithinRange(SpcfDecimal.createInstance(.06725)));
        assertFalse(getLawById("117").rateFallsWithinRange(SpcfDecimal.createInstance(.00520)));

        assertTrue(getLawById("118").rateFallsWithinRange(SpcfDecimal.createInstance(.054)));
        assertTrue(getLawById("118").rateFallsWithinRange(SpcfDecimal.createInstance(.109)));
        assertFalse(getLawById("118").rateFallsWithinRange(SpcfDecimal.createInstance(.002)));
        assertFalse(getLawById("118").rateFallsWithinRange(SpcfDecimal.createInstance(.14600)));

        assertTrue(getLawById("119").rateFallsWithinRange(SpcfDecimal.createInstance(.0475)));
        assertFalse(getLawById("119").rateFallsWithinRange(SpcfDecimal.createInstance(.093)));
        assertFalse(getLawById("119").rateFallsWithinRange(SpcfDecimal.createInstance(.0001)));

        assertTrue(getLawById("120").rateFallsWithinRange(SpcfDecimal.createInstance(.038)));
        assertFalse(getLawById("120").rateFallsWithinRange(SpcfDecimal.createInstance(.055)));
        assertTrue(getLawById("120").rateFallsWithinRange(SpcfDecimal.createInstance(.017)));
        assertTrue(getLawById("120").rateFallsWithinRange(SpcfDecimal.createInstance(.013)));
        assertFalse(getLawById("120").rateFallsWithinRange(SpcfDecimal.createInstance(.0009)));

        assertTrue(getLawById("121").rateFallsWithinRange(SpcfDecimal.createInstance(.0834685)));
        assertTrue(getLawById("121").rateFallsWithinRange(SpcfDecimal.createInstance(.120333)));
        assertFalse(getLawById("121").rateFallsWithinRange(SpcfDecimal.createInstance(.011)));
        assertFalse(getLawById("121").rateFallsWithinRange(SpcfDecimal.createInstance(.180978)));
        assertTrue(getLawById("121").rateFallsWithinRange(SpcfDecimal.createInstance(.120333)));

        assertTrue(getLawById("123").rateFallsWithinRange(SpcfDecimal.createInstance(.06695)));
        assertFalse(getLawById("123").rateFallsWithinRange(SpcfDecimal.createInstance(.1189)));
        assertFalse(getLawById("123").rateFallsWithinRange(SpcfDecimal.createInstance(.0050)));

        assertTrue(getLawById("124").rateFallsWithinRange(SpcfDecimal.createInstance(.03675)));
        assertFalse(getLawById("124").rateFallsWithinRange(SpcfDecimal.createInstance(.0795)));
        assertFalse(getLawById("124").rateFallsWithinRange(SpcfDecimal.createInstance(.0610)));


        assertTrue(getLawById("125").rateFallsWithinRange(SpcfDecimal.createInstance(.049)));
        assertFalse(getLawById("125").rateFallsWithinRange(SpcfDecimal.createInstance(.0968)));

        assertTrue(getLawById("126").rateFallsWithinRange(SpcfDecimal.createInstance(.058)));
        assertFalse(getLawById("126").rateFallsWithinRange(SpcfDecimal.createInstance(.107)));
        assertFalse(getLawById("126").rateFallsWithinRange(SpcfDecimal.createInstance(.00003)));

        assertTrue(getLawById("127").rateFallsWithinRange(SpcfDecimal.createInstance(.03945)));
        assertFalse(getLawById("127").rateFallsWithinRange(SpcfDecimal.createInstance(.0830)));
        assertTrue(getLawById("127").rateFallsWithinRange(SpcfDecimal.createInstance(.0008)));
        assertTrue(getLawById("127").rateFallsWithinRange(SpcfDecimal.createInstance(.0032)));

        assertTrue(getLawById("128").rateFallsWithinRange(SpcfDecimal.createInstance(.0445)));
        assertFalse(getLawById("128").rateFallsWithinRange(SpcfDecimal.createInstance(.086)));
        assertTrue(getLawById("128").rateFallsWithinRange(SpcfDecimal.createInstance(.002)));

        assertTrue(getLawById("129").rateFallsWithinRange(SpcfDecimal.createInstance(.0383)));
        assertFalse(getLawById("129").rateFallsWithinRange(SpcfDecimal.createInstance(.0688)));
        assertFalse(getLawById("129").rateFallsWithinRange(SpcfDecimal.createInstance(.00)));

        assertTrue(getLawById("130").rateFallsWithinRange(SpcfDecimal.createInstance(.047)));
        assertTrue(getLawById("130").rateFallsWithinRange(SpcfDecimal.createInstance(.048)));
        assertFalse(getLawById("130").rateFallsWithinRange(SpcfDecimal.createInstance(.08900)));
        assertFalse(getLawById("130").rateFallsWithinRange(SpcfDecimal.createInstance(.002)));
        assertTrue(getLawById("130").rateFallsWithinRange(SpcfDecimal.createInstance(.04500)));

        assertFalse(getLawById("131").rateFallsWithinRange(SpcfDecimal.createInstance(.1034)));
        assertTrue(getLawById("131").rateFallsWithinRange(SpcfDecimal.createInstance(.1033)));
        assertFalse(getLawById("131").rateFallsWithinRange(SpcfDecimal.createInstance(-.0002)));

        assertTrue(getLawById("132").rateFallsWithinRange(SpcfDecimal.createInstance(.05035)));
        assertFalse(getLawById("132").rateFallsWithinRange(SpcfDecimal.createInstance(.129)));
        assertFalse(getLawById("132").rateFallsWithinRange(SpcfDecimal.createInstance(-.0003)));

        assertTrue(getLawById("133").rateFallsWithinRange(SpcfDecimal.createInstance(.05)));
        assertFalse(getLawById("133").rateFallsWithinRange(SpcfDecimal.createInstance(.086)));
        assertFalse(getLawById("133").rateFallsWithinRange(SpcfDecimal.createInstance(.014)));

        assertFalse(getLawById("146").rateFallsWithinRange(SpcfDecimal.createInstance(.0006)));
        assertTrue(getLawById("146").rateFallsWithinRange(SpcfDecimal.createInstance(.0002)));

        assertTrue(getLawById("147").rateFallsWithinRange(SpcfDecimal.createInstance(.0013)));
        assertFalse(getLawById("147").rateFallsWithinRange(SpcfDecimal.createInstance(.0028)));
        assertFalse(getLawById("147").rateFallsWithinRange(SpcfDecimal.createInstance(0.0006)));

        assertTrue(getLawById("153").rateFallsWithinRange(SpcfDecimal.createInstance(0.0000)));
        assertFalse(getLawById("153").rateFallsWithinRange(SpcfDecimal.createInstance(.0326)));
        assertFalse(getLawById("153").rateFallsWithinRange(SpcfDecimal.createInstance(.0014)));

        assertTrue(getLawById("161").rateFallsWithinRange(SpcfDecimal.createInstance(.00049)));
        assertFalse(getLawById("161").rateFallsWithinRange(SpcfDecimal.createInstance(.0298)));
        assertTrue(getLawById("161").rateFallsWithinRange(SpcfDecimal.createInstance(.0000)));

        assertTrue(getLawById("162").rateFallsWithinRange(SpcfDecimal.createInstance(.0049)));
        assertFalse(getLawById("162").rateFallsWithinRange(SpcfDecimal.createInstance(.00928)));

        assertTrue(getLawById("185").rateFallsWithinRange(SpcfDecimal.createInstance(.001)));
        assertFalse(getLawById("185").rateFallsWithinRange(SpcfDecimal.createInstance(.005)));

        assertTrue(getLawById("194").rateFallsWithinRange(SpcfDecimal.createInstance(.0011)));
        assertFalse(getLawById("194").rateFallsWithinRange(SpcfDecimal.createInstance(.2125)));

        assertTrue(getLawById("201").rateFallsWithinRange(SpcfDecimal.createInstance(.0000)));
        assertFalse(getLawById("201").rateFallsWithinRange(SpcfDecimal.createInstance(-.0200)));
        assertFalse(getLawById("201").rateFallsWithinRange(SpcfDecimal.createInstance(-.0367)));

        assertTrue(getLawById("220").rateFallsWithinRange(SpcfDecimal.createInstance(.01600)));
        assertFalse(getLawById("220").rateFallsWithinRange(SpcfDecimal.createInstance(-.2528)));

        Application.rollbackUnitOfWork();
    }

    @Test
    public void TestLawRateValues() {
        Application.beginUnitOfWork();

        // If we add value checks for more states, we should add checks here.
        DomainEntitySet<LawRateValue> values = Application.find(LawRateValue.class,
                                                                LawRateValue.Rate().isNotNull());
        assertEquals(20, values.size());

        assertTrue(getLawById("145").rateIsValidValue(SpcfDecimal.createInstance(.008)));
        assertFalse(getLawById("145").rateIsValidValue(SpcfDecimal.createInstance(.0081)));

        assertTrue(getLawById("150").rateIsValidValue(SpcfDecimal.createInstance(.00056)));
        assertFalse(getLawById("150").rateIsValidValue(SpcfDecimal.createInstance(.0007)));

        assertTrue(getLawById("152").rateIsValidValue(SpcfDecimal.createInstance(.00075)));
        assertFalse(getLawById("152").rateIsValidValue(SpcfDecimal.createInstance(.00085)));

        assertTrue(getLawById("154").rateIsValidValue(SpcfDecimal.createInstance(0)));
        assertTrue(getLawById("154").rateIsValidValue(SpcfDecimal.createInstance(.0006)));
        assertFalse(getLawById("154").rateIsValidValue(SpcfDecimal.createInstance(.0009)));

        assertTrue(getLawById("155").rateIsValidValue(SpcfDecimal.createInstance(0)));
        assertTrue(getLawById("155").rateIsValidValue(SpcfDecimal.createInstance(.0001)));
        assertFalse(getLawById("155").rateIsValidValue(SpcfDecimal.createInstance(.0002)));

        assertTrue(getLawById("158").rateIsValidValue(SpcfDecimal.createInstance(.001)));
        assertFalse(getLawById("158").rateIsValidValue(SpcfDecimal.createInstance(.0011)));

        assertTrue(getLawById("159").rateIsValidValue(SpcfDecimal.createInstance(0)));
        assertTrue(getLawById("159").rateIsValidValue(SpcfDecimal.createInstance(.0005)));
        assertFalse(getLawById("159").rateIsValidValue(SpcfDecimal.createInstance(.0006)));

        assertTrue(getLawById("160").rateIsValidValue(SpcfDecimal.createInstance(.0021)));
        assertFalse(getLawById("160").rateIsValidValue(SpcfDecimal.createInstance(.0052)));

        assertTrue(getLawById("164").rateIsValidValue(SpcfDecimal.createInstance(.0002)));
        assertTrue(getLawById("164").rateIsValidValue(SpcfDecimal.createInstance(.0003)));
        assertFalse(getLawById("164").rateIsValidValue(SpcfDecimal.createInstance(.0004)));

        assertTrue(getLawById("167").rateIsValidValue(SpcfDecimal.createInstance(.00118)));
        assertFalse(getLawById("167").rateIsValidValue(SpcfDecimal.createInstance(.00128)));

        assertTrue(getLawById("179").rateIsValidValue(SpcfDecimal.createInstance(0)));
        assertTrue(getLawById("179").rateIsValidValue(SpcfDecimal.createInstance(.001)));
        assertFalse(getLawById("179").rateIsValidValue(SpcfDecimal.createInstance(.0001)));

        assertTrue(getLawById("189").rateIsValidValue(SpcfDecimal.createInstance(.0007)));
        assertFalse(getLawById("189").rateIsValidValue(SpcfDecimal.createInstance(.0006)));

        assertTrue(getLawById("219").rateIsValidValue(SpcfDecimal.createInstance(.0015)));
        assertFalse(getLawById("219").rateIsValidValue(SpcfDecimal.createInstance(.0007)));

        Application.rollbackUnitOfWork();
    }

    private static Company createAssistedCompanyWithRatesOld(String state, String psid, String ein, String aid) {

        //  Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds(state);
        DataLoadServices.addCompanyLawsWithAgencyId(aid, company, state, lawIds);
        DataLoadServices.addCompanyLawRates(company);
        return company;
    }

    public static Company createAssistedCompanyWithRates(String state, String psid, String ein, String aid) {

        // Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds(state);
        lawIds.removeAll(Arrays.asList(RateConverterFactory.INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE));  //remove all inactive laws for SUI rate exchange
        DataLoadServices.addCompanyLawsWithAgencyId(aid, company, state, lawIds);
        DataLoadServices.addCompanyLawRates(company);
        return company;
    }


    private Law getLawById(String lawId) {
        return Application.findById(Law.class, lawId);
    }

    private static void generateAdeRequest( String state, int year, int quarter ) {
        ADETool ade = new ADETool(state, year, quarter);
        ade.generateAdeRequest();
    }

    private static void applyAdeRates( String state, int year, int quarter, String ...agencyNames) {
        // Read the test JSON file and apply the rates to the database.
        ADETool ade = new ADETool(state, year, quarter);
        ade.applyAdeRates(agencyNames);
    }

    private static void copyFile( String fromFile, String toFile ) {
        try {
            FileInputStream is = new FileInputStream(TEST_FILE_PATH + fromFile);
            ADERateUtils.verifyDirectory(DIR_ROOT + RateApplyProcess.RESPONSE_FOLDER);
            FileOutputStream os = new FileOutputStream(DIR_ROOT + RateApplyProcess.RESPONSE_FOLDER + toFile);
            FileUtils.copyInputStream(is, os);
        } catch (FileNotFoundException fnf) {
            fail("File not found - " + fromFile);
        } catch (Throwable ex) {
            fail("Error during file copy.");
        }
    }

    private static String findAndUpdateAgencyRequest(String agencyName, int year, int quarter, AgencyRateRequestStatus status) {
        PayrollServices.beginUnitOfWork();
        AgencyRateRequest request = getAgencyRequest(agencyName, year, quarter);
        request.setStatus(status);
        PayrollServices.commitUnitOfWork();

        return request.getId().toString();
    }

    private static Double getCurrentRate( String lawTypeCd, String psid, int year, int quarter ) {
        SpcfCalendar firstDayOfQuarter = CalendarUtils.getFirstDayOfQuarter(year, quarter);
        DomainEntitySet<CompanyLawRate> clrs = Application.find(CompanyLawRate.class,
                                                                CompanyLawRate.CompanyLaw().Law().LawTypeCd().equalTo(lawTypeCd)
                                                                              .And(CompanyLawRate.CompanyLaw().CompanyAgency().Company().SourceCompanyId().equalTo(psid))
                                                                              .And(CompanyLawRate.InvalidDate().isNull())
                                                                              .And(CompanyLawRate.EffectiveDate().greaterOrEqualThan(firstDayOfQuarter)));
        assertEquals("Number of rates for company/law", 1, clrs.size());
        return clrs.getFirst().getRate();
    }
    private static Double getCurrentActiveRateWithoutAssert( String lawTypeCd, String psid, int year, int quarter ) {
        SpcfCalendar firstDayOfQuarter = CalendarUtils.getFirstDayOfQuarter(year, quarter);
        DomainEntitySet<CompanyLawRate> clrs = Application.find(CompanyLawRate.class,
                                                                CompanyLawRate.CompanyLaw().Law().LawTypeCd().equalTo(lawTypeCd)
                                                                              .And(CompanyLawRate.CompanyLaw().CompanyAgency().Company().SourceCompanyId().equalTo(psid))
                                                                              .And(CompanyLawRate.InvalidDate().isNull())
                                                                              .And(CompanyLawRate.CompanyLaw().FilingStatus().equalTo(PayrollItemStatus.Active))
                                                                              .And(CompanyLawRate.EffectiveDate().greaterOrEqualThan(firstDayOfQuarter)));
        if(clrs != null  && clrs.getFirst() !=null ){
            return clrs.getFirst().getRate();
        }
        return null;
    }
    private static Double getCurrentAdditinalFilingRate( String taxItem, String psid, int year, int quarter ) {
        SpcfCalendar firstDayOfQuarter = CalendarUtils.getFirstDayOfQuarter(year, quarter);
        DomainEntitySet<CompanyFilingAmount> clfs = Application.find(CompanyFilingAmount.class,
                                                                     CompanyFilingAmount.Name().equalTo(taxItem)
                                                                                        .And(CompanyFilingAmount.Name().equalTo(taxItem))
                                                                                        .And(CompanyFilingAmount.EffectiveDate().equalTo(firstDayOfQuarter))
                                                                                        .And(CompanyFilingAmount.InvalidDate().isNull()));
        if (clfs == null || clfs.size() == 0) {
            return null;
        }
        return clfs.getFirst().getAmount();

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

    private static AgencyRateRequest getAgencyRequest( String agencyId, int year, int quarter ) {
        DomainEntitySet<AgencyRateRequest> requests = Application.find(AgencyRateRequest.class,
                    AgencyRateRequest.Agency().AgencyId().equalTo(agencyId)
                        .And(AgencyRateRequest.YearQuarter().equalTo("" + year + quarter))
                        .And(AgencyRateRequest.Status().equalTo(AgencyRateRequestStatus.RequestGenerated)));

        assertEquals(requests.size(), 1);
        return requests.getFirst();
    }

    private static AgencyRateRequest getAgencyRequestAllStatuses(String agencyId, int year, int quarter) {
        DomainEntitySet<AgencyRateRequest> requests = Application.find(AgencyRateRequest.class,
                                                                       AgencyRateRequest.Agency().AgencyId().equalTo(agencyId)
                                                                                        .And(AgencyRateRequest.YearQuarter().equalTo("" + year + quarter))
        );

        assertEquals(requests.size(), 1);
        return requests.getFirst();
    }

}
