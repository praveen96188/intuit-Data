package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.UpdateCompanyAgencyIdRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.UpdateCompanyFilingFrequencyRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.UpdateCompanyAgencyIdResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.UpdateCompanyFilingFrequencyResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexUnitDataLoaderService;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * $Author: DWeinberg $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPRefundEmployerFinancialTransactionTests.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/23 15:00:48 $
 * $Author: DWeinberg $
 */
public class PSPUpdateCompanyFilingFrequencyTests {

    private String psid = "123456789";
    private PayrollRun payrollRun1 = null;
    private PayrollRun payrollRun2 = null;
    private Company company;
    private String fnTxToRefund;

    public static String AID = "123";
    public static String LAW_ID = "DE123";
    public static String NOTE_TO_ATTACH = "Note";

    @Before
    public void loadDataHappyPath() {

        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        // Load the test users, as refund requires permissions.
        FlexUnitDataLoaderService.AddUsers();

        company = DISCompanyDataloader.setupCompany();

        psid = company.getSourceCompanyId();
        payrollRun1 = DISCompanyDataloader.loadPayroll(psid, "20110105");
        DataLoadServices.runOffload();
        DataLoadServices.runACHTransactionProcessor(5);
        payrollRun2 = DISCompanyDataloader.loadPayroll(psid, "20110112");

    }

    @Test
    public void testHappyPathUpdateFilingFrequency() {
        try {
            SAPUser sapSalesUser = TestHelper.loginAdminUser();
            UpdateCompanyFilingFrequencyRequestDISDTO request = getUpdateCompanyFilingFrequencyRequest(sapSalesUser, true, NOTE_TO_ATTACH);
            UpdateCompanyFilingFrequencyResponseDISDTO responseDISDTO = performFilingFrequencyUpdate(request);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testUserSessionTimedOut() {
        try {
            SAPUser sapAdminUser = TestHelper.loginAdminUser();

            PayrollServices.beginUnitOfWork();
            SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
            spcfCalendar.addDays(3);
            PSPDate.setPSPTime(spcfCalendar);
            PayrollServices.commitUnitOfWork();

            UpdateCompanyFilingFrequencyRequestDISDTO request = getUpdateCompanyFilingFrequencyRequest(sapAdminUser, true, NOTE_TO_ATTACH);
            UpdateCompanyFilingFrequencyResponseDISDTO responseDISDTO = performFilingFrequencyUpdate(request);
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
            UpdateCompanyFilingFrequencyRequestDISDTO request = getUpdateCompanyFilingFrequencyRequest(sapSalesUser, true, NOTE_TO_ATTACH);
            UpdateCompanyFilingFrequencyResponseDISDTO responseDISDTO = performFilingFrequencyUpdate(request);
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
            UpdateCompanyFilingFrequencyRequestDISDTO request = getUpdateCompanyFilingFrequencyRequest(sapAdminUser, true, NOTE_TO_ATTACH);
            request.setSourceCompanyId(sourceCoIdDNE);

            DISAdapter disAdapter = new DISAdapter();
            UpdateCompanyFilingFrequencyResponseDISDTO responseDISDTO = disAdapter.Update_CompanyFilingFrequency(request);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    // Helper Methods
    private UpdateCompanyFilingFrequencyResponseDISDTO performFilingFrequencyUpdate(UpdateCompanyFilingFrequencyRequestDISDTO pRequest) {
        UpdateCompanyFilingFrequencyResponseDISDTO responseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            responseDISDTO = disAdapter.Update_CompanyFilingFrequency(pRequest);
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail(t.getMessage());
        }
        return responseDISDTO;
    }

    private UpdateCompanyFilingFrequencyRequestDISDTO getUpdateCompanyFilingFrequencyRequest(SAPUser pSAPUser, Boolean pFileType944, String pNoteToAttachEvent) {

        UpdateCompanyFilingFrequencyRequestDISDTO requestDISDTO = new UpdateCompanyFilingFrequencyRequestDISDTO();
        requestDISDTO.setSourceCompanyId(psid);
        requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
        requestDISDTO.setToken(pSAPUser.getAuthorizationToken());
        requestDISDTO.setCorpId(pSAPUser.getCorpId());
        requestDISDTO.setFileType944(pFileType944);
        requestDISDTO.setNoteToAttachTEvent(pNoteToAttachEvent);
        return requestDISDTO;
    }

}
