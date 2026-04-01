package com.intuit.sbd.payroll.psp.batchjobs.raf;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.MailSenderHolder;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.gateways.efe.EfeGateway;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import org.easymock.EasyMock;
import org.junit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * User: rnorian
 * Date: 11/23/11
 * Time: 3:50 PM
 */
public class TestRAFWriterBatchJob {

    private static List<File> rafFiles = new ArrayList<File>();
    private static File rafAdd = null;
    private static File rafDelete = null;
    private static String defaultSender = "OSP-FILE";
    private static String defaultAuthCode = "PSP-UNIT-TEST";
    private static String defaultEmail = null;

    @BeforeClass
    public static void beforeClass() {
        rafAdd = new File(Application.findFileOnClassPath("raf/expected/RAF_add_HappyPath"));
        rafFiles.add(rafAdd);
        rafDelete = new File(Application.findFileOnClassPath("raf/expected/RAF_delete_HappyPath"));
        rafFiles.add(rafDelete);

        for (File rafFile : rafFiles) {
            if (!rafFile.canRead()) {
                throw new RuntimeException("can't read file: " + rafFile.getName());
            }
        }
    }

    @Before
    public void beforeEachTest() {
        DataLoadServices.reinitialize();
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        EfeGateway.setInstance(null);
        MailSenderHolder.setMessage(null);
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testEnrollmentSend_HappyPathAdd() {
        DataLoadServices.reinitialize();
        int startingPsid = 523456789;

        for (int i=0; i<1; i++) {
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
        assertEnrollmentFile(RAFFileStatus.Initiated);

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFFileWriterStep.class, "");
        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFEmailWriterStep.class, "");
        assertEnrollmentFile(RAFFileStatus.Finalized);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<RAFEnrollmentFile> enrollmentFiles = Application.find(RAFEnrollmentFile.class);
        RAFEnrollmentFile enrollmentFile = enrollmentFiles.get(0);
        PayrollServices.rollbackUnitOfWork();

        String senderId = BatchUtils.getConfigString("psp_efe_raf_enrollment_senderid");
        String senderAuthCode = BatchUtils.getConfigString("psp_efe_raf_enrollment_authcode");
        String senderEmail = BatchUtils.getConfigString("psp_efe_raf_enrollment_notification_email");

        EfeGateway efeMock = EasyMock.createMock(EfeGateway.class);
        efeMock.sendRAFEnrollmentFile(new File(enrollmentFile.getFileName()), senderId, senderAuthCode, senderEmail);
        EasyMock.replay(efeMock);
        EfeGateway.setInstance(efeMock);

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFFileSendStep.class);
        assertEnrollmentFile(RAFFileStatus.Transmitted);

        System.setProperty("psp.test.email", "true");
        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFEmailStep.class);
        assertEnrollmentFile(RAFFileStatus.Emailed);

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFArchiveStep.class);
        assertEnrollmentFile(RAFFileStatus.Completed);
    }

    @Test
    public void testEnrollmentSend_TransmissionFailure() {
        DataLoadServices.reinitialize();
        int startingPsid = 523456789;

        for (int i=0; i<1; i++) {
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
        assertEnrollmentFile(RAFFileStatus.Initiated);

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFFileWriterStep.class, "");
        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFEmailWriterStep.class, "");
        assertEnrollmentFile(RAFFileStatus.Finalized);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<RAFEnrollmentFile> enrollmentFiles = Application.find(RAFEnrollmentFile.class);
        RAFEnrollmentFile enrollmentFile = enrollmentFiles.get(0);
        PayrollServices.rollbackUnitOfWork();

        String senderId = BatchUtils.getConfigString("psp_efe_raf_enrollment_senderid");
        String senderAuthCode = BatchUtils.getConfigString("psp_efe_raf_enrollment_authcode");
        String senderEmail = BatchUtils.getConfigString("psp_efe_raf_enrollment_notification_email");

        EfeGateway efeMock = EasyMock.createMock(EfeGateway.class);
        efeMock.sendRAFEnrollmentFile(new File(enrollmentFile.getFileName()), senderId, senderAuthCode, senderEmail);
        EasyMock.expectLastCall().andThrow(new RuntimeException("Simulate EFE Failure"));

        EasyMock.replay(efeMock);
        EfeGateway.setInstance(efeMock);

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFFileSendStep.class);
        assertEnrollmentFile(RAFFileStatus.Finalized);

        // simulate resend
        EasyMock.reset(efeMock);
        efeMock.sendRAFEnrollmentFile(new File(enrollmentFile.getFileName()), senderId, senderAuthCode, senderEmail);
        EasyMock.replay(efeMock);

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFFileSendStep.class);
        assertEnrollmentFile(RAFFileStatus.Transmitted);

        System.setProperty("psp.test.email", "true");
        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFEmailStep.class);
        assertEnrollmentFile(RAFFileStatus.Emailed);

        BatchJobManager.runJobStep(BatchJobType.RAFWriter, RAFProcessor.RAFArchiveStep.class);
        assertEnrollmentFile(RAFFileStatus.Completed);
    }

    private void assertEnrollmentFile(RAFFileStatus pExpectedStatus) {
        PayrollServices.beginUnitOfWork();

        DomainEntitySet<RAFEnrollmentFile> enrollmentFiles = Application.find(RAFEnrollmentFile.class);
        Assert.assertEquals("enrollmentFiles count", 1, enrollmentFiles.size());
        RAFEnrollmentFile enrollmentFile = enrollmentFiles.get(0);
        Assert.assertEquals("EnrollmentFile Status", pExpectedStatus, enrollmentFile.getStatus());

        if (pExpectedStatus.equals(RAFFileStatus.Finalized)) {
            Assert.assertNotNull("EnrollmentFile Name", enrollmentFile.getFileName());
            Assert.assertTrue("File Exists", new File(enrollmentFile.getFileName()).exists());
            Assert.assertEquals("processing dir", BatchUtils.getConfigString("psp_raf_ftp_srcdir"), new File(enrollmentFile.getFileName()).getParent());
        }

        if (pExpectedStatus.equals(RAFFileStatus.Transmitted) || pExpectedStatus.equals(RAFFileStatus.Emailed)) {
            Assert.assertNotNull("EnrollmentFile Name", enrollmentFile.getFileName());
            Assert.assertTrue("File Exists", new File(enrollmentFile.getFileName()).exists());
            Assert.assertEquals("processing dir", BatchUtils.getConfigString("psp_raf_ftp_srcdir"), new File(enrollmentFile.getFileName()).getParent());

            Assert.assertNotNull("EnrollmentFile EmailFileName", enrollmentFile.getEmailFileName());
            Assert.assertTrue("Email File Exists", new File(enrollmentFile.getEmailFileName()).exists());
            Assert.assertEquals("processing dir", BatchUtils.getConfigString("psp_raf_ftp_srcdir"), new File(enrollmentFile.getFileName()).getParent());
        }

        if (pExpectedStatus.equals(RAFFileStatus.Completed)) {
            Assert.assertNotNull("EnrollmentFile Name", enrollmentFile.getFileName());
            Assert.assertTrue("File Exists", new File(enrollmentFile.getFileName()).exists());
            Assert.assertEquals("File Archived", BatchUtils.getConfigString("psp_raf_ftp_archdir"), new File(enrollmentFile.getFileName()).getParentFile().getPath());

            Assert.assertNotNull("EnrollmentFile EmailFileName", enrollmentFile.getEmailFileName());
            Assert.assertTrue("Email File Exists", new File(enrollmentFile.getEmailFileName()).exists());
            Assert.assertEquals("File Archived", BatchUtils.getConfigString("psp_raf_ftp_archdir"), new File(enrollmentFile.getEmailFileName()).getParentFile().getPath());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEnrollmentSend_NoEnrollments() {
        DataLoadServices.reinitialize();

        PayrollServices.beginUnitOfWork();
        RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();
        assertEnrollmentFile(RAFFileStatus.Initiated);

        System.setProperty("psp.test.email", "true");
        BatchJobManager.runJob(BatchJobType.RAFWriter);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<RAFEnrollmentFile> enrollmentFiles = Application.find(RAFEnrollmentFile.class);
        Assert.assertEquals("enrollment file deleted", 0, enrollmentFiles.size());
        Assert.assertNull("enrollment email", MailSenderHolder.getMessage());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testEnrollmentSend_InvalidFilePaths() {
        try {
            EfeGateway.getInstance().sendRAFEnrollmentFile(new File("non-existent file"), defaultSender, defaultAuthCode, defaultEmail);
            Assert.fail("exception should have been thrown for failed file existence");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testEnrollmentSend_InvalidArgsToEfe() {
        try {
            EfeGateway.getInstance().sendRAFEnrollmentFile(rafAdd, "", defaultAuthCode, defaultEmail);
            Assert.fail("exception should have been thrown for failed field validation");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
