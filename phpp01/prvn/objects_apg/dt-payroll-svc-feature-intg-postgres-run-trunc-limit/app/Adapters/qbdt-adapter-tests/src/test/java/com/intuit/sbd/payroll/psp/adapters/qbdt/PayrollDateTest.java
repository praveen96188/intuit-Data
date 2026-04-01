package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Feb 5, 2008
 * Time: 7:57:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class PayrollDateTest {
    private static Company company = null;
    private static SIGNONMSGSRQV1 signOnMsg = null;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();
        SpcfCalendar cal = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(cal, 1);
        // Set the time to before offload or offload will fail.
        cal.setValues(cal.getYear(), cal.getMonth(), cal.getDay(), 15, 0, 0, 0);
        PSPDate.setPSPTime(cal);
        company = companyQB1DataLoader.persistQBCompany1();
        ObjectFactory objFact = new ObjectFactory();
        signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();
        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testPaycheckDateWeekendHolidayInFuture() {

        try {
            PayrollServices.beginUnitOfWork();

            company = Application.refresh(company);

            OffloadGroup offloadGroup = company.getOffloadGroup();
            SpcfCalendar firstPossibleSettlementDate = FinancialTransaction.getSettlementDate(offloadGroup);

            SpcfCalendar firstWeekendOrHolidayAfterFirstPossibleOffloadDate = firstPossibleSettlementDate.copy();

            // Get the first weekend or holiday after the next possible offload.
            while (!CalendarUtils.isWeekendOrHoliday(firstWeekendOrHolidayAfterFirstPossibleOffloadDate)) {
                firstWeekendOrHolidayAfterFirstPossibleOffloadDate.addDays(1);
            }

            Date date = new Date(firstWeekendOrHolidayAfterFirstPossibleOffloadDate.getTimeInMilliseconds());
            DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
            String firstWeekendOrHolidayAfterFirstPossibleSettlementStr = dfm.format(date);
            PayrollServices.commitUnitOfWork();

            String resultErDebitDate = runPayroll(firstWeekendOrHolidayAfterFirstPossibleSettlementStr);

            PayrollServices.beginUnitOfWork();

            SpcfCalendar expectedErDebitDate = firstWeekendOrHolidayAfterFirstPossibleOffloadDate.copy();
            CalendarUtils.addBusinessDays(expectedErDebitDate,-1);

            String expectedErDebitDateStr = dfm.format(new Date(expectedErDebitDate.getTimeInMilliseconds()));
            PayrollServices.commitUnitOfWork();
            assertEquals(expectedErDebitDateStr, resultErDebitDate);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }

    }

    @Test
    /**
     * This tests a payroll date in future but it falls on weekend, so Monday is not
     *    a good paycheck date.  It needs to be Tuesday.
     */
    public void testSaturdayPaycheckDateSubmittedOnFriday() {

        try {
            PayrollServices.beginUnitOfWork();
            SpcfCalendar fridayMay02_2008 = PSPDate.getPSPTime();
            // Set the time to Friday May 2, 2008.
            fridayMay02_2008.setValues(2008, 05, 02, 15, 0, 0, 0);
            PSPDate.setPSPTime(fridayMay02_2008);

            DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                    .findCompanyService(company, ServiceCode.DirectDeposit);
            SpcfCalendar saturdayMay03_2008 = PSPDate.getPSPTime();
            saturdayMay03_2008.setValues(2008, 05, 03, 15, 0, 0, 0);

            Date date = new Date(saturdayMay03_2008.getTimeInMilliseconds());
            DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
            String saturdayMay03_2008Str = dfm.format(date);
            PayrollServices.commitUnitOfWork();

            String erDebitDateStr = runPayroll(saturdayMay03_2008Str);

            PayrollServices.beginUnitOfWork();

            // The paycheck should be Tuesday
            SpcfCalendar expectedErDebitDate = PSPDate.getPSPTime();
            expectedErDebitDate.setValues(2008, 05, 05, 15, 0, 0, 0);

            String expectedErDebitDateStr = dfm.format(new Date(expectedErDebitDate.getTimeInMilliseconds()));

            assertEquals(expectedErDebitDateStr, erDebitDateStr);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());

        }

    }

    /**
     * This tests a payroll date in future that makes the 44th
     *   day is a Sat, making the following Monday 46 days out.
     */
    @Test
    public void test44DaySaturdayPaycheckDate() {

        try {
            PayrollServices.beginUnitOfWork();
            // Friday June 13 2008
            SpcfCalendar fridayInJune = SpcfCalendar.getNow();
            fridayInJune.setValues(2008,5,13,0,0,0,0);
            PSPDate.setPSPTime(fridayInJune);

            SpcfCalendar saturday44DaysLater = SpcfCalendar.getNow();
            // Saturday July 27 2008
            saturday44DaysLater.setValues(2008,7,27,0,0,0,0);

            DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
            String saturday44DaysLaterStr = dfm.format(new Date(saturday44DaysLater.getTimeInMilliseconds()));
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();

            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFX();
            List<IPAYROLLRUN> payrolls = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
            IPAYROLLRUN firstPayroll = payrolls.get(0);
            firstPayroll.setIDTPAYCHKS(saturday44DaysLaterStr);

            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj,ErrorMessages.FutureDatedPayrollTooFarInfutureError());


//            OFXDataloader ofxDataLoader = new OFXDataloader();
//
//            // Need session because we are using SPCFCal
//            Application.beginUnitOfWork();
//            // Load Test OFX
//            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
//            Application.commitUnitOfWork();
//
//
//            List<IPAYROLLRUN> payrolls = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
//            IPAYROLLRUN firstPayroll = payrolls.get(0);
//            firstPayroll.setIDTPAYCHKS(firstSatAfter45BusinessDaysStr);
//
//            // Convert from Sameple OFX Jaxb Obj to a String
//            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//
//            // Processes the OFX String
//            Application.beginUnitOfWork();
//            SpcfCalendar spcfServerCal = PSPDate.getPSPTime();
//            String spcfServerCalStr = dfm.format(new Date(spcfServerCal.getTimeInMilliseconds()));
//            System.out.println("Server Date: " + spcfServerCalStr);
//            Application.commitUnitOfWork();
//
//            QBDTRequestProcessor qbdtRequestProcessor = new QBDTRequestProcessor();
//            String ofxResponseStr = qbdtRequestProcessor.processRequest(ofxStr);
//
//            // Turn the OFX String into an OFX object
//            // We will check a couple of items to spot check, but the
//            //    heart of the validation is happening the sign on
//            //    and request processor tests.
//            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
//            String signOnResponseErrorMsg = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE();
//            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR,signOnResponseCode);
//            ErrorMessage error = ErrorMessages.FutureDatedPayrollTooFarInfutureError();
//            assertEquals(error.getErrorDescription(),signOnResponseErrorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());

        }

    }


    public String runPayroll(String testPaycheckDate) {
        String rtnErDebitDateStr = null;
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();

            // Need session because we are using SPCFCal
            Application.beginUnitOfWork();
            // Load Test OFX
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            Application.commitUnitOfWork();


            List<IPAYROLLRUN> payrolls = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
            IPAYROLLRUN firstPayroll = payrolls.get(0);
            firstPayroll.setIDTPAYCHKS(testPaycheckDate);

            // Processes the OFX String
            String ofxResponseStr = QBDTTestHelper.submitQBDTRequestStringResponse(happyPathOfxObj);

            // Turn the OFX String into an OFX object
            // We will check a couple of items to spot check, but the
            //    heart of the validation is happening the sign on
            //    and request processor tests.
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            rtnErDebitDateStr = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIDTTX();
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
        return rtnErDebitDateStr;
    }

    //@TODO Enable once getSettlementDate() bug Active Change Request PSRV000169 fixed.
    public void testPaycheckDateWeekendHolidayInPast() {
        try {
            PayrollServices.beginUnitOfWork();

            SpcfCalendar firstWeekendOrHolidayInPast = PSPDate.getPSPTime();

            // Get the first weekend or holiday in past.
            firstWeekendOrHolidayInPast.addDays(-1);
            while (!CalendarUtils.isWeekendOrHoliday(firstWeekendOrHolidayInPast)) {
                firstWeekendOrHolidayInPast.addDays(-1);
            }

            Date date = new Date(firstWeekendOrHolidayInPast.getTimeInMilliseconds());
            DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
            String firstWeekendOrHolidayInPastStr = dfm.format(date);
            PayrollServices.commitUnitOfWork();

            String erDebitDateStr = runPayroll(firstWeekendOrHolidayInPastStr);

            PayrollServices.beginUnitOfWork();

            OffloadGroup offloadGroup = company.getOffloadGroup();
            SpcfCalendar firstPossibleSettlementDate = FinancialTransaction.getSettlementDate(offloadGroup);
            SpcfCalendar expectedErDebitDate = firstPossibleSettlementDate.copy();

            PayrollServices.commitUnitOfWork();

            String expectedErDebitDateStr = dfm.format(new Date(expectedErDebitDate.getTimeInMilliseconds()));
            assertEquals(expectedErDebitDateStr, erDebitDateStr);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    //@TODO Enable once getSettlementDate() bug Active Change Request PSRV000169 fixed.
    public void testPayrollWeekdayInPast() {
        try {
            PayrollServices.beginUnitOfWork();

            SpcfCalendar firstWeekdayInPast = PSPDate.getPSPTime();

            // Get the first weekday or holiday in past.
            firstWeekdayInPast.addDays(-1);
            while (CalendarUtils.isWeekendOrHoliday(firstWeekdayInPast)) {
                firstWeekdayInPast.addDays(-1);
            }

            Date date = new Date(firstWeekdayInPast.getTimeInMilliseconds());
            DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
            String firstWeekendOrHolidayInPastStr = dfm.format(date);
            PayrollServices.commitUnitOfWork();

            String erDebitDateStr = runPayroll(firstWeekendOrHolidayInPastStr);

            PayrollServices.beginUnitOfWork();

            OffloadGroup offloadGroup = company.getOffloadGroup();

            SpcfCalendar firstPossibleSettlementDate = FinancialTransaction.getSettlementDate(offloadGroup);
            SpcfCalendar expectedErDebitDate = firstPossibleSettlementDate.copy();
            String expectedErDebitDateStr = dfm.format(new Date(expectedErDebitDate.getTimeInMilliseconds()));
            PayrollServices.commitUnitOfWork();
            assertEquals(expectedErDebitDateStr, erDebitDateStr);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

}
