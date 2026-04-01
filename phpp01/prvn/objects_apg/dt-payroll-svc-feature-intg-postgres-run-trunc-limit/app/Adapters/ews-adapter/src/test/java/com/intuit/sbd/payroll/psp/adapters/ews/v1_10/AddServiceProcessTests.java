package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.client.ius.IUSGrantClient;
import com.intuit.client.ius.IUSRestTransport;
import com.intuit.client.ius.IUSRestTransportImpl;
import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.platform.integration.ius.common.types.IntuitContext;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsBankAccountType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsBankVerificationStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsServiceStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.AddServiceProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSAppCallback;
import com.intuit.sbd.payroll.psp.gateways.iam.identity.IdentityConfigManager;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.ius.IUSCompany;
import com.intuit.sbd.payroll.psp.ius.IUSDataGenerator;
import com.intuit.sbd.payroll.psp.ius.TestAuthorizationManager;
import com.intuit.sbd.payroll.psp.processes.ConstantValues;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.hamcrest.core.StringContains;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertSuccess;
import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class AddServiceProcessTests {

    private static final String PAYROLL_PLUGIN_ASSET_ALIAS = "Intuit.payroll.dirctdeposit.qbdtpayrolltronexp";
    private EwsAddService mRequest;
    private EwsAddServiceResponse mResponse;

    private CreateAccountProcessTests mCreateAccountProcessTests = new CreateAccountProcessTests();
    private CreatePinProcessTests mCreatePinProcessTests = new CreatePinProcessTests();
    private TestAuthorizationManager testAuthorizationManager = new TestAuthorizationManager();

    private IUSDataGenerator iusDataGenerator = new IUSDataGenerator();

    private String psid = null;

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
    public void startUp() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void addServiceAssistedToCloud() throws Exception {
        mCreateAccountProcessTests.createAccountCloudOnly();
        psid = mCreateAccountProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getEwsServicesResponse());

        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

        //assertEquals("S00001", ewsAssistedServiceResponse.getSourceCode());
        //assertNull(ewsAssistedServiceResponse.getPriceCode());
        assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
        assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());

        assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

        EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                        TransmissionType.CreateAccount), "v1_10/test_addServiceProcess_account.xml",
                Arrays.asList("EIN", "DateTimeStamp", "PSID", "ServiceKey", "SubscriptionNumber"));
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                        TransmissionType.AddService), "v1_10/test_addServicesProcess_Service.xml",
                Arrays.asList("DateTimeStamp", "PSID", "RandomDebitDateTime"));
    }

    @Test
    public void addServiceAssistedToCloudError1068() throws Exception {
        mCreateAccountProcessTests.createAccountCloudOnly();
        psid = mCreateAccountProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        company.setPriceType("Costco");
        Application.save(company);
        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());

        EntitlementCodeOffering entitlementCodeOffering = Application.findById(EntitlementCodeOffering.class, SpcfUniqueId.createInstance("97951845-6771-4f77-8cb0-ee01ad67ee68"));
        entitlementCodeOffering.setIsDefault(false);
        Application.save(entitlementCodeOffering);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementUnitDTO.setAssetItemNumber("1100860");
        entitlementUnitDTO.setEditionType(null);
        entitlementUnitDTO.setNumberOfEmployeesType(null);
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        //With PSP-4922 with have added Tax as default offering for Costco effective 07/22/2013
        assertEquals(0, mResponse.getEwsResponseStatus().getCode());
        assertEquals("Success", mResponse.getEwsResponseStatus().getMessage());
    }

    @Test
    public void addServiceAssistedToCloudRamdomDebitFalse() throws Exception {
        mCreateAccountProcessTests.createAccountCloudOnly();
        psid = mCreateAccountProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        mRequest.setForceRandomDollar(false);

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getEwsServicesResponse());

        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

        //assertEquals("S00001", ewsAssistedServiceResponse.getSourceCode());
        //assertNull(ewsAssistedServiceResponse.getPriceCode());
        assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
        assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());

        assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

        EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void addServiceAssistedSymphonyToCloud() throws Exception {
        mCreateAccountProcessTests.createAccountCloudOnly();
        psid = mCreateAccountProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedSymphonyEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getEwsServicesResponse());

        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

        //assertEquals("S00001", ewsAssistedServiceResponse.getSourceCode());
        //assertNull(ewsAssistedServiceResponse.getPriceCode());
        assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
        assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());

        assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

        EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void addServiceAssistedToCloudWithPin() throws Exception {
        mCreatePinProcessTests.createPinCloudOnly();
        psid = mCreatePinProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getEwsServicesResponse());

        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

        assertNull(ewsAssistedServiceResponse.getSourceCode());
        //assertNull(ewsAssistedServiceResponse.getPriceCode());
        assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
        assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());

        assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

        EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void addServiceAssistedCostcoToCloud() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            company.setPriceType("Costco");
            Application.save(company);
            EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
            EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
            entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                    .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                            entitlementUnitDTO);
            assertTrue(entitlementPR.isSuccess());
            Application.commitUnitOfWork();

            Application.beginUnitOfWork();
            EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
            entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
            entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                    (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
            assertTrue(entitlementPR.isSuccess());
            entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
            Application.save(entitlementPR.getResult());
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^9\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            //assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNotNull(ewsServicesResponse.getAssistedResponse());

            EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

            //assertEquals("S00001", ewsAssistedServiceResponse.getSourceCode());
            //assertNull(ewsAssistedServiceResponse.getPriceCode());
            assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
            assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());

            assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            assertNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsServicesResponse.getDirectDepositResponse());
            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());

            PayrollServices.beginUnitOfWork();
            company = Application.refresh(company);
            CompanyOffering companyOffering = company.getOffering(ServiceCode.DirectDeposit);
            assertEquals(OfferingCode.COSTCO672, companyOffering.getOffering().getOfferingCode());
            PayrollServices.rollbackUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void addServiceAssistedToCancelledAssisted() throws Exception {
        mCreateAccountProcessTests.createAccountCloudAndAssisted();

        psid = mCreateAccountProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();

        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        DataLoadServices.addAssistedEntitlementUnit(company, "5", "6", true);

        Application.beginUnitOfWork();
        ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.updateServiceStatus
                (SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
        assertSuccess("Cancel tax service", servicePR);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);

        assertFalse(psid.equals(mResponse.getPsid()));
        psid = mResponse.getPsid();

        company = PspFactory.findCompany(psid);
        assertFalse(company.isMoneyMovementOnboardingEnabled());

        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());

        EwsServicesResponse services = mResponse.getEwsServicesResponse();

        assertNotNull(services.getAssistedResponse());
        EwsAssistedServiceResponse assistedService = services.getAssistedResponse();
        assertEquals(EwsServiceStatus.PendingActivation, assistedService.getStatus());

        assertNotNull(services.getCloudResponse());
        EwsBaseServiceResponse cloudService = services.getCloudResponse();
        assertEquals(EwsServiceStatus.PendingPinCreation, cloudService.getStatus());

        assertNull(services.getDirectDepositResponse());
        assertNull(services.getBillPaymentResponse());
        assertNull(services.getCheckDistributionResponse());
        assertNull(services.getThirdParty401kResponse());
    }

    @Test
    public void addServiceDDToCancelledAssisted() throws Exception {
        addServiceAssistedToCloud();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();

        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);
        DataLoadServices.addDIYEntitlementUnit(company, "6", "7", EditionType.Basic, NumberOfEmployeesType.UNLIMITED);

        Application.beginUnitOfWork();
        ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.updateServiceStatus
                (SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
        assertTrue(servicePR.isSuccess());

        servicePR = PayrollServices.companyManager.updateServiceStatus
                (SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
        assertTrue(servicePR.isSuccess());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);

        psid = mResponse.getPsid();

        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());

        EwsServicesResponse services = mResponse.getEwsServicesResponse();

        assertNotNull(services.getDirectDepositResponse());
        EwsDirectDepositServiceResponse ddService = services.getDirectDepositResponse();
        assertEquals(EwsServiceStatus.PendingBankVerification, ddService.getStatus());

        assertNotNull(services.getCloudResponse());
        EwsBaseServiceResponse cloudService = services.getCloudResponse();
        assertEquals(EwsServiceStatus.PendingPinCreation, cloudService.getStatus());

        assertNull(services.getAssistedResponse());
        assertNull(services.getBillPaymentResponse());
        assertNull(services.getCheckDistributionResponse());
        assertNull(services.getThirdParty401kResponse());

        PayrollServices.beginUnitOfWork();
        assertEquals(1, Company.findCompany(psid, SourceSystemCode.QBDT).getCompanyBankAccountCollection().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void addServiceAssistedToCancelledDDAndAssisted() throws Exception {
        addServiceDDToCancelledAssisted();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();

        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        DataLoadServices.addAssistedEntitlementUnit(company, "5", "6", true);

        Application.beginUnitOfWork();
        ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.updateServiceStatus
                (SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
        assertTrue(servicePR.isSuccess());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);

        psid = mResponse.getPsid();

        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());

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
    }

    @Test
    public void addServiceDDToCloud() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();

            assertEquals(EwsServiceStatus.PendingBankVerification, ewsDirectDepositServiceResponse.getStatus());

            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            //assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            //assertNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    @Test
    public void addServiceDDToCloudForTRONCompany() {
        try {
            setPayrollPluginContext();

            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            setNewIAMRealmId(company.getSourceCompanyId());

            mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());

            mRequest.setForceRandomDollar(false);

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();

            assertEquals(EwsServiceStatus.PendingBankVerification, ewsDirectDepositServiceResponse.getStatus());

            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            //assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            //assertNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());

            assertMoneyMovementOnboardingEnabled();

        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        } finally {
            removePayrollPluginContext();
        }
    }

    @Test
    public void addServiceDDToCloudWithFDPBankTokenForTRONCompany() {
        try {
            setPayrollPluginContext();

            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            setNewIAMRealmId(company.getSourceCompanyId());

            mRequest = TestDataFactory.createEwsAddServiceDDToCloudWithFDPBankToken(company.getSourceCompanyId());

            mRequest.setForceRandomDollar(false);

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();

            assertEquals(EwsServiceStatus.PendingBankVerification, ewsDirectDepositServiceResponse.getStatus());

            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("20891", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            //assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            //assertNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());

            assertMoneyMovementOnboardingEnabled();

        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        } finally {
            removePayrollPluginContext();
        }
    }

    @Test
    public void addServiceDDToCloudRandomDebitFalse() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());

            mRequest.setForceRandomDollar(false);

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();

            assertEquals(EwsServiceStatus.PendingBankVerification, ewsDirectDepositServiceResponse.getStatus());

            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            //assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            //assertNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    @Test
    public void addServiceDDToCloudWithPin() {
        try {
            mCreatePinProcessTests.createPinCloudOnly();
            psid = mCreatePinProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();

            assertEquals(EwsServiceStatus.PendingBankVerification, ewsDirectDepositServiceResponse.getStatus());

            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            //assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            //assertNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    @Test
    public void addServiceDDToDDCancelled() {
        try {
            mCreatePinProcessTests.createPinCloudAndDD();
            psid = mCreatePinProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            ProcessResult<CompanyService> processResult = PayrollServices.companyManager.updateServiceStatus
                    (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
            assertTrue(processResult.isSuccess());
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();

            assertNotNull(ewsServicesResponse.getCloudResponse());
            EwsBaseServiceResponse ewsBaseServiceResponse = ewsServicesResponse.getCloudResponse();
            assertEquals(EwsServiceStatus.PendingFirstPayroll, ewsBaseServiceResponse.getStatus());

            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();

            assertEquals(EwsServiceStatus.PendingFirstPayroll, ewsDirectDepositServiceResponse.getStatus());

            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            //assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.Verified, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            assertNotNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    @Test
    public void addServiceBillPaymentToDD() {
        try {
            mCreatePinProcessTests.createPinCloudAndDD();
            psid = mCreatePinProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceBillPaymentToDD(company.getSourceCompanyId());

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();

            assertNotNull(ewsServicesResponse.getCloudResponse());
            EwsBaseServiceResponse ewsBaseServiceResponse = ewsServicesResponse.getCloudResponse();
            assertEquals(EwsServiceStatus.PendingFirstPayroll, ewsBaseServiceResponse.getStatus());

            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();

            assertEquals(EwsServiceStatus.PendingFirstPayroll, ewsDirectDepositServiceResponse.getStatus());

            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            //assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.Verified, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            assertNotNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNotNull(ewsServicesResponse.getBillPaymentResponse());

            ewsBaseServiceResponse = ewsServicesResponse.getBillPaymentResponse();
            assertEquals(EwsServiceStatus.Active, ewsBaseServiceResponse.getStatus());

            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    @Test
    public void addServiceBillPaymentToTax() throws Exception {
        mCreatePinProcessTests.createPinCloudAndAssisted();
        psid = mCreatePinProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceBillPaymentToDD(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertNotNull(mResponse);
        assertEquals(psid, mResponse.getPsid());
        assertNotNull(mResponse.getEwsServicesResponse());
        assertNotNull(mResponse.getEwsServicesResponse().getBillPaymentResponse());
        assertEquals(EwsServiceStatus.Active, mResponse.getEwsServicesResponse().getBillPaymentResponse().getStatus());
        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void addServiceBillPaymentToBillPaymentCancelled() {
        try {
            addServiceBillPaymentToDD();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            ProcessResult<CompanyService> processResult = PayrollServices.companyManager.updateServiceStatus
                    (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.BillPayment, ServiceSubStatusCode.Cancelled);
            assertTrue(processResult.isSuccess());
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceBillPaymentToDD(company.getSourceCompanyId());

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();

            assertNotNull(ewsServicesResponse.getCloudResponse());
            EwsBaseServiceResponse ewsBaseServiceResponse = ewsServicesResponse.getCloudResponse();
            assertEquals(EwsServiceStatus.PendingFirstPayroll, ewsBaseServiceResponse.getStatus());

            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();

            assertEquals(EwsServiceStatus.PendingFirstPayroll, ewsDirectDepositServiceResponse.getStatus());

            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            //assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.Verified, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            assertNotNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNotNull(ewsServicesResponse.getBillPaymentResponse());

            ewsBaseServiceResponse = ewsServicesResponse.getBillPaymentResponse();
            assertEquals(EwsServiceStatus.Active, ewsBaseServiceResponse.getStatus());

            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    @Test
    public void addServiceDdAndDD4VToCloud() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());
            mRequest.getEwsBaseServices().setBillPayment(new EwsBaseService());

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertNull(mResponse.getEwsServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30144, ewsResponseStatus.getCode());
            assertEquals("Direct Deposit Service Status is not valid for activating Bill Payment Service.", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    @Test
    public void addServiceDdToCloudDupRequest() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertNull(mResponse.getEwsServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30137, ewsResponseStatus.getCode());
            assertEquals("Company already exists on Direct Deposit Service.", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    @Test
    public void addServiceReactivateDDWithEUInactive() {
        try {
            mCreatePinProcessTests.createPinCloudAndDD();
            psid = mCreatePinProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);

            EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
            EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
            entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            ProcessResult euPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                    (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
            assertTrue(euPR.isSuccess());

            ProcessResult<CompanyService> processResult = PayrollServices.companyManager.updateServiceStatus
                    (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
            assertTrue(processResult.isSuccess());
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertNull(mResponse.getEwsServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30145, ewsResponseStatus.getCode());
            assertEquals("Company must have an active Entitlement before services can be added or reactivated.", ewsResponseStatus.getMessage());
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    @Test
    public void addServiceAssistedToDD() throws Exception {
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE, "1099426");
        PayrollServices.commitUnitOfWork();
        mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
        psid = mCreateAccountProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^9\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getEwsServicesResponse());

        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

        assertNull(ewsAssistedServiceResponse.getSourceCode());
        assertNull(ewsAssistedServiceResponse.getPriceCode());
        assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
        assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());
        assertEquals("0.00", ewsAssistedServiceResponse.getMonthlyFee());

        assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

        EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void addServiceAssistedToDDForTRONCompany() throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE, "1099426");
            PayrollServices.commitUnitOfWork();
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
            EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
            entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                    .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                            entitlementUnitDTO);
            assertTrue(entitlementPR.isSuccess());
            Application.commitUnitOfWork();

            Application.beginUnitOfWork();
            EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
            entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
            entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                    (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
            assertTrue(entitlementPR.isSuccess());
            entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
            Application.save(entitlementPR.getResult());
            Application.commitUnitOfWork();

            setPayrollPluginContext();
            setNewIAMRealmId(company.getSourceCompanyId());

            mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

            mRequest.setForceRandomDollar(false);

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^9\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            //assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNotNull(ewsServicesResponse.getAssistedResponse());

            EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

            assertNull(ewsAssistedServiceResponse.getSourceCode());
            assertNull(ewsAssistedServiceResponse.getPriceCode());
            assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
            assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());
            assertEquals("0.00", ewsAssistedServiceResponse.getMonthlyFee());

            assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
            assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());
            assertNull(ewsBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsServicesResponse.getDirectDepositResponse());
            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());

            assertMoneyMovementOnboardingEnabled();
        } finally {
            removePayrollPluginContext();
        }
    }

    @Test
    public void addServiceAssistedToDDAndDD4V() throws Exception {
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE, "1099426");
        PayrollServices.commitUnitOfWork();
        addServiceBillPaymentToDD();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^9\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getEwsServicesResponse());

        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

        assertNull(ewsAssistedServiceResponse.getSourceCode());
        assertNull(ewsAssistedServiceResponse.getPriceCode());
        assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
        assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());
        assertEquals("0.00", ewsAssistedServiceResponse.getMonthlyFee());

        assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

        EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.Verified, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNotNull(ewsServicesResponse.getBillPaymentResponse());
        EwsBaseServiceResponse ewsBaseServiceResponse = ewsServicesResponse.getBillPaymentResponse();
        assertEquals(EwsServiceStatus.Active, ewsBaseServiceResponse.getStatus());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void addServiceAssistedToDDActiveCurrent() throws Exception {
        mCreatePinProcessTests.createPinCloudAndDD();
        psid = mCreatePinProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);

        CompanyService companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        Application.save(companyService);

        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^9\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getEwsServicesResponse());

        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

        assertNull(ewsAssistedServiceResponse.getSourceCode());
        assertNull(ewsAssistedServiceResponse.getPriceCode());
        assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
        assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());
        assertEquals("0.00", ewsAssistedServiceResponse.getMonthlyFee());

        assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

        EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();
        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.Verified, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNotNull(ewsServicesResponse.getDirectDepositResponse());
        EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();
        assertEquals(EwsServiceStatus.Active, ewsDirectDepositServiceResponse.getStatus());

        assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

        ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();
        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.Verified, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());
    }

    @Test
    public void addServiceAssistedToDDWithPin() throws Exception {
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE, "1099426");
        PayrollServices.commitUnitOfWork();
        DataLoadServices.resetQbdtSourceCompanyIdSeq(); //AS/400 only working for 100000000
        String pin = "ABCDefgh1234";

        mCreatePinProcessTests.createPinCloudAndDD();
        psid = mCreatePinProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());
        mRequest.setPin(pin);

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^1\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getEwsServicesResponse());

        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

        assertNull(ewsAssistedServiceResponse.getSourceCode());
        assertNull(ewsAssistedServiceResponse.getPriceCode());
        assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
        assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());
        assertEquals("0.00", ewsAssistedServiceResponse.getMonthlyFee());

        assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

        EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.Verified, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNotNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        PayrollServices.beginUnitOfWorkWithSecondary();
        Expression<SourceSystemTransmission> query =
                new Query<SourceSystemTransmission>()
                        .Where(SourceSystemTransmission.CompanyId().equalTo(company.getId().toString())
                                .And(SourceSystemTransmission.Type().equalTo(TransmissionType.AddService)))
                        .OrderBy(SourceSystemTransmission.CreatedDate().Descending());

        DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = ApplicationSecondary.find(SourceSystemTransmission.class, query);
        assertFalse(sourceSystemTransmissions.isEmpty());


        assertFalse(sourceSystemTransmissions.get(0).getRequestDocument().contains(pin));
        PayrollServices.commitUnitOfWorkWithSecondary();

        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                        TransmissionType.AddService), "v1_10/test_addServiceAssistedToDDWithPin.xml",
                Arrays.asList("DateTimeStamp", "LastRetryDateTime", "RandomDebitDateTime"));
    }

    @Test
    public void addServiceAssistedToDDWithNullPriceType() throws Exception {
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE, "1099426");
        PayrollServices.commitUnitOfWork();
        mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
        psid = mCreateAccountProcessTests.getPSID();

        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        EntitlementUnit entitlementUnit = company.getEntitlementUnitCollection().get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        ProcessResult<EntitlementUnit> entitlementPR = PayrollServices.entitlementManager
                .addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        EwsEntitlement ewsEntitlement = TestDataFactory.createAssistedEntitlement();
        entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, company.getFedTaxId());
        entitlementPR = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                (company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(entitlementPR.isSuccess());
        entitlementPR.getResult().setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementPR.getResult());

        company = PspFactory.findCompany(psid);
        company.setPriceType(null);
        Application.save(company);
        Application.commitUnitOfWork();

        mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

        AddServiceProcess process = new AddServiceProcess(mRequest);
        mResponse = process.execute();

        assertResponseSuccess(mResponse);
        psid = mResponse.getPsid();

        Pattern pattern = Pattern.compile("^9\\d{8}$");
        Matcher matcher = pattern.matcher(psid);
        //assertTrue(matcher.matches());

        assertNotNull(mResponse.getEwsServicesResponse());

        EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
        assertNotNull(ewsServicesResponse.getCloudResponse());
        assertNotNull(ewsServicesResponse.getAssistedResponse());

        EwsAssistedServiceResponse ewsAssistedServiceResponse = ewsServicesResponse.getAssistedResponse();

        assertNull(ewsAssistedServiceResponse.getSourceCode());
        assertNull(ewsAssistedServiceResponse.getPriceCode());
        assertEquals("0", ewsAssistedServiceResponse.getMostCurrentTaxYear());
        assertEquals(EwsServiceStatus.PendingActivation, ewsAssistedServiceResponse.getStatus());
        assertEquals("0.00", ewsAssistedServiceResponse.getMonthlyFee());

        assertNotNull(ewsAssistedServiceResponse.getEwsBankAccountResponse());

        EwsBankAccountResponse ewsBankAccountResponse = ewsAssistedServiceResponse.getEwsBankAccountResponse();

        assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
        assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
        assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
        assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
        assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
        assertNull(ewsBankAccountResponse.getHoldReason());
        assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
        assertNotNull(ewsBankAccountResponse.getRandomDebitDateTime());
        assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
        assertTrue(ewsBankAccountResponse.isRetries());
        assertNull(ewsBankAccountResponse.getLastRetryDateTime());

        assertNull(ewsServicesResponse.getDirectDepositResponse());
        assertNull(ewsServicesResponse.getCheckDistributionResponse());
        assertNull(ewsServicesResponse.getBillPaymentResponse());
        assertNull(ewsServicesResponse.getThirdParty401kResponse());

        assertNotNull(mResponse.getEwsResponseStatus());
        EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
        assertEquals(0, ewsResponseStatus.getCode());
        assertEquals("Success", ewsResponseStatus.getMessage());

        assertNotNull(mResponse.getDateTimeStamp());

        Application.beginUnitOfWork();
        company = PspFactory.findCompany(psid);
        assertEquals("Standard", company.getPriceType());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void addServiceAssistedToCancelledAssistedForTRONCompany() throws Exception {
        try {
            // setPayrollPluginContext();
            mCreateAccountProcessTests.createAccountCloudAndAssisted();

            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
            Application.commitUnitOfWork();

            setNewIAMRealmId(company.getSourceCompanyId());

            DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

            DataLoadServices.addAssistedEntitlementUnit(company, "5", "6", true);

            Application.beginUnitOfWork();
            ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.updateServiceStatus
                    (SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
            assertSuccess("Cancel tax service", servicePR);
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceAssistedToCloud(company.getSourceCompanyId());

            setPayrollPluginContext();
            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);

            assertFalse(psid.equals(mResponse.getPsid()));
            psid = mResponse.getPsid();

            company = PspFactory.findCompany(psid);
            assertTrue(company.isMoneyMovementOnboardingEnabled());

            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());

            EwsServicesResponse services = mResponse.getEwsServicesResponse();

            assertNotNull(services.getAssistedResponse());
            EwsAssistedServiceResponse assistedService = services.getAssistedResponse();
            assertEquals(EwsServiceStatus.PendingActivation, assistedService.getStatus());

            assertNotNull(services.getCloudResponse());
            EwsBaseServiceResponse cloudService = services.getCloudResponse();
            assertEquals(EwsServiceStatus.PendingPinCreation, cloudService.getStatus());

            assertNull(services.getDirectDepositResponse());
            assertNull(services.getBillPaymentResponse());
            assertNull(services.getCheckDistributionResponse());
            assertNull(services.getThirdParty401kResponse());
        } finally {
            removePayrollPluginContext();
        }
    }

    //Adding VMP and DD Service to Cloud through TRON by setting user context
    @Test
    public void addServiceDDAutoVMPtoCloudByTRON() throws Exception {

        IAMTicket iamTicket = getIAMTicket();

        try {
            //setting context and source
            setSourceName("TRON");
            setPayrollPluginContext();
            if(PayrollApplicationBeanFactory.getBean(IdentityConfigManager.class).isGrant2Enabled()) {
                testAuthorizationManager.setUserAuthorizationContext();
            } else {
                testAuthorizationManager.setUserAuthorizationContext(iamTicket);
            }
            //creating DIY account
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();
            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            company.setIAMRealmId(iamTicket.getRealmId());
            Application.commitUnitOfWork();

            //setNewIAMRealmId(psid);

            //creating VMP and DD Add Service Request
            mRequest = TestDataFactory.createEwsAddServiceDDVMPToCloud(company.getSourceCompanyId());
            mRequest.setForceRandomDollar(false);

            //initiating Add Service Process
            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();
            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();
            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());
            assertNotNull(mResponse.getEwsServicesResponse());
            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());
            assertNotNull(ewsServicesResponse.getViewMyPaycheckResponse());
            assertNotNull(ewsServicesResponse.getCloudV2Response());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();
            assertEquals(EwsServiceStatus.PendingBankVerification, ewsDirectDepositServiceResponse.getStatus());
            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();
            assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
            assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
            assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
            assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
            assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
            assertNull(ewsBankAccountResponse.getHoldReason());
            assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
            assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
            assertTrue(ewsBankAccountResponse.isRetries());

            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());
            assertNotNull(mResponse.getEwsResponseStatus());

            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());
            assertNotNull(mResponse.getDateTimeStamp());
            assertMoneyMovementOnboardingEnabled();

            company = PspFactory.findCompany(psid);
            //validating Grant
            RealmManager realmManager = new RealmManager();
            boolean isVMPGrantPresent = realmManager.realmHasVMPGrant(company.getIAMRealmId());
            assertEquals(true, isVMPGrantPresent);

            //validating OII Flag
            boolean isVMPEnabled = company.isVMPEnabled();
            assertEquals(true, isVMPEnabled);

            //validating Company Event
            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.AutoEnabledVMP);
            assertNotNull(companyEvents);

        }  catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        } finally {
            removePayrollPluginContext();
            removeSourceName();
            testAuthorizationManager.removeUserAuthorizationContext();
            removeGrant(iamTicket, IUSGrantClient.VMP_GRANT_OFFERING_ID);
        }
    }

    //simulate grant failure by not setting user context
    @Test
    public void addServiceDDAutoVMPtoCloudByTRONFailedVMPGrant() throws Exception {
        try {
            //setting context and source
            setSourceName("TRON");
            setPayrollPluginContext();

            //creating DIY account
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();
            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            setNewIAMRealmId(psid);

            //creating VMP and DD Add Service Request
            mRequest = TestDataFactory.createEwsAddServiceDDVMPToCloud(company.getSourceCompanyId());
            mRequest.setForceRandomDollar(false);

            //initiating Add Service Process
            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertNull(mResponse.getPsid());
            assertNull(mResponse.getEwsServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30168, ewsResponseStatus.getCode());
            assertNotNull(mResponse.getDateTimeStamp());

            //if VMP grant failure send only DD activation request
            if(ewsResponseStatus.getCode() == 30168) {
                mRequest = TestDataFactory.createEwsAddServiceDDToCloud(company.getSourceCompanyId());
                mRequest.setForceRandomDollar(false);

                process = new AddServiceProcess(mRequest);
                mResponse = process.execute();

                assertResponseSuccess(mResponse);
                psid = mResponse.getPsid();
                Pattern pattern = Pattern.compile("^1\\d{8}$");
                Matcher matcher = pattern.matcher(psid);
                assertTrue(matcher.matches());
                assertNotNull(mResponse.getEwsServicesResponse());
                EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
                assertNotNull(ewsServicesResponse.getCloudResponse());
                assertNull(ewsServicesResponse.getAssistedResponse());
                assertNotNull(ewsServicesResponse.getDirectDepositResponse());
                assertNull(ewsServicesResponse.getViewMyPaycheckResponse());
                assertNull(ewsServicesResponse.getCloudV2Response());

                EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();
                assertEquals(EwsServiceStatus.PendingBankVerification, ewsDirectDepositServiceResponse.getStatus());
                assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

                EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();
                assertEquals("Bank of Intuit", ewsBankAccountResponse.getBankName());
                assertEquals("12345-12345", ewsBankAccountResponse.getAccountNumber());
                assertEquals("091050807", ewsBankAccountResponse.getRoutingNumber());
                assertEquals("BOFI", ewsBankAccountResponse.getQuickBooksName());
                assertEquals(EwsBankAccountType.Checking, ewsBankAccountResponse.getAccountType());
                assertNull(ewsBankAccountResponse.getHoldReason());
                assertFalse(ewsBankAccountResponse.getPendingPayrollExists());
                assertEquals(EwsBankVerificationStatus.New, ewsBankAccountResponse.getVerificationStatus());
                assertTrue(ewsBankAccountResponse.isRetries());

                assertNull(ewsServicesResponse.getCheckDistributionResponse());
                assertNull(ewsServicesResponse.getBillPaymentResponse());
                assertNull(ewsServicesResponse.getThirdParty401kResponse());
                assertNotNull(mResponse.getEwsResponseStatus());

                ewsResponseStatus = mResponse.getEwsResponseStatus();
                assertEquals(0, ewsResponseStatus.getCode());
                assertEquals("Success", ewsResponseStatus.getMessage());
                assertNotNull(mResponse.getDateTimeStamp());
                assertMoneyMovementOnboardingEnabled();
            }

            company = PspFactory.findCompany(psid);
            //validating Grant
            RealmManager realmManager = new RealmManager();
            boolean isVMPGrantPresent = realmManager.realmHasVMPGrant(company.getIAMRealmId());
            assertEquals(false, isVMPGrantPresent);

            //validating OII Flag
            boolean isVMPEnabled = company.isVMPEnabled();
            assertEquals(false, isVMPEnabled);

            //validating Company Event
            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.AutoEnabledVMP);
            boolean isEventPresent = companyEvents.isEmpty() ? false : true;
            assertEquals(false, isEventPresent);

        }  catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        } finally {
            removePayrollPluginContext();
            removeSourceName();
        }
    }

    //Add VMP Service to Cloud from EWS
    @Test
    public void addServiceVMPtoCloudByEWS() throws Exception {
        try {
            //creating DIY account
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();
            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();

            //creating VMP Add Service Request
            mRequest = TestDataFactory.createEwsAddServiceVMPToCloud(company.getSourceCompanyId());

            //initiating Add Service Process
            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();
            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();
            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());
            assertNotNull(mResponse.getEwsServicesResponse());
            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNull(ewsServicesResponse.getDirectDepositResponse());
            assertNotNull(ewsServicesResponse.getViewMyPaycheckResponse());
            assertNotNull(ewsServicesResponse.getCloudV2Response());
            assertNull(ewsServicesResponse.getCheckDistributionResponse());
            assertNull(ewsServicesResponse.getBillPaymentResponse());
            assertNull(ewsServicesResponse.getThirdParty401kResponse());
            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());
            assertNotNull(mResponse.getDateTimeStamp());

            company = PspFactory.findCompany(psid);
            //validating Grant
            RealmManager realmManager = new RealmManager();
            boolean isVMPGrantPresent = realmManager.realmHasVMPGrant(company.getIAMRealmId());
            assertEquals(false, isVMPGrantPresent);

            //validating OII Flag
            boolean isVMPEnabled = company.isVMPEnabled();
            assertEquals(false, isVMPEnabled);

            //validating Company Event
            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.AutoEnabledVMP);
            boolean isEventPresent = companyEvents.isEmpty() ? false : true;
            assertEquals(false, isEventPresent);

        }  catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        }
    }

    private void assertResponseSuccess(EwsAddServiceResponse pEwsAddServiceResponse) {
        assertNotNull("Response null", pEwsAddServiceResponse);
        assertTrue("Request Failed: \n" + pEwsAddServiceResponse.getEwsResponseStatus().getMessage(), pEwsAddServiceResponse.getEwsResponseStatus().getCode() == 0);
    }

    private void setNewIAMRealmId(String sourceCompanyId) {
        Application.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        company.setIAMRealmId(getNewIAMRealmId());
        Application.commitUnitOfWork();
    }

    private String getNewIAMRealmId() {
        return "9130352961219286";
        //return iusDataGenerator.createRealmUsingSystemTicket("1", "TRON", "qbdttrontest+231020001@gmail.com");
    }

    //create IAM ticket
    private IAMTicket getIAMTicket() {
        IUSDataGenerator iusDataGenerator = new IUSDataGenerator();
        IUSCompany iusCompany =  iusDataGenerator.createCompany();
        IAMTicket iamTicket = iusCompany.getIamTicket();
        iamTicket.setRealmId(iusCompany.getRealm().getRealmId());
        return iamTicket;
    }

    //remove grant after running test for adding grant flow
    private void removeGrant(IAMTicket iamTicket, String offeringId) {
        IUSAppCallback appCallback = new IUSAppCallback();
        IUSRestTransport iusRestTransport = new IUSRestTransportImpl(appCallback);
        IUSGrantClient.setTransport(iusRestTransport);
        IUSGrantClient.deleteGrantIgnoreNotFound(iamTicket, iamTicket.getRealmId(), offeringId);
    }

    //set source name as we do in RequestHeaderHandler
    private void setSourceName(String sourceName) {
        RequestAttributesUtils.setAttribute(ConstantValues.HEADER_INTUIT_AUTO_VMP_SOURCE, sourceName);
    }

    //remove source name as we do in outbound of Request Header Handler
    private void removeSourceName() {
        RequestAttributesUtils.removeAttribute(ConstantValues.HEADER_INTUIT_AUTO_VMP_SOURCE);
    }

    private void setPayrollPluginContext() {
        IntuitContext intuitContext = new IntuitContext();
        intuitContext.setAssetAlias(PAYROLL_PLUGIN_ASSET_ALIAS);
        RequestAttributesUtils.setAttribute(ContextConstants.INTUIT_CONTEXT, intuitContext);
    }

    private void removePayrollPluginContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.INTUIT_CONTEXT);
    }

    private void assertMoneyMovementOnboardingEnabled() throws Exception {
        Application.beginUnitOfWork();
        Company company = PspFactory.findCompany(psid);
        assertTrue("OII is not enabled", company.isOIIEnabled());
        assertTrue("Money Movement Onboarding is not enabled", company.isMoneyMovementOnboardingEnabled());
        assertTrue("Activate Direct Deposit is not enabled", company.getWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT).equals(WorkflowState.ENABLED));
        Application.rollbackUnitOfWork();
    }


    @Test
    public void testGetBankAccountByWalletId() {
        try {
            setPayrollPluginContext();

            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            Application.commitUnitOfWork();


            Application.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
            company.setIAMRealmId("9130357123530646");
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsAddServiceDDToCloudWithWallet(company.getSourceCompanyId());

            mRequest.setForceRandomDollar(false);

            AddServiceProcess process = new AddServiceProcess(mRequest);
            mResponse = process.execute();

            assertResponseSuccess(mResponse);
            psid = mResponse.getPsid();

            Pattern pattern = Pattern.compile("^1\\d{8}$");
            Matcher matcher = pattern.matcher(psid);
            assertTrue(matcher.matches());

            assertNotNull(mResponse.getEwsServicesResponse());

            EwsServicesResponse ewsServicesResponse = mResponse.getEwsServicesResponse();
            assertNotNull(ewsServicesResponse.getCloudResponse());
            assertNull(ewsServicesResponse.getAssistedResponse());
            assertNotNull(ewsServicesResponse.getDirectDepositResponse());

            EwsDirectDepositServiceResponse ewsDirectDepositServiceResponse = ewsServicesResponse.getDirectDepositResponse();

            assertNotNull(ewsDirectDepositServiceResponse.getEwsBankAccountResponse());

            EwsBankAccountResponse ewsBankAccountResponse = ewsDirectDepositServiceResponse.getEwsBankAccountResponse();

            assertThat(ewsBankAccountResponse.getAccountNumber(), StringContains.containsString("7600"));

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertMoneyMovementOnboardingEnabled();

        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            fail(e.getMessage());
        } finally {
            removePayrollPluginContext();
        }
    }
}