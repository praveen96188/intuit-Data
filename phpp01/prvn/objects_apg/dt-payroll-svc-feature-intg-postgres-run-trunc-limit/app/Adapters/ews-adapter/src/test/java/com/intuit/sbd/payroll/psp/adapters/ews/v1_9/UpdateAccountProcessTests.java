package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsCompanyResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsResponseStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsUpdateAccount;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsUpdateAccountResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsDeliveryType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.UpdateAccountProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class UpdateAccountProcessTests {

    private EwsUpdateAccount mRequest;
    private EwsUpdateAccountResponse mResponse;

    private String psid = null;
    private Boolean assistedTest;

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
    public void updateAccountCloudOnly() {
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateAccountCloudOnly(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            UpdateAccountProcess process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }        
    }

    @Test
    public void updateAccountCloudAndDirectDeposit() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsUpdateAccountCloudAndDD(company.getSourceCompanyId());

            UpdateAccountProcess process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());

            // Validate IAM info
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
            assertEquals("1234567890", company.getIAMRealmId());

            Contact contact = company.getContactByRoleCode(ContactRole.PayrollAdmin);
            assertEquals("09876", contact.getIAMAuthenticationId());

            contact = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
            assertEquals("54321", contact.getIAMAuthenticationId());
            PayrollServices.rollbackUnitOfWork();

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void updateAccountCloudAndAssisted() {
        assistedTest = true;
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsUpdateAccountCloudAndAssisted(company.getSourceCompanyId());

            mRequest.getEwsCompany().setClientPacketDeliveryPreference(EwsDeliveryType.electronic);
            mRequest.getEwsCompany().setW2DeliveryPreference(EwsDeliveryType.electronic);

            UpdateAccountProcess process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getCompanyResponse());
            EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
            assertEquals(EwsDeliveryType.electronic, ewsCompanyResponse.getClientPacketDeliveryPreference());
            assertEquals(EwsDeliveryType.electronic, ewsCompanyResponse.getW2DeliveryPreference());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void updateAccountCloudAndCanceledAssisted() {
        assistedTest = true;

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EWSAdapter));

        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            PayrollServices.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            CompanyService taxService = company.getService(ServiceCode.Tax);
            taxService.setStatusCd(ServiceSubStatusCode.Cancelled);
            Application.save(taxService);
            PayrollServices.commitUnitOfWork();

            mRequest = TestDataFactory.createEwsUpdateAccountCloudAndAssisted(company.getSourceCompanyId());

            mRequest.getEwsCompany().setClientPacketDeliveryPreference(EwsDeliveryType.electronic);
            mRequest.getEwsCompany().setW2DeliveryPreference(EwsDeliveryType.electronic);

            UpdateAccountProcess process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getCompanyResponse());
            EwsCompanyResponse ewsCompanyResponse = mResponse.getCompanyResponse();
            assertEquals(EwsDeliveryType.mail, ewsCompanyResponse.getClientPacketDeliveryPreference());
            assertEquals(EwsDeliveryType.mail, ewsCompanyResponse.getW2DeliveryPreference());

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void updateAccountFieldValidationPayrollAdminEmail() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndAssisted();
            psid = mCreateAccountProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsUpdateAccountCloudAndAssisted(company.getSourceCompanyId());

            mRequest.getEwsCompany().getPayrollAdmin().seteMail("Bad eMail");

            UpdateAccountProcess process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(30005, ewsResponseStatus.getCode());
            assertEquals("Field eMail in Contact does not contain valid data", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void updateAccountCloudAndDirectDepositMissingRealmId() {
        try {
            mCreateAccountProcessTests.createAccountCloudAndDirectDeposit();
            psid = mCreateAccountProcessTests.getPSID();

            Company company = PspFactory.findCompany(psid);

            mRequest = TestDataFactory.createEwsUpdateAccountCloudAndDD(company.getSourceCompanyId());
            mRequest.getEwsCompany().setRealmId(null);

            UpdateAccountProcess process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            assertNotNull(mResponse);

            assertNotNull(mResponse.getEwsResponseStatus());
            EwsResponseStatus ewsResponseStatus = mResponse.getEwsResponseStatus();
            assertEquals(0, ewsResponseStatus.getCode());
            assertEquals("Success", ewsResponseStatus.getMessage());

            assertNotNull(mResponse.getDateTimeStamp());

            // Validate IAM info
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
            assertEquals("1234567890", company.getIAMRealmId());

            Contact contact = company.getContactByRoleCode(ContactRole.PayrollAdmin);
            assertEquals("09876", contact.getIAMAuthenticationId());

            contact = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
            assertEquals("54321", contact.getIAMAuthenticationId());
            PayrollServices.rollbackUnitOfWork();

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
