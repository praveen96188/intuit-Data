package com.intuit.sbd.payroll.psp.gateways.email;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXDataloader;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.EoqSUITaxAdjustments;
import com.intuit.sbd.payroll.psp.batchjobs.offload.CreateTransactionOffloadedEvents;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.request.ObjectFactory;
import com.intuit.sbd.payroll.psp.common.ofx.request.SIGNONMSGSRQV1;
import com.intuit.sbd.payroll.psp.common.ofx.request.SONRQ;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.gateways.email.factory.CompanyEventEmailManager;
import com.intuit.sbd.payroll.psp.gateways.email.factory.EventEmailTemplateFactory;
import com.intuit.sbd.payroll.psp.gateways.email.factory.NotificationServiceFactory;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbd.payroll.psp.gateways.email.oinp.OINPEmailHelper;
import com.intuit.sbd.payroll.psp.gateways.email.txe.TxeEmailHelper;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.junit.*;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Aug 1, 2008
 * Time: 6:07:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestEmailGateway {
    public static final String QBDT_COMPANY_PSID = "8574536";
    public static final String QBOE_COMPANY_PSID = "1234567";
    public static final String QBDT_BP_COMPANY_PSID = "123272727";

    private static NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @AfterClass
    public static void afterClass() {

        NotificationServiceFactory.useDefaultPortFactory();
    }

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.resetPSPTime();
        resetSystemParameter();
        PayrollServices.commitUnitOfWork();
    }

    public void beforeEachTestQBOE() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        prepareQBOECompany();
    }

    public void beforeEachTestQBDT() {
        QBDTTestHelper.typicalRunBeforeEachTest();
        prepareQBDTCompany();
    }

    public void beforeEachTestQBDT_BP() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        prepareQBDT_BPCompany();
    }

    public void beforeEachTestQBDT_BP_PayBills() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        prepareQBDT_BPCompany_PayBills();
    }

    public void beforeEachTestQBDT_BP_SignUp(ContactRole pContactRole) {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        prepareQBDT_BPCompany_SignUp(pContactRole);
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    private void resetSystemParameter() {
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "false");
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_EBA_ADD_EMAIL_NOTIFICATION, "false");
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_PBA_ADD_EMAIL_NOTIFICATION, "false");
    }

    /**
     * Loads the QBOE test company in preparation for testing.
     */
    private void prepareQBOECompany() {
        // set psp time
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        // create company and payroll run for company
        PayrollServices.beginUnitOfWork();

        Company1Dataloader c1dl = new Company1Dataloader();

        c1dl.persistCompany1();

        c1dl.persistPayrollRun(c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Loads the QBDT test company in preparation for testing.
     */
    private void prepareQBDTCompany() {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }


    /**
     * Loads the QBDT - BillPayment test company in preparation for testing.
     */
    private void prepareQBDT_BPCompany() {
        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(QBDT_BP_COMPANY_PSID, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 1; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }
        PayrollServices.beginUnitOfWork();
        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Loads the QBDT - BillPayment test company in preparation for testing.
     */
    private void prepareQBDT_BPCompany_SignUp(ContactRole pContactRole) {
        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit_ContactRole(pContactRole);
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Loads the QBDT - BillPayment (paid from PayBills) test company in preparation for testing.
     */
    private void prepareQBDT_BPCompany_PayBills() {
        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(QBDT_BP_COMPANY_PSID, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 1; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment_PayBills("Payee" + i, new DateDTO("2007-09-10"), 2, "Reference" + i,
                    "Memo" + i);
            billPaymentDTOs.add(billPaymentDTO);
        }
        PayrollServices.beginUnitOfWork();
        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDDSignupConfirmation() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());

        IEventEmail currRecipient = currEmailTemplates.get(2).getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property5 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

//    /**
//     * Test to ensure DDSignupConfirmation email is generated when a company is re-activated
//     */
//    @Test
//    public void testDDSignupConfirmation_CompanyReactivated() {
//        addCompanyWithBankAccount();
//
//        // deactivate company
//        PayrollServices.beginUnitOfWork();
//        ProcessResult<CompanyService> deactivateResult = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, ServiceCode.DirectDeposit);
//        PayrollServices.commitUnitOfWork();
//        assertSuccess(deactivateResult);
//
//        // re-activate company
//        PayrollServices.beginUnitOfWork();
//        ProcessResult<CompanyService> reactivateResult = PayrollServices.companyManager.reactivateService(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, ServiceCode.DirectDeposit);
//        PayrollServices.commitUnitOfWork();
//        assertSuccess(reactivateResult);
//
//        PayrollServices.beginUnitOfWork();
//        DDServiceInfoDTO dtoDDService = new DDServiceInfoDTO();
//        dtoDDService.setAveragePayrollAmount(new BigDecimal("3000"));
//        dtoDDService.setHighAnnualPayrollAmount(new BigDecimal("20000"));
//        ProcessResult<CompanyService> updateServiceProcessResult = PayrollServices.companyManager.updateService(
//                            SourceSystemCode.QBDT, QBDT_COMPANY_PSID, dtoDDService);
//        PayrollServices.commitUnitOfWork();
//        assertSuccess(updateServiceProcessResult);
//
//        PayrollServices.beginUnitOfWork();
//        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
//
//        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
//        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(0).getTemplateId());
//        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(1).getTemplateId());
//        IEventEmail currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
//        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
//        assertEquals("Recipient Email", "someEmail1234@aol.com", currRecipient.getRecipientEmail());
//        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());
//
//        Properties recipientProperties = currRecipient.getProperties();
//        assertEquals("Number Of Email Properties ", 5, recipientProperties.size());
//
//        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
//        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
//        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
//        assertEquals("Property4 Value ", "someEmail1234@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
//        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
//
//        // DDSignUpConfirmation when service re-activated
//        currRecipient = currEmailTemplates.get(1).getRecipientsToTransmit().get(0).get(0);
//        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
//        assertEquals("Recipient Email", "someEmail1234@aol.com", currRecipient.getRecipientEmail());
//        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());
//
//        recipientProperties = currRecipient.getProperties();
//        assertEquals("Number Of Email Properties ", 5, recipientProperties.size());
//
//        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
//        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
//        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
//        assertEquals("Property4 Value ", "someEmail1234@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
//        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
//
//        PayrollServices.commitUnitOfWork();
//    }

    @Test
    public void testEmailProcessingSize() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "5000");
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> pendingEventList = CompanyEventEmail.findEmailEventsByStatus(EventEmailStatus.Pending);
        assertEquals("Pending Events", 0, pendingEventList.size());
        PayrollServices.commitUnitOfWork();

        beforeEachTestQBDT_BP();

        PayrollServices.beginUnitOfWork();
        pendingEventList = CompanyEventEmail.findEmailEventsByStatus(EventEmailStatus.Pending);
        assertEquals("Pending Events", 8, pendingEventList.size());
        assertNotNull("CompanyEventEmail company null",pendingEventList.get(0).getCompany());
        assertEquals(pendingEventList.get(0).getCompanyEvent().getCompany().getSourceCompanyId(),
                pendingEventList.get(0).getCompany().getSourceCompanyId());
        PayrollServices.commitUnitOfWork();


        try {
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "1");
            PayrollServices.commitUnitOfWork();

            new EmailGateway().processCompanyEventsForEmail();

            PayrollServices.beginUnitOfWork();
            pendingEventList = CompanyEventEmail.findEmailEventsByStatus(EventEmailStatus.Pending);
            assertEquals("Pending Events", 7, pendingEventList.size());
            assertNotNull("CompanyEventEmail company null",pendingEventList.get(0).getCompany());
        assertEquals(pendingEventList.get(0).getCompanyEvent().getCompany().getSourceCompanyId(),
                pendingEventList.get(0).getCompany().getSourceCompanyId());
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "3");
            PayrollServices.commitUnitOfWork();

            new EmailGateway().processCompanyEventsForEmail();

            PayrollServices.beginUnitOfWork();
            pendingEventList = CompanyEventEmail.findEmailEventsByStatus(EventEmailStatus.Pending);
            assertEquals("Pending Events", 6, pendingEventList.size());
            assertNotNull("CompanyEventEmail company null",pendingEventList.get(0).getCompany());
        assertEquals(pendingEventList.get(0).getCompanyEvent().getCompany().getSourceCompanyId(),
                pendingEventList.get(0).getCompany().getSourceCompanyId());
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            try {
                PayrollServices.rollbackUnitOfWork();
            } catch (Throwable th) {
                //Nothing
            }

        } finally {
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "5000");
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void testBPEvents_WriteChecks() {
        if(getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_VENDOR_ENABLED)){
            return;
        }
        beforeEachTestQBDT_BP();
        testBPEvents();
    }

    @Test
    public void testBPEvents_WriteChecks_MTL() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_VENDOR_ENABLED)){
            return;
        }
        beforeEachTestQBDT_BP();
        testBPEvents();
    }

    @Test
    public void testBPEvents_WriteChecks_MTL_OneEmp() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_VENDOR_ENABLED)){
            return;
        }
        beforeEachTestQBDT_BP();
        testBPEvents();
    }

    @Test
    public void testBPEvents_PayBills_MTL() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_VENDOR_ENABLED)){
            return;
        }
        beforeEachTestQBDT_BP_PayBills();
        testBPEvents();
    }

    @Test
    public void testBPEvents_PayBills() {
        if (getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_VENDOR_ENABLED)){
            return;
        }
        beforeEachTestQBDT_BP_PayBills();
        testBPEvents();
    }

    public void testBPEvents() {
        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());

        for (EventEmailTemplate currTemplate : currEmailTemplates) {
            IEventEmail currRecipient;
            Properties recipientProperties;

            switch (currTemplate.getTemplateId()) {

                case VendorPaymentSignupConfirmation:
                    currRecipient = currTemplate.getRecipientsToTransmit().get(0).get(0);
                    int numberORecipients = currTemplate.getRecipientsToTransmit().get(0).size();
                    assertEquals("Company ID", QBDT_BP_COMPANY_PSID, currRecipient.getCompanyId());
                    assertEquals("Number of Recipients", 3, numberORecipients);

                    recipientProperties = currRecipient.getProperties();
                    assertEquals("Number Of Email Properties ", 6, recipientProperties.size());
                    assertEquals("Property3 Value ", "Dreams Come True, Inc", recipientProperties.getProperty("CompanyLegalName"));
                    //assertEquals("Property4 Value ", "PayrollAdmin2@email.com", recipientProperties.getProperty("PayrollAdminEmail"));
                    assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
                    assertEquals("Property6 Value ", "Direct Deposit for Vendors", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
                    break;
                case VendorPaymentReceived1:
                    currRecipient = currTemplate.getRecipientsToTransmit().get(0).get(0);

                    assertEquals("Company ID", QBDT_BP_COMPANY_PSID, currRecipient.getCompanyId());
                    assertEquals("Recipient Email", "PayrollAdmin2@email.com", currRecipient.getRecipientEmail());
                    assertEquals("Recipient Name", "Steve Payrolladmin2", currRecipient.getRecipientName());
                    recipientProperties = currRecipient.getProperties();

                    assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
                    assertEquals("Property1 Value ", "Steve", recipientProperties.getProperty("PayrollAdminFirstName"));
                    assertEquals("Property2 Value ", "Payrolladmin2", recipientProperties.getProperty("PayrollAdminLastName"));
                    assertEquals("Property3 Value ", "Dreams Come True, Inc", recipientProperties.getProperty("CompanyLegalName"));
                    assertEquals("Property4 Value ", "PayrollAdmin2@email.com", recipientProperties.getProperty("PayrollAdminEmail"));
                    assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
                    assertEquals("Property6 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
                    assertEquals("Property7 Value ", "September 12, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
                    assertEquals("Property8 Value ", "12:00 AM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
                    assertEquals("Property9 Value ", "September 10, 2007", recipientProperties.getProperty("PayrollRunDate"));
                    assertTrue("Property10 Value ", recipientProperties.getProperty("VendorPaymentList") != null);
                    assertEquals("Property11 Value ", "September 11, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));
                    break;
                case VendorPaymentReceived1MTL:
                    currRecipient = currTemplate.getRecipientsToTransmit().get(0).get(0);

                    assertEquals("Company ID", QBDT_BP_COMPANY_PSID, currRecipient.getCompanyId());
                    assertEquals("Recipient Email", "PayrollAdmin2@email.com", currRecipient.getRecipientEmail());
                    assertEquals("Recipient Name", "Steve Payrolladmin2", currRecipient.getRecipientName());
                    recipientProperties = currRecipient.getProperties();

                    assertEquals("Number Of Email Properties ", 15, recipientProperties.size());
                    assertEquals("Property1 Value ", "Steve", recipientProperties.getProperty("PayrollAdminFirstName"));
                    assertEquals("Property2 Value ", "Payrolladmin2", recipientProperties.getProperty("PayrollAdminLastName"));
                    assertEquals("Property3 Value ", "Dreams Come True, Inc", recipientProperties.getProperty("CompanyLegalName"));
                    assertEquals("Property4 Value ", "PayrollAdmin2@email.com", recipientProperties.getProperty("PayrollAdminEmail"));
                    assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
                    assertEquals("Property6 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
                    assertEquals("Property7 Value ", "September 12, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
                    assertEquals("Property8 Value ", "12:00 AM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
                    assertEquals("Property9 Value ", "September 10, 2007", recipientProperties.getProperty("PayrollRunDate"));
                    assertTrue("Property10 Value ", recipientProperties.getProperty("VendorPaymentList") != null);
                    assertEquals("Property11 Value ", "September 11, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));
                    assertEquals("Property12 Value ", "$0.08",recipientProperties.getProperty("SalesTaxAmount"));
                    assertEquals("Property13 Value ", "$24.50",recipientProperties.getProperty("IntuitHandlingFee"));
                    assertEquals("Property14 Value ", "$2,056.30",recipientProperties.getProperty("PaymentAmount"));

            }
        }


        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBPOffloadedInvalidEmailAddress_WriteChecks() {
        beforeEachTestQBDT_BP();
        testBPOffloadedInvalidEmailAddress();
    }

    @Test
    public void testBPOffloadedInvalidEmailAddress_PayBills() {
        beforeEachTestQBDT_BP_PayBills();
        testBPOffloadedInvalidEmailAddress();
    }

    public void testBPOffloadedInvalidEmailAddress() {

        PayrollServices.beginUnitOfWork();
        Company c = assertOne(Application.<Company>find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));

        Payee p = assertOne(Application.<Payee>find(Payee.class, Payee.SourcePayeeId().equalTo("Payee1")));

        PayeeDTO payeeDTO = new PayeeDTO();
        payeeDTO.setEmail("James@intuit.com; Platt@Intuit.com;Invalid");
        payeeDTO.setIs1099(p.getIs1099());
        payeeDTO.setName(p.getName());
        payeeDTO.setPhone(p.getPhone());
        payeeDTO.setSourcePayeeId(p.getSourcePayeeId());
        payeeDTO.setTaxId(p.getTaxId());
        assertSuccess(PayrollServices.billPaymentManager.addOrUpdatePayee(c.getSourceSystemCd(), c.getSourceCompanyId(), payeeDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Payee payee2 = assertOne(Application.<Payee>find(Payee.class, Payee.SourcePayeeId().equalTo("Payee2")));
        PayeeDTO payeeDTOWoEmail = new PayeeDTO();
        payeeDTOWoEmail.setEmail("James;John");
        payeeDTOWoEmail.setIs1099(payee2.getIs1099());
        payeeDTOWoEmail.setName(payee2.getName());
        payeeDTOWoEmail.setPhone(payee2.getPhone());
        payeeDTOWoEmail.setSourcePayeeId(payee2.getSourcePayeeId());
        payeeDTOWoEmail.setTaxId(payee2.getTaxId());
        assertSuccess(PayrollServices.billPaymentManager.addOrUpdatePayee(c.getSourceSystemCd(), c.getSourceCompanyId(), payeeDTOWoEmail));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Payee payee3 = assertOne(Application.<Payee>find(Payee.class, Payee.SourcePayeeId().equalTo("Payee3")));
        PayeeDTO payeeDTOEmptyEmail = new PayeeDTO();
        payeeDTOEmptyEmail.setEmail("");
        payeeDTOEmptyEmail.setIs1099(payee3.getIs1099());
        payeeDTOEmptyEmail.setName(payee3.getName());
        payeeDTOEmptyEmail.setPhone(payee3.getPhone());
        payeeDTOEmptyEmail.setSourcePayeeId(payee3.getSourcePayeeId());
        payeeDTOEmptyEmail.setTaxId(payee3.getTaxId());
        assertSuccess(PayrollServices.billPaymentManager.addOrUpdatePayee(c.getSourceSystemCd(), c.getSourceCompanyId(), payeeDTOEmptyEmail));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Payee payee4 = assertOne(Application.<Payee>find(Payee.class, Payee.SourcePayeeId().equalTo("Payee4")));
        PayeeDTO payeeDTOInvalidEmail = new PayeeDTO();
        payeeDTOInvalidEmail.setEmail("John");
        payeeDTOInvalidEmail.setIs1099(payee4.getIs1099());
        payeeDTOInvalidEmail.setName(payee4.getName());
        payeeDTOInvalidEmail.setPhone(payee4.getPhone());
        payeeDTOInvalidEmail.setSourcePayeeId(payee4.getSourcePayeeId());
        payeeDTOInvalidEmail.setTaxId(payee4.getTaxId());
        assertSuccess(PayrollServices.billPaymentManager.addOrUpdatePayee(c.getSourceSystemCd(), c.getSourceCompanyId(), payeeDTOInvalidEmail));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2007, 9, 11));

        PayrollServices.beginUnitOfWork();

        BillPayment bp = assertOne(Application.<BillPayment>find(BillPayment.class, BillPayment.Payee().equalTo(p)));

        for (BillPaymentSplit bps : bp.getBillPaymentSplitCollection()) {
            DomainEntitySet<CompanyEventDetail> ced = Application.<CompanyEventDetail>find(CompanyEventDetail.class, CompanyEventDetail.Value().equalTo(bps.getId().toString()).And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.BillPaymentOffloaded)));
            CompanyEvent ce = ced.get(0).getCompanyEvent();
            assertEquals(1, ce.getCompanyEventEmailCollection().size());
        }

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        int vendorInvalidEmailTemplateIndex = getTemplateIndex(EventEmailTemplateTypeCode.VendorInvalidEmail, currEmailTemplates);
        assertTrue("Template Id Found", vendorInvalidEmailTemplateIndex >= 0);
        List<List<IEventEmail>> currRecipients = currEmailTemplates.get(vendorInvalidEmailTemplateIndex).getRecipientsToTransmit();

        IEventEmail currRecipient = currRecipients.get(0).get(0);

        assertEquals("Number of Recipients", 6, currRecipients.get(0).size());
        assertEquals("Company ID", QBDT_BP_COMPANY_PSID, currRecipient.getCompanyId());
        Properties recipientProperties = currRecipient.getProperties();
        String payeeFirstName = recipientProperties.getProperty("PayrollAdminFirstName").trim();
        assertEquals("Recipient Email", "PayrollAdmin2@email.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", recipientProperties.getProperty("PayrollAdminFirstName") + " "
                + recipientProperties.getProperty("PayrollAdminLastName"), currRecipient.getRecipientName());


        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());
        assertEquals("Property1 Value ", payeeFirstName, recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin2", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Dreams Come True, Inc", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "September 12, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertNotNull(recipientProperties.getProperty("VendorInvalidEmailAddress"));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testVendorPaymentSignupConfirmation_OnlyPayrollAdmin() {
        beforeEachTestQBDT_BP_SignUp(null);

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());

        for (EventEmailTemplate currTemplate : currEmailTemplates) {
            IEventEmail currRecipient;
            Properties recipientProperties;

            switch (currTemplate.getTemplateId()) {

                case VendorPaymentSignupConfirmation:
                    currRecipient = currTemplate.getRecipientsToTransmit().get(0).get(0);
                    int numberORecipients = currTemplate.getRecipientsToTransmit().get(0).size();
                    assertEquals("Company ID", QBDT_BP_COMPANY_PSID, currRecipient.getCompanyId());
                    assertEquals("Number of Recipients", 1, numberORecipients);

                    recipientProperties = currRecipient.getProperties();
                    assertEquals("Number Of Email Properties ", 6, recipientProperties.size());
                    assertEquals("Property1 Value ", "Steve", recipientProperties.getProperty("PayrollAdminFirstName"));
                    assertEquals("Property2 Value ", "Payrolladmin2", recipientProperties.getProperty("PayrollAdminLastName"));
                    assertEquals("Property3 Value ", "Dreams Come True, Inc", recipientProperties.getProperty("CompanyLegalName"));
                    //assertEquals("Property4 Value ", "PayrollAdmin2@email.com", recipientProperties.getProperty("PayrollAdminEmail"));
                    assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
                    assertEquals("Property6 Value ", "Direct Deposit for Vendors", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
                    break;


            }
        }


        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVendorPaymentSignupConfirmation_PayrollAdmin_PrimaryPrincipal() {
        beforeEachTestQBDT_BP_SignUp(ContactRole.SecondaryPrincipal);

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());

        for (EventEmailTemplate currTemplate : currEmailTemplates) {
            IEventEmail currRecipient;
            Properties recipientProperties;

            switch (currTemplate.getTemplateId()) {

                case VendorPaymentSignupConfirmation:
                    currRecipient = currTemplate.getRecipientsToTransmit().get(0).get(0);
                    int numberORecipients = currTemplate.getRecipientsToTransmit().get(0).size();
                    assertEquals("Company ID", QBDT_BP_COMPANY_PSID, currRecipient.getCompanyId());
                    assertEquals("Number of Recipients", 2, numberORecipients);

                    recipientProperties = currRecipient.getProperties();
                    assertEquals("Number Of Email Properties ", 6, recipientProperties.size());
                    assertEquals("Property3 Value ", "Dreams Come True, Inc", recipientProperties.getProperty("CompanyLegalName"));
                    //assertEquals("Property4 Value ", "PayrollAdmin2@email.com", recipientProperties.getProperty("PayrollAdminEmail"));
                    assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
                    assertEquals("Property6 Value ", "Direct Deposit for Vendors", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
                    break;


            }
        }


        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVendorPaymentOffloadedEmail() {
        beforeEachTestQBDT_BP();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());

        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.VendorPaymentOffloadedForWriteChecks, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        List<List<IEventEmail>> currRecipients = currEmailTemplates.get(templateIndex).getRecipientsToTransmit();

        IEventEmail currRecipient = currRecipients.get(0).get(0);

        assertEquals("Number if Recipients", 28, currRecipients.get(0).size());
        assertEquals("Company ID", QBDT_BP_COMPANY_PSID, currRecipient.getCompanyId());
        Properties recipientProperties = currRecipient.getProperties();
        String payeeFirstName = recipientProperties.getProperty("PayrollAdminFirstName").substring(0, 7).trim();
        assertEquals("Recipient Email", payeeFirstName + "@hotmail.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", payeeFirstName + " Name. ,", currRecipient.getRecipientName());


        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());
        assertEquals("Property1 Value ", payeeFirstName + " Name.", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", ",", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Dreams Come True, Inc", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "September 12, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertNotNull(recipientProperties.getProperty("VendorBankAccountLastFour"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVendorPaymentOffloadedEmail_PayBills() {
        beforeEachTestQBDT_BP_PayBills();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());

        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.VendorPaymentOffloadedForPayBills, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        List<List<IEventEmail>> currRecipients = currEmailTemplates.get(templateIndex).getRecipientsToTransmit();

        IEventEmail currRecipient = currRecipients.get(0).get(0);

        assertEquals("Number if Recipients", 28, currRecipients.get(0).size());
        assertEquals("Company ID", QBDT_BP_COMPANY_PSID, currRecipient.getCompanyId());
        Properties recipientProperties = currRecipient.getProperties();
        String payeeFirstName = recipientProperties.getProperty("PayrollAdminFirstName").substring(0, 7).trim();
        assertEquals("Recipient Email", payeeFirstName + "@hotmail.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", payeeFirstName + " Name. ,", currRecipient.getRecipientName());


        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", payeeFirstName + " Name.", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", ",", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Dreams Come True, Inc", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "September 12, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertNotNull(recipientProperties.getProperty("Memo"));
        assertNotNull(recipientProperties.getProperty("ReferenceNumber"));
        assertNotNull(recipientProperties.getProperty("VendorAccountNumber"));
        assertNotNull(recipientProperties.getProperty("VendorBankAccountLastFour"));
        assertNotNull(recipientProperties.getProperty("PaymentAmount"));


        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDDBankVerificationSuccessful() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(templateIndex).getTemplateId());

        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDDBankVerificationSuccessful_BankAccountVerifiedTwice() {
        if(getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);

        // add a second BankAccountVerified event to see if PINCreated email processes correctly
        // (first was added in 'before' work).
        CompanyEvent.createCBAVerifiedEvent(cba);

        // add a second PINCreated event to see if only one email is created
        // (first was added in 'before' work).
        CompanyEvent.createPINCreatedEvent(company);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> eventList;

        eventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.BankAccountVerified, null, null, null);

        // make sure we really have two BankAccountVerified events
        assertEquals("Number of BankAccountVerified events", 2, eventList.size());

        eventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.PINCreated, null, null, null);

        // make sure we really have two PINCreated events
        assertEquals("Number of PINCreated events", 2, eventList.size());

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        // just check for email count and types here since email params are checked elsewhere for each of these types
        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmation, currEmailTemplates.get(3).getTemplateId());

        PayrollServices.commitUnitOfWork();

        // check to see if any errors exist for pending emails
        String emailErrorLogName = checkEmailTemplatesForErrors();

        // emailErrorLogName should be null, but in case it isn't, print the log name to stdout
        if (emailErrorLogName != null) {
            System.out.println("Error log name: " + emailErrorLogName);
        }

        assertTrue("Email error log is empty", emailErrorLogName == null);
    }

    @Test
    public void testDDBankVerificationSuccessful_BankAccountVerifiedTwiceMTL() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);

        // add a second BankAccountVerified event to see if PINCreated email processes correctly
        // (first was added in 'before' work).
        CompanyEvent.createCBAVerifiedEvent(cba);

        // add a second PINCreated event to see if only one email is created
        // (first was added in 'before' work).
        CompanyEvent.createPINCreatedEvent(company);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> eventList;

        eventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.BankAccountVerified, null, null, null);

        // make sure we really have two BankAccountVerified events
        assertEquals("Number of BankAccountVerified events", 2, eventList.size());

        eventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.PINCreated, null, null, null);

        // make sure we really have two PINCreated events
        assertEquals("Number of PINCreated events", 2, eventList.size());

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        // just check for email count and types here since email params are checked elsewhere for each of these types
        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmationMTL, currEmailTemplates.get(3).getTemplateId());

        PayrollServices.commitUnitOfWork();

        // check to see if any errors exist for pending emails
        String emailErrorLogName = checkEmailTemplatesForErrors();

        // emailErrorLogName should be null, but in case it isn't, print the log name to stdout
        if (emailErrorLogName != null) {
            System.out.println("Error log name: " + emailErrorLogName);
        }

        assertTrue("Email error log is empty", emailErrorLogName == null);
    }

    /**
     * Test to ensure no QBOE Emails while signup, successful bank verification and payroll confirmation
     */
    @Test
    public void testPayrollConfirmation_QBOE() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number Of Emails", 0, currEmailTemplates.size());
    }

    @Test
    public void testDeleteAfterOffload() {
        if(getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTest();
        PayrollServices.beginUnitOfWork();
        CompanyDDPlus401kDataLoader ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        ddAnd401kDL.deleteAfterOffload();

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.TOKVoidDelete, currEmailTemplates.get(4).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(4).getRecipientsToTransmit().get(0);

        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8575577", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "psp_ach_offload_notify_test@intuit.com", currRecipient.getRecipientEmail());
        //Ken says this doesn't actually appear anywhere in the email
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "psp_ach_offload_notify_test@intuit.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "242335465", recipientProperties.getProperty("CompanyEIN"));
        assertEquals("Property6 Value ", "2/18/2010", recipientProperties.getProperty("PayPeriodBeginDate"));
        assertEquals("Property7 Value ", "2/26/2010", recipientProperties.getProperty("PayPeriodEndDate"));
        assertEquals("Property8 Value ", "FirstNameOfEE1", recipientProperties.getProperty("EmployeeFirstName"));
        assertEquals("Property9 Value ", "TestLastName", recipientProperties.getProperty("EmployeeLastName"));
        assertEquals("Property10 Value ", "Deleted", recipientProperties.getProperty("VoidOrDelete"));
        assertEquals("Property11 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDeleteAfterOffloadMTL() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTest();
        PayrollServices.beginUnitOfWork();
        CompanyDDPlus401kDataLoader ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        ddAnd401kDL.deleteAfterOffload();

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.TOKVoidDelete, currEmailTemplates.get(3).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(3).getRecipientsToTransmit().get(0);

        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8575577", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "psp_ach_offload_notify_test@intuit.com", currRecipient.getRecipientEmail());
        //Ken says this doesn't actually appear anywhere in the email
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "psp_ach_offload_notify_test@intuit.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "242335465", recipientProperties.getProperty("CompanyEIN"));
        assertEquals("Property6 Value ", "2/18/2010", recipientProperties.getProperty("PayPeriodBeginDate"));
        assertEquals("Property7 Value ", "2/26/2010", recipientProperties.getProperty("PayPeriodEndDate"));
        assertEquals("Property8 Value ", "FirstNameOfEE1", recipientProperties.getProperty("EmployeeFirstName"));
        assertEquals("Property9 Value ", "TestLastName", recipientProperties.getProperty("EmployeeLastName"));
        assertEquals("Property10 Value ", "Deleted", recipientProperties.getProperty("VoidOrDelete"));
        assertEquals("Property11 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test401kCompanyGoesOnFraudHold() {
        beforeEachTest();
        PayrollServices.beginUnitOfWork();
        CompanyDDPlus401kDataLoader ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        ddAnd401kDL.persistQBCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ddAnd401kDL.loadPayrollItems();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult onHoldSetResult = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT, "8575577", ServiceSubStatusCode.PendingTermination);
        assertSuccess(onHoldSetResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        int numberOfTOKNotifiedOfFraudHoldEvents = CompanyEvent.getEventCountByType(company, EventTypeCode.TOKNotifiedOfCompanyFraud);
        assertEquals("Number of active TOK notified events", 0, numberOfTOKNotifiedOfFraudHoldEvents);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        onHoldSetResult = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT, "8575577", ServiceSubStatusCode.Fraud);
        assertSuccess(onHoldSetResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        numberOfTOKNotifiedOfFraudHoldEvents = CompanyEvent.getEventCountByType(company, EventTypeCode.TOKNotifiedOfCompanyFraud);
        assertEquals("Number of active TOK notified events", 1, numberOfTOKNotifiedOfFraudHoldEvents);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVoidAfterOffload() {
        if(getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTest();
        PayrollServices.beginUnitOfWork();
        CompanyDDPlus401kDataLoader ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult procResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8575577", ddAnd401kDL.get401kWarningCloudPayrollRunDTO());
        assertSuccess(procResult2);
        PayrollServices.commitUnitOfWork();

        ddAnd401kDL.makePaychecksOffloadable();
        ddAnd401kDL.voidAfterOffload();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.TOKVoidDelete, currEmailTemplates.get(4).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(4).getRecipientsToTransmit().get(0);

        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8575577", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "psp_ach_offload_notify_test@intuit.com", currRecipient.getRecipientEmail());
        //Ken says this doesn't actually appear anywhere in the email
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "psp_ach_offload_notify_test@intuit.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "242335465", recipientProperties.getProperty("CompanyEIN"));
        assertEquals("Property6 Value ", "2/18/2010", recipientProperties.getProperty("PayPeriodBeginDate"));
        assertEquals("Property7 Value ", "2/26/2010", recipientProperties.getProperty("PayPeriodEndDate"));
        assertEquals("Property8 Value ", "FirstNameOfEE1", recipientProperties.getProperty("EmployeeFirstName"));
        assertEquals("Property9 Value ", "TestLastName", recipientProperties.getProperty("EmployeeLastName"));
        assertEquals("Property10 Value ", "Voided", recipientProperties.getProperty("VoidOrDelete"));
        assertEquals("Property11 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVoidAfterOffloadMTL() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTest();
        PayrollServices.beginUnitOfWork();
        CompanyDDPlus401kDataLoader ddAnd401kDL = new CompanyDDPlus401kDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 1, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ddAnd401kDL.persistHappyPathPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        ProcessResult procResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8575577", ddAnd401kDL.get401kWarningCloudPayrollRunDTO());
        assertSuccess(procResult2);
        PayrollServices.commitUnitOfWork();

        ddAnd401kDL.makePaychecksOffloadable();
        ddAnd401kDL.voidAfterOffload();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.TOKVoidDelete, currEmailTemplates.get(3).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(3).getRecipientsToTransmit().get(0);

        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8575577", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "psp_ach_offload_notify_test@intuit.com", currRecipient.getRecipientEmail());
        //Ken says this doesn't actually appear anywhere in the email
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "psp_ach_offload_notify_test@intuit.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "242335465", recipientProperties.getProperty("CompanyEIN"));
        assertEquals("Property6 Value ", "2/18/2010", recipientProperties.getProperty("PayPeriodBeginDate"));
        assertEquals("Property7 Value ", "2/26/2010", recipientProperties.getProperty("PayPeriodEndDate"));
        assertEquals("Property8 Value ", "FirstNameOfEE1", recipientProperties.getProperty("EmployeeFirstName"));
        assertEquals("Property9 Value ", "TestLastName", recipientProperties.getProperty("EmployeeLastName"));
        assertEquals("Property10 Value ", "Voided", recipientProperties.getProperty("VoidOrDelete"));
        assertEquals("Property11 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPayrollConfirmation_QBDT() {
        if(getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmation, currEmailTemplates.get(3).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(3).getRecipientsToTransmit().get(0);

        Collections.sort(emails, new Comparator<IEventEmail>() {
            public int compare(IEventEmail email1, IEventEmail email2) {
                String amount1 = email1.getProperties().getProperty("PayrollDebitAmount");
                String amount2 = email2.getProperties().getProperty("PayrollDebitAmount");
                return amount1.compareToIgnoreCase(amount2);
            }
        });

        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "12:00 AM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1123.96)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property11 Value ", "September 10, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));

        currRecipient = emails.get(1);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "September 17, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "12:00 AM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(10203.63)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property11 Value ", "September 14, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testPayrollConfirmation_QBDTMTL() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmationMTL, currEmailTemplates.get(4).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(4).getRecipientsToTransmit().get(0);

        Collections.sort(emails, new Comparator<IEventEmail>() {
            public int compare(IEventEmail email1, IEventEmail email2) {
                String amount1 = email1.getProperties().getProperty("PayrollDebitAmount");
                String amount2 = email2.getProperties().getProperty("PayrollDebitAmount");
                return amount1.compareToIgnoreCase(amount2);
            }
        });

        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 17, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "12:00 AM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1123.96)).toString())), recipientProperties.getProperty("PaymentAmount"));
        assertEquals("Property11 Value ", "$0.16" , recipientProperties.getProperty("SalesTaxAmount"));
        assertEquals("Property12 Value ", "$5.90", recipientProperties.getProperty("IntuitHandlingFee"));
        assertTrue(recipientProperties.getProperty("EmployeeList") != null);
        assertEquals("Property13 Value ", "Donovan McNabb<br/>Abe Lincoln<br/>", recipientProperties.getProperty("EmployeeList"));
        assertEquals("Property14 Value ", "$1,120.80" , recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property15 Value ", "DIY" , recipientProperties.getProperty("ServiceType"));

        currRecipient = emails.get(1);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 17, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "September 17, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "12:00 AM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(10203.63)).toString())), recipientProperties.getProperty("PaymentAmount"));
        assertEquals("Property11 Value ", "September 14, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));
        assertEquals("Property11 Value ", "$0.08" , recipientProperties.getProperty("SalesTaxAmount"));
        assertEquals("Property12 Value ", "$2.90", recipientProperties.getProperty("IntuitHandlingFee"));
        assertTrue( recipientProperties.getProperty("EmployeeList") != null);
        assertEquals("Property13 Value ", "Donovan McNabb<br/>Abe Lincoln<br/>", recipientProperties.getProperty("EmployeeList"));
        assertEquals("Property14 Value ", "$10,203.55" , recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property15 Value ", "DIY" , recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();

    }

    /**
     * This test will ensure no email is created for a company that has no payroll admin contact
     */
    @Test
    public void testPayrollConfirmation_QBDT_NoPayrollAdmin() {
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        // create company
        PayrollServices.beginUnitOfWork();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");

        Company company = companyQB1DataLoader.persistQBCompany1();

        // create company
        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();

        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // change the payroll admin contact to 'Other'
        PayrollServices.beginUnitOfWork();

        Contact contact = company.getContactByRoleCode(ContactRole.PayrollAdmin);

        contact.setContactRoleCd(ContactRole.Other);

        Application.save(contact);

        PayrollServices.commitUnitOfWork();

        // create payroll run for company
        PayrollServices.beginUnitOfWork();

        PayrollRunDTO payrollRunDTO = companyQB1DataLoader.getCompany1PayrollRunDTO();

        PSPDate.setPSPTime("20071022000000");

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);

        PayrollServices.commitUnitOfWork();

        // ensure there is no payroll confirmation email (since there is no payroll admin)
        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNoPayrollConfirmationEmail_ExceedsDDLimits_QBDT() {
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");

        Company company = companyQB1DataLoader.persistQBCompany1();

        // create company
        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();

        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // create payroll run for company with exceeding DDLimits
        PayrollServices.beginUnitOfWork();

        PayrollRunDTO payrollRunDTO = companyQB1DataLoader.get3rdCompany2PR_ExceedsOldLimits();

        PSPDate.setPSPTime("20071022000000");

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * test to verify no payroll confirmation email is generated when payroll violates the maximum number of employees
     * paid to the same bank account.
     */
    @Test
    public void testNoPayrollConfirmationEmail_MoreEmployeesPaidToSameBankAccount_QBDT() {
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");

        CompanyDTO companyDTO = companyQB1DataLoader.getCompany1();
        companyDTO.setNextEmployeeId("1");
        companyDTO.setNextPaycheckId("1");
        companyDTO.setNextPayrollItemId("1");
        companyDTO.setNextPayrollTransactionId("1");
        companyDTO.getLegalAddress().setCity("Honolulu");
        companyDTO.getLegalAddress().setState("HI");
        companyDTO.getLegalAddress().setZipCode("96813");

        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();

        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

        companyDTO.setQuickBooksInfo(qbInfoDTO);

        DataLoader dataloader = new DataLoader();

        Company company = dataloader.persistCompany(companyDTO);

        dataloader.persistCompanyService(company, companyQB1DataLoader.getCompany1Service());

        DataLoader dl = new DataLoader();

        CompanyBankAccount bankAccount1 =
                dl.persistCompanyBankAccount(company, companyQB1DataLoader.getCompany1BankAccount());

        companyQB1DataLoader.persistCompanyPIN();

        // create company
        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();

        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        company = Application.findById(Company.class, company.getId());
        bankAccount1 = Application.findById(CompanyBankAccount.class, bankAccount1.getId());

        Company1Dataloader c1dl = new Company1Dataloader();

        c1dl.setCompany1(company);
        c1dl.setBankAccount1(bankAccount1);

        Employee employee1 = c1dl.persistEmployee(c1dl.getEmployee1(company));
        Employee employee2 = c1dl.persistEmployee(c1dl.getEmployee2(company));
        Employee employee3 = c1dl.persistEmployee(c1dl.getEmployee3(company));
        Employee employee4 = c1dl.persistEmployee(c1dl.getEmployee4(company));

        EmployeeBankAccount eeba1 = c1dl.persistEEBA(company, employee1, c1dl.getEmployee1BankAccount());
        EmployeeBankAccount eeba2 = c1dl.persistEEBA(company, employee2, c1dl.getEmployee1BankAccount());

        EmployeeBankAccountDTO eebaDTO3 = c1dl.getEmployee1BankAccount();
        eebaDTO3.setEmployeeBankAccountId("EEBA3");
        EmployeeBankAccount eeba3 = c1dl.persistEEBA(company, employee3, eebaDTO3);

        EmployeeBankAccountDTO eebaDTO4 = c1dl.getEmployee1BankAccount();
        eebaDTO4.setEmployeeBankAccountId("EEBA4");
        EmployeeBankAccount eeba4 = c1dl.persistEEBA(company, employee4, eebaDTO4);

        c1dl.setEmployee1(employee1);
        c1dl.setEmployee2(employee2);
        c1dl.setEmployee3(employee3);
        c1dl.setEmployee4(employee4);
        c1dl.setEmployeeBankAccount1(eeba1);
        c1dl.setEmployeeBankAccount2(eeba2);
        c1dl.setEmployeeBankAccount3(eeba3);
        c1dl.setEmployeeBankAccount4(eeba4);

        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_OneBAFourEEs(new DateDTO("2007-10-02"));

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNoPayrollConfirmationEmail_CBAInActive_QBDT() {
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");

        Company company = companyQB1DataLoader.persistQBCompany1();

        // create company
        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();

        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // deactivate company bank account
        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);

        ProcessResult<CompanyBankAccount> deactivateCbaResult =
                PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBDT,
                        QBDT_COMPANY_PSID,
                        cba.getSourceBankAccountId(),
                        false, false);

        PayrollServices.commitUnitOfWork();

        assertSuccess(deactivateCbaResult);

        // create payroll run for company with inactive cba
        PayrollServices.beginUnitOfWork();

        PayrollRunDTO payrollRunDTO = companyQB1DataLoader.getCompany1PayrollRunDTO();

        PSPDate.setPSPTime("20071022000000");

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPayrollConfirmation_TaxExemption_QBDT() throws Exception {
        if(getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        QBDTTestHelper.typicalRunBeforeEachTest();

        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addYears(1);

        companyDTO.setTaxExemptStatus(TaxExemptStatusCode.Exempt);
        companyDTO.setTaxExemptExpirationDate(new DateDTO(date));

        ProcessResult<Company> result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, companyDTO);

        PayrollServices.commitUnitOfWork();

        assertSuccess(result);

        QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmation, currEmailTemplates.get(3).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(3).getRecipientsToTransmit().get(0);

        Collections.sort(emails, new Comparator<IEventEmail>() {
            public int compare(IEventEmail email1, IEventEmail email2) {
                String amount1 = email1.getProperties().getProperty("PayrollDebitAmount");
                String amount2 = email2.getProperties().getProperty("PayrollDebitAmount");
                return amount1.compareToIgnoreCase(amount2);
            }
        });

        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "12:00 AM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1123.8)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property11 Value ", "September 10, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testPayrollConfirmation_TaxExemption_QBDTMTL() throws Exception {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        QBDTTestHelper.typicalRunBeforeEachTest();

        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addYears(1);

        companyDTO.setTaxExemptStatus(TaxExemptStatusCode.Exempt);
        companyDTO.setTaxExemptExpirationDate(new DateDTO(date));

        ProcessResult<Company> result =
                PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, companyDTO);

        PayrollServices.commitUnitOfWork();

        assertSuccess(result);

        QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmationMTL, currEmailTemplates.get(4).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(4).getRecipientsToTransmit().get(0);

        Collections.sort(emails, new Comparator<IEventEmail>() {
            public int compare(IEventEmail email1, IEventEmail email2) {
                String amount1 = email1.getProperties().getProperty("PayrollDebitAmount");
                String amount2 = email2.getProperties().getProperty("PayrollDebitAmount");
                return amount1.compareToIgnoreCase(amount2);
            }
        });

        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 17, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "12:00 AM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", "$1,120.80", recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property11 Value ", "September 10, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));
        assertEquals("Property12 Value ", "$0.00" , recipientProperties.getProperty("SalesTaxAmount"));
        assertEquals("Property13 Value ", "$5.90", recipientProperties.getProperty("IntuitHandlingFee"));
        assertTrue(recipientProperties.getProperty("EmployeeList") != null);
        assertEquals("Property14 Value ", "Donovan McNabb<br/>Abe Lincoln<br/>", recipientProperties.getProperty("EmployeeList"));
        assertEquals("Property15 Value ", "$1,120.80" , recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property16 Value ", "DIY" , recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testPayrollConfirmation_BackDatedPayroll_QBDT() {
        if(getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");

        Company company = companyQB1DataLoader.persistQBCompany1();

        // create company
        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();

        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // submit a backdated payroll
        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime("20070901163000");

        PayrollRunDTO payrollRunDTO = companyQB1DataLoader.getCompany1PayrollRunDTO();
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO("2007-08-25"));

        ProcessResult<PayrollRun> submitPayrollResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);

        PayrollServices.commitUnitOfWork();

        assertSuccess(submitPayrollResult);

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmation, currEmailTemplates.get(3).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(3).getRecipientsToTransmit().get(0);
        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "September 6, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "4:30 PM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(250.08)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property11 Value ", "September 5, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testPayrollConfirmation_BackDatedPayroll_QBDTMTL() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");

        Company company = companyQB1DataLoader.persistQBCompany1();

        // create company
        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();

        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // submit a backdated payroll
        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime("20070901163000");

        PayrollRunDTO payrollRunDTO = companyQB1DataLoader.getCompany1PayrollRunDTO();
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO("2007-08-25"));

        ProcessResult<PayrollRun> submitPayrollResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);

        PayrollServices.commitUnitOfWork();

        assertSuccess(submitPayrollResult);

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmationMTL, currEmailTemplates.get(4).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(4).getRecipientsToTransmit().get(0);
        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 17, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "September 6, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "4:30 PM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", "$250.00", recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property11 Value ", "September 5, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));
        assertEquals("Property12 Value ", "$0.08" , recipientProperties.getProperty("SalesTaxAmount"));
        assertEquals("Property13 Value ", "$3.50", recipientProperties.getProperty("IntuitHandlingFee"));
        assertTrue(recipientProperties.getProperty("EmployeeList") != null);
        assertEquals("Property14 Value ", "ThirdCompEEFirst ThirdCompEELast<br/>ThirdCompEEFirstTwo ThirdCompEELastTwo<br/>", recipientProperties.getProperty("EmployeeList"));
        assertEquals("Property15 Value ", "$250.00" , recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property16 Value ", "DIY" , recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testPayrollConfirmation_After5PM_QBDT() {
        if(getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");

        Company company = companyQB1DataLoader.persistQBCompany1();

        // create company
        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();

        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // submit a backdated payroll
        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime("20070901173000");

        PayrollRunDTO payrollRunDTO = companyQB1DataLoader.getCompany1PayrollRunDTO();

        ProcessResult<PayrollRun> submitPayrollResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);

        PayrollServices.commitUnitOfWork();

        assertSuccess(submitPayrollResult);

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmation, currEmailTemplates.get(3).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(3).getRecipientsToTransmit().get(0);
        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "October 2, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "5:30 PM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(250.08)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property11 Value ", "October 1, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testPayrollConfirmation_After5PM_QBDTMTL() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");

        Company company = companyQB1DataLoader.persistQBCompany1();

        // create company
        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();

        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // submit a backdated payroll
        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime("20070901173000");

        PayrollRunDTO payrollRunDTO = companyQB1DataLoader.getCompany1PayrollRunDTO();

        ProcessResult<PayrollRun> submitPayrollResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);

        PayrollServices.commitUnitOfWork();

        assertSuccess(submitPayrollResult);

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmationMTL, currEmailTemplates.get(4).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(4).getRecipientsToTransmit().get(0);
        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 17, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "October 2, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "5:30 PM Pacific Time", recipientProperties.getProperty("PayrollRunTime"));
        assertEquals("Property9 Value ", "September 1, 2007", recipientProperties.getProperty("PayrollRunDate"));
        assertEquals("Property10 Value ", "$250.00", recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property11 Value ", "October 1, 2007", recipientProperties.getProperty("PayrollDebitSettlementDate"));
        assertEquals("Property12 Value ", "$0.08" , recipientProperties.getProperty("SalesTaxAmount"));
        assertEquals("Property13 Value ", "$3.50", recipientProperties.getProperty("IntuitHandlingFee"));
        assertTrue(recipientProperties.getProperty("EmployeeList") != null);
        assertEquals("Property14 Value ", "ThirdCompEEFirst ThirdCompEELast<br/>ThirdCompEEFirstTwo ThirdCompEELastTwo<br/>", recipientProperties.getProperty("EmployeeList"));
        assertEquals("Property16 Value ", "DIY" , recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testNonACHPaymentReceivedInFull_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");

        // record NON-ACH Redebit
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);

        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();

        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("1250.00"),
                new DateDTO("2007-09-13"),
                SettlementTypeDTO.Wire);

        allRedebits.add(currRedebitImpoundDTO);

        ProcessResult procResult =
                PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        allRedebits);

        assertSuccess(procResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());

        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testNonACHPaymentReceivedInFull_PartialRedebit_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");

        // add a partial redebit
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);

        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();

        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("78.38"),
                new DateDTO("2007-09-14"),
                SettlementTypeDTO.ACH);

        allRedebits.add(currRedebitImpoundDTO);

        ProcessResult procResult =
                PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        allRedebits);

        assertSuccess(procResult);

        PayrollServices.commitUnitOfWork();

        // offload the partial redebit
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // complete the partial redebit transaction
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20070925");
        PayrollServices.commitUnitOfWork();

        // record wire the remaining amount
        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);

        allRedebits = new ArrayList<RedebitImpoundDTO>();

        currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("1125.92"),
                new DateDTO("2007-09-25"),
                SettlementTypeDTO.Wire);

        allRedebits.add(currRedebitImpoundDTO);

        procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                allRedebits);

        assertSuccess(procResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Number Of Emails", 8, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1, currEmailTemplates.get(templateIndex).getTemplateId());

        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        // verify manual redebit
        templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);

        assertEquals("Template ID", EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates.get(templateIndex).getTemplateId());
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "$78.38", recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "September 17, 2007", recipientProperties.getProperty("RedebitSettlementDate"));
        assertEquals("Property9 Value ", "September 21, 2007", recipientProperties.getProperty("RedebitCompletedDate"));
        assertEquals("Property10 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property11 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonACHPaymentReceivedInFull_PartialWire_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");

        // add a partial wire
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        CollectionStage domainCollectionStage = PayrollServices.entityFinder.findById(CollectionStage.class, CollectionStageCode.FirstCollectionAttempt);
        ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO(payrollRun.getSourcePayRunId(),
                new DateDTO("2007-09-14"), domainCollectionStage, ActionEventCode.DDRedebitEdit, false);
        ProcessResult modifyWireExpectedProcResult =
                PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, wireExpectedDTO);

        PayrollServices.commitUnitOfWork();
        assertSuccess(modifyWireExpectedProcResult);

        PayrollServices.beginUnitOfWork();

        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();

        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("103.38"),
                new DateDTO("2007-09-13"),
                SettlementTypeDTO.Wire);

        allRedebits.add(currRedebitImpoundDTO);

        ProcessResult procResult =
                PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        allRedebits);

        assertSuccess(procResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 8, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.NonACHPMTReceivedLiabOutstanding1, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.NonACHPMTReceivedLiabOutstanding1, currEmailTemplates.get(templateIndex).getTemplateId());

        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1120.66)).toString())), recipientProperties.getProperty("CompanyBalanceDue"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));


        PayrollServices.commitUnitOfWork();

        // record wire with the remaining amount
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);

        allRedebits = new ArrayList<RedebitImpoundDTO>();

        currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("1152.13"),
                new DateDTO("2007-09-13"),
                SettlementTypeDTO.Wire);

        allRedebits.add(currRedebitImpoundDTO);

        procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                allRedebits);

        assertSuccess(procResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 9, currEmailTemplates.size());
        templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1, currEmailTemplates.get(templateIndex).getTemplateId());

        currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonACHPaymentReceivedInFull_QBOE() {
        beforeEachTestQBOE();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBOEEmployerReturn("R01", "This is an NSF description");
        // record NON-ACH Redebit
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBOE_COMPANY_PSID,
                SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("300.00"), new DateDTO("2007-09-28"), SettlementTypeDTO.Wire);
        allRedebits.add(currRedebitImpoundDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), allRedebits);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 1, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1, currEmailTemplates.get(0).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testNonACHPaymentReceivedInFullActionRequired_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");
        // add three strikes
        PayrollServices.beginUnitOfWork();

        //Create one that is > 12 months in the past
        ProcessResult<CompanyEvent> strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike1",
                SpcfCalendar.createInstance(2006, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike2",
                SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike3",
                SpcfCalendar.createInstance(2007, 9, 2, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike4",
                SpcfCalendar.createInstance(2007, 9, 3, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        PayrollServices.commitUnitOfWork();
        // record NON-ACH Redebit
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);

        int strikeCount = com.intuit.sbd.payroll.psp.domain.util.EmailUtils.getCompanyStrikeCount(company);
        assertEquals("", 4, strikeCount);

        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("1250.00"), new DateDTO("2007-09-13"), SettlementTypeDTO.Wire);
        allRedebits.add(currRedebitImpoundDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), allRedebits);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.NonACHPaymentReceivedInFullActionRequired, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.NonACHPaymentReceivedInFullActionRequired, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 17, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
        assertEquals("Property8 Value ", "4", recipientProperties.getProperty("NumberOfStrikes"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonACHPaymentReceivedInFullActionRequired_QBOE() {
        beforeEachTestQBOE();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBOEEmployerReturn("R01", "This is an NSF description");
        // add three strikes
        PayrollServices.beginUnitOfWork();

        //Create one that is > 12 months in the past
        ProcessResult<CompanyEvent> strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBOE, QBOE_COMPANY_PSID, "Strike1",
                SpcfCalendar.createInstance(2006, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBOE, QBOE_COMPANY_PSID, "Strike2",
                SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBOE, QBOE_COMPANY_PSID, "Strike3",
                SpcfCalendar.createInstance(2007, 9, 2, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBOE, QBOE_COMPANY_PSID, "Strike4",
                SpcfCalendar.createInstance(2007, 9, 3, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        PayrollServices.commitUnitOfWork();
        // record NON-ACH Redebit
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBOE_COMPANY_PSID,
                SourceSystemCode.QBOE);

        int strikeCount = com.intuit.sbd.payroll.psp.domain.util.EmailUtils.getCompanyStrikeCount(company);
        assertEquals("", 4, strikeCount);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("300.00"), new DateDTO("2007-09-28"), SettlementTypeDTO.Wire);
        allRedebits.add(currRedebitImpoundDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), allRedebits);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 1, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.NonACHPaymentReceivedInFullActionRequired, currEmailTemplates.get(0).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "October 2, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
        assertEquals("Property8 Value ", "4", recipientProperties.getProperty("NumberOfStrikes"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonACHPaymentReceivedLiabilityOutstanding_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");
        // record NON-ACH Redebit
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("1125.92"), new DateDTO("2007-09-13"), SettlementTypeDTO.Wire);
        allRedebits.add(currRedebitImpoundDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), allRedebits);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.NonACHPMTReceivedLiabOutstanding1, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.NonACHPMTReceivedLiabOutstanding1, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(98.12)).toString())), recipientProperties.getProperty("CompanyBalanceDue"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonACHPaymentReceived_AnotherPayrollLiabilityOutstanding_QBDT() {
        beforeEachTestQBDT();

        // create an ER return for payroll 1
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");

        // create an ER return for payroll 2
        Application.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company).sort(PayrollRun.PaycheckDate());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRuns.get(1).getSourcePayRunId());
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerDdDebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        // record NON-ACH Redebit
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("1250.00"), new DateDTO("2007-09-13"), SettlementTypeDTO.Wire);
        allRedebits.add(currRedebitImpoundDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), allRedebits);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.NonACHPMTReceivedLiabOutstanding1, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.NonACHPMTReceivedLiabOutstanding1, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", "8574536", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(4)).add(SpcfDecimal.createInstance(10177.67)).toString())), recipientProperties.getProperty("CompanyBalanceDue"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testNonACHPaymentReceivedLiabilityOutstanding_QBOE() {
        beforeEachTestQBOE();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBOEEmployerReturn("R02", "This is an NSF description");
        // record NON-ACH Redebit
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBOE_COMPANY_PSID,
                SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO currRedebitImpoundDTO = new RedebitImpoundDTO(originalDebitTransaction.getId().toString(),
                new SpcfMoney("180.00"), new DateDTO("2007-09-28"), SettlementTypeDTO.Wire);
        allRedebits.add(currRedebitImpoundDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), allRedebits);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 1, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.NonACHPMTReceivedLiabOutstanding1, currEmailTemplates.get(0).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "$100.00", recipientProperties.getProperty("CompanyBalanceDue"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBilledNonPayrollRelatedFee_QBDT() {
        beforeEachTestQBDT();

        // offload all
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070917000000");
        Application.commitUnitOfWork();


        // add fee transaction
        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        date.setValues(2007, 9, 25);

        setCurrentPrincipal();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.QBDT,
                QBDT_COMPANY_PSID,
                payrollRun.getSourcePayRunId(), SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(date), new SpcfMoney("50"),
                OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        FinancialTransaction feeTxn = processResult.getResult().getFirst();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // offload fee transaction
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create rebill fee transaction
        PayrollServices.beginUnitOfWork();
        RebillFeeTransactionDTO rebillFeeTransactionDTO = new RebillFeeTransactionDTO(feeTxn.getId().toString(), new SpcfMoney(SpcfDecimal.createInstance("100")));

        ProcessResult<DomainEntitySet<BillingDetail>> rebillResult = PayrollServices.financialTransactionManager.rebillFeeTransaction(rebillFeeTransactionDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(rebillResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.BilledNonPayrollRelatedFee2);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 18, 2007", recipientProperties.getProperty("NonPayrollFeeSettlementDate"));
        assertEquals("Property7 Value ", "Billed: $50.00 for Reversal Fee on September 18, 2007 <br>Billed: $0.08 for Reversal Fee (Sales Tax) on September 18, 2007 <br>", recipientProperties.getProperty("BilledFeeList"));
        assertEquals("Property8 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property9 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBilledNonPayrollRelatedFee_QBOE() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        Application.commitUnitOfWork();

        setCurrentPrincipal();

        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        date.setValues(2007, 10, 10);

        PayrollServices.beginUnitOfWork();
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.QBOE,
                QBOE_COMPANY_PSID,
                "BatchTest05", SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(date), new SpcfMoney("50"),
                OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        FinancialTransaction feeTxn = processResult.getResult().getFirst();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // offload fee transaction
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create rebill fee transaction
        PayrollServices.beginUnitOfWork();
        RebillFeeTransactionDTO rebillFeeTransactionDTO = new RebillFeeTransactionDTO(feeTxn.getId().toString(), new SpcfMoney(SpcfDecimal.createInstance("45")));

        ProcessResult<DomainEntitySet<BillingDetail>> rebillResult = PayrollServices.financialTransactionManager.rebillFeeTransaction(rebillFeeTransactionDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(rebillResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        IEventEmail currRecipient = null;
        for (EventEmailTemplate eventEmailTemplate : currEmailTemplates) {
            if (EventEmailTemplateTypeCode.BilledNonPayrollRelatedFee2.equals(eventEmailTemplate.getTemplateId())) {
                currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
            }
        }
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "October 2, 2007", recipientProperties.getProperty("NonPayrollFeeSettlementDate"));
        assertEquals("Property7 Value ", "Billed: $50.00 for Reversal Fee on October 2, 2007 <br>", recipientProperties.getProperty("BilledFeeList"));
        assertEquals("Property8 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property9 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBilledNonPayrollRelatedFee_ProcessError_NoEmail() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        setCurrentPrincipal();

        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        date.setValues(2007, 10, 10);

        PayrollServices.beginUnitOfWork();
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.QBOE,
                QBOE_COMPANY_PSID,
                "Invalid", SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(date), new SpcfMoney("50"),
                OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        FinancialTransaction feeTxn = processResult.getResult() == null ? null : processResult.getResult().getFirst();
        PayrollServices.commitUnitOfWork();
        assertTrue(!processResult.isSuccess());

        // offload fee transaction
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create rebill fee transaction
        PayrollServices.beginUnitOfWork();
        RebillFeeTransactionDTO rebillFeeTransactionDTO = new RebillFeeTransactionDTO("00000000-0000-0000-0000-000000000000", new SpcfMoney(SpcfDecimal.createInstance("45")));

        ProcessResult<DomainEntitySet<BillingDetail>> rebillResult = PayrollServices.financialTransactionManager.rebillFeeTransaction(rebillFeeTransactionDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(!rebillResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        // verify there is no BilledNonPayrollRelatedFee email
        assertEquals("Number Of Emails", 0, currEmailTemplates.size());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRefundedFeeAmount_QBDT() {
        beforeEachTestQBDT();

        // offload all
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070917000000");
        Application.commitUnitOfWork();


        // add fee transaction
        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        date.setValues(2007, 9, 25);

        setCurrentPrincipal();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.QBDT,
                QBDT_COMPANY_PSID,
                payrollRun.getSourcePayRunId(), SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(date), new SpcfMoney("50"),
                OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        FinancialTransaction feeTxn = processResult.getResult().getFirst();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // offload fee transaction
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // create refund fee transaction
        PayrollServices.beginUnitOfWork();
        ERRefundDTO refundDTO = new ERRefundDTO();
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney(SpcfMoney.createInstance("100")));
        refundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
        refundDTO.setFinancialTxId(feeTxn.getId().toString());
        ProcessResult<FinancialTransaction> result = PayrollServices.financialTransactionManager.refundEmployerTransaction(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, refundDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.RefundedFeeAmount1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Refunded: $50.00 for Reversal Fee on September 25, 2007<br>", recipientProperties.getProperty("RefundedFeeList"));
        assertEquals("Property7 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRefundedFeeAmount_QBOE() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        setCurrentPrincipal();

        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        date.setValues(2007, 10, 10);

        PayrollServices.beginUnitOfWork();
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.QBOE,
                QBOE_COMPANY_PSID,
                "BatchTest05", SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(date), new SpcfMoney("50"),
                OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        FinancialTransaction feeTxn = processResult.getResult().getFirst();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // offload fee transaction
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        Application.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create refund fee transaction
        PayrollServices.beginUnitOfWork();
        ERRefundDTO refundDTO = new ERRefundDTO();
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney(SpcfMoney.createInstance("100")));
        refundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
        refundDTO.setFinancialTxId(feeTxn.getId().toString());
        ProcessResult<FinancialTransaction> result = PayrollServices.financialTransactionManager.refundEmployerTransaction(
                SourceSystemCode.QBOE, QBOE_COMPANY_PSID, refundDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.RefundedFeeAmount1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Refunded: $50.00 for Reversal Fee on October 10, 2007<br>", recipientProperties.getProperty("RefundedFeeList"));
        assertEquals("Property7 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRefundWithRebillFeeAmount_QBDT() {
        beforeEachTestQBDT();

        // offload all
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070914000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        // add fee transaction
        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        date.setValues(2007, 9, 25);

        setCurrentPrincipal();

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.QBDT,
                QBDT_COMPANY_PSID,
                payrollRun.getSourcePayRunId(), SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(date), new SpcfMoney("50"),
                OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        FinancialTransaction feeTxn = processResult.getResult().getFirst();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // offload fee transaction
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create rebill fee transaction
        PayrollServices.beginUnitOfWork();
        RebillFeeTransactionDTO rebillFeeTransactionDTO = new RebillFeeTransactionDTO(feeTxn.getId().toString(), new SpcfMoney(SpcfDecimal.createInstance("100")));

        ProcessResult<DomainEntitySet<BillingDetail>> rebillResult = PayrollServices.financialTransactionManager.rebillFeeTransaction(rebillFeeTransactionDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(rebillResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.RefundWithRebillFeeAmount1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Billed: $100.08 for Reversal Fee will occur on September 25, 2007<br>", recipientProperties.getProperty("BilledFeeList"));
        assertEquals("Property7 Value ", "Refunded: $50.08 for Reversal Fee will occur on September 25, 2007<br>", recipientProperties.getProperty("RefundedFeeList"));
        assertEquals("Property8 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property9 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRefundWithRebillFeeAmount_QBOE() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        setCurrentPrincipal();

        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        date.setValues(2007, 10, 10);

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.QBOE,
                QBOE_COMPANY_PSID,
                "BatchTest05", SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(date), new SpcfMoney("50"),
                OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        FinancialTransaction feeTxn = processResult.getResult().getFirst();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // offload fee transaction
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create rebill fee transaction
        PayrollServices.beginUnitOfWork();
        RebillFeeTransactionDTO rebillFeeTransactionDTO = new RebillFeeTransactionDTO(feeTxn.getId().toString(), new SpcfMoney(SpcfDecimal.createInstance("45")));

        ProcessResult<DomainEntitySet<BillingDetail>> rebillResult = PayrollServices.financialTransactionManager.rebillFeeTransaction(rebillFeeTransactionDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(rebillResult);


        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.RefundWithRebillFeeAmount1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Billed: $45.00 for Reversal Fee will occur on October 10, 2007<br>", recipientProperties.getProperty("BilledFeeList"));

        assertEquals("Property7 Value ", "Refunded: $50.00 for Reversal Fee will occur on October 10, 2007<br>", recipientProperties.getProperty("RefundedFeeList"));
        assertEquals("Property8 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property9 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAllPaycheckReversalsSuccessful_QBDT() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);

        // offload reversal
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20070928");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.AllPaycheckReversalsSuccessful1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PrimaryPrincipal@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Primaryprincipal", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Primaryprincipal", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "Abe L&#8217;s direct deposit in the amount $153.11 was successfully reversed on September 17, 2007<br>" +
                        "Abe L&#8217;s direct deposit in the amount $40.00 was successfully reversed on September 17, 2007<br>" +
                        "Donovan M&#8217;s direct deposit in the amount $927.69 was successfully reversed on September 17, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("Property7 Value ", "October 1, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("Property8 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property9 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPartialPayrollReversalsSuccessful_QBOE() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a partial payroll reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBOE_COMPANY_PSID, SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();
        types.add(TransactionTypeCode.EmployeeDdCredit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> eeTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        eeTxns = eeTxns.sort(FinancialTransaction.FinancialTransactionAmount());

        List<String> txnsToReverse = new ArrayList<String>();
        txnsToReverse.add(eeTxns.get(0).getPaycheckSplit().getSourceDdTxnId());

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId("BatchTest05");
        txnReverseDTO.setDdTransactionIdList(txnsToReverse);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);

        // offload reversal
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20071010");
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.AllPaycheckReversalsSuccessful1, currEmailTemplates.get(0).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "FirstNameOfEE1 T&#8217;s direct deposit in the amount $30.00 was successfully reversed on October 2, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("Property7 Value ", "October 11, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("Property8 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property9 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAllPaycheckReversalsFailed_QBDT() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
//        txnReverseDTO.setDdTransactionIdList(txnsToReverse);
//        txnReverseDTO.setTxDate(null);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);

        // offload reversal
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        returnEEReversals(company, payrollRun.getSourcePayRunId(), true);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20070928");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.AllPaycheckReversalsFailed1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PrimaryPrincipal@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Primaryprincipal", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Primaryprincipal", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "Abe L&#8217;s direct deposit in the amount $153.11 could not be reversed because Insufficient Funds<br>" +
                        "Abe L&#8217;s direct deposit in the amount $40.00 could not be reversed because Insufficient Funds<br>" +
                        "Donovan M&#8217;s direct deposit in the amount $927.69 could not be reversed because Insufficient Funds<br>",
                recipientProperties.getProperty("ReversalFailedList"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAllPaycheckReversalsFailed_QBOE() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a partial payroll reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBOE_COMPANY_PSID, SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();
        types.add(TransactionTypeCode.EmployeeDdCredit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> eeTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        eeTxns = eeTxns.sort(FinancialTransaction.FinancialTransactionAmount());

        List<String> txnsToReverse = new ArrayList<String>();
        txnsToReverse.add(eeTxns.get(1).getPaycheckSplit().getSourceDdTxnId());

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId("BatchTest05");
        txnReverseDTO.setDdTransactionIdList(txnsToReverse);
//        txnReverseDTO.setTxDate(null);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);


        // offload reversal
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // return a reversal
        returnEEReversals(company, "BatchTest05", true);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20071010");
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.AllPaycheckReversalsFailed1, currEmailTemplates.get(0).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "FirstNameOfEE2 T&#8217;s direct deposit in the amount $150.00 could not be reversed because Insufficient Funds<br>",
                recipientProperties.getProperty("ReversalFailedList"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPartialPaycheckReversal_QBDT() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
//        txnReverseDTO.setDdTransactionIdList(txnsToReverse);
//        txnReverseDTO.setTxDate(null);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);

        // offload reversal
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        returnEEReversals(company, payrollRun.getSourcePayRunId(), false);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20070928");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.PartialPaycheckReversal1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PrimaryPrincipal@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Primaryprincipal", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Primaryprincipal", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "Abe L&#8217;s direct deposit in the amount $40.00 could not be reversed because Insufficient Funds<br>",
                recipientProperties.getProperty("ReversalFailedList"));
        assertEquals("Property7 Value ",
                "Abe L&#8217;s direct deposit in the amount $153.11 was successfully reversed on September 17, 2007<br>" +
                        "Donovan M&#8217;s direct deposit in the amount $927.69 was successfully reversed on September 17, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("Property8 Value ", "October 1, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("Property9 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property10 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPartialPaycheckReversal_QBOE() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBOE_COMPANY_PSID, SourceSystemCode.QBOE);

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId("BatchTest05");
//        txnReverseDTO.setDdTransactionIdList(txnsToReverse);
//        txnReverseDTO.setTxDate(null);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);


        // offload reversal
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // return a reversal
        returnEEReversals(company, "BatchTest05", false);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20071010");
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.PartialPaycheckReversal1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "FirstNameOfEE1 T&#8217;s direct deposit in the amount $30.00 could not be reversed because Insufficient Funds<br>",
                recipientProperties.getProperty("ReversalFailedList"));
        assertEquals("Property7 Value ",
                "FirstNameOfEE2 T&#8217;s direct deposit in the amount $150.00 was successfully reversed on October 2, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("Property8 Value ", "October 11, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("Property9 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property10 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testTwoStagePartialPaycheckReversal() {
        // don't call default pre-test for QBOE - we need special payroll for this test (four paychecks)
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        // create company and payroll run for company
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_4PC_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // set the psp time to allow offload of verification transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        // offload verification transactions
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // set the psp time to allow offload of payroll transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        // offload payroll transactions
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //
        // We want to stagger the reversals on different days to ensure the email gateway will correctly
        // differentiate them into two different groups, resulting in two different emails with the appropriate dates.
        //

        // Request a reversal for two txns on 10/02
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBOE_COMPANY_PSID, SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTestFourPaychecks");
        List<String> ddTransactionIdList = new Vector<String>();
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployeeDdCredit);
        states.add(TransactionStateCode.Executed);

        DomainEntitySet<FinancialTransaction> eeCredits = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states)
                .sort(FinancialTransaction.FinancialTransactionAmount());

        ddTransactionIdList.add(eeCredits.get(0).getPaycheckSplit().getSourceDdTxnId());
        ddTransactionIdList.add(eeCredits.get(1).getPaycheckSplit().getSourceDdTxnId());

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId("BatchTestFourPaychecks");
        txnReverseDTO.setDdTransactionIdList(ddTransactionIdList);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.
                reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);

        PayrollServices.commitUnitOfWork();

        assertSuccess(reverseTxnProcResult);

        // offload reversal transactions for 10/02
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a reversal for one txn on 10/03
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071003000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        eeCredits = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states)
                .sort(FinancialTransaction.FinancialTransactionAmount());

        ddTransactionIdList.clear();
        ddTransactionIdList.add(eeCredits.get(2).getPaycheckSplit().getSourceDdTxnId());

        txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId("BatchTestFourPaychecks");
        txnReverseDTO.setDdTransactionIdList(ddTransactionIdList);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        reverseTxnProcResult = PayrollServices.payrollManager.
                reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);

        PayrollServices.commitUnitOfWork();

        assertSuccess(reverseTxnProcResult);

        // offload reversal transaction for 10/03
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        List<EventEmailTemplate> emailTemplateList;
        List<List<IEventEmail>> recipientList;
        Properties recipientProperties;
        String emailErrorLogName;
        IEventEmail recipient;

        //
        // Ensure the reversal requested emails are properly generated
        //

        // generate email templates
        emailTemplateList = findEmailTemplates();

        assertEquals("Number of email templates", 1, emailTemplateList.size());

        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(emailTemplateList, EventEmailTemplateTypeCode.CustomerInitiatedDDReversal1);

        recipientList = eventEmailTemplate.getRecipientsToTransmit();

        assertTrue("Recipient list not null", recipientList != null);
        assertEquals("Number of email recipient lists", 1, recipientList.size());
        assertEquals("Number of email recipients", 1, recipientList.get(0).size());

        recipient = recipientList.get(0).get(0);

        assertEquals("Company id", QBOE_COMPANY_PSID, recipient.getCompanyId());
        assertEquals("Recipient email", "someEmail@aol.com", recipient.getRecipientEmail());
        assertEquals("Recipient name", "John Doe", recipient.getRecipientName());

        recipientProperties = recipient.getProperties();

        assertEquals("Number of email properties", 7, recipientProperties.size());
        assertEquals("PrimaryPrincipalFirstName", "John", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("PrimaryPrincipalLastName", "Doe", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("CompanyLegalName", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("PrimaryPrincipalEmail", "someEmail@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("SourcePayrollSystem", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("ReversalPendingList",
                "FirstNameOfEE1 T&#8217;s direct deposit in the amount $10.00 will be reversed<br>" +
                        "FirstNameOfEE1 T&#8217;s direct deposit in the amount $100.00 will be reversed<br>" +
                        "FirstNameOfEE2 T&#8217;s direct deposit in the amount $20.00 will be reversed<br>",
                recipientProperties.getProperty("ReversalPendingList"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        // check to see if any errors exist for pending emails
        emailErrorLogName = checkEmailTemplatesForErrors();

        // emailErrorLogName should be null, but in case it isn't, print the log name to stdout
        if (emailErrorLogName != null) {
            System.out.println("Error log name: " + emailErrorLogName);
        }

        assertTrue("Email error log is empty", emailErrorLogName == null);

        //
        // Return one of the reversals to confirm the email gateway will properly place it into a status of
        // 'GroupIncomplete' and not send an email prematurely.
        // (at this point, no ReversalOK events exist, only the single ReversalReturn event)
        // (this must be tested before the ach txn processor 'Completes' txns)
        //

        // return a reversal
        returnEEReversals(company, "BatchTestFourPaychecks", false);

        // generate email templates (there should be none at this point)
        emailTemplateList = findEmailTemplates();

        assertEquals("Number of email templates", 0, emailTemplateList.size());

        // check to see if any errors exist for pending emails
        emailErrorLogName = checkEmailTemplatesForErrors();

        // emailErrorLogName should be null, but in case it isn't, print the log name to stdout
        if (emailErrorLogName != null) {
            System.out.println("Error log name: " + emailErrorLogName);
        }

        assertTrue("Email error log is empty", emailErrorLogName == null);

        // check to ensure ReversalReturn company event has an email_status of 'GroupIncomplete'
        DomainEntitySet<CompanyEventEmail> reversalReturnEmailEvents = Application.find(CompanyEventEmail.class,
                CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.AllPaycheckReversalsFailed1));

        assertEquals("ReversalReturn company event count", 1, reversalReturnEmailEvents.size());
        assertEquals("ReversalReturn company event email status",
                EventEmailStatus.GroupIncomplete,
                reversalReturnEmailEvents.get(0).getStatusCd());
        assertNotNull("CompanyEventEmail company null",reversalReturnEmailEvents.get(0).getCompany());

        //
        // Finally, run the ach txn processor to 'Complete' all txns and check for appropriate reversal notifications
        // (do for correct dates to ensure email content will reflect the correct dates)
        //

        // set the psp date to allow 10/02 executed transactions to be cleared
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        // complete executed transactions from 10/02
        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20071010");
        PayrollServices.commitUnitOfWork();

        // set the psp date to allow 10/03 executed transactions to be cleared
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        Application.commitUnitOfWork();

        // complete executed transactions from 10/03
        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20071011");
        PayrollServices.commitUnitOfWork();

        // generate email templates
        emailTemplateList = findEmailTemplates();

        assertEquals("Number of email templates", 2, emailTemplateList.size());

        eventEmailTemplate = findEventEmailTemplate(emailTemplateList, EventEmailTemplateTypeCode.AllPaycheckReversalsSuccessful1);

        recipientList = eventEmailTemplate.getRecipientsToTransmit();

        assertTrue("Recipient list not null", recipientList != null);
        assertEquals("Number of email recipient lists", 1, recipientList.size());
        assertEquals("Number of email recipients", 1, recipientList.get(0).size());

        recipient = recipientList.get(0).get(0);

        assertEquals("Company ID", QBOE_COMPANY_PSID, recipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", recipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", recipient.getRecipientName());

        recipientProperties = recipient.getProperties();

        assertEquals("Number Of Email Properties", 9, recipientProperties.size());
        assertEquals("PrimaryPrincipalFirstName", "John", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("PrimaryPrincipalLastName", "Doe", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("CompanyLegalName", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("PrimaryPrincipalEmail", "someEmail@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("SourcePayrollSystem", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("EffectiveCreditPostingDate", "October 12, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("CompanyBankAccountLastFour", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("ReversalSuccessfulList",
                "FirstNameOfEE1 T&#8217;s direct deposit in the amount $100.00 was successfully reversed on October 4, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("ServiceType", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        eventEmailTemplate = findEventEmailTemplate(emailTemplateList, EventEmailTemplateTypeCode.PartialPaycheckReversal1);

        recipientList = eventEmailTemplate.getRecipientsToTransmit();

        assertTrue("Recipient list not null", recipientList != null);
        assertEquals("Number of email recipient lists", 1, recipientList.size());
        assertEquals("Number of email recipients", 1, recipientList.get(0).size());

        recipient = recipientList.get(0).get(0);

        assertEquals("Company ID", QBOE_COMPANY_PSID, recipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", recipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", recipient.getRecipientName());

        recipientProperties = recipient.getProperties();

        assertEquals("Number Of Email Properties", 10, recipientProperties.size());
        assertEquals("PrimaryPrincipalFirstName", "John", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("PrimaryPrincipalLastName", "Doe", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("CompanyLegalName", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("PrimaryPrincipalEmail", "someEmail@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("SourcePayrollSystem", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("EffectiveCreditPostingDate", "October 11, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("CompanyBankAccountLastFour", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("ReversalFailedList",
                "FirstNameOfEE1 T&#8217;s direct deposit in the amount $10.00 could not be reversed because Insufficient Funds<br>",
                recipientProperties.getProperty("ReversalFailedList"));
        assertEquals("ReversalSuccessfulList",
                "FirstNameOfEE2 T&#8217;s direct deposit in the amount $20.00 was successfully reversed on October 3, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("ServiceType", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        // check to see if any errors exist for pending emails
        emailErrorLogName = checkEmailTemplatesForErrors();

        // emailErrorLogName should be null, but in case it isn't, print the log name to stdout
        if (emailErrorLogName != null) {
            System.out.println("Error log name: " + emailErrorLogName);
        }

        assertTrue("Email error log is empty", emailErrorLogName == null);
    }

    @Test
    public void testUntimelyReturnOfReversalDebit_QBDT() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
//        txnReverseDTO.setDdTransactionIdList(txnsToReverse);
//        txnReverseDTO.setTxDate(null);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);

        // offload reversal
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify emails
        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.AllPaycheckReversalsSuccessful1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PrimaryPrincipal@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Primaryprincipal", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Primaryprincipal", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "Abe L&#8217;s direct deposit in the amount $153.11 was successfully reversed on September 17, 2007<br>" +
                        "Abe L&#8217;s direct deposit in the amount $40.00 was successfully reversed on September 17, 2007<br>" +
                        "Donovan M&#8217;s direct deposit in the amount $927.69 was successfully reversed on September 17, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("Property7 Value ", "October 1, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("Property8 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property9 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();

        // check to see if any errors exist for pending emails
        String emailErrorLogName = checkEmailTemplatesForErrors();

        // emailErrorLogName should be null, but in case it isn't, print the log name to stdout
        if (emailErrorLogName != null) {
            System.out.println("Error log name: " + emailErrorLogName);
        }

        assertTrue("Email error log is empty", emailErrorLogName == null);

        // now return one reversal
        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();
        types.add(TransactionTypeCode.EmployeeDdReversalDebit);
        states.add(TransactionStateCode.Completed);
        DomainEntitySet<FinancialTransaction> eeReversals = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        eeReversals = eeReversals.sort(FinancialTransaction.FinancialTransactionAmount());
        while (eeReversals.size() > 1) {
            eeReversals.remove(eeReversals.size() - 1);
        }

        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(eeReversals, "R01", "NSF Return");

        PayrollServices.beginUnitOfWork();
        currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 1, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.PartialPaycheckReversal1, currEmailTemplates.get(0).getTemplateId());
        currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PrimaryPrincipal@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Primaryprincipal", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Primaryprincipal", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "Abe L&#8217;s direct deposit in the amount $40.00 could not be reversed because Insufficient Funds<br>",
                recipientProperties.getProperty("ReversalFailedList"));
        assertEquals("Property7 Value ",
                "Abe L&#8217;s direct deposit in the amount $153.11 was successfully reversed on September 17, 2007<br>" +
                        "Donovan M&#8217;s direct deposit in the amount $927.69 was successfully reversed on September 17, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("Property8 Value ", "October 1, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("Property9 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property10 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        // check to see if any errors exist for pending emails
        emailErrorLogName = checkEmailTemplatesForErrors();

        // emailErrorLogName should be null, but in case it isn't, print the log name to stdout
        if (emailErrorLogName != null) {
            System.out.println("Error log name: " + emailErrorLogName);
        }

        assertTrue("Email error log is empty", emailErrorLogName == null);

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testUntimelyReversalReturns_QBOE() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a partial payroll reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBOE_COMPANY_PSID, SourceSystemCode.QBOE);
//        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId("BatchTest05");
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);

        // offload reversal
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20071010");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.AllPaycheckReversalsSuccessful1, currEmailTemplates.get(0).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "FirstNameOfEE1 T&#8217;s direct deposit in the amount $30.00 was successfully reversed on October 2, 2007<br>" +
                        "FirstNameOfEE2 T&#8217;s direct deposit in the amount $150.00 was successfully reversed on October 2, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("Property7 Value ", "October 11, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("Property8 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property9 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();

        // check to see if any errors exist for pending emails
        String emailErrorLogName = checkEmailTemplatesForErrors();

        // emailErrorLogName should be null, but in case it isn't, print the log name to stdout
        if (emailErrorLogName != null) {
            System.out.println("Error log name: " + emailErrorLogName);
        }

        assertTrue("Email error log is empty", emailErrorLogName == null);

        // now return one reversal
        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();
        types.add(TransactionTypeCode.EmployeeDdReversalDebit);
        states.add(TransactionStateCode.Completed);
        DomainEntitySet<FinancialTransaction> eeReversals = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        eeReversals = eeReversals.sort(FinancialTransaction.FinancialTransactionAmount());
        while (eeReversals.size() > 1) {
            eeReversals.remove(eeReversals.size() - 1);
        }

        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(eeReversals, "R02", "Account Closed");

        PayrollServices.beginUnitOfWork();
        currEmailTemplates = findEmailTemplates();
        assertEquals("Number Of Emails", 1, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.PartialPaycheckReversal1, currEmailTemplates.get(0).getTemplateId());
        currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "FirstNameOfEE1 T&#8217;s direct deposit in the amount $30.00 could not be reversed because Account Closed<br>",
                recipientProperties.getProperty("ReversalFailedList"));
        assertEquals("Property7 Value ",
                "FirstNameOfEE2 T&#8217;s direct deposit in the amount $150.00 was successfully reversed on October 2, 2007<br>",
                recipientProperties.getProperty("ReversalSuccessfulList"));
        assertEquals("Property8 Value ", "October 11, 2007", recipientProperties.getProperty("EffectiveCreditPostingDate"));
        assertEquals("Property9 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property10 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testCustomerInitiatedDDReversal_QBDT() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
//        txnReverseDTO.setDdTransactionIdList(txnsToReverse);
//        txnReverseDTO.setTxDate(null);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.CustomerInitiatedDDReversal1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PrimaryPrincipal@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Primaryprincipal", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Primaryprincipal", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "Abe L&#8217;s direct deposit in the amount $153.11 will be reversed<br>" + "Abe L&#8217;s direct deposit in the amount $40.00 will be reversed<br>" +
                        "Donovan M&#8217;s direct deposit in the amount $927.69 will be reversed<br>",
                recipientProperties.getProperty("ReversalPendingList"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testCustomerInitiatedDDReversal_QBOE() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // Request a reaversal
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBOE_COMPANY_PSID, SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(true);
        txnReverseDTO.setIntuitInitiatedReversals(false);
        txnReverseDTO.setSourcePayrollRunId("BatchTest05");
//        txnReverseDTO.setDdTransactionIdList(txnsToReverse);
//        txnReverseDTO.setTxDate(null);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(reverseTxnProcResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 1, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.CustomerInitiatedDDReversal1, currEmailTemplates.get(0).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ",
                "FirstNameOfEE1 T&#8217;s direct deposit in the amount $30.00 will be reversed<br>" +
                        "FirstNameOfEE2 T&#8217;s direct deposit in the amount $150.00 will be reversed<br>",
                recipientProperties.getProperty("ReversalPendingList"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testDDServiceCancelledConfirmation() {
        addCompanyWithBankAccount();
        // change email
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> processResult = PayrollServices.companyManager.updateServiceStatus(
                SourceSystemCode.QBDT,
                QBDT_COMPANY_PSID,
                ServiceCode.DirectDeposit,
                ServiceSubStatusCode.Cancelled);

        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);
        // verify the emails
        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        EventEmailTemplate serviceCancelConfirmationEmail = null;
        for (EventEmailTemplate currEmailTemplate : currEmailTemplates) {
            if (currEmailTemplate.getTemplateId() == EventEmailTemplateTypeCode.ServiceCancelledConfirmation1) {
                serviceCancelConfirmationEmail = currEmailTemplate;
                break;
            }
        }
        assertNotNull(serviceCancelConfirmationEmail);

        assertEquals("Template ID", EventEmailTemplateTypeCode.ServiceCancelledConfirmation1, serviceCancelConfirmationEmail.getTemplateId());
        IEventEmail currRecipient = serviceCancelConfirmationEmail.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PrimaryPrincipal@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Primaryprincipal", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Primaryprincipal", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));

        PayrollServices.commitUnitOfWork();
    }

    // todo v2
//    public void testDDBankVerificationReminder() {
//
//    }

    @Test
    public void testDDPINChangeConfirmation() {
        QBDTTestHelper.typicalRunBeforeEachTest();
        // change email
        PayrollServices.beginUnitOfWork();

        ProcessResult<HashMap<String, String>> updatePinPR = PayrollServices.subscriptionManager.updateCompanyPIN
                (SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "efghEFGH5678");

        PayrollServices.commitUnitOfWork();

        assertSuccess(updatePinPR);
        // verify the emails
        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDPINChangeConfirmation1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDDPINChange_InvalidPIN() {
        QBDTTestHelper.typicalRunBeforeEachTest();
        // change email
        PayrollServices.beginUnitOfWork();

        ProcessResult<HashMap<String, String>> updatePinPR = PayrollServices.subscriptionManager.updateCompanyPIN
                (SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "InvalidPIN");

        PayrollServices.commitUnitOfWork();

        assertTrue(!updatePinPR.isSuccess());
        // verify the emails
        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDDERBankAccountChange() {
        QBDTTestHelper.typicalRunBeforeEachTest();
        // change email
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();
        CompanyBankAccountDTO companyBankAccount = companyQB1DataLoader.getCompany1BankAccount();
        companyBankAccount.getBankAccountDTO().setAccountNumber("4747474747");
        ProcessResult<CompanyBankAccount> companyBankAccountPR = PayrollServices.companyManager.changeCompanyBankAccount
                (SourceSystemCode.QBDT, company.getSourceCompanyId(), companyBankAccount, true, false, false);
//        company2.setNotificationEmail("modified@domain.com");

        PayrollServices.commitUnitOfWork();

        assertSuccess(companyBankAccountPR);
        // verify the emails
        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDERBankAccountChange, currEmailTemplates.get(1).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(1).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDDERBankAccountChange_OnlyName() {
        QBDTTestHelper.typicalRunBeforeEachTest();
        // change email
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();
        CompanyBankAccountDTO companyBankAccount = companyQB1DataLoader.getCompany1BankAccount();
        companyBankAccount.getBankAccountDTO().setBankName("Changed Bank");
        ProcessResult<CompanyBankAccount> companyBankAccountPR = PayrollServices.companyManager.updateCompanyBankAccount
                (SourceSystemCode.QBDT, company.getSourceCompanyId(), companyBankAccount);
//        company2.setNotificationEmail("modified@domain.com");

        PayrollServices.commitUnitOfWork();

        assertSuccess(companyBankAccountPR);
        // verify the emails
        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        // verify there is no email for DDERBankAccountChange
        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEEBankAccountAdded() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "true");
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_EBA_ADD_EMAIL_NOTIFICATION, "true");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", false,
                ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.activateDDService(company, true);
        DataLoadServices.activateTaxService(company);
        Employee employee = DataLoadServices.addEEWithBankAccount(company);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates,
                EventEmailTemplateTypeCode.DDEEBankAccountChange1);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "123456789", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().getFirst();

        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", company.getLegalName(), recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Assisted", recipientProperties.getProperty("ServiceType"));
        String employeeList = String.format("<b>%s %s</b>&#8217;s account number (ending in) <b>%s</b> was added <br>",
                employee.getFirstName(), employee.getLastName().substring(0, 1),
                com.intuit.sbd.payroll.psp.domain.util.EmailUtils.getBALastFourDigit(employeeBankAccount.getBankAccount()));
        assertEquals("Property6 Value ", employeeList, recipientProperties.getProperty("EmployeeList"));
        PayrollServices.commitUnitOfWork();
    }

    //TODO : Ravi, Email for PBA
    @Test
    public void testPBAAddAndChange() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "true");
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_PBA_ADD_EMAIL_NOTIFICATION, "true");
        PayrollServices.commitUnitOfWork();

        /* Test scenario 1 : Validate PBA added email */
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", false,
                ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.activateDDService(company, true);
        DataLoadServices.activateTaxService(company);
        Payee payee = DataLoadServices.createPayeeWithBankAccount(company);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates,
                EventEmailTemplateTypeCode.DDPayeeBankAccountChange);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "123456789", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findActivePayeeBankAccount(company, payee);

        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", company.getLegalName(), recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", SourceSystemCode.QBDT.toString(), recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Assisted", recipientProperties.getProperty("ServiceType"));
        String payeeList = String.format("<b>%s</b>&#8217;s account number (ending in) <b>%s</b> was added <br>",
                payee.getName(), com.intuit.sbd.payroll.psp.domain.util.EmailUtils.getBALastFourDigit(payeeBankAccount.getBankAccount()));
        assertEquals("Property6 Value ", payeeList, recipientProperties.getProperty("PayeeList"));
        PayrollServices.rollbackUnitOfWork();

        /* Test scenario 2 : Validate PBA Change email */
        PayrollServices.beginUnitOfWork();
        PayeeBankAccountDTO payeeBankAccountDTO = GenerateData.getPayeeBankAccountDTO(payeeBankAccount.getSourceBankAccountId());
        ProcessResult<PayeeBankAccount> processResult = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount
                (company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO);

        // Verify that no  validation errors have been returned
        assertSuccess("addOrUpdatePayeeBankAccount", processResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        currEmailTemplates = findEmailTemplates();
        eventEmailTemplate = findEventEmailTemplate(currEmailTemplates,
                EventEmailTemplateTypeCode.DDPayeeBankAccountChange);

        currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "123456789", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();

        PayeeBankAccount payeeBankAccount2 = PayeeBankAccount.findActivePayeeBankAccount(company, payee);

        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", company.getLegalName(), recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", SourceSystemCode.QBDT.toString(), recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Assisted", recipientProperties.getProperty("ServiceType"));
        payeeList = String.format("<b>%s</b>&#8217;s account number (ending in) <b>%s</b> was added <br>" +
                        "<b>%s</b>&#8217;s old account number (ending in) <b>%s</b> was changed to new account number (ending in) <b>%s</b><br>",
                payee.getName(), com.intuit.sbd.payroll.psp.domain.util.EmailUtils.getBALastFourDigit(payeeBankAccount.getBankAccount()),
                payee.getName(), com.intuit.sbd.payroll.psp.domain.util.EmailUtils.getBALastFourDigit(payeeBankAccount.getBankAccount()),
                com.intuit.sbd.payroll.psp.domain.util.EmailUtils.getBALastFourDigit(payeeBankAccount2.getBankAccount()));
        assertEquals("Property6 Value ", payeeList, recipientProperties.getProperty("PayeeList"));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEEBankAccountAdded_SwitchOff() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_EBA_ADD_EMAIL_NOTIFICATION, "false");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", false,
                ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.activateDDService(company, true);
        DataLoadServices.activateTaxService(company);
        Employee employee = DataLoadServices.addEEWithBankAccount(company);

        PayrollServices.beginUnitOfWork();
        try {
            List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
            findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDEEBankAccountChange1);
        } catch (AssertionFailedError assertionFailedError) {
            assertEquals("Template type not found: DDEEBankAccountChange1", assertionFailedError.getMessage());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_EBA_ADD_EMAIL_NOTIFICATION, "true");
        PayrollServices.commitUnitOfWork();
    }

    /**
     * When an Employee’s bank account number changes, a DDEEBankAccountChange email should be created
     */
    @Test
    public void testEEBankAccountChange_AccountNumberChanged() {
        QBDTTestHelper.typicalRunBeforeEachTest();

        // change EE bank account
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        String sourceBankAccountId = employeeBankAccount.getSourceBankAccountId();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO(sourceBankAccountId);
        ProcessResult<EmployeeBankAccount> processResult =
                PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        employee.getSourceEmployeeId(),
                        employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        // verify the emails
        PayrollServices.beginUnitOfWork();

        // verify there is an email for DDEEBankAccountChange and no email for DDERBankAccountChange
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDBankVerificationSuccessful);
        findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDSignupConfirmation);
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDEEBankAccountChange1);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));

        String regex = "(<b>[A-Za-z]+ [A-Z]</b>&#8217;s( old)? account number \\(ending in\\) <b>[0-9]{4}</b> was (added|changed) "
                + "(to new account number \\(ending in\\) <b>[0-9]{4}</b>)?<br>)+";
        assertTrue("Property6 Value ", recipientProperties.getProperty("EmployeeList").matches(regex));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    /**
     * When an Employee’s bank account number changes, a DDEEBankAccountChange email should be created - if any other
     * EE BA changes occur, no email should be created (i.e. the email should only be sent for account number changes.)
     */
    @Test
    public void testEEBankAccountChange_AccountNumberNotChanged() {
        QBDTTestHelper.typicalRunBeforeEachTest();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        Employee employee = company.getDirectDepositEmployees().get(0);
        String sourceEmployeeId = employee.getSourceEmployeeId();
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);

        // Since an email is only supposed to be generated for EE AccountNumber changes,
        // this test will only change the RoutingNumber to ensure no email is generated
        // (a company event will be created, but not an associated email)
        BankAccountDTO bankAccountDTO = BankAccountDataLoader.getBankAccountDTOFromBankAccount(employeeBankAccount.getBankAccount());
        bankAccountDTO.setRoutingNumber(BankAccountDataLoader.generateRoutingNumber());
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        employeeBankAccountDTO.setEmployeeBankAccountId(employeeBankAccount.getSourceBankAccountId());
        employeeBankAccountDTO.setBankAccount(bankAccountDTO);

        ProcessResult<EmployeeBankAccount> processResult =
                PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        employee.getSourceEmployeeId(),
                        employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        // verify the emails
        PayrollServices.beginUnitOfWork();

        // verify there is *not* an email for DDEEBankAccountChange
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * When an Employee’s bank account number changes, a DDEEBankAccountChange email should be created
     * When the PayrollAdmin email address also changes, a DDEEBankAccountChange email should be sent to the old
     * PA emails address as well (in addition to the new one.)
     */
    @Test
    public void testEEBankAccountChange_AccountNumberAndPayrollAdminChanged() {
        QBDTTestHelper.typicalRunBeforeEachTest();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();
        CompanyDTO company2 = companyQB1DataLoader.getCompany1();
        Collection<ContactDTO> pspContacts = company2.getContacts();

        for (ContactDTO contactDTO : pspContacts) {
            if (contactDTO.getContactRoleCd().equals(ContactRole.PayrollAdmin)) {
                contactDTO.setEmail("modified@domain.com");
            }
        }

        // change PayrollAdmin email and EE bank account (want in same db txn to get proper email processing)
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> processResult3 =
                PayrollServices.companyManager.updateCompany(company2.getSourceSystemCd(),
                        company2.getCompanyId(),
                        company2);

        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        String sourceBankAccountId = employeeBankAccount.getSourceBankAccountId();
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO(sourceBankAccountId);

        ProcessResult<EmployeeBankAccount> processResult1 =
                PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        employee.getSourceEmployeeId(),
                        employeeBankAccountDTO);

        employee = company.getDirectDepositEmployees().get(1);
        employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        sourceBankAccountId = employeeBankAccount.getSourceBankAccountId();
        employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO(sourceBankAccountId);

        ProcessResult<EmployeeBankAccount> processResult2 =
                PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        employee.getSourceEmployeeId(),
                        employeeBankAccountDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Update EE bank account", processResult1);
        assertSuccess("Update EE bank account", processResult2);
        assertSuccess("Update PayrollAdmin email", processResult3);

        // verify the emails
        PayrollServices.beginUnitOfWork();

        String regex = "(<b>[A-Za-z]+ [A-Z]</b>&#8217;s( old)? account number \\(ending in\\) <b>[0-9]{4}</b> was (added|changed) "
                + "(to new account number \\(ending in\\) <b>[0-9]{4}</b>)?<br>)+";

        // verify there is an email for DDEEBankAccountChange and that it’s sent to old and new PA email addresses.
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.EmailChangeNotification);
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDEEBankAccountChange1);

        List<IEventEmail> emails = eventEmailTemplate.getRecipientsToTransmit().get(0);

        assertEquals("Number Of Emails", 2, emails.size());

        Collections.sort(emails, new Comparator<IEventEmail>() {
            public int compare(IEventEmail lhs, IEventEmail rhs) {
                String email1 = lhs.getProperties().getProperty("PayrollAdminEmail");
                String email2 = rhs.getProperties().getProperty("PayrollAdminEmail");
                return email1.compareToIgnoreCase(email2);
            }
        });

        // check email to new PA email address
        IEventEmail currRecipient = emails.get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "modified@domain.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "modified@domain.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertTrue("Property6 Value ", recipientProperties.getProperty("EmployeeList").matches(regex));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        // check email to old PA email address
        currRecipient = emails.get(1);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertTrue("Property6 Value ", recipientProperties.getProperty("EmployeeList").matches(regex));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEEBankAccountChange_AssistedEmailSend() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "true");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", false,
                ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.activateDDService(company, true);
        DataLoadServices.activateTaxService(company);
        Employee employee = DataLoadServices.addEEWithBankAccount(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
        if (employeeBankAccounts.isNotEmpty()) {
            EmployeeBankAccount employeeBankAccount = employeeBankAccounts.getFirst();
            BankAccount bankAccount = employeeBankAccount.getBankAccount();
            BankAccountDTO bankAccountDTO = DataLoadServices.createBankAccount(bankAccount.getAccountTypeCd(),
                    bankAccount.getRoutingNumber(), "123456789", bankAccount.getBankName());
            EmployeeBankAccountDTO employeeBankAccountDTO = DataLoadServices.createEmployeeBankAccount(employee,
                    bankAccountDTO);
            employeeBankAccountDTO.setEmployeeBankAccountId(employeeBankAccount.getSourceBankAccountId());
            ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager
                    .updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(),
                            employee.getSourceEmployeeId(), employeeBankAccountDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates,
                EventEmailTemplateTypeCode.DDEEBankAccountChange1);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "123456789", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", company.getLegalName(), recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Assisted", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEEBankAccountChange_AssistedEmailNotSend() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "false");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", false,
                ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.activateDDService(company, true);
        DataLoadServices.activateTaxService(company);
        Employee employee = DataLoadServices.addEEWithBankAccount(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
        if (employeeBankAccounts.isNotEmpty()) {
            EmployeeBankAccount employeeBankAccount = employeeBankAccounts.getFirst();
            BankAccount bankAccount = employeeBankAccount.getBankAccount();
            BankAccountDTO bankAccountDTO = DataLoadServices.createBankAccount(bankAccount.getAccountTypeCd(),
                    bankAccount.getRoutingNumber(), "123456789", bankAccount.getBankName());
            EmployeeBankAccountDTO employeeBankAccountDTO = DataLoadServices.createEmployeeBankAccount(employee,
                    bankAccountDTO);
            employeeBankAccountDTO.setEmployeeBankAccountId(employeeBankAccount.getSourceBankAccountId());
            ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager
                    .updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(),
                            employee.getSourceEmployeeId(), employeeBankAccountDTO);

            assertSuccess(processResult);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        try {
            List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
            findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDEEBankAccountChange1);
        } catch (AssertionFailedError assertionFailedError) {
            assertEquals("Template type not found: DDEEBankAccountChange1", assertionFailedError.getMessage());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "true");
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testERBankAccountChange_DIYEmailSend() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "true");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true,
                ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates,
                EventEmailTemplateTypeCode.DDERBankAccountChange);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "123456789", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", company.getLegalName(), recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testERBankAccountChange_AssistedEmailSend() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "true");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true,
                ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates,
                EventEmailTemplateTypeCode.DDERBankAccountChangeAssisted);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", "123456789", currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", company.getLegalName(), recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Assisted", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testERBankAccountChange_AssistedEmailNotSend() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "false");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true,
                ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        try {
            findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDERBankAccountChangeAssisted);
        } catch (AssertionFailedError assertionFailedError) {
            assertEquals("Template type not found: DDERBankAccountChangeAssisted", assertionFailedError.getMessage());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager
                .updateSystemParameterValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, "true");
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEmployerNOC() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("C01", "12100035825625625651325454321");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.EmployerNOC1);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 6, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEmployeeNOC() {
        beforeEachTestQBDT();

        // create an EE return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployeeReturn("C01", "12100035825625625651325454321", false);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.EmployeeNOC2, currEmailTemplates.get(3).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(3).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        // don't check the EmployeeList property since it can vary from run to run
        //assertEquals("Property6 Value ", "<b>Abe L.<b><br>", recipientProperties.getProperty("EmployeeList"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testMultipleEmployeeNOC() {
        beforeEachTestQBDT();


        // offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        ;
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployeeDdCredit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        c1FinTxns.remove(2);
        assertEquals("Number of C1 EmployerDDDebit EX txns", 2, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "C01", "12100035825625625651325454321");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.EmployeeNOC2, currEmailTemplates.get(3).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(3).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        // don't check the EmployeeList property since it can vary from run to run
        //assertEquals("Property6 Value ", "<b>Abe L.<b><br><b>Donovan M.<b><br>", recipientProperties.getProperty("EmployeeList"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testERandEENOC() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("C01", "12100035825625625651325454321");
        // create an EE return
        FinancialTransaction originalCreditTransaction = setUpQBDTEmployeeReturn("C01", "12100035825625625651325454321", false);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());

        EventEmailTemplate nocEventEmailTemplate = null;
        for (EventEmailTemplate currEmailTemplate : currEmailTemplates) {
            if (currEmailTemplate.getTemplateId() == EventEmailTemplateTypeCode.EmployerNOC1) {
                nocEventEmailTemplate = currEmailTemplate;
                break;
            }
        }

        assertNotNull("EmployeeNOC2 Template", nocEventEmailTemplate);
        IEventEmail currRecipient = nocEventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        // don't check the EmployeeList property since it can vary from run to run
        //assertEquals("Property6 Value ", "<b>Abe L.<b><br>", recipientProperties.getProperty("EmployeeList"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEmailChangeNotification() {
        QBDTTestHelper.typicalRunBeforeEachTest();

        // change email
        PayrollServices.beginUnitOfWork();

        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();
        CompanyDTO company2 = companyQB1DataLoader.getCompany1();
        Collection<ContactDTO> pspContacts = company2.getContacts();

        for (ContactDTO contactDTO : pspContacts) {
            if (contactDTO.getContactRoleCd().equals(ContactRole.PayrollAdmin)) {
                contactDTO.setEmail("modified@domain.com");
            }
        }

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.updateCompany(company2.getSourceSystemCd(),
                company2.getCompanyId(),
                company2);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result2);

        // verify the emails
        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Email Templates", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.EmailChangeNotification, currEmailTemplates.get(3).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(3).getRecipientsToTransmit().get(0);

        assertEquals("Number Of Emails", 2, emails.size());

        Collections.sort(emails, new Comparator<IEventEmail>() {
            public int compare(IEventEmail lhs, IEventEmail rhs) {
                String email1 = lhs.getProperties().getProperty("CurrentEmail") + lhs.getRecipientEmail();
                String email2 = rhs.getProperties().getProperty("CurrentEmail") + rhs.getRecipientEmail();
                return email1.compareToIgnoreCase(email2);
            }
        });

        // make sure we're sending an email to the old address
        IEventEmail currRecipient = emails.get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "modified@domain.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "modified@domain.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PriorEmail"));
        assertEquals("Property7 Value ", "modified@domain.com", recipientProperties.getProperty("CurrentEmail"));
        assertEquals("Property8 Value ", "Johnny", recipientProperties.getProperty("EmailFirstName"));
        assertEquals("Property9 Value ", "PayrollAdmin", recipientProperties.getProperty("EmailLastName"));

        // make sure we're sending an email to the new address
        currRecipient = emails.get(1);

        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PriorEmail"));
        assertEquals("Property7 Value ", "modified@domain.com", recipientProperties.getProperty("CurrentEmail"));
        assertEquals("Property8 Value ", "Johnny", recipientProperties.getProperty("EmailFirstName"));
        assertEquals("Property9 Value ", "PayrollAdmin", recipientProperties.getProperty("EmailLastName"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBothPayrollAdminAndPrimaryPricipalEmailChange() {
        QBDTTestHelper.typicalRunBeforeEachTest();

        CompanyDTO companyDTO = new CompanyQB1DataLoader().getCompany1();

        // change both emails
        for (ContactDTO contactDTO : companyDTO.getContacts()) {
            if (contactDTO.getContactRoleCd().equals(ContactRole.PayrollAdmin)) {
                contactDTO.setEmail("modifiedadmin@domain.com");
            } else if (contactDTO.getContactRoleCd().equals(ContactRole.PrimaryPrincipal)) {
                contactDTO.setEmail("modifiedprincipal@domain.com");
            }
        }

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(companyDTO.getSourceSystemCd(),
                companyDTO.getCompanyId(),
                companyDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompany", result);

        // verify the emails
        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Email Templates", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.EmailChangeNotification, currEmailTemplates.get(3).getTemplateId());

        List<IEventEmail> emails = currEmailTemplates.get(3).getRecipientsToTransmit().get(0);

        assertEquals("Number of EmailAddress change emails", 4, emails.size());

        Collections.sort(emails, new Comparator<IEventEmail>() {
            public int compare(IEventEmail lhs, IEventEmail rhs) {
                String email1 = lhs.getProperties().getProperty("CurrentEmail") + lhs.getRecipientEmail();
                String email2 = rhs.getProperties().getProperty("CurrentEmail") + rhs.getRecipientEmail();
                return email1.compareToIgnoreCase(email2);
            }
        });

        // make sure we're sending an email to the new payroll admin address
        IEventEmail currRecipient = emails.get(0);

        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "modifiedadmin@domain.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "modifiedadmin@domain.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PriorEmail"));
        assertEquals("Property7 Value ", "modifiedadmin@domain.com", recipientProperties.getProperty("CurrentEmail"));
        assertEquals("Property8 Value ", "Johnny", recipientProperties.getProperty("EmailFirstName"));
        assertEquals("Property9 Value ", "PayrollAdmin", recipientProperties.getProperty("EmailLastName"));

        // make sure we're sending an email to the old payroll admin address
        currRecipient = emails.get(1);

        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PriorEmail"));
        assertEquals("Property7 Value ", "modifiedadmin@domain.com", recipientProperties.getProperty("CurrentEmail"));
        assertEquals("Property8 Value ", "Johnny", recipientProperties.getProperty("EmailFirstName"));
        assertEquals("Property9 Value ", "PayrollAdmin", recipientProperties.getProperty("EmailLastName"));

        // make sure we're sending an email to the new primary principal address
        currRecipient = emails.get(2);

        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "modifiedprincipal@domain.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Primaryprincipal", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Primaryprincipal", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "modifiedprincipal@domain.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PriorEmail"));
        assertEquals("Property7 Value ", "modifiedprincipal@domain.com", recipientProperties.getProperty("CurrentEmail"));
        assertEquals("Property8 Value ", "Johnny", recipientProperties.getProperty("EmailFirstName"));
        assertEquals("Property9 Value ", "PrimaryPrincipal", recipientProperties.getProperty("EmailLastName"));

        // make sure we're sending an email to the old primary principal address
        currRecipient = emails.get(3);

        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PrimaryPrincipal@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Primaryprincipal", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PrimaryPrincipalFirstName"));
        assertEquals("Property2 Value ", "Primaryprincipal", recipientProperties.getProperty("PrimaryPrincipalLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PrimaryPrincipalEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "PrimaryPrincipal@aol.com", recipientProperties.getProperty("PriorEmail"));
        assertEquals("Property7 Value ", "modifiedprincipal@domain.com", recipientProperties.getProperty("CurrentEmail"));
        assertEquals("Property8 Value ", "Johnny", recipientProperties.getProperty("EmailFirstName"));
        assertEquals("Property9 Value ", "PrimaryPrincipal", recipientProperties.getProperty("EmailLastName"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBankVerificationFailed() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        CompanyQB1DataLoader qb1dataloader = new CompanyQB1DataLoader();
        // Create Company and CompanyBankAccount
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        CompanyDTO company1 = qb1dataloader.getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");

        company1.getLegalAddress().setCity("Honolulu");
        company1.getLegalAddress().setState("HI");
        company1.getLegalAddress().setZipCode("96813");

        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();

        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

        company1.setQuickBooksInfo(qbInfoDTO);
        DataLoader dataloader = new DataLoader();
        Company company = dataloader.persistCompany(company1);
        CompanyService ddCompanyService = dataloader.persistCompanyService(company, qb1dataloader.getCompany1Service());

        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), qb1dataloader.getCompany1BankAccount(), true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070823000000");
        PayrollServices.commitUnitOfWork();

        // return the two verifications
        Application.beginUnitOfWork();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction.findFinancialTransactions(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getBankAccount(),
                TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);
        assertEquals("Number of C1 EmployerDDDebit EX txns", 2, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        // verify the emails
        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.BankVerificationFailed1);
        findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDSignupConfirmation);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBankVerificationFailed_OneTxnReturned() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        CompanyQB1DataLoader qb1dataloader = new CompanyQB1DataLoader();
        // Create Company and CompanyBankAccount
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        CompanyDTO company1 = qb1dataloader.getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");

        company1.getLegalAddress().setCity("Honolulu");
        company1.getLegalAddress().setState("HI");
        company1.getLegalAddress().setZipCode("96813");

        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();

        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

        company1.setQuickBooksInfo(qbInfoDTO);
        DataLoader dataloader = new DataLoader();
        Company company = dataloader.persistCompany(company1);
        CompanyService ddCompanyService = dataloader.persistCompanyService(company, qb1dataloader.getCompany1Service());

        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), qb1dataloader.getCompany1BankAccount(), true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070823000000");
        Application.commitUnitOfWork();

        // return the two verifications
        Application.beginUnitOfWork();
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction.findFinancialTransactions(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getBankAccount(),
                TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);
        assertEquals("Number of verification transactions", 2, c1FinTxns.size());
        c1FinTxns.remove(1);
        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        // verify the emails
        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 4, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.BankVerificationFailed1);
        findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.DDSignupConfirmation);

        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAutoRedebit() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.AutoRedebit3, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.AutoRedebit3, currEmailTemplates.get(templateIndex).getTemplateId());

        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 15, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1224.04)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 17, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property8 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property9 Value ", "Insufficient Funds", recipientProperties.getProperty("FailureReason"));
        assertEquals("Property10 Value ", "September 14, 2007", recipientProperties.getProperty("TodaysDate"));
        assertEquals("Property11 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1123.96)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property12 Value ", "$100.08", recipientProperties.getProperty("IntuitHandlingFee"));
        assertEquals("Property13 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property14 Value ", "September 21, 2007", recipientProperties.getProperty("RedebitCompletedDate"));
        assertEquals("Property15 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAutoRedebit_BackDatedPayroll() {
        // create company
        SIGNONMSGSRQV1 signOnMsg = null;
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");
        Company company = companyQB1DataLoader.persistQBCompany1();
        ObjectFactory objFact = new ObjectFactory();
        signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();
        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // submit a backdated payroll
        PayrollServices.beginUnitOfWork();

        PayrollRunDTO payrollRunDTO = companyQB1DataLoader.getCompany1PayrollRunDTO();
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO("2007-08-25"));
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(submitPayrollResult);

        // return er debit
        // offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070905000000");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest01");
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerDdDebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "NSF");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.AutoRedebit3, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.AutoRedebit3, currEmailTemplates.get(templateIndex).getTemplateId());

        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 15, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(350.16)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 6, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property8 Value ", "September 6, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property9 Value ", "Insufficient Funds", recipientProperties.getProperty("FailureReason"));
        assertEquals("Property10 Value ", "September 5, 2007", recipientProperties.getProperty("TodaysDate"));
        assertEquals("Property11 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(250.08)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property12 Value ", "$100.08", recipientProperties.getProperty("IntuitHandlingFee"));
        assertEquals("Property13 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property14 Value ", "September 12, 2007", recipientProperties.getProperty("RedebitCompletedDate"));
        assertEquals("Property15 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAutoRedebit_BankAccountNotActive() {
        if(getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTestQBDT();

        // deactivate cba
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        ProcessResult<CompanyBankAccount> processResult =
                PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, cba.getSourceBankAccountId(), true, false);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // create an ER return
        setUpQBDTEmployerReturn("R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmation, currEmailTemplates.get(3).getTemplateId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAutoRedebit_BankAccountNotActiveMTL() {
        if(!getFeatureFlagValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED)){
            return;
        }
        beforeEachTestQBDT();

        // deactivate cba
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        ProcessResult<CompanyBankAccount> processResult =
                PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, cba.getSourceBankAccountId(), true, false);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // create an ER return
        setUpQBDTEmployerReturn("R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(0).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(2).getTemplateId());
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmationMTL, currEmailTemplates.get(4).getTemplateId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAutoRedebitFourStrikes() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        // add three strikes
        PayrollServices.beginUnitOfWork();

        ProcessResult<CompanyEvent> strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike1",
                SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike2",
                SpcfCalendar.createInstance(2007, 9, 2, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike3",
                SpcfCalendar.createInstance(2007, 9, 3, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        PayrollServices.commitUnitOfWork();

        // create an ER return (will cause a fourth strike)
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        assertEquals("Template ID", EventEmailTemplateTypeCode.AutoRedebitFourStrikes, currEmailTemplates.get(0).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(0).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 17, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1224.04)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 17, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property8 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property9 Value ", "Insufficient Funds", recipientProperties.getProperty("FailureReason"));
        assertEquals("Property10 Value ", "September 14, 2007", recipientProperties.getProperty("TodaysDate"));
        assertEquals("Property11 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1123.96)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property12 Value ", "$100.08", recipientProperties.getProperty("IntuitHandlingFee"));
        assertEquals("Property13 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property14 Value ", "4", recipientProperties.getProperty("NumberOfStrikes"));
        assertEquals("Property15 Value ", "September 28, 2007", recipientProperties.getProperty("TodaysDatePlus14CalendarDays"));
        assertEquals("Property16 Value ", "September 21, 2007", recipientProperties.getProperty("RedebitCompletedDate"));
        assertEquals("Property17 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDebitReturned() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R02", "This is an R02 description");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.DebitReturned4, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.DebitReturned4, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 13, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1223.96)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 17, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property8 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property9 Value ", "Account Closed", recipientProperties.getProperty("FailureReason"));
        assertEquals("Property10 Value ", QBDT_COMPANY_PSID, recipientProperties.getProperty("CompanyID"));
        assertEquals("Property11 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1123.96)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property12 Value ", "$100.00", recipientProperties.getProperty("IntuitHandlingFee"));
        assertEquals("Property13 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFeeDebitReturned() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");

        // offload auto redebits and fee debit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070914000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.AutoRedebit3, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.AutoRedebit3, currEmailTemplates.get(templateIndex).getTemplateId());
        templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDBankVerificationSuccessful, currEmailTemplates.get(templateIndex).getTemplateId());
        templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.DDSignupConfirmation, currEmailTemplates.get(templateIndex).getTemplateId());
        templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.QBDTPayrollConfirmation, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.QBDTPayrollConfirmation, currEmailTemplates.get(templateIndex).getTemplateId());
        PayrollServices.commitUnitOfWork();

        // check to see if any errors exist for pending emails
        String emailErrorLogName = checkEmailTemplatesForErrors();

        // emailErrorLogName should be null, but in case it isn't, print the log name to stdout
        if (emailErrorLogName != null) {
            System.out.println("Error log name: " + emailErrorLogName);
        }

        assertTrue("Email error log is empty", emailErrorLogName == null);

        // return fee debit
        Application.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerFeeDebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        assertEquals("Number of C1 EmployerFeeDebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "R01 description");

        // verify no new emails are created
        PayrollServices.beginUnitOfWork();
        currEmailTemplates = findEmailTemplates();
        assertEquals("Number Of Emails", 0, currEmailTemplates.size());

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testDebitReturnedFourStrikes() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        // add three strikes
        PayrollServices.beginUnitOfWork();

        ProcessResult<CompanyEvent> strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike1",
                SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike2",
                SpcfCalendar.createInstance(2007, 9, 2, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        strikeProcess = PayrollServices.companyManager.addStrikeEvent(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, "Strike3",
                SpcfCalendar.createInstance(2007, 9, 3, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(strikeProcess);

        PayrollServices.commitUnitOfWork();

        // create an ER return (will cause a fourth strike)
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R02", "This is an R02 description");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.DebitReturnedFourStrikes3, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.DebitReturnedFourStrikes3, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 15, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1223.96)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 17, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property8 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property9 Value ", "Account Closed", recipientProperties.getProperty("FailureReason"));
        assertEquals("Property10 Value ", QBDT_COMPANY_PSID, recipientProperties.getProperty("CompanyID"));
        assertEquals("Property11 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1123.96)).toString())), recipientProperties.getProperty("PayrollDebitAmount"));
        assertEquals("Property12 Value ", "$100.00", recipientProperties.getProperty("IntuitHandlingFee"));
        assertEquals("Property13 Value ", "September 28, 2007", recipientProperties.getProperty("TodaysDatePlus14CalendarDays"));
        assertEquals("Property14 Value ", "4", recipientProperties.getProperty("NumberOfStrikes"));
        assertEquals("Property15 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testWireExpectedNotification_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        CollectionStage domainCollectionStage = PayrollServices.entityFinder.findById(CollectionStage.class, CollectionStageCode.FirstCollectionAttempt);
        ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO(payrollRun.getSourcePayRunId(),
                new DateDTO("2007-09-14"), domainCollectionStage, ActionEventCode.DDRedebitEdit, false);
        ProcessResult modifyWireExpectedProcResult =
                PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, wireExpectedDTO);

        PayrollServices.commitUnitOfWork();
        assertSuccess(modifyWireExpectedProcResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.WireExpectedNotification4, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.WireExpectedNotification4, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1224.04)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "Insufficient Funds", recipientProperties.getProperty("FailureReason"));
        assertEquals("Property9 Value ", QBDT_COMPANY_PSID, recipientProperties.getProperty("CompanyID"));
        assertEquals("Property10 Value ", "September 14, 2007", recipientProperties.getProperty("WireExpectedDate"));
        assertEquals("Property11 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testWireExpectedNotification_QBOE() {
        beforeEachTestQBOE();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBOEEmployerReturn("R02", "This is an R02 description");

        // create redebit
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalDebitTransaction.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO("2007-10-02"));
        redebitDTO.setOriginalFinancialTxId(originalDebitTransaction.getId().toString());

        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                SourceSystemCode.QBOE, QBOE_COMPANY_PSID, redebitCollection);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);


        PayrollServices.beginUnitOfWork();
        CollectionStage domainCollectionStage = PayrollServices.entityFinder.findById(CollectionStage.class, CollectionStageCode.FirstCollectionAttempt);
        ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO("BatchTest05",
                new DateDTO("2007-10-01"), domainCollectionStage, ActionEventCode.DDRedebitEdit, false);
        ProcessResult modifyWireExpectedProcResult =
                PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, QBOE_COMPANY_PSID, wireExpectedDTO);

        PayrollServices.commitUnitOfWork();
        assertSuccess(modifyWireExpectedProcResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.WireExpectedNotification4);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "$280.00", recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "October 2, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "Account Closed", recipientProperties.getProperty("FailureReason"));
        assertEquals("Property9 Value ", QBOE_COMPANY_PSID, recipientProperties.getProperty("CompanyID"));
        assertEquals("Property10 Value ", "October 1, 2007", recipientProperties.getProperty("WireExpectedDate"));
        assertEquals("Property11 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testManualRedebit_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R02", "This is an R02 description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("1120.8"));
        redebitDTO.setInitiationDate(new DateDTO("2007-09-14"));
        redebitDTO.setOriginalFinancialTxId(originalDebitTransaction.getId().toString());

        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);

        ProcessResult result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                company.getSourceSystemCd(), company.getSourceCompanyId(), redebitCollection);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "$1,120.80", recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "September 17, 2007", recipientProperties.getProperty("RedebitSettlementDate"));
        assertEquals("Property9 Value ", "September 21, 2007", recipientProperties.getProperty("RedebitCompletedDate"));
        assertEquals("Property10 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property11 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testManualFeeRedebit_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R02", "This is an R02 description");

        // add fee redebit
        Application.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerFeeDebit);
        states.add(TransactionStateCode.Returned);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        c1FinTxns = c1FinTxns.sort(FinancialTransaction.FinancialTransactionAmount());


        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(c1FinTxns.get(0).getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO("2007-09-14"));
        redebitDTO.setOriginalFinancialTxId(c1FinTxns.get(0).getId().toString());

        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);

        ProcessResult result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                company.getSourceSystemCd(), company.getSourceCompanyId(), redebitCollection);
        assertSuccess(result);

        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(0)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "September 17, 2007", recipientProperties.getProperty("RedebitSettlementDate"));
        assertEquals("Property9 Value ", "September 21, 2007", recipientProperties.getProperty("RedebitCompletedDate"));
        assertEquals("Property10 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property11 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testManualRedebit_QBOE() {
        beforeEachTestQBOE();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBOEEmployerReturn("R02", "This is an R02 description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBOE_COMPANY_PSID,
                SourceSystemCode.QBOE);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("180"));
        redebitDTO.setInitiationDate(new DateDTO("2007-10-01"));
        redebitDTO.setOriginalFinancialTxId(originalDebitTransaction.getId().toString());

        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);

        ProcessResult result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                company.getSourceSystemCd(), company.getSourceCompanyId(), redebitCollection);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 1, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "$180.00", recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "October 2, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "October 2, 2007", recipientProperties.getProperty("RedebitSettlementDate"));
        assertEquals("Property9 Value ", "October 9, 2007", recipientProperties.getProperty("RedebitCompletedDate"));
        assertEquals("Property10 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property11 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPartialManualRedebit_QBOE() {
        beforeEachTestQBOE();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBOEEmployerReturn("R02", "This is an R02 description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBOE_COMPANY_PSID,
                SourceSystemCode.QBOE);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("100"));
        redebitDTO.setInitiationDate(new DateDTO("2007-10-01"));
        redebitDTO.setOriginalFinancialTxId(originalDebitTransaction.getId().toString());

        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);

        ProcessResult result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                company.getSourceSystemCd(), company.getSourceCompanyId(), redebitCollection);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 1, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.ManualRedebit3, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 11, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "$100.00", recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "October 2, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "October 2, 2007", recipientProperties.getProperty("RedebitSettlementDate"));
        assertEquals("Property9 Value ", "October 9, 2007", recipientProperties.getProperty("RedebitCompletedDate"));
        assertEquals("Property10 Value ", "3098", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property11 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRedebitFailed_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R02", "This is an R02 description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("1120.8"));
        redebitDTO.setInitiationDate(new DateDTO("2007-09-14"));
        redebitDTO.setOriginalFinancialTxId(originalDebitTransaction.getId().toString());

        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);

        ProcessResult result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                company.getSourceSystemCd(), company.getSourceCompanyId(), redebitCollection);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        // offload redebit and return
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070914000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerDdRedebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        assertEquals("Number of C1 EmployerDDRedebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 8, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.RedebitFailed1, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.RedebitFailed1, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1223.96)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", QBDT_COMPANY_PSID, recipientProperties.getProperty("CompanyID"));
        assertEquals("Property9 Value ", "September 17, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property10 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRedebitFailed_QBOE() {
        beforeEachTestQBOE();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBOEEmployerReturn("R02", "This is an R02 description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBOE_COMPANY_PSID,
                SourceSystemCode.QBOE);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("180"));
        redebitDTO.setInitiationDate(new DateDTO("2007-10-01"));
        redebitDTO.setOriginalFinancialTxId(originalDebitTransaction.getId().toString());

        ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
        redebitCollection.add(redebitDTO);

        ProcessResult result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                company.getSourceSystemCd(), company.getSourceCompanyId(), redebitCollection);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        // offload redebit and return
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        company = Company.findCompany(QBOE_COMPANY_PSID,
                SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerDdRedebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        assertEquals("Number of C1 EmployerDDRedebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.RedebitFailed1, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.RedebitFailed1, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "$280.00", recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "October 2, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", QBOE_COMPANY_PSID, recipientProperties.getProperty("CompanyID"));
        assertEquals("Property9 Value ", "October 2, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property10 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testLastChanceEmail_QBDT_Assisted() {
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"));

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 4, 13, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun payrollrun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.enrollEFTPS(company);
        //Sent
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT"));
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 14, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(payrollrun, TransactionTypeCode.EmployerTaxDebit);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        CollectionStage domainCollectionStage = PayrollServices.entityFinder.findById(CollectionStage.class, CollectionStageCode.FirstCollectionAttempt);
        ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO(payrollRun.getSourcePayRunId(),
                new DateDTO("2011-04-15"), domainCollectionStage, ActionEventCode.DDRedebitEdit, true);
        ProcessResult modifyWireExpectedProcResult =
                PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBDT, company.getSourceCompanyId(), wireExpectedDTO);

        PayrollServices.commitUnitOfWork();
        assertSuccess(modifyWireExpectedProcResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 5, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.LastChanceEmail4, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.LastChanceEmail4, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", company.getSourceCompanyId(), currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertNotNull("Property3 Value ", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(280.18)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "April 15, 2011", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", company.getSourceCompanyId(), recipientProperties.getProperty("CompanyID"));
        assertEquals("Property9 Value ", "April 15, 2011", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property10 Value ", "Assisted", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testLastChanceEmail_QBDT() {
        beforeEachTestQBDT();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBDTEmployerReturn("R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        CollectionStage domainCollectionStage = PayrollServices.entityFinder.findById(CollectionStage.class, CollectionStageCode.FirstCollectionAttempt);
        ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO(payrollRun.getSourcePayRunId(),
                new DateDTO("2007-09-14"), domainCollectionStage, ActionEventCode.DDRedebitEdit, true);
        ProcessResult modifyWireExpectedProcResult =
                PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBDT, QBDT_COMPANY_PSID, wireExpectedDTO);

        PayrollServices.commitUnitOfWork();
        assertSuccess(modifyWireExpectedProcResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 8, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.LastChanceEmail4, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.LastChanceEmail4, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).add(SpcfDecimal.createInstance(1224.04)).toString())), recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", QBDT_COMPANY_PSID, recipientProperties.getProperty("CompanyID"));
        assertEquals("Property9 Value ", "September 14, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property10 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testLastChanceEmail_QBOE() {
        beforeEachTestQBOE();

        // create an ER return
        FinancialTransaction originalDebitTransaction = setUpQBOEEmployerReturn("R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        CollectionStage domainCollectionStage = PayrollServices.entityFinder.findById(CollectionStage.class, CollectionStageCode.FirstCollectionAttempt);
        ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO("BatchTest05",
                new DateDTO("2007-10-01"), domainCollectionStage, ActionEventCode.DDRedebitEdit, true);
        ProcessResult modifyWireExpectedProcResult =
                PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, QBOE_COMPANY_PSID, wireExpectedDTO);

        PayrollServices.commitUnitOfWork();
        assertSuccess(modifyWireExpectedProcResult);

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 2, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.LastChanceEmail4, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.LastChanceEmail4, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBOE_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "someEmail@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "John Doe", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "John", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Doe", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "Intuit", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "someEmail@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBOE", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "$280.00", recipientProperties.getProperty("AdjustedPayrollDebitAmount"));
        assertEquals("Property7 Value ", "October 2, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", QBOE_COMPANY_PSID, recipientProperties.getProperty("CompanyID"));
        assertEquals("Property9 Value ", "October 1, 2007", recipientProperties.getProperty("NextBusinessDate"));
        assertEquals("Property10 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPayrollCancellationNotification_BeforeFiveDays() {
        beforeEachTestQBDT();

        // add OnHold
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> processResult = PayrollServices.companyManager.addOnHoldReason(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, ServiceSubStatusCode.PendingTermination);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // Run Missed Payroll Processor before 5 days of cancellation
        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070913");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.PayrollCancellationNotification2, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.PayrollCancellationNotification2, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 20, 2007", recipientProperties.getProperty("PayrollCancelDate"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();

        // Run Missed Payroll Processor before 1 day of cancellation
        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070919000000");
        process = new ProcessMissedPayrolls();
        process.process("20070919");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.PayrollCancellationNotification2, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.PayrollCancellationNotification2, currEmailTemplates.get(templateIndex).getTemplateId());
        List<IEventEmail> emails = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0);

        Collections.sort(emails, new Comparator<IEventEmail>() {
            public int compare(IEventEmail email1, IEventEmail email2) {
                String settlementDate1 = email1.getProperties().getProperty("PaycheckSettlementDate");
                String settlementDate2 = email2.getProperties().getProperty("PaycheckSettlementDate");
                return settlementDate1.compareToIgnoreCase(settlementDate2);
            }
        });
        assertEquals("Number of PayrollCancellationNotification Emails", 3, emails.size());

        currRecipient = emails.get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 20, 2007", recipientProperties.getProperty("PayrollCancelDate"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        currRecipient = emails.get(1);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 20, 2007", recipientProperties.getProperty("PayrollCancelDate"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        currRecipient = emails.get(2);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 26, 2007", recipientProperties.getProperty("PayrollCancelDate"));
        assertEquals("Property7 Value ", "September 17, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPayrollCancellationNotification_BeforeOneDay() {
        beforeEachTestQBDT();

        // add OnHold
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> processResult = PayrollServices.companyManager.addOnHoldReason(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // Run Missed Payroll Processor before 1 day of cancellation
        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070919000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070919");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.PayrollCancellationNotification2, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.PayrollCancellationNotification2, currEmailTemplates.get(templateIndex).getTemplateId());
        List<IEventEmail> emails = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0);
        Collections.sort(emails, new Comparator<IEventEmail>() {
            public int compare(IEventEmail email1, IEventEmail email2) {
                String settlementDate1 = email1.getProperties().getProperty("PaycheckSettlementDate");
                String settlementDate2 = email2.getProperties().getProperty("PaycheckSettlementDate");
                return settlementDate1.compareToIgnoreCase(settlementDate2);
            }
        });
        IEventEmail currRecipient = emails.get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 20, 2007", recipientProperties.getProperty("PayrollCancelDate"));
        assertEquals("Property7 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        currRecipient = emails.get(1);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 26, 2007", recipientProperties.getProperty("PayrollCancelDate"));
        assertEquals("Property7 Value ", "September 17, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPayrollCancelledNotification() {
        beforeEachTestQBDT();

        // add OnHold
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> processResult = PayrollServices.companyManager.addOnHoldReason(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070921000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070921");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.PayrollCancelledNotification2, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.PayrollCancelledNotification2, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 11, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSUIAdjustmentsEmail_ImmediateDebit() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));


        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 5, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        // Finalize AR-209B-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();

        SpcfUniqueId mmtId = moneyMovementTransactions.get(0).getId();
        Company company = Company.findCompany(moneyMovementTransactions.get(0).getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Adjust Finalized Payment With Immediate Debit
        company = DataLoadServices.refreshCompany(company);
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, true, null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 3, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.SameDaySUIDebitNotification4, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.SameDaySUIDebitNotification4, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 10, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property4 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property5 Value ", "1", recipientProperties.getProperty("Quarter"));
        assertEquals("Property6 Value ", "2011", recipientProperties.getProperty("Year"));
        assertEquals("Property7 Value ", "127.27", recipientProperties.getProperty("Amount"));
        assertEquals("Property7 Value ", "March 30, 2011", recipientProperties.getProperty("DebitSettlementDate"));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSUIAdjustmentsEmail_ImmediateCredit() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));
        // Finalize AR-209B-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();

        SpcfUniqueId mmtId = moneyMovementTransactions.get(0).getId();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Adjust Finalized Payment With Immediate Credit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, true, null);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSUIAdjustmentsEmail_EoqDebit() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        // Finalize AR-209B-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();

        SpcfUniqueId mmtId = moneyMovementTransactions.get(0).getId();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Adjust Finalized Payment With Variance Debit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();

        // Check Variance Transactions
        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        Company company = Company.findCompany(mmt.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        long beginningToken = company.getCurrentToken();
        long beginningTransactionId = Long.parseLong(company.getNextPayrollTransactionId());
        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 1, financialTransactions.size());
        FinancialTransaction ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxReceivable variance transaction created: ", TransactionTypeCode.EmployerSUITaxReceivable, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxReceivable amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "-127.27", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1, SpcfTimeZone.getLocalTimeZone());
        new EoqSUITaxAdjustments().process(processingDate, null, true);
    }

    @Test
    public void testSUIAdjustmentsEmail_EoqCredit() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));

        // Finalize AR-209B-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("AR-209B-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();

        SpcfUniqueId mmtId = moneyMovementTransactions.get(0).getId();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Adjust Finalized Payment With Variance - Credit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "85");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();

        // Check Variance Transactions

        mmt = Application.findById(MoneyMovementTransaction.class, mmtId);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable)
                                .And(FinancialTransaction.Company().equalTo(mmt.getCompany())));

        DomainEntitySet<FinancialTransaction> financialTransactions = PayrollServices.entityFinder.find(FinancialTransaction.class, where);
        assertEquals("Number of variance transactions created: ", 1, financialTransactions.size());
        FinancialTransaction ft = financialTransactions.get(0);
        assertEquals("EmployerSUITaxPayable variance transaction created: ", TransactionTypeCode.EmployerSUITaxPayable, ft.getTransactionType().getTransactionTypeCd());
        assertEquals("EmployerSUITaxPayable amount: ", "127.27", ft.getFinancialTransactionAmount().toString());
        // Check Ledger Balance
        SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(mmt.getCompany(), LedgerAccountCode.ERSUITaxDue));
        assertEquals("ERSUITaxDue amount: ", "127.27", balance.toString());
        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1);
        new EoqSUITaxAdjustments().process(processingDate, null, true);


    }

    @Test
    public void testSUIAdjustmentsEmail_AZ_EoqCredit() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        String[] statesList = new String[]{"AZ"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10));

        // Finalize AZ-UC018-PAYMENT
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("AZ-UC018-PAYMENT");
        PayrollServices.paymentManager.finalizeSUIPayments(null, paymentTemplate, 2011, 1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paymentTemplate = PaymentTemplate.findPaymentTemplate("AZ-UC018-PAYMENT");
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(2011, 1);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(2011, 1);

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(paymentTemplate)
                        .setPeriodBeginDate(quarterBeginDate)
                        .setPeriodEndDate(quarterEndDate)
                        .find();

        SpcfUniqueId mmtId = moneyMovementTransactions.get(0).getId();
        for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
            assertEquals("Tax Payment Status: ", TaxPaymentStatus.ATFFinalized, mmt.getTaxPaymentStatus());
        }

        PayrollServices.rollbackUnitOfWork();

        // Adjust Finalized Payment With Variance - Credit

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, mmtId);
        HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
        Law law = Application.findById(Law.class, "86");
        lawAmounts.put(law, new SpcfMoney("-127.27"));
        law = Application.findById(Law.class, "179");
        lawAmounts.put(law, new SpcfMoney("100.00"));
        PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, false, null);
        PayrollServices.commitUnitOfWork();

        SpcfCalendar processingDate = SpcfCalendar.createInstance(2011, 5, 1);
        new EoqSUITaxAdjustments().process(processingDate, null, true);


    }


    @Test
    public void testPayrollCancelledNotification_BackDatedPayroll() {
        // create company
        SIGNONMSGSRQV1 signOnMsg = null;
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");
        Company company = companyQB1DataLoader.persistQBCompany1();
        ObjectFactory objFact = new ObjectFactory();
        signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();
        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // submit a backdated payroll
        PayrollServices.beginUnitOfWork();

        PayrollRunDTO payrollRunDTO = companyQB1DataLoader.getCompany1PayrollRunDTO();
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO("2007-08-25"));
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(submitPayrollResult);

        // add OnHold
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> processResult = PayrollServices.companyManager.addOnHoldReason(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070921000000");
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070921");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.PayrollCancelledNotification2, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.PayrollCancelledNotification2, currEmailTemplates.get(templateIndex).getTemplateId());
        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 7, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "September 6, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property7 Value ", "Direct Deposit", recipientProperties.getProperty("ServiceType"));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCancelPayments() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 1; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }
        PayrollServices.beginUnitOfWork();
        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // cancel  2 txns
        PayrollServices.beginUnitOfWork();
        Collection<String> billPaymentCancelIds = new ArrayList<String>();
        it = billPaymentDTOs.iterator();
        for (int i = 0; i < 2; i++) {
            if (it.hasNext()) {
                billPaymentCancelIds.add(((BillPaymentDTO) it.next()).getBillPaymentId());
            }
        }
        ProcessResult cancelResult = PayrollServices.billPaymentManager.cancelBillPaymentTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentCancelIds,null);
        PayrollServices.commitUnitOfWork();

        // cancel  3 txns
        PayrollServices.beginUnitOfWork();
        billPaymentCancelIds = new ArrayList<String>();
        for (int i = 0; i < 3; i++) {
            if (it.hasNext()) {
                billPaymentCancelIds.add(((BillPaymentDTO) it.next()).getBillPaymentId());
            }
        }
        cancelResult = PayrollServices.billPaymentManager.cancelBillPaymentTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentCancelIds,null);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to verify PSRV000854
     */
    @Test
    public void testFeeRefund() {
        beforeEachTestQBDT();

        // create a return for Fee Debit
        // offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerFeeDebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        c1FinTxns = c1FinTxns.sort(FinancialTransaction.FinancialTransactionAmount());
        c1FinTxns.remove(1);
        assertEquals("Number of C1 EmployerFeeDebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "R01 Description");

        // offload and complete the fee redebit
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHtxs = new ProcessACHTransactions();
        processACHtxs.process("20070928");
        PayrollServices.commitUnitOfWork();

        // create refund fee transaction
        PayrollServices.beginUnitOfWork();

        types = new Vector<TransactionTypeCode>();
        states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerFeeRedebit);
        states.add(TransactionStateCode.Completed);
        Application.refresh(payrollRun);
        c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        c1FinTxns = c1FinTxns.sort(FinancialTransaction.FinancialTransactionAmount());

        ERRefundDTO refundDTO = new ERRefundDTO();
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(c1FinTxns.get(0).getFinancialTransactionAmount());
        refundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
        refundDTO.setFinancialTxId(c1FinTxns.get(0).getId().toString());
        ProcessResult<FinancialTransaction> result = PayrollServices.financialTransactionManager.refundEmployerTransaction(
                SourceSystemCode.QBDT, QBDT_COMPANY_PSID, refundDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        // verify emails
        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();
        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.RefundedFeeAmount1, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.RefundedFeeAmount1, currEmailTemplates.get(templateIndex).getTemplateId());

        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Refunded: " + currencyFormat.format(Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceCharge().multiply(SpcfDecimal.createInstance(2)).toString())) + " for Direct Deposit Fee on October 1, 2007<br>", recipientProperties.getProperty("RefundedFeeList"));
        assertEquals("Property7 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));


        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to verify PSRV000854
     */
    @Test
    public void testServiceAndSalesTaxRefund() {
        beforeEachTestQBDT();

        // create a return for Fee Debit

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);

        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.ServiceSalesAndUseTax);
        states.add(TransactionStateCode.Executed);

        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction.
                findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);
        assertEquals("Number of ServiceSalesAndUseTax EX txns", 2, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "R01 Description");

        // offload and complete the fee redebit
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // complete the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20070928");
        PayrollServices.commitUnitOfWork();

        // create refund fee transaction
        PayrollServices.beginUnitOfWork();

        types = new Vector<TransactionTypeCode>();
        states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
        states.add(TransactionStateCode.Completed);
        Application.refresh(payrollRun);
        c1FinTxns = FinancialTransaction.
                findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        for (FinancialTransaction txn : c1FinTxns) {
            ERRefundDTO refundDTO = new ERRefundDTO();

            refundDTO.setSettlementType(SettlementTypeDTO.ACH);
            refundDTO.setFinancialTxAmt(txn.getFinancialTransactionAmount());
            refundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
            refundDTO.setFinancialTxId(txn.getId().toString());

            ProcessResult<FinancialTransaction> result =
                    PayrollServices.financialTransactionManager.refundEmployerTransaction(SourceSystemCode.QBDT,
                            QBDT_COMPANY_PSID,
                            refundDTO);

            assertSuccess(result);
        }

        PayrollServices.commitUnitOfWork();

        // verify emails
        PayrollServices.beginUnitOfWork();

        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 7, currEmailTemplates.size());
        int templateIndex = getTemplateIndex(EventEmailTemplateTypeCode.RefundedFeeAmount1, currEmailTemplates);
        assertTrue("Template Id Found", templateIndex >= 0);
        assertEquals("Template ID", EventEmailTemplateTypeCode.RefundedFeeAmount1, currEmailTemplates.get(templateIndex).getTemplateId());

        IEventEmail currRecipient = currEmailTemplates.get(templateIndex).getRecipientsToTransmit().get(0).get(0);

        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();

        assertEquals("Number Of Email Properties ", 8, recipientProperties.size());
        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property4 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property5 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property6 Value ", "Refunded: $0.08 for Direct Deposit Fee on October 1, 2007<br>" +
                        "Refunded: $0.08 for Per transmission fee on October 1, 2007<br>",
                recipientProperties.getProperty("RefundedFeeList"));
        assertEquals("Property7 Value ", "4747", recipientProperties.getProperty("CompanyBankAccountLastFour"));
        assertEquals("Property8 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDDRejectSuccessful_QBOECompany() {
        beforeEachTestQBOE();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee1 = Employee.findEmployee(company, "EE1");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findEmployeeFinancialTransactions(company, "BatchTest05", employee1, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);
        assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is a non-NSF description");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        PayrollServices.commitUnitOfWork();

        assertEquals("DDReject Refund Credit Financial Transactions ", 1, ddRejectRefundFinTxns.size());

        //Assertion for DDReject Company Event
        assertEquals("DDReject Company Event", 1, companyEventsList.size());

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 0, currEmailTemplates.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDDRejectSuccessful_QBDTCompany() {
        beforeEachTestQBDT();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company).sort(PayrollRun.PayrollRunDate());
        Employee employee1 = Employee.findEmployee(company, "Donovan McNabb");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findEmployeeFinancialTransactions(company, payrollRuns.get(1).getSourcePayRunId(), employee1, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);
        assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is a non-NSF description");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        PayrollServices.commitUnitOfWork();

        assertEquals("DDReject Refund Credit Financial Transactions ", 1, ddRejectRefundFinTxns.size());

        //Assertion for DDReject Company Event
        assertEquals("DDReject Company Event", 1, companyEventsList.size());

        PayrollServices.beginUnitOfWork();
        List<EventEmailTemplate> currEmailTemplates = findEmailTemplates();

        assertEquals("Number Of Emails", 6, currEmailTemplates.size());
        EventEmailTemplate eventEmailTemplate = findEventEmailTemplate(currEmailTemplates, EventEmailTemplateTypeCode.EEDDREJECT1);
        IEventEmail currRecipient = eventEmailTemplate.getRecipientsToTransmit().get(0).get(0);
        assertEquals("Company ID", QBDT_COMPANY_PSID, currRecipient.getCompanyId());
        assertEquals("Recipient Email", "PayrollAdmin@aol.com", currRecipient.getRecipientEmail());
        assertEquals("Recipient Name", "Johnny Payrolladmin", currRecipient.getRecipientName());

        Properties recipientProperties = currRecipient.getProperties();
        assertEquals("Number Of Email Properties ", 9, recipientProperties.size());

        assertEquals("Property1 Value ", "Johnny", recipientProperties.getProperty("PayrollAdminFirstName"));
        assertEquals("Property2 Value ", "Payrolladmin", recipientProperties.getProperty("PayrollAdminLastName"));
        assertEquals("Property3 Value ", "M", recipientProperties.getProperty("EmployeeLastNameFirstInitial"));
        assertEquals("Property4 Value ", "QB Desktop 3", recipientProperties.getProperty("CompanyLegalName"));
        assertEquals("Property5 Value ", "September 17, 2007", recipientProperties.getProperty("PaycheckSettlementDate"));
        assertEquals("Property6 Value ", "PayrollAdmin@aol.com", recipientProperties.getProperty("PayrollAdminEmail"));
        assertEquals("Property7 Value ", "QBDT", recipientProperties.getProperty("SourcePayrollSystem"));
        assertEquals("Property8 Value ", "Donovan", recipientProperties.getProperty("EmployeeFirstName"));
        assertEquals("Property9 Value ", "Direct Deposit", recipientProperties.getProperty(EventEmailParamTypeCode.ServiceType.toString()));

        PayrollServices.commitUnitOfWork();
    }

    private List<EventEmailTemplate> findEmailTemplates() {
        boolean manageTransaction = !Application.hasActiveTransaction();

        if (manageTransaction) {
            PayrollServices.beginUnitOfWork();
        }

        DomainEntitySet<CompanyEventEmail> eventList = CompanyEventEmail.findEmailEventsByStatus(
                EventEmailStatus.Pending, EventEmailStatus.GroupIncomplete, EventEmailStatus.Resend);

        Collection<CompanyEventEmailManager> companyEmailManagers =
                EventEmailTemplateFactory.buildCompanyEmailManagers(eventList);

        List<EventEmailTemplate> masterTemplateList =
                EventEmailTemplateFactory.buildMasterTemplateList(companyEmailManagers);

        List<EventEmailTemplate> currEmailTemplates = new ArrayList<EventEmailTemplate>();

        for (EventEmailTemplate currTemplate : masterTemplateList) {
            //Recipients
            List<List<IEventEmail>> recipients = currTemplate.getRecipientsToTransmit();
            if (recipients.get(0).size() > 0) {
                currEmailTemplates.add(currTemplate);
            }
        }

        if (manageTransaction) {
            PayrollServices.commitUnitOfWork();
        }

        return currEmailTemplates;
    }

    private String checkEmailTemplatesForErrors() {
        String errorLogFileName = null;
        boolean manageTransaction = !Application.hasActiveTransaction();

        if (manageTransaction) {
            PayrollServices.beginUnitOfWork();
        }

        DomainEntitySet<CompanyEventEmail> eventList = CompanyEventEmail.findEmailEventsByStatus(
                EventEmailStatus.Pending, EventEmailStatus.GroupIncomplete, EventEmailStatus.Resend);

        Collection<CompanyEventEmailManager> companyEmailManagers =
                EventEmailTemplateFactory.buildCompanyEmailManagers(eventList);

        List<EventEmailTemplate> masterTemplateList =
                EventEmailTemplateFactory.buildMasterTemplateList(companyEmailManagers);

        List<EventEmailTemplate> currEmailTemplates = new ArrayList<EventEmailTemplate>();

        for (EventEmailTemplate currTemplate : masterTemplateList) {
            //Recipients
            List<List<IEventEmail>> recipients = currTemplate.getRecipientsToTransmit();
            if (recipients.get(0).size() > 0) {
                currEmailTemplates.add(currTemplate);

                // flag all of the successfully templated emails as ’sent'
                // (this will keep these from flagging as errors below)
                for (List<IEventEmail> eventEmailList : recipients) {
                    for (IEventEmail eventEmail : eventEmailList) {
                        currTemplate.emailSent(eventEmail.getRecipientId());
                    }
                }
            }
        }

        // update all domain objects with their corresponding statuses
        for (CompanyEventEmailManager manager : companyEmailManagers) {
            manager.persistEventStatuses();
        }

        // generate the error log for emails failing validation
        // this will return the name of the generated error log or null if no errors were found
        // (this needs to happen after the above templated emails have been flagged as ’sent')
        errorLogFileName = EmailUtils.generateErrorFile(companyEmailManagers);

        if (manageTransaction) {
            PayrollServices.commitUnitOfWork();
        }

        return errorLogFileName;
    }

    private FinancialTransaction setUpQBDTEmployerReturn(String pReturnReason, String pBankReturnDescription) {
        // offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(QBDT_COMPANY_PSID,
                SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerDdDebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);
        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, pReturnReason, pBankReturnDescription);

        return c1FinTxns.get(0);
    }

    private DomainEntitySet<FinancialTransaction> returnEEReversals(Company pCompany, String pSourcePayrollRunId, boolean pReverseAll) {
        // return few reversal transactions
        PayrollServices.beginUnitOfWork();
        Company company = Application.findById(Company.class, pCompany.getId());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pSourcePayrollRunId);

        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();
        types.add(TransactionTypeCode.EmployeeDdReversalDebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> eeReversals = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        eeReversals = eeReversals.sort(FinancialTransaction.FinancialTransactionAmount());

        if (!pReverseAll) {
            while (eeReversals.size() > 1) {
                eeReversals.remove(eeReversals.size() - 1);
            }
        }
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(eeReversals, "R01", "NSF Return");

        return eeReversals;
    }

    private FinancialTransaction setUpQBDTEmployeeReturn(String pReturnReason, String pBankReturnDescription, boolean pReturnAll) {
        // offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070913000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(QBDT_COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployeeDdCredit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);

        if (!pReturnAll) {
            c1FinTxns.remove(1);
            c1FinTxns.remove(1);
        }

        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, pReturnReason, pBankReturnDescription);
//        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
//        assertEquals("Number of returns", 1, returnList.size());

        return c1FinTxns.get(0);
    }

    private FinancialTransaction setUpQBOEEmployerReturn(String pReturnReason, String pBankReturnDescription) {
        ACHReturnsDataLoader dataLoader = new ACHReturnsDataLoader();
        // offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company company = Company.findCompany(QBOE_COMPANY_PSID,
                SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
        Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

        types.add(TransactionTypeCode.EmployerDdDebit);
        states.add(TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactionsForPayrollByTypeAndState(payrollRun, types, states);
        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());

        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, pReturnReason, pBankReturnDescription);

        return c1FinTxns.get(0);
    }

    private void setCurrentPrincipal() {
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("RMRep");
        ProcessResult processResult1 = PayrollServices.userManager.addUser("TestAdapter", Arrays.asList(foundRole.getRoleId()), "TestAdapter", "TestAdapter");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser) processResult1.getResult();
        user = Application.findById(AuthUser.class, user.getId());
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));
    }

    private void addCompanyWithBankAccount() {
        PayrollServicesTest.beforeEachTest();

        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        CompanyQB1DataLoader qb1dataloader = new CompanyQB1DataLoader();

        // Create Company and CompanyBankAccount
        Application.beginUnitOfWork();

        PSPDate.setPSPTime("20070822000000");

        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();
        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

        CompanyDTO companyDTO = qb1dataloader.getCompany1();
        companyDTO.setNextEmployeeId("1");
        companyDTO.setNextPaycheckId("1");
        companyDTO.setNextPayrollItemId("1");
        companyDTO.setNextPayrollTransactionId("1");
        companyDTO.getLegalAddress().setCity("Honolulu");
        companyDTO.getLegalAddress().setState("HI");
        companyDTO.getLegalAddress().setZipCode("96813");
        companyDTO.setQuickBooksInfo(qbInfoDTO);

        DataLoader dataloader = new DataLoader();

        Company company = dataloader.persistCompany(companyDTO);

        dataloader.persistCompanyService(company, qb1dataloader.getCompany1Service());

        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        qb1dataloader.getCompany1BankAccount(),
                        true, true);

        assertSuccess("addCompanyBankAccount", addCBAProcResult);

        Application.commitUnitOfWork();
    }

    private int getTemplateIndex(EventEmailTemplateTypeCode pTemplate, List<EventEmailTemplate> pTemplateList) {
        int i = 0;
        for (EventEmailTemplate template : pTemplateList) {
            if (template.getTemplateId().equals(pTemplate)) {
                return pTemplateList.indexOf(template);
            }

        }
        return -1;
    }

    private EventEmailTemplate findEventEmailTemplate(List<EventEmailTemplate> pEventEmailTemplates, EventEmailTemplateTypeCode pEventEmailTemplateTypeCode) {
        for (EventEmailTemplate eventEmailTemplate : pEventEmailTemplates) {
            if (eventEmailTemplate.getTemplateId() == pEventEmailTemplateTypeCode) {
                return eventEmailTemplate;
            }
        }

        fail("Template type not found: " + pEventEmailTemplateTypeCode);
        return null;
    }

    private void setupCompanyAndSubmitPayroll(String pPsid, String pFein) {
        // create companies
        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime("20070822000000");

        CompanyQB1DataLoader dataloader = new CompanyQB1DataLoader();

        Company company = dataloader.persistQBCompany1(pPsid, pFein);

        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();

        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // create payroll run for company
        PayrollServices.beginUnitOfWork();

        PayrollRunDTO payrollRunDTO = dataloader.getCompany1PayrollRunDTO();

        PSPDate.setPSPTime("20070928000000");

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, pPsid, payrollRunDTO);

        PayrollServices.commitUnitOfWork();
    }

    private Map<SystemParameter.Code, String> beforeEmailNotificationServiceErrorHandlingTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        Map<SystemParameter.Code, String> origParamsMap = new HashMap<SystemParameter.Code, String>();

        PayrollServices.beginUnitOfWork();

        // Save the original system params values to be restored after the test
        origParamsMap.put(SystemParameter.Code.EMAIL_GATEWAY_NTF_BATCH_SIZE, SystemParameter.findStringValue(SystemParameter.Code.EMAIL_GATEWAY_NTF_BATCH_SIZE, "100"));
        origParamsMap.put(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY, SystemParameter.findStringValue(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY, "false"));
        origParamsMap.put(SystemParameter.Code.EMAIL_GATEWAY_SEND_MAX_RETRY, SystemParameter.findStringValue(SystemParameter.Code.EMAIL_GATEWAY_SEND_MAX_RETRY));

        // Set the NTF batch size to 2 so we can force 3 template batches to be used to process our 6 emails
        SystemParameter.update(SystemParameter.Code.EMAIL_GATEWAY_NTF_BATCH_SIZE, "2");

        // Set to create offload batches on the fly since our dataloader's processing dates are pretty far in the past
        SystemParameter.update(SystemParameter.Code.CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY, "true");

        // turn retries back on
        SystemParameter.update(SystemParameter.Code.EMAIL_GATEWAY_SEND_MAX_RETRY, "5");

        PayrollServices.commitUnitOfWork();

        // setup 6 companies so we will have three full QBDTPayrollConfirmation template batches (at the configured batch size of 2)
        setupCompanyAndSubmitPayroll("123456789", "987654321");
        setupCompanyAndSubmitPayroll("234567891", "198765432");
        setupCompanyAndSubmitPayroll("345678912", "219876543");
        setupCompanyAndSubmitPayroll("456789123", "321987654");
        setupCompanyAndSubmitPayroll("567891234", "432198765");
        setupCompanyAndSubmitPayroll("678912345", "543219876");

        // Set all non-QBDTPayrollConfirmation emails to Sent so they don't interfere with the tests to follow
        Application.executeSqlCommand("UPDATE PSP_COMPANY_EVENT_EMAIL SET STATUS_CD = 'Sent' WHERE EMAIL_TEMPLATE_TYPE_CD != 'QBDTPayrollConfirmation'", true);

        //
        // We should have 6 Pending QBDTPayrollConfirmation emails at this point
        //
        Expression<CompanyEventEmail> query =
                new Query<CompanyEventEmail>().Select(CompanyEventEmail.StatusCd().Count())
                        .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                                .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Pending)));
        Long count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

        assertEquals("CompanyEventEmail count", 6, count.longValue());

        return origParamsMap;
    }

    private void afterEmailNotificationServiceErrorHandlingTest(Map<SystemParameter.Code, String> pOrigParamsMap) {
        //
        // Reset system params back to their original pre-test values
        //
        PayrollServices.beginUnitOfWork();

        for (Map.Entry<SystemParameter.Code, String> entry : pOrigParamsMap.entrySet()) {
            SystemParameter.update(entry.getKey(), entry.getValue());
        }

        PayrollServices.commitUnitOfWork();
    }

    /*
     * PSRV003705 - Customers are Receiving Multiple copies of acknowledgement e-mails from PSP
     *
     * Test INPUT error handling
     */
    @Test
    public void testEmailNotificationServiceInputErrorHandling() {

        //We check if TXE or OINP is enabled. If it is enabled we do not want to run this test against TXE or OINP service so we exit the test
        Boolean isEtTxeEnabled = TxeEmailHelper.isEtTemplateTxeEnabled("IntegrationTest");
        OINPEmailHelper oinpEmailHelper = new OINPEmailHelper();
        Boolean isOINPEnabled = oinpEmailHelper.isTemplateOINPEnabled("IntegrationTest");
        if (isEtTxeEnabled || isOINPEnabled) {
            return;
        }

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "5000");
        PayrollServices.commitUnitOfWork();
        Map<SystemParameter.Code, String> origParamsMap = null;

        try {
            origParamsMap = beforeEmailNotificationServiceErrorHandlingTest();

            Expression<CompanyEventEmail> query;
            List result;

            //
            // Set the mock port factory to return an INPUT error on the second iteration (call) to the service
            //
            MockNotificationPortFactory mockPortFactory = new MockNotificationPortFactory();
            NotificationServiceFactory.setPortFactory(mockPortFactory);
            mockPortFactory.forceInputError(2);

            //
            // Call the email gateway to process any Pending emails
            //
            new EmailGateway().processCompanyEventsForEmail();

            //
            // We should have the following at this point:
            // - 2 Sent QBDTPayrollConfirmation emails (those that were in the first batch)
            // - 2 Resend QBDTPayrollConfirmation emails (those that were in the second batch we forced to return an error)
            // - 2 Pending QBDTPayrollConfirmation emails (those that were in the third batch that never got processed due to the error in batch 2)
            //
            // Note that we need to do this in three count queries because GroupBy is not working properly...
            //
            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent)));
            Long count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 2, count.longValue());

            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Resend)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 2, count.longValue());

            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Pending)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 2, count.longValue());

            //
            // Remove any forced errors from the mock port factory to allow all emails to process
            //
            mockPortFactory.clearErrorStatus();

            //
            // Call the email gateway to process any Pending/Resend emails
            //
            new EmailGateway().processCompanyEventsForEmail();

            //
            // We should have the following at this point:
            // - 6 Sent QBDTPayrollConfirmation emails
            //
            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 6, count.longValue());
        } finally {
            if (origParamsMap != null) {
                afterEmailNotificationServiceErrorHandlingTest(origParamsMap);
            }
        }
    }

    /*
     * PSRV003705 - Customers are Receiving Multiple copies of acknowledgement e-mails from PSP
     *
     * Test SYSTEM error handling
     */
    @Test
    public void testEmailNotificationServiceSystemErrorHandling() {

        //We check if TXE or OINP is enabled. If it is enabled we do not want to run this test against TXE or OINP service so we exit the test
        Boolean isEtTxeEnabled = TxeEmailHelper.isEtTemplateTxeEnabled("IntegrationTest");
        OINPEmailHelper oinpEmailHelper = new OINPEmailHelper();
        Boolean isOINPEnabled = oinpEmailHelper.isTemplateOINPEnabled("IntegrationTest");
        if (isEtTxeEnabled || isOINPEnabled) {
            return;
        }

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "5000");
        PayrollServices.commitUnitOfWork();
        Map<SystemParameter.Code, String> origParamsMap = null;

        try {
            origParamsMap = beforeEmailNotificationServiceErrorHandlingTest();

            Expression<CompanyEventEmail> query;
            List result;

            //
            // Set the mock port factory to return a SYSTEM error on the second iteration (call) to the service
            //
            MockNotificationPortFactory mockPortFactory = new MockNotificationPortFactory();
            NotificationServiceFactory.setPortFactory(mockPortFactory);
            mockPortFactory.forceSystemError(2);

            //
            // Call the email gateway to process any Pending emails
            //
            new EmailGateway().processCompanyEventsForEmail();

            //
            // We should have the following at this point:
            // - 2 Sent QBDTPayrollConfirmation emails (those that were in the first batch)
            // - 2 Resend QBDTPayrollConfirmation emails (those that were in the second batch we forced to return an error)
            // - 2 Pending QBDTPayrollConfirmation emails (those that were in the third batch that never got processed due to the error in batch 2)
            //
            // Note that we need to do this in three count queries because GroupBy is not working properly...
            //
            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent)));
            Long count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 2, count.longValue());

            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Resend)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 2, count.longValue());

            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Pending)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 2, count.longValue());

            //
            // Remove any forced errors from the mock port factory to allow all emails to process
            //
            mockPortFactory.clearErrorStatus();

            //
            // Call the email gateway to process any Pending/Resend emails
            //
            new EmailGateway().processCompanyEventsForEmail();

            //
            // We should have the following at this point:
            // - 6 Sent QBDTPayrollConfirmation emails
            //
            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 6, count.longValue());
        } finally {
            afterEmailNotificationServiceErrorHandlingTest(origParamsMap);
        }
    }

    /*
     * PSRV003705 - Customers are Receiving Multiple copies of acknowledgement e-mails from PSP
     *
     * Test BUSINESS_LOGIC error handling
     */
    @Test
    public void testEmailNotificationServiceBusinessLogicErrorHandling() {
        //We check if TXE or OINP is enabled. If it is enabled we do not want to run this test against TXE or OINP service so we exit the test
        Boolean isEtTxeEnabled = TxeEmailHelper.isEtTemplateTxeEnabled("IntegrationTest");
        OINPEmailHelper oinpEmailHelper = new OINPEmailHelper();
        Boolean isOINPEnabled = oinpEmailHelper.isTemplateOINPEnabled("IntegrationTest");
        if (isEtTxeEnabled || isOINPEnabled) {
            return;
        }

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "5000");
        PayrollServices.commitUnitOfWork();

        Map<SystemParameter.Code, String> origParamsMap = null;

        try {
            origParamsMap = beforeEmailNotificationServiceErrorHandlingTest();

            Expression<CompanyEventEmail> query;

            //
            // Retrieve the Recipient ID for one of the emails
            //
            Application.beginUnitOfWork();
            query = CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                    .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Pending));
            DomainEntitySet<CompanyEventEmail> emails = Application.find(CompanyEventEmail.class, query);

            CompanyEventEmail companyEventEmail = emails.get(0);
            CompanyEvent companyEvent = companyEventEmail.getCompanyEvent();
            Company company = companyEvent.getCompany();
            String recipientId = "%templateid%-%eventtype%-%companyid%-%guid%"
                    .replaceFirst("%templateid%", Matcher.quoteReplacement(companyEventEmail.getEmailTemplateTypeCd().toString()))
                    .replaceFirst("%eventtype%", Matcher.quoteReplacement(companyEvent.getEventTypeCd().toString()))
                    .replaceFirst("%companyid%", Matcher.quoteReplacement(company.getSourceCompanyId()))
                    .replaceFirst("%guid%", Matcher.quoteReplacement(companyEventEmail.getId().toString()));
            Application.commitUnitOfWork();

            //
            // Set the mock port factory to return a BUSINESS_LOGIC error on the given recipient id
            //
            MockNotificationPortFactory mockPortFactory = new MockNotificationPortFactory();
            NotificationServiceFactory.setPortFactory(mockPortFactory);
            mockPortFactory.forceBusinessLogicError(recipientId);

            //
            // Call the email gateway to process any Pending emails
            //
            new EmailGateway().processCompanyEventsForEmail();

            //
            // We should have the following at this point:
            // - 5 Sent QBDTPayrollConfirmation emails
            // - 1 SendFailed QBDTPayrollConfirmation emails (the one that matched the recipientId we set to err)
            //
            // Note that we need to do this in three count queries because GroupBy is not working properly...
            //
            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent)));
            Long count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 5, count.longValue());

            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.SendFailed)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 1, count.longValue());

            //
            // Remove any forced errors from the mock port factory to allow all emails to process
            //
            mockPortFactory.clearErrorStatus();

            //
            // Call the email gateway to process any Pending/Resend emails
            //
            new EmailGateway().processCompanyEventsForEmail();

            //
            // We should still have the following at this point:
            // - 5 Sent QBDTPayrollConfirmation emails
            // - 1 SendFailed QBDTPayrollConfirmation emails (the one that matched the recipientId we set to err)
            //
            // Note that we need to do this in three count queries because GroupBy is not working properly...
            //
            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 5, count.longValue());

            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.SendFailed)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 1, count.longValue());
        } finally {
            afterEmailNotificationServiceErrorHandlingTest(origParamsMap);
        }
    }

    /*
     * JIRA PSP-2366 - Handle emails undeliverable because of List Detective error - Core
     *
     * Test BUSINESS_LOGIC error (Error Code 24 - List Detective)
     */
    @Test
    public void testEmailNotificationListDetectiveErrorHandling() {
        //We check if TXE or OINP is enabled. If it is enabled we do not want to run this test against TXE or OINP service so we exit the test
        Boolean isEtTxeEnabled = TxeEmailHelper.isEtTemplateTxeEnabled("IntegrationTest");
        OINPEmailHelper oinpEmailHelper = new OINPEmailHelper();
        Boolean isOINPEnabled = oinpEmailHelper.isTemplateOINPEnabled("IntegrationTest");
        if (isEtTxeEnabled || isOINPEnabled) {
            return;
        }

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "5000");
        PayrollServices.commitUnitOfWork();
        Map<SystemParameter.Code, String> origParamsMap = null;

        try {
            origParamsMap = beforeEmailNotificationServiceErrorHandlingTest();

            Expression<CompanyEventEmail> query;

            //
            // Retrieve the Recipient ID for one of the emails
            //
            Application.beginUnitOfWork();
            query = CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                    .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Pending));
            DomainEntitySet<CompanyEventEmail> emails = Application.find(CompanyEventEmail.class, query);

            CompanyEventEmail companyEventEmail = emails.get(0);
            CompanyEvent companyEvent = companyEventEmail.getCompanyEvent();
            Company company = companyEvent.getCompany();
            String recipientId = "%templateid%-%eventtype%-%companyid%-%guid%"
                    .replaceFirst("%templateid%", Matcher.quoteReplacement(companyEventEmail.getEmailTemplateTypeCd().toString()))
                    .replaceFirst("%eventtype%", Matcher.quoteReplacement(companyEvent.getEventTypeCd().toString()))
                    .replaceFirst("%companyid%", Matcher.quoteReplacement(company.getSourceCompanyId()))
                    .replaceFirst("%guid%", Matcher.quoteReplacement(companyEventEmail.getId().toString()));
            Application.commitUnitOfWork();

            //
            // Set the mock port factory to return a BUSINESS_LOGIC error on the given recipient id
            //
            MockNotificationPortFactory mockPortFactory = new MockNotificationPortFactory();
            NotificationServiceFactory.setPortFactory(mockPortFactory);
            mockPortFactory.forceListDetectiveError(recipientId);

            //
            // Call the email gateway to process any Pending emails
            //
            new EmailGateway().processCompanyEventsForEmail();

            //
            // We should have the following at this point:
            // - 5 Sent QBDTPayrollConfirmation emails
            // - 1 SendFailedInvalidEmailId QBDTPayrollConfirmation emails (the one that matched the recipientId we set to err)
            //
            // Note that we need to do this in three count queries because GroupBy is not working properly...
            //
            Application.beginUnitOfWork();
            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent)));
            Long count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 5, count.longValue());

            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.SendFailedInvalidEmailId)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 1, count.longValue());

            //Resending the company event email which is in SendFailedInvalidEmailId, to check this will get updated to SendSkippedInvalidEmailId
            companyEventEmail = assertOne(CompanyEventEmail.findEmailEventsByStatus(EventEmailStatus.SendFailedInvalidEmailId));
            companyEventEmail.setStatusCd(EventEmailStatus.Resend);

            // Validating company event for SendFailedInvalidEmailId - Event Type Code is SendEmailFailed
            CompanyEvent companyEvent1 = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.SendEmailFailed));
            assertEquals("EmailTemplateTypeCd", EventEmailTemplateTypeCode.QBDTPayrollConfirmation, EventEmailTemplateTypeCode.valueOf(companyEvent1.getCompanyEventDetailValue(EventDetailTypeCode.EmailTemplateType)));
            assertEquals("Email error code", "SEND-NTS-31010", companyEvent1.getCompanyEventDetailValue(EventDetailTypeCode.ErrorCode));
            assertTrue("Email error message", companyEvent1.getCompanyEventDetailValue(EventDetailTypeCode.ErrorMessage).contains("Error Code: 24"));
            Application.commitUnitOfWork();

            //
            // Remove any forced errors from the mock port factory to allow all emails to process
            //
            mockPortFactory.clearErrorStatus();

            //
            // Call the email gateway to process any Pending/Resend emails
            //
            new EmailGateway().processCompanyEventsForEmail();

            //
            // We should still have the following at this point:
            // - 5 Sent QBDTPayrollConfirmation emails
            // - 1 SendSkippedInvalidEmailId QBDTPayrollConfirmation emails (the one that matched the recipientId we set to err)
            //
            // Note that we need to do this in three count queries because GroupBy is not working properly...
            //
            PayrollServices.beginUnitOfWork();
            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 5, count.longValue());

            query = new Query<CompanyEventEmail>()
                    .Select(CompanyEventEmail.StatusCd().Count())
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.QBDTPayrollConfirmation)
                            .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.SendSkippedInvalidEmailId)));
            count = Application.executeScalarAggQuery(CompanyEventEmail.class, query);

            assertEquals("CompanyEventEmail count", 1, count.longValue());

            // Validating company event for SendSkippedInvalidEmailId - Event Type Code is SendEmailSkipped
            companyEvent1 = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.SendEmailSkipped));
            assertEquals("EmailTemplateTypeCd", EventEmailTemplateTypeCode.QBDTPayrollConfirmation, EventEmailTemplateTypeCode.valueOf(companyEvent1.getCompanyEventDetailValue(EventDetailTypeCode.EmailTemplateType)));
            PayrollServices.rollbackUnitOfWork();
        } finally {
            afterEmailNotificationServiceErrorHandlingTest(origParamsMap);
        }
    }

    /*
     * JIRA PSP-2500 - Handle email errors when BillPaymentOffloaded vendor email addresses are invalid - Core
     * Test BUSINESS_LOGIC error (Error Code 24 - List Detective)
     */
    @Test
    public void testVendorEmailNotificationListDetectiveErrorHandling() {
        //We check if TXE or OINP is enabled. If it is enabled we do not want to run this test against TXE or OINP service so we exit the test
        Boolean isEtTxeEnabled = TxeEmailHelper.isEtTemplateTxeEnabled("IntegrationTest");
        OINPEmailHelper oinpEmailHelper = new OINPEmailHelper();
        Boolean isOINPEnabled = oinpEmailHelper.isTemplateOINPEnabled("IntegrationTest");
        if (isEtTxeEnabled || isOINPEnabled) {
            return;
        }

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "5000");
        PayrollServices.commitUnitOfWork();
        Map<SystemParameter.Code, String> origParamsMap = null;

        try {
            origParamsMap = beforeEmailNotificationServiceErrorHandlingTest();
            String sourceCompanyId = "123272727";

            // company setup
            PayrollServices.beginUnitOfWork();
            PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
            psdl.loadDataForBillPaymentSubmit();
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

            Payee p = assertOne(Application.<Payee>find(Payee.class, Payee.SourcePayeeId().equalTo("Payee1")));

            PayeeDTO payeeDTO = new PayeeDTO();
            payeeDTO.setEmail("a@a.com;b@b.com");
            payeeDTO.setIs1099(p.getIs1099());
            payeeDTO.setName(p.getName());
            payeeDTO.setPhone(p.getPhone());
            payeeDTO.setSourcePayeeId(p.getSourcePayeeId());
            payeeDTO.setTaxId(p.getTaxId());
            assertSuccess(PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO));
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);

            Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
            billPaymentDTOs.add(billPaymentDTO);

            ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);

            DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.BillPaymentReceived);
            assertEquals("BillPaymentReceived event count", 1, events.size());
            PayrollServices.commitUnitOfWork();

            assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);
            // offload all txns
            OffloadACHTransactions offloader = new OffloadACHTransactions();
            // offload QBOE ER DB
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20070910000000");
            Application.commitUnitOfWork();

            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            Expression<CompanyEventEmail> query;

            //
            // Retrieve the Recipient ID for one of the emails
            //
            Application.beginUnitOfWork();
            query = CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.VendorPaymentOffloadedForWriteChecks)
                    .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Pending));
            DomainEntitySet<CompanyEventEmail> emails = Application.find(CompanyEventEmail.class, query);

            // Note we are forcing only the first email in the list as listdetecive error
            CompanyEventEmail companyEventEmail = emails.get(0);
            CompanyEvent companyEvent = companyEventEmail.getCompanyEvent();
            String recipientId = "%templateid%-%eventtype%-%companyid%-%guid%"
                    .replaceFirst("%templateid%", Matcher.quoteReplacement(companyEventEmail.getEmailTemplateTypeCd().toString()))
                    .replaceFirst("%eventtype%", Matcher.quoteReplacement(companyEvent.getEventTypeCd().toString()))
                    .replaceFirst("%companyid%", Matcher.quoteReplacement(company.getSourceCompanyId()))
                    .replaceFirst("%guid%", Matcher.quoteReplacement(companyEventEmail.getId().toString()));

            if (recipientId.length() > 100) {
                recipientId = recipientId.substring(0, 99);
            }
            Application.commitUnitOfWork();

            //
            // Set the mock port factory to return a BUSINESS_LOGIC error on the given recipient id
            //
            MockNotificationPortFactory mockPortFactory = new MockNotificationPortFactory();
            NotificationServiceFactory.setPortFactory(mockPortFactory);
            mockPortFactory.forceListDetectiveError(recipientId);

            //
            // Call the email gateway to process any Pending emails
            //
            new EmailGateway().processCompanyEventsForEmail();

            PayrollServices.beginUnitOfWork();
            Application.refresh(companyEventEmail);
            assertEquals("Event email status", EventEmailStatus.SendFailedInvalidEmailId, companyEventEmail.getStatusCd());
            Payee payee = Payee.findPayee(company, "Payee1");
            assertTrue("Email Invalid flag", payee.getHasInvalidEmail());
            PayrollServices.rollbackUnitOfWork();

        } finally {
            afterEmailNotificationServiceErrorHandlingTest(origParamsMap);
        }
    }

    //Empty value should not make it through
    @Test
    public void testEmptyCompanyDbaNameEmail() {
        Employee employee = null;
        try {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.Cloud);
            PayrollServices.beginUnitOfWork();
            Application.refresh(company);
            company.setDbaName("");
            Application.save(company);
            employee = company.getEmployees().getFirst();
            CompanyEvent.createVmpSignUpEmployeeEmailEvent(employee, employee.getEmail());
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        List<EventEmailTemplate> emailTemplates = findEmailTemplates();
        List<IEventEmail> emails = emailTemplates.get(0).getRecipientsToTransmit().get(0);
        IEventEmail eventEmail = emails.get(0);
        Properties eventEmailProperties = eventEmail.getProperties();
        //No empty value should exist for DBA
        Assert.assertNull(eventEmailProperties.getProperty(EventEmailParamTypeCode.CompanyDBAName.toString()));
    }

    //Null value should not make it through
    @Test
    public void testNullCompanyDbaNameEmail() {
        Employee employee = null;
        try {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.Cloud);
            PayrollServices.beginUnitOfWork();
            Application.refresh(company);
            company.setDbaName(null);
            Application.save(company);
            employee = company.getEmployees().getFirst();
            CompanyEvent.createVmpSignUpEmployeeEmailEvent(employee, employee.getEmail());
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        List<EventEmailTemplate> emailTemplates = findEmailTemplates();
        List<IEventEmail> emails = emailTemplates.get(0).getRecipientsToTransmit().get(0);
        IEventEmail eventEmail = emails.get(0);
        Properties eventEmailProperties = eventEmail.getProperties();
        //No empty value should exist for DBA
        Assert.assertNull(eventEmailProperties.getProperty(EventEmailParamTypeCode.CompanyDBAName.toString()));
    }

    @Test
    public void testVmpSignUpEmployeeEmail() {
        Employee employee = null;
        try {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.Cloud);
            PayrollServices.beginUnitOfWork();
            employee = company.getEmployees().getFirst();
            CompanyEvent.createVmpSignUpEmployeeEmailEvent(employee, employee.getEmail());
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        List<EventEmailTemplate> emailTemplates = findEmailTemplates();
        List<IEventEmail> emails = emailTemplates.get(0).getRecipientsToTransmit().get(0);
        IEventEmail eventEmail = emails.get(0);
        Properties eventEmailProperties = eventEmail.getProperties();
        Assert.assertEquals(employee.getEmail(), eventEmail.getRecipientEmail());
        Assert.assertEquals(employee.getFirstName() + " " + employee.getLastName(), eventEmail.getRecipientName());
        Assert.assertEquals(employee.getFirstName(), eventEmailProperties.getProperty(EventEmailParamTypeCode.EmployeeFirstName.toString()));
        Assert.assertEquals(employee.getCompany().getDbaName(), eventEmailProperties.getProperty(EventEmailParamTypeCode.CompanyDBAName.toString()));
    }

    @Test
    public void testVmpSignUpEmployerEmail() {
        Employee employee = null;
        try {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.Cloud);
            PayrollServices.beginUnitOfWork();
            employee = company.getEmployees().getFirst();
            CompanyEvent.createVmpSignUpEmployerEmailEvent(employee);
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        List<EventEmailTemplate> emailTemplates = findEmailTemplates();
        List<IEventEmail> emails = emailTemplates.get(0).getRecipientsToTransmit().get(0);
        IEventEmail eventEmail = emails.get(0);
        Properties eventEmailProperties = eventEmail.getProperties();
        Contact payrollAdmin = employee.getCompany().getContactByRoleCode(ContactRole.PayrollAdmin);
        Assert.assertEquals(payrollAdmin.getEmail(), eventEmail.getRecipientEmail());
        //Last name is changed to all lowercase except first letter
        String payrollAdminLastName = payrollAdmin.getLastName().substring(0, 1).toUpperCase() + payrollAdmin.getLastName().substring(1).toLowerCase();
        Assert.assertEquals(payrollAdmin.getFirstName() + " " + payrollAdminLastName, eventEmail.getRecipientName());
        Assert.assertEquals(employee.getFirstName(), eventEmailProperties.getProperty(EventEmailParamTypeCode.EmployeeFirstName.toString()));
        Assert.assertEquals(employee.getLastName(), eventEmailProperties.getProperty(EventEmailParamTypeCode.EmployeeLastName.toString()));
        Assert.assertEquals(payrollAdmin.getFirstName(), eventEmailProperties.getProperty(EventEmailParamTypeCode.PayrollAdminFirstName.toString()));
        Assert.assertEquals(employee.getCompany().getDbaName(), eventEmailProperties.getProperty(EventEmailParamTypeCode.CompanyDBAName.toString()));
    }

    @Test
    @Ignore("used to test the email gateway all the way through IAS")
    public void testSendEmails() {
        PayrollServicesTest.beforeEachTest();
        new EmailGateway().processCompanyEventsForEmail();
    }

    //JIRA : PSP-3326 Courtesy fee refund emails fail - Validating the code changes for this JIRA
    @Test
    public void test_CourtesyFeeRefundEmailSend() {
        //We check if TXE or OINP is enabled. If it is enabled we do not want to run this test against TXE or OINP service so we exit the test
        Boolean isEtTxeEnabled = TxeEmailHelper.isEtTemplateTxeEnabled("IntegrationTest");
        OINPEmailHelper oinpEmailHelper = new OINPEmailHelper();
        Boolean isOINPEnabled = oinpEmailHelper.isTemplateOINPEnabled("IntegrationTest");
        if (isEtTxeEnabled || isOINPEnabled) {
            return;
        }
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Assert.assertNotNull(company);
        CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
        Assert.assertNotNull(taxService);
        junit.framework.Assert.assertEquals("Company Service Status", ServiceSubStatusCode.ActiveCurrent, taxService.getStatusCd());

        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        Assert.assertNotNull(payroll);
        DataLoadServices.assertPayrollsEqual(payrollDTO, payroll);
        PayrollServices.commitUnitOfWork();

        SpcfCalendar settlementDate = SpcfCalendar.createInstance(2012, 4, 24, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate = SpcfCalendar.createInstance(2012, 4, 23, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(2012, 4, 23);

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> refundResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), new SpcfMoney("5.00"), "NoteText - Testing", SettlementTypeDTO.ACH);
        FinancialTransaction financialTransaction = refundResult.getResult();
        assertSuccess(refundResult);
        assertEquals("Refund Transaction type", TransactionType.findTransactionType(TransactionTypeCode.ERCourtesyRefundCredit), financialTransaction.getTransactionType());
        assertEquals("Refund Txn status", TransactionState.findTransactionState(TransactionStateCode.Created), financialTransaction.getCurrentTransactionState());
        assertEquals("Refund Txn Amount", new SpcfMoney("5.00"), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Refund Txn settlement type", SettlementType.ACH, financialTransaction.getSettlementTypeCd());
        assertEquals("Refund Txn settlement date", settlementDate, financialTransaction.getSettlementDate().toLocal());

        assertEquals("MMT Txn Amount", new SpcfMoney("5.00"), financialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("MMT Txn Payment method", PaymentMethod.ACHDirectDeposit, financialTransaction.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
        assertEquals("MMT Due date", settlementDate, financialTransaction.getMoneyMovementTransaction().getDueDate().toLocal());
        assertEquals("MMT Init date", initDate, financialTransaction.getMoneyMovementTransaction().getInitiationDate().toLocal());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> emailEvents = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.RefundedFeeAmount1);
        assertEquals("Company email events", 1, emailEvents.size());
        assertNotNull("CompanyEventEmail company null",emailEvents.get(0).getCompany());
        PayrollServices.rollbackUnitOfWork();

        // Call the email gateway to process any Pending emails
        new EmailGateway().processCompanyEventsForEmail();

        PayrollServices.beginUnitOfWork();
        assertOne(Application.find(CompanyEventEmail.class, CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.AssistedPayrollConfirmation)
                .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent))));
        assertOne(Application.find(CompanyEventEmail.class, CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.RefundedFeeAmount1)
                .And(CompanyEventEmail.StatusCd().equalTo(EventEmailStatus.Sent))));
        PayrollServices.rollbackUnitOfWork();
    }

    //  Test DG Disassociated Company
    @Test
    public void testForDGDeletedCompany() {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "5000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventEmail> pendingEventList = CompanyEventEmail.findEmailEventsByStatus(EventEmailStatus.Pending);
        assertEquals("Pending Events", 0, pendingEventList.size());
        PayrollServices.commitUnitOfWork();

        beforeEachTestQBDT_BP();


        PayrollServices.beginUnitOfWork();
        pendingEventList = CompanyEventEmail.findEmailEventsByStatus(EventEmailStatus.Pending);
        assertEquals("Pending Events", 8, pendingEventList.size());
        assertNotNull("CompanyEventEmail company null",pendingEventList.get(0).getCompany());
        assertEquals(pendingEventList.get(0).getCompanyEvent().getCompany().getSourceCompanyId(),
                pendingEventList.get(0).getCompany().getSourceCompanyId());
        PayrollServices.commitUnitOfWork();

        try {
            int count = 0;
            String psid = pendingEventList.get(0).getCompanyEvent().getCompany().getSourceCompanyId();
            Company dgDisassociatedCompany;

            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "1");
            PayrollServices.commitUnitOfWork();

            new EmailGateway().processCompanyEventsForEmail();

            PayrollServices.beginUnitOfWork();
            count=0;
            for(CompanyEventEmail companyEventEmail : pendingEventList) {
                Application.refresh(companyEventEmail);
                if(companyEventEmail.getStatusCd().toString().equals("Pending")) {
                    count++;
                }
            }
            assertEquals("Pending Events", 7, count);
            PayrollServices.commitUnitOfWork();

            //Mark Company as Dg Disassociated
            PayrollServices.beginUnitOfWork();
            dgDisassociatedCompany = Company.findCompany(psid, SourceSystemCode.QBDT);
            dgDisassociatedCompany.setIsDgDisassociated(Boolean.TRUE);
            PayrollServices.commitUnitOfWork();

            new EmailGateway().processCompanyEventsForEmail();

            PayrollServices.beginUnitOfWork();
            count=0;
            for(CompanyEventEmail companyEventEmail : pendingEventList) {
                Application.refresh(companyEventEmail);
                if (companyEventEmail.getStatusCd().toString().equals("Pending")) {
                    count++;
                }
            }
            assertEquals("Pending Events", 7, count);
            PayrollServices.commitUnitOfWork();

            //Unmark Company as Dg Disassociated
            PayrollServices.beginUnitOfWork();
            Application.refresh(dgDisassociatedCompany);
            dgDisassociatedCompany.setIsDgDisassociated(Boolean.FALSE);
            PayrollServices.commitUnitOfWork();

            new EmailGateway().processCompanyEventsForEmail();

            PayrollServices.beginUnitOfWork();
            count=0;
            for(CompanyEventEmail companyEventEmail : pendingEventList) {
                Application.refresh(companyEventEmail);
                if (companyEventEmail.getStatusCd().toString().equals("Pending")) {
                    count++;
                }
            }
            assertEquals("Pending Events", 6, count);
            PayrollServices.commitUnitOfWork();

        } catch (Throwable t) {
            try {
                PayrollServices.rollbackUnitOfWork();
            } catch (Throwable th) {
                //Nothing
            }

        } finally {
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, "5000");
            PayrollServices.commitUnitOfWork();
        }
    }

    public static Boolean getFeatureFlagValue(FeatureFlags.Key key){
        Boolean bFeatureFlag = false;
        if(FeatureFlags.get().booleanValue(key,false)){
            bFeatureFlag = true;
            return bFeatureFlag;
        }
        return bFeatureFlag;
    }
}
