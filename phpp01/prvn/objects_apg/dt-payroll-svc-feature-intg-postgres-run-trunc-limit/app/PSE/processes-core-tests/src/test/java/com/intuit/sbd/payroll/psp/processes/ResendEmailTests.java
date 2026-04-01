package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.util.EmailUtils;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.TransactionReverseCoreDataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * User: dweinberg
 * Date: Dec 16, 2009
 * Time: 4:05:35 PM
 */
public class ResendEmailTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testResendQBDTEmail() {
        AddCompanyDataLoader.addQBDTCompanyCoreDiffAgreeFailsFraudControls(null);

        //"Send" the emails
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> emails = PayrollServices.entityFinder.find(CompanyEventEmail.class);
        for (CompanyEventEmail email : emails) {
            email.setStatusCd(EventEmailStatus.Sent);
        }
        PayrollServices.commitUnitOfWork();

        //resend one of them
        PayrollServices.beginUnitOfWork();
        Company c = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyEvent ce = CompanyEvent.findCompanyEvents(c, EventTypeCode.PINCreated, CompanyEventStatus.Active, false).get(0) ;
        CompanyEventEmail cee = ce.getCompanyEventEmailCollection().get(0);
        ResendEmail resendEmail = new ResendEmail(SourceSystemCode.QBDT, "8574536", cee.getId().toString(), true, null);
        ProcessResult pr = resendEmail.execute();
        assertTrue("Email resent success", pr.isSuccess());
        PayrollServices.commitUnitOfWork();

        //check email fields
        PayrollServices.beginUnitOfWork();
        Application.refresh(ce);
        CompanyEventEmail resentCee = ce.getCompanyEventEmailCollection().find(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.PendingResend)).get(0);
        assertEquals(EventEmailTemplateTypeCode.DDBankVerificationSuccessful, resentCee.getEmailTemplateTypeCd());
        assertEquals(EventEmailStatus.PendingResend, resentCee.getStatusCd());
        assertNotNull("CompanyEventEmail company null",resentCee.getCompany());

        //check email parameters
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyBankAccountLastFour, "4747");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.SourcePayrollSystem, "QBDT");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyLegalName, "QB Desktop 3");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminEmail, "PayrollAdmin@aol.com");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminFirstName, "Johnny");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminLastName, "Payrolladmin");


        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testResendQBDTEmailContactChanged() {
        AddCompanyDataLoader.addQBDTCompanyCoreDiffAgreeFailsFraudControls(null);

        //"Send" the emails
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> emails = PayrollServices.entityFinder.find(CompanyEventEmail.class);
        for (CompanyEventEmail email : emails) {
            email.setStatusCd(EventEmailStatus.Sent);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //change the contact information
        Company c = Company.findCompany("8574536", SourceSystemCode.QBDT);
        Contact admin = c.getContactByRoleCode(ContactRole.PayrollAdmin);
        admin.setFirstName("Bob");
        admin.setLastName("Wehadababyitsaboy");
        admin.setEmail("bob@wehadababyitsaboy.com");
        PayrollServices.commitUnitOfWork();

        //resend one of them
        PayrollServices.beginUnitOfWork();
        CompanyEvent ce = CompanyEvent.findCompanyEvents(c, EventTypeCode.PINCreated, CompanyEventStatus.Active, false).get(0) ;
        CompanyEventEmail cee = ce.getCompanyEventEmailCollection().get(0);
        ResendEmail resendEmail = new ResendEmail(SourceSystemCode.QBDT, "8574536", cee.getId().toString(), true,null);
        ProcessResult pr = resendEmail.execute();
        assertTrue("Email resent success", pr.isSuccess());
        PayrollServices.commitUnitOfWork();

        //check email fields
        PayrollServices.beginUnitOfWork();
        Application.refresh(ce);
        CompanyEventEmail resentCee = ce.getCompanyEventEmailCollection().find(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.PendingResend)).get(0);
        assertEquals(EventEmailTemplateTypeCode.DDBankVerificationSuccessful, resentCee.getEmailTemplateTypeCd());
        assertEquals(EventEmailStatus.PendingResend, resentCee.getStatusCd());
        assertNotNull("CompanyEventEmail company null",resentCee.getCompany());

        //check email parameters
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyBankAccountLastFour, "4747");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.SourcePayrollSystem, "QBDT");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyLegalName, "QB Desktop 3");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminEmail, "bob@wehadababyitsaboy.com");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminFirstName, "Bob");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminLastName, "Wehadababyitsaboy");

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testResendQBOEEmailContactChanged() {
        TransactionReverseCoreDataLoader.loadPayrollRunForTransactionReverseTest();
        new TransactionReverseCoreTests().testBug800();


        //"Send" the emails
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> emails = PayrollServices.entityFinder.find(CompanyEventEmail.class);
        for (CompanyEventEmail email : emails) {
            email.setStatusCd(EventEmailStatus.Sent);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //change the contact information
        //testing that if we change the PA info it is set to the PP in the resend
        Company c = Company.findCompany("123272727", SourceSystemCode.QBOE);
        Contact admin = c.getContactByRoleCode(ContactRole.PayrollAdmin);
        admin.setFirstName("Bob");
        admin.setLastName("Wehadababyitsaboy");
        admin.setEmail("bob@wehadababyitsaboy.com");
        PayrollServices.commitUnitOfWork();

        //resend one of them
        PayrollServices.beginUnitOfWork();
        CompanyEvent ce=null;
        for (CompanyEvent findCe : CompanyEvent.findCompanyEvents(c, EventTypeCode.ReversalRequested, CompanyEventStatus.Active, false)) {
            FinancialTransaction finTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class,
                SpcfUniqueId.createInstance(findCe.getCompanyEventDetails(EventDetailTypeCode.FinancialTransactionId).get(0).getValue()));
            if (finTxn.getEmployeeBankAccount().getEmployee().getFirstName().equals("Emp1")) {
                ce = findCe;
                break;
            }
        }
        if (ce == null) {
            fail("Couldn't find event to resend email on");
        }
        CompanyEventEmail cee = ce.getCompanyEventEmailCollection().get(0);
        ResendEmail resendEmail = new ResendEmail(SourceSystemCode.QBOE, "123272727", cee.getId().toString(), false, null);
        ProcessResult pr = resendEmail.execute();
        assertTrue(pr.getMessages().toString(), pr.isSuccess());

        //find out what the amount is
        String txnId = ce.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId);
        FinancialTransaction txn = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(txnId));
        String amount = EmailUtils.formatMoney(txn.getOriginalTransaction().getFinancialTransactionAmount());
        PayrollServices.commitUnitOfWork();

        //check email fields
        PayrollServices.beginUnitOfWork();
        Application.refresh(ce);
        CompanyEventEmail resentCee = ce.getCompanyEventEmailCollection().find(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.PendingResend)).get(0);
        assertEquals(EventEmailTemplateTypeCode.CustomerInitiatedDDReversal1, resentCee.getEmailTemplateTypeCd());
        assertEquals(EventEmailStatus.PendingResend, resentCee.getStatusCd());
        assertNotNull("CompanyEventEmail company null",resentCee.getCompany());



        //check email parameters
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.ReversalPendingList, "Emp1 E&#8217;s direct deposit in the amount " + amount + " will be reversed<br>");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.SourcePayrollSystem, "QBOE");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyLegalName, "Dreams Come True, Inc");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PrimaryPrincipalEmail, "bob@wehadababyitsaboy.com");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PrimaryPrincipalFirstName, "Bob");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PrimaryPrincipalLastName, "Wehadababyitsaboy");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.ServiceType, "Direct Deposit");


        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        assertEquals(resentCee.getEmailParamForEmailEvent(EventEmailParamTypeCode.SourcePayrollSystem).get(0).getCompany().getId(),
                company.getId());
        assertEquals(resentCee.getEmailParamForEmailEvent(EventEmailParamTypeCode.ReversalPendingList).get(0).getCompany().getId(),
                company.getId());
        assertEquals(resentCee.getEmailParamForEmailEvent(EventEmailParamTypeCode.PrimaryPrincipalFirstName).get(0).getCompany().getId(),
                company.getId());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testResendQBOEEmailContactChangedResendingRelated() {
        TransactionReverseCoreDataLoader.loadPayrollRunForTransactionReverseTest();
        new TransactionReverseCoreTests().testBug800();


        //"Send" the emails
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> emails = PayrollServices.entityFinder.find(CompanyEventEmail.class);
        for (CompanyEventEmail email : emails) {
            email.setStatusCd(EventEmailStatus.Sent);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //change the contact information
        //testing that if we change the PA info it is set to the PP in the resend
        Company c = Company.findCompany("123272727", SourceSystemCode.QBOE);
        Contact admin = c.getContactByRoleCode(ContactRole.PayrollAdmin);
        admin.setFirstName("Bob");
        admin.setLastName("Wehadababyitsaboy");
        admin.setEmail("bob@wehadababyitsaboy.com");
        PayrollServices.commitUnitOfWork();

        //resend one of them
        PayrollServices.beginUnitOfWork();
        CompanyEvent ce=null;
        for (CompanyEvent findCe : CompanyEvent.findCompanyEvents(c, EventTypeCode.ReversalRequested, CompanyEventStatus.Active, false)) {
            FinancialTransaction finTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class,
                    SpcfUniqueId.createInstance(findCe.getCompanyEventDetails(EventDetailTypeCode.FinancialTransactionId).get(0).getValue()));
            if (finTxn.getEmployeeBankAccount().getEmployee().getFirstName().equals("Emp1")) {
                ce = findCe;
                break;
            }
        }
        if (ce == null) {
            fail("Couldn't find event to resend email on");
        }
        CompanyEventEmail cee = ce.getCompanyEventEmailCollection().get(0);
        ResendEmail resendEmail = new ResendEmail(SourceSystemCode.QBOE, "123272727", cee.getId().toString(), true, null);
        ProcessResult pr = resendEmail.execute();
        assertTrue(pr.getMessages().toString(), pr.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //check all the emails are there
        DomainEntitySet<CompanyEventEmail> resentEmails = CompanyEventEmail.findEmailEventsByStatus(EventEmailStatus.PendingResend);

        assertEquals("Number of resent emails", 4, resentEmails.size());

        
        for (CompanyEventEmail resentCee : resentEmails) {
            //check email fields
            assertEquals(EventEmailTemplateTypeCode.CustomerInitiatedDDReversal1, resentCee.getEmailTemplateTypeCd());
            assertEquals(EventEmailStatus.PendingResend, resentCee.getStatusCd());

            //check email parameters
            assertNotNull(resentCee.getEmailParamForEmailEvent(EventEmailParamTypeCode.ReversalPendingList).get(0).getValue());            
            assertEmailParamIs(resentCee, EventEmailParamTypeCode.SourcePayrollSystem, "QBOE");
            assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyLegalName, "Dreams Come True, Inc");
            assertEmailParamIs(resentCee, EventEmailParamTypeCode.PrimaryPrincipalEmail, "bob@wehadababyitsaboy.com");
            assertEmailParamIs(resentCee, EventEmailParamTypeCode.PrimaryPrincipalFirstName, "Bob");
            assertEmailParamIs(resentCee, EventEmailParamTypeCode.PrimaryPrincipalLastName, "Wehadababyitsaboy");
            assertEmailParamIs(resentCee, EventEmailParamTypeCode.ServiceType, "Direct Deposit");

            //check company object is set
            assertNotNull("CompanyEventEmail company null",resentCee.getCompany());
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testResendQBDTEmailWithSessionEmailId() {
        AddCompanyDataLoader.addQBDTCompanyCoreDiffAgreeFailsFraudControls(null);

        //"Send" the emails
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> emails = PayrollServices.entityFinder.find(CompanyEventEmail.class);
        for (CompanyEventEmail email : emails) {
            email.setStatusCd(EventEmailStatus.Sent);
        }
        PayrollServices.commitUnitOfWork();

        //resend one of them
        PayrollServices.beginUnitOfWork();
        Company c = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyEvent ce = CompanyEvent.findCompanyEvents(c, EventTypeCode.PINCreated, CompanyEventStatus.Active, false).get(0) ;
        CompanyEventEmail cee = ce.getCompanyEventEmailCollection().get(0);
        ResendEmail resendEmail = new ResendEmail(SourceSystemCode.QBDT, "8574536", cee.getId().toString(), true,"agent_email@mock.com");
        ProcessResult pr = resendEmail.execute();
        assertTrue("Email resent success", pr.isSuccess());
        PayrollServices.commitUnitOfWork();

        //check email fields
        PayrollServices.beginUnitOfWork();
        Application.refresh(ce);
        CompanyEventEmail resentCee = ce.getCompanyEventEmailCollection().find(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.PendingResend)).get(0);
        assertEquals(EventEmailTemplateTypeCode.DDBankVerificationSuccessful, resentCee.getEmailTemplateTypeCd());
        assertEquals(EventEmailStatus.PendingResend, resentCee.getStatusCd());
        assertNotNull("CompanyEventEmail company null",resentCee.getCompany());

        //check email parameters
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyBankAccountLastFour, "4747");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.SourcePayrollSystem, "QBDT");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyLegalName, "QB Desktop 3");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminEmail, "agent_email@mock.com");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminFirstName, "Johnny");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminLastName, "Payrolladmin");


        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testResendQBDTEmailContactChangedWithSessionEmailId() {
        AddCompanyDataLoader.addQBDTCompanyCoreDiffAgreeFailsFraudControls(null);

        //"Send" the emails
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> emails = PayrollServices.entityFinder.find(CompanyEventEmail.class);
        for (CompanyEventEmail email : emails) {
            email.setStatusCd(EventEmailStatus.Sent);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //change the contact information
        Company c = Company.findCompany("8574536", SourceSystemCode.QBDT);
        Contact admin = c.getContactByRoleCode(ContactRole.PayrollAdmin);
        admin.setFirstName("Sherlock");
        admin.setLastName("Holmes");
        admin.setEmail("sherlock@holmes.com");
        PayrollServices.commitUnitOfWork();

        //resend one of them
        PayrollServices.beginUnitOfWork();
        CompanyEvent ce = CompanyEvent.findCompanyEvents(c, EventTypeCode.PINCreated, CompanyEventStatus.Active, false).get(0) ;
        CompanyEventEmail cee = ce.getCompanyEventEmailCollection().get(0);
        ResendEmail resendEmail = new ResendEmail(SourceSystemCode.QBDT, "8574536", cee.getId().toString(), true,"agent_email@mock.com");
        ProcessResult pr = resendEmail.execute();
        assertTrue("Email resent success", pr.isSuccess());
        PayrollServices.commitUnitOfWork();

        //check email fields
        PayrollServices.beginUnitOfWork();
        Application.refresh(ce);
        CompanyEventEmail resentCee = ce.getCompanyEventEmailCollection().find(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.PendingResend)).get(0);
        assertEquals(EventEmailTemplateTypeCode.DDBankVerificationSuccessful, resentCee.getEmailTemplateTypeCd());
        assertEquals(EventEmailStatus.PendingResend, resentCee.getStatusCd());
        assertNotNull("CompanyEventEmail company null",resentCee.getCompany());

        //check email parameters
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyBankAccountLastFour, "4747");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.SourcePayrollSystem, "QBDT");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.CompanyLegalName, "QB Desktop 3");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminEmail, "agent_email@mock.com");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminFirstName, "Sherlock");
        assertEmailParamIs(resentCee, EventEmailParamTypeCode.PayrollAdminLastName, "Holmes");

        PayrollServices.commitUnitOfWork();
    }

    private static void assertEmailParamIs(CompanyEventEmail email, EventEmailParamTypeCode code, String value) {
        assertEquals(value, email.getEmailParamForEmailEvent(code).get(0).getValue());
    }

}
