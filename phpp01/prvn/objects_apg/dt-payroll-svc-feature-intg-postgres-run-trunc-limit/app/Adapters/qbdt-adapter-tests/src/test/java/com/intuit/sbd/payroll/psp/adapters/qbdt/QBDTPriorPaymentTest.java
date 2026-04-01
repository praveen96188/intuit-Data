package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.MockSocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.SocketManagerFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEEQuarterlyTotals;
import com.intuit.sbd.payroll.psp.common.MalformedOFXException;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OFXToJavaMappingError;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRQ;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.request.ITXLINE;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import javax.transaction.Transaction;
import java.io.IOException;
import java.util.Iterator;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

public class QBDTPriorPaymentTest {

    @BeforeClass
    public static void beforeClass() {
        SocketManagerFactory.setInstanceClass(MockSocketManager.class);
    }

    @AfterClass
    public static void afterClass() {
        SocketManagerFactory.setInstanceClass(null);
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 12, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testModToNewLiabChkAndPriorPmt() throws Exception {
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file_9135.xml"));
        OFX requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        IPAYROLLRQ ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 14, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 0, ipayrollrq.getIPAYROLLTX().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 0, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 14, ipayrollrq.getIPAYROLLTX().size());

        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Jun10_OFX.xml"));
        requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 2, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 1, ipayrollrq.getIPAYROLLTX().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 2, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 1, ipayrollrq.getIPAYROLLTX().size());

        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_tax_dd_1.xml"));
        requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 5, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 34, ipayrollrq.getIPAYROLLTX().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 0, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 39, ipayrollrq.getIPAYROLLTX().size());

        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balancefile_hpde1.xml"));
        requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 8, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 31, ipayrollrq.getIPAYROLLTX().size());
        assertEquals("", 0, ipayrollrq.getIEMP().size());
        assertEquals("", 15, ipayrollrq.getIEMPMOD().size());
        assertEquals("", 25, ipayrollrq.getIPITEM().size());
        assertEquals("", 0, ipayrollrq.getIPITEMMOD().size());
        assertEquals("", 14, ipayrollrq.getIPAYROLLRUN().size());
        assertEquals("", 0, ipayrollrq.getIPAYROLLTXDELID().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 2, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 37, ipayrollrq.getIPAYROLLTX().size());
        assertEquals("", 0, ipayrollrq.getIEMP().size());
        assertEquals("", 15, ipayrollrq.getIEMPMOD().size());
        assertEquals("", 25, ipayrollrq.getIPITEM().size());
        assertEquals("", 0, ipayrollrq.getIPITEMMOD().size());
        assertEquals("", 14, ipayrollrq.getIPAYROLLRUN().size());
        assertEquals("", 0, ipayrollrq.getIPAYROLLTXDELID().size());
    }

    @Test
    public void testModToNewLiabChkAndPriorPmtHPDE() throws Exception {

        String psid = "448034658";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 4, 12, 12, 57, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "273541938", false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 4, 13, 9, 22, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.enrollEFTPS(company);
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file_9135.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        OFX requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        IPAYROLLRQ ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 14, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 0, ipayrollrq.getIPAYROLLTX().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 0, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 14, ipayrollrq.getIPAYROLLTX().size());
        SpcfDecimal amount = getTotalPriorPayment(requestOfx, company);
        Application.beginUnitOfWork();
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.Company().equalTo(company)
                                               .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold, PaymentStatus.Executed).And(MoneyMovementTransaction.PaymentTemplate().isNotNull()).And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.HPDE))));
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, query);
        assertEquals("MMT count ", 18, mmts.size());
        SpcfDecimal sum = SpcfMoney.ZERO;
        for (MoneyMovementTransaction mmt : mmts) {
            sum = sum.add(mmt.getMoneyMovementTransactionAmount());
        }
        assertEquals("MMT amount ", amount, sum);
        Application.rollbackUnitOfWork();

    }

    @Test
    public void testModToNewLiabChkAndPriorPmtHPDE_ofxhasModAndNew() throws Exception {

        String psid = "105868803";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014, 8, 19, 15, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "273541938", false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014, 8, 20, 20, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.enrollEFTPS(company);
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balancefile_hpde1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        IPAYROLLRQ ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 8, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 31, ipayrollrq.getIPAYROLLTX().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 2, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 37, ipayrollrq.getIPAYROLLTX().size());
        SpcfDecimal amount = getTotalPriorPayment(requestOfx, company);

        Application.beginUnitOfWork();
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.Company().equalTo(company)
                                               .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold, PaymentStatus.Executed).And(MoneyMovementTransaction.PaymentTemplate().isNotNull()).And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.HPDE))));
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, query);
        assertEquals("MMT count ", 12, mmts.size());
        SpcfDecimal sum = SpcfMoney.ZERO;
        for (MoneyMovementTransaction mmt : mmts) {
            sum = sum.add(mmt.getMoneyMovementTransactionAmount());
        }
        assertEquals("MMT amount ", amount, sum);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testHPDEPSP_10814() throws Exception {

        String psid = "448034710";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 4, 20, 15, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "273541938", false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 4, 27, 9, 14, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.enrollEFTPS(company);
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file_PSP_10814.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        IPAYROLLRQ ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 5, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 0, ipayrollrq.getIPAYROLLTX().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 2, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 3, ipayrollrq.getIPAYROLLTX().size());
        SpcfDecimal amount = getTotalPriorPayment(requestOfx, company);

        Application.beginUnitOfWork();
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.Company().equalTo(company)
                                               .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold, PaymentStatus.Executed).And(MoneyMovementTransaction.PaymentTemplate().isNotNull()).And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.HPDE))));
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, query);
        assertEquals("MMT count ", 3, mmts.size());
        SpcfDecimal sum = SpcfMoney.ZERO;
        for (MoneyMovementTransaction mmt : mmts) {
            sum = sum.add(mmt.getMoneyMovementTransactionAmount());
        }
        assertEquals("MMT amount ", amount, sum);
        Application.rollbackUnitOfWork();
    }
    @Test
    public void testHPDEPSP_10687() throws Exception {

        String psid = "106608938";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 7, 20, 15, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "273541938", false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 7, 28, 10, 57, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.enrollEFTPS(company);
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file_PSP_10687.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        IPAYROLLRQ ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 146, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 0, ipayrollrq.getIPAYROLLTX().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 2, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 144, ipayrollrq.getIPAYROLLTX().size());
        SpcfDecimal amount = getTotalPriorPayment(requestOfx, company);

        Application.beginUnitOfWork();
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.Company().equalTo(company)
                                               .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold, PaymentStatus.Executed).And(MoneyMovementTransaction.PaymentTemplate().isNotNull()).And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.HPDE))));
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, query);
        assertEquals("MMT count ", 78, mmts.size());
        SpcfDecimal sum = SpcfMoney.ZERO;
        for (MoneyMovementTransaction mmt : mmts) {
            sum = sum.add(mmt.getMoneyMovementTransactionAmount());
        }
        assertEquals("MMT amount ", amount, sum);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testHPDEPSP_9606() throws Exception {

        String psid = "106278742";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 5, 6, 9, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "273541938", true, ServiceCode.DirectDeposit);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/diy_file_9606_1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.addTaxService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 5, 6, 10, 44, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.enrollEFTPS(company);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file_9606.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        IPAYROLLRQ ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 10, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 44, ipayrollrq.getIPAYROLLTX().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 0, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 54, ipayrollrq.getIPAYROLLTX().size());
        SpcfDecimal amount = getTotalPriorPayment(requestOfx, company);

        Application.beginUnitOfWork();
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.Company().equalTo(company)
                                               .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold, PaymentStatus.Executed).And(MoneyMovementTransaction.PaymentTemplate().isNotNull()).And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.HPDE))));
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, query);
        assertEquals("MMT count ", 29, mmts.size());
        SpcfDecimal sum = SpcfMoney.ZERO;
        for (MoneyMovementTransaction mmt : mmts) {
            sum = sum.add(mmt.getMoneyMovementTransactionAmount());
        }
        assertEquals("MMT amount ", amount, sum);
        Application.rollbackUnitOfWork();
        DataLoadServices.runMMTJobs(1);
        DataLoadServices.setPSPDate(2015, 5, 10);

        Application.beginUnitOfWork();
        DomainEntitySet<CompanyLaw> companyLaw = CompanyLaw.findAllCompanyLaws(company);
        for (CompanyLaw pCompanyLaw : companyLaw) {
            pCompanyLaw.setIsArchived(false);
        }
        Application.commitUnitOfWork();
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess, "-mode:" + CalculateEEQuarterlyTotals.Mode.UPDATE.toString());
        BatchJobManager.runJob(BatchJobType.EmployeeW2TotalsCalculationProcessor, "-year:" + 2015, "-companyId:" + company.getSourceCompanyId());

    }

    @Test
    public void testHPDEPSP_9606_void_Payroll() throws IOException, MalformedOFXException, OFXToJavaMappingError {

        String psid = "106278742";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 5, 6, 9, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "273541938", true, ServiceCode.DirectDeposit);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/diy_file_9606_1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.addTaxService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 5, 6, 10, 44, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.enrollEFTPS(company);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file_9606.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX requestOfx = OFXManager.ofxRequestToJava(OFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        IPAYROLLRQ ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        assertEquals("", 10, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 44, ipayrollrq.getIPAYROLLTX().size());
        AssistedRequestProcessor.moveModToNewForLIABCHKandPRIORPMT(ipayrollrq, QBOFX.isOFXBalanceFile(OFX));
        assertEquals("", 0, ipayrollrq.getIPAYROLLTXMOD().size());
        assertEquals("", 54, ipayrollrq.getIPAYROLLTX().size());
        SpcfDecimal amount = getTotalPriorPayment(requestOfx, company);

        Application.beginUnitOfWork();
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.Company().equalTo(company)
                                               .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold, PaymentStatus.Executed).And(MoneyMovementTransaction.PaymentTemplate().isNotNull()).And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.HPDE))));
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, query);
        assertEquals("MMT count ", 29, mmts.size());
        SpcfDecimal sum = SpcfMoney.ZERO;
        for (MoneyMovementTransaction mmt : mmts) {
            sum = sum.add(mmt.getMoneyMovementTransactionAmount());
        }
        Application.rollbackUnitOfWork();
        Application.beginUnitOfWork();
        Expression<PayrollRun> queryPayroll = new Query<PayrollRun>().Where(PayrollRun.PayrollRunStatus().in(PayrollStatus.Superseded));
        DomainEntitySet<PayrollRun> pr = Application.find(PayrollRun.class, queryPayroll);
        Application.rollbackUnitOfWork();
        assertEquals("MMT amount ", amount, sum);

        DataLoadServices.runMMTJobs(1);
        DataLoadServices.setPSPDate(2015, 5, 10);

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(pr.get(0).getSourcePayRunId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();
    }
    /**
     *
     * @param ofx
     * @param company
     * @return
     */
    private static SpcfMoney getTotalPriorPayment(OFX ofx, Company company) {
        IPAYROLLRQ ipayrollrq = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        Iterator ipayrolltxns = ipayrollrq.getIPAYROLLTX().iterator();
        SpcfMoney amount = SpcfMoney.ZERO;
        while (ipayrolltxns.hasNext()) {
            SpcfMoney amount1 = SpcfMoney.ZERO;
            IPAYROLLTX ipayroll = (IPAYROLLTX) ipayrolltxns.next();
            QBOFX.OFXPayrollTransactionTransactionType transactionType = QBOFX.OFXPayrollTransactionTransactionType.valueOf(ipayroll.getIPAYROLLTXTYPE().trim());
            if (QBOFX.OFXPayrollTransactionTransactionType.LIABCHK.equals(transactionType)) {
                for (ITXLINE iptx : ipayroll.getITXLINE()) {
                    if (iptx.getIPITEMID() != null) {
                        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, iptx.getIPITEMID());
                        if (companyLaw != null && !(iptx.getIAMT() == null || "".equals(iptx.getIAMT().trim()))) {
                            amount1 = (SpcfMoney) amount1.add(QBOFX.mapOFXStringToMoney(iptx.getIAMT()));
                        }
                    }

                }
            }
            if (QBOFX.OFXPayrollTransactionTransactionType.PRIORPMT.equals(transactionType)) {
                for (ITXLINE iptx : ipayroll.getITXLINE()) {
                    if (iptx.getIPITEMID() != null) {
                        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, iptx.getIPITEMID());
                        if (companyLaw != null && !(iptx.getIAMT() == null || "".equals(iptx.getIAMT().trim()))) {
                            amount1 = (SpcfMoney) amount1.add(QBOFX.mapOFXStringToMoney(iptx.getIAMT()));
                        }
                    }
                }
            }
            amount = (SpcfMoney) amount.add(amount1);
        }
        return amount;
    }

}
