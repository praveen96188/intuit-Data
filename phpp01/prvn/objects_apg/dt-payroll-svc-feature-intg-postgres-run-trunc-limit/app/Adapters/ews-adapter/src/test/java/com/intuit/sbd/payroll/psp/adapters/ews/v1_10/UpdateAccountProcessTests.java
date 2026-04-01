package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsCompanyResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResponseStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsUpdateAccount;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsUpdateAccountResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsDeliveryType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.UpdateAccountProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.QuickbooksInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.UpdateQBCompanyInfoCore;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.sun.tools.xjc.generator.bean.ImplStructureStrategy;
import org.junit.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
            PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                    TransmissionType.UpdateAccount), "v1_10/test_updateAcount_CloudOnly.xml",
                    Arrays.asList("EIN", "DateTimeStamp", "PSID"));
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
            assertEquals("9130360425658956", company.getIAMRealmId());

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
            assertEquals("9130360425658956", company.getIAMRealmId());

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
    public void testAlphaBetaRatableLicenseUpdate(){
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateAccountCloudOnly(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            UpdateAccountProcess process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            //check if valid update is done when major version is updated for R version (not alpha/beta upgrade)
            Expression<QuickbooksInfo> query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                                                          .Where(QuickbooksInfo.Company().equalTo(company));
            List result =  Application.executeQuery(QuickbooksInfo.class,query);

            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("18.00.R.9",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-477",value[1].toString());
            }

            //Should update the Major version and license number in case of Alpha/beta
            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createAlphaBetaEwsCreateAccount(psid, "19.00.A.9/20716#professional", "6487-4844-4441-479");
            Application.commitUnitOfWork();

            process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();


            //check if valid update is done when major version is updated for R version (not alpha/beta upgrade)
             query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                                                          .Where(QuickbooksInfo.Company().equalTo(company));
             result =  Application.executeQuery(QuickbooksInfo.class,query);

            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("19.00.A.9",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-479",value[1].toString());
            }

            //Should not update the Major version and license number in case of Alpha/beta
            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createAlphaBetaEwsCreateAccount(psid,"19.00.R.9/20716#professional","6487-4844-4441-479");
            Application.commitUnitOfWork();

            process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            //check if valid update is done when major version is updated for R version (not alpha/beta upgrade)
            query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                               .Where(QuickbooksInfo.Company().equalTo(company));
            result =  Application.executeQuery(QuickbooksInfo.class,query);

            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("19.00.R.9",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-479",value[1].toString());
            }


        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testMinorVersionRatableLicenseUpdate() throws Exception{
        try{
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();

            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createEwsUpdateAccountCloudOnly(company.getSourceCompanyId());
            Application.commitUnitOfWork();

            UpdateAccountProcess process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            Expression<QuickbooksInfo> query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                                                          .Where(QuickbooksInfo.Company().equalTo(company));
            List result =  Application.executeQuery(QuickbooksInfo.class,query);

            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("18.00.R.9",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-477",value[1].toString());
            }

            Application.beginUnitOfWork();
           company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createAlphaBetaEwsCreateAccount(psid, "25.00.R.8/20716#professional", "6487-4844-4441-479");
            Application.commitUnitOfWork();

            process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            //check if valid update is done when major version is upgraded
            query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                               .Where(QuickbooksInfo.Company().equalTo(company));
            result =  Application.executeQuery(QuickbooksInfo.class,query);

            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("25.00.R.8",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-479",value[1].toString());
            }

            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createAlphaBetaEwsCreateAccount(psid, "25.00.R.6/20716#professional", "6487-4844-4441-479");
            Application.commitUnitOfWork();

            process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            //check if valid update is done when minor version is downgraded
            query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                               .Where(QuickbooksInfo.Company().equalTo(company));
            result =  Application.executeQuery(QuickbooksInfo.class, query);
            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("25.00.R.6",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-479",value[1].toString());
            }
            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createAlphaBetaEwsCreateAccount(psid, "25.00.R.9/20716#professional", "6487-4844-4441-479");
            Application.commitUnitOfWork();

            process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            //check if valid update is done when minor version is upgraded
            query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                               .Where(QuickbooksInfo.Company().equalTo(company));
            result =  Application.executeQuery(QuickbooksInfo.class,query);

            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("25.00.R.9",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-479",value[1].toString());
            }

            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createAlphaBetaEwsCreateAccount(psid, "24.00.R.9/20716#professional", "6487-4844-4441-469");
            Application.commitUnitOfWork();

            process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            //Should not update when major version is downgraded
            query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                               .Where(QuickbooksInfo.Company().equalTo(company));
            result =  Application.executeQuery(QuickbooksInfo.class,query);

            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("25.00.R.9",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-479",value[1].toString());
            }

            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createAlphaBetaEwsCreateAccount(psid, "26.00.R.9/20716#professional", "6487-4844-4441-470");
            Application.commitUnitOfWork();

            process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            //check if valid update is done when major version is upgraded
            query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                               .Where(QuickbooksInfo.Company().equalTo(company));
            result =  Application.executeQuery(QuickbooksInfo.class,query);

            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("26.00.R.9",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-470",value[1].toString());
            }


        }catch (Exception e){
            fail(e.getMessage());
        }
    }


    @Test
    public void testAlphaBetaDirectUpdatetoNonrelease(){
        try {
            mCreateAccountProcessTests.createAccountCloudOnly();
            psid = mCreateAccountProcessTests.getPSID();


            Application.beginUnitOfWork();
            Company company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createAlphaBetaEwsCreateAccount(psid, "19.00.A.9/20716#professional", "6487-4844-4441-479");
            Application.commitUnitOfWork();

            UpdateAccountProcess process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();


            //check if valid update is done when major version is updated for R version (not alpha/beta upgrade)
            Expression<QuickbooksInfo> query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber());
            List result =  Application.executeQuery(QuickbooksInfo.class,query);

            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("19.00.A.9",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-479",value[1].toString());
            }

            //Should not update the Major version and license number in case of Alpha/beta
            Application.beginUnitOfWork();
            company = PspFactory.findCompany(psid);
            mRequest = TestDataFactory.createAlphaBetaEwsCreateAccount(psid,"19.00.R.9/20716#professional","6487-4844-4441-479");
            Application.commitUnitOfWork();

            process = new UpdateAccountProcess(mRequest);
            mResponse = process.execute();

            //check if valid update is done when major version is updated for R version (not alpha/beta upgrade)
            query = new Query<QuickbooksInfo>().Select(QuickbooksInfo.ApplicationVersion(), QuickbooksInfo.LicenseNumber())
                                               .Where(QuickbooksInfo.Company().equalTo(company));
            result =  Application.executeQuery(QuickbooksInfo.class, query);


            if(!result.isEmpty() && result.get(0) != null){
                Object[] value = (Object []) result.get(0);

                assertNotNull(value[0]);
                assertEquals("19.00.R.9",value[0].toString());

                assertNotNull(value[1]);
                assertEquals("6487-4844-4441-479",value[1].toString());
            }


        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCanUpdateLicenseNumberDBBelIncomingBelacct(){
        QuickbooksInfo quickbooksInfo = null;
        QuickbooksInfoDTO quickbooksInfoDTO = null;

        //ALPHA BETA TESTS
        // Test new subscription for alpha beta
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("19.00.A.9");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfoDTO.setQuickbooksSku("professional");
        assertTrue("Update should go through for a new connection for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));
        //Same major version, same sku, diff minor version for alpha beta
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.A.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("professional");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.A.9");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfoDTO.setQuickbooksSku("professional");
        assertTrue("Update should go through for Same major version, same sku, diff minor version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo,quickbooksInfoDTO));
        //Same major version, same sku, diff minor version for bel Sku alpha beta
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.A.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.A.12");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for Same major version, same bel sku, diff minor version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo,quickbooksInfoDTO));
        //Higher major version incoming and lower major version in DB
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.A.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("26.00.A.12");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-470");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for higher major version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo,quickbooksInfoDTO));
        //downgrade of major version for Alpha beta should  update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.A.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("24.00.A.12");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-470");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for downgrade of major version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));

        //Release incoming & alpha/beta in db
        // Test new subscription for alpha beta
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.A.9");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfoDTO.setQuickbooksSku("professional");
        assertTrue("Update should go through for a new connection for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));
        //Same major version, same sku, diff minor version for alpha beta in DB and release incoming
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.A.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("professional");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.9");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-469");
        quickbooksInfoDTO.setQuickbooksSku("professional");
        assertTrue("Update should go through for Same major version, same sku, for alpha beta in DB and release incoming", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo,quickbooksInfoDTO));
        //Same major version, same sku, diff minor version for bel Sku alpha beta in DB and Release incoming
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.A.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.12");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-469");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for Same major version, same bel sku, for alpha beta in DB and release incoming", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo,quickbooksInfoDTO));
        //Higher major version incoming and lower major version in DB
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.A.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("26.00.R.12");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-470");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for higher major version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo,quickbooksInfoDTO));
        //downgrade of major version should  update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.A.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("24.00.R.12");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-470");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for downgrade of major version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));

        //Release version tests
        // Test new subscription for release
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.9");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfoDTO.setQuickbooksSku("professional");
        assertTrue("Update should go through for a new connection for release", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));

        //Higher major version, same sku should  update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("26.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-579");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for Same major version, same sku, diff minor version for release", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo,quickbooksInfoDTO));

        //Equal major version , same sku -  bel should  update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for Same major version, same sku, diff minor version for release", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo,quickbooksInfoDTO));

        //Equal major version , in DB sku belacct , incoming sku bel should  update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("belacct");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-579");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for Same major version, belacct sku in DB and bel Sku incoming, diff minor version for release", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo,quickbooksInfoDTO));

        //Equal major version ,incoming sku belacct , in DB bel sku should not update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-579");
        quickbooksInfoDTO.setQuickbooksSku("belacct");
        assertFalse("Update should not go through for Same major version, bel sku in DB and belacct Sku incoming, diff minor version for release", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));

        //Equal major version ,with different license number this should not update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-579");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertFalse("Update should not  go through for Same major version, bel sku in DB and belacct Sku incoming, diff minor version for release", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));

        //Lower incoming major version ,in DB sku belacct , incoming bel sku should  update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("26.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("belacct");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-579");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertTrue("Update should go through for Lower incoming major version, same sku, diff minor version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));

        //Lower incoming major version ,in DB sku bel , incoming bel sku should not update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("26.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-579");
        quickbooksInfoDTO.setQuickbooksSku("bel");
        assertFalse("Update should not go through for Lower incoming major version, same sku, diff minor version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));

        //Lower incoming major version ,in DB sku bel , incoming belacct sku should not update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("26.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-579");
        quickbooksInfoDTO.setQuickbooksSku("belacct");
        assertFalse("Update should not go through for Lower incoming major version, same sku, diff minor version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));

        //Lower incoming major version ,in DB sku belacct , incoming belacct sku should not update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("26.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("belacct");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-579");
        quickbooksInfoDTO.setQuickbooksSku("belacct");
        assertFalse("Update should not go through for Lower incoming major version, same sku, diff minor version for alpha beta", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));

        //Higher major version ,incoming Sku is null ,it should not update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("26.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-579");
        quickbooksInfoDTO.setQuickbooksSku(null);
        assertTrue("Update should  go through for higher incoming major version", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));


        //Same major version ,incoming Sku is null ,it should not update the license
        quickbooksInfo = new QuickbooksInfo();
        quickbooksInfo.setApplicationVersion("25.00.R.10");
        quickbooksInfo.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfo.setQuickbooksSku("bel");
        quickbooksInfoDTO = new QuickbooksInfoDTO();
        quickbooksInfoDTO.setApplicationVersion("25.00.R.4");
        quickbooksInfoDTO.setLicenseNumber("6487-4844-4441-479");
        quickbooksInfoDTO.setQuickbooksSku(null);
        assertFalse("Update should not  go through for same major version wih incoming Sku being null", UpdateQBCompanyInfoCore.canUpdateLicenseNumber(quickbooksInfo, quickbooksInfoDTO));




    }

}
