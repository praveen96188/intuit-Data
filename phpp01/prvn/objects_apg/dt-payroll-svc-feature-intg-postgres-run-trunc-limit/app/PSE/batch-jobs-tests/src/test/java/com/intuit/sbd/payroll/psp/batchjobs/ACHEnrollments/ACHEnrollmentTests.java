package com.intuit.sbd.payroll.psp.batchjobs.ACHEnrollments;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.HashMap;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * User: ihannur
 * Date: 2/4/13
 * Time: 4:21 PM
 */
public class ACHEnrollmentTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.FL_SUI, SpcfCalendar.createInstance(2011, 1, 1));

        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(2013, 1, 22);
        DataLoadServices.reinitialize();
        DataLoadServices.deleteAllACHEnrollmentDirFiles();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testACHEnrollmentWithCustomRequirement() {
        // EXEMPTED_AGENCY_IDS = 9999999,0999999,0000000,1234567 These are all invalid Ids
        String[] states = {"FL"};
        String psid = "11";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.addCompanyLawsWithAgencyId("1234567", company, "FL");

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2013-02-01"), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find());
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("Agency Id custom requirement", 0, Application.find(ACHEnrollment.class).size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addCompanyLawsWithAgencyId("0000000", company, "FL");
        PayrollServices.beginUnitOfWork();
        assertEquals("Agency Id custom requirement", 0, Application.find(ACHEnrollment.class).size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addCompanyLawsWithAgencyId("9999999", company, "FL");
        PayrollServices.beginUnitOfWork();
        assertEquals("Agency Id custom requirement", 0, Application.find(ACHEnrollment.class).size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addCompanyLawsWithAgencyId("0999999", company, "FL");
        PayrollServices.beginUnitOfWork();
        assertEquals("Agency Id custom requirement", 0, Application.find(ACHEnrollment.class).size());
        PayrollServices.rollbackUnitOfWork();

        //Updating with Valid agency Id
        DataLoadServices.addCompanyLawsWithAgencyId("2134567", company, "FL");
        PayrollServices.beginUnitOfWork();
        assertEquals("Agency Id custom requirement", 1, Application.find(ACHEnrollment.class).size());
        PayrollServices.rollbackUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");
        PayrollServices.beginUnitOfWork();
        ACHEnrollment achEnrollment = assertOne(Application.find(ACHEnrollment.class));
        assertEquals(ACHEnrollmentStatus.PendingEnrollmentResponse, achEnrollment.getStatus());

        ACHEnrollmentDetail enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
        assertNotNull("ACH Enrollment details", achEnrollment.getACHEnrollmentDetail());
        assertEquals("Company Legal Name included in ADD file", "TEST COMPANY 1", enrollmentDetail.getLegalName());
        assertEquals("Company FEIN included in ADD file", "000000001", enrollmentDetail.getFEIN());
        assertEquals("Company Agency Id included in ADD file", "2134567", enrollmentDetail.getAgencyId());

        assertNotNull("ADD Request file", enrollmentDetail.getRequestFile());
        assertNotNull("ADD Request file name", enrollmentDetail.getRequestFile().getFileName());
        assertEquals("ADD Request file status", ACHEnrollmentFileStatus.Archived, enrollmentDetail.getRequestFile().getStatus());
        assertEquals("ADD Request file type", ACHEnrollmentFileType.Add, enrollmentDetail.getRequestFile().getType());

        assertNull("Response file", enrollmentDetail.getResponseFile());
        PayrollServices.commitUnitOfWork();

        //Upload response file with conformation number
        PayrollServices.beginUnitOfWork();
        String responseFileContent = "Account\tFEIN\t\tTrade Name\t\t\t\tStatus\t\tBegin Date\tEnd Date\tTax Year 2011\t\t\tTax Year 2012\t\t\tTax Year 2013\t\t\t\r\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\r\n" +
                "2134567\t000000001\tWALLACE CORPORATION                \tActive  \t2007-10-01\t9999-12-31\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\n" +
                "9853347\t590874048\tRIVERSIDE ELECTRIC COMPANY         \tActive-Required\t2007-10-01\t9999-12-31\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0000\n";

        ProcessResult<ACHEnrollmentFile> result = PayrollServices.companyManager.uploadACHResponseFile("TestFile1", responseFileContent);
        assertSuccess(result);
        ACHEnrollmentFile responseFile = result.getResult();
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentResponseBatchJob);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("FL Payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());

        Application.refresh(enrollmentDetail);
        Application.refresh(responseFile);

        assertEquals("Response File", responseFile, enrollmentDetail.getResponseFile());
        assertEquals("Response file name", "TestFile1", responseFile.getFileName());
        assertEquals("Response file status", ACHEnrollmentFileStatus.Processed, responseFile.getStatus());
        assertEquals("Response file type", ACHEnrollmentFileType.Response, responseFile.getType());

        assertEquals("ACHEnrollment Status", ACHEnrollmentStatus.Enrolled, enrollmentDetail.getACHEnrollment().getStatus());
        assertNull("ACHEnrollment Confirmation number", enrollmentDetail.getACHEnrollment().getConfirmationNumber());
        assertEquals("ACHEnrollment Status reason", "Found in FL Agent listing file", enrollmentDetail.getACHEnrollment().getStatusReason());

        //Verify events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ACHEnrollmentStatusChanged).sort(CompanyEvent.EventTimeStamp());
        assertEquals("ACHEnrollment status events", 3, companyEvents.size());

        assertNull("First event details - From status", companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst());
        assertEquals("First event details - To status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Second event details - From status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Second event details - To status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Third event details - From status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Third event details - To status", ACHEnrollmentStatus.Enrolled.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testACHEnrollmentAndAutoRegister() {
        String[] states = {"FL"};
        String psid = "11";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.addCompanyLawsWithAgencyId("7654321", company, "FL");

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2013-02-01"), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");

        PayrollServices.beginUnitOfWork();

        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);

        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find());
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());

        ACHEnrollment achEnrollment = assertOne(Application.find(ACHEnrollment.class));
        assertEquals(ACHEnrollmentStatus.PendingEnrollmentResponse, achEnrollment.getStatus());

        ACHEnrollmentDetail enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
        assertNotNull("ACH Enrollment details", achEnrollment.getACHEnrollmentDetail());
        assertEquals("Company Legal Name included in ADD file", "TEST COMPANY 1", enrollmentDetail.getLegalName());
        assertEquals("Company FEIN included in ADD file", "000000001", enrollmentDetail.getFEIN());
        assertEquals("Company Agency Id included in ADD file", "7654321", enrollmentDetail.getAgencyId());

        assertNotNull("ADD Request file", enrollmentDetail.getRequestFile());
        assertNotNull("ADD Request file name", enrollmentDetail.getRequestFile().getFileName());
        assertEquals("ADD Request file status", ACHEnrollmentFileStatus.Archived, enrollmentDetail.getRequestFile().getStatus());
        assertEquals("ADD Request file type", ACHEnrollmentFileType.Add, enrollmentDetail.getRequestFile().getType());

        assertNull("Response file", enrollmentDetail.getResponseFile());
        PayrollServices.commitUnitOfWork();

        //Upload response file with conformation number
        PayrollServices.beginUnitOfWork();
        String responseFileContent = "Account\tFEIN\t\tTrade Name\t\t\t\tStatus\t\tBegin Date\tEnd Date\tTax Year 2011\t\t\tTax Year 2012\t\t\tTax Year 2013\t\t\t\r\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\r\n" +
                "7654321\t000000001\tWALLACE CORPORATION                \tActive  \t2007-10-01\t9999-12-31\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\n" +
                "9853347\t590874048\tRIVERSIDE ELECTRIC COMPANY         \tActive-Required\t2007-10-01\t9999-12-31\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0000\n";

        ProcessResult<ACHEnrollmentFile> result = PayrollServices.companyManager.uploadACHResponseFile("TestFile1", responseFileContent);
        assertSuccess(result);
        ACHEnrollmentFile responseFile = result.getResult();
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentResponseBatchJob);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("FL Payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());

        Application.refresh(enrollmentDetail);
        Application.refresh(responseFile);

        assertEquals("Response File", responseFile, enrollmentDetail.getResponseFile());
        assertEquals("Response file name", "TestFile1", responseFile.getFileName());
        assertEquals("Response file status", ACHEnrollmentFileStatus.Processed, responseFile.getStatus());
        assertEquals("Response file type", ACHEnrollmentFileType.Response, responseFile.getType());

        assertEquals("ACHEnrollment Status", ACHEnrollmentStatus.Enrolled, enrollmentDetail.getACHEnrollment().getStatus());
        assertNull("ACHEnrollment Confirmation number", enrollmentDetail.getACHEnrollment().getConfirmationNumber());
        assertEquals("ACHEnrollment Status reason", "Found in FL Agent listing file", enrollmentDetail.getACHEnrollment().getStatusReason());

        //Verify events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ACHEnrollmentStatusChanged).sort(CompanyEvent.EventTimeStamp());
        assertEquals("ACHEnrollment status events", 3, companyEvents.size());

        assertNull("First event details - From status", companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst());
        assertEquals("First event details - To status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Second event details - From status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Second event details - To status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Third event details - From status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Third event details - To status", ACHEnrollmentStatus.Enrolled.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testACHEnrollmentAddAndDelete() {
        String[] states = {"FL"};
        Company company = assertOne(DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.addCompanyLawsWithAgencyId("7654321", company, "FL");

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2013-02-01"), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find());
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());

        ACHEnrollment achEnrollment = assertOne(Application.find(ACHEnrollment.class));
        assertEquals(ACHEnrollmentStatus.PendingEnrollmentResponse, achEnrollment.getStatus());

        ACHEnrollmentDetail enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
        assertNotNull("ACH Enrollment details", achEnrollment.getACHEnrollmentDetail());
        assertEquals("Company Legal Name included in ADD file", "TEST COMPANY 1", enrollmentDetail.getLegalName());
        assertEquals("Company FEIN included in ADD file", "000000001", enrollmentDetail.getFEIN());
        assertEquals("Company Agency Id included in ADD file", "7654321", enrollmentDetail.getAgencyId());

        assertNotNull("ADD Request file", enrollmentDetail.getRequestFile());
        assertNotNull("ADD Request file name", enrollmentDetail.getRequestFile().getFileName());
        assertEquals("ADD Request file status", ACHEnrollmentFileStatus.Archived, enrollmentDetail.getRequestFile().getStatus());
        assertEquals("ADD Request file type", ACHEnrollmentFileType.Add, enrollmentDetail.getRequestFile().getType());

        assertNull("Response file", enrollmentDetail.getResponseFile());
        PayrollServices.commitUnitOfWork();

        //Upload response file with conformation number
        PayrollServices.beginUnitOfWork();
        String responseFileContent = "Account\tFEIN\t\tTrade Name\t\t\t\tStatus\t\tBegin Date\tEnd Date\tTax Year 2011\t\t\tTax Year 2012\t\t\tTax Year 2013\t\t\t\r\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\r\n" +
                "7654321\t000000001\tWALLACE CORPORATION                \tActive  \t2007-10-01\t9999-12-31\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\n" +
                "9853347\t590874048\tRIVERSIDE ELECTRIC COMPANY         \tActive-Required\t2007-10-01\t9999-12-31\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0000\n";

        ProcessResult<ACHEnrollmentFile> result = PayrollServices.companyManager.uploadACHResponseFile("TestFile1", responseFileContent);
        assertSuccess(result);
        ACHEnrollmentFile responseFile = result.getResult();
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentResponseBatchJob);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("FL Payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());

        Application.refresh(enrollmentDetail);
        Application.refresh(responseFile);

        assertEquals("Response File", responseFile, enrollmentDetail.getResponseFile());
        assertEquals("Response file name", "TestFile1", responseFile.getFileName());
        assertEquals("Response file status", ACHEnrollmentFileStatus.Processed, responseFile.getStatus());
        assertEquals("Response file type", ACHEnrollmentFileType.Response, responseFile.getType());

        assertEquals("ACHEnrollment Status", ACHEnrollmentStatus.Enrolled, enrollmentDetail.getACHEnrollment().getStatus());
        assertNull("ACHEnrollment Confirmation number", enrollmentDetail.getACHEnrollment().getConfirmationNumber());
        assertEquals("ACHEnrollment Status reason", "Found in FL Agent listing file", enrollmentDetail.getACHEnrollment().getStatusReason());

        //Verify events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ACHEnrollmentStatusChanged).sort(CompanyEvent.EventTimeStamp());
        assertEquals("ACHEnrollment status events", 3, companyEvents.size());

        assertNull("First event details - From status", companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst());
        assertEquals("First event details - To status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Second event details - From status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Second event details - To status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Third event details - From status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Third event details - To status", ACHEnrollmentStatus.Enrolled.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.deleteACHEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHDeEnrollmentBatchJob, "20121231");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ACHEnrollment> achEnrollments = Application.find(ACHEnrollment.class);
        assertEquals("ACHEnrollments", 2, achEnrollments.size());
        achEnrollment = achEnrollments.findEntity(ACHEnrollment.Status().equalTo(ACHEnrollmentStatus.Deleted));

        enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
        assertNotNull("ACH Enrollment details", achEnrollment.getACHEnrollmentDetail());
        assertEquals("Company Legal Name included in ADD file", "TEST COMPANY 1", enrollmentDetail.getLegalName());
        assertEquals("Company FEIN included in ADD file", "000000001", enrollmentDetail.getFEIN());
        assertEquals("Company Agency Id included in ADD file", "7654321", enrollmentDetail.getAgencyId());

        assertNull("Response file", enrollmentDetail.getResponseFile());
        assertNotNull("ADD Request file", enrollmentDetail.getRequestFile());
        assertNotNull("ADD Request file name", enrollmentDetail.getRequestFile().getFileName());
        assertEquals("ADD Request file status", ACHEnrollmentFileStatus.Archived, enrollmentDetail.getRequestFile().getStatus());
        assertEquals("ADD Request file type", ACHEnrollmentFileType.Delete, enrollmentDetail.getRequestFile().getType());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testACHEnrollmentAddAndDeleteWithInvalidAIDs() {
        String[] states = {"FL"};
        Company company = assertOne(DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.addCompanyLawsWithAgencyId("7654321", company, "FL");

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2013-02-01"), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        DataLoadServices.addCompanyLawsWithAgencyId("EXEMPT", company, "FL");
        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");

        ACHEnrollment achEnrollment = assertOne(Application.find(ACHEnrollment.class));
        assertEquals("AchEnrollment Sent.",ACHEnrollmentStatus.PendingEnrollment, achEnrollment.getStatus());//Since AID is invalid , enrollment not sent.

        DataLoadServices.addCompanyLawsWithAgencyId("7654321 ", company, "FL");
        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");

         achEnrollment = assertOne(Application.find(ACHEnrollment.class));
        assertEquals("AchEnrollment Sent.",ACHEnrollmentStatus.PendingEnrollment, achEnrollment.getStatus());//Since AID is invalid , enrollment not sent.


        //Check AID with space at the end
        DataLoadServices.addCompanyLawsWithAgencyId("7654321", company, "FL");     // In this case AID will trimmed and sent for enrollment
        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");


        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find());
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());

         achEnrollment = assertOne(Application.find(ACHEnrollment.class));
        assertEquals(ACHEnrollmentStatus.PendingEnrollmentResponse, achEnrollment.getStatus());

        ACHEnrollmentDetail enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
        assertNotNull("ACH Enrollment details", achEnrollment.getACHEnrollmentDetail());
        assertEquals("Company Legal Name included in ADD file", "TEST COMPANY 1", enrollmentDetail.getLegalName());
        assertEquals("Company FEIN included in ADD file", "000000001", enrollmentDetail.getFEIN());
        assertEquals("Company Agency Id included in ADD file", "7654321", enrollmentDetail.getAgencyId());

        assertNotNull("ADD Request file", enrollmentDetail.getRequestFile());
        assertNotNull("ADD Request file name", enrollmentDetail.getRequestFile().getFileName());
        assertEquals("ADD Request file status", ACHEnrollmentFileStatus.Archived, enrollmentDetail.getRequestFile().getStatus());
        assertEquals("ADD Request file type", ACHEnrollmentFileType.Add, enrollmentDetail.getRequestFile().getType());

        assertNull("Response file", enrollmentDetail.getResponseFile());
        PayrollServices.commitUnitOfWork();

        //Upload response file with conformation number
        PayrollServices.beginUnitOfWork();
        String responseFileContent = "Account\tFEIN\t\tTrade Name\t\t\t\tStatus\t\tBegin Date\tEnd Date\tTax Year 2011\t\t\tTax Year 2012\t\t\tTax Year 2013\t\t\t\r\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\r\n" +
                "7654321\t000000001\tWALLACE CORPORATION                \tActive  \t2007-10-01\t9999-12-31\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\n" +
                "9853347\t590874048\tRIVERSIDE ELECTRIC COMPANY         \tActive-Required\t2007-10-01\t9999-12-31\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0000\n";

        ProcessResult<ACHEnrollmentFile> result = PayrollServices.companyManager.uploadACHResponseFile("TestFile1", responseFileContent);
        assertSuccess(result);
        ACHEnrollmentFile responseFile = result.getResult();
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentResponseBatchJob);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("FL Payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());

        Application.refresh(enrollmentDetail);
        Application.refresh(responseFile);

        assertEquals("Response File", responseFile, enrollmentDetail.getResponseFile());
        assertEquals("Response file name", "TestFile1", responseFile.getFileName());
        assertEquals("Response file status", ACHEnrollmentFileStatus.Processed, responseFile.getStatus());
        assertEquals("Response file type", ACHEnrollmentFileType.Response, responseFile.getType());

        assertEquals("ACHEnrollment Status", ACHEnrollmentStatus.Enrolled, enrollmentDetail.getACHEnrollment().getStatus());
        assertNull("ACHEnrollment Confirmation number", enrollmentDetail.getACHEnrollment().getConfirmationNumber());
        assertEquals("ACHEnrollment Status reason", "Found in FL Agent listing file", enrollmentDetail.getACHEnrollment().getStatusReason());

        //Verify events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ACHEnrollmentStatusChanged).sort(CompanyEvent.EventTimeStamp());
        assertEquals("ACHEnrollment status events", 3, companyEvents.size());

        assertNull("First event details - From status", companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst());
        assertEquals("First event details - To status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Second event details - From status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Second event details - To status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Third event details - From status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Third event details - To status", ACHEnrollmentStatus.Enrolled.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.addCompanyLawsWithAgencyId("7654321 ", company, "FL");
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.deleteACHEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHDeEnrollmentBatchJob, "20121231");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ACHEnrollment> achEnrollments = Application.find(ACHEnrollment.class);
        assertEquals("ACHEnrollments", 2, achEnrollments.size());
        achEnrollment = achEnrollments.findEntity(ACHEnrollment.Status().equalTo(ACHEnrollmentStatus.PendingDelete));//Not sent in Delete ach enrollmentas AID is EXEMPT(invalid)
        assertNotNull("ACHEnrollment PendingDelete", achEnrollment);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addCompanyLawsWithAgencyId("7654321", company, "FL"); //whenever you edit AID, you need to re-enroll.
        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");

        //Upload response file with conformation number
        PayrollServices.beginUnitOfWork();
         responseFileContent = "Account\tFEIN\t\tTrade Name\t\t\t\tStatus\t\tBegin Date\tEnd Date\tTax Year 2011\t\t\tTax Year 2012\t\t\tTax Year 2013\t\t\t\r\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\r\n" +
                "7654321\t000000001\tWALLACE CORPORATION                \tActive  \t2007-10-01\t9999-12-31\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\n" +
                "9853347\t590874048\tRIVERSIDE ELECTRIC COMPANY         \tActive-Required\t2007-10-01\t9999-12-31\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0000\n";

        result = PayrollServices.companyManager.uploadACHResponseFile("TestFile1", responseFileContent);
        assertSuccess(result);
         responseFile = result.getResult();
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentResponseBatchJob);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.deleteACHEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI));
        PayrollServices.commitUnitOfWork();
        BatchJobManager.runJob(BatchJobType.ACHDeEnrollmentBatchJob, "20121231");

        PayrollServices.beginUnitOfWork();
         achEnrollments = Application.find(ACHEnrollment.class);
        assertEquals("ACHEnrollments", 4, achEnrollments.size());
        achEnrollment = achEnrollments.findEntity(ACHEnrollment.Status().equalTo(ACHEnrollmentStatus.Deleted));

        enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
        assertNotNull("ACH Enrollment details", achEnrollment.getACHEnrollmentDetail());
        assertEquals("Company Legal Name included in ADD file", "TEST COMPANY 1", enrollmentDetail.getLegalName());
        assertEquals("Company FEIN included in ADD file", "000000001", enrollmentDetail.getFEIN());
        assertEquals("Company Agency Id included in ADD file", "7654321", enrollmentDetail.getAgencyId());

        assertNull("Response file", enrollmentDetail.getResponseFile());
        assertNotNull("ADD Request file", enrollmentDetail.getRequestFile());
        assertNotNull("ADD Request file name", enrollmentDetail.getRequestFile().getFileName());
        assertEquals("ADD Request file status", ACHEnrollmentFileStatus.Archived, enrollmentDetail.getRequestFile().getStatus());
        assertEquals("ADD Request file type", ACHEnrollmentFileType.Delete, enrollmentDetail.getRequestFile().getType());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testACHEnrollmentAddRejected() {
        String[] states = {"FL"};
        Company company = assertOne(DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.addCompanyLawsWithAgencyId("7654321", company, "FL");

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2013-02-01"), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar today = PSPDate.getPSPTime();
        PayrollServices.rollbackUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, CalendarUtils.getFirstDayOfQuarter(today).format(BatchUtils.DATE_FORMAT));

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find());
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());

        ACHEnrollment achEnrollment = assertOne(Application.find(ACHEnrollment.class));
        assertEquals(ACHEnrollmentStatus.PendingEnrollmentResponse, achEnrollment.getStatus());

        ACHEnrollmentDetail enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
        assertNotNull("ACH Enrollment details", achEnrollment.getACHEnrollmentDetail());
        assertEquals("Company Legal Name included in ADD file", "TEST COMPANY 1", enrollmentDetail.getLegalName());
        assertEquals("Company FEIN included in ADD file", "000000001", enrollmentDetail.getFEIN());
        assertEquals("Company Agency Id included in ADD file", "7654321", enrollmentDetail.getAgencyId());

        assertNotNull("ADD Request file", enrollmentDetail.getRequestFile());
        assertNotNull("ADD Request file name", enrollmentDetail.getRequestFile().getFileName());
        assertEquals("ADD Request file status", ACHEnrollmentFileStatus.Archived, enrollmentDetail.getRequestFile().getStatus());
        assertEquals("ADD Request file type", ACHEnrollmentFileType.Add, enrollmentDetail.getRequestFile().getType());
        PayrollServices.commitUnitOfWork();

        //Upload response file with Rejection
        PayrollServices.beginUnitOfWork();
        String responseFileContent = "Account\tFEIN\t\tTrade Name\t\t\t\tStatus\t\tBegin Date\tEnd Date\tTax Year 2011\t\t\tTax Year 2012\t\t\tTax Year 2013\t\t\t\r\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\r\n" +
                "7654421\t000000001\tWALLACE CORPORATION                \tActive  \t2007-10-01\t9999-12-31\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\n" +
                "9853347\t590874048\tRIVERSIDE ELECTRIC COMPANY         \tActive-Required\t2007-10-01\t9999-12-31\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0000\n";

        ProcessResult<ACHEnrollmentFile> result = PayrollServices.companyManager.uploadACHResponseFile("TestFile2", responseFileContent);
        assertSuccess(result);
        ACHEnrollmentFile responseFile = result.getResult();
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentResponseBatchJob);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());

        Application.refresh(enrollmentDetail);
        Application.refresh(responseFile);

        assertNull("Response File", enrollmentDetail.getResponseFile());
        assertEquals("Response file name", "TestFile2", responseFile.getFileName());
        assertEquals("Response file status", ACHEnrollmentFileStatus.Processed, responseFile.getStatus());
        assertEquals("Response file type", ACHEnrollmentFileType.Response, responseFile.getType());

        assertEquals("ACHEnrollment Status", ACHEnrollmentStatus.EnrollmentRejected, enrollmentDetail.getACHEnrollment().getStatus());
        assertNull("ACHEnrollment Confirmation number", enrollmentDetail.getACHEnrollment().getConfirmationNumber());
        assertEquals("ACHEnrollment Status reason", "Not found in FL Agent listing file", enrollmentDetail.getACHEnrollment().getStatusReason());

        //Verify events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ACHEnrollmentStatusChanged).sort(CompanyEvent.EventTimeStamp());
        assertEquals("ACHEnrollment status events", 3, companyEvents.size());

        assertNull("First event details - From status", companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst());
        assertEquals("First event details - To status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Second event details - From status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Second event details - To status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Third event details - From status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Third event details - To status", ACHEnrollmentStatus.EnrollmentRejected.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testACHEnrollmentAddWith7DigitAgencyId() {
        String[] states = {"FL"};
        Company company = assertOne(DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2013-02-01"), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        //Updating FL-UCT6-PAYMENT agency Id with 7 digit agency Id.
        DataLoadServices.updateAgencyTaxpayerId(company, PaymentTemplate.FL_SUI, "2234567");

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find());
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());

        ACHEnrollment achEnrollment = assertOne(Application.find(ACHEnrollment.class));
        assertEquals(ACHEnrollmentStatus.PendingEnrollmentResponse, achEnrollment.getStatus());

        ACHEnrollmentDetail enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
        assertNotNull("ACH Enrollment details", achEnrollment.getACHEnrollmentDetail());
        assertEquals("Company Legal Name included in ADD file", "TEST COMPANY 1", enrollmentDetail.getLegalName());
        assertEquals("Company FEIN included in ADD file", "000000001", enrollmentDetail.getFEIN());
        assertEquals("Company Agency Id included in ADD file", "2234567", enrollmentDetail.getAgencyId());

        assertNotNull("ADD Request file", enrollmentDetail.getRequestFile());
        assertNotNull("ADD Request file name", enrollmentDetail.getRequestFile().getFileName());
        assertEquals("ADD Request file status", ACHEnrollmentFileStatus.Archived, enrollmentDetail.getRequestFile().getStatus());
        assertEquals("ADD Request file type", ACHEnrollmentFileType.Add, enrollmentDetail.getRequestFile().getType());

        assertNull("Response file", enrollmentDetail.getResponseFile());
        PayrollServices.commitUnitOfWork();

        //Upload response file with conformation number
        PayrollServices.beginUnitOfWork();
        String responseFileContent = "Account\tFEIN\t\tTrade Name\t\t\t\tStatus\t\tBegin Date\tEnd Date\tTax Year 2011\t\t\tTax Year 2012\t\t\tTax Year 2013\t\t\t\r\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\tQtr1\tQtr2\tQtr3\tQtr4\r\n" +
                "2234567\t000000001\tWALLACE CORPORATION                \tActive  \t2007-10-01\t9999-12-31\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\t.0000\n" +
                "9853347\t590874048\tRIVERSIDE ELECTRIC COMPANY         \tActive-Required\t2007-10-01\t9999-12-31\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0540\t.0000\n";

        ProcessResult<ACHEnrollmentFile> result = PayrollServices.companyManager.uploadACHResponseFile("TestFile1", responseFileContent);
        assertSuccess(result);
        ACHEnrollmentFile responseFile = result.getResult();
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentResponseBatchJob);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("FL Payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());

        Application.refresh(enrollmentDetail);
        Application.refresh(responseFile);

        assertEquals("Response File", responseFile, enrollmentDetail.getResponseFile());
        assertEquals("Response file name", "TestFile1", responseFile.getFileName());
        assertEquals("Response file status", ACHEnrollmentFileStatus.Processed, responseFile.getStatus());
        assertEquals("Response file type", ACHEnrollmentFileType.Response, responseFile.getType());

        assertEquals("ACHEnrollment Status", ACHEnrollmentStatus.Enrolled, enrollmentDetail.getACHEnrollment().getStatus());
        assertNull("ACHEnrollment Confirmation number", enrollmentDetail.getACHEnrollment().getConfirmationNumber());
        assertEquals("ACHEnrollment Status reason", "Found in FL Agent listing file", enrollmentDetail.getACHEnrollment().getStatusReason());
        PayrollServices.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void testACHEnrollmentInvalidContents() {
        String[] states = {"FL"};
        Company company = assertOne(DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2013-02-01"), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        //Updating FL-UCT6-PAYMENT agency Id with 7 digit agency Id.
        DataLoadServices.updateAgencyTaxpayerId(company, PaymentTemplate.FL_SUI, "1234567");

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob);

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find());
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());

        ACHEnrollment achEnrollment = assertOne(Application.find(ACHEnrollment.class));
        assertEquals(ACHEnrollmentStatus.PendingEnrollmentResponse, achEnrollment.getStatus());

        ACHEnrollmentDetail enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
        assertNotNull("ACH Enrollment details", achEnrollment.getACHEnrollmentDetail());
        assertEquals("Company Legal Name included in ADD file", "TEST COMPANY 1", enrollmentDetail.getLegalName());
        assertEquals("Company FEIN included in ADD file", "000000001", enrollmentDetail.getFEIN());
        assertEquals("Company Agency Id included in ADD file", "01234567", enrollmentDetail.getAgencyId());

        assertNotNull("ADD Request file", enrollmentDetail.getRequestFile());
        assertNotNull("ADD Request file name", enrollmentDetail.getRequestFile().getFileName());
        assertEquals("ADD Request file status", ACHEnrollmentFileStatus.Archived, enrollmentDetail.getRequestFile().getStatus());
        assertEquals("ADD Request file type", ACHEnrollmentFileType.Add, enrollmentDetail.getRequestFile().getType());

        assertNull("Response file", enrollmentDetail.getResponseFile());
        PayrollServices.commitUnitOfWork();

        //Upload response file with conformation number
        PayrollServices.beginUnitOfWork();
        String responseFileContent = "\f\n" +
                "RUN DATE: 02/04/2013                           STATE OF FLORIDA DEPARTMENT OF REVENUE                                  SUNTAX REPORT\n" +
                "FILE NAME: ut001.rep                        DIVISION OF INFORMATION SYSTEMS AND SERVICES                               PAGE: 1\n" +
                "RUNID: UT001                                    REPORT OF CONFIRMATION NUMBERS ASSIGNED                                 \n" +
                "                                                        COMPANY NAME: COMPUTING RESOURCES, INC.               \n" +
                " \n" +
                "ACCOUNT   FEI        CONFIRMATION NUMBER\n" +
                "--------  ---------  --------------------------------------------------------------------------------------------------------------\n" +
                " \n" +
                "AA01234567  000000001  Confirmation number assigned: 99002494412\n" +
                "09853347  590874048  Confirmation number assigned: 99002494592\n" +
                " \n" +
                " \n" +
                "Total confirmation numbers assigned: 171 in 190 agent and emp record(s) read.\n";

        ProcessResult<ACHEnrollmentFile> result = PayrollServices.companyManager.uploadACHResponseFile("TestFile1", responseFileContent);
        assertSuccess(result);
        ACHEnrollmentFile responseFile = result.getResult();
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentResponseBatchJob);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("FL Payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());

        Application.refresh(enrollmentDetail);
        Application.refresh(responseFile);

        assertEquals("ACHEnrollment Status", ACHEnrollmentStatus.PendingEnrollmentResponse, enrollmentDetail.getACHEnrollment().getStatus());
    }

}


