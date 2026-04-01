package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryCompanyPayrollsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryEmployerFinancialTransactionsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryCompanyPayrollsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryEmployerFinancialTransactionsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollTransaction;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryCompanyPayrollsTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryCompanyPayrollsTests {
    private String company1Psid;

    private SpcfCalendar assistedCompany1CreateDate;
    private SpcfCalendar assistedCompany2CreateDate;
    private PayrollRun payrollRun1;
    private PayrollRun payrollRun2;

    @Before
    public void loadDataHappyPath() {

        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 4, SpcfTimeZone.getLocalTimeZone()));
        payrollRun1 = runPayroll(company, new DateDTO("2011-02-06"), "1");
        DataLoadServices.runOffload();
        DataLoadServices.runACHTransactionProcessor(5);


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()));
        payrollRun2 = runPayroll(company, new DateDTO("2011-02-20"), "3");
        DataLoadServices.runOffload();

//        payrollRun1 = processResult.getResult();

        company1Psid = company.getSourceCompanyId();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 25, SpcfTimeZone.getLocalTimeZone()));

    }

    @Test
    public void testQueryCompanyPayrollHappyPathSinglePayrollsReturned() {
        try {
            SpcfCalendar beforeSpcfCal = payrollRun1.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(beforeSpcfCal,-2);
            Calendar beforeCal = CalendarUtils.convertToCalendar(beforeSpcfCal);
            SpcfCalendar afterSpcfCal = payrollRun1.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(afterSpcfCal,2);
            Calendar afterCal = CalendarUtils.convertToCalendar(afterSpcfCal);
            performQueryCompanyPayrolls(beforeCal, afterCal, 1);
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testQueryCompanyPayrollHappyPathMultiplePayrollsReturned() {
        try {
            SpcfCalendar beforeSpcfCal = payrollRun1.getPaycheckSettlementDate().copy();
            CalendarUtils.addBusinessDays(beforeSpcfCal,-2);
            Calendar beforeCal = CalendarUtils.convertToCalendar(beforeSpcfCal);
            SpcfCalendar afterSpcfCal = payrollRun2.getPaycheckSettlementDate().copy();
            CalendarUtils.addBusinessDays(afterSpcfCal,2);
            Calendar afterCal = CalendarUtils.convertToCalendar(afterSpcfCal);
            performQueryCompanyPayrolls(beforeCal, afterCal, 2);
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testQueryCompanyPayrollHappyPathToDateNull() {
        try {
            Calendar beforeCal = null;
            SpcfCalendar afterSpcfCal = payrollRun1.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(afterSpcfCal,2);
            Calendar afterCal = CalendarUtils.convertToCalendar(afterSpcfCal);
            performQueryCompanyPayrolls(beforeCal, afterCal, 1);
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    @Test
    public void testQueryCompanyPayrollHappyPathFromDateNull() {
        try {
            SpcfCalendar beforeSpcfCal = payrollRun2.getPaycheckDate().copy();
            CalendarUtils.addBusinessDays(beforeSpcfCal,-2);
            Calendar beforeCal = CalendarUtils.convertToCalendar(beforeSpcfCal);
            Calendar afterCal = null;
            performQueryCompanyPayrolls(beforeCal, afterCal, 1);
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
            TestCase.fail(pThrowable.getMessage());
        }
    }

    public QueryCompanyPayrollsResponseDISDTO performQueryCompanyPayrolls(Calendar pFromDate, Calendar pToDate, int pExpectedPayrollCount) {
        QueryCompanyPayrollsResponseDISDTO responseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyPayrollsRequestDISDTO requestDISDTO = new QueryCompanyPayrollsRequestDISDTO();
            requestDISDTO.setToDate(pToDate);
            requestDISDTO.setFromDate(pFromDate);
            requestDISDTO.setSourceCompanyId(company1Psid);
            requestDISDTO.setSourceSystem(SourceSystemEnum.QBDT);

            responseDISDTO = disAdapter.Query_CompanyPayrolls(requestDISDTO);
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
            TestCase.assertEquals(pExpectedPayrollCount,responseDISDTO.getCompanyPayrollDISDTOs().size());

        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail(t.getMessage());
        }
        return responseDISDTO;
    }

    private Company setupCompany() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.claimNoFeesOffer(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);
        return company;
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

    private PayrollRun runPayroll(Company company, DateDTO date, String amount) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, date, new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"6", "67", "87", "142", "61", "62", "63", "64", "66", "1"}, new String[]{amount, amount, amount, amount, amount, amount, amount, amount, amount, amount});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        return processResult.getResult();
    }

}
