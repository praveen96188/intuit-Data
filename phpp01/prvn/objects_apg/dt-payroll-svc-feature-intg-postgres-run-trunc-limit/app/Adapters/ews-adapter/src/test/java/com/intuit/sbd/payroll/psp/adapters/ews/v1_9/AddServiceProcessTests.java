package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsAddService;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsAddServiceResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsAssistedServiceResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsBankAccountResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsBaseService;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsBaseServiceResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsDirectDepositServiceResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsEntitlement;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsResponseStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsServicesResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsBankAccountType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsBankVerificationStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsServiceStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.AddServiceProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertSuccess;
import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class AddServiceProcessTests {

    private EwsAddService mRequest;
    private EwsAddServiceResponse mResponse;

    private CreateAccountProcessTests mCreateAccountProcessTests = new CreateAccountProcessTests();
    private CreatePinProcessTests mCreatePinProcessTests = new CreatePinProcessTests();

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
            //Costco codes have been updated PSP-5049
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
    
    private void assertResponseSuccess(EwsAddServiceResponse pEwsAddServiceResponse) {
        assertNotNull("Response null", pEwsAddServiceResponse);
        assertTrue("Request Failed: \n" + pEwsAddServiceResponse.getEwsResponseStatus().getMessage(), pEwsAddServiceResponse.getEwsResponseStatus().getCode() == 0);
    }    

}
