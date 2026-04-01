package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * User: ihannur
 * Date: 2/14/13
 * Time: 10:12 AM
 */
public class UpdateACHEnrollmentStatusCoreTests {

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
    public void testValidations() {
        String[] states = {"FL"};
        Company company = assertOne(DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.SUI));

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.NY_WH, ACHEnrollmentStatus.Enrolled);
        assertEquals("Error messages", 1, result.getMessages().size());
        assertEquals("Error message code", "10113", result.getMessages().get(0).getMessageCode());
        assertEquals("Error message", "ACH Enrollment is not applicable for Payment Template: NY-1MN-PAYMENT.", result.getMessages().get(0).getMessage());
        PayrollServices.rollbackUnitOfWork();

        //Delete ACHEnrollment record to validate for error - 10114
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        ACHEnrollment achEnrollment = company.getCurrentACHEnrollment();
        Application.delete(achEnrollment);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.Enrolled);
        assertEquals("Error messages", 1, result.getMessages().size());
        assertEquals("Error message code", "10114", result.getMessages().get(0).getMessageCode());
        assertEquals("Error message", "ACH Enrollment does not exist on Company: QBDT:1 and Payment Template: FL-UCT6-PAYMENT.", result.getMessages().get(0).getMessage());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testUpdateToEnrolledFromPendingEnrollment() {
        String[] states = {"FL"};
        Company company = assertOne(DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.SUI));

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.Enrolled));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        ACHEnrollment achEnrollment = assertOne(company.getAllACHEnrollments());
        assertEquals("Current ACHEnrollment status", ACHEnrollmentStatus.Enrolled, achEnrollment.getStatus());

        //Verify events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ACHEnrollmentStatusChanged).sort(CompanyEvent.EventTimeStamp());
        assertEquals("ACHEnrollment status events", 2, companyEvents.size());

        assertNull("First event details - From status", companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst());
        assertEquals("First event details - To status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Second event details - From status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Second event details - To status", ACHEnrollmentStatus.Enrolled.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testHappyPath() {
        String[] states = {"FL"};
        Company company = assertOne(DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2013-02-01"), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");

        //Calling directly status update process to test for status update.
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.EnrollmentRejected);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 23);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        ACHEnrollment achEnrollment = assertOne(company.getAllACHEnrollments());
        assertEquals("Current ACHEnrollment status", ACHEnrollmentStatus.EnrollmentRejected, achEnrollment.getStatus());

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

        DataLoadServices.setPSPDate(2013, 1, 24);

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.PendingEnrollment);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        DomainEntitySet<ACHEnrollment> achEnrollments = Application.find(ACHEnrollment.class, ACHEnrollment.CompanyAgency().Company().equalTo(company))
                                                                   .sort(ACHEnrollment.StatusEffectiveDate().Descending(),
                                                                                ACHEnrollment.Status().Descending()); // Adding status in sorting to avoid failures, if both records have same time stamp
        assertEquals("Current ACHEnrollment status", ACHEnrollmentStatus.PendingEnrollment, achEnrollments.get(0).getStatus());
        assertEquals("Current ACHEnrollment status", ACHEnrollmentStatus.PendingEnrollment, company.getCurrentACHEnrollment().getStatus());

        assertEquals("EnrollmentRejected ACHEnrollment status updated to Cancelled", ACHEnrollmentStatus.Cancelled, achEnrollments.get(1).getStatus());

        companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ACHEnrollmentStatusChanged)
                                                            .sort(CompanyEvent.EventTimeStamp());
        assertEquals("ACHEnrollment status events", 5, companyEvents.size());
        companyEvents = CompanyEvent.findCompanyEventWithDetailsEagerLoaded(company, EventTypeCode.ACHEnrollmentStatusChanged, EventDetailTypeCode.ACHEnrollmentId, achEnrollments.get(1).getId().toString());
        assertEquals("ACHEnrollment status events, with Details of old ACHEnrollment record", 4, companyEvents.size());

        assertNull("First event details - From status", companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst());
        assertEquals("First event details - To status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());
        assertEquals("First event details - ACHEnrollmentId", achEnrollments.get(1).getId().toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.ACHEnrollmentId).getFirst().getValue());

        assertEquals("Second event details - From status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Second event details - To status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());
        assertEquals("Second event details - ACHEnrollmentId", achEnrollments.get(1).getId().toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.ACHEnrollmentId).getFirst().getValue());

        assertEquals("Third event details - From status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Third event details - To status", ACHEnrollmentStatus.EnrollmentRejected.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());
        assertEquals("Third event details - ACHEnrollmentId", achEnrollments.get(1).getId().toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.ACHEnrollmentId).getFirst().getValue());

        assertEquals("Fourth event details - From status", ACHEnrollmentStatus.EnrollmentRejected.toString(), companyEvents.get(3).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Fourth event details - To status", ACHEnrollmentStatus.Cancelled.toString(), companyEvents.get(3).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());
        assertEquals("Fourth event details - ACHEnrollmentId", achEnrollments.get(1).getId().toString(), companyEvents.get(3).getCompanyEventDetails(EventDetailTypeCode.ACHEnrollmentId).getFirst().getValue());

        companyEvents = CompanyEvent.findCompanyEventWithDetailsEagerLoaded(company, EventTypeCode.ACHEnrollmentStatusChanged, EventDetailTypeCode.ACHEnrollmentId, achEnrollments.get(0).getId().toString());
        assertEquals("ACHEnrollment status events, with Details of new ACHEnrollment record", 1, companyEvents.size());

        assertEquals("Fifth event details - From status", ACHEnrollmentStatus.Cancelled.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Fifth event details - To status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());
        // This event will have current ACHEnrollment Id.
        assertEquals("Fifth event details - ACHEnrollmentId", achEnrollments.get(0).getId().toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.ACHEnrollmentId).getFirst().getValue());
        PayrollServices.rollbackUnitOfWork();

    }

}
