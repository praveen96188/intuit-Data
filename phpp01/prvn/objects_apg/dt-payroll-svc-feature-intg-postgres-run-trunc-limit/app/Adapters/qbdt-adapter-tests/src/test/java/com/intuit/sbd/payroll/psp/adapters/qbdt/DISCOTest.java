package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.SIGNONMSGSRSV1;
import com.intuit.sbd.payroll.psp.common.ofx.response.SONRS;
import com.intuit.sbd.payroll.psp.common.ofx.response.STATUS;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: sbarenz
 * Date: Apr 6, 2009
 * Time: 9:44:12 AM

 */
public class DISCOTest {

    private String source_company_id = null;

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
    // Expect no alert message.  Note it is assumed that the minimum version of Quickbooks will switch
    // in order to accommodate the newest valid QuickBooks Version
    public void submitPayrollBeforeDiscoChecksAfterDiscoTest() {
        Company company;
        try {
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
            sunsetParam.setParameterValue("QuickBooks 2007");
            sunsetParam = Application.save(sunsetParam);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            SpcfCalendar endDate = sunsetParam.getExpirationDate().toLocal();

            endDate.addDays(-1);
            String formattedDate = getFormattedDateString(endDate);
            ErrorMessage message = ErrorMessages.QBReleaseWithChecksAfterSunset(sunsetParam.getParameterValue(), sunsetParam.getParameterValue(), formattedDate, formattedDate);
            endDate.addMinutes(-1);
            PSPDate.setPSPTime(endDate);

            List<IPAYROLLRUN> payrollRunList = happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
            IPAYROLLRUN payrollRun = payrollRunList.get(0);
            endDate.addDays(2);

            String ofxDate = getOFXFormattedDateString(endDate);
            System.out.println("OFX DATE:" + ofxDate); 
            payrollRun.setIDTPAYCHKS(ofxDate);

            String happyPathOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            company = Company.findCompany(source_company_id, SourceSystemCode.QBDT);
            long origToken = company.getCurrentToken();
            PayrollServices.commitUnitOfWork();


            // Get the error message together
            String responseStr = QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, message);

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
            assertEquals(origToken, company.getCurrentToken());
            

            // Put the Param back
            PayrollServices.payrollManager.updateSourcePayrollParameter(SourceSystemCode.QBDT,
                                                                        SourcePayrollParameterCode.QBVersionSunsetString,
                                                                        "QuickBooks 2006");
            PayrollServices.commitUnitOfWork();
            QBDTTestHelper.verifyTransmissionDataSaved(happyPathOfxStr,responseStr, TransmissionType.PayrollSubmission);
            DataLoadServices.updateSourcePayrollParameter(SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString, "QuickBooks 2006");

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    private String getFormattedDateString(SpcfCalendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d,yyyy");
        return formatter.format(CalendarUtils.convertToDate(calendar));
    }

    private String getOFXFormattedDateString(SpcfCalendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.format(CalendarUtils.convertToDate(calendar));
    }

    private ErrorMessage getErrorMessage () {
        SourcePayrollParameter parameter = SourcePayrollParameter.findSourcePayrollParameter(
                    SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString);
        SpcfCalendar expirationDate = parameter.getExpirationDate().toLocal();
        String quickbooksVersionString = parameter.getParameterValue();

        SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d,yyyy");
        String expirationDateString = formatter.format(CalendarUtils.convertToDate(expirationDate));
        expirationDate.addDays(-1);
        String formattedDate = formatter.format(CalendarUtils.convertToDate(expirationDate));
        return ErrorMessages.QBVersionSunsetted(quickbooksVersionString, quickbooksVersionString, formattedDate, expirationDateString);
    }

}
