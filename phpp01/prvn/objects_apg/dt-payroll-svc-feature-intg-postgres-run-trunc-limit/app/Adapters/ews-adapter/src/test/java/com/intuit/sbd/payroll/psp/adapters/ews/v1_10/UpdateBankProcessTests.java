package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsBankVerificationStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.UpdateBankProcess;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.ValidateBankProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
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
public class UpdateBankProcessTests {

    private EwsUpdateBank mRequest;
    private EwsBankResponse mResponse;
    private String psid = null;
    private ValidateBankProcessTests mValidateBankProcessTests = new ValidateBankProcessTests();
    private Boolean assistedTest;

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
    public void UpdateBankDD() {
        try {
            mValidateBankProcessTests.ValidateBankDD();
            psid = mValidateBankProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateBankDD(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            UpdateBankProcess updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

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
            assertNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankAssistedServiceResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());

            PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                            TransmissionType.UpdateBankAccount),"v1_10/test_UpdateBankDD.xml",
                    Arrays.asList("DateTimeStamp", "PSID"));
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void UpdateBankDDCompanyOnHold() {
        try {
            mValidateBankProcessTests.ValidateBankDD();
            psid = mValidateBankProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateBankDD(company.getSourceCompanyId());
            company.addOnHoldReason(ServiceSubStatusCode.AchRejectOther);
            Application.commitUnitOfWork();

            UpdateBankProcess updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

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
            assertNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankAssistedServiceResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void UpdateBankForceRandomDollarFalse() {
        try {
            mValidateBankProcessTests.ValidateBankDD();
            psid = mValidateBankProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateBankDD(company.getSourceCompanyId());
            mRequest.setForceRandomDollar(false);
            Application.commitUnitOfWork();

            UpdateBankProcess updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

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
            assertNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankAssistedServiceResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void UpdateBankDDNamesOnly() {
        try {
            mValidateBankProcessTests.ValidateBankDD();
            psid = mValidateBankProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateBankNameOnlyDD(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            UpdateBankProcess updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

            assertNotNull(mResponse);
            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsBankServicesResponse());

            EwsBankServicesResponse ewsBankServicesResponse = mResponse.getEwsBankServicesResponse();
            assertNotNull(ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse());

            EwsBankDirectDepositServiceResponse ewsBankDirectDepositServiceResponse = ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse();
            assertNotNull(ewsBankDirectDepositServiceResponse.getEwsBaseBankAccountResponse());

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
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void UpdateBankAssisted() {
        assistedTest = true;
        try {
            mValidateBankProcessTests.ValidateBankAssisted();
            psid = mValidateBankProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateBankAssisted(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            UpdateBankProcess updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

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
            assertNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    @Test
    public void UpdateBankDDInvalidRouting() {
        try {
            mValidateBankProcessTests.ValidateBankDD();
            psid = mValidateBankProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateBankDD(company.getSourceCompanyId());
            mRequest.getEwsUpdateBankServices().getEwsUpdateBankDirectDepositService().getEwsBankAccount().setRoutingNumber("123456789");
            Application.commitUnitOfWork();

            UpdateBankProcess updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

            assertNotNull(mResponse);
            assertNull(mResponse.getPsid());

            assertNull(mResponse.getEwsBankServicesResponse());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30005, ewsResponseStatus.getCode());
            assertEquals("Field RoutingNumber in BankAccount does not contain valid data", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Test
    public void PSRV002543() {
        try {
            mValidateBankProcessTests.ValidateBankDD();
            psid = mValidateBankProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateBankDD(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            UpdateBankProcess updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

            EwsValidateBank request = TestDataFactory.createEwsValidateBankDD(company.getSourceCompanyId());
            ValidateBankProcess validateBankProcess = new ValidateBankProcess(request);
            mResponse = validateBankProcess.execute();

            mRequest = TestDataFactory.createEwsUpdateBankDD(company.getSourceCompanyId());
            updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

            validateBankProcess = new ValidateBankProcess(request);
            mResponse = validateBankProcess.execute();

            mRequest = TestDataFactory.createEwsUpdateBankDD(company.getSourceCompanyId());
            updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

            validateBankProcess = new ValidateBankProcess(request);
            mResponse = validateBankProcess.execute();

            mRequest = TestDataFactory.createEwsUpdateBankDD(company.getSourceCompanyId());
            updateBankProcess = new UpdateBankProcess(mRequest);
            mResponse = updateBankProcess.execute();

            validateBankProcess = new ValidateBankProcess(request);
            mResponse = validateBankProcess.execute();

            assertNotNull(mResponse);
/*            assertNotNull(mResponse.getPsid());

            assertNotNull(mResponse.getEwsBankServicesResponse());

            EwsBankServicesResponse ewsBankServicesResponse = mResponse.getEwsBankServicesResponse();
            assertNotNull(ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse());

            EwsBankDirectDepositServiceResponse ewsBankDirectDepositServiceResponse = ewsBankServicesResponse.getEwsBankDirectDepositServiceResponse();
            assertNotNull(ewsBankDirectDepositServiceResponse.getEwsBaseBankAccountResponse());

            EwsBaseBankAccountResponse ewsBaseBankAccountResponse = ewsBankDirectDepositServiceResponse.getEwsBaseBankAccountResponse();
            assertEquals(EwsBankVerificationStatus.New, ewsBaseBankAccountResponse.getVerificationStatus());
            assertEquals(true, ewsBaseBankAccountResponse.isRetries());
            assertNull(ewsBaseBankAccountResponse.getLastRetryDateTime());

            assertNull(ewsBankServicesResponse.getEwsBankAssistedServiceResponse());*/

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull( mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }    
}
