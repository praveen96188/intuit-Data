package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.QuickBooksWSDTO;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.As400Factory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.CreateAccountProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
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
public class CreateAccountProcessTests {

    private EwsCreateAccount mRequest;
    private EwsCreateAccountResponse mResponse;

    private String psid = null;
    private Boolean assistedTest;

    public String getPSID() {
        return psid;
    }

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
    public void beforeEachTest() {
        assistedTest = false;
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.beforeEachTest();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EWSAdapter));
        PayrollServicesTest.truncateTables();
    }

    @After
    public void afterEachTest() {

        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void createAccountCloudOnly() {
        mRequest = TestDataFactory.createEwsCreateAccount();

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());
        EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
        assertEquals("Acme Software", ewsCompanyResponse.getDba());
        assertNull(ewsCompanyResponse.getClientPacketDeliveryPreference());
        assertNull(ewsCompanyResponse.getW2DeliveryPreference());
        assertNotNull(ewsCompanyResponse.getEin());

        assertNotNull(ewsCompanyResponse.getLegalInfo());
        EwsLegalInfo ewsLegalInfo = ewsCompanyResponse.getLegalInfo();
        assertEquals("Acme Software", ewsLegalInfo.getLegalName());
        assertEquals("123 Main St", ewsLegalInfo.getAddressLine1());
        assertNull(ewsLegalInfo.getAddressLine2());
        assertEquals("Reno", ewsLegalInfo.getCity());
        assertEquals("NV", ewsLegalInfo.getState());
        assertEquals("89511", ewsLegalInfo.getZip());

        assertNotNull(ewsCompanyResponse.getMailingAddress());
        EwsAddress ewsAddress = ewsCompanyResponse.getMailingAddress();
        assertEquals("123 Main St", ewsAddress.getAddressLine1());
        assertNull(ewsAddress.getAddressLine2());
        assertEquals("Reno", ewsAddress.getCity());
        assertEquals("NV", ewsAddress.getState());
        assertEquals("89511", ewsAddress.getZip());

        assertNotNull(ewsCompanyResponse.getPayrollAdmin());
        EwsContact ewsContact = ewsCompanyResponse.getPayrollAdmin();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First1", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last1", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test1@intuit.com", ewsContact.geteMail());
        assertEquals("999-999-9999", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNotNull(ewsCompanyResponse.getPrimaryPrincipal());
        ewsContact = ewsCompanyResponse.getPrimaryPrincipal();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First2", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last2", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test2@intuit.com", ewsContact.geteMail());
        assertEquals("888-888-8888", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNull(ewsCompanyResponse.getSecondaryPrincipal());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
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

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Entitlement entitlement = company.getActivePrimaryEntitlementUnit().getEntitlement();
        assertEquals(OrderSourceCode.EStore, entitlement.getOrderSourceCd());

        com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission sourceSystemTransmission = ApplicationSecondary.find(com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission.class).getFirst();
        assertEquals("17.00.R.9", sourceSystemTransmission.getApplicationVersion());
        assertEquals("20716", sourceSystemTransmission.getTaxTableId());
        assertEquals("professional", sourceSystemTransmission.getApplicationId());

        Application.rollbackUnitOfWork();
    }

    @Test
    public void createAccountViewMyPaycheck() {
        mRequest = TestDataFactory.createEwsCreateAccount();
        mRequest.getEwsServices().setViewMyPaycheck(new EwsBaseService());

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());
        EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
        assertEquals("Acme Software", ewsCompanyResponse.getDba());
        assertNull(ewsCompanyResponse.getClientPacketDeliveryPreference());
        assertNull(ewsCompanyResponse.getW2DeliveryPreference());
        assertNotNull(ewsCompanyResponse.getEin());

        assertNotNull(ewsCompanyResponse.getLegalInfo());
        EwsLegalInfo ewsLegalInfo = ewsCompanyResponse.getLegalInfo();
        assertEquals("Acme Software", ewsLegalInfo.getLegalName());
        assertEquals("123 Main St", ewsLegalInfo.getAddressLine1());
        assertNull(ewsLegalInfo.getAddressLine2());
        assertEquals("Reno", ewsLegalInfo.getCity());
        assertEquals("NV", ewsLegalInfo.getState());
        assertEquals("89511", ewsLegalInfo.getZip());

        assertNotNull(ewsCompanyResponse.getMailingAddress());
        EwsAddress ewsAddress = ewsCompanyResponse.getMailingAddress();
        assertEquals("123 Main St", ewsAddress.getAddressLine1());
        assertNull(ewsAddress.getAddressLine2());
        assertEquals("Reno", ewsAddress.getCity());
        assertEquals("NV", ewsAddress.getState());
        assertEquals("89511", ewsAddress.getZip());

        assertNotNull(ewsCompanyResponse.getPayrollAdmin());
        EwsContact ewsContact = ewsCompanyResponse.getPayrollAdmin();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First1", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last1", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test1@intuit.com", ewsContact.geteMail());
        assertEquals("999-999-9999", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNotNull(ewsCompanyResponse.getPrimaryPrincipal());
        ewsContact = ewsCompanyResponse.getPrimaryPrincipal();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First2", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last2", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test2@intuit.com", ewsContact.geteMail());
        assertEquals("888-888-8888", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNull(ewsCompanyResponse.getSecondaryPrincipal());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
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

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudV2Response());
        assertEquals("Active", ewsServicesResponse.getCloudV2Response().getStatus().toString());

        assertNotNull(ewsServicesResponse.getViewMyPaycheckResponse());
        assertEquals("Active", ewsServicesResponse.getViewMyPaycheckResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Entitlement entitlement = company.getActivePrimaryEntitlementUnit().getEntitlement();
        assertEquals(OrderSourceCode.EStore, entitlement.getOrderSourceCd());

        com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission sourceSystemTransmission = ApplicationSecondary.find(com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission.class).getFirst();
        assertEquals("17.00.R.9", sourceSystemTransmission.getApplicationVersion());
        assertEquals("20716", sourceSystemTransmission.getTaxTableId());
        assertEquals("professional", sourceSystemTransmission.getApplicationId());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void createAccountCloudAndSymphony() {
        mRequest = TestDataFactory.createEwsCreateAccountSymphony();

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());
        EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
        assertEquals("Acme Software", ewsCompanyResponse.getDba());
        assertNull(ewsCompanyResponse.getClientPacketDeliveryPreference());
        assertNull(ewsCompanyResponse.getW2DeliveryPreference());
        assertNotNull(ewsCompanyResponse.getEin());

        assertNotNull(ewsCompanyResponse.getLegalInfo());
        EwsLegalInfo ewsLegalInfo = ewsCompanyResponse.getLegalInfo();
        assertEquals("Acme Software", ewsLegalInfo.getLegalName());
        assertEquals("123 Main St", ewsLegalInfo.getAddressLine1());
        assertNull(ewsLegalInfo.getAddressLine2());
        assertEquals("Reno", ewsLegalInfo.getCity());
        assertEquals("NV", ewsLegalInfo.getState());
        assertEquals("89511", ewsLegalInfo.getZip());

        assertNotNull(ewsCompanyResponse.getMailingAddress());
        EwsAddress ewsAddress = ewsCompanyResponse.getMailingAddress();
        assertEquals("123 Main St", ewsAddress.getAddressLine1());
        assertNull(ewsAddress.getAddressLine2());
        assertEquals("Reno", ewsAddress.getCity());
        assertEquals("NV", ewsAddress.getState());
        assertEquals("89511", ewsAddress.getZip());

        assertNotNull(ewsCompanyResponse.getPayrollAdmin());
        EwsContact ewsContact = ewsCompanyResponse.getPayrollAdmin();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First1", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last1", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test1@intuit.com", ewsContact.geteMail());
        assertEquals("999-999-9999", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNotNull(ewsCompanyResponse.getPrimaryPrincipal());
        ewsContact = ewsCompanyResponse.getPrimaryPrincipal();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First2", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last2", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test2@intuit.com", ewsContact.geteMail());
        assertEquals("888-888-8888", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNull(ewsCompanyResponse.getSecondaryPrincipal());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("18", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        Application.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Entitlement entitlement = company.getActivePrimaryEntitlementUnit().getEntitlement();
        assertEquals(OrderSourceCode.EStore, entitlement.getOrderSourceCd());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void createAccountDiskDelivery() {
        mRequest = TestDataFactory.createEwsCreateAccount();

        mRequest.getEwsEntitlements().get(0).setEdition(EwsEditionType.Standard);
        mRequest.getEwsEntitlements().get(0).setTier(EwsTierType.Unlimited);
        mRequest.getEwsEntitlements().get(0).setAssetItemNumber("1099574");

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());
        EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
        assertEquals("Acme Software", ewsCompanyResponse.getDba());
        assertNull(ewsCompanyResponse.getClientPacketDeliveryPreference());
        assertNull(ewsCompanyResponse.getW2DeliveryPreference());
        assertNotNull(ewsCompanyResponse.getEin());

        assertNotNull(ewsCompanyResponse.getLegalInfo());
        EwsLegalInfo ewsLegalInfo = ewsCompanyResponse.getLegalInfo();
        assertEquals("Acme Software", ewsLegalInfo.getLegalName());
        assertEquals("123 Main St", ewsLegalInfo.getAddressLine1());
        assertNull(ewsLegalInfo.getAddressLine2());
        assertEquals("Reno", ewsLegalInfo.getCity());
        assertEquals("NV", ewsLegalInfo.getState());
        assertEquals("89511", ewsLegalInfo.getZip());

        assertNotNull(ewsCompanyResponse.getMailingAddress());
        EwsAddress ewsAddress = ewsCompanyResponse.getMailingAddress();
        assertEquals("123 Main St", ewsAddress.getAddressLine1());
        assertNull(ewsAddress.getAddressLine2());
        assertEquals("Reno", ewsAddress.getCity());
        assertEquals("NV", ewsAddress.getState());
        assertEquals("89511", ewsAddress.getZip());

        assertNotNull(ewsCompanyResponse.getPayrollAdmin());
        EwsContact ewsContact = ewsCompanyResponse.getPayrollAdmin();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First1", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last1", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test1@intuit.com", ewsContact.geteMail());
        assertEquals("999-999-9999", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNotNull(ewsCompanyResponse.getPrimaryPrincipal());
        ewsContact = ewsCompanyResponse.getPrimaryPrincipal();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First2", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last2", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test2@intuit.com", ewsContact.geteMail());
        assertEquals("888-888-8888", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNull(ewsCompanyResponse.getSecondaryPrincipal());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNotNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("1", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountDiskDeliveryAddEin() {
        createAccountDiskDelivery();

        mRequest.getEwsCompany().setEin("876543211");

        mRequest.getEwsEntitlements().get(0).setEdition(EwsEditionType.Standard);
        mRequest.getEwsEntitlements().get(0).setTier(EwsTierType.Unlimited);
        mRequest.getEwsEntitlements().get(0).setAssetItemNumber("1099574");
        mRequest.getEwsEntitlements().get(0).setEwsBillingDetails(null);
        mRequest.getEwsEntitlements().get(0).setAddEin(true);

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());
        EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
        assertEquals("Acme Software", ewsCompanyResponse.getDba());
        assertNull(ewsCompanyResponse.getClientPacketDeliveryPreference());
        assertNull(ewsCompanyResponse.getW2DeliveryPreference());
        assertEquals("876543211", ewsCompanyResponse.getEin());

        assertNotNull(ewsCompanyResponse.getLegalInfo());
        EwsLegalInfo ewsLegalInfo = ewsCompanyResponse.getLegalInfo();
        assertEquals("Acme Software", ewsLegalInfo.getLegalName());
        assertEquals("123 Main St", ewsLegalInfo.getAddressLine1());
        assertNull(ewsLegalInfo.getAddressLine2());
        assertEquals("Reno", ewsLegalInfo.getCity());
        assertEquals("NV", ewsLegalInfo.getState());
        assertEquals("89511", ewsLegalInfo.getZip());

        assertNotNull(ewsCompanyResponse.getMailingAddress());
        EwsAddress ewsAddress = ewsCompanyResponse.getMailingAddress();
        assertEquals("123 Main St", ewsAddress.getAddressLine1());
        assertNull(ewsAddress.getAddressLine2());
        assertEquals("Reno", ewsAddress.getCity());
        assertEquals("NV", ewsAddress.getState());
        assertEquals("89511", ewsAddress.getZip());

        assertNotNull(ewsCompanyResponse.getPayrollAdmin());
        EwsContact ewsContact = ewsCompanyResponse.getPayrollAdmin();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First1", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last1", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test1@intuit.com", ewsContact.geteMail());
        assertEquals("999-999-9999", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNotNull(ewsCompanyResponse.getPrimaryPrincipal());
        ewsContact = ewsCompanyResponse.getPrimaryPrincipal();
        assertNull(ewsContact.getTitle());
        assertNull(ewsContact.getTitleSuffix());
        assertEquals("First2", ewsContact.getFirstName());
        assertNull(ewsContact.getMiddleName());
        assertEquals("Last2", ewsContact.getLastName());
        assertNull(ewsContact.getJobTitle());
        assertEquals("test2@intuit.com", ewsContact.geteMail());
        assertEquals("888-888-8888", ewsContact.getWorkPhone());
        assertNull(ewsContact.getHomePhone());
        assertNull(ewsContact.getAddress());

        assertNull(ewsCompanyResponse.getSecondaryPrincipal());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNotNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("1", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountCloudAndDirectDeposit() {
        mRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);
        if (mResponse.getPsid() == null) {
            //additional logging -- why is this failing on TC?
            fail("no PSID " + mResponse.getEwsResponseStatus().getCode() + " " + mResponse.getEwsResponseStatus().getMessage());
        }
        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
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

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        EwsBankAccountResponse ewsBankAccountResponse = ewsServicesResponse.getDirectDepositResponse().getEwsBankAccountResponse();
        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertEquals(false, ewsBankAccountResponse.getPendingPayrollExists());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertEquals(true, ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        // Validate IAM info
        assertEquals("1234567890", company.getIAMRealmId());

        Contact contact = company.getContactByRoleCode(ContactRole.PayrollAdmin);
        assertEquals("12345", contact.getIAMAuthenticationId());

        contact = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
        assertEquals("67890", contact.getIAMAuthenticationId());

        // Validate Offer
        DomainEntitySet<CompanyOffer> companyOffers = company.getCompanyOffers();
        assertFalse(companyOffers.isEmpty());

        Offer offer = companyOffers.get(0).getOffer();
        assertEquals("Waive all major fees", offer.getOfferCd());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void createAccountCloudAndSymphonyDirectDeposit() {
        mRequest = TestDataFactory.createEwsCreateAccountSymphonyDirectDeposit();

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);
        if (mResponse.getPsid() == null) {
            //additional logging -- why is this failing on TC?
            fail("no PSID " + mResponse.getEwsResponseStatus().getCode() + " " + mResponse.getEwsResponseStatus().getMessage());
        }
        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
        assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
        assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
        assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

        assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
        EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
        assertEquals("test3@intuit.com", ewsEntitlementResponse.getBuyerEmailAddress());
        assertEquals("0", ewsEntitlementResponse.getBillingAccountId());
        assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
        assertEquals("18", ewsEntitlementResponse.getSubType());
        assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        EwsBankAccountResponse ewsBankAccountResponse = ewsServicesResponse.getDirectDepositResponse().getEwsBankAccountResponse();
        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertEquals(false, ewsBankAccountResponse.getPendingPayrollExists());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertEquals(true, ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        // Validate IAM info
        assertEquals("1234567890", company.getIAMRealmId());

        Contact contact = company.getContactByRoleCode(ContactRole.PayrollAdmin);
        assertEquals("12345", contact.getIAMAuthenticationId());

        contact = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
        assertEquals("67890", contact.getIAMAuthenticationId());

        // Validate Offer
        DomainEntitySet<CompanyOffer> companyOffers = company.getCompanyOffers();
        assertFalse(companyOffers.isEmpty());

        Offer offer = companyOffers.get(0).getOffer();
        assertEquals("Waive all major fees", offer.getOfferCd());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void createAccountCloudAndAssisted() {
        assistedTest = true;
        mRequest = TestDataFactory.createEwsCreateAccountAssisted();

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^9\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());
        EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
        assertEquals(EwsDeliveryType.mail, ewsCompanyResponse.getClientPacketDeliveryPreference());
        assertEquals(EwsDeliveryType.mail, ewsCompanyResponse.getW2DeliveryPreference());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
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

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals(EwsServiceStatus.PendingPinCreation, ewsServicesResponse.getCloudResponse().getStatus());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse assistedServiceResponse = ewsServicesResponse.getAssistedResponse();
        assertEquals(EwsServiceStatus.PendingActivation, assistedServiceResponse.getStatus());
        assertEquals("0", assistedServiceResponse.getMostCurrentTaxYear());
        assertNull(assistedServiceResponse.getPriceCode());
        assertNull(assistedServiceResponse.getSourceCode());
        assertEquals("0.00", assistedServiceResponse.getMonthlyFee());

        EwsBankAccountResponse ewsBankAccountResponse = ewsServicesResponse.getAssistedResponse().getEwsBankAccountResponse();
        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertEquals(false, ewsBankAccountResponse.getPendingPayrollExists());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertEquals(true, ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // Validate Offer
        DomainEntitySet<CompanyOffer> companyOffers = company.getCompanyOffers();
        assertFalse(companyOffers.isEmpty());

        Offer offer = companyOffers.get(0).getOffer();
        assertEquals("1099426", offer.getPromotionId());

        //Assisted accounts should not send this email.
        assertTrue(CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending,
                EventEmailTemplateTypeCode.DDSignupConfirmation).isEmpty());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
        public void createAccountCloudAndAssistedForceRandomDollarFalse() {
            assistedTest = true;
            mRequest = TestDataFactory.createEwsCreateAccountAssisted();

            mRequest.setForceRandomDollar(false);

            CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^9\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            //assertTrue(matcher.matches());

            assertNotNull(mResponse.getCompanyResponse());
            EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
            assertEquals(EwsDeliveryType.mail, ewsCompanyResponse.getClientPacketDeliveryPreference());
            assertEquals(EwsDeliveryType.mail, ewsCompanyResponse.getW2DeliveryPreference());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
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

            assertNotNull(mResponse.getEwsServicesResponse());
            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertEquals(EwsServiceStatus.PendingPinCreation, ewsServicesResponse.getCloudResponse().getStatus());

            assertNull(ewsServicesResponse.getDirectDepositResponse());
            assertNotNull(ewsServicesResponse.getAssistedResponse());

            EwsAssistedServiceResponse assistedServiceResponse = ewsServicesResponse.getAssistedResponse();
            assertEquals(EwsServiceStatus.PendingActivation, assistedServiceResponse.getStatus());
            assertEquals("0", assistedServiceResponse.getMostCurrentTaxYear());
            assertNull(assistedServiceResponse.getPriceCode());
            assertNull(assistedServiceResponse.getSourceCode());
            assertEquals("0.00", assistedServiceResponse.getMonthlyFee());

        EwsBankAccountResponse ewsBankAccountResponse = ewsServicesResponse.getAssistedResponse().getEwsBankAccountResponse();
        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertEquals(false, ewsBankAccountResponse.getPendingPayrollExists());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertEquals(true, ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // Validate Offer
        DomainEntitySet<CompanyOffer> companyOffers = company.getCompanyOffers();
        assertFalse(companyOffers.isEmpty());

        Offer offer = companyOffers.get(0).getOffer();
        assertEquals("1099426", offer.getPromotionId());

        //Assisted accounts should not send this email.
        assertTrue(CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending,
                EventEmailTemplateTypeCode.DDSignupConfirmation).isEmpty());

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    @Ignore("401k is no longer created")
    public void createAccountCloudAnd401k() {

        mRequest = TestDataFactory.createEwsCreateAccount();

        Application.beginUnitOfWork();
        ThirdParty401kSignUpQueue thirdParty401kSignUpQueue = new ThirdParty401kSignUpQueue();
        thirdParty401kSignUpQueue.setCustodialId("Test");
        thirdParty401kSignUpQueue.setEffectiveDate(PSPDate.getPSPTime());
        thirdParty401kSignUpQueue.setFedTaxId(mRequest.getEwsCompany().getEin());
        thirdParty401kSignUpQueue.setHasSafeHarbor(false);
        thirdParty401kSignUpQueue.setLegalName("Acme");
        thirdParty401kSignUpQueue.setStatus(ThirdParty401kSignUpQueueStatusCode.Pending);
        Application.save(thirdParty401kSignUpQueue);
        Application.commitUnitOfWork();

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
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

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getThirdParty401kResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getThirdParty401kResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountFieldValidation_EIN() {
        mRequest = TestDataFactory.createEwsCreateAccountAssisted();

        mRequest.getEwsCompany().setEin("87654321");

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(30005, ewsResponseStatus.getCode());
        assertEquals("Field EIN in Company does not contain valid data", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountFieldValidationEin() {
        mRequest = TestDataFactory.createEwsCreateAccountAssisted();

        mRequest.getEwsCompany().setEin("87654321");

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(30005, ewsResponseStatus.getCode());
        assertEquals("Field EIN in Company does not contain valid data", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountFieldValidationPayrollAdminEmail() {
        mRequest = TestDataFactory.createEwsCreateAccount();

        mRequest.getEwsCompany().getPayrollAdmin().seteMail("Bad eMail");

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(30005, ewsResponseStatus.getCode());
        assertEquals("Field eMail in Contact does not contain valid data", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountFieldValidationEdition() {
        mRequest = TestDataFactory.createEwsCreateAccount();

        mRequest.getEwsEntitlements().get(0).setEdition(null);

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(30004, ewsResponseStatus.getCode());
        assertEquals("Field Edition in Entitlement is required and cannot be null or empty", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountFieldValidationTier() {
        mRequest = TestDataFactory.createEwsCreateAccount();

        mRequest.getEwsEntitlements().get(0).setTier(null);

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(30004, ewsResponseStatus.getCode());
        assertEquals("Field Tier in Entitlement is required and cannot be null or empty", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void createAccountEinActiveOnDD() {
        createAccountCloudAndDirectDeposit();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(getPSID(), SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        mRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();
        mRequest.getEwsCompany().setEin(company.getFedTaxId());
        mRequest.getEwsEntitlements().get(0).setLicenseNumber("11144412");
        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        assertEquals(30154, mResponse.getEwsResponseStatus().getCode());
        assertEquals("EIN already has services active other then DIY.", mResponse.getEwsResponseStatus().getMessage());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void createAccountCloudAndDirectDepositMissingRealmId() {
        mRequest = TestDataFactory.createEwsCreateAccountDirectDeposit();

        mRequest.getEwsCompany().setRealmId(null);

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
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

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        assertEquals("PendingBankVerification", ewsServicesResponse.getDirectDepositResponse().getStatus().toString());

        EwsBankAccountResponse ewsBankAccountResponse = ewsServicesResponse.getDirectDepositResponse().getEwsBankAccountResponse();
        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertEquals(false, ewsBankAccountResponse.getPendingPayrollExists());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertEquals(true, ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getAssistedResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        // Validate IAM info
        assertNull(company.getIAMRealmId());

        Contact contact = company.getContactByRoleCode(ContactRole.PayrollAdmin);
        assertEquals("12345", contact.getIAMAuthenticationId());

        contact = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
        assertEquals("67890", contact.getIAMAuthenticationId());

        // Validate Offer
        DomainEntitySet<CompanyOffer> companyOffers = company.getCompanyOffers();
        assertFalse(companyOffers.isEmpty());

        Offer offer = companyOffers.get(0).getOffer();
        assertEquals("Waive all major fees", offer.getOfferCd());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void createAccountApplyAutoOffer() {
        assistedTest = true;
        mRequest = TestDataFactory.createEwsCreateAccountAssisted();
        mRequest.getEwsServices().getAssistedService().setPromotionId(null);

        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE, "1099426");
        PayrollServices.commitUnitOfWork();

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^9\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
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

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse assistedServiceResponse = ewsServicesResponse.getAssistedResponse();
        assertEquals(EwsServiceStatus.PendingActivation, assistedServiceResponse.getStatus());
        assertEquals("0", assistedServiceResponse.getMostCurrentTaxYear());
        assertNull(assistedServiceResponse.getPriceCode());
        assertNull(assistedServiceResponse.getSourceCode());
        assertEquals("0.00", assistedServiceResponse.getMonthlyFee());

        EwsBankAccountResponse ewsBankAccountResponse = ewsServicesResponse.getAssistedResponse().getEwsBankAccountResponse();
        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertEquals(true, ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // Validate Offer
        DomainEntitySet<CompanyOffer> companyOffers = company.getCompanyOffers();
        assertFalse(companyOffers.isEmpty());

        Offer offer = companyOffers.get(0).getOffer();
        assertEquals("1099426", offer.getPromotionId());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void createAccountAutoOfferNull() {
        assistedTest = true;
        mRequest = TestDataFactory.createEwsCreateAccountAssisted();
        mRequest.getEwsServices().getAssistedService().setPromotionId(null);

        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE, null);
        PayrollServices.commitUnitOfWork();

        assertNull(SystemParameter.findStringValue(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE));

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getPsid());
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^9\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getCompanyResponse());

        assertNotNull(mResponse.getEwsEntitlementUnitResponses());
        assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
        EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
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

        assertNotNull(mResponse.getEwsServicesResponse());
        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertEquals("PendingPinCreation", ewsServicesResponse.getCloudResponse().getStatus().toString());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse assistedServiceResponse = ewsServicesResponse.getAssistedResponse();
        assertEquals(EwsServiceStatus.PendingActivation, assistedServiceResponse.getStatus());
        assertEquals("0", assistedServiceResponse.getMostCurrentTaxYear());
        assertNull(assistedServiceResponse.getPriceCode());
        assertNull(assistedServiceResponse.getSourceCode());
        assertEquals("79.00", assistedServiceResponse.getMonthlyFee());

        EwsBankAccountResponse ewsBankAccountResponse = ewsServicesResponse.getAssistedResponse().getEwsBankAccountResponse();
        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertEquals(true, ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // Validate Offer
        DomainEntitySet<CompanyOffer> companyOffers = company.getCompanyOffers();
        assertTrue(companyOffers.isEmpty());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCreateQuickBooksWSDTO() {
        QuickbooksInfo quickbooksInfo = new QuickbooksInfo();

        quickbooksInfo.setApplicationVersion("21.00.R.11/21212#superpro");
        quickbooksInfo.setApplicationId("QBWPRO");
        quickbooksInfo.setTaxTableId("21212");
        QuickBooksWSDTO quickBooksWSDTO = As400Factory.createQuickBooksWSDTO(quickbooksInfo);
        assertEquals("21.00.R.11/21212", quickBooksWSDTO.getAppVer());
        assertEquals("QBWPRO", quickBooksWSDTO.getAppId());

        quickbooksInfo.setApplicationVersion("21.00.R.11/21212#superpro");
        quickbooksInfo.setApplicationId("QBW");
        quickbooksInfo.setTaxTableId("21212");
        quickBooksWSDTO = As400Factory.createQuickBooksWSDTO(quickbooksInfo);
        assertEquals("21.00.R.11/21212", quickBooksWSDTO.getAppVer());
        assertEquals("QBW", quickBooksWSDTO.getAppId());

        quickbooksInfo.setApplicationVersion("21.00.R.11/21212");
        quickbooksInfo.setApplicationId("QBWPRO");
        quickbooksInfo.setTaxTableId("21212");
        quickBooksWSDTO = As400Factory.createQuickBooksWSDTO(quickbooksInfo);
        assertEquals("21.00.R.11/21212", quickBooksWSDTO.getAppVer());
        assertEquals("QBWPRO", quickBooksWSDTO.getAppId());

        quickbooksInfo.setApplicationVersion("21.00.R.11");
        quickbooksInfo.setApplicationId("QBWPRO");
        quickbooksInfo.setTaxTableId("21212");
        quickBooksWSDTO = As400Factory.createQuickBooksWSDTO(quickbooksInfo);
        assertEquals("21.00.R.11/21212", quickBooksWSDTO.getAppVer());
        assertEquals("QBWPRO", quickBooksWSDTO.getAppId());

        quickbooksInfo.setApplicationVersion("21.00.R.11");
        quickbooksInfo.setApplicationId(null);
        quickbooksInfo.setTaxTableId("21212");
        quickBooksWSDTO = As400Factory.createQuickBooksWSDTO(quickbooksInfo);
        assertEquals("21.00.R.11/21212", quickBooksWSDTO.getAppVer());
        assertNull(quickBooksWSDTO.getAppId());

        quickbooksInfo.setApplicationVersion("21.00.R.11");
        quickbooksInfo.setApplicationId(null);
        quickbooksInfo.setTaxTableId(null);
        quickBooksWSDTO = As400Factory.createQuickBooksWSDTO(quickbooksInfo);
        assertEquals("21.00.R.11", quickBooksWSDTO.getAppVer());
        assertNull(quickBooksWSDTO.getAppId());

        quickbooksInfo.setApplicationVersion("21.00.R.11");
        quickbooksInfo.setApplicationId("professional");
        quickbooksInfo.setTaxTableId("21212");
        quickBooksWSDTO = As400Factory.createQuickBooksWSDTO(quickbooksInfo);
        assertEquals("21.00.R.11/21212", quickBooksWSDTO.getAppVer());
        assertNull(quickBooksWSDTO.getAppId());

        quickbooksInfo.setApplicationVersion("21.00.R.11");
        quickbooksInfo.setApplicationId("professional");
        quickbooksInfo.setTaxTableId(null);
        quickBooksWSDTO = As400Factory.createQuickBooksWSDTO(quickbooksInfo);
        assertEquals("21.00.R.11", quickBooksWSDTO.getAppVer());
        assertNull(quickBooksWSDTO.getAppId());
    }
}
