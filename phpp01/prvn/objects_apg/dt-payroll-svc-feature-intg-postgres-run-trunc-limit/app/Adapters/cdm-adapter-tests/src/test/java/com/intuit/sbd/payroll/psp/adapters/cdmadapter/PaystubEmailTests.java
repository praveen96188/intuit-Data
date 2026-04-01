package com.intuit.sbd.payroll.psp.adapters.cdmadapter;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.IamEmailAddressProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PaystubEmailTests {
    private static final String psid = "99000123";
    //This needs to be a valid consumer realm ID in IAM E2E environment
    private static final String realmId = "222112271";

    @Before
    public void startUp() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 1, 6));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void shutdown() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testPaystubNotificationEmailWithPaycheckDate() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2012, 11, 30));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        DomainEntitySet<CompanyEvent> companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(1, companyEvents.size());

        Application.beginUnitOfWork();
        Application.refresh(employee);
        //More than 7 days ago, there should be a paystub created event but no email
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2012, 12, 29));
        companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(2, companyEvents.size());
        DomainEntitySet<CompanyEventEmail> paystubNotificationEmails = getVmpPaystubNotificationEmails();
        Assert.assertEquals(0, paystubNotificationEmails.size());
        //6 days ago, there should be a paystub created event and an email event
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2012, 12, 31));
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(3, companyEvents.size());
        paystubNotificationEmails = getVmpPaystubNotificationEmails();
        Assert.assertEquals(1, paystubNotificationEmails.size());
        CompanyEventEmail companyEventEmail = paystubNotificationEmails.getFirst();
        //It should be in the FormatError state now prior to the batch job inserting the email
        Assert.assertEquals(EventEmailStatus.FormatError, companyEventEmail.getStatusCd());
        DomainEntitySet<CompanyEventEmailParam> parameters = getVmpPaystubNotificationParameters();
        DomainEntitySet<CompanyEventEmailParam> recipientEmailParameters = parameters.find(CompanyEventEmailParam.ParamTypeCd().equalTo(EventEmailParamTypeCode.RecipientEmail));
        assertEquals(1, companyEventEmail.getCompanyEvent().getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.EmployeeId).size());

        //Fetch the paycheck date param and test to make sure we did get the data back
        DomainEntitySet<CompanyEventEmailParam> paycheckSettlementEmailParameters = parameters.find(CompanyEventEmailParam.ParamTypeCd().equalTo(EventEmailParamTypeCode.PaycheckSettlementDate));
        assertEquals(1, paycheckSettlementEmailParameters.size());

        Assert.assertEquals(0, recipientEmailParameters.size());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testPaystubNotificationEmail() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2012, 11, 30));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        DomainEntitySet<CompanyEvent> companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(1, companyEvents.size());

        Application.beginUnitOfWork();
        Application.refresh(employee);
        //More than 7 days ago, there should be a paystub created event but no email
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2012, 12, 29));
        companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(2, companyEvents.size());
        DomainEntitySet<CompanyEventEmail> paystubNotificationEmails = getVmpPaystubNotificationEmails();
        Assert.assertEquals(0, paystubNotificationEmails.size());
        //6 days ago, there should be a paystub created event and an email event
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2012, 12, 31));
        PstubPayItem pstubPayItem = VmpTestUtil.findPayItem(paystub.getId().toString());
        Assert.assertNotNull(pstubPayItem);
        Assert.assertNotNull(pstubPayItem.getCompany());
        Assert.assertEquals(pstubPayItem.getCompany().getId(), Company.findCompany(psid, SourceSystemCode.QBDT).getId());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(3, companyEvents.size());
        paystubNotificationEmails = getVmpPaystubNotificationEmails();
        Assert.assertEquals(1, paystubNotificationEmails.size());
        CompanyEventEmail companyEventEmail = paystubNotificationEmails.getFirst();
        //It should be in the FormatError state now prior to the batch job inserting the email
        Assert.assertEquals(EventEmailStatus.FormatError, companyEventEmail.getStatusCd());
        DomainEntitySet<CompanyEventEmailParam> parameters = getVmpPaystubNotificationParameters();
        DomainEntitySet<CompanyEventEmailParam> recipientEmailParameters = parameters.find(CompanyEventEmailParam.ParamTypeCd().equalTo(EventEmailParamTypeCode.RecipientEmail));
        assertEquals(1, companyEventEmail.getCompanyEvent().getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.EmployeeId).size());
        Assert.assertEquals(0, recipientEmailParameters.size());
        Application.rollbackUnitOfWork();

        IamEmailAddressProcessor iamEmailAddressProcessor = new IamEmailAddressProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.IamEmailAddressProcessor, "job 1", "run");
        iamEmailAddressProcessor.executeJob();

        Application.beginUnitOfWork();
        paystubNotificationEmails = getVmpPaystubNotificationEmails();
        companyEventEmail = paystubNotificationEmails.getFirst();
        //Should be in pending status now and the recipient email address should be populated
        Assert.assertEquals(EventEmailStatus.Pending, companyEventEmail.getStatusCd());
        String recipientEmailAddress = companyEventEmail.getEmailParamValue(EventEmailParamTypeCode.RecipientEmail);
        Assert.assertEquals("vmpqa2011+ee1bonus_iamtestpass@gmail.com", recipientEmailAddress);
        DomainEntitySet<CompanyEventEmailParam> notificationParameters = getVmpPaystubNotificationParameters();
        DomainEntitySet<CompanyEventEmailParam> eventEmailParameters = notificationParameters.find(CompanyEventEmailParam.ParamTypeCd().equalTo(EventEmailParamTypeCode.RecipientEmail));
        Assert.assertEquals(1, eventEmailParameters.size());
        Assert.assertNotNull(eventEmailParameters.get(0).getCompany());
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Assert.assertEquals(eventEmailParameters.get(0).getCompany().getId(), company.getId());
        Application.rollbackUnitOfWork();
    }

    private DomainEntitySet<CompanyEvent> getPaystubCreatedEvents() {
        Expression<CompanyEvent> query = new Query<CompanyEvent>()
            .Where(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.PaystubCreated));
        return Application.find(CompanyEvent.class, query);
    }

    private DomainEntitySet<CompanyEventEmail> getVmpPaystubNotificationEmails() {
        Expression<CompanyEventEmail> query = new Query<CompanyEventEmail>()
            .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.VmpPaystubNotification));
        return Application.find(CompanyEventEmail.class, query);
    }

    private DomainEntitySet<CompanyEventEmailParam> getVmpPaystubNotificationParameters() {
        Expression<CompanyEventEmailParam> query = new Query<CompanyEventEmailParam>()
            .Where(CompanyEventEmailParam.CompanyEventEmail().EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.VmpPaystubNotification));
        return  Application.find(CompanyEventEmailParam.class, query);
    }

    @Ignore
    /**
     * //Cert is missing on class path when running as a test, we will be adding the cert file and editing the response to contain a null value for receipientEmailAddreess
     *  1. Place the Intuit.ems.psp.jks from C:\Dev\psp\dev\Gateways\IAM\test\src\resources\keystore to C:\Dev\psp\dev\Adapters\CdmAdapterTests\src\resources\keystore
     *  2. Place debug point at ConsumerRealm.getConsumerRealmInformation() line 20.
     *  3. Edit the Identity object and set the email value to null
     */
    @Test
    public void testPaystubNotificationEmailWithNullRecipientEmail() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2012, 11, 30));
        Application.commitUnitOfWork();
        VmpTestUtil.associateEmployeeWithRealm(realmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        DomainEntitySet<CompanyEvent> companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(1, companyEvents.size());

        Application.beginUnitOfWork();
        Application.refresh(employee);
        //More than 7 days ago, there should be a paystub created event but no email
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2012, 12, 29));
        companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(2, companyEvents.size());
        DomainEntitySet<CompanyEventEmail> paystubNotificationEmails = getVmpPaystubNotificationEmails();
        Assert.assertEquals(0, paystubNotificationEmails.size());
        //6 days ago, there should be a paystub created event and an email event
        VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2012, 12, 31));
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(3, companyEvents.size());
        paystubNotificationEmails = getVmpPaystubNotificationEmails();
        Assert.assertEquals(1, paystubNotificationEmails.size());
        CompanyEventEmail companyEventEmail = paystubNotificationEmails.getFirst();
        //It should be in the FormatError state now prior to the batch job inserting the email
        Assert.assertEquals(EventEmailStatus.FormatError, companyEventEmail.getStatusCd());
        DomainEntitySet<CompanyEventEmailParam> parameters = getVmpPaystubNotificationParameters();
        DomainEntitySet<CompanyEventEmailParam> recipientEmailParameters = parameters.find(CompanyEventEmailParam.ParamTypeCd().equalTo(EventEmailParamTypeCode.RecipientEmail));
        assertEquals(1, companyEventEmail.getCompanyEvent().getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.EmployeeId).size());

        //Fetch the paycheck date param and test to make sure we did get the data back
        DomainEntitySet<CompanyEventEmailParam> paycheckSettlementEmailParameters = parameters.find(CompanyEventEmailParam.ParamTypeCd().equalTo(EventEmailParamTypeCode.PaycheckSettlementDate));
        assertEquals(1, paycheckSettlementEmailParameters.size());

        Assert.assertEquals(0, recipientEmailParameters.size());
        Application.rollbackUnitOfWork();

        //RUn the IamEmailAddressProcessor job, so that it calls the Iam Service to fetch the emailId information
        BatchJobManager.runJob(BatchJobType.IamEmailAddressProcessor);
        BatchJobManager.runJob(BatchJobType.EmailGateway);
        //Running it twice, default batch size is 3 and test creates 4 emails to be sent
        BatchJobManager.runJob(BatchJobType.EmailGateway);

        Application.beginUnitOfWork();
        companyEvents = getPaystubCreatedEvents();
        Assert.assertEquals(3, companyEvents.size());
        paystubNotificationEmails = getVmpPaystubNotificationEmails();
        Assert.assertEquals(1, paystubNotificationEmails.size());
        companyEventEmail = paystubNotificationEmails.getFirst();
        //It should be in the Ignore state now after the batch job finding null value for recipientEmailAddress
        Assert.assertEquals(EventEmailStatus.Ignore, companyEventEmail.getStatusCd());
        parameters = getVmpPaystubNotificationParameters();
        recipientEmailParameters = parameters.find(CompanyEventEmailParam.ParamTypeCd().equalTo(EventEmailParamTypeCode.RecipientEmail));
        assertEquals(0,recipientEmailParameters.size());
        Application.rollbackUnitOfWork();
    }
}
