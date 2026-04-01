package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.BANKACCT;
import com.intuit.sbd.payroll.psp.common.ofx.request.ICOINFOMOD;
import com.intuit.sbd.payroll.psp.common.ofx.request.IDDITEM;
import com.intuit.sbd.payroll.psp.common.ofx.request.IDDLINE;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMP;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPITEM;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.*;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.junit.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PayrollSubmitTest {


    @AfterClass
    public static void afterClass() {
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.QBDT_NEW_EMPLOYEE_COUNT, "5");
        PayrollServices.commitUnitOfWork();
    }    

    @Before
    public void runBeforeEachTest() {
        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }


    @Test
    public void testZeroPayroll() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        long beforeToken = company.getCurrentToken();
        PayrollServices.commitUnitOfWork();

        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX zeroPayroll = ofxDataloader.loadZeroPayroll();
        String ofxResponse = QBDTTestHelper.processOFXRequestSuccess(zeroPayroll);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseOFXObj = OFXManager.ofxResponseToJava(ofxResponse, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertEquals(zeroPayroll.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getTRNUID(), ofxResponseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getTRNUID());
        assertEquals(ofxResponseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getCODE(), QBOFX.SUCCESS_STATUS_CODE);
        assertEquals(ofxResponseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY(), QBOFX.MESSAGE_SEVERITY.INFO);
        assertNull(ofxResponseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE());
        QBDTTestHelper.verifyEventExists(EventTypeCode.ZeroPayrollReceived);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        long afterToken = company.getCurrentToken();
        PayrollServices.commitUnitOfWork();

        // no actions were performed
        assertEquals(beforeToken, afterToken);
    }

    @Test
    // The ONSERVICE flag should be ignored.  This
    //  test verifies that.
    public void testOnserviceFlagNProcessed() {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).setIONSERVICE("N");
            String requestOfxStr = OFXManager.javaToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            assertEquals(2, payrollRunReqList.size());

            QBDTTestHelper.verifyTransmissionDataSaved(requestOfxStr, payrollResponseMsg, TransmissionType.PayrollSubmission);
            //@TODO Add verification back in
//            QBDTTestHelper.verifyTransmissionMessagePaycheckAdded(4);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testPayrollHappyPath() {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            int originalPaycheckIdCnt = Integer.parseInt(company.getNextPaycheckId());
            int originalPayrollIdCnt = Integer.parseInt(company.getNextPayrollTransactionId());
            long originalTokenInt = company.getCurrentToken();

            PayrollServices.commitUnitOfWork();


            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();
            String requestOfxStr = OFXManager.javaToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);

            // Two payrolls, so token + 2
            assertEquals(originalTokenInt + 1, company.getCurrentToken());


            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertTrue(payrollResponseOfxObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            assertEquals(2, payrollRunReqList.size());

            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIEMPNEXTID());
            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPITEMNEXTID());
            String updatedNextPaycheckId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYCHKNEXTID();
            assertEquals((originalPaycheckIdCnt + 4) + "", updatedNextPaycheckId);
            String updatedNextPayrollId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTXNEXTID();
            assertEquals((originalPayrollIdCnt + 2) + "", updatedNextPayrollId);

            Company updatedCompany = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

            assertEquals(updatedNextPaycheckId, updatedCompany.getNextPaycheckId());
            assertEquals(updatedNextPayrollId, updatedCompany.getNextPayrollTransactionId());

            verifyPayroll1(payrollRunReqList.get(0), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);
            verifyPayroll2(payrollRunReqList.get(1), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);

            //@TODO Do these need to be tested?
//        IPAYROLLUPDATEDATA payrollUpdateData = payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA();
//        ITAXSERVSTATUS taxSvcStatus = payrollResponseMsg.getIPAYROLLUPDATERS().getITAXSERVSTATUS();
            QBDTTestHelper.verifyTransmissionDataSaved(requestOfxStr, payrollResponseMsg, TransmissionType.PayrollSubmission);
//@TODO Add test back in
//            QBDTTestHelper.verifyTransmissionMessagePaycheckAdded(4);
            QBDTTestHelper.verifyTransmissionMessageDuplicatePayroll(false);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }


    @Test
    @Ignore("Payroll runs are now controlled by paycheck id not token")
    public void testDuplicatePayrollRunId() {
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            Integer originalPayrollIdCnt = Integer.parseInt(company.getNextPayrollTransactionId());
            long originalTokenInt = company.getCurrentToken();
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();
            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            company.setNextPayrollTransactionId(originalPayrollIdCnt.toString());
            Application.save(company);
            PayrollServices.commitUnitOfWork();

            // Two payrolls, so token + 1
            assertEquals(originalTokenInt + 1, company.getCurrentToken());

            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertTrue(payrollResponseOfxObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            assertEquals(2, payrollRunReqList.size());

            QBDTTestHelper.updateOFXWithNextCompanyToken(happyPathOfxObj);
            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            QBDTTestHelper.processRequestPayrollErrorDynamicTransmissionError(ofxStr, ErrorMessages.DuplicatePayrollRunId());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testPayrollHappyPathWithAZeroPaycheck() {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            int originalPaycheckIdCnt = Integer.parseInt(company.getNextPaycheckId());
            int originalPayrollIdCnt = Integer.parseInt(company.getNextPayrollTransactionId());
            long originalTokenInt = company.getCurrentToken();

            PayrollServices.commitUnitOfWork();


            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathWithZeroDollarCheckOFX();
            String requestOfxStr = OFXManager.javaToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);

            // Two payrolls, so token + 2
            assertEquals(originalTokenInt + 1, company.getCurrentToken());


            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertTrue(payrollResponseOfxObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            assertEquals(2, payrollRunReqList.size());

            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIEMPNEXTID());
            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPITEMNEXTID());
            String updatedNextPaycheckId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYCHKNEXTID();
            assertEquals((originalPaycheckIdCnt + 4) + "", updatedNextPaycheckId);
            String updatedNextPayrollId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTXNEXTID();
            assertEquals((originalPayrollIdCnt + 2) + "", updatedNextPayrollId);

            Company updatedCompany = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

            assertEquals(updatedNextPaycheckId, updatedCompany.getNextPaycheckId());
            assertEquals(updatedNextPayrollId, updatedCompany.getNextPayrollTransactionId());

            verifyPayroll1WithZeroCheck(payrollRunReqList.get(0), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);
            verifyPayroll2(payrollRunReqList.get(1), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);

            //@TODO Do these need to be tested?
//        IPAYROLLUPDATEDATA payrollUpdateData = payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA();
//        ITAXSERVSTATUS taxSvcStatus = payrollResponseMsg.getIPAYROLLUPDATERS().getITAXSERVSTATUS();
            QBDTTestHelper.verifyTransmissionDataSaved(requestOfxStr, payrollResponseMsg, TransmissionType.PayrollSubmission);
//@TODO Add test back in
//            QBDTTestHelper.verifyTransmissionMessagePaycheckAdded(4);
            QBDTTestHelper.verifyTransmissionMessageDuplicatePayroll(false);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testPayrollHappyPathWithOffer() {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            int originalPaycheckIdCnt = Integer.parseInt(company.getNextPaycheckId());
            int originalPayrollIdCnt = Integer.parseInt(company.getNextPayrollTransactionId());
            long originalTokenInt = company.getCurrentToken();
            PayrollServices.companyManager.claimOfferForCompany("P57553", null, company);
            PayrollServices.commitUnitOfWork();


            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();
            String requestOfxStr = OFXManager.javaToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);

            // Two payrolls, so token + 2
            assertEquals(originalTokenInt + 1, company.getCurrentToken());


            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertTrue(payrollResponseOfxObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            assertEquals(2, payrollRunReqList.size());

            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIEMPNEXTID());
            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPITEMNEXTID());
            String updatedNextPaycheckId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYCHKNEXTID();
            assertEquals((originalPaycheckIdCnt + 4) + "", updatedNextPaycheckId);
            String updatedNextPayrollId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTXNEXTID();
            assertEquals((originalPayrollIdCnt + 2) + "", updatedNextPayrollId);

            Company updatedCompany = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

            assertEquals(updatedNextPaycheckId, updatedCompany.getNextPaycheckId());
            assertEquals(updatedNextPayrollId, updatedCompany.getNextPayrollTransactionId());

            verifyPayroll3(payrollRunReqList.get(0), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq, 4, String.format("Fee for 2 direct deposit(s) at $%s each (includes $%s discount)",ServiceChargePrices.getNormalPerPayrollServiceCharge(),ServiceChargePrices.getNormalPerPayrollServiceCharge()), "2.95");
            // verifyPayroll2(payrollRunReqList.get(1), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(),happyPathOfxObj.getIPAYROLLMSGSRQV1(),payrollTxReq);

            //@TODO Do these need to be tested?
//        IPAYROLLUPDATEDATA payrollUpdateData = payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA();
//        ITAXSERVSTATUS taxSvcStatus = payrollResponseMsg.getIPAYROLLUPDATERS().getITAXSERVSTATUS();
            QBDTTestHelper.verifyTransmissionDataSaved(requestOfxStr, payrollResponseMsg, TransmissionType.PayrollSubmission);
//@TODO Add test back in
//            QBDTTestHelper.verifyTransmissionMessagePaycheckAdded(4);
            QBDTTestHelper.verifyTransmissionMessageDuplicatePayroll(false);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testPayrollHappyPathWithWAVIEALL() {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            int originalPaycheckIdCnt = Integer.parseInt(company.getNextPaycheckId());
            int originalPayrollIdCnt = Integer.parseInt(company.getNextPayrollTransactionId());
            long originalTokenInt = company.getCurrentToken();
            PayrollServices.commitUnitOfWork();

            DataLoadServices.updateOffering(company, OfferingCode.DIYDDSTD, "DIYDD-STD");

            DataLoadServices.claimNoFeesOffer(company);

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();
            String requestOfxStr = OFXManager.javaToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);

            // Two payrolls, so token + 2
            assertEquals(originalTokenInt + 1, company.getCurrentToken());


            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertTrue(payrollResponseOfxObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            assertEquals(2, payrollRunReqList.size());

            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIEMPNEXTID());
            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPITEMNEXTID());
            String updatedNextPaycheckId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYCHKNEXTID();
            assertEquals((originalPaycheckIdCnt + 4) + "", updatedNextPaycheckId);
            String updatedNextPayrollId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTXNEXTID();
            assertEquals((originalPayrollIdCnt + 2) + "", updatedNextPayrollId);

            Company updatedCompany = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

            assertEquals(updatedNextPaycheckId, updatedCompany.getNextPaycheckId());
            assertEquals(updatedNextPayrollId, updatedCompany.getNextPayrollTransactionId());

            verifyPayroll3(payrollRunReqList.get(0), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq, 3, "No fee for 2 direct deposit(s)", "0.00");


            QBDTTestHelper.verifyTransmissionDataSaved(requestOfxStr, payrollResponseMsg, TransmissionType.PayrollSubmission);
//@TODO Add test back in
//            QBDTTestHelper.verifyTransmissionMessagePaycheckAdded(4);
            QBDTTestHelper.verifyTransmissionMessageDuplicatePayroll(false);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    //PayrollServices.companyManager.claimOfferForCompany("P57553" ,company);

    @Test
    public void testPayrollSubmitCustomerNotInSync() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        String tooLowToken = (company.getCurrentToken() - 1) + "";
        OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFXWithSpecifiedToken(tooLowToken);
        String expectedToken = company.getCurrentToken() + "";
        //@TODO Verify transmission type
        QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.ClientOutOfSyncMessage(tooLowToken, expectedToken));
        QBDTTestHelper.verifyEventExists(EventTypeCode.PayrollRejected, 1);
    }

    @Test
    public void testPayrollSubmitTokenTooHigh() {

        try {

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            PayrollServices.commitUnitOfWork();

            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN((company.getCurrentToken() + 1) + "");

            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.ClientOutOfSyncMessage((company.getCurrentToken() + 1) + "", (company.getCurrentToken()) + ""));
            QBDTTestHelper.verifyEventExists(EventTypeCode.PayrollRejected, 1);

//            QBDTTestHelper.submitCompanyPayrollSubmitError(happyPathOfxObj,ErrorMessages.ClientOutOfSyncMessage().getErrorDescription(),TransmissionType.PayrollSubmission);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }

    }

    @Test
    @Ignore("Duplicate paychecks are now removed if there is an exact match in the request")
    // If payrolls with matching IDs are reci
    public void testDuplicatePaychecksInSameTransmission() {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            IPAYROLLRUN payrollRun1 = ofxDataloader.loadCompany3PayrollRun1();

            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().add(payrollRun1);

            int originalPayrollRunCnt = PayrollRun.findPayrollRuns(company).size();

            String requestOfxStr = OFXManager.javaToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            assertEquals(3, payrollRunReqList.size());

            int afterPayrollRunCnt = PayrollRun.findPayrollRuns(company).size();
            assertEquals(originalPayrollRunCnt + 2, afterPayrollRunCnt);

            verifyPayroll1(payrollRunReqList.get(0), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);
            verifyPayroll2(payrollRunReqList.get(1), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);

            //@TODO Do these need to be tested?
//        IPAYROLLUPDATEDATA payrollUpdateData = payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA();
//        ITAXSERVSTATUS taxSvcStatus = payrollResponseMsg.getIPAYROLLUPDATERS().getITAXSERVSTATUS();
            QBDTTestHelper.verifyTransmissionDataSaved(requestOfxStr, payrollResponseMsg, TransmissionType.PayrollSubmission);
// @TODO Add test back in
//            QBDTTestHelper.verifyTransmissionMessagePaycheckAdded(4);
            QBDTTestHelper.verifyTransmissionMessageDuplicatePayroll(true);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }

    }

    @Test
    @Ignore("Duplicate paychecks are now removed if they have an exact match in the request")
    public void testDuplicatePaychecksInDifferentTransmissions() {

        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            IPAYROLLRUN payrollRun1 = ofxDataloader.loadCompany3PayrollRun1();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().add(payrollRun1);

            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN("1");
            String secondPayrollResponseMsg = QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.DuplicatePaycheckSubmitted());

            com.intuit.sbd.payroll.psp.common.ofx.response.OFX secondPayrollResponseOfxObj = OFXManager.ofxResponseToJava(secondPayrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String signOnResponseCode = secondPayrollResponseOfxObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO, signOnResponseCode);
            ErrorMessage badOfxError = ErrorMessages.DuplicatePaycheckSubmitted();
            String errMsg = secondPayrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE();
            assertEquals(badOfxError.getErrorDescription(), errMsg);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }

    }

    @Test
    public void testNoDDWorkAnyPaychecksOnPayroll() {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            int prePayrollRunCnt = PayrollRun.findPayrollRuns(company).size();
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            // Remove the DD Advice off of the first payroll; leave the second payroll DDADVICE.
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).setIDDADVICE(null);

            // Remove the DD Lines off of the paychecks on the first payroll; leave the second payroll DDADVICE.
            for (IPAYCHK paycheck : happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK()) {
                paycheck.getIDDLINE().clear();
            }

            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            DomainEntitySet<PayrollRun> payrollRunSet = PayrollRun.findPayrollRuns(company);
            PayrollServices.commitUnitOfWork();

            assertEquals(prePayrollRunCnt + 2, payrollRunSet.size());

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }

    }

    @Test
    public void testNoDDWorkSomePaychecksOnPayroll() {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            int prePayrollRunCnt = PayrollRun.findPayrollRuns(company).size();
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            // Remove the DD Advice off of the first payroll for the first paycheck; leave the second payroll DDADVICE.
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().remove(0);

            // Remove the DD Lines off one of the two paychecks on the first payroll; leave the second payroll DDADVICE.
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().clear();

            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            DomainEntitySet<PayrollRun> payrollRunSet = PayrollRun.findPayrollRuns(company);
            assertEquals(prePayrollRunCnt + 2, payrollRunSet.size());
            assertEquals(2, payrollRunSet.get(0).getPaycheckCollection().size());
            PayrollServices.commitUnitOfWork();


        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }

    }

    void verifyPayroll1(IPAYROLLRUN payroll1RunReqObj, IPAYROLLMSGSRSV1 payrollResponseMsg, IPAYROLLMSGSRQV1 payrollReqMsgOFXObj, IPAYROLLTRNRQ payrollTxReq) throws Exception {
        assertEquals(2, payroll1RunReqObj.getIPAYCHK().size());
        IPAYCHK dmPaycheckRequest = payroll1RunReqObj.getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payroll1RunReqObj.getIPAYCHK().get(1);

        assertEquals(1, dmPaycheckRequest.getIDDLINE().size());
        IDDLINE dmDDLine = dmPaycheckRequest.getIDDLINE().get(0);
        assertEquals(2, alPaycheckRequest.getIDDLINE().size());
        IDDLINE al1DDLine = alPaycheckRequest.getIDDLINE().get(0);
        IDDLINE al2DDLine = alPaycheckRequest.getIDDLINE().get(1);

        IDDSTATUS ddStatus = payrollResponseMsg.getIPAYROLLUPDATERS().getIDDSTATUS();
        assertNull(ddStatus);

        IPAYROLLTRNRS payrollTxResp = payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYROLLTRNRS();
        STATUS payrollTxStatusObj = payrollTxResp.getSTATUS();
        IPAYROLLRS payrollRespObj = payrollTxResp.getIPAYROLLRS();
        List<IPAYROLLTX> payrollTxList = payrollRespObj.getIPAYROLLTX();
        assertEquals(2, payrollTxList.size());
        IPAYROLLTX payrollTxObj = payrollTxList.get(0);

        String origPayrollTxId = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getTRNUID();
        assertEquals(origPayrollTxId, payrollTxReq.getTRNUID());

        assertEquals(QBOFX.SUCCESS_STATUS_CODE, payrollTxStatusObj.getCODE());
        assertEquals(QBOFX.MESSAGE_SEVERITY.INFO, payrollTxStatusObj.getSEVERITY());
        assertNull(payrollTxStatusObj.getMESSAGE());
        assertEquals(0, payrollRespObj.getIPAYCHKMOD().size());
        payrollTxObj.getIACCTNAME();

        SpcfDecimal totalPaycheckDebit = new SpcfMoney();
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(dmDDLine.getIAMT().substring(1)));
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(al1DDLine.getIAMT().substring(1)));
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(al2DDLine.getIAMT().substring(1)));

        SpcfDecimal totalPayrollDebit = new SpcfMoney(totalPaycheckDebit);
        // Add on fees
        totalPayrollDebit = totalPayrollDebit.add(new SpcfMoney("3").negate());
        totalPayrollDebit = totalPayrollDebit.add(ServiceChargePrices.getNormalPerPayrollServiceCharge().negate());
        totalPayrollDebit = totalPayrollDebit.add(ServiceChargePrices.getNormalPerPayrollServiceCharge().negate());

        assertEquals(QBOFX.DEFAULT_CLEARED_RESPONSE_STR, QBOFX.DEFAULT_CLEARED_RESPONSE_STR);

        assertEquals(payroll1RunReqObj.getIDTPAYCHKS(), payrollTxObj.getIDTPAYPDEND());
        DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
        Date d = dfm.parse(payroll1RunReqObj.getIDTPAYCHKS());
        SpcfCalendar businessDayBeforePaycheckDateSpcfCal = SpcfCalendar.createInstance(d.getTime());
        CalendarUtils.addBusinessDays(businessDayBeforePaycheckDateSpcfCal, -1);
        String dayBeforeOffload = dfm.format(new Date(businessDayBeforePaycheckDateSpcfCal.getTimeInMilliseconds()));

        assertEquals(dayBeforeOffload, payrollTxObj.getIDTTX());
        Date currentDate = new Date(PSPDate.getPSPTime().getTimeInMilliseconds());
        assertEquals(QBOFX.MEMOS.getCreatedByPayrollServiceMemo(currentDate), payrollTxObj.getIMEMO());
        assertEquals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, payrollTxObj.getINAME());
        assertEquals(QBOFX.OFX_YN.Y, payrollTxObj.getIONSERVICE());
        //@TODO Need tx id support
//        dmPayrollTxObj.getIPAYROLLTXID();
        assertEquals(QBOFX.PAYROLL_TX_TYPE.PAYROLL_LIAB_CHECK, payrollTxObj.getIPAYROLLTXTYPE());
        assertEquals(QBOFX.OFX_YN.N, payrollTxObj.getIVOID());

        assertEquals("", payrollTxObj.getIREFNUM());
        List<ITXLINE> txLineList = payrollTxObj.getITXLINE();
        assertEquals(4, txLineList.size());

        boolean receivedDDTx = false;
        boolean receivedTxFeeTx = false;
        boolean receivedTransmissionFeeTx = false;
        //Handle Payroll ER Debit Tx
        SpcfDecimal salesTax = new SpcfMoney();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        String legalState = company.getLegalAddress().getState();
        PayrollServices.commitUnitOfWork();
        for (ITXLINE txLine : txLineList) {
            if ((txLine.getIMEMO() != null)
                    && (txLine.getIMEMO().compareTo(QBOFX.MEMOS.getSalesTaxMemo(legalState)) == 0)) {
                salesTax = new SpcfMoney(txLine.getIAMT().substring(1));
            } else {
                if (QBOFX.nullStringCheck(txLine.getIACCTNAME()) == null) {
                    assertEquals("$" + totalPaycheckDebit.abs().toString(), txLine.getIAMT());
                    assertEquals(txLine.getIISDD(), QBOFX.OFX_YN.Y);
                    assertNull(txLine.getIMEMO());
                    receivedDDTx = true;
                } else {
                    if (txLine.getIMEMO().compareTo(BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE) == 0) {
                        receivedTransmissionFeeTx = true;
                    } else {
                        receivedTxFeeTx = true;
                    }
                }
            }
        }
        totalPayrollDebit = totalPayrollDebit.add(salesTax.negate());
        assertEquals("$" + totalPayrollDebit.toString(), payrollTxObj.getIAMT());

        assertTrue(receivedDDTx);
        assertTrue(receivedTxFeeTx);
        assertTrue(receivedTransmissionFeeTx);
    }

    void verifyPayroll1WithZeroCheck(IPAYROLLRUN payroll1RunReqObj, IPAYROLLMSGSRSV1 payrollResponseMsg, IPAYROLLMSGSRQV1 payrollReqMsgOFXObj, IPAYROLLTRNRQ payrollTxReq) throws Exception {
        assertEquals(2, payroll1RunReqObj.getIPAYCHK().size());
        IPAYCHK dmPaycheckRequest = payroll1RunReqObj.getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payroll1RunReqObj.getIPAYCHK().get(1);

        assertEquals(1, dmPaycheckRequest.getIDDLINE().size());
        IDDLINE dmDDLine = dmPaycheckRequest.getIDDLINE().get(0);
        assertEquals(2, alPaycheckRequest.getIDDLINE().size());
        IDDLINE al1DDLine = alPaycheckRequest.getIDDLINE().get(0);
        IDDLINE al2DDLine = alPaycheckRequest.getIDDLINE().get(1);

        IDDSTATUS ddStatus = payrollResponseMsg.getIPAYROLLUPDATERS().getIDDSTATUS();
        assertNull(ddStatus);

        IPAYROLLTRNRS payrollTxResp = payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYROLLTRNRS();
        STATUS payrollTxStatusObj = payrollTxResp.getSTATUS();
        IPAYROLLRS payrollRespObj = payrollTxResp.getIPAYROLLRS();
        List<IPAYROLLTX> payrollTxList = payrollRespObj.getIPAYROLLTX();
        assertEquals(2, payrollTxList.size());
        IPAYROLLTX payrollTxObj = payrollTxList.get(0);

        String origPayrollTxId = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getTRNUID();
        assertEquals(origPayrollTxId, payrollTxReq.getTRNUID());

        assertEquals(QBOFX.SUCCESS_STATUS_CODE, payrollTxStatusObj.getCODE());
        assertEquals(QBOFX.MESSAGE_SEVERITY.INFO, payrollTxStatusObj.getSEVERITY());
        assertNull(payrollTxStatusObj.getMESSAGE());
        assertEquals(0, payrollRespObj.getIPAYCHKMOD().size());
        payrollTxObj.getIACCTNAME();

        SpcfDecimal totalPaycheckDebit = new SpcfMoney();
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(dmDDLine.getIAMT().substring(1)));
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(al1DDLine.getIAMT().substring(1)));
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(al2DDLine.getIAMT().substring(1)));

        SpcfDecimal totalPayrollDebit = new SpcfMoney(totalPaycheckDebit);
        // Add on fees
        totalPayrollDebit = totalPayrollDebit.add(new SpcfMoney("3").negate());
        totalPayrollDebit = totalPayrollDebit.add(ServiceChargePrices.getNormalPerPayrollServiceCharge().negate());

        assertEquals(QBOFX.DEFAULT_CLEARED_RESPONSE_STR, QBOFX.DEFAULT_CLEARED_RESPONSE_STR);

        assertEquals(payroll1RunReqObj.getIDTPAYCHKS(), payrollTxObj.getIDTPAYPDEND());
        DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
        Date d = dfm.parse(payroll1RunReqObj.getIDTPAYCHKS());
        SpcfCalendar businessDayBeforePaycheckDateSpcfCal = SpcfCalendar.createInstance(d.getTime());
        CalendarUtils.addBusinessDays(businessDayBeforePaycheckDateSpcfCal, -1);
        String dayBeforeOffload = dfm.format(new Date(businessDayBeforePaycheckDateSpcfCal.getTimeInMilliseconds()));

        assertEquals(dayBeforeOffload, payrollTxObj.getIDTTX());
        Date currentDate = new Date(PSPDate.getPSPTime().getTimeInMilliseconds());
        assertEquals(QBOFX.MEMOS.getCreatedByPayrollServiceMemo(currentDate), payrollTxObj.getIMEMO());
        assertEquals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, payrollTxObj.getINAME());
        assertEquals(QBOFX.OFX_YN.Y, payrollTxObj.getIONSERVICE());
        //@TODO Need tx id support
//        dmPayrollTxObj.getIPAYROLLTXID();
        assertEquals(QBOFX.PAYROLL_TX_TYPE.PAYROLL_LIAB_CHECK, payrollTxObj.getIPAYROLLTXTYPE());
        assertEquals(QBOFX.OFX_YN.N, payrollTxObj.getIVOID());

        assertEquals("", payrollTxObj.getIREFNUM());
        List<ITXLINE> txLineList = payrollTxObj.getITXLINE();
        assertEquals(4, txLineList.size());

        boolean receivedDDTx = false;
        boolean receivedTxFeeTx = false;
        boolean receivedTransmissionFeeTx = false;
        //Handle Payroll ER Debit Tx
        SpcfDecimal salesTax = new SpcfMoney();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        String legalState = company.getLegalAddress().getState();
        PayrollServices.commitUnitOfWork();
        for (ITXLINE txLine : txLineList) {
            if ((txLine.getIMEMO() != null)
                    && (txLine.getIMEMO().compareTo(QBOFX.MEMOS.getSalesTaxMemo(legalState)) == 0)) {
                salesTax = new SpcfMoney(txLine.getIAMT().substring(1));
            } else {
                if (QBOFX.nullStringCheck(txLine.getIACCTNAME()) == null) {
                    assertEquals("$" + totalPaycheckDebit.abs().toString(), txLine.getIAMT());
                    assertEquals(txLine.getIISDD(), QBOFX.OFX_YN.Y);
                    assertNull(txLine.getIMEMO());
                    receivedDDTx = true;
                } else {
                    if (txLine.getIMEMO().compareTo(BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE) == 0) {
                        receivedTransmissionFeeTx = true;
                    } else {
                        receivedTxFeeTx = true;
                    }
                }
            }
        }
        totalPayrollDebit = totalPayrollDebit.add(salesTax.negate());
        assertEquals("$" + totalPayrollDebit.toString(), payrollTxObj.getIAMT());

        assertTrue(receivedDDTx);
        assertTrue(receivedTxFeeTx);
        assertTrue(receivedTransmissionFeeTx);
    }


    private void verifyPayroll2(IPAYROLLRUN payroll1RunReqObj, IPAYROLLMSGSRSV1 payrollResponseMsg, IPAYROLLMSGSRQV1 payrollReqMsgOFXObj, IPAYROLLTRNRQ payrollTxReq) throws Exception {

        assertEquals(2, payroll1RunReqObj.getIPAYCHK().size());
        IPAYCHK dmPaycheckRequest = payroll1RunReqObj.getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payroll1RunReqObj.getIPAYCHK().get(1);

        assertEquals(1, dmPaycheckRequest.getIDDLINE().size());
        IDDLINE dmDDLine = dmPaycheckRequest.getIDDLINE().get(0);
        assertEquals(2, alPaycheckRequest.getIDDLINE().size());
        IDDLINE al1DDLine = alPaycheckRequest.getIDDLINE().get(0);
        IDDLINE al2DDLine = alPaycheckRequest.getIDDLINE().get(1);


        //@TODO Enable this once we get tokens implemented
// String resultTokenStr = payrollResponseMsg.getIPAYROLLUPDATERS().getTOKEN();
//        int resultTokenInt = Integer.parseInt(resultTokenStr);
//        assertEquals(requestTokenInt+1,resultTokenInt);

        //@TODO Add when token code implmented
//        payrollResponseMsg.getIPAYROLLUPDATERS().getIEMPNEXTID();
//        payrollResponseMsg.getIPAYROLLUPDATERS().getIPITEMNEXTID();
//        payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYCHKNEXTID();
//        payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYROLLTXNEXTID();


        IDDSTATUS ddStatus = payrollResponseMsg.getIPAYROLLUPDATERS().getIDDSTATUS();
        assertNull(ddStatus);

        IPAYROLLTRNRS payrollTxResp = payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYROLLTRNRS();
        STATUS payrollTxStatusObj = payrollTxResp.getSTATUS();
        List<IPAYROLLTX> payrollTxList = payrollTxResp.getIPAYROLLRS().getIPAYROLLTX();
        IPAYROLLTX payrollTxObj = payrollTxList.get(1);

        String origPayrollTxId = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getTRNUID();
        assertEquals(origPayrollTxId, payrollTxReq.getTRNUID());

        assertEquals(QBOFX.SUCCESS_STATUS_CODE, payrollTxStatusObj.getCODE());
        assertEquals(QBOFX.MESSAGE_SEVERITY.INFO, payrollTxStatusObj.getSEVERITY());
        assertNull(payrollTxStatusObj.getMESSAGE());
        assertEquals(0, payrollTxResp.getIPAYROLLRS().getIPAYCHKMOD().size());
        payrollTxObj.getIACCTNAME();

        SpcfDecimal totalPaycheckDebit = new SpcfMoney();
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(dmDDLine.getIAMT().substring(1)));
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(al1DDLine.getIAMT().substring(1)));
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(al2DDLine.getIAMT().substring(1)));

        SpcfDecimal totalPayrollDebit = new SpcfMoney(totalPaycheckDebit);
        // Add on fees
        totalPayrollDebit = totalPayrollDebit.add(ServiceChargePrices.getNormalPerPayrollServiceCharge().negate());
        totalPayrollDebit = totalPayrollDebit.add(ServiceChargePrices.getNormalPerPayrollServiceCharge().negate());

        assertEquals(QBOFX.DEFAULT_CLEARED_RESPONSE_STR, QBOFX.DEFAULT_CLEARED_RESPONSE_STR);


        assertEquals(payroll1RunReqObj.getIDTPAYCHKS(), payrollTxObj.getIDTPAYPDEND());

        DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
        Date d = dfm.parse(payroll1RunReqObj.getIDTPAYCHKS());
        SpcfCalendar businessDayBeforePaycheckDateSpcfCal = SpcfCalendar.createInstance(d.getTime());
        CalendarUtils.addBusinessDays(businessDayBeforePaycheckDateSpcfCal, -1);
        String dayBeforeOffloadStr = dfm.format(new Date(businessDayBeforePaycheckDateSpcfCal.getTimeInMilliseconds()));
        assertEquals(dayBeforeOffloadStr, payrollTxObj.getIDTTX());

        Date currentDate = new Date(PSPDate.getPSPTime().getTimeInMilliseconds());
        assertEquals(QBOFX.MEMOS.getCreatedByPayrollServiceMemo(currentDate), payrollTxObj.getIMEMO());
        assertEquals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, payrollTxObj.getINAME());
        assertEquals(QBOFX.OFX_YN.Y, payrollTxObj.getIONSERVICE());
        //@TODO Need tx id support
//        dmPayrollTxObj.getIPAYROLLTXID();
        assertEquals(QBOFX.PAYROLL_TX_TYPE.PAYROLL_LIAB_CHECK, payrollTxObj.getIPAYROLLTXTYPE());
        assertEquals(QBOFX.OFX_YN.N, payrollTxObj.getIVOID());

        assertEquals("", payrollTxObj.getIREFNUM());
        List<ITXLINE> txLineList = payrollTxObj.getITXLINE();
        assertEquals(3, txLineList.size());

        boolean receivedDDTx = false;
        boolean receivedTxFeeTx = false;

        //Handle Payroll ER Debit Tx
        SpcfDecimal salesTax = new SpcfMoney();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        String legalState = company.getLegalAddress().getState();
        PayrollServices.commitUnitOfWork();
        for (ITXLINE txLine : txLineList) {
            if ((txLine.getIMEMO() != null)
                    && (txLine.getIMEMO().compareTo(QBOFX.MEMOS.getSalesTaxMemo(legalState)) == 0)) {
                salesTax = new SpcfMoney(txLine.getIAMT().substring(1));
            } else {
                if (QBOFX.nullStringCheck(txLine.getIACCTNAME()) == null) {
                    assertEquals("$" + totalPaycheckDebit.abs().toString(), txLine.getIAMT());
                    assertEquals(txLine.getIISDD(), QBOFX.OFX_YN.Y);
                    assertNull(txLine.getIMEMO());
                    receivedDDTx = true;
                } else {
                    if (txLine.getIMEMO().compareTo(BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE) == 0) {

                    } else {
                        txLine.getIISDD();
                        receivedTxFeeTx = true;
                    }
                }
            }
        }
        totalPayrollDebit = totalPayrollDebit.add(salesTax.negate());
        assertEquals("$" + totalPayrollDebit.toString(), payrollTxObj.getIAMT());

        assertTrue(receivedDDTx);
        assertTrue(receivedTxFeeTx);
    }

    void verifyPayroll3(IPAYROLLRUN payroll1RunReqObj, IPAYROLLMSGSRSV1 payrollResponseMsg, IPAYROLLMSGSRQV1 payrollReqMsgOFXObj, IPAYROLLTRNRQ payrollTxReq, int lineCount, String memoline, String fee) throws Exception {
        IPAYCHK dmPaycheckRequest = payroll1RunReqObj.getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payroll1RunReqObj.getIPAYCHK().get(1);

        assertEquals(1, dmPaycheckRequest.getIDDLINE().size());
        IDDLINE dmDDLine = dmPaycheckRequest.getIDDLINE().get(0);
        assertEquals(2, alPaycheckRequest.getIDDLINE().size());
        IDDLINE al1DDLine = alPaycheckRequest.getIDDLINE().get(0);
        IDDLINE al2DDLine = alPaycheckRequest.getIDDLINE().get(1);

        IDDSTATUS ddStatus = payrollResponseMsg.getIPAYROLLUPDATERS().getIDDSTATUS();
        assertNull(ddStatus);

        IPAYROLLTRNRS payrollTxResp = payrollResponseMsg.getIPAYROLLUPDATERS().getIPAYROLLTRNRS();
        STATUS payrollTxStatusObj = payrollTxResp.getSTATUS();
        IPAYROLLRS payrollRespObj = payrollTxResp.getIPAYROLLRS();
        List<IPAYROLLTX> payrollTxList = payrollRespObj.getIPAYROLLTX();
        assertEquals(2, payrollTxList.size());
        IPAYROLLTX payrollTxObj = payrollTxList.get(0);

        String origPayrollTxId = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getTRNUID();
        assertEquals(origPayrollTxId, payrollTxReq.getTRNUID());

        assertEquals(QBOFX.SUCCESS_STATUS_CODE, payrollTxStatusObj.getCODE());
        assertEquals(QBOFX.MESSAGE_SEVERITY.INFO, payrollTxStatusObj.getSEVERITY());
        assertNull(payrollTxStatusObj.getMESSAGE());
        assertEquals(0, payrollRespObj.getIPAYCHKMOD().size());
        payrollTxObj.getIACCTNAME();

        SpcfDecimal totalPaycheckDebit = new SpcfMoney();
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(dmDDLine.getIAMT().substring(1)));
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(al1DDLine.getIAMT().substring(1)));
        totalPaycheckDebit = totalPaycheckDebit.add(new SpcfMoney(al2DDLine.getIAMT().substring(1)));

        SpcfDecimal totalPayrollDebit = new SpcfMoney(totalPaycheckDebit);
        // Add on fees
        totalPayrollDebit = totalPayrollDebit.add(new SpcfMoney(fee).negate());


        assertEquals(QBOFX.DEFAULT_CLEARED_RESPONSE_STR, QBOFX.DEFAULT_CLEARED_RESPONSE_STR);

        assertEquals(payroll1RunReqObj.getIDTPAYCHKS(), payrollTxObj.getIDTPAYPDEND());
        DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
        Date d = dfm.parse(payroll1RunReqObj.getIDTPAYCHKS());
        SpcfCalendar businessDayBeforePaycheckDateSpcfCal = SpcfCalendar.createInstance(d.getTime());
        CalendarUtils.addBusinessDays(businessDayBeforePaycheckDateSpcfCal, -1);
        String dayBeforeOffload = dfm.format(new Date(businessDayBeforePaycheckDateSpcfCal.getTimeInMilliseconds()));

        assertEquals(dayBeforeOffload, payrollTxObj.getIDTTX());
        Date currentDate = new Date(PSPDate.getPSPTime().getTimeInMilliseconds());
        assertEquals(QBOFX.MEMOS.getCreatedByPayrollServiceMemo(currentDate), payrollTxObj.getIMEMO());
        assertEquals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, payrollTxObj.getINAME());
        assertEquals(QBOFX.OFX_YN.Y, payrollTxObj.getIONSERVICE());
        //@TODO Need tx id support
//        dmPayrollTxObj.getIPAYROLLTXID();
        assertEquals(QBOFX.PAYROLL_TX_TYPE.PAYROLL_LIAB_CHECK, payrollTxObj.getIPAYROLLTXTYPE());
        assertEquals(QBOFX.OFX_YN.N, payrollTxObj.getIVOID());

        assertEquals("", payrollTxObj.getIREFNUM());
        List<ITXLINE> txLineList = payrollTxObj.getITXLINE();
        assertEquals(lineCount, txLineList.size());

        boolean receivedDDTx = false;
        boolean receivedTxFeeTx = false;
        boolean receivedTransmissionFeeTx = false;
        //Handle Payroll ER Debit Tx
        SpcfDecimal salesTax = new SpcfMoney();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        String legalState = company.getLegalAddress().getState();
        PayrollServices.commitUnitOfWork();
        for (ITXLINE txLine : txLineList) {
            if ((txLine.getIMEMO() != null)
                    && (txLine.getIMEMO().compareTo(QBOFX.MEMOS.getSalesTaxMemo(legalState)) == 0)) {
                salesTax = new SpcfMoney(txLine.getIAMT().substring(1));
            } else {
                if (QBOFX.nullStringCheck(txLine.getIACCTNAME()) == null) {
                    assertEquals("$" + totalPaycheckDebit.abs().toString(), txLine.getIAMT());
                    assertEquals(txLine.getIISDD(), QBOFX.OFX_YN.Y);
                    assertNull(txLine.getIMEMO());
                    receivedDDTx = true;
                } else {
                    if (txLine.getIMEMO().compareTo(BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE) == 0) {
                        receivedTransmissionFeeTx = true;
                    } else {
                        receivedTxFeeTx = true;
                        assertEquals(memoline, txLine.getIMEMO());
                    }
                }
            }
        }
        totalPayrollDebit = totalPayrollDebit.add(salesTax.negate());
        //totalPayrollDebit = totalPayrollDebit .add(salesTax.negate());
        assertEquals("$" + totalPayrollDebit.toString(), payrollTxObj.getIAMT());

        assertTrue(receivedDDTx);
        assertTrue(receivedTxFeeTx);
        assertTrue(receivedTransmissionFeeTx);
    }

    @Test
    public void testSalesTaxLegalAddressChanged() {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX payroll1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
            String payroll1ResponseOFXStr = QBDTTestHelper.processOFXRequestSuccess(payroll1OFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responsePayroll1OFX = OFXManager.ofxResponseToJava(payroll1ResponseOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String expectedState = company.getLegalAddress().getState();
            PayrollServices.commitUnitOfWork();
            boolean found = true;
            for (ITXLINE txLine : responsePayroll1OFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getITXLINE()) {
                if (txLine.getIMEMO() != null) {
                    if (txLine.getIMEMO().compareTo(QBOFX.MEMOS.getSalesTaxMemo(expectedState)) == 0) {
                        found = true;
                    }
                }
            }
            assertTrue("Could not find expected sales tax string: " + QBOFX.MEMOS.getSalesTaxMemo(company.getLegalAddress().getState()), found);

            PayrollServices.beginUnitOfWork();
            String newLegalAddrCity = "Toledo";
            String newLegalAddrState = "OH";
            String newLegalZipCode = "43601";

            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            AddressDTO addressDTO = coDTO.getLegalAddress();
            addressDTO.setCity(newLegalAddrCity);
            addressDTO.setState(newLegalAddrState);
            addressDTO.setZipCode(newLegalZipCode);
            PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), coDTO);
            PayrollServices.commitUnitOfWork();

            OFX payroll2OFX = ofxDataloader.loadHappyPathOFXPayroll2();
            String payroll2ResponseOFXStr = QBDTTestHelper.processOFXRequestSuccess(payroll2OFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responsePayroll2OFX = OFXManager.ofxResponseToJava(payroll2ResponseOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            found = true;
            for (ITXLINE txLine : responsePayroll2OFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getITXLINE()) {
                if (txLine.getIMEMO() != null) {
                    if (txLine.getIMEMO().compareTo(QBOFX.MEMOS.getSalesTaxMemo(newLegalAddrState)) == 0) {
                        found = true;
                    }
                }
            }
            assertTrue("Could not find expected sales tax string: " + QBOFX.MEMOS.getSalesTaxMemo(newLegalAddrState), found);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testEmptyCompanyBankName() {
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            CompanyBankAccount coBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);


            CompanyBankAccountDTO coBADTO = PayrollServices.dtoFactory.create(coBankAcct);
            coBADTO.getBankAccountDTO().setBankName(null);
            ProcessResult<CompanyBankAccount> coBAUpdatePR = PayrollServices.companyManager.updateCompanyBankAccount(SourceSystemCode.QBDT, OFXDataloader.companyPSID, coBADTO);
            assertTrue(coBAUpdatePR.isSuccess());
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX payroll1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
            QBDTTestHelper.processOFXRequestPayrollSubmitSuccess(payroll1OFX);

            OFX payroll2OFX = ofxDataloader.loadHappyPathOFXPayroll2();
            QBDTTestHelper.processOFXRequestPayrollSubmitSuccess(payroll2OFX);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testEmptyEmployeeBankName() {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX payroll1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
            payroll1OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().setIACCTNAME("");
            QBDTTestHelper.processOFXRequestPayrollSubmitSuccess(payroll1OFX);

            OFX payroll2OFX = ofxDataloader.loadHappyPathOFXPayroll2();
            payroll2OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().setIACCTNAME("");
            QBDTTestHelper.processOFXRequestPayrollSubmitSuccess(payroll2OFX);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            Employee employee = Employee.findEmployee(company, OFXDataloader.dmEmpName);
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = Application.find(EmployeeBankAccount.class, EmployeeBankAccount.Employee().equalTo(employee));
            PayrollServices.commitUnitOfWork();
            assertEquals(1, employeeBankAccounts.size());

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    @Ignore("We want to record employees with Id... They are adding the ids for us in the new versions of QB")
    public void testPaycheckWithNonZeroEmplolyeeId() {
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            DomainEntitySet<Employee> employeeList = Employee.findEmployees(company);
            int originalEmployeeCount = employeeList.size();
            PayrollServices.commitUnitOfWork();
            String nonZeroEmployeeID = "1";

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).setIEMPID(nonZeroEmployeeID);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1).setIEMPID(nonZeroEmployeeID);
            }
            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).setIEMPID(nonZeroEmployeeID);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(1).setIEMPID(nonZeroEmployeeID);
            }

            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIPAYCHK().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIPAYCHK().get(0).setIEMPID(nonZeroEmployeeID);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIPAYCHK().get(1).setIEMPID(nonZeroEmployeeID);
            }
            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIDDADVICE().getIDD().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIDDADVICE().getIDD().get(0).setIEMPID(nonZeroEmployeeID);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIDDADVICE().getIDD().get(1).setIEMPID(nonZeroEmployeeID);
            }

            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            verifyPayroll1(payrollRunReqList.get(0), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);
            verifyPayroll2(payrollRunReqList.get(1), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            employeeList = Employee.findEmployees(company);
            PayrollServices.commitUnitOfWork();
            assertEquals(originalEmployeeCount + 2, employeeList.size());
            boolean testedEmpWithId = false;
            for (Employee emp : employeeList) {
                if (emp.getSourceEmployeeId().compareTo(nonZeroEmployeeID) == 0) {
                    testedEmpWithId = true;
                    assertEquals(OFXDataloader.dmEmpName, emp.getFirstName() + " " + emp.getLastName());
                }
            }
            assertFalse("Found employee with id " + nonZeroEmployeeID + ".", testedEmpWithId);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }


    @Test
    public void testPaycheckWithNumericEmployeeName() {
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            DomainEntitySet<Employee> employeeList = Employee.findEmployees(company);
            int originalEmployeeCount = employeeList.size();
            PayrollServices.commitUnitOfWork();
            String numericEmployeeName = "123";

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).setIEMPNAME(numericEmployeeName);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1).setIEMPNAME(numericEmployeeName);
            }
            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).setIEMPNAME(numericEmployeeName);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(1).setIEMPNAME(numericEmployeeName);
            }

            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIPAYCHK().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIPAYCHK().get(0).setIEMPNAME(numericEmployeeName);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIPAYCHK().get(1).setIEMPNAME(numericEmployeeName);
            }
            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIDDADVICE().getIDD().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIDDADVICE().getIDD().get(0).setIEMPNAME(numericEmployeeName);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIDDADVICE().getIDD().get(1).setIEMPNAME(numericEmployeeName);
            }

            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            verifyPayroll1(payrollRunReqList.get(0), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);
            verifyPayroll2(payrollRunReqList.get(1), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            employeeList = Employee.findEmployees(company);
            PayrollServices.commitUnitOfWork();
            assertEquals(originalEmployeeCount + 2, employeeList.size());
            boolean testedEmpWithId = false;
            for (Employee emp : employeeList) {
                if (emp.getSourceEmployeeId().compareTo(numericEmployeeName+"_") == 0) {
                    testedEmpWithId = true;
                    assertEquals("<EMPTY> 123", emp.getFirstName() + " " + emp.getLastName());
                }
            }
            assertTrue("Found employee with id " + numericEmployeeName+"_.", testedEmpWithId);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testOnePartName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("Johnson", "<EMPTY>", null, "Johnson");
    }

    @Test
    public void testTwoPartName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("Jack Johnson", "Jack", null, "Johnson");
    }

    @Test
    public void testThreePartName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("Jack J. Johnson", "Jack", "J.", "Johnson");
    }

    @Test
    public void testFivePartName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("A Bee C D E", "A", "Bee", "C D E");
    }

    @Test
    public void testTrimNameBeginWithLastName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("Johnson,  John  A ", "John", "A", "Johnson");
    }

    @Test
    public void testTrimNameManySpacesBeginWithLastName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("  Johnson,   John    A   ", "John", "A", "Johnson");
    }

    @Test
    public void testFivePartNameWithSpacesBeginWithLastName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("  E,  A  Bee   C   D ", "A", "Bee", "C D E");
    }

    @Test
    public void testTwoPartNameBeginWithLastName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("Johnson, Jack", "Jack", null, "Johnson");
    }

    @Test
    public void testThreePartNameBeginWithLastName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("Johnson, Jack J.", "Jack", "J.", "Johnson");
    }

    @Test
    public void testFivePartNameBeginWithLastName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("E, A Bee C D", "A", "Bee", "C D E");
    }

    @Test
    public void testTrimName() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents(" John A  Johnson", "John", "A", "Johnson");
    }

    @Test
    public void testTrimNameLastNameOnly() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("   Johnson", "<EMPTY>", null, "Johnson");
    }

    @Test
    public void testTrimNameManySpaces() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("  John   A  Johnson   ", "John", "A", "Johnson");
    }

    @Test
    public void testFivePartNameWithSpaces() throws Exception {
        testFullEmployeeNameOnPaycheckSplitsToComponents("  A   Bee   C   D  E  ", "A", "Bee", "C D E");
    }

    private void testFullEmployeeNameOnPaycheckSplitsToComponents(String fullName, String expectedFirstName, String expectedMiddleName, String expectedLastName) throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

        //just test one employee
        happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().remove(1);
        happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().remove(1);

        happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).setIEMPNAME(fullName);

        QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        Employee newEmployee = assertOne(Application.find(Employee.class, Employee.Company().equalTo(company)
                                                                                  .And(Employee.SourceEmployeeId().equalTo(fullName.trim()))));

        assertEquals(newEmployee.getFirstName(), expectedFirstName);
        assertEquals(newEmployee.getMiddleName(), expectedMiddleName);
        assertEquals(newEmployee.getLastName(), expectedLastName);
    }

    @Test
    public void testPaycheckWithNumericAndAlphabeticEmployeeName() {
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            DomainEntitySet<Employee> employeeList = Employee.findEmployees(company);
            int originalEmployeeCount = employeeList.size();
            PayrollServices.commitUnitOfWork();
            String numericEmployeeName = "1a23";

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).setIEMPNAME(numericEmployeeName);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1).setIEMPNAME(numericEmployeeName);
            }
            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).setIEMPNAME(numericEmployeeName);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(1).setIEMPNAME(numericEmployeeName);
            }

            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIPAYCHK().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIPAYCHK().get(0).setIEMPNAME(numericEmployeeName);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIPAYCHK().get(1).setIEMPNAME(numericEmployeeName);
            }
            if (happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIDDADVICE().getIDD().get(0).getIEMPNAME().compareTo(OFXDataloader.dmEmpName) == 0) {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIDDADVICE().getIDD().get(0).setIEMPNAME(numericEmployeeName);
            } else {
                happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(1).getIDDADVICE().getIDD().get(1).setIEMPNAME(numericEmployeeName);
            }

            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            verifyPayroll1(payrollRunReqList.get(0), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);
            verifyPayroll2(payrollRunReqList.get(1), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            employeeList = Employee.findEmployees(company);
            PayrollServices.commitUnitOfWork();
            assertEquals(originalEmployeeCount + 2, employeeList.size());
            boolean testedEmpWithId = false;
            for (Employee emp : employeeList) {
                if (emp.getSourceEmployeeId().compareTo(numericEmployeeName) == 0) {
                    testedEmpWithId = true;
                    assertEquals("<EMPTY> 1a23", emp.getFirstName() + " " + emp.getLastName());
                }
            }
            assertTrue("Found employee with id " + numericEmployeeName+".", testedEmpWithId);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testCOABlank() {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            int originalPaycheckIdCnt = Integer.parseInt(company.getNextPaycheckId());
            int originalPayrollIdCnt = Integer.parseInt(company.getNextPayrollTransactionId());
            long originalTokenInt = company.getCurrentToken();

            PayrollServices.commitUnitOfWork();


            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();
            String requestOfxStr = OFXManager.javaToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String payrollResponseMsg = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);

            // Two payrolls, so token + 2
            assertEquals(originalTokenInt + 1, company.getCurrentToken());


            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollResponseOfxObj = OFXManager.ofxResponseToJava(payrollResponseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertTrue(payrollResponseOfxObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
            IPAYROLLTRNRQ payrollTxReq = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();
            IPAYROLLRQ payrollRequest = payrollTxReq.getIPAYROLLRQ();
            List<IPAYROLLRUN> payrollRunReqList = payrollRequest.getIPAYROLLRUN();
            assertEquals(2, payrollRunReqList.size());

            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIEMPNEXTID());
            assertEquals("1", payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPITEMNEXTID());
            String updatedNextPaycheckId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYCHKNEXTID();
            assertEquals((originalPaycheckIdCnt + 4) + "", updatedNextPaycheckId);
            String updatedNextPayrollId = payrollResponseOfxObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTXNEXTID();
            assertEquals((originalPayrollIdCnt + 2) + "", updatedNextPayrollId);

            Company updatedCompany = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

            assertEquals(updatedNextPaycheckId, updatedCompany.getNextPaycheckId());
            assertEquals(updatedNextPayrollId, updatedCompany.getNextPayrollTransactionId());

            verifyPayroll1(payrollRunReqList.get(0), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);
            verifyPayroll2(payrollRunReqList.get(1), payrollResponseOfxObj.getIPAYROLLMSGSRSV1(), happyPathOfxObj.getIPAYROLLMSGSRQV1(), payrollTxReq);


            QBDTTestHelper.verifyTransmissionMessageDuplicatePayroll(false);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testInvalidAcctType() {
        try {

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            IPAYROLLRQ iPAYROLLRQ = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("");
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setBANKID("");
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTTYPE("UNKNOWN");

            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setACCTID("");
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setBANKID("");
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setACCTTYPE("UNKNOWN");

            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.SaxParsingError("Error: Invalid bank account found"));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testEmptyAcctType() {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            IPAYROLLRQ iPAYROLLRQ = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTTYPE("");

            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setACCTTYPE("");

            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj, ErrorMessages.SaxParsingError("OFX could not be mapped to Java: org.xml.sax.SAXParseException: cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[UNKNOWN, CHECKING, SAVINGS]'. It must be a value from the enumeration."));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testNullAcctType() {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            IPAYROLLRQ iPAYROLLRQ = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTTYPE(null);

            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setACCTTYPE(null);

            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj, ErrorMessages.SaxParsingError("OFX could not be mapped to Java: org.xml.sax.SAXParseException: cvc-complex-type.2.4.b: The content of element 'BANKACCTTO' is not complete. One of '{ACCTTYPE}' is expected."));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testInvalidAcctId() {
        try {

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            IPAYROLLRQ iPAYROLLRQ = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("");

            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setACCTID("");

            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.SaxParsingError("Error: Invalid bank account found"));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testNullAcctId() {
        try {

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            IPAYROLLRQ iPAYROLLRQ = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID(null);

            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setACCTID(null);

            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj, ErrorMessages.SaxParsingError("OFX could not be mapped to Java: org.xml.sax.SAXParseException: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ACCTTYPE'. One of '{ACCTID}' is expected."));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testInvalidBankId() {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            IPAYROLLRQ iPAYROLLRQ = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setBANKID("");

            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setBANKID("");

            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.SaxParsingError("Error: Invalid bank account found"));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testNullBankId() {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            IPAYROLLRQ iPAYROLLRQ = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setBANKID(null);

            iPAYROLLRQ.getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setBANKID(null);

            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj, ErrorMessages.SaxParsingError("OFX could not be mapped to Java: org.xml.sax.SAXParseException: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ACCTID'. One of '{BANKID}' is expected."));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testTransmissionDisabledError() {
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
            company.getQuickbooksInfo().setAllowTransmissions(false);
            Application.save(company.getQuickbooksInfo());
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj, ErrorMessages.TransmissionsDisabled());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testValidPaycheckAccountsInvalidCOINFOMOD() throws Exception{
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();
        OFX ofxRequestWithCoInfoMod = ofxDataloader.loadCompany3WithCOINFOMODChangingAll();

        IPAYROLLRQ iPAYROLLRQ = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        if(iPAYROLLRQ.getICOINFOMOD() == null) {
            iPAYROLLRQ.setICOINFOMOD(ofxRequestWithCoInfoMod.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getICOINFOMOD());
        }

        ICOINFOMOD coInfoModRequest = iPAYROLLRQ.getICOINFOMOD();

        coInfoModRequest.setBANKACCTFROM(new BANKACCT());
        coInfoModRequest.getBANKACCTFROM().setACCTID("^@~*");
        coInfoModRequest.getBANKACCTFROM().setBANKID("^@~*");
        coInfoModRequest.getBANKACCTFROM().setACCTTYPE("CHECKING");

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = QBDTTestHelper.submitQBDTRequest(happyPathOfxObj);
        String signOnResponseCode = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY();
        TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnResponseCode);
    }

    @Test
    public void testDIYSTDOffering() {
        String psid = "123218979";
        DataLoadServices.setPSPDate(2012, 8, 15);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        DataLoadServices.updateOffering(company, OfferingCode.DIYDDSTD, "DIYDD-STD");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 8, 20));
        PayrollServices.rollbackUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        IPAYROLLTX ipayrolltx = assertOne(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX());

        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME, "$3.00", null, false, BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE, null);
        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME, "$1.45", null, false, String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 1, 1.45), null);
        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME, "$0.16", null, false, QBOFX.MEMOS.getSalesTaxMemo("NJ"), null);
        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), null, "$1.00", null, true, null, null);
    }

    @Test
    public void testMigrationSubmit() {
        DataLoadServices.setPSPDate(2012, 9, 15);
        String psid = "1234569";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);
        DataLoadServices.addTaxService(company);

        DataLoadServices.cancelService(company, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService companyService = company.getCompanyService(ServiceCode.Tax);
        companyService.setServiceStartDate(null);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addDDService(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 10, 1));
        PayrollServices.rollbackUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        assertEquals(2, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEmployeeIdChangedRevertForQB() {
        DataLoadServices.setPSPDate(2012, 10, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        List<IEMP> iemps = OFXRequestGenerator.generateNewEmployees(2, new ArrayList<IPITEM>(), new ArrayList<IPITEM>(), new ArrayList<IPITEM>());
        for (IEMP iemp : iemps) {
            iemp.setIEMPID(iemp.getIEMPID() + 1);
            iemp.setIQBUNIQUEID("List" + iemp.getIEMPID());
            iemp.setIEMPTAX(null);
            iemp.setIEMPCOMPLIANCE(null);
            iemp.setIPAYROLL(null);
            iemp.setISICK(null);
            iemp.setIVAC(null);
        }

        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 iemps,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        ofx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(ofx);

        IPITEM ddItem = new IPITEM();
        ddItem.setIDDITEM(new IDDITEM());
        ddItem.setIPITEMID("0");

        // change the emp ids
        Map<String, String> oldEmpIdNewEmpIdMap = new HashMap<String, String>();
        for (IEMP iemp : iemps) {
            String newId = iemp.getIEMPID() + 0;
            oldEmpIdNewEmpIdMap.put(iemp.getIEMPID(), newId);
            iemp.setIEMPID(newId);
        }

        IPAYROLLRUN ipayrollrun = OFXRequestGenerator.generatePayrollRun(iemps, Arrays.asList(ddItem), new Date("10/20/2012"), new Date("10/20/2012"), new Date("10/20/2012"), false);
        for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
            ipaychk.setIPAYCHKID(ipaychk.getIPAYCHKID() + "0");
        }

        OFX newPayroll = new OFX();
        newPayroll.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        newPayroll.getSIGNONMSGSRQV1().getSONRQ().setAPPVER("22.00.R.14/21014#retail");
        ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                   null,
                                                                   null,
                                                                   iemps,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   Arrays.asList(ipayrollrun));
        newPayroll.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(newPayroll);

        // all of the employee ids should be the new ids - renumber logic in place have a look at PSP-4792
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        for (String newId : oldEmpIdNewEmpIdMap.values()) {
            Employee employee = Employee.findEmployee(company, newId);
            assertNotNull("Employee does not exist", employee);

            // the employee should have 1 paycheck
            DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class, Paycheck.SourceEmployee().equalTo(employee));
            assertEquals(1, paychecks.size());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeeIdChangedRevertForQB_NonSupportedQBVersion() {
        DataLoadServices.setPSPDate(2012, 10, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        List<IEMP> iemps = OFXRequestGenerator.generateNewEmployees(2, new ArrayList<IPITEM>(), new ArrayList<IPITEM>(), new ArrayList<IPITEM>());
        for (IEMP iemp : iemps) {
            iemp.setIEMPID(iemp.getIEMPID() + 1);
            iemp.setIQBUNIQUEID("List" + iemp.getIEMPID());
            iemp.setIEMPTAX(null);
            iemp.setIEMPCOMPLIANCE(null);
            iemp.setIPAYROLL(null);
            iemp.setISICK(null);
            iemp.setIVAC(null);
        }

        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 iemps,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        ofx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(ofx);

        IPITEM ddItem = new IPITEM();
        ddItem.setIDDITEM(new IDDITEM());
        ddItem.setIPITEMID("0");

        // change the emp ids
        Map<String, String> oldEmpIdNewEmpIdMap = new HashMap<String, String>();
        for (IEMP iemp : iemps) {
            String newId = iemp.getIEMPID() + 0;
            oldEmpIdNewEmpIdMap.put(iemp.getIEMPID(), newId);
            iemp.setIEMPID(newId);
        }

        IPAYROLLRUN ipayrollrun = OFXRequestGenerator.generatePayrollRun(iemps, Arrays.asList(ddItem), new Date("10/20/2012"), new Date("10/20/2012"), new Date("10/20/2012"), false);
        for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
            ipaychk.setIPAYCHKID(ipaychk.getIPAYCHKID() + "0");
        }

        OFX newPayroll = new OFX();
        newPayroll.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                   null,
                                                                   null,
                                                                   iemps,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   Arrays.asList(ipayrollrun));
        newPayroll.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(newPayroll);

        // all of the employee ids should not be the old ids
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        for (String oldId : oldEmpIdNewEmpIdMap.keySet()) {
            Employee employee = Employee.findEmployee(company, oldId);
            assertNull("Employee exists", employee);
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEEBankAccountNotChanged() throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX payroll1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
        QBDTTestHelper.processOFXRequestSuccess(payroll1OFX);

        // Submit second payroll with same info, changing
        //   emp bank acct.
        OFX payroll1OFX2 = ofxDataloader.loadHappyPathOFXPayroll2();
        QBDTTestHelper.processOFXRequestSuccess(payroll1OFX2);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        for (Employee emp : empList) {
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = Application.find(EmployeeBankAccount.class, EmployeeBankAccount.Employee().equalTo(emp));
            // Abe Lincoln has two bank accounts.
            if (emp.getSourceEmployeeId().compareTo(OFXDataloader.alEmpName) == 0) {
                assertEquals(2, employeeBankAccounts.size());
            }
            // Donovan McNabb has one bank account.
            if (emp.getSourceEmployeeId().compareTo(OFXDataloader.dmEmpName) == 0) {
                assertEquals(1, employeeBankAccounts.size());
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEEBankAccountChanged() throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX payroll1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
        QBDTTestHelper.processOFXRequestSuccess(payroll1OFX);

        // Submit second payroll with same info, changing
        //   emp bank acct.
        OFX payroll1OFX2 = ofxDataloader.loadHappyPathOFXPayroll2();
        // Change account number for DM on second payroll submission.
        String newBankAcctId = "1233345";
        payroll1OFX2.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID(newBankAcctId);
        payroll1OFX2.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(0).getBANKACCTTO().setACCTID(newBankAcctId);
        QBDTTestHelper.processOFXRequestSuccess(payroll1OFX2);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);

        Paycheck dmPaycheck1 = Paycheck.findPaycheck(company, "1");
        BankAccount dmBankAccountPayroll1 = dmPaycheck1.getPaycheckSplitCollection().iterator().next().getFinancialTransaction().getCreditBankAccount();

        assertEquals(OFXDataloader.DM_ACCT_ID, dmBankAccountPayroll1.getAccountNumber());
        assertEquals(OFXDataloader.DM_BANK_ID, dmBankAccountPayroll1.getRoutingNumber());
        assertEquals(OFXDataloader.DM_BANK_NAME, dmBankAccountPayroll1.getBankName());
        assertEquals(OFXDataloader.DM_PSP_ACCTTYPE, dmBankAccountPayroll1.getAccountTypeCd());

        Paycheck dmPaycheck2 = Paycheck.findPaycheck(company, "3");
        BankAccount dmBankAccountPayroll2 = dmPaycheck2.getPaycheckSplitCollection().iterator().next().getFinancialTransaction().getCreditBankAccount();

        assertEquals(newBankAcctId, dmBankAccountPayroll2.getAccountNumber());
        assertEquals(OFXDataloader.DM_BANK_ID, dmBankAccountPayroll2.getRoutingNumber());
        assertEquals(OFXDataloader.DM_BANK_NAME, dmBankAccountPayroll2.getBankName());
        assertEquals(OFXDataloader.DM_PSP_ACCTTYPE, dmBankAccountPayroll2.getAccountTypeCd());

        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        for (Employee emp : empList) {
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = Application.find(EmployeeBankAccount.class, EmployeeBankAccount.Employee().equalTo(emp));
            // Abe Lincoln has two bank accounts.
            if (emp.getSourceEmployeeId().compareTo(OFXDataloader.alEmpName) == 0) {
                assertEquals(2, employeeBankAccounts.size());
            }
            // Donovan McNabb has one bank account.
            if (emp.getSourceEmployeeId().compareTo(OFXDataloader.dmEmpName) == 0) {
                assertEquals(2, employeeBankAccounts.size());
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEEBankAccountSameBankAccountOnSplit() throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX payroll1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
        // Change AL second bank account to be same as first.
        payroll1OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1).getIDDLINE().get(1).getIDDACCT().getBANKACCTTO().setACCTID(OFXDataloader.AL_PAYCHECK1_ACCT_ID);
        payroll1OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1).getIDDLINE().get(1).getIDDACCT().getBANKACCTTO().setACCTTYPE(OFXDataloader.AL_PAYCHECK1_ACCTTYPE);
        payroll1OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1).getIDDLINE().get(1).getIDDACCT().getBANKACCTTO().setBANKID(OFXDataloader.AL_PAYCHECK1_BANK_ID);
        payroll1OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1).getIDDLINE().get(1).getIDDACCT().setIACCTNAME(OFXDataloader.AL_PAYCHECK1_BANK_NAME);

        payroll1OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(1).getBANKACCTTO().setACCTID(OFXDataloader.AL_PAYCHECK1_ACCT_ID);
        payroll1OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(1).getBANKACCTTO().setACCTTYPE(OFXDataloader.AL_PAYCHECK1_ACCTTYPE);
        payroll1OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDDADVICE().getIDD().get(1).getBANKACCTTO().setBANKID(OFXDataloader.AL_PAYCHECK1_BANK_ID);

        QBDTTestHelper.processOFXRequestSuccess(payroll1OFX);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        DomainEntitySet<Employee> empList = Employee.findEmployees(company);
        for (Employee emp : empList) {
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = Application.find(EmployeeBankAccount.class, EmployeeBankAccount.Employee().equalTo(emp));
            // Abe Lincoln has two bank accounts.
            if (emp.getSourceEmployeeId().compareTo(OFXDataloader.alEmpName) == 0) {
                assertEquals(2, employeeBankAccounts.size());
            }
            // Donovan McNabb has one bank account.
            if (emp.getSourceEmployeeId().compareTo(OFXDataloader.dmEmpName) == 0) {
                assertEquals(1, employeeBankAccounts.size());
            }
        }
        PayrollServices.commitUnitOfWork();


    }

    @Test
    public void testVoidEmpNameOnEmpWithNoFirstName() throws Exception {

        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX requestOFXObj = ofxDataloader.loadHappyPathOFX();

        PayrollServices.commitUnitOfWork();
        QBDTTestHelper.offloadCompanyPayroll(requestOFXObj);

        PayrollServices.beginUnitOfWork();
        List<String> paycheckVoidList = new ArrayList<String>();
        paycheckVoidList.add("1");
        OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList, requestOFXObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
        QBDTTestHelper.updateOFXWithNextCompanyToken(voidPayrollOfx);

        String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();

        OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfx);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX voidResponseOFXObj = OFXManager.ofxResponseToJava(responseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        boolean foundMatch = false;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE txLine : voidResponseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD().get(0).getITXLINE()) {
            if (txLine.getIMEMO() != null && txLine.getIMEMO().contains(QBOFX.MEMOS.getVoidAfterOffloadMemoStr(OFXDataloader.dmEmpName))) {
                foundMatch = true;
                break;
            }
        }
        assertTrue("Did not find expected ITXLINE memo '" + QBOFX.MEMOS.getVoidAfterOffloadMemoStr(OFXDataloader.dmEmpName) + "'", foundMatch);
    }
    @Test
    public void testDIYDDFY16Offering() {
        String psid = "123218979";
        DataLoadServices.setPSPDate(2015, 7, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);
        DataLoadServices.setPSPDate(2015, 8, 1);
        DataLoadServices.updateOffering(company, OfferingCode.DIYDDFY16, "DIYDDFY16");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2015, 8, 2));
        PayrollServices.rollbackUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        IPAYROLLTX ipayrolltx = assertOne(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX());

        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME, "$1.75", null, false, String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 1, 1.75), null);
        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME, "$0.08", null, false, QBOFX.MEMOS.getSalesTaxMemo("NJ"), null);
        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), null, "$1.00", null, true, null, null);

        DataLoadServices.setPSPDate(2015, 9, 1);
        DataLoadServices.updateOffering(company, OfferingCode.DIYDDFY163, "DIYDDFY16+3");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
         payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2015, 9, 2));
        PayrollServices.rollbackUnitOfWork();

        response = QBDTTestHelper.submitPayroll(company, payrollRunDTO);

         ipayrolltx = assertOne(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX());

        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME, "$1.75", null, false, String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 1, 1.75), null);
        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME, "$0.16", null, false, QBOFX.MEMOS.getSalesTaxMemo("NJ"), null);
        OFXAssert.assertTXLINEFound(ipayrolltx.getITXLINE(), null, "$1.00", null, true, null, null);
    }

}
