package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.FinancialTransactionDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryEmployerFinancialTransactionsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryEmployerFinancialTransactionsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollTransaction;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertNotNull;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryEmployerFinancialTransactionsTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryEmployerFinancialTransactionsTests {
    private String company1Psid;

    private PayrollRun payrollRun1;
    private PayrollRun payrollRun2;

    @Before
    public void loadDataHappyPath() {

        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        DISCompanyDataloader.setupPaymentTemplateForEachTest();

        Company company = DISCompanyDataloader.setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
        payrollRun1 = DISCompanyDataloader.runPayroll(company, new DateDTO("2011-01-10"), "50");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));
        // Move ahead five days so transaction in this payroll are completed.
        DataLoadServices.runACHTransactionProcessor(5);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 24, SpcfTimeZone.getLocalTimeZone()));
        payrollRun2 = DISCompanyDataloader.runPayroll(company, new DateDTO("2011-01-24"), "55");
        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

//        payrollRun1 = processResult.getResult();

        company1Psid = company.getSourceCompanyId();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 25, SpcfTimeZone.getLocalTimeZone()));

    }

    @Test
    public void testRetrieveEmployerFinancialTransactionsHappyPath() {
        try {
            SpcfCalendar beforeSpcfCal = payrollRun1.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(beforeSpcfCal,-2);
            SpcfCalendar afterSpcfCal = payrollRun1.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(afterSpcfCal, 2);
            PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
            ArrayList<SAPPayrollTransaction> payroll1FnTxns = payrollRunAdapter.findEmployerTransactions(
                    company1Psid,
                    SourceSystemCode.QBDT.toString(),
                    payrollRun1.getSourcePayRunId(),
                    CalendarUtils.convertToDate(beforeSpcfCal),
                    CalendarUtils.convertToDate(afterSpcfCal)
            );

            performRetrieveEmployerFinancialTransactions(payrollRun1.getSourcePayRunId(), payroll1FnTxns.size());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testRetrieveEmployerFinancialTransactionsPayrollIdDNE() {
        try {
            String payrollRunIdDNE = "PayrollIdDNE";

            DISAdapter disAdapter = new DISAdapter();
            QueryEmployerFinancialTransactionsRequestDISDTO requestDISDTO = new QueryEmployerFinancialTransactionsRequestDISDTO();
            requestDISDTO.setSourceCompanyId(company1Psid);
            requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
            requestDISDTO.setSourcePayRunId(payrollRunIdDNE);

            QueryEmployerFinancialTransactionsResponseDISDTO responseDISDTO = disAdapter.Query_EmployerFinancialTransactions(requestDISDTO);
            TestHelper.verifyDISResponse(DISMessages.payrollNotFound(payrollRunIdDNE,company1Psid),responseDISDTO.getDisResponse());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testRetrieveEmployerFinancialTransactionsRefundedTransaction() {
        try {
            SpcfCalendar beforeSpcfCal = payrollRun1.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(beforeSpcfCal,-2);
            SpcfCalendar afterSpcfCal = payrollRun1.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(afterSpcfCal,2);
            PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
            ArrayList<SAPPayrollTransaction> payroll1FnTxns = payrollRunAdapter.findEmployerTransactions(
                    company1Psid,
                    SourceSystemCode.QBDT.toString(),
                    payrollRun1.getSourcePayRunId(),
                    CalendarUtils.convertToDate(beforeSpcfCal),
                    CalendarUtils.convertToDate(afterSpcfCal)
            );

            SAPPayrollTransaction fnTxToRefund = null;
            // Get employer fee debit
            for (SAPPayrollTransaction sapPayrollTransaction : payroll1FnTxns) {
                if (sapPayrollTransaction.getTxnType()==TransactionTypeCode.EmployerFeeDebit) {
                    fnTxToRefund = sapPayrollTransaction;
                    break;
                }
            }


            String fnTxToRefundId = fnTxToRefund.getId().toString();
            payrollRunAdapter.refundEmployerTransaction(
                    SourceSystemCode.QBDT.toString(),
                    company1Psid,
                    fnTxToRefund.getId(),
                    fnTxToRefund.getAmount(),
                    fnTxToRefund.getTxnDate(),
                    fnTxToRefund.getSettlementType().toString()
            );

            QueryEmployerFinancialTransactionsResponseDISDTO responseDISDTO = performRetrieveEmployerFinancialTransactions(payrollRun1.getSourcePayRunId(), payroll1FnTxns.size() + 1);
            for (FinancialTransactionDISDTO financialTransactionDISDTO : responseDISDTO.getFinancialTransactionDISDTOs()) {
                if (financialTransactionDISDTO.getId().equals(fnTxToRefundId)) {
                    assertNotNull(financialTransactionDISDTO.getRefundTransactionId());
                }
            }
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }

    }

    public QueryEmployerFinancialTransactionsResponseDISDTO performRetrieveEmployerFinancialTransactions(String pSourcePayrollRunId,int pExpectedTransactionCount) {
        QueryEmployerFinancialTransactionsResponseDISDTO responseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryEmployerFinancialTransactionsRequestDISDTO requestDISDTO = new QueryEmployerFinancialTransactionsRequestDISDTO();
            requestDISDTO.setSourceCompanyId(company1Psid);
            requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);
            requestDISDTO.setSourcePayRunId(pSourcePayrollRunId);

            responseDISDTO = disAdapter.Query_EmployerFinancialTransactions(requestDISDTO);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
            TestCase.assertEquals(pExpectedTransactionCount,responseDISDTO.getFinancialTransactionDISDTOs().size());

        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail(t.getMessage());
        }
        return responseDISDTO;
    }

    private void refundERTransaction(String pFinancialTxId,double pFinancialTxAmt,Date pTxnDate,String pSettlementType) throws Throwable {
        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
        payrollRunAdapter.refundEmployerTransaction(
            SourceSystemCode.QBDT.toString(),
            company1Psid,
            pFinancialTxId,
            pFinancialTxAmt,
            pTxnDate,
            pSettlementType);
    }

}
