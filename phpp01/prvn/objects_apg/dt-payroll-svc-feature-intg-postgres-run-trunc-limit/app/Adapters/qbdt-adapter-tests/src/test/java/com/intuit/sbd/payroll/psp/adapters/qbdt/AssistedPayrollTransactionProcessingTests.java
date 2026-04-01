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
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.easymock.IMocksControl;
import org.junit.*;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.easymock.EasyMock.createStrictControl;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 1, 2010
 * Time: 4:48:15 PM
 */
@SuppressWarnings("deprecation")
public class AssistedPayrollTransactionProcessingTests {
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
    public void testSkipAS400Processing() throws Exception {
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.QBDT_FORWARD_REQUESTS_TO_AS400, "false");
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, false);

        IMocksControl ctrl = createStrictControl();
        ctrl.replay();

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx, true);
        ctrl.verify();

        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.QBDT_FORWARD_REQUESTS_TO_AS400, "true");
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testUpdateOfferingAndSubmitBALF() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.updateOffering(company, OfferingCode.COSTCO49, "COSTCO-49");
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setPriceType("Costco");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertOne(company.getCompanyOfferingCollection().find(CompanyOffering.Offering().OfferingCode().equalTo(OfferingCode.COSTCO49)));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPriorPaymentsAddedWithBalanceFile() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,  MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.HPDE));
        // 5 payment submissions * (2 IRS Templates + 2 CA Templates)
        assertEquals(20, moneyMovementTransactions.size());
        OFXAssert.assertPayrollTransactions(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testPriorPaymentsAndRefundsWithBalanceFile() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, true, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,  MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.HPDE));
        // 5 payment submissions * (2 IRS Templates + 2 CA Templates)
        assertEquals(20, moneyMovementTransactions.size());
        OFXAssert.assertPayrollTransactions(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testPriorPaymentsAndRefundsAfterBalanceFile() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, true, false);
        List<IPAYROLLTX> ipayrolltxs = new ArrayList<IPAYROLLTX>(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX());
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().clear();

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

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
                                                                                 ipayrolltxs,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        ofx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,  MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.PostBalfHPDE));
        // 5 payment submissions * (2 IRS Templates + 2 CA Templates)
        assertEquals(20, moneyMovementTransactions.size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testDeletePayrollTransactions() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PriorPaymentSubmission> priorPaymentSubmissions = Application.find(PriorPaymentSubmission.class);
        assertEquals(5, priorPaymentSubmissions.size());
        OFXAssert.assertPayrollTransactions(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

        ofx = OFXRequestGenerator.generatePayrollTransactionDeletes(ofx);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        priorPaymentSubmissions = Application.find(PriorPaymentSubmission.class);
        assertEquals(5, priorPaymentSubmissions.size());
        for (PriorPaymentSubmission priorPaymentSubmission : priorPaymentSubmissions) {
            for (QbdtTransactionInfo qbdtTransactionInfo : priorPaymentSubmission.getQbdtTransactionInfoCollection()) {
                assertTrue(qbdtTransactionInfo.getIsDeleted());
            }
        }
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testUpdateLiabilityCheck() throws Exception {
        String psid = "123456789";

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        OFX liabilityCheckOFX = new OFX();
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
                    new SpcfMoney("-351.00"),
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
        liabilityCheckOFX.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                liabilityChecks,
                null,
                null,
                null);
        liabilityCheckOFX.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTTestHelper.submitQBDTRequestStringResponse(liabilityCheckOFX);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<LiabilityCheck> domainLiabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company));
        assertEquals("liability checks", 1, domainLiabilityChecks.size());
        OFXAssert.assertPayrollTransactions(liabilityCheckOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
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
            liabilityCheck.setIAMT("$-361.00");
        }

        liabilityCheckOFX = new OFX();
        liabilityCheckOFX.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
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
        liabilityCheckOFX.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTTestHelper.submitQBDTRequestStringResponse(liabilityCheckOFX);

        PayrollServices.beginUnitOfWork();
        domainLiabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company));
        assertEquals("liability checks", 1, domainLiabilityChecks.size());
        OFXAssert.assertPayrollTransactions(liabilityCheckOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityCheck_ModBeforeAdd() throws Exception {
        String psid = "123456789";

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        OFX liabilityCheckOFX = new OFX();
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
                    new SpcfMoney("-351.00"),
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
        liabilityCheckOFX.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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
        liabilityCheckOFX.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTTestHelper.submitQBDTRequestStringResponse(liabilityCheckOFX);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<LiabilityCheck> domainLiabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company));
        assertEquals("liability checks", 1, domainLiabilityChecks.size());
        OFXAssert.assertPayrollTransactions(liabilityCheckOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVoidLiabilityCheck() throws Exception {
        String psid = "123456789";

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class);
        PayrollServices.rollbackUnitOfWork();

        OFX liabilityCheckOFX = new OFX();
        List<IPAYROLLTX> liabilityChecks = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        for (IPITEM payrollItem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            if(companyLaws.findEntity(CompanyLaw.SourceId().equalTo(payrollItem.getIPITEMID())) == null) {
                itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                                                                         new SpcfMoney("0.00"),
                                                                         "Class" + payrollItem.getIPITEMID(),
                                                                         false,
                                                                         "Memo" + payrollItem.getIPITEMID(),
                                                                         payrollItem.getIPITEMID(),
                                                                         null,
                                                                         null));
            }
        }
        liabilityChecks.add(OFXRequestGenerator.generatePayrollTransaction("Account" + 1,
                    new SpcfMoney("0.00"),
                    "" + 1,
                    new Date("01/31/2011"),
                    new Date("01/30/2011"),
                    null,
                    "Memo " + 1,
                    "Agency" + 1,
                    true,
                    QBOFX.OFXPayrollTransactionTransactionType.LIABCHK,
                    "Ref" + 1,
                    true,
                    itxlines));
        liabilityCheckOFX.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                liabilityChecks,
                null,
                null,
                null);
        liabilityCheckOFX.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTTestHelper.submitQBDTRequestStringResponse(liabilityCheckOFX);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<LiabilityCheck> domainLiabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company));
        assertEquals("liability checks", 1, domainLiabilityChecks.size());
        OFXAssert.assertPayrollTransactions(liabilityCheckOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

        for (IPAYROLLTX liabilityCheck : liabilityChecks) {
            for (ITXLINE itxline : liabilityCheck.getITXLINE()) {
                itxline.setIAMT("$0.00");
            }
            liabilityCheck.setIAMT("$0.00");
            liabilityCheck.setIVOID("Y");
        }

        liabilityCheckOFX = new OFX();
        liabilityCheckOFX.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
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
        liabilityCheckOFX.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTTestHelper.submitQBDTRequestStringResponse(liabilityCheckOFX);

        PayrollServices.beginUnitOfWork();
        domainLiabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company));
        assertEquals("liability checks", 1, domainLiabilityChecks.size());
        OFXAssert.assertPayrollTransactions(liabilityCheckOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustmentMods_IncreaseAmounts() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = QBDTTestHelper.submitBalanceFile(company, true);

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
                Assert.assertEquals("amount", "$" + amount, itxline.getIAMT());
            } else {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                total = total.add(amount);
            }
        }

        Assert.assertEquals("amount", "$-" + total, ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

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
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustmentMods_DecreaseAmounts() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid, true);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

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

        int total = 0;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            assertNotNull("payroll item id", itxline.getIPITEMID());
            int amount = Integer.parseInt(itxline.getIPITEMID());
            total += amount;
            assertEquals("amount", "$" + amount + ".00", itxline.getIAMT());
        }

        assertEquals("amount", "$-" + total + ".00", ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

        // adjust all the transaction lines by -1
        liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        IPAYROLLTX liabilityAdjustment = adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().get(0);
        linesTotal = 0;
        for (ITXLINE itxline : liabilityAdjustment.getITXLINE()) {
            int amount = Integer.parseInt(itxline.getIPITEMID()) - 1;
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

        // the liability adjustments should be on a liability check
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTXMOD().size());
        ipayrolltx = ipayrollrs.getIPAYROLLTXMOD().get(0);

        total = 0;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            assertNotNull("payroll item id", itxline.getIPITEMID());
            int amount = Integer.parseInt(itxline.getIAMT().replaceAll("\\$", "").replaceAll("\\.00", ""));
            total += amount;
        }

        assertEquals("amount", "$-" + total + ".00", ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustmentMods_DecreaseAmountsNull() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid, true);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

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

        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTX().size());
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());

        int total = 0;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            assertNotNull("payroll item id", itxline.getIPITEMID());
            int amount = Integer.parseInt(itxline.getIPITEMID());
            total += amount;
            assertEquals("amount", "$" + amount + ".00", itxline.getIAMT());
        }

        assertEquals("amount", "$-" + total + ".00", ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

        // adjust all the transaction lines to null
        liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        IPAYROLLTX liabilityAdjustment = adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().get(0);
        for (ITXLINE itxline : liabilityAdjustment.getITXLINE()) {
            itxline.setIAMT("");
        }
        liabilityAdjustment.setIAMT("");
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

        // the liability adjustments should be on a liability check
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTXMOD().size());
        ipayrolltx = ipayrollrs.getIPAYROLLTXMOD().get(0);

        total = 0;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            assertNotNull("payroll item id", itxline.getIPITEMID());
            int amount = Integer.parseInt(itxline.getIAMT().replaceAll("\\$", "").replaceAll("\\.00", ""));
            total += amount;
        }

        assertEquals("amount", "$" + total + ".00", ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();        

        SpcfDecimal adjustmentTotal = SpcfDecimal.createInstance(0);
        DomainEntitySet<LiabilityAdjustment> domainLiabilityAdjustments = Application.find(LiabilityAdjustment.class);
        for (LiabilityAdjustment domainLiabilityAdjustment : domainLiabilityAdjustments) {
            adjustmentTotal = adjustmentTotal.add(domainLiabilityAdjustment.getAmount());
        }

        assertEquals(SpcfMoney.ZERO, adjustmentTotal);

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustmentMods_AddAdditionalLine() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid, true);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // generate 1 employee liability adjustment
        List<IPAYROLLTX> liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        List<ITXLINE> additionalLines = new ArrayList<ITXLINE>();
        int linesTotal = 0;
        for (IPITEM payrollItem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            ITXLINE itxline = OFXRequestGenerator.generateTransactionLine(null,
                                                                          new SpcfMoney(SpcfDecimal.createInstance(QBOFX.mapOFXStringToBoolean(payrollItem.getIISEMP()) ? "-" + payrollItem.getIPITEMID() : payrollItem.getIPITEMID())),
                                                                          "Class" + payrollItem.getIPITEMID(),
                                                                          false,
                                                                          "Memo" + payrollItem.getIPITEMID(),
                                                                          payrollItem.getIPITEMID(),
                                                                          new SpcfMoney(SpcfDecimal.createInstance(payrollItem.getIPITEMID() + 1)),
                                                                          new SpcfMoney(SpcfDecimal.createInstance(payrollItem.getIPITEMID() + 2)));
            if(!payrollItem.getIPITEMID().equals("3")) {
                linesTotal += Integer.parseInt(payrollItem.getIPITEMID());
                itxlines.add(itxline);
            } else {
                additionalLines.add(itxline);
            }
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
        
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTX().size());
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());

        int total = 0;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            assertNotNull("payroll item id", itxline.getIPITEMID());
            int amount = Integer.parseInt(itxline.getIPITEMID());
            total += amount;
            assertEquals("amount", "$" + amount + ".00", itxline.getIAMT());
        }

        assertEquals("amount", "$-" + total + ".00", ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

        // add a line to the transaction
        liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        IPAYROLLTX liabilityAdjustment = adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().get(0);
        liabilityAdjustment.getITXLINE().addAll(additionalLines);
        linesTotal = 0;
        for (ITXLINE itxline : liabilityAdjustment.getITXLINE()) {
            int amount = Integer.parseInt(itxline.getIPITEMID());
            linesTotal += amount;
        }

        ITXLINE line = new ITXLINE();
        line.setIMEMO("This line has no amount");
        line.setIAMT("");
        line.setIACCTNAME("");
        line.setIWB("");
        line.setITAXABLEWAGE("");
        line.setICLASS("");
        line.setIISDD("N");
        liabilityAdjustment.getITXLINE().add(line);

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

        // there should be an additional liability check for the $3 difference
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

        total = 0;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            assertNotNull("payroll item id", itxline.getIPITEMID());
            int amount = Integer.parseInt(itxline.getIPITEMID());
            total += amount;
            assertEquals("amount", "$" + amount + ".00", itxline.getIAMT());
        }

        assertEquals("amount", "$-" + total + ".00", ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustmentMods_RemoveLine() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid, true);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

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

        // the liability adjustments should be on a liability check
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTX().size());
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());

        int total = 0;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            assertNotNull("payroll item id", itxline.getIPITEMID());
            int amount = Integer.parseInt(itxline.getIPITEMID());
            total += amount;
            assertEquals("amount", "$" + amount + ".00", itxline.getIAMT());
        }

        assertEquals("amount", "$-" + total + ".00", ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

        // remove the first line
        liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        IPAYROLLTX liabilityAdjustment = adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().get(0);
        liabilityAdjustment.getITXLINE().remove(0);
        linesTotal = 0;                
        for (ITXLINE itxline : liabilityAdjustment.getITXLINE()) {
            int amount = Integer.parseInt(itxline.getIPITEMID()) - 1;
            linesTotal += amount;
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

        // the liability adjustments should be on a liability check
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTXMOD().size());
        ipayrolltx = ipayrollrs.getIPAYROLLTXMOD().get(0);

        total = 0;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            assertNotNull("payroll item id", itxline.getIPITEMID());
            int amount = Integer.parseInt(itxline.getIAMT().replaceAll("\\$", "").replaceAll("\\.00", ""));
            total += amount;
        }

        assertEquals("amount", "$-" + total + ".00", ipayrolltx.getIAMT());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustment_NegativeCobra() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        // add cobra
        List<IPITEM> cobra = new ArrayList<IPITEM>();
        cobra.add(OFXRequestGenerator.generateCobraPITEM());

        // generate 1 employee liability adjustment for cobra
        List<IPAYROLLTX> liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        int linesTotal = 0;
        for (IPITEM payrollItem : cobra) {
            linesTotal += Integer.parseInt(payrollItem.getIPITEMID());
            itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                    new SpcfMoney(SpcfDecimal.createInstance("-" + payrollItem.getIPITEMID())),
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
                cobra,
                null,
                null,
                liabilityAdjustments,
                null,
                null,
                null);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(adjustmentOfx, true);
        assertNotNull("Response", response);
        assertNull( response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

        QBDTTestHelper.submitPayroll(company, ofx, new Date("02/05/2011"), new Date("02/05/2011"), new Date("02/05/2011"));
    }

    @Test
    public void testLiabilityAdjustment_PositiveCobra() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        // add cobra
        List<IPITEM> cobra = new ArrayList<IPITEM>();
        cobra.add(OFXRequestGenerator.generateCobraPITEM());

        // generate 1 employee liability adjustment for cobra
        List<IPAYROLLTX> liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        int linesTotal = 0;
        for (IPITEM payrollItem : cobra) {
            linesTotal += Integer.parseInt(payrollItem.getIPITEMID());
            itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                    new SpcfMoney(SpcfDecimal.createInstance(payrollItem.getIPITEMID())),
                    "Class" + payrollItem.getIPITEMID(),
                    false,
                    "Memo" + payrollItem.getIPITEMID(),
                    payrollItem.getIPITEMID(),
                    new SpcfMoney(SpcfDecimal.createInstance(payrollItem.getIPITEMID() + 1)),
                    new SpcfMoney(SpcfDecimal.createInstance(payrollItem.getIPITEMID() + 2))));
        }
        String empId = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0).getIEMPID();
        liabilityAdjustments.add(OFXRequestGenerator.generatePayrollTransaction("Account" + empId,
                                                                                new SpcfMoney(-linesTotal + ""),
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
                cobra,
                null,
                null,
                liabilityAdjustments,
                null,
                null,
                null);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(adjustmentOfx, true);
        assertNotNull("Response", response);
        assertNull( response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS());

        // we are removing positive cobra adjustments so the request should not have created any adjustments
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = Application.find(CompanyAdjustmentSubmission.class,
                                                                                                     CompanyAdjustmentSubmission.Company().equalTo(company)
                                                                                                             .And(CompanyAdjustmentSubmission.SourceId().equalTo(liabilityAdjustments.get(0).getIPAYROLLTXID())));
        assertEquals("company adjustment submission exists", 0, companyAdjustmentSubmissions.size());

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PositiveCobraReceived);
        assertEquals("cobra company event not created", 1, companyEvents.size());
        assertEquals("cobra event detail missing", 1, companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.Details).size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustment_VoidCobra() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        // add cobra
        List<IPITEM> cobra = new ArrayList<IPITEM>();
        cobra.add(OFXRequestGenerator.generateCobraPITEM());

        // generate 1 employee liability adjustment for cobra
        List<IPAYROLLTX> liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        int linesTotal = 0;
        for (IPITEM payrollItem : cobra) {
            linesTotal += Integer.parseInt(payrollItem.getIPITEMID());
            itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                    new SpcfMoney(SpcfDecimal.createInstance("-" + payrollItem.getIPITEMID())),
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
                cobra,
                null,
                null,
                liabilityAdjustments,
                null,
                null,
                null);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(adjustmentOfx, true);
        assertNotNull("Response", response);
        assertNull( response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

        // void the cobra adjustment
        liabilityAdjustments.get(0).setIVOID("Y");
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
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTX().size());
        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrollrs.getIPAYROLLTX().get(0), ipayrollrs.getIPAYROLLTX().get(0).getIDTPAYPDEND());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTXMOD(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustment_DeleteCobra() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        // add cobra
        List<IPITEM> cobra = new ArrayList<IPITEM>();
        cobra.add(OFXRequestGenerator.generateCobraPITEM());

        // generate 1 employee liability adjustment for cobra
        List<IPAYROLLTX> liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        int linesTotal = 0;
        for (IPITEM payrollItem : cobra) {
            linesTotal += Integer.parseInt(payrollItem.getIPITEMID());
            itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                    new SpcfMoney(SpcfDecimal.createInstance("-" + payrollItem.getIPITEMID())),
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
                cobra,
                null,
                null,
                liabilityAdjustments,
                null,
                null,
                null);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(adjustmentOfx, true);
        assertNotNull("Response", response);
        assertNull( response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        OFXAssert.assertPayrolls(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        OFXAssert.assertPayrollTransactions(adjustmentOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), company);
        PayrollServices.rollbackUnitOfWork();

        // void the cobra adjustment
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
                                                                   null,
                                                                   new ArrayList<String>(Arrays.asList(liabilityAdjustments.get(0).getIPAYROLLTXID())),
                                                                   null);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        response = QBDTTestHelper.submitQBDTRequest(adjustmentOfx, true);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTX().size());
        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrollrs.getIPAYROLLTX().get(0), ipayrollrs.getIPAYROLLTX().get(0).getIDTPAYPDEND());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = Application.find(CompanyAdjustmentSubmission.class,
                                                                                                     CompanyAdjustmentSubmission.Company().equalTo(company)
                                                                                                             .And(CompanyAdjustmentSubmission.SourceId().equalTo(liabilityAdjustments.get(0).getIPAYROLLTXID())));
        assertEquals("company adjustments", 1, companyAdjustmentSubmissions.size());
        CompanyAdjustmentSubmission companyAdjustmentSubmission = companyAdjustmentSubmissions.get(0);
        assertTrue("adjustment void", companyAdjustmentSubmission.isVoid());
        assertTrue("adjustment deleted", companyAdjustmentSubmission.getQbdtTransactionInfo().getIsDeleted());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testLiabilityAdjustmentRollsIntoVoid() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        Date paycheckDate =new Date("01/31/2011");
        List<IPAYROLLRUN> ipayrollruns = QBDTTestHelper.submitPayroll(company, ofx, paycheckDate, paycheckDate, paycheckDate);
        // void all of the paychecks
        for (IPAYROLLRUN ipayrollrun : ipayrollruns) {
            for (Iterator<IPAYCHK> iterator = ipayrollrun.getIPAYCHK().iterator(); iterator.hasNext();) {
                IPAYCHK ipaychk = iterator.next();
                ipaychk.setIVOID("Y");
                ipayrollrun.getIPAYCHKMOD().add(ipaychk);
                iterator.remove();
            }
        }

        List<IPAYROLLTX> liabilityAdjustments = new ArrayList<IPAYROLLTX>();
        List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
        for (IPITEM payrollItem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            itxlines.add(OFXRequestGenerator.generateTransactionLine(null,
                    new SpcfMoney(SpcfDecimal.createInstance(QBOFX.mapOFXStringToBoolean(payrollItem.getIISEMP()) ? "-" + "0.03" : "0.03")),
                    "Class" + payrollItem.getIPITEMID(),
                    false,
                    "Memo" + payrollItem.getIPITEMID(),
                    payrollItem.getIPITEMID(),
                    null,
                    null));
        }

        liabilityAdjustments.add(OFXRequestGenerator.generatePayrollTransaction("Account" + 1,
                                                                                new SpcfMoney(SpcfDecimal.createInstance(1)),
                                                                                "" + 1,
                                                                                new Date("01/01/2011"),
                                                                                new Date("01/01/2011"),
                                                                                null,
                                                                                "Memo " + 1,
                                                                                "Agency" + 1,
                                                                                true,
                                                                                QBOFX.OFXPayrollTransactionTransactionType.LIABADJ,
                                                                                "Ref" + 1,
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
                ipayrollruns);
        adjustmentOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(adjustmentOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull("Payroll response", ipayrollrs);

        // one mod for the voids, and zero new liab checks
        assertEquals("liability checks", 1, ipayrollrs.getIPAYROLLTXMOD().size());
        assertEquals("liability checks", 0, ipayrollrs.getIPAYROLLTX().size());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);

        DomainEntitySet<PayrollRun> domainPayrollRuns = PayrollRun.findPayrollRuns(company);
        assertEquals(1, domainPayrollRuns.size());
        PayrollRun payrollRun = domainPayrollRuns.get(0);
        for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltxmod : ipayrollrs.getIPAYROLLTXMOD()) {
            assertEquals("account name", companyBankAccount.getSourceBankAccountName(), ipayrolltxmod.getIACCTNAME());
            assertEquals("cleared", "0", ipayrolltxmod.getICLEARED());
            assertEquals("on service", "Y", ipayrolltxmod.getIONSERVICE());
            assertEquals("void", "N", ipayrolltxmod.getIVOID());
            assertEquals("memo", QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK, ipayrolltxmod.getIMEMO());
            assertNotNull("id", ipayrolltxmod.getIPAYROLLTXID());
            assertEquals("period end date", QBOFX.getDTTXResponse(SpcfUtils.convertSpcfCalendarToDate(payrollRun.getPaycheckDate())), ipayrolltxmod.getIDTPAYPDEND());

            FinancialTransaction erDebit;
            assertEquals("name", QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, ipayrolltxmod.getINAME());
            DomainEntitySet<FinancialTransaction> erTaxDebit = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit));
            assertNotNull("er debit", erTaxDebit.get(0));
            erDebit = erTaxDebit.get(0);
            assertEquals("transaction date", QBOFX.getDTTXResponse(SpcfUtils.convertSpcfCalendarToDate(erDebit.getSettlementDate())), ipayrolltxmod.getIDTTX());

            // each transaction line should have 0.03 for the adjustment
            SpcfDecimal total = SpcfMoney.ZERO;
            for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltxmod.getITXLINE()) {
                if(itxline.getIISDD() == null && itxline.getIPITEMID() != null) {
                    assertEquals("amount", "$0.03", itxline.getIAMT());
                    total = total.add(SpcfDecimal.createInstance(0.03));
                    if(itxline.getIMEMO() != null) {
                        assertEquals("memo", QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK, itxline.getIMEMO());
                    }
                } else {
                    SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                    total = total.add(amount);
                    if(itxline.getIMEMO() != null && itxline.getIISDD() != null) {
                        assertEquals("memo", QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK, itxline.getIMEMO());
                    }
                }
            }

            Assert.assertEquals("amount", "$-" + total, ipayrolltxmod.getIAMT());
        }
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testLiabilityCheck_ValueTooLarge() throws Exception {
        String psid = "123456789";

        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        OFX liabilityCheckOFX = new OFX();
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
                    new SpcfMoney("1234567891234.00"),
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
        liabilityCheckOFX.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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
        liabilityCheckOFX.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTTestHelper.submitQBDTRequestStringResponse(liabilityCheckOFX, false);
    }

}
