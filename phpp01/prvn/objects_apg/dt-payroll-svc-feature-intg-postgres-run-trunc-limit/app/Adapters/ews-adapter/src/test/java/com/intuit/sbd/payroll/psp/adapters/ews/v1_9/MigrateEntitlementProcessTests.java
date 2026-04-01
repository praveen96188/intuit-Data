package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsEntitlementStateCode;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsEntitlementUnitStatusCode;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.CreateAccountProcess;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.MigrateEntitlementProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class MigrateEntitlementProcessTests {

    private String psid = null;
    private Boolean assistedTest;
    private EwsCreateAccount mCreateRequest;
    private EwsCreateAccountResponse mCreateResponse;
    private EwsMigrateEntitlement mMigrateRequest;
    private EwsMigrateEntitlementResponse mMigrateResponse;

    @BeforeClass
    public static void beforeClass() {
        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByPromotionId("1099426");
        SpcfCalendar newOfferEndDate = SpcfCalendar.createInstance();
        newOfferEndDate.addDays(30);
        offer.setEndDate(newOfferEndDate);
        Application.save(offer);
        PayrollServices.commitUnitOfWork();
    }

    @AfterClass
    public static void afterClass() {
        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByPromotionId("1099426");
        SpcfCalendar oldOfferEndDate = SpcfCalendar.createInstance(2013, 7, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        offer.setEndDate(oldOfferEndDate);
        Application.save(offer);
        PayrollServices.commitUnitOfWork();
    }

    @Before
    public void startUp() {
        assistedTest = false;
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void afterEachTest() {

        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void migrateEntitlementFromDIYtoAssisted() {
        assistedTest = true;
        mCreateRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getPsid());
        psid = mCreateResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mCreateResponse.getCompanyResponse());

        assertNotNull(mCreateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mCreateResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mCreateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("16", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mCreateResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mCreateResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());

        mMigrateRequest = TestDataFactory.createEwsMigrateEntitlement(mCreateRequest.getEwsCompany(), psid);

        MigrateEntitlementProcess migrateProcess = new MigrateEntitlementProcess(mMigrateRequest);
        mMigrateResponse = migrateProcess.execute();

        assertNotNull(mMigrateResponse);

        assertNotNull(mMigrateResponse.getPsid());
        psid = mMigrateResponse.getPsid();

        assertNotSame(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber());
        assertNotSame(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey());
    }

    @Test
    public void migrateAccountFromDIY_X_to_DIY_Y() {
        assistedTest = true;
        mCreateRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getPsid());
        psid = mCreateResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mCreateResponse.getCompanyResponse());

        assertNotNull(mCreateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mCreateResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mCreateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("16", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mCreateResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mCreateResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());

        // Modify Account

        mMigrateRequest = TestDataFactory.createEwsMigrateEntitlementDIY(mCreateRequest.getEwsCompany(), psid);
        mMigrateRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("0987654321");

        MigrateEntitlementProcess migrateProcess = new MigrateEntitlementProcess(mMigrateRequest);
        mMigrateResponse = migrateProcess.execute();

        assertNotNull(mMigrateResponse);

        assertNotNull(mMigrateResponse.getPsid());
        psid = mMigrateResponse.getPsid();

        assertNotNull(mMigrateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mMigrateResponse.getEwsEntitlementUnitResponses().size());
        ewsEntitlementUnitResponse = mMigrateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());
        assertEquals("1099581", ewsEntitlementResponse.getAssetItemNumber());
        assertEquals("1", ewsEntitlementResponse.getLicenseNumber());
        assertEquals("0987654321", ewsEntitlementResponse.getEntitlementOfferingCode());
        assertEquals("15", ewsEntitlementResponse.getSubType());

        assertEquals(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber());
        assertEquals(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey());
    }

    @Test
    public void migrateAccountFromDIY_X_to_DIY_YWithDDTerminated() {
        mCreateRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        psid = mCreateResponse.getPsid();

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());

        // Cancel DD
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.updateServiceStatus
                (SourceSystemCode.QBDT, mCreateResponse.getPsid(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Terminated);
        assertTrue(servicePR.isSuccess());
        PayrollServices.commitUnitOfWork();

        // Modify Account
        mMigrateRequest = TestDataFactory.createEwsMigrateEntitlementDIY(mCreateRequest.getEwsCompany(), psid);
        mMigrateRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("0987654321");

        MigrateEntitlementProcess migrateProcess = new MigrateEntitlementProcess(mMigrateRequest);
        mMigrateResponse = migrateProcess.execute();

        assertNotNull(mMigrateResponse);

        assertNotNull(mMigrateResponse.getPsid());
        psid = mMigrateResponse.getPsid();

        assertNotNull(mMigrateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mMigrateResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mMigrateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());
        assertEquals("1099581", ewsEntitlementResponse.getAssetItemNumber());
        assertEquals("1", ewsEntitlementResponse.getLicenseNumber());
        assertEquals("0987654321", ewsEntitlementResponse.getEntitlementOfferingCode());
        assertEquals("15", ewsEntitlementResponse.getSubType());

        assertEquals(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber());
        assertEquals(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey());
    }

    @Test
    public void migrateAccountFromDIY_With_Null_Edition() {
        assistedTest = true;
        mCreateRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getPsid());
        psid = mCreateResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mCreateResponse.getCompanyResponse());

        assertNotNull(mCreateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mCreateResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mCreateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("16", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mCreateResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mCreateResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());

        // Modify Account

        mMigrateRequest = TestDataFactory.createEwsMigrateEntitlementDIY(mCreateRequest.getEwsCompany(), psid);
        mMigrateRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("0987654321");
        mMigrateRequest.getEwsEntitlements().get(0).setEdition(null);

        MigrateEntitlementProcess migrateProcess = new MigrateEntitlementProcess(mMigrateRequest);
        mMigrateResponse = migrateProcess.execute();

        assertNotNull(mMigrateResponse);

        assertNotNull(mMigrateResponse.getEwsResponseStatus());
        ewsResponseStatus = mMigrateResponse.getEwsResponseStatus();
        assertEquals(30004, ewsResponseStatus.getCode());
        assertEquals("Field Edition in Entitlement is required and cannot be null or empty", ewsResponseStatus.getMessage());
    }

    @Test
    public void migrateAccountFromDIY_X_to_DIY_YDeactivatedEUandE() {
        assistedTest = true;
        mCreateRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getPsid());
        psid = mCreateResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mCreateResponse.getCompanyResponse());

        assertNotNull(mCreateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mCreateResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mCreateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("16", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mCreateResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mCreateResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());

        // Modify Account
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCreateResponse.getPsid(), SourceSystemCode.QBDT);
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        Application.save(eu);
        Entitlement e = eu.getEntitlement();
        e.setEntitlementState(EntitlementStateCode.Disabled);
        Application.save(e);
        PayrollServices.commitUnitOfWork();

        mMigrateRequest = TestDataFactory.createEwsMigrateEntitlementDIY(mCreateRequest.getEwsCompany(), psid);
        mMigrateRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("0987654321");

        MigrateEntitlementProcess migrateProcess = new MigrateEntitlementProcess(mMigrateRequest);
        mMigrateResponse = migrateProcess.execute();

        assertNotNull(mMigrateResponse);

        assertNotNull(mMigrateResponse.getPsid());
        psid = mMigrateResponse.getPsid();

        assertNotNull(mMigrateResponse.getEwsEntitlementUnitResponses());
        assertEquals(2, mMigrateResponse.getEwsEntitlementUnitResponses().size());

        for (EwsEntitlementUnitResponse ewsEUR : mMigrateResponse.getEwsEntitlementUnitResponses()) {
            if (ewsEUR.getStatus().equals(EwsEntitlementUnitStatusCode.Activated)) {
                ewsEntitlementUnitResponse = ewsEUR;
            }
        }

        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());
        assertEquals("1099581", ewsEntitlementResponse.getAssetItemNumber());
        assertEquals("1", ewsEntitlementResponse.getLicenseNumber());
        assertEquals("0987654321", ewsEntitlementResponse.getEntitlementOfferingCode());
        assertEquals("15", ewsEntitlementResponse.getSubType());

        assertFalse(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber().equals(ewsEntitlementUnitResponse.getEwsEntitlementResponse().getSubscriptionNumber()));
        assertFalse(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey().equals(ewsEntitlementUnitResponse.getServiceKey()));
    }

    @Test
    public void migrateAccountFromDIY_X_to_DIY_YDeactivatedEU() {
        assistedTest = true;
        mCreateRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getPsid());
        psid = mCreateResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mCreateResponse.getCompanyResponse());

        assertNotNull(mCreateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mCreateResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mCreateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("16", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mCreateResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mCreateResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());

        // Modify Account
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCreateResponse.getPsid(), SourceSystemCode.QBDT);
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        Application.save(eu);
        PayrollServices.commitUnitOfWork();

        mMigrateRequest = TestDataFactory.createEwsMigrateEntitlementDIY(mCreateRequest.getEwsCompany(), psid);
        mMigrateRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("0987654321");

        MigrateEntitlementProcess migrateProcess = new MigrateEntitlementProcess(mMigrateRequest);
        mMigrateResponse = migrateProcess.execute();

        assertNotNull(mMigrateResponse);

        assertNotNull(mMigrateResponse.getPsid());
        psid = mMigrateResponse.getPsid();

        assertNotNull(mMigrateResponse.getEwsEntitlementUnitResponses());
        assertEquals(2, mMigrateResponse.getEwsEntitlementUnitResponses().size());

        for (EwsEntitlementUnitResponse ewsEUR : mMigrateResponse.getEwsEntitlementUnitResponses()) {
            if (ewsEUR.getStatus().equals(EwsEntitlementUnitStatusCode.Activated)) {
                ewsEntitlementUnitResponse = ewsEUR;
            }
        }

        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());
        assertEquals("1099581", ewsEntitlementResponse.getAssetItemNumber());
        assertEquals("1", ewsEntitlementResponse.getLicenseNumber());
        assertEquals("0987654321", ewsEntitlementResponse.getEntitlementOfferingCode());
        assertEquals("15", ewsEntitlementResponse.getSubType());

        assertFalse(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber().equals(ewsEntitlementUnitResponse.getEwsEntitlementResponse().getSubscriptionNumber()));
        assertFalse(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey().equals(ewsEntitlementUnitResponse.getServiceKey()));
    }

    @Test
    public void migrateAccountFromDIY_X_to_DIY_YDeactivatedE() {
        assistedTest = true;
        mCreateRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getPsid());
        psid = mCreateResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mCreateResponse.getCompanyResponse());

        assertNotNull(mCreateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mCreateResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mCreateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("16", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mCreateResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mCreateResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());

        // Modify Account
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCreateResponse.getPsid(), SourceSystemCode.QBDT);
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        Entitlement e = eu.getEntitlement();
        e.setEntitlementState(EntitlementStateCode.Disabled);
        Application.save(e);
        PayrollServices.commitUnitOfWork();

        mMigrateRequest = TestDataFactory.createEwsMigrateEntitlementDIY(mCreateRequest.getEwsCompany(), psid);
        mMigrateRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("0987654321");

        MigrateEntitlementProcess migrateProcess = new MigrateEntitlementProcess(mMigrateRequest);
        mMigrateResponse = migrateProcess.execute();

        assertNotNull(mMigrateResponse);

        assertNotNull(mMigrateResponse.getPsid());
        psid = mMigrateResponse.getPsid();

        assertNotNull(mMigrateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mMigrateResponse.getEwsEntitlementUnitResponses().size());

        ewsEntitlementUnitResponse = mMigrateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());
        assertEquals("1099581", ewsEntitlementResponse.getAssetItemNumber());
        assertEquals("1", ewsEntitlementResponse.getLicenseNumber());
        assertEquals("0987654321", ewsEntitlementResponse.getEntitlementOfferingCode());
        assertEquals("15", ewsEntitlementResponse.getSubType());

        assertEquals(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber());
        assertEquals(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey());
    }

    @Test
    public void migrateAccountFrom_Nothing_to_DIY_Y() {
        assistedTest = true;
        mCreateRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getPsid());
        psid = mCreateResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mCreateResponse.getCompanyResponse());

        assertNotNull(mCreateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mCreateResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mCreateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("16", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mCreateResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mCreateResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());

        // Modify Account
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCreateResponse.getPsid(), SourceSystemCode.QBDT);
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        Application.delete(eu);
        PayrollServices.commitUnitOfWork();

        mMigrateRequest = TestDataFactory.createEwsMigrateEntitlementDIY(mCreateRequest.getEwsCompany(), psid);
        mMigrateRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("0987654321");

        MigrateEntitlementProcess migrateProcess = new MigrateEntitlementProcess(mMigrateRequest);
        mMigrateResponse = migrateProcess.execute();

        assertNotNull(mMigrateResponse);

        assertNotNull(mMigrateResponse.getPsid());
        psid = mMigrateResponse.getPsid();

        assertNotNull(mMigrateResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mMigrateResponse.getEwsEntitlementUnitResponses().size());

        ewsEntitlementUnitResponse = mMigrateResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());
        assertEquals("1099581", ewsEntitlementResponse.getAssetItemNumber());
        assertEquals("1", ewsEntitlementResponse.getLicenseNumber());
        assertEquals("0987654321", ewsEntitlementResponse.getEntitlementOfferingCode());
        assertEquals("15", ewsEntitlementResponse.getSubType());

        assertFalse(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber().equals(mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber()));
        assertFalse(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey().equals(mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey()));
    }

}
