package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryCompanyLatestPayrollDatesRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryCompanyLatestPayrollDatesResponseDISDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static junit.framework.Assert.*;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryCompanyPayrollDatesTests.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/15 17:18:20 $
 * $Author: JChickanosky $
 * <p/>
 * There are the following payroll types in PSP:
 * FeeOnly,
 * Regular,
 * Adjustment,
 * BillPayment,
 * CloudOnly;
 * <p/>
 * When getting the payroll check date and payroll date, CloudOnly and Regular
 * are the ones that should be returned.
 * CloudOnly is taxes only payroll (no DD).
 * Regular is DD and taxes.
 * Bill Payment does not apply, as you
 * can't have Bill Payment service and Tax at the same time.
 */
public class PSPQueryCompanyPayrollDatesTests {
    private String psid = "123456789";
    private PayrollRun payrollRun1 = null;
    private PayrollRun payrollRun2 = null;

    @Before
    public void loadDataHappyPath() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @Test
    public void testCompanyMultiplePayrolls() {
//        List<PayrollRun> payrollRuns = DISCompanyDataloader.loadPayroll(psid);
//        payrollRun1 = payrollRuns.get(0);
//        payrollRun2 = payrollRuns.get(1);

        Company company = DISCompanyDataloader.setupCompany();

        psid = company.getSourceCompanyId();
        payrollRun1 = DISCompanyDataloader.loadPayroll(psid, "20110105");
        payrollRun2 = DISCompanyDataloader.loadPayroll(psid, "20110112");

        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyLatestPayrollDatesRequestDISDTO QueryCompanyLatestPayrollDatesRequestDISDTO = new QueryCompanyLatestPayrollDatesRequestDISDTO();
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceCompanyId(psid);
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryCompanyLatestPayrollDatesResponseDISDTO QueryCompanyLatestPayrollDatesResponseDISDTO = disAdapter.Query_CompanyLatestPayrollDates(QueryCompanyLatestPayrollDatesRequestDISDTO);
            TestHelper.verifySuccess(QueryCompanyLatestPayrollDatesResponseDISDTO.getDisResponse());
            Assert.assertEquals(payrollRun1.getPaycheckDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollCheckDate().getTimeInMillis());
            Assert.assertEquals(payrollRun1.getPayrollRunDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollRunDate().getTimeInMillis());
            Assert.assertEquals(payrollRun2.getPaycheckDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollCheckDate().getTimeInMillis());
            Assert.assertEquals(payrollRun2.getPayrollRunDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollRunDate().getTimeInMillis());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testCompanyNoPayrolls() {
        DataLoadServices.setupCompany(psid);

        EventTypeCode testEventTypeCode = EventTypeCode.TransmissionError;
        int specificTypeEventsCnt = 0;
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyLatestPayrollDatesRequestDISDTO QueryCompanyLatestPayrollDatesRequestDISDTO = new QueryCompanyLatestPayrollDatesRequestDISDTO();
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceCompanyId(psid);
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryCompanyLatestPayrollDatesResponseDISDTO QueryCompanyLatestPayrollDatesResponseDISDTO = disAdapter.Query_CompanyLatestPayrollDates(QueryCompanyLatestPayrollDatesRequestDISDTO);
            TestHelper.verifySuccess(QueryCompanyLatestPayrollDatesResponseDISDTO.getDisResponse());
            Assert.assertNull(QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollCheckDate());
            Assert.assertNull(QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollRunDate());
            Assert.assertNull(QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollCheckDate());
            Assert.assertNull(QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollRunDate());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testNoCompanyFound() {
        try {
            String sourceCoIdDNE = "companyDNE";

            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyLatestPayrollDatesRequestDISDTO QueryCompanyLatestPayrollDatesRequestDISDTO = new QueryCompanyLatestPayrollDatesRequestDISDTO();
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceCompanyId(sourceCoIdDNE);
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryCompanyLatestPayrollDatesResponseDISDTO response = disAdapter.Query_CompanyLatestPayrollDates(QueryCompanyLatestPayrollDatesRequestDISDTO);
            TestHelper.verifyDISResponse(DISMessages.companyDoesNotExist(sourceCoIdDNE), response.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testMigratedCompanyWithDDAndAssistedPayrolls() {
        psid = DISCompanyDataloader.setupMigratedCompanyWithDDPayrollAndAssistedPayroll().getSourceCompanyId();
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyLatestPayrollDatesRequestDISDTO QueryCompanyLatestPayrollDatesRequestDISDTO = new QueryCompanyLatestPayrollDatesRequestDISDTO();
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceCompanyId(psid);
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryCompanyLatestPayrollDatesResponseDISDTO QueryCompanyLatestPayrollDatesResponseDISDTO = disAdapter.Query_CompanyLatestPayrollDates(QueryCompanyLatestPayrollDatesRequestDISDTO);
            TestHelper.verifySuccess(QueryCompanyLatestPayrollDatesResponseDISDTO.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testCloudAsLastPayroll() {
        Company company = DISCompanyDataloader.setupCompany();

        psid = company.getSourceCompanyId();
        payrollRun1 = DISCompanyDataloader.loadPayroll(psid, "20110105");
        payrollRun2 = DISCompanyDataloader.loadPayroll(psid, "20110112");

        PayrollRun offloadOnlyPayroll = DISCompanyDataloader.loadOffloadOnlyPayroll(psid, "20110119");

        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyLatestPayrollDatesRequestDISDTO QueryCompanyLatestPayrollDatesRequestDISDTO = new QueryCompanyLatestPayrollDatesRequestDISDTO();
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceCompanyId(psid);
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryCompanyLatestPayrollDatesResponseDISDTO QueryCompanyLatestPayrollDatesResponseDISDTO = disAdapter.Query_CompanyLatestPayrollDates(QueryCompanyLatestPayrollDatesRequestDISDTO);
            TestHelper.verifySuccess(QueryCompanyLatestPayrollDatesResponseDISDTO.getDisResponse());
            Assert.assertEquals(payrollRun1.getPaycheckDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollCheckDate().getTimeInMillis());
            Assert.assertEquals(payrollRun1.getPayrollRunDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollRunDate().getTimeInMillis());
            Assert.assertEquals(offloadOnlyPayroll.getPaycheckDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollCheckDate().getTimeInMillis());
            Assert.assertEquals(offloadOnlyPayroll.getPayrollRunDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollRunDate().getTimeInMillis());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testFeeOnlyPayrollAsLastPayroll() {
        Company company = DISCompanyDataloader.setupCompany();

        psid = company.getSourceCompanyId();
        payrollRun1 = DISCompanyDataloader.loadPayroll(psid, "20110105");

        // offload verification transactions
        DataLoadServices.runOffload(company, 2011, 01, 05);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 06, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        try {
            PayrollRun feeOnlyPayrollRun = DISCompanyDataloader.createFeeOnlyPayroll(company);

            // Guarantee that adjustment payroll dates are after payroll 1 dates
            assertTrue(feeOnlyPayrollRun.getPaycheckDate().getTimeInMilliseconds() > payrollRun1.getPaycheckDate().getTimeInMilliseconds());
            assertTrue(feeOnlyPayrollRun.getPayrollRunDate().getTimeInMilliseconds() > payrollRun1.getPayrollRunDate().getTimeInMilliseconds());

            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyLatestPayrollDatesRequestDISDTO QueryCompanyLatestPayrollDatesRequestDISDTO = new QueryCompanyLatestPayrollDatesRequestDISDTO();
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceCompanyId(psid);
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryCompanyLatestPayrollDatesResponseDISDTO QueryCompanyLatestPayrollDatesResponseDISDTO = disAdapter.Query_CompanyLatestPayrollDates(QueryCompanyLatestPayrollDatesRequestDISDTO);
            TestHelper.verifySuccess(QueryCompanyLatestPayrollDatesResponseDISDTO.getDisResponse());
            Assert.assertEquals(payrollRun1.getPaycheckDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollCheckDate().getTimeInMillis());
            Assert.assertEquals(payrollRun1.getPayrollRunDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollRunDate().getTimeInMillis());
            Assert.assertEquals(payrollRun1.getPaycheckDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollCheckDate().getTimeInMillis());
            Assert.assertEquals(payrollRun1.getPayrollRunDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollRunDate().getTimeInMillis());
        } catch (Throwable t) {
            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(t.getMessage());
        }
    }

    @Test
    public void testAdjustmentPayrollAsLastPayroll() {
        Company company = DISCompanyDataloader.setupCompany();

        psid = company.getSourceCompanyId();
        payrollRun1 = DISCompanyDataloader.loadPayroll(psid, "20110105");

        // offload verification transactions
        DataLoadServices.runOffload(company, 2011, 01, 05);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 06, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        try {
            CompanyAdjustmentSubmission companyAdjustmentSubmission = DISCompanyDataloader.createAdjustmentPayroll(company);
            // Guarantee that adjustment payroll dates are after payroll 1 dates
            assertTrue(companyAdjustmentSubmission.getPayrollRun().getPaycheckDate().getTimeInMilliseconds() > payrollRun1.getPaycheckDate().getTimeInMilliseconds());
            assertTrue(companyAdjustmentSubmission.getPayrollRun().getPayrollRunDate().getTimeInMilliseconds() > payrollRun1.getPayrollRunDate().getTimeInMilliseconds());
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyLatestPayrollDatesRequestDISDTO QueryCompanyLatestPayrollDatesRequestDISDTO = new QueryCompanyLatestPayrollDatesRequestDISDTO();
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceCompanyId(psid);
            QueryCompanyLatestPayrollDatesRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));

            QueryCompanyLatestPayrollDatesResponseDISDTO QueryCompanyLatestPayrollDatesResponseDISDTO = disAdapter.Query_CompanyLatestPayrollDates(QueryCompanyLatestPayrollDatesRequestDISDTO);
            TestHelper.verifySuccess(QueryCompanyLatestPayrollDatesResponseDISDTO.getDisResponse());
            Assert.assertEquals(payrollRun1.getPaycheckDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollCheckDate().getTimeInMillis());
            Assert.assertEquals(payrollRun1.getPayrollRunDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getFirstPayrollRunDate().getTimeInMillis());
            Assert.assertEquals(payrollRun1.getPaycheckDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollCheckDate().getTimeInMillis());
            Assert.assertEquals(payrollRun1.getPayrollRunDate().getTimeInMilliseconds(), QueryCompanyLatestPayrollDatesResponseDISDTO.getLatestPayrollRunDate().getTimeInMillis());
        } catch (Throwable t) {
            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(t.getMessage());
        }
    }


}
