package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.RefundEmployerFinancialTransactionRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.RefundEmployerFinancialTransactionResponseDISDTO;
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
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * $Author: DWeinberg $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPRefundEmployerFinancialTransactionTests.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/23 15:00:48 $
 * $Author: DWeinberg $
 */
public class PSPRefundEmployerFinancialTransactionTests {

    private String psid = "123456789";
    private PayrollRun payrollRun1 = null;
    private PayrollRun payrollRun2 = null;
    private Company company;
    private String fnTxToRefund;

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
    public void testHappyPathRefund() {
        try {
            RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRefund();
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
            String refundTransactionId = responseDISDTO.getRefundTransactionId();

            PayrollServices.beginUnitOfWork();
            FinancialTransaction originalFnTx = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(fnTxToRefund));
            DomainEntitySet<FinancialTransaction> fnTxAssoc = originalFnTx.getAssociatedTransactionsCollection();
            TestCase.assertEquals(1,fnTxAssoc.size());
            TestCase.assertEquals(fnTxAssoc.get(0).getId().toString(),refundTransactionId);

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

            FinancialTransaction fnTxToRefund = getHappyPathFinancialTransactionToRefund();
            RefundEmployerFinancialTransactionRequestDISDTO request = getRefundRequest(fnTxToRefund, sapSalesUser);
            RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRefund(request);
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
            FinancialTransaction fnTxToRefund = getHappyPathFinancialTransactionToRefund();
            RefundEmployerFinancialTransactionRequestDISDTO request = getRefundRequest(fnTxToRefund, sapSalesUser);
            RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRefund(request);
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
            RefundEmployerFinancialTransactionRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
            requestDISDTO.setSourceCompanyId(sourceCoIdDNE);

            DISAdapter disAdapter = new DISAdapter();
            RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = disAdapter.Update_RefundEmployerFinancialTransaction(requestDISDTO);
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
            RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRefund();
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
            RefundEmployerFinancialTransactionRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
            requestDISDTO.setFinancialTransactionId("Invalid_Fn_Tx_id");


            DISAdapter disAdapter = new DISAdapter();
            RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = disAdapter.Update_RefundEmployerFinancialTransaction(requestDISDTO);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testRetrieveEmployerFinancialTransactionNonRefundableState() {
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

            FinancialTransaction pFinancialTransactionToRefund = erDebitFnTxns.get(0);
            RefundEmployerFinancialTransactionRequestDISDTO requestDISDTO = new RefundEmployerFinancialTransactionRequestDISDTO();
            requestDISDTO.setSourceCompanyId(psid);
            requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
            requestDISDTO.setToken("");
            fnTxToRefund = pFinancialTransactionToRefund.getId().toString();
            requestDISDTO.setFinancialTransactionId(fnTxToRefund);
            requestDISDTO.setRefundAmount(SpcfUtils.convertToBigDecimal(pFinancialTransactionToRefund.getFinancialTransactionAmount()));
            requestDISDTO.setSettlementType(SettlementType.ACH);

            RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRefund(requestDISDTO);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testAlreadyRefunded() {
        try {
            FinancialTransaction fnTxToRefund = getHappyPathFinancialTransactionToRefund();
            RefundEmployerFinancialTransactionRequestDISDTO request = getRefundRequest(fnTxToRefund);
            RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = performRefund(request);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
            responseDISDTO = performRefund(request);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testRefundPartialAmount() {
        // NOTE: SAP currently takes a refund amount, but it is ignored and the full amount
        //    is refunded.
        try {
            RefundEmployerFinancialTransactionRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
            requestDISDTO.setRefundAmount(new BigDecimal(1));

            DISAdapter disAdapter = new DISAdapter();
            RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = disAdapter.Update_RefundEmployerFinancialTransaction(requestDISDTO);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    // Helper Methods
    public FinancialTransaction getHappyPathFinancialTransactionToRefund() throws Throwable {
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

    public RefundEmployerFinancialTransactionResponseDISDTO performRefund() throws Throwable {
        RefundEmployerFinancialTransactionRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
        return performRefund(requestDISDTO);
    }

    public RefundEmployerFinancialTransactionResponseDISDTO performRefund(RefundEmployerFinancialTransactionRequestDISDTO pRequestDISDTO) {

        RefundEmployerFinancialTransactionResponseDISDTO responseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            responseDISDTO = disAdapter.Update_RefundEmployerFinancialTransaction(pRequestDISDTO);

        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail(t.getMessage());
        }
        return responseDISDTO;
    }

    private RefundEmployerFinancialTransactionRequestDISDTO getHappyPathRefundRequest() throws Throwable {
        FinancialTransaction financialTransactionToRefund = getHappyPathFinancialTransactionToRefund();
        return getRefundRequest(financialTransactionToRefund);
    }

    private RefundEmployerFinancialTransactionRequestDISDTO getRefundRequest(FinancialTransaction pFinancialTransactionToRefund) throws Throwable {
        SAPUser sapUser = TestHelper.loginAdminUser();
        return getRefundRequest(pFinancialTransactionToRefund,sapUser);
    }

    private RefundEmployerFinancialTransactionRequestDISDTO getRefundRequest(FinancialTransaction pFinancialTransactionToRefund,SAPUser pSAPUser) throws Throwable {

        RefundEmployerFinancialTransactionRequestDISDTO requestDISDTO = new RefundEmployerFinancialTransactionRequestDISDTO();
        requestDISDTO.setSourceCompanyId(psid);
        requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
        requestDISDTO.setToken(pSAPUser.getAuthorizationToken());
        requestDISDTO.setCorpId(pSAPUser.getCorpId());
        requestDISDTO.setNoteToAttachToRefundEvent("Test\nNote\nHere");
        fnTxToRefund = pFinancialTransactionToRefund.getId().toString();
        requestDISDTO.setFinancialTransactionId(fnTxToRefund);
        requestDISDTO.setRefundAmount(SpcfUtils.convertToBigDecimal(pFinancialTransactionToRefund.getFinancialTransactionAmount()));
        requestDISDTO.setSettlementType(SettlementType.ACH);
        return requestDISDTO;
    }

}
