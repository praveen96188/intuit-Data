package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.raf.RAFFileWriter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.junit.*;

import static org.junit.Assert.assertTrue;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 9, 2009
 * Time: 10:59:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateEnrollmentStatusCoreTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2009, 4, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testRejectHappyPath() {
        rejectRAFEnrollment("123456789");
    }

    @Test
    public void updateRAFEnrollmentStatusTestHappyPath() {
        String psid = "123456789";
        createRAFChangeToPendingEnrollmentTape(psid);

        // Check events
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventDetailTypeCode.EnrollmentType, "RAFEnrollment");

        //Assertion for EnrollmentType - RAFEnrollment events
        assertEquals("Company Events", 2, companyEventsList.size());

        CompanyEvent enrollmentEvent = companyEventsList.get(1);

        assertEquals("Agency Id", Agency.IRS,
                enrollmentEvent.getCompanyEventDetailValue(EventDetailTypeCode.AgencyId));

        assertEquals("Old String Value", EnumUtils.getReadableName(RAFEnrollmentStatus.PendingEnrollment),
                enrollmentEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldStringValue));

        assertEquals("New String Value", EnumUtils.getReadableName(RAFEnrollmentStatus.PendingEnrollmentTape),
                enrollmentEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue));

        PayrollServices.commitUnitOfWork();
    }

    public void createRAFChangeToPendingEnrollmentTape(String psid) {

        DataLoadServices.setupCompany(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingEnrollmentTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        //
        assertTrue(RAFEnrollmentStatus.PendingEnrollmentTape == getRAFEnrollment(company).getStatus());
    }

//�	Scenario: An agent attempts to move a RAF enrollment to Enrolled for an enrollment that is not in PendingEnrollmentResponse status
//o	Expected results
//?	Validation error: cannot move a RAF re-enrollment to enrolled when its status is: x
    @Test
    public void testRAFEnrollmentToEnrolledInvalidStatus() {
        String psid = "123456789";
        createRAFChangeToPendingEnrollmentTape(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();
        assertFalse("Update enrollment status result", result.isSuccess());
        assertEquals("One error message", 1, result.getErrorMessages().size());
        assertEquals("Error message text: ", "Enrollment 'RAFEnrollment' does not allow a transition from 'PendingEnrollmentTape' to 'Enrolled'.", result.getErrorMessages().get(0).getMessage());
    }

//�	Scenario: An agent manually initiates deletion for a terminated or cancelled company with an enrollment in a status other than Enrolled status
//o	Expected results
//?	Validation error: cannot delete a RAF enrollment that is in a status other than Enrolled
    @Test
    public void testRAFEnrollmentToDeletedInvalidStatus() {
        String psid = "123456789";
        createRAFChangeToPendingEnrollmentTape(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingDeleteTape);
        PayrollServices.commitUnitOfWork();
        assertFalse("Update enrollment status result", result.isSuccess());
        assertEquals("One error message", 1, result.getErrorMessages().size());
        assertEquals("Error message text: ", "Enrollment 'RAFEnrollment' does not allow a transition from 'PendingEnrollmentTape' to 'PendingDeleteTape'.", result.getErrorMessages().get(0).getMessage());
    }    
    

    @Test
    public void updateRAFEnrollmentStatusByChangingEIN() {
        String psid = "123456789";

        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingEnrollmentTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);


        PayrollServices.beginUnitOfWork();
        //      Simulate running this process:  PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.Add);  Don't run the actual process since that will try to schedule the job in flux
        RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafWriter = new RAFFileWriter();
        rafWriter.execute();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);


        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("876666666");
        assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();

        assertEquals("RAF Enrollment Status", RAFEnrollmentStatus.PendingEnrollment, getRAFEnrollment(company).getStatus());

        DomainEntitySet<RAFEnrollment> allEnrollments = company.getAllRAFEnrollments();
        assertEquals("Number of RAF enrollments", 2, allEnrollments.size());
        RAFEnrollment oldEnrollment = allEnrollments.get(1);
        RAFEnrollment newEnrollment = allEnrollments.get(0);
        assertEquals("Old RAF Enrollment Status", RAFEnrollmentStatus.Cancelled, oldEnrollment.getStatus());
        //Make sure the old enrollment is the one that still has the detail, file, etc. associated with it.  make sure the agency is still associated
        assertNotNull(oldEnrollment.getCompanyAgency());
        PayrollServices.beginUnitOfWork();
        oldEnrollment = Application.refresh(oldEnrollment);
        RAFEnrollmentDetail rafEnrollmentDetail = oldEnrollment.getRAFEnrollmentDetail();
        PayrollServices.commitUnitOfWork();

        assertNotNull(rafEnrollmentDetail);
        assertNotNull(rafEnrollmentDetail.getEnrollmentFile());
        assertEquals("New RAF Enrollment Status", RAFEnrollmentStatus.PendingEnrollment, newEnrollment.getStatus());
    }

    @Test
    public void changeEINRAFNotEnrolled() {
        String psid = "123456789";

        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingEnrollmentTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("876666666");
        assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();

        assertEquals("RAF Enrollment Status", RAFEnrollmentStatus.PendingEnrollmentTape, getRAFEnrollment(company).getStatus());

        DomainEntitySet<RAFEnrollment> allEnrollments = company.getAllRAFEnrollments();
        assertEquals("Number of RAF enrollments", 1, allEnrollments.size());
    }

    public void rejectRAFEnrollment(String psid) {
        sendRAFToTape(psid);



        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        String rejectReason = "Rejected by agency";
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.rejectRAFEnrollment(company.getCurrentRAFEnrollment(),
                rejectReason);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        RAFEnrollment rafEnrollment = company.getCurrentRAFEnrollment();
        assertEquals("RAF status", RAFEnrollmentStatus.Rejected, rafEnrollment.getStatus());
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,EventDetailTypeCode.Details,rejectReason);
        assertEquals(rejectReason + " Events", 1, companyEvents.size());

        assertEquals("Reject notes", rejectReason, rafEnrollment.getStatusReason());
    }

    @Test
    public void rejectRAFEnrollment_CancelledCompany() {
        String psid="238548544";
        sendRAFToTape(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        //Cancel all the company's payroll runs
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        for (PayrollRun payrollRun : payrollRuns) {
            TransactionCancelEEDTO txRecallDTO = new TransactionCancelEEDTO();
            txRecallDTO.setTransmissionId(null);
            txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
            ProcessResult recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
            assertSuccess(recallPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult<CompanyService> cancelServiceCore2 = PayrollServices.companyManager.deactivateService(
                company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();
        assertSuccess(cancelServiceCore2);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        String rejectReason = "Rejected by agency";
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.rejectRAFEnrollment(company.getCurrentRAFEnrollment(),
                rejectReason);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        RAFEnrollment rafEnrollment = company.getCurrentRAFEnrollment();
        assertEquals("RAF status", RAFEnrollmentStatus.Cancelled, rafEnrollment.getStatus());
        assertEquals("Reject notes", rejectReason, rafEnrollment.getStatusReason());
    }

    public void sendRAFToTape(String psid) {
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingEnrollmentTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);


        PayrollServices.beginUnitOfWork();
        //      Simulate running this process:  PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.Add);  Don't run the actual process since that will try to schedule the job in flux
        RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafWriter = new RAFFileWriter();
        rafWriter.execute();
    }


    @Test
    public void rejectRAFEnrollment_invalidStatus() {
        String psid = "123456789";

        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingEnrollmentTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        result = PayrollServices.companyManager.rejectRAFEnrollment(company.getCurrentRAFEnrollment(),
                "Rejected by agency");
        PayrollServices.commitUnitOfWork();
        assertFalse("Update enrollment status result", result.isSuccess());
        assertEquals("One error message", 1, result.getErrorMessages().size());
        assertEquals("Error message text: ", "A RAFEnrollment cannot be rejected in its current status: PendingEnrollmentTape", result.getErrorMessages().get(0).getMessage());
    }

    @Test
    public void testRejectThenCancel() {
        String psid = "123456791";
        rejectRAFEnrollment(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        //Cancel all the company's payroll runs
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        for (PayrollRun payrollRun : payrollRuns) {
            TransactionCancelEEDTO txRecallDTO = new TransactionCancelEEDTO();
            txRecallDTO.setTransmissionId(null);
            txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
            ProcessResult recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
            assertSuccess(recallPR);
        }
        PayrollServices.commitUnitOfWork();        

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult<CompanyService> cancelServiceCore2 = PayrollServices.companyManager.deactivateService(
                company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();

        assertSuccess(cancelServiceCore2);

        RAFEnrollment rafEnrollment2 = company.getCurrentRAFEnrollment();
        Assert.assertNotNull(rafEnrollment2);
        Assert.assertEquals("RAF Status", RAFEnrollmentStatus.Cancelled, rafEnrollment2.getStatus());
        Assert.assertEquals("Company's IRS agency", CompanyAgency.findCompanyAgency(company, Agency.IRS),rafEnrollment2.getCompanyAgency());
    }

    //�	Scenario: An Assisted customer is cancelled or terminated, and the customer�s enrollment is in PendingEnrollmentResponse or PendingDeleteTape status
    @Test
    public void testCancelCustomerDontCancelRAF() {
        String psid = "123456790";

        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingEnrollmentTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);


        PayrollServices.beginUnitOfWork();
        //      Simulate running this process:  PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.Add);  Don't run the actual process since that will try to schedule the job in flux
        RAFEnrollmentFile.createFile(RAFActionCode.Add);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafWriter = new RAFFileWriter();
        rafWriter.execute();


        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);

        //Cancel all the company's payroll runs
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        for (PayrollRun payrollRun : payrollRuns) {
            TransactionCancelEEDTO txRecallDTO = new TransactionCancelEEDTO();
            txRecallDTO.setTransmissionId(null);
            txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
            ProcessResult recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
            assertSuccess(recallPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult<CompanyService> cancelServiceCore2 = PayrollServices.companyManager.deactivateService(
                company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();

        assertSuccess(cancelServiceCore2);

        RAFEnrollment rafEnrollment2 = company.getCurrentRAFEnrollment();
        Assert.assertNotNull(rafEnrollment2);
        Assert.assertEquals("RAF Status", RAFEnrollmentStatus.PendingEnrollmentResponse, rafEnrollment2.getStatus());
        Assert.assertEquals("Company's IRS agency", CompanyAgency.findCompanyAgency(company, Agency.IRS),rafEnrollment2.getCompanyAgency());
    }

    @Test
    public void initiateManualReEnrollment_Rejected() {
        String psid = "123456789";
        rejectRAFEnrollment(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult<RAFEnrollment> result = PayrollServices.companyManager.reInitiateRAFEnrollment(company.getCurrentRAFEnrollment());
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        DomainEntitySet<RAFEnrollment> allEnrollments = company.getAllRAFEnrollments();
        assertEquals("Number of RAF enrollments", 2, allEnrollments.size());
        RAFEnrollment oldEnrollment = allEnrollments.get(1);
        RAFEnrollment newEnrollment = allEnrollments.get(0);
        assertEquals("Old RAF Enrollment Status", RAFEnrollmentStatus.Cancelled, oldEnrollment.getStatus());
        //Make sure the old enrollment is the one that still has the detail, file, etc. associated with it.  make sure the agency is still associated
        assertNotNull(oldEnrollment.getCompanyAgency());
        PayrollServices.beginUnitOfWork();
        RAFEnrollmentDetail rafEnrollmentDetail = Application.refresh(oldEnrollment).getRAFEnrollmentDetail();
        PayrollServices.commitUnitOfWork();
        assertNotNull(rafEnrollmentDetail);
        assertNotNull(rafEnrollmentDetail.getEnrollmentFile());
        assertEquals("New RAF Enrollment Status", RAFEnrollmentStatus.PendingEnrollment, newEnrollment.getStatus());
    }

//�	Scenario: An agent manually initiates manual re-enrollment for an enrollment in any other status besides Rejected or Invalid
//o	Expected results
//?	Validation error: cannot initiate RAF re-enrollment for status: x
    @Test
    public void initiateManualReEnrollment_PendingEnrollmentTape() {
        String psid = "123456789";

        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingEnrollmentTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult<RAFEnrollment> result2 = PayrollServices.companyManager.reInitiateRAFEnrollment(company.getCurrentRAFEnrollment());
        PayrollServices.commitUnitOfWork();
        assertFalse("Reinitate process result", result2.isSuccess());

        assertEquals("One error message", 1, result2.getErrorMessages().size());
        assertEquals("Error message text: ", "A RAFEnrollment cannot be re-enrolled unless its status is Rejected.", result2.getErrorMessages().get(0).getMessage());

        DomainEntitySet<RAFEnrollment> allEnrollments = company.getAllRAFEnrollments();
        assertEquals("Number of RAF enrollments", 1, allEnrollments.size());
        RAFEnrollment newEnrollment = allEnrollments.get(0);
        assertEquals("New RAF Enrollment Status", RAFEnrollmentStatus.PendingEnrollmentTape, newEnrollment.getStatus());
    }

//�	Scenario: A customer whose latest RAF enrollment status is Deleted or Cancelled re-activates Assisted service
//o	Expected results
//?	A new RAF enrollment record is created in PendingEnrollment status
//�	A company event is created indicating this status change

    @Test
    public void reactivateServiceDeleted() {
        String psid="143455555";
        sendRAFToTape(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingDeleteTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        PayrollServices.beginUnitOfWork();
        //      Simulate running this process:  PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.Add);  Don't run the actual process since that will try to schedule the job in flux
        RAFEnrollmentFile.createFile(RAFActionCode.Delete);
        PayrollServices.commitUnitOfWork();

        RAFFileWriter rafWriter = new RAFFileWriter();
        rafWriter.execute();


        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);

        //Cancel all the company's payroll runs
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        for (PayrollRun payrollRun : payrollRuns) {
            TransactionCancelEEDTO txRecallDTO = new TransactionCancelEEDTO();
            txRecallDTO.setTransmissionId(null);
            txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
            ProcessResult recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
            assertSuccess(recallPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult deactivateResult = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        assertSuccess("Deactivate tax result", deactivateResult);

        //Set the tax service created date to after the DD service to bypass the reactivate service logic
        company = Application.refresh(company);
        CompanyService taxService = company.getService(ServiceCode.Tax);
        taxService.setCreatedDate(PSPDate.getPSPTime());
        Application.save(taxService);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult reactivateResult = PayrollServices.companyManager.reactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Reactivate tax result", reactivateResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        RAFEnrollment rafEnrollment = company.getCurrentRAFEnrollment();
        assertEquals("RAF status", RAFEnrollmentStatus.PendingEnrollment, rafEnrollment.getStatus());
    }

//�	Scenario: A customer whose latest RAF enrollment status is PendingDeleteTape re-activates Assisted service
//o	Expected results
//?	The existing enrollment�s status is changed to Enrolled
//�	A company event is created indicating this status change
    @Test
    public void reactivateServicePendingDeleteTape() {
        String psid="153455555";
        sendRAFToTape(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingDeleteTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);

        //Cancel all the company's payroll runs
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        for (PayrollRun payrollRun : payrollRuns) {
            TransactionCancelEEDTO txRecallDTO = new TransactionCancelEEDTO();
            txRecallDTO.setTransmissionId(null);
            txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
            ProcessResult recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
            assertSuccess(recallPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult deactivateResult = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        assertSuccess("Deactivate tax result", deactivateResult);

        //Set the tax service created date to after the DD service to bypass the reactivate service logic
        company = Application.refresh(company);
        CompanyService taxService = company.getService(ServiceCode.Tax);
        taxService.setCreatedDate(PSPDate.getPSPTime());
        Application.save(taxService);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult reactivateResult = PayrollServices.companyManager.reactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Reactivate tax result", reactivateResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        RAFEnrollment rafEnrollment = company.getCurrentRAFEnrollment();
        assertEquals("RAF status", RAFEnrollmentStatus.Enrolled, rafEnrollment.getStatus());
    }

//�	Scenario: A customer whose latest RAF enrollment status is in any other status besides Cancelled, PendingDeleteTape, or Deleted re-activates Assisted service
//o	Expected results
//?	No enrollments are cancelled or created
//?	No events are created
    @Test
    public void reactivateServiceOtherStatus() {
        String psid="163455558";
        sendRAFToTape(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);

        //Cancel all the company's payroll runs
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        for (PayrollRun payrollRun : payrollRuns) {
            TransactionCancelEEDTO txRecallDTO = new TransactionCancelEEDTO();
            txRecallDTO.setTransmissionId(null);
            txRecallDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            txRecallDTO.setRequestId(SpcfUniqueId.generateRandomUniqueIdString());
            ProcessResult recallPR = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txRecallDTO);
            assertSuccess(recallPR);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult deactivateResult = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        assertSuccess("Deactivate tax result", deactivateResult);

        //Set the tax service created date to after the DD service to bypass the reactivate service logic
        company = Application.refresh(company);
        CompanyService taxService = company.getService(ServiceCode.Tax);
        taxService.setCreatedDate(PSPDate.getPSPTime());
        Application.save(taxService);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult reactivateResult = PayrollServices.companyManager.reactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Reactivate tax result", reactivateResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        RAFEnrollment rafEnrollment = company.getCurrentRAFEnrollment();
        assertEquals("RAF status", RAFEnrollmentStatus.Enrolled, rafEnrollment.getStatus());
    }

    protected RAFEnrollment getRAFEnrollment(Company company) {
        PayrollServices.beginUnitOfWork();
        RAFEnrollment raf = company.getCurrentRAFEnrollment();
        PayrollServices.commitUnitOfWork();
        return raf;
    }
}
