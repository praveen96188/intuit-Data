package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.CreateTransactionOffloadedEvents;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.ICOINFOMOD;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLUPDATEDATA;
import com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQBNotVerifiedBankAcctDataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Jul 15, 2008
 * Time: 3:59:34 PM
 */
public class SyncRequestTest {


    @Before
    public void runBeforeEachTest() {
        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }

    @Test
    public void testBankAccountAdded() {
        try {
            PayrollServices.beginUnitOfWork();
            Application.truncateTables();
            ApplicationSecondary.truncateTables();
            CompanyQBNotVerifiedBankAcctDataLoader companyQB1DataLoader = new CompanyQBNotVerifiedBankAcctDataLoader();
            Company company = companyQB1DataLoader.persistQBCompanyNotVerifiedCoBankAcct();
//            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            CompanyBankAccount coBankAcct = CompanyBankAccount.findCompanyBankAccounts(company).get(0);
            String companyCOA = coBankAcct.getSourceBankAccountName();
            String feeCOA = company.getQuickbooksInfo().getCoaFeeAccountName();
            DomainEntitySet<FinancialTransaction> verificationTransactions = coBankAcct.getVerificationTransactions();

            int nextTransactionId = Integer.valueOf(company.getNextPayrollTransactionId());

            ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
            for (FinancialTransaction financialTransaction : verificationTransactions) {
                amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
            }
            SpcfCalendar currPSPCal = PSPDate.getPSPTime();
            while (CalendarUtils.isWeekendOrHoliday(currPSPCal)) {
                CalendarUtils.addBusinessDays(currPSPCal, 1);
            }
            PSPDate.setPSPTime(currPSPCal);
            assertFalse("PSPDate on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
            assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
            Application.commitUnitOfWork();

            OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
            offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

            Application.beginUnitOfWork();

            // Set PSP Time to a date in the future so that the validation of settlement date will pass
            PSPDate.addDaysToPSPTime(10);

            FinancialTransaction smallerFnTx = verificationTransactions.get(0);
            FinancialTransaction largerFnTx = verificationTransactions.get(1);
            if (verificationTransactions.get(0).getFinancialTransactionAmount().compareTo(verificationTransactions.get(1).getFinancialTransactionAmount()) < 0) {
                smallerFnTx = verificationTransactions.get(0);
                largerFnTx = verificationTransactions.get(1);
            } else {
                smallerFnTx = verificationTransactions.get(1);
                largerFnTx = verificationTransactions.get(0);
            }

            ProcessResult<CompanyBankAccount> coBankAcctUpdated = PayrollServices.companyManager.verifyCompanyBankAccount(
                    SourceSystemCode.QBDT,
                    OFXDataloader.companyPSID,
                    coBankAcct.getSourceBankAccountId(),
                    smallerFnTx.getFinancialTransactionAmount(),
                    largerFnTx.getFinancialTransactionAmount(), false);
            assertTrue(coBankAcctUpdated.isSuccess());

            ProcessResult<HashMap<String, String>> procResult =
                    PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBDT, OFXDataloader.companyPSID, "test1234");
            PayrollServicesTest.assertSuccess("createPINResult", procResult);

            PayrollServices.commitUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathSyncOfxObj = ofxDataloader.loadHappyPathSyncRequest(QBOFX.EMPTY_STR);
            String responseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathSyncOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            List<IPAYROLLTX> txModList = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD();
            assertEquals(2, txModList.size());
            IPAYROLLTX txMod1 = txModList.get(0);
            DomainEntitySet<CompanyEvent> coEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.BankAccountVerified, null, null, null);
            assertEquals(1, coEventList.size());

            assertEquals(txMod1.getIPAYROLLTXID(), nextTransactionId + "");
            assertEquals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, txMod1.getINAME());
            assertEquals(companyCOA, txMod1.getIACCTNAME());
            assertEquals("$-" + smallerFnTx.getFinancialTransactionAmount().toString(), txMod1.getIAMT());
            assertEquals(QBOFX.MEMOS.getCreatedByPayrollServiceMemo(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), txMod1.getIMEMO());
            assertEquals(QBOFX.DEFAULT_CLEARED_RESPONSE_STR, txMod1.getICLEARED());
            assertEquals(QBOFX.getDTTXResponse(new Date(smallerFnTx.getSettlementDate().getTimeInMilliseconds())), txMod1.getIDTTX());
            assertEquals("", txMod1.getIREFNUM());
            assertEquals(QBOFX.PAYROLL_TX_TYPE.PAYROLL_LIAB_CHECK, txMod1.getIPAYROLLTXTYPE());
            assertEquals(QBOFX.getDTTXResponse(new Date(smallerFnTx.getSettlementDate().getTimeInMilliseconds())), txMod1.getIDTPAYPDEND());
            assertEquals(QBOFX.OFX_YN.N, txMod1.getIVOID());
            assertEquals(QBOFX.OFX_YN.Y, txMod1.getIONSERVICE());
            assertEquals(feeCOA, txMod1.getITXLINE().get(0).getIACCTNAME());
            assertEquals("$" + smallerFnTx.getFinancialTransactionAmount().toString(), txMod1.getITXLINE().get(0).getIAMT());
            assertEquals(BillingDetail.MEMOS.ENROLLMENT_FEE, txMod1.getITXLINE().get(0).getIMEMO());

            IPAYROLLTX txMod2 = txModList.get(1);
            assertEquals(txMod2.getIPAYROLLTXID(), (nextTransactionId + 1) + "");
            assertEquals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, txMod2.getINAME());
            assertEquals(companyCOA, txMod2.getIACCTNAME());
            assertEquals("$-" + largerFnTx.getFinancialTransactionAmount().toString(), txMod2.getIAMT());
            assertEquals(QBOFX.MEMOS.getCreatedByPayrollServiceMemo(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), txMod2.getIMEMO());
            assertEquals(QBOFX.DEFAULT_CLEARED_RESPONSE_STR, txMod2.getICLEARED());
            assertEquals(QBOFX.getDTTXResponse(new Date(largerFnTx.getSettlementDate().getTimeInMilliseconds())), txMod2.getIDTTX());
            assertEquals("", txMod2.getIREFNUM());
            assertEquals(QBOFX.PAYROLL_TX_TYPE.PAYROLL_LIAB_CHECK, txMod2.getIPAYROLLTXTYPE());
            assertEquals(QBOFX.getDTTXResponse(new Date(largerFnTx.getSettlementDate().getTimeInMilliseconds())), txMod2.getIDTPAYPDEND());
            assertEquals(QBOFX.OFX_YN.N, txMod2.getIVOID());
            assertEquals(QBOFX.OFX_YN.Y, txMod2.getIONSERVICE());
            assertEquals(feeCOA, txMod2.getITXLINE().get(0).getIACCTNAME());
            assertEquals("$" + largerFnTx.getFinancialTransactionAmount().toString(), txMod2.getITXLINE().get(0).getIAMT());
            assertEquals(BillingDetail.MEMOS.ENROLLMENT_FEE, txMod2.getITXLINE().get(0).getIMEMO());

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testLegalAddressChanged() {
        try {
            // Process a sync request to handle the bank events.
            String origSyncResponseStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX origSyncResponseOFX = OFXManager.ofxResponseToJava(origSyncResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String newLegalAddrLine1 = company.getLegalAddress().getAddressLine1() + "x";
            String newLegalAddrLine2 = company.getLegalAddress().getAddressLine2() + "x";
            String newLegalAddrCity = company.getLegalAddress().getCity() + "x";
            String origFEIN = company.getFedTaxId();
            String origLegalName = company.getLegalName();
            String newLegalZipCode = "12233";
            String origServiceKey = company.getActivePrimaryEntitlementUnit().getServiceKey();
            String newLegalAddrState = "NM";

            long origToken = Long.parseLong(origSyncResponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN());

            CompanyBankAccount origCoBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
            String origCOA = origCoBankAcct.getSourceBankAccountName();

            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            AddressDTO addressDTO = coDTO.getLegalAddress();
            addressDTO.setAddressLine1(newLegalAddrLine1);
            addressDTO.setAddressLine2(newLegalAddrLine2);
            addressDTO.setCity(newLegalAddrCity);
            addressDTO.setState(newLegalAddrState);
            addressDTO.setZipCode(newLegalZipCode);

            PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), coDTO);
            PayrollServices.commitUnitOfWork();


            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = performCoInfoModSync(origToken + "");
            assertEquals(ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            ICOINFOMOD coInfoModObj = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD();

            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            assertEquals(origToken + 1, Long.parseLong(ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN()));

            CompanyBankAccount newCoBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
            String newCOA = newCoBankAcct.getSourceBankAccountName();
            assertEquals(origCOA, newCOA);
            assertEquals(coInfoModObj.getIADDR1(), newLegalAddrLine1);
            assertEquals(coInfoModObj.getIADDR2(), QBOFX.convertNULLToEmptyString(newLegalAddrLine2));
            assertEquals(coInfoModObj.getICITY(), newLegalAddrCity);
            assertEquals(coInfoModObj.getIFEIN(), "24-2335465");
            assertEquals(coInfoModObj.getILEGALNAME(), origLegalName);
            assertEquals(coInfoModObj.getIPOSTALCODE(), newLegalZipCode);
            assertEquals(coInfoModObj.getISERVICEKEY(), origServiceKey);
            assertEquals(coInfoModObj.getISTATE(), newLegalAddrState);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testLegalNameChanged() {
        try {
            // Process a sync request to handle the bank events.
            String origSyncResponseStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX origSyncResponseOFX = OFXManager.ofxResponseToJava(origSyncResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            long origToken = Long.parseLong(origSyncResponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN());

            String origLegalAddrLine1 = company.getLegalAddress().getAddressLine1();
            String origLegalAddrLine2 = company.getLegalAddress().getAddressLine2();
            String origLegalAddrCity = company.getLegalAddress().getCity();
            String origFEIN = company.getFedTaxId();
            String origLegalName = company.getLegalName();
            String origLegalZipCode = company.getLegalAddress().getZipCode();
            String origServiceKey = company.getActivePrimaryEntitlementUnit().getServiceKey();
            String origLegalAddrState = company.getLegalAddress().getState();

            CompanyBankAccount origCoBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
            String origCOA = origCoBankAcct.getSourceBankAccountName();

            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            String newLegalName = "New Legal Name";
            coDTO.setLegalName(newLegalName);
            PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), coDTO);
            PayrollServices.commitUnitOfWork();

            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = performCoInfoModSync("1");
            assertEquals(ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            ICOINFOMOD coInfoModObj = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD();

            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            CompanyBankAccount newCoBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
            String newCOA = newCoBankAcct.getSourceBankAccountName();
            assertEquals(origCOA, newCOA);
            assertEquals(coInfoModObj.getIADDR1(), origLegalAddrLine1);
            assertEquals(coInfoModObj.getIADDR2(), QBOFX.convertNULLToEmptyString(origLegalAddrLine2));
            assertEquals(coInfoModObj.getICITY(), origLegalAddrCity);
            assertEquals(coInfoModObj.getIFEIN(), "24-2335465");
            assertEquals(coInfoModObj.getILEGALNAME(), newLegalName);
            assertEquals(coInfoModObj.getIPOSTALCODE(), origLegalZipCode);
            assertEquals(coInfoModObj.getISERVICEKEY(), origServiceKey);
            assertEquals(coInfoModObj.getISTATE(), origLegalAddrState);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testEINChanged() {
        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String origLegalAddrLine1 = company.getLegalAddress().getAddressLine1();
            String origLegalAddrLine2 = company.getLegalAddress().getAddressLine2();
            String origLegalAddrCity = company.getLegalAddress().getCity();
            String origFEIN = company.getFedTaxId();
            String origLegalName = company.getLegalName();
            String origLegalZipCode = company.getLegalAddress().getZipCode();
            String origLegalAddrState = company.getLegalAddress().getState();

            CompanyBankAccount origCoBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
            String origCOA = origCoBankAcct.getSourceBankAccountName();

            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            String newFEIN = "121231231";
            coDTO.setFein(newFEIN);
            PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), coDTO);
            PayrollServices.commitUnitOfWork();
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = performCoInfoModSync("1");
            assertEquals(ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            ICOINFOMOD coInfoModObj = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD();

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            CompanyBankAccount newCoBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
            String newCOA = newCoBankAcct.getSourceBankAccountName();
            assertEquals(origCOA, newCOA);
            assertEquals(coInfoModObj.getIADDR1(), origLegalAddrLine1);
            assertEquals(coInfoModObj.getIADDR2(), QBOFX.convertNULLToEmptyString(origLegalAddrLine2));
            assertEquals(coInfoModObj.getICITY(), origLegalAddrCity);
            assertEquals(coInfoModObj.getIFEIN(), "12-1231231");
            assertEquals(coInfoModObj.getILEGALNAME(), origLegalName);
            assertEquals(coInfoModObj.getIPOSTALCODE(), origLegalZipCode);
            assertEquals(coInfoModObj.getISERVICEKEY(), company.getActivePrimaryEntitlementUnit().getServiceKey());
            assertEquals(coInfoModObj.getISTATE(), origLegalAddrState);
            PayrollServices.rollbackUnitOfWork();

        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }

    @Test
    // Verify that only one event gets passed back each time when token from
    //   response is passed in for next request.  Then verify that both
    //   event get passed back if resync.
    public void testEventsQuantityProcessed() {
        try {
            // Process a sync request to handle the bank events.
            String bankResponseOFXStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxBankResponse = OFXManager.ofxResponseToJava(bankResponseOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            // Should not be a CONIFOMOD
            assertEquals(ofxBankResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            assertTrue(ofxBankResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD() == null);
            // Should be two bank returns
            int memoCnt = 0;
            for (IPAYROLLTX txMod : ofxBankResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD()) {
                for (ITXLINE txLine : txMod.getITXLINE()) {
                    String memo = txLine.getIMEMO();
                    assertEquals(memo, BillingDetail.MEMOS.ENROLLMENT_FEE);
                    memoCnt++;
                }
            }
            assertEquals(memoCnt, 2);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            String newFEIN = "121231231";
            coDTO.setFein(newFEIN);
            PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), coDTO);
            PayrollServices.commitUnitOfWork();

            String tokenFromBankResponse = ofxBankResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN() + "";
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxCoInfoResponseObj = performCoInfoModSync(tokenFromBankResponse);
            assertEquals(ofxCoInfoResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            assertTrue(ofxCoInfoResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD() != null);
            assertEquals(0, ofxCoInfoResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().size());

            // Now verify
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxReSyncResponseObj = performCoInfoModSync("0");
            assertEquals(ofxReSyncResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            assertTrue(ofxReSyncResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD() != null);
            assertEquals(2, ofxReSyncResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().size());

        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }


    private com.intuit.sbd.payroll.psp.common.ofx.response.OFX performCoInfoModSync(String token) throws Exception {
        OFXDataloader ofxDataloader = new OFXDataloader();
        com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathSyncOfxObj = ofxDataloader.loadHappyPathSyncRequest(token);
        String responseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathSyncOfxObj);
        return OFXManager.ofxResponseToJava(responseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
    }

    @Test
    public void testNSFFee() {
        try {

            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            // Offload Payroll
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX requestOFX = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            QBDTTestHelper.offloadCompanyPayroll(requestOFX);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            int nextTransactionId = Integer.valueOf(company.getNextPayrollTransactionId());

            OffloadGroup offloadGroup = company.getOffloadGroup();

            SpcfCalendar offloadDate = FinancialTransaction.getSettlementDate(offloadGroup);
            SpcfCalendar paycheckDate = offloadDate.copy();

            PayrollServices.commitUnitOfWork();

            SpcfCalendar createDate = PSPDate.getPSPTime();
            setupNSFFee();
            verifyFee(nextTransactionId, createDate, paycheckDate, BillingDetail.MEMOS.NSF_FEE_FOR_RETURNED_PAYROLL);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testPaymentArrangementFee() {
        try {

            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            // Offload Payroll
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX requestOFX = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            String reponseOFXStr = QBDTTestHelper.offloadCompanyPayroll(requestOFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX reponseOFX = OFXManager.ofxResponseToJava(reponseOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            int nextTransactionId = Integer.valueOf(company.getNextPayrollTransactionId());

            PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
            ERFeeAddDTO erFeeAddDTO = new ERFeeAddDTO();
            erFeeAddDTO.setFeeTypeCode(OfferingServiceChargeType.PaymentArrangementFee);
            erFeeAddDTO.setSettlementTypeCode(SettlementTypeDTO.ACH);
            erFeeAddDTO.setSourceCompanyId(company.getSourceCompanyId());
            erFeeAddDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            erFeeAddDTO.setSourceSystemCd(company.getSourceSystemCd());
//            erFeeAddDTO.setTxDate();

            SpcfCalendar createDate = PSPDate.getPSPTime().copy();
            SpcfCalendar fnTxSettlmentDate = assertSuccessResult(PayrollServices.financialTransactionManager.addFeeTransaction(erFeeAddDTO)).getFirst().getSettlementDate();
            PayrollServices.commitUnitOfWork();

            SpcfCalendar fakePaycheckDate = fnTxSettlmentDate.copy();
            CalendarUtils.addBusinessDays(fakePaycheckDate, 1);
            QBDTTestHelper.runOffload(QBOFX.getDate("yyyyMMdd", new Date(fakePaycheckDate.getTimeInMilliseconds())));

            verifyFee(nextTransactionId, createDate, fnTxSettlmentDate, BillingDetail.MEMOS.PAYMENT_ARRANGEMENT_FEE);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testReversalFee() {
        try {

            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            // Offload Payroll
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX requestOFX = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            String reponseOFXStr = QBDTTestHelper.offloadCompanyPayroll(requestOFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX reponseOFX = OFXManager.ofxResponseToJava(reponseOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            int nextTransactionId = Integer.valueOf(company.getNextPayrollTransactionId());

            PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
            ERFeeAddDTO erFeeAddDTO = new ERFeeAddDTO();
            erFeeAddDTO.setFeeTypeCode(OfferingServiceChargeType.ReversalFee);
            erFeeAddDTO.setSettlementTypeCode(SettlementTypeDTO.ACH);
            erFeeAddDTO.setSourceCompanyId(company.getSourceCompanyId());
            erFeeAddDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            erFeeAddDTO.setSourceSystemCd(company.getSourceSystemCd());
//            erFeeAddDTO.setTxDate();

            SpcfCalendar createDate = PSPDate.getPSPTime().copy();
            SpcfCalendar fnTxSettlmentDate = assertSuccessResult(PayrollServices.financialTransactionManager.addFeeTransaction(erFeeAddDTO)).getFirst().getSettlementDate();
            PayrollServices.commitUnitOfWork();

            SpcfCalendar fakePaycheckDate = fnTxSettlmentDate.copy();
            CalendarUtils.addBusinessDays(fakePaycheckDate, 1);
            QBDTTestHelper.runOffload(QBOFX.getDate("yyyyMMdd", new Date(fakePaycheckDate.getTimeInMilliseconds())));

            verifyFee(nextTransactionId, createDate, fnTxSettlmentDate, BillingDetail.MEMOS.DIRECT_DEPOSIT_REVERSAL_FEE);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }


    private void verifyFee(int nextTransactionId, SpcfCalendar createDate, SpcfCalendar txDate, String memoStr) throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        CompanyBankAccount coBankAcct = CompanyBankAccount.findCompanyBankAccounts(company).get(0);
        String companyCOA = coBankAcct.getSourceBankAccountName();
        String feeCOA = company.getQuickbooksInfo().getCoaFeeAccountName();
        String salesTaxCOA = company.getQuickbooksInfo().getCoaSalesTaxAccountName();
        DomainEntitySet<CompanyEvent> coEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeOffloaded, null, null, null);
        assertEquals(1, coEventList.size());
        PayrollServices.commitUnitOfWork();

        String responseOFX = QBDTTestHelper.processOFXSyncRequestDataRecovery((company.getCurrentToken() - 1) + "");
        System.out.println(responseOFX);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX reponseOFX = OFXManager.ofxResponseToJava(responseOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertEquals(reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
        List<IPAYROLLTX> txModList = reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD();
        assertEquals(2, txModList.size());
        IPAYROLLTX txMod = null;
        for (IPAYROLLTX ipayrolltx : txModList) {
            if (ipayrolltx.getIPAYROLLTXID().equals(nextTransactionId + "")) {
                txMod = ipayrolltx;
            }
        }
        assertNotNull(txMod);

        assertEquals(txMod.getIPAYROLLTXID(), nextTransactionId + "");
        assertEquals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, txMod.getINAME());
        assertEquals(companyCOA, txMod.getIACCTNAME());
        assertEquals(QBOFX.MEMOS.getCreatedByPayrollServiceMemo(new Date(createDate.getTimeInMilliseconds())), txMod.getIMEMO());
        assertEquals(QBOFX.DEFAULT_CLEARED_RESPONSE_STR, txMod.getICLEARED());
        assertEquals(QBOFX.getDTTXResponse(new Date(txDate.getTimeInMilliseconds())), txMod.getIDTTX());
        assertEquals("", txMod.getIREFNUM());
        assertEquals(QBOFX.PAYROLL_TX_TYPE.PAYROLL_LIAB_CHECK, txMod.getIPAYROLLTXTYPE());
        assertEquals(QBOFX.getDTTXResponse(new Date(txDate.getTimeInMilliseconds())), txMod.getIDTPAYPDEND());
        assertEquals(QBOFX.OFX_YN.N, txMod.getIVOID());
        assertEquals(QBOFX.OFX_YN.Y, txMod.getIONSERVICE());
        assertEquals(feeCOA, txMod.getITXLINE().get(0).getIACCTNAME());
        assertNull(txMod.getITXLINE().get(0).getIISDD());
        assertEquals(memoStr, txMod.getITXLINE().get(0).getIMEMO());
        // Sales Tax
        assertEquals(salesTaxCOA, txMod.getITXLINE().get(1).getIACCTNAME());
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        assertEquals(QBOFX.MEMOS.getSalesTaxMemo(company.getLegalAddress().getState()), txMod.getITXLINE().get(1).getIMEMO());
        PayrollServices.commitUnitOfWork();
    }

    private void setupNSFFee() throws Exception {

//            QBDTTestHelper.submitCompanyPayroll();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

        // Get a payroll id
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);

        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO();
        feeAddDTO.setFeeTypeCode(OfferingServiceChargeType.DebitReturnFee);
        feeAddDTO.setSettlementTypeCode(SettlementTypeDTO.ACH);
        feeAddDTO.setSourceCompanyId(company.getSourceCompanyId());
        feeAddDTO.setSourcePayrollRunId(payrollRuns.get(0).getSourcePayRunId());
        feeAddDTO.setSourceSystemCd(company.getSourceSystemCd());

        ProcessResult<DomainEntitySet<FinancialTransaction>> addFeeTxPR = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
//            assertEquals(1,addFeeTxPR.getResult().size());
        assertTrue(addFeeTxPR.isSuccess());
//            FinancialTransaction updatedFnTx = addFeeTxPR.getResult();

        OffloadGroup offloadGroup = company.getOffloadGroup();
        SpcfCalendar offloadDate = FinancialTransaction.getSettlementDate(offloadGroup);
        CalendarUtils.addBusinessDays(offloadDate, 1);

        PayrollServices.commitUnitOfWork();
        QBDTTestHelper.runOffload(QBOFX.getDate("yyyyMMdd", new Date(offloadDate.getTimeInMilliseconds())));

    }

    @Test
    public void testACHReturnR01() throws Exception {
        performACHReturnTest(ACHReturnReason.R01, ReturnReasonDesc.findReturnDescription("R01"));
    }

    @Test
    public void testACHReturnR02() throws Exception {
        performACHReturnTest(ACHReturnReason.R02, ReturnReasonDesc.findReturnDescription("R02"));
    }

    @Test
    public void testACHReturnR03() throws Exception {
        performACHReturnTest(ACHReturnReason.R03, ReturnReasonDesc.findReturnDescription("R03"));
    }

    @Test
    public void testACHReturnR04() throws Exception {
        performACHReturnTest(ACHReturnReason.R04, ReturnReasonDesc.findReturnDescription("R04"));
    }

    @Test
    public void testACHReturnR05() throws Exception {
        performACHReturnTest(ACHReturnReason.R05, ReturnReasonDesc.findReturnDescription("R05"));
    }

    @Test
    public void testACHReturnR06() throws Exception {
        performACHReturnTest(ACHReturnReason.R06, ReturnReasonDesc.findReturnDescription("R06"));
    }

    @Test
    public void testACHReturnR07() throws Exception {
        performACHReturnTest(ACHReturnReason.R07, ReturnReasonDesc.findReturnDescription("R07"));
    }

    @Test
    public void testACHReturnR08() throws Exception {
        performACHReturnTest(ACHReturnReason.R08, ReturnReasonDesc.findReturnDescription("R08"));
    }

    @Test
    public void testACHReturnR09() throws Exception {
        performACHReturnTest(ACHReturnReason.R09, ReturnReasonDesc.findReturnDescription("R09"));
    }

    @Test
    public void testACHReturnR10() throws Exception {
        performACHReturnTest(ACHReturnReason.R10, ReturnReasonDesc.findReturnDescription("R10"));
    }

    @Test
    public void testACHReturnR12() throws Exception {
        performACHReturnTest(ACHReturnReason.R12, ReturnReasonDesc.findReturnDescription("R12"));
    }

    @Test
    public void testACHReturnR13() throws Exception {
        performACHReturnTest(ACHReturnReason.R13, ReturnReasonDesc.findReturnDescription("R13"));
    }

    @Test
    public void testACHReturnR14() throws Exception {
        performACHReturnTest(ACHReturnReason.R14, ReturnReasonDesc.findReturnDescription("R14"));
    }

    @Test
    public void testACHReturnR15() throws Exception {
        performACHReturnTest(ACHReturnReason.R15, ReturnReasonDesc.findReturnDescription("R15"));
    }

    @Test
    public void testACHReturnR16() throws Exception {
        performACHReturnTest(ACHReturnReason.R16, ReturnReasonDesc.findReturnDescription("R16"));
    }

    @Test
    public void testACHReturnR18() throws Exception {
        performACHReturnTest(ACHReturnReason.R18, ReturnReasonDesc.findReturnDescription("R18"));
    }

    @Test
    public void testACHReturnR20() throws Exception {
        performACHReturnTest(ACHReturnReason.R20, ReturnReasonDesc.findReturnDescription("R20"));
    }

    @Test
    public void testACHReturnR24() throws Exception {
        performACHReturnTest(ACHReturnReason.R24, ReturnReasonDesc.findReturnDescription("R24"));
    }

    @Test
    public void testACHReturnR28() throws Exception {
        performACHReturnTest(ACHReturnReason.R28, ReturnReasonDesc.findReturnDescription("R28"));
    }

    @Test
    public void testACHReturnR29() throws Exception {
        performACHReturnTest(ACHReturnReason.R29, ReturnReasonDesc.findReturnDescription("R29"));
    }

    private void performACHReturnTest(ACHReturnReason achReturnReason, String achTxMemo) throws Exception {
        // Process a sync request to handle the bank events.
        QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

        //Offload a payroll
        PayrollServices.beginUnitOfWork();
        OFXDataloader ofxDataloader = new OFXDataloader();
        OFX requestOFX = ofxDataloader.loadHappyPathOFX();
        PayrollServices.commitUnitOfWork();
        String origOFXResponseStr = QBDTTestHelper.offloadCompanyPayroll(requestOFX);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX origReponseOFX = OFXManager.ofxResponseToJava(origOFXResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String origResponseToken = origReponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN();


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        String nextPayrollTxIdStr = company.getNextPayrollTransactionId();

        CompanyBankAccount coBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);
        String coCOA = coBankAcct.getSourceBankAccountName();

        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRuns(company);
        DomainEntitySet<FinancialTransaction> eeFnTxns = payrollRunList.get(0).getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransactions.add(eeFnTxns.get(0));
        DataLoadServices.returnTxns(financialTransactions, achReturnReason.toString(), achTxMemo + "      123456");

        String responseOFX = QBDTTestHelper.processOFXSyncRequestDataRecovery(origResponseToken);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX reponseOFX = OFXManager.ofxResponseToJava(responseOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        assertEquals(reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
        List<IPAYROLLTX> txModList = reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> coEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.DDReject, null, null, null);
        CompanyEvent coEvent = coEventList.get(0);

        DomainEntitySet<CompanyEventDetail> fnTxCoEventList = coEvent.getCompanyEventDetails(EventDetailTypeCode.FinancialTransactionId);
        DomainEntitySet<FinancialTransaction> fnTxList = PayrollServices.entityFinder.find(FinancialTransaction.class, FinancialTransaction.Id().equalTo(SpcfUniqueId.createInstance(fnTxCoEventList.get(0).getValue())));
        FinancialTransaction fnTx = fnTxList.get(0);

        assertEquals(1, txModList.size());
        IPAYROLLTX txMod = txModList.get(0);
        assertEquals(txMod.getIPAYROLLTXID(), nextPayrollTxIdStr);

        assertEquals(coCOA, txMod.getIACCTNAME());
        assertEquals("$" + fnTx.getFinancialTransactionAmount().toString(), txMod.getIAMT());
        assertEquals(QBOFX.DEFAULT_CLEARED_RESPONSE_STR, txMod.getICLEARED());

        assertEquals(QBOFX.getDTTXResponse(new Date(fnTx.getSettlementDate().getTimeInMilliseconds())), txMod.getIDTPAYPDEND());
        assertEquals(QBOFX.getDTTXResponse(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())), txMod.getIDTTX());
        String employeeId = fnTx.getPaycheckSplit().getPaycheck().getDDEmployee().getLastName() + ", " + fnTx.getPaycheckSplit().getPaycheck().getDDEmployee().getFirstName();
        assertEquals(employeeId, txMod.getIEMPNAME());
        assertEquals(null, txMod.getIMEMO());
        assertEquals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, txMod.getINAME());
        assertEquals(QBOFX.OFX_YN.Y, txMod.getIONSERVICE());
        // Company payroll tx should be one more than this transaction id
        assertEquals(Integer.parseInt(company.getNextPayrollTransactionId()),
                     Integer.parseInt(txMod.getIPAYROLLTXID()) + 1);

        assertEquals(QBOFX.PAYROLL_TX_TYPE.DD_RETURN, txMod.getIPAYROLLTXTYPE());
        assertEquals("", txMod.getIREFNUM());
        assertEquals(1, txMod.getITXLINE().size());
        assertEquals(null, QBOFX.nullStringCheck(txMod.getITXLINE().get(0).getIACCTNAME()));
        assertEquals("$-" + fnTx.getFinancialTransactionAmount().toString(), txMod.getITXLINE().get(0).getIAMT());
        assertEquals(QBOFX.OFX_YN.Y, txMod.getITXLINE().get(0).getIISDD());
        assertEquals(achTxMemo, txMod.getITXLINE().get(0).getIMEMO());
        assertEquals(QBOFX.OFX_YN.N, txMod.getIVOID());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEmptySync() {
        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String nextTokenStr = (company.getCurrentToken()) + "";
            String nextEmpIdStr = company.getNextEmployeeId();
            String nextPaycheckIdStr = company.getNextPaycheckId();
            String nextPayrollTxIdStr = company.getNextPayrollTransactionId();
            String nextPayItemIdStr = company.getNextPayrollItemId();
            PayrollServices.commitUnitOfWork();

            String responseOfx = QBDTTestHelper.processOFXSyncRequestHappyPath(nextTokenStr);

            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFX = OFXManager.ofxResponseToJava(responseOfx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertNotNull(responseOFX.getSIGNONMSGSRSV1().getSONRS().getDTSERVER());
            assertEquals(QBOFX.LANGUAGE, responseOFX.getSIGNONMSGSRSV1().getSONRS().getLANGUAGE());
            assertNull(responseOFX.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE());
            assertEquals(QBOFX.SUCCESS_STATUS_CODE, responseOFX.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE());
            assertEquals(QBOFX.MESSAGE_SEVERITY.INFO, responseOFX.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY());

            //assertNull(responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIDDSTATUS());

            assertEquals(nextEmpIdStr, responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIEMPNEXTID());
            assertEquals(nextPaycheckIdStr, responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYCHKNEXTID());
            assertNull(responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS());
            assertEquals(nextPayrollTxIdStr, responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTXNEXTID());
            assertNull(responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA());
            assertEquals(nextPayItemIdStr, responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPITEMNEXTID());
            //assertNull(responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS());
            assertEquals(nextTokenStr, responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testPSRV001576() {
        try {
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String nextTokenStr = (company.getCurrentToken()) + "";

            Contact contact = company.getContactByRoleCode(ContactRole.PayrollAdmin);
            contact.setPhone(null);
            Application.save(contact);

            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX companyInfoMod = ofxDataloader.loadCompany3WithCOINFOMODChangeOnlyLegalNameAndAddress();
            String companyInfoModString = OFXManager.javaRequestToOFX(companyInfoMod, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            QBDTTestHelper.processRequestPayrollErrorDynamicTransmissionError(companyInfoModString, ErrorMessages.AssistedProcessingDataError("5001"));
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testSyncRerequestLastTokenSymphony() {
        setupSyncLastRequest();

        // hijack the company
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        String licNum = eu.getEntitlement().getLicenseNumber();
        String eoc = eu.getEntitlement().getEntitlementOfferingCode();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, OFXDataloader.companyPSID, ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathPayrollOfxObj = ofxDataloader.loadHappyPathOFXPayroll1();
            QBDTTestHelper.processOFXRequestSuccess(happyPathPayrollOfxObj);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }

        company = DataLoadServices.refreshCompany(company);
        String currentTokenStr = company.getCurrentToken() + "";
        String nextTokenStr = (company.getCurrentToken() - 2) + "";

        DataLoadServices.deactivateEntitlementUnit(eu);
        DataLoadServices.addEntitlementUnit(company, "09876543210987654321", "09876543210987654321", EditionType.Basic, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, null);

        // test
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathSyncOfxObj = ofxDataloader.loadHappyPathSyncRequest(nextTokenStr);
            String responseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathSyncOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(responseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals("expected token update", currentTokenStr, ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN());
            //assertNull(ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }

        // roll back the company entitlement
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
        PayrollServices.companyManager.reactivateService(SourceSystemCode.QBDT, OFXDataloader.companyPSID, ServiceCode.DirectDeposit);
        eu = company.getActivePrimaryEntitlementUnit();
        PayrollServices.commitUnitOfWork();
        DataLoadServices.deactivateEntitlementUnit(eu);
        DataLoadServices.addEntitlementUnit(company, licNum, eoc, EditionType.Basic, NumberOfEmployeesType.UPTO3, DataLoadServices.AssetItemNumber.DIY_YEARLY, null);
    }


    private void setupSyncLastRequest() {
        try {
// Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String nextTokenStr = (company.getCurrentToken()) + "";
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathSyncOfxObj = ofxDataloader.loadHappyPathSyncRequest(nextTokenStr);

            QBDTTestHelper.processOFXRequestSuccess(happyPathSyncOfxObj);

            OFX happyPathPayroll1 = ofxDataloader.loadHappyPathOFXPayroll1();
            QBDTTestHelper.processOFXRequestSuccess(happyPathPayroll1);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            nextTokenStr = (company.getCurrentToken()) + "";
            PayrollServices.commitUnitOfWork();

            happyPathSyncOfxObj = ofxDataloader.loadHappyPathSyncRequest(nextTokenStr);
            QBDTTestHelper.processOFXRequestSuccess(happyPathSyncOfxObj);

            OFX happyPathPayroll2 = ofxDataloader.loadHappyPathOFXPayroll2();
//            String happyPathPayroll1OFXStr = OFXManager.javaRequestToOFX(happyPathPayroll2,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            QBDTTestHelper.processOFXRequestSuccess(happyPathPayroll2);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }

    }

    @Test
    public void testDDCompanyTerminated() {
        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            // Test that ACTIVE or TERMINATED returned accordingly
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String nextTokenStr = (company.getCurrentToken()) + "";
            PayrollServices.companyManager.terminateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathSyncOfxObj = ofxDataloader.loadHappyPathSyncRequest(QBOFX.EMPTY_STR);
            happyPathSyncOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(nextTokenStr);
            String responseOFX = QBDTTestHelper.processOFXRequestSuccess(happyPathSyncOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFXObj = OFXManager.ofxResponseToJava(responseOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(QBOFX.DD_MODES.TERMINATED, responseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIDDSTATUS().getIDDMODE());
            assertNull(responseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testAssistedCompanyTerminated() {
        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            DataLoadServices.addTaxService(company);
            DataLoadServices.activateTaxService(company);

            // Test that ACTIVE or TERMINATED returned accordingly
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            String nextTokenStr = (company.getCurrentToken() + 1) + "";
            assertSuccess(PayrollServices.companyManager.terminateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax));
            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            Application.save(company);
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathSyncOfxObj = ofxDataloader.loadHappyPathSyncRequest(QBOFX.EMPTY_STR);
            happyPathSyncOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(nextTokenStr);
            String responseOFX = QBDTTestHelper.processOFXRequestSuccess(happyPathSyncOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFXObj = OFXManager.ofxResponseToJava(responseOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(QBOFX.DD_MODES.TERMINATED, responseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIDDSTATUS().getIDDMODE());
            assertEquals(QBOFX.DD_MODES.TERMINATED, responseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE());

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testAS400OFXCOINFOMOD() {

        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            // Test that ACTIVE or TERMINATED returned accordingly
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            coDTO.setFein("111111111");
            ProcessResult<Company> companyPR = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), coDTO);
            assertTrue(companyPR.isSuccess());
            PayrollServices.commitUnitOfWork();

            OFXDataloader ofxDataloader = new OFXDataloader();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX happyPathSyncOfxObj = ofxDataloader.loadHappyPathSyncRequest();
            String responseOFX = QBDTTestHelper.processOFXRequestSuccess(happyPathSyncOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFXObj = OFXManager.ofxResponseToJava(responseOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(responseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            assertNotNull(responseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    @Ignore("This must be a migration test... I can't find an event creation with type AS400EventOFX anywhere but this test file")
    public void testAS400OFXFeeEvent() {
        try {
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            long tokenBeforeEventAdded = company.getCurrentToken();

            com.intuit.sbd.payroll.psp.common.ofx.response.ObjectFactory objFactory = new com.intuit.sbd.payroll.psp.common.ofx.response.ObjectFactory();
            IPAYROLLTX payrollTxModObj = objFactory.createIPAYROLLTX();
            String strIACCTNAME = "IACCTNAME";
            String strIAMT = "$-2.23";
            String strICLEARED = QBOFX.OFX_YN.Y;
            String strIDTPAYPDEND = "20080808";
            String strIDTTX = "20080808";
            String strIEMPNAME = "Mr. Employee";
            String strIMEMO = "IMEMO";
            String strINAME = "INAME";
            String strIONSERVICE = QBOFX.OFX_YN.Y;
            String strIPAYROLLTXID = "0";
            String strIPAYROLLTXTYPE = QBOFX.PAYROLL_TX_TYPE.PAYROLL_LIAB_CHECK;
            String strIREFNUM = "";
            String strIVOID = QBOFX.OFX_YN.N;

            String strTXLINE1_IACCTNAME = "TXLINE1_IACCTNAME";
            String strTXLINE1_IAMT = "$1.23";
            String strTXLINE1_IISDD = QBOFX.OFX_YN.Y;
            String strTXLINE1_IMEMO = "TXLINE1_IMEMO";

            String strTXLINE2_IAMT = "$1.00";
            String strTXLINE2_IISDD = QBOFX.OFX_YN.Y;
            String strTXLINE2_IMEMO = "TXLINE2_IMEMO";

            payrollTxModObj.setIACCTNAME(strIACCTNAME);
            payrollTxModObj.setIAMT(strIAMT);
            payrollTxModObj.setICLEARED(strICLEARED);
            payrollTxModObj.setIDTPAYPDEND(strIDTPAYPDEND);
            payrollTxModObj.setIDTTX(strIDTTX);
            payrollTxModObj.setIEMPNAME(strIEMPNAME);
            payrollTxModObj.setIMEMO(strIMEMO);
            payrollTxModObj.setINAME(strINAME);
            payrollTxModObj.setIONSERVICE(strIONSERVICE);
            payrollTxModObj.setIPAYROLLTXID(strIPAYROLLTXID);
            payrollTxModObj.setIPAYROLLTXTYPE(strIPAYROLLTXTYPE);
            payrollTxModObj.setIREFNUM(strIREFNUM);
            payrollTxModObj.setIVOID(strIVOID);

            ITXLINE txLine1 = objFactory.createITXLINE();
            txLine1.setIACCTNAME(strTXLINE1_IACCTNAME);
            txLine1.setIAMT(strTXLINE1_IAMT);
            txLine1.setIISDD(strTXLINE1_IISDD);
            txLine1.setIMEMO(strTXLINE1_IMEMO);
            payrollTxModObj.getITXLINE().add(txLine1);

            ITXLINE txLine2 = objFactory.createITXLINE();
            txLine2.setIACCTNAME(null);
            txLine2.setIAMT(strTXLINE2_IAMT);
            txLine2.setIISDD(strTXLINE2_IISDD);
            txLine2.setIMEMO(strTXLINE2_IMEMO);
            payrollTxModObj.getITXLINE().add(txLine2);

            IPAYROLLUPDATEDATA ipayrollupdatedata = new IPAYROLLUPDATEDATA();
            ipayrollupdatedata.getIPAYROLLTXMOD().add(payrollTxModObj);
            String ofxStr = OFXManager.javaToOFX(ipayrollupdatedata, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            ofxStr = ofxStr.replaceAll("<I.PAYROLLUPDATEDATA>\\n", "");
            ofxStr = ofxStr.replaceAll("<\\/I.PAYROLLUPDATEDATA>\\n", "");

            CompanyEventDTO coEvtDTO = new CompanyEventDTO();
            coEvtDTO.setEventTypeCode(EventTypeCode.AS400Event);
            Collection<CompanyEventDetailDTO> coEventDetails = new ArrayList();

            CompanyEventDetailDTO eventDetailOFX = new CompanyEventDetailDTO();
            eventDetailOFX.setEventDetailTypeCode(EventDetailTypeCode.AS400EventOFX);
            eventDetailOFX.setEventDetailValue(ofxStr);
            coEventDetails.add(eventDetailOFX);

            CompanyEventDetailDTO eventDetailType = new CompanyEventDetailDTO();
            eventDetailType.setEventDetailTypeCode(EventDetailTypeCode.AS400EventName);
            eventDetailType.setEventDetailValue("NOT A COINFOMOD");
            coEventDetails.add(eventDetailType);

            coEvtDTO.setEventDetails(coEventDetails);

            ProcessResult<CompanyEvent> coEventPR = PayrollServices.companyManager.addCompanyEvent(company.getSourceSystemCd(), company.getSourceCompanyId(), coEvtDTO);
            assertTrue(coEventPR.isSuccess());

            String nextTransactionIdStr = Integer.valueOf(company.getNextPayrollTransactionId()) + "";
            CompanyBankAccount coBankAcct = CompanyBankAccount.findCompanyBankAccounts(company).get(0);
            String companyCOA = coBankAcct.getSourceBankAccountName();
            String feeCOA = company.getQuickbooksInfo().getCoaFeeAccountName();

            PayrollServices.commitUnitOfWork();

            String responseOFX = QBDTTestHelper.processOFXSyncRequestHappyPath((tokenBeforeEventAdded) + "");

            com.intuit.sbd.payroll.psp.common.ofx.response.OFX reponseOFX = OFXManager.ofxResponseToJava(responseOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(1, reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().size());
            assertEquals(reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            List<IPAYROLLTX> payrollTxModList = reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD();
            assertEquals(1, payrollTxModList.size());
            IPAYROLLTX payrollTxMod = payrollTxModList.get(0);

            assertEquals(companyCOA, payrollTxMod.getIACCTNAME());
            assertEquals(strIAMT, payrollTxMod.getIAMT());
            assertEquals(strICLEARED, payrollTxMod.getICLEARED());
            assertEquals(strIDTPAYPDEND, payrollTxMod.getIDTPAYPDEND());
            assertEquals(strIDTTX, payrollTxMod.getIDTTX());
            assertEquals(strIEMPNAME, payrollTxMod.getIEMPNAME());
            assertEquals(strIMEMO, payrollTxMod.getIMEMO());
            assertEquals(strINAME, payrollTxMod.getINAME());
            assertEquals(strIONSERVICE, payrollTxMod.getIONSERVICE());
            assertEquals(nextTransactionIdStr, payrollTxMod.getIPAYROLLTXID());
            assertEquals(strIPAYROLLTXTYPE, payrollTxMod.getIPAYROLLTXTYPE());
            assertEquals(strIREFNUM, payrollTxMod.getIREFNUM());
            assertEquals(strIVOID, payrollTxMod.getIVOID());

            List<ITXLINE> txLineList = payrollTxMod.getITXLINE();
            assertEquals(2, txLineList.size());
            ITXLINE txLine1Response = txLineList.get(0);
            assertEquals(feeCOA, txLine1Response.getIACCTNAME());
            assertEquals(strTXLINE1_IAMT, txLine1Response.getIAMT());
            assertEquals(strTXLINE1_IISDD, txLine1Response.getIISDD());
            assertEquals(strTXLINE1_IMEMO, txLine1Response.getIMEMO());

            ITXLINE txLine2Response = txLineList.get(1);
            assertEquals("Account Name", null, txLine2Response.getIACCTNAME());
            assertEquals(strTXLINE2_IAMT, txLine2Response.getIAMT());
            assertEquals(strTXLINE2_IISDD, txLine2Response.getIISDD());
            assertEquals(strTXLINE2_IMEMO, txLine2Response.getIMEMO());

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }


    @Test
    @Ignore("This must be a migration test... I can't find an event creation with type AS400EventOFX anywhere but this test file")
    // Test actual OFX that Satish sent me that will come from AS400 in event.
    public void testOFXTextSample() {
        try {
            String ofxStr = "<I.PAYROLLTXMOD>\n" +
                    "<I.PAYROLLTXID>3\n" +
                    "<I.NAME>Quickbooks Payroll Service\n" +
                    "<I.ACCTNAME>PAYROLL ACCT\n" +
                    "<I.AMT>$-0.48\n" +
                    "<I.MEMO>Created by Payroll Service on 10/08/2008\n" +
                    "<I.CLEARED>0\n" +
                    "<I.DTTX>20081008\n" +
                    "<I.REFNUM>^@~*\n" +
                    "<I.PAYROLLTXTYPE>LIABCHK\n" +
                    "<I.DTPAYPDEND>20081008\n" +
                    "<I.VOID>N\n" +
                    "<I.ONSERVICE>Y\n" +
                    "<I.TXLINE>\n" +
                    "<I.ACCTNAME>Payroll Expenses\n" +
                    "<I.AMT>$0.48\n" +
                    "<I.MEMO>Enrollment fee\n" +
                    "</I.TXLINE>\n" +
                    "</I.PAYROLLTXMOD>\n";

            String strINAME = "Quickbooks Payroll Service";
            String strIAMT = "$-0.48";
            String strIMEMO = "Created by Payroll Service on 10/08/2008";
            String strICLEARED = "0";
            String strIDTTX = "20081008";
            String strIREFNUM = ""; // ^@~* = EMPTY
            String strIPAYROLLTXTYPE = "LIABCHK";
            String strIDTPAYPDEND = "20081008";
            String strIVOID = "N";
            String strIONSERVICE = "Y";

            String strITXLINE_ACCTNAME = "Payroll Expenses";
            String strITXLINE_AMT = "$0.48";
            String strITXLINE_MEMO = "Enrollment fee";

            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            long tokenBeforeEventAdded = company.getCurrentToken();

            CompanyEventDTO coEvtDTO = new CompanyEventDTO();
            coEvtDTO.setEventTypeCode(EventTypeCode.AS400Event);
            Collection<CompanyEventDetailDTO> coEventDetails = new ArrayList();

            CompanyEventDetailDTO eventDetailOFX = new CompanyEventDetailDTO();
            eventDetailOFX.setEventDetailTypeCode(EventDetailTypeCode.AS400EventOFX);
            eventDetailOFX.setEventDetailValue(ofxStr);
            coEventDetails.add(eventDetailOFX);

            CompanyEventDetailDTO eventDetailType = new CompanyEventDetailDTO();
            eventDetailType.setEventDetailTypeCode(EventDetailTypeCode.AS400EventName);
            eventDetailType.setEventDetailValue("NOT A COINFOMOD");
            coEventDetails.add(eventDetailType);

            coEvtDTO.setEventDetails(coEventDetails);

            ProcessResult<CompanyEvent> coEventPR = PayrollServices.companyManager.addCompanyEvent(company.getSourceSystemCd(), company.getSourceCompanyId(), coEvtDTO);
            assertTrue(coEventPR.isSuccess());

            String nextTransactionIdStr = Integer.valueOf(company.getNextPayrollTransactionId()) + "";
            CompanyBankAccount coBankAcct = CompanyBankAccount.findCompanyBankAccounts(company).get(0);
            String companyCOA = coBankAcct.getSourceBankAccountName();
            String feeCOA = company.getQuickbooksInfo().getCoaFeeAccountName();

            PayrollServices.commitUnitOfWork();

            String responseOFX = QBDTTestHelper.processOFXSyncRequestHappyPath((tokenBeforeEventAdded) + "");

            com.intuit.sbd.payroll.psp.common.ofx.response.OFX reponseOFX = OFXManager.ofxResponseToJava(responseOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(1, reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().size());
            assertEquals(reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            List<IPAYROLLTX> payrollTxModList = reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD();
            assertEquals(1, payrollTxModList.size());
            IPAYROLLTX payrollTxMod = payrollTxModList.get(0);

            assertEquals(companyCOA, payrollTxMod.getIACCTNAME());
            assertEquals(strIAMT, payrollTxMod.getIAMT());
            assertEquals(strICLEARED, payrollTxMod.getICLEARED());
            assertEquals(strIDTPAYPDEND, payrollTxMod.getIDTPAYPDEND());
            assertEquals(strIDTTX, payrollTxMod.getIDTTX());
            assertEquals(strIMEMO, payrollTxMod.getIMEMO());
            assertEquals(strINAME, payrollTxMod.getINAME());
            assertEquals(strIONSERVICE, payrollTxMod.getIONSERVICE());
            assertEquals(nextTransactionIdStr, payrollTxMod.getIPAYROLLTXID());
            assertEquals(strIPAYROLLTXTYPE, payrollTxMod.getIPAYROLLTXTYPE());
            assertEquals(strIREFNUM, payrollTxMod.getIREFNUM());
            assertEquals(strIVOID, payrollTxMod.getIVOID());

            List<ITXLINE> txLineList = payrollTxMod.getITXLINE();
            assertEquals(1, txLineList.size());
            ITXLINE txLine1Response = txLineList.get(0);
            assertEquals(feeCOA, txLine1Response.getIACCTNAME());
            assertEquals(strITXLINE_ACCTNAME, txLine1Response.getIACCTNAME());
            assertEquals(strITXLINE_AMT, txLine1Response.getIAMT());
            assertEquals(strITXLINE_MEMO, txLine1Response.getIMEMO());


        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testSyncTokenTooHigh() {
        try {
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            OFXDataloader ofxDataloader = new OFXDataloader();
            String currToken = company.getCurrentToken() + "";
            String highToken = (company.getCurrentToken() + 10) + "";
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX highTokenOfxObj = ofxDataloader.loadHappyPathSyncRequest(highToken);
            QBDTTestHelper.processOFXRequestSignOnError(highTokenOfxObj, ErrorMessages.ClientOutOfSyncMessage(highToken, currToken));
            QBDTTestHelper.verifyEventExists(EventTypeCode.PayrollRejected, 1);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testMultipleEventsReturned() {
        try {
            // Process a sync request to handle the bank events.
            String origSyncResponseStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX origSyncResponseOFX = OFXManager.ofxResponseToJava(origSyncResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            // Offload Payroll
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX requestOFX = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            QBDTTestHelper.offloadCompanyPayroll(requestOFX);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);

            CompanyBankAccount origCoBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(company);

            long origCoToken = company.getCurrentToken();

            CompanyDTO coDTO = PayrollServices.dtoFactory.create(company);
            String newLegalName = "New Legal Name";
            coDTO.setLegalName(newLegalName);
            PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), coDTO);
            PayrollServices.commitUnitOfWork();

            setupNSFFee();

            String responseOFX = QBDTTestHelper.processOFXSyncRequestDataRecovery(origCoToken + "");
            System.out.println(responseOFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX reponseOFX = OFXManager.ofxResponseToJava(responseOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);
            List<IPAYROLLTX> txModList = reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD();
            assertEquals(2, txModList.size());

            assertNotNull(reponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getICOINFOMOD());

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testNewBankAccountWithSameIDNoDebits() {
        try {
            // Process a sync request to handle the bank events.
            String origSyncResponseStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX origSyncResponseOFX = OFXManager.ofxResponseToJava(origSyncResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String origTokenResponse = origSyncResponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN();

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            CompanyBankAccount origCoBankAcct = CompanyBankAccount.findCompanyBankAccounts(company).get(0);
            PayrollServices.commitUnitOfWork();

            // Add new bank account
            CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
            BankAccountDTO bankAcctDTO = new BankAccountDTO();
            bankAcctDTO.setAccountNumber("1212121212");
            bankAcctDTO.setAccountType(BankAccountType.Checking);
            bankAcctDTO.setBankName("My Bank Name");
            bankAcctDTO.setRoutingNumber("123123123");

            companyBankAccountDTO.setBankAccountDTO(bankAcctDTO);
            companyBankAccountDTO.setCompanyBankAccountID(origCoBankAcct.getSourceBankAccountId());
            companyBankAccountDTO.setSourceBankAccountName(origCoBankAcct.getSourceBankAccountName());

            PayrollServices.beginUnitOfWork();
            ProcessResult<CompanyBankAccount> coBankAcctPR = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBDT, OFXDataloader.companyPSID, companyBankAccountDTO, false, true, false);
            PayrollServices.commitUnitOfWork();
            assertTrue(coBankAcctPR.isSuccess());

            // Offload
            SpcfCalendar offloadDate = PSPDate.getPSPTime();
            SpcfCalendar newCalendar = PSPDate.getPSPTime();
            CalendarUtils.addBusinessDays(offloadDate, 2);

            QBDTTestHelper.runOffload(QBOFX.getDate("yyyyMMdd", new Date(offloadDate.getTimeInMilliseconds())));

            // Sync
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(newCalendar);
            PayrollServices.commitUnitOfWork();
            String syncResponseStr = QBDTTestHelper.processOFXSyncRequestHappyPath(origTokenResponse);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponseOFX = OFXManager.ofxResponseToJava(syncResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            System.out.println(syncResponseStr);
            assertNull(syncResponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA());

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testNewBankAccountRedebits() {
        try {
            // Process a sync request to handle the bank events.
            String origSyncResponseStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX origSyncResponseOFX = OFXManager.ofxResponseToJava(origSyncResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String origTokenResponse = origSyncResponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN();

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            CompanyBankAccount origCoBankAcct = CompanyBankAccount.findCompanyBankAccounts(company).get(0);
            PayrollServices.commitUnitOfWork();

            // Add new bank account
            CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
            BankAccountDTO bankAcctDTO = new BankAccountDTO();
            bankAcctDTO.setAccountNumber("1212121212");
            bankAcctDTO.setAccountType(BankAccountType.Checking);
            bankAcctDTO.setBankName("My Bank Name");
            bankAcctDTO.setRoutingNumber("123123123");

            companyBankAccountDTO.setBankAccountDTO(bankAcctDTO);
            companyBankAccountDTO.setCompanyBankAccountID(origCoBankAcct.getSourceBankAccountId());
            companyBankAccountDTO.setSourceBankAccountName(origCoBankAcct.getSourceBankAccountName());

            PayrollServices.beginUnitOfWork();
            ProcessResult<CompanyBankAccount> coBankAcctPR = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBDT, OFXDataloader.companyPSID, companyBankAccountDTO, true, true, false);
            PayrollServices.commitUnitOfWork();
            assertTrue(coBankAcctPR.isSuccess());

            // Offload
            SpcfCalendar offloadDate = PSPDate.getPSPTime();
            CalendarUtils.addBusinessDays(offloadDate, 3);

            String dateStr = QBOFX.getDate("yyyyMMdd", new Date(offloadDate.getTimeInMilliseconds()));
            QBDTTestHelper.runOffload(dateStr);

            CalendarUtils.addBusinessDays(offloadDate, 1);

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(offloadDate);
            PayrollServices.commitUnitOfWork();

            // Sync - Should not get debits yet
            String syncResponseStr = QBDTTestHelper.processOFXSyncRequestHappyPath(origTokenResponse);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponseOFX = OFXManager.ofxResponseToJava(syncResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertNull(syncResponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA());

            // Run payroll, ensure rejected
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj = ofxDataloader.loadHappyPathOFX();

            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.BankAccountNotActiveError());
            // Verify Debits
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(OFXDataloader.companyPSID, SourceSystemCode.QBDT);
            CompanyBankAccount coBankAcct = CompanyBankAccount.findCompanyBankAccount(company, BankAccountStatus.PendingVerification);
            DomainEntitySet<FinancialTransaction> verificationTransactions = coBankAcct.getVerificationTransactions();
            ProcessResult<CompanyBankAccount> coBAPR = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), coBankAcct.getSourceBankAccountId(), verificationTransactions.get(0).getFinancialTransactionAmount(), verificationTransactions.get(1).getFinancialTransactionAmount(), false);
            assertTrue(coBAPR.isSuccess());
            PayrollServices.commitUnitOfWork();

            // Run sync, ensure debits returned
            syncResponseStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(origTokenResponse);
            syncResponseOFX = OFXManager.ofxResponseToJava(syncResponseStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertEquals(syncResponseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE(), QBOFX.OFX_YESNO.NO);

            // Run payroll, should be ok
//            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void testRedebitFees() {
        try {
            CompanyQB1DataLoader c1dl = new CompanyQB1DataLoader();
            PayrollServices.beginUnitOfWork();
            Company pspCompany = Company.findCompany(c1dl.COMPANY_PSID, SourceSystemCode.QBDT);
            c1dl.setCompany(pspCompany);
            PayrollServices.commitUnitOfWork();

            // Sync back bank transactions
            QBDTTestHelper.processOFXSyncRequestHappyPath();

            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX requestOFX = ofxDataloader.loadHappyPathOFX();
            String responseOFXStr = QBDTTestHelper.processOFXRequestPayrollSubmitSuccess(requestOFX);
            OFXManager.ofxResponseToJava(responseOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            String payrollRunID = PayrollRun.findFirstCompanyPayrollRun(pspCompany).getSourcePayRunId();
            String paycheckId = requestOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIPAYCHKID();
            PayrollServices.beginUnitOfWork();
            Paycheck paycheck = Paycheck.findPaycheck(pspCompany, paycheckId);
            String sourcePaycheckSplitId = paycheck.getPaycheckSplitCollection().iterator().next().getSourceDdTxnId();
            PayrollServices.commitUnitOfWork();

//        Application.beginUnitOfWork();
//        loadDataHappyPath2Day();
//        Application.commitUnitOfWork();

            //Offload the payroll
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20070907000000");
            Application.commitUnitOfWork();

            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            pspCompany = Company.findCompany(pspCompany.getSourceCompanyId(), pspCompany.getSourceSystemCd());
            long tokenAfterPayrollOffload = pspCompany.getCurrentToken();


            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20070928000000");
            PayrollServices.commitUnitOfWork();
            //Reverse single ee txn from payroll
            c1dl.reverseSingleTransactionInPayrollChargeFee(payrollRunID, sourcePaycheckSplitId);

            //Offload the reversal & fee
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20070928000000");
            Application.commitUnitOfWork();

            OffloadACHTransactions offloader2 = new OffloadACHTransactions();
            offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            CreateTransactionOffloadedEvents createTransactionOffloadedEvents = new CreateTransactionOffloadedEvents();
            createTransactionOffloadedEvents.createTransactionOffloadedEvents();

            //Return the fee
            Application.beginUnitOfWork();
            FinancialTransaction reversalFee = null;
            DomainEntitySet<FinancialTransaction> finTxnsToReturn = new DomainEntitySet<FinancialTransaction>();
            DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBDT, c1dl.getCompany1().getCompanyId(),
                                               TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
            for (FinancialTransaction currTxn : c1FinTxns) {
                if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                    OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                    if (OfferingServiceChargeType.ReversalFee == osc) {
                        reversalFee = currTxn;
                    }
                }
            }

            finTxnsToReturn.add(reversalFee);

            PSPDate.setPSPTime("20070910000000");
            DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(finTxnsToReturn, "R01",
                                                                                                           "This is an NSF description");
            Application.commitUnitOfWork();

            assertNotNull("Found reversal fee", reversalFee);
            assertEquals("Number of returns", 1, returnList.size());

            //Execute the return handlers
            for (TransactionReturn currRet : returnList) {
                Application.beginUnitOfWork();
                currRet = Application.findById(TransactionReturn.class, currRet.getId());
                TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
                returnHandler.execute(currRet);
                Application.commitUnitOfWork();
            }

            FinancialTransaction returnedFeeFT = null;
            Application.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> c1FinTxns2 = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBDT, c1dl.COMPANY_PSID,
                                               TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Returned);
            for (FinancialTransaction currTxn : c1FinTxns2) {
                if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                    OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                    if (OfferingServiceChargeType.ReversalFee == osc) {
                        returnedFeeFT = currTxn;
                    }
                }
            }
            Application.commitUnitOfWork();

            Application.beginUnitOfWork();
            PSPDate.setPSPTime("20071001000000");
            RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
            redebitDTO.setAmount(returnedFeeFT.getFinancialTransactionAmount());
            redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
            redebitDTO.setOriginalFinancialTxId(returnedFeeFT.getId().toString());

            ArrayList<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
            redebitImpoundDTOs.add(redebitDTO);
            ProcessResult redebitProcess = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                    SourceSystemCode.QBDT,
                    c1dl.COMPANY_PSID, redebitImpoundDTOs);
            Application.commitUnitOfWork();

            assertSuccess(redebitProcess);

            //Offload the fee redebit
            OffloadACHTransactions offloader22 = new OffloadACHTransactions();
            offloader22.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            createTransactionOffloadedEvents = new CreateTransactionOffloadedEvents();
            createTransactionOffloadedEvents.createTransactionOffloadedEvents();

            //Return the fee redebit
            Application.beginUnitOfWork();

            DomainEntitySet<FinancialTransaction> allFinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBDT, c1dl.COMPANY_PSID,
                                               TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Executed);

            PSPDate.setPSPTime("20071002000000");
            DomainEntitySet<TransactionReturn> returnList2 = ACHReturnsDataLoader.persistTransactionReturns(allFinTxns, "R01", "Test NSF");
            Application.commitUnitOfWork();

            assertEquals("Found 1 reversal fee redebit", 1, allFinTxns.size());
            assertEquals("Number of returns", 1, returnList.size());

            //Execute the return handlers
            for (TransactionReturn currRet : returnList2) {
                Application.beginUnitOfWork();
                currRet = Application.findById(TransactionReturn.class, currRet.getId());
                TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
                returnHandler.execute(currRet);
                Application.commitUnitOfWork();
            }

            responseOFXStr = QBDTTestHelper.processOFXSyncRequestDataRecovery(tokenAfterPayrollOffload + "");
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFX = OFXManager.ofxResponseToJava(responseOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

            // There should only be one transaction in the response.
            // The redebit should have been skipped.
            assertEquals(2, responseOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().size());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }
    }
}
