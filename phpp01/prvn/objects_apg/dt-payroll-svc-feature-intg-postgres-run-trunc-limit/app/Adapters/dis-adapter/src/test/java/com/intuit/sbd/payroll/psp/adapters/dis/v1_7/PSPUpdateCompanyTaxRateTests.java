package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.TaxRateUpdateDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.UpdateCompanyTaxRateRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.UpdateCompanyTaxRateResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexUnitDataLoaderService;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * $Author: DWeinberg $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPRefundEmployerFinancialTransactionTests.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/23 15:00:48 $
 * $Author: DWeinberg $
 */
public class PSPUpdateCompanyTaxRateTests {

    private String psid = "123456789";
    private Company company;

    public static String NOTE_TO_ATTACH = "Note";
    public static Calendar effectiveDate;

    public static List<TaxRateUpdateDISDTO> taxRates = null;

    static {
        taxRates = new ArrayList<TaxRateUpdateDISDTO>();
        TaxRateUpdateDISDTO taxRateUpdateDISDTO = new TaxRateUpdateDISDTO();
        taxRates.add(taxRateUpdateDISDTO);

        taxRateUpdateDISDTO.setLawId(new Integer(87));
        taxRateUpdateDISDTO.setRate(new BigDecimal(11));
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        ProcessResult processResult;
        effectiveDate = CalendarUtils.convertToCalendar(CalendarUtils.getFirstDayOfQuarter(2013,2));

        company = DataLoadServices.setupAssistedCompanyForCA("606001967", 1, true);
        psid = company.getSourceCompanyId();

        FlexUnitDataLoaderService.AddUsers();
        DataLoadServices.setPSPDate(2013, 4, 10);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPathUpdateTaxRate() {
        try {
            SAPUser sapSalesUser = TestHelper.loginAdminUser();
            UpdateCompanyTaxRateRequestDISDTO request = getUpdateCompanyTaxRateRequest(sapSalesUser);
            UpdateCompanyTaxRateResponseDISDTO responseDISDTO = performTaxRateUpdate(request);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(psid, SourceSystemCode.QBDT);
            CompanyLaw caSuiLaw = CompanyLaw.findCompanyLaw(company, "87");
            double currentRate = caSuiLaw.getCompanyLawRateCollection().get(0).getRate()*100;
            TestCase.assertEquals(taxRates.get(0).getRate().doubleValue(),currentRate);
            DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(companies.get(0), EventTypeCode.CompanyLawUpdated);
            TestCase.assertEquals(1, companyEvents.size());
            TestCase.assertEquals(1, companyEvents.get(0).getCompanyNoteCollection().size());
            TestCase.assertEquals(NOTE_TO_ATTACH, companyEvents.get(0).getCompanyNoteCollection().get(0).getNotes());

            PayrollServices.rollbackUnitOfWork();
        } catch (Throwable pThrowable) {
            PayrollServices.rollbackUnitOfWork();
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testUserSessionTimedOut() {
        try {
            SAPUser sapAdminUser = TestHelper.loginAdminUser();

            PayrollServices.beginUnitOfWork();
            SpcfCalendar spcfCalendar = PSPDate.getPSPTime();// SpcfCalendar.getNow();
            spcfCalendar.addDays(3);
            PSPDate.setPSPTime(spcfCalendar);
            PayrollServices.commitUnitOfWork();

            UpdateCompanyTaxRateRequestDISDTO request = getUpdateCompanyTaxRateRequest(sapAdminUser);
            UpdateCompanyTaxRateResponseDISDTO responseDISDTO = performTaxRateUpdate(request);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testUserDoesNotHavePermissions() {
        try {
            SAPUser sapSalesUser = TestHelper.loginSalesUser();
            UpdateCompanyTaxRateRequestDISDTO request = getUpdateCompanyTaxRateRequest(sapSalesUser);
            UpdateCompanyTaxRateResponseDISDTO responseDISDTO = performTaxRateUpdate(request);
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
            UpdateCompanyTaxRateRequestDISDTO request = getUpdateCompanyTaxRateRequest(sapAdminUser);
            request.setSourceCompanyId(sourceCoIdDNE);

            DISAdapter disAdapter = new DISAdapter();
            UpdateCompanyTaxRateResponseDISDTO responseDISDTO = disAdapter.Update_CompanyTaxRate(request);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    //Helper Methods
    private UpdateCompanyTaxRateResponseDISDTO performTaxRateUpdate(UpdateCompanyTaxRateRequestDISDTO pRequest) {
        UpdateCompanyTaxRateResponseDISDTO responseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            responseDISDTO = disAdapter.Update_CompanyTaxRate(pRequest);
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail(t.getMessage());
        }
        return responseDISDTO;
    }

    private UpdateCompanyTaxRateRequestDISDTO getUpdateCompanyTaxRateRequest(SAPUser pSAPUser) {
        return getUpdateCompanyTaxRateRequest(pSAPUser, taxRates, NOTE_TO_ATTACH,effectiveDate);
    }

    private UpdateCompanyTaxRateRequestDISDTO getUpdateCompanyTaxRateRequest(
            SAPUser pSAPUser,
            List<TaxRateUpdateDISDTO> pTaxRates,
            String pNoteToAttachEvent,
            Calendar pEffectiveDate
    ) {

        UpdateCompanyTaxRateRequestDISDTO requestDISDTO = new UpdateCompanyTaxRateRequestDISDTO();
        requestDISDTO.setSourceCompanyId(psid);
        requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
        requestDISDTO.setToken(pSAPUser.getAuthorizationToken());
        requestDISDTO.setCorpId(pSAPUser.getCorpId());

        requestDISDTO.setTaxRates(pTaxRates);
        requestDISDTO.setNoteToAttachToEvent(pNoteToAttachEvent);
        requestDISDTO.setEffectiveDate(pEffectiveDate);
        requestDISDTO.setOverrideBlackout(false);
        requestDISDTO.setPushToQuickbooks(true);
        requestDISDTO.setSupportRatesOutsideBoundaries(false);
        return requestDISDTO;
    }

}
