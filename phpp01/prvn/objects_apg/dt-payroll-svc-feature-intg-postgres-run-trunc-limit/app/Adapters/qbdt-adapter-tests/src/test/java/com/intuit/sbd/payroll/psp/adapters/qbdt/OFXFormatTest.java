package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.IDDLINE;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Apr 25, 2008
 * Time: 4:33:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class OFXFormatTest {

    @Before
    public void runBeforeEachTest() {
        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }

//    @Test
//    public void happyPathPayrollWithGoodOFX() {
//        try {
//            OFXDataloader ofxDataLoader = new OFXDataloader();
//            // Need session because we are using SPCFCal
//            PayrollServices.beginUnitOfWork();
//            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
//            PayrollServices.commitUnitOfWork();
//            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//            QBDTRequestProcessor qbdtRequestProcessor= new QBDTRequestProcessor();
//            String ofxResponseStr = qbdtRequestProcessor.processRequest(ofxStr);
//            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr);
//            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
//            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnResponseCode);
//            PayrollServices.beginUnitOfWork();
//            assertEquals(2,PayrollServices.entityFinder.find(PayrollRun.class).size());
//            QBDTTestHelper.verifyTransmissionDataSaved(ofxStr,ofxResponseStr,TransmissionType.PayrollSubmission);
//            PayrollServices.commitUnitOfWork();
//        } catch (Exception e) {
//            e.printStackTrace();
//            PayrollServices.rollbackUnitOfWork();
//            TestCase.fail(e.toString());
//        }
//    }

    @Test
    public void badCheckDateTest() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            // Need session because we are using SPCFCal
            PayrollServices.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            List<IPAYROLLRUN> payrollRunList = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
            payrollRunList.get(0).setIDTPAYCHKS("8939");
            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj,ErrorMessages.BadOFXError(""));
//@TODO Fix test to test these other values
//            QBDTRequestProcessor qbdtRequestProcessor= new QBDTRequestProcessor();
//            String ofxResponseStr = qbdtRequestProcessor.processRequest(ofxStr);
//            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
//            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR,signOnResponseCode);
//            ErrorMessage badOfxError = ErrorMessages.BadOFXError("");
//            String errMsg = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE();
//            PayrollServices.beginUnitOfWork();
//            assertEquals(badOfxError.getErrorDescription(),errMsg);
//            assertEquals(0,PayrollServices.entityFinder.find(PayrollRun.class).size());
//            QBDTTestHelper.verifyTransmissionDataSaved(ofxStr,ofxResponseStr,TransmissionType.Unknown);
//            QBDTTestHelper.verifyTransmissionMessageIsError();
//            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void languageNotENGTest() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            // Need session because we are using SPCFCal
            PayrollServices.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setLANGUAGE("BADLANG");
            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj,ErrorMessages.BadOFXError(""));
//@TODO Fix test to test these other values
//            String ofxResponseStr = QBDTTestHelper.submitCompanyPayrollSubmitError(happyPathOfxObj,ErrorMessages.BadOFXError("").getErrorDescription(),TransmissionType.Unknown);
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void invalidVoidValue() {
        try {

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOFX = ofxDataloader.loadHappyPathOFX();
            QBDTTestHelper.processOFXRequestSuccess(happyPathOFX);

            PayrollServices.beginUnitOfWork();
            assertEquals(2,PayrollServices.entityFinder.find(PayrollRun.class).size());

            List<String> paychecksToVoid = new ArrayList();
            paychecksToVoid.add("1");
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX voidOfxObj = ofxDataloader.loadVoidCompany3Payroll(paychecksToVoid,happyPathOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
            PayrollServices.commitUnitOfWork();

            voidOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHKMOD().get(0).setIVOID("X");
            QBDTTestHelper.processRequestSignOnErrorDynamicTransmissionError(voidOfxObj,ErrorMessages.BadOFXError(""));
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void invalidAppVersionOFXElement() {
        try {
            // Need session because we are using SPCFCal
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER("1.1.1.R.2.2/BADBADBAD");
            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj,ErrorMessages.BadOFXError(""));
//@TODO Fix test to test these other values
//            String ofxResponseStr = QBDTTestHelper.submitCompanyPayrollSubmitError(happyPathOfxObj,ErrorMessages.BadOFXError("").getErrorDescription(),1,TransmissionType.Unknown);
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void onServiceFlagInvalidTest() {
        try {
            // Need session because we are using SPCFCal
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            List<IPAYROLLRUN> payrollRunList = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
            List<IPAYCHK> paycheckList = payrollRunList.get(0).getIPAYCHK();
            paycheckList.get(0).setIONSERVICE("X");
            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj,ErrorMessages.BadOFXError(""));
//@TODO Fix test to test these other values
//            String ofxResponseStr = QBDTTestHelper.submitCompanyPayrollSubmitError(happyPathOfxObj,ErrorMessages.BadOFXError("").getErrorDescription(),1,TransmissionType.Unknown);
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void negativeToken() {
        try {
            // Need session because we are using SPCFCal
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN("-1");
            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj,ErrorMessages.BadOFXError(""));
//@TODO Fix test to test these other values
//            String ofxResponseStr = QBDTTestHelper.submitCompanyPayrollSubmitError(happyPathOfxObj,ErrorMessages.BadOFXError("").getErrorDescription(),1,TransmissionType.Unknown);
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void invalidToken() {
        try {
            // Need session because we are using SPCFCal
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN("BAD2");
            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj,ErrorMessages.BadOFXError(""));

//@TODO Fix test to test these other values
//            String ofxResponseStr = QBDTTestHelper.submitCompanyPayrollSubmitError(happyPathOfxObj,ErrorMessages.BadOFXError("").getErrorDescription(),1,TransmissionType.Unknown);
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
   public void emptyToken() {
        try {
            // @TODO Fix this test.  Not really testing empty token.
            OFXDataloader ofxDataLoader = new OFXDataloader();
            // Need session because we are using SPCFCal
            PayrollServices.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            String payrollRespMsgOFXStr = QBDTTestHelper.processOFXPayrollRequestHappyPath();
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void illegalRoutingNumber() {
        // This should never happen, so an unexpected error should be thrown.
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            List<IPAYROLLRUN> payrollRunList = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
            List<IPAYCHK> paychecks = payrollRunList.get(0).getIPAYCHK();
            List<IDDLINE> ddLineList = paychecks.get(0).getIDDLINE();
            String badRoutingNumber = "123123124";
            ddLineList.get(0).getIDDACCT().getBANKACCTTO().setBANKID(badRoutingNumber);
            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            QBDTTestHelper.processRequestPayrollErrorDynamicTransmissionError(ofxStr,ErrorMessages.InvalidEERoutingNumber("123123124","Donovan McNabb"));
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void stringRoutingNumber() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            List<IPAYROLLRUN> payrollRunList = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
            List<IPAYCHK> paychecks = payrollRunList.get(0).getIPAYCHK();
            List<IDDLINE> ddLineList = paychecks.get(0).getIDDLINE();
            ddLineList.get(0).getIDDACCT().getBANKACCTTO().setBANKID("FOO");
            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj,ErrorMessages.BadOFXError(""));
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void illegalAccountType() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            List<IPAYROLLRUN> payrollRunList = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
            List<IPAYCHK> paychecks = payrollRunList.get(0).getIPAYCHK();
            List<IDDLINE> ddLineList = paychecks.get(0).getIDDLINE();
            String badAccountType = "BADACCTTYPE";
            ddLineList.get(0).getIDDACCT().getBANKACCTTO().setACCTTYPE(badAccountType);
            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj, ErrorMessages.SaxParsingError("OFX could not be mapped to Java: org.xml.sax.SAXParseException: cvc-enumeration-valid: Value 'BADACCTTYPE' is not facet-valid with respect to enumeration '[CHECKING, SAVINGS]'. It must be a value from the enumeration."));
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }

    }

    @Test
    public void emptyOFXTagTest() {
        try {
            TestHttpServletResponse testHttpServletResponse = new TestHttpServletResponse();
            QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
            qbdtPegServlet.service(new TestHttpServletRequest("<OFX></OFX>"), testHttpServletResponse);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(testHttpServletResponse.toString(),OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR,signOnResponseCode);
            ErrorMessage badOfxError = ErrorMessages.BadOFXError(ErrorMessages.BadOFXErrorMessages.PSID_MISSING);
            String errMsg = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE();
            PayrollServices.beginUnitOfWork();
            assertEquals(badOfxError.getErrorDescription(),errMsg);
            assertEquals(0,PayrollServices.entityFinder.find(PayrollRun.class).size());
            QBDTTestHelper.verifyTransmissionDataNotSaved();
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void missingRequiredFieldTest() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            ofxStr = ofxStr.replace("<LANGUAGE>","<LANGGGG>");
            QBDTTestHelper.processRequestSignOnErrorDynamicTransmissionError(ofxStr,ErrorMessages.BadOFXError(""));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    // An OFX tag which is a data element, the name is changed.  The OFX tag is not required.
    public void unrecognizedChildTagTest() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            ofxStr = ofxStr.replace("<I.QBFILENAME>","<I.QBFILENAMEBAD>");
            QBDTTestHelper.processRequestSignOnErrorDynamicTransmissionError(ofxStr,ErrorMessages.BadOFXError(""));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    // An OFX tag which has a opening and closing, the opening tag name is changed.
    public void unrecognizedParentTagTest() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            ofxStr = ofxStr.replace("<I.PAYROLLMSGSRQV1>","<I.PAYROLLMSGSRQV1BAD>");
            TestHttpServletResponse testHttpServletResponse = new TestHttpServletResponse();
            QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
            qbdtPegServlet.service(new TestHttpServletRequest(ofxStr), testHttpServletResponse);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(testHttpServletResponse.toString(),OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR,signOnResponseCode);
            ErrorMessage badOfxError = ErrorMessages.BadOFXError("");
            String errMsg = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE();
            PayrollServices.beginUnitOfWork();
            assertEquals(badOfxError.getErrorDescription(),errMsg);
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    // An OFX tag which has a opening and closing, the opening tag name is changed.
    public void missingPSID() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            // Need session because we are using SPCFCal
            PayrollServices.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            ofxStr = ofxStr.replace("<USERID>","<USERDD>");
            QBDTRequestProcessor qbdtRequestProcessor= new QBDTRequestProcessor();
            String ofxResponseStr = qbdtRequestProcessor.processRequest(ofxStr, null,"Test IP");
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR,signOnResponseCode);
            ErrorMessage badOfxError = ErrorMessages.BadOFXError(ErrorMessages.BadOFXErrorMessages.PSID_MISSING);
            String errMsg = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE();
            PayrollServices.beginUnitOfWork();
            assertEquals(badOfxError.getErrorDescription(),errMsg);
            assertEquals(0,PayrollServices.entityFinder.find(PayrollRun.class).size());
            QBDTTestHelper.verifyTransmissionDataNotSaved();
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }

    }
}
