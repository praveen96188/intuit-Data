package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.paycycle.ops.eftpsBp.EdiEftpsFileValidator;
import com.paycycle.ops.eftpsBp.EdiEftpsRecordList;
import com.paycycle.util.PgpUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * User: svenkata
 * Date: Nov 30, 2010
 * Time: 10:58:53 AM
 */
public class EftpsEnrollmentTests {

    private SourceSystemCode srcSystemCodeForNewCompany = SourceSystemCode.QBDT;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        EftpsDataLoader.deleteAllTestDirFiles();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
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
    public void testConfigParameterSet() {
        File file = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("Work Directory \" %s \"  does not exists.", file.getName()), file.exists());

        file = new File(EftpsUtil.getArchiveDir());
        assertTrue(String.format("Archive Directory \" %s \"  does not exists.", file.getName()), file.exists());

        file = new File(EftpsUtil.getAS400Dir());
        assertTrue(String.format("AS400 Directory \" %s \" does not exists.", file.getName()), file.exists());

        file = new File(EftpsUtil.getErrDir());
        assertTrue(String.format("Error Directory \" %s \"  does not exists.", file.getName()), file.exists());

        file = new File(EftpsUtil.getTfaDir());
        assertTrue(String.format("TFA Directory \" %s \"  does not exists.", file.getName()), file.exists());

//        assertTrue("Ftp server name is not configured.", !EftpsUtil.getConfigString("psp_eftps_ftp_server").trim().equals(""));
//
//        assertTrue("Ftp server user name is not configured.", !EftpsUtil.getConfigString("psp_eftps_ftp_username").trim().equals(""));
//
//        assertTrue("Ftp server password is not set.", !EftpsUtil.getConfigString("psp_eftps_ftp_password").trim().equals(""));

    }

    @Test
    public void testNoCompanyExists() {

        File fileSendDir = new File(EftpsUtil.getWorkDir());

        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("eftps file records found.", 0, eftpsFiles.size());
        assertEquals("physical file created.", 0, fileSendDir.list().length);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testMoveToErrorStatus() {
        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);

        DomainEntitySet<EftpsFile> eftpsFile = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(" incorrect number of files", 1, eftpsFile.size());

        PayrollServices.beginUnitOfWork();

        for (EftpsFile file : eftpsFile) {

            File physicalFile = EftpsUtil.moveFile(file.getFileName(), EftpsUtil.getErrDir());
            EftpsFile.updateErrorStatus(file.getFileId(), physicalFile.getPath());
        }
        PayrollServices.commitUnitOfWork();

        eftpsFile = Application.find(EftpsFile.class);
        assertEquals(" incorrect number of files", 1, eftpsFile.size());
        assertEquals(" incorrect status", EdiFileStatus.Error, eftpsFile.get(0).getStatusCd());
    }

    @Test
    public void testOneComapnyToEnroll() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);
    }

    @Test
    public void testTwoComapnisWithDifferentStatesToEnroll() {

        String psid = "1234567";
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1234568";
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        
        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> company1Enrollments =  company1.getAllEnrollments();
        assertEquals("Company1 Enrollment size",1,company1Enrollments.size());
        assertEquals("Company1 Enrollment status",EftpsEnrollmentStatus.PendingEnrollment,company1Enrollments.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company2 = Company.findCompany(company2.getSourceCompanyId(), company2.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> company2Enrollments =  company2.getAllEnrollments();
        assertEquals("Company2 Enrollment size",1,company2Enrollments.size());
        assertEquals("Company2 Enrollment status",EftpsEnrollmentStatus.PendingEnrollment,company2Enrollments.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company2 = Company.findCompany(company2.getSourceCompanyId(), company2.getSourceSystemCd());
        company2.getAllEnrollments().get(0).setStatusCd(EftpsEnrollmentStatus.AgedOut);
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        company1Enrollments =  company1.getAllEnrollments();
        assertEquals("Company1 Enrollment size",1,company1.getAllEnrollments().size());
        assertEquals("Company1 Enrollment status",EftpsEnrollmentStatus.PendingAcceptance,company1.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company2 = Company.findCompany(company2.getSourceCompanyId(), company2.getSourceSystemCd());
        company2Enrollments =  company2.getAllEnrollments();
        assertEquals("Company2 Enrollment size",1,company2.getAllEnrollments().size());
        assertEquals("Company2 Enrollment status",EftpsEnrollmentStatus.AgedOut,company2.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        assertEquals("Eftps Files created.",1,EftpsFile.getPendingTransmissionEftpsFiles().size());
    }
    
    @Test
    public void testElevenCompaniesToEnroll() {

        String psid = "1111111";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111112";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111113";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111114";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111115";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111116";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111117";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111118";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111119";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111110";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        psid = "1111121";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);


        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        PayrollServices.beginUnitOfWork();
        String eftps_838_max_segment_count = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_838_MAX_SEGMENT_COUNT);
        String eftps_838_max_transaction_count = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_838_MAX_TRANSACTION_COUNT);
        SystemParameter.update(SystemParameter.Code.EFTPS_838_MAX_SEGMENT_COUNT, "2");
        SystemParameter.update(SystemParameter.Code.EFTPS_838_MAX_TRANSACTION_COUNT, "5");
        PayrollServices.commitUnitOfWork();

        try {
            EdiManager.processEnrollments();
        } finally {
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EFTPS_838_MAX_SEGMENT_COUNT, eftps_838_max_segment_count);
            SystemParameter.update(SystemParameter.Code.EFTPS_838_MAX_TRANSACTION_COUNT, eftps_838_max_transaction_count);
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();

        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertEquals("Incorrect number of enrollments created.",2,Application.find(EftpsFile.class).size());
    }

    @Test
    public void testWithInvalidFedTaxId() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("12345B89");
        company.setLegalName(" New Legal Name1 ");

        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = Application.find(EftpsEnrollment.class);
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("No Enrollments.", EftpsEnrollmentStatus.Invalid, eftpsEnrolls.get(0).getStatusCd());
        assertEquals("Enrollment file(s) exists.",0,Application.find(EftpsFile.class).size());
    }

    @Test
    public void testMissingFedTaxId() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("");
        company.setLegalName(" New Legal Name1 ");

        Application.save(company);

        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = Application.find(EftpsEnrollment.class);
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("No Enrollments.", EftpsEnrollmentStatus.Invalid, eftpsEnrolls.get(0).getStatusCd());
        assertEquals("Enrollment file(s) exists.",0,Application.find(EftpsFile.class).size());
    }

    @Test
    public void testFedTaxIdLengthMoreThan9() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("123456789123");
        company.setLegalName(" New Legal Name1 ");

        Application.save(company);
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = Application.find(EftpsEnrollment.class);
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("No Enrollments.", EftpsEnrollmentStatus.Invalid, eftpsEnrolls.get(0).getStatusCd());
        assertEquals("Enrollment file(s) exists.",0,Application.find(EftpsFile.class).size());
    }

    @Test
    public void testWithInvalidLegalZip() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("123456789");
        company.setLegalName(" New Legal Name1 ");
        company.getLegalAddress().setZipCode("891B");

        Application.save(company);
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = Application.find(EftpsEnrollment.class);
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("No Enrollments.", EftpsEnrollmentStatus.Invalid, eftpsEnrolls.get(0).getStatusCd());
        assertEquals("Enrollment file(s) exists.",0,Application.find(EftpsFile.class).size());
    }

    @Test
    public void testLegalZipLenghtOver5() {
        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.getLegalAddress().setZipCode("891111");       // should truncate and valid.
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = company.getAllEnrollments();
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("No Enrollments.", EftpsEnrollmentStatus.PendingAcceptance, eftpsEnrolls.get(0).getStatusCd());
        assertEquals("Enrollment file(s) doesn't exists.",1,Application.find(EftpsFile.class).size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNoLegalZip() {
        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("123456789");
        company.setLegalName(" New Legal Name1 ");
        company.getLegalAddress().setZipCode("");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = Application.find(EftpsEnrollment.class);
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("No Enrollments.", EftpsEnrollmentStatus.Invalid, eftpsEnrolls.get(0).getStatusCd());
        assertEquals("Enrollment file(s) exists.",0,Application.find(EftpsFile.class).size());
    }

    @Test
    public void testEmptyLegalName() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("123456789");
        company.setLegalName("");
        Application.save(company);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {

            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        PayrollServices.commitUnitOfWork();
        assertTrue(isIRS);      // IRS should exist.


        EdiManager.processEnrollments();

        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = Application.find(EftpsEnrollment.class);
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("No Enrollments.", EftpsEnrollmentStatus.Invalid, eftpsEnrolls.get(0).getStatusCd());
        assertEquals("Enrollment file(s) exists.",0,Application.find(EftpsFile.class).size());
    }

    @Test
    public void testLegalNameHavingSpecialChars() {
        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("123456789");
        company.setLegalName("LEGAL \\ AND ~");

        Application.save(company);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        assertTrue(isIRS);      // IRS should exist.

        EdiManager.processEnrollments();

        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = Application.find(EftpsEnrollment.class);
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("Invalid Enrollments Status.", EftpsEnrollmentStatus.PendingAcceptance, eftpsEnrolls.get(0).getStatusCd());
        assertEquals("Enrollment file(s) doesn't exists.",1,Application.find(EftpsFile.class).size());
    }

    @Test
    public void testLegalNameLengthOver35() {
        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("123456789");
        company.setLegalName("New Legal Name1 LEGAL LEGAL LEGAL LEGAL");
        Application.save(company);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        PayrollServices.commitUnitOfWork();
        assertTrue(isIRS);      // IRS should exist.

        EdiManager.processEnrollments();

        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = Application.find(EftpsEnrollment.class);
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("Invalid Enrollments Status.", EftpsEnrollmentStatus.PendingAcceptance, eftpsEnrolls.get(0).getStatusCd());
        assertEquals("Enrollment file(s) doesn't exists.",1,Application.find(EftpsFile.class).size());
    }

    @Test
    public void testNoLegalAddress() {
        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("123456789");
        company.setLegalName("New Legal Name1 LEGAL ");
        company.setLegalAddress(null);
        Application.save(company);
        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      // IRS should exist.
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = company.getAllEnrollments();
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("Invalid Enrollments Status.", EftpsEnrollmentStatus.Invalid, eftpsEnrolls.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTwoCompanies() {
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, "1111111", true, ServiceCode.Tax);
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, "2222222", true, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();

        EftpsDataLoader dataLoader = new EftpsDataLoader();

        company1 = Company.findCompany("1111111", srcSystemCodeForNewCompany);
        company1.setSourceCompanyId("111111111");
        company1.setFedTaxId("123456789");
        company1.setLegalName("New Legal Name1 LEGAL ");

        company2 = Company.findCompany("2222222", srcSystemCodeForNewCompany);
        company2.setSourceCompanyId("211111111");
        company2.setFedTaxId("123456789");
        company2.setLegalName("New Legal Name2 LEGAL ");

        Application.save(company1);
        Application.save(company2);

        DomainEntitySet<CompanyAgency> companyAgencies = company1.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency mCompanyAgency : companyAgencies) {
            if (mCompanyAgency.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      // IRS should exist.

        PayrollServices.commitUnitOfWork();

        File fileSendDir = new File(EftpsUtil.getWorkDir());

        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        PayrollServices.beginUnitOfWork();
        String eftps_838_max_segment_count = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_838_MAX_SEGMENT_COUNT);
        String eftps_838_max_transaction_count = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_838_MAX_TRANSACTION_COUNT);
        SystemParameter.update(SystemParameter.Code.EFTPS_838_MAX_SEGMENT_COUNT, "1");
        SystemParameter.update(SystemParameter.Code.EFTPS_838_MAX_TRANSACTION_COUNT, "1");
        PayrollServices.commitUnitOfWork();

        try {
            EdiManager.processEnrollments();
        } finally {
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EFTPS_838_MAX_SEGMENT_COUNT, eftps_838_max_segment_count);
            SystemParameter.update(SystemParameter.Code.EFTPS_838_MAX_TRANSACTION_COUNT, eftps_838_max_transaction_count);
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<EftpsFile> mEftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No enrollments for company1.", 1, company1.getAllEnrollments().size());
        assertEquals("Incorrect enrollment status for company1.", EftpsEnrollmentStatus.PendingAcceptance, company1.getAllEnrollments().get(0).getStatusCd());
        assertEquals("No enrollments for company2.", 1, company2.getAllEnrollments().size());
        assertEquals("Incorrect enrollment status for company2.", EftpsEnrollmentStatus.PendingAcceptance, company2.getAllEnrollments().get(0).getStatusCd());
        assertEquals("Should exist two files.", 2, mEftpsFiles.size());
        assertEquals("Invalid status.", EdiFileStatus.PendingTransmission, mEftpsFiles.get(0).getStatusCd());
        assertEquals("Invalid status.", EdiFileStatus.PendingTransmission, mEftpsFiles.get(1).getStatusCd());

        PayrollServices.commitUnitOfWork();
    }


    //    public void testUploadSingleFile() {
    //        ServiceInfoDTO assistedCompanyService = new ServiceInfoDTO();
    //        assistedCompanyService.setServiceCode(ServiceCode.Tax);
    //
    //        File fileSendDir = new File(EftpsUtil.getWorkDir());
    //        assertTrue("psp_eftps_ftp_work_dir doesn't exists", fileSendDir.exists());
    //
    //        PayrollServices.beginUnitOfWork();
    //        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
    //        EftpsDataLoader dataLoader = new EftpsDataLoader();
    //        OfferingInfoDTO offeringInfoDTO = new OfferingInfoDTO();
    //        offeringInfoDTO.setOfferingCode(OfferingCode.Assisted);
    //        ProcessResult procresult = PayrollServices.companyManager.addCompany(dataLoader.getCompany1());
    //        assertSuccess(procresult);
    //        procresult = PayrollServices.companyManager.addService(srcSystemCodeForNewCompany, "1234567", assistedCompanyService, offeringInfoDTO);
    //        assertSuccess(procresult);
    //        PayrollServices.commitUnitOfWork();
    //
    //        PayrollServices.beginUnitOfWork();
    //        Company company = Company
    //                .findCompany("1234567", srcSystemCodeForNewCompany);
    //        company.setSourceCompanyId("111111111");
    //        company.setFedTaxId("123456789");
    //        company.setLegalName(" New Legal Name1 ");
    //
    //        Agency agency = Application.findById(Agency.class, Agency.IRS);
    //
    //        CompanyAgency companyAgency =
    //                CompanyAgency.findCompanyAgency(company, agency.getAgencyId());
    //        if (companyAgency == null) {
    //            companyAgency = new CompanyAgency();
    //            companyAgency.setIntuitResponsibilityStartDate(CalendarUtils.getFirstDayOfQuarter(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone())));
    //            assertNotNull(agency);
    //            company.addCompanyAgency(companyAgency);
    //            companyAgency.setCompany(company);
    //            companyAgency.setAgency(agency);
    //            companyAgency.setAgencyTaxpayerId(company.getFedTaxId());//If IRS
    //            companyAgency = Application.save(companyAgency);
    //        }
    //
    //        EftpsEnrollment enroll = dataLoader.getEnrollment();
    //        EftpsEnrollmentDetail enrlDetail = dataLoader.getEnrollmentDetail();
    //        enrlDetail.setEftpsEnrollment(enroll);
    //        enrlDetail.getEftpsEnrollment().setCompanyAgency(companyAgency);
    //        Application.save(company);
    //        Application.save(enroll);
    //
    //        PayrollServices.commitUnitOfWork();
    //
    //        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
    //
    //        boolean isIRS = false;
    //        for (CompanyAgency companyAgency1 : companyAgencies) {
    //            if (companyAgency1.getAgency().isIRS()) {
    //                isIRS = true;
    //                break;
    //            }
    //        }
    //        assertTrue(isIRS);      //IRS should exist.
    //
    //        EdiManager.processEnrollments();
    //
    //        assert (Application.find(EftpsEnrollmentDetail.class).size() > 0);
    //
    //        DomainEntitySet<EftpsFile> mEftpsFiles = Application.find(EftpsFile.class);
    //
    //        assertTrue("No EFTPS file exists", mEftpsFiles.size() > 0);
    //
    //        String mGeneratedFilePath = ((EftpsFile) mEftpsFiles.get(0)).getFileName();
    //
    //        new EftpsFileMonitor().processPendingTransmissions();
    //
    //        Expression<EftpsFile> query = new Query<EftpsFile>().Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.Completed));
    //        mEftpsFiles = Application.find(EftpsFile.class, query);
    //
    //        assertTrue("No file has been uploaded. ", mEftpsFiles.size() == 1);
    //    }

    //    @Test
    //    public void testUploadTwoFiles() {
    //        testTwoCompanies();
    //        new EftpsFileMonitor().processPendingTransmissions();
    //        Expression<EftpsFile> query = new Query<EftpsFile>().Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.Completed));
    //        DomainEntitySet<EftpsFile> mEftpsFiles = Application.find(EftpsFile.class, query);
    //        assertTrue("No/few file(s) have been uploaded. ", mEftpsFiles.size() == 2);
    //    }

    @Test
    public void testOneComapnyToAgeOut() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setLegalName("legal name");
        Application.save(company);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency mCompanyAgency : companyAgencies) {
            if (mCompanyAgency.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      // IRS should exist.
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        EdiManager.ageOutEnrollments();

        PayrollServices.beginUnitOfWork();
        assertEquals("No enrollments for company.", 1, company.getAllEnrollments().size());
        assertEquals("Enrollment should not get aged out.", EftpsEnrollmentStatus.PendingAcceptance, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime((Integer.parseInt(EftpsUtil.getConfigString("psp_eftps_enrollment_ageout_days")) + 10));      // add more than 3 business days.
        PayrollServices.commitUnitOfWork();

        EdiManager.ageOutEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("No enrollments for company1.", 1, company.getAllEnrollments().size());
        assertEquals("Enrollment should not get aged out.", EftpsEnrollmentStatus.AgedOut, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEndToEnd_HappyPath() throws S3ConnectionException,S3UploadException {
        Company company = createAndProcessEnrollmentFile();

        EdiManager.processAS400Files();
        EdiManager.processPendingTransmissions();
        EdiManager.processWaitingResponseFiles();
        EdiManager.archiveFiles();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("Enrollment count is not correct.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid Enrollment status.", EftpsEnrollmentStatus.Enrolled, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEndToEnd_HappyPath_Ordered() throws Exception{
        Company company = createAndProcessEnrollmentFile();
        
        EdiManager.processAS400Files();
        EdiManager.processPendingTransmissions();

        List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir());

        for (File file : fileList) {
            file = PgpUtils.getUnencryptedFile(file);
            EdiEftpsFileValidator ediFile = new EdiEftpsFileValidator(file);
            if (!ediFile.isValid()) {
                throw new RuntimeException("EDI file failed validation.");
            }
            if(ediFile.getEftpsFileType().equals(EdiFileType.EftpsEnrollmentAck)) {
                EdiManager.processAcknowledgementFile(ediFile.getEdiFile().getPath());
            }
        }

        for (File file : fileList) {
            file = PgpUtils.getUnencryptedFile(file);
            EdiEftpsFileValidator ediFile = new EdiEftpsFileValidator(file);
            if (!ediFile.isValid()) {
                throw new RuntimeException("EDI file failed validation.");
            }
            if(ediFile.getEftpsFileType().equals(EdiFileType.EftpsEnrollmentResponse)) {
                EdiManager.processEnrollmentResponseFile(ediFile.getEdiFile().getPath());
            }
        }

        EdiManager.archiveFiles();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("Enrollment count is not correct.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid Enrollment status.", EftpsEnrollmentStatus.Enrolled, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEndToEnd_HappyPath_DGDeletedCompany() throws Exception{
        Company company = createAndProcessEnrollmentFile();

        EdiManager.processAS400Files();
        EdiManager.processPendingTransmissions();

        List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir());

        for (File file : fileList) {
            file = PgpUtils.getUnencryptedFile(file);
            EdiEftpsFileValidator ediFile = new EdiEftpsFileValidator(file);
            if (!ediFile.isValid()) {
                throw new RuntimeException("EDI file failed validation.");
            }
            if(ediFile.getEftpsFileType().equals(EdiFileType.EftpsEnrollmentAck)) {
                EdiManager.processAcknowledgementFile(ediFile.getEdiFile().getPath());
            }
        }

        //Mark the company as DGDeleted
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        Application.commitUnitOfWork();

        for (File file : fileList) {
            file = PgpUtils.getUnencryptedFile(file);
            EdiEftpsFileValidator ediFile = new EdiEftpsFileValidator(file);
            if (!ediFile.isValid()) {
                throw new RuntimeException("EDI file failed validation.");
            }
            if(ediFile.getEftpsFileType().equals(EdiFileType.EftpsEnrollmentResponse)) {
                EdiManager.processEnrollmentResponseFile(ediFile.getEdiFile().getPath());
            }
        }

        EdiManager.archiveFiles();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertEquals("Enrollment count is not correct.", 0, company.getAllEnrollments().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEndToEnd_HappyPath_ReverseOrdered() throws Exception{
        Company company = createAndProcessEnrollmentFile();

        EdiManager.processAS400Files();
        EdiManager.processPendingTransmissions();

        List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir());

        for (File file : fileList) {
            file = PgpUtils.getUnencryptedFile(file);
            EdiEftpsFileValidator ediFile = new EdiEftpsFileValidator(file);
            if (!ediFile.isValid()) {
                throw new RuntimeException("EDI file failed validation.");
            }
            if(ediFile.getEftpsFileType().equals(EdiFileType.EftpsEnrollmentResponse)) {
                EdiManager.processEnrollmentResponseFile(ediFile.getEdiFile().getPath());
            }
        }

        for (File file : fileList) {
            file = PgpUtils.getUnencryptedFile(file);
            EdiEftpsFileValidator ediFile = new EdiEftpsFileValidator(file);
            if (!ediFile.isValid()) {
                throw new RuntimeException("EDI file failed validation.");
            }
            if(ediFile.getEftpsFileType().equals(EdiFileType.EftpsEnrollmentAck)) {
                EdiManager.processAcknowledgementFile(ediFile.getEdiFile().getPath());
            }
        }

        EdiManager.archiveFiles();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("Enrollment count is not correct.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid Enrollment status.", EftpsEnrollmentStatus.Enrolled, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }
    
    private Company createAndProcessEnrollmentFile() throws S3ConnectionException,S3UploadException{
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> enrollments = company.getAllEnrollments();
        PayrollServices.rollbackUnitOfWork();

        assertEquals("Enrollment count is not correct.", 1, enrollments.size());

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        enrollments = company.getAllEnrollments();

        assertEquals("Enrollment count is not correct.", 1, enrollments.size());

        assertEquals("Enrollment should be in PendingAcceptance.", EftpsEnrollmentStatus.PendingAcceptance, enrollments.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        DomainEntitySet<EftpsFile> eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(1, eftpsFiles.size());

        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        DomainEntitySet<EftpsFile> list = EftpsFile.getCompletedEftpsFiles();

        TFASimulator simulator = new TFASimulator();

        for (EftpsFile eftpsFile : list) {
            try {
                File file = new File(eftpsFile.getFileName());
                file = PgpUtils.getUnencryptedFile(file);
                simulator.processFile(file, EftpsUtil.getTfaDir());
            } catch (Throwable t) {
                t.printStackTrace();
                assertTrue("Error processing enrollment file  Simulator.", "".equals("something."));
            }
        }
        EdiManager.archiveFiles();
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("Invalid Enrollment status.", EftpsEnrollmentStatus.PendingAcceptance, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        return company;
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Pending Enrollment
     * and EIN change.
     */
    @Test
    public void testPendingEnrollmentWhenEINChange() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("987654321");

        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(String.format("No enrollments found for company %s", company.getLegalName()), company.getAllEnrollments().size() > 0);

        assertEquals(EftpsEnrollmentStatus.PendingEnrollment, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Pending Enrollment
     * and Legal Name change.
     */
    @Test
    public void testPendingEnrollmentWhenLegalNameChange() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setLegalName(" New Legal Name1 ");
        Application.save(company);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.
        PayrollServices.commitUnitOfWork();

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setLegalName("Changed legal Name");

        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> enrollments = company.getAllEnrollments();
        assertTrue(String.format("No enrollments found for company %s", company.getLegalName()), enrollments.size() > 0);

        assertEquals(EftpsEnrollmentStatus.PendingEnrollment, enrollments.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Pending Enrollment
     * and Legal ZIP change.
     */
    @Test
    public void testPendingEnrollmentWhenLegalZipChange() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        AddressDTO addressDTO = PayrollServices.dtoFactory.create(company.getLegalAddress());
        addressDTO.setZipCode("98676");
        companyDTO.setLegalAddress(addressDTO);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.

        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(result);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(String.format("No enrollments found for company %s", company.getLegalName()), company.getAllEnrollments().size() > 0);

        assertEquals(EftpsEnrollmentStatus.PendingEnrollment, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Pending Enrollment
     * and Cancel Assisted service.
     */
    @Test
    public void testPendingEnrollmentWhenCancelAsstdService() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.

        ProcessResult<CompanyService> result = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);

        PayrollServices.commitUnitOfWork();

        assertSuccess(result);

        assertEquals("Messages size", 0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        assertTrue("No Enrollments found.", company.getAllEnrollments().size() > 0);

        assertEquals(EftpsEnrollmentStatus.Cancelled, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Pending Enrollment
     * and reenroll manually.
     */
    @Test
    public void testPendingEnrollWhenManualReEnroll() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        ProcessResult<EftpsEnrollment> result = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Cancelled);

        ProcessResult<EftpsEnrollment> result1 = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingEnrollment);

        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        assertTrue("No Enrollments found.", company.getAllEnrollments().size() > 0);

        assertEquals(EftpsEnrollmentStatus.PendingEnrollment, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Pending Acceptance
     * and Change EIN.
     */
    @Test
    public void testPAEInChange() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setFedTaxId("123456789");
        Application.save(company);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.
        PayrollServices.commitUnitOfWork();

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("987654321");

        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(String.format("No enrollments found for company %s", company.getLegalName()), company.getAllEnrollments().size() > 0);
        assertEquals("Two enrollments one with Cancelled status and another with PendingEnrollment should be present.", 2, company.getAllEnrollments().size());
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Expression<EftpsEnrollment> query = new Query<EftpsEnrollment>().OrderBy(EftpsEnrollment.CreatedDate());
        DomainEntitySet<EftpsEnrollment> mEnrollments = Application.find(EftpsEnrollment.class, query);

        assertEquals("Two enrollment records shold be available.", 2, mEnrollments.size());
        assertEquals("Previous enrollment should be cancelled.", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(0).getStatusCd());
        assertEquals("New enrollment should be in PendingEnrollment status.", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(1).getStatusCd());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Pending Acceptance
     * and Change LegalName.
     */
    @Test
    public void testPALegalNameChange() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);
        assertEquals("No enrollment found.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid enrollment status.", EftpsEnrollmentStatus.PendingAcceptance, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setLegalName("MODIFIED LEGAL NAME.");

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.

        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();

        assertEquals("Two enrollment records shold be available.", 2, mEnrollments.size());

        assertEquals("Previous enrollment should be cancelled.", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(0).getStatusCd());
        assertEquals("New enrollment should be PendingEnrollment status.", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(1).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Pending Acceptance
     * and Change LegalZip.
     */
    @Test
    public void testPALegalZipChange() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);
        assertEquals("No enrollment found.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid enrollment status.", EftpsEnrollmentStatus.PendingAcceptance, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        AddressDTO addressDTO = PayrollServices.dtoFactory.create(company.getLegalAddress());
        addressDTO.setZipCode("98676");
        companyDTO.setLegalAddress(addressDTO);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.

        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(String.format("No enrollments found for company %s", company.getLegalName()), company.getAllEnrollments().size() > 0);
        assertEquals("Two enrollments one with Cancelled status and another with PendingEnrollment should be present.", 2, company.getAllEnrollments().size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Expression<EftpsEnrollment> query = new Query<EftpsEnrollment>().OrderBy(EftpsEnrollment.CreatedDate());
        DomainEntitySet<EftpsEnrollment> mEnrollments = Application.find(EftpsEnrollment.class, query);
        assertEquals("Two enrollment records shold be available.", 2, mEnrollments.size());
        assertEquals("Previous enrollment should be cancelled.", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(0).getStatusCd());
        assertEquals("New enrollment should be PendingEnrollment status.", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(1).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Pending Acceptance
     * and Cancel Assisted service.
     */
    @Test
    public void testPendingAcceptEnrlWhenCancelAsstdService() {
        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);
        assertEquals("No enrollment found.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid enrollment status.", EftpsEnrollmentStatus.PendingAcceptance, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.

        ProcessResult<CompanyService> result = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();

        assertEquals("Messages size", 0, result.getMessages().size());

        assertEquals("No Enrollments found.", 1, mEnrollments.size());

        assertEquals(EftpsEnrollmentStatus.Cancelled, mEnrollments.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Enrolled.
     * and Change EIN.
     */
    @Test
    public void testEnrolledEInChange() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);
        assertEquals("No enrollment found.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid enrollment status.", EftpsEnrollmentStatus.PendingAcceptance, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();

        ProcessResult mResult = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), "1234567", EftpsEnrollmentStatus.Enrolled);
        assertSuccess("UpdateEftpsEnrollmentCore", mResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(String.format("No enrollments found"), company.getAllEnrollments().size() > 0);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("987654321");

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.

        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(String.format("No enrollments found for company %s", company.getFedTaxId()), company.getAllEnrollments().size() > 0);

        assertEquals("Two enrollments one with Cancelled status and another with PendingEnrollment should be present.", 2, company.getAllEnrollments().size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Expression<EftpsEnrollment> query = new Query<EftpsEnrollment>().OrderBy(EftpsEnrollment.CreatedDate());

        DomainEntitySet<EftpsEnrollment> mEnrollments = Application.find(EftpsEnrollment.class, query);
        assertEquals("Two enrollment records shold be available.", 2, mEnrollments.size());
        assertEquals("Previous enrollment should be cancelled.", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(0).getStatusCd());
        assertEquals("New enrollment should be PendingEnrollment status.", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(1).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Enrolled.
     * and Change Legal name.
     */
    @Test
    public void testEnrolledLegalNameChange() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);
        assertEquals("No enrollment found.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid enrollment status.", EftpsEnrollmentStatus.PendingAcceptance, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult mResult = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), "1234567", EftpsEnrollmentStatus.Enrolled);
        assertSuccess("UpdateEftpsEnrollmentCore", mResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setLegalName("modified legal name");

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(String.format("No enrollments found for company %s", company.getLegalName()), company.getAllEnrollments().size() > 0);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Expression<EftpsEnrollment> query = new Query<EftpsEnrollment>().OrderBy(EftpsEnrollment.CreatedDate());
        DomainEntitySet<EftpsEnrollment> mEnrollments = Application.find(EftpsEnrollment.class, query);
        assertEquals("no enrollment records.", 1, mEnrollments.size());
        assertEquals("Previous enrollment should be cancelled.", EftpsEnrollmentStatus.Enrolled, mEnrollments.get(0).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Enrolled.
     * and Change Legal name.
     */
    @Test
    public void testEnrolledManualRenrollment() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertTrue(Application.find(EftpsEnrollmentDetail.class).size() > 0);
        assertTrue(Application.find(EftpsFile.class).size() > 0);
        assertEquals("No enrollment found.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid enrollment status.", EftpsEnrollmentStatus.PendingAcceptance, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult mResult = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), "1234567", EftpsEnrollmentStatus.Enrolled);
        assertSuccess("UpdateEftpsEnrollmentCore", mResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);

        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setLegalName("modified legal name");

        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      //IRS should exist.

        ProcessResult<EftpsEnrollment> result = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingEnrollment);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();
        assertEquals("Incorrect enrollments found for company", 2, mEnrollments.size());
        assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(0).getStatusCd());
        assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(1).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

    }

    /**
     * Method to test condition.
     * EnrollmentStatus = Invalid
     * and Cancel Assisted service.
     */
    @Test
    public void testInvalidWhenCancelAsstdService() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setSourceCompanyId("111111111");
        company.setFedTaxId("123456789");
        company.setLegalName("New Legal Name1 LEGAL ");
        company.setLegalAddress(null);
        Application.save(company);
        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      // IRS should exist.
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = company.getAllEnrollments();
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("Invalid Enrollments Status.", EftpsEnrollmentStatus.Invalid, eftpsEnrolls.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> result1 = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
        assertSuccess(result1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();

        assertEquals("Messages size", 0, result1.getMessages().size());

        assertEquals("No Enrollments found.", 1, mEnrollments.size());

        assertEquals(EftpsEnrollmentStatus.Cancelled, mEnrollments.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }
    /**
     * Method to test condition.
     * EnrollmentStatus = Invalid
     * and Manual renrollment.
     */
    @Test
    public void testInvalidWhenInitiateManualReenrollment() {

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setFedTaxId("12345678999");
        Application.save(company);
        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        assertTrue(isIRS);      // IRS should exist.
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> eftpsEnrolls = company.getAllEnrollments();
        assertEquals("No Enrollments.", 1, eftpsEnrolls.size());
        assertEquals("Invalid Enrollments Status.", EftpsEnrollmentStatus.Invalid, eftpsEnrolls.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
        company.setFedTaxId("123456789");
        Application.save(company);

        ProcessResult<EftpsEnrollment> result = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingEnrollment);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();
        assertEquals("Incorrect enrollments found for company", 2, mEnrollments.size());
        assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(0).getStatusCd());
        assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(1).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }
    /**
    * Method to test condition.
    * EnrollmentStatus = Ageout
    * and FedTaxId change.
    */
   @Test
   public void testAgeOutEINChange() {

       PayrollServices.beginUnitOfWork();
       PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
       PayrollServices.commitUnitOfWork();

       DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);
       EdiManager.processEnrollments();

       PayrollServices.beginUnitOfWork();
       Company company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
       PSPDate.addDaysToPSPTime((Integer.parseInt(EftpsUtil.getConfigString("psp_eftps_enrollment_ageout_days")) + 10));      // add more than 3 business days.
       PayrollServices.commitUnitOfWork();

       EdiManager.ageOutEnrollments();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
       assertEquals("No enrollments for company1.", 1, company.getAllEnrollments().size());
       assertEquals("Enrollment should get aged out.", EftpsEnrollmentStatus.AgedOut, company.getAllEnrollments().get(0).getStatusCd());
       PayrollServices.rollbackUnitOfWork();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
       CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
       companyDTO.setFein("987654321");

       assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
       PayrollServices.commitUnitOfWork();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
       DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();
       assertEquals("Incorrect enrollments found for company", 2, mEnrollments.size());
       assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(0).getStatusCd());
       assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(1).getStatusCd());
       PayrollServices.rollbackUnitOfWork();
    }
    /**
    * Method to test condition.
    * EnrollmentStatus = Ageout
    * and LegalName change.
    */
   @Test
   public void testAgeOutLegalNameChange() {

       PayrollServices.beginUnitOfWork();
       PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
       PayrollServices.commitUnitOfWork();

       DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);
       EdiManager.processEnrollments();

       PayrollServices.beginUnitOfWork();
       Company company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
       PSPDate.addDaysToPSPTime((Integer.parseInt(EftpsUtil.getConfigString("psp_eftps_enrollment_ageout_days")) + 10));      // add more than 3 business days.
       PayrollServices.commitUnitOfWork();

       EdiManager.ageOutEnrollments();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
       assertEquals("No enrollments for company1.", 1, company.getAllEnrollments().size());
       assertEquals("Enrollment should get aged out.", EftpsEnrollmentStatus.AgedOut, company.getAllEnrollments().get(0).getStatusCd());
       PayrollServices.rollbackUnitOfWork();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
       CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
       companyDTO.setLegalName("legal name changed.");

       ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
       PayrollServices.commitUnitOfWork();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
       DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();
       assertEquals("Incorrect enrollments found for company", 2, mEnrollments.size());
       assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(0).getStatusCd());
       assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(1).getStatusCd());
       PayrollServices.rollbackUnitOfWork();
    }

    /**
    * Method to test condition.
    * EnrollmentStatus = Ageout
    * and LegalZip change.
    */
   @Test
   public void testAgeOutLegalZipChange() {

       PayrollServices.beginUnitOfWork();
       PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
       PayrollServices.commitUnitOfWork();

       DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);
       EdiManager.processEnrollments();

       PayrollServices.beginUnitOfWork();
       Company company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
       PSPDate.addDaysToPSPTime((Integer.parseInt(EftpsUtil.getConfigString("psp_eftps_enrollment_ageout_days")) + 10));      // add more than 3 business days.
       PayrollServices.commitUnitOfWork();

       EdiManager.ageOutEnrollments();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
       assertEquals("No enrollments for company1.", 1, company.getAllEnrollments().size());
       assertEquals("Enrollment should get aged out.", EftpsEnrollmentStatus.AgedOut, company.getAllEnrollments().get(0).getStatusCd());
       PayrollServices.rollbackUnitOfWork();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
       CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
       AddressDTO addressDTO = PayrollServices.dtoFactory.create(company.getLegalAddress());
       addressDTO.setZipCode("98676");
       companyDTO.setLegalAddress(addressDTO);

       ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
       PayrollServices.commitUnitOfWork();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
       DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();
       assertEquals("Incorrect enrollments found for company", 2, mEnrollments.size());
       assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(0).getStatusCd());
       assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(1).getStatusCd());
       PayrollServices.rollbackUnitOfWork();
    }
    /**
    * Method to test condition.
    * EnrollmentStatus = Ageout
    * and Cancel Assisted service
    */
   @Test
   public void testAgeOutCancelAssistedService() {

       PayrollServices.beginUnitOfWork();
       PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
       PayrollServices.commitUnitOfWork();

       DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);
       EdiManager.processEnrollments();

       PayrollServices.beginUnitOfWork();
       Company company = Company.findCompany("1234567", srcSystemCodeForNewCompany);
       PSPDate.addDaysToPSPTime((Integer.parseInt(EftpsUtil.getConfigString("psp_eftps_enrollment_ageout_days")) + 10));      // add more than 3 business days.
       PayrollServices.commitUnitOfWork();

       EdiManager.ageOutEnrollments();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
       assertEquals("No enrollments for company1.", 1, company.getAllEnrollments().size());
       assertEquals("Enrollment should get aged out.", EftpsEnrollmentStatus.AgedOut, company.getAllEnrollments().get(0).getStatusCd());
       PayrollServices.rollbackUnitOfWork();

       PayrollServices.beginUnitOfWork();
       ProcessResult<CompanyService> result = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
       assertSuccess(result);
       PayrollServices.commitUnitOfWork();

       PayrollServices.beginUnitOfWork();
       company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
       DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();
       assertEquals("Incorrect enrollments found for company", 1, mEnrollments.size());
       assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(0).getStatusCd());
       PayrollServices.rollbackUnitOfWork();
    }
     /**
     * Method to test condition.
     * EnrollmentStatus = Ageout
     * and Cancel Manual Renrollment.
     */
    @Test
    public void testAgeOutManualReEnrollment() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax); 
        EdiManager.processEnrollments();
         
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", srcSystemCodeForNewCompany); 
        PSPDate.addDaysToPSPTime((Integer.parseInt(EftpsUtil.getConfigString("psp_eftps_enrollment_ageout_days")) + 10));      // add more than 3 business days.
        PayrollServices.commitUnitOfWork();

        EdiManager.ageOutEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("No enrollments for company1.", 1, company.getAllEnrollments().size());
        assertEquals("Enrollment should get aged out.", EftpsEnrollmentStatus.AgedOut, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult<EftpsEnrollment> result = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingEnrollment);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> mEnrollments = company.getAllEnrollments();
        assertEquals("Incorrect enrollments found for company", 2, mEnrollments.size());
        assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.PendingEnrollment, mEnrollments.get(0).getStatusCd());
        assertEquals("Incorrect enrollment found", EftpsEnrollmentStatus.Cancelled, mEnrollments.get(1).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

     }

    @Test
    public void test838FileCreation() throws Exception {
        DataLoadServices.reinitialize();

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();

        Agency agency = Application.findById(Agency.class, Agency.IRS);
        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>().Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.PendingTransmission));

        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class, query);

        assertEquals("No file 838 generated.", 1, eftpsFiles.size());

        PayrollServices.commitUnitOfWork();

        File buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsEnrollment_838.838");
        EdiEftpsRecordList lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
        File file = new File(eftpsFiles.get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList rhsFile = new EdiEftpsRecordList(file);
        assertTrue(lhsFile.equals(rhsFile));
    }
    @Test
    public void test_997_824_FileCreation() throws Exception {
        DataLoadServices.reinitialize();

        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();

        Agency agency = Application.findById(Agency.class, Agency.IRS);
        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>().Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.PendingTransmission));

        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class, query);

        assertEquals("No file 838 generated.", 1, eftpsFiles.size());

        PayrollServices.commitUnitOfWork();

        File buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsEnrollment_838.838");
        EdiEftpsRecordList lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
        File file = new File(eftpsFiles.get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList rhsFile = new EdiEftpsRecordList(file);
        assertTrue(lhsFile.equals(rhsFile));

        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        DomainEntitySet<EftpsFile> list = EftpsFile.getCompletedEftpsFiles();

        TFASimulator simulator = new TFASimulator();
        ConfigurationManager.getConfiguration(ConfigurationModule.TaxAgency).getConfigurationEntries().add("psp_eftps_ftp_send_dir", EftpsUtil.getWorkDir());

        for (EftpsFile eftpsFile : list) {
            try {
                File fileEftps = new File(eftpsFile.getFileName());
                fileEftps = PgpUtils.getUnencryptedFile(fileEftps);
                simulator.processFile(fileEftps, EftpsUtil.getTfaDir());
            } catch (Throwable t) {
                t.printStackTrace();
                assertTrue("Error in processing file at Simulator.", "".equals("something."));
            }
        }
        EdiManager.archiveFiles();

        List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir());

        for (File mFile : fileList) {
            mFile = PgpUtils.getUnencryptedFile(mFile);

            switch (EftpsDataLoader.readEdiFile(mFile).getEftpsFileType()) {
                case EftpsEnrollmentAck: {
                    buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsEnrollmentAck_997.997");
                    lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
                    rhsFile = new EdiEftpsRecordList(new File(mFile.getPath()));
                    assertTrue(lhsFile.equals(rhsFile));
                    break;
                }
                case EftpsEnrollmentResponse: {
                    buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsEnrollmentResponse_824.824");
                    lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
                    rhsFile = new EdiEftpsRecordList(new File(mFile.getPath()));
                    assertTrue(lhsFile.equals(rhsFile));
                    break;
                }
            }
        }
    }

    @Test
    public void testEndToEnd_Reject() throws S3ConnectionException,S3UploadException{

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "1234567", true, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();

        Agency agency = Application.findById(Agency.class, Agency.IRS);
        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

        boolean isIRS = false;
        for (CompanyAgency companyAgency1 : companyAgencies) {
            if (companyAgency1.getAgency().isIRS()) {
                isIRS = true;
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EftpsEnrollment> enrollments = company.getAllEnrollments();

        assertEquals("Enrollment count is not correct.", 1, enrollments.size());
        PayrollServices.rollbackUnitOfWork();

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        enrollments = company.getAllEnrollments();

        assertEquals("Enrollment count is not correct.", 1, enrollments.size());

        assertEquals("Enrollment should be in PendingAcceptance.", EftpsEnrollmentStatus.PendingAcceptance, enrollments.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        DomainEntitySet<EftpsFile> eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(1, eftpsFiles.size());

        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        DomainEntitySet<EftpsFile> list = EftpsFile.getCompletedEftpsFiles();

        TFASimulator simulator = new TFASimulator();

        for (EftpsFile eftpsFile : list) {
            try {
                File file = new File(eftFile.getFileName());
                file = PgpUtils.getUnencryptedFile(file);
                List<RejectionInfo> rejInfo = EftpsDataLoader.induceEnrollmentRejectInfo(file.getAbsolutePath());

                simulator.processEnrollmentFileWithErrors(file, EftpsUtil.getTfaDir(),rejInfo);
            } catch (Throwable t) {
                t.printStackTrace();
                assertTrue("Error processing enrollment file  Simulator.", "".equals("something."));
            }
        }
        EdiManager.archiveFiles();

        EdiManager.processAS400Files();
        EdiManager.processPendingTransmissions();
        EdiManager.processWaitingResponseFiles();
        EdiManager.archiveFiles();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("Enrollment count is not correct.", 1, company.getAllEnrollments().size());
        assertEquals("Invalid Enrollment status.", EftpsEnrollmentStatus.Rejected, company.getAllEnrollments().get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSecondaryEnrollment() throws Throwable {

        DataLoadServices.setPSPDate(2012, 12, 31);
        Company company = createAndProcessEnrollmentFile();
        EdiManager.processPendingTransmissions();
        EdiManager.processWaitingResponseFiles();

        DataLoadServices.setPSPDate(2013, 1, 2);
        Application.beginUnitOfWork();
        ProcessResult<EftpsEnrollment> secondaryEftpsEnrollment = PayrollServices.companyManager.createSecondaryEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), "119876543", "Stella's Avatar", "45014");
        assertSuccess(secondaryEftpsEnrollment);
        Application.commitUnitOfWork();

        EftpsEnrollment enrollment = secondaryEftpsEnrollment.getResult();
        assertTrue(enrollment.getSecondary());
        assertEquals(EftpsEnrollmentStatus.PendingEnrollment, enrollment.getStatusCd());
        EftpsEnrollmentDetail detail = enrollment.findEnrollmentDetail();
        assertEquals(EftpsEnrollmentStatus.None, detail.getStatusCd());
        assertEquals("119876543", detail.getFedTaxId());
        assertEquals("Stella's Avatar", detail.getLegalName());
        assertEquals("45014", detail.getLegalZip());
        assertNull(detail.getParentFile());

        DataLoadServices.setPSPDate(2013, 1, 3);
        EdiManager.processEnrollments();

        simulateComplete(assertOne(EftpsFile.getPendingTransmissionEftpsFiles().find(EftpsFile.FileType().equalTo(EdiFileType.EftpsEnrollmentResponseAck))));
        simulateComplete(assertOne(EftpsFile.getPendingTransmissionEftpsFiles().find(EftpsFile.FileType().equalTo(EdiFileType.EftpsEnrollment))));

        DomainEntitySet<EftpsFile> list = EftpsFile.getCompletedEftpsFiles();

        TFASimulator simulator = new TFASimulator();

        for (EftpsFile eftpsFile : list) {
            File file = new File(eftpsFile.getFileName());
            System.out.println(eftpsFile.getFileName());
            file = PgpUtils.getUnencryptedFile(file);
            simulator.processFile(file, EftpsUtil.getTfaDir());
        }
        EdiManager.archiveFiles();

        DataLoadServices.setPSPDate(2013, 1, 4);
        EdiManager.processPendingTransmissions();
        EdiManager.processWaitingResponseFiles();
        EdiManager.archiveFiles();

        PayrollServices.beginUnitOfWork();
        Application.refresh(detail);
        assertEquals(EftpsEnrollmentStatus.Enrolled, detail.getStatusCd());
        assertEquals(EftpsEnrollmentStatus.Enrolled, detail.getEftpsEnrollment().getStatusCd());

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EnrollmentStatusChanged, null, SpcfCalendar.createInstance(2013, 1, 1), null);
        assertEquals(3, companyEvents.size());
        CompanyEvent createdEvent = companyEvents.get(0);
        assertEquals("Manual Enrollment: 119876543/Stella's Avatar/45014", createdEvent.getCompanyEventDetailValue(EventDetailTypeCode.NoteText));
        assertEquals("<none>", createdEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldStringValue));
        assertEquals("PendingEnrollment", createdEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue));

        CompanyEvent pendingAcceptEvent = companyEvents.get(1);
        assertEquals("Manual Enrollment: 119876543/Stella's Avatar/45014", pendingAcceptEvent.getCompanyEventDetailValue(EventDetailTypeCode.NoteText));
        assertEquals("PendingEnrollment", pendingAcceptEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldStringValue));
        assertEquals("PendingAcceptance", pendingAcceptEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue));

        CompanyEvent enrolledEvent = companyEvents.get(2);
        assertEquals("Manual Enrollment: 119876543/Stella's Avatar/45014", enrolledEvent.getCompanyEventDetailValue(EventDetailTypeCode.NoteText));
        assertEquals("PendingAcceptance", enrolledEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldStringValue));
        assertEquals("Enrolled", enrolledEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue));

        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void testvalidateEnrollmentDataForDGDeletedCompany() {
        String psid = "1234567";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        //Check if the work directory exists or not where enrollment file going to be created.
        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        Application.commitUnitOfWork();

        EdiManager.processEnrollments();

        DomainEntitySet<EftpsFile> eftpsFile = Application.find(EftpsFile.class);
        assertEquals(" incorrect number of files", 0, eftpsFile.size());
        assertEquals("physical file created.", 0, fileSendDir.list().length);

    }

    private void simulateComplete(EftpsFile file) {
        PayrollServices.beginUnitOfWork();
        Application.refresh(file);
        file.setStatusCd(EdiFileStatus.Completed);
        Application.save(file);
        PayrollServices.commitUnitOfWork();
    }

}
