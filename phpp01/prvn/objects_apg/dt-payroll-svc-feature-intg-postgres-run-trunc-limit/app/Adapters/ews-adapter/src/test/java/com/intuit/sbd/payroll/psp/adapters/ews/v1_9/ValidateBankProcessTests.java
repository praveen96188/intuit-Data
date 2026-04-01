package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsBankVerificationStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsServiceStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.ValidateBankProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class ValidateBankProcessTests {

    private EwsValidateBank mRequest;
    private EwsBankResponse mResponse;

    private String psid = null;
    private Boolean assistedTest;

    public String getPSID() {
        return psid;
    }

    private CreateAccountProcessTests mCreateAccountProcessTests = new CreateAccountProcessTests();

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
    public void ValidateBankDD() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            psid = company.getSourceCompanyId();
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsValidateBankDD(company.getSourceCompanyId());

            ValidateBankProcess validateBankProcess = new ValidateBankProcess(mRequest);
            mResponse = validateBankProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsBankServicesResponse());

            EwsBankServicesResponse ewsBankServicesResponse = mResponse.getEwsBankServicesResponse();
            assertNotNull(ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse());

            EwsBankDirectDepositServiceResponse ewsBankDirectDepositServiceResponse = ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse();
            assertNotNull(ewsBankDirectDepositServiceResponse.getEwsBaseBankAccountResponse());

            assertEquals(EwsServiceStatus.PendingPinCreation, ewsBankDirectDepositServiceResponse.getStatus());

            EwsBaseBankAccountResponse ewsBaseBankAccountResponse = ewsBankDirectDepositServiceResponse.getEwsBaseBankAccountResponse();
            assertEquals(EwsBankVerificationStatus.Verified, ewsBaseBankAccountResponse.getVerificationStatus());
            assertEquals(true, ewsBaseBankAccountResponse.isRetries());
            assertNotNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankAssistedServiceResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void ValidateBankAssisted() {
        assistedTest = true;
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsValidateBankAssisted(company.getSourceCompanyId());

            ValidateBankProcess validateBankProcess = new ValidateBankProcess(mRequest);
            mResponse = validateBankProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsBankServicesResponse());

            EwsBankServicesResponse ewsBankServicesResponse = mResponse.getEwsBankServicesResponse();
            assertNotNull(ewsBankServicesResponse.getEwsBankAssistedServiceResponse());

            EwsBankAssistedServiceResponse ewsBankAssistedServiceResponse = ewsBankServicesResponse.getEwsBankAssistedServiceResponse();
            assertNotNull(ewsBankAssistedServiceResponse.getEwsBaseBankAccountResponse());

            assertEquals(EwsServiceStatus.PendingActivation, ewsBankAssistedServiceResponse.getStatus());

            EwsBaseBankAccountResponse ewsBaseBankAccountResponse = ewsBankAssistedServiceResponse.getEwsBaseBankAccountResponse();
            assertEquals(EwsBankVerificationStatus.Verified, ewsBaseBankAccountResponse.getVerificationStatus());
            assertEquals(true, ewsBaseBankAccountResponse.isRetries());
            assertNotNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void ValidateBankDDWrongDebits() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsValidateBankDD(company.getSourceCompanyId());

            mRequest.getEwsValidateBankServices().getEwsValidateBankDirectDepositService().getEwsValidateBankAccount().setRandomDebit1(".1");

            ValidateBankProcess validateBankProcess = new ValidateBankProcess(mRequest);
            mResponse = validateBankProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsBankServicesResponse());

            EwsBankServicesResponse ewsBankServicesResponse = mResponse.getEwsBankServicesResponse();
            assertNotNull(ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse());

            EwsBankDirectDepositServiceResponse ewsBankDirectDepositServiceResponse = ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse();
            assertNotNull(ewsBankDirectDepositServiceResponse.getEwsBaseBankAccountResponse());

            EwsBaseBankAccountResponse ewsBaseBankAccountResponse = ewsBankDirectDepositServiceResponse.getEwsBaseBankAccountResponse();
            assertEquals(EwsBankVerificationStatus.New, ewsBaseBankAccountResponse.getVerificationStatus());
            assertEquals(true, ewsBaseBankAccountResponse.isRetries());
            assertNotNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankAssistedServiceResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30104, ewsResponseStatus.getCode());
            assertEquals("Random Dollars For Bank Don't Match", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void ValidateBankAssistedWrongDebits() {
        assistedTest = true;
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsValidateBankAssisted(company.getSourceCompanyId());
            mRequest.getEwsValidateBankServices().getEwsValidateBankAssistedService().getEwsValidateBankAccount().setRandomDebit1(".1");
            Application.commitUnitOfWork();

            ValidateBankProcess validateBankProcess = new ValidateBankProcess(mRequest);
            mResponse = validateBankProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsBankServicesResponse());

            EwsBankServicesResponse ewsBankServicesResponse = mResponse.getEwsBankServicesResponse();
            assertNotNull(ewsBankServicesResponse.getEwsBankAssistedServiceResponse());

            EwsBankAssistedServiceResponse ewsBankAssistedServiceResponse = ewsBankServicesResponse.getEwsBankAssistedServiceResponse();
            assertNotNull(ewsBankAssistedServiceResponse.getEwsBaseBankAccountResponse());

            EwsBaseBankAccountResponse ewsBaseBankAccountResponse = ewsBankAssistedServiceResponse.getEwsBaseBankAccountResponse();
            assertEquals(EwsBankVerificationStatus.New, ewsBaseBankAccountResponse.getVerificationStatus());
            assertEquals(true, ewsBaseBankAccountResponse.isRetries());
            assertNotNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30104, ewsResponseStatus.getCode());
            assertEquals("Random Dollars For Bank Don't Match", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void ValidateBankDDServiceNull() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsValidateBankDD(company.getSourceCompanyId());

            mRequest.getEwsValidateBankServices().setEwsValidateBankDirectDepositService(null);

            ValidateBankProcess validateBankProcess = new ValidateBankProcess(mRequest);
            mResponse = validateBankProcess.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertNull(mResponse.getEwsBankServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30006, ewsResponseStatus.getCode());
            assertEquals("Object DirectDeposit is required and cannot be null", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void ValidateBankAssistedServiceNull() {
        assistedTest = true;
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsValidateBankDD(company.getSourceCompanyId());

            mRequest.getEwsValidateBankServices().setEwsValidateBankAssistedService(null);

            ValidateBankProcess validateBankProcess = new ValidateBankProcess(mRequest);
            mResponse = validateBankProcess.execute();

            assertNotNull(mResponse);

            assertNull(mResponse.getPsid());

            assertNull(mResponse.getEwsBankServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30006, ewsResponseStatus.getCode());
            assertEquals("Object Assisted is required and cannot be null", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void ValidateBankDDPinExists() {
        try {
            AddServiceProcessTests addServiceProcessTests = new AddServiceProcessTests();
            addServiceProcessTests.addServiceDDToCloudWithPin();

            psid = addServiceProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            psid = company.getSourceCompanyId();
            Application.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsValidateBankDD(company.getSourceCompanyId());

            ValidateBankProcess validateBankProcess = new ValidateBankProcess(mRequest);
            mResponse = validateBankProcess.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsBankServicesResponse());

            EwsBankServicesResponse ewsBankServicesResponse = mResponse.getEwsBankServicesResponse();
            assertNotNull(ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse());

            EwsBankDirectDepositServiceResponse ewsBankDirectDepositServiceResponse = ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse();
            assertNotNull(ewsBankDirectDepositServiceResponse.getEwsBaseBankAccountResponse());

            assertEquals(EwsServiceStatus.PendingFirstPayroll, ewsBankDirectDepositServiceResponse.getStatus());

            EwsBaseBankAccountResponse ewsBaseBankAccountResponse = ewsBankDirectDepositServiceResponse.getEwsBaseBankAccountResponse();
            assertEquals(EwsBankVerificationStatus.Verified, ewsBaseBankAccountResponse.getVerificationStatus());
            assertEquals(true, ewsBaseBankAccountResponse.isRetries());
            assertNotNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankAssistedServiceResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
