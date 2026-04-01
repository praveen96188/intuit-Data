package com.intuit.sbd.payroll.psp.batchjobs.enrollments;

import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyLawDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TaxServiceInfoDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.raf.RAFProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileInputStream;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * User: dweinberg
 * Date: 4/24/13
 * Time: 4:33 PM
 */
public class EnrollmentDeleteSelectionTests {

    private final static SpcfLogger logger = Application.getLogger(EnrollmentDeleteSelectionTests.class);
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("FL-UCT6-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));


    }


    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testDeleteFLActive() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        runSelection();
        assertFLDeleted(company, false);

        DataLoadServices.enrollACH(company);

        runSelection();
        assertFLDeleted(company, false);

        updateFLFilingFlag(company, PayrollItemStatus.Inactive);
        runSelection();
        assertFLDeleted(company, true);

        runSelection();
        assertFLDeleted(company, true);

        cancelDelete(company);
        assertFLDeleted(company, false);

        updateFLFilingFlag(company, PayrollItemStatus.Active);
        runSelection();
        assertFLDeleted(company, false);

        updateFLFilingFlag(company, PayrollItemStatus.Inactive);
        runSelection();
        assertFLDeleted(company, true);
    }

    @Test
    public void testDeleteFLCancelled() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.enrollACH(company);

        DataLoadServices.cancelService(company, ServiceCode.Tax);

        runSelection();
        assertFLDeleted(company, false);

        updateLastTaxQuarter(company, 20121);

        DataLoadServices.setPSPDate(2012, 4, 2);

        runSelection();
        assertFLDeleted(company, true);

    }

    @Test
    public void testDeleteFLCancelledDoNotFile() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.enrollACH(company);

        DataLoadServices.cancelService(company, ServiceCode.Tax);

        runSelection();
        assertFLDeleted(company, false);

        updateLastTaxQuarter(company, TaxCompanyServiceInfo.LAST_TAX_QUARTER_DO_NOT_FILE);

        runSelection();
        assertFLDeleted(company, true);

    }

    @Test
    public void testDeleteFLPendingPayments() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        runSelection();
        assertFLDeleted(company, false);

        DataLoadServices.enrollACH(company);

        runSelection();
        assertFLDeleted(company, false);

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2012, 1, 10));

        updateFLFilingFlag(company, PayrollItemStatus.Inactive);
        runSelection();
        assertFLDeleted(company, false);

        DataLoadServices.setPSPDate(2012, 4, 26);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI));

        updateFLFilingFlag(company, PayrollItemStatus.Inactive);
        runSelection();
        assertFLDeleted(company, true);
    }

    @Test
    public void testDeleteRAF() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        enrollRAF(company);

        runSelection();
        assertRAFDeleted(company, false);

        DataLoadServices.cancelService(company, ServiceCode.Tax);

        runSelection();
        assertRAFDeleted(company, false);

        updateLastTaxQuarter(company, 20121);

        DataLoadServices.setPSPDate(2012, 4, 2);

        runSelection();
        assertRAFDeleted(company, true);
    }

    @Test
    public void testDeleteRAFIfDoNotFileIsSelected() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        enrollRAF(company);

        runSelection();
        assertRAFDeleted(company, false);

        DataLoadServices.cancelService(company, ServiceCode.Tax);

        runSelection();
        assertRAFDeleted(company, false);

        updateLastTaxQuarter(company, TaxCompanyServiceInfo.LAST_TAX_QUARTER_DO_NOT_FILE);

        runSelection();
        assertRAFDeleted(company, true);
    }

    @Test
    public void testDeleteRAFPendingPayments() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        enrollRAF(company);

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2012, 1, 10));

        DataLoadServices.setPSPDate(2012, 1, 12);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.cancelService(company, ServiceCode.Tax);
        updateLastTaxQuarter(company, 20121);

        DataLoadServices.setPSPDate(2012, 4, 1);

        runSelection();
        assertRAFDeleted(company, false);

        DataLoadServices.setPSPDate(2012, 4, 27);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());

        runSelection();
        assertRAFDeleted(company, true);
    }

    private void assertFLDeleted(Company company, boolean deleted) {
        Application.beginUnitOfWork();
        Application.refresh(company);
        ACHEnrollment currentACHEnrollment = company.getCurrentACHEnrollment();
        assertEquals(deleted, currentACHEnrollment.getStatus() == ACHEnrollmentStatus.PendingDelete);

        //make sure there are no additional deletes or anything
        for (ACHEnrollment achEnrollment : company.getAllACHEnrollmentsIncludingCancelled()) {
            if (achEnrollment != currentACHEnrollment) {
                assertTrue(achEnrollment.getStatus().in(ACHEnrollmentStatus.Cancelled, ACHEnrollmentStatus.Enrolled));
            }
        }

        Application.rollbackUnitOfWork();
    }

    private void assertRAFDeleted(Company company, boolean deleted) {
        Application.beginUnitOfWork();
        Application.refresh(company);
        RAFEnrollment currentRAFEnrollment = company.getCurrentRAFEnrollment();
        assertEquals(deleted, currentRAFEnrollment.getStatus() == RAFEnrollmentStatus.PendingDeleteTape);

        //make sure there are no additional deletes or anything
        for (RAFEnrollment rafEnrollment : company.getAllRAFEnrollments()) {
            if (rafEnrollment != currentRAFEnrollment) {
                assertTrue(rafEnrollment.getStatus().in(RAFEnrollmentStatus.Cancelled, RAFEnrollmentStatus.Enrolled));
            }
        }

        Application.rollbackUnitOfWork();
    }

    private void updateFLFilingFlag(Company company, PayrollItemStatus flag) {
        Application.beginUnitOfWork();
        Application.refresh(company);

        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(CompanyLaw.findCompanyLaw(company, "92"));
        companyLawDTO.setFilingStatus(flag);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));

        Application.commitUnitOfWork();
    }

    private void runSelection() {
        PayrollServices.beginUnitOfWork();
        new EnrollmentDeleteSelection().selectACHEnrollmentsForDelete();
        new EnrollmentDeleteSelection().selectRAFEnrollmentsForDelete();
        PayrollServices.commitUnitOfWork();
    }

    private void cancelDelete(Company company) {
        Application.beginUnitOfWork();
        Application.refresh(company);
        assertSuccess(PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.Cancelled));
        Application.commitUnitOfWork();
    }

    private void updateLastTaxQuarter(Company company, int lastTaxQuarter) {
        Application.beginUnitOfWork();
        Application.refresh(company);
        TaxServiceInfoDTO taxServiceInfoDTO = (TaxServiceInfoDTO) PayrollServices.dtoFactory.create(company.getService(ServiceCode.Tax));
        taxServiceInfoDTO.setLastQuarterToFile(lastTaxQuarter);
        assertSuccess(PayrollServices.companyManager.updateService(company.getSourceSystemCd(), company.getSourceCompanyId(), taxServiceInfoDTO));
        Application.commitUnitOfWork();
    }

    private void enrollRAF(Company company) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertSuccess(PayrollServices.companyManager.updateRAFEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(), RAFEnrollmentStatus.PendingEnrollmentTape));
        assertSuccess(PayrollServices.companyManager.updateRAFEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(), RAFEnrollmentStatus.PendingEnrollmentResponse));
        assertSuccess(PayrollServices.companyManager.updateRAFEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(), RAFEnrollmentStatus.Enrolled));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testMonthlyEnrollmentReport() {

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2012, 1, 2);
        Company company1 = DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();

        Application.beginUnitOfWork();
        Application.refresh(company2);
        company2.setLegalName("Company Name, with comma");
        Application.commitUnitOfWork();

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company1);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company2);

        PayrollServices.beginUnitOfWork();
        try {
            new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }catch (Throwable t){
            logger.error("Error in writing monthly enrollment report",t);
        }
        PayrollServices.commitUnitOfWork();

        String temp_Dir = BatchUtils.getConfigString("psp_batch_temp", "");
        String expected_Dir = "PSE/batch-jobs-tests/src/test/resources/monthlyenrollmentreport/";
        String filename = String.format("Monthly_Enrollment_Report for %s", PSPDate.getPSPTime().format("MMM yyyy"));

        File actual_file = new File(temp_Dir, filename + ".csv");

        assertNotNull(actual_file);
        if(StreamUtil.isFileIDPSEncrypted(actual_file)){
           // String fileWithoutExt = actual_file.
            try {
                String fileWithoutExt = StreamUtil.FileWithoutExt(actual_file.getAbsolutePath());
                String fileName = fileWithoutExt + "." + "decrpt";
                File testDecFile = new File(fileName);
                Key key = IDPSFileStreamManager.newKeyHandleLatest();
                StreamUtil.streamDecryptFileSingleThread(key, actual_file, testDecFile);
                assertEquals(417, testDecFile.length());
                testDecFile.delete();
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        else {
            assertEquals(417, actual_file.length());  // This is the size of the file with only the headers and the data for newly added laws as part of companySetup process
        }
        logger.info("Test for file with only company setup data (only company laws) passed");
        //Test for file with only company setup data (only company laws) passed

        DataLoadServices.cancelService(company1, ServiceCode.Tax);
        DataLoadServices.cancelService(company2, ServiceCode.Cloud);
        DataLoadServices.cancelService(company2, ServiceCode.Tax);

        DataLoadServices.addCompanyLaws(company1, "40");

        PayrollServices.beginUnitOfWork();
        try {
            new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }catch (Throwable t){
            logger.error("Error in writing monthly enrollment report",t);
        }
        PayrollServices.commitUnitOfWork();

        File expected_file = new File(expected_Dir, "test_ServiceCancellation_Active_PaymentTemplates.csv");
        actual_file = new File(temp_Dir, filename + ".csv");

        assertNotNull(actual_file);
        assertNotNull(expected_file);
        if(StreamUtil.isFileIDPSEncrypted(actual_file)){

            try {
                String fileWithoutExt = StreamUtil.FileWithoutExt(actual_file.getAbsolutePath());
                String fileName = fileWithoutExt + "." + "decrpt";
                File testDecFile = new File(fileName);
                Key key = IDPSFileStreamManager.newKeyHandleLatest();
                IDPSFileReader reader = new IDPSFileReader(actual_file,key);
                BufferedReader actualFileData = new BufferedReader(reader);
                String data = actualFileData.readLine();
                BufferedWriter writer = new BufferedWriter(new FileWriter(testDecFile));
                PrintWriter out = new PrintWriter(writer);

                while(data!= null){
                    out.println(data);
                    data = actualFileData.readLine();
                }
                writer.close();
                assertEquals(expected_file.length(), testDecFile.length());
                testDecFile.delete();
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        else {
            assertEquals(expected_file.length(), actual_file.length());
        }
        assertCompareFiles(expected_file, actual_file);

        //Test for Service Cancelled companies with active payment templates passed
        logger.info("Test for Service Cancelled companies with active payment templates passed");

        Company company3 = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company3);

        Company company4 = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addFederalAndMEStateTaxCompanyLaws(company4);
        DataLoadServices.addCompanyLaws(company4,"189");

        DataLoadServices.updateCompanyLawFilingFlag(company3, "61");        //IRS
        DataLoadServices.updateCompanyLawFilingFlag(company3, "92");        //FL-UCT6

        /*A payment template is Inactive, if all the lawids using it have filingStatus as Inactive in CompanyLaw.
        Here, both the lawIds referring to the payment template CA-UIETT-PAYMENT are being turned Inactive*/
        DataLoadServices.updateCompanyLawFilingFlag(company3, "87");        //CA-UIETT
        DataLoadServices.updateCompanyLawFilingFlag(company3, "142");        //CA-UIETT

        /* for payment template ME-941C1ME-PAYMENT, only company law filingStatus for LawId 104 is turned Inactive (where as 189 is still active),
         so the payment template still remains Active                                                     */
        DataLoadServices.updateCompanyLawFilingFlag(company4, "104");
        DataLoadServices.updateCompanyLawFilingFlag(company4, "23");


        PayrollServices.beginUnitOfWork();
        try {
            new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }catch (Throwable t){
            logger.error("Error in writing monthly enrollment report",t);
        }
        PayrollServices.commitUnitOfWork();

        expected_file =new File(expected_Dir, "test_ActiveCompany_Inactive_PaymentTemplates.csv");
        actual_file = new File(temp_Dir, filename + ".csv");

        assertNotNull(actual_file);
        assertCompareFiles(expected_file, actual_file);
        //Test for Active companies with Inactive payment templates passed
        logger.info("Test for Active companies with Inactive payment templates passed");

        //Adding new CompanyLaws
        logger.info("Adding New Laws");
        DataLoadServices.setPSPDate(2012, 2, 15);
        DataLoadServices.addCompanyLaws(company1, "6");
        DataLoadServices.setPSPDate(2012, 2, 17);
        DataLoadServices.addCompanyLaws(company2, "6");

        PayrollServices.beginUnitOfWork();
        try {
            new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }catch (Throwable t){
            logger.error("Error in writing monthly enrollment report",t);
        }
        PayrollServices.commitUnitOfWork();

        filename = String.format("Monthly_Enrollment_Report for %s", PSPDate.getPSPTime().format("MMM yyyy"));
        expected_file =new File(expected_Dir, "test_Newly_Added_Laws_to_Company.csv");
        actual_file = new File(temp_Dir, filename + ".csv");

        assertNotNull(actual_file);
        assertCompareFiles(expected_file, actual_file);
        //Test for Newly Added Laws to a Company passed
        logger.info("Test for Newly Added Laws to a Company passed");
    }


    @Test
    public void testDeleteFLActiveDGDeletedCompany() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.enrollACH(company);
        updateFLFilingFlag(company, PayrollItemStatus.Inactive);

        //Mark the company as DGDeleted
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        Application.commitUnitOfWork();

        runSelection();
        assertFLDeleted(company, false);

    }

    @Test
    public void testDeleteRAFDGDeleteCompany() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        enrollRAF(company);
        DataLoadServices.cancelService(company, ServiceCode.Tax);
        updateLastTaxQuarter(company, 20121);
        DataLoadServices.setPSPDate(2012, 4, 2);
        assertRAFDeleted(company, false);

        //Mark the company as DGDeleted
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        Application.commitUnitOfWork();

        runSelection();
        assertRAFDeleted(company, false);

    }

    @Test
    public void testMonthlyEnrollmentReportWithDGDeletedCompanies() {
        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2012, 1, 2);
        Company company1 = DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();

        Application.beginUnitOfWork();
        Application.refresh(company2);
        company2.setLegalName("Company Name, with comma");
        Application.commitUnitOfWork();

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company1);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company2);

        Application.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company1, "142");
        companyLaw.setFilingStatus(PayrollItemStatus.Inactive);
        Application.save(companyLaw);
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        Application.refresh(companyLaw);
        companyLaw.setFilingStatus(PayrollItemStatus.Active);
        Application.save(companyLaw);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        try {
            new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Throwable t) {
            logger.error("Error in writing monthly enrollment report", t);
        }
        PayrollServices.commitUnitOfWork();

        String temp_Dir = BatchUtils.getConfigString("psp_batch_temp", "");
        String expected_Dir = "PSE/batch-jobs-tests/src/test/resources/monthlyenrollmentreport/";
        String filename = String.format("Monthly_Enrollment_Report for %s", PSPDate.getPSPTime().format("MMM yyyy"));

        File actual_file = new File(temp_Dir, filename + ".csv");

        assertNotNull(actual_file);
        if (StreamUtil.isFileIDPSEncrypted(actual_file)) {
            // String fileWithoutExt = actual_file.
            try {
                String fileWithoutExt = StreamUtil.FileWithoutExt(actual_file.getAbsolutePath());
                String fileName = fileWithoutExt + "." + "decrpt";
                File testDecFile = new File(fileName);
                Key key = IDPSFileStreamManager.newKeyHandleLatest();
                StreamUtil.streamDecryptFileSingleThread(key, actual_file, testDecFile);
                assertEquals(636, testDecFile.length());
                testDecFile.delete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            assertEquals(636, actual_file.length());  // This is the size of the file with only the headers and the data for newly added laws as part of companySetup process
        }
        logger.info("Test for file with only company setup data (only company laws) passed");
        //Test for file with only company setup data (only company laws) passed

        DataLoadServices.cancelService(company1, ServiceCode.Tax);
        DataLoadServices.cancelService(company2, ServiceCode.Cloud);
        DataLoadServices.cancelService(company2, ServiceCode.Tax);

        DataLoadServices.addCompanyLaws(company1, "40");

        PayrollServices.beginUnitOfWork();
        try {
            new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Throwable t) {
            logger.error("Error in writing monthly enrollment report", t);
        }
        PayrollServices.commitUnitOfWork();

        File expected_file = new File(expected_Dir, "test_ServiceCancellation_Active_PaymentTemplates.csv");
        actual_file = new File(temp_Dir, filename + ".csv");

        assertNotNull(actual_file);
        assertNotNull(expected_file);
        if (StreamUtil.isFileIDPSEncrypted(actual_file)) {

            try {
                String fileWithoutExt = StreamUtil.FileWithoutExt(actual_file.getAbsolutePath());
                String fileName = fileWithoutExt + "." + "decrpt";
                File testDecFile = new File(fileName);
                Key key = IDPSFileStreamManager.newKeyHandleLatest();
                IDPSFileReader reader = new IDPSFileReader(actual_file, key);
                BufferedReader actualFileData = new BufferedReader(reader);
                String data = actualFileData.readLine();
                BufferedWriter writer = new BufferedWriter(new FileWriter(testDecFile));
                PrintWriter out = new PrintWriter(writer);

                while (data != null) {
                    out.println(data);
                    data = actualFileData.readLine();
                }
                writer.close();
                assertEquals(expected_file.length(), testDecFile.length());
                testDecFile.delete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            assertEquals(expected_file.length(), actual_file.length());
        }
        assertCompareFiles(expected_file, actual_file);

        //Mark one company as DGDeleted
        Application.beginUnitOfWork();
        Application.refresh(company1);
        company1.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company1);
        Application.commitUnitOfWork();

        //Generate the report again
        PayrollServices.beginUnitOfWork();
        try {
            new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Throwable t) {
            logger.error("Error in writing monthly enrollment report", t);
        }
        PayrollServices.commitUnitOfWork();

        expected_file = new File(expected_Dir, "test_Newly_Added_Laws_to_DGDeletedCompany.csv");
        actual_file = new File(temp_Dir, filename + ".csv");

        assertNotNull(actual_file);
        assertNotNull(expected_file);
        if (StreamUtil.isFileIDPSEncrypted(actual_file)) {

            try {
                String fileWithoutExt = StreamUtil.FileWithoutExt(actual_file.getAbsolutePath());
                String fileName = fileWithoutExt + "." + "decrpt";
                File testDecFile = new File(fileName);
                Key key = IDPSFileStreamManager.newKeyHandleLatest();
                IDPSFileReader reader = new IDPSFileReader(actual_file, key);
                BufferedReader actualFileData = new BufferedReader(reader);
                String data = actualFileData.readLine();
                BufferedWriter writer = new BufferedWriter(new FileWriter(testDecFile));
                PrintWriter out = new PrintWriter(writer);

                while (data != null) {
                    out.println(data);
                    data = actualFileData.readLine();
                }
                writer.close();
                assertEquals(expected_file.length(), testDecFile.length());
                testDecFile.delete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            assertEquals(expected_file.length(), actual_file.length());
        }
        assertCompareFiles(expected_file, actual_file);


        //Mark one company as DGDeleted and reset the other
        Application.beginUnitOfWork();
        Application.refresh(company1);
        Application.refresh(company2);
        company1.setIsDgDisassociated(Boolean.FALSE);
        company2.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company2);
        Application.save(company1);
        Application.commitUnitOfWork();

        //Generate the report again
        PayrollServices.beginUnitOfWork();
        try {
            new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Throwable t) {
            logger.error("Error in writing monthly enrollment report", t);
        }
        PayrollServices.commitUnitOfWork();

        expected_file = new File(expected_Dir, "test_ServiceCancellation_Active_PaymentTemplates_DGDeletedCompany.csv");
        actual_file = new File(temp_Dir, filename + ".csv");

        assertNotNull(actual_file);
        assertNotNull(expected_file);
        if (StreamUtil.isFileIDPSEncrypted(actual_file)) {

            try {
                String fileWithoutExt = StreamUtil.FileWithoutExt(actual_file.getAbsolutePath());
                String fileName = fileWithoutExt + "." + "decrpt";
                File testDecFile = new File(fileName);
                Key key = IDPSFileStreamManager.newKeyHandleLatest();
                IDPSFileReader reader = new IDPSFileReader(actual_file, key);
                BufferedReader actualFileData = new BufferedReader(reader);
                String data = actualFileData.readLine();
                BufferedWriter writer = new BufferedWriter(new FileWriter(testDecFile));
                PrintWriter out = new PrintWriter(writer);

                while (data != null) {
                    out.println(data);
                    data = actualFileData.readLine();
                }
                writer.close();
                assertEquals(expected_file.length(), testDecFile.length());
                testDecFile.delete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            assertEquals(expected_file.length(), actual_file.length());
        }
        assertCompareFiles(expected_file, actual_file);

    }



    private void assertCompareFiles(File expectedFile, File actualFile){
        try {
            InputStreamReader fileReader=null;
            BufferedReader actualFileData = null;
            Key key = IDPSFileStreamManager.newKeyHandleLatest();
            if(StreamUtil.isFileIDPSEncrypted(actualFile)){
                try{
                    fileReader = new IDPSFileReader(actualFile,key);
                    actualFileData = new BufferedReader(new InputStreamReader(new IDPSFileInputStream(actualFile.getAbsolutePath(),key)));
                }catch (IdpsException e){
                    logger.info(e.getMessage());
                }catch(Exception e){

                }
            }else {
               // fileReader = new FileReader(actualFile);
                actualFileData = new BufferedReader(new InputStreamReader(new FileInputStream(actualFile.getAbsolutePath())));
            }
          //  actualFileData = new BufferedReader(new InputStreamReader(new FileInputStream(actualFile.getAbsolutePath())));
            BufferedReader expectedFileData = new BufferedReader(new InputStreamReader(new FileInputStream(expectedFile.getAbsolutePath())));

            HashSet<String> actualFileRecords = new HashSet<String>();
            HashSet<String> expectedFileRecords = new HashSet<String>();
            String actString = actualFileData.readLine();
            while(actString!= null){
                actualFileRecords.add(actString);
                actString = actualFileData.readLine();
            }
            while(expectedFileData.ready()){
                expectedFileRecords.add(expectedFileData.readLine());
            }
            List<String> actualFileRecordsList = actualFileRecords.stream().sorted().collect(Collectors.toList());
            List<String> expectedFileRecordsList = expectedFileRecords.stream().sorted().collect(Collectors.toList());
            Collections.sort(actualFileRecordsList);
            Collections.sort(expectedFileRecordsList);
            assertEquals(expectedFileRecordsList, actualFileRecordsList);
        } catch (FileNotFoundException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
