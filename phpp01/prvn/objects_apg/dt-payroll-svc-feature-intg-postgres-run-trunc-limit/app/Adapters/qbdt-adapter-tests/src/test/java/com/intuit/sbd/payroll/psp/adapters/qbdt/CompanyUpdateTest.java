package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.BANKACCT;
import com.intuit.sbd.payroll.psp.common.ofx.request.ICOINFOMOD;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLMSGSRSV1;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLUPDATERS;
import com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Mar 4, 2008
 * Time: 2:07:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyUpdateTest {


    @Before
    public void runBeforeEachTest() {
        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }

    @Test
    public void testCoInfoModHappyPath() {
        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            Company origCompany = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            long origToken = origCompany.getCurrentToken();

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX ofxRequestWithCoInfoMod = ofxDataloader.loadCompany3WithCOINFOMODChangingAll();
            ICOINFOMOD coInfoModRequest = ofxRequestWithCoInfoMod.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getICOINFOMOD();
            String requestOfxStr = OFXManager.javaToOFX(ofxRequestWithCoInfoMod, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            PayrollServices.commitUnitOfWork();
            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(ofxRequestWithCoInfoMod);

            PayrollServices.beginUnitOfWork();
            
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnResponseCode);

            IPAYROLLMSGSRSV1 payrollResponseMsg = ofxResponseObj.getIPAYROLLMSGSRSV1();

            PayrollServices.commitUnitOfWork();

            IPAYROLLUPDATERS payrollUpdateRS = payrollResponseMsg.getIPAYROLLUPDATERS();
            assertNull(payrollUpdateRS.getIDDSTATUS());
            payrollUpdateRS.getIPAYROLLTRNRS();

            PayrollServices.beginUnitOfWork();
            Company updatedCompany = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            long newToken = updatedCompany.getCurrentToken();
            // Token should go up by two since there is a legal name change as well
            //    as a legal address change.
            assertEquals(origToken+1,newToken);
            Address legalAddress = updatedCompany.getLegalAddress();
            assertEquals(coInfoModRequest.getIADDR1(),legalAddress.getAddressLine1());
            assertEquals(coInfoModRequest.getIADDR2(),QBOFX.convertNULLToEmptyString(legalAddress.getAddressLine2()));
            assertEquals(coInfoModRequest.getICITY(),legalAddress.getCity());
            assertEquals(coInfoModRequest.getISTATE(),legalAddress.getState());
            assertEquals(coInfoModRequest.getIPOSTALCODE(),legalAddress.getZipCode());
            // FEIN Should not be updated in Core.
            assertFalse(coInfoModRequest.getIFEIN().compareTo(updatedCompany.getFedTaxId())==0);
            assertEquals(coInfoModRequest.getILEGALNAME(),updatedCompany.getLegalName());

            assertNull(payrollUpdateRS.getITAXSERVSTATUS());
            PayrollServices.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void testPrimaryCOAChange() throws Exception {
        String newCOA = "New Bank Id";
        String newCOA2 = "New Bank Id Too";
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        CompanyBankAccount coBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
        String origBankAcctName = coBankAcct.getSourceBankAccountName();
        PayrollServices.commitUnitOfWork();

        // Run Payroll 1
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX payrollRequest1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
        QBDTTestHelper.updateOFXWithNextCompanyToken(payrollRequest1OFX);
        String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(payrollRequest1OFX);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertEquals(origBankAcctName,ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIACCTNAME());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        coBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
        CompanyBankAccountDTO coBADTO = PayrollServices.dtoFactory.create(coBankAcct);
        coBADTO.setSourceBankAccountName(newCOA);
        ProcessResult<CompanyBankAccount> coBAUpdatePR = PayrollServices.companyManager.updateCompanyBankAccount(SourceSystemCode.QBDT, OFXDataloader.companyPSID, coBADTO);
        assertTrue(coBAUpdatePR.isSuccess());
        PayrollServices.commitUnitOfWork();

        // Run Payroll 2
        OFX requestOFXPayroll2 = ofxDataloader.loadHappyPathOFXPayroll2();
        QBDTTestHelper.updateOFXWithNextCompanyToken(requestOFXPayroll2);
        OFX ofxPayrollRequest1= ofxDataloader.loadHappyPathOFXPayroll2();
        String ofxResponseStr2 = QBDTTestHelper.processOFXRequestSuccess(ofxPayrollRequest1);
//            requestOfxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//            QBDTRequestProcessor qbdtRequestProcessor2= new QBDTRequestProcessor();
//            ofxResponseStr = qbdtRequestProcessor2.processRequest(requestOfxStr);
        ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr2,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        assertEquals(newCOA,ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIACCTNAME());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        coBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
        coBADTO = PayrollServices.dtoFactory.create(coBankAcct);
        coBADTO.setSourceBankAccountName(newCOA2);
        PayrollServices.companyManager.updateCompanyBankAccount(SourceSystemCode.QBDT, OFXDataloader.companyPSID, coBADTO);
        PayrollServices.commitUnitOfWork();

        // Run Payroll Void
        List<String> paycheckIdVoidList = new LinkedList<String>();
        paycheckIdVoidList.add("1");
        OFX voidOfxObj= ofxDataloader.loadVoidCompany3Payroll(paycheckIdVoidList,requestOFXPayroll2.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
        QBDTTestHelper.updateOFXWithNextCompanyToken(voidOfxObj);
        String ofxVoidResponseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfxObj);
//            String requestOfxStr = OFXManager.javaToOFX(ofxVoidResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//            QBDTRequestProcessor qbdtRequestProcessor3 = new QBDTRequestProcessor();
//            ofxResponseStr = qbdtRequestProcessor3.processRequest(requestOfxStr);
        ofxResponseObj = OFXManager.ofxResponseToJava(ofxVoidResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        assertEquals(voidOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHKMOD().get(0).getIACCTNAME(),ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD().get(0).getIACCTNAME());
        assertEquals(newCOA2,ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD().get(0).getIACCTNAME());
    }

    @Test
    public void testFeeCOAChange() {
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String origFeeCOA = company.getQuickbooksInfo().getCoaFeeAccountName();
            PayrollServices.commitUnitOfWork();

            // Run Payroll 1
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX payrollRequest1OFX = ofxDataloader.loadHappyPathOFXPayroll1();
            QBDTTestHelper.updateOFXWithNextCompanyToken(payrollRequest1OFX);
            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(payrollRequest1OFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            String accountNameFromOFX = null;
            for (ITXLINE txLine : ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getITXLINE()) {
                if (txLine.getIMEMO()!=null) {
                    if (txLine.getIMEMO().compareTo(BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE)==0) {
                        accountNameFromOFX=txLine.getIACCTNAME();
                    }
                }
            }


            assertEquals(origFeeCOA,accountNameFromOFX);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String newFeeCOA = "New Fee COA";
            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            coDTO.getQuickBooksInfo().setCoaFeeAccountName(newFeeCOA);
            PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, OFXDataloader.companyPSID, coDTO);
            PayrollServices.commitUnitOfWork();

            // Run Payroll 2
            payrollRequest1OFX = ofxDataloader.loadHappyPathOFXPayroll2();
            QBDTTestHelper.updateOFXWithNextCompanyToken(payrollRequest1OFX);
            ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(payrollRequest1OFX);
            ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            String newAccountNameFromOFX = null;
            for (ITXLINE txLine : ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getITXLINE()) {
                if (txLine.getIMEMO()!=null) {
                    if (txLine.getIMEMO().compareTo(BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE)==0) {
                        newAccountNameFromOFX=txLine.getIACCTNAME();
                    }
                }
            }
            assertEquals(newAccountNameFromOFX,newFeeCOA);

            // Run Payroll Void
            List<String> paycheckIdVoidList = new LinkedList<String>();
            paycheckIdVoidList.add("1");
            OFX voidOfxObj= ofxDataloader.loadVoidCompany3Payroll(paycheckIdVoidList,payrollRequest1OFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
            QBDTTestHelper.updateOFXWithNextCompanyToken(voidOfxObj);
            ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfxObj);
            ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            String newAccountNameFromVoidOFX = null;
            for (ITXLINE txLine : ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD().get(0).getITXLINE()) {
                if (txLine.getIMEMO()!=null) {
                    if (txLine.getIMEMO().compareTo(BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE)==0) {
                        newAccountNameFromVoidOFX=txLine.getIACCTNAME();
                        break;
                    }
                }
            }
            assertEquals(newAccountNameFromOFX,newAccountNameFromVoidOFX);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    // QuickBooks encodes <, > and & only.
    // Verify that the company name gets saved
    //      and is returned correctly when it contains
    //      XML special chars.
    public void testCoInfoSpecialCharacterHandling() {

        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            Company origCompany = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX ofxRequestWithCoInfoMod = ofxDataloader.loadCompany3WithCOINFOMODChangingAll();
            ICOINFOMOD coInfoModRequest = ofxRequestWithCoInfoMod.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getICOINFOMOD();
            String ofxSpecialCharStr = "yyy&lt;'\"&gt;&amp;1zz`z";
            String decodedSpcialCharStr = "yyy<'\">&1zz`z";
            coInfoModRequest.setILEGALNAME(decodedSpcialCharStr);
            PayrollServices.commitUnitOfWork();
            QBDTTestHelper.processOFXRequestSuccess(ofxRequestWithCoInfoMod);

            PayrollServices.beginUnitOfWork();
            Company updatedCompany = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            PayrollServices.commitUnitOfWork();
            assertEquals(decodedSpcialCharStr,updatedCompany.getLegalName());

            // Change EIN to force COINFOMOD going back to QB
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String tokenBeforeEINUpdate = company.getCurrentToken() + "";

            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            String newFEIN= "121231231";
            coDTO.setFein(newFEIN);
            PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(),company.getSourceCompanyId(),coDTO);
            PayrollServices.commitUnitOfWork();

            String ofxResponseStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(tokenBeforeEINUpdate);


            Pattern linePattern = Pattern.compile("\\s*(<I.LEGALNAME>(.*))");
            Matcher lineMatcher = linePattern.matcher(ofxResponseStr);
            lineMatcher.find();
            assertEquals(ofxSpecialCharStr,lineMatcher.group(2));

            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(decodedSpcialCharStr,ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD().getILEGALNAME());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    // QuickBooks encodes <, > and & only.
    // Company legal name max is 100.  So &amp; * 90 should
    // work for a company name.
    public void testCoInfoSpecialCharacterFieldLength() {

        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            Company origCompany = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX ofxRequestWithCoInfoMod = ofxDataloader.loadCompany3WithCOINFOMODChangingAll();
            ICOINFOMOD coInfoModRequest = ofxRequestWithCoInfoMod.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getICOINFOMOD();
            StringWriter ofxSpecialCharStr = new StringWriter();
            StringWriter decodedSpcialCharStr = new StringWriter();
            for (int i=0;i<99;i++) {
                ofxSpecialCharStr.append("&amp;");
                decodedSpcialCharStr.append("&");
            }
            coInfoModRequest.setILEGALNAME(decodedSpcialCharStr.toString());
            PayrollServices.commitUnitOfWork();
            QBDTTestHelper.processOFXRequestSuccess(ofxRequestWithCoInfoMod);

            PayrollServices.beginUnitOfWork();
            Company updatedCompany = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            PayrollServices.commitUnitOfWork();
            assertEquals(decodedSpcialCharStr.toString(),updatedCompany.getLegalName());

            // Change EIN to force COINFOMOD going back to QB
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String tokenBeforeEINUpdate = company.getCurrentToken() + "";

            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            String newFEIN= "121231231";
            coDTO.setFein(newFEIN);
            PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(),company.getSourceCompanyId(),coDTO);
            PayrollServices.commitUnitOfWork();

            String ofxResponseStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(tokenBeforeEINUpdate);

            Pattern linePattern = Pattern.compile("\\s*(<I.LEGALNAME>(.*))");
            Matcher lineMatcher = linePattern.matcher(ofxResponseStr);
            lineMatcher.find();
            assertEquals(ofxSpecialCharStr.toString(),lineMatcher.group(2));

            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(decodedSpcialCharStr.toString(),ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD().getILEGALNAME());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void testEmptyBankAcctFrom() throws Exception {
        // Process a sync request to handle the bank events.
        QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

        PayrollServices.beginUnitOfWork();
        Company origCompany = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        long origToken = origCompany.getCurrentToken();

        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX ofxRequestWithCoInfoMod = ofxDataloader.loadCompany3WithCOINFOMODChangingAll();
        ICOINFOMOD coInfoModRequest = ofxRequestWithCoInfoMod.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getICOINFOMOD();

        coInfoModRequest.setBANKACCTFROM(new BANKACCT());
        coInfoModRequest.getBANKACCTFROM().setACCTID("^@~*");
        coInfoModRequest.getBANKACCTFROM().setBANKID("^@~*");
        coInfoModRequest.getBANKACCTFROM().setACCTTYPE("CHECKING");

        PayrollServices.commitUnitOfWork();
        String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(ofxRequestWithCoInfoMod);
        PayrollServices.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String signOnResponseCode = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY();
        TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnResponseCode);

        IPAYROLLMSGSRSV1 payrollResponseMsg = ofxResponseObj.getIPAYROLLMSGSRSV1();

        PayrollServices.commitUnitOfWork();

        IPAYROLLUPDATERS payrollUpdateRS = payrollResponseMsg.getIPAYROLLUPDATERS();
        assertNull(payrollUpdateRS.getIDDSTATUS());
        payrollUpdateRS.getIPAYROLLTRNRS();

        PayrollServices.beginUnitOfWork();
        Company updatedCompany = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        long newToken = updatedCompany.getCurrentToken();
        // Token should go up by two since there is a legal name change as well
        //    as a legal address change.
        assertEquals(origToken+1,newToken);
        Address legalAddress = updatedCompany.getLegalAddress();
        assertEquals(coInfoModRequest.getIADDR1(),legalAddress.getAddressLine1());
        assertEquals(coInfoModRequest.getIADDR2(),QBOFX.convertNULLToEmptyString(legalAddress.getAddressLine2()));
        assertEquals(coInfoModRequest.getICITY(),legalAddress.getCity());
        assertEquals(coInfoModRequest.getISTATE(),legalAddress.getState());
        assertEquals(coInfoModRequest.getIPOSTALCODE(),legalAddress.getZipCode());
        // FEIN Should not be updated in Core.
        assertFalse(coInfoModRequest.getIFEIN().compareTo(updatedCompany.getFedTaxId())==0);
        assertEquals(coInfoModRequest.getILEGALNAME(),updatedCompany.getLegalName());

        assertNull(payrollUpdateRS.getITAXSERVSTATUS());
        PayrollServices.commitUnitOfWork();
    }
}
