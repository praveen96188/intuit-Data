package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;


import com.intuit.client.ius.GrantType;
import com.intuit.client.ius.IUSGrantClient;
import com.intuit.ems.payroll.psp.gateways.ers.ERSGateway;
import com.intuit.ems.payroll.psp.gateways.ers.ERSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ers.ERSMockGateway;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementInfoDTO;
import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.platform.integration.ius.common.types.NameValuePair;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessage;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQuickBooks;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsValidateSubscription;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsValidateSubscriptionResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsEinSubscriptionStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsPaymentMethod;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.webservices.EWSAdapter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOWSGateway;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOWSGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOWSMockGateway;
import com.intuit.sbd.payroll.psp.gateways.amo.GetCustomerAssetResponseTypeDTO;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.ius.IUSDataGenerator;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: rnorian
 * Date: Jul 21, 2010
 * Time: 11:04:20 PM
 */
public class ValidateSubscriptionProcessTests {

    private CreateAccountProcessTests mCreateAccountProcessTests = new CreateAccountProcessTests();
    private IUSDataGenerator iusDataGenerator = new IUSDataGenerator();

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
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();

        AMOWSMockGateway.clear();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        ERSGatewayFactory.setInstanceClass(ERSGateway.class);
        AMOWSGatewayFactory.setInstanceClass(AMOWSGateway.class);
    }

    @Test
    public void validateSubscriptionDD() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        String subscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getLicenseNumber();
        String eoc = company.getActivePrimaryEntitlementUnit().getEntitlement().getEntitlementOfferingCode();
        PayrollServices.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, subscriptionNumber, eoc);
        Application.commitUnitOfWork();

        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);
        assertEquals("subscription status", validateResponse.getSubscriptionStatus(), EwsEinSubscriptionStatus.Activated);
        assertNotNull("subscription status", validateResponse.getSubscriptionStatus());
        assertNotNull("legal info", validateResponse.getCompanyLegalInfo());
        assertNotNull("qb account name", validateResponse.getQbAccountName());
        assertNotNull("billing info", validateResponse.getSubscriptionBillingInfo());
        //todo_rhn: add assertion when value is implemented
        //assertNotNull("subscription end date", validateResponse.getSubscriptionEndDate());
        assertEquals("end date, charge date", validateResponse.getSubscriptionEndDate(), validateResponse.getSubscriptionBillingInfo().getSubscriptionNextBillDate());

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        assertNotNull(eu.getLastValidationDate());
        SpcfCalendar oldValue = eu.getLastValidationDate();
        Application.rollbackUnitOfWork();

        webservice = new EWSAdapter();
        validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        eu = company.getActivePrimaryEntitlementUnit();
        assertEquals(oldValue.toString(), eu.getLastValidationDate().toString());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void validateSubscriptionGrantAddSuccess() {
        if(!StringUtils.equalsIgnoreCase(FeatureFlags.get().stringValue(FeatureFlags.Key.REALMID_GUID_SYNC_FLAG,"DISABLE"), "WRITE")) {
            return;
        }

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        String subscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getLicenseNumber();
        String eoc = company.getActivePrimaryEntitlementUnit().getEntitlement().getEntitlementOfferingCode();
        PayrollServices.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        company = Application.refresh(company);
        String realmId = getNewRealmId();
        company.setIAMRealmId(realmId);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, subscriptionNumber, eoc);
        Application.commitUnitOfWork();

        EWSAdapter webservice = new EWSAdapter();
        validateRequest.setRealmID(realmId);
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);
        assertEquals("subscription status", validateResponse.getSubscriptionStatus(), EwsEinSubscriptionStatus.Activated);
        assertNotNull("subscription status", validateResponse.getSubscriptionStatus());
        assertNotNull("legal info", validateResponse.getCompanyLegalInfo());
        assertNotNull("qb account name", validateResponse.getQbAccountName());
        assertNotNull("billing info", validateResponse.getSubscriptionBillingInfo());
        //todo_rhn: add assertion when value is implemented
        //assertNotNull("subscription end date", validateResponse.getSubscriptionEndDate());
        assertEquals("end date, charge date", validateResponse.getSubscriptionEndDate(), validateResponse.getSubscriptionBillingInfo().getSubscriptionNextBillDate());

        RealmManager realmManager = new RealmManager();
        Grant grant = realmManager.findPayrollGrant(realmId);
        assetPayrollGrant(grant);
        assertEquals("Payroll", getGrantEntitlementInfo(grant, RealmManager.EntitlementInfoKey.SOURCE));
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                TransmissionType.ValidateSubscription), "v1_10/test_validateSubscription_grantAdd.xml",
                Arrays.asList("DateTimeStamp", "SubscriptionEndDate", "SubscriptionNextBillDate", "EntitlementCreationDate", "LegalName", "QBAccountName"));
    }

    @Test
    public void validateSubscriptionMultipleThreads() throws Throwable {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        String subscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getLicenseNumber();
        String eoc = company.getActivePrimaryEntitlementUnit().getEntitlement().getEntitlementOfferingCode();
        PayrollServices.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, subscriptionNumber, eoc);
        Application.commitUnitOfWork();

        //do a validate first to set some things on QuickbooksInfo so they aren't being set from null every thread.
        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);

        int NUM_THREADS = 8;

        ValidatorRunnable[] runnableArray = new ValidatorRunnable[8];
        Thread[] threadArray = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            runnableArray[i] = new ValidatorRunnable(validateRequest);
            threadArray[i] = new Thread(runnableArray[i]);
            threadArray[i].start();
        }

        Thread.sleep(5000);
        for (int i = 0; i < NUM_THREADS; i++) {
            runnableArray[i].go = false;
            threadArray[i].join();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            if (runnableArray[i].lastThrowable != null) {
                throw runnableArray[i].lastThrowable;
            }
        }

    }

    private class ValidatorRunnable implements Runnable {

        public EwsValidateSubscription request;
        public boolean go = true;
        public Throwable lastThrowable;

        private ValidatorRunnable(EwsValidateSubscription pRequest) {
            request = pRequest;
        }

        public void run() {
            while (go) {
                try {
                    Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EWSAdapter));
                    EWSAdapter webservice = new EWSAdapter();
                    EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(request);
                    assertResponseSuccess(validateResponse);
                } catch (Throwable t) {
                    lastThrowable = t;
                }
            }
        }
    }

    @Test
    public void validateSubscriptionAssisted() {
        CreateAccountProcessTests createAccountProcessTests = new CreateAccountProcessTests();
        createAccountProcessTests.createAccountCloudAndAssisted();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(createAccountProcessTests.getPSID(), SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(company);
        Entitlement entitlement = company.getActivePrimaryEntitlementUnit().getEntitlement();
        entitlement.setNextChargeDate(PSPDate.getPSPTime());
        entitlement.setCreditCardExpiration("01/01");
        entitlement.setCreditCardNumber("1234");
        entitlement.setCreditCardType("Visa");
        entitlement.setPaymentMethodType(EntitlementPaymentMethodType.CC);
        entitlement = Application.save(entitlement);

        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        Application.commitUnitOfWork();

        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);
        assertEquals("subscription status", validateResponse.getSubscriptionStatus(), EwsEinSubscriptionStatus.Activated);
        assertNotNull("subscription status", validateResponse.getSubscriptionStatus());
        assertNotNull("legal info", validateResponse.getCompanyLegalInfo());
        assertEquals("qb account name", "BOFI", validateResponse.getQbAccountName());

        assertNotNull("billing info", validateResponse.getSubscriptionBillingInfo());
        //assertEquals("01/01", validateResponse.getSubscriptionBillingInfo().getCreditCardExp());
        //assertEquals("1234", validateResponse.getSubscriptionBillingInfo().getCreditCardNumber());
        //assertEquals("Visa", validateResponse.getSubscriptionBillingInfo().getCreditCardType());
        //assertEquals(EwsPaymentMethod.CC, validateResponse.getSubscriptionBillingInfo().getPaymentMethod());
        assertNull(validateResponse.getSubscriptionBillingInfo().getSubscriptionNextBillDate());

        //todo_rhn: add assertion when value is implemented
        //assertNotNull("subscription end date", validateResponse.getSubscriptionEndDate());
        assertEquals("end date, charge date", validateResponse.getSubscriptionEndDate(), validateResponse.getSubscriptionBillingInfo().getSubscriptionNextBillDate());

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        assertNotNull(eu.getLastValidationDate());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void validateSubscriptionPsidMismatch() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        String subscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getLicenseNumber();
        String eoc = company.getActivePrimaryEntitlementUnit().getEntitlement().getEntitlementOfferingCode();
        PayrollServices.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, subscriptionNumber, eoc);
        validateRequest.setPsid("000000000");
        Application.commitUnitOfWork();

        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);
        assertEquals("subscription status", validateResponse.getSubscriptionStatus(), EwsEinSubscriptionStatus.Activated);
        assertNotNull("subscription status", validateResponse.getSubscriptionStatus());
        assertNotNull("legal info", validateResponse.getCompanyLegalInfo());
        assertNotNull("qb account name", validateResponse.getQbAccountName());
        assertNotNull("billing info", validateResponse.getSubscriptionBillingInfo());
        //todo_rhn: add assertion when value is implemented
        //assertNotNull("subscription end date", validateResponse.getSubscriptionEndDate());
        assertEquals("end date, charge date", validateResponse.getSubscriptionEndDate(), validateResponse.getSubscriptionBillingInfo().getSubscriptionNextBillDate());

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        assertNotNull(eu.getLastValidationDate());

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PSIDMismatch);
        assertEquals(1, companyEvents.size());
        CompanyEvent companyEvent = companyEvents.get(0);
        assertEquals(CompanyEventStatus.Inactive, companyEvent.getStatusCd());

        assertEquals(2, companyEvent.getCompanyEventDetailCollection().size());
        EwsMessage ewsMessage = EwsMessages.psidMismatch(validateRequest.getEin(), validateRequest.getSubscriptionNumber(), validateRequest.getPsid(), validateResponse.getPsid());
        assertEquals(String.valueOf(ewsMessage.getCode()), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ErrorCode));
        assertEquals(ewsMessage.getMessage(), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ErrorMessage));

        Application.rollbackUnitOfWork();
    }

    @Test
    public void validateSubscriptionCompanyCancelled() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        String subscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getLicenseNumber();
        String eoc = company.getActivePrimaryEntitlementUnit().getEntitlement().getEntitlementOfferingCode();
        PayrollServices.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);

        company.getQuickbooksInfo().setApplicationVersion("18.00.R.9");
        company.getQuickbooksInfo().setLicenseNumber("6487-4844-4441-477");
        Application.save(company.getQuickbooksInfo());

        PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Terminated);
        PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceSubStatusCode.Fraud);
        PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.Cloud, ServiceSubStatusCode.Cancelled);

        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, subscriptionNumber, eoc);
        validateRequest.getQuickBooks().setAppVersion("18.00.R.10");
        validateRequest.getQuickBooks().setLicenseNumber("6487-4844-4441-478");

        Application.commitUnitOfWork();

        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);
        assertEquals("subscription status", validateResponse.getSubscriptionStatus(), EwsEinSubscriptionStatus.Activated);
        assertNotNull("subscription status", validateResponse.getSubscriptionStatus());
        assertNotNull("legal info", validateResponse.getCompanyLegalInfo());
        assertNotNull("qb account name", validateResponse.getQbAccountName());
        assertNotNull("billing info", validateResponse.getSubscriptionBillingInfo());
        //todo_rhn: add assertion when value is implemented
        //assertNotNull("subscription end date", validateResponse.getSubscriptionEndDate());
        assertEquals("end date, charge date", validateResponse.getSubscriptionEndDate(), validateResponse.getSubscriptionBillingInfo().getSubscriptionNextBillDate());

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        assertNotNull(eu.getLastValidationDate());

        assertEquals("18.00.R.10", company.getQuickbooksInfo().getApplicationVersion());
        assertEquals("6487-4844-4441-478", company.getQuickbooksInfo().getLicenseNumber());

        Application.rollbackUnitOfWork();
    }

    @Test
    public void validateSubscriptionTwoEUSameEin() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        String subscriptionNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        EntitlementUnit eu = DataLoadServices.addDIYEntitlementUnit(company, subscriptionNumber, eoc, EditionType.Enhanced, NumberOfEmployeesType.UNLIMITED); 

        Application.beginUnitOfWork();
        eu = Application.findById(EntitlementUnit.class, eu.getId());
        eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        Application.save(eu);
        Application.commitUnitOfWork();

        eoc = "1234567889";
        DataLoadServices.addAssistedEntitlementUnit(company, subscriptionNumber, "1234567889", true);

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, subscriptionNumber, eoc);
        Application.commitUnitOfWork();

        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);
        
        assertEquals("subscription status", validateResponse.getSubscriptionStatus(), EwsEinSubscriptionStatus.Activated);
        assertNotNull("subscription status", validateResponse.getSubscriptionStatus());
        assertNotNull("legal info", validateResponse.getCompanyLegalInfo());
        assertNotNull("qb account name", validateResponse.getQbAccountName());
        assertNotNull("billing info", validateResponse.getSubscriptionBillingInfo());
        //todo_rhn: add assertion when value is implemented
        //assertNotNull("subscription end date", validateResponse.getSubscriptionEndDate());
        assertEquals("end date, charge date", validateResponse.getSubscriptionEndDate(), validateResponse.getSubscriptionBillingInfo().getSubscriptionNextBillDate());

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        eu = company.getActivePrimaryEntitlementUnit();
        assertNotNull(eu.getLastValidationDate());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void validateSubscriptionDummyEntitlement() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        Entitlement entitlement = company.getActivePrimaryEntitlementUnit().getEntitlement();
        String licenseNumber = entitlement.getLicenseNumber();
        String eoc = entitlement.getEntitlementOfferingCode();

        entitlement.setEntitlementCode(EntitlementCode.findEntitlementCode("1099581", null, null));
        Application.save(entitlement);

        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc, null, null, DataLoadServices.AssetItemNumber.DIY_YEARLY, null);

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, licenseNumber, eoc);
        Application.commitUnitOfWork();

        ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEditionType(EditionType.Enhanced);
        entitlementInfoDTO.setNumberOfEmployeesType(NumberOfEmployeesType.UNLIMITED);
        ERSMockGateway.setEntitlementDTO(entitlementInfoDTO);

        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);

        assertEquals("subscription status", validateResponse.getSubscriptionStatus(), EwsEinSubscriptionStatus.Activated);
        assertNotNull("subscription status", validateResponse.getSubscriptionStatus());
        assertNotNull("legal info", validateResponse.getCompanyLegalInfo());
        assertNotNull("qb account name", validateResponse.getQbAccountName());
        assertNotNull("billing info", validateResponse.getSubscriptionBillingInfo());
        //todo_rhn: add assertion when value is implemented
        //assertNotNull("subscription end date", validateResponse.getSubscriptionEndDate());
        assertEquals("end date, charge date", validateResponse.getSubscriptionEndDate(), validateResponse.getSubscriptionBillingInfo().getSubscriptionNextBillDate());
        assertEquals("15", validateResponse.getSubType());

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        assertNotNull(eu.getLastValidationDate());
        assertFalse(eu.getEntitlement().hasDummyEntitlementCode());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void validateSubscriptionExpiredNCD() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 25, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        Entitlement entitlement = company.getActivePrimaryEntitlementUnit().getEntitlement();
        String licenseNumber = entitlement.getLicenseNumber();
        String eoc = entitlement.getEntitlementOfferingCode();

        entitlement.setNextChargeDate(PSPDate.getPSPTime());
        Application.save(entitlement);

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, licenseNumber, eoc);
        Application.commitUnitOfWork();

        AMOWSGatewayFactory.setInstanceClass(AMOWSMockGateway.class);
        GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = new GetCustomerAssetResponseTypeDTO();

        getCustomerAssetResponseTypeDTO.setContactName("Updated Name");
        getCustomerAssetResponseTypeDTO.setPaymentMethodType(EntitlementPaymentMethodType.CC);
        getCustomerAssetResponseTypeDTO.setSubscriptionEndDate(PSPDate.getPSPTime());
        getCustomerAssetResponseTypeDTO.setNextChargeDate(SpcfCalendar.createInstance(2013, 1, 25, SpcfTimeZone.getLocalTimeZone()));
        getCustomerAssetResponseTypeDTO.setBillingDayOfMonth(28);
        getCustomerAssetResponseTypeDTO.setBillingProfileId("123456789");
        getCustomerAssetResponseTypeDTO.setBillingZipCode("89511");
        getCustomerAssetResponseTypeDTO.setContactEmail("updated@email.com");
        getCustomerAssetResponseTypeDTO.setCreditCardExpiration("01/12");
        getCustomerAssetResponseTypeDTO.setCreditCardNumber("1234");
        getCustomerAssetResponseTypeDTO.setCreditCardType("Visa");
        getCustomerAssetResponseTypeDTO.setEntitlementOfferingCode(eoc);
        getCustomerAssetResponseTypeDTO.setCustomerId(entitlement.getCustomerId());
        getCustomerAssetResponseTypeDTO.setEntitlementState(EntitlementStateCode.Enabled);
        getCustomerAssetResponseTypeDTO.setLicenseNumber(licenseNumber);

        AMOWSMockGateway.setGetCustomerAssetResponseTypeDTO(getCustomerAssetResponseTypeDTO);

        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);

        assertEquals("subscription status", validateResponse.getSubscriptionStatus(), EwsEinSubscriptionStatus.Activated);
        assertNotNull(validateResponse.getPsid());
        assertNotNull("qb account name", validateResponse.getQbAccountName());

        //if the subscription end date is null which in this case it is the next charge date is used.
        assertEquals("end date", getCustomerAssetResponseTypeDTO.getNextChargeDate(), CalendarUtils.convertToSpcfCalendar(validateResponse.getSubscriptionEndDate()));
        assertNotNull(validateResponse.getEntitlementCreationDate());
        assertNotNull(validateResponse.getDateTimeStamp());
        assertEquals("8", validateResponse.getSubType());

        assertNotNull("legal info", validateResponse.getCompanyLegalInfo());
        assertNotNull("TEST_COMPANY_1", validateResponse.getCompanyLegalInfo().getLegalName());
        assertEquals("COLEGAL_AddressLine1", validateResponse.getCompanyLegalInfo().getAddressLine1());
        assertEquals("COLEGAL_AddressLine2", validateResponse.getCompanyLegalInfo().getAddressLine2());
        assertEquals("Ridgewood", validateResponse.getCompanyLegalInfo().getCity());
        assertEquals("NJ", validateResponse.getCompanyLegalInfo().getState());
        assertEquals("07450-4444", validateResponse.getCompanyLegalInfo().getZip());

        assertNotNull("billing info", validateResponse.getSubscriptionBillingInfo());
        //assertEquals(getCustomerAssetResponseTypeDTO.getCreditCardExpiration(), validateResponse.getSubscriptionBillingInfo().getCreditCardExp());
        //assertEquals(getCustomerAssetResponseTypeDTO.getCreditCardType(), validateResponse.getSubscriptionBillingInfo().getCreditCardType());
        //assertEquals(getCustomerAssetResponseTypeDTO.getCreditCardNumber(), validateResponse.getSubscriptionBillingInfo().getCreditCardNumber());
        assertEquals("charge date", getCustomerAssetResponseTypeDTO.getNextChargeDate(), CalendarUtils.convertToSpcfCalendar(validateResponse.getSubscriptionBillingInfo().getSubscriptionNextBillDate()));

        assertNotNull(validateResponse.getEwsResponseStatus());
        assertEquals(0, validateResponse.getEwsResponseStatus().getCode());
        assertEquals("Success", validateResponse.getEwsResponseStatus().getMessage());
    }

    @Test
    public void subscriptionDoesNotExist() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";
        DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, eoc, EditionType.Enhanced, NumberOfEmployeesType.UNLIMITED);

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, licenseNumber, eoc);
        // set EIN to an EIN not on the subscription
        validateRequest.setSubscriptionNumber("XXX");
        Application.commitUnitOfWork();

        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseFailure(validateResponse, 30142);
        assertNull("subscription status", validateResponse.getSubscriptionStatus());
        assertNull("legal info", validateResponse.getCompanyLegalInfo());
        assertNull("qb account name", validateResponse.getQbAccountName());
        assertNull("billing info", validateResponse.getSubscriptionBillingInfo());
        assertNull("subscription end date", validateResponse.getSubscriptionEndDate());
    }

    @Test
    public void einNotSubscribed() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091336", true, ServiceCode.DirectDeposit);

        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";
        DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, eoc, EditionType.Enhanced, NumberOfEmployeesType.UNLIMITED);

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, licenseNumber, eoc);
        // set EIN to an EIN not on the subscription
        validateRequest.setEin("113456789");
        Application.commitUnitOfWork();

        EWSAdapter webservice = new EWSAdapter();
        EwsValidateSubscriptionResponse validateResponse = webservice.Validate_Subscription(validateRequest);
        assertResponseSuccess(validateResponse);
        assertEquals("subscription status", validateResponse.getSubscriptionStatus(), EwsEinSubscriptionStatus.EinNotSubscribed);
        assertNull("legal info", validateResponse.getCompanyLegalInfo());
        assertNull("qb account name", validateResponse.getQbAccountName());
        assertNull("billing info", validateResponse.getSubscriptionBillingInfo());
        assertNull("subscription end date", validateResponse.getSubscriptionEndDate());
    }

    @Test
    public void badSubscriptionNumber() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);

        EwsValidateSubscription validateRequest = createRequest(company);
        validateRequest.setSubscriptionNumber("234AS");

        EWSAdapter webservice = new EWSAdapter();
        EwsResponse response = webservice.Validate_Subscription(validateRequest);
        assertResponseFailure(response, 30005);
    }

    @Test
    public void badPSID() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        String subscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getLicenseNumber();
        String eoc = company.getActivePrimaryEntitlementUnit().getEntitlement().getEntitlementOfferingCode();
        PayrollServices.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        EwsValidateSubscription validateRequest = TestDataFactory.createEwsValidateAccount(company, subscriptionNumber, eoc);
        Application.commitUnitOfWork();

        validateRequest.setPsid("FAIL");

        EWSAdapter webservice = new EWSAdapter();
        EwsResponse response = webservice.Validate_Subscription(validateRequest);
        assertResponseFailure(response, 30005);
    }

    private EwsValidateSubscription createRequest(Company company) {
        String qbLicenseNumber = company.getQuickbooksInfo().getLicenseNumber();
        String subscriptionNumber = null;
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
            if (entitlementUnit.getEntitlement().getLicenseNumber().equalsIgnoreCase(qbLicenseNumber)) {
                subscriptionNumber = entitlementUnit.getEntitlement().getSubscriptionNumber();
            }
        }

        EwsValidateSubscription validateRequest = new EwsValidateSubscription();
        validateRequest.setEin(company.getFedTaxId());
        validateRequest.setPsid(company.getSourceCompanyId());
        validateRequest.setSubscriptionNumber(subscriptionNumber);

        EwsQuickBooks ewsQuickBooks = new EwsQuickBooks();
        ewsQuickBooks.setAppVersion(company.getQuickbooksInfo().getApplicationVersion());
        ewsQuickBooks.setLicenseNumber(company.getQuickbooksInfo().getLicenseNumber());

        validateRequest.setDateTimeStamp(CalendarUtils.convertToCalendar(PSPDate.getPSPTime()));
        validateRequest.setIpAddress("127.0.0.1");

        return validateRequest;
    }

    public void assertResponseFailure(EwsResponse response, int expectedErrorCode) {
        assertNotNull("response status", response.getEwsResponseStatus());
        assertEquals("error code", expectedErrorCode, response.getEwsResponseStatus().getCode());
    }

    public void assertResponseSuccess(EwsResponse response) {
        Assert.assertNotNull("response status", response.getEwsResponseStatus());
        assertEquals("error code", 0, response.getEwsResponseStatus().getCode());
    }

    private void assetPayrollGrant(Grant grant) {
        assertNotNull("Grant not found for the Realm", grant);
        assertEquals(GrantType.OFFERING_APP_GRANT.name(), grant.getGrantType());
        assertEquals(IUSGrantClient.EWS_GRANT_OFFERING_ID, grant.getOfferingId());
        assertEquals("ACTIVE", grant.getStatus());
    }

    private String getGrantEntitlementInfo(Grant grant, RealmManager.EntitlementInfoKey entitlementInfoKey) {
        List<NameValuePair> nameValuePairList = grant.getEntitlementInfo();
        for(NameValuePair nameValuePair: nameValuePairList) {
            if(StringUtils.equalsIgnoreCase(entitlementInfoKey.name(), nameValuePair.getName())) {
                return nameValuePair.getValue();
            }
        }
        return null;
    }

    private String getNewRealmId() {
        return iusDataGenerator.createRealmUsingSystemTicket("1", "TRON", "qbdttrontest+231020001@gmail.com");
    }

}
