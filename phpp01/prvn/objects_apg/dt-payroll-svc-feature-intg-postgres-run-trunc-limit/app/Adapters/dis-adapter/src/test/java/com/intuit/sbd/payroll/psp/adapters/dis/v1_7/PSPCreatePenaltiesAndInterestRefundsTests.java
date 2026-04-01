package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.CreatePenaltiesAndInterestRefundsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.CreatePenaltiesAndInterestRefundsResponseDISDTO;
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
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPCreatePenaltiesAndInterestRefundsTests.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/23 15:00:48 $
 * $Author: DWeinberg $
 */
public class PSPCreatePenaltiesAndInterestRefundsTests {

    private String psid = "123456789";
    private PayrollRun payrollRun1 = null;
    private PayrollRun payrollRun2 = null;
    private Company company;
    private String fnTxToRefund;

    public static BigDecimal interestAmount = new BigDecimal("12.12");
    public static BigDecimal penaltyAmount = new BigDecimal("22.33");


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
            CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = performRefund();
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
            String interestRefundTransactionId = responseDISDTO.getInterestRefundTransactionId();
            String penaltyRefundTransactionId = responseDISDTO.getPenaltyRefundTransactionId();

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> interestRefundTransactions = FinancialTransaction.findAllFinancialTransaction(company,TransactionTypeCode.EmployerInterestRefundCredit);
            TestCase.assertEquals(1, interestRefundTransactions.size());
            FinancialTransaction interestFinancialTransaction =interestRefundTransactions.get(0);
            TestCase.assertEquals(interestRefundTransactionId, interestFinancialTransaction.getId().toString());
            TestCase.assertEquals(interestAmount, SpcfUtils.convertToBigDecimal(interestFinancialTransaction.getFinancialTransactionAmount()));

            DomainEntitySet<FinancialTransaction> penaltyRefundTransactions = FinancialTransaction.findAllFinancialTransaction(company,TransactionTypeCode.EmployerPenaltiesRefundCredit);
            TestCase.assertEquals(1, penaltyRefundTransactions.size());
            FinancialTransaction penaltyRefundTransaction = penaltyRefundTransactions.get(0);
            TestCase.assertEquals(penaltyRefundTransactionId, penaltyRefundTransaction.getId().toString());
            TestCase.assertEquals(penaltyAmount, SpcfUtils.convertToBigDecimal(penaltyRefundTransaction.getFinancialTransactionAmount()));

        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testZeroPenalty() {
        try {
            CreatePenaltiesAndInterestRefundsRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
            requestDISDTO.setPenaltiesRefundAmount(new BigDecimal(0));
            CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = performRefund(requestDISDTO);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
            String interestRefundTransactionId = responseDISDTO.getInterestRefundTransactionId();
            String penaltyRefundTransactionId = responseDISDTO.getPenaltyRefundTransactionId();

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> interestRefundTransactions = FinancialTransaction.findAllFinancialTransaction(company,TransactionTypeCode.EmployerInterestRefundCredit);
            TestCase.assertEquals(1, interestRefundTransactions.size());
            FinancialTransaction interestFinancialTransaction =interestRefundTransactions.get(0);
            TestCase.assertEquals(interestRefundTransactionId, interestFinancialTransaction.getId().toString());
            TestCase.assertEquals(interestAmount, SpcfUtils.convertToBigDecimal(interestFinancialTransaction.getFinancialTransactionAmount()));

            DomainEntitySet<FinancialTransaction> penaltyRefundTransactions = FinancialTransaction.findAllFinancialTransaction(company,TransactionTypeCode.EmployerPenaltiesRefundCredit);
            TestCase.assertEquals(0, penaltyRefundTransactions.size());
            TestCase.assertNull(penaltyRefundTransactionId);

        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testZeroInterest() {
        try {
            CreatePenaltiesAndInterestRefundsRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
            requestDISDTO.setInterestRefundAmount(new BigDecimal(0));
            CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = performRefund(requestDISDTO);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
            String interestRefundTransactionId = responseDISDTO.getInterestRefundTransactionId();
            String penaltyRefundTransactionId = responseDISDTO.getPenaltyRefundTransactionId();

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> interestRefundTransactions = FinancialTransaction.findAllFinancialTransaction(company,TransactionTypeCode.EmployerInterestRefundCredit);
            TestCase.assertEquals(0, interestRefundTransactions.size());
            TestCase.assertNull(interestRefundTransactionId);

            DomainEntitySet<FinancialTransaction> penaltyRefundTransactions = FinancialTransaction.findAllFinancialTransaction(company,TransactionTypeCode.EmployerPenaltiesRefundCredit);
            TestCase.assertEquals(1, penaltyRefundTransactions.size());
            FinancialTransaction penaltyRefundTransaction = penaltyRefundTransactions.get(0);
            TestCase.assertEquals(penaltyRefundTransactionId, penaltyRefundTransaction.getId().toString());
            TestCase.assertEquals(penaltyAmount, SpcfUtils.convertToBigDecimal(penaltyRefundTransaction.getFinancialTransactionAmount()));

        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testNegativeInterestAmount() {
        try {
            // SAP throws error when interest amount negative
            CreatePenaltiesAndInterestRefundsRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
            requestDISDTO.setInterestRefundAmount(new BigDecimal(-1));
            CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = performRefund(requestDISDTO);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());

        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testNegativePenaltyAmount() {
        try {
            // SAP throws error when interest amount negative
            CreatePenaltiesAndInterestRefundsRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
            requestDISDTO.setPenaltiesRefundAmount(new BigDecimal(-1));
            CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = performRefund(requestDISDTO);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());

        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testPenaltyAndInterestBothZero() {
        try {
            // SAP throws error when interest amount negative
            CreatePenaltiesAndInterestRefundsRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
            requestDISDTO.setPenaltiesRefundAmount(new BigDecimal(0));
            requestDISDTO.setInterestRefundAmount(new BigDecimal(0));
            CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = performRefund(requestDISDTO);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());

        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testUserSessionTimedOut() {
        try {

            CreatePenaltiesAndInterestRefundsRequestDISDTO request = getHappyPathRefundRequest();

            PayrollServices.beginUnitOfWork();
            SpcfCalendar spcfCalendar = SpcfCalendar.getNow();
            spcfCalendar.addDays(3);
            PSPDate.setPSPTime(spcfCalendar);
            PayrollServices.commitUnitOfWork();

            CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = performRefund(request);
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
            CreatePenaltiesAndInterestRefundsRequestDISDTO request = getHappyPathRefundRequest();
            SAPUser sapSalesUser = TestHelper.loginSalesUser();
            request.setCorpId(sapSalesUser.getCorpId());
            request.setToken(sapSalesUser.getAuthorizationToken());
            CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = performRefund(request);
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
            CreatePenaltiesAndInterestRefundsRequestDISDTO request = getHappyPathRefundRequest();
            request.setSourceCompanyId(sourceCoIdDNE);
            CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = performRefund(request);
            TestHelper.verifyFailure(responseDISDTO.getDisResponse());
            System.out.println("ERR: " + responseDISDTO.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    public CreatePenaltiesAndInterestRefundsResponseDISDTO performRefund() throws Throwable {
        CreatePenaltiesAndInterestRefundsRequestDISDTO requestDISDTO = getHappyPathRefundRequest();
        return performRefund(requestDISDTO);
    }

    private CreatePenaltiesAndInterestRefundsRequestDISDTO getHappyPathRefundRequest() throws Throwable {
        return getRefundRequest(penaltyAmount, interestAmount);
    }

    private CreatePenaltiesAndInterestRefundsRequestDISDTO getRefundRequest(BigDecimal pPenaltyRefundAmt,
                                                                            BigDecimal pInterestRefundAmt) throws Throwable {
        SAPUser sapUser = TestHelper.loginAdminUser();
        return getRefundRequest(pPenaltyRefundAmt, pInterestRefundAmt,sapUser);
    }

    public CreatePenaltiesAndInterestRefundsResponseDISDTO performRefund(CreatePenaltiesAndInterestRefundsRequestDISDTO pRequestDISDTO) {

        CreatePenaltiesAndInterestRefundsResponseDISDTO responseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            responseDISDTO = disAdapter.Update_CreatePenaltiesAndInterestRefunds(pRequestDISDTO);

        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail(t.getMessage());
        }
        return responseDISDTO;
    }

    private CreatePenaltiesAndInterestRefundsRequestDISDTO getRefundRequest(BigDecimal pPenaltyRefundAmt,
                                                                            BigDecimal pInterestRefundAmt,
                                                                            SAPUser pSAPUser
    ) throws Throwable {

        CreatePenaltiesAndInterestRefundsRequestDISDTO requestDISDTO = new CreatePenaltiesAndInterestRefundsRequestDISDTO();
        requestDISDTO.setSourceCompanyId(psid);
        requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
        requestDISDTO.setToken(pSAPUser.getAuthorizationToken());
        requestDISDTO.setCorpId(pSAPUser.getCorpId());
        requestDISDTO.setNoteToAttachToRefund("Test\nNote\nHere");
        requestDISDTO.setPenaltiesRefundAmount(pPenaltyRefundAmt);
        requestDISDTO.setInterestRefundAmount(pInterestRefundAmt);
        requestDISDTO.setSettlementType(SettlementType.ACH);
        return requestDISDTO;
    }

}
