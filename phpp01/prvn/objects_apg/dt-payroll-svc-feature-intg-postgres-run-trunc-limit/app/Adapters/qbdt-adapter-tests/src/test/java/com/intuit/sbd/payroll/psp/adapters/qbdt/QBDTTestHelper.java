package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.ISocketManager;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.CreateTransactionOffloadedEvents;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OFXToJavaMappingError;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertFalse;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Jun 2, 2008
 * Time: 1:43:02 PM
 */
public class QBDTTestHelper {

    static public final BigDecimal PER_TRANSMISSION_PRICE = BigDecimal.valueOf(3.00);
    static public final BigDecimal PER_PAYCHECK_PRICE = new BigDecimal(ServiceChargePrices.getNormalPerPayrollServiceCharge().toString());
    public static final String DYNAMIC_ERROR = "<DYNAMIC ERROR>";


    // ************************************** PUBLIC STUFF **************************************
    // BEGIN REFACTORED -------------------------------------------------------------------------------
    // PUBLIC METHODS
    public static String processOFXSyncRequestHappyPath(String token) throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathSyncRequest(token);
        String requestStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseStr = processRequest(happyPathOfxObj,QBOFX.MESSAGE_SEVERITY.INFO);
        verifyTransmissionDataSaved(requestStr,responseStr,AssistedConnectionInformation.getSyncMessage());

        return responseStr;
    }

    public static String processOFXSyncRequestDataRecovery(String token) throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathSyncRequest(token);
        String requestStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseStr = processRequest(happyPathOfxObj,QBOFX.MESSAGE_SEVERITY.INFO);
        verifyTransmissionDataSaved(requestStr,responseStr,AssistedConnectionInformation.getDataRecoveryMessage());

        return responseStr;
    }

    public static String processOFXSyncRequestHappyPath() throws Exception {
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        String token = company.getCurrentToken() + "";
        return processOFXSyncRequestHappyPath(token);
    }

    public static String processOFXPayrollRequestHappyPath() throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFX();
        String requestStr = OFXManager.javaRequestToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseStr = processRequest(happyPathOfxObj,QBOFX.MESSAGE_SEVERITY.INFO);
        verifyTransmissionDataSaved(requestStr,responseStr,AssistedConnectionInformation.getNewPayrollMessage(4));
        return responseStr;
    }

    public static String processOFXRequestSuccess(OFX syncOfxObj,String tranmissionDescription) throws Exception {
        String responseStr = processOFXRequestSuccess(syncOfxObj);
        String requestStr = OFXManager.javaRequestToOFX(syncOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        verifyTransmissionDataSaved(requestStr,responseStr,tranmissionDescription);
        return responseStr;
    }

    public static String processOFXRequestSuccessAndVerifyToken(OFX syncOfxObj,String tranmissionDescription,int tokenIncreaseCnt) throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        long tokenBefore = company.getCurrentToken();
        PayrollServices.commitUnitOfWork();
        String responseStr = processOFXRequestSuccess(syncOfxObj);
        String requestStr = OFXManager.javaRequestToOFX(syncOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        long tokenAfter = company.getCurrentToken();
        PayrollServices.commitUnitOfWork();
        assertEquals(tokenBefore+tokenIncreaseCnt,tokenAfter);
        verifyTransmissionDataSavedIncludingToken(requestStr,responseStr,tranmissionDescription,tokenBefore,tokenAfter);
        return responseStr;
    }

    public static String processOFXRequestPayrollSubmitSuccess(OFX syncOfxObj) throws Exception {
        String responseStr = processRequest(syncOfxObj,QBOFX.MESSAGE_SEVERITY.INFO);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX resposneOFX = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertEquals(resposneOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY(),QBOFX.MESSAGE_SEVERITY.INFO);
        return responseStr;
    }

    public static String processOFXRequestSuccess(OFX syncOfxObj) throws Exception {
        String responseStr = processRequest(syncOfxObj,QBOFX.MESSAGE_SEVERITY.INFO);
        return responseStr;
    }

    public static String processOFXRequestSignOnError(OFX syncOfxObj) throws Exception {
        String responseStr = processRequest(syncOfxObj,QBOFX.MESSAGE_SEVERITY.ERROR);
        return responseStr;
    }

    public static String processOFXRequestSignOnError(OFX signErrorOfxObj,ErrorMessage errMsg, String tranmissionDescription) throws Exception {
        String requestStr = OFXManager.javaRequestToOFX(signErrorOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String ofxResponseStr = processOFXRequestSignOnError(signErrorOfxObj);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        TestCase.assertEquals(errMsg.getErrorDescription(),ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE());
        verifyTransmissionDataSaved(requestStr,ofxResponseStr,tranmissionDescription);
        verifyTransmissionEventErrorExists(errMsg.getTransmissionErrorDescription());
        return ofxResponseStr;
    }


    public static String processOFXRequestSignOnError(OFX signErrorOfxObj,ErrorMessage errMsg) throws Exception {
        return processOFXRequestSignOnError(signErrorOfxObj,errMsg,QBDTTransmissionMessageDescription.getErrorDescriptor());
    }

    public static String offloadCompanyPayroll(OFX ofxRequest) throws Exception {
        updateOFXWithNextCompanyToken(ofxRequest);
        String ofxResponseStr = processOFXRequestSuccess(ofxRequest);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
        TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnResponseCode);

        runOffload(ofxRequest.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());

        return ofxResponseStr;
    }

    public static void runOffload(String paycheckDate) {
        // Run offload
        PayrollServices.beginUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        offloadDate.setValues(Integer.parseInt(paycheckDate.substring(0,4)),
                Integer.parseInt(paycheckDate.substring(4,6)),
                Integer.parseInt(paycheckDate.substring(6,8)),
                23,
                0,
                0,
                0);

        CalendarUtils.addBusinessDays(offloadDate,-2);
        PSPDate.setPSPTime(offloadDate);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Create fee offload events
        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

    }

    // PRIVATE METHODS
    private static void verifyTransmissionDataSaved(String requestOfxStr, String ofxResponseStr,String tranmissionDescription) throws Exception {
        verifyTransmissionDataSaved(requestOfxStr,ofxResponseStr);
            DomainEntitySet<SourceSystemTransmission> transmissionList = getTransmissionListOrderingByCreateDateSecondary();
            SourceSystemTransmission transmissionObj = transmissionList.get(0);
            assertEquals(tranmissionDescription, transmissionObj.getDescription());
    }

    private static void verifyTransmissionDataSavedIncludingToken(String requestOfxStr, String ofxResponseStr,String tranmissionDescription,long requestToken,long responseToken) throws Exception {

        verifyTransmissionDataSaved(requestOfxStr,ofxResponseStr);
            DomainEntitySet<SourceSystemTransmission> transmissionList = getTransmissionListOrderingByCreateDateSecondary();
            SourceSystemTransmission transmissionObj = transmissionList.get(0);
            assertEquals(tranmissionDescription, transmissionObj.getDescription());

            assertEquals(requestToken, transmissionObj.getRequestToken());
            assertEquals(responseToken, transmissionObj.getResponseToken());
    }

    private static void verifyTransmissionDataSaved(String requestOfxStr, String ofxResponseStr) throws Exception {
        Boolean manageTransaction = !ApplicationSecondary.hasActiveTransaction();
        try {
            //creating active session to access clob data
            if (manageTransaction) {
                ApplicationSecondary.beginUnitOfWork();
            }

            DomainEntitySet<SourceSystemTransmission> transmissionList = getTransmissionListOrderingByCreateDateSecondary();
            SourceSystemTransmission transmissionObj = transmissionList.get(0);

            String ofx = formatOfx(transmissionObj.getRequestDocument());

            assertEquals(QBDTRequestProcessor.stripOutPassword(requestOfxStr), ofx);

            com.intuit.sbd.payroll.psp.common.ofx.request.OFX requestOfxObj = null;
            boolean convertedSuccessfully = true;
            try {
                requestOfxObj = OFXManager.ofxRequestToJava(requestOfxStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            } catch (Exception e) {
                convertedSuccessfully = false;
            }
            if (convertedSuccessfully && !QBOFX.hasInvalidBankAccount(requestOfxStr)) {
                com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
                if (ofxResponseObj.getIPAYROLLMSGSRSV1() != null) {
                    long originalTokenInt = QBOFX.tokenVal(requestOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN());
                    assertEquals(originalTokenInt, transmissionObj.getRequestToken());
                }
            }
            ofx = formatOfx(transmissionObj.getResponseDocument());
            assertEquals(ofxResponseStr, ofx);
        } finally{
            if (manageTransaction) {
                ApplicationSecondary.rollbackUnitOfWork();
            }
        }
    }

    private static void verifyTransmissionEventError(String transmissionEventErrorDesc) {
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> companyTransmissionEventList = CompanyEvent.findCompanyEvents(company,EventTypeCode.TransmissionError,null,null,null);
        assertTrue(companyTransmissionEventList.size() > 0);
        CompanyEvent coEvent = companyTransmissionEventList.get(companyTransmissionEventList.size()-1);
        DomainEntitySet<CompanyEventDetail> tranmissionErrorMsgList = coEvent.getCompanyEventDetails(EventDetailTypeCode.ErrorMessage);
        assertEquals(1,tranmissionErrorMsgList.size());
        if (transmissionEventErrorDesc.compareTo(DYNAMIC_ERROR)==0) {
            assertNotNull(tranmissionErrorMsgList.get(0).getValue());
        } else {
            assertEquals(transmissionEventErrorDesc,tranmissionErrorMsgList.get(0).getValue());
        }
        DomainEntitySet<CompanyEventDetail> tranmissionErrorIdList = companyTransmissionEventList.get(companyTransmissionEventList.size()-1).getCompanyEventDetails(EventDetailTypeCode.TransmissionId);
        assertEquals(1,tranmissionErrorIdList.size());

            DomainEntitySet<SourceSystemTransmission> transmissionList = getTransmissionListOrderingByCreateDateSecondary();
            SourceSystemTransmission transmissionObj = transmissionList.get(0);
            assertEquals(transmissionObj.getTransmissionIdentifier(),tranmissionErrorIdList.get(0).getValue());
    }

    public static void verifyTransmissionEventErrorCode(Company pCompany,String pMessageCode) {
        DomainEntitySet<CompanyEvent> companyEventList = CompanyEvent.findCompanyEvents(pCompany,EventTypeCode.TransmissionError);
        junit.framework.Assert.assertEquals(companyEventList.size(),1);
        CompanyEvent event = companyEventList.get(0);
        DomainEntitySet<CompanyEventDetail> errorMessage = event.getCompanyEventDetails(EventDetailTypeCode.ErrorMessage);
        boolean expectedErrorMessage = (errorMessage.size()>0 && errorMessage.get(0).getValue().contains(pMessageCode));
        assertTrue(expectedErrorMessage);
    }

    // END REFACTORED -------------------------------------------------------------------------------
    // BEGIN REFACTORED AND NOW DEPRICATED ----------------------------------------------------------------

    @Deprecated
    public static void verifyTransmissionDataSaved(String requestOfxStr, String ofxResponseStr,TransmissionType transmissionType) throws Exception {
        verifyTransmissionDataSaved(QBDTRequestProcessor.stripOutPassword(requestOfxStr), ofxResponseStr,transmissionType,1);
            DomainEntitySet<SourceSystemTransmission> transmissionList = getTransmissionListOrderingByCreateDateSecondary();
            SourceSystemTransmission transmissionObj = transmissionList.get(0);
            assertEquals(transmissionType, transmissionObj.getType());
    }

    @Deprecated
    public static void verifyTransmissionDataSaved(String requestOfxStr, String ofxResponseStr,TransmissionType transmissionType, int transmissionCnt) throws Exception {
        verifyTransmissionDataSaved(requestOfxStr, ofxResponseStr);
            DomainEntitySet<SourceSystemTransmission> transmissionList = getTransmissionListOrderingByCreateDateSecondary();
            assertEquals(transmissionCnt,transmissionList.size());
    }

    @Deprecated
    public static void verifyTransmissionDataNotSaved() {
            DomainEntitySet<SourceSystemTransmission> transmissionList = PayrollServices.entityFinderSecondary.find(SourceSystemTransmission.class);
            assertEquals(0,transmissionList.size());
    }

    // END REFACTORED AND NOW DEPRICATED ----------------------------------------------------------------

    // BEGIN NOT REFACTORED YET -----------------------------------------------------------------------

    public static String processRequestSignOnErrorDynamicTransmissionError(OFX ofxObj,ErrorMessage errorMessage) throws Exception {
        String requestOfxStr = OFXManager.javaToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        return processRequestSignOnErrorDynamicTransmissionError(requestOfxStr,errorMessage);
    }

    public static String processRequestSignOnErrorDynamicTransmissionError(String ofxStr,ErrorMessage errorMessage) throws Exception {
        String ofxResponseStr = processRequest(ofxStr,QBOFX.MESSAGE_SEVERITY.ERROR,null);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        TestCase.assertEquals(errorMessage.getErrorDescription(),ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE());
        verifyTransmissionEventErrorExists(errorMessage.getTransmissionErrorDescription());
        return ofxResponseStr;
    }

//    public static String processRequestSignOnError(String ofxStr,ErrorMessage expectedSignOnErrorMessage) throws Exception {
//        String ofxResponseStr = processRequest(ofxStr,QBOFX.MESSAGE_SEVERITY.ERROR,null);
//        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//        TestCase.assertEquals(expectedSignOnErrorMessage.getErrorDescription(),ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE());
//        verifyTransmissionEventError(expectedSignOnErrorMessage.getTransmissionErrorDescription());
//        return ofxResponseStr;
//    }

//    public static String processOFXRequestSignOnError(OFX ofxObj,ErrorMessage errorMessage) throws Exception {
//        String requestOfxStr = OFXManager.javaToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//        return processRequestSignOnError(requestOfxStr,errorMessage);
//    }

    public static String processRequestPayrollErrorDynamicTransmissionError(String ofxStr,ErrorMessage errorMessage) throws Exception {
        String ofxResponseStr = processRequest(ofxStr,QBOFX.MESSAGE_SEVERITY.INFO,QBOFX.MESSAGE_SEVERITY.ERROR);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        TestCase.assertEquals(errorMessage.getErrorDescription(),ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE());
        verifyTransmissionEventErrorExists(errorMessage.getTransmissionErrorDescription());
        return ofxResponseStr;
    }

    public static String processRequestPayrollError(ErrorMessage expectedPayrollMessage) throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFX();
        return processRequestPayrollError(happyPathOfxObj,expectedPayrollMessage);
    }

    public static String processRequestPayrollError(String ofxStr,ErrorMessage expectedPayrollMessage, String expectedSignOnMessageSeverity) throws Exception {
        String ofxResponseStr = processRequest(ofxStr,expectedSignOnMessageSeverity,QBOFX.MESSAGE_SEVERITY.ERROR);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        // error message will be escaped when converted to an OFX string
        String errStr = expectedPayrollMessage.getErrorDescription();
        if(ofxResponseObj.getIPAYROLLMSGSRSV1() != null){
            TestCase.assertEquals(errStr,ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE());            
        }
        verifyTransmissionEventError(expectedPayrollMessage.getTransmissionErrorDescription());
        return ofxResponseStr;
    }

    public static String processRequestPayrollError(String ofxStr,ErrorMessage expectedPayrollMessage) throws Exception {
        return processRequestPayrollError(ofxStr,expectedPayrollMessage, QBOFX.MESSAGE_SEVERITY.INFO);
    }

    public static String processRequestPayrollError(OFX ofxObj,ErrorMessage errorMessage) throws Exception {
        String requestOfxStr = OFXManager.javaToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        return processRequestPayrollError(requestOfxStr,errorMessage);
    }

    public static void verifyTransmissionMessageDuplicatePayroll(boolean shouldExist) {
            Expression<SourceSystemTransmission> query =
                    new Query<SourceSystemTransmission>()
                            .Where(SourceSystemTransmission.CreatedDate().isNotNull())
                            .OrderBy(SourceSystemTransmission.CreatedDate().Descending());

            DomainEntitySet<SourceSystemTransmission> transmissionList = PayrollServices.entityFinderSecondary.find(SourceSystemTransmission.class, query);
            if (shouldExist) {
                assertTrue(transmissionList.get(0).getDescription().indexOf(QBDTTransmissionMessageDescription.getDuplicatePaycheckDescriptor())!=-1);
            } else {
                assertFalse(transmissionList.get(0).getDescription().indexOf(QBDTTransmissionMessageDescription.getDuplicatePaycheckDescriptor())!=-1);
            }
    }

    public static void typicalRunBeforeEachTest() {

        SIGNONMSGSRQV1 signOnMsg = null;
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");
        Company company = companyQB1DataLoader.persistQBCompany1();
        ObjectFactory objFact = new ObjectFactory();
        signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();
        signOnRequest.setUSERID(company.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateOffering(company, OfferingCode.DIYDDSTD, "DIYDD-STD");
    }
    public static void typicalRunAfterEachTest() {

        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    public static void updateOFXWithNextCompanyToken(OFX ofx) {
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        String nextTokenStr = (company.getCurrentToken()) + "";
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(nextTokenStr);
    }

    @Deprecated
    public static void verifyEventExists(EventTypeCode eventType) {
        verifyEventExists(eventType,1);
    }

    public static void verifyEventExists(EventTypeCode eventType,int expectedCnt) {
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        int foundCnt = 0;
        DomainEntitySet<CompanyEvent> companyEventList = CompanyEvent.findCompanyEvents(company);
        for (CompanyEvent coEvent : companyEventList) {
            if (coEvent.getEventTypeCd()==eventType) {
                foundCnt++;
            }
        }
        if (foundCnt != expectedCnt) {
            TestCase.fail("Event type code '"+eventType.toString()+"' expected "+expectedCnt+ " times but found "+foundCnt+" times.");
        }
    }

    public static void verifyCompanyEventDetailExists(EventDetailTypeCode eventType,int expectedCnt) {
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        int foundCnt = 0;
        DomainEntitySet<CompanyEventDetail> companyEventDetails = CompanyEventDetail.findCompanyEventDetails(company);
        for (CompanyEventDetail coEvent : companyEventDetails) {
            if (coEvent.getEventDetailTypeCd()==eventType) {
                foundCnt++;
            }
        }
        if (foundCnt != expectedCnt) {
            TestCase.fail("Company Event Detail type code '"+eventType.toString()+"' expected "+expectedCnt+ " times but found "+foundCnt+" times.");
        }
    }

    // ************************************** PRIVATE STUFF **************************************
    private  static DomainEntitySet<SourceSystemTransmission> getTransmissionListOrderingByCreateDate() {
        Expression<SourceSystemTransmission> query =
                new Query<SourceSystemTransmission>()
                          .Where(SourceSystemTransmission.CreatedDate().isNotNull())
                          .OrderBy(SourceSystemTransmission.CreatedDate().Descending());

        DomainEntitySet<SourceSystemTransmission> transmissionList = PayrollServices.entityFinder.find(SourceSystemTransmission.class,query);
        return transmissionList ;
    }

    private  static DomainEntitySet<SourceSystemTransmission> getTransmissionListOrderingByCreateDateSecondary() {
        Expression<SourceSystemTransmission> query =
                new Query<SourceSystemTransmission>()
                        .Where(SourceSystemTransmission.CreatedDate().isNotNull())
                        .OrderBy(SourceSystemTransmission.CreatedDate().Descending());

        DomainEntitySet<SourceSystemTransmission> transmissionList = PayrollServices.entityFinderSecondary.find(SourceSystemTransmission.class,query);
        return transmissionList ;
    }

    private static void verifyTransmissionEventErrorExists(String transmissionErrorDesc) {
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> companyTransmissionEventList = CompanyEvent.findCompanyEvents(company,EventTypeCode.TransmissionError,null,null,null);
        assertTrue(companyTransmissionEventList.size() > 0);
        DomainEntitySet<CompanyEventDetail> tranmissionErrorList = companyTransmissionEventList.get(companyTransmissionEventList.size()-1).getCompanyEventDetails(EventDetailTypeCode.ErrorMessage);
        assertEquals(1,tranmissionErrorList.size());
        assertNotNull(tranmissionErrorList.get(0).getValue());
    }


    private static String formatOfx(String ofx) {
        try {
            StringBuffer strOut = new StringBuffer();
            String line;

            Reader inputString = new StringReader(ofx);
            BufferedReader br = new BufferedReader(inputString);

            while ((line = br.readLine()) != null) {
                strOut.append(line);
                strOut.append('\n');
            }

            return strOut.toString();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    static String stripOutPassword(String requestOFX) {
        Pattern p1 = Pattern.compile("((^\\s*<USERPASS>)(.*?)($))", Pattern.MULTILINE | Pattern.UNIX_LINES);
        Matcher m1 = p1.matcher(requestOFX);
        StringBuffer sb1 = new StringBuffer();
        while (m1.find()){
            m1.appendReplacement(sb1,"$----$4");
        }
        m1.appendTail(sb1);
        return sb1.toString();
    }

    /**
     * Return ofxResponseStr.
     *
     * @param ofxObj
     * @return
     * @throws Exception
     */
    private static String processRequest(OFX ofxObj) throws Exception {
        return processRequest(ofxObj,QBOFX.MESSAGE_SEVERITY.INFO);
    }

    public static String processRequest(String ofxStr,String expectedSignOnMessageSeverity,String expectedParyollMessageSeveriry) throws Exception {
        boolean returnSuccessful = true;
        if(expectedSignOnMessageSeverity != null) {
            returnSuccessful = QBOFX.MESSAGE_SEVERITY.INFO.equals(expectedSignOnMessageSeverity);
        }

        if(expectedParyollMessageSeveriry != null) {
            returnSuccessful = QBOFX.MESSAGE_SEVERITY.INFO.equals(expectedParyollMessageSeveriry);
        }

        String ofxResponseStr;
        try {
            ofxResponseStr = submitQBDTRequestStringResponse(OFXManager.ofxRequestToJava(ofxStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES), returnSuccessful);
        } catch (OFXToJavaMappingError e) {
            TestHttpServletResponse testHttpServletResponse = new TestHttpServletResponse();
            QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
            qbdtPegServlet.service(new TestHttpServletRequest(ofxStr), testHttpServletResponse);
            ofxResponseStr = testHttpServletResponse.toString();
        }
        verifyTransmissionDataSaved(ofxStr, ofxResponseStr);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String signOnSeverity = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
        TestCase.assertEquals(expectedSignOnMessageSeverity,signOnSeverity);
        if (expectedParyollMessageSeveriry != null && ofxResponseObj.getIPAYROLLMSGSRSV1() !=null ) {
            String payrollSeverity = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(expectedParyollMessageSeveriry,payrollSeverity);
        }
        return ofxResponseStr;
    }

    private static String processRequest(OFX ofxObj,String messageSeverity) throws Exception {
        String requestOfxStr = OFXManager.javaToOFX(ofxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        return processRequest(requestOfxStr,messageSeverity,null);
    }

    // END NOT REFACTORED YET -----------------------------------------------------------------------

    public static void assertNoErrorRequests(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<QbdtUnprocessedRequest> qbdtUnprocessedRequests =  QbdtUnprocessedRequest.findUnprocessedRequests(pCompany, false, QbdtRequestStatus.Error);
        Assert.assertEquals("Assisted unprocessed requests with errors: " + (qbdtUnprocessedRequests.size() > 0 ? qbdtUnprocessedRequests.get(0).getErrorMessage() : ""), 0, qbdtUnprocessedRequests.size());
        PayrollServices.rollbackUnitOfWork();
    }

    public static final String UNSUCCESSFUL_RESPONSE = "<OFX>\n" +
                    " <SIGNONMSGSRSV1>\n" +
                    "  <SONRS>\n" +
                    "   <STATUS>\n" +
                    "    <CODE>101\n" +
                    "    <SEVERITY>ERROR\n" +
                    "    <MESSAGE>An Error message\n" +
                    "   </STATUS>\n" +
                    "   <DTSERVER>20080725002636\n" +
                    "   <LANGUAGE>ENG\n" +
                    "  </SONRS>\n" +
                    " </SIGNONMSGSRSV1>\n" +
                    "</OFX>";

    public static final String SUCCESSFUL_RESPONSE = "AS400Response";

    public static final String SUCCESSFUL_OFX_RESPONSE = "<OFX>\n" +
            "<SIGNONMSGSRSV1>\n" +
            "<SONRS>\n" +
            "<STATUS>\n" +
            "<CODE>0\n" +
            "<SEVERITY>INFO\n" +
            "</STATUS>\n" +
            "<DTSERVER>20100901190114\n" +
            "<LANGUAGE>ENG\n" +
            "</SONRS>\n" +
            "</SIGNONMSGSRSV1>\n" +
            "<I.PAYROLLMSGSRSV1>\n" +
            "<I.PAYROLLUPDATERS>\n" +
            "<TOKEN>82\n" +
            "<I.PAYROLLTXNEXTID>64\n" +
            "<I.PAYCHKNEXTID>41\n" +
            "<I.EMPNEXTID>7\n" +
            "<I.PITEMNEXTID>24\n" +
            "<I.PAYROLLTRNRS>\n" +
            "<TRNUID>D8E79D80-7ABF-1000-B771-DF9B1D700026\n" +
            "<STATUS>\n" +
            "<CODE>0\n" +
            "<SEVERITY>INFO\n" +
            "</STATUS>\n" +            
            "</I.PAYROLLTRNRS>\n" +
            "</I.PAYROLLUPDATERS>\n" +
            "</I.PAYROLLMSGSRSV1>\n" +
            "</OFX>\n";

    public static final String TERM_OFX_RESPONSE = "<OFX>\n" +
            "  <SIGNONMSGSRSV1>\n" +
            "    <SONRS>\n" +
            "      <STATUS>\n" +
            "        <CODE>0\n" +
            "        <SEVERITY>INFO\n" +
            "      </STATUS>\n" +
            "      <DTSERVER>20110412110509\n" +
            "      <LANGUAGE>ENG\n" +
            "    </SONRS>          \n" +
            "  </SIGNONMSGSRSV1>\n" +
            "  <I.PAYROLLMSGSRSV1>\n" +
            "    <I.PAYROLLUPDATERS>\n" +
            "      <TOKEN>189\n" +
            "      <I.PAYROLLTXNEXTID>341\n" +
            "      <I.PAYCHKNEXTID>451\n" +
            "      <I.EMPNEXTID>6\n" +
            "      <I.PITEMNEXTID>19\n" +
            "      <I.TAXSERVSTATUS>\n" +
            "        <I.TAXSERVMODE>TERMINATED\n" +
            "      </I.TAXSERVSTATUS>\n" +
            "      <I.DDSTATUS>\n" +
            "        <I.DDMODE>TERMINATED\n" +
            "      </I.DDSTATUS>\n" +
            "    </I.PAYROLLUPDATERS>\n" +
            "  </I.PAYROLLMSGSRSV1>\n" +
            "</OFX>";

    public static ISocketManager initializeSuccessfulMockSocket() {
        return initializeMockSocket(SUCCESSFUL_RESPONSE);
    }

    public static ISocketManager initializeSuccessfulOFXMockSocket() {
        return initializeMockSocket(SUCCESSFUL_OFX_RESPONSE);
    }

    public static ISocketManager initializeUnsuccessfulMockSocket() {
        return initializeMockSocket(UNSUCCESSFUL_RESPONSE);
    }

    public static ISocketManager initializeTermMockSocket() {
        return initializeMockSocket(TERM_OFX_RESPONSE);
    }

    public static ISocketManager initializeMockSocket(String pAS400Response) {
        try {
            IMocksControl ctrl = createStrictControl();
            ISocketManager mockSocket = ctrl.createMock(ISocketManager.class);
            mockSocket.open((String)anyObject(), anyInt(), anyInt(), anyInt());
            mockSocket.processRequest((String) anyObject());
            expectLastCall().andReturn(pAS400Response);
            mockSocket.close();
            ctrl.replay();
            return mockSocket;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static OFX setupCompanyAndSubmitBalanceFile(String pPSID) {
        return setupCompanyAndSubmitBalanceFile(pPSID, false);
    }

    public static OFX setupCompanyAndSubmitBalanceFile(String pPSID, boolean pTaxesOnly) {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, pPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        return submitBalanceFile(company, pTaxesOnly);
    }

    public static OFX submitBalanceFile(Company pCompany, boolean pTaxesOnly) {
        OFX ofx = OFXRequestGenerator.generateBalanceFile(pCompany.getSourceCompanyId(), false, false, pTaxesOnly, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        return ofx;
    }

    public static List<IPAYROLLRUN> submitPayroll(Company pCompany,
                                                  OFX pOfx,
                                                  Date pPaycheckDate,
                                                  Date pPeriodBeginDate,
                                                  Date pPeriodEndDate) throws Exception {
        return submitPayroll(pCompany, pOfx, pPaycheckDate, pPeriodBeginDate, pPeriodEndDate, true);
    }

    public static List<IPAYROLLRUN> submitPayroll(Company pCompany,
                                                  OFX pOfx,
                                                  Date pPaycheckDate,
                                                  Date pPeriodBeginDate,
                                                  Date pPeriodEndDate,
                                                  boolean pAssertLiabilityCheckLines) throws Exception {
        return submitPayroll(pCompany, pOfx, pPaycheckDate, pPeriodBeginDate, pPeriodEndDate, pAssertLiabilityCheckLines, true);
    }

    public static List<IPAYROLLRUN> submitPayroll(Company pCompany,
                                                  OFX pOfx,
                                                  Date pPaycheckDate,
                                                  Date pPeriodBeginDate,
                                                  Date pPeriodEndDate,
                                                  boolean pAssertLiabilityCheckLines,
                                                  boolean pSkipAssertions) throws Exception {

        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                pPaycheckDate,
                pPeriodBeginDate,
                pPeriodEndDate,
                false));

        PayrollServices.beginUnitOfWork();
        // find aeic
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(pCompany, "143");
        PayrollServices.rollbackUnitOfWork();

        // updated the tax amounts to match the payroll item id
        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                for (Iterator<ITAXLINE> iterator = ipaychk.getITAXLINE().iterator(); iterator.hasNext();) {
                    ITAXLINE itaxline = iterator.next();
                    if(itaxline.getIPITEMID().equals(companyLaw.getSourceId())) {
                        iterator.remove();
                    } else {
                        if(itaxline.getIAMT().contains("-")) {
                            itaxline.setIAMT("$-" + itaxline.getIPITEMID() + ".00");
                        } else {
                            itaxline.setIAMT("$" + itaxline.getIPITEMID() + ".00");
                        }
                    }
                }
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(pCompany.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(pCompany, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        if(pSkipAssertions) {
            IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
            assertNotNull("Payroll response", ipayrollrs);

            Assert.assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTX().size());
            com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

            OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(pCompany, ipayrolltx, ipayrolltx.getIDTPAYPDEND());

            SpcfDecimal total = SpcfMoney.ZERO;
            total.setScale(2);
            for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
                if(pAssertLiabilityCheckLines) {
                    if(itxline.getIMEMO() != null && itxline.getIMEMO().equals(QBOFX.MEMOS.DEBIT_REDUCED.APPLIED_OVERPAID_TAX_FUNDS)) {
                        assertNotNull("payroll item id", itxline.getIPITEMID());
                        SpcfMoney overpaymentAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                        assertTrue("overpayment is not negative", overpaymentAmount.isLessThan(SpcfMoney.ZERO));
                        total = total.add(overpaymentAmount);
                    } else {
                        if(itxline.getIPITEMID() == null || itxline.getIISDD() != null) {
                            // fees and dd
                            SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                            total = total.add(amount);
                        } else {
                            SpcfDecimal amount = SpcfDecimal.createInstance(Integer.parseInt(itxline.getIPITEMID())*5);
                            total = total.add(amount);
                            Assert.assertEquals("amount", "$" + amount + ".00", itxline.getIAMT());
                        }
                    }
                } else {
                    SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                    total = total.add(amount);
                }
            }

            if(total.isGreaterThan(SpcfMoney.ZERO)) {
                Assert.assertEquals("amount", "$-" + total, ipayrolltx.getIAMT());
            } else {
                Assert.assertEquals("amount", "$" + total, ipayrolltx.getIAMT());
            }

            QBDTTestHelper.assertNoErrorRequests(pCompany);

            PayrollServices.beginUnitOfWork();
            pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
            OFXAssert.assertPayrolls(payrollOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), pCompany);
            OFXAssert.assertPayrollTransactions(payrollOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), pCompany);
            PayrollServices.rollbackUnitOfWork();
        }

        return payrollRuns;
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitPayrollWithNoDetails(Company pCompany,
                                                  OFX pOfx,
                                                  Date pPaycheckDate,
                                                  Date pPeriodBeginDate,
                                                  Date pPeriodEndDate,
                                                  boolean pModification,
                                                  boolean pShouldReturnSuccessful) throws Exception {

        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                pPaycheckDate,
                pPeriodBeginDate,
                pPeriodEndDate,
                false));

        PayrollServices.beginUnitOfWork();
        PayrollServices.rollbackUnitOfWork();
        for(IPAYROLLRUN payrollRun:payrollRuns)  {
            for(IPAYCHK paychk:payrollRun.getIPAYCHK()) {
                paychk.getITAXLINE().clear();
            }
        }

        if(pModification) {
            for(IPAYROLLRUN payrollRun:payrollRuns)  {
                for(IPAYCHK paychk:payrollRun.getIPAYCHK()) {
                    payrollRun.getIPAYCHKMOD().add(paychk);
                }
                payrollRun.getIPAYCHK().clear();
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(pCompany.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(pCompany, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx, pShouldReturnSuccessful);
        return response;
    }


    public static String submitQBDTRequestStringResponse(OFX pRequest) {
        return submitQBDTRequestStringResponse(pRequest, true);
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitQBDTRequest(OFX pRequest) {
        return submitQBDTRequest(pRequest, true);
    }

    public static String submitQBDTRequestStringResponse(OFX pRequest,
                                                         boolean pInitializeSuccessfulMockSocket,
                                                         boolean pShouldReturnSuccessful,
                                                         ISocketManager pISocketManager) {
        String ofxString = OFXManager.javaRequestToOFX(pRequest, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        TestHttpServletResponse testHttpServletResponse = new TestHttpServletResponse();
        QBDTPegServlet qbdtPegServlet;
        if(pISocketManager != null) {
            qbdtPegServlet = new QBDTPegServlet(pISocketManager);
        } else {
            qbdtPegServlet = new QBDTPegServlet(pInitializeSuccessfulMockSocket ?
                                                        QBDTTestHelper.initializeSuccessfulOFXMockSocket() :
                                                        QBDTTestHelper.initializeUnsuccessfulMockSocket());
        }
        try {
            qbdtPegServlet.service(new TestHttpServletRequest(ofxString), testHttpServletResponse);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        if(pShouldReturnSuccessful) {
            assertFalse("response contains errors " + getErrorMessage(pRequest.getSIGNONMSGSRQV1().getSONRQ().getUSERID()), QBOFX.ofxStringContainsErrorSeverity(testHttpServletResponse.toString()));
        } else {
            assertTrue("response does not contain errors " + testHttpServletResponse.toString(), QBOFX.ofxStringContainsErrorSeverity(testHttpServletResponse.toString()));
        }

        return testHttpServletResponse.toString();
    }

    public static String submitQBDTRequestStringResponse(OFX pRequest,
                                                         boolean pShouldReturnSuccessful) {
        String ofxString = OFXManager.javaRequestToOFX(pRequest, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        TestHttpServletResponse testHttpServletResponse = new TestHttpServletResponse();
        QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();

        try {
            qbdtPegServlet.service(new TestHttpServletRequest(ofxString), testHttpServletResponse);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        if(pShouldReturnSuccessful) {
            assertFalse("response contains errors " + getErrorMessage(pRequest.getSIGNONMSGSRQV1().getSONRQ().getUSERID()), QBOFX.ofxStringContainsErrorSeverity(testHttpServletResponse.toString()));
        } else {
            assertTrue("response does not contain errors " + testHttpServletResponse.toString(), QBOFX.ofxStringContainsErrorSeverity(testHttpServletResponse.toString()));
        }

        return testHttpServletResponse.toString();
    }

    private static String getErrorMessage(String pUSERID) {
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<CompanyEventDetail> companyEventDetails = CompanyEventDetail.findCompanyEventDetails(Company.findCompany(pUSERID, SourceSystemCode.QBDT)).find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ErrorMessage));
            if(companyEventDetails.size() > 0) {
                return companyEventDetails.get(0).toString();
            }
            return "";
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitQBDTRequest(OFX pRequest,
                                                                                       boolean pShouldReturnSuccessful) {
        try {
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX response =
                    OFXManager.ofxResponseToJava(submitQBDTRequestStringResponse(pRequest, pShouldReturnSuccessful), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertNotNull("Response", response);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String submitQBDTRequestStringResponse(String pRequest,
                                                         boolean pShouldReturnSuccessful) {
        TestHttpServletResponse testHttpServletResponse = new TestHttpServletResponse();
        QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
        try {
            qbdtPegServlet.service(new TestHttpServletRequest(pRequest), testHttpServletResponse);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        if(pShouldReturnSuccessful) {
            assertFalse("response contains errors " + testHttpServletResponse.toString(), QBOFX.ofxStringContainsErrorSeverity(testHttpServletResponse.toString()));
        } else {
            assertTrue("response does not contain errors " + testHttpServletResponse.toString(), QBOFX.ofxStringContainsErrorSeverity(testHttpServletResponse.toString()));
        }

        return testHttpServletResponse.toString();
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX voidPaychecks(Company pCompany, List<IPAYROLLRUN> pPayrollRuns, boolean pPartialVoid) throws Exception {
        pCompany = DataLoadServices.refreshCompany(pCompany);
        OFX voidOfx = new OFX();
        List<IPAYROLLRUN> voidPayrolls = new ArrayList<IPAYROLLRUN>();
        for (IPAYROLLRUN ipayrollrun : pPayrollRuns) {
            IPAYROLLRUN voidIpayrollrun = new IPAYROLLRUN();
            voidIpayrollrun.setIDTPAYCHKS(ipayrollrun.getIDTPAYCHKS());
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                if(!pPartialVoid || Integer.parseInt(ipaychk.getIPAYCHKID()) % 2 == 0) {
                    IPAYCHK ipaychkmod = new IPAYCHK();
                    ipaychkmod.setIPAYCHKID(ipaychk.getIPAYCHKID());
                    ipaychkmod.setIEMPID(ipaychk.getIEMPID());
                    ipaychkmod.setIPAYCHKTYPE(ipaychk.getIPAYCHKTYPE());
                    ipaychkmod.setIEMPNAME(ipaychk.getIEMPNAME());
                    ipaychkmod.setICLASS(ipaychk.getICLASS());
                    ipaychkmod.setIACCTNAME(ipaychk.getIACCTNAME());
                    ipaychkmod.setIPAYCHKINFO(ipaychk.getIPAYCHKINFO());
                    ipaychkmod.getIPAYCHKINFO().setICHKNUM("c" + ipaychkmod.getIPAYCHKINFO().getICHKNUM());
                    ipaychkmod.setIVOID("Y");
                    ipaychkmod.setIDTPAYPDBEGIN(ipaychk.getIDTPAYPDBEGIN());
                    ipaychkmod.setIDTPAYPDEND(ipaychk.getIDTPAYPDEND());
                    ipaychkmod.setIMEMO(ipaychk.getIMEMO());
                    ipaychkmod.setICLEARED("9");
                    voidIpayrollrun.getIPAYCHKMOD().add(ipaychkmod);
                }
            }
            voidPayrolls.add(voidIpayrollrun);
        }
        voidOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(pCompany.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 voidPayrolls);
        voidOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(pCompany, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(voidOfx);

        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        DomainEntitySet<PayrollRun> domainPayrollRuns = PayrollRun.findPayrollRuns(pCompany);
        if (!pPartialVoid && domainPayrollRuns.size() == 1) {
            OFXAssert.assertPayrolls(voidOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), pCompany);
        }
        PayrollServices.rollbackUnitOfWork();

        assertNotNull("Response", response);

        return response;
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitPayroll(Company pCompany, PayrollRunDTO pPayrollRunDTO) {
        return submitPayroll(pCompany, pPayrollRunDTO, true);
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitPayroll(Company pCompany, PayrollRunDTO pPayrollRunDTO, boolean pUpdatePaycheckIds) {
        return submitPayroll(pCompany, pPayrollRunDTO, pUpdatePaycheckIds, true, false);
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitPayroll(Company pCompany, PayrollRunDTO pPayrollRunDTO, boolean pUpdatePaycheckIds, boolean pShouldReturnSuccessful, boolean pIsModification) {
        if(pUpdatePaycheckIds) {
            pCompany = DataLoadServices.refreshCompany(pCompany);
            int nextPaycheckId = Integer.parseInt(pCompany.getNextPaycheckId());
            for (PaycheckDTO paycheckDTO : pPayrollRunDTO.getPaychecks()) {
                paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            }
        }

        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(pCompany, pPayrollRunDTO, false, pIsModification));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(pCompany.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(pCompany, true, ipayrolltrnrq));

        return QBDTTestHelper.submitQBDTRequest(payrollOfx, pShouldReturnSuccessful);
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitSyncRequest(Company pCompany, Long pToken, boolean pShouldReturnSuccessfully) {
        if(pToken == null) {
            pCompany = DataLoadServices.refreshCompany(pCompany);
            pToken = pCompany.getCurrentToken();
        }
        return QBDTTestHelper.submitQBDTRequest(OFXRequestGenerator.generateSyncRequest(pCompany.getSourceCompanyId(), pToken), pShouldReturnSuccessfully);
    }

    public static com.intuit.sbd.payroll.psp.common.ofx.response.OFX submitZeroPayroll(Company pCompany) {
        pCompany = DataLoadServices.refreshCompany(pCompany);

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(pCompany.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(pCompany, true, ipayrolltrnrq));

        return QBDTTestHelper.submitQBDTRequest(payrollOfx);
    }
}
