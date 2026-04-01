package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLUPDATEDATA;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 27, 2011
 * Time: 6:38:49 AM
 */
@SuppressWarnings("deprecation")
public class AssistedDisasterRecoveryTests {


    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OFXRequestGenerator.reset();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();

        DataLoadServices.updateIRSPaymentTemplateSupportDate(null);
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(null);
    }

    @Test
      public void testFixedRateReturnValueAfterDR() {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().clear();
        Double rate = -2.5;
        IPITEM ipitem = OFXRequestGenerator.generatePayrollItemTax(false,
                                                                   false,
                                                                   "CA - Unemployment",
                                                                   null,
                                                                   OFXRequestGenerator.generateTaxItem(false,
                                                                                                       false,
                                                                                                       "999-9999-5",
                                                                                                       new SpcfMoney("7000.00"),
                                                                                                       OFXRequestGenerator.EXPENSE_ACCOUNT,
                                                                                                       null,
                                                                                                       OFXRequestGenerator.LIABILITY_ACCOUNT,
                                                                                                       "EDD",
                                                                                                       true,
                                                                                                       "143",
                                                                                                       "CA",
                                                                                                       "SUI_ER",
                                                                                                       null,
                                                                                                       rate,
                                                                                                       null));
        //overwrite the rate from x% to $x
        String rateString = "$" + rate.toString();
        ipitem.getITAXITEM().setIRATE(rateString);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().add(ipitem);
        QBDTTestHelper.submitQBDTRequest(ofx);

        // sync item
        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken() - 1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);
        /**
         * PSP-3156 - Because of a bug in QB, sending % and $ in a single OFX is causing failures/crashes. Unless
         * this issue is fixed we cannot send $ amounts back to QB, please note PSP will continue to store $ rates
         * and will send them out as percentage same as before PSP-3156. e.g.
         * Input Rate   Output Rate
         * 5%           5%
         * $5           5%
         *
         * Once the issue in QB is fixed simply uncomment the line below and comment the "Temporary Fix Verification"
         * line of code below
         */
        //assertEquals("$" + rate.toString(), syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD().get(0).getITAXITEM().getIRATE());
        //PSP-3156 - Temporary Fix Verification
        assertEquals(rate.toString() + "%", syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD().get(0).getITAXITEM().getIRATE());
    }

    @Test
    public void testIncludeLiabilityCheckWithDR_OneTokenBehind() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008,1,1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // submit few payrolls
        QBDTTestHelper.submitPayroll(company,
                      ofx,
                      new Date("01/31/2011"),
                      new Date("01/31/2011"),
                      new Date("01/31/2011"));

        QBDTTestHelper.submitPayroll(company,
                      ofx,
                      new Date("02/02/2011"),
                      new Date("02/02/2011"),
                      new Date("02/02/2011"));

        // submit sync request with a lower token
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken() - 1);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(syncRequest);
        IPAYROLLUPDATEDATA ipayrollupdatedata = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA();
        assertNotNull("Payroll response", ipayrollupdatedata);

        assertEquals("liability checks", 1, ipayrollupdatedata.getIPAYROLLTXMOD().size());
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltxmod = ipayrollupdatedata.getIPAYROLLTXMOD().get(0);

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltxmod, ipayrolltxmod.getIDTPAYPDEND());

        SpcfDecimal total = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltxmod.getITXLINE()) {
            if(itxline.getIISDD() == null && itxline.getIPITEMID() != null) {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(5));
                total = total.add(amount);
                assertEquals("amount", "$" + amount, itxline.getIAMT());
            } else {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                total = total.add(amount);
            }
        }

        assertEquals("amount", "$-" + total, ipayrolltxmod.getIAMT());
    }

    @Test
    public void testIncludeLiabilityCheckWithDR_CompleteDR() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008,1,1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // submit few payrolls
        QBDTTestHelper.submitPayroll(company,
                                     ofx,
                                     new Date("01/31/2011"),
                                     new Date("01/31/2011"),
                                     new Date("01/31/2011"));

        QBDTTestHelper.submitPayroll(company,
                                     ofx,
                                     new Date("02/02/2011"),
                                     new Date("02/02/2011"),
                                     new Date("02/02/2011"));

        // submit sync request with a lower token
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, null);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(syncRequest);
        assertNotNull("Response", response);
        IPAYROLLUPDATEDATA ipayrollupdatedata = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA();
        assertNotNull("Payroll response", ipayrollupdatedata);

        assertEquals("liability checks", 2, ipayrollupdatedata.getIPAYROLLTXMOD().size());
        for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltxmod : ipayrollupdatedata.getIPAYROLLTXMOD()) {
            OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltxmod, ipayrolltxmod.getIDTPAYPDEND());

            SpcfDecimal total = SpcfMoney.ZERO;
            for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltxmod.getITXLINE()) {
                if(itxline.getIISDD() == null && itxline.getIPITEMID() != null) {
                    SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(5));
                    total = total.add(amount);
                    assertEquals("amount", "$" + amount, itxline.getIAMT());
                } else {
                    SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                    total = total.add(amount);
                }
            }

            assertEquals("amount", "$-" + total, ipayrolltxmod.getIAMT());
        }
    }

    @Test
    public void testLimitNotExceeded_CompleteDR() throws Exception {

        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008,1,1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        QBDTTestHelper.submitPayroll(company,
                                     ofx,
                                     new Date("01/31/2011"),
                                     new Date("01/31/2011"),
                                     new Date("01/31/2011"));

        QBDTTestHelper.submitPayroll(company,
                                     ofx,
                                     new Date("02/02/2011"),
                                     new Date("02/02/2011"),
                                     new Date("02/02/2011"));

        QBDTTestHelper.submitPayroll(company,
                                     ofx,
                                     new Date("02/05/2011"),
                                     new Date("02/05/2011"),
                                     new Date("02/05/2011"));

        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, null);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(syncRequest);
        assertNotNull("Response", response);
        IPAYROLLUPDATEDATA ipayrollupdatedata = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA();
        assertNotNull("Payroll response", ipayrollupdatedata);

        Assert.assertEquals("Payrolls count", 3, ipayrollupdatedata.getIPAYROLLTXMOD().size());
        Assert.assertEquals("Paychecks count", 15, ipayrollupdatedata.getIPAYCHKMOD().size());
        Assert.assertEquals("Employees count", 5, ipayrollupdatedata.getIEMPMOD().size());

    }

    @Test
    public void testPaychecksLimitExceeded_CompleteDR() throws Exception {

        String oldValue = SystemParameter.findValue(SystemParameter.Code.QBDT_MAX_PAYCHECKS_PER_DR) ;

        try {
            // reduce the limit for the test
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.QBDT_MAX_PAYCHECKS_PER_DR, "0");
            PayrollServices.commitUnitOfWork();

            String psid = "123456789";
            DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

            OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

            QBDTTestHelper.submitPayroll(company,
                                         ofx,
                                         new Date("01/31/2011"),
                                         new Date("01/31/2011"),
                                         new Date("01/31/2011"));

            OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, null);

            // pass in false - response does contain errors
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(syncRequest, false);
            assertNotNull("Response", response);
            assertNull("PayrollMsg response", response.getIPAYROLLMSGSRSV1());
            assertEquals("ERROR", response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY());

        } finally {
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.QBDT_MAX_PAYCHECKS_PER_DR, oldValue);
            PayrollServices.commitUnitOfWork();
        }

    }

    @Test
    public void testEmployeesLimitExceeded_CompleteDR() throws Exception {

        String oldValue = SystemParameter.findValue(SystemParameter.Code.QBDT_MAX_EMPLOYEES_PER_DR) ;

        try {
            // reduce the limit for the test
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.QBDT_MAX_EMPLOYEES_PER_DR, "1");
            PayrollServices.commitUnitOfWork();

            String psid = "123456789";
            DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

            OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

            // Do not submit payroll - just sync, the company has 5 employees

            OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, null);

            // pass in false - response does contain errors
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(syncRequest, false);
            assertNotNull("Response", response);
            assertNull("PayrollMsg response", response.getIPAYROLLMSGSRSV1());
            assertEquals("ERROR", response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY());

        } finally {
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.QBDT_MAX_EMPLOYEES_PER_DR, oldValue);
            PayrollServices.commitUnitOfWork();
        }

    }


}
