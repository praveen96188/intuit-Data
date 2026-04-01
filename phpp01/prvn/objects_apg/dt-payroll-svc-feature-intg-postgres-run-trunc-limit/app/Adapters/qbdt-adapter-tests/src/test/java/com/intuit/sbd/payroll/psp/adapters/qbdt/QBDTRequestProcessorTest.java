package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Feb 20, 2008
 * Time: 12:35:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class QBDTRequestProcessorTest {

    private static final String QBDT_SIGN_ON = "<OFX><SIGNONMSGSRQV1><SONRQ><DTCLIENT>20080124185539<USERID>8574536<USERPASS>test1234<LANGUAGE>ENG<APPVER>17.00.R.6/20801#accountant<APPID>QBWPRO</SONRQ></SIGNONMSGSRQV1><I.PAYROLLMSGSRQV1><I.PAYROLLUPDATERQ><TOKEN>16<REJECTIFMISSING>N</I.PAYROLLUPDATERQ></I.PAYROLLMSGSRQV1></OFX>";
    private String source_company_id = OFXDataloader.companyPSID;
    public static String newline = System.getProperty("line.separator");


    @Before
    public void runBeforeEachTest() {
        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }

    @Test
    public void happyPath() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();

            // Need session because we are using SPCFCal
            Application.beginUnitOfWork();
            // Load Test OFX
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            Company company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
            long originalTokenInt = company.getCurrentToken();
            Application.commitUnitOfWork();

            // Processes the OFX String
            // Token count shoud go up by 2 (one for each payroll)
            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccessAndVerifyToken(happyPathOfxObj,AssistedConnectionInformation.getNewPayrollMessage(4),1);

            // Turn the OFX String into an OFX object
            // We will check a couple of items to spot check, but the
            //    heart of the validation is happening the sign on
            //    and request processor tests.
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnResponseCode);
            String responseTokenStr = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN();
            long responseTokenInt = Integer.parseInt(responseTokenStr);

            // Two payrolls offloaded, so request token + 2 payrolls + 1 since next avail token is returned.
            assertEquals(originalTokenInt+1,responseTokenInt);
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
            PayrollServices.commitUnitOfWork();
            //@TODO Implement once token implemented.
//            assertEquals(originalTokenInt+1,responseTokenInt);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }


    @Test
    public void missingPSIDTest() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            // Need session because we are using SPCFCal
            Application.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxObj = ofxDataLoader.loadHappyPathOFX();
            happyPathOfxObj.getSIGNONMSGSRQV1().getSONRQ().setUSERID(null);
            Application.commitUnitOfWork();
            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            QBDTRequestProcessor qbdtRequestProcessor= new QBDTRequestProcessor();
            String ofxResponseStr = qbdtRequestProcessor.processRequest(ofxStr, null,"Test IP");
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR,signOnResponseCode);
            ErrorMessage badOfxError = ErrorMessages.BadOFXError("");
            String errMsg = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE();
            assertEquals(badOfxError.getErrorDescription(),errMsg);
            QBDTTestHelper.verifyTransmissionDataNotSaved();
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }


    @Test
    public void testPSIDExtractionSuccess() {
        QBDTRequestProcessor qbdtRequestProcessor= new QBDTRequestProcessor();
        String origPSID = "8574536";
        String ofxStr =
                "<OFX>" + newline +
                        " <SIGNONMSGSRQV1>" + newline +
                        "  <SONRQ>" + newline +
                        "   <DTCLIENT>20080118004807" + newline +
                        "   <USERID>"+origPSID+"" + newline +
                        "   <USERPASS>mypin123" + newline +
                        "   <LANGUAGE>ENG" + newline +
                        "   <APPVER>50.00.R.3/20804#pro" + newline +
                        "   <APPID>QBWPRO" + newline +
                        "   <I.QBFILENAME>C:\\Documents and Settings\\All Users\\Documents\\Intuit\\QuickBooks\\Company Files\\Joes Cool Co.QBW" + newline +
                        "   <I.QBFILEID>c8e251053a984b3b9e107e8daa9bb640" + newline +
                        "   <I.IPADDRESS>FileInfo:QB_data_engine_18:172.17.214.180#10180" + newline +
                        "   <I.QBUSERNAME>Admin" + newline +
                        "  </SONRQ>" + newline +
                        " </SIGNONMSGSRQV1>" + newline +
                        "</OFX>" + newline;

        QBDTProcessResult<String> coPSIDPR = qbdtRequestProcessor.retrieveCompanyPSIDFromRequestString(ofxStr);
        assertTrue(coPSIDPR.isSuccess());
        String rtnPSID = coPSIDPR.getResult();
        org.junit.Assert.assertEquals(origPSID,rtnPSID);

    }

    @Test
    public void testEmptyPSIDExtractionSuccess() {
        QBDTRequestProcessor qbdtRequestProcessor= new QBDTRequestProcessor();
        String origPSID = "";
        String ofxStr =
                "<OFX>" + newline +
                        " <SIGNONMSGSRQV1>" + newline +
                        "  <SONRQ>" + newline +
                        "   <DTCLIENT>20080118004807" + newline +
                        "   <USERID>"+origPSID+"" + newline +
                        "   <USERPASS>mypin123" + newline +
                        "   <LANGUAGE>ENG" + newline +
                        "   <APPVER>50.00.R.3/20804#pro" + newline +
                        "   <APPID>QBWPRO" + newline +
                        "   <I.QBFILENAME>C:\\Documents and Settings\\All Users\\Documents\\Intuit\\QuickBooks\\Company Files\\Joes Cool Co.QBW" + newline +
                        "   <I.QBFILEID>c8e251053a984b3b9e107e8daa9bb640" + newline +
                        "   <I.IPADDRESS>FileInfo:QB_data_engine_18:172.17.214.180#10180" + newline +
                        "   <I.QBUSERNAME>Admin" + newline +
                        "  </SONRQ>" + newline +
                        " </SIGNONMSGSRQV1>" + newline +
                        "</OFX>" + newline;

        QBDTProcessResult<String> coPSIDPR = qbdtRequestProcessor.retrieveCompanyPSIDFromRequestString(ofxStr);
        assertTrue(coPSIDPR.isSuccess());
        String rtnPSID = coPSIDPR.getResult();
        org.junit.Assert.assertEquals(origPSID,rtnPSID);

    }

    @Test
    public void ddCustomerSendsBalanceFile() throws Exception {
        OFXDataloader ofxDataLoader = new OFXDataloader();
        com.intuit.sbd.payroll.psp.common.ofx.request.OFX balanceFileObj = ofxDataLoader.loadBalanceFile();
        QBDTTestHelper.processRequestPayrollError(balanceFileObj,ErrorMessages.DDCustomerBalanceFileError());
    }

    @Test
    public void testCanceledAssistedCompany() {
        String psid = "606104305";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled));
        assertSuccess(PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled));
        PayrollServices.commitUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitSyncRequest(company, company.getCurrentToken(), true);
        assertEquals("term response", QBOFX.TAX_MODES.TERMINATED, response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE());
    }

    @Test
    public void testCanceledAssisted_CanceledDD() {
        String psid = "606104305"; 
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);

        // submit an assisted request
        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        // cancel assisted
        // refresh company before cancel
        company = DataLoadServices.refreshCompany(company);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled));
        PayrollServices.commitUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitSyncRequest(company, company.getCurrentToken(), true);
        assertEquals("No tax service", QBOFX.TAX_MODES.TERMINATED, response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE());

        // cannot submit balf for dd company
        ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequestStringResponse(ofx, false);

        // make sure company has canceled assisted service and active dd service
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("assisted service status", ServiceSubStatusCode.Cancelled, company.getService(ServiceCode.Tax).getStatusCd());
        assertEquals("dd service status", ServiceSubStatusCode.Cancelled, company.getService(ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }


}