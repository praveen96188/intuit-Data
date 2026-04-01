package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.PaymentMethodAgencyIdRequirementsDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.UpdateCompanyAgencyIdRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.UpdateCompanyAgencyIdResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexUnitDataLoaderService;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyAgencyPaymentTemplateAgencyId;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.iam.HeaderUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.PayrollSubmitTaxTests;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.mockito.Mockito.mockStatic;

/**
 * $Author: DWeinberg $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPRefundEmployerFinancialTransactionTests.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/23 15:00:48 $
 * $Author: DWeinberg $
 */
public class PSPUpdateCompanyAgencyIdTests {

    public static String aid = "1234567-1";
    public static String paymentTemplateCd = "AZ-UC018-PAYMENT";
    public static String noteToAttach = "Note";
    String psid;
    public static PayrollSubmitTaxTests payrollSubmitTaxTests = new PayrollSubmitTaxTests();
    MockedStatic headerUtilsMockedStatic = mockStatic(HeaderUtils.class);

    @Before
    public void before() {

        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        payrollSubmitTaxTests.runBeforeEachTest();

        try {
            payrollSubmitTaxTests.testAZStateThreshold941Payments_Over100K_State();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        psid = companies.get(0).getSourceCompanyId();
        PayrollServices.rollbackUnitOfWork();

        FlexUnitDataLoaderService.AddUsers();

        headerUtilsMockedStatic.when(HeaderUtils::isOfflineTicket).thenReturn(true);
    }

    @After
    public void afterEach() {
        headerUtilsMockedStatic.close();
    }

    @AfterClass
    public static void afterClass() {
        payrollSubmitTaxTests.afterClass();
    }

    @After
    public void runAfterEachTest() {
        payrollSubmitTaxTests.runAfterEachTest();
    }

    @Test
    public void testHappyPathUpdateAID() {
        try {
            SAPUser adminUser = TestHelper.loginAdminUser();
            RequestAttributesUtils.setAttribute("CorpId",adminUser.getCorpId());
            UpdateCompanyAgencyIdRequestDISDTO request = getUpdateCompanyAgencyIdRequest(adminUser);
            UpdateCompanyAgencyIdResponseDISDTO responseDISDTO = performAgencyIdUpdate(request);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());

            verifyAID(paymentTemplateCd,aid);
            TestCase.assertEquals(1,
                                  responseDISDTO.getPaymentMethodAgencyIdRequirements().size());
            TestCase.assertEquals(String.format(PaymentMethodAgencyIdRequirementsDISDTO.REQUIREMENTS_DESCRIPTION_STRING,"ACHCredit"),
                                  responseDISDTO.getPaymentMethodAgencyIdRequirements().get(0).getRequirementsDescription());
            TestCase.assertEquals(1,
                                  responseDISDTO.getPaymentMethodAgencyIdRequirements().get(0).getAgencyIdRequirements().size());
            TestCase.assertTrue(responseDISDTO.getPaymentMethodAgencyIdRequirements().get(0).getAgencyIdRequirements().get(0).isFulfilled());

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(companies.get(0), EventTypeCode.StateIdModified);
            TestCase.assertEquals(1,companyEvents.size());
            TestCase.assertEquals(1,companyEvents.get(0).getCompanyNoteCollection().size());
            TestCase.assertEquals(noteToAttach, companyEvents.get(0).getCompanyNoteCollection().get(0).getNotes());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testRequirementNotMet() {
        try {
            SAPUser adminUser = TestHelper.loginAdminUser();
            RequestAttributesUtils.setAttribute("CorpId",adminUser.getCorpId());
            UpdateCompanyAgencyIdRequestDISDTO request = getUpdateCompanyAgencyIdRequest(adminUser);
            String unsupportedACHCreditAID = "234";
            request.setAgencyId(unsupportedACHCreditAID);
            UpdateCompanyAgencyIdResponseDISDTO responseDISDTO = performAgencyIdUpdate(request);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());

            verifyAID(paymentTemplateCd,unsupportedACHCreditAID);
            TestCase.assertEquals(1,
                                  responseDISDTO.getPaymentMethodAgencyIdRequirements().size());
            TestCase.assertEquals(String.format(PaymentMethodAgencyIdRequirementsDISDTO.REQUIREMENTS_DESCRIPTION_STRING,"ACHCredit"),
                                  responseDISDTO.getPaymentMethodAgencyIdRequirements().get(0).getRequirementsDescription());
            TestCase.assertEquals(1,
                                  responseDISDTO.getPaymentMethodAgencyIdRequirements().get(0).getAgencyIdRequirements().size());
            TestCase.assertFalse(responseDISDTO.getPaymentMethodAgencyIdRequirements().get(0).getAgencyIdRequirements().get(0).isFulfilled());

        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    private void verifyAID(String pPaymentTemplateCd, String pAid) throws Throwable {
        TaxAdapter taxAdapter = new TaxAdapter();
        List<SAPCompanyAgencyPaymentTemplateAgencyId> currentSAPCompanyAgencyPaymentTemplateAgencyIds =
                taxAdapter.findAgencyIDs(
                        SourceSystemEnum.QBDT.toString(),
                        psid,
                        pPaymentTemplateCd);
        TestCase.assertEquals(pAid,currentSAPCompanyAgencyPaymentTemplateAgencyIds.get(0).getId());
    }

    @Test
    public void testUserSessionTimedOut() {
        if((FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_DISPSPUSERAUTH_HANDLER, true)))
            return;
        try {
            SAPUser sapAdminUser = TestHelper.loginAdminUser();
            RequestAttributesUtils.setAttribute("CorpId",sapAdminUser.getCorpId());

            PayrollServices.beginUnitOfWork();
            SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
            spcfCalendar.addDays(3);
            PSPDate.setPSPTime(spcfCalendar);
            PayrollServices.commitUnitOfWork();

            UpdateCompanyAgencyIdRequestDISDTO request = getUpdateCompanyAgencyIdRequest(sapAdminUser);
            UpdateCompanyAgencyIdResponseDISDTO responseDISDTO = performAgencyIdUpdate(request);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testUserDoesNotHavePermissions() {
        if((FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_DISPSPUSERAUTH_HANDLER, true)))
            return;
        try {
            SAPUser sapSalesUser = TestHelper.loginSalesUser();
            RequestAttributesUtils.setAttribute("CorpId",sapSalesUser.getCorpId());
            UpdateCompanyAgencyIdRequestDISDTO request = getUpdateCompanyAgencyIdRequest(sapSalesUser);
            UpdateCompanyAgencyIdResponseDISDTO responseDISDTO = performAgencyIdUpdate(request);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testNoCompanyFound() {
        try {
            String sourceCoIdDNE = "companyDNE";
            SAPUser sapAdminUser = TestHelper.loginAdminUser();
            RequestAttributesUtils.setAttribute("CorpId",sapAdminUser.getCorpId());
            UpdateCompanyAgencyIdRequestDISDTO request = getUpdateCompanyAgencyIdRequest(sapAdminUser);
            request.setSourceCompanyId(sourceCoIdDNE);

            DISAdapter disAdapter = new DISAdapter();
            UpdateCompanyAgencyIdResponseDISDTO responseDISDTO = disAdapter.Update_CompanyAgencyId(request);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    // Helper Methods
    private UpdateCompanyAgencyIdResponseDISDTO performAgencyIdUpdate(UpdateCompanyAgencyIdRequestDISDTO pRequest) {
        UpdateCompanyAgencyIdResponseDISDTO responseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            responseDISDTO = disAdapter.Update_CompanyAgencyId(pRequest);
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail(t.getMessage());
        }
        return responseDISDTO;
    }

    private UpdateCompanyAgencyIdRequestDISDTO getUpdateCompanyAgencyIdRequest(SAPUser pSAPUser) {
        return getUpdateCompanyAgencyIdRequest(pSAPUser, aid, paymentTemplateCd, noteToAttach);
    }

    private UpdateCompanyAgencyIdRequestDISDTO getUpdateCompanyAgencyIdRequest(SAPUser pSAPUser, String pAID, String pPaymentTemplateCd, String pNoteToAttachEvent) {

        UpdateCompanyAgencyIdRequestDISDTO requestDISDTO = new UpdateCompanyAgencyIdRequestDISDTO();
        requestDISDTO.setSourceCompanyId(psid);
        requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
        requestDISDTO.setToken(pSAPUser.getAuthorizationToken());
        requestDISDTO.setCorpId(pSAPUser.getCorpId());
        requestDISDTO.setAgencyId(pAID);
        requestDISDTO.setPaymentTemplateCd(pPaymentTemplateCd);
        requestDISDTO.setNoteToAttachToEvent(pNoteToAttachEvent);
        return requestDISDTO;
    }

}
