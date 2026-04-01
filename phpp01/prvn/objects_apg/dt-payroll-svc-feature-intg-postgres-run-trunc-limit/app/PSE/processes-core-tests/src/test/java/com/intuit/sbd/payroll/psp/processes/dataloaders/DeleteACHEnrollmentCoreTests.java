package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
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
 * Time: 11:15 AM
 */
public class DeleteACHEnrollmentCoreTests {

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
    public void testValidationAndHappyPath() {
        String[] states = {"FL"};
        Company company = assertOne(DataLoadServices.setupCompany(1l, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2013, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2013-02-01"), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, "20130101");

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.deleteACHEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI);
        assertEquals("Error messages", 1, result.getMessages().size());
        assertEquals("Error message code", "10116", result.getMessages().get(0).getMessageCode());
        assertEquals("Error message", "ACH Enrollment status is not currently Enrolled to send Delete request for Company: QBDT:1 and Payment Template: FL-UCT6-PAYMENT.", result.getMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        result = PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.Enrolled);
        assertSuccess(result);
        assertEquals("Current ACHEnrollment status", ACHEnrollmentStatus.Enrolled, company.getCurrentACHEnrollmentStatus());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.deleteACHEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertEquals("Number of ACHEnrollment records", 2, company.getAllACHEnrollments().size());
        assertEquals("Current ACHEnrollment status", ACHEnrollmentStatus.PendingDelete, company.getCurrentACHEnrollmentStatus());
        assertEquals("Latest ACHEnrollment status", ACHEnrollmentStatus.PendingDelete, company.getAllACHEnrollments().get(0).getStatus());
        assertEquals("Earlier ACHEnrollment status", ACHEnrollmentStatus.Enrolled, company.getAllACHEnrollments().get(1).getStatus());

        //Verify events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ACHEnrollmentStatusChanged).sort(CompanyEvent.EventTimeStamp());
        assertEquals("ACHEnrollment status events", 4, companyEvents.size());

        assertNull("First event details - From status", companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst());
        assertEquals("First event details - To status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Second event details - From status", ACHEnrollmentStatus.PendingEnrollment.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Second event details - To status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(1).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertEquals("Third event details - From status", ACHEnrollmentStatus.PendingEnrollmentResponse.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst().getValue());
        assertEquals("Third event details - To status", ACHEnrollmentStatus.Enrolled.toString(), companyEvents.get(2).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());

        assertNull("Fourth event details - From status", companyEvents.get(3).getCompanyEventDetails(EventDetailTypeCode.OldStringValue).getFirst());
        assertEquals("Fourth event details - To status", ACHEnrollmentStatus.PendingDelete.toString(), companyEvents.get(3).getCompanyEventDetails(EventDetailTypeCode.NewStringValue).getFirst().getValue());
        PayrollServices.rollbackUnitOfWork();

    }

}
