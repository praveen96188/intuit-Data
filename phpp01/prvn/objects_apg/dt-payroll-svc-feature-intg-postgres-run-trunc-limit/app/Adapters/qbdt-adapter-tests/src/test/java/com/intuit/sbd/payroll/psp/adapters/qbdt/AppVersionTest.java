package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SourcePayrollParameterDTO;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

public class AppVersionTest {

    private String source_company_id = null;
    private Integer minAppQBVersionSupported = null;
    public AppVersionTest() {
        String minAppQBVersionSupportedStr =
                SourcePayrollParameter.findSourcePayrollParameter(
                    SourceSystemCode.QBDT, SourcePayrollParameterCode.MinQBVersionSupported).getParameterValue();
        minAppQBVersionSupported = new Integer(minAppQBVersionSupportedStr);
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
    public void testPreReleaseVersion() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            Application.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest(company.getCurrentToken() + "");
            Application.commitUnitOfWork();
            String preReleaseVersion = "22.01.P.101/555533#pro";
            happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(preReleaseVersion);
            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxSyncObj);
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testProcessSignOnRequestUnsupportedVersion() {
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            Application.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest();
            Application.commitUnitOfWork();

            String unsupportedAppVersionStr = (this.minAppQBVersionSupported.intValue()-1) + "";
            String newAppId = createAPPID(unsupportedAppVersionStr + ".00","123","456");
            happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(newAppId);

            SourcePayrollParameter parameter = SourcePayrollParameter.findSourcePayrollParameter(
                        SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString);
            SpcfCalendar expirationDate = parameter.getExpirationDate().toLocal();
            String quickbooksVersionString = parameter.getParameterValue();

            SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d,yyyy");
            String expirationDateString = formatter.format(CalendarUtils.convertToDate(expirationDate));
            expirationDate.addDays(-1);
            String formattedDate = formatter.format(CalendarUtils.convertToDate(expirationDate));


            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxSyncObj,ErrorMessages.QBVersionSunsetted(newAppId, quickbooksVersionString, formattedDate, expirationDateString));
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testVerifyAppVersionBadFormat() {
        String[] validAppIds = {"A50.00.R.3/20804#pro","50A.00.R.3/20804#pro","5-0.00.R.3/20804#pro"};
        for (String appId : validAppIds) {
            try {
                OFXDataloader ofxDataLoader = new OFXDataloader();
                com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest();
                
                happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(appId);

                String requestOfxStr = OFXManager.javaToOFX(happyPathOfxSyncObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
                QBDTTestHelper.processRequestSignOnErrorDynamicTransmissionError(requestOfxStr, ErrorMessages.BadOFXError(""));
            } catch (Exception e) {
                TestCase.fail(e.toString());
            }
        }
    }

    @Test
    public void testChangeInAppVersion() {
        try {
            Company co = Company.findCompany(OFXDataloader.companyPSID,SourceSystemCode.QBDT);

            OFXDataloader ofxDataLoader = new OFXDataloader();
            // Need session because we are using SPCFCal
            Application.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadZeroPayroll();
            Application.commitUnitOfWork();
            String newCoAppId = "QBWPRO";
            String newCoAppVersion = "22.01.R.101";
            String newTaxTableId = "555533";
            String newAPPVERStr = newCoAppVersion + "/" + newTaxTableId + "#pro";
            happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPID(newCoAppId);
            happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(newAPPVERStr);

            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxSyncObj);

            co = Company.findCompany(OFXDataloader.companyPSID,SourceSystemCode.QBDT);
            assertEquals(newAPPVERStr.split("#")[1],co.getQuickbooksInfo().getQuickbooksSku());
            assertEquals(newCoAppVersion,co.getQuickbooksInfo().getApplicationVersion());
            assertEquals(newTaxTableId,co.getQuickbooksInfo().getTaxTableId());

        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testQBUnsupportedVersion() {
        try {
            PayrollServices.beginUnitOfWork();
            String[] invalidAppIds = {"52.00.R.3","50.00.R.2"};
            String invalidAppIdStr = invalidAppIds[0] + "|" + invalidAppIds[1];
            SourcePayrollParameter origSourcePayrollParameter = SourcePayrollParameter.findSourcePayrollParameter(SourceSystemCode.QBDT,SourcePayrollParameterCode.UnsupportedQBVersionList);
            List<SourcePayrollParameterDTO> sourcePayrollParameterDTOList = new ArrayList<SourcePayrollParameterDTO>();
            SourcePayrollParameterDTO sourcePayrollParameterDTO = new SourcePayrollParameterDTO(SourceSystemCode.QBDT, SourcePayrollParameterCode.UnsupportedQBVersionList, invalidAppIdStr);
            sourcePayrollParameterDTOList.add(sourcePayrollParameterDTO);
            PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBDT, sourcePayrollParameterDTOList);
            PayrollServices.commitUnitOfWork();
            int cnt = 0;
            for (String appId : invalidAppIds) {
                    OFXDataloader ofxDataLoader = new OFXDataloader();
                    // Need session because we are using SPCFCal
                    com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest();
                    happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(appId + "/12345#bel");
                    String ofxResponseStr = QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxSyncObj,ErrorMessages.QBReleaseNotSupported(appId));
                    QBDTTestHelper.verifyEventExists(EventTypeCode.PayrollRejected,++cnt);
            }
            // Verify that a payroll with an APPID not in the invalid list
            //    succeeds.
            OFXDataloader ofxDataLoader = new OFXDataloader();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            // Need session because we are using SPCFCal
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest(company.getCurrentToken() + "");
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxSyncObj);
        } catch (Exception e) {
            TestCase.fail(e.toString());
        } finally {
            List<SourcePayrollParameterDTO> resetSourcePayrollParameterDTOList = new ArrayList<SourcePayrollParameterDTO>();
            SourcePayrollParameterDTO resetSourcePayrollParameterDTO = new SourcePayrollParameterDTO(SourceSystemCode.QBDT, SourcePayrollParameterCode.UnsupportedQBVersionList, "NONE");
            resetSourcePayrollParameterDTOList.add(resetSourcePayrollParameterDTO);
            PayrollServices.beginUnitOfWork();
            assertSuccess(PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBDT, resetSourcePayrollParameterDTOList));
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void testQBUnsupportedTaxTable() throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            String[] invalidIds = {"21014", "21012"};
            String invalidIdStr = invalidIds[0] + "|" + invalidIds[1];
            List<SourcePayrollParameterDTO> sourcePayrollParameterDTOList = new ArrayList<SourcePayrollParameterDTO>();
            SourcePayrollParameterDTO sourcePayrollParameterDTO = new SourcePayrollParameterDTO(SourceSystemCode.QBDT, SourcePayrollParameterCode.UnsupportedTaxTableList, invalidIdStr);
            sourcePayrollParameterDTOList.add(sourcePayrollParameterDTO);
            assertSuccess(PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBDT, sourcePayrollParameterDTOList));
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            PayrollServices.commitUnitOfWork();

            int cnt = 0;
            for (String id : invalidIds) {
                OFXDataloader ofxDataLoader = new OFXDataloader();
                com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest();
                happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER("52.00.R.3/" + id + "#bel");
                happyPathOfxSyncObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
                QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxSyncObj,ErrorMessages.QBTaxTableNotSupported(id));
                QBDTTestHelper.verifyEventExists(EventTypeCode.PayrollRejected,++cnt);
            }

            OFXDataloader ofxDataLoader = new OFXDataloader();

            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest(company.getCurrentToken() + "");
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxSyncObj);
        } finally {
            List<SourcePayrollParameterDTO> resetSourcePayrollParameterDTOList = new ArrayList<SourcePayrollParameterDTO>();
            SourcePayrollParameterDTO resetSourcePayrollParameterDTO = new SourcePayrollParameterDTO(SourceSystemCode.QBDT, SourcePayrollParameterCode.UnsupportedTaxTableList, "NONE");
            resetSourcePayrollParameterDTOList.add(resetSourcePayrollParameterDTO);
            PayrollServices.beginUnitOfWork();
            assertSuccess(PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBDT, resetSourcePayrollParameterDTOList));
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void testQBMinSupportedTaxTable() throws Exception {
        PayrollServices.beginUnitOfWork();
        List<SourcePayrollParameterDTO> sourcePayrollParameterDTOList = new ArrayList<SourcePayrollParameterDTO>();
        SourcePayrollParameterDTO sourcePayrollParameterDTO = new SourcePayrollParameterDTO(SourceSystemCode.QBDT, SourcePayrollParameterCode.MinSupportedTaxTableVersion, "21012");
        sourcePayrollParameterDTOList.add(sourcePayrollParameterDTO);
        assertSuccess(PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBDT, sourcePayrollParameterDTOList));
        PayrollServices.commitUnitOfWork();
        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest();
            happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER("52.00.R.3/21011#bel");
            QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxSyncObj,ErrorMessages.QBTaxTableNotSupported("21011"));
            QBDTTestHelper.verifyEventExists(EventTypeCode.PayrollRejected,1);

            ofxDataLoader = new OFXDataloader();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest(company.getCurrentToken() + "");
            happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER("52.00.R.3/21012#bel");
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxSyncObj);
        } finally {
            List<SourcePayrollParameterDTO> resetSourcePayrollParameterDTOList = new ArrayList<SourcePayrollParameterDTO>();
            SourcePayrollParameterDTO resetSourcePayrollParameterDTO = new SourcePayrollParameterDTO(SourceSystemCode.QBDT, SourcePayrollParameterCode.MinSupportedTaxTableVersion, "0");
            resetSourcePayrollParameterDTOList.add(resetSourcePayrollParameterDTO);
            PayrollServices.beginUnitOfWork();
            assertSuccess(PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBDT, resetSourcePayrollParameterDTOList));
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void testQBVersionSunsetted() {

        try {
            OFXDataloader ofxDataLoader = new OFXDataloader();
            // Need session because we are using SPCFCal
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathOfxSyncObj = ofxDataLoader.loadHappyPathSyncRequest();
            String appVer = "11.01.R.100";
            happyPathOfxSyncObj.getSIGNONMSGSRQV1().getSONRQ().setAPPVER(appVer + "/12345#bel");

            SourcePayrollParameter parameter = SourcePayrollParameter.findSourcePayrollParameter(
                        SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString);
            SpcfCalendar expirationDate = parameter.getExpirationDate().toLocal();
            String quickbooksVersionString = parameter.getParameterValue();

            SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d,yyyy");
            String expirationDateString = formatter.format(CalendarUtils.convertToDate(expirationDate));
            expirationDate.addDays(-1);
            String formattedDate = formatter.format(CalendarUtils.convertToDate(expirationDate));

            String ofxResponseStr = QBDTTestHelper.processOFXRequestSignOnError(happyPathOfxSyncObj,ErrorMessages.QBVersionSunsetted(appVer, quickbooksVersionString, formattedDate, expirationDateString));
            QBDTTestHelper.verifyEventExists(EventTypeCode.PayrollRejected,1);
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    private String createAPPID(String qbVersionStr, String buildStr, String taxTableIdStr) {
        StringWriter strWriter = new StringWriter();
        strWriter.write(qbVersionStr);
        strWriter.write(".R.");
        strWriter.write(buildStr);
        strWriter.write("/");
        strWriter.write(taxTableIdStr);
        strWriter.write("#bel");
        return strWriter.toString();
    }

}
