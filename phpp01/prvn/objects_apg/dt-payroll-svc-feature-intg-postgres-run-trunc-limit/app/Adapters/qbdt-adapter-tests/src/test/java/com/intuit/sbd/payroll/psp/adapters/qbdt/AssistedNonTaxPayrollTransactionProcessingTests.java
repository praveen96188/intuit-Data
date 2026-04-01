package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 11/15/11
 * Time: 7:06 PM
 */
@SuppressWarnings("deprecation")
public class AssistedNonTaxPayrollTransactionProcessingTests {
    @BeforeClass
    public static void beforeClass() {
    }

    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFXRequestGenerator.reset();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(null);
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(null);
    }

    @Test
    public void testPriorPaymentsAndRefunds_TaxAndNonTax() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(null);
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(null);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, true, false, false);
        for (IPAYROLLTX ipayrolltx : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX()) {
            ipayrolltx.getITXLINE().clear();
            int lineCount = 0;
            int transactionTotal = 0;
            for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
                int amount = lineCount++;
                transactionTotal = amount;
                ipayrolltx.getITXLINE().add(OFXRequestGenerator.generateTransactionLine(null,
                                                                                        new SpcfMoney(SpcfDecimal.createInstance(amount)),
                                                                                        "Class" + lineCount,
                                                                                        false,
                                                                                        "Memo" + lineCount,
                                                                                        ipitem.getIPITEMID(),
                                                                                        null,
                                                                                        null));
                lineCount++;
            }
            ipayrolltx.setIAMT("$-" + transactionTotal + ".00");
        }


        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.HPDE));
        // 5 payment submissions * (2 IRS Templates + 2 CA Templates)
        assertEquals(20, moneyMovementTransactions.size());
        OFXAssert.assertPayrollTransactions(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company, true, true);
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1));
        assertEquals("tx count", ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().size(),
                     response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().size());
        OFXAssert.assertPayrollTransactions(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(),
                                            response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD());
    }

    @Test
    public void testPriorPaymentsAndRefunds_NonTax() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, true, false, false);
        for (IPAYROLLTX ipayrolltx : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX()) {
            ipayrolltx.getITXLINE().clear();
            int lineCount = 0;
            int transactionTotal = 0;
            for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
                if(ipitem.getITAXITEM() == null) {
                    int amount = lineCount++;
                    transactionTotal = amount;
                    ipayrolltx.getITXLINE().add(OFXRequestGenerator.generateTransactionLine(null,
                                                                                            new SpcfMoney(SpcfDecimal.createInstance(amount)),
                                                                                            "Class" + lineCount,
                                                                                            false,
                                                                                            "Memo" + lineCount,
                                                                                            ipitem.getIPITEMID(),
                                                                                            new SpcfMoney(SpcfDecimal.createInstance(lineCount)),
                                                                                            new SpcfMoney(SpcfDecimal.createInstance(lineCount))));
                    lineCount++;
                }
            }
            ipayrolltx.setIAMT("$-" + transactionTotal + ".00");
        }


        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        // no tax lines
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.HPDE));
        assertEquals(0, moneyMovementTransactions.size());
        OFXAssert.assertPayrollTransactions(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustmentMods_IncreaseAmountsWithNonTaxItems() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = QBDTTestHelper.submitBalanceFile(company, false);

        // generate 1 employee liability adjustment
        List<IPAYROLLTX> liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        int linesTotal = 0;
        for (IPITEM payrollItem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            linesTotal += Integer.parseInt(payrollItem.getIPITEMID());
            itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                                                                     new SpcfMoney(SpcfDecimal.createInstance(QBOFX.mapOFXStringToBoolean(payrollItem.getIISEMP()) ? "-" + payrollItem.getIPITEMID() : payrollItem.getIPITEMID())),
                                                                     "Class" + payrollItem.getIPITEMID(),
                                                                     false,
                                                                     "Memo" + payrollItem.getIPITEMID(),
                                                                     payrollItem.getIPITEMID(),
                                                                     new SpcfMoney(SpcfDecimal.createInstance(payrollItem.getIPITEMID() + 1)),
                                                                     new SpcfMoney(SpcfDecimal.createInstance(payrollItem.getIPITEMID() + 2))));
        }
        String empId = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0).getIEMPID();
        liabilityAdjustments.add(OFXRequestGenerator.generatePayrollTransaction("Account" + empId,
                                                                                new SpcfMoney(linesTotal + ""),
                                                                                "1",
                                                                                new Date("01/31/2011"),
                                                                                new Date("01/31/2011"),
                                                                                empId,
                                                                                "Memo " + empId,
                                                                                "Agency" + empId,
                                                                                true,
                                                                                QBOFX.OFXPayrollTransactionTransactionType.LIABADJ,
                                                                                "Ref" + empId,
                                                                                false,
                                                                                itxlines));
        OFX adjustmentOfx = new OFX();
        adjustmentOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 liabilityAdjustments,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(adjustmentOfx, true);
        assertNotNull("Response", response);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull("Payroll response", ipayrollrs);

        // the payroll and the liability adjustments all had the same paycheck date so they should be on the same liability check
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTX().size());
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());

        SpcfDecimal total = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() == null && itxline.getIPITEMID() != null) {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID());
                total = total.add(amount);
                assertEquals("amount", "$" + amount, itxline.getIAMT());
            } else {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                total = total.add(amount);
            }
        }

        assertEquals("amount", "$-" + total, ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company, false, true);
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);

        response = QBDTTestHelper.submitQBDTRequest(OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1));
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX> ipayrolltxes = new ArrayList<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX>();
        for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx1 : response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD()) {
            if(!ipayrolltx1.getIPAYROLLTXTYPE().equals("LIABCHK")) {
                ipayrolltxes.add(ipayrolltx1);
            }
        }
        assertEquals("tx count", adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().size(), ipayrolltxes.size());
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), ipayrolltxes);

        // adjust all the transaction lines by 1
        liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        IPAYROLLTX liabilityAdjustment = adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().get(0);
        linesTotal = 0;
        for (ITXLINE itxline : liabilityAdjustment.getITXLINE()) {
            int amount = Integer.parseInt(itxline.getIPITEMID()) + 1;
            linesTotal += amount;
            itxline.setIAMT("$" + (itxline.getIAMT().contains("-") ? "-" + amount : amount) + ".00");
        }
        liabilityAdjustment.setIAMT("$" + linesTotal + ".00");
        liabilityAdjustments.add(liabilityAdjustment);
        adjustmentOfx = new OFX();
        adjustmentOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   liabilityAdjustments,
                                                                   null,
                                                                   null);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        response = QBDTTestHelper.submitQBDTRequest(adjustmentOfx, true);
        assertNotNull("Response", response);
        ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull("Payroll response", ipayrollrs);

        // there should be an additional liability check for the $7 difference
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTX().size());
        ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);
        for (PayrollRun payrollRun : PayrollRun.findPayrollRuns(company)) {
            PayrollServices.beginUnitOfWork();
            payrollRun = Application.findById(PayrollRun.class, payrollRun.getId());
            LiabilityAdjustment domainLiabilityAdjustment = payrollRun.getLiabilityAdjustmentCollection().get(0);
            String sourceId = domainLiabilityAdjustment.getCompanyAdjustmentSubmission().getSourceId();
            PayrollServices.rollbackUnitOfWork();
            if(sourceId == null) {
                OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, payrollRun);
            }
        }

        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            assertNotNull("payroll item id", itxline.getIPITEMID());
            assertEquals("amount", "$1.00", itxline.getIAMT());
        }

        assertEquals("amount", "$-11.00", ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company, false, true);
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);
        assertEquals(2, syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().size());

        // stop adjustment with source id 1
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = Application.find(CompanyAdjustmentSubmission.class, CompanyAdjustmentSubmission.SourceId().equalTo("1").And(CompanyAdjustmentSubmission.OriginalSubmission().isNull()));
        assertEquals(1, companyAdjustmentSubmissions.size());
        companyAdjustmentSubmissions.get(0).getQbdtTransactionInfo().setToken(-1);
        PayrollServices.commitUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);
        assertEquals(1, syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().size());
    }

    @Test
    public void testUpdateLiabilityCheckAsPriorPayment() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateBalanceFile(company.getSourceCompanyId(), false, false, false, false);

        List<IPAYROLLTX> liabilityChecks = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        for (IPITEM payrollItem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                                                                     new SpcfMoney(SpcfDecimal.createInstance(payrollItem.getIPITEMID())),
                                                                     "Class" + payrollItem.getIPITEMID(),
                                                                     false,
                                                                     "Memo" + payrollItem.getIPITEMID(),
                                                                     payrollItem.getIPITEMID(),
                                                                     null,
                                                                     null));
        }
        liabilityChecks.add(OFXRequestGenerator.generatePayrollTransaction("Account" + 1,
                                                                           new SpcfMoney("351.00"),
                                                                           "" + 1,
                                                                           new Date("01/31/2011"),
                                                                           new Date("01/30/2011"),
                                                                           null,
                                                                           "Memo " + 1,
                                                                           "Agency" + 1,
                                                                           true,
                                                                           QBOFX.OFXPayrollTransactionTransactionType.LIABCHK,
                                                                           "Ref" + 1,
                                                                           false,
                                                                           itxlines));
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().addAll(liabilityChecks);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<LiabilityCheck> domainLiabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company));
        assertEquals("liability checks", 0, domainLiabilityChecks.size());
        OFXAssert.assertPayrollTransactions(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company, true, true);
        PayrollServices.rollbackUnitOfWork();

        for (IPAYROLLTX liabilityCheck : liabilityChecks) {
            for (ITXLINE itxline : liabilityCheck.getITXLINE()) {
                itxline.setIACCTNAME(itxline.getIACCTNAME() + "changed");
                itxline.setIMEMO(itxline.getIMEMO() + "changed");
            }
            ITXLINE itxline = new ITXLINE();
            itxline.setIACCTNAME("Payroll Expenses");
            itxline.setIMEMO("extra line");
            itxline.setIAMT("$10.00");
            liabilityCheck.getITXLINE().add(itxline);
            liabilityCheck.setIAMT("$361.00");
        }

        company = DataLoadServices.refreshCompany(company);
        ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 liabilityChecks,
                                                                                 null,
                                                                                 null);
        ofx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        domainLiabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company));
        assertEquals("liability checks", 0, domainLiabilityChecks.size());
        OFXAssert.assertPayrollTransactions(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company, true, true);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDDReturn() throws Exception {
        String psid = "123456789";

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid, false);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        List<IPAYROLLTX> ddreturns = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        for (IPITEM payrollItem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            if(payrollItem.getISPECIALTYPE() != null && payrollItem.getISPECIALTYPE().equals(QbdtSpecialType.DIRDEP.toString())) {
                itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                                                                         new SpcfMoney("-200.00"),
                                                                         null,
                                                                         true,
                                                                         "Unable to Locate Account",
                                                                         null,
                                                                         null,
                                                                         null));
            }
        }
        ddreturns.add(OFXRequestGenerator.generatePayrollTransaction("Some Account",
                                                                     new SpcfMoney("200.00"),
                                                                     "0",
                                                                     new Date("01/31/2011"),
                                                                     new Date("01/30/2011"),
                                                                     null,
                                                                     null,
                                                                     "QB PS",
                                                                     true,
                                                                     QBOFX.OFXPayrollTransactionTransactionType.DDRETURN,
                                                                     "",
                                                                     false,
                                                                     itxlines));
        ddreturns.get(0).setIEMPNAME("Emp name, why couldn't we use Id");
        OFX adjustmentOfx = new OFX();
        adjustmentOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 ddreturns,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(adjustmentOfx, true);
        assertNotNull("Response", response);
        assertNull(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS());

        PayrollServices.beginUnitOfWork();
        OFXAssert.assertPayrollTransactions(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company, true, true);
        PayrollServices.rollbackUnitOfWork();
    }

}
