package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsEntitlementStateCode;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsEntitlementUnitStatusCode;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsServiceStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.QueryAccountProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Arrays;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class QueryAccountProcessTests {

    private EwsQueryAccount mRequest;
    private EwsQueryAccountResponse mResponse;

    private String psid = null;
    private Boolean assistedTest;

    public String getPSID() {
        return psid;
    }

    private CreateAccountProcessTests mCreateAccountProcessTests = new CreateAccountProcessTests();
    private AddServiceProcessTests mAddServiceProcessTests = new AddServiceProcessTests();

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
    public void queryAccountCloudOnlyPsid() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
            assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
            assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
            assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

            assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
            EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
            assertFalse(ewsEntitlementResponse.getHasMultipleActiveEINs());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
            PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                    TransmissionType.QueryAccount), "v1_10/test_QueryAccount_CloudOnlyPsid.xml",
                    Arrays.asList("EIN", "DateTimeStamp", "PSID", "ServiceKey", "SubscriptionNumber"));
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountViewMyPaycheckOnlyPsid() {
        try {
            mCreateAccountProcessTests.createAccountViewMyPaycheck();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
            assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
            assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
            assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

            assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
            EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
            assertFalse(ewsEntitlementResponse.getHasMultipleActiveEINs());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());
            assertNotNull(mResponse.getDateTimeStamp());
            PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                    TransmissionType.QueryAccount), "v1_10/test_QueryAccount_ViewMyPayCheck.xml",
                    Arrays.asList("EIN", "DateTimeStamp", "PSID", "ServiceKey", "SubscriptionNumber"));
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudOnlyMultiEIN() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();
            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            company.setFedTaxId("876543211");
            EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
            entitlementUnit.setFedTaxId("876543211");
            Application.save(entitlementUnit);
            Application.save(company);
            Application.commitUnitOfWork();

            mCreateAccountProcessTests.createAccountCloudOnly();

            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
            assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
            assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
            assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

            assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
            EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
            assertTrue(ewsEntitlementResponse.getHasMultipleActiveEINs());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudDiskDelivery() {
        try {
            mCreateAccountProcessTests.createAccountDiskDelivery();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

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

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudAndDDPsid() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
            assertEquals("9130360425658956", ewsCompanyResponse.getRealmId());

            EwsContact ewsContact = ewsCompanyResponse.getPayrollAdmin();
            assertEquals("12345", ewsContact.getAuthenticationId());

            ewsContact = ewsCompanyResponse.getPrimaryPrincipal();
            assertEquals("67890", ewsContact.getAuthenticationId());

            assertNotNull(mResponse.getEwsServicesResponse());
            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();

            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse().getEwsBankAccountResponse());
            assertEquals(EwsServiceStatus.PendingBankVerification, ewsServicesResponse.getDirectDepositResponse().getStatus());

            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudAndDDPsidOnHold() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            company.addOnHoldReason(ServiceSubStatusCode.AchRejectR1R9);
            Application.commitUnitOfWork();

            Application.beginUnitOfWork();
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
            assertEquals("9130360425658956", ewsCompanyResponse.getRealmId());

            EwsContact ewsContact = ewsCompanyResponse.getPayrollAdmin();
            assertEquals("12345", ewsContact.getAuthenticationId());

            ewsContact = ewsCompanyResponse.getPrimaryPrincipal();
            assertEquals("67890", ewsContact.getAuthenticationId());

            assertEquals(EwsServiceStatus.PendingBankVerification, mResponse.getEwsServicesResponse().getDirectDepositResponse().getStatus());
            assertTrue(mResponse.getCompanyResponse().isOnHold());
            assertEquals("AchRejectR1R9", mResponse.getCompanyResponse().getOnHoldReason());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudAndDDRealmId() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            mRequest.setPsid(null);
            mRequest.setSubscriptionNumber(null);
            mRequest.getEwsBaseCompany().setEin(null);
            mRequest.getEwsBaseCompany().setRealmId(company.getIAMRealmId());

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
            assertEquals("9130360425658956", ewsCompanyResponse.getRealmId());

            EwsContact ewsContact = ewsCompanyResponse.getPayrollAdmin();
            assertEquals("12345", ewsContact.getAuthenticationId());

            ewsContact = ewsCompanyResponse.getPrimaryPrincipal();
            assertEquals("67890", ewsContact.getAuthenticationId());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudAndAssistedPsid() {
        assistedTest = true;
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    @Ignore("401k is no longer created")
    public void queryAccountCloudAnd401kPsid() {
        try {
            mCreateAccountProcessTests.createAccountCloudAnd401k();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

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
            assertNull(ewsEntitlementResponse.getBillingZip());
            assertNull(ewsEntitlementResponse.getCancellationReason());
            assertEquals("1234567890", ewsEntitlementResponse.getEntitlementOfferingCode());
            assertEquals("1", ewsEntitlementResponse.getLicenseNumber());
            assertEquals(EwsEntitlementStateCode.Enabled, ewsEntitlementResponse.getState());
            assertNotNull(ewsEntitlementResponse.getSubscriptionNumber());
            assertEquals("16", ewsEntitlementResponse.getSubType());
            assertEquals(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString(), ewsEntitlementResponse.getAssetItemNumber());

            assertNotNull(mResponse.getEwsServicesResponse());
            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertEquals(EwsServiceStatus.PendingPinCreation, ewsServicesResponse.getCloudResponse().getStatus());
            assertNotNull(ewsServicesResponse.getThirdParty401kResponse());
            assertEquals(EwsServiceStatus.PendingPinCreation, ewsServicesResponse.getThirdParty401kResponse().getStatus());

            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getDirectDepositResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudOnlyEin() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountEin(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudAndDDEin() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountEin(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudAndAssistedEin() {
        assistedTest = true;
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountEin(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudDDAndAssistedEin() {
        assistedTest = true;
        try {
            mAddServiceProcessTests.addServiceAssistedToDDActiveCurrent();
            psid = mAddServiceProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountEin(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            EwsServicesResponse services = mResponse.getEwsServicesResponse();

            assertNotNull(services.getAssistedResponse());
            EwsAssistedServiceResponse assistedService = services.getAssistedResponse();
            assertEquals(EwsServiceStatus.PendingActivation, assistedService.getStatus());

            assertNotNull(services.getCloudResponse());
            EwsBaseServiceResponse cloudService = services.getCloudResponse();
            assertEquals(EwsServiceStatus.PendingFirstPayroll, cloudService.getStatus());

            assertNotNull(services.getDirectDepositResponse());
            EwsDirectDepositServiceResponse ddService = services.getDirectDepositResponse();
            assertEquals(EwsServiceStatus.Active, ddService.getStatus());

            assertNull(services.getBillPaymentResponse());
            assertNull(services.getCheckDistributionResponse());
            assertNull(services.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudActiveAssistedCancelledEin() {
        assistedTest = true;
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            //Update contact information in PSP, to validate response will have contact information from PSP.
            PayrollServices.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            //company.setFedTaxId("121212125"); // Ein number
            company.setDbaName("new DbaName"); // Dba Name
            company.setLegalName("Acme Systems Inc"); // Legal Name
            company.getLegalAddress().setAddressLine1("456 Main St"); // Address Line1
            Application.save(company);
            PayrollServices.commitUnitOfWork();

            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);

            //Cancel the psp service
            ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.updateServiceStatus
                    (SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
            assertTrue(servicePR.isSuccess());

            servicePR = PayrollServices.companyManager.updateServiceStatus
                    (SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
            assertTrue(servicePR.isSuccess());

            mRequest = TestDataFactory.createEwsQueryAccountEin(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            EwsServicesResponse services = mResponse.getEwsServicesResponse();

            assertNotNull(services.getAssistedResponse());
            EwsAssistedServiceResponse assistedService = services.getAssistedResponse();
            assertEquals(EwsServiceStatus.Cancelled, assistedService.getStatus());

            assertNotNull(services.getCloudResponse());
            EwsBaseServiceResponse cloudService = services.getCloudResponse();
            assertEquals(EwsServiceStatus.PendingPinCreation, cloudService.getStatus());

            assertNull(services.getBillPaymentResponse());
            assertNull(services.getCheckDistributionResponse());
            assertNull(services.getDirectDepositResponse());
            assertNull(services.getThirdParty401kResponse());

            assertNotNull(mResponse.getDateTimeStamp());

            //Validate - contact information in response is from PSP.
            EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
            assertEquals("Legal Name", "Acme Systems Inc", ewsCompanyResponse.getLegalInfo().getLegalName());
            assertNotNull(ewsCompanyResponse.getEin());
            assertEquals("Address Line1", "456 Main St", ewsCompanyResponse.getLegalInfo().getAddressLine1());
            assertEquals("Dba Name", "new DbaName", ewsCompanyResponse.getDba());

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudAssistedEin() {
        assistedTest = true;
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();


            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            //Cancel the psp service
            mRequest = TestDataFactory.createEwsQueryAccountEin(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            EwsServicesResponse services = mResponse.getEwsServicesResponse();

            assertNotNull(services.getAssistedResponse());
            EwsAssistedServiceResponse assistedService = services.getAssistedResponse();
            assertEquals(EwsServiceStatus.PendingActivation, assistedService.getStatus());

            assertNotNull(services.getCloudResponse());
            EwsBaseServiceResponse cloudService = services.getCloudResponse();
            assertEquals(EwsServiceStatus.PendingPinCreation, cloudService.getStatus());

            assertNull(services.getBillPaymentResponse());
            assertNull(services.getCheckDistributionResponse());
            assertNull(services.getDirectDepositResponse());
            assertNull(services.getThirdParty401kResponse());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountPsidNotFound() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            mRequest.setPsid("000000000");

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30101, ewsResponseStatus.getCode());
            assertEquals("PSID Does Not Exist", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEINNotFound() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            mRequest.setPsid(null);
            mRequest.getEwsBaseCompany().setEin("000000000");
            mRequest.setSubscriptionNumber(eu.getEntitlement().getSubscriptionNumber());
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30102, ewsResponseStatus.getCode());
            assertEquals("EIN Does Not Exist", ewsResponseStatus.getMessage());
            assertNotNull(mResponse.getDateTimeStamp());


            //Checking for null EIN
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            mRequest.setPsid(null);
            mRequest.getEwsBaseCompany().setEin(null);
            mRequest.setSubscriptionNumber(eu.getEntitlement().getSubscriptionNumber());

            queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30005, ewsResponseStatus.getCode());
            assertEquals("Field PSID in Request does not contain valid data", ewsResponseStatus.getMessage());
            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEUActivationHold() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);

            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.ActivationHold);
            Application.save(eu);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
            assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
            assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
            assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

            assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
            EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
            assertFalse(ewsEntitlementResponse.getHasMultipleActiveEINs());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEUErrorActivating() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);

            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
            Application.save(eu);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
            assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
            assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
            assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

            assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
            EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
            assertFalse(ewsEntitlementResponse.getHasMultipleActiveEINs());
            assertEquals("The payment status should be Suspended", EwsEntitlementPaymentStatusCode.Active.toString(),
                         ewsEntitlementResponse.getPaymentStatus());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEUPendingActivation() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);

            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
            Application.save(eu);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
            assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
            assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
            assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

            assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
            EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
            assertFalse(ewsEntitlementResponse.getHasMultipleActiveEINs());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEUPendingReactivation() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);

            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingReactivation);
            Application.save(eu);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
            assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
            assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
            assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

            assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
            EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
            assertFalse(ewsEntitlementResponse.getHasMultipleActiveEINs());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountCloudOnlyPsidEinSubNumWithPsidMismatch() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountEin(company);
            mRequest.setPsid("000000000");
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());

            Application.beginUnitOfWork();
            company = Application.refresh(company);
            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PSIDMismatch);
            assertEquals(1, companyEvents.size());
            CompanyEvent companyEvent = companyEvents.get(0);
            assertEquals(CompanyEventStatus.Inactive, companyEvent.getStatusCd());

            assertEquals(2, companyEvent.getCompanyEventDetailCollection().size());
            EwsMessage ewsMessage = EwsMessages.psidMismatch(mRequest.getEwsBaseCompany().getEin(), mRequest.getSubscriptionNumber(), mRequest.getPsid(), mResponse.getPsid());
            assertEquals(String.valueOf(ewsMessage.getCode()), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ErrorCode));
            assertEquals(ewsMessage.getMessage(), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ErrorMessage));
            Application.rollbackUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountPsidEntitlementUnitInactive() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
            entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            Application.save(entitlementUnit);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertFalse(mResponse.getEwsEntitlementUnitResponses().isEmpty());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEinSubscriptionDoesNotExists() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountEin(company);
            mRequest.getEwsBaseCompany().setEin("909886864");
            mRequest.setSubscriptionNumber("0");
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertNull(mResponse.getCompanyResponse());
            assertTrue(mResponse.getEwsEntitlementUnitResponses().isEmpty());
            assertNull(mResponse.getEwsServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30102, ewsResponseStatus.getCode());
            assertEquals("EIN Does Not Exist", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEinEntitlementUnitInactive() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
            entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            entitlementUnit = Application.save(entitlementUnit);
            mRequest = TestDataFactory.createEwsQueryAccountEin(company, entitlementUnit);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertFalse(mResponse.getEwsEntitlementUnitResponses().isEmpty());

            assertNotNull(mResponse.getEwsServicesResponse());
            assertNotNull(mResponse.getEwsServicesResponse().getCloudResponse());

            assertNull(mResponse.getEwsServicesResponse().getAssistedResponse());
            assertNull(mResponse.getEwsServicesResponse().getBillPaymentResponse());
            assertNull(mResponse.getEwsServicesResponse().getCheckDistributionResponse());
            assertNull(mResponse.getEwsServicesResponse().getDirectDepositResponse());
            assertNull(mResponse.getEwsServicesResponse().getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEinTwoEntitlementUnitsOneActiveOneInactive() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
            entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            Application.save(entitlementUnit);
            Application.commitUnitOfWork();

            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            entitlementUnit = company.getActivePrimaryEntitlementUnit();
            mRequest = TestDataFactory.createEwsQueryAccountEin(company, entitlementUnit);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());

            assertNotNull(mResponse.getEwsServicesResponse());
            assertNotNull(mResponse.getEwsServicesResponse().getCloudResponse());

            assertNull(mResponse.getEwsServicesResponse().getAssistedResponse());
            assertNull(mResponse.getEwsServicesResponse().getBillPaymentResponse());
            assertNull(mResponse.getEwsServicesResponse().getCheckDistributionResponse());
            assertNull(mResponse.getEwsServicesResponse().getDirectDepositResponse());
            assertNull(mResponse.getEwsServicesResponse().getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEinTwoEntitlementUnitsBothInactive() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
            entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            Application.save(entitlementUnit);
            Application.commitUnitOfWork();

            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);

            entitlementUnit = company.getActivePrimaryEntitlementUnit();
            Entitlement entitlement = entitlementUnit.getEntitlement();

            for (EntitlementUnit eu : entitlement.getEntitlementUnitCollection()) {
                eu.setFedTaxId(entitlementUnit.getFedTaxId());
                eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                entitlementUnit = Application.save(eu);
            }

            mRequest = TestDataFactory.createEwsQueryAccountEin(company, entitlementUnit);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertTrue(mResponse.getEwsEntitlementUnitResponses().isEmpty());

            assertNull(mResponse.getEwsServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30161, ewsResponseStatus.getCode());
            assertEquals("Unable to find a unique entitlement unit for this ein.", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEinTwoEntitlementUnitsBothActive() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
            entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            Application.save(entitlementUnit);
            Application.commitUnitOfWork();

            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);

            entitlementUnit = company.getActivePrimaryEntitlementUnit();
            Entitlement entitlement = entitlementUnit.getEntitlement();

            for (EntitlementUnit eu : entitlement.getEntitlementUnitCollection()) {
                eu.setFedTaxId(entitlementUnit.getFedTaxId());
                eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
                entitlementUnit = Application.save(eu);
            }

            mRequest = TestDataFactory.createEwsQueryAccountEin(company, entitlementUnit);
            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertTrue(mResponse.getEwsEntitlementUnitResponses().isEmpty());

            assertNull(mResponse.getEwsServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30160, ewsResponseStatus.getCode());
            assertEquals("Duplicate active entitlement units found for this ein.", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEntitlementPaymentFailure() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);

            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
            Application.save(eu);

            Entitlement entitlement = eu.getEntitlement();
            entitlement.setSubscriptionEndDate(SpcfCalendar.createInstance(2013, 10, 1));
            Application.save(entitlement);

            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();
            PayrollServices.beginUnitOfWork();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
            assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
            assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
            assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

            assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
            EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
            assertFalse(ewsEntitlementResponse.getHasMultipleActiveEINs());
            assertEquals("SHould be suspended", ewsEntitlementResponse.getPaymentStatus(),
                         EwsEntitlementPaymentStatusCode.Suspended.toString());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
            PayrollServices.rollbackUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void queryAccountEntitlementPaymentSuccess() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsQueryAccountPsid(company);

            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            eu.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
            Application.save(eu);

            Entitlement entitlement = eu.getEntitlement();
            entitlement.setSubscriptionEndDate(null);
            Application.save(entitlement);

            Application.commitUnitOfWork();

            QueryAccountProcess queryAccountProcess = new QueryAccountProcess(mRequest);
            mResponse = queryAccountProcess.execute();
            PayrollServices.beginUnitOfWork();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsEntitlementUnitResponses());
            assertEquals(1, mResponse.getEwsEntitlementUnitResponses().size());
            EwsEntitlementUnitResponse ewsEntitlementUnitResponse = mResponse.getEwsEntitlementUnitResponses().get(0);
            assertNotNull(ewsEntitlementUnitResponse.getServiceKey());
            assertNull(ewsEntitlementUnitResponse.getDiskDeliveryKey());
            assertEquals(EwsEntitlementUnitStatusCode.Activated, ewsEntitlementUnitResponse.getStatus());

            assertNotNull(ewsEntitlementUnitResponse.getEwsEntitlementResponse());
            EwsEntitlementResponse ewsEntitlementResponse = ewsEntitlementUnitResponse.getEwsEntitlementResponse();
            assertFalse(ewsEntitlementResponse.getHasMultipleActiveEINs());
            assertEquals("Should be Active", ewsEntitlementResponse.getPaymentStatus(),
                         EwsEntitlementPaymentStatusCode.Active.toString());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
            PayrollServices.rollbackUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

}
