package com.intuit.sbd.payroll.psp.batchjobs.raf;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.utils.CompareResults;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.gateways.efe.EfeGateway;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Jan 11, 2011
 * Time: 5:13:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestRAFFileWriter {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void beforeEachTest() {
        DataLoadServices.reinitialize();
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPath() {
        DataLoadServices.reinitialize();
        int startingPsid = 523456789;

        for (int i=0; i<51; i++) {
            String newPSID = Integer.toString(startingPsid++);
            PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(newPSID);
            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, newPSID, payrollDTO);
            PayrollServices.commitUnitOfWork();
            assertSuccess(processResult);
    
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                    RAFEnrollmentStatus.PendingEnrollmentTape);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);
        }

        PayrollServices.beginUnitOfWork();
//      Simulate running this process:  PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.Add);  Don't run the actual process since that will try to schedule the job in flux
        RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafWriter = new RAFFileWriter();
        rafWriter.execute();

        RAFEmailWriter emailWriter = new RAFEmailWriter();
        emailWriter.execute();

        //persistence testing
        DomainEntitySet<RAFEnrollmentFile> files = Application.find(RAFEnrollmentFile.class);
        assertEquals("One enrollment file", 1, files.size());
        RAFEnrollmentFile file = files.get(0);
        assertEquals("Action code", RAFActionCode.Add, file.getRAFActionCode());
        assertEquals("Status", RAFFileStatus.Finalized, file.getStatus());
        assertNotNull("Email file name", file.getEmailFileName());
        assertNotNull("File file name", file.getFileName());

        validateFile(
                Application.findFileOnClassPath("raf/expected/RAF_add_HappyPath"),
                file.getFileName());

        validateFile(
                Application.findFileOnClassPath("raf/expected/RAF_add_HappyPath.csv"),
                file.getEmailFileName());

        startingPsid = 523456789;
        for (int i=0; i<51; i++) {
            String newPSID = Integer.toString(startingPsid++);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            RAFEnrollment currentEnrollment = company.getCurrentRAFEnrollment();
            assertEquals("RAF status", RAFEnrollmentStatus.PendingEnrollmentResponse, currentEnrollment.getStatus());

            RAFEnrollmentDetail enrollmentDetail = currentEnrollment.getRAFEnrollmentDetail();
            assertNotNull(enrollmentDetail);

            assertEquals("Enrollment detail EIN", company.getFedTaxId(), enrollmentDetail.getFedTaxid());
            assertEquals("Enrollment detail legal name", company.getLegalName(), enrollmentDetail.getLegalName());
            assertEquals("Enrollment detail legal street address", RAFFileWriter.getStreetAddress(company.getLegalAddress()), enrollmentDetail.getLegalStreetAddress());
            assertEquals("Enrollment detail City", company.getLegalAddress().getCity(), enrollmentDetail.getLegalCity());
            assertEquals("Enrollment detail State", company.getLegalAddress().getState(), enrollmentDetail.getLegalState());
            assertEquals("Enrollment detail Zip", company.getLegalAddress().getZipCode(), enrollmentDetail.getLegalZipCode());
            assertEquals("Enrollment detail 940 Tax Period", "201012", enrollmentDetail.getF940TaxPeriod());
            assertEquals("Enrollment detail 941 Tax Period ", "201012", enrollmentDetail.getF941TaxPeriod());
            assertEquals("Enrollment detail 94x FTD Tax Period", "201012", enrollmentDetail.getF94xFTDPeriod());
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testRecreateFile() {
        int startingPsid = 623456789;

        for (int i=0; i<3; i++) {
            String newPSID = Integer.toString(startingPsid++);
            PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(newPSID);
            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, newPSID, payrollDTO);
            PayrollServices.commitUnitOfWork();
            assertSuccess(processResult);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                    RAFEnrollmentStatus.PendingEnrollmentTape);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);
        }

        PayrollServices.beginUnitOfWork();
//      Simulate running this process:  PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.Add);  Don't run the actual process since that will try to schedule the job in flux
        RAFEnrollmentFile enrollmentFile = RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFFileWriterStep.class);
        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFEmailWriterStep.class);

        String senderId = BatchUtils.getConfigString("psp_efe_raf_enrollment_senderid");
        String senderAuthCode = BatchUtils.getConfigString("psp_efe_raf_enrollment_authcode");
        String senderEmail = BatchUtils.getConfigString("psp_efe_raf_enrollment_notification_email");

        Application.beginUnitOfWork();
        enrollmentFile = Application.findById(RAFEnrollmentFile.class, enrollmentFile.getId());
        Application.rollbackUnitOfWork();

        EfeGateway efeMock = EasyMock.createMock(EfeGateway.class);
        efeMock.sendRAFEnrollmentFile(new File(enrollmentFile.getFileName()), senderId, senderAuthCode, senderEmail);
        EasyMock.replay(efeMock);
        EfeGateway.setInstance(efeMock);

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFFileSendStep.class);

        System.setProperty("psp.test.email", "true");
        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFEmailStep.class);

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFArchiveStep.class);

        Application.beginUnitOfWork();
        //persistence testing
        DomainEntitySet<RAFEnrollmentFile> files = Application.find(RAFEnrollmentFile.class);
        assertEquals("One enrollment file", 1, files.size());
        RAFEnrollmentFile file = files.get(0);
        assertEquals("Action code", RAFActionCode.Add, file.getRAFActionCode());
        assertEquals("Status", RAFFileStatus.Completed, file.getStatus());
        assertNotNull("Email file name", file.getEmailFileName());
        assertNotNull("File file name", file.getFileName());

        RAFEnrollmentFile.initiateRecreation(file);
        Application.commitUnitOfWork();

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFFileWriterStep.class);

        Application.beginUnitOfWork();
        file = Application.findById(RAFEnrollmentFile.class, file.getId());
        Application.rollbackUnitOfWork();

        File rafFile = new File(file.getFileName());
        assertTrue("RAF file moved back to proper location", rafFile.exists());

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFEmailWriterStep.class);

        //make sure the files were moved back to their original locations in the proc directory
        Application.beginUnitOfWork();
        file = Application.findById(RAFEnrollmentFile.class, file.getId());
        Application.rollbackUnitOfWork();
        File emailFile = new File(file.getEmailFileName());
        assertTrue("Email file moved back to proper location", emailFile.exists());
    }

    @Test
    public void testEmailCreation() {
        int startingPsid = 723456789;

        for (int i=0; i<5; i++) {
            String newPSID = Integer.toString(startingPsid++);
            PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(newPSID);
            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, newPSID, payrollDTO);
            PayrollServices.commitUnitOfWork();
            assertSuccess(processResult);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                    RAFEnrollmentStatus.PendingEnrollmentTape);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);
        }

        PayrollServices.beginUnitOfWork();
//      Simulate running this process:  PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.Add);  Don't run the actual process since that will try to schedule the job in flux
        RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafWriter = new RAFFileWriter();
        rafWriter.execute();

        RAFEmailWriter emailWriter = new RAFEmailWriter();
        emailWriter.execute();

        //persistence testing
        DomainEntitySet<RAFEnrollmentFile> files = Application.find(RAFEnrollmentFile.class);
        assertEquals("One enrollment file", 1, files.size());
        RAFEnrollmentFile file = files.get(0);

        validateFile(
                Application.findFileOnClassPath("raf/expected/RAF_email_HappyPath.csv"),
                file.getEmailFileName());

    }

    @Test
    public void testHappyPath_Delete() {
        int startingPsid = 823456789;

        for (int i=0; i<5; i++) {
            String newPSID = Integer.toString(startingPsid++);
            PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(newPSID);
            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, newPSID, payrollDTO);
            PayrollServices.commitUnitOfWork();
            assertSuccess(processResult);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                    RAFEnrollmentStatus.PendingEnrollmentTape);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);
        }

        PayrollServices.beginUnitOfWork();
        //      Simulate running this process:  PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.Add);  Don't run the actual process since that will try to schedule the job in flux
        RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafWriter = new RAFFileWriter();
        rafWriter.execute();

        startingPsid = 823456789;
        int startingEIN = 876666666;

        for (int i=0; i<5; i++) {
            String newPSID = Integer.toString(startingPsid++);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                    RAFEnrollmentStatus.Enrolled);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            companyDTO.setFein(Integer.toString(startingEIN++));
            assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(newPSID, SourceSystemCode.QBDT);

            //Send the old enrollment to the delete tape
            result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(),  
                    company.getAllRAFEnrollments().get(1), RAFEnrollmentStatus.PendingDeleteTape);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);
        }

        PayrollServices.beginUnitOfWork();
        //      Simulate running this process:  PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.Delete);  Don't run the actual process since that will try to schedule the job in flux
        RAFEnrollmentFile.createFile(RAFActionCode.Delete);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafDeleteWriter = new RAFFileWriter();
        rafDeleteWriter.execute();

        //persistence testing
        DomainEntitySet<RAFEnrollmentFile> files = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Finalized);
        assertEquals("Two enrollment files", 2, files.size());
        RAFEnrollmentFile file = files.get(1);
        assertEquals("Action code", RAFActionCode.Delete, file.getRAFActionCode());
        assertEquals("Status", RAFFileStatus.Finalized, file.getStatus());
        assertNotNull("Email file name", file.getEmailFileName());
        assertNotNull("File file name", file.getFileName());

        startingPsid = 823456789;
        startingEIN = 1;
        for (int i=0; i<5; i++) {
            String newPSID = Integer.toString(startingPsid++);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            RAFEnrollment deletedEnrollment = company.getAllRAFEnrollments().get(1);
            assertEquals("RAF status", RAFEnrollmentStatus.Deleted, deletedEnrollment.getStatus());

            RAFEnrollmentDetail enrollmentDetail = deletedEnrollment.getRAFEnrollmentDetail();
            assertNotNull(enrollmentDetail);

            assertEquals("Enrollment detail EIN", "00000000"+startingEIN++, enrollmentDetail.getFedTaxid());
            assertEquals("Enrollment detail legal name", company.getLegalName(), enrollmentDetail.getLegalName());
            assertEquals("Enrollment detail legal street address", RAFFileWriter.getStreetAddress(company.getLegalAddress()), enrollmentDetail.getLegalStreetAddress());
            assertEquals("Enrollment detail City", company.getLegalAddress().getCity(), enrollmentDetail.getLegalCity());
            assertEquals("Enrollment detail State", company.getLegalAddress().getState(), enrollmentDetail.getLegalState());
            assertEquals("Enrollment detail Zip", company.getLegalAddress().getZipCode(), enrollmentDetail.getLegalZipCode());
            assertEquals("Enrollment detail 940 Tax Period", "201012", enrollmentDetail.getF940TaxPeriod());
            assertEquals("Enrollment detail 941 Tax Period ", "201012", enrollmentDetail.getF941TaxPeriod());
            assertEquals("Enrollment detail 94x FTD Tax Period", "201012", enrollmentDetail.getF94xFTDPeriod());
            assertNotNull(enrollmentDetail.getDeleteFile());
            assertNotNull(enrollmentDetail.getEnrollmentFile());

            PayrollServices.rollbackUnitOfWork();
        }

        RAFEmailWriter emailWriter = new RAFEmailWriter();
        emailWriter.execute();

        validateFile(
                Application.findFileOnClassPath("raf/expected/RAF_delete_HappyPath"),
                file.getFileName());

        validateFile(
                Application.findFileOnClassPath("raf/expected/RAF_delete_HappyPath.csv"),
                file.getEmailFileName());

    }

    @Test
    public void testAddDGDeletedCompany() {
        DataLoadServices.reinitialize();
        int startingPsid = 523456789;

        for (int i = 0; i < 2; i++) {
            String newPSID = Integer.toString(startingPsid++);
            PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(newPSID);
            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, newPSID, payrollDTO);
            PayrollServices.commitUnitOfWork();
            assertSuccess(processResult);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                    RAFEnrollmentStatus.PendingEnrollmentTape);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);
        }

        PayrollServices.beginUnitOfWork();
        RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("523456789", SourceSystemCode.QBDT);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        PayrollServices.commitUnitOfWork();


        RAFFileWriter rafWriter = new RAFFileWriter();
        rafWriter.execute();

        RAFEmailWriter emailWriter = new RAFEmailWriter();
        emailWriter.execute();

        //persistence testing
        DomainEntitySet<RAFEnrollmentFile> files = Application.find(RAFEnrollmentFile.class);
        assertEquals("One enrollment file", 1, files.size());
        RAFEnrollmentFile file = files.get(0);
        assertEquals("Action code", RAFActionCode.Add, file.getRAFActionCode());
        assertEquals("Status", RAFFileStatus.Finalized, file.getStatus());
        assertNotNull("Email file name", file.getEmailFileName());
        assertNotNull("File file name", file.getFileName());

        validateFile(
                Application.findFileOnClassPath("raf/expected/RAF_add_HappyPathDG"),
                file.getFileName());

        validateFile(
                Application.findFileOnClassPath("raf/expected/RAF_add_HappyPathDG.csv"),
                file.getEmailFileName());

        //Validations on DGDeletedCompany
        Application.beginUnitOfWork();
        Application.refresh(company);
        RAFEnrollment currentRAFEnrollment = company.getCurrentRAFEnrollment();
        assertEquals("RAF status", RAFEnrollmentStatus.PendingEnrollmentTape, currentRAFEnrollment.getStatus());
        assertNull(currentRAFEnrollment.getRAFEnrollmentDetail());
        Application.rollbackUnitOfWork();

        startingPsid = 523456790;

        String newPSID = Integer.toString(startingPsid);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
        RAFEnrollment currentEnrollment = company.getCurrentRAFEnrollment();
        assertEquals("RAF status", RAFEnrollmentStatus.PendingEnrollmentResponse, currentEnrollment.getStatus());

        RAFEnrollmentDetail enrollmentDetail = currentEnrollment.getRAFEnrollmentDetail();
        assertNotNull(enrollmentDetail);

        assertEquals("Enrollment detail EIN", company.getFedTaxId(), enrollmentDetail.getFedTaxid());
        assertEquals("Enrollment detail legal name", company.getLegalName(), enrollmentDetail.getLegalName());
        assertEquals("Enrollment detail legal street address", RAFFileWriter.getStreetAddress(company.getLegalAddress()), enrollmentDetail.getLegalStreetAddress());
        assertEquals("Enrollment detail City", company.getLegalAddress().getCity(), enrollmentDetail.getLegalCity());
        assertEquals("Enrollment detail State", company.getLegalAddress().getState(), enrollmentDetail.getLegalState());
        assertEquals("Enrollment detail Zip", company.getLegalAddress().getZipCode(), enrollmentDetail.getLegalZipCode());
        assertEquals("Enrollment detail 940 Tax Period", "201012", enrollmentDetail.getF940TaxPeriod());
        assertEquals("Enrollment detail 941 Tax Period ", "201012", enrollmentDetail.getF941TaxPeriod());
        assertEquals("Enrollment detail 94x FTD Tax Period", "201012", enrollmentDetail.getF94xFTDPeriod());
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void testDeleteDGDeletedCompany() {
        int startingPsid = 823456789;

        //Create 5 companies and set the RAFEnrollment status to PendingEnrollmentTape
        for (int i = 0; i < 5; i++) {
            String newPSID = Integer.toString(startingPsid++);
            PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(newPSID);
            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, newPSID, payrollDTO);
            PayrollServices.commitUnitOfWork();
            assertSuccess(processResult);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                    RAFEnrollmentStatus.PendingEnrollmentTape);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);
        }

        PayrollServices.beginUnitOfWork();
        RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafWriter = new RAFFileWriter();
        rafWriter.execute();

        startingPsid = 823456789;
        int startingEIN = 876666666;

        for (int i = 0; i < 5; i++) {
            String newPSID = Integer.toString(startingPsid++);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                    RAFEnrollmentStatus.Enrolled);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);

//            PayrollServices.beginUnitOfWork();
//            company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
//            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
//            companyDTO.setFein(Integer.toString(startingEIN++));
//            assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
//            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(newPSID, SourceSystemCode.QBDT);

            //Send the old enrollment to the delete tape
            result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                    company.getSourceSystemCd(), company.getSourceCompanyId(),
                    company.getAllRAFEnrollments().get(0), RAFEnrollmentStatus.PendingDeleteTape);
            PayrollServices.commitUnitOfWork();
            assertSuccess("Update enrollment status result", result);
        }

        // Mark one company as DGDeleted
        Application.beginUnitOfWork();
        Company company1 = Company.findCompany("823456789", SourceSystemCode.QBDT);
        company1.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company1);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        RAFEnrollmentFile.createFile(RAFActionCode.Delete);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafDeleteWriter = new RAFFileWriter();
        rafDeleteWriter.execute();

        //persistence testing
        DomainEntitySet<RAFEnrollmentFile> files = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Finalized);
        assertEquals("Two enrollment files", 2, files.size());
        RAFEnrollmentFile file = files.get(1);
        assertEquals("Action code", RAFActionCode.Delete, file.getRAFActionCode());
        assertEquals("Status", RAFFileStatus.Finalized, file.getStatus());
        assertNotNull("Email file name", file.getEmailFileName());
        assertNotNull("File file name", file.getFileName());

        //DGDeleted company validations
        Application.beginUnitOfWork();
        Application.refresh(company1);
        DomainEntitySet<RAFEnrollment> currentRAFEnrollments = company1.getAllRAFEnrollments();
        RAFEnrollment rafEnrollmentDelete = null;
        for (RAFEnrollment rafEnrollment : currentRAFEnrollments) {
            if (rafEnrollment.getStatus().equals(RAFEnrollmentStatus.PendingDeleteTape)) {
                rafEnrollmentDelete = rafEnrollment;
                break;
            }
        }
        assertNotNull(rafEnrollmentDelete);
        assertEquals("RAF status", RAFEnrollmentStatus.PendingDeleteTape, rafEnrollmentDelete.getStatus());
        Application.rollbackUnitOfWork();

        startingPsid = 823456790;
        startingEIN = 2;
        for (int i = 0; i < 4; i++) {
            String newPSID = Integer.toString(startingPsid++);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(newPSID, SourceSystemCode.QBDT);
            RAFEnrollment deletedEnrollment = company.getAllRAFEnrollments().get(0);
            assertEquals("RAF status", RAFEnrollmentStatus.Deleted, deletedEnrollment.getStatus());

            RAFEnrollmentDetail enrollmentDetail = deletedEnrollment.getRAFEnrollmentDetail();
            assertNotNull(enrollmentDetail);

            assertEquals("Enrollment detail EIN", "00000000" + startingEIN++, enrollmentDetail.getFedTaxid());
            assertEquals("Enrollment detail legal name", company.getLegalName(), enrollmentDetail.getLegalName());
            assertEquals("Enrollment detail legal street address", RAFFileWriter.getStreetAddress(company.getLegalAddress()), enrollmentDetail.getLegalStreetAddress());
            assertEquals("Enrollment detail City", company.getLegalAddress().getCity(), enrollmentDetail.getLegalCity());
            assertEquals("Enrollment detail State", company.getLegalAddress().getState(), enrollmentDetail.getLegalState());
            assertEquals("Enrollment detail Zip", company.getLegalAddress().getZipCode(), enrollmentDetail.getLegalZipCode());
            assertEquals("Enrollment detail 940 Tax Period", "201012", enrollmentDetail.getF940TaxPeriod());
            assertEquals("Enrollment detail 941 Tax Period ", "201012", enrollmentDetail.getF941TaxPeriod());
            assertEquals("Enrollment detail 94x FTD Tax Period", "201012", enrollmentDetail.getF94xFTDPeriod());
            assertNotNull(enrollmentDetail.getDeleteFile());
            assertNotNull(enrollmentDetail.getEnrollmentFile());

            PayrollServices.rollbackUnitOfWork();
        }

        RAFEmailWriter emailWriter = new RAFEmailWriter();
        emailWriter.execute();

        validateFile(
                Application.findFileOnClassPath("raf/expected/RAF_delete_HappyPathDG"),
                file.getFileName());

        validateFile(
                Application.findFileOnClassPath("raf/expected/RAF_delete_HappyPathDG.csv"),
                file.getEmailFileName());
    }

        private void validateFile(String pExpectedFileName, String pCreatedFileName) {
        try {
            BufferedReader expectedReader = new BufferedReader(new FileReader(pExpectedFileName));
            BufferedReader compareReader;
            Key key = IDPSFileStreamManager.newKeyHandleLatest();

            if(StreamUtil.isFileIDPSEncrypted(pCreatedFileName)){
                compareReader = new BufferedReader(new IDPSFileReader(pCreatedFileName,key));
            }else{
                compareReader = new BufferedReader(new FileReader(pCreatedFileName));
            }

            CompareResults compareResults = compareFiles(expectedReader, compareReader);

            if (!compareResults.getStatus()) {
                System.out.println(compareResults.toString());
            }
            assertEquals("File "+pCreatedFileName+" does not matches expected file "+pExpectedFileName, true, compareResults.getStatus());

        } catch (Exception ex) {
            ex.printStackTrace();
            TestCase.fail(ex.getMessage());
        }
    }


    public CompareResults compareFiles(BufferedReader inFile, BufferedReader compareFile) {
        // reset static vars
        ArrayList failureReasons = new ArrayList();
        int currentLine = 0;

        boolean valid = true;
        boolean eof = false;
        CompareResults result = new CompareResults();
        Set<String> inFileSet = new HashSet<String>();
        Set<String> compareFileSet = new HashSet<String>();
        try {
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
                }
                inFileSet.add(line);
                compareFileSet.add(compareLine);
                // increment current line
                currentLine++;
            }

            if(valid)
                valid = inFileSet.equals(compareFileSet);

            // return results
            result.setReasons(failureReasons);
            result.setStatus(valid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
