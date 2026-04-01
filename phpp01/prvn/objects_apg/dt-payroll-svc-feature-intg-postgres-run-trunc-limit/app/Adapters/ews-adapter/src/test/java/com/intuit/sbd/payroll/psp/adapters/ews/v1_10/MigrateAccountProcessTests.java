package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsEntitlementStateCode;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsEntitlementUnitStatusCode;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsServiceStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.CreateAccountProcess;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.MigrateAccountProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Marcela Villani
 */
public class MigrateAccountProcessTests {

    private String psid = null;
    private Boolean assistedTest;
    private EwsCreateAccount mCreateRequest;
    private EwsCreateAccountResponse mCreateResponse;
    private EwsMigrateAccount mMigrateRequest;
    private EwsMigrateAccountResponse mMigrateResponse;

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


    //Test not valid for prod like EWS flows.
    @Ignore
    @Test
    public void migrateAccountFromDIYtoAssisted() {
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

        mMigrateRequest = TestDataFactory.createEwsMigrateAccount(mCreateRequest.getEwsCompany(), psid);

        MigrateAccountProcess migrateProcess = new MigrateAccountProcess(mMigrateRequest);
        mMigrateResponse = migrateProcess.execute();

        assertNotNull(mMigrateResponse);

        assertNotNull(mMigrateResponse.getPsid());
        psid = mMigrateResponse.getPsid();

        assertNotSame(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber());
        assertNotSame(mCreateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey(), mMigrateResponse.getEwsEntitlementUnitResponses().get(0).getServiceKey());
    }

    @Test
    public void migrateAccountFromDIY_X_to_DIY_Y() throws Exception {
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

        mMigrateRequest = TestDataFactory.createEwsMigrateAccountDIY(mCreateRequest.getEwsCompany(), psid);
        mMigrateRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("0987654321");

        MigrateAccountProcess migrateProcess = new MigrateAccountProcess(mMigrateRequest);
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

        Company company = PspFactory.findCompany(psid);
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                        TransmissionType.MigrateAccount),"v1_10/test_migrateAccountFromDIY_X_to_DIY_Y.xml",
                Arrays.asList("DateTimeStamp", "PSID", "EIN", "ServiceKey", "SubscriptionNumber"));
    }

    @Test
    public void migrateAccountAssisted() {
        assistedTest = true;
        mCreateRequest = TestDataFactory.createEwsCreateAccountAssisted();

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getPsid());
        psid = mCreateResponse.getPsid();

        Pattern pattern = Pattern.compile("^9\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

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
        assertEquals("4", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mCreateResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mCreateResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getDirectDepositResponse());

        assertNotNull(ewsServicesResponse.getAssistedResponse());
        //Todo this may not be the right status
        assertEquals(EwsServiceStatus.PendingActivation, ewsServicesResponse.getAssistedResponse().getStatus());

        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());
    }

   

    @Test
    public void createAccountFieldValidation_EIN() {
        mCreateRequest = TestDataFactory.createEwsCreateAccountAssisted();

        mCreateRequest.getEwsCompany().setEin("87654321");

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(30005, ewsResponseStatus.getCode());
        assertEquals("Field EIN in Company does not contain valid data", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountFieldValidationEin() {
        mCreateRequest = TestDataFactory.createEwsCreateAccountAssisted();

        mCreateRequest.getEwsCompany().setEin("87654321");

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(30005, ewsResponseStatus.getCode());
        assertEquals("Field EIN in Company does not contain valid data", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountFieldValidationPayrollAdminEmail() {
        mCreateRequest = TestDataFactory.createEwsCreateAccount();

        mCreateRequest.getEwsCompany().getPayrollAdmin().seteMail("Bad eMail");

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(30005, ewsResponseStatus.getCode());
        assertEquals("Field eMail in Contact does not contain valid data", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountFieldValidationEdition() {
        mCreateRequest = TestDataFactory.createEwsCreateAccount();

        mCreateRequest.getEwsEntitlements().get(0).setEdition(null);

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(30004, ewsResponseStatus.getCode());
        assertEquals("Field Edition in Entitlement is required and cannot be null or empty", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountFieldValidationTier() {
        mCreateRequest = TestDataFactory.createEwsCreateAccount();

        mCreateRequest.getEwsEntitlements().get(0).setTier(null);

        CreateAccountProcess process = new CreateAccountProcess(mCreateRequest, false);
        mCreateResponse = process.execute();

        assertNotNull(mCreateResponse);

        assertNotNull(mCreateResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mCreateResponse.getEwsResponseStatus();
        assertEquals(30004, ewsResponseStatus.getCode());
        assertEquals("Field Tier in Entitlement is required and cannot be null or empty", ewsResponseStatus.getMessage());

        assertNotNull(mCreateResponse.getDateTimeStamp());
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
        mMigrateRequest = TestDataFactory.createEwsMigrateAccountDIY(mCreateRequest.getEwsCompany(), psid);
        mMigrateRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("0987654321");

        MigrateAccountProcess migrateProcess = new MigrateAccountProcess(mMigrateRequest);
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
}
