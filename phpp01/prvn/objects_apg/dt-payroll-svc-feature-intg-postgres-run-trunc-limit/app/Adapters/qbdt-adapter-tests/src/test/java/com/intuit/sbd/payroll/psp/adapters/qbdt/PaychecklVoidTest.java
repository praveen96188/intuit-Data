package com.intuit.sbd.payroll.psp.adapters.qbdt;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLMSGSRQV1;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLUPDATERS;
import com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.apache.commons.lang.ObjectUtils;
import org.junit.*;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Feb 28, 2008
 * Time: 1:08:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaychecklVoidTest {


    public static SpcfDecimal transmissionFee = null;
    public static SpcfDecimal costPerTx = null;

    public PaychecklVoidTest() {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        String expectedTransmissionFeeStr = n.format(QBDTTestHelper.PER_TRANSMISSION_PRICE);
        transmissionFee = new SpcfMoney(expectedTransmissionFeeStr.substring(1));
        String costPerTxStr = n.format(QBDTTestHelper.PER_PAYCHECK_PRICE);
        costPerTx = new SpcfMoney(costPerTxStr.substring(1));
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
    public void testVoidSinglePaycheckBeforeOffload() {

        try {

            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX payrollReqMsgOFXRoot = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();

            String payrollRespMsgOFXStr = QBDTTestHelper.processOFXRequestSuccess(payrollReqMsgOFXRoot);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(payrollRespMsgOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            voidPaycheckFundsRecovered(payrollReqMsgOFXRoot,ofxResponseObj,1, true);


        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }


    public void voidPaycheckFundsRecovered(OFX payrollReqMsgOFXRoot,com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj,int paychecksVoidedWithEvents, boolean expectPayrollTx,boolean isSymphony) throws Exception {
        PayrollServices.beginUnitOfWork();
        String originalPayrollTxId = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIPAYROLLTXID();

        SpcfDecimal salesTaxBeforeVoid = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID, "1");

        IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = payrollReqMsgOFXRoot.getIPAYROLLMSGSRQV1();
        IPAYCHK dmPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1);

        List<String> paycheckVoidList = new ArrayList<String>();
        paycheckVoidList.add("1");
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList, payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
        String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();

        OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfx, AssistedConnectionInformation.getVoidPayrollMessage(1));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFX = OFXManager.ofxResponseToJava(responseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        IPAYROLLUPDATERS payrollUpdateResponseObj = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS();
        validateVoidRun(payrollUpdateResponseObj, voidOfx.getIPAYROLLMSGSRQV1(), paychecksVoidedWithEvents);
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModObjList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD();
        assertEquals(paycheckVoidList.size(), paycheckModObjList.size());
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModList = paycheckModObjList;
        for (int cnt = 0; cnt < paycheckVoidList.size(); cnt++) {
            com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD paycheckmod = paycheckModList.get(cnt);
            assertEquals(paycheckmod.getIPAYCHKID(), paycheckVoidList.get(0));
            validatePaycheckModVoid(paycheckmod, voidPayrollOfx, false);
        }

        if (expectPayrollTx) {
            List<IPAYROLLTX> payrollUpdateResponseList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD();
            assertEquals(1, payrollUpdateResponseList.size());

            String paycheckCntAtTimeOffloadStr = "1";

            // Total Abe Lincoln's Two paychecks
            SpcfDecimal expectTotalPaycheckCreditAmt = new SpcfMoney();
            expectTotalPaycheckCreditAmt = expectTotalPaycheckCreditAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1, alPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));
            expectTotalPaycheckCreditAmt = expectTotalPaycheckCreditAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(1).getIAMT().substring(1, alPaycheckRequest.getIDDLINE().get(1).getIAMT().length())));
            SpcfDecimal expectedTransactionFee = costPerTx.multiply(new SpcfMoney(paycheckCntAtTimeOffloadStr));

            SpcfDecimal expectedTotal = expectTotalPaycheckCreditAmt;
            expectedTotal = expectedTotal.add(transmissionFee.negate());
            expectedTotal = expectedTotal.add(expectedTransactionFee.negate());

            PayrollServices.beginUnitOfWork();
            SpcfDecimal salesTaxAfterVoid = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID, "1");
            PayrollServices.commitUnitOfWork();
            expectedTotal = expectedTotal.add(salesTaxAfterVoid.negate());
            // Sales tax should have stayed the same or decreased.
            if ((salesTaxBeforeVoid.getIntegerPart() > 0) || (salesTaxBeforeVoid.getFractionalPart() > 0)) {
                assertTrue(salesTaxAfterVoid.getIntegerPart() <= salesTaxBeforeVoid.getIntegerPart());
                if (salesTaxAfterVoid.getIntegerPart() == salesTaxBeforeVoid.getIntegerPart()) {
                    assertTrue(salesTaxAfterVoid.getFractionalPart() <= salesTaxBeforeVoid.getFractionalPart());
                }
            } else {
                assertEquals(salesTaxAfterVoid.getIntegerPart(), 0);
                assertEquals(salesTaxAfterVoid.getFractionalPart(), 0);
            }

            IPAYROLLTX payrollTxModResponseObj = payrollUpdateResponseList.get(0);

            if(!isSymphony){

                validateDIYPaycheckContent(originalPayrollTxId, voidPayrollOfx, paycheckCntAtTimeOffloadStr, expectTotalPaycheckCreditAmt, expectedTransactionFee, expectedTotal, payrollTxModResponseObj);

            }
        }
    }
        public void voidPaycheckFundsRecovered(OFX payrollReqMsgOFXRoot,com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj,int paychecksVoidedWithEvents, boolean expectPayrollTx) throws Exception {
        PayrollServices.beginUnitOfWork();
        String originalPayrollTxId = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIPAYROLLTXID();

        SpcfDecimal salesTaxBeforeVoid = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID,"1");

        IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = payrollReqMsgOFXRoot.getIPAYROLLMSGSRQV1();
        IPAYCHK dmPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1);

        List<String> paycheckVoidList = new ArrayList<String>();
        paycheckVoidList.add("1");
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList,payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
        String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();

        OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfx,AssistedConnectionInformation.getVoidPayrollMessage(1));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFX = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        IPAYROLLUPDATERS payrollUpdateResponseObj = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS();
        validateVoidRun(payrollUpdateResponseObj,voidOfx.getIPAYROLLMSGSRQV1(),paychecksVoidedWithEvents);
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModObjList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD();
        assertEquals(paycheckVoidList.size(),paycheckModObjList.size());
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModList = paycheckModObjList;
        for (int cnt=0;cnt<paycheckVoidList.size();cnt++) {
            com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD paycheckmod = paycheckModList.get(cnt);
            assertEquals(paycheckmod.getIPAYCHKID(),paycheckVoidList.get(0));
            validatePaycheckModVoid(paycheckmod,voidPayrollOfx,false);
        }

        if(expectPayrollTx) {
        List<IPAYROLLTX> payrollUpdateResponseList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD();
        assertEquals(1,payrollUpdateResponseList.size());

        String paycheckCntAtTimeOffloadStr = "1";

        // Total Abe Lincoln's Two paychecks
        SpcfDecimal expectTotalPaycheckCreditAmt = new SpcfMoney();
        expectTotalPaycheckCreditAmt = expectTotalPaycheckCreditAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));
        expectTotalPaycheckCreditAmt = expectTotalPaycheckCreditAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(1).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(1).getIAMT().length())));
        SpcfDecimal expectedTransactionFee = costPerTx.multiply(new SpcfMoney(paycheckCntAtTimeOffloadStr));

        SpcfDecimal expectedTotal = expectTotalPaycheckCreditAmt;
        expectedTotal = expectedTotal.add(transmissionFee.negate());
        expectedTotal = expectedTotal.add(expectedTransactionFee.negate());

        PayrollServices.beginUnitOfWork();
        SpcfDecimal salesTaxAfterVoid = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID,"1");
        PayrollServices.commitUnitOfWork();
        expectedTotal = expectedTotal.add(salesTaxAfterVoid.negate());
        // Sales tax should have stayed the same or decreased.
        if ((salesTaxBeforeVoid.getIntegerPart() > 0) || (salesTaxBeforeVoid.getFractionalPart() > 0)) {
            assertTrue(salesTaxAfterVoid.getIntegerPart() <= salesTaxBeforeVoid.getIntegerPart());
            if (salesTaxAfterVoid.getIntegerPart() == salesTaxBeforeVoid.getIntegerPart()) {
                assertTrue(salesTaxAfterVoid.getFractionalPart() <= salesTaxBeforeVoid.getFractionalPart());
            }
        } else {
            assertEquals(salesTaxAfterVoid.getIntegerPart(),0);
            assertEquals(salesTaxAfterVoid.getFractionalPart(),0);
        }

        IPAYROLLTX payrollTxModResponseObj = payrollUpdateResponseList.get(0);


            validateDIYPaycheckContent(originalPayrollTxId, voidPayrollOfx, paycheckCntAtTimeOffloadStr, expectTotalPaycheckCreditAmt, expectedTransactionFee, expectedTotal, payrollTxModResponseObj);

        }
    }

    private void validateDIYPaycheckContent(String originalPayrollTxId, OFX voidPayrollOfx, String paycheckCntAtTimeOffloadStr, SpcfDecimal expectTotalPaycheckCreditAmt, SpcfDecimal expectedTransactionFee, SpcfDecimal expectedTotal, IPAYROLLTX payrollTxModResponseObj) throws Exception {
        assertEquals(originalPayrollTxId,payrollTxModResponseObj.getIPAYROLLTXID());
        validatePayrollTxModElementContents(payrollTxModResponseObj,
            voidPayrollOfx.getIPAYROLLMSGSRQV1(),
            "$"+expectedTotal.toString());


        SpcfDecimal expectTotalPaycheckDebitAmt  = (SpcfMoney)expectTotalPaycheckCreditAmt.negate();
        //@TODO Get real fee acct name once added.
        String feeActName = QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME;

        validateSpecifiedPayrollTxModTxLineExists(
            payrollTxModResponseObj.getITXLINE(),
            null,
            "$"+ expectTotalPaycheckDebitAmt.toString(),
                QBOFX.OFX_YN.Y,
                QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK
                );

        validateSpecifiedPayrollTxModTxLineExists(
            payrollTxModResponseObj.getITXLINE(),
            feeActName,
            "$"+ transmissionFee.toString(),
                null,
                BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE
                );

        validateSpecifiedPayrollTxModTxLineExists(
             payrollTxModResponseObj.getITXLINE(),
             feeActName,
             "$"+ expectedTransactionFee.toString(),
                 null,
                 String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, Integer.parseInt(paycheckCntAtTimeOffloadStr), costPerTx.toString())
                 );
    }

    @Test
    public void testVoidMultiplePaychecksBeforeOffload() {
        try {

            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX payrollReqMsgOFXRoot = ofxDataloader.loadHappyPathOFX();

            String payrollRespMsgOFXStr = QBDTTestHelper.processOFXRequestSuccess(payrollReqMsgOFXRoot);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(payrollRespMsgOFXStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String originalPayrollTxId = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIPAYROLLTXID();

            PayrollServices.beginUnitOfWork();

            IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = payrollReqMsgOFXRoot.getIPAYROLLMSGSRQV1();
            IPAYCHK dmPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0);
            IPAYCHK alPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1);

            List<String> paycheckVoidList = new ArrayList<String>();
            paycheckVoidList.add("1");
            paycheckVoidList.add("2");
            OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList,payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
            String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            PayrollServices.commitUnitOfWork();

            OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String responseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfx,AssistedConnectionInformation.getVoidPayrollMessage(2));
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFX = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            IPAYROLLUPDATERS payrollUpdateResponseObj = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS();
            validateVoidRun(payrollUpdateResponseObj,voidOfx.getIPAYROLLMSGSRQV1());
            List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModObjList = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD();
            assertEquals(paycheckVoidList.size(),paycheckModObjList.size());
            List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModList = paycheckModObjList;
            assertEquals(paycheckVoidList.size(),paycheckModList.size());
            for (String paycheckVoidId : paycheckVoidList) {
                com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD paycheckMod =  null;
                // Find the paycheck mod matching the paycheck id voided.
                for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD listPaycheckMod : paycheckModList) {
                    if (listPaycheckMod.getIPAYCHKID().compareTo(paycheckVoidId)==0) {
                        paycheckMod = listPaycheckMod;
                        break;
                    }
                }
                assertNotNull(paycheckMod);
                validatePaycheckModVoid(paycheckMod,voidPayrollOfx,false);
            }

            List<IPAYROLLTX> payrollUpdateResponseList = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD();
            assertEquals(1,payrollUpdateResponseList.size());

            SpcfDecimal expectTotalDebitAmt = new SpcfMoney("0.00");

            String paycheckCntAtTimeOfOffloadStr = "0";

            SpcfDecimal expectedTransactionFee = costPerTx.multiply(new SpcfMoney(paycheckCntAtTimeOfOffloadStr));
            expectTotalDebitAmt = expectTotalDebitAmt.add(expectedTransactionFee);
            expectTotalDebitAmt = expectTotalDebitAmt.add(transmissionFee);
            PayrollServices.beginUnitOfWork();
            SpcfDecimal salesTax = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID,"1");
            PayrollServices.commitUnitOfWork();
            expectTotalDebitAmt = expectTotalDebitAmt.add(salesTax);

            IPAYROLLTX payrollTxModResponseObj = payrollUpdateResponseList.get(0);

            assertEquals(originalPayrollTxId,payrollTxModResponseObj.getIPAYROLLTXID());

            validatePayrollTxModElementContents(payrollTxModResponseObj,
                voidPayrollOfx.getIPAYROLLMSGSRQV1(),
                "$" + expectTotalDebitAmt.negate().toString());

            validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                null,
                "$0.00",
                QBOFX.OFX_YN.Y,
                QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK
                );

            //@TODO Get real fee acct name once added.
            String feeActName = QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME;

            validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$"+ transmissionFee.toString(),
                    null,
                    BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE
                    );

            validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$"+ expectedTransactionFee.toString(),
                null,
                String.format(BillingDetail.MEMOS.NO_FEE_FOR_DIRECT_DEPOSIT, Integer.parseInt(paycheckCntAtTimeOfOffloadStr))
                );

        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testVoidSubsequentPaychecksAfterOffload() throws Exception {
        // Void one paycheck
        IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = runAndOffloadPostOffloadSinglePaycheckTest();


        // Now void the other

        // Sleep for one second so that the tranmission save
        //    ensures that the first and second one have different
        //    times.
        Thread.sleep(2000);

        OFXDataloader ofxDataloader = new OFXDataloader();
        // Load the happy path OFX so we can pull the IPAYCHKs off.

        PayrollServices.beginUnitOfWork();
        IPAYCHK dmPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1);

        List<String> paycheckVoidList = new ArrayList<String>();
        paycheckVoidList.add("2");
        OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList,payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
        QBDTTestHelper.updateOFXWithNextCompanyToken(voidPayrollOfx);
        String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();

        OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfx,AssistedConnectionInformation.getVoidPayrollMessage(1));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFX = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        String ofxVoidResponseStr = OFXManager.javaToOFX(responseOFX,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        IPAYROLLUPDATERS payrollUpdateResponseObj = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS();
        validateVoidRun(payrollUpdateResponseObj,voidOfx.getIPAYROLLMSGSRQV1());
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModObjList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD();
        assertEquals(paycheckVoidList.size(),paycheckModObjList.size());
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModList = paycheckModObjList;
        // Match up the paycheck void request ids with the paycheck mods.
        for (int cnt=0;cnt<paycheckVoidList.size();cnt++) {
            com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD paycheckmod = paycheckModList.get(cnt);
            boolean found = false;
            for (int cnt2=0;cnt2<paycheckVoidList.size();cnt2++) {
                if (paycheckmod.getIPAYCHKID().compareTo(paycheckVoidList.get(cnt2))!=0) {
                    continue;
                }
                validatePaycheckModVoid(paycheckmod,voidPayrollOfx,true);
                found = true;
                break;
            }
            assertTrue("Could not find paycheck void with id " + paycheckmod.getIPAYCHKID(),found);
        }

        List<IPAYROLLTX> payrollUpdateResponseList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD();
        assertEquals(1,payrollUpdateResponseList.size());

        String paycheckCntAtTimeOffloadStr = "2";

        // Total Abe Lincoln's Two paychecks
        SpcfDecimal expectedTransactionFee = costPerTx.multiply(new SpcfMoney(paycheckCntAtTimeOffloadStr));

        SpcfDecimal abeLincolnTotalPaycheckDebitAmt = new SpcfMoney();
        abeLincolnTotalPaycheckDebitAmt = abeLincolnTotalPaycheckDebitAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));
        abeLincolnTotalPaycheckDebitAmt = abeLincolnTotalPaycheckDebitAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(1).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(1).getIAMT().length())));

        SpcfDecimal dmTotalPaycheckDebitAmt = new SpcfMoney();
        dmTotalPaycheckDebitAmt = dmTotalPaycheckDebitAmt.add(new SpcfMoney(dmPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1,dmPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));

        SpcfDecimal expectTotalPaycheckCreditAmt = new SpcfMoney();
        SpcfDecimal expectedTotal = expectTotalPaycheckCreditAmt;
        expectedTotal = expectedTotal.add(expectedTransactionFee.negate());
        expectedTotal = expectedTotal.add(transmissionFee.negate());
        expectedTotal = expectedTotal.add(abeLincolnTotalPaycheckDebitAmt);
        expectedTotal = expectedTotal.add(dmTotalPaycheckDebitAmt);

        PayrollServices.beginUnitOfWork();
        SpcfDecimal salesTax = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID,"1");
        PayrollServices.commitUnitOfWork();
        expectedTotal = expectedTotal.add(salesTax.negate());

        IPAYROLLTX payrollTxModResponseObj = payrollUpdateResponseList.get(0);
        validatePayrollTxModElementContents(payrollTxModResponseObj,
                                            voidPayrollOfx.getIPAYROLLMSGSRQV1(),
                                            "$"+expectedTotal.toString());

        String feeActName = QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME;

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                null,
                "$"+ expectTotalPaycheckCreditAmt.toString(),
                QBOFX.OFX_YN.Y,
                QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK
        );

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$"+ transmissionFee.toString(),
                null,
                BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE
        );

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$"+ expectedTransactionFee.toString(),
                null,
                String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, Integer.parseInt(paycheckCntAtTimeOffloadStr), costPerTx.toString())
        );

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                null,
                "$"+ dmPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(2),
                QBOFX.OFX_YN.Y,
                QBOFX.MEMOS.getVoidAfterOffloadMemoStr(OFXDataloader.dmEmpName)
        );

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                null,
                "$" + abeLincolnTotalPaycheckDebitAmt.negate().toString(),
                QBOFX.OFX_YN.Y,
                QBOFX.MEMOS.getVoidAfterOffloadMemoStr(OFXDataloader.alEmpName)
        );
    }

    @Test
    public void testVoidSinglePaycheckAfterOffload() throws Exception {
        runAndOffloadPostOffloadSinglePaycheckTest();
    }

    @Test
    // Test that paycheck cannot be voided when it still created, but
    //   is past the offload time.
    public void testVoidSinglePaycheckAfterOffloadTime() {
        try {
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX requestOFX = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();

            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(requestOFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFXObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            Paycheck paycheck = Paycheck.findPaycheck(company,"1");
            PayrollRun payroll = paycheck.getPayrollRun();
            SpcfCalendar offloadDateAtFiveElevenPM = payroll.getPaycheckDate();
            CalendarUtils.addBusinessDays(offloadDateAtFiveElevenPM,-2);
            offloadDateAtFiveElevenPM.setValues(offloadDateAtFiveElevenPM.getYear(), offloadDateAtFiveElevenPM.getMonth(), offloadDateAtFiveElevenPM.getDay()+1, 0, 11, 0, 0);
            PSPDate.setPSPTime(offloadDateAtFiveElevenPM);
            PayrollServices.commitUnitOfWork();

            String originalPayrollTxId = responseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIPAYROLLTXID();
            runPostOffloadSinglePaycheckTest(requestOFX,originalPayrollTxId);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private IPAYROLLMSGSRQV1 runAndOffloadPostOffloadSinglePaycheckTest() throws Exception {
        IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = null;

        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX requestOFX = ofxDataloader.loadHappyPathOFX();
        PayrollServices.commitUnitOfWork();

        String responseOFXStr = QBDTTestHelper.offloadCompanyPayroll(requestOFX);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFXObj = OFXManager.ofxResponseToJava(responseOFXStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String originalPayrollTxId = responseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIPAYROLLTXID();
        payrollReqMsgOFXObj = runPostOffloadSinglePaycheckTest(requestOFX,originalPayrollTxId);

        return payrollReqMsgOFXObj;
    }

    public IPAYROLLMSGSRQV1 runPostOffloadSinglePaycheckTestForSymohony(OFX requestOFX,String originalPayrollTxId,boolean isSymphony) throws Exception {
        IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = requestOFX.getIPAYROLLMSGSRQV1();
        OFXDataloader ofxDataloader = new OFXDataloader();

        PayrollServices.beginUnitOfWork();
        IPAYCHK dmPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1);

        List<String> paycheckVoidList = new ArrayList<String>();
        paycheckVoidList.add("1");
        OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList,payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
        QBDTTestHelper.updateOFXWithNextCompanyToken(voidPayrollOfx);

        String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();

        OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfx,AssistedConnectionInformation.getVoidPayrollMessage(1));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX voidResponseOFXObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        String ofxVoidResponseStr = OFXManager.javaToOFX(voidResponseOFXObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        System.out.println(ofxVoidResponseStr);
        IPAYROLLUPDATERS payrollUpdateResponseObj = voidResponseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS();
        validateVoidRun(payrollUpdateResponseObj,voidOfx.getIPAYROLLMSGSRQV1());
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModObjList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD();
        assertEquals(paycheckVoidList.size(),paycheckModObjList.size());
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModList = paycheckModObjList;
        for (int cnt=0;cnt<paycheckVoidList.size();cnt++) {
            com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD paycheckmod = paycheckModList.get(cnt);
            assertEquals(paycheckmod.getIPAYCHKID(),paycheckVoidList.get(0));
            validatePaycheckModVoid(paycheckmod,voidPayrollOfx,true);
        }

        List<IPAYROLLTX> payrollUpdateResponseList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD();
        assertEquals(1,payrollUpdateResponseList.size());

        String paycheckCntAtTimeOffloadStr = "2";

        // Total Abe Lincoln's Two paychecks
        SpcfDecimal expectedTransactionFee = costPerTx.multiply(new SpcfDecimalImpl(paycheckCntAtTimeOffloadStr));

        SpcfDecimal expectTotalPaycheckDebitAmt = new SpcfMoney();
        expectTotalPaycheckDebitAmt = expectTotalPaycheckDebitAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));
        expectTotalPaycheckDebitAmt = expectTotalPaycheckDebitAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(1).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(1).getIAMT().length())));
        SpcfDecimal expectedTotal = expectTotalPaycheckDebitAmt;
        //expectedTotal = expectedTotal.add(expectedTransactionFee.negate());
        //expectedTotal = expectedTotal.add(transmissionFee.negate());
        expectedTotal = expectedTotal.add(new SpcfMoney(dmPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1,dmPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));
        PayrollServices.beginUnitOfWork();
        SpcfDecimal salesTax = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID,"1");
        PayrollServices.commitUnitOfWork();
        //expectedTotal = expectedTotal.add(salesTax.negate());

        IPAYROLLTX payrollTxModResponseObj = payrollUpdateResponseList.get(0);

        assertEquals(originalPayrollTxId,payrollTxModResponseObj.getIPAYROLLTXID());

        if(!isSymphony) {

            validationPostOffloadForDIY(dmPaycheckRequest, voidPayrollOfx, paycheckCntAtTimeOffloadStr, expectedTransactionFee, expectTotalPaycheckDebitAmt, expectedTotal, payrollTxModResponseObj);
        }
        return payrollReqMsgOFXObj;
    }




    public IPAYROLLMSGSRQV1 runPostOffloadSinglePaycheckTest(OFX requestOFX,String originalPayrollTxId) throws Exception {
        IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = requestOFX.getIPAYROLLMSGSRQV1();
        OFXDataloader ofxDataloader = new OFXDataloader();

        PayrollServices.beginUnitOfWork();
        IPAYCHK dmPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1);

        List<String> paycheckVoidList = new ArrayList<String>();
        paycheckVoidList.add("1");
        OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList,payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
        QBDTTestHelper.updateOFXWithNextCompanyToken(voidPayrollOfx);

        String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();

        OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfx,AssistedConnectionInformation.getVoidPayrollMessage(1));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX voidResponseOFXObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        String ofxVoidResponseStr = OFXManager.javaToOFX(voidResponseOFXObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        System.out.println(ofxVoidResponseStr);
        IPAYROLLUPDATERS payrollUpdateResponseObj = voidResponseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS();
        validateVoidRun(payrollUpdateResponseObj,voidOfx.getIPAYROLLMSGSRQV1());
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModObjList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD();
        assertEquals(paycheckVoidList.size(),paycheckModObjList.size());
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModList = paycheckModObjList;
        for (int cnt=0;cnt<paycheckVoidList.size();cnt++) {
            com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD paycheckmod = paycheckModList.get(cnt);
            assertEquals(paycheckmod.getIPAYCHKID(),paycheckVoidList.get(0));
            validatePaycheckModVoid(paycheckmod,voidPayrollOfx,true);
        }

        List<IPAYROLLTX> payrollUpdateResponseList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD();
        assertEquals(1,payrollUpdateResponseList.size());

        String paycheckCntAtTimeOffloadStr = "2";

        // Total Abe Lincoln's Two paychecks
        SpcfDecimal expectedTransactionFee = costPerTx.multiply(new SpcfDecimalImpl(paycheckCntAtTimeOffloadStr));

        SpcfDecimal expectTotalPaycheckDebitAmt = new SpcfMoney();
        expectTotalPaycheckDebitAmt = expectTotalPaycheckDebitAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));
        expectTotalPaycheckDebitAmt = expectTotalPaycheckDebitAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(1).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(1).getIAMT().length())));
        SpcfDecimal expectedTotal = expectTotalPaycheckDebitAmt;
        expectedTotal = expectedTotal.add(expectedTransactionFee.negate());
        expectedTotal = expectedTotal.add(transmissionFee.negate());
        expectedTotal = expectedTotal.add(new SpcfMoney(dmPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1,dmPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));
        PayrollServices.beginUnitOfWork();
        SpcfDecimal salesTax = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID,"1");
        PayrollServices.commitUnitOfWork();
        expectedTotal = expectedTotal.add(salesTax.negate());

        IPAYROLLTX payrollTxModResponseObj = payrollUpdateResponseList.get(0);

        assertEquals(originalPayrollTxId,payrollTxModResponseObj.getIPAYROLLTXID());

        validationPostOffloadForDIY(dmPaycheckRequest, voidPayrollOfx, paycheckCntAtTimeOffloadStr, expectedTransactionFee, expectTotalPaycheckDebitAmt, expectedTotal, payrollTxModResponseObj);

        return payrollReqMsgOFXObj;
    }

    private void validationPostOffloadForDIY(IPAYCHK dmPaycheckRequest, OFX voidPayrollOfx, String paycheckCntAtTimeOffloadStr, SpcfDecimal expectedTransactionFee, SpcfDecimal expectTotalPaycheckDebitAmt, SpcfDecimal expectedTotal, IPAYROLLTX payrollTxModResponseObj) throws Exception {
        validatePayrollTxModElementContents(payrollTxModResponseObj,
                                             voidPayrollOfx.getIPAYROLLMSGSRQV1(),
                                             "$"+expectedTotal.toString());

        SpcfDecimal expectTotalPaycheckCreditAmt = (SpcfMoney)expectTotalPaycheckDebitAmt.negate();
        String feeActName = QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME;

        validateSpecifiedPayrollTxModTxLineExists(
                 payrollTxModResponseObj.getITXLINE(),
                 null,
                 "$"+ expectTotalPaycheckCreditAmt.toString(),
                 QBOFX.OFX_YN.Y,
                 QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK
         );

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$"+ transmissionFee.toString(),
                null,
                BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE
        );

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$"+ expectedTransactionFee.toString(),
                null,
                String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, Integer.parseInt(paycheckCntAtTimeOffloadStr), costPerTx.toString())
        );

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                null,
                "$"+ dmPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(2),
                QBOFX.OFX_YN.Y,
                QBOFX.MEMOS.getVoidAfterOffloadMemoStr(OFXDataloader.dmEmpName)
        );
    }

    @Test
    public void testVoidMultiplePaycheckAfterOffload() {
        try {

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOFX = ofxDataloader.loadHappyPathOFX();
            String payrollRespMsgOFXStr = QBDTTestHelper.offloadCompanyPayroll(happyPathOFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX payrollRespMsgOFXObj = OFXManager.ofxResponseToJava(payrollRespMsgOFXStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            String originalPayrollTxId = payrollRespMsgOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIPAYROLLTXID();

            PayrollServices.beginUnitOfWork();
            IPAYCHK dmPaycheckRequest = happyPathOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0);
            IPAYCHK alPaycheckRequest = happyPathOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1);

            List<String> paycheckVoidList = new ArrayList<String>();
            paycheckVoidList.add("1");
            paycheckVoidList.add("2");
            OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList,happyPathOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
            QBDTTestHelper.updateOFXWithNextCompanyToken(voidPayrollOfx);
            String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            PayrollServices.commitUnitOfWork();

            OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String responseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfx,AssistedConnectionInformation.getVoidPayrollMessage(2));
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFX = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            String ofxVoidResponseStr = OFXManager.javaToOFX(responseOFX,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            IPAYROLLUPDATERS payrollUpdateResponseObj = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS();
            validateVoidRun(payrollUpdateResponseObj,voidOfx.getIPAYROLLMSGSRQV1());
            List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModObjList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD();
            assertEquals(paycheckVoidList.size(),paycheckModObjList.size());
            List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD> paycheckModList = paycheckModObjList;
            // Match up the paycheck void request ids with the paycheck mods.
            for (int cnt=0;cnt<paycheckVoidList.size();cnt++) {
                com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD paycheckmod = paycheckModList.get(cnt);
                boolean found = false;
                for (int cnt2=0;cnt2<paycheckVoidList.size();cnt2++) {
                    if (paycheckmod.getIPAYCHKID().compareTo(paycheckVoidList.get(cnt2))!=0) {
                        continue;
                    }
                    validatePaycheckModVoid(paycheckmod,voidPayrollOfx,true);
                    found = true;
                    break;
                }
                assertTrue("Could not find paycheck void with id " + paycheckmod.getIPAYCHKID(),found);
            }

            List<IPAYROLLTX> payrollUpdateResponseList = payrollUpdateResponseObj.getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD();
            assertEquals(1,payrollUpdateResponseList.size());

            String paycheckCntAtTimeOffloadStr = "2";

            // Total Abe Lincoln's Two paychecks
            SpcfDecimal expectedTransactionFee = costPerTx.multiply(new SpcfDecimalImpl(paycheckCntAtTimeOffloadStr));

            SpcfDecimal dmTotalPaycheckDebitAmt = new SpcfMoney();
            dmTotalPaycheckDebitAmt = dmTotalPaycheckDebitAmt.add(new SpcfMoney(dmPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1,dmPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));
            SpcfDecimal abeLincolnTotalPaycheckDebitAmt = new SpcfMoney();
            abeLincolnTotalPaycheckDebitAmt = abeLincolnTotalPaycheckDebitAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(0).getIAMT().length())));
            abeLincolnTotalPaycheckDebitAmt = abeLincolnTotalPaycheckDebitAmt.add(new SpcfMoney(alPaycheckRequest.getIDDLINE().get(1).getIAMT().substring(1,alPaycheckRequest.getIDDLINE().get(1).getIAMT().length())));

            SpcfDecimal expectedTotal = new SpcfMoney();
            expectedTotal = expectedTotal.add(dmTotalPaycheckDebitAmt);
            expectedTotal = expectedTotal.add(abeLincolnTotalPaycheckDebitAmt);
            expectedTotal = expectedTotal.add(expectedTransactionFee.negate());
            expectedTotal = expectedTotal.add(transmissionFee.negate());
            PayrollServices.beginUnitOfWork();
            SpcfDecimal salesTax = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID,"1");
            PayrollServices.commitUnitOfWork();
            expectedTotal = expectedTotal.add(salesTax.negate());

            IPAYROLLTX payrollTxModResponseObj = payrollUpdateResponseList.get(0);
            validatePayrollTxModElementContents(payrollTxModResponseObj,
                voidPayrollOfx.getIPAYROLLMSGSRQV1(),
                "$"+expectedTotal.toString());

            SpcfDecimal expectTotalPaycheckCreditAmt = new SpcfMoney().negate();
            //@TODO Get real fee acct name once added.
            String feeActName = QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME;

            expectedTotal = expectedTotal.add(expectedTransactionFee);
            expectedTotal = expectedTotal.add(transmissionFee);

            validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                null,
                "$"+ expectTotalPaycheckCreditAmt.toString(),
                    QBOFX.OFX_YN.Y,
                    QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK
                    );

            validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$"+ transmissionFee.toString(),
                null,
                BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE
                );

            validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$"+ expectedTransactionFee.toString(),
                null,
                String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, Integer.parseInt(paycheckCntAtTimeOffloadStr), costPerTx.toString())
                );

            validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                null,
                "$"+ dmPaycheckRequest.getIDDLINE().get(0).getIAMT().substring(2),
                QBOFX.OFX_YN.Y,
                QBOFX.MEMOS.getVoidAfterOffloadMemoStr(OFXDataloader.dmEmpName)
                );

            validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                null,
                "$" + abeLincolnTotalPaycheckDebitAmt.negate().toString(),
                QBOFX.OFX_YN.Y,
                QBOFX.MEMOS.getVoidAfterOffloadMemoStr(OFXDataloader.alEmpName)
                );

        } catch (Exception e) {
            TestCase.fail(e.toString());
        }

    }

    private void validateSpecifiedPayrollTxModTxLineExists(
            List<ITXLINE> ddTxList,
            String acctName,
            String amount,
            String isDD,
            String memo) {
        boolean foundRecord = false;
        for (ITXLINE txLine : ddTxList) {
            if (!ObjectUtils.equals(acctName, QBOFX.nullStringCheck(txLine.getIACCTNAME()))) {
                continue;
            }

            if (!ObjectUtils.equals(amount, txLine.getIAMT())) {
                continue;
            }

            if (!ObjectUtils.equals(isDD, txLine.getIISDD())) {
                continue;
            }

            if (!ObjectUtils.equals(memo, txLine.getIMEMO())) {
                continue;
            }

            foundRecord=true;
            break;
        }
        assertTrue("Could not find expected payroll tx with amount "+amount+".",foundRecord);
    }

    private void validatePayrollTxModElementContents(IPAYROLLTX payrollTxModResponseObj,
                                                     IPAYROLLMSGSRQV1 voidPayrollReqMsgOFXObj,
                                                     String ftargetIAmt) throws Exception {
        //@TODO Need to get server payroll id here.
//        String originalPayrollRequestId = voidPayrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().get(0).getIPAYROLLTXID();
//        assertEquals(originalPayrollRequestId,payrollTxModResponseObj.getIPAYROLLTXID());
        assertEquals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE,payrollTxModResponseObj.getINAME());
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        CompanyBankAccount coBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
        String coCOA = coBankAcct.getSourceBankAccountName();
        assertEquals(coCOA,payrollTxModResponseObj.getIACCTNAME());
        assertEquals(ftargetIAmt,payrollTxModResponseObj.getIAMT());
        assertEquals(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK,payrollTxModResponseObj.getIMEMO());
        assertEquals(QBOFX.DEFAULT_CLEARED_RESPONSE_STR,payrollTxModResponseObj.getICLEARED());
        String expectedPaycheckDateStr = voidPayrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS();

        String DATE_FORMAT = "yyyyMMdd";
        DateFormat YYYYMMDDdfm = new SimpleDateFormat(DATE_FORMAT);

        SpcfCalendar expectedErDebitCal = SpcfCalendar.createInstance(YYYYMMDDdfm.parse(expectedPaycheckDateStr).getTime());
        CalendarUtils.addBusinessDays(expectedErDebitCal,-1);
        String expectedErDebitStr = YYYYMMDDdfm.format(expectedErDebitCal.getTimeInMilliseconds());

        assertEquals(expectedErDebitStr,payrollTxModResponseObj.getIDTTX());
        verifyOFXFieldStringEqual(QBOFX.EMPTY_STR,payrollTxModResponseObj.getIREFNUM());
        assertEquals(QBOFX.PAYROLL_TX_TYPE.PAYROLL_LIAB_CHECK,payrollTxModResponseObj.getIPAYROLLTXTYPE());
        String expectedPayPeriodEnd = voidPayrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS();
        assertEquals(expectedPayPeriodEnd,payrollTxModResponseObj.getIDTPAYPDEND());
        assertEquals(QBOFX.OFX_YN.N,payrollTxModResponseObj.getIVOID());
        assertEquals(QBOFX.OFX_YN.Y,payrollTxModResponseObj.getIONSERVICE());
    }

    private void validatePaycheckModVoid(com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD paycheckModObj,
                                      OFX payrollReqMsgOFXObj,
                                      boolean offloadAlreadyHappened) {

        // Verify Paycheck Mod Response
//        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTRNRS payrollTxResp = payrollUpdateResponseObj.getIPAYROLLTRNRS();

        com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLMSGSRQV1 voidPayrollMsg = payrollReqMsgOFXObj.getIPAYROLLMSGSRQV1();
        com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTRNRQ voidTxRequest = voidPayrollMsg.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ();

        com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN voidPayrollRunRequest = voidTxRequest.getIPAYROLLRQ().getIPAYROLLRUN().get(0);

        //Find paycheck Mod with this ID
        com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK paycheckModRequest = null;
        for (com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK paycheckModItem : voidPayrollRunRequest.getIPAYCHKMOD()) {
            if (paycheckModItem.getIPAYCHKID().compareTo(paycheckModObj.getIPAYCHKID())==0) {
                paycheckModRequest=paycheckModItem;
                break;
            }
        }
        voidPayrollRunRequest.getIPAYCHKMOD().get(0);
        assertNotNull(paycheckModRequest);

        verifyOFXFieldStringEqual(paycheckModRequest.getIEMPID(),paycheckModObj.getIEMPID());
        verifyOFXFieldStringEqual(paycheckModRequest.getIEMPID(),paycheckModObj.getIEMPID());
        verifyOFXFieldStringEqual(paycheckModRequest.getIEMPNAME(),paycheckModObj.getIEMPNAME());
        //@TODO Ask Tracey if this can change.
        verifyOFXFieldStringEqual(voidPayrollRunRequest.getIDTPAYCHKS(),paycheckModObj.getIDTTX());
        verifyOFXFieldStringEqual(paycheckModRequest.getIDTPAYPDBEGIN(),paycheckModObj.getIDTPAYPDBEGIN());
        verifyOFXFieldStringEqual(paycheckModRequest.getIDTPAYPDEND(),paycheckModObj.getIDTPAYPDEND());
        verifyOFXFieldStringEqual(paycheckModRequest.getICLEARED(),paycheckModObj.getICLEARED());
        verifyOFXFieldStringEqual(paycheckModRequest.getICLASS(),paycheckModObj.getICLASS());
        //@TODO Validate once COA defined.
//        paycheckModObj.getIACCTNAME();
        verifyOFXFieldStringEqual(QBOFX.ZERO_DOLLAR_AMT_STR,paycheckModObj.getIAMT());
        verifyOFXFieldStringEqual(paycheckModRequest.getIPAYCHKTYPE(),paycheckModObj.getIPAYCHKTYPE());

        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKINFO paycheckResponseInfoObj = paycheckModObj.getIPAYCHKINFO();
        com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHKINFO paycheckRequestInfoObj = paycheckModRequest.getIPAYCHKINFO();
        verifyOFXFieldStringEqual(paycheckRequestInfoObj.getICHKNUM(),paycheckResponseInfoObj.getICHKNUM());
        verifyOFXFieldStringEqual(paycheckRequestInfoObj.getIPRORATE(),paycheckResponseInfoObj.getIPRORATE());
        verifyOFXFieldStringIntValuesEqual(paycheckRequestInfoObj.getISICKACCRUED(),paycheckResponseInfoObj.getISICKACCRUED());
        verifyOFXFieldStringIntValuesEqual(paycheckRequestInfoObj.getIVACACCRUED(),paycheckResponseInfoObj.getIVACACCRUED());

        if (offloadAlreadyHappened) {
            verifyOFXFieldStringEqual(paycheckModRequest.getIMEMO() + " " + Paycheck.VOID_FUNDS_NOT_RECOVERED,paycheckModObj.getIMEMO());
        } else {
            verifyOFXFieldStringEqual(paycheckModRequest.getIMEMO() + " " + Paycheck.VOID_FUNDS_RECOVERED,paycheckModObj.getIMEMO());
        }
        verifyOFXFieldStringEqual(QBOFX.OFX_YN.Y,paycheckModObj.getIVOID());
        verifyOFXFieldStringEqual(QBOFX.OFX_YN.Y,paycheckModObj.getIONSERVICE());
    }

    private void verifyOFXFieldStringEqual(String expectedStr, String actualStr) {
        if (expectedStr.compareTo(QBOFX.EMPTY_STR)==0) {
            assertEquals("",actualStr);
        } else {
            assertEquals(expectedStr,actualStr);
        }
    }

    private void verifyOFXFieldStringIntValuesEqual(String expectedStr, String actualStr) {
        if (expectedStr.compareTo(QBOFX.EMPTY_STR)==0) {
            assertEquals("0",actualStr);
        } else {
            assertEquals(expectedStr,actualStr);
        }
    }

    private void validateVoidRun(com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLUPDATERS payrollUpdateResponseObj,
                                 IPAYROLLMSGSRQV1 payrollReqMsgOFXObj) {
        int paycheckVoidCnt = 0;

        for (IPAYROLLRUN payrollRun : payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
             paycheckVoidCnt += payrollRun.getIPAYCHKMOD().size();
        }
        validateVoidRun(payrollUpdateResponseObj,payrollReqMsgOFXObj,paycheckVoidCnt);
    }

    private void validateVoidRun(com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLUPDATERS payrollUpdateResponseObj,
                                 IPAYROLLMSGSRQV1 payrollReqMsgOFXObj,int paychecksVoidedWithEvents) {
// @TODO FIX ME!
//        assertEquals(Integer.parseInt(payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getTOKEN())+paychecksVoidedWithEvents,Integer.parseInt(payrollUpdateResponseObj.getTOKEN()));

        assertNull(payrollUpdateResponseObj.getIDDSTATUS());
        assertNull(payrollUpdateResponseObj.getIPAYROLLUPDATEDATA());
        assertNull(payrollUpdateResponseObj.getITAXSERVSTATUS());

        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTRNRS payrollTxResp = payrollUpdateResponseObj.getIPAYROLLTRNRS();
        com.intuit.sbd.payroll.psp.common.ofx.response.STATUS statusObj = payrollTxResp.getSTATUS();
        assertEquals(QBOFX.SUCCESS_STATUS_CODE,statusObj.getCODE());
        assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,statusObj.getSEVERITY());
        assertNull(statusObj.getMESSAGE());

        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS payrollRespObj = payrollTxResp.getIPAYROLLRS();
        if(payrollRespObj != null) {
            assertEquals(0,payrollRespObj.getIPAYROLLTX().size());
        }

    }

    @Test
    @Ignore("The new request processor merges duplicate paycheck mods")
    public void testVoidDuplicateFailure() {

        try {
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX requestOFX = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();

            String responseOFXStr = QBDTTestHelper.offloadCompanyPayroll(requestOFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFXObj = OFXManager.ofxResponseToJava(responseOFXStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = requestOFX.getIPAYROLLMSGSRQV1();
            PayrollServices.beginUnitOfWork();
            IPAYCHK dmPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0);
            IPAYCHK alPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1);

            List<String> paycheckVoidList = new ArrayList<String>();
            paycheckVoidList.add("1");
            OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList,payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
            List paycheckIdList = new ArrayList<String>();
            paycheckIdList.add("1");
            IPAYROLLMSGSRQV1 payrollCopy2 = ofxDataloader.loadVoidCompany3PayrollMessage(paycheckIdList,payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
            voidPayrollOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().add(payrollCopy2.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0));

            String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            PayrollServices.commitUnitOfWork();

//            OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String responseStr = QBDTTestHelper.processRequestPayrollErrorDynamicTransmissionError(voidPayrollStr,ErrorMessages.DuplicateIPaycheckModSubmitted("test"));
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//
//            String ofxResponseStr = OFXManagerQB.javaToOFX(ofxResponseObj);
//            String signOnResponseCode = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY();
//            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR,signOnResponseCode);
//            QBDTTestHelper.verifyTransmissionDataSaved(voidPayrollStr,ofxResponseStr, TransmissionType.PayrollSubmission,2);
//            QBDTTestHelper.verifyTransmissionMessageIsError();
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    // This will happen for migrated payrolls
    public void testPaycheckIdNotFoundForVoidOnlyPaycheckNotFound() throws Exception {
        // Process a sync request to handle the bank events.
        QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX payrollReqMsgOFXRoot = ofxDataloader.loadHappyPathOFX();
        PayrollServices.commitUnitOfWork();

        QBDTTestHelper.processOFXRequestPayrollSubmitSuccess(payrollReqMsgOFXRoot);

        PayrollServices.beginUnitOfWork();
        List<String> paycheckVoidList = new ArrayList<String>();
        paycheckVoidList.add("0");
        OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList,"20070101");
        PayrollServices.commitUnitOfWork();
        String responseStr = QBDTTestHelper.processOFXRequestPayrollSubmitSuccess(voidPayrollOfx);
        System.out.println(responseStr);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        // Verify IPAYROLLRS is empty and has no children.
        assertEquals(0,ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD().size());
        assertEquals(0,ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().size());
        assertEquals(0,ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD().size());
        assertFalse(responseStr.contains("<I.PAYROLLRS>^@~*"));
        assertTrue(responseStr.contains("<I.PAYROLLRS>"));
        assertTrue(responseStr.contains("</I.PAYROLLRS>"));
    }

    @Test
    // This will happen for migrated payrolls
    public void testPaycheckIdNotFoundForVoidPlusOneSuccessfulVoid() {

        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX payrollReqMsgOFXRoot = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();

            QBDTTestHelper.processOFXRequestPayrollSubmitSuccess(payrollReqMsgOFXRoot);

            PayrollServices.beginUnitOfWork();
            List<String> paycheckVoidList = new ArrayList<String>();
            paycheckVoidList.add("0");
            paycheckVoidList.add("1");
            OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList,"20070101");
            PayrollServices.commitUnitOfWork();
            String responseStr = QBDTTestHelper.processOFXRequestPayrollSubmitSuccess(voidPayrollOfx);
            System.out.println(responseStr);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(1,ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD().size());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    // This simulates the canceling of paycheck via DDM.  The
    //    QB sync happens before the paycheck was to be offloaded.
    public void testVoidExternallyCancelledPaycheck() throws Exception {
        // Process a sync request to handle the bank events.
        QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX requestOFX = ofxDataloader.loadHappyPathOFX();
        PayrollServices.commitUnitOfWork();

        String payrollRespMsgOFXStr = QBDTTestHelper.processOFXRequestSuccess(requestOFX);

        List<String> paycheckVoidList = new ArrayList<String>();
        paycheckVoidList.add("1");

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        Paycheck paycheck = Paycheck.findPaycheck(company,paycheckVoidList.get(0));
        PayrollRun payroll = paycheck.getPayrollRun();

        TransactionCancelEEDTO txCancelDTO = new TransactionCancelEEDTO();

        List sourcePaycheckList = new LinkedList();
        sourcePaycheckList.add(paycheck.getSourcePaycheckId());
        txCancelDTO.setSourcePaycheckIdList(sourcePaycheckList);
        txCancelDTO.setSourcePayrollRunId(payroll.getSourcePayRunId());
        ProcessResult cancelPR = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txCancelDTO);
        assertTrue(cancelPR.isSuccess());
        PayrollServices.commitUnitOfWork();

        // remove current principal from Agent
        PayrollServices.setCurrentPrincipal(principal);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(payrollRespMsgOFXStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        voidPaycheckFundsRecovered(requestOFX,ofxResponseObj,0, false);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        paycheck = Paycheck.findPaycheck(company,paycheckVoidList.get(0));
        assertTrue(paycheck.isRecalled());
    }

    @Test
    public void testVoidAndInsertDuplicateFailure() {

//        try {
//            PayrollServices.beginUnitOfWork();
//
//            OFXDataloader ofxDataloader = new OFXDataloader();
//            OFX voidPayrollOfx = ofxDataloader.loadHappyPathOFX();
//            List paycheckIdList = new ArrayList<String>();
//            paycheckIdList.add("1");
//            IPAYROLLMSGSRQV1 payrollCopy2 = ofxDataloader.loadVoidCompany3PayrollMessage(paycheckIdList,payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
//            voidPayrollOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().add(payrollCopy2.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0));
//
//            String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//            PayrollServices.commitUnitOfWork();
//
//            QBDTRequestProcessor qbdtRequestProcessor = new QBDTRequestProcessor();
//            String ofxResponseStr = qbdtRequestProcessor.processRequest(voidPayrollStr);
//            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
//            String signOnResponseCode = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY();
//            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.ERROR,signOnResponseCode);
//            QBDTTestHelper.verifyTransmissionDataSaved(voidPayrollStr,ofxResponseStr, TransmissionType.PayrollSubmission,1);
//            QBDTTestHelper.verifyTransmissionMessageIsError();
//        } catch (Exception e) {
//            TestCase.fail(e.toString());
//        }
    }

    public static SpcfDecimal getSalesTaxOffPayroll(String companyPSID,String paycheckId) {
        Company company = Company.findCompany(companyPSID, SourceSystemCode.QBDT);
        Paycheck paycheck = Paycheck.findPaycheck(company,paycheckId);
        PayrollRun payroll = paycheck.getPayrollRun();
        CompanyBankAccount coBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
        SpcfDecimal totalSalesTax = new SpcfMoney();
        for (BillingDetail billingDetail : payroll.getBillingDetailCollection()) {
            totalSalesTax = totalSalesTax.add(billingDetail.getTaxAmount());
        }
        return totalSalesTax;
    }

    @Test
    public void testVoidMultiplePaychecksBeforeOffloadVoidInDMMFirst() throws Exception {
        // Process a sync request to handle the bank events.
        QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX payrollReqMsgOFXRoot = ofxDataloader.loadHappyPathOFX();

        String payrollRespMsgOFXStr = QBDTTestHelper.processOFXRequestSuccess(payrollReqMsgOFXRoot);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(payrollRespMsgOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String originalPayrollTxId = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIPAYROLLTXID();

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        long beforeToken = company.getCurrentToken();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        Paycheck paycheck = Paycheck.findPaycheck(company, "1");
        PayrollRun payroll = paycheck.getPayrollRun();

        TransactionCancelEEDTO txCancelDTO = new TransactionCancelEEDTO();

        SpcfDecimal payrollAmt = payroll.getPayrollDirectDepositAmount();
        SpcfDecimal totalPaycheck1Amt = new SpcfMoney();
        List sourcePaycheckList = new LinkedList();
        sourcePaycheckList.add(paycheck.getSourcePaycheckId());
        for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
            totalPaycheck1Amt = totalPaycheck1Amt.add(paycheckSplit.getFinancialTransaction().getFinancialTransactionAmount());
        }
        txCancelDTO.setSourcePaycheckIdList(sourcePaycheckList);
        txCancelDTO.setSourcePayrollRunId(payroll.getSourcePayRunId());
        ProcessResult cancelPR = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txCancelDTO);
        assertTrue(cancelPR.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payroll = Application.findById(PayrollRun.class, payroll.getId());
        assertEquals(payrollAmt.subtract(totalPaycheck1Amt).toString(), payroll.getPayrollDirectDepositAmount().toString());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        paycheck = Paycheck.findPaycheck(company, "2");
        payroll = paycheck.getPayrollRun();

        txCancelDTO = new TransactionCancelEEDTO();

        sourcePaycheckList = new LinkedList();
        SpcfDecimal totalPaycheck2Amt = new SpcfMoney();
        sourcePaycheckList.add(paycheck.getSourcePaycheckId());
        for(PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
            totalPaycheck2Amt = totalPaycheck2Amt.add(paycheckSplit.getFinancialTransaction().getFinancialTransactionAmount());
        }
        txCancelDTO.setSourcePaycheckIdList(sourcePaycheckList);
        txCancelDTO.setSourcePayrollRunId(payroll.getSourcePayRunId());
        cancelPR = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txCancelDTO);
        assertTrue(cancelPR.isSuccess());

        payroll = Application.findById(PayrollRun.class, payroll.getId());
        assertEquals(payroll.getPayrollDirectDepositAmount().toString(), payrollAmt.subtract(totalPaycheck1Amt).subtract(totalPaycheck2Amt).toString());

        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        // remove current principal from Agent
        PayrollServices.setCurrentPrincipal(principal);

        IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = payrollReqMsgOFXRoot.getIPAYROLLMSGSRQV1();
        IPAYCHK dmPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0);
        IPAYCHK alPaycheckRequest = payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(1);

        List<String> paycheckVoidList = new ArrayList<String>();
        paycheckVoidList.add("1");
        paycheckVoidList.add("2");
        OFX voidPayrollOfx = ofxDataloader.loadVoidCompany3Payroll(paycheckVoidList, payrollReqMsgOFXObj.getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());
        String voidPayrollStr = OFXManager.javaToOFX(voidPayrollOfx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        PayrollServices.commitUnitOfWork();


        String responseStr = QBDTTestHelper.processOFXRequestSuccess(ofxDataloader.loadHappyPathSyncRequest(beforeToken + ""));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFX = OFXManager.ofxResponseToJava(responseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        List<IPAYROLLTX> payrollUpdateResponseList = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD();
        assertEquals(1, payrollUpdateResponseList.size());

        SpcfDecimal expectTotalDebitAmt = new SpcfMoney("0.00");

        String paycheckCntAtTimeOfOffloadStr = "0";

        SpcfDecimal expectedTransactionFee = costPerTx.multiply(new SpcfMoney(paycheckCntAtTimeOfOffloadStr));
        expectTotalDebitAmt = expectTotalDebitAmt.add(expectedTransactionFee);
        expectTotalDebitAmt = expectTotalDebitAmt.add(transmissionFee);
        PayrollServices.beginUnitOfWork();
        SpcfDecimal salesTax = getSalesTaxOffPayroll(CompanyQB1DataLoader.COMPANY_PSID, "1");
        PayrollServices.commitUnitOfWork();
        expectTotalDebitAmt = expectTotalDebitAmt.add(salesTax);

        IPAYROLLTX payrollTxModResponseObj = payrollUpdateResponseList.get(0);

        assertEquals(originalPayrollTxId, payrollTxModResponseObj.getIPAYROLLTXID());

        validatePayrollTxModElementContents(payrollTxModResponseObj,
                                            voidPayrollOfx.getIPAYROLLMSGSRQV1(),
                                            "$" + expectTotalDebitAmt.negate().toString());

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                null,
                "$0.00",
                QBOFX.OFX_YN.Y,
                QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK
        );

        //@TODO Get real fee acct name once added.
        String feeActName = QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME;

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$" + transmissionFee.toString(),
                null,
                BillingDetail.MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE
        );

        validateSpecifiedPayrollTxModTxLineExists(
                payrollTxModResponseObj.getITXLINE(),
                feeActName,
                "$" + expectedTransactionFee.toString(),
                null,
                String.format(BillingDetail.MEMOS.NO_FEE_FOR_DIRECT_DEPOSIT, Integer.parseInt(paycheckCntAtTimeOfOffloadStr))
        );


        OFX voidOfx = OFXManager.ofxRequestToJava(voidPayrollStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        responseStr = QBDTTestHelper.processOFXRequestSuccess(voidOfx, AssistedConnectionInformation.getVoidPayrollMessage(2));
        responseOFX = OFXManager.ofxResponseToJava(responseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        IPAYROLLUPDATERS payrollUpdateResponseObj = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS();
        validateVoidRun(payrollUpdateResponseObj, voidOfx.getIPAYROLLMSGSRQV1());
        List<IPAYCHKMOD> paycheckModObjList = responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYCHKMOD();
        assertEquals(paycheckVoidList.size(), paycheckModObjList.size());
        List<IPAYCHKMOD> paycheckModList = paycheckModObjList;
        assertEquals(paycheckVoidList.size(), paycheckModList.size());
        for (String paycheckVoidId : paycheckVoidList) {
            IPAYCHKMOD paycheckMod = null;
            // Find the paycheck mod matching the paycheck id voided.
            for (IPAYCHKMOD listPaycheckMod : paycheckModList) {
                if (listPaycheckMod.getIPAYCHKID().compareTo(paycheckVoidId) == 0) {
                    paycheckMod = listPaycheckMod;
                    break;
                }
            }
            assertNotNull(paycheckMod);
            validatePaycheckModVoid(paycheckMod, voidPayrollOfx, false);
        }
    }


}
