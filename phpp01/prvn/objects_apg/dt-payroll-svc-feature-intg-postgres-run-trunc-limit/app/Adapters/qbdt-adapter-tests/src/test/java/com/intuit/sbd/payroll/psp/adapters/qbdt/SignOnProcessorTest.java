package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.request.SIGNONMSGSRQV1;
import com.intuit.sbd.payroll.psp.common.ofx.response.SIGNONMSGSRSV1;
import com.intuit.sbd.payroll.psp.common.ofx.response.SONRS;
import com.intuit.sbd.payroll.psp.common.ofx.response.STATUS;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.*;

import java.text.SimpleDateFormat;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Feb 21, 2008
 * Time: 1:59:45 PM
 */
public class SignOnProcessorTest {
    private String source_company_id = null;

    @BeforeClass
    public static void beforeClass() {

    }

    @AfterClass
    public static void afterClass() {

    }

    @Before
    public void runBeforeEachTest() {
        source_company_id = CompanyQB1DataLoader.COMPANY_PSID;
        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }

    @Test
    public void happyPath() throws Exception {
        Company company;
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        long origToken = company.getCurrentToken();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathSyncRequest(origToken + "");
        String happyPathOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        SIGNONMSGSRSV1 signOnMsgResponse = ofxResponseObj.getSIGNONMSGSRSV1();
        SONRS signOnResponse = signOnMsgResponse.getSONRS();
        assertNotNull(signOnResponse.getDTSERVER());
        assertNotNull(signOnResponse.getLANGUAGE());
        STATUS statusObj = signOnResponse.getSTATUS();
        assertNotNull(statusObj.getCODE());
        assertNotNull(statusObj.getSEVERITY());
        assertNull(statusObj.getMESSAGE());

        company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        assertEquals(origToken,company.getCurrentToken());

        PayrollServices.commitUnitOfWork();
        QBDTTestHelper.verifyTransmissionDataSaved(happyPathOfxStr,responseStr, TransmissionType.Sync);
    }

    @Test
    public void signonSunsetVersionOnEarlyBoundary() throws Exception {
        Company company;
        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        //OFX happyPathOfxObj= ofxDataloader.loadHappyPathSyncRequest();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1();

        SourcePayrollParameter param  = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.MinQBVersionSupported);
        String earlyVersion = param.getParameterValue() + ".00.R.3/20";
        happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(earlyVersion);

        SourcePayrollParameter sunsetParam = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.QBVersionSunsetString);
        SpcfCalendar effectiveDate = sunsetParam.getEffectiveDate().toLocal();
        SpcfCalendar endDate = sunsetParam.getExpirationDate().toLocal();
        endDate.addDays(-1);
        String formattedDate = getFormattedDateString(endDate);
        ErrorMessage message = ErrorMessages.QBReleaseToBeSunset(sunsetParam.getParameterValue(), formattedDate, formattedDate);

        PSPDate.setPSPTime(effectiveDate);
        String happyPathOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        SIGNONMSGSRSV1 signOnMsgResponse = ofxResponseObj.getSIGNONMSGSRSV1();
        SONRS signOnResponse = signOnMsgResponse.getSONRS();
        assertNotNull(signOnResponse.getDTSERVER());
        assertNotNull(signOnResponse.getLANGUAGE());
        STATUS statusObj = signOnResponse.getSTATUS();
        assertNotNull(statusObj.getCODE());
        assertNotNull(statusObj.getSEVERITY());
        assertEquals(message.getErrorDescription(), statusObj.getMESSAGE());

        company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        assertEquals(2, company.getCurrentToken());

        PayrollServices.commitUnitOfWork();
        QBDTTestHelper.verifyTransmissionDataSaved(happyPathOfxStr,responseStr, TransmissionType.PayrollSubmission);
    }

    @Test
    // Expect Null Message
    public void signonSunsetVersionBeforeEarlyBoundary() throws Exception {
        Company company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        long origToken = company.getCurrentToken();
        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathSyncRequest(origToken + "");

        SourcePayrollParameter param  = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.MinQBVersionSupported);
        String earlyVersion = param.getParameterValue() + ".00.R.3/20";
        happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(earlyVersion);

        SourcePayrollParameter sunsetParam = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.QBVersionSunsetString);
        SpcfCalendar effectiveDate = sunsetParam.getEffectiveDate().toLocal();
        effectiveDate.addMinutes(-1);

        PSPDate.setPSPTime(effectiveDate);
        String happyPathOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        SIGNONMSGSRSV1 signOnMsgResponse = ofxResponseObj.getSIGNONMSGSRSV1();
        SONRS signOnResponse = signOnMsgResponse.getSONRS();
        assertNotNull(signOnResponse.getDTSERVER());
        assertNotNull(signOnResponse.getLANGUAGE());
        STATUS statusObj = signOnResponse.getSTATUS();
        assertNotNull(statusObj.getCODE());
        assertNotNull(statusObj.getSEVERITY());
        assertNull(statusObj.getMESSAGE());

        company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        assertEquals(origToken,company.getCurrentToken());

        PayrollServices.commitUnitOfWork();
        QBDTTestHelper.verifyTransmissionDataSaved(happyPathOfxStr,responseStr, TransmissionType.Sync);
    }

    @Test
    // Expect no alert message.  Note it is assumed that the minimum version of Quickbooks will switch
    // in order to accomodate the newest valid QuickBooks Version
    public void signonSunsetVersionBeforeLateBoundary() throws Exception {
        Company company;
        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();

        OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1();

        SourcePayrollParameter param  = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.MinQBVersionSupported);
        String earlyVersion = param.getParameterValue() + ".00.R.3/20";
        happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(earlyVersion);

        SourcePayrollParameter sunsetParam = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.QBVersionSunsetString);
        SpcfCalendar endDate = sunsetParam.getExpirationDate().toLocal();
        endDate.addDays(-1);
        String formattedDate = getFormattedDateString(endDate);
        ErrorMessage message = ErrorMessages.QBReleaseToBeSunset(sunsetParam.getParameterValue(), formattedDate, formattedDate);

        endDate.addMinutes(-1);

        PSPDate.setPSPTime(endDate);
        String happyPathOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();

        String responseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        SIGNONMSGSRSV1 signOnMsgResponse = ofxResponseObj.getSIGNONMSGSRSV1();
        SONRS signOnResponse = signOnMsgResponse.getSONRS();
        assertNotNull(signOnResponse.getDTSERVER());
        assertNotNull(signOnResponse.getLANGUAGE());
        STATUS statusObj = signOnResponse.getSTATUS();
        assertNotNull(statusObj.getCODE());
        assertNotNull(statusObj.getSEVERITY());
        assertEquals(message.getErrorDescription(), statusObj.getMESSAGE());

        company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        assertEquals(2, company.getCurrentToken());

        PayrollServices.commitUnitOfWork();
        QBDTTestHelper.verifyTransmissionDataSaved(happyPathOfxStr,responseStr, TransmissionType.PayrollSubmission);
    }

    @Test
    // Expect no alert message.  Note it is assumed that the minimum version of Quickbooks will switch
    // in order to accommodate the newest valid QuickBooks Version
    public void signonSunsetVersionVersionString2010() throws Exception {
        Company company;
        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1();
        String savedParameterValue = null;

        SourcePayrollParameter param  = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.MinQBVersionSupported);
        String earlyVersion = param.getParameterValue() + ".00.R.3/20";
        happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(earlyVersion);

        PayrollServices.commitUnitOfWork();

        SourcePayrollParameter sunsetParam = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.QBVersionSunsetString);
        if (!"Quickbooks 2010".equals(sunsetParam.getParameterValue())) {
            savedParameterValue = sunsetParam.getParameterValue();
            DataLoadServices.updateSourcePayrollParameter(SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString, "Quickbooks 2010");
        }

        PayrollServices.beginUnitOfWork();
        Application.refresh(sunsetParam);
        SpcfCalendar endDate = sunsetParam.getExpirationDate().toLocal();
        endDate.addDays(-1);
        String formattedDate = getFormattedDateString(endDate);
        ErrorMessage message = ErrorMessages.QBReleaseToBeSunset(sunsetParam.getParameterValue(), formattedDate, formattedDate);

        endDate.addMinutes(-1);

        PSPDate.setPSPTime(endDate);
        String happyPathOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();

        String responseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        SIGNONMSGSRSV1 signOnMsgResponse = ofxResponseObj.getSIGNONMSGSRSV1();
        SONRS signOnResponse = signOnMsgResponse.getSONRS();
        assertNotNull(signOnResponse.getDTSERVER());
        assertNotNull(signOnResponse.getLANGUAGE());
        STATUS statusObj = signOnResponse.getSTATUS();
        assertNotNull(statusObj.getCODE());
        assertNotNull(statusObj.getSEVERITY());
        assertEquals(message.getErrorDescription(), statusObj.getMESSAGE());

        company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        assertEquals(2, company.getCurrentToken());

        PayrollServices.commitUnitOfWork();
        QBDTTestHelper.verifyTransmissionDataSaved(happyPathOfxStr,responseStr, TransmissionType.PayrollSubmission);

        // If it changed, put the param back
        if (savedParameterValue != null) {
            DataLoadServices.updateSourcePayrollParameter(SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString, savedParameterValue);
        }
    }


    @Test
    // Expect no alert message.  Note it is assumed that the minimum version of Quickbooks will switch
    // in order to accommodate the newest valid QuickBooks Version
    public void signonSunsetVersionOnLateBoundary() throws Exception {
        Company company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        long origToken = company.getCurrentToken();
        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathSyncRequest(origToken + "");

        SourcePayrollParameter param  = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.MinQBVersionSupported);
        String earlyVersion = param.getParameterValue() + ".00.R.3/20";
        happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(earlyVersion);

        SourcePayrollParameter sunsetParam = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT,
                SourcePayrollParameterCode.QBVersionSunsetString);
        SpcfCalendar endDate = sunsetParam.getExpirationDate().toLocal();

        PSPDate.setPSPTime(endDate);
        String happyPathOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);        
        PayrollServices.commitUnitOfWork();
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        SIGNONMSGSRSV1 signOnMsgResponse = ofxResponseObj.getSIGNONMSGSRSV1();
        SONRS signOnResponse = signOnMsgResponse.getSONRS();
        assertNotNull(signOnResponse.getDTSERVER());
        assertNotNull(signOnResponse.getLANGUAGE());
        STATUS statusObj = signOnResponse.getSTATUS();
        assertNotNull(statusObj.getCODE());
        assertNotNull(statusObj.getSEVERITY());
        assertNull(statusObj.getMESSAGE());

        company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        assertEquals(origToken,company.getCurrentToken());

        PayrollServices.commitUnitOfWork();
        QBDTTestHelper.verifyTransmissionDataSaved(happyPathOfxStr,responseStr, TransmissionType.Sync);
    }


    @Test
    public void testInvalidPIN() throws Exception {
        String badUserPass = "blahblah";
        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathSyncRequest();
        happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setUSERPASS(badUserPass);
        String happyPathOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();
        String responseStr = QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxObj,ErrorMessages.AuthenticationFailedError(),QBDTTransmissionMessageDescription.getErrorBadPIN());
        System.out.println(responseStr);
        PayrollServices.beginUnitOfWork();
        QBDTTestHelper.verifyTransmissionDataSaved(happyPathOfxStr,responseStr, TransmissionType.Sync);
        PayrollServices.commitUnitOfWork();
        QBDTTestHelper.verifyEventExists(EventTypeCode.IncorrectPIN,1);
    }

    @Test
    public void testInvalidClientId() {
        // This event should not happen as long as client's not supported by
        //   PSP are forwarded along to the AS400.
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        long origToken = company.getCurrentToken();
        OFXDataloader ofxDataLoader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataLoader.loadHappyPathSyncRequest(origToken + "");
        SIGNONMSGSRQV1 signOnRequestMessage = happyPathOfxObj.getSIGNONMSGSRQV1();
        SignOnProcessor signOnProcessor = new SignOnProcessor("",company);

        signOnRequestMessage.getSONRQ().setUSERPASS("invalidPassword");

        OFXAPPVERObject ofxAPPIDObject = new OFXAPPVERObject(signOnRequestMessage.getSONRQ().getAPPVER());

        QBDTProcessResult<SIGNONMSGSRSV1> signOnProcessPR = signOnProcessor.processSignOnRequest(signOnRequestMessage, happyPathOfxObj);
        assertFalse(signOnProcessPR.isSuccess());

        assertEquals(ErrorMessages.AuthenticationFailedError().getErrorDescription(),signOnProcessPR.getMessage().getErrorDescription());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAlphaClientId() {
        PayrollServices.beginUnitOfWork();
        OFXAPPVERObject ofxAPPIDObject = new OFXAPPVERObject("21.00.A.16/20916#bel");
        assertNotNull("QB Version is not null", ofxAPPIDObject.getIntQBVersion());
        assertEquals("QB Version", new Integer(21), ofxAPPIDObject.getIntQBVersion());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testUnexpectedAppId() throws Exception {
        String badAppId = "UnexpectedAppID";
        Company company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
        long origToken = company.getCurrentToken();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathSyncRequest(origToken + "");
        happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setAPPID(badAppId);
        QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
    }

    @Test
    public void testInvalidAppVersion() throws Exception {
        String badAppVer = "BadAppVer";
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathSyncRequest();
        happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(badAppVer);
        String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        QBDTTestHelper.processRequestSignOnErrorDynamicTransmissionError(ofxStr, ErrorMessages.BadOFXError(""));
    }

    @Test
    public void testInvalidMaxPinExceeded() {
        try {
            Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            OFXDataloader ofxDataLoader = new OFXDataloader();
            OFX happyPathSyncOFX = ofxDataLoader.loadHappyPathSyncRequest();

            int maxRetryCnt = 3;
            int loopCnt = 1;

            happyPathSyncOFX.getSIGNONMSGSRQV1().getSONRQ().setUSERPASS("BadPassword");

            while (loopCnt++ < maxRetryCnt) {
                QBDTTestHelper.processOFXRequestSignOnError(happyPathSyncOFX,ErrorMessages.AuthenticationFailedError(),QBDTTransmissionMessageDescription.getErrorBadPIN());
            }
            // @TODO Should the transmission description be ERROR, Invalid PIN or it's own string
            //     once max retry count exceeded?
            QBDTTestHelper.processOFXRequestSignOnError(happyPathSyncOFX,ErrorMessages.MaxPinRetryError(),QBDTTransmissionMessageDescription.getErrorBadPIN());
            QBDTTestHelper.verifyEventExists(EventTypeCode.AccountLocked,1);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testQBFileId_DD() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        testQBFileId(company);
    }

    @Test
    public void testQBFileId_Assisted() {
        String psid = "123456789";
        QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        testQBFileId(company);
    }

    private void testQBFileId(Company pCompany) {
        String newFileId = "newFileId";
        String changedFileId = "changedFileId";
        OFX ofx = OFXRequestGenerator.generateSyncRequest(pCompany.getSourceCompanyId(), 0L);
        ofx.getSIGNONMSGSRQV1().getSONRQ().setIQBFILEID(newFileId);
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        // saved the new company file id
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        assertEquals("QB File Id", newFileId, pCompany.getQuickbooksInfo().getFileId());
        PayrollServices.rollbackUnitOfWork();

        // submit a request with the same file id
        pCompany = DataLoadServices.refreshCompany(pCompany);
        ofx = OFXRequestGenerator.generateSyncRequest(pCompany.getSourceCompanyId(), pCompany.getCurrentToken());
        ofx.getSIGNONMSGSRQV1().getSONRQ().setIQBFILEID(newFileId);
        QBDTTestHelper.submitQBDTRequest(ofx);

        // submit a request with a different file id
        assertTrue("Token needs to be greater than 0:" + pCompany.getCurrentToken(), pCompany.getCurrentToken() > 0);
        ofx = OFXRequestGenerator.generateSyncRequest(pCompany.getSourceCompanyId(), pCompany.getCurrentToken());
        ofx.getSIGNONMSGSRQV1().getSONRQ().setIQBFILEID(changedFileId);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(ofx, false);
        assertTrue("Error code", response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE().contains("Message Code 2002"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(pCompany, EventTypeCode.TransmissionError);
        assertEquals("Transmission Errors", 1, companyEvents.size());
        PayrollServices.rollbackUnitOfWork();

        // update the file id
        ofx = OFXRequestGenerator.generateSyncRequest(pCompany.getSourceCompanyId(), 0L);
        ofx.getSIGNONMSGSRQV1().getSONRQ().setIQBFILEID(changedFileId);
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        // save the new company file id
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        assertEquals("QB File Id", changedFileId, pCompany.getQuickbooksInfo().getFileId());
        PayrollServices.rollbackUnitOfWork();
    }

    private String getFormattedDateString(SpcfCalendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d,yyyy");
        return formatter.format(CalendarUtils.convertToDate(calendar));
    }

}
