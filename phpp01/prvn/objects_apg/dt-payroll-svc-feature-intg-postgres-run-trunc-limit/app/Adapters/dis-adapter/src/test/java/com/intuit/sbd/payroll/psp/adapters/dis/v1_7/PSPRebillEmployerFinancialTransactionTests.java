package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.RebillEmployerFinancialTransactionRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.RebillEmployerFinancialTransactionResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexUnitDataLoaderService;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollTransaction;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * $Author: DWeinberg $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPRebillEmployerFinancialTransactionTests.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/23 15:00:48 $
 * $Author: DWeinberg $
 */
public class PSPRebillEmployerFinancialTransactionTests {

    private String psid = "123456789";
    private PayrollRun payrollRun1 = null;
    private PayrollRun payrollRun2 = null;
    private Company company;
    private String fnTxToRebill;
    private BigDecimal originalFnTxAmt;
    private BigDecimal perUnitRebillAmount = new BigDecimal(1.00);
    private SpcfDecimal perUnitRebillAmountSpcfDecimal = SpcfDecimal.createInstance(perUnitRebillAmount.doubleValue());

    @Before
    public void loadDataHappyPath() {

        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        // Load the test users, as Rebill requires permissions.
        FlexUnitDataLoaderService.AddUsers();

        company = DISCompanyDataloader.setupCompany();

        psid = company.getSourceCompanyId();
        payrollRun1 = DISCompanyDataloader.loadPayroll(psid, "20110105");
        DataLoadServices.runOffload();
        DataLoadServices.runACHTransactionProcessor(5);
        payrollRun2 = DISCompanyDataloader.loadPayroll(psid, "20110112");

    }

    @Test
    public void testRebillHappyPath() {
        try {
            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRebill();
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());

            PayrollServices.beginUnitOfWork();

            BillingDetail billingDetail = Application.findById(BillingDetail.class, SpcfUniqueId.createInstance(responseDISDTO.getFeeBillingDetailId()));

            TestCase.assertEquals(perUnitRebillAmountSpcfDecimal.getFractionalPart(),billingDetail.getPretaxAmount().getFractionalPart());
            TestCase.assertEquals(perUnitRebillAmountSpcfDecimal.getIntegerPart(), billingDetail.getPretaxAmount().getIntegerPart());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testRebillNonIntegerDivisibleAmount() {
        try {
            RebillEmployerFinancialTransactionRequestDISDTO requestDISDTO = getHappyPathRebillRequest();
            BigDecimal nonIntegerDivisibleUnitRebillAmount = new BigDecimal(1.11);

            requestDISDTO.setRebillAmount(nonIntegerDivisibleUnitRebillAmount);
            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRebill(requestDISDTO);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());

            PayrollServices.beginUnitOfWork();

            BillingDetail billingDetail = Application.findById(BillingDetail.class, SpcfUniqueId.createInstance(responseDISDTO.getFeeBillingDetailId()));

            TestCase.assertEquals(1, billingDetail.getPretaxAmount().getIntegerPart());
            // PSP Rounds up, so 1.11 for two transactions will be .555 which is rounded up resulting in total refund of 1.12
            TestCase.assertEquals(12,billingDetail.getPretaxAmount().getFractionalPart());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testUserSessionTimedOut() {
        try {
            SAPUser sapSalesUser = TestHelper.loginAdminUser();

            PayrollServices.beginUnitOfWork();
            SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
            spcfCalendar.addDays(3);
            PSPDate.setPSPTime(spcfCalendar);
            PayrollServices.commitUnitOfWork();

            FinancialTransaction fnTxToRebill = getHappyPathFinancialTransactionToRebill();
            RebillEmployerFinancialTransactionRequestDISDTO request = getRebillRequest(fnTxToRebill, sapSalesUser);
            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRebill(request);
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
            FinancialTransaction fnTxToRebill = getHappyPathFinancialTransactionToRebill();
            RebillEmployerFinancialTransactionRequestDISDTO request = getRebillRequest(fnTxToRebill, sapSalesUser);
            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRebill(request);
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
            RebillEmployerFinancialTransactionRequestDISDTO requestDISDTO = getHappyPathRebillRequest();
            requestDISDTO.setSourceCompanyId(sourceCoIdDNE);

            DISAdapter disAdapter = new DISAdapter();
            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = disAdapter.Update_RebillEmployerFinancialTransaction(requestDISDTO);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testCompanyOnHold() {
        try {
            DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.FraudReview);
            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRebill();
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testRetrieveEmployerFinancialTransactionDNE() {
        try {
            RebillEmployerFinancialTransactionRequestDISDTO requestDISDTO = getHappyPathRebillRequest();
            requestDISDTO.setFinancialTransactionId("Invalid_Fn_Tx_id");

            DISAdapter disAdapter = new DISAdapter();
            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = disAdapter.Update_RebillEmployerFinancialTransaction(requestDISDTO);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testRetrieveEmployerFinancialTransactionNonRebillableState() {
        try {
            SpcfCalendar beforeSpcfCal = payrollRun2.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(beforeSpcfCal, -2);
            SpcfCalendar afterSpcfCal = payrollRun2.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(afterSpcfCal, 2);
            PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
            ArrayList<SAPPayrollTransaction> payroll1FnTxns = payrollRunAdapter.findEmployerTransactions(
                    psid,
                    SourceSystemCode.QBDT.toString(),
                    payrollRun2.getSourcePayRunId(),
                    CalendarUtils.convertToDate(beforeSpcfCal),
                    CalendarUtils.convertToDate(afterSpcfCal)
            );

            DomainEntitySet<FinancialTransaction> erDebitFnTxns = FinancialTransaction.findFinancialTransactions(
                    company,
                    TransactionTypeCode.EmployerFeeDebit,
                    TransactionStateCode.Created);

            TestCase.assertEquals(1, erDebitFnTxns.size());

            FinancialTransaction pFinancialTransactionToRebill = erDebitFnTxns.get(0);
            RebillEmployerFinancialTransactionRequestDISDTO requestDISDTO = new RebillEmployerFinancialTransactionRequestDISDTO();
            requestDISDTO.setSourceCompanyId(psid);
            requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
            requestDISDTO.setToken("");
            fnTxToRebill = pFinancialTransactionToRebill.getId().toString();
            requestDISDTO.setFinancialTransactionId(fnTxToRebill);
            requestDISDTO.setRebillAmount(SpcfUtils.convertToBigDecimal(pFinancialTransactionToRebill.getFinancialTransactionAmount()));

            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRebill(requestDISDTO);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testAlreadyRebilled() {
        try {
            FinancialTransaction fnTxToRebill = getHappyPathFinancialTransactionToRebill();
            RebillEmployerFinancialTransactionRequestDISDTO request = getRebillRequest(fnTxToRebill);
            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRebill(request);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
            responseDISDTO = performRebill(request);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testRebillPartialAmount() {
        // NOTE: SAP currently takes a Rebill amount, but it is ignored and the full amount
        //    is Rebilled.
        try {
            RebillEmployerFinancialTransactionRequestDISDTO requestDISDTO = getHappyPathRebillRequest();
            requestDISDTO.setRebillAmount(new BigDecimal(1));

            DISAdapter disAdapter = new DISAdapter();
            RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = disAdapter.Update_RebillEmployerFinancialTransaction(requestDISDTO);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    // Helper Methods
    public FinancialTransaction getHappyPathFinancialTransactionToRebill() throws Throwable {
        SpcfCalendar beforeSpcfCal = payrollRun1.getPaycheckDate().copy();
        CalendarUtils.addBusinessDays(beforeSpcfCal, -2);
        SpcfCalendar afterSpcfCal = payrollRun1.getPaycheckDate().copy();
        CalendarUtils.addBusinessDays(afterSpcfCal, 2);
        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
        ArrayList<SAPPayrollTransaction> payroll1FnTxns = payrollRunAdapter.findEmployerTransactions(
                psid,
                SourceSystemCode.QBDT.toString(),
                payrollRun1.getSourcePayRunId(),
                CalendarUtils.convertToDate(beforeSpcfCal),
                CalendarUtils.convertToDate(afterSpcfCal)
        );


        DomainEntitySet<FinancialTransaction> erDebitFnTxns = FinancialTransaction.findFinancialTransactions(
                company,
                TransactionTypeCode.EmployerFeeDebit,
                TransactionStateCode.Completed);

        TestCase.assertEquals(2, erDebitFnTxns.size());

        return erDebitFnTxns.get(0);
    }

    public RebillEmployerFinancialTransactionResponseDISDTO performRebill() throws Throwable {
        RebillEmployerFinancialTransactionRequestDISDTO requestDISDTO = getHappyPathRebillRequest();
        return performRebill(requestDISDTO);
    }

    public RebillEmployerFinancialTransactionResponseDISDTO performRebill(RebillEmployerFinancialTransactionRequestDISDTO pRequestDISDTO) {

        RebillEmployerFinancialTransactionResponseDISDTO responseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            responseDISDTO = disAdapter.Update_RebillEmployerFinancialTransaction(pRequestDISDTO);

        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail(t.getMessage());
        }
        return responseDISDTO;
    }

    private RebillEmployerFinancialTransactionRequestDISDTO getHappyPathRebillRequest() throws Throwable {
        FinancialTransaction financialTransactionToRebill = getHappyPathFinancialTransactionToRebill();
        return getRebillRequest(financialTransactionToRebill);
    }

    private RebillEmployerFinancialTransactionRequestDISDTO getRebillRequest(FinancialTransaction pFinancialTransactionToRebill) throws Throwable {
        SAPUser sapUser = TestHelper.loginAdminUser();
        return getRebillRequest(pFinancialTransactionToRebill, sapUser);
    }

    private RebillEmployerFinancialTransactionRequestDISDTO getRebillRequest(FinancialTransaction pFinancialTransactionToRebill,SAPUser pSAPUser) throws Throwable {

        RebillEmployerFinancialTransactionRequestDISDTO requestDISDTO = new RebillEmployerFinancialTransactionRequestDISDTO();
        requestDISDTO.setSourceCompanyId(psid);
        requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
        requestDISDTO.setToken(pSAPUser.getAuthorizationToken());
        requestDISDTO.setCorpId(pSAPUser.getCorpId());
        requestDISDTO.setNoteToAttachToRebillEvent("Test\nNote\nHere");
        fnTxToRebill = pFinancialTransactionToRebill.getId().toString();
        requestDISDTO.setFinancialTransactionId(fnTxToRebill);
        originalFnTxAmt = SpcfUtils.convertToBigDecimal(pFinancialTransactionToRebill.getFinancialTransactionAmount());
        requestDISDTO.setRebillAmount(perUnitRebillAmount);
        return requestDISDTO;
    }

}
